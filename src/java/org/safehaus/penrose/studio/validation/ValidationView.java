/**
 * Copyright 2009 Red Hat, Inc.
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
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.studio.plugin.PluginManager;
import org.safehaus.penrose.studio.plugin.Plugin;
import org.safehaus.penrose.studio.util.Helper;
import org.safehaus.penrose.validation.ValidationResult;

public class ValidationView extends ViewPart {

	private Logger log = Logger.getLogger(getClass());
	
	Composite parent;

	Table table;
    Collection<ValidationResult> results = new ArrayList<ValidationResult>();

	public void createPartControl(Composite parent) {
		this.parent = parent;
		
		parent.setLayout(new FillLayout());
		
		table = new Table(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);

		Helper.hookContextMenu(table, new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				Action refreshAction = new Action("Refresh") {
					public void run() {
                        try {
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                            ErrorDialog.open(e);
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

        for (ValidationResult result : results) {

            TableItem item = new TableItem(table, SWT.NONE);
            item.setText(0, result.getType());
            item.setText(1, result.getMessage());
            item.setText(2, result.getPartitionName()+"."+result.getObjectName());

            if (ValidationResult.ERROR.equals(result.getType())) {
                item.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_RED));
            } else {
                item.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_BLACK));
            }

            item.setData(result);
        }
    }

	void handleDoubleClick() throws Exception {
        if (table.getSelectionCount() == 0) return;

        TableItem item = table.getSelection()[0];
		ValidationResult result = (ValidationResult)item.getData();
        String partitionName = result.getPartitionName();

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        PluginManager pluginManager = penroseStudio.getPluginManager();

        if (result.getObjectType() == ValidationResult.CONNECTION) {

            String connectionName = result.getObjectName();
            String adapterName = null;

            IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            IWorkbenchPage page = window.getActivePage();

            Plugin plugin = pluginManager.getPlugin(adapterName);
            ConnectionEditorInput ei = plugin.createConnectionEditorInput();
            ei.setPartitionName(partitionName);
            ei.setConnectionName(connectionName);

            String connectionEditorClass = plugin.getConnectionEditorClass();
            page.openEditor(ei, connectionEditorClass);

		} else if (result.getObjectType() == ValidationResult.SOURCE) {

            String sourceName = result.getObjectName();
            String adapterName = null;

            IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            IWorkbenchPage page = window.getActivePage();

            Plugin plugin = pluginManager.getPlugin(adapterName);
            SourceEditorInput ei = plugin.createSourceEditorInput();
            ei.setPartitionName(partitionName);
            ei.setSourceName(sourceName);

            String sourceEditorClass = plugin.getSourceEditorClass();
            page.openEditor(ei, sourceEditorClass);

		} else if (result.getObjectType() == ValidationResult.ENTRY) {

            String entryName = result.getObjectName();

            EntryEditorInput ei = new EntryEditorInput();
            ei.setPartitionName(partitionName);
            ei.setEntryName(entryName);

            IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            IWorkbenchPage page = window.getActivePage();

            page.openEditor(ei, EntryEditor.class.getName());
		}
	}

    public Collection getResults() {
        return results;
    }

    public void setResults(Collection<ValidationResult> results) {
        this.results.clear();
        this.results.addAll(results);
    }
}

