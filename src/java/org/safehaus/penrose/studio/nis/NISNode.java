package org.safehaus.penrose.studio.nis;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.project.ProjectNode;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.nis.wizard.NISDomainWizard;
import org.safehaus.penrose.studio.nis.editor.NISEditorInput;
import org.safehaus.penrose.studio.nis.editor.NISEditor;
import org.safehaus.penrose.nis.NISDomain;
import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchPage;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Endi S. Dewata
 */
public class NISNode extends Node {

    private ServersView view;
    private ProjectNode projectNode;

    protected NISTool nisTool;
    protected boolean started;

    Map<String,Node> children = new TreeMap<String,Node>();

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

        manager.add(new Action("Add Domain...") {
            public void run() {
                try {
                    addDomain();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
            public boolean isEnabled() {
                return started;
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
            public boolean isEnabled() {
                return started;
            }
        });
    }

    public void open() throws Exception {
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

    public void start() throws Exception {
        Project project = projectNode.getProject();

        nisTool = new NISTool();
        nisTool.init(project);

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
                PenrosePlugin.getImage(PenroseImage.FOLDER),
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

    public void addDomain() throws Exception {

        NISDomainWizard wizard = new NISDomainWizard(projectNode.getProject(), this);
        WizardDialog dialog = new WizardDialog(view.getSite().getShell(), wizard);
        dialog.setPageSize(600, 300);
        dialog.open();
        
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
