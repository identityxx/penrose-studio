package org.safehaus.penrose.studio.federation.global.wizard;

import org.apache.log4j.Logger;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.safehaus.penrose.federation.GlobalRepository;
import org.safehaus.penrose.federation.FederationClient;
import org.safehaus.penrose.studio.ldap.connection.LDAPConnectionWizardPage;
import org.safehaus.penrose.studio.federation.ldap.wizard.LDAPPartitionsWizardPage;

import java.net.URI;

/**
 * @author Endi S. Dewata
 */
public class GlobalRepositoryEditorWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    LDAPConnectionWizardPage connectionPage;
    LDAPPartitionsWizardPage partitionsPage;

    GlobalRepository repository;

    public GlobalRepositoryEditorWizard(GlobalRepository repository) {
        this.repository = repository;

        setWindowTitle("Global Repository");
    }

    public void addPages() {

        connectionPage = new LDAPConnectionWizardPage();

        if (repository != null) {
            connectionPage.setProviderUrl(repository.getParameter(GlobalRepository.LDAP_URL));
            connectionPage.setSuffix(repository.getParameter(GlobalRepository.LDAP_SUFFIX));
            connectionPage.setBindDn(repository.getParameter(GlobalRepository.LDAP_USER));
            connectionPage.setBindPassword(repository.getParameter(GlobalRepository.LDAP_PASSWORD));
        }

        addPage(connectionPage);

        partitionsPage = new LDAPPartitionsWizardPage();

        partitionsPage.setSuffix(repository.getParameter(GlobalRepository.SUFFIX));
        partitionsPage.setTemplate(repository.getParameter(GlobalRepository.TEMPLATE));

        addPage(partitionsPage);
    }

    public boolean canFinish() {
        if (!connectionPage.isPageComplete()) return false;
        if (!partitionsPage.isPageComplete()) return false;
        return true;
    }

    public IWizardPage getNextPage(IWizardPage page) {
        if (connectionPage == page) {
            if (repository == null) {
                String suffix = connectionPage.getSuffix();
                partitionsPage.setSuffix(suffix);
            }
        }

        return super.getNextPage(page);
    }

    public boolean performFinish() {
        try {
            if (repository == null) {
                repository = new GlobalRepository();
                repository.setName(FederationClient.GLOBAL);
                repository.setType("GLOBAL");
            }

            repository.setParameter(GlobalRepository.LDAP_URL, connectionPage.getProviderUrl());
            repository.setParameter(GlobalRepository.LDAP_SUFFIX, connectionPage.getSuffix());
            repository.setParameter(GlobalRepository.LDAP_USER, connectionPage.getBindDn());
            repository.setParameter(GlobalRepository.LDAP_PASSWORD, connectionPage.getBindPassword());

            repository.setParameter(GlobalRepository.SUFFIX, partitionsPage.getSuffix());
            repository.setParameter(GlobalRepository.TEMPLATE, partitionsPage.getTemplate());

            return true;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    public boolean needsPreviousAndNextButtons() {
        return true;
    }

    public GlobalRepository getRepository() {
        return repository;
    }
}