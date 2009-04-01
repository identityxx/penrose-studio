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
package org.safehaus.penrose.studio.user.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.ietf.ldap.LDAPDN;

public class AdministratorWizardPage extends WizardPage {

    Logger log = LoggerFactory.getLogger(getClass());

    public final static String NAME = "Administrator Properties";

	Text dnText;
	Text passwordText;
    Text retypeText;

    private String dn;

    public AdministratorWizardPage() {
        super(NAME);
        setDescription("Enter administrator's DN and password.");
    }

    public void createControl(final Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        composite.setLayout(new GridLayout(2, false));

		Label usernameLabel = new Label(composite, SWT.NONE);
        usernameLabel.setText("DN:");

		dnText = new Text(composite, SWT.BORDER);
        dnText.setText(dn == null ? "" : dn);
        dnText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        dnText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                setPageComplete(validatePage());
            }
        });

		Label passwordLabel = new Label(composite, SWT.NONE);
        passwordLabel.setText("Password:");

		passwordText = new Text(composite, SWT.BORDER | SWT.PASSWORD);
		passwordText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        passwordText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                setPageComplete(validatePage());
            }
        });

        Label retypeLabel = new Label(composite, SWT.NONE);
        retypeLabel.setText("Retype:");

        retypeText = new Text(composite, SWT.BORDER | SWT.PASSWORD);
        retypeText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        retypeText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                setPageComplete(validatePage());
            }
        });

        setPageComplete(validatePage());
    }

    public boolean validatePage() {

        String dn = getDn();
        if (dn == null) return false;

        if (!LDAPDN.isValid(dn)) return false;

        String password = getPassword();
        String retype = getRetype();

        if (password != null || retype != null) {
            if (password == null) return false;
            if (!password.equals(retype)) return false;
        }

        return true;
    }

    public String getDn() {
        String s = dnText.getText().trim();
        return "".equals(s) ? null : s;
    }

    public void setDn(String dn) {
        this.dn = dn;
    }

    public String getPassword() {
        String s = passwordText.getText().trim();
        return "".equals(s) ? null : s;
    }

    public String getRetype() {
        String s = retypeText.getText().trim();
        return "".equals(s) ? null : s;
    }
}