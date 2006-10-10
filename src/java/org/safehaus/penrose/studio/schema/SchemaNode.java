/**
 * Copyright (c) 2000-2005, Identyx Corporation.
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
import org.safehaus.penrose.studio.object.ObjectsView;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseApplication;
import org.safehaus.penrose.studio.PenroseImage;
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

    ObjectsView view;

    private SchemaConfig schemaConfig;

    public SchemaNode(ObjectsView view, String name, String type, Image image, Object object, Object parent) {
        super(name, type, image, object, parent);
        this.view = view;
    }

    public void showMenu(IMenuManager manager) {

        manager.add(new Action("Open") {
            public void run() {
                try {
                    open();
                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                }
            }
        });
/*
        manager.add(new Action("Paste") {
            public void run() {
                try {
                    paste();
                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                }
            }
        });
*/
        manager.add(new Action("Delete", PenrosePlugin.getImageDescriptor(PenroseImage.DELETE)) {
            public void run() {
                try {
                    remove();
                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                }
            }
        });
    }

    public void open() throws Exception {

        PenroseApplication penroseApplication = PenroseApplication.getInstance();
        SchemaManager schemaManager = penroseApplication.getSchemaManager();
        Schema schema = schemaManager.getSchema(schemaConfig.getName());
        SchemaEditorInput ei = new SchemaEditorInput(schemaConfig, schema);

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        page.openEditor(ei, SchemaEditor.class.getName());
    }

    public void remove() throws Exception {

        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

        boolean confirm = MessageDialog.openQuestion(
                shell,
                "Confirmation",
                "Remove Schema \""+schemaConfig.getName()+"\"?");

        if (!confirm) return;

        PenroseApplication penroseApplication = PenroseApplication.getInstance();
        PenroseConfig penroseConfig = penroseApplication.getPenroseConfig();
        penroseConfig.removeSchemaConfig(schemaConfig.getName());

        SchemaManager schemaManager = penroseApplication.getSchemaManager();
        schemaManager.removeSchema(schemaConfig.getName());

        penroseApplication.notifyChangeListeners();
    }

    public boolean hasChildren() throws Exception {
        return false;
    }
/*
    public Collection getChildren() throws Exception {

        Collection children = new ArrayList();

        AttributeTypesNode attributeTypesNode = new AttributeTypesNode(
                view,
                ObjectsView.ATTRIBUTE_TYPES,
                ObjectsView.ATTRIBUTE_TYPES,
                PenrosePlugin.getImage(PenroseImage.FOLDER),
                ObjectsView.ATTRIBUTE_TYPES,
                this
        );

        attributeTypesNode.setSchemaConfig(schemaConfig);

        children.add(attributeTypesNode);

        ObjectClassesNode objectClassesNode = new ObjectClassesNode(
                view,
                ObjectsView.OBJECT_CLASSES,
                ObjectsView.OBJECT_CLASSES,
                PenrosePlugin.getImage(PenroseImage.FOLDER),
                ObjectsView.OBJECT_CLASSES,
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
}
