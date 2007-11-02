package org.safehaus.penrose.studio.federation.nis;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.PenroseStudioPlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.project.ProjectNode;
import org.safehaus.penrose.studio.federation.FederationNode;
import org.safehaus.penrose.studio.federation.event.FederationEventAdapter;
import org.safehaus.penrose.studio.federation.nis.editor.NISEditorInput;
import org.safehaus.penrose.studio.federation.nis.editor.NISEditor;
import org.safehaus.penrose.studio.federation.nis.ownership.NISOwnershipNode;
import org.safehaus.penrose.studio.federation.nis.linking.NISLinkingNode;
import org.safehaus.penrose.studio.federation.nis.conflict.NISConflictsNode;
import org.safehaus.penrose.studio.federation.nis.domain.NISDomainNode;
import org.safehaus.penrose.studio.federation.event.FederationEvent;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.Separator;
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

    private ProjectNode projectNode;
    private FederationNode federationNode;

    NISFederation nisFederation;

    Map<String,Node> children = new TreeMap<String,Node>();

    NISLinkingNode linkingNode;
    NISConflictsNode conflictsNode;
    NISOwnershipNode ownershipNode;

    public NISNode(String name, FederationNode federationNode) throws Exception {
        super(name, null, PenroseStudioPlugin.getImage(PenroseImage.FOLDER), null, federationNode);

        this.federationNode = federationNode;
        this.projectNode = federationNode.getProjectNode();

        nisFederation = federationNode.getFederation().getNisFederation();

        nisFederation.addListener(new FederationEventAdapter() {
            public void repositoryAdded(FederationEvent event) {
                NISDomain repository = (NISDomain)event.getRepository();
                addRepository(repository);

                PenroseStudio penroseStudio = PenroseStudio.getInstance();
                penroseStudio.notifyChangeListeners();
            }
            public void repositoryRemoved(FederationEvent event) {
                NISDomain repository = (NISDomain)event.getRepository();
                removeRepository(repository.getName());

                PenroseStudio penroseStudio = PenroseStudio.getInstance();
                penroseStudio.notifyChangeListeners();
            }
        });

        refresh();
    }

    public void refresh() {
        for (NISDomain nisDomain : nisFederation.getRepositories()) {
            addRepository(nisDomain);
        }
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
        });
    }

    public void open() throws Exception {

        NISEditorInput ei = new NISEditorInput();
        ei.setProject(projectNode.getProject());
        ei.setNisTool(nisFederation);

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

    public void addRepository(NISDomain domain) {

        String name = domain.getName();

        NISDomainNode node = new NISDomainNode(
                name,
                domain,
                this
        );

        children.put(name, node);
    }

    public void removeRepository(String name) {
        children.remove(name);
    }

    public boolean hasChildren() throws Exception {
        return !children.isEmpty();
    }

    public Collection<Node> getChildren() throws Exception {
        return children.values();
    }

    public NISFederation getNisFederation() {
        return nisFederation;
    }

    public void setNisTool(NISFederation nisFederation) {
        this.nisFederation = nisFederation;
    }

    public ProjectNode getProjectNode() {
        return projectNode;
    }

    public void setProjectNode(ProjectNode projectNode) {
        this.projectNode = projectNode;
    }

    public FederationNode getFederationNode() {
        return federationNode;
    }

    public void setFederationNode(FederationNode federationNode) {
        this.federationNode = federationNode;
    }
}
