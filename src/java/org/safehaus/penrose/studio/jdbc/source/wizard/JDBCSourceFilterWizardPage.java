/**
 * Copyright 2009 Red Hat, Inc.
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
package org.safehaus.penrose.studio.jdbc.source.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.apache.log4j.Logger;

/**
 * @author Endi S. Dewata
 */
public class JDBCSourceFilterWizardPage extends WizardPage {

    Logger log = Logger.getLogger(getClass());

    public final static String NAME = "SQL Filter";

    Text filterText;

    private String filter;

    public JDBCSourceFilterWizardPage() {
        super(NAME);

        setDescription(
                "Enter SQL filter.\n"+
                "Example: active = 'Y' and level < 5"
        );
    }

    public void createControl(final Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        composite.setLayout(new GridLayout(2, false));

        Label filterLabel = new Label(composite, SWT.NONE);
        filterLabel.setText("SQL Filter:");


        GridData gd = new GridData();
        gd.verticalAlignment = GridData.BEGINNING;
        filterLabel.setLayoutData(gd);

        filterText = new Text(composite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
        filterText.setLayoutData(new GridData(GridData.FILL_BOTH));

        if (filter != null) filterText.setText(filter);

        setPageComplete(validatePage());
    }

    public void setVisible(boolean b) {
        super.setVisible(b);
        if (b) refresh();
    }

    public void refresh() {
        try {
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }

    public boolean validatePage() {
        return true;
    }

    public String getFilter() {
        return "".equals(filterText.getText()) ? null : filterText.getText();
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }
}