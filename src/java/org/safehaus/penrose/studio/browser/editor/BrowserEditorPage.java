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
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.window.Window;
import org.ietf.ldap.*;
import org.safehaus.penrose.ldap.DN;
import org.safehaus.penrose.studio.browser.wizard.BrowserConnectionWizard;

public class BrowserEditorPage extends FormPage {

	Logger log = Logger.getLogger(getClass());

    public final static String LDAP_PORT             = "ldapPort";
    public final static int DEFAULT_LDAP_PORT        = 10389;

    FormToolkit toolkit;

    Text urlText;
    Tree tree;

    Text dnText;
    Table attributesTable;

    LDAPConnection connection = new LDAPConnection();

    String hostname;
    int port;
    String suffix;
    String bindDn;
    byte[] password;

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
        form.setText("LDAP Browser");

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

        toolkit.createLabel(composite, "URL:", SWT.NONE);

        urlText = new Text(composite, SWT.BORDER);
        urlText.setEnabled(false);
        urlText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

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
                    throw new RuntimeException(e.getMessage(), e);
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
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });

        reset();

        return composite;
    }

    public Composite createDirectoryRightControl(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);

        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);

        Button connectButton = new Button(composite, SWT.PUSH);
        connectButton.setText("Connect...");
        connectButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        connectButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    LDAPUrl url = new LDAPUrl(urlText.getText());

                    BrowserConnectionWizard wizard = new BrowserConnectionWizard();
                    wizard.setProviderUrl(url.toString());
                    wizard.setSuffix(suffix);
                    wizard.setBindDn(bindDn);
                    wizard.setBindPassword(new String(password));

                    IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                    WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
                    dialog.setPageSize(600, 300);
                    int rc = dialog.open();

                    if (rc == Window.CANCEL) return;

                    url = new LDAPUrl(wizard.getProviderUrl());
                    hostname = url.getHost();
                    port = url.getPort();
                    suffix = wizard.getSuffix();
                    bindDn = wizard.getBindDn();
                    password = wizard.getBindPassword().getBytes();

                    disconnect();
                    refresh();
                    connect();
                    reset();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e.getMessage(), e);
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

        Label dnLabel = new Label(composite, SWT.NONE);
        dnLabel.setText("DN:");

        dnText = new Text(composite, SWT.BORDER | SWT.READ_ONLY);
        dnText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

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

    public void setActive(boolean b) {
        super.setActive(b);
        if (b) refresh();
    }

    public void refresh() {
        LDAPUrl ldapUrl = new LDAPUrl(hostname, port, "");
        urlText.setText(ldapUrl.toString());
    }

    public void reset() {
        tree.removeAll();

        TreeItem item = new TreeItem(tree, SWT.NONE);
        item.setText(suffix == null ? "Root DSE" : suffix);
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

        if (!isConnected()) connect();

        for (TreeItem ti : item.getItems()) ti.dispose();

        try {
            DN baseDn = (DN)item.getData();

            if (baseDn.isEmpty()) {

                LDAPSearchResults sr = connection.search(
                        "",
                        LDAPConnection.SCOPE_BASE,
                        "(objectClass=*)",
                        new String[] { "*", "+" },
                        false
                );

                LDAPEntry rootDse = sr.next();

                LDAPAttribute namingContexts = rootDse.getAttribute("namingContexts");
                if (namingContexts != null) {
                    for (Enumeration e = namingContexts.getStringValues(); e.hasMoreElements(); ) {
                        String dn = (String)e.nextElement();

                        TreeItem ti = new TreeItem(item, SWT.NONE);
                        ti.setText(dn);
                        ti.setData(new DN(dn));

                        new TreeItem(ti, SWT.NONE);
                    }
                }

            } else {

                LDAPSearchResults sr = connection.search(
                        baseDn.toString(),
                        LDAPConnection.SCOPE_ONE,
                        "(objectClass=*)",
                        new String[] { "*", "+" },
                        true
                );

                while (sr.hasMore()) {
                    LDAPEntry entry = sr.next();
                    DN dn = new DN(entry.getDN());
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

    public void showEntry(TreeItem item) throws Exception {

        if (!isConnected()) connect();

        DN dn = (DN)item.getData();

        dnText.setText(dn.toString());

        attributesTable.removeAll();

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
            String name = attribute.getName();

            for (Enumeration e = attribute.getStringValues(); e.hasMoreElements();) {
                String value = (String) e.nextElement();

                TableItem tableItem = new TableItem(attributesTable, SWT.NONE);
                tableItem.setText(0, name);
                tableItem.setText(1, value);
            }
        }
    }
}