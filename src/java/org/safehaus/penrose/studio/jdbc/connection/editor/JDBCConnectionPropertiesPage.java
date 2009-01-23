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
import org.safehaus.penrose.studio.jdbc.connection.wizard.JDBCConnectionPropertiesWizard;
import org.safehaus.penrose.jdbc.JDBCClient;

/**
 * @author Endi S. Dewata
 */
public class JDBCConnectionPropertiesPage extends ConnectionEditorPage {

    Label adapterText;
    Label driverText;
    Label urlText;
    Label usernameText;
    Label passwordText;

    public JDBCConnectionPropertiesPage(JDBCConnectionEditor editor) {
        super(editor, "JDBC", "JDBC");
    }

    public void createFormContent(IManagedForm managedForm) {
        super.createFormContent(managedForm);

        ScrolledForm form = managedForm.getForm();

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section jdbcSection = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        jdbcSection.setText("JDBC");
        jdbcSection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Control jdbcControl = createJDBCControl(jdbcSection);
        jdbcSection.setClient(jdbcControl);
    }

    public Composite createJDBCControl(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        Composite leftControl = createJDBCLeftControl(composite);
        leftControl.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite rightControl = createJDBCRightControl(composite);
        GridData gd = new GridData(GridData.FILL_VERTICAL);
        gd.widthHint = 100;
        rightControl.setLayoutData(gd);

        return composite;
    }

    public Composite createJDBCLeftControl(Composite parent) {

        Composite composite = toolkit.createComposite(parent);

        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);

        toolkit.createLabel(composite, "Adapter:");

        adapterText = toolkit.createLabel(composite, "", SWT.READ_ONLY);
        adapterText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        toolkit.createLabel(composite, "Driver:");

        driverText = toolkit.createLabel(composite, "", SWT.NONE);
        driverText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        toolkit.createLabel(composite, "URL:");

        urlText = toolkit.createLabel(composite, "", SWT.NONE);
        urlText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        toolkit.createLabel(composite, "Username:");

        usernameText = toolkit.createLabel(composite, "", SWT.NONE);
        usernameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        toolkit.createLabel(composite, "Password:");

        passwordText = toolkit.createLabel(composite, "", SWT.PASSWORD );
        passwordText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        return composite;
    }

    public Composite createJDBCRightControl(final Composite parent) {

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
                    JDBCConnectionPropertiesWizard wizard = new JDBCConnectionPropertiesWizard();
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

        String adapter = connectionConfig.getAdapterName();
        adapterText.setText(adapter == null ? "" : adapter);

        String s = connectionConfig.getParameter(JDBCClient.DRIVER);
        driverText.setText(s == null ? "" : s);

        s = connectionConfig.getParameter(JDBCClient.URL);
        urlText.setText(s == null ? "" : s);

        s = connectionConfig.getParameter(JDBCClient.USER);
        usernameText.setText(s == null ? "" : s);

        passwordText.setText("*****");
    }
}
