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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.window.Window;
import org.safehaus.penrose.studio.connection.editor.ConnectionEditorPage;
import org.safehaus.penrose.studio.ldap.connection.wizard.LDAPConnectionSettingsWizard;

import javax.naming.Context;
import javax.naming.InitialContext;

/**
 * @author Endi S. Dewata
 */
public class LDAPConnectionPropertiesPage extends ConnectionEditorPage {

    Label urlText;
    Label bindDnText;
    Label passwordText;

    String url;

    public LDAPConnectionPropertiesPage(LDAPConnectionEditor editor) {
        super(editor, "LDAP", "LDAP");
    }

    public void createFormContent(IManagedForm managedForm) {
        super.createFormContent(managedForm);

        ScrolledForm form = managedForm.getForm();

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section ldapSection = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        ldapSection.setText("LDAP");
        ldapSection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Control ldapControl = createLDAPControl(ldapSection);
        ldapSection.setClient(ldapControl);

        refresh();
    }

    public Composite createLDAPControl(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        Composite leftControl = createLDAPLeftControl(composite);
        leftControl.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite rightControl = createLDAPRightControl(composite);
        GridData gd = new GridData(GridData.FILL_VERTICAL);
        gd.widthHint = 100;
        rightControl.setLayoutData(gd);

        return composite;
    }

    public Composite createLDAPLeftControl(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);

        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);

        toolkit.createLabel(composite, "URL:");

        urlText = toolkit.createLabel(composite, "", SWT.NONE);
        //protocolCombo.add("ldap");
        //protocolCombo.add("ldaps");
        urlText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
/*
        protocolCombo.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                String url = getURL();
                connectionConfig.setParameter(Context.PROVIDER_URL, url);
                checkDirty();
            }
        });

        toolkit.createLabel(composite, "Host:");

        hostText = toolkit.createLabel(composite, "", SWT.NONE);
        hostText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        hostText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                String url = getURL();
                connectionConfig.setParameter(Context.PROVIDER_URL, url);
                checkDirty();
            }
        });

        toolkit.createLabel(composite, "Port:");

        portText = toolkit.createLabel(composite, "", SWT.NONE);
        portText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        portText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                String url = getURL();
                connectionConfig.setParameter(Context.PROVIDER_URL, url);
                checkDirty();
            }
        });
*/
/*
        suffixCombo.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                String url = getURL();
                connectionConfig.setParameter(Context.PROVIDER_URL, url);
                checkDirty();
            }
        });

        Button fetchButton = new Button(composite, SWT.PUSH);
		fetchButton.setText("Fetch Base DNs");

        fetchButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        fetchButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                LDAPClient client = null;
                try {
                    suffixCombo.removeAll();

                    client = new LDAPClient(connectionConfig.getParameters());

                    SearchResult rootDse = client.getRootDSE();

                    Attribute namingContexts = rootDse.getAttributes().get("namingContexts");
                    if (namingContexts != null) {
                        for (Object value : namingContexts.getValues()) {
                            String namingContext = (String)value;
                            suffixCombo.add(namingContext);
                        }
                    }

                    suffixCombo.select(0);

                } catch (Exception ex) {
                    log.error(ex.getMessage(), ex);
                    ErrorDialog.open(ex);

                } finally {
                    if (client != null) try { client.close(); } catch (Exception e) { log.error(e.getMessage(), e); }
                }
            }
        });
*/
        toolkit.createLabel(composite, "Principal:");

        bindDnText = toolkit.createLabel(composite, "", SWT.NONE);

        bindDnText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
/*
        bindDnText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                connectionConfig.setParameter(Context.SECURITY_PRINCIPAL, bindDnText.getText());
                checkDirty();
            }
        });
*/
        toolkit.createLabel(composite, "Credentials:");

        passwordText = toolkit.createLabel(composite, "", SWT.PASSWORD);

        passwordText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
/*
        passwordText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                connectionConfig.setParameter(Context.SECURITY_CREDENTIALS, passwordText.getText());
                checkDirty();
            }
        });

        toolkit.createLabel(composite, "");

        Button testButton = new Button(composite, SWT.PUSH);
		testButton.setText("Test Connection");

        gd = new GridData();
        gd.horizontalSpan = 5;
        testButton.setLayoutData(gd);

        testButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                Helper.testJndiConnection(
                        editor.getSite().getShell(),
                        "com.sun.jndi.ldap.LdapCtxFactory",
                        getURL(),
                        bindDnText.getText(),
                        passwordText.getText()
                );
            }
        });
*/
        return composite;
    }

    public Composite createLDAPRightControl(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);

        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);

        Button editButton = new Button(composite, SWT.PUSH);
		editButton.setText("Edit");
        editButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        editButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    LDAPConnectionSettingsWizard wizard = new LDAPConnectionSettingsWizard();
                    wizard.setServer(server);
                    wizard.setPartitionName(partitionName);
                    wizard.setConnectionConfig(connectionConfig);

                    WizardDialog dialog = new WizardDialog(editor.getSite().getShell(), wizard);
                    dialog.setPageSize(600, 300);

                    int rc = dialog.open();
                    if (rc == Window.CANCEL) return;

                    editor.store();
                    refresh();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });

        return composite;
    }

    public void refresh() {
        String url = connectionConfig.getParameter(InitialContext.PROVIDER_URL);
        //String[] s = LDAPClient.parseURL(url);

        urlText.setText(url == null ? "" : url);
        //hostText.setText(s[1] == null ? "" : s[1]);
        //portText.setText(s[2] == null ? "" : s[2]);

        String bindDn = connectionConfig.getParameter(Context.SECURITY_PRINCIPAL);
        bindDnText.setText(bindDn == null ? "" : bindDn);

        passwordText.setText("*****");
    }

    public void checkDirty() {
        editor.checkDirty();
    }
}