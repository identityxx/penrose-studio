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
        try {
            NISDomain repository = new NISDomain();
            repository.setName(repositoryPage.getRepository());
            repository.setFullName(domainPage.getDomain());
            repository.setServer(domainPage.getServer());
            repository.setSuffix(partitionPage.getSuffix());
            repository.setNssSuffix(partitionPage.getNssSuffix());

            nisFederation.addRepository(repository);

            PartitionConfig partitionConfig = nisFederation.createPartitionConfig(repository);
            project.upload("partitions/"+ repository.getName());

            PartitionConfig nssPartitionConfig = nisFederation.createNssPartitionConfig(repository);
            project.upload("partitions/"+ nssPartitionConfig.getName());

            nisFederation.createDatabase(repository);

            PenroseClient penroseClient = project.getClient();

            penroseClient.startPartition(repository.getName());
            nisFederation.loadPartition(partitionConfig);

            penroseClient.startPartition(nssPartitionConfig.getName());
            nisFederation.loadPartition(nssPartitionConfig);

            return true;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            MessageDialog.openError(getShell(), "Action Failed", e.getMessage());
            return false;
        }
    }

    public boolean needsPreviousAndNextButtons() {
        return true;
    }
}
