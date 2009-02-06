package org.safehaus.penrose.studio.server.wizard;

import org.eclipse.jface.wizard.Wizard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.safehaus.penrose.studio.server.ServerConfig;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.studio.util.ApplicationConfig;

/**
 * @author Endi Sukma Dewata
 */
public class EditServerWizard extends Wizard {

    Logger log = LoggerFactory.getLogger(getClass());

    ServerWizardPage propertiesPage;

    ServerConfig serverConfig;

    public EditServerWizard() {
        setWindowTitle("Edit Server");
    }

    public void addPages() {

        propertiesPage = new ServerWizardPage();

        propertiesPage.setServerName(serverConfig.getName());
        propertiesPage.setType(serverConfig.getType());
        propertiesPage.setHost(serverConfig.getHost());
        propertiesPage.setPort(""+serverConfig.getPort());
        propertiesPage.setBindDn(serverConfig.getUsername());
        propertiesPage.setBindPassword(serverConfig.getPassword());

        addPage(propertiesPage);
    }

    public boolean canFinish() {
        return true;
    }

    public boolean performFinish() {
        try {
            ServerConfig newServerConfig = new ServerConfig();

            newServerConfig.setName(propertiesPage.getServerName());
            newServerConfig.setType(propertiesPage.getType());
            newServerConfig.setHost(propertiesPage.getHost());

            String port = propertiesPage.getPort();
            newServerConfig.setPort(port == null ? 0 : Integer.parseInt(port));

            newServerConfig.setUsername(propertiesPage.getBindDn());
            newServerConfig.setPassword(propertiesPage.getBindPassword());

            PenroseStudio penroseStudio = PenroseStudio.getInstance();

            ApplicationConfig applicationConfig = penroseStudio.getApplicationConfig();
            applicationConfig.updateServerConfig(serverConfig.getName(), newServerConfig);

            penroseStudio.store();

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

    public ServerConfig getServerConfig() {
        return serverConfig;
    }

    public void setServerConfig(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }
}