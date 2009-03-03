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
package org.safehaus.penrose.studio.server.action;

import org.eclipse.swt.SWT;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.safehaus.penrose.studio.server.ServerConfig;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.server.wizard.AddServerWizard;
import org.apache.log4j.Logger;

public class AddServerAction extends Action {

    Logger log = Logger.getLogger(getClass());

	public AddServerAction() {
        setText("&New Server...");
        setImageDescriptor(PenroseStudio.getImageDescriptor(PenroseImage.NEW));
        setAccelerator(SWT.CTRL | 'N');
        setToolTipText("New Server");
        setId(getClass().getName());
	}
	
	public void run() {
        try {
            ServersView serversView = ServersView.getInstance();

            ServerConfig serverConfig = new ServerConfig();
            serverConfig.setName("My Server");
            serverConfig.setHost("localhost");
            serverConfig.setPort(1099);
            serverConfig.setUsername("uid=admin,ou=system");
            serverConfig.setPassword("secret");

            AddServerWizard wizard = new AddServerWizard();
            wizard.setServerConfig(serverConfig);
            
            WizardDialog dialog = new WizardDialog(serversView.getSite().getShell(), wizard);
            dialog.setPageSize(600, 300);
            int rc = dialog.open();

            if (rc == Window.CANCEL) return;

            serversView.refresh();

            PenroseStudio penroseStudio = PenroseStudio.getInstance();
            penroseStudio.notifyChangeListeners();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e.getMessage());
        }
	}
	
}