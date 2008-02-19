package org.safehaus.penrose.studio.federation.nis.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Endi Sukma Dewata
 */
public class NISLdapWizardPage extends WizardPage implements ModifyListener, SelectionListener {

    public Logger log = LoggerFactory.getLogger(getClass());

    public final static String NAME = "NIS LDAP";


    Button nisEnabledButton;
    Text nisSuffixText;

    Button ypEnabledButton;
    Text ypSuffixText;

    Button nssEnabledButton;
    Text nssSuffixText;

    boolean visited;

    public NISLdapWizardPage() {
        super(NAME);

        setDescription("Enter an LDAP suffix for the YP entries, NIS entries, and NSS entries.");
    }

    public void createControl(final Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        GridLayout sectionLayout = new GridLayout();
        sectionLayout.numColumns = 2;
        composite.setLayout(sectionLayout);

        Label ypEnabledLabel = new Label(composite, SWT.NONE);
        ypEnabledLabel.setText("YP Enabled:");
        GridData gd = new GridData();
        gd.widthHint = 100;
        ypEnabledLabel.setLayoutData(gd);

        ypEnabledButton = new Button(composite, SWT.CHECK);
        ypEnabledButton.setSelection(true);
        ypEnabledButton.addSelectionListener(this);

        Label ypSuffixLabel = new Label(composite, SWT.NONE);
        ypSuffixLabel.setText("YP Suffix:");
        ypSuffixLabel.setLayoutData(new GridData());

        ypSuffixText = new Text(composite, SWT.BORDER);
        ypSuffixText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        ypSuffixText.addModifyListener(this);

        new Label(composite, SWT.NONE);
        new Label(composite, SWT.NONE);

        Label nisEnabledLabel = new Label(composite, SWT.NONE);
        nisEnabledLabel.setText("NIS Enabled:");

        nisEnabledButton = new Button(composite, SWT.CHECK);
        nisEnabledButton.setSelection(true);
        nisEnabledButton.addSelectionListener(this);

        Label suffixLabel = new Label(composite, SWT.NONE);
        suffixLabel.setText("NIS Suffix:");

        nisSuffixText = new Text(composite, SWT.BORDER);
        nisSuffixText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        nisSuffixText.addModifyListener(this);

        new Label(composite, SWT.NONE);
        new Label(composite, SWT.NONE);

        Label nssEnabledLabel = new Label(composite, SWT.NONE);
        nssEnabledLabel.setText("NSS Enabled:");

        nssEnabledButton = new Button(composite, SWT.CHECK);
        nssEnabledButton.setSelection(true);
        nssEnabledButton.addSelectionListener(this);

        Label nssSuffixLabel = new Label(composite, SWT.NONE);
        nssSuffixLabel.setText("NSS Suffix:");
        nssSuffixLabel.setLayoutData(new GridData());

        nssSuffixText = new Text(composite, SWT.BORDER);
        nssSuffixText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        nssSuffixText.addModifyListener(this);

        setPageComplete(validatePage());
    }

    public boolean validatePage() {
        if ("".equals(getNisSuffix())) return false;
        if ("".equals(getYpSuffix())) return false;
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

    public void setNisSuffix(String suffix) {
        nisSuffixText.setText(suffix);
    }

    public String getNisSuffix() {
        return nisSuffixText.getText();
    }

    public boolean isNisEnabled() {
        return nisEnabledButton.getSelection();
    }

    public void setNisEnabled(boolean nisEnabled) {
        nisEnabledButton.setSelection(nisEnabled);
    }

    public void setYpSuffix(String ldapSuffix) {
        ypSuffixText.setText(ldapSuffix);
    }

    public String getYpSuffix() {
        return ypSuffixText.getText();
    }

    public boolean isYpEnabled() {
        return ypEnabledButton.getSelection();
    }

    public void setYpEnabled(boolean ypEnabled) {
        ypEnabledButton.setSelection(ypEnabled);
    }

    public void setNssSuffix(String nssSuffix) {
        nssSuffixText.setText(nssSuffix);
    }

    public String getNssSuffix() {
        return nssSuffixText.getText();
    }

    public boolean isNssEnabled() {
        return nssEnabledButton.getSelection();
    }

    public void setNssEnabled(boolean nssEnabled) {
        nssEnabledButton.setSelection(nssEnabled);
    }

    public void modifyText(ModifyEvent event) {
        setPageComplete(validatePage());
    }

    public void widgetSelected(SelectionEvent event) {
        setPageComplete(validatePage());
    }

    public void widgetDefaultSelected(SelectionEvent event) {
        setPageComplete(validatePage());
    }
}
