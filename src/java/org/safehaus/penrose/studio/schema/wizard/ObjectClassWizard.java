package org.safehaus.penrose.studio.schema.wizard;

import org.eclipse.jface.wizard.Wizard;
import org.apache.log4j.Logger;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.schema.ObjectClass;

/**
 * @author Endi Sukma Dewata
 */
public class ObjectClassWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    ObjectClassPropertiesWizardPage propertiesPage;
    ObjectClassAttributesWizardPage attributesPage;

    private Server server;
    private ObjectClass objectClass;

    public ObjectClassWizard() {
    }

    public void addPages() {

        propertiesPage = new ObjectClassPropertiesWizardPage();

        propertiesPage.setOid(objectClass.getOid());
        propertiesPage.setNames(objectClass.getNames());
        propertiesPage.setObjectClassDescription(objectClass.getDescription());
        propertiesPage.setType(objectClass.getType());
        propertiesPage.setSuperClasses(objectClass.getSuperClasses());
        propertiesPage.setObsolete(objectClass.isObsolete());

        addPage(propertiesPage);

        attributesPage = new ObjectClassAttributesWizardPage();

        attributesPage.setServer(server);
        attributesPage.setRequiredAttributes(objectClass.getRequiredAttributes());
        attributesPage.setOptionalAttributes(objectClass.getOptionalAttributes());

        addPage(attributesPage);
    }

    public boolean canFinish() {

        if (!propertiesPage.isPageComplete()) return false;
        if (!attributesPage.isPageComplete()) return false;

        return true;
    }

    public boolean performFinish() {
        try {
            objectClass.setOid(propertiesPage.getOid());
            objectClass.setNames(propertiesPage.getNames());
            objectClass.setDescription(propertiesPage.getObjectClassDescription());
            objectClass.setType(propertiesPage.getType());
            objectClass.setSuperClasses(propertiesPage.getSuperClasses());
            objectClass.setObsolete(propertiesPage.isObsolete());

            objectClass.setRequiredAttributes(attributesPage.getRequiredAttributes());
            objectClass.setOptionalAttributes(attributesPage.getOptionalAttributes());

            return true;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
            return false;
        }
    }

    public boolean needsPreviousAndNextButtons() {
        return true;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public ObjectClass getObjectClass() {
        return objectClass;
    }

    public void setObjectClass(ObjectClass objectClass) {
        this.objectClass = objectClass;
    }
}
