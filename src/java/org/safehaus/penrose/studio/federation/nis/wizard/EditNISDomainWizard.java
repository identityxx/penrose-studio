package org.safehaus.penrose.studio.federation.nis.wizard;

import org.eclipse.jface.wizard.Wizard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.safehaus.penrose.federation.NISDomain;
import org.safehaus.penrose.federation.Repository;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.studio.federation.linking.wizard.LinkingParametersWizardPage;

/**
 * @author Endi Sukma Dewata
 */
public class EditNISDomainWizard extends Wizard {

    public Logger log = LoggerFactory.getLogger(getClass());

    NISDomainWizardPage         connectionPage;
    NISPartitionsWizardPage     partitionsPage;
    LinkingParametersWizardPage linkingPage;

    NISDomain repository;

    public EditNISDomainWizard(NISDomain domain) {
        this.repository = domain;

        setWindowTitle("Edit NIS Domain");
    }

    public void addPages() {

        connectionPage = new NISDomainWizardPage();

        connectionPage.setServer(repository.getParameter(NISDomain.NIS_SERVER));
        connectionPage.setDomain(repository.getParameter(NISDomain.NIS_DOMAIN));

        addPage(connectionPage);

        partitionsPage = new NISPartitionsWizardPage();

        partitionsPage.setYpEnabled(repository.getBooleanParameter(NISDomain.YP_ENABLED));
        partitionsPage.setYpSuffix(repository.getParameter(NISDomain.YP_SUFFIX));
        partitionsPage.setYpTemplate(repository.getParameter(NISDomain.YP_TEMPLATE));

        partitionsPage.setNisEnabled(repository.getBooleanParameter(NISDomain.NIS_ENABLED));
        partitionsPage.setNisSuffix(repository.getParameter(NISDomain.NIS_SUFFIX));
        partitionsPage.setNisTemplate(repository.getParameter(NISDomain.NIS_TEMPLATE));

        partitionsPage.setNssEnabled(repository.getBooleanParameter(NISDomain.NSS_ENABLED));
        partitionsPage.setNssSuffix(repository.getParameter(NISDomain.NSS_SUFFIX));
        partitionsPage.setNssTemplate(repository.getParameter(NISDomain.NSS_TEMPLATE));

        addPage(partitionsPage);

        linkingPage = new LinkingParametersWizardPage();

        linkingPage.setLocalAttribute(repository.getParameter(Repository.LINKING_LOCAL_ATTRIBUTE));
        linkingPage.setGlobalAttribute(repository.getParameter(Repository.LINKING_GLOBAL_ATTRIBUTE));
        linkingPage.setImportMappingName(repository.getParameter(Repository.IMPORT_MAPPING_NAME));
        linkingPage.setImportMappingPrefix(repository.getParameter(Repository.IMPORT_MAPPING_PREFIX));

        addPage(linkingPage);
    }

    public boolean canFinish() {
        if (!connectionPage.isPageComplete()) return false;
        if (!partitionsPage.isPageComplete()) return false;
        if (!linkingPage.isPageComplete()) return false;
        return true;
    }

    public boolean performFinish() {
        try {
            repository.setParameter(NISDomain.NIS_SERVER, connectionPage.getServer());
            repository.setParameter(NISDomain.NIS_DOMAIN, connectionPage.getDomain());

            repository.setParameter(NISDomain.YP_ENABLED, partitionsPage.isYpEnabled());
            repository.setParameter(NISDomain.YP_SUFFIX, partitionsPage.getYpSuffix());
            repository.setParameter(NISDomain.YP_TEMPLATE, partitionsPage.getYpTemplate());

            repository.setParameter(NISDomain.NIS_ENABLED, partitionsPage.isNisEnabled());
            repository.setParameter(NISDomain.NIS_SUFFIX, partitionsPage.getNisSuffix());
            repository.setParameter(NISDomain.NIS_TEMPLATE, partitionsPage.getNisTemplate());

            repository.setParameter(NISDomain.NSS_ENABLED, partitionsPage.isNssEnabled());
            repository.setParameter(NISDomain.NSS_SUFFIX, partitionsPage.getNssSuffix());
            repository.setParameter(NISDomain.NSS_TEMPLATE, partitionsPage.getNssTemplate());

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
}