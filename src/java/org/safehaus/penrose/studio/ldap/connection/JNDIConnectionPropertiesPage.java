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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.safehaus.penrose.ldap.Attribute;
import org.safehaus.penrose.ldap.LDAPClient;
import org.safehaus.penrose.ldap.SearchResult;
import org.safehaus.penrose.studio.connection.editor.ConnectionEditorPage;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.studio.ldap.connection.editor.LDAPConnectionEditor;
import org.safehaus.penrose.studio.parameter.ParameterDialog;
import org.safehaus.penrose.studio.util.Helper;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author Endi S. Dewata
 */
public class JNDIConnectionPropertiesPage extends ConnectionEditorPage {

    Text nameText;
    Combo protocolCombo;
    Text hostText;
    Combo suffixCombo;
    Text portText;
    Text bindDnText;
    Text passwordText;

    Table parametersTable;

    String url;

    public JNDIConnectionPropertiesPage(LDAPConnectionEditor editor) {
        super(editor, "PROPERTIES", "  Properties  ");
    }

    public void createFormContent(IManagedForm managedForm) {
        super.createFormContent(managedForm);

        ScrolledForm form = managedForm.getForm();

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Connection Name");
        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Control nameSection = createNameSection(section);
        section.setClient(nameSection);

        section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Connection Info");
        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Control infoSection = createInfoSection(section);
        section.setClient(infoSection);

        section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Connection Parameters");
        section.setLayoutData(new GridData(GridData.FILL_BOTH));

        Control parametersSection = createParametersSection(section);
        section.setClient(parametersSection);

        refresh();
    }

    public Composite createNameSection(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        Label connectionNameLabel = toolkit.createLabel(composite, "Name:");
        GridData gd = new GridData();
        gd.widthHint = 100;
        connectionNameLabel.setLayoutData(gd);

        nameText = toolkit.createText(composite, connectionConfig.getName(), SWT.BORDER);
        nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        nameText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                connectionConfig.setName(nameText.getText());
                checkDirty();
            }
        });

        return composite;
    }

    public Composite createInfoSection(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(6, false));

        String url = connectionConfig.getParameter(InitialContext.PROVIDER_URL);
        String[] s = LDAPClient.parseURL(url);

        Label protocolLabel = toolkit.createLabel(composite, "Protocol:");
        GridData gd = new GridData();
        gd.widthHint = 100;
        protocolLabel.setLayoutData(gd);

        protocolCombo = new Combo(composite, SWT.BORDER);
        protocolCombo.add("ldap");
        protocolCombo.add("ldaps");
        protocolCombo.setLayoutData(new GridData());

        if (s[0] != null) protocolCombo.setText(s[0]);

        protocolCombo.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                String url = getURL();
                connectionConfig.setParameter(Context.PROVIDER_URL, url);
                checkDirty();
            }
        });

        toolkit.createLabel(composite, "Host:");

        hostText = toolkit.createText(composite, "", SWT.BORDER);
        hostText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        if (s[1] != null) hostText.setText(s[1]);

        hostText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                String url = getURL();
                connectionConfig.setParameter(Context.PROVIDER_URL, url);
                checkDirty();
            }
        });

        toolkit.createLabel(composite, "Port:");

        portText = toolkit.createText(composite, "", SWT.BORDER);
        gd = new GridData();
        gd.widthHint = 50;
        portText.setLayoutData(gd);

        if (s[2] != null) portText.setText(s[2]);

        portText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                String url = getURL();
                connectionConfig.setParameter(Context.PROVIDER_URL, url);
                checkDirty();
            }
        });

        toolkit.createLabel(composite, "Base DN:");

        suffixCombo = new Combo(composite, SWT.BORDER);
        if (s[3] != null) suffixCombo.setText(s[3]);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 3;
        suffixCombo.setLayoutData(gd);

        suffixCombo.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                String url = getURL();
                connectionConfig.setParameter(Context.PROVIDER_URL, url);
                checkDirty();
            }
        });

        Button fetchButton = new Button(composite, SWT.PUSH);
		fetchButton.setText("Fetch Base DNs");

        gd = new GridData();
        gd.horizontalSpan = 2;
        gd.widthHint = 120;
        fetchButton.setLayoutData(gd);

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

        toolkit.createLabel(composite, "Principal:");

        bindDnText = toolkit.createText(composite, "", SWT.BORDER);
        String value = connectionConfig.getParameter(Context.SECURITY_PRINCIPAL);
        if (value != null) bindDnText.setText(value);

        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 5;
        bindDnText.setLayoutData(gd);

        bindDnText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                connectionConfig.setParameter(Context.SECURITY_PRINCIPAL, bindDnText.getText());
                checkDirty();
            }
        });

        toolkit.createLabel(composite, "Credentials:");

        passwordText = toolkit.createText(composite, "", SWT.BORDER | SWT.PASSWORD);
        

        value = connectionConfig.getParameter(Context.SECURITY_CREDENTIALS);
        if (value != null) passwordText.setText(value);

        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 5;
        passwordText.setLayoutData(gd);

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

        return composite;
    }

    public Composite createParametersSection(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        parametersTable = new Table(composite, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
        parametersTable.setHeaderVisible(true);
        parametersTable.setLinesVisible(true);
        parametersTable.setLayoutData(new GridData(GridData.FILL_BOTH));

        TableColumn tc = new TableColumn(parametersTable, SWT.NONE);
        tc.setText("Name");
        tc.setWidth(250);

        tc = new TableColumn(parametersTable, SWT.NONE);
        tc.setText("Value");
        tc.setWidth(250);

        parametersTable.addMouseListener(new MouseAdapter() {
            public void mouseDoubleClick(MouseEvent event) {
                try {
                    if (parametersTable.getSelectionCount() == 0) return;

                    int index = parametersTable.getSelectionIndex();
                    TableItem item = parametersTable.getSelection()[0];

                    String oldName = item.getText(0);
                    String oldValue = item.getText(1);

                    ParameterDialog dialog = new ParameterDialog(parent.getShell(), SWT.NONE);
                    dialog.setText("Edit parameter...");
                    dialog.setName(oldName);
                    dialog.setValue(oldValue);
                    dialog.open();

                    if (dialog.getAction() == ParameterDialog.CANCEL) return;

                    String newName = dialog.getName();
                    String newValue = dialog.getValue();

                    if (!oldName.equals(newName)) {
                        connectionConfig.removeParameter(oldName);
                    }

                    connectionConfig.setParameter(newName, newValue);

                    refresh();
                    parametersTable.setSelection(index);
                    checkDirty();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        Composite buttons = toolkit.createComposite(composite, SWT.NONE);
        buttons.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        buttons.setLayout(new GridLayout());

        Button addButton = new Button(buttons, SWT.PUSH);
		addButton.setText("Add");

        addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                ParameterDialog dialog = new ParameterDialog(parent.getShell(), SWT.NONE);
                dialog.setText("Add parameter...");
                dialog.open();

                if (dialog.getAction() == ParameterDialog.CANCEL) return;

                connectionConfig.setParameter(dialog.getName(), dialog.getValue());

                refresh();
                checkDirty();
            }
        });

        Button removeButton = new Button(buttons, SWT.PUSH);
		removeButton.setText("Delete");

        removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                if (parametersTable.getSelectionCount() == 0) return;

                TableItem items[] = parametersTable.getSelection();
                for (int i=0; i<items.length; i++) {
                    String name = items[i].getText(0);
                    connectionConfig.removeParameter(name);
                }

                refresh();
                checkDirty();
            }
        });

        return composite;
    }

    public void refresh() {
        parametersTable.removeAll();

        Collection parameters = connectionConfig.getParameterNames();
        for (Iterator i=parameters.iterator(); i.hasNext(); ) {
            String name = (String)i.next();

            if (Context.PROVIDER_URL.equals(name)) continue;
            if (Context.SECURITY_PRINCIPAL.equals(name)) continue;
            if (Context.SECURITY_CREDENTIALS.equals(name)) continue;

            String value = connectionConfig.getParameter(name);
            TableItem ti = new TableItem(parametersTable, SWT.NONE);
            ti.setText(0, name);
            ti.setText(1, value);
        }
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

    public void checkDirty() {
        editor.checkDirty();
    }
}