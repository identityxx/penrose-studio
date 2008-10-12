package org.safehaus.penrose.studio.federation.nis.domain;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.safehaus.penrose.federation.NISFederationClient;
import org.safehaus.penrose.federation.FederationRepositoryConfig;
import org.safehaus.penrose.studio.federation.linking.editor.LinkingSettingsPage;
import org.safehaus.penrose.studio.project.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NISDomainEditor extends FormEditor {

    public Logger log = LoggerFactory.getLogger(getClass());

    public Project project;
    public NISFederationClient nisFederation;
    public FederationRepositoryConfig domain;

    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        NISDomainEditorInput ei = (NISDomainEditorInput)input;
        project = ei.getProject();
        nisFederation = ei.getNisFederation();
        domain = ei.getDomain();

        setSite(site);
        setInput(input);
        setPartName(ei.getName());
    }

    public void addPages() {
        try {
            addPage(new NISDomainSettingsPage(this));

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public void doSave(IProgressMonitor iProgressMonitor) {
    }

    public void doSaveAs() {
    }

    public boolean isDirty() {
        return false;
    }

    public boolean isSaveAsAllowed() {
        return false;
    }

    public FederationRepositoryConfig getDomain() {
        return domain;
    }

    public void setDomain(FederationRepositoryConfig domain) {
        this.domain = domain;
    }

    public NISFederationClient getNisFederation() {
        return nisFederation;
    }
}
