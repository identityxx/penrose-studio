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
package org.safehaus.penrose.studio.browser.editor;

import java.util.Enumeration;
import java.util.Collection;
import java.util.ArrayList;
import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.TreeAdapter;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.core.runtime.IProgressMonitor;
import org.ietf.ldap.*;
import org.safehaus.penrose.ldap.DN;
import org.safehaus.penrose.studio.browser.wizard.BrowserOptionsWizard;
import org.safehaus.penrose.studio.dialog.ErrorDialog;

public class BrowserEditorPage extends FormPage {

	Logger log = Logger.getLogger(getClass());

    public final static String LDAP_PORT             = "ldapPort";
    public final static int DEFAULT_LDAP_PORT        = 10389;

    FormToolkit toolkit;

    Tree tree;

    Table attributesTable;

    LDAPConnection connection = new LDAPConnection();

    String hostname;
    int port;
    String suffix;
    String bindDn;
    byte[] password;

    Long sizeLimit;

    public BrowserEditorPage(BrowserEditor editor) {
        super(editor, "BROWSER", "  Browser  ");

        BrowserEditorInput ei = (BrowserEditorInput)editor.getEditorInput();
        hostname = ei.getHostname();
        suffix = ei.getSuffix();
        port = ei.getPort();
        bindDn = ei.getBindDn();
        password = ei.getPassword();
    }

    public void createFormContent(IManagedForm managedForm) {
        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText("Browser");

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

        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);

        tree = new Tree(composite, SWT.BORDER);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.horizontalSpan = 2;
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
                    if (tree.getSelectionCount() == 0) return;

                    TreeItem item = tree.getSelection()[0];
                    showEntry(item);

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        return composite;
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
                    BrowserOptionsWizard wizard = new BrowserOptionsWizard();
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

        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);

        attributesTable = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.horizontalSpan = 2;
        attributesTable.setLayoutData(gd);

        attributesTable.setHeaderVisible(true);
        attributesTable.setLinesVisible(true);

        TableColumn tc = new TableColumn(attributesTable, SWT.NONE, 0);
        tc.setText("Name");
        tc.setWidth(200);

        tc = new TableColumn(attributesTable, SWT.NONE, 1);
        tc.setText("Value");
        tc.setWidth(350);

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
        attributesTable.removeAll();

        TreeItem item = new TreeItem(tree, SWT.NONE);
        item.setText(suffix == null ? "Root" : suffix);
        item.setData(suffix == null ? new DN() : new DN(suffix));

        new TreeItem(item, SWT.NONE);
    }

    public void connect() throws Exception {
        connection.connect(hostname, port);
        connection.bind(3, bindDn, password);
    }

    public void disconnect() throws Exception {
        connection.disconnect();
    }

    public boolean isConnected() throws Exception {
        return connection.isConnected();
    }

    public void expand(TreeItem item) throws Exception {
        try {
            for (TreeItem ti : item.getItems()) ti.dispose();

            if (!isConnected()) connect();

            final DN baseDn = (DN)item.getData();
            final Collection<DN> results = new ArrayList<DN>();

            IProgressService progressService = PlatformUI.getWorkbench().getProgressService();

            progressService.busyCursorWhile(new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor) throws InvocationTargetException {
                    try {
                        monitor.beginTask("Retrieving data...", IProgressMonitor.UNKNOWN);

                        if (baseDn.isEmpty()) {

                            monitor.subTask("Searching Root DSE...");

                            LDAPSearchResults sr = connection.search(
                                    "",
                                    LDAPConnection.SCOPE_BASE,
                                    "(objectClass=*)",
                                    new String[] { "*", "+" },
                                    false
                            );

                            monitor.worked(1);

                            monitor.subTask("Processing results...");
                            
                            LDAPEntry rootDse = sr.next();

                            LDAPAttribute namingContexts = rootDse.getAttribute("namingContexts");
                            if (namingContexts != null) {
                                for (Enumeration e = namingContexts.getStringValues(); e.hasMoreElements(); ) {
                                    String dn = (String)e.nextElement();
                                    results.add(new DN(dn));
                                }
                            }

                            monitor.worked(1);

                        } else {

                            monitor.subTask("Searching children of "+baseDn+"...");

                            LDAPSearchConstraints constraints = new LDAPSearchConstraints();
                            if (sizeLimit != null) constraints.setMaxResults(sizeLimit.intValue());

                            LDAPSearchResults sr = connection.search(
                                    baseDn.toString(),
                                    LDAPConnection.SCOPE_ONE,
                                    "(objectClass=*)",
                                    new String[] { "dn" },
                                    true,
                                    constraints
                            );

                            monitor.worked(1);

                            monitor.subTask("Processing results...");

                            while (sr.hasMore()) {
                                LDAPEntry entry = sr.next();
                                DN dn = new DN(entry.getDN());
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
            attributesTable.removeAll();

            if (!isConnected()) connect();

            final DN dn = (DN)item.getData();
            if (dn == null) return;

            final Collection<LDAPAttribute> results = new ArrayList<LDAPAttribute>();

            IProgressService progressService = PlatformUI.getWorkbench().getProgressService();

            progressService.busyCursorWhile(new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor) throws InvocationTargetException {
                    try {
                        monitor.beginTask("Retrieving data...", IProgressMonitor.UNKNOWN);

                        monitor.subTask("Searching for "+dn+"...");

                        LDAPSearchResults sr = connection.search(
                                dn.toString(),
                                LDAPConnection.SCOPE_BASE,
                                "(objectClass=*)",
                                new String[] { "*", "+" },
                                false
                        );

                        if (!sr.hasMore()) return;

                        LDAPEntry entry = sr.next();
                        LDAPAttributeSet attributes = entry.getAttributeSet();

                        for (Object object : attributes) {
                            LDAPAttribute attribute = (LDAPAttribute) object;
                            results.add(attribute);
                        }

                        monitor.worked(1);

                    } catch (Exception e) {
                        throw new InvocationTargetException(e);

                    } finally {
                        monitor.done();
                    }
                }
            });

            for (LDAPAttribute attribute : results) {
                String name = attribute.getName();

                for (Enumeration e = attribute.getStringValues(); e.hasMoreElements();) {
                    String value = (String) e.nextElement();

                    TableItem tableItem = new TableItem(attributesTable, SWT.NONE);
                    tableItem.setText(0, name);
                    tableItem.setText(1, value);
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