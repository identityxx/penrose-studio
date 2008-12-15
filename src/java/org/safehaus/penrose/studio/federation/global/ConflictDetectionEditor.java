package org.safehaus.penrose.studio.federation.global;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.safehaus.penrose.federation.FederationClient;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.federation.global.UsersPage;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.module.ModuleManagerClient;
import org.safehaus.penrose.module.ModuleClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConflictDetectionEditor extends FormEditor {

    public Logger log = LoggerFactory.getLogger(getClass());

    public Project project;
    public FederationClient federationClient;

    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        super.init(site, input);

        ConflictDetectionInput ei = (ConflictDetectionInput)input;
        project = ei.getProject();
        federationClient = ei.getFederationClient();

        setPartName(ei.getName());
    }

    public void addPages() {
        try {
            PartitionClient partitionClient = federationClient.getPartitionClient();
            ModuleManagerClient moduleManagerClient = partitionClient.getModuleManagerClient();

            ModuleClient usersModuleClient = moduleManagerClient.getModuleClient("Users");

            UsersPage usersPage = new UsersPage(this);
            usersPage.setObjectClass(usersModuleClient.getParameter("objectClass"));
            usersPage.setAttributeName(usersModuleClient.getParameter("attribute"));
            addPage(usersPage);

            ModuleClient groupsModuleClient = moduleManagerClient.getModuleClient("Groups");

            GroupsPage groupsPage = new GroupsPage(this);
            groupsPage.setObjectClass(groupsModuleClient.getParameter("objectClass"));
            groupsPage.setAttributeName(groupsModuleClient.getParameter("attribute"));
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

    public FederationClient getFederationClient() {
        return federationClient;
    }
}