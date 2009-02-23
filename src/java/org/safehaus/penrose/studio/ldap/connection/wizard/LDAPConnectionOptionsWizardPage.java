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
package org.safehaus.penrose.studio.ldap.connection.wizard;

import org.apache.log4j.Logger;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.safehaus.penrose.studio.server.Server;

/**
 * @author Endi S. Dewata
 */
public class LDAPConnectionOptionsWizardPage extends WizardPage {

    Logger log = Logger.getLogger(getClass());

    public final static String NAME = "LDAP Connection Options";

    private Server server;
    private Long sizeLimit;

    Text sizeLimitText;

    public LDAPConnectionOptionsWizardPage() {
        super(NAME);
        setDescription("Enter LDAP connection options.");
    }

    public void createControl(final Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);

        Composite bottomControl = createBottomControl(composite);
        bottomControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        setPageComplete(validatePage());
    }

    public Composite createBottomControl(final Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));

        Label sizeLimitLabel = new Label(composite, SWT.NONE);
        sizeLimitLabel.setText("Size Limit:");

        sizeLimitText = new Text(composite, SWT.BORDER);
        sizeLimitText.setText(sizeLimit == null ? "" : sizeLimit.toString());

        GridData gd = new GridData();
        gd.widthHint = 100;
        sizeLimitText.setLayoutData(gd);

        sizeLimitText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                String s = sizeLimitText.getText().trim();
                sizeLimit = "".equals(s) ? null : Long.parseLong(s);
            }
        });

        return composite;
    }

    public boolean validatePage() {
        return true;
    }

    public Long getSizeLimit() {
        return sizeLimit;
    }

    public void setSizeLimit(Long sizeLimit) {
        this.sizeLimit = sizeLimit;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }
}