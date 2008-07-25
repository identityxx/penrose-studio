package org.safehaus.penrose.studio.federation.ldap.wizard;

import org.eclipse.jface.wizard.Wizard;
import org.apache.log4j.Logger;
import org.safehaus.penrose.studio.ldap.connection.LDAPConnectionWizardPage;
import org.safehaus.penrose.studio.federation.linking.wizard.LinkingParametersWizardPage;
import org.safehaus.penrose.studio.federation.Federation;
import org.safehaus.penrose.federation.repository.LDAPRepository;

/**
 * @author Endi S. Dewata
 */
public class EditLDAPRepositoryWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    LDAPConnectionWizardPage connectionPage;
    LinkingParametersWizardPage linkingPage;

    LDAPRepository repository;

    public EditLDAPRepositoryWizard(LDAPRepository repository) {
        this.repository = repository;

        setWindowTitle("Edit LDAP Repository");
    }

    public void addPages() {

        connectionPage = new LDAPConnectionWizardPage();

        connectionPage.setProviderUrl(repository.getUrl());
        connectionPage.setBindDn(repository.getUser());
        connectionPage.setBindPassword(repository.getPassword());
        connectionPage.setSuffix(repository.getSuffix());

        addPage(connectionPage);

        linkingPage = new LinkingParametersWizardPage();

        linkingPage.setLocalAttribute(repository.getParameter(Federation.LINKING_LOCAL_ATTRIBUTE));
        linkingPage.setGlobalAttribute(repository.getParameter(Federation.LINKING_GLOBAL_ATTRIBUTE));
        linkingPage.setImportMappingName(repository.getParameter(Federation.IMPORT_MAPPING_NAME));
        linkingPage.setImportMappingPrefix(repository.getParameter(Federation.IMPORT_MAPPING_PREFIX));

        addPage(linkingPage);
    }

    public boolean canFinish() {
        if (!connectionPage.isPageComplete()) return false;
        if (!linkingPage.isPageComplete()) return false;
        return true;
    }

    public boolean performFinish() {
        try {
            repository.setSuffix(connectionPage.getSuffix());

            repository.setUrl(connectionPage.getProviderUrl());
            repository.setUser(connectionPage.getBindDn());
            repository.setPassword(connectionPage.getBindPassword());

            repository.setParameter(Federation.LINKING_LOCAL_ATTRIBUTE, linkingPage.getLocalAttribute());
            repository.setParameter(Federation.LINKING_GLOBAL_ATTRIBUTE, linkingPage.getGlobalAttribute());
            repository.setParameter(Federation.IMPORT_MAPPING_NAME, linkingPage.getImportMappingName());
            repository.setParameter(Federation.IMPORT_MAPPING_PREFIX, linkingPage.getImportMappingPrefix());

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
