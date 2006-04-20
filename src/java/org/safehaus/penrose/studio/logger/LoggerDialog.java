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
package org.safehaus.penrose.studio.logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;
import org.safehaus.penrose.studio.driver.Parameter;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseImage;

/**
 * @author Endi S. Dewata
 */
public class LoggerDialog extends Dialog {

    public final static int CANCEL = 0;
    public final static int OK     = 1;

    Shell shell;

    Text nameText;
    Combo levelCombo;

    private int action;

    String level;

	public LoggerDialog(Shell parent, int style) {
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
        shell.setImage(PenrosePlugin.getImage(PenroseImage.LOGGER));
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
        nameText.setEnabled(false);
        nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

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

        Composite buttons = new Composite(parent, SWT.NONE);
        buttons.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        buttons.setLayout(new RowLayout());

		Button saveButton = new Button(buttons, SWT.PUSH);
        saveButton.setText("Save");

		saveButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
                level = "".equals(levelCombo.getText()) ? null : levelCombo.getText();
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

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public void setLoggerName(String name) {
        nameText.setText(name);
    }

    public void setLoggerLevel(String level) {
        this.level = level;
        levelCombo.setText(level == null ? "" : level);
    }

    public String getLoggerLevel() {
        return level;
    }
}
