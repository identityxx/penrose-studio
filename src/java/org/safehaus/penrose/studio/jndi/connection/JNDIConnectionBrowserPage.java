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
package org.safehaus.penrose.studio.jndi.connection;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.ui.forms.widgets.*;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.safehaus.penrose.schema.Schema;
import org.safehaus.penrose.schema.ObjectClass;
import org.safehaus.penrose.studio.jndi.connection.JNDISourceWizard;
import org.safehaus.penrose.studio.connection.editor.ConnectionEditorPage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.ldap.LDAPClient;
import org.safehaus.penrose.ldap.DN;

import javax.naming.directory.SearchResult;
import javax.naming.directory.Attributes;
import javax.naming.directory.Attribute;
import javax.naming.NamingEnumeration;
import java.util.*;
import java.io.StringWriter;
import java.io.PrintWriter;

/**
 * @author Endi S. Dewata
 */
public class JNDIConnectionBrowserPage extends ConnectionEditorPage implements TreeListener {

    Tree tree;
    Table table;

    Schema schema;

    public JNDIConnectionBrowserPage(JNDIConnectionEditor editor) {
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
                try {
                    TreeItem ti = tree.getSelection()[0];
                    SearchResult entry = (SearchResult)ti.getData();

                    LDAPClient client = new LDAPClient(connectionConfig.getParameters());
                    showEntry(client, entry);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
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
                try {
                    LDAPClient client = new LDAPClient(connectionConfig.getParameters());
                    Schema schema = client.getSchema();

                    TreeItem treeItem = tree.getSelection()[0];
                    SearchResult entry = (SearchResult)treeItem.getData();
                    String baseDn = entry.getName();

                    Collection attributeNames = getAttributeNames(schema, entry);

                    JNDISourceWizard wizard = new JNDISourceWizard(
                            client,
                            partitionConfig,
                            connectionConfig,
                            baseDn,
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
                }
            }
        });

        mi = new MenuItem(menu, SWT.PUSH);
        mi.setText("Create source from the children of this node...");

        mi.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    LDAPClient client = new LDAPClient(connectionConfig.getParameters());

                    TreeItem treeItem = tree.getSelection()[0];
                    SearchResult entry = (SearchResult)treeItem.getData();
                    String baseDn = entry.getName();

                    JNDISourceWizard wizard = new JNDISourceWizard(
                            client,
                            partitionConfig,
                            connectionConfig,
                            baseDn,
                            "(objectClass=*)",
                            "ONELEVEL",
                            new ArrayList()
                    );
                    wizard.setProject(editor.getProject());

                    WizardDialog dialog = new WizardDialog(editor.getSite().getShell(), wizard);
                    dialog.setPageSize(600, 300);
                    dialog.open();

                    PenroseStudio penroseStudio = PenroseStudio.getInstance();
                    penroseStudio.notifyChangeListeners();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        mi = new MenuItem(menu, SWT.PUSH);
        mi.setText("Create source from nodes with the same object classes...");

        mi.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    LDAPClient client = new LDAPClient(connectionConfig.getParameters());
                    Schema schema = client.getSchema();

                    TreeItem treeItem = tree.getSelection()[0];
                    SearchResult entry = (SearchResult)treeItem.getData();
                    DN baseDn = new DN(entry.getName());
                    DN parentDn = baseDn.getParentDn();

                    Attribute oc = entry.getAttributes().get("objectClass");
                    int counter = 0;
                    StringBuilder sb = new StringBuilder();
                    for (NamingEnumeration en=oc.getAll(); en.hasMore(); ) {
                        String value = (String)en.next();
                        if ("top".equalsIgnoreCase(value)) continue;
                        sb.append("(objectClass=");
                        sb.append(value);
                        sb.append(")");
                        counter++;
                    }

                    String filter;
                    if (counter > 1) {
                        filter = "(&"+sb+")";
                    } else {
                        filter = sb.toString();
                    }

                    Collection attributeNames = getAttributeNames(schema, entry);

                    JNDISourceWizard wizard = new JNDISourceWizard(
                            client,
                            partitionConfig,
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
                }
            }
        });

        return composite;
    }

    public Collection getAttributeNames(Schema schema, SearchResult entry) throws Exception {
        Map<String,ObjectClass> map = new TreeMap<String,ObjectClass>();

        Attributes attributes = entry.getAttributes();

        Attribute objectClass = attributes.get("objectClass");
        for (NamingEnumeration en=objectClass.getAll(); en.hasMore(); ) {
            String ocName = (String)en.next();
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

        for (NamingEnumeration i=attributes.getAll(); i.hasMore(); ) {
            Attribute at = (Attribute)i.next();
            String name = at.getID();
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

    public void showEntry(LDAPClient client, SearchResult entry) throws Exception {
        table.removeAll();

        Attributes attributes = entry.getAttributes();
        for (NamingEnumeration i=attributes.getAll(); i.hasMore(); ) {
            Attribute attribute = (Attribute)i.next();
            String name = attribute.getID();

            for (NamingEnumeration j=attribute.getAll(); j.hasMore(); ) {
                Object value = j.next();
                String label = value instanceof byte[] ? "(binary)" : value.toString();

                TableItem ti = new TableItem(table, SWT.NONE);
                ti.setText(0, name);
                ti.setText(1, label);
                ti.setData(value);
            }
        }
    }

    public void refresh() {
        try {
            tree.removeAll();

            LDAPClient client = new LDAPClient(connectionConfig.getParameters());
            SearchResult root = client.getEntry("");
            if (root == null) return;

            TreeItem item = new TreeItem(tree, SWT.NONE);

            DN suffix = client.getSuffix();
            if (suffix.isEmpty()) {
                item.setText("Root DSE");
            } else {
                item.setText(suffix.toString());
            }
            item.setData(root);

            Collection<SearchResult> results = client.getChildren("");

            for (SearchResult entry : results) {
                String dn = entry.getName();

                TreeItem it = new TreeItem(item, SWT.NONE);
                it.setText(dn);
                it.setData(entry);

                new TreeItem(it, SWT.NONE);
            }

            showEntry(client, root);

            item.setExpanded(true);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }

    public void treeCollapsed(TreeEvent event) {
    }

    public void treeExpanded(TreeEvent event) {
        try {
            if (event.item == null) return;

            TreeItem item = (TreeItem)event.item;
            SearchResult entry = (SearchResult)item.getData();
            String baseDn = entry.getName();
            //log.debug("Expanding "+baseDn);

            TreeItem items[] = item.getItems();
            for (TreeItem item1 : items) {
                item1.dispose();
            }

            LDAPClient client = new LDAPClient(connectionConfig.getParameters());
            Collection<SearchResult> results = client.getChildren(baseDn);

            for (SearchResult en : results) {
                DN dn = new DN(en.getName());
                String rdn = dn.getRdn().toString();

                TreeItem it = new TreeItem(item, SWT.NONE);
                it.setText(rdn);
                it.setData(en);

                new TreeItem(it, SWT.NONE);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }
}