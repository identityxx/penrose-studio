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
package org.safehaus.penrose.studio.connection.editor;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.events.*;
import org.eclipse.ui.forms.widgets.*;
import org.eclipse.ui.forms.IManagedForm;
import org.safehaus.penrose.studio.util.Helper;
import org.safehaus.penrose.jdbc.JDBCClient;

/**
 * @author Endi S. Dewata
 */
public class JDBCConnectionPropertiesPage extends JDBCConnectionEditorPage {

    Text nameText;
    Text descriptionText;

    Text driverText;
    Text urlText;
    Text usernameText;
    Text passwordText;

    public JDBCConnectionPropertiesPage(JDBCConnectionEditor editor) {
        super(editor, "PROPERTIES", "  Properties  ");
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

        section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("JDBC Properties");
        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Control jdbcPropertiesControl = createJDBCPropertiesControl(section);
        section.setClient(jdbcPropertiesControl);
    }

    public void refresh() {
        nameText.setText(connectionConfig.getName() == null ? "" : connectionConfig.getName());
        descriptionText.setText(connectionConfig.getDescription() == null ? "" : connectionConfig.getDescription());

        String s = connectionConfig.getParameter(JDBCClient.DRIVER);
        driverText.setText(s == null ? "" : s);

        s = connectionConfig.getParameter(JDBCClient.URL);
        urlText.setText(s == null ? "" : s);

        s = connectionConfig.getParameter(JDBCClient.USER);
        usernameText.setText(s == null ? "" : s);

        s = connectionConfig.getParameter(JDBCClient.PASSWORD);
        passwordText.setText(s == null ? "" : s);
    }

    public Composite createPropertiesControl(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        Label connectionNameLabel = toolkit.createLabel(composite, "Name:");
        GridData gd = new GridData();
        gd.widthHint = 100;
        connectionNameLabel.setLayoutData(gd);

        nameText = toolkit.createText(composite, "", SWT.BORDER);
        nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

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

        descriptionText = toolkit.createText(composite, "", SWT.BORDER);
        descriptionText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        descriptionText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                connectionConfig.setDescription("".equals(descriptionText.getText()) ? null : descriptionText.getText());
                checkDirty();
            }
        });

        return composite;
    }

    public Composite createJDBCPropertiesControl(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        Label driverLabel = toolkit.createLabel(composite, "Driver:");
        GridData gd = new GridData();
        gd.widthHint = 100;
        driverLabel.setLayoutData(gd);

        driverText = toolkit.createText(composite, "", SWT.BORDER);
        driverText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        driverText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                connectionConfig.setParameter(JDBCClient.DRIVER, driverText.getText());
                checkDirty();
            }
        });

        toolkit.createLabel(composite, "URL:");

        urlText = toolkit.createText(composite, "", SWT.BORDER);
        urlText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        urlText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                connectionConfig.setParameter(JDBCClient.URL, urlText.getText());
                checkDirty();
            }
        });

        toolkit.createLabel(composite, "Username:");

        usernameText = toolkit.createText(composite, "", SWT.BORDER);
        usernameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        usernameText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                connectionConfig.setParameter(JDBCClient.USER, usernameText.getText());
                checkDirty();
            }
        });

        toolkit.createLabel(composite, "Password:");

        passwordText = toolkit.createText(composite, "", SWT.BORDER | SWT.PASSWORD);
        passwordText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        passwordText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                connectionConfig.setParameter(JDBCClient.PASSWORD, passwordText.getText());
                checkDirty();
            }
        });

        toolkit.createLabel(composite, "");

        Button testButton = toolkit.createButton(composite, "Test Connection", SWT.PUSH);

        testButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {

                Helper.testJdbcConnection(getEditor().getSite().getShell(), driverText.getText(), urlText.getText(), usernameText.getText(), passwordText.getText());
            }
        });

        return composite;
    }
}