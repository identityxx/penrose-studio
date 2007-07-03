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
package org.safehaus.penrose.studio.connection.action;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.wizard.WizardDialog;
import org.safehaus.penrose.studio.object.ObjectsView;
import org.safehaus.penrose.studio.PenroseApplication;
import org.safehaus.penrose.studio.source.wizard.JDBCSourceWizard;
import org.safehaus.penrose.studio.source.wizard.JNDISourceWizard;
import org.safehaus.penrose.studio.connection.ConnectionNode;
import org.safehaus.penrose.partition.ConnectionConfig;
import org.safehaus.penrose.partition.Partition;
import org.apache.log4j.Logger;

public class NewSourceAction extends Action {

    Logger log = Logger.getLogger(getClass());

    ConnectionNode node;

	public NewSourceAction(ConnectionNode node) {
        this.node = node;

        setText("New Source...");
        setId(getClass().getName());
	}
	
	public void run() {
        try {
            IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            IWorkbenchPage page = window.getActivePage();
            ObjectsView objectsView = (ObjectsView)page.showView(ObjectsView.class.getName());

            Shell shell = window.getShell();

            Partition partition = node.getPartition();
            ConnectionConfig connectionConfig = node.getConnectionConfig();
            String adapterName = connectionConfig.getAdapterName();

            if ("JDBC".equals(adapterName)) {
                JDBCSourceWizard wizard = new JDBCSourceWizard(partition, connectionConfig);
                WizardDialog dialog = new WizardDialog(shell, wizard);
                dialog.setPageSize(600, 300);
                dialog.open();

            } else if ("LDAP".equals(adapterName)) {
                JNDISourceWizard wizard = new JNDISourceWizard(partition, connectionConfig);
                WizardDialog dialog = new WizardDialog(shell, wizard);
                dialog.setPageSize(600, 300);
                dialog.open();
            }

            PenroseApplication penroseApplication = PenroseApplication.getInstance();
            penroseApplication.notifyChangeListeners();

            objectsView.show(node);

        } catch (Exception e) {
            log.debug(e.getMessage(), e);
        }
	}
	
}