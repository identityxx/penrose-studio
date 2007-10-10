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

import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.project.ProjectNode;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.schema.action.ImportSchemaAction;
import org.safehaus.penrose.studio.schema.action.NewSchemaAction;
import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.config.PenroseConfig;
import org.safehaus.penrose.schema.SchemaConfig;
import org.eclipse.jface.action.IMenuManager;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.ArrayList;

/**
 * @author Endi S. Dewata
 */
public class SchemasNode extends Node {

    Logger log = Logger.getLogger(getClass());

    private ServersView view;
    private ProjectNode projectNode;

    public SchemasNode(String name, String type, Object object, Object parent) {
        super(name, type, PenrosePlugin.getImage(PenroseImage.FOLDER), object, parent);
        projectNode = (ProjectNode)parent;
        view = projectNode.getServersView();
    }

    public void showMenu(IMenuManager manager) {
        manager.add(new NewSchemaAction());
        manager.add(new ImportSchemaAction());
    }

    public boolean hasChildren() throws Exception {
        Project project = projectNode.getProject();
        PenroseConfig penroseConfig = project.getPenroseConfig();
        return !penroseConfig.getSchemaConfigs().isEmpty();
    }

    public Collection<Node> getChildren() throws Exception {

        Collection<Node> children = new ArrayList<Node>();

        Project project = projectNode.getProject();
        PenroseConfig penroseConfig = project.getPenroseConfig();

        for (SchemaConfig schemaConfig : penroseConfig.getSchemaConfigs()) {

            SchemaNode schemaNode = new SchemaNode(
                    schemaConfig.getName(),
                    ServersView.SCHEMA,
                    PenrosePlugin.getImage(PenroseImage.SCHEMA),
                    schemaConfig,
                    this
            );

            schemaNode.setSchemaConfig(schemaConfig);

            children.add(schemaNode);
        }

        return children;
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
