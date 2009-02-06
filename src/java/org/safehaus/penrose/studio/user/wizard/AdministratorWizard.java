package org.safehaus.penrose.studio.user.wizard;

import org.eclipse.jface.wizard.Wizard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.user.UserConfig;

/**
 * @author Endi Sukma Dewata
 */
public class  AdministratorWizard extends Wizard {

    Logger log = LoggerFactory.getLogger(getClass());

    AdministratorWizardPage propertiesPage;

    UserConfig userConfig;

    public AdministratorWizard() {
        setWindowTitle("Edit Administrator Properties");
    }

    public void addPages() {

        propertiesPage = new AdministratorWizardPage();
        propertiesPage.setDn(userConfig.getDn().toString());

        addPage(propertiesPage);
    }

    public boolean canFinish() {
        if (!propertiesPage.isPageComplete()) return false;
        return true;
    }

    public boolean performFinish() {
        try {
            userConfig.setDn(propertiesPage.getDn());

            String password = propertiesPage.getPassword();
            if (password != null) {
                userConfig.setPassword(password);
            }

            return true;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e.getMessage());
            return false;
        }
    }

    public boolean needsPreviousAndNextButtons() {
        return true;
    }

    public UserConfig getUserConfig() {
        return userConfig;
    }

    public void setUserConfig(UserConfig userConfig) {
        this.userConfig = userConfig;
    }
}