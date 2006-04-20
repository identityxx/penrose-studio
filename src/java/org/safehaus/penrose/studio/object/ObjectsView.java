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
package org.safehaus.penrose.studio.object;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.part.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.action.*;
import org.eclipse.swt.SWT;
import org.safehaus.penrose.studio.util.ChangeListener;
import org.safehaus.penrose.studio.PenroseApplication;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.logger.LoggersNode;
import org.safehaus.penrose.studio.properties.SystemPropertiesNode;
import org.safehaus.penrose.studio.rootDse.RootDSENode;
import org.safehaus.penrose.studio.user.AdministratorNode;
import org.safehaus.penrose.studio.service.ServicesNode;
import org.safehaus.penrose.studio.util.Helper;
import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.connector.ConnectorNode;
import org.safehaus.penrose.studio.engine.EngineNode;
import org.safehaus.penrose.studio.schema.SchemasNode;
import org.safehaus.penrose.studio.cache.CachesNode;
import org.safehaus.penrose.studio.partition.PartitionsNode;

import java.util.ArrayList;
import java.util.Collection;

public class ObjectsView extends ViewPart implements ChangeListener, ISelectionChangedListener {

    Logger log = Logger.getLogger(getClass());

    public final static String PARTITIONS        = "Partitions";
    public final static String PARTITION         = "Partition";
    public final static String DIRECTORY         = "Directory";
    public final static String ENTRY             = "Entry";
    public final static String DATA_SOURCES      = "Data Sources";
    public final static String CONNECTIONS       = "Connections";
    public final static String CONNECTION        = "Connection";
    public final static String SOURCES           = "Sources";
    public final static String SOURCE            = "Source";
    public final static String CACHES            = "Caches";
    public final static String ENTRY_CACHE       = "Entry Cache";
    public final static String SOURCE_CACHE      = "Source Cache";
    public final static String ENGINES           = "Engines";
    public final static String ENGINE            = "Engine";
    public final static String ENGINE_CACHE      = "Engine Cache";
    public final static String CONNECTORS        = "Connectors";
    public final static String CONNECTOR         = "Connector";
    public final static String CONNECTOR_CACHE   = "Connector Cache";
    public final static String MODULES           = "Modules";
    public final static String MODULE            = "Module";
    public final static String SCHEMAS           = "Schemas";
    public final static String SCHEMA            = "Schema";
    public final static String OBJECT_CLASSES    = "Object Classes";
    public final static String OBJECT_CLASS      = "Object Class";
    public final static String ATTRIBUTE_TYPES   = "Attribute Types";
    public final static String ATTRIBUTE_TYPE    = "Attribute Type";
    public final static String SERVICES          = "Services";
    public final static String SERVICE           = "Service";
    public final static String ADMINISTRATOR     = "Administrator";
    public final static String ROOT_DSE          = "Root DSE";
    public final static String SYSTEM_PROPERTIES = "System Properties";
    public final static String LOGGERS           = "Loggers";
    public final static String LOGGER            = "Logger";

	private TreeViewer treeViewer;

    Object clipboard;

    Collection nodes = new ArrayList();

    private PartitionsNode partitionsNode;
    private SchemasNode schemasNode;
    private ServicesNode servicesNode;
    private CachesNode cachesNode;

    public ObjectsView() {
        partitionsNode = new PartitionsNode(this, PARTITIONS, PARTITIONS, PenrosePlugin.getImage(PenroseImage.FOLDER), PARTITIONS, null);
        nodes.add(partitionsNode);

        schemasNode = new SchemasNode(this, SCHEMAS, SCHEMAS, PenrosePlugin.getImage(PenroseImage.FOLDER), SCHEMAS, null);
        nodes.add(schemasNode);

        servicesNode = new ServicesNode(this, SERVICES, SERVICES, PenrosePlugin.getImage(PenroseImage.FOLDER), SERVICES, null);
        nodes.add(servicesNode);

        cachesNode = new CachesNode(this, CACHES, CACHES, PenrosePlugin.getImage(PenroseImage.FOLDER), CACHES, null);
        nodes.add(cachesNode);

        nodes.add(new EngineNode(
                this,
                ENGINE,
                ENGINE,
                PenrosePlugin.getImage(PenroseImage.ENGINE),
                ENGINE,
                this
        ));

        nodes.add(new ConnectorNode(
                this,
                CONNECTOR,
                CONNECTOR,
                PenrosePlugin.getImage(PenroseImage.CONNECTOR),
                CONNECTOR,
                null
        ));

        nodes.add(new AdministratorNode(
                this,
                ADMINISTRATOR,
                ADMINISTRATOR,
                PenrosePlugin.getImage(PenroseImage.ADMINISTRATOR),
                ADMINISTRATOR,
                null
        ));

        nodes.add(new SystemPropertiesNode(
                this,
                SYSTEM_PROPERTIES,
                SYSTEM_PROPERTIES,
                PenrosePlugin.getImage(PenroseImage.SYSTEM_PROPERTIES),
                SYSTEM_PROPERTIES,
                null
        ));
/*
        nodes.add(new LoggersNode(
                this,
                LOGGERS,
                LOGGERS,
                PenrosePlugin.getImage(PenroseImage.SYSTEM_PROPERTIES),
                LOGGERS,
                null
        ));
*/
	}
	
	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		try {
			treeViewer = new TreeViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);

			treeViewer.setContentProvider(new ObjectsContentProvider(this));
            treeViewer.setLabelProvider(new ObjectsLabelProvider(this));
			treeViewer.setInput(getViewSite());
			treeViewer.addSelectionChangedListener(this);

			Helper.hookContextMenu(treeViewer.getControl(), new IMenuListener() {
				public void menuAboutToShow(IMenuManager manager) {

                    try {
                        if (treeViewer.getTree().getSelectionCount() == 0) return;
                        TreeItem item = treeViewer.getTree().getSelection()[0];
                        Object object = item.getData();

                        Node node = (Node)object;
                        node.showMenu(manager);

                    } catch (Exception e) {
                        log.debug(e.getMessage(), e);
                    }
				}
			});

            treeViewer.addDoubleClickListener(new IDoubleClickListener() {
                public void doubleClick(DoubleClickEvent event) {
                    try {
                        ISelection selection = treeViewer.getSelection();
                        Object object = ((IStructuredSelection)selection).getFirstElement();

                        Node node = (Node)object;
                        node.open();

                        treeViewer.refresh();

                    } catch (Exception e) {
                        log.debug(e.getMessage(), e);
                    }
                }
            });

            PenroseApplication penroseApplication = PenroseApplication.getInstance();
			penroseApplication.addChangeListener(this);

		} catch (Exception ex) {
			log.debug(ex.toString(), ex);
		}
	}

    public void setClipboard(Object object) throws Exception {
        this.clipboard = object;
    }

    public Object getClipboard() throws Exception {
        return clipboard;
    }

	public void setFocus() {
		treeViewer.getControl().setFocus();
	}
	
	public void selectionChanged(SelectionChangedEvent event) {
	}
	
    public void handleChange(Object o) {
        treeViewer.refresh();
	}

    public void show(Object object) {
        treeViewer.setExpandedState(object, true);
    }

    public TreeViewer getTreeViewer() {
        return treeViewer;
    }

    public void setTreeViewer(TreeViewer treeViewer) {
        this.treeViewer = treeViewer;
    }

    public PartitionsNode getPartitionsNode() {
        return partitionsNode;
    }

    public void setPartitionsNode(PartitionsNode partitionsNode) {
        this.partitionsNode = partitionsNode;
    }

    public SchemasNode getSchemasNode() {
        return schemasNode;
    }

    public void setSchemasNode(SchemasNode schemasNode) {
        this.schemasNode = schemasNode;
    }

    public ServicesNode getServicesNode() {
        return servicesNode;
    }

    public void setServicesNode(ServicesNode servicesNode) {
        this.servicesNode = servicesNode;
    }

    public CachesNode getCachesNode() {
        return cachesNode;
    }

    public void setCachesNode(CachesNode cachesNode) {
        this.cachesNode = cachesNode;
    }
}