package org.safehaus.penrose.studio.nis;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.project.ProjectNode;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.nis.editor.NISDomainDialog;
import org.safehaus.penrose.studio.nis.editor.NISUserDialog;
import org.safehaus.penrose.nis.NISDomain;
import org.safehaus.penrose.config.PenroseConfig;
import org.safehaus.penrose.naming.PenroseContext;
import org.safehaus.penrose.management.PenroseClient;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchActionConstants;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Map;
import java.io.File;

/**
 * @author Endi S. Dewata
 */
public class NISNode extends Node {

    private ServersView view;
    private ProjectNode projectNode;

    protected NISTool nisTool;

    public NISNode(String name, String type, Image image, Object object, Object parent) {
        super(name, type, image, object, parent);
        projectNode = (ProjectNode)parent;
        view = projectNode.getView();
    }

    public void showMenu(IMenuManager manager) throws Exception {

        manager.add(new Action("New Domain...") {
            public void run() {
                try {
                    newDomain();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(new Action("Refresh") {
            public void run() {
                try {
                    refresh();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });
    }

    public void open() throws Exception {
        if (nisTool == null) start();
    }

    public void start() throws Exception {
        Project project = projectNode.getProject();
        PenroseConfig penroseConfig = project.getPenroseConfig();
        PenroseContext penroseContext = project.getPenroseContext();
        File workDir = project.getWorkDir();

        nisTool = new NISTool();
        nisTool.init(penroseConfig, penroseContext, workDir);
    }

    public boolean hasChildren() throws Exception {

        if (nisTool == null) return true;

        Map<String,NISDomain> domains = nisTool.getNisDomains();
        return domains.size() > 0;
    }

    public Collection<Node> getChildren() throws Exception {

        if (nisTool == null) start();

        Collection<Node> children = new ArrayList<Node>();

        for (NISDomain nisDomain : nisTool.getNisDomains().values()) {

            NISDomainNode node = new NISDomainNode(
                    nisDomain.getName(),
                    ServersView.ENTRY,
                    PenrosePlugin.getImage(PenroseImage.NODE),
                    nisDomain,
                    this
            );

            children.add(node);
        }

        return children;
    }

    public void newDomain() throws Exception {

        if (nisTool == null) start();

        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        NISDomainDialog dialog = new NISDomainDialog(shell, SWT.NONE);

        dialog.open();

        int action = dialog.getAction();
        if (action == NISUserDialog.CANCEL) return;

        String domainName = dialog.getName();
        String partitionName = dialog.getPartition();
        String server = dialog.getServer();
        String suffix = dialog.getSuffix();

        NISDomain domain = new NISDomain();
        domain.setName(domainName);
        domain.setPartition(partitionName);
        domain.setServer(server);
        domain.setSuffix(suffix);

        nisTool.createDomain(domain);
        nisTool.createPartitionConfig(domain);
        nisTool.createDatabase(domain);
        nisTool.createPartition(domain);

        Project project = projectNode.getProject();
        project.upload("partitions/"+partitionName);
        
        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }

    public void refresh() throws Exception {
        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }

    public NISTool getNisTool() {
        return nisTool;
    }

    public void setNisTool(NISTool nisTool) {
        this.nisTool = nisTool;
    }

    public ServersView getView() {
        return view;
    }

    public void setView(ServersView view) {
        this.view = view;
    }

    public ProjectNode getProjectNode() {
        return projectNode;
    }

    public void setProjectNode(ProjectNode projectNode) {
        this.projectNode = projectNode;
    }
}
