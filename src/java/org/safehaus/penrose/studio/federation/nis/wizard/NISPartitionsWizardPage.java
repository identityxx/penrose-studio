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
public class NISPartitionsWizardPage extends WizardPage implements ModifyListener, SelectionListener {

    public Logger log = LoggerFactory.getLogger(getClass());

    public final static String NAME = "NIS PARTITIONS";

    Button ypEnabledButton;
    Text ypSuffixText;
    Text ypTemplateText;

    Button nisEnabledButton;
    Text nisSuffixText;
    Text nisTemplateText;

    Button nssEnabledButton;
    Text nssSuffixText;
    Text nssTemplateText;

    boolean visited;

    boolean ypEnabled = true;
    String ypSuffix;
    String ypTemplate;

    boolean nisEnabled = true;
    String nisTemplate;
    String nisSuffix;

    boolean nssEnabled = true;
    String nssSuffix;
    String nssTemplate;

    public NISPartitionsWizardPage() {
        super(NAME);

        setDescription("Enter a suffix for the YP, NIS, and NSS partitions.");
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
        ypEnabledButton.addSelectionListener(this);

        Label ypSuffixLabel = new Label(composite, SWT.NONE);
        ypSuffixLabel.setText("YP Suffix:");
        ypSuffixLabel.setLayoutData(new GridData());

        ypSuffixText = new Text(composite, SWT.BORDER);
        ypSuffixText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        ypSuffixText.addModifyListener(this);

        Label ypTemplateLabel = new Label(composite, SWT.NONE);
        ypTemplateLabel.setText("YP Template:");
        ypTemplateLabel.setLayoutData(new GridData());

        ypTemplateText = new Text(composite, SWT.BORDER);
        ypTemplateText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        ypTemplateText.addModifyListener(this);

        new Label(composite, SWT.NONE);
        new Label(composite, SWT.NONE);

        Label nisEnabledLabel = new Label(composite, SWT.NONE);
        nisEnabledLabel.setText("NIS Enabled:");

        nisEnabledButton = new Button(composite, SWT.CHECK);
        nisEnabledButton.addSelectionListener(this);

        Label nisSuffixLabel = new Label(composite, SWT.NONE);
        nisSuffixLabel.setText("NIS Suffix:");

        nisSuffixText = new Text(composite, SWT.BORDER);
        nisSuffixText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        nisSuffixText.addModifyListener(this);

        Label nisTemplateLabel = new Label(composite, SWT.NONE);
        nisTemplateLabel.setText("NIS Template:");

        nisTemplateText = new Text(composite, SWT.BORDER);
        nisTemplateText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        nisTemplateText.addModifyListener(this);

        new Label(composite, SWT.NONE);
        new Label(composite, SWT.NONE);

        Label nssEnabledLabel = new Label(composite, SWT.NONE);
        nssEnabledLabel.setText("NSS Enabled:");

        nssEnabledButton = new Button(composite, SWT.CHECK);
        nssEnabledButton.addSelectionListener(this);

        Label nssSuffixLabel = new Label(composite, SWT.NONE);
        nssSuffixLabel.setText("NSS Suffix:");
        nssSuffixLabel.setLayoutData(new GridData());

        nssSuffixText = new Text(composite, SWT.BORDER);
        nssSuffixText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        nssSuffixText.addModifyListener(this);

        Label nssTemplateLabel = new Label(composite, SWT.NONE);
        nssTemplateLabel.setText("NSS Template:");
        nssTemplateLabel.setLayoutData(new GridData());

        nssTemplateText = new Text(composite, SWT.BORDER);
        nssTemplateText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        nssTemplateText.addModifyListener(this);

        refresh();
    }

    public void setVisible(boolean b) {
        super.setVisible(b);
        if (b) {
            visited = true;
            refresh();
        }
    }

    public void refresh() {
        ypEnabledButton.setSelection(ypEnabled);
        ypSuffixText.setText(ypSuffix == null ? "" : ypSuffix);
        ypTemplateText.setText(ypTemplate == null ? "" : ypTemplate);

        nisEnabledButton.setSelection(nisEnabled);
        nisSuffixText.setText(nisSuffix == null ? "" : nisSuffix);
        nisTemplateText.setText(nisTemplate == null ? "" : nisTemplate);

        nssEnabledButton.setSelection(nssEnabled);
        nssSuffixText.setText(nssSuffix == null ? "" : nssSuffix);
        nssTemplateText.setText(nssTemplate == null ? "" : nssTemplate);

        setPageComplete(validatePage());
    }

    public boolean validatePage() {
        if (getNisSuffix() == null) return false;
        if (getYpSuffix() == null) return false;
        if (getNssSuffix() == null) return false;
        return visited;
    }

    public void setNisSuffix(String nisSuffix) {
        this.nisSuffix = nisSuffix;
    }

    public String getNisSuffix() {
        String s = nisSuffixText.getText();
        return s.equals("") ? null : s;
    }

    public void setNisTemplate(String nisTemplate) {
        this.nisTemplate = nisTemplate;
    }

    public String getNisTemplate() {
        String s = nisTemplateText.getText();
        return s.equals("") ? null : s;
    }

    public boolean isNisEnabled() {
        return nisEnabledButton.getSelection();
    }

    public void setNisEnabled(boolean nisEnabled) {
        this.nisEnabled = nisEnabled;
    }

    public void setYpSuffix(String ypSuffix) {
        this.ypSuffix = ypSuffix;
    }

    public String getYpSuffix() {
        String s = ypSuffixText.getText();
        return s.equals("") ? null : s;
    }

    public void setYpTemplate(String ypTemplate) {
        this.ypTemplate = ypTemplate;
    }

    public String getYpTemplate() {
        String s = ypTemplateText.getText();
        return s.equals("") ? null : s;
    }

    public boolean isYpEnabled() {
        return ypEnabledButton.getSelection();
    }

    public void setYpEnabled(boolean ypEnabled) {
        this.ypEnabled = ypEnabled;
    }

    public void setNssSuffix(String nssSuffix) {
        this.nssSuffix = nssSuffix;
    }

    public String getNssSuffix() {
        String s = nssSuffixText.getText();
        return s.equals("") ? null : s;
    }

    public void setNssTemplate(String nssTemplate) {
        this.nssTemplate = nssTemplate;
    }

    public String getNssTemplate() {
        String s = nssTemplateText.getText();
        return s.equals("") ? null : s;
    }

    public boolean isNssEnabled() {
        return nssEnabledButton.getSelection();
    }

    public void setNssEnabled(boolean nssEnabled) {
        this.nssEnabled = nssEnabled;
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
