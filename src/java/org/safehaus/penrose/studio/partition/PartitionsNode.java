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
package org.safehaus.penrose.studio.partition;

import org.safehaus.penrose.studio.*;
import org.safehaus.penrose.studio.util.FileUtil;
import org.safehaus.penrose.studio.partition.action.*;
import org.safehaus.penrose.studio.object.ObjectsView;
import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.management.PenroseClient;
import org.safehaus.penrose.partition.PartitionConfig;
import org.safehaus.penrose.partition.PartitionConfigs;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchWindow;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.io.File;

/**
 * @author Endi S. Dewata
 */
public class PartitionsNode extends Node {

    Logger log = Logger.getLogger(getClass());

    ObjectsView view;

    protected Map<String,Node> children;

    public PartitionsNode(ObjectsView view, String name, String type, Image image, Object object, Object parent) {
        super(name, type, image, object, parent);
        this.view = view;
    }

    public void showMenu(IMenuManager manager) {
        manager.add(new NewPartitionAction());
        manager.add(new ImportPartitionAction());

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        PenroseWorkbenchAdvisor workbenchAdvisor = penroseStudio.getWorkbenchAdvisor();
        PenroseWorkbenchWindowAdvisor workbenchWindowAdvisor = workbenchAdvisor.getWorkbenchWindowAdvisor();
        PenroseActionBarAdvisor actionBarAdvisor = workbenchWindowAdvisor.getActionBarAdvisor();

        //if (actionBarAdvisor.getShowCommercialFeaturesAction().isChecked()) {
            manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
            manager.add(new NewLDAPSnapshotPartitionAction());
            manager.add(new NewLDAPProxyPartitionAction());
        //}

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(new Action("Paste") {
            public void run() {
                try {
                    paste();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
            public boolean isEnabled() {
                Object object = view.getClipboard();
                return object != null && object instanceof PartitionConfig;
            }
        });

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(new RefreshAction(this));
    }

    public void refresh() throws Exception {
        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        PenroseClient penroseClient = penroseStudio.getClient();

        children = new TreeMap<String,Node>();

        for (PartitionConfig partitionConfig : penroseStudio.getPartitionConfigs().getPartitionConfigs()) {

            PartitionNode partitionNode = new PartitionNode(
                    view,
                    partitionConfig.getName(),
                    ObjectsView.PARTITION,
                    PenrosePlugin.getImage(PenroseImage.PARTITION),
                    partitionConfig,
                    this
            );

            children.put(partitionConfig.getName(), partitionNode);
        }

        for (String name : penroseClient.getPartitionNames()) {

            if (children.containsKey(name)) continue;

            PartitionNode partitionNode = new PartitionNode(
                    view,
                    name,
                    ObjectsView.PARTITION,
                    PenrosePlugin.getImage(PenroseImage.PARTITION),
                    null,
                    this
            );

            children.put(name, partitionNode);
        }
    }

    public void paste() throws Exception {

        Object object = view.getClipboard();
        if (!(object instanceof PartitionConfig)) return;

        PartitionConfig oldPartition = (PartitionConfig)object;
        //PartitionConfig newPartition = (PartitionConfig)oldPartition.clone();

        String oldName = oldPartition.getName();

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        Shell shell = window.getShell();

        PartitionDialog dialog = new PartitionDialog(shell, SWT.NONE);
        dialog.setName(oldName);
        dialog.setText("New Partition");

        dialog.open();

        if (dialog.getAction() == PartitionDialog.CANCEL) return;

        String newName = dialog.getName();

        log.debug("Pasting "+oldName+" partition into "+newName+".");

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        PartitionConfigs partitionConfigs = penroseStudio.getPartitionConfigs();

        File workDir = penroseStudio.getWorkDir();

        File oldDir = new File(workDir, "partitions"+File.separator+oldName);
        File newDir = new File(workDir, "partitions"+File.separator+newName);
        FileUtil.copy(oldDir, newDir);

        PartitionConfig newPartition = partitionConfigs.load(newDir);
        partitionConfigs.addPartitionConfig(newPartition);

        view.setClipboard(null);

        refresh();

        penroseStudio.notifyChangeListeners();
    }

    public boolean hasChildren() throws Exception {

        if (children == null) {
            refresh();
        }

        return !children.isEmpty();
    }

    public Collection<Node> getChildren() throws Exception {

        if (children == null) {
            refresh();
        }

        return children.values();
    }
}
