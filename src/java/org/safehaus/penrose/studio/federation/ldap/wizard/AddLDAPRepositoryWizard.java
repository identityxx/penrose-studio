package org.safehaus.penrose.studio.federation.ldap.wizard;

import org.eclipse.jface.wizard.Wizard;
import org.safehaus.penrose.federation.LDAPRepository;
import org.safehaus.penrose.federation.FederationRepositoryConfig;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.studio.ldap.connection.wizard.LDAPConnectionSettingsWizardPage;
import org.safehaus.penrose.studio.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Endi Sukma Dewata
 */
public class AddLDAPRepositoryWizard extends Wizard {

    public Logger log = LoggerFactory.getLogger(getClass());

    Server server;
    LDAPRepositoryWizardPage    repositoryPage;
    LDAPConnectionSettingsWizardPage connectionPage;

    FederationRepositoryConfig repository = new FederationRepositoryConfig();

    public AddLDAPRepositoryWizard() {
        setWindowTitle("Add LDAP Repository");
    }

    public void addPages() {

        repositoryPage = new LDAPRepositoryWizardPage();
        addPage(repositoryPage);

        connectionPage = new LDAPConnectionSettingsWizardPage();
        connectionPage.setServer(server);
        addPage(connectionPage);
    }

    public boolean canFinish() {
        if (!repositoryPage.isPageComplete()) return false;
        if (!connectionPage.isPageComplete()) return false;
        return true;
    }

    public boolean performFinish() {

        try {
            repository.setType("LDAP");
            repository.setName(repositoryPage.getRepository());
            repository.setParameter(LDAPRepository.URL, connectionPage.getProviderUrl());
            repository.setParameter(LDAPRepository.SUFFIX, connectionPage.getSuffix());
            repository.setParameter(LDAPRepository.USER, connectionPage.getBindDn());
            repository.setParameter(LDAPRepository.PASSWORD, connectionPage.getBindPassword());

            return true;

        } catch (Exception e) {
            ErrorDialog.open(e);
            return false;
        }
    }

    public FederationRepositoryConfig getRepository() {
        return repository;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }
}
