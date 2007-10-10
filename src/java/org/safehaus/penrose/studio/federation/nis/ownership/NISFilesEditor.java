package org.safehaus.penrose.studio.federation.nis.ownership;

import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.safehaus.penrose.studio.federation.nis.NISFederation;
import org.safehaus.penrose.studio.federation.nis.NISRepository;
import org.safehaus.penrose.studio.federation.nis.editor.NISHostsPage;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class NISFilesEditor extends FormEditor {

    public Logger log = LoggerFactory.getLogger(getClass());

    NISFederation nisFederation;
    NISRepository domain;

    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        NISFilesEditorInput ei = (NISFilesEditorInput)input;
        nisFederation = ei.getNisTool();
        domain = ei.getDomain();

        setSite(site);
        setInput(input);
        setPartName("NIS Alignment Tool - "+domain.getName());
    }

    public void addPages() {
        try {
            addPage(new NISHostsPage(this));
            addPage(new NISFilesPage(this));
            addPage(new NISScriptsPage(this));

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

    public NISRepository getDomain() {
        return domain;
    }

    public void setDomain(NISRepository domain) {
        this.domain = domain;
    }

    public NISFederation getNisTool() {
        return nisFederation;
    }
}
