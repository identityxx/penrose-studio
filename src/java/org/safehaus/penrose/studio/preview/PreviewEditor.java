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
package org.safehaus.penrose.studio.preview;

import java.io.File;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.*;
import org.eclipse.core.runtime.IProgressMonitor;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.Penrose;
import org.safehaus.penrose.PenroseFactory;
import org.safehaus.penrose.studio.PenrosePlugin;

public class PreviewEditor extends EditorPart {

	private Logger log = Logger.getLogger(getClass());
	
	SashForm sash;
	
	TreeViewer treeViewer;
	PreviewTreeProvider treeProvider;
	
	TableViewer tableViewer;
	PreviewTableProvider tableProvider;
	
	Penrose penrose;

    Image refreshImage;

    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        setSite(site);
        setInput(input);

        refreshImage = PenrosePlugin.getImage(PenroseImage.REFRESH);
    }

    public void dispose() {
    }

	public void createPartControl(Composite parent) {

        start();

        parent.setLayout(new GridLayout());

        ToolBar toolBar = new ToolBar(parent, SWT.FLAT);
        toolBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        ToolItem toolItem = new ToolItem(toolBar, SWT.PUSH);
        toolItem.setText("Refresh");
        //toolItem.setImage(refreshImage);
        //toolItem.setWidth(200);

        toolItem.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                stop();
                start();
                refresh();
            }
        });

		sash = new SashForm(parent, SWT.VERTICAL);
        sash.setLayoutData(new GridData(GridData.FILL_BOTH));

		treeViewer = new TreeViewer(sash, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		treeProvider = new PreviewTreeProvider(this);
		treeViewer.setContentProvider(treeProvider);
		treeViewer.setLabelProvider(treeProvider);
		treeViewer.addSelectionChangedListener(treeProvider);
		treeViewer.setInput(getSite());

		tableViewer = new TableViewer(sash, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		tableProvider = new PreviewTableProvider(this);
		tableViewer.setContentProvider(tableProvider);
		tableViewer.setLabelProvider(tableProvider);
		tableViewer.getTable().setHeaderVisible(true);
		tableViewer.getTable().setLinesVisible(true);
		tableViewer.setCellModifier(tableProvider);
		tableViewer.addSelectionChangedListener(tableProvider);
		tableViewer.setUseHashlookup(true);
		tableViewer.setInput(getSite());
		tableViewer.setColumnProperties(new String[] {"name", "value"});

		TableColumn tc = new TableColumn(tableViewer.getTable(), SWT.NONE, 0);
		tc.setText("Attribute Name");
		tc.setWidth(150);

		tc = new TableColumn(tableViewer.getTable(), SWT.NONE, 1);
		tc.setText("Attribute Value");
		tc.setWidth(350);
	}

	public void setFocus() {
		//parent.setFocus();
	}
	
	public void refresh() {
		log.debug("Refreshing tree");
		treeViewer.refresh();
	}

    public void stop() {
        try {
            if (penrose != null) {
                penrose.stop();
                penrose = null;
            }
        } catch (Exception e) {
            log.debug(e.toString(), e);
            MessageDialog.openError(getSite().getShell(), "Error", e.getMessage());
        }
    }

    public void start() {
		try {
            log.debug("Starting Penrose");

            PenroseFactory penroseFactory = PenroseFactory.getInstance();
			penrose = penroseFactory.createPenrose(System.getProperty("user.dir")+File.separator+"tmp");
			penrose.start();

            log.debug("Penrose started");

		} catch (Exception e) {
			log.error(e.getMessage(), e);
            MessageDialog.openError(getSite().getShell(), "Error", e.getMessage());
		}
	}

    public void doSave(IProgressMonitor iProgressMonitor) {
    }

    public void doSaveAs() {
    }

    public boolean isDirty() {
        return false;
    }

    public boolean isSaveAsAllowed() {
        return false;
    }

}

