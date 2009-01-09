package org.safehaus.penrose.studio.federation.ldap.wizard;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.safehaus.penrose.federation.LDAPRepository;
import org.safehaus.penrose.federation.FederationRepositoryConfig;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.studio.ldap.connection.wizard.LDAPConnectionSettingsWizardPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author Endi Sukma Dewata
 */
public class AddLDAPRepositoryWizard extends Wizard {

    public Logger log = LoggerFactory.getLogger(getClass());

    LDAPRepositoryWizardPage    repositoryPage;
    LDAPConnectionSettingsWizardPage connectionPage;

    //LDAPPartitionsWizardPage    partitionsPage;
    //LinkingParametersWizardPage linkingPage;

    FederationRepositoryConfig repository = new FederationRepositoryConfig();

    public AddLDAPRepositoryWizard() {
        setWindowTitle("Add LDAP Repository");
    }

    public void addPages() {

        repositoryPage = new LDAPRepositoryWizardPage();
        addPage(repositoryPage);

        connectionPage = new LDAPConnectionSettingsWizardPage();
        addPage(connectionPage);
/*
        partitionsPage = new LDAPPartitionsWizardPage();
        addPage(partitionsPage);

        linkingPage = new LinkingParametersWizardPage();
        addPage(linkingPage);
*/
    }

    public boolean canFinish() {
        if (!repositoryPage.isPageComplete()) return false;
        if (!connectionPage.isPageComplete()) return false;
        //if (!partitionsPage.isPageComplete()) return false;
        //if (!linkingPage.isPageComplete()) return false;
        return true;
    }

    public IWizardPage getNextPage(IWizardPage page) {
/*
        if (connectionPage == page) {
            String suffix = connectionPage.getSuffix();
            partitionsPage.setSuffix(suffix);
        }
*/
        return super.getNextPage(page);
    }

    public boolean performFinish() {

        try {
            Map<String,String> parameters = connectionPage.getParameters();

            repository.setType("LDAP");
            repository.setName(repositoryPage.getRepository());
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
            ErrorDialog.open(e);
            return false;
        }
    }

    public FederationRepositoryConfig getRepository() {
        return repository;
    }
}
