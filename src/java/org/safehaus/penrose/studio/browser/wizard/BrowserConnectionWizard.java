package org.safehaus.penrose.studio.browser.wizard;

import org.eclipse.jface.wizard.Wizard;
import org.apache.log4j.Logger;
import org.safehaus.penrose.studio.ldap.connection.wizard.LDAPConnectionSettingsWizardPage;
import org.safehaus.penrose.studio.server.Server;

/**
 * @author Endi S. Dewata
 */
public class BrowserConnectionWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    LDAPConnectionSettingsWizardPage connectionPage;

    Server server;
    String providerUrl;
    String suffix;
    String bindDn;
    String bindPassword;

    public BrowserConnectionWizard() {
        setWindowTitle("LDAP Browser");
    }

    public void addPages() {

        connectionPage = new LDAPConnectionSettingsWizardPage();
        connectionPage.setServer(server);
        connectionPage.setProviderUrl(providerUrl);
        connectionPage.setSuffix(suffix);
        connectionPage.setBindDn(bindDn);
        connectionPage.setBindPassword(bindPassword);

        addPage(connectionPage);
    }

    public boolean canFinish() {
        if (!connectionPage.isPageComplete()) return false;
        return true;
    }

    public boolean performFinish() {
        try {
            providerUrl = connectionPage.getProviderUrl();
            suffix = connectionPage.getSuffix();
            bindDn = connectionPage.getBindDn();
            bindPassword = connectionPage.getBindPassword();

            return true;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    public boolean needsPreviousAndNextButtons() {
        return true;
    }

    public String getProviderUrl() {
        return providerUrl;
    }

    public void setProviderUrl(String providerUrl) {
        this.providerUrl = providerUrl;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getBindDn() {
        return bindDn;
    }

    public void setBindDn(String bindDn) {
        this.bindDn = bindDn;
    }

    public String getBindPassword() {
        return bindPassword;
    }

    public void setBindPassword(String bindPassword) {
        this.bindPassword = bindPassword;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }
}