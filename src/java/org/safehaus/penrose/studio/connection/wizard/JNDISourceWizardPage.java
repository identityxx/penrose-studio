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
package org.safehaus.penrose.studio.connection.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;

/**
 * @author Endi S. Dewata
 */
public class JNDISourceWizardPage extends WizardPage implements SelectionListener, ModifyListener {

    public final static String NAME = "Source Property";

    Text nameText;
    Text baseDnText;
    Text filterText;
    Combo scopeCombo;

    String sourceName;
    String baseDn;
    String filter;
    String scope;

    public JNDISourceWizardPage(String sourceName, String baseDn) {
        this(sourceName, baseDn, "(objectClass=*)", "OBJECT");
    }

    public JNDISourceWizardPage(String sourceName, String baseDn, String filter, String scope) {
        super(NAME);

        this.sourceName = sourceName;
        this.baseDn = baseDn;
        this.filter = filter;
        this.scope = scope;

        setDescription("Enter the name of the source.");
    }

    public void createControl(Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        GridLayout sectionLayout = new GridLayout();
        sectionLayout.numColumns = 2;
        composite.setLayout(sectionLayout);

        Label nameLabel = new Label(composite, SWT.NONE);
        nameLabel.setText("Name:");
        GridData gd = new GridData(GridData.FILL);
        gd.widthHint = 50;
        nameLabel.setLayoutData(gd);

        nameText = new Text(composite, SWT.BORDER);
        nameText.setText(sourceName);
        nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        nameText.addModifyListener(this);

        Label separator = new Label(composite, SWT.NONE);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        separator.setLayoutData(gd);

        Label baseDnLabel = new Label(composite, SWT.NONE);
        baseDnLabel.setText("Base DN:");
        baseDnLabel.setLayoutData(new GridData(GridData.FILL));

        baseDnText = new Text(composite, SWT.BORDER | SWT.READ_ONLY);
        baseDnText.setText(baseDn == null ? "" : baseDn);
        baseDnText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label filterLabel = new Label(composite, SWT.NONE);
        filterLabel.setText("Filter:");
        filterLabel.setLayoutData(new GridData(GridData.FILL));

        filterText = new Text(composite, SWT.BORDER);
        filterText.setText(filter);
        filterText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label scopeLabel = new Label(composite, SWT.NONE);
        scopeLabel.setText("Scope:");
        scopeLabel.setLayoutData(new GridData(GridData.FILL));

        scopeCombo = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
        scopeCombo.add("OBJECT");
        scopeCombo.add("ONELEVEL");
        scopeCombo.add("SUBTREE");
        scopeCombo.setText(scope);
        scopeCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        setPageComplete(validatePage());
    }

    public String getSourceName() {
        return nameText.getText().trim();
    }

    public boolean validatePage() {
        if ("".equals(getSourceName())) return false;
        if ("".equals(getFilter())) return false;
        if ("".equals(getScope())) return false;
        return true;
    }

    public void widgetSelected(SelectionEvent event) {
        setPageComplete(validatePage());
    }

    public void widgetDefaultSelected(SelectionEvent event) {
    }

    public void modifyText(ModifyEvent event) {
        setPageComplete(validatePage());
    }
    public String getFilter() {
        return filterText.getText().trim();
    }

    public String getScope() {
        return scopeCombo.getText().trim();
    }
}
