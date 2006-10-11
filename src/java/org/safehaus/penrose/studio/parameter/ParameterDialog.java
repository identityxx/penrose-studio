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
package org.safehaus.penrose.studio.parameter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.apache.log4j.Logger;

public class ParameterDialog extends Dialog {

    Logger log = Logger.getLogger(getClass());

    public final static int CANCEL = 0;
    public final static int OK     = 1;

    Shell shell;

	Text nameText;
	Text valueText;

    int action;

    String name;
    String value;

	public ParameterDialog(Shell parent, int style) {
		super(parent, style);

        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);

        createControl(shell);
    }

    public void open () {

        Point size = new Point(400, 300);
        shell.setSize(size);

        Point l = getParent().getLocation();
        Point s = getParent().getSize();

        shell.setLocation(l.x + (s.x - size.x)/2, l.y + (s.y - size.y)/2);

        shell.setText(getText());
        shell.setImage(PenrosePlugin.getImage(PenroseImage.LOGO16));
        shell.open();

        Display display = getParent().getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
    }

    public void createControl(final Shell parent) {

        parent.setLayout(new GridLayout());

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        composite.setLayout(layout);

		Label baseDnLabel = new Label(composite, SWT.NONE);
        baseDnLabel.setText("Name:");

		nameText = new Text(composite, SWT.BORDER);
		nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label filterLabel = new Label(composite, SWT.NONE);
        filterLabel.setText("Value:");
        GridData gd = new GridData(GridData.FILL);
        gd.verticalAlignment = GridData.BEGINNING;
        filterLabel.setLayoutData(gd);

		valueText = new Text(composite, SWT.BORDER | SWT.MULTI);
        valueText.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite buttons = new Composite(parent, SWT.NONE);
        buttons.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        buttons.setLayout(new RowLayout());

		Button saveButton = new Button(buttons, SWT.PUSH);
        saveButton.setText("Save");

		saveButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
                try {
                    name = nameText.getText();
                    value = valueText.getText();
                    action = OK;
                    shell.close();
                } catch (Exception ex) {
                    log.debug(ex.getMessage(), ex);
                }
			}
		});

		Button cancelButton = new Button(buttons, SWT.PUSH);
        cancelButton.setText("Cancel");

		cancelButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				shell.close();
			}
		});
	}

    public void setName(String name) {
        this.name = name;
        nameText.setText(name == null ? "" : name);
    }

    public void setValue(String value) {
        this.value = value;
        valueText.setText(value == null ? "" : value);
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }
}
