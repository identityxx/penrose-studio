package org.safehaus.penrose.studio.federation.linking.wizard;

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
public class LinkingParametersWizardPage extends WizardPage implements ModifyListener {

    public Logger log = LoggerFactory.getLogger(getClass());

    public final static String NAME = "Identity Linking Parameters";

    Text localAttributeText;
    Text globalAttributeText;

    Text importMappingNameText;
    Text importMappingPrefixText;

    String localAttribute;
    String globalAttribute;

    String importMappingName;
    String importMappingPrefix;

    public LinkingParametersWizardPage() {
        super(NAME);

        setDescription("Enter identity linking parameters.");
    }

    public void createControl(final Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        GridLayout sectionLayout = new GridLayout();
        sectionLayout.numColumns = 2;
        composite.setLayout(sectionLayout);

        Label localAttributeLabel = new Label(composite, SWT.NONE);
        localAttributeLabel.setText("Local Attribute:");
        GridData gd = new GridData();
        gd.widthHint = 100;
        localAttributeLabel.setLayoutData(gd);

        localAttributeText = new Text(composite, SWT.BORDER);
        localAttributeText.setText(localAttribute == null ? "" : localAttribute);
        localAttributeText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        localAttributeText.addModifyListener(this);

        Label globalAttributeLabel = new Label(composite, SWT.NONE);
        globalAttributeLabel.setText("Global Attribute:");
        globalAttributeLabel.setLayoutData(new GridData());

        globalAttributeText = new Text(composite, SWT.BORDER);
        globalAttributeText.setText(globalAttribute == null ? "" : globalAttribute);
        globalAttributeText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        globalAttributeText.addModifyListener(this);

        Label separator = new Label(composite, SWT.NONE);
        gd = new GridData();
        gd.horizontalSpan = 2;
        separator.setLayoutData(gd);

        Label importMappingNameLabel = new Label(composite, SWT.NONE);
        importMappingNameLabel.setText("Import mapping name:");
        gd = new GridData();
        gd.widthHint = 100;
        importMappingNameLabel.setLayoutData(gd);

        importMappingNameText = new Text(composite, SWT.BORDER);
        importMappingNameText.setText(importMappingName == null ? "" : importMappingName);
        importMappingNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        importMappingNameText.addModifyListener(this);

        Label importMappingPrefixLabel = new Label(composite, SWT.NONE);
        importMappingPrefixLabel.setText("Import mapping prefix:");
        gd = new GridData();
        gd.widthHint = 100;
        importMappingPrefixLabel.setLayoutData(gd);

        importMappingPrefixText = new Text(composite, SWT.BORDER);
        importMappingPrefixText.setText(importMappingPrefix == null ? "" : importMappingPrefix);
        importMappingPrefixText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        importMappingPrefixText.addModifyListener(this);

        setPageComplete(validatePage());
    }

    public boolean validatePage() {
        return true;
    }

    public void setGlobalAttribute(String globalAttribute) {
        this.globalAttribute = globalAttribute;
    }

    public String getGlobalAttribute() {
        String s = globalAttributeText.getText();
        return s.equals("") ? null : s;
    }

    public void setLocalAttribute(String localAttribute) {
        this.localAttribute = localAttribute;
    }

    public String getLocalAttribute() {
        String s = localAttributeText.getText();
        return s.equals("") ? null : s;
    }

    public void modifyText(ModifyEvent event) {
        setPageComplete(validatePage());
    }

    public String getImportMappingName() {
        String s = importMappingNameText.getText();
        return s.equals("") ? null : s;
    }

    public void setImportMappingName(String importMappingName) {
        this.importMappingName = importMappingName;
    }

    public String getImportMappingPrefix() {
        String s = importMappingPrefixText.getText();
        return s.equals("") ? null : s;
    }

    public void setImportMappingPrefix(String importMappingPrefix) {
        this.importMappingPrefix = importMappingPrefix;
    }
}