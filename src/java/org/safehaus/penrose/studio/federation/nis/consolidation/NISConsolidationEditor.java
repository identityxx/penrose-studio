package org.safehaus.penrose.studio.federation.nis.consolidation;

import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.studio.federation.nis.NISDomain;
import org.safehaus.penrose.studio.federation.nis.NISFederation;

public class NISConsolidationEditor extends FormEditor {

    public Logger log = LoggerFactory.getLogger(getClass());

    private Partition partition;
    private NISDomain domain;
    private NISFederation nisFederation;

    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        NISConsolidationEditorInput ei = (NISConsolidationEditorInput)input;
        partition = ei.getPartition();
        domain = ei.getDomain();
        nisFederation = ei.getNisFederation();

        setSite(site);
        setInput(input);
        setPartName(ei.getName());
    }

    public void addPages() {
        try {
            addPage(new NISConsolidationPage(this));

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

    public Partition getPartition() {
        return partition;
    }

    public void setPartition(Partition partition) {
        this.partition = partition;
    }

    public NISDomain getDomain() {
        return domain;
    }

    public void setDomain(NISDomain domain) {
        this.domain = domain;
    }

    public NISFederation getNisFederation() {
        return nisFederation;
    }

    public void setNisFederation(NISFederation nisFederation) {
        this.nisFederation = nisFederation;
    }
}
