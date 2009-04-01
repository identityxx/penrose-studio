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
package org.safehaus.penrose.studio.directory.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.safehaus.penrose.ldap.DN;
import org.safehaus.penrose.ldap.RDN;
import org.safehaus.penrose.studio.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Endi S. Dewata
 */
public class EntryRDNWizardPage extends WizardPage implements ModifyListener {

    Logger log = LoggerFactory.getLogger(getClass());
    
    public final static String NAME = "Entry RDN";

    Text rdnText;
    Label parentDnText;
    Button browseButton;

    private Server server;
    private String partitionName;
    private RDN rdn;
    private DN parentDn;

    public EntryRDNWizardPage() {
        super(NAME);
    }

    public void createControl(final Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        composite.setLayout(new GridLayout(2, false));

        Label nameLabel = new Label(composite, SWT.NONE);
        nameLabel.setText("RDN:");
        nameLabel.setLayoutData(new GridData());

        rdnText = new Text(composite, SWT.BORDER);

        rdnText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        rdnText.addModifyListener(this);

        new Label(composite, SWT.NONE);

        Label exampleRdnLabel = new Label(composite, SWT.NONE);
        exampleRdnLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        exampleRdnLabel.setText("Example: ou=Users");

        new Label(composite, SWT.NONE);
        new Label(composite, SWT.NONE);

        Label parentDnLabel = new Label(composite, SWT.NONE);
        parentDnLabel.setText("Parent DN:");
        parentDnLabel.setLayoutData(new GridData());

        parentDnText = new Label(composite, SWT.NONE);
        parentDnText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        //parentDnText.addModifyListener(this);
/*
        browseButton = new Button(composite, SWT.PUSH);
        browseButton.setText("Browse...");

        browseButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    EntrySelectionDialog dialog = new SelectEntryDialog(parent.getShell(), SWT.NONE);
                    dialog.setText("Select parent entry...");
                    dialog.setServer(server);
                    dialog.setPartitionName(partitionName);
                    dialog.open();

                    DN dn = dialog.getDn();
                    if (dn == null) return;

                    parentDnText.setText(dn.toString());
                    
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });
*/
        rdnText.setText(rdn == null ? "" : rdn.toString());
        parentDnText.setText(parentDn == null ? "" : parentDn.toString());

        setPageComplete(validatePage());
    }

    public RDN getRdn() {
        if ("".equals(rdnText.getText())) return null;

        try {
            return new RDN(rdnText.getText());

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public String getParentDn() {
        if ("".equals(parentDnText.getText())) return null;

        try {
            return parentDnText.getText();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public boolean validatePage() {
        RDN rdn = getRdn();
        if (rdn == null) return false;
        return true;
    }

    public void modifyText(ModifyEvent event) {
        setPageComplete(validatePage());
    }

    public String getPartitionName() {
        return partitionName;
    }

    public void setPartitionName(String partitionName) {
        this.partitionName = partitionName;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public void setRdn(RDN rdn) {
        this.rdn = rdn;
    }

    public void setParentDn(DN parentDn) {
        this.parentDn = parentDn;
    }
}
