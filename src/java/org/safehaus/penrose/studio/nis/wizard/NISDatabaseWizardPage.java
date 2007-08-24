package org.safehaus.penrose.studio.nis.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Endi Sukma Dewata
 */
public class NISDatabaseWizardPage extends WizardPage {

    public Logger log = LoggerFactory.getLogger(getClass());

    public final static String NAME = "NIS Database Actions";

    Button createCheckbox;

    boolean visited;

    public NISDatabaseWizardPage() {
        super(NAME);

        setDescription("Specify database actions to execute.");
    }

    public void createControl(final Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        GridLayout sectionLayout = new GridLayout();
        composite.setLayout(sectionLayout);

        createCheckbox = new Button(composite, SWT.CHECK);
        createCheckbox.setSelection(true);
        createCheckbox.setText("Create database.");
        createCheckbox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    }

    public boolean validatePage() {
        return visited;
    }

    public void setVisible(boolean b) {
        super.setVisible(b);
        if (b) {
            visited = true;
            setPageComplete(validatePage());
        }
    }

    public void setCreate(boolean create) {
        createCheckbox.setSelection(create);
    }

    public boolean isCreate() {
        return createCheckbox.getSelection();
    }
}
