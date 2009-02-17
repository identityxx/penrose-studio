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
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.directory.editor.EntryEditorInput;
import org.safehaus.penrose.studio.directory.editor.EntryEditor;
import org.safehaus.penrose.studio.connection.editor.*;
import org.safehaus.penrose.studio.source.editor.*;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.plugin.PluginManager;
import org.safehaus.penrose.studio.plugin.Plugin;
import org.safehaus.penrose.studio.util.Helper;
import org.safehaus.penrose.partition.*;
import org.safehaus.penrose.source.SourceConfig;
import org.safehaus.penrose.connection.ConnectionConfig;
import org.safehaus.penrose.directory.EntryConfig;

public class ValidationView extends ViewPart {

	private Logger log = Logger.getLogger(getClass());
	
	Composite parent;

	Table table;
    private Collection results = new ArrayList();

	public void createPartControl(Composite parent) {
		this.parent = parent;
		
		parent.setLayout(new FillLayout());
		
		table = new Table(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);

		Helper.hookContextMenu(table, new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				Action refreshAction = new Action("Refresh") {
					public void run() {
                        try {
                            PenroseStudio penroseStudio = PenroseStudio.getInstance();
                            //penroseStudio.validatePartitions();
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }
					}
				};
				refreshAction.setImageDescriptor(PenroseStudio.getImageDescriptor(PenroseImage.REFRESH));
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
            item.setText(2, result.getSource().toString());

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

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        //PartitionConfigManager partitionConfigManager = penroseStudio.getPartitionConfigManager();
        PluginManager pluginManager = penroseStudio.getPluginManager();

        if (object instanceof ConnectionConfig) {
            ConnectionConfig connectionConfig = (ConnectionConfig)object;
            PartitionConfig partitionConfig = null; // partitionConfigManager.getPartitionConfig(connectionConfig);

            IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            IWorkbenchPage page = window.getActivePage();

            Plugin plugin = pluginManager.getPlugin(connectionConfig.getAdapterName());
            ConnectionEditorInput ei = plugin.createConnectionEditorInput();
            ei.setPartitionName(partitionConfig.getName());
            ei.setConnectionName(connectionConfig.getName());

            String connectionEditorClass = plugin.getConnectionEditorClass();
            page.openEditor(ei, connectionEditorClass);

		} else if (object instanceof SourceConfig) {
			SourceConfig sourceConfig = (SourceConfig)object;
            PartitionConfig partitionConfig = null; // partitionConfigManager.getPartitionConfig(sourceConfig);
            ConnectionConfig connection = partitionConfig.getConnectionConfigManager().getConnectionConfig(sourceConfig.getConnectionName());

            IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            IWorkbenchPage page = window.getActivePage();

            Plugin plugin = pluginManager.getPlugin(connection.getAdapterName());
            SourceEditorInput ei = plugin.createSourceEditorInput();
            ei.setPartitionName(partitionConfig.getName());
            ei.setSourceName(sourceConfig.getName());

            String sourceEditorClass = plugin.getSourceEditorClass();
            page.openEditor(ei, sourceEditorClass);

		} else if (object instanceof EntryConfig) {
            EntryConfig entryConfig = (EntryConfig)object;
            PartitionConfig partitionConfig = null; // partitionConfigManager.getPartitionConfig(entryConfig);

            EntryEditorInput ei = new EntryEditorInput();
            ei.setPartitionName(partitionConfig.getName());
            ei.setEntryName(entryConfig.getName());

            IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            IWorkbenchPage page = window.getActivePage();

            page.openEditor(ei, EntryEditor.class.getName());
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

