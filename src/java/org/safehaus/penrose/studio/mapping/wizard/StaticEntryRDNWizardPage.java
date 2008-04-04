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
package org.safehaus.penrose.studio.mapping.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.ietf.ldap.LDAPDN;
import org.safehaus.penrose.ldap.DN;
import org.safehaus.penrose.studio.mapping.EntrySelectionDialog;
import org.safehaus.penrose.studio.project.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Endi S. Dewata
 */
public class StaticEntryRDNWizardPage extends WizardPage implements ModifyListener {

    Logger log = LoggerFactory.getLogger(getClass());
    
    public final static String NAME = "Entry RDN";

    Text rdnText;
    Text parentDnText;
    Button browseButton;

    private Project project;
    private String partitionName;
    private DN parentDn;

    public StaticEntryRDNWizardPage() {
        super(NAME);
        setDescription("Enter the RDN of the entry.");
    }

    public void createControl(final Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        GridLayout sectionLayout = new GridLayout();
        sectionLayout.numColumns = 3;
        composite.setLayout(sectionLayout);

        Label nameLabel = new Label(composite, SWT.NONE);
        nameLabel.setText("RDN:");
        GridData gd = new GridData(GridData.FILL);
        gd.widthHint = 50;
        nameLabel.setLayoutData(gd);

        rdnText = new Text(composite, SWT.BORDER);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        rdnText.setLayoutData(gd);
        rdnText.addModifyListener(this);

        new Label(composite, SWT.NONE);

        Label exampleLabel = new Label(composite, SWT.NONE);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        exampleLabel.setLayoutData(gd);
        exampleLabel.setText("Example: ou=Users");

        new Label(composite, SWT.NONE);

        new Label(composite, SWT.NONE);

        new Label(composite, SWT.NONE);

        Label parentDnLabel = new Label(composite, SWT.NONE);
        parentDnLabel.setText("Parent DN:");
        parentDnLabel.setLayoutData(new GridData());

        parentDnText = new Text(composite, SWT.BORDER);
        if (parentDn != null) {
            parentDnText.setText(parentDn.toString());
        }

        parentDnText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        parentDnText.addModifyListener(this);

        browseButton = new Button(composite, SWT.PUSH);
        browseButton.setText("Browse...");

        browseButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    EntrySelectionDialog dialog = new EntrySelectionDialog(parent.getShell(), SWT.NONE);
                    dialog.setText("Select parent entry...");
                    dialog.setPartitionName(partitionName);
                    dialog.setProject(project);
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

        setPageComplete(validatePage());
    }

    public String getRdn() {
        return "".equals(rdnText.getText()) ? null : rdnText.getText();
    }

    public String getParentDn() {
        return "".equals(parentDnText.getText()) ? null : parentDnText.getText();
    }

    public boolean validatePage() {
        String rdn = getRdn();
        if (rdn == null || !LDAPDN.isValid(rdn)) return false;
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

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public void setParentDn(DN parentDn) {
        this.parentDn = parentDn;
    }
}
