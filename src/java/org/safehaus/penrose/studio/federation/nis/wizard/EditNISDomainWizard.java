package org.safehaus.penrose.studio.federation.nis.wizard;

import org.eclipse.jface.wizard.Wizard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.safehaus.penrose.federation.repository.NISDomain;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.studio.federation.linking.wizard.LinkingParametersWizardPage;
import org.safehaus.penrose.studio.federation.Federation;

/**
 * @author Endi Sukma Dewata
 */
public class EditNISDomainWizard extends Wizard {

    public Logger log = LoggerFactory.getLogger(getClass());

    NISDomainWizardPage     connectionPage;
    NISPartitionsWizardPage partitionsPage;
    LinkingParametersWizardPage linkingPage;

    NISDomain repository;

    public EditNISDomainWizard(NISDomain domain) {
        this.repository = domain;

        setWindowTitle("Edit NIS Domain");
    }

    public void addPages() {

        connectionPage = new NISDomainWizardPage();

        connectionPage.setDomain(repository.getFullName());
        connectionPage.setServer(repository.getServer());

        addPage(connectionPage);

        partitionsPage = new NISPartitionsWizardPage();

        partitionsPage.setYpEnabled(repository.isYpEnabled());
        partitionsPage.setYpSuffix(repository.getYpSuffix());

        partitionsPage.setNisEnabled(repository.isNisEnabled());
        partitionsPage.setNisSuffix(repository.getNisSuffix());

        partitionsPage.setNssEnabled(repository.isNssEnabled());
        partitionsPage.setNssSuffix(repository.getNssSuffix());

        addPage(partitionsPage);

        linkingPage = new LinkingParametersWizardPage();

        linkingPage.setLocalAttribute(repository.getParameter(Federation.LINKING_LOCAL_ATTRIBUTE));
        linkingPage.setGlobalAttribute(repository.getParameter(Federation.LINKING_GLOBAL_ATTRIBUTE));
        linkingPage.setImportMappingName(repository.getParameter(Federation.IMPORT_MAPPING_NAME));
        linkingPage.setImportMappingPrefix(repository.getParameter(Federation.IMPORT_MAPPING_PREFIX));

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
            repository.setFullName(connectionPage.getDomain());
            repository.setServer(connectionPage.getServer());

            repository.setYpEnabled(partitionsPage.isYpEnabled());
            repository.setYpSuffix(partitionsPage.getYpSuffix());

            repository.setNisEnabled(partitionsPage.isNisEnabled());
            repository.setNisSuffix(partitionsPage.getNisSuffix());

            repository.setNssEnabled(partitionsPage.isNssEnabled());
            repository.setNssSuffix(partitionsPage.getNssSuffix());

            repository.setParameter(Federation.LINKING_LOCAL_ATTRIBUTE, linkingPage.getLocalAttribute());
            repository.setParameter(Federation.LINKING_GLOBAL_ATTRIBUTE, linkingPage.getGlobalAttribute());
            repository.setParameter(Federation.IMPORT_MAPPING_NAME, linkingPage.getImportMappingName());
            repository.setParameter(Federation.IMPORT_MAPPING_PREFIX, linkingPage.getImportMappingPrefix());

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