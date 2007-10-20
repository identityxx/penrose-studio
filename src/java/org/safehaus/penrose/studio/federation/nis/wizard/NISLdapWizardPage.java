package org.safehaus.penrose.studio.federation.nis.wizard;

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
public class NISLdapWizardPage extends WizardPage implements ModifyListener {

    public Logger log = LoggerFactory.getLogger(getClass());

    public final static String NAME = "NIS LDAP";

    Text suffixText;
    Text nssSuffixText;

    boolean visited;

    public NISLdapWizardPage() {
        super(NAME);

        setDescription("Enter an LDAP suffix for the NIS entries and another LDAP suffix for the stacking authentication (NSS).");
    }

    public void createControl(final Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        GridLayout sectionLayout = new GridLayout();
        sectionLayout.numColumns = 2;
        composite.setLayout(sectionLayout);

        Label suffixLabel = new Label(composite, SWT.NONE);
        suffixLabel.setText("NIS Suffix:");
        GridData gd = new GridData();
        gd.widthHint = 100;
        suffixLabel.setLayoutData(gd);

        suffixText = new Text(composite, SWT.BORDER);
        suffixText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        suffixText.addModifyListener(this);
        
        Label nssSuffixLabel = new Label(composite, SWT.NONE);
        nssSuffixLabel.setText("NSS Suffix:");
        nssSuffixLabel.setLayoutData(new GridData());

        nssSuffixText = new Text(composite, SWT.BORDER);
        nssSuffixText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        nssSuffixText.addModifyListener(this);

        setPageComplete(validatePage());
    }

    public boolean validatePage() {
        if ("".equals(getSuffix())) return false;
        if ("".equals(getNssSuffix())) return false;
        return visited;
    }

    public void setVisible(boolean b) {
        super.setVisible(b);
        if (b) {
            visited = true;
            setPageComplete(validatePage());
        }
    }

    public void setSuffix(String suffix) {
        suffixText.setText(suffix);
    }

    public String getSuffix() {
        return suffixText.getText();
    }

    public void setNssSuffix(String nssSuffix) {
        nssSuffixText.setText(nssSuffix);
    }

    public String getNssSuffix() {
        return nssSuffixText.getText();
    }

    public void modifyText(ModifyEvent event) {
        setPageComplete(validatePage());
    }
}
