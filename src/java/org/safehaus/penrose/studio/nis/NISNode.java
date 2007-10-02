package org.safehaus.penrose.studio.nis;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.project.ProjectNode;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.nis.wizard.NISToolWizard;
import org.safehaus.penrose.studio.nis.editor.NISEditorInput;
import org.safehaus.penrose.studio.nis.editor.NISEditor;
import org.safehaus.penrose.studio.nis.domain.NISDomainNode;
import org.safehaus.penrose.studio.nis.ownership.NISOwnershipNode;
import org.safehaus.penrose.studio.nis.linking.NISLinkingNode;
import org.safehaus.penrose.studio.nis.conflict.NISConflictsNode;
import org.safehaus.penrose.studio.nis.event.NISEventAdapter;
import org.safehaus.penrose.studio.nis.event.NISEvent;
import org.safehaus.penrose.partition.PartitionConfigs;
import org.safehaus.penrose.partition.PartitionConfig;
import org.safehaus.penrose.connection.ConnectionConfigs;
import org.safehaus.penrose.connection.ConnectionConfig;
import org.safehaus.penrose.nis.NISDomain;
import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchPage;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.io.File;

/**
 * @author Endi S. Dewata
 */
public class NISNode extends Node {

    private ServersView view;
    private ProjectNode projectNode;

    protected NISTool nisTool;
    protected boolean started;

    Map<String,Node> children = new TreeMap<String,Node>();

    NISLinkingNode linkingNode;
    NISConflictsNode conflictsNode;
    NISOwnershipNode ownershipNode;

    public NISNode(String name, String type, Image image, Object object, Object parent) {
        super(name, type, image, object, parent);
        projectNode = (ProjectNode)parent;
        view = projectNode.getServersView();
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

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(new Action("Properties") {
            public void run() {
                try {
                    edit();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
            public boolean isEnabled() {
                return started;
            }
        });
    }

    public void open() throws Exception {

        Project project = projectNode.getProject();
        PartitionConfigs partitionConfigs = project.getPartitionConfigs();

        File workDir = project.getWorkDir();
        File sampleDir = new File(workDir, "samples/"+NISTool.NIS_TOOL);

        if (!sampleDir.exists()) {
            project.download("samples/"+NISTool.NIS_TOOL);
        }

        if (partitionConfigs.getPartitionConfig(NISTool.NIS_TOOL) == null) {
            boolean b = createNisPartition();
            if (!b) return;
        }

        if (!started) start();

        NISEditorInput ei = new NISEditorInput();
        ei.setProject(projectNode.getProject());
        ei.setNisTool(nisTool);

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        page.openEditor(ei, NISEditor.class.getName());

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }

    public void edit() throws Exception {
        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }

    public boolean createNisPartition() throws Exception {

        log.debug("Creating NIS Partition");

        Project project = projectNode.getProject();
        PartitionConfigs partitionConfigs = project.getPartitionConfigs();

        File workDir = project.getWorkDir();

        File sampleDir = new File(workDir, "samples/"+NISTool.NIS_TOOL);
        PartitionConfig partitionConfig = partitionConfigs.load(sampleDir);

        ConnectionConfigs connectionConfigs = partitionConfig.getConnectionConfigs();
        ConnectionConfig nisToolConnectionConfig = connectionConfigs.getConnectionConfig(NISTool.NIS_CONNECTION_NAME);
        ConnectionConfig ldapConnectionConfig = connectionConfigs.getConnectionConfig(NISTool.LDAP_CONNECTION_NAME);

        NISToolWizard wizard = new NISToolWizard();
        wizard.setProject(project);
        wizard.setPartitionConfig(partitionConfig);
        wizard.setDbConnectionConfig(nisToolConnectionConfig);
        wizard.setLdapConnectionConfig(ldapConnectionConfig);

        WizardDialog dialog = new WizardDialog(view.getSite().getShell(), wizard);
        dialog.setPageSize(600, 300);

        return dialog.open() == Window.OK;
    }

    public void start() throws Exception {

        log.debug("Starting NIS Tool");

        Project project = projectNode.getProject();

        nisTool = new NISTool();
        nisTool.init(project);

        nisTool.addNISListener(new NISEventAdapter() {
            public void domainAdded(NISEvent event) {
                NISDomain domain = event.getDomain();
                addNisDomain(domain);

                PenroseStudio penroseStudio = PenroseStudio.getInstance();
                penroseStudio.notifyChangeListeners();
            }
            public void domainRemoved(NISEvent event) {
                NISDomain domain = event.getDomain();
                removeNisDomain(domain.getName());

                PenroseStudio penroseStudio = PenroseStudio.getInstance();
                penroseStudio.notifyChangeListeners();
            }
        });

        for (NISDomain nisDomain : nisTool.getNisDomains().values()) {
            addNisDomain(nisDomain);
        }

        started = true;
    }

    public void addNisDomain(NISDomain domain) {

        String domainName = domain.getName();

        NISDomainNode node = new NISDomainNode(
                domainName,
                ServersView.ENTRY,
                domain,
                this
        );

        children.put(domainName, node);
    }

    public void removeNisDomain(String domainName) {
        children.remove(domainName);
    }

    public boolean hasChildren() throws Exception {
        return true;
    }

    public Collection<Node> getChildren() throws Exception {
        if (!started) start();
        return children.values();
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
