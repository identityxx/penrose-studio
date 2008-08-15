package org.safehaus.penrose.studio.federation.nis.editor;

import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.safehaus.penrose.studio.federation.nis.conflict.NISUserChangesPage;
import org.safehaus.penrose.studio.federation.nis.conflict.NISGroupChangesPage;
import org.safehaus.penrose.studio.federation.nis.editor.NISDomainsPage;
import org.safehaus.penrose.studio.federation.nis.editor.NISPartitionsPage;
import org.safehaus.penrose.federation.NISFederationClient;
import org.safehaus.penrose.studio.federation.nis.editor.NISEditorInput;
import org.safehaus.penrose.studio.project.Project;

public class NISEditor extends FormEditor {

    public Logger log = LoggerFactory.getLogger(getClass());

    public Project project;
    public NISFederationClient nisFederation;

    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        NISEditorInput ei = (NISEditorInput)input;
        project = ei.getProject();
        nisFederation = ei.getNisTool();

        setSite(site);
        setInput(input);
        setPartName(ei.getName());
    }

    public void addPages() {
        try {
            addPage(new NISDomainsPage(this, nisFederation));
            addPage(new NISYPConfPage(this, nisFederation));
            //addPage(new NISDatabasesPage(this, nisFederation));
            addPage(new NISPartitionsPage(this, nisFederation));
            //addPage(new NISLDAPPage(this, nisFederation));
            addPage(new NISUserChangesPage(this, nisFederation));
            addPage(new NISGroupChangesPage(this, nisFederation));

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

    public NISFederationClient getNisTool() {
        return nisFederation;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
