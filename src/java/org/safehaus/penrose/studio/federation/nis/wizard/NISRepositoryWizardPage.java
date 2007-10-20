package org.safehaus.penrose.studio.federation.nis.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Endi Sukma Dewata
 */
public class NISRepositoryWizardPage extends WizardPage implements ModifyListener {

    public Logger log = LoggerFactory.getLogger(getClass());

    public final static String NAME = "NIS Domain";

    Text repositoryText;

    public NISRepositoryWizardPage() {
        super(NAME);

        setDescription("Enter a short name of the NIS domain (not the full NIS domain name).");
    }

    public void createControl(final Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        GridLayout sectionLayout = new GridLayout();
        sectionLayout.numColumns = 2;
        composite.setLayout(sectionLayout);

        Label repositoryLabel = new Label(composite, SWT.NONE);
        repositoryLabel.setText("Name:");
        GridData gd = new GridData();
        gd.widthHint = 100;
        repositoryLabel.setLayoutData(gd);

        repositoryText = new Text(composite, SWT.BORDER);
        repositoryText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        repositoryText.addModifyListener(this);

        setPageComplete(validatePage());
    }

    public boolean validatePage() {
        if ("".equals(getRepository())) return false;
        return true;
    }

    public void setRepository(String repository) {
        repositoryText.setText(repository);
    }

    public String getRepository() {
        return repositoryText.getText();
    }

    public void modifyText(ModifyEvent event) {
        setPageComplete(validatePage());
    }
}
