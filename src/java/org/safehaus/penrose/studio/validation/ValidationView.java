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
package org.safehaus.penrose.studio.validation;

import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.part.*;
import org.safehaus.penrose.mapping.*;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.mapping.MappingEditorInput;
import org.safehaus.penrose.studio.mapping.MappingEditor;
import org.safehaus.penrose.studio.connection.editor.ConnectionEditorInput;
import org.safehaus.penrose.studio.source.SourceEditorInput;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.adapter.PenroseStudioAdapter;
import org.safehaus.penrose.studio.server.ServerNode;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.object.ObjectsView;
import org.safehaus.penrose.studio.util.SWTUtil;
import org.safehaus.penrose.partition.*;
import org.safehaus.penrose.connection.ConnectionConfig;

public class ValidationView extends ViewPart {

    private Logger log = Logger.getLogger(getClass());

    Composite parent;

    Table table;
    private Collection results = new ArrayList();

    public void createPartControl(Composite parent) {
        log.debug("createPartControl");

        this.parent = parent;

        parent.setLayout(new FillLayout());

        table = new Table(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);

        SWTUtil.hookContextMenu(table, new IMenuListener() {
            public void menuAboutToShow(IMenuManager manager) {
                Action refreshAction = new Action("Refresh") {
                    public void run() {
                        try {
                            PenroseStudio penroseStudio = PenroseStudio.getInstance();
                            for (Iterator i=penroseStudio.getServers().iterator(); i.hasNext(); ) {
                                Server server = (Server)i.next();
                                server.validate();
                            }

                        } catch (Exception e) {
                            log.debug(e.getMessage(), e);
                        }
                    }
                };
                refreshAction.setImageDescriptor(PenrosePlugin.getImageDescriptor(PenroseImage.REFRESH));
                manager.add(refreshAction);
                manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
            }
        });

        table.addMouseListener(new MouseAdapter() {
            public void mouseDoubleClick(MouseEvent e) {
                try {
                    handleDoubleClick();
                } catch (Exception ex) {
                    log.debug(ex.getMessage(), ex);
                }
            }
        });

        TableColumn tc = new TableColumn(table, SWT.NONE);
        tc.setText("Type");
        tc.setWidth(100);

        tc = new TableColumn(table, SWT.NONE);
        tc.setText("Message");
        tc.setWidth(400);

        tc = new TableColumn(table, SWT.NONE);
        tc.setText("Object");
        tc.setWidth(200);

        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        refresh();
    }

    public void setFocus() {
        parent.setFocus();
    }

    public void refresh() {
        table.removeAll();
        for (Iterator i=results.iterator(); i.hasNext(); ) {
            PartitionValidationResult result = (PartitionValidationResult)i.next();

            TableItem item = new TableItem(table, SWT.NONE);
            item.setText(0, result.getType());
            item.setText(1, result.getMessage());
            item.setText(2, result.getSource());

            if (PartitionValidationResult.ERROR.equals(result.getType())) {
                item.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_RED));
            } else {
                item.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_BLACK));
            }

            item.setData(result.getObject());
        }
    }

    void handleDoubleClick() throws Exception {
        if (table.getSelectionCount() == 0) return;

        TableItem item = table.getSelection()[0];
        Object object = item.getData();

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        ObjectsView objectsView = (ObjectsView)page.showView(ObjectsView.class.getName());

        ServerNode serverNode = objectsView.getSelectedProjectNode();
        if (serverNode == null) return;

        Server server = serverNode.getServer();
        PartitionManager partitionManager = server.getPartitionManager();

        if (object instanceof ConnectionConfig) {
            ConnectionConfig connectionConfig = (ConnectionConfig)object;
            Partition partition = partitionManager.getPartition(connectionConfig);

            ConnectionEditorInput ei = new ConnectionEditorInput();
            ei.setPartition(partition);
            ei.setConnectionConfig(connectionConfig);

            PenroseStudio penroseStudio = PenroseStudio.getInstance();
            PenroseStudioAdapter adapter = penroseStudio.getAdapter(connectionConfig.getAdapterName());
            page.openEditor(ei, adapter.getConnectionEditorClassName());

            /*
            if ("LDAP".equals(connectionConfig.getAdapterName())) {
                page.openEditor(ei, LDAPConnectionEditor.class.getName());
                
            } else if ("JDBC".equals(connectionConfig.getAdapterName())) {
                page.openEditor(ei, JDBCConnectionEditor.class.getName());
            }
            */

        } else if (object instanceof SourceConfig) {
            SourceConfig sourceConfig = (SourceConfig)object;
            Partition partition = partitionManager.getPartition(sourceConfig);
            ConnectionConfig connection = partition.getConnectionConfig(sourceConfig.getConnectionName());

            SourceEditorInput ei = new SourceEditorInput();
            ei.setPartition(partition);
            ei.setSourceConfig(sourceConfig);

            PenroseStudio penroseStudio = PenroseStudio.getInstance();
            PenroseStudioAdapter adapter = penroseStudio.getAdapter(connection.getAdapterName());
            page.openEditor(ei, adapter.getSourceEditorClassName());

            /*
            if ("JDBC".equals(connection.getAdapterName())) {
                page.openEditor(ei, JDBCSourceEditor.class.getName());

            } else if ("LDAP".equals(connection.getAdapterName())) {
                page.openEditor(ei, LDAPSourceEditor.class.getName());
            }
            */

        } else if (object instanceof EntryMapping) {
            EntryMapping entryMapping = (EntryMapping)object;
            Partition partition = partitionManager.getPartition(entryMapping);

            MappingEditorInput mei = new MappingEditorInput();
            mei.setPartition(partition);
            mei.setEntryDefinition(entryMapping);

            page.openEditor(mei, MappingEditor.class.getName());
        }
    }

    public Collection getResults() {
        return results;
    }

    public void setResults(Collection results) {
        this.results.clear();
        this.results.addAll(results);
    }
}

