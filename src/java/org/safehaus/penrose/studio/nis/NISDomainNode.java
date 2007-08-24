package org.safehaus.penrose.studio.nis;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.*;
import org.safehaus.penrose.studio.project.ProjectNode;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.nis.editor.NISDomainEditorInput;
import org.safehaus.penrose.studio.nis.editor.NISDomainEditor;
import org.safehaus.penrose.nis.NISDomain;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.partition.PartitionConfigs;
import org.safehaus.penrose.partition.PartitionConfig;
import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchPage;

import java.util.Collection;
import java.util.ArrayList;

/**
 * @author Endi S. Dewata
 */
public class NISDomainNode extends Node {

    ServersView view;
    ProjectNode projectNode;
    NISNode nisNode;

    private NISTool nisTool;
    private NISDomain domain;

    public NISDomainNode(String name, String type, Image image, Object object, Object parent) {
        super(name, type, image, object, parent);

        domain = (NISDomain)object;
        
        nisNode = (NISNode)parent;
        projectNode = nisNode.getProjectNode();
        view = projectNode.getServersView();

        nisTool = nisNode.getNisTool();
    }

    public Image getImage() {
        Project project = nisTool.getProject();
        PartitionConfigs partitionConfigs = project.getPartitionConfigs();
        PartitionConfig partitionConfig = partitionConfigs.getPartitionConfig(domain.getName());
        return PenrosePlugin.getImage(partitionConfig == null ? PenroseImage.RED_FOLDER : PenroseImage.FOLDER);

        //Partition partition = nisTool.getPartitions().getPartition(domain.getName());
        //return PenrosePlugin.getImage(partition == null ? PenroseImage.RED_FOLDER : PenroseImage.FOLDER);
    }

    public void showMenu(IMenuManager manager) throws Exception {

        manager.add(new Action("Open") {
            public void run() {
                try {
                    open();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });
    }

    public void open() throws Exception {

        NISDomainEditorInput ei = new NISDomainEditorInput();
        ei.setProject(projectNode.getProject());
        ei.setNisTool(nisTool);
        ei.setDomain(domain);

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        page.openEditor(ei, NISDomainEditor.class.getName());
    }

    public NISDomain getDomain() {
        return domain;
    }

    public void setDomain(NISDomain domain) {
        this.domain = domain;
    }

    public boolean hasChildren() throws Exception {
        return true;
    }

    public Collection<Node> getChildren() throws Exception {
        Collection<Node> children = new ArrayList<Node>();

        NISUsersNode usersNode = new NISUsersNode(
                view,
                "Users",
                "Users",
                PenrosePlugin.getImage(PenroseImage.FOLDER),
                null,
                this
        );

        usersNode.setNisTool(nisTool);
        usersNode.setDomain(domain);

        children.add(usersNode);

        NISGroupsNode groupsNode = new NISGroupsNode(
                view,
                "Groups",
                "Groups",
                PenrosePlugin.getImage(PenroseImage.FOLDER),
                null,
                this
        );

        groupsNode.setNisTool(nisTool);
        groupsNode.setDomain(domain);

        children.add(groupsNode);

        NISFilesNode filesNode = new NISFilesNode(
                view,
                "Files",
                "Files",
                PenrosePlugin.getImage(PenroseImage.FOLDER),
                null,
                this
        );

        filesNode.setNisTool(nisTool);
        filesNode.setDomain(domain);

        children.add(filesNode);

        return children;
    }

    public NISTool getNisTool() {
        return nisTool;
    }

    public void setNisTool(NISTool nisTool) {
        this.nisTool = nisTool;
    }
}
