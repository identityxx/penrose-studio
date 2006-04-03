/**
 * Copyright (c) 2000-2005, Identyx Corporation.
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
package org.safehaus.penrose.studio.connection;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.events.*;
import org.eclipse.ui.forms.widgets.*;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.IManagedForm;
import org.safehaus.penrose.connector.JDBCAdapter;
import org.safehaus.penrose.partition.ConnectionConfig;
import org.safehaus.penrose.studio.util.Helper;
import org.safehaus.penrose.partition.Partition;
import org.apache.log4j.Logger;

/**
 * @author Endi S. Dewata
 */
public class JDBCConnectionPropertiesPage extends FormPage {

    Logger log = Logger.getLogger(getClass());
    
    FormToolkit toolkit;

    Text nameText;
    Text driverText;
    Text urlText;
    Text usernameText;
    Text passwordText;

    JDBCConnectionEditor editor;
    Partition partition;
    ConnectionConfig connection;

    public JDBCConnectionPropertiesPage(JDBCConnectionEditor editor) {
        super(editor, "PROPERTIES", "  Properties  ");

        this.editor = editor;
        this.partition = editor.getPartition();
        this.connection = editor.getConnectionConfig();
    }

    public void createFormContent(IManagedForm managedForm) {
        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText("Connection Editor");

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Connection Name");
        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Control connectionSection = createHeadSection(section);
        section.setClient(connectionSection);

        section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Connection Properties");
        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Control propertySection = createJdbcConnectionSection(section);
        section.setClient(propertySection);
    }

    public Composite createHeadSection(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        Label connectionNameLabel = toolkit.createLabel(composite, "Name:");
        GridData gd = new GridData();
        gd.widthHint = 100;
        connectionNameLabel.setLayoutData(gd);

        nameText = toolkit.createText(composite, connection.getName(), SWT.BORDER);
		nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        nameText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                connection.setName(nameText.getText());
                checkDirty();
            }
        });

        return composite;
    }

    public Composite createJdbcConnectionSection(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        Label driverLabel = toolkit.createLabel(composite, "JDBC Driver:");
        GridData gd = new GridData();
        gd.widthHint = 100;
        driverLabel.setLayoutData(gd);

        driverText = toolkit.createText(composite, "", SWT.BORDER);
		driverText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        String s = connection.getParameter(JDBCAdapter.DRIVER);
        if (s != null) driverText.setText(s);

        driverText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                connection.setParameter(JDBCAdapter.DRIVER, driverText.getText());
                checkDirty();
            }
        });

        toolkit.createLabel(composite, "JDBC URL:");

        urlText = toolkit.createText(composite, "", SWT.BORDER);
        urlText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        s = connection.getParameter(JDBCAdapter.URL);
        if (s != null) urlText.setText(s);

        urlText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                connection.setParameter(JDBCAdapter.URL, urlText.getText());
                checkDirty();
            }
        });

        toolkit.createLabel(composite, "Username:");

        usernameText = toolkit.createText(composite, "", SWT.BORDER);
        usernameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        s = connection.getParameter(JDBCAdapter.USER);
        if (s != null) usernameText.setText(s);

        usernameText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                connection.setParameter(JDBCAdapter.USER, usernameText.getText());
                checkDirty();
            }
        });

        toolkit.createLabel(composite, "Password:");

        passwordText = toolkit.createText(composite, "", SWT.BORDER | SWT.PASSWORD);
        passwordText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        s = connection.getParameter(JDBCAdapter.PASSWORD);
        if (s != null) passwordText.setText(s);

        passwordText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                connection.setParameter(JDBCAdapter.PASSWORD, passwordText.getText());
                checkDirty();
            }
        });

        toolkit.createLabel(composite, "");

        Button testButton = toolkit.createButton(composite, "Test Connection", SWT.PUSH);

        testButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                Helper.testJdbcConnection(editor.getSite().getShell(), driverText.getText(), urlText.getText(), usernameText.getText(), passwordText.getText());
            }
        });

        return composite;
    }

    public void checkDirty() {
        editor.checkDirty();
    }
}