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
package org.safehaus.penrose.studio.ldap.connection;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.events.*;
import org.eclipse.ui.forms.widgets.*;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.jface.dialogs.MessageDialog;
import org.safehaus.penrose.studio.util.Helper;
import org.safehaus.penrose.studio.connection.editor.ConnectionEditorPage;
import org.safehaus.penrose.studio.connection.editor.ConnectionEditor;
import org.safehaus.penrose.ldap.LDAPClient;

import javax.naming.InitialContext;
import javax.naming.Context;
import java.util.*;

/**
 * @author Endi S. Dewata
 */
public class ConnectionPropertiesPage extends ConnectionEditorPage {

    Text nameText;
    Text descriptionText;

    Combo protocolCombo;
    Text hostText;
    Combo suffixCombo;
    Text portText;
    Text bindDnText;
    Text passwordText;

    String url;

    public ConnectionPropertiesPage(ConnectionEditor editor) {
        super(editor, "PROPERTIES", "  Properties  ");
    }

    public void createFormContent(IManagedForm managedForm) {
        super.createFormContent(managedForm);

        ScrolledForm form = managedForm.getForm();
        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section section = getToolkit().createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Properties");
        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Control nameSection = createPropertiesControl(section);
        section.setClient(nameSection);

        section = getToolkit().createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("LDAP Properties");
        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Control ldapPropertiesControl = createLDAPPropertiesControl(section);
        section.setClient(ldapPropertiesControl);
    }

    public void refresh() {
        nameText.setText(getConnectionConfig().getName() == null ? "" : getConnectionConfig().getName());
        descriptionText.setText(getConnectionConfig().getDescription() == null ? "" : getConnectionConfig().getDescription());

        String url = getConnectionConfig().getParameter(InitialContext.PROVIDER_URL);
        String[] s = LDAPClient.parseURL(url);

        if (s[0] != null) protocolCombo.setText(s[0]);
        if (s[1] != null) hostText.setText(s[1]);
        if (s[2] != null) portText.setText(s[2]);
        if (s[3] != null) suffixCombo.setText(s[3]);

        String bindDn = getConnectionConfig().getParameter(Context.SECURITY_PRINCIPAL);
        if (bindDn != null) bindDnText.setText(bindDn);

        String bindPassword = getConnectionConfig().getParameter(Context.SECURITY_CREDENTIALS);
        if (bindPassword != null) passwordText.setText(bindPassword);

    }

    public Composite createPropertiesControl(final Composite parent) {

        Composite composite = getToolkit().createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        Label connectionNameLabel = getToolkit().createLabel(composite, "Name:");
        GridData gd = new GridData();
        gd.widthHint = 100;
        connectionNameLabel.setLayoutData(gd);

        nameText = getToolkit().createText(composite, "", SWT.BORDER);
        nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        nameText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                getConnectionConfig().setName(nameText.getText());
                checkDirty();
            }
        });

        Label descriptionLabel = getToolkit().createLabel(composite, "Description:");
        gd = new GridData();
        gd.widthHint = 100;
        descriptionLabel.setLayoutData(gd);

        descriptionText = getToolkit().createText(composite, "", SWT.BORDER);
        descriptionText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        descriptionText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                getConnectionConfig().setDescription("".equals(descriptionText.getText()) ? null : descriptionText.getText());
                checkDirty();
            }
        });

        return composite;
    }

    public Composite createLDAPPropertiesControl(final Composite parent) {

        Composite composite = getToolkit().createComposite(parent);
        composite.setLayout(new GridLayout(6, false));

        Label protocolLabel = getToolkit().createLabel(composite, "Protocol:");
        GridData gd = new GridData();
        gd.widthHint = 100;
        protocolLabel.setLayoutData(gd);

        protocolCombo = new Combo(composite, SWT.BORDER);
        protocolCombo.add("ldap");
        protocolCombo.add("ldaps");
        protocolCombo.setLayoutData(new GridData());

        protocolCombo.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                String url = getURL();
                getConnectionConfig().setParameter(Context.PROVIDER_URL, url);
                checkDirty();
            }
        });

        getToolkit().createLabel(composite, "Host:");

        hostText = getToolkit().createText(composite, "", SWT.BORDER);
        hostText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        hostText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                String url = getURL();
                getConnectionConfig().setParameter(Context.PROVIDER_URL, url);
                checkDirty();
            }
        });

        getToolkit().createLabel(composite, "Port:");

        portText = getToolkit().createText(composite, "", SWT.BORDER);
        gd = new GridData();
        gd.widthHint = 50;
        portText.setLayoutData(gd);

        portText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                String url = getURL();
                getConnectionConfig().setParameter(Context.PROVIDER_URL, url);
                checkDirty();
            }
        });

        getToolkit().createLabel(composite, "Base DN:");

        suffixCombo = new Combo(composite, SWT.BORDER);

        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 3;
        suffixCombo.setLayoutData(gd);

        suffixCombo.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                String url = getURL();
                getConnectionConfig().setParameter(Context.PROVIDER_URL, url);
                checkDirty();
            }
        });

        Button fetchButton = getToolkit().createButton(composite, "Fetch Base DNs", SWT.PUSH);
        gd = new GridData();
        gd.horizontalSpan = 2;
        gd.widthHint = 120;
        fetchButton.setLayoutData(gd);

        fetchButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                try {
                    LDAPClient client = new LDAPClient(getConnectionConfig().getParameters());
                    Collection list = client.getNamingContexts();

                    suffixCombo.removeAll();
                    for (Iterator i=list.iterator(); i.hasNext(); ) {
                        String baseDn = (String)i.next();
                        suffixCombo.add(baseDn);
                    }
                    suffixCombo.select(0);

                } catch (Exception ex) {
                    log.error(ex.getMessage(), ex);
                    MessageDialog.openError(parent.getShell(), "Failed to fetch base DNs", "Error: "+ex.getMessage());
                }
            }
        });

        getToolkit().createLabel(composite, "Principal:");

        bindDnText = getToolkit().createText(composite, "", SWT.BORDER);

        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 5;
        bindDnText.setLayoutData(gd);

        bindDnText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                getConnectionConfig().setParameter(Context.SECURITY_PRINCIPAL, bindDnText.getText());
                checkDirty();
            }
        });

        getToolkit().createLabel(composite, "Credentials:");

        passwordText = getToolkit().createText(composite, "", SWT.BORDER | SWT.PASSWORD);
        passwordText.setEchoChar('*');

        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 5;
        passwordText.setLayoutData(gd);

        passwordText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                getConnectionConfig().setParameter(Context.SECURITY_CREDENTIALS, passwordText.getText());
                checkDirty();
            }
        });

        getToolkit().createLabel(composite, "");

        Button testButton = getToolkit().createButton(composite, "Test Connection", SWT.PUSH);
        gd = new GridData();
        gd.horizontalSpan = 5;
        testButton.setLayoutData(gd);

        testButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                Helper.testLDAPConnection(
                        getEditor().getSite().getShell(),
                        "com.sun.jndi.ldap.LdapCtxFactory",
                        getURL(),
                        bindDnText.getText(),
                        passwordText.getText()
                );
            }
        });

        return composite;
    }

    public String getURL() {
        String protocol = protocolCombo.getText();
        String hostname = hostText.getText();
        String port = portText.getText();
        String suffix = suffixCombo.getText();

        if (!port.equals("")) {
            if ("ldap".equals(protocol) && "389".equals(port)) {
                port = "";
            } else if ("ldaps".equals(protocol) && "636".equals(port)) {
                port = "";
            } else {
                port = ":"+port;
            }
        }

        return protocol + "://" + hostname + port + "/" + suffix;
    }
}