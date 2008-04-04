package org.safehaus.penrose.studio.federation.ldap.wizard;

import org.eclipse.jface.wizard.Wizard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.safehaus.penrose.studio.federation.ldap.LDAPFederation;
import org.safehaus.penrose.federation.repository.LDAPRepository;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.ldap.connection.LDAPConnectionWizardPage;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.connection.ConnectionConfig;

import javax.naming.Context;
import java.util.Map;

/**
 * @author Endi Sukma Dewata
 */
public class LDAPRepositoryWizard extends Wizard {

    public Logger log = LoggerFactory.getLogger(getClass());

    LDAPRepositoryWizardPage repositoryPage;
    LDAPConnectionWizardPage connectionPage;
    //LDAPPartitionsWizardPage partitionsPage;

    LDAPFederation ldapFederation;
    Project project;

    ConnectionConfig ldapConfig = new ConnectionConfig();

    public LDAPRepositoryWizard(LDAPFederation ldapFederation) {
        setWindowTitle("Add LDAP Repository");

        this.ldapFederation = ldapFederation;
        project = ldapFederation.getProject();
    }

    public boolean canFinish() {
        if (!repositoryPage.isPageComplete()) return false;
        if (!connectionPage.isPageComplete()) return false;
        //if (!partitionsPage.isPageComplete()) return false;
        return true;
    }

    public void addPages() {
        repositoryPage = new LDAPRepositoryWizardPage();
        addPage(repositoryPage);

        connectionPage = new LDAPConnectionWizardPage();
        connectionPage.setDescription("Enter LDAP connection parameters.");
        connectionPage.setParameters(ldapConfig.getParameters());
        addPage(connectionPage);

        //partitionsPage = new LDAPPartitionsWizardPage();
        //addPage(partitionsPage);
    }
/*
    public IWizardPage getNextPage(IWizardPage page) {
        if (connectionPage == page) {
            String suffix = connectionPage.getSuffix();

            partitionsPage.setSuffix(suffix);
        }
        return super.getNextPage(page);
    }
*/
    public boolean performFinish() {

        try {
            Map<String,String> map = connectionPage.getParameters();

            LDAPRepository repository = new LDAPRepository();
            repository.setName(repositoryPage.getRepository());
            repository.setUrl(map.get(Context.PROVIDER_URL));
            repository.setUser(map.get(Context.SECURITY_PRINCIPAL));
            repository.setPassword(map.get(Context.SECURITY_CREDENTIALS));
            repository.setSuffix(connectionPage.getSuffix());

            ldapFederation.addRepository(repository);

            ldapFederation.createPartitions(repository);

            return true;

        } catch (Exception e) {
            ErrorDialog.open(e);
            return false;
        }
    }
}
