package org.safehaus.penrose.studio.nis.wizard;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.dialogs.MessageDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.safehaus.penrose.nis.NISDomain;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.nis.NISNode;
import org.safehaus.penrose.studio.nis.NISTool;

/**
 * @author Endi Sukma Dewata
 */
public class NISDomainWizard extends Wizard {
    
    public Logger log = LoggerFactory.getLogger(getClass());

    Project project;

    NISDomainWizardPage domainPage;
    NISPartitionWizardPage partitionPage;
    NISDatabaseWizardPage databasePage;

    NISNode nisNode;

    public NISDomainWizard(Project project, NISNode nisNode) {
        setWindowTitle("Add NIS Domain");

        this.project = project;
        this.nisNode = nisNode;

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
            suffix = "dc="+suffix.replaceAll("\\.", ",dc=");

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

        NISTool nisTool = nisNode.getNisTool();

        try {
            nisTool.createDomain(domain);
            nisNode.addNisDomain(domain);

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

        try {
            nisTool.loadPartition(domain);

        } catch (Exception e) {
            MessageDialog.openError(
                    getShell(),
                    "Failed initializing partition.",
                    e.getMessage()
            );

            return false;
        }

        if (databasePage.isLoad()) {
            try {
                nisTool.loadCache(domain);

            } catch (Exception e) {
                MessageDialog.openError(getShell(), "Failed loading database.", e.getMessage());
                return false;
            }
        }

        return true;
    }

    public boolean needsPreviousAndNextButtons() {
        return true;
    }
}
