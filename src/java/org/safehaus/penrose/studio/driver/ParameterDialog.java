/**
 * Copyright (c) 2000-2005, Identyx Corporation.
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
package org.safehaus.penrose.studio.driver;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;

/**
 * @author Endi S. Dewata
 */
public class ParameterDialog extends Dialog {

    public final static int CANCEL = 0;
    public final static int OK     = 1;

    Shell shell;

    Text nameText;
    Combo typeCombo;
    Text displayNameText;
    Text defaultValue;

    private int action;

    private Parameter parameter;

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

        Label nameLabel = new Label(composite, SWT.NONE);
        nameLabel.setText("Name:");

        nameText = new Text(composite, SWT.BORDER);
        nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label typeLabel = new Label(composite, SWT.NONE);
        typeLabel.setText("Type:");

        typeCombo = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
        typeCombo.add("NORMAL");
        typeCombo.add("REQUIRED");
        typeCombo.add("HIDDEN");
        typeCombo.add("PASSWORD");
        typeCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label descriptionLabel = new Label(composite, SWT.NONE);
        descriptionLabel.setText("Display Name:");

        displayNameText = new Text(composite, SWT.BORDER);
        displayNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label defaultValueLabel = new Label(composite, SWT.NONE);
        defaultValueLabel.setText("Default Value:");

        defaultValue = new Text(composite, SWT.BORDER);
        defaultValue.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Composite buttons = new Composite(parent, SWT.NONE);
        buttons.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        buttons.setLayout(new RowLayout());

		Button saveButton = new Button(buttons, SWT.PUSH);
        saveButton.setText("Save");

		saveButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
                parameter.setName(nameText.getText());
                parameter.setTypeAsString(typeCombo.getText());
                parameter.setDisplayName(displayNameText.getText());
                parameter.setDefaultValue(defaultValue.getText().equals("") ? null : defaultValue.getText());
                action = OK;
                shell.close();
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

    public Parameter getParameter() {
        return parameter;
    }

    public void setParameter(Parameter parameter) {
        this.parameter = parameter;
        nameText.setText(parameter.getName() == null ? "" : parameter.getName());
        typeCombo.setText(parameter.getTypeAsString() == null ? "" : parameter.getTypeAsString());
        displayNameText.setText(parameter.getDisplayName() == null ? "" : parameter.getDisplayName());
        defaultValue.setText(parameter.getDefaultValue() == null ? "" : parameter.getDefaultValue());
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }
}
