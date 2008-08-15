package org.safehaus.penrose.studio.federation.nis.wizard;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.safehaus.penrose.federation.NISDomain;
import org.safehaus.penrose.federation.Repository;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.studio.federation.linking.wizard.LinkingParametersWizardPage;

/**
 * @author Endi Sukma Dewata
 */
public class AddNISDomainWizard extends Wizard {
    
    public Logger log = LoggerFactory.getLogger(getClass());

    NISRepositoryWizardPage     repositoryPage;
    NISDomainWizardPage         connectionPage;
    NISPartitionsWizardPage     partitionsPage;
    LinkingParametersWizardPage linkingPage;

    NISDomain repository = new NISDomain();

    public AddNISDomainWizard() {
        setWindowTitle("Add NIS Domain");
    }

    public void addPages() {
        repositoryPage = new NISRepositoryWizardPage();
        addPage(repositoryPage);

        connectionPage = new NISDomainWizardPage();
        addPage(connectionPage);

        partitionsPage = new NISPartitionsWizardPage();
        addPage(partitionsPage);

        linkingPage = new LinkingParametersWizardPage();
        addPage(linkingPage);
    }

    public boolean canFinish() {
        if (!repositoryPage.isPageComplete()) return false;
        if (!connectionPage.isPageComplete()) return false;
        if (!partitionsPage.isPageComplete()) return false;
        if (!linkingPage.isPageComplete()) return false;
        return true;
    }

    public IWizardPage getNextPage(IWizardPage page) {
        if (repositoryPage == page) {
            String name = repositoryPage.getRepositoryName();
            connectionPage.setDomain(name);
            
        } else if (connectionPage == page) {
            String name = repositoryPage.getRepositoryName();
            String domainName = connectionPage.getDomain();

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

            partitionsPage.setNisSuffix(nisSuffix);
            partitionsPage.setYpSuffix(ypSuffix);
            partitionsPage.setNssSuffix(nssSuffix);
        }

        return super.getNextPage(page);
    }

    public boolean performFinish() {
        try {
            repository.setName(repositoryPage.getRepositoryName());

            repository.setParameter(NISDomain.NIS_SERVER, connectionPage.getServer());
            repository.setParameter(NISDomain.NIS_DOMAIN, connectionPage.getDomain());

            repository.setParameter(NISDomain.YP_ENABLED, partitionsPage.isYpEnabled());
            repository.setParameter(NISDomain.YP_SUFFIX, partitionsPage.getYpSuffix());

            repository.setParameter(NISDomain.NIS_ENABLED, partitionsPage.isNisEnabled());
            repository.setParameter(NISDomain.NIS_SUFFIX, partitionsPage.getNisSuffix());

            repository.setParameter(NISDomain.NSS_ENABLED, partitionsPage.isNssEnabled());
            repository.setParameter(NISDomain.NSS_SUFFIX, partitionsPage.getNssSuffix());

            repository.setParameter(Repository.LINKING_LOCAL_ATTRIBUTE, linkingPage.getLocalAttribute());
            repository.setParameter(Repository.LINKING_GLOBAL_ATTRIBUTE, linkingPage.getGlobalAttribute());
            repository.setParameter(Repository.IMPORT_MAPPING_NAME, linkingPage.getImportMappingName());
            repository.setParameter(Repository.IMPORT_MAPPING_PREFIX, linkingPage.getImportMappingPrefix());

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

    public NISDomain getRepository() {
        return repository;
    }
}
