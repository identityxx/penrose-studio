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

    Text nameText;
    Text suffixText;

    boolean visited;

    public NISPartitionWizardPage() {
        super(NAME);

        setDescription("Enter a short name for this domain and an LDAP suffix.");
    }

    public void createControl(final Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        GridLayout sectionLayout = new GridLayout();
        sectionLayout.numColumns = 2;
        composite.setLayout(sectionLayout);

        Label partitionLabel = new Label(composite, SWT.NONE);
        partitionLabel.setText("Name:");
        GridData gd = new GridData();
        gd.widthHint = 100;
        partitionLabel.setLayoutData(gd);

        nameText = new Text(composite, SWT.BORDER);
        nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        nameText.addModifyListener(this);

        Label suffixLabel = new Label(composite, SWT.NONE);
        suffixLabel.setText("LDAP Suffix:");
        suffixLabel.setLayoutData(new GridData());

        suffixText = new Text(composite, SWT.BORDER);
        suffixText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        suffixText.addModifyListener(this);
        
        setPageComplete(validatePage());
    }

    public boolean validatePage() {
        if ("".equals(getShortName())) return false;
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
    
    public void setShortName(String name) {
        nameText.setText(name);
    }

    public String getShortName() {
        return nameText.getText();
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
