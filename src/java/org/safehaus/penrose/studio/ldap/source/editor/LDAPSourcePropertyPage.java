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
package org.safehaus.penrose.studio.ldap.source.editor;

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
import org.safehaus.penrose.studio.source.editor.SourceEditorPage;
import org.safehaus.penrose.studio.ldap.source.wizard.LDAPSourcePropertiesWizard;
import org.safehaus.penrose.ldap.source.LDAPSource;

public class LDAPSourcePropertyPage extends SourceEditorPage {

    Label connectionText;
    Label baseDnText;
    Label filterText;
	Label scopeCombo;
    Label objectClassesText;

	String[] scopes = new String[] { "OBJECT", "ONELEVEL", "SUBTREE" };

    public LDAPSourcePropertyPage(LDAPSourceEditor editor) throws Exception {
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

	public Composite createLDAPLeftControl(Composite parent) {

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

		toolkit.createLabel(composite, "Base DN:");

        baseDnText = toolkit.createLabel(composite, "", SWT.NONE);
        baseDnText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		toolkit.createLabel(composite, "Filter:");

        filterText = toolkit.createLabel(composite, "", SWT.NONE);
        filterText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		toolkit.createLabel(composite, "Scope:");

		scopeCombo = toolkit.createLabel(composite, "", SWT.NONE);
        scopeCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        toolkit.createLabel(composite, "Object Classes:");

        objectClassesText = toolkit.createLabel(composite, "", SWT.NONE);
        objectClassesText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

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
                    LDAPSourcePropertiesWizard wizard = new LDAPSourcePropertiesWizard();
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
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });

        return composite;
    }

    public void refresh() {

        String connection = sourceConfig.getConnectionName();
        connectionText.setText(connection == null ? "" : connection);

        String baseDn = sourceConfig.getParameter(LDAPSource.BASE_DN);
        baseDnText.setText(baseDn == null ? "" : baseDn);

        String filter = sourceConfig.getParameter(LDAPSource.FILTER);
        filterText.setText(filter == null ? "" : filter);

        String scope = sourceConfig.getParameter(LDAPSource.SCOPE);
        scopeCombo.setText(scope == null ? "" : scope);

        String objectClasses = sourceConfig.getParameter(LDAPSource.OBJECT_CLASSES);
        objectClassesText.setText(objectClasses == null ? "" : objectClasses);
    }
}
