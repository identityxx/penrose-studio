package org.safehaus.penrose.studio.project;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.object.ObjectsView;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.util.PenroseStudioClipboard;
import org.safehaus.penrose.studio.action.PenroseStudioActions;
import org.apache.log4j.Logger;
import org.eclipse.jface.action.IMenuManager;

import java.util.*;

/**
 * @author Endi S. Dewata
 */
public class ProjectsNode extends Node {

    Logger log = Logger.getLogger(getClass());

    ObjectsView view;

    public ProjectsNode(
            ObjectsView view,
            String name,
            String type,
            Object object,
            Node parent
    ) {
        super(name, type, PenrosePlugin.getImage(PenroseImage.FOLDER), object, parent);
        this.view = view;
    }

    public void showMenu(IMenuManager manager) {

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        PenroseStudioActions actions = penroseStudio.getActions();

        manager.add(actions.getPasteAction());
    }

    public void paste(PenroseStudioClipboard clipboard) throws Exception {

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        Object object = clipboard.get();
        log.debug("Pasting: "+object);
        
        if (object instanceof ProjectConfig) {
            ProjectConfig projectConfig = (ProjectConfig)object;
            projectConfig.setName(projectConfig.getName()+" (2)");
            penroseStudio.addProject(projectConfig);
        }
    }

    public boolean hasChildren() throws Exception {

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        return !penroseStudio.getProjects().isEmpty();
    }

    public Collection getChildren() throws Exception {

        Collection children = new ArrayList();

        PenroseStudio penroseStudio = PenroseStudio.getInstance();

        for (Iterator i=penroseStudio.getProjects().iterator(); i.hasNext(); ) {
            Project project = (Project)i.next();

            ProjectNode projectNode = new ProjectNode(
                    view,
                    project.getName(),
                    ObjectsView.PROJECT,
                    project,
                    this
            );

            children.add(projectNode);
        }

        return children;
    }
}
