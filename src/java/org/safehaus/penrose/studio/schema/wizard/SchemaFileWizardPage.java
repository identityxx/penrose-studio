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
package org.safehaus.penrose.studio.schema.wizard;

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
public class SchemaFileWizardPage extends WizardPage implements ModifyListener {

    public final static String NAME = "Schema File";

    Text filenameText;
    Button browseButton;

    public SchemaFileWizardPage() {
        super(NAME);
        setDescription("Enter the schema file to import.");
    }

    public void createControl(Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        composite.setLayout(new GridLayout(2, false));

        Label locationLabel = new Label(composite, SWT.NONE);
        locationLabel.setText("File:");
        GridData gd = new GridData(GridData.FILL);
        gd.widthHint = 50;
        locationLabel.setLayoutData(gd);

        filenameText = new Text(composite, SWT.BORDER);
        filenameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        filenameText.addModifyListener(this);

        browseButton = new Button(composite, SWT.PUSH);
        browseButton.setLayoutData(new GridData(GridData.FILL));
        browseButton.setText("Browse...");

        browseButton.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent event) {
                IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

                String dir = System.getProperty("user.dir");

                FileDialog dialog = new FileDialog(window.getShell());
                dialog.setText("Import");
                dialog.setFilterPath(dir);
                dialog.setFilterExtensions(new String[] { "*.schema", "*.*" });

                String filename = dialog.open();
                if (filename == null) return;

                filenameText.setText(filename);
            }
            public void widgetDefaultSelected(SelectionEvent event) {
            }
        });
        
        setPageComplete(validatePage());
    }

    public String getFilename() {
        return filenameText.getText().trim();
    }

    public boolean validatePage() {
        if ("".equals(getFilename())) return false;
        return true;
    }

    public void modifyText(ModifyEvent event) {
        setPageComplete(validatePage());
    }
}
