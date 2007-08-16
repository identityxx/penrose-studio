package org.safehaus.penrose.studio.nis.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Endi Sukma Dewata
 */
public class NISDomainWizardPage extends WizardPage implements ModifyListener {

    public Logger log = LoggerFactory.getLogger(getClass());

    public final static String NAME = "NIS Domain";

    Text domainText;
    Text serverText;

    public NISDomainWizardPage() {
        super(NAME);

        setDescription("Enter NIS domain name and NIS server name.");
    }

    public void createControl(final Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        GridLayout sectionLayout = new GridLayout();
        sectionLayout.numColumns = 2;
        composite.setLayout(sectionLayout);

        Label domainLabel = new Label(composite, SWT.NONE);
        domainLabel.setText("NIS Domain:");
        GridData gd = new GridData();
        gd.widthHint = 100;
        domainLabel.setLayoutData(gd);

        domainText = new Text(composite, SWT.BORDER);
        domainText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        domainText.addModifyListener(this);

        Label serverLabel = new Label(composite, SWT.NONE);
        serverLabel.setText("NIS Server:");
        serverLabel.setLayoutData(new GridData());

        serverText = new Text(composite, SWT.BORDER);
        serverText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        serverText.addModifyListener(this);

        setPageComplete(validatePage());
    }

    public boolean validatePage() {
        if ("".equals(getDomain())) return false;
        if ("".equals(getServer())) return false;
        return true;
    }

    public void setDomain(String domain) {
        domainText.setText(domain);
    }

    public String getDomain() {
        return domainText.getText();
    }

    public void setServer(String server) {
        serverText.setText(server);
    }

    public String getServer() {
        return serverText.getText();
    }

    public void modifyText(ModifyEvent event) {
        setPageComplete(validatePage());
    }
}
