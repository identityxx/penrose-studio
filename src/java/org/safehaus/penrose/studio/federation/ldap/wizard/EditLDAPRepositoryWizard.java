package org.safehaus.penrose.studio.federation.ldap.wizard;

import org.eclipse.jface.wizard.Wizard;
import org.apache.log4j.Logger;
import org.safehaus.penrose.studio.ldap.connection.wizard.LDAPConnectionSettingsWizardPage;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.federation.LDAPRepository;
import org.safehaus.penrose.federation.FederationRepositoryConfig;

/**
 * @author Endi S. Dewata
 */
public class EditLDAPRepositoryWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    LDAPConnectionSettingsWizardPage connectionPage;

    Server server;
    FederationRepositoryConfig repository;

    public EditLDAPRepositoryWizard(FederationRepositoryConfig repository) {
        this.repository = repository;

        setWindowTitle("Edit LDAP Repository");
    }

    public void addPages() {

        connectionPage = new LDAPConnectionSettingsWizardPage();
        connectionPage.setServer(server);
        connectionPage.setProviderUrl(repository.getParameter(LDAPRepository.URL));
        connectionPage.setSuffix(repository.getParameter(LDAPRepository.SUFFIX));
        connectionPage.setBindDn(repository.getParameter(LDAPRepository.USER));
        connectionPage.setBindPassword(repository.getParameter(LDAPRepository.PASSWORD));

        addPage(connectionPage);
    }

    public boolean canFinish() {
        if (!connectionPage.isPageComplete()) return false;
        return true;
    }

    public boolean performFinish() {
        try {
            repository.setParameter(LDAPRepository.URL, connectionPage.getProviderUrl());
            repository.setParameter(LDAPRepository.SUFFIX, connectionPage.getSuffix());
            repository.setParameter(LDAPRepository.USER, connectionPage.getBindDn());
            repository.setParameter(LDAPRepository.PASSWORD, connectionPage.getBindPassword());

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

    public FederationRepositoryConfig getRepository() {
        return repository;
    }

    public void setRepository(FederationRepositoryConfig repository) {
        this.repository = repository;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }
}
