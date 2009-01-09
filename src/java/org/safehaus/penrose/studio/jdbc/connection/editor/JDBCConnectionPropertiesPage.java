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
package org.safehaus.penrose.studio.jdbc.connection.editor;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.events.*;
import org.eclipse.ui.forms.widgets.*;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.window.Window;
import org.safehaus.penrose.studio.connection.editor.ConnectionEditorPage;
import org.safehaus.penrose.studio.connection.wizard.ConnectionNameWizard;
import org.safehaus.penrose.studio.jdbc.connection.wizard.JDBCConnectionSettingsWizard;
import org.safehaus.penrose.jdbc.JDBCClient;

/**
 * @author Endi S. Dewata
 */
public class JDBCConnectionPropertiesPage extends ConnectionEditorPage {

    Label nameText;
    //Label descriptionText;

    Label adapterText;
    Label driverText;
    Label urlText;
    Label usernameText;
    Label passwordText;

    public JDBCConnectionPropertiesPage(JDBCConnectionEditor editor) {
        super(editor, "PROPERTIES", "Properties");
    }

    public void createFormContent(IManagedForm managedForm) {
        super.createFormContent(managedForm);

        ScrolledForm form = managedForm.getForm();

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Properties");
        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Control propertiesControl = createPropertiesControl(section);
        section.setClient(propertiesControl);

        Section settingsSection = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        settingsSection.setText("Settings");
        settingsSection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Control settingsControl = createSettingsControl(settingsSection);
        settingsSection.setClient(settingsControl);
    }

    public Composite createPropertiesControl(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        Composite leftControl = createPropertiesLeftControl(composite);
        leftControl.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite rightControl = createPropertiesRightControl(composite);
        GridData gd = new GridData(GridData.FILL_VERTICAL);
        gd.widthHint = 100;
        rightControl.setLayoutData(gd);

        return composite;
    }

    public Composite createPropertiesLeftControl(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);

        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);

        Label connectionNameLabel = toolkit.createLabel(composite, "Name:");
        GridData gd = new GridData();
        gd.widthHint = 100;
        connectionNameLabel.setLayoutData(gd);

        nameText = toolkit.createLabel(composite, "", SWT.NONE);
        nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
/*
        nameText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                connectionConfig.setName("".equals(nameText.getText()) ? null : nameText.getText());
                checkDirty();
            }
        });

        Label descriptionLabel = toolkit.createLabel(composite, "Description:");
        gd = new GridData();
        gd.widthHint = 100;
        descriptionLabel.setLayoutData(gd);

        descriptionText = toolkit.createLabel(composite, "", SWT.NONE);
        descriptionText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        descriptionText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                connectionConfig.setDescription("".equals(descriptionText.getText()) ? null : descriptionText.getText());
                checkDirty();
            }
        });
*/

        return composite;
    }

    public Composite createPropertiesRightControl(final Composite parent) {

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
                    String name = connectionConfig.getName();

                    ConnectionNameWizard wizard = new ConnectionNameWizard();
                    wizard.setServer(server);
                    wizard.setPartitionName(partitionName);
                    wizard.setConnectionName(name);

                    WizardDialog dialog = new WizardDialog(editor.getSite().getShell(), wizard);
                    dialog.setPageSize(600, 300);

                    int rc = dialog.open();
                    if (rc == Window.CANCEL) return;

                    String newName = wizard.getConnectionName();

                    editor.rename(name, newName);

                    refresh();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });

        return composite;
    }

    public Composite createSettingsControl(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        Composite leftControl = createSettingsLeftControl(composite);
        leftControl.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite rightControl = createSettingsRightControl(composite);
        GridData gd = new GridData(GridData.FILL_VERTICAL);
        gd.widthHint = 100;
        rightControl.setLayoutData(gd);

        return composite;
    }

    public Composite createSettingsLeftControl(Composite parent) {

        Composite composite = toolkit.createComposite(parent);

        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);

        Label adapterLabel = toolkit.createLabel(composite, "Adapter:");
        GridData gd = new GridData();
        gd.widthHint = 100;
        adapterLabel.setLayoutData(gd);

        adapterText = toolkit.createLabel(composite, "", SWT.NONE);
        adapterText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        toolkit.createLabel(composite, "Driver:");

        driverText = toolkit.createLabel(composite, "", SWT.NONE);
        driverText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
/*
        driverText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                connectionConfig.setParameter(JDBCClient.DRIVER, driverText.getText());
                checkDirty();
            }
        });
*/
        toolkit.createLabel(composite, "URL:");

        urlText = toolkit.createLabel(composite, "", SWT.NONE);
        urlText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
/*
        urlText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                connectionConfig.setParameter(JDBCClient.URL, urlText.getText());
                checkDirty();
            }
        });
*/
        toolkit.createLabel(composite, "Username:");

        usernameText = toolkit.createLabel(composite, "", SWT.NONE);
        usernameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
/*
        usernameText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                connectionConfig.setParameter(JDBCClient.USER, usernameText.getText());
                checkDirty();
            }
        });
*/
        toolkit.createLabel(composite, "Password:");

        passwordText = toolkit.createLabel(composite, "", SWT.PASSWORD );
		
        passwordText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
/*
        passwordText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                connectionConfig.setParameter(JDBCClient.PASSWORD, passwordText.getText());
                checkDirty();
            }
        });

        toolkit.createLabel(composite, "");

        Button testButton = new Button(composite, SWT.PUSH);
		testButton.setText("Test Connection");


        testButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {

                Helper.testJdbcConnection(getEditor().getSite().getShell(), driverText.getText(), urlText.getText(), usernameText.getText(), passwordText.getText());
            }
        });
*/
        return composite;
    }

    public Composite createSettingsRightControl(final Composite parent) {

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
                    JDBCConnectionSettingsWizard wizard = new JDBCConnectionSettingsWizard();
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
        nameText.setText(connectionConfig.getName() == null ? "" : connectionConfig.getName());
        //descriptionText.setText(connectionConfig.getDescription() == null ? "" : connectionConfig.getDescription());

        adapterText.setText(connectionConfig.getAdapterName() == null ? "" : connectionConfig.getAdapterName());

        String s = connectionConfig.getParameter(JDBCClient.DRIVER);
        driverText.setText(s == null ? "" : s);

        s = connectionConfig.getParameter(JDBCClient.URL);
        urlText.setText(s == null ? "" : s);

        s = connectionConfig.getParameter(JDBCClient.USER);
        usernameText.setText(s == null ? "" : s);

        passwordText.setText("*****");
    }
}
