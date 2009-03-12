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
package org.safehaus.penrose.studio.jdbc.source.editor;

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
import org.safehaus.penrose.jdbc.JDBC;
import org.safehaus.penrose.studio.source.editor.SourceEditorPage;
import org.safehaus.penrose.studio.jdbc.source.wizard.JDBCSourcePropertiesWizard;
import org.safehaus.penrose.studio.dialog.ErrorDialog;

public class JDBCSourcePropertyPage extends SourceEditorPage {

    Label connectionText;
    Label catalogText;
    Label schemaText;
	Label tableText;
    Label filterText;

    public JDBCSourcePropertyPage(JDBCSourceEditor editor) {
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

        refresh();
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

        Label connectionLabel = toolkit.createLabel(composite, "Connection:");
        GridData gd = new GridData();
        gd.widthHint = 100;
        connectionLabel.setLayoutData(gd);

        connectionText = toolkit.createLabel(composite, "", SWT.READ_ONLY);
        connectionText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        toolkit.createLabel(composite, "Catalog:");

        catalogText = toolkit.createLabel(composite, "", SWT.NONE);
        catalogText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label schemaLabel = toolkit.createLabel(composite, "Schema:");
        gd = new GridData();
        gd.widthHint = 100;
        schemaLabel.setLayoutData(gd);

        schemaText = toolkit.createLabel(composite, "", SWT.NONE);
        schemaText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label tableLabel = toolkit.createLabel(composite, "Table:");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        tableLabel.setLayoutData(gd);

		tableText = toolkit.createLabel(composite, "", SWT.NONE);
        tableText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label filterLabel = toolkit.createLabel(composite, "Filter:");
        gd = new GridData();
        gd.widthHint = 100;
        filterLabel.setLayoutData(gd);

        filterText = toolkit.createLabel(composite, "", SWT.NONE);
        filterText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

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
                    JDBCSourcePropertiesWizard wizard = new JDBCSourcePropertiesWizard();
                    wizard.setServer(server);
                    wizard.setPartitionName(partitionName);
                    wizard.setSourceConfig(sourceConfig);

                    WizardDialog dialog = new WizardDialog(editor.getSite().getShell(), wizard);
                    dialog.setPageSize(600, 300);

                    int rc = dialog.open();
                    if (rc == Window.CANCEL) return;

                    editor.store();
                    refresh();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        return composite;
    }

    public void refresh() {

        String connection = sourceConfig.getConnectionName();
        connectionText.setText(connection == null ? "" : connection);

        String catalog = sourceConfig.getParameter(JDBC.CATALOG);
        catalogText.setText(catalog == null ? "" : catalog);

        String schema = sourceConfig.getParameter(JDBC.SCHEMA);
        schemaText.setText(schema == null ? "" : schema);

        String table = sourceConfig.getParameter(JDBC.TABLE);
        tableText.setText(table == null ? "" : table);

        String filter = sourceConfig.getParameter(JDBC.FILTER);
        filterText.setText(filter == null ? "" : filter);
    }

    public void checkDirty() {
    }
}
