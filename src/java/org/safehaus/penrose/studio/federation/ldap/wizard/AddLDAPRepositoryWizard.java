package org.safehaus.penrose.studio.federation.ldap.wizard;

import org.eclipse.jface.wizard.Wizard;
import org.safehaus.penrose.federation.repository.LDAPRepository;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.studio.federation.Federation;
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

    LDAPRepositoryWizardPage repositoryPage;
    LDAPConnectionWizardPage connectionPage;
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

        linkingPage = new LinkingParametersWizardPage();
        addPage(linkingPage);
    }

    public boolean canFinish() {
        if (!repositoryPage.isPageComplete()) return false;
        if (!connectionPage.isPageComplete()) return false;
        if (!linkingPage.isPageComplete()) return false;
        return true;
    }

    public boolean performFinish() {

        try {
            Map<String,String> parameters = connectionPage.getParameters();

            repository.setName(repositoryPage.getRepository());
            repository.setUrl(parameters.get(Context.PROVIDER_URL));
            repository.setUser(parameters.get(Context.SECURITY_PRINCIPAL));
            repository.setPassword(parameters.get(Context.SECURITY_CREDENTIALS));
            repository.setSuffix(connectionPage.getSuffix());

            repository.setParameter(Federation.LINKING_LOCAL_ATTRIBUTE, linkingPage.getLocalAttribute());
            repository.setParameter(Federation.LINKING_GLOBAL_ATTRIBUTE, linkingPage.getGlobalAttribute());
            repository.setParameter(Federation.IMPORT_MAPPING_NAME, linkingPage.getImportMappingName());
            repository.setParameter(Federation.IMPORT_MAPPING_PREFIX, linkingPage.getImportMappingPrefix());

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
