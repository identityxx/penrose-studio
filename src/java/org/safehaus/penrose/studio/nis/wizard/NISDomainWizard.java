package org.safehaus.penrose.studio.nis.wizard;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.dialogs.MessageDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.safehaus.penrose.nis.NISDomain;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.nis.NISTool;
import org.safehaus.penrose.management.PenroseClient;
import org.safehaus.penrose.management.SourceClient;
import org.safehaus.penrose.management.PartitionClient;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.partition.PartitionConfigs;
import org.safehaus.penrose.partition.PartitionConfig;
import org.safehaus.penrose.source.SourceConfig;

/**
 * @author Endi Sukma Dewata
 */
public class NISDomainWizard extends Wizard {
    
    public Logger log = LoggerFactory.getLogger(getClass());

    NISDomainWizardPage domainPage;
    NISPartitionWizardPage partitionPage;
    NISDatabaseWizardPage databasePage;

    NISTool nisTool;
    Project project;

    public NISDomainWizard(NISTool nisTool) {
        setWindowTitle("Add NIS Domain");

        this.nisTool = nisTool;
        project = nisTool.getProject();

        domainPage = new NISDomainWizardPage();
        partitionPage = new NISPartitionWizardPage();
        databasePage = new NISDatabaseWizardPage();
    }

    public boolean canFinish() {
        if (!domainPage.isPageComplete()) return false;
        if (!partitionPage.isPageComplete()) return false;
        if (!databasePage.isPageComplete()) return false;
        return true;
    }

    public void addPages() {
        addPage(domainPage);
        addPage(partitionPage);
        addPage(databasePage);
    }

    public IWizardPage getNextPage(IWizardPage page) {
        if (domainPage == page) {
            String domainName = domainPage.getDomain();
            int i = domainName.indexOf('.');
            if (i >= 0) domainName = domainName.substring(0, i);

            partitionPage.setShortName(domainName);

            String suffix = domainPage.getDomain();
            suffix = "ou="+suffix.replaceAll("\\.", ",dc=");

            partitionPage.setSuffix(suffix);
        }

        return super.getNextPage(page);
    }

    public boolean performFinish() {

        NISDomain domain = new NISDomain();

        domain.setName(partitionPage.getShortName());
        domain.setFullName(domainPage.getDomain());
        domain.setServer(domainPage.getServer());
        domain.setSuffix(partitionPage.getSuffix());

        try {
            nisTool.createDomain(domain);

        } catch (Exception e) {
            MessageDialog.openError(getShell(), "Failed creating domain.", e.getMessage());
            return false;
        }

        try {
            nisTool.createPartitionConfig(domain);
            project.upload("partitions/"+domain.getName());

        } catch (Exception e) {
            MessageDialog.openError(getShell(), "Failed creating partition.", e.getMessage());
            return false;
        }

        if (databasePage.isCreate()) {
            try {
                nisTool.createDatabase(domain);

            } catch (Exception e) {
                MessageDialog.openError(getShell(), "Failed creating database.", e.getMessage());
                return false;
            }
        }

        PenroseClient penroseClient = project.getClient();

        try {
            PartitionClient partitionClient = penroseClient.getPartitionClient(domain.getName());
            partitionClient.start();
            nisTool.loadPartition(domain);

        } catch (Exception e) {
            MessageDialog.openError(
                    getShell(),
                    "Failed initializing partition.",
                    e.getMessage()
            );

            return false;
        }

        return true;
    }

    public boolean needsPreviousAndNextButtons() {
        return true;
    }
}
