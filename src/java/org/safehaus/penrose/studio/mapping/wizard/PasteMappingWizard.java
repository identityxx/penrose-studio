package org.safehaus.penrose.studio.mapping.wizard;

import org.apache.log4j.Logger;
import org.eclipse.jface.wizard.Wizard;
import org.safehaus.penrose.mapping.MappingConfig;
import org.safehaus.penrose.studio.dialog.ErrorDialog;

/**
 * @author Endi Sukma Dewata
 */
public class PasteMappingWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    public MappingPropertyWizardPage propertyPage;

    String mappingName;

    public PasteMappingWizard() {

        setWindowTitle("Paste mapping");

        propertyPage = new MappingPropertyWizardPage();
        propertyPage.setDescription("Enter the mapping name.");
    }

    public boolean canFinish() {
        if (!propertyPage.isPageComplete()) return false;
        return true;
    }

    public void addPages() {
        addPage(propertyPage);
    }

    public boolean needsPreviousAndNextButtons() {
        return true;
    }

    public boolean performFinish() {
        try {
            mappingName = propertyPage.getMappingName();

            return true;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
            return false;
        }
    }

    public String getMappingName() {
        return mappingName;
    }
}