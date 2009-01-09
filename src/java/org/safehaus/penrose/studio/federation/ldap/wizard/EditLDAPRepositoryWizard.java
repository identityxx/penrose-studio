package org.safehaus.penrose.studio.federation.ldap.wizard;

import org.eclipse.jface.wizard.Wizard;
import org.apache.log4j.Logger;
import org.safehaus.penrose.studio.ldap.connection.wizard.LDAPConnectionSettingsWizardPage;
import org.safehaus.penrose.federation.LDAPRepository;
import org.safehaus.penrose.federation.FederationRepositoryConfig;

/**
 * @author Endi S. Dewata
 */
public class EditLDAPRepositoryWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    LDAPConnectionSettingsWizardPage connectionPage;
    //LDAPPartitionsWizardPage    partitionsPage;
    //LinkingParametersWizardPage linkingPage;

    FederationRepositoryConfig repository;

    public EditLDAPRepositoryWizard(FederationRepositoryConfig repository) {
        this.repository = repository;

        setWindowTitle("Edit LDAP Repository");
    }

    public void addPages() {

        connectionPage = new LDAPConnectionSettingsWizardPage();

        connectionPage.setProviderUrl(repository.getParameter(LDAPRepository.URL));
        connectionPage.setSuffix(repository.getParameter(LDAPRepository.SUFFIX));
        connectionPage.setBindDn(repository.getParameter(LDAPRepository.USER));
        connectionPage.setBindPassword(repository.getParameter(LDAPRepository.PASSWORD));

        addPage(connectionPage);
/*
        partitionsPage = new LDAPPartitionsWizardPage();

        partitionsPage.setSuffix(repository.getParameter(LDAPRepository.SUFFIX));
        partitionsPage.setTemplate(repository.getParameter(LDAPRepository.TEMPLATE));

        addPage(partitionsPage);

        linkingPage = new LinkingParametersWizardPage();

        linkingPage.setLocalAttribute(repository.getParameter(FederationRepositoryConfig.LINKING_LOCAL_ATTRIBUTE));
        linkingPage.setGlobalAttribute(repository.getParameter(FederationRepositoryConfig.LINKING_GLOBAL_ATTRIBUTE));
        linkingPage.setImportMappingName(repository.getParameter(FederationRepositoryConfig.IMPORT_MAPPING_NAME));
        linkingPage.setImportMappingPrefix(repository.getParameter(FederationRepositoryConfig.IMPORT_MAPPING_PREFIX));

        addPage(linkingPage);
*/
    }

    public boolean canFinish() {
        if (!connectionPage.isPageComplete()) return false;
        //if (!partitionsPage.isPageComplete()) return false;
        //if (!linkingPage.isPageComplete()) return false;
        return true;
    }

    public boolean performFinish() {
        try {
            repository.setParameter(LDAPRepository.URL, connectionPage.getProviderUrl());
            repository.setParameter(LDAPRepository.SUFFIX, connectionPage.getSuffix());
            repository.setParameter(LDAPRepository.USER, connectionPage.getBindDn());
            repository.setParameter(LDAPRepository.PASSWORD, connectionPage.getBindPassword());
/*
            repository.setParameter(LDAPRepository.SUFFIX, partitionsPage.getSuffix());
            repository.setParameter(LDAPRepository.TEMPLATE, partitionsPage.getTemplate());

            repository.setParameter(FederationRepositoryConfig.LINKING_LOCAL_ATTRIBUTE, linkingPage.getLocalAttribute());
            repository.setParameter(FederationRepositoryConfig.LINKING_GLOBAL_ATTRIBUTE, linkingPage.getGlobalAttribute());
            repository.setParameter(FederationRepositoryConfig.IMPORT_MAPPING_NAME, linkingPage.getImportMappingName());
            repository.setParameter(FederationRepositoryConfig.IMPORT_MAPPING_PREFIX, linkingPage.getImportMappingPrefix());
*/
            return true;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
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
}
