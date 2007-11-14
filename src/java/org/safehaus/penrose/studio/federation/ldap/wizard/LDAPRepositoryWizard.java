package org.safehaus.penrose.studio.federation.ldap.wizard;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.dialogs.MessageDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.safehaus.penrose.studio.federation.ldap.LDAPFederation;
import org.safehaus.penrose.studio.federation.ldap.LDAPRepository;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.jndi.connection.JNDIConnectionInfoWizardPage;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.connection.ConnectionConfig;
import org.safehaus.penrose.management.PenroseClient;
import org.safehaus.penrose.partition.PartitionConfig;

import javax.naming.Context;
import java.util.Map;

/**
 * @author Endi Sukma Dewata
 */
public class LDAPRepositoryWizard extends Wizard {

    public Logger log = LoggerFactory.getLogger(getClass());

    LDAPRepositoryWizardPage repositoryPage;
    JNDIConnectionInfoWizardPage ldapPage;

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
        if (!ldapPage.isPageComplete()) return false;
        return true;
    }

    public void addPages() {
        repositoryPage = new LDAPRepositoryWizardPage();
        addPage(repositoryPage);

        ldapPage = new JNDIConnectionInfoWizardPage();
        ldapPage.setDescription("Enter LDAP connection parameters.");
        ldapPage.setParameters(ldapConfig.getParameters());
        addPage(ldapPage);

    }

    public boolean performFinish() {

        try {
            Map<String,String> map = ldapPage.getParameters();

            LDAPRepository repository = new LDAPRepository();
            repository.setName(repositoryPage.getRepository());
            repository.setUrl(map.get(Context.PROVIDER_URL));
            repository.setUser(map.get(Context.SECURITY_PRINCIPAL));
            repository.setPassword(map.get(Context.SECURITY_CREDENTIALS));

            ldapFederation.addRepository(repository);

            PartitionConfig partitionConfig = ldapFederation.createPartitionConfig(repository);
            project.upload("partitions/"+ repository.getName());

            PenroseClient penroseClient = project.getClient();

            penroseClient.startPartition(repository.getName());
            ldapFederation.loadPartition(partitionConfig);

            return true;

        } catch (Exception e) {
            ErrorDialog.open(e);
            return false;
        }
    }
}
