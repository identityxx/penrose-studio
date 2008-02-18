package org.safehaus.penrose.studio.federation.nis.wizard;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.safehaus.penrose.federation.repository.NISDomain;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.federation.nis.NISFederation;
import org.safehaus.penrose.studio.dialog.ErrorDialog;

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

            String nisSuffix = "ou="+name+",ou=nis";
            String ypSuffix  = "ou="+name+",ou=yp";
            String nssSuffix = "ou="+name+",ou=nss";

            String s[] = domainName.split("\\.");
            if (s.length > 2) {
                String suffix = ",dc="+s[s.length-2]+",dc="+s[s.length-1];
                nisSuffix = nisSuffix+suffix;
                ypSuffix  = ypSuffix+suffix;
                nssSuffix = nssSuffix+suffix;
            }

            partitionPage.setNisSuffix(nisSuffix);
            partitionPage.setYpSuffix(ypSuffix);
            partitionPage.setNssSuffix(nssSuffix);
        }

        return super.getNextPage(page);
    }

    public boolean performFinish() {
        try {
            NISDomain domain = new NISDomain();
            domain.setName(repositoryPage.getRepository());
            domain.setFullName(domainPage.getDomain());
            domain.setServer(domainPage.getServer());

            domain.setNisEnabled(partitionPage.isNisEnabled());
            domain.setNisSuffix(partitionPage.getNisSuffix());

            domain.setYpEnabled(partitionPage.isYpEnabled());
            domain.setYpSuffix(partitionPage.getYpSuffix());

            domain.setNssEnabled(partitionPage.isNssEnabled());
            domain.setNssSuffix(partitionPage.getNssSuffix());

            nisFederation.addRepository(domain);

            nisFederation.createPartitions(domain);

            return true;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
            return false;
        }
    }

    public boolean needsPreviousAndNextButtons() {
        return true;
    }
}
