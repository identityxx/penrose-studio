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

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.project.ProjectNode;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.schema.SchemaConfig;
import org.safehaus.penrose.schema.SchemaManager;
import org.safehaus.penrose.schema.Schema;
import org.safehaus.penrose.config.PenroseConfig;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchPage;
import org.apache.log4j.Logger;

/**
 * @author Endi S. Dewata
 */
public class SchemaNode extends Node {

    Logger log = Logger.getLogger(getClass());

    ServersView view;
    protected ProjectNode projectNode;
    protected SchemasNode schemasNode;

    private SchemaConfig schemaConfig;

    public SchemaNode(String name, String type, Image image, Object object, Object parent) {
        super(name, type, image, object, parent);
        schemasNode = (SchemasNode)parent;
        projectNode = schemasNode.getProjectNode();
        view = projectNode.getView();
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
        manager.add(new Action("Delete", PenrosePlugin.getImageDescriptor(PenroseImage.DELETE)) {
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
        SchemaManager schemaManager = project.getSchemaManager();
        Schema schema = schemaManager.getSchema(schemaConfig.getName());

        SchemaEditorInput ei = new SchemaEditorInput(schemaConfig, schema);
        ei.setProject(project);

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        page.openEditor(ei, SchemaEditor.class.getName());
    }

    public void remove() throws Exception {

        boolean confirm = MessageDialog.openQuestion(
                view.getSite().getShell(),
                "Confirmation",
                "Remove Schema \""+schemaConfig.getName()+"\"?");

        if (!confirm) return;

        Project project = projectNode.getProject();
        PenroseConfig penroseConfig = project.getPenroseConfig();
        penroseConfig.removeSchemaConfig(schemaConfig.getName());

        SchemaManager schemaManager = project.getSchemaManager();
        schemaManager.removeSchema(schemaConfig.getName());

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
                PenrosePlugin.getImage(PenroseImage.FOLDER),
                ServersView.ATTRIBUTE_TYPES,
                this
        );

        attributeTypesNode.setSchemaConfig(schemaConfig);

        children.add(attributeTypesNode);

        ObjectClassesNode objectClassesNode = new ObjectClassesNode(
                view,
                ServersView.OBJECT_CLASSES,
                ServersView.OBJECT_CLASSES,
                PenrosePlugin.getImage(PenroseImage.FOLDER),
                ServersView.OBJECT_CLASSES,
                this
        );

        objectClassesNode.setSchemaConfig(schemaConfig);

        children.add(objectClassesNode);

        return children;
    }
*/
    public SchemaConfig getSchemaConfig() {
        return schemaConfig;
    }

    public void setSchemaConfig(SchemaConfig schemaConfig) {
        this.schemaConfig = schemaConfig;
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
