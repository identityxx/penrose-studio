package org.safehaus.penrose.studio.federation.nis.synchronization;

import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.safehaus.penrose.federation.NISFederationClient;
import org.safehaus.penrose.federation.NISDomain;
import org.safehaus.penrose.federation.FederationRepositoryConfig;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.source.SourceManagerClient;

import java.util.Collection;

public class NISSynchronizationEditor extends FormEditor {

    public Logger log = LoggerFactory.getLogger(getClass());

    Project project;
    NISFederationClient nisFederationClient;
    FederationRepositoryConfig domain;

    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        NISSynchronizationEditorInput ei = (NISSynchronizationEditorInput)input;
        project = ei.getProject();
        nisFederationClient = ei.getNisTool();
        domain = ei.getDomain();

        setSite(site);
        setInput(input);
        setPartName(ei.getName());
    }

    public void addPages() {
        try {

            String federationName = nisFederationClient.getFederationClient().getName();
            PenroseClient penroseClient = project.getClient();

            PartitionManagerClient partitionManagerClient = penroseClient.getPartitionManagerClient();
            PartitionClient partitionClient = partitionManagerClient.getPartitionClient(federationName+"_"+domain.getName());

            SourceManagerClient sourceManagerClient = partitionClient.getSourceManagerClient();
            Collection<String> sourceNames = sourceManagerClient.getSourceNames();

            addPage(new NISSynchronizationPage(this));
            if (sourceNames.contains("changes")) {
                addPage(new NISSynchronizationChangeLogPage(this));
            }
            addPage(new NISSynchronizationErrorsPage(this));

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

    public NISFederationClient getNISFederationClient() {
        return nisFederationClient;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
