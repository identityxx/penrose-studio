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

    Text repositoryNameText;

    String repositoryName;

    public NISRepositoryWizardPage() {
        super(NAME);

        setDescription("Enter a short name of the NIS domain (not the full NIS repositoryConfig name).");
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

        repositoryNameText = new Text(composite, SWT.BORDER);
        repositoryNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        repositoryNameText.addModifyListener(this);

        refresh();
    }

    public void setVisible(boolean b) {
        super.setVisible(b);
        if (b) {
            refresh();
        }
    }

    public void refresh() {
        repositoryNameText.setText(repositoryName == null ? "" : repositoryName);

        setPageComplete(validatePage());
    }

    public boolean validatePage() {
        if (getRepositoryName() == null) return false;
        return true;
    }

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    public String getRepositoryName() {
        String s = repositoryNameText.getText();
        return s.equals("") ? null : s;
    }

    public void modifyText(ModifyEvent event) {
        setPageComplete(validatePage());
    }
}
