package org.safehaus.penrose.studio.federation.ldap.wizard;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.safehaus.penrose.federation.LDAPRepository;
import org.safehaus.penrose.federation.Repository;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.studio.federation.linking.wizard.LinkingParametersWizardPage;
import org.safehaus.penrose.studio.ldap.connection.LDAPConnectionWizardPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import java.util.Map;

/**
 * @author Endi Sukma Dewata
 */
public class AddLDAPRepositoryWizard extends Wizard {

    public Logger log = LoggerFactory.getLogger(getClass());

    LDAPRepositoryWizardPage    repositoryPage;
    LDAPConnectionWizardPage    connectionPage;
    LDAPPartitionsWizardPage    partitionsPage;
    LinkingParametersWizardPage linkingPage;

    LDAPRepository repository = new LDAPRepository();

    public AddLDAPRepositoryWizard() {
        setWindowTitle("Add LDAP Repository");
    }

    public void addPages() {

        repositoryPage = new LDAPRepositoryWizardPage();
        addPage(repositoryPage);

        connectionPage = new LDAPConnectionWizardPage();
        addPage(connectionPage);

        partitionsPage = new LDAPPartitionsWizardPage();
        addPage(partitionsPage);

        linkingPage = new LinkingParametersWizardPage();
        addPage(linkingPage);
    }

    public boolean canFinish() {
        if (!repositoryPage.isPageComplete()) return false;
        if (!connectionPage.isPageComplete()) return false;
        if (!partitionsPage.isPageComplete()) return false;
        if (!linkingPage.isPageComplete()) return false;
        return true;
    }

    public IWizardPage getNextPage(IWizardPage page) {
        if (connectionPage == page) {
            String suffix = connectionPage.getSuffix();
            partitionsPage.setSuffix(suffix);
        }

        return super.getNextPage(page);
    }

    public boolean performFinish() {

        try {
            Map<String,String> parameters = connectionPage.getParameters();

            repository.setName(repositoryPage.getRepository());
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
            ErrorDialog.open(e);
            return false;
        }
    }

    public LDAPRepository getRepository() {
        return repository;
    }
}
