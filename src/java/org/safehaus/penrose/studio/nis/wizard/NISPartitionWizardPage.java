package org.safehaus.penrose.studio.nis.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Endi Sukma Dewata
 */
public class NISPartitionWizardPage extends WizardPage implements ModifyListener {

    public Logger log = LoggerFactory.getLogger(getClass());

    public final static String NAME = "NIS Partition";

    Text partitionText;
    Text suffixText;

    boolean visited;

    public NISPartitionWizardPage() {
        super(NAME);

        setDescription("Enter a partition name and an LDAP suffix.");
    }

    public void createControl(final Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        GridLayout sectionLayout = new GridLayout();
        sectionLayout.numColumns = 2;
        composite.setLayout(sectionLayout);

        Label domainLabel = new Label(composite, SWT.NONE);
        domainLabel.setText("Partition:");
        GridData gd = new GridData();
        gd.widthHint = 100;
        domainLabel.setLayoutData(gd);

        partitionText = new Text(composite, SWT.BORDER);
        partitionText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        partitionText.addModifyListener(this);

        Label serverLabel = new Label(composite, SWT.NONE);
        serverLabel.setText("Suffix:");
        serverLabel.setLayoutData(new GridData());

        suffixText = new Text(composite, SWT.BORDER);
        suffixText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        suffixText.addModifyListener(this);
        
        setPageComplete(validatePage());
    }

    public boolean validatePage() {
        if ("".equals(getPartition())) return false;
        if ("".equals(getSuffix())) return false;
        return visited;
    }

    public void setVisible(boolean b) {
        super.setVisible(b);
        if (b) {
            visited = true;
            setPageComplete(validatePage());
        }
    }
    
    public void setPartition(String partition) {
        partitionText.setText(partition);
    }

    public String getPartition() {
        return partitionText.getText();
    }

    public void setSuffix(String suffix) {
        suffixText.setText(suffix);
    }

    public String getSuffix() {
        return suffixText.getText();
    }

    public void modifyText(ModifyEvent event) {
        setPageComplete(validatePage());
    }
}
