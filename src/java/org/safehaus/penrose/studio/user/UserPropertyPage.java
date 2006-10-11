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
package org.safehaus.penrose.studio.user;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.*;
import org.safehaus.penrose.studio.user.UserEditor;
import org.safehaus.penrose.user.UserConfig;

/**
 * @author Endi S. Dewata
 */
public class UserPropertyPage {

    FormToolkit toolkit;

    Text dnText;
	Text passwordText;

    UserEditor editor;
    UserConfig userConfig;

    public UserPropertyPage(UserEditor editor) {
        this.editor = editor;
        this.userConfig = editor.userConfig;
    }

    public Control createControl() {
        toolkit = new FormToolkit(editor.getParent().getDisplay());

        Form form = toolkit.createForm(editor.getParent());
        form.setText("User Editor");

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Properties");
        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Control propertiesSection = createPropertiesSection(section);
        section.setClient(propertiesSection);

        return form;
	}

	public Composite createPropertiesSection(Composite parent) {

		Composite composite = toolkit.createComposite(parent);
		composite.setLayout(new GridLayout(2, false));

		Label dnLabel = toolkit.createLabel(composite, "DN:");
        GridData gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        dnLabel.setLayoutData(gd);


		dnText = toolkit.createText(composite, userConfig.getDn(), SWT.BORDER);
        gd = new GridData(GridData.FILL_HORIZONTAL);
		dnText.setLayoutData(gd);

        dnText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                userConfig.setDn(dnText.getText());
                checkDirty();
            }
        });

		Label passwordLabel = toolkit.createLabel(composite, "Password:");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        passwordLabel.setLayoutData(gd);

        passwordText = toolkit.createText(composite, userConfig.getPassword(), SWT.BORDER);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        passwordText.setLayoutData(gd);

        passwordText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                userConfig.setPassword(passwordText.getText());
                checkDirty();
            }
        });

		return composite;
	}
	
    public void load() {
    }

    public void checkDirty() {
        editor.checkDirty();
    }

    public void dispose() {
        toolkit.dispose();
    }
}
