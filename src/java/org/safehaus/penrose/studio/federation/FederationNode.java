package org.safehaus.penrose.studio.federation;

import org.safehaus.penrose.studio.plugin.PluginNode;
import org.safehaus.penrose.studio.plugin.PluginsNode;
import org.safehaus.penrose.studio.federation.nis.NISNode;
import org.safehaus.penrose.studio.federation.ldap.LDAPNode;
import org.safehaus.penrose.studio.federation.wizard.FederationWizard;
import org.safehaus.penrose.studio.federation.editor.FederationEditorInput;
import org.safehaus.penrose.studio.federation.editor.FederationEditor;
import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.project.ProjectNode;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.partition.PartitionConfigs;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchPage;

import java.util.Collection;
import java.util.ArrayList;

/**
 * @author Endi Sukma Dewata
 */
public class FederationNode extends PluginNode {

    protected Collection<Node> children = new ArrayList<Node>();
    protected boolean started;

    private Project project;
    private Federation federation;

    public FederationNode(PluginsNode parentsNode) throws Exception {
          super("Federation", parentsNode);

        ProjectNode projectNode = parentsNode.getProjectNode();
        project = projectNode.getProject();
    }

    public void open() throws Exception {
        if (!started) start();

        FederationEditorInput ei = new FederationEditorInput();
        ei.setProject(project);
        ei.setFederation(federation);

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        page.openEditor(ei, FederationEditor.class.getName());

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }

    public void start() throws Exception {

        PartitionConfigs partitionConfigs = project.getPartitionConfigs();

        if (partitionConfigs.getPartitionConfig(Federation.PARTITION) == null) {
            boolean b = createPartition();
            if (!b) return;
        }

        federation = new Federation(project);
/*
        children.add(new GlobalNode(
                "Global",
                this
        ));
*/
        children.add(new LDAPNode(
                "LDAP",
                this
        ));

        children.add(new NISNode(
                "NIS",
                this
        ));

        started = true;
    }

    public boolean createPartition() throws Exception {

        log.debug("Creating Federation partition");

        FederationWizard wizard = new FederationWizard();
        wizard.init(project);

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

        WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
        dialog.setPageSize(600, 300);

        return dialog.open() == Window.OK;
    }

    public boolean hasChildren() throws Exception {
        return true;
    }

    public Collection<Node> getChildren() throws Exception {
        if (!started) start();
        return children;
    }

    public Federation getFederation() {
        return federation;
    }

    public void setFederation(Federation federation) {
        this.federation = federation;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
