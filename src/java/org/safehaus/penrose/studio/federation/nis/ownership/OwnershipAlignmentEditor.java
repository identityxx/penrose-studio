package org.safehaus.penrose.studio.federation.nis.ownership;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.safehaus.penrose.federation.NISRepositoryClient;
import org.safehaus.penrose.federation.FederationRepositoryConfig;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.module.ModuleManagerClient;
import org.safehaus.penrose.module.ModuleClient;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.client.PenroseClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OwnershipAlignmentEditor extends FormEditor {

    public Logger log = LoggerFactory.getLogger(getClass());

    public Project project;
    public NISRepositoryClient nisFederationClient;
    public FederationRepositoryConfig repositoryConfig;

    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        OwnershipAlignmentInput ei = (OwnershipAlignmentInput)input;
        project = ei.getProject();
        nisFederationClient = ei.getNisFederationClient();
        repositoryConfig = ei.getDomain();

        setSite(site);
        setInput(input);
        setPartName(ei.getName());
    }

    public void addPages() {
        try {
            String federationName = nisFederationClient.getFederationClient().getFederationDomain();
            String localPartitionName = repositoryConfig.getName();

            PenroseClient client = project.getClient();
            PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
            PartitionClient localPartitionClient = partitionManagerClient.getPartitionClient(federationName+"_"+localPartitionName);
            ModuleManagerClient moduleManagerClient = localPartitionClient.getModuleManagerClient();

            ModuleClient usersModuleClient = moduleManagerClient.getModuleClient("Users");

            UsersPage usersPage = new UsersPage(this);

            usersPage.setObjectClass(usersModuleClient.getParameter("objectClass"));
            usersPage.setRdnAttribute(usersModuleClient.getParameter("rdnAttribute"));
            usersPage.setSourceAttribute(usersModuleClient.getParameter("sourceAttribute"));
            usersPage.setTargetAttribute(usersModuleClient.getParameter("targetAttribute"));
            usersPage.setLinkingAttribute(usersModuleClient.getParameter("linkingAttribute"));
            usersPage.setLinkingKey(usersModuleClient.getParameter("linkingKey"));

            addPage(usersPage);

            ModuleClient groupsModuleClient = moduleManagerClient.getModuleClient("Groups");

            GroupsPage groupsPage = new GroupsPage(this);

            groupsPage.setObjectClass(groupsModuleClient.getParameter("objectClass"));
            groupsPage.setRdnAttribute(groupsModuleClient.getParameter("rdnAttribute"));
            groupsPage.setSourceAttribute(groupsModuleClient.getParameter("sourceAttribute"));
            groupsPage.setTargetAttribute(usersModuleClient.getParameter("targetAttribute"));
            groupsPage.setLinkingAttribute(groupsModuleClient.getParameter("linkingAttribute"));
            groupsPage.setLinkingKey(usersModuleClient.getParameter("linkingKey"));

            addPage(groupsPage);

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

    public FederationRepositoryConfig getRepositoryConfig() {
        return repositoryConfig;
    }

    public void setRepositoryConfig(FederationRepositoryConfig repositoryConfig) {
        this.repositoryConfig = repositoryConfig;
    }

    public NISRepositoryClient getNisFederationClient() {
        return nisFederationClient;
    }
}
