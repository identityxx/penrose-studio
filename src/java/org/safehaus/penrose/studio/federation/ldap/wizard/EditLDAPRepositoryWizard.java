package org.safehaus.penrose.studio.federation.ldap.wizard;

import org.eclipse.jface.wizard.Wizard;
import org.apache.log4j.Logger;
import org.safehaus.penrose.studio.ldap.connection.LDAPConnectionWizardPage;
import org.safehaus.penrose.studio.federation.linking.wizard.LinkingParametersWizardPage;
import org.safehaus.penrose.federation.LDAPRepository;
import org.safehaus.penrose.federation.Repository;

/**
 * @author Endi S. Dewata
 */
public class EditLDAPRepositoryWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    LDAPConnectionWizardPage    connectionPage;
    LDAPPartitionsWizardPage    partitionsPage;
    LinkingParametersWizardPage linkingPage;

    LDAPRepository repository;

    public EditLDAPRepositoryWizard(LDAPRepository repository) {
        this.repository = repository;

        setWindowTitle("Edit LDAP Repository");
    }

    public void addPages() {

        connectionPage = new LDAPConnectionWizardPage();

        connectionPage.setProviderUrl(repository.getParameter(LDAPRepository.LDAP_URL));
        connectionPage.setSuffix(repository.getParameter(LDAPRepository.LDAP_SUFFIX));
        connectionPage.setBindDn(repository.getParameter(LDAPRepository.LDAP_USER));
        connectionPage.setBindPassword(repository.getParameter(LDAPRepository.LDAP_PASSWORD));

        addPage(connectionPage);

        partitionsPage = new LDAPPartitionsWizardPage();

        partitionsPage.setSuffix(repository.getParameter(LDAPRepository.SUFFIX));
        partitionsPage.setTemplate(repository.getParameter(LDAPRepository.TEMPLATE));

        addPage(partitionsPage);

        linkingPage = new LinkingParametersWizardPage();

        linkingPage.setLocalAttribute(repository.getParameter(Repository.LINKING_LOCAL_ATTRIBUTE));
        linkingPage.setGlobalAttribute(repository.getParameter(Repository.LINKING_GLOBAL_ATTRIBUTE));
        linkingPage.setImportMappingName(repository.getParameter(Repository.IMPORT_MAPPING_NAME));
        linkingPage.setImportMappingPrefix(repository.getParameter(Repository.IMPORT_MAPPING_PREFIX));

        addPage(linkingPage);
    }

    public boolean canFinish() {
        if (!connectionPage.isPageComplete()) return false;
        if (!partitionsPage.isPageComplete()) return false;
        if (!linkingPage.isPageComplete()) return false;
        return true;
    }

    public boolean performFinish() {
        try {
            repository.setParameter(LDAPRepository.LDAP_URL, connectionPage.getProviderUrl());
            repository.setParameter(LDAPRepository.LDAP_SUFFIX, connectionPage.getSuffix());
            repository.setParameter(LDAPRepository.LDAP_USER, connectionPage.getBindDn());
            repository.setParameter(LDAPRepository.LDAP_PASSWORD, connectionPage.getBindPassword());

            repository.setParameter(LDAPRepository.SUFFIX, partitionsPage.getSuffix());
            repository.setParameter(LDAPRepository.TEMPLATE, partitionsPage.getTemplate());

            repository.setParameter(Repository.LINKING_LOCAL_ATTRIBUTE, linkingPage.getLocalAttribute());
            repository.setParameter(Repository.LINKING_GLOBAL_ATTRIBUTE, linkingPage.getGlobalAttribute());
            repository.setParameter(Repository.IMPORT_MAPPING_NAME, linkingPage.getImportMappingName());
            repository.setParameter(Repository.IMPORT_MAPPING_PREFIX, linkingPage.getImportMappingPrefix());

            return true;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    public boolean needsPreviousAndNextButtons() {
        return true;
    }

    public LDAPRepository getRepository() {
        return repository;
    }

    public void setRepository(LDAPRepository repository) {
        this.repository = repository;
    }
}
