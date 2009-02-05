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
package org.safehaus.penrose.studio.browser.action;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.safehaus.penrose.service.ServiceManagerClient;
import org.safehaus.penrose.service.ServiceConfig;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.browser.editor.BrowserEditor;
import org.safehaus.penrose.studio.browser.editor.BrowserEditorInput;
import org.safehaus.penrose.studio.server.ServerConfig;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.server.*;
import org.safehaus.penrose.studio.server.node.ServerNode;
import org.safehaus.penrose.user.UserConfig;
import org.safehaus.penrose.client.PenroseClient;

/**
 * @author Endi S. Dewata
 */
public class BrowserAction extends Action {

    Logger log = Logger.getLogger(getClass());

    public final static String LDAP_PORT             = "ldapPort";
    public final static int DEFAULT_LDAP_PORT        = 10389;

    public BrowserAction() {

        setText("&Browser");
        setImageDescriptor(PenroseStudio.getImageDescriptor(PenroseImage.BROWSER));
        setToolTipText("Browser");
        setId(getClass().getName());
    }

	public void run() {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        
        try {
            ServersView serversView = ServersView.getInstance();
            ServerNode projectNode = serversView.getSelectedServerNode();
            Server project = projectNode.getServer();
            PenroseClient client = project.getClient();

            ServerConfig projectConfig = project.getServerConfig();
            String hostname = projectConfig.getHost();

            ServiceManagerClient serviceManagerClient = client.getServiceManagerClient();
            ServiceConfig serviceConfig = serviceManagerClient.getServiceConfig("LDAP");
            String s = serviceConfig == null ? null : serviceConfig.getParameter(LDAP_PORT);
            int port = s == null ? DEFAULT_LDAP_PORT : Integer.parseInt(s);

            PenroseClient penroseClient = project.getClient();
            UserConfig rootUserConfig = penroseClient.getRootUserConfig();

            String bindDn = rootUserConfig.getDn().toString();
            byte[] password = rootUserConfig.getPassword();

            BrowserEditorInput ei = new BrowserEditorInput();
            ei.setHostname(hostname);
            ei.setPort(port);
            ei.setBindDn(bindDn);
            ei.setPassword(password);

            IWorkbenchPage page = window.getActivePage();
            page.openEditor(ei, BrowserEditor.class.getName());

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
	}
}