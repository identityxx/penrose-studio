package org.safehaus.penrose.studio.federation.nis.wizard;

import org.eclipse.jface.wizard.Wizard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.safehaus.penrose.federation.NISDomain;
import org.safehaus.penrose.federation.FederationRepositoryConfig;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.studio.federation.linking.wizard.LinkingParametersWizardPage;

/**
 * @author Endi Sukma Dewata
 */
public class EditNISDomainWizard extends Wizard {

    public Logger log = LoggerFactory.getLogger(getClass());

    NISDomainWizardPage         connectionPage;
    //NISPartitionsWizardPage     partitionsPage;
    //LinkingParametersWizardPage linkingPage;

    FederationRepositoryConfig repository;

    public EditNISDomainWizard(FederationRepositoryConfig domain) {
        this.repository = domain;

        setWindowTitle("Edit NIS Domain");
    }

    public void addPages() {

        connectionPage = new NISDomainWizardPage();

        connectionPage.setServer(repository.getParameter(NISDomain.SERVER));
        connectionPage.setDomain(repository.getParameter(NISDomain.DOMAIN));

        addPage(connectionPage);
/*
        partitionsPage = new NISPartitionsWizardPage();

        partitionsPage.setYpSuffix(repository.getParameter(NISDomain.YP_SUFFIX));
        partitionsPage.setYpTemplate(repository.getParameter(NISDomain.YP_TEMPLATE));

        partitionsPage.setNisSuffix(repository.getParameter(NISDomain.NIS_SUFFIX));
        partitionsPage.setNisTemplate(repository.getParameter(NISDomain.NIS_TEMPLATE));

        partitionsPage.setNssSuffix(repository.getParameter(NISDomain.NSS_SUFFIX));
        partitionsPage.setNssTemplate(repository.getParameter(NISDomain.NSS_TEMPLATE));

        addPage(partitionsPage);

        linkingPage = new LinkingParametersWizardPage();

        linkingPage.setLocalAttribute(repository.getParameter(FederationRepositoryConfig.LINKING_LOCAL_ATTRIBUTE));
        linkingPage.setGlobalAttribute(repository.getParameter(FederationRepositoryConfig.LINKING_GLOBAL_ATTRIBUTE));
        linkingPage.setImportMappingName(repository.getParameter(FederationRepositoryConfig.IMPORT_MAPPING_NAME));
        linkingPage.setImportMappingPrefix(repository.getParameter(FederationRepositoryConfig.IMPORT_MAPPING_PREFIX));

        addPage(linkingPage);
*/
    }

    public boolean canFinish() {
        if (!connectionPage.isPageComplete()) return false;
        //if (!partitionsPage.isPageComplete()) return false;
        //if (!linkingPage.isPageComplete()) return false;
        return true;
    }

    public boolean performFinish() {
        try {
            repository.setParameter(NISDomain.SERVER, connectionPage.getServer());
            repository.setParameter(NISDomain.DOMAIN, connectionPage.getDomain());
/*
            repository.setParameter(NISDomain.YP_SUFFIX, partitionsPage.getYpSuffix());
            repository.setParameter(NISDomain.YP_TEMPLATE, partitionsPage.getYpTemplate());

            repository.setParameter(NISDomain.NIS_SUFFIX, partitionsPage.getNisSuffix());
            repository.setParameter(NISDomain.NIS_TEMPLATE, partitionsPage.getNisTemplate());

            repository.setParameter(NISDomain.NSS_SUFFIX, partitionsPage.getNssSuffix());
            repository.setParameter(NISDomain.NSS_TEMPLATE, partitionsPage.getNssTemplate());

            repository.setParameter(FederationRepositoryConfig.LINKING_LOCAL_ATTRIBUTE, linkingPage.getLocalAttribute());
            repository.setParameter(FederationRepositoryConfig.LINKING_GLOBAL_ATTRIBUTE, linkingPage.getGlobalAttribute());
            repository.setParameter(FederationRepositoryConfig.IMPORT_MAPPING_NAME, linkingPage.getImportMappingName());
            repository.setParameter(FederationRepositoryConfig.IMPORT_MAPPING_PREFIX, linkingPage.getImportMappingPrefix());
*/
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