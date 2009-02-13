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
package org.safehaus.penrose.studio.log.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;

/**
 * @author Endi S. Dewata
 */
public class RootLoggerPropertiesWizardPage extends WizardPage {

    public final static String NAME = "Logger Properties";

    Combo levelCombo;

    private String level;

    public RootLoggerPropertiesWizardPage() {
        super(NAME);
        setDescription("Enter the logger properties.");
    }

    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        composite.setLayout(new GridLayout(2, false));

        Label typeLabel = new Label(composite, SWT.NONE);
        typeLabel.setText("Level:");

        levelCombo = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
        levelCombo.add("");
        levelCombo.add("OFF");
        levelCombo.add("FATAL");
        levelCombo.add("ERROR");
        levelCombo.add("WARN");
        levelCombo.add("INFO");
        levelCombo.add("DEBUG");
        levelCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        levelCombo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                level = levelCombo.getText().trim();
                level = "".equals(level) ? null : level;
                setPageComplete(validatePage());
            }
        });

        setPageComplete(validatePage());
    }

    public void setVisible(boolean b) {
        super.setVisible(b);
        if (b) refresh();
    }

    public void refresh() {
        levelCombo.setText(level == null ? "" : level);
    }

    public boolean validatePage() {
        return true;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }
}