package org.safehaus.penrose.studio.nis.wizard;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.safehaus.penrose.nis.NISDomain;

/**
 * @author Endi Sukma Dewata
 */
public class NewNISDomainWizard extends Wizard {
    
    public Logger log = LoggerFactory.getLogger(getClass());

    NISDomainWizardPage domainPage;
    NISPartitionWizardPage partitionPage;

    private NISDomain domain;

    public NewNISDomainWizard() {
        setWindowTitle("New NIS Domain");

        domainPage = new NISDomainWizardPage();
        partitionPage = new NISPartitionWizardPage();
    }

    public boolean canFinish() {
        if (!domainPage.isPageComplete()) return false;
        if (!partitionPage.isPageComplete()) return false;
        return true;
    }

    public void addPages() {
        addPage(domainPage);
        addPage(partitionPage);
    }

    public IWizardPage getNextPage(IWizardPage page) {
        if (domainPage == page) {
            String partition = domainPage.getDomain();
            int i = partition.indexOf('.');
            if (i >= 0) partition = partition.substring(0, i);

            partitionPage.setPartition(partition);

            String suffix = domainPage.getDomain();
            suffix = "dc="+suffix.replaceAll("\\.", ",dc=");

            partitionPage.setSuffix(suffix);
        }

        return super.getNextPage(page);
    }

    public boolean performFinish() {
        try {
            domain = new NISDomain();

            domain.setName(domainPage.getDomain());
            domain.setServer(domainPage.getServer());

            domain.setPartition(partitionPage.getPartition());
            domain.setSuffix(partitionPage.getSuffix());

            return true;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    public boolean needsPreviousAndNextButtons() {
        return true;
    }

    public NISDomain getDomain() {
        return domain;
    }

    public void setDomain(NISDomain domain) {
        this.domain = domain;
    }
}
