package org.safehaus.penrose.studio.federation.ldap.wizard;

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
public class LDAPPartitionsWizardPage extends WizardPage implements ModifyListener, SelectionListener {

    public Logger log = LoggerFactory.getLogger(getClass());

    public final static String NAME = "LDAP PARTITIONS";

    Button enabledButton;
    Text suffixText;
    Text templateText;

    boolean visited;

    boolean enabled = true;
    String suffix;
    String template;

    public LDAPPartitionsWizardPage() {
        super(NAME);

        setDescription("Enter a suffix for the LDAP partition.");
    }

    public void createControl(final Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        GridLayout sectionLayout = new GridLayout();
        sectionLayout.numColumns = 2;
        composite.setLayout(sectionLayout);
/*
        Label enabledLabel = new Label(composite, SWT.NONE);
        enabledLabel.setText("Enabled:");
        GridData gd = new GridData();
        gd.widthHint = 100;
        enabledLabel.setLayoutData(gd);

        ldapEnabledButton = new Button(composite, SWT.CHECK);
        ldapEnabledButton.setSelection(true);
        ldapEnabledButton.addSelectionListener(this);
*/
        Label suffixLabel = new Label(composite, SWT.NONE);
        suffixLabel.setText("Suffix:");
        GridData gd = new GridData();
        gd.widthHint = 100;
        suffixLabel.setLayoutData(gd);

        suffixText = new Text(composite, SWT.BORDER);
        suffixText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        suffixText.addModifyListener(this);

        Label templateLabel = new Label(composite, SWT.NONE);
        templateLabel.setText("Template:");
        templateLabel.setLayoutData(new GridData());

        templateText = new Text(composite, SWT.BORDER);
        templateText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        templateText.addModifyListener(this);

        refresh();
    }

    public void refresh() {

        //enabledButton.setSelection(enabled);
        suffixText.setText(suffix == null ? "" : suffix);
        templateText.setText(template == null ? "" : template);

        setPageComplete(validatePage());
    }

    public boolean validatePage() {
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

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getSuffix() {
        String s = suffixText.getText();
        return "".equals(s) ? null : s;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getTemplate() {
        String s = templateText.getText();
        return "".equals(s) ? null : s;
    }

    public boolean isEnabled() {
        return enabledButton.getSelection();
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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