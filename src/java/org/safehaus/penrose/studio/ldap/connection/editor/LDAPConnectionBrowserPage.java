/**
 * Copyright (c) 2000-2006, Identyx Corporation.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.safehaus.penrose.studio.ldap.connection.editor;

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.safehaus.penrose.ldap.*;
import org.safehaus.penrose.schema.ObjectClass;
import org.safehaus.penrose.schema.Schema;
import org.safehaus.penrose.schema.SchemaUtil;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.jndi.connection.JNDISourceWizard;
import org.safehaus.penrose.studio.connection.editor.ConnectionEditorPage;
import org.safehaus.penrose.studio.dialog.ErrorDialog;

import java.util.*;

/**
 * @author Endi S. Dewata
 */
public class LDAPConnectionBrowserPage extends ConnectionEditorPage implements TreeListener {

    Tree tree;
    Table table;

    Schema schema;

    public LDAPConnectionBrowserPage(LDAPConnectionEditor editor) {
        super(editor, "BROWSER", "  Browser  ");
    }

    public void createFormContent(IManagedForm managedForm) {
        super.createFormContent(managedForm);

        ScrolledForm form = managedForm.getForm();

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

/*
        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        if (penroseStudio.isFreeware()) {
            Label label = toolkit.createLabel(body, PenroseStudio.FEATURE_NOT_AVAILABLE);
            label.setLayoutData(new GridData(GridData.FILL_BOTH));
            return;
        }
*/
        Section section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Actions");
        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Control actionsSection = createActionsSection(section);
        section.setClient(actionsSection);

        section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Directory Tree");
        section.setLayoutData(new GridData(GridData.FILL_BOTH));

        Control browserSection = createTreeSection(section);
        section.setClient(browserSection);

        section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Entry");
        section.setLayoutData(new GridData(GridData.FILL_BOTH));

        Control entrySection = createEntrySection(section);
        section.setClient(entrySection);

        refresh();
    }

    public Composite createActionsSection(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new RowLayout());

        Hyperlink refreshSchema = toolkit.createHyperlink(composite, "Refresh", SWT.NONE);

        refreshSchema.addHyperlinkListener(new HyperlinkAdapter() {
            public void linkActivated(HyperlinkEvent event) {
                refresh();
            }
        });

        return composite;
    }

    public Composite createTreeSection(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout());

        tree = toolkit.createTree(composite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.heightHint = 100;
        tree.setLayoutData(gd);

        tree.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                LDAPClient client = null;
                try {
                    TreeItem ti = tree.getSelection()[0];
                    DN dn = (DN)ti.getData();

                    client = new LDAPClient(connectionConfig.getParameters());
                    SearchResult entry = client.find(dn);
                    showEntry(entry);

                } catch (Exception e) {
                    log.error(e.getMessage(), e);

                } finally {
                    if (client != null) try { client.close(); } catch (Exception e) { log.error(e.getMessage(), e); }
                }
            }
        });

        tree.addTreeListener(this);

        Menu menu = new Menu(tree);
        tree.setMenu(menu);

        MenuItem mi = new MenuItem(menu, SWT.PUSH);
        mi.setText("Create source from this node...");

        mi.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                LDAPClient client = null;
                try {
                    client = new LDAPClient(connectionConfig.getParameters());

                    SchemaUtil schemaUtil = new SchemaUtil();
                    Schema schema = schemaUtil.getSchema(client);

                    TreeItem treeItem = tree.getSelection()[0];
                    DN dn = (DN)treeItem.getData();
                    SearchResult entry = client.find(dn);

                    Collection<String> attributeNames = getAttributeNames(schema, entry);

                    JNDISourceWizard wizard = new JNDISourceWizard(
                            client,
                            partitionName,
                            connectionConfig,
                            dn.toString(),
                            "(objectClass=*)",
                            "OBJECT",
                            attributeNames
                    );
                    wizard.setProject(editor.getProject());

                    WizardDialog dialog = new WizardDialog(editor.getSite().getShell(), wizard);
                    dialog.setPageSize(600, 300);
                    dialog.open();

                    PenroseStudio penroseStudio = PenroseStudio.getInstance();
                    penroseStudio.notifyChangeListeners();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);

                } finally {
                    if (client != null) try { client.close(); } catch (Exception e) { log.error(e.getMessage(), e); }
                }
            }
        });

        mi = new MenuItem(menu, SWT.PUSH);
        mi.setText("Create source from the children of this node...");

        mi.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                LDAPClient client = null;
                try {
                    client = new LDAPClient(connectionConfig.getParameters());

                    TreeItem treeItem = tree.getSelection()[0];
                    DN baseDn = (DN)treeItem.getData();

                    JNDISourceWizard wizard = new JNDISourceWizard(
                            client,
                            partitionName,
                            connectionConfig,
                            baseDn.toString(),
                            "(objectClass=*)",
                            "ONELEVEL",
                            new ArrayList<String>()
                    );
                    wizard.setProject(editor.getProject());

                    WizardDialog dialog = new WizardDialog(editor.getSite().getShell(), wizard);
                    dialog.setPageSize(600, 300);
                    dialog.open();

                    PenroseStudio penroseStudio = PenroseStudio.getInstance();
                    penroseStudio.notifyChangeListeners();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);

                } finally {
                    if (client != null) try { client.close(); } catch (Exception e) { log.error(e.getMessage(), e); }
                }
            }
        });

        mi = new MenuItem(menu, SWT.PUSH);
        mi.setText("Create source from nodes with the same object classes...");

        mi.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                LDAPClient client = null;
                try {
                    client = new LDAPClient(connectionConfig.getParameters());

                    SchemaUtil schemaUtil = new SchemaUtil();
                    Schema schema = schemaUtil.getSchema(client);
                    
                    TreeItem treeItem = tree.getSelection()[0];
                    DN baseDn = (DN)treeItem.getData();
                    DN parentDn = baseDn.getParentDn();

                    SearchResult entry = client.find(baseDn);
                    Attribute oc = entry.getAttributes().get("objectClass");
                    int counter = 0;
                    StringBuilder sb = new StringBuilder();
                    for (Object value : oc.getValues()) {
                        String objectClass = (String)value;
                        if ("top".equalsIgnoreCase(objectClass)) continue;
                        sb.append("(objectClass=");
                        sb.append(objectClass);
                        sb.append(")");
                        counter++;
                    }

                    String filter;
                    if (counter > 1) {
                        filter = "(&"+sb+")";
                    } else {
                        filter = sb.toString();
                    }

                    Collection<String> attributeNames = getAttributeNames(schema, entry);

                    JNDISourceWizard wizard = new JNDISourceWizard(
                            client,
                            partitionName,
                            connectionConfig,
                            parentDn.toString(),
                            filter,
                            "ONELEVEL",
                            attributeNames
                    );
                    wizard.setProject(editor.getProject());

                    WizardDialog dialog = new WizardDialog(editor.getSite().getShell(), wizard);
                    dialog.setPageSize(600, 300);
                    dialog.open();

                    PenroseStudio penroseStudio = PenroseStudio.getInstance();
                    penroseStudio.notifyChangeListeners();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);

                } finally {
                    if (client != null) try { client.close(); } catch (Exception e) { log.error(e.getMessage(), e); }
                }
            }
        });

        return composite;
    }

    public Collection<String> getAttributeNames(Schema schema, SearchResult entry) throws Exception {
        Map<String,ObjectClass> map = new TreeMap<String,ObjectClass>();

        Attributes attributes = entry.getAttributes();

        Attribute objectClass = attributes.get("objectClass");
        for (Object value : objectClass.getValues()) {
            String ocName = (String)value;
            schema.getAllObjectClasses(ocName, map);
        }

        Collection<String> attributeNames = new TreeSet<String>();

        for (ObjectClass oc : map.values()) {

            for (String atName : oc.getRequiredAttributes()) {
                attributeNames.add(atName);
            }

            for (String atName : oc.getOptionalAttributes()) {
                attributeNames.add(atName);
            }
        }

        for (Attribute at : attributes.getAll()) {
            String name = at.getName();
            if ("objectClass".equalsIgnoreCase(name)) continue;
            attributeNames.add(name);
        }

        return attributeNames;
    }

    public Composite createEntrySection(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout());

        table = toolkit.createTable(composite, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.heightHint = 100;
        table.setLayoutData(gd);

        TableColumn tc = new TableColumn(table, SWT.LEFT);
        tc.setText("Attribute Name");
        tc.setWidth(200);

        tc = new TableColumn(table, SWT.LEFT);
        tc.setText("Attribute Value");
        tc.setWidth(300);

        return composite;
    }

    public void showEntry(SearchResult entry) throws Exception {
        table.removeAll();

        Attributes attributes = entry.getAttributes();
        for (Attribute attribute : attributes.getAll()) {
            String name = attribute.getName();

            for (Object value : attribute.getValues()) {
                String label = value instanceof byte[] ? "(binary)" : value.toString();

                TableItem ti = new TableItem(table, SWT.NONE);
                ti.setText(0, name);
                ti.setText(1, label);
                ti.setData(value);
            }
        }
    }

    public void refresh() {
        LDAPClient client = null;
        try {
            tree.removeAll();

            client = new LDAPClient(connectionConfig.getParameters());

            DN emptyDn = new DN();
            SearchResult root = client.find(emptyDn);
            if (root == null) return;

            TreeItem item = new TreeItem(tree, SWT.NONE);

            item.setText("Root DSE");
            item.setData(emptyDn);

            Collection<SearchResult> results = client.findChildren("");

            for (SearchResult entry : results) {
                DN dn = entry.getDn();

                TreeItem it = new TreeItem(item, SWT.NONE);
                it.setText(dn.toString());
                it.setData(dn);

                new TreeItem(it, SWT.NONE);
            }

            showEntry(root);

            item.setExpanded(true);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);

        } finally {
            if (client != null) try { client.close(); } catch (Exception e) { log.error(e.getMessage(), e); }
        }
    }

    public void treeCollapsed(TreeEvent event) {
    }

    public void treeExpanded(TreeEvent event) {
        LDAPClient client = null;
        try {
            if (event.item == null) return;

            TreeItem item = (TreeItem)event.item;
            DN baseDn = (DN)item.getData();
            //log.debug("Expanding "+baseDn);

            TreeItem items[] = item.getItems();
            for (TreeItem item1 : items) {
                item1.dispose();
            }

            client = new LDAPClient(connectionConfig.getParameters());
            Collection<SearchResult> results = client.findChildren(baseDn);

            for (SearchResult en : results) {
                DN dn = en.getDn();

                TreeItem it = new TreeItem(item, SWT.NONE);
                it.setText(dn.getRdn().toString());
                it.setData(dn);

                new TreeItem(it, SWT.NONE);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        } finally {
            if (client != null) try { client.close(); } catch (Exception e) { log.error(e.getMessage(), e); }
        }
    }
}