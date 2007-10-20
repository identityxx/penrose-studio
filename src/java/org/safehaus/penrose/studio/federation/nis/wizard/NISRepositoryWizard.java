package org.safehaus.penrose.studio.federation.nis.wizard;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.dialogs.MessageDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.safehaus.penrose.studio.federation.nis.NISDomain;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.federation.nis.NISFederation;
import org.safehaus.penrose.management.PenroseClient;
import org.safehaus.penrose.partition.PartitionConfig;

/**
 * @author Endi Sukma Dewata
 */
public class NISRepositoryWizard extends Wizard {
    
    public Logger log = LoggerFactory.getLogger(getClass());

    NISRepositoryWizardPage repositoryPage;
    NISDomainWizardPage domainPage;
    NISLdapWizardPage partitionPage;

    NISFederation nisFederation;
    Project project;

    public NISRepositoryWizard(NISFederation nisFederation) {
        setWindowTitle("Add NIS Domain");

        this.nisFederation = nisFederation;
        project = nisFederation.getProject();
    }

    public boolean canFinish() {
        if (!repositoryPage.isPageComplete()) return false;
        if (!domainPage.isPageComplete()) return false;
        if (!partitionPage.isPageComplete()) return false;
        return true;
    }

    public void addPages() {
        repositoryPage = new NISRepositoryWizardPage();
        addPage(repositoryPage);

        domainPage = new NISDomainWizardPage();
        addPage(domainPage);

        partitionPage = new NISLdapWizardPage();
        addPage(partitionPage);
    }

    public IWizardPage getNextPage(IWizardPage page) {
        if (repositoryPage == page) {
            String name = repositoryPage.getRepository();
            domainPage.setDomain(name);
            
        } else if (domainPage == page) {
            String name = repositoryPage.getRepository();
            String domainName = domainPage.getDomain();

            String suffix = "ou="+name+",ou=nis";
            String nssSuffix = "ou="+name+",ou=nss";

            String s[] = domainName.split("\\.");
            if (s.length > 2) {
                suffix = suffix+",dc="+s[s.length-2]+",dc="+s[s.length-1];
                nssSuffix = nssSuffix+",dc="+s[s.length-2]+",dc="+s[s.length-1];
            }

            partitionPage.setSuffix(suffix);
            partitionPage.setNssSuffix(nssSuffix);
        }

        return super.getNextPage(page);
    }

    public boolean performFinish() {

        NISDomain repository = new NISDomain();
        repository.setName(repositoryPage.getRepository());
        repository.setFullName(domainPage.getDomain());
        repository.setServer(domainPage.getServer());
        repository.setSuffix(partitionPage.getSuffix());
        repository.setNssSuffix(partitionPage.getNssSuffix());

        try {
            nisFederation.addRepository(repository);

        } catch (Exception e) {
            MessageDialog.openError(getShell(), "Failed creating repository.", e.getMessage());
            return false;
        }

        PartitionConfig partitionConfig = null;
        PartitionConfig nssPartitionConfig = null;
        try {
            partitionConfig = nisFederation.createPartitionConfig(repository);
            project.upload("partitions/"+ repository.getName());

            nssPartitionConfig = nisFederation.createNssPartitionConfig(repository);
            project.upload("partitions/"+ nssPartitionConfig.getName());

        } catch (Exception e) {
            MessageDialog.openError(getShell(), "Failed creating partition.", e.getMessage());
            return false;
        }

        try {
            nisFederation.createDatabase(repository);

        } catch (Exception e) {
            MessageDialog.openError(getShell(), "Failed creating database.", e.getMessage());
            return false;
        }

        PenroseClient penroseClient = project.getClient();

        try {
            penroseClient.startPartition(repository.getName());
            nisFederation.loadPartition(partitionConfig);

            penroseClient.startPartition(nssPartitionConfig.getName());
            nisFederation.loadPartition(nssPartitionConfig);

        } catch (Exception e) {
            MessageDialog.openError(getShell(), "Failed initializing partition.", e.getMessage());
            return false;
        }

        return true;
    }

    public boolean needsPreviousAndNextButtons() {
        return true;
    }
}
