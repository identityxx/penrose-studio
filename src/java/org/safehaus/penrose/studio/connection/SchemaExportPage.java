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
package org.safehaus.penrose.studio.connection;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * @author Endi S. Dewata
 */
public class SchemaExportPage extends WizardPage implements ModifyListener {

    public final static String NAME = "Schema Name";

    Text fileNameText;
    Button browseButton;

    //Text schemaNameText;
    Button excludeDuplicateCheckbox;

    public SchemaExportPage() {
        super(NAME);
    }

    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        composite.setLayout(new GridLayout(3, false));

        Label locationLabel = new Label(composite, SWT.NONE);
        locationLabel.setText("Location:");
        GridData gd = new GridData(GridData.FILL);
        gd.widthHint = 50;
        locationLabel.setLayoutData(gd);

        fileNameText = new Text(composite, SWT.BORDER);
        fileNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        fileNameText.addModifyListener(this);

        browseButton = new Button(composite, SWT.PUSH);
        browseButton.setLayoutData(new GridData(GridData.FILL));
        browseButton.setText("Browse...");

        browseButton.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent event) {
                IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

                FileDialog dialog = new FileDialog(window.getShell(), SWT.SAVE);
                dialog.setText("Export");

                String filename = dialog.open();
                if (filename == null) return;

                fileNameText.setText(filename);
            }
            public void widgetDefaultSelected(SelectionEvent event) {
            }
        });
/*
        Label nameLabel = new Label(composite, SWT.NONE);
        nameLabel.setText("Name:");
        GridData gd = new GridData();
        gd.widthHint = 50;
        nameLabel.setLayoutData(gd);

        schemaNameText = new Text(composite, SWT.BORDER);
        schemaNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        schemaNameText.addModifyListener(this);
*/
        Label separatorLabel = new Label(composite, SWT.NONE);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        separatorLabel.setLayoutData(gd);

        excludeDuplicateCheckbox = new Button(composite, SWT.CHECK);
        excludeDuplicateCheckbox.setSelection(true);
        excludeDuplicateCheckbox.setText("Exclude attribute types and object classes already defined in Penrose.");
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        excludeDuplicateCheckbox.setLayoutData(gd);

        setPageComplete(validatePage());
    }
/*
    public String getSchemaName() {
        return schemaNameText.getText().trim();
    }
*/
    public String getPath() {
        return fileNameText.getText().trim();
    }

    public boolean getExcludeDuplicate() {
        return excludeDuplicateCheckbox.getSelection();
    }

    public boolean validatePage() {
        if ("".equals(getPath())) return false;
        return true;
    }

    public void modifyText(ModifyEvent event) {
        setPageComplete(validatePage());
    }
}
