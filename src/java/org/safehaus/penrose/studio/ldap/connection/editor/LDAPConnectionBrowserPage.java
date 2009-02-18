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
import org.eclipse.jface.window.Window;
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
import org.safehaus.penrose.ldap.connection.LDAPConnectionClient;
import org.safehaus.penrose.schema.ObjectClass;
import org.safehaus.penrose.schema.Schema;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.ldap.connection.wizard.LDAPSourceWizard;
import org.safehaus.penrose.studio.connection.editor.ConnectionEditorPage;
import org.safehaus.penrose.studio.dialog.ErrorDialog;

import java.util.*;

/**
 * @author Endi S. Dewata
 */
public class LDAPConnectionBrowserPage extends ConnectionEditorPage implements TreeListener {

    Tree tree;
    Table table;

    LDAPConnectionClient connectionClient;

    public LDAPConnectionBrowserPage(LDAPConnectionEditor editor) throws Exception {
        super(editor, "BROWSER", "Browser");

        connectionClient = new LDAPConnectionClient(
                editor.getServer().getClient(),
                partitionName,
                editor.getConnectionName()
        );
    }

    public void createFormContent(IManagedForm managedForm) {
        super.createFormContent(managedForm);

        ScrolledForm form = managedForm.getForm();

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Actions");
        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Control actionsSection = createActionsSection(section);
        section.setClient(actionsSection);

        section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Entries");
        section.setLayoutData(new GridData(GridData.FILL_BOTH));

        Control entriesSection = createEntriesSection(section);
        section.setClient(entriesSection);

        section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Attributes");
        section.setLayoutData(new GridData(GridData.FILL_BOTH));

        Control attributesSection = createAttributesSection(section);
        section.setClient(attributesSection);

        update();
    }

    public Composite createActionsSection(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new RowLayout());

        Hyperlink refreshSchema = toolkit.createHyperlink(composite, "Refresh", SWT.NONE);

        refreshSchema.addHyperlinkListener(new HyperlinkAdapter() {
            public void linkActivated(HyperlinkEvent event) {
                update();
            }
        });

        return composite;
    }

    public Composite createEntriesSection(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout());

        tree = toolkit.createTree(composite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.heightHint = 100;
        tree.setLayoutData(gd);

        tree.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    table.removeAll();

                    TreeItem ti = tree.getSelection()[0];
                    DN dn = (DN)ti.getData();
                    if (dn == null) return;

                    SearchResult entry = connectionClient.find(dn);
                    showAttributes(entry);

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
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
                    Schema schema = connectionClient.getSchema();

                    TreeItem treeItem = tree.getSelection()[0];
                    DN dn = (DN)treeItem.getData();
                    SearchResult entry = connectionClient.find(dn);

                    Collection<String> attributeNames = getAttributeNames(schema, entry);

                    LDAPSourceWizard wizard = new LDAPSourceWizard(
                            partitionName,
                            connectionConfig,
                            dn.toString(),
                            "(objectClass=*)",
                            "OBJECT",
                            attributeNames
                    );
                    wizard.setServer(editor.getServer());

                    WizardDialog dialog = new WizardDialog(editor.getSite().getShell(), wizard);
                    dialog.setPageSize(600, 300);
                    int rc = dialog.open();

                    if (rc == Window.CANCEL) return;

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
                    TreeItem treeItem = tree.getSelection()[0];
                    DN baseDn = (DN)treeItem.getData();

                    LDAPSourceWizard wizard = new LDAPSourceWizard(
                            partitionName,
                            connectionConfig,
                            baseDn.toString(),
                            "(objectClass=*)",
                            "ONELEVEL",
                            new ArrayList<String>()
                    );
                    wizard.setServer(editor.getServer());

                    WizardDialog dialog = new WizardDialog(editor.getSite().getShell(), wizard);
                    dialog.setPageSize(600, 300);
                    int rc = dialog.open();

                    if (rc == Window.CANCEL) return;

                    PenroseStudio penroseStudio = PenroseStudio.getInstance();
                    penroseStudio.notifyChangeListeners();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        mi = new MenuItem(menu, SWT.PUSH);
        mi.setText("Create source from nodes with the same object classes...");

        mi.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    Schema schema = connectionClient.getSchema();
                    
                    TreeItem treeItem = tree.getSelection()[0];
                    DN baseDn = (DN)treeItem.getData();
                    DN parentDn = baseDn.getParentDn();

                    SearchResult entry = connectionClient.find(baseDn);
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

                    LDAPSourceWizard wizard = new LDAPSourceWizard(
                            partitionName,
                            connectionConfig,
                            parentDn.toString(),
                            filter,
                            "ONELEVEL",
                            attributeNames
                    );
                    wizard.setServer(editor.getServer());

                    WizardDialog dialog = new WizardDialog(editor.getSite().getShell(), wizard);
                    dialog.setPageSize(600, 300);
                    int rc = dialog.open();

                    if (rc == Window.CANCEL) return;

                    PenroseStudio penroseStudio = PenroseStudio.getInstance();
                    penroseStudio.notifyChangeListeners();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
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

    public Composite createAttributesSection(final Composite parent) {

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

    public void showAttributes(SearchResult entry) throws Exception {

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

    public void update() {
        try {
            tree.removeAll();
            table.removeAll();

            DN rootDn = new DN();
            SearchResult root = connectionClient.find(rootDn);
            if (root == null) return;

            TreeItem item = new TreeItem(tree, SWT.NONE);
            item.setText("Root");
            item.setData(rootDn);

            expand(item);

            showAttributes(root);

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
            expand(item);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }

    public void expand(TreeItem item) throws Exception {

        for (TreeItem ti : item.getItems()) {
            ti.dispose();
        }

        try {
            DN baseDn = (DN)item.getData();

            if (baseDn.isEmpty()) {

                SearchRequest req = new SearchRequest();
                req.setScope(SearchRequest.SCOPE_BASE);
                req.setAttributes(new String[] { "*", "+" });

                SearchResponse response = new SearchResponse();

                SearchResponse res = connectionClient.search(req, response);
                SearchResult rootDse = res.next();

                Attributes attributes = rootDse.getAttributes();
                Attribute attribute = attributes.get("namingContexts");

                for (Object value : attribute.getValues()) {
                    String dn = (String)value;

                    TreeItem ti = new TreeItem(item, SWT.NONE);
                    ti.setText(dn);
                    ti.setData(new DN(dn));

                    new TreeItem(ti, SWT.NONE);
                }

            } else {

                SearchRequest req = new SearchRequest();
                req.setDn(baseDn);
                req.setScope(SearchRequest.SCOPE_ONE);

                SearchResponse response = new SearchResponse();

                response = connectionClient.search(req, response);

                while (response.hasNext()) {
                    SearchResult result = response.next();
                    DN dn = result.getDn();
                    String label = dn.getRdn().toString();

                    TreeItem ti = new TreeItem(item, SWT.NONE);
                    ti.setText(label);
                    ti.setData(dn);

                    new TreeItem(ti, SWT.NONE);
                }
            }

        } catch (Exception e) {
            TreeItem ti = new TreeItem(item, SWT.NONE);
            ti.setText("Error: "+e.getMessage());
        }
    }
}