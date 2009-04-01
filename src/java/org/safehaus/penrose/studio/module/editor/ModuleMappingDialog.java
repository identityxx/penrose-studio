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
package org.safehaus.penrose.studio.module.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;
import org.safehaus.penrose.module.ModuleMapping;
import org.safehaus.penrose.ldap.DN;
import org.apache.log4j.Logger;

public class ModuleMappingDialog extends Dialog {

    Logger log = Logger.getLogger(getClass());

    public final static int CANCEL = 0;
    public final static int OK     = 1;

    Shell shell;

	Text baseDnText;
	Text filterText;
	Combo scopeCombo;

    int action;

	ModuleMapping moduleMapping;
	
	String[] scopes = new String[] { "OBJECT", "ONELEVEL", "SUBTREE" };

	public ModuleMappingDialog(Shell parent, int style) {
		super(parent, style);
    }

    public void open () {

        shell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);

        createControl(shell);

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

		Label baseDnLabel = new Label(composite, SWT.NONE);
        baseDnLabel.setText("Base DN:");

		baseDnText = new Text(composite, SWT.BORDER);
		baseDnText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        DN baseDn = moduleMapping.getBaseDn();
        baseDnText.setText(baseDn == null ? "" : baseDn.toString());

		Label filterLabel = new Label(composite, SWT.NONE);
        filterLabel.setText("Filter:");

		filterText = new Text(composite, SWT.BORDER);
        filterText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        String filter = moduleMapping.getFilter();
        filterText.setText(filter == null ? "" : filter);

		Label scopeLabel = new Label(composite, SWT.NONE);
        scopeLabel.setText("Scope:");

		scopeCombo = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
		scopeCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        for (String scope : scopes) {
            scopeCombo.add(scope);
        }

        String scope = moduleMapping.getScope();
        scopeCombo.setText(scope == null ? "" : scope);
/*
        for (int i=0; i<scopes.length; i++) {
            if (scopes[i].equals(scope)) {
                scopeCombo.select(i);
            }
        }
*/
        Composite buttons = new Composite(parent, SWT.NONE);
        buttons.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        buttons.setLayout(new RowLayout());

		Button saveButton = new Button(buttons, SWT.PUSH);
        saveButton.setText("Save");

		saveButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
                try {
                    store();
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

    public void setModuleMapping(ModuleMapping moduleMapping) {
        this.moduleMapping = moduleMapping;
    }

    public void store() throws Exception {
        moduleMapping.setBaseDn(baseDnText.getText());
        moduleMapping.setFilter(filterText.getText());
        moduleMapping.setScope(scopeCombo.getText());
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }
}
