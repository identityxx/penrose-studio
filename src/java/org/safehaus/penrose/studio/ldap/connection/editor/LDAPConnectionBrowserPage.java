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
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.core.runtime.IProgressMonitor;
import org.safehaus.penrose.ldap.*;
import org.safehaus.penrose.ldap.connection.LDAPConnectionClient;
import org.safehaus.penrose.schema.ObjectClass;
import org.safehaus.penrose.schema.Schema;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.ldap.connection.wizard.LDAPSourceWizard;
import org.safehaus.penrose.studio.ldap.connection.wizard.LDAPConnectionBrowserOptionsWizard;
import org.safehaus.penrose.studio.connection.editor.ConnectionEditorPage;
import org.safehaus.penrose.studio.dialog.ErrorDialog;

import java.util.*;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Endi S. Dewata
 */
public class LDAPConnectionBrowserPage extends ConnectionEditorPage {

    Tree tree;
    Table table;

    LDAPConnectionClient connectionClient;

    Long sizeLimit;

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

        Section directorySection = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        directorySection.setText("Directory");
        directorySection.setLayoutData(new GridData(GridData.FILL_BOTH));

        Control directoryControl = createDirectoryControl(directorySection);
        directorySection.setClient(directoryControl);

        Section entrySection = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        entrySection.setText("Entry");
        entrySection.setLayoutData(new GridData(GridData.FILL_BOTH));

        Control entryControl = createEntryControl(entrySection);
        entrySection.setClient(entryControl);

        reset();
    }

    public Composite createDirectoryControl(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        Composite leftControl = createDirectoryLeftControl(composite);
        leftControl.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite rightControl = createDirectoryRightControl(composite);
        GridData gd = new GridData(GridData.FILL_VERTICAL);
        gd.widthHint = 100;
        rightControl.setLayoutData(gd);

        return composite;
    }

    public Composite createDirectoryLeftControl(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);

        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);

        tree = toolkit.createTree(composite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.heightHint = 100;
        tree.setLayoutData(gd);

        tree.addTreeListener(new TreeAdapter() {
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
        });

        tree.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    TreeItem item = tree.getSelection()[0];
                    showEntry(item);

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

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

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
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

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
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

    public Composite createDirectoryRightControl(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);

        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);

        Button settingsButton = new Button(composite, SWT.PUSH);
        settingsButton.setText("Settings...");
        settingsButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        settingsButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    LDAPConnectionBrowserOptionsWizard wizard = new LDAPConnectionBrowserOptionsWizard();
                    wizard.setSizeLimit(sizeLimit);

                    IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                    WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
                    dialog.setPageSize(600, 300);
                    int rc = dialog.open();

                    if (rc == Window.CANCEL) return;

                    sizeLimit = wizard.getSizeLimit();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        return composite;
    }

    public Composite createEntryControl(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        Composite leftControl = createEntryLeftControl(composite);
        leftControl.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite rightControl = createEntryRightControl(composite);
        GridData gd = new GridData(GridData.FILL_VERTICAL);
        gd.widthHint = 100;
        rightControl.setLayoutData(gd);

        return composite;
    }

    public Composite createEntryLeftControl(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);

        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);

        table = toolkit.createTable(composite, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.heightHint = 100;
        table.setLayoutData(gd);

        TableColumn tc = new TableColumn(table, SWT.LEFT);
        tc.setText("Name");
        tc.setWidth(200);

        tc = new TableColumn(table, SWT.LEFT);
        tc.setText("Value");
        tc.setWidth(300);

        return composite;
    }

    public Composite createEntryRightControl(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);

        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);

        return composite;
    }

    public void reset() {
        tree.removeAll();
        table.removeAll();

        TreeItem item = new TreeItem(tree, SWT.NONE);
        item.setText("Root");
        item.setData(new DN());

        new TreeItem(item, SWT.NONE);
    }

    public void expand(TreeItem item) throws Exception {
        try {
            for (TreeItem ti : item.getItems()) ti.dispose();

            final DN baseDn = (DN)item.getData();
            final Collection<DN> results = new ArrayList<DN>();

            IProgressService progressService = PlatformUI.getWorkbench().getProgressService();

            progressService.busyCursorWhile(new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor) throws InvocationTargetException {
                    try {
                        monitor.beginTask("Retrieving data...", IProgressMonitor.UNKNOWN);

                        if (baseDn.isEmpty()) {

                            monitor.subTask("Searching Root DSE...");

                            SearchRequest request = new SearchRequest();
                            request.setScope(SearchRequest.SCOPE_BASE);
                            request.setAttributes(new String[] { "*", "+" });

                            SearchResponse response = new SearchResponse();

                            connectionClient.search(request, response);

                            monitor.worked(1);

                            monitor.subTask("Processing results...");

                            SearchResult rootDse = response.next();

                            Attributes attributes = rootDse.getAttributes();
                            Attribute attribute = attributes.get("namingContexts");

                            for (Object value : attribute.getValues()) {
                                String dn = (String)value;
                                results.add(new DN(dn));
                            }

                            monitor.worked(1);

                        } else {

                            monitor.subTask("Searching children of "+baseDn+"...");

                            SearchRequest request = new SearchRequest();
                            request.setDn(baseDn);
                            request.setScope(SearchRequest.SCOPE_ONE);
                            request.setAttributes(new String[] { "dn" });
                            if (sizeLimit != null) request.setSizeLimit(sizeLimit);

                            SearchResponse response = new SearchResponse();

                            connectionClient.search(request, response);

                            monitor.worked(1);

                            monitor.subTask("Processing results...");

                            while (response.hasNext()) {
                                SearchResult result = response.next();
                                DN dn = result.getDn();
                                results.add(dn);
                                monitor.worked(1);
                            }
                        }

                    } catch (Exception e) {
                        throw new InvocationTargetException(e);

                    } finally {
                        monitor.done();
                    }
                }
            });

            for (DN dn : results) {

                String label = baseDn.isEmpty() ? dn.toString() : dn.getRdn().toString();

                TreeItem ti = new TreeItem(item, SWT.NONE);
                ti.setText(label);
                ti.setData(dn);

                new TreeItem(ti, SWT.NONE);
            }

        } catch (InvocationTargetException e) {
            Throwable t = e.getCause();
            TreeItem ti = new TreeItem(item, SWT.NONE);
            ti.setText("Error: "+t.getMessage());

        } catch (Exception e) {
            TreeItem ti = new TreeItem(item, SWT.NONE);
            ti.setText("Error: "+e.getMessage());
        }
    }

    public void showEntry(TreeItem item) throws Exception {
        try {
            table.removeAll();

            final DN dn = (DN)item.getData();
            if (dn == null) return;

            final Attributes results = new Attributes();

            IProgressService progressService = PlatformUI.getWorkbench().getProgressService();

            progressService.busyCursorWhile(new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor) throws InvocationTargetException {
                    try {
                        monitor.beginTask("Retrieving data...", IProgressMonitor.UNKNOWN);

                        monitor.subTask("Searching for "+dn+"...");

                        SearchResult entry = connectionClient.find(dn);
                        if (entry == null) return;

                        Attributes attributes = entry.getAttributes();
                        results.add(attributes);

                        monitor.worked(1);

                    } catch (Exception e) {
                        throw new InvocationTargetException(e);

                    } finally {
                        monitor.done();
                    }
                }
            });

            for (Attribute attribute : results.getAll()) {
                String name = attribute.getName();
    
                for (Object value : attribute.getValues()) {
                    String label = value instanceof byte[] ? "(binary)" : value.toString();

                    TableItem ti = new TableItem(table, SWT.NONE);
                    ti.setText(0, name);
                    ti.setText(1, label);
                    ti.setData(value);
                }
            }

        } catch (InvocationTargetException e) {
            Throwable t = e.getCause();
            log.error(t.getMessage(), t);
            ErrorDialog.open(t);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }
}