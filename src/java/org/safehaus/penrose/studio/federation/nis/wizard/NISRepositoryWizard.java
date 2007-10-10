package org.safehaus.penrose.studio.federation.nis.wizard;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.dialogs.MessageDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.safehaus.penrose.studio.federation.nis.NISRepository;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.federation.nis.NISFederation;
import org.safehaus.penrose.management.PenroseClient;

/**
 * @author Endi Sukma Dewata
 */
public class NISRepositoryWizard extends Wizard {
    
    public Logger log = LoggerFactory.getLogger(getClass());

    NISRepositoryWizardPage repositoryPage;
    NISPartitionWizardPage partitionPage;

    NISFederation nisFederation;
    Project project;

    public NISRepositoryWizard(NISFederation nisFederation) {
        setWindowTitle("Add NIS Domain");

        this.nisFederation = nisFederation;
        project = nisFederation.getProject();
    }

    public boolean canFinish() {
        if (!repositoryPage.isPageComplete()) return false;
        if (!partitionPage.isPageComplete()) return false;
        return true;
    }

    public void addPages() {
        repositoryPage = new NISRepositoryWizardPage();
        addPage(repositoryPage);

        partitionPage = new NISPartitionWizardPage();
        addPage(partitionPage);
    }

    public IWizardPage getNextPage(IWizardPage page) {
        if (repositoryPage == page) {
            String domainName = repositoryPage.getDomain();
            int i = domainName.indexOf('.');
            if (i >= 0) domainName = domainName.substring(0, i);

            partitionPage.setRepository(domainName);

            String suffix = repositoryPage.getDomain();
            suffix = "ou="+suffix.replaceAll("\\.", ",dc=");

            partitionPage.setSuffix(suffix);
        }

        return super.getNextPage(page);
    }

    public boolean performFinish() {

        NISRepository repository = new NISRepository();
        repository.setName(partitionPage.getRepository());
        repository.setFullName(repositoryPage.getDomain());
        repository.setServer(repositoryPage.getServer());
        repository.setSuffix(partitionPage.getSuffix());

        try {
            nisFederation.addRepository(repository);

        } catch (Exception e) {
            MessageDialog.openError(getShell(), "Failed creating repository.", e.getMessage());
            return false;
        }

        try {
            nisFederation.createPartitionConfig(repository);
            project.upload("partitions/"+ repository.getName());

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
            nisFederation.loadPartition(repository);

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
