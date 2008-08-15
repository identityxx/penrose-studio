/**
 * Copyright (c) 2000-2006, Identyx Corporation.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.safehaus.penrose.studio.schema;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.safehaus.penrose.management.PenroseClient;
import org.safehaus.penrose.schema.SchemaManagerClient;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.PenroseStudioPlugin;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.project.ProjectNode;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.tree.Node;

/**
 * @author Endi S. Dewata
 */
public class SchemaNode extends Node {

    Logger log = Logger.getLogger(getClass());

    ServersView view;
    protected ProjectNode projectNode;
    protected SchemasNode schemasNode;

    private String schemaName;

    public SchemaNode(String name, Image image, Object object, SchemasNode parent) {
        super(name, image, object, parent);
        schemasNode = (SchemasNode)parent;
        projectNode = schemasNode.getProjectNode();
        schemaName = (String)object;
        view = projectNode.getServersView();
    }

    public void showMenu(IMenuManager manager) {

        manager.add(new Action("Open") {
            public void run() {
                try {
                    open();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });
/*
        manager.add(new Action("Paste") {
            public void run() {
                try {
                    paste();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });
*/
        manager.add(new Action("Delete", PenroseStudioPlugin.getImageDescriptor(PenroseImage.SIZE_16x16, PenroseImage.DELETE)) {
            public void run() {
                try {
                    remove();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });
    }

    public void open() throws Exception {

        Project project = projectNode.getProject();

        SchemaEditorInput ei = new SchemaEditorInput();
        ei.setProject(project);
        ei.setSchemaName(schemaName);

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        page.openEditor(ei, SchemaEditor.class.getName());
    }

    public void remove() throws Exception {

        boolean confirm = MessageDialog.openQuestion(
                view.getSite().getShell(),
                "Confirmation",
                "Remove Schema \""+ schemaName+"\"?");

        if (!confirm) return;

        Project project = projectNode.getProject();
        PenroseClient client = project.getClient();
        SchemaManagerClient schemaManagerClient = client.getSchemaManagerClient();
        schemaManagerClient.removeSchema(schemaName);

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }

    public boolean hasChildren() throws Exception {
        return false;
    }
/*
    public Collection getChildren() throws Exception {

        Collection children = new ArrayList();

        AttributeTypesNode attributeTypesNode = new AttributeTypesNode(
                view,
                ServersView.ATTRIBUTE_TYPES,
                ServersView.ATTRIBUTE_TYPES,
                PenroseStudioPlugin.getImage(PenroseImage.FOLDER),
                ServersView.ATTRIBUTE_TYPES,
                this
        );

        attributeTypesNode.setSchemaConfig(schemaConfig);

        children.add(attributeTypesNode);

        ObjectClassesNode objectClassesNode = new ObjectClassesNode(
                view,
                ServersView.OBJECT_CLASSES,
                ServersView.OBJECT_CLASSES,
                PenroseStudioPlugin.getImage(PenroseImage.FOLDER),
                ServersView.OBJECT_CLASSES,
                this
        );

        objectClassesNode.setSchemaConfig(schemaConfig);

        children.add(objectClassesNode);

        return children;
    }
*/
    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public ProjectNode getProjectNode() {
        return projectNode;
    }

    public void setProjectNode(ProjectNode projectNode) {
        this.projectNode = projectNode;
    }

    public SchemasNode getSchemasNode() {
        return schemasNode;
    }

    public void setSchemasNode(SchemasNode schemasNode) {
        this.schemasNode = schemasNode;
    }
}
