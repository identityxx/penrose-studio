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
package org.safehaus.penrose.studio.mapping.wizard;

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
public class BinaryExportWizardPage extends WizardPage implements ModifyListener {

    public final static String NAME = "Export Binary Data";

    Text fileNameText;
    Text directoryText;
    Button browseButton;

    public BinaryExportWizardPage() {
        super(NAME);
        setDescription("Enter the filename and directory to which the binary data will be exported.");
    }

    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        composite.setLayout(new GridLayout(3, false));

        Label nameLabel = new Label(composite, SWT.NONE);
        nameLabel.setText("File Name:");
        GridData gd = new GridData();
        gd.widthHint = 80;
        nameLabel.setLayoutData(gd);

        fileNameText = new Text(composite, SWT.BORDER);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        fileNameText.setLayoutData(gd);

        fileNameText.addModifyListener(this);

        Label locationLabel = new Label(composite, SWT.NONE);
        locationLabel.setText("Directory:");
        gd = new GridData(GridData.FILL);
        locationLabel.setLayoutData(new GridData());

        directoryText = new Text(composite, SWT.BORDER);
        directoryText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        directoryText.addModifyListener(this);

        browseButton = new Button(composite, SWT.PUSH);
        browseButton.setLayoutData(new GridData(GridData.FILL));
        browseButton.setText("Browse...");

        browseButton.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent event) {
                IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

                FileDialog dialog = new FileDialog(window.getShell(), SWT.SAVE);
                dialog.setText("Export Binary Data");

                String filename = dialog.open();
                if (filename == null) return;

                directoryText.setText(filename);
            }
            public void widgetDefaultSelected(SelectionEvent event) {
            }
        });

        setPageComplete(validatePage());
    }

    public String getFileName() {
        return fileNameText.getText().trim();
    }

    public String getDirectory() {
        return directoryText.getText().trim();
    }

    public boolean validatePage() {
        if ("".equals(getFileName())) return false;
        if ("".equals(getDirectory())) return false;
        return true;
    }

    public void modifyText(ModifyEvent event) {
        setPageComplete(validatePage());
    }
}
