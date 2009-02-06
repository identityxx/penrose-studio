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
package org.safehaus.penrose.studio.user.editor;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.user.UserConfig;

/**
 * @author Endi S. Dewata
 */
public class UserEditor extends FormEditor {

    Logger log = Logger.getLogger(getClass());

    Server server;

    UserConfig userConfig;

    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        super.init(site, input);

        UserEditorInput ei = (UserEditorInput)input;
        server = ei.getServer();
        userConfig = ei.getUserConfig();

        setPartName(ei.getName());
    }

    protected void addPages() {
        try {
            addPage(new UserPropertyPage(this));

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public boolean isSaveAsAllowed() {
        return false;
    }

    public void doSave(IProgressMonitor iProgressMonitor) {
    }

    public void doSaveAs() {
    }

    public void store() throws Exception {

        PenroseClient client = server.getClient();
        client.setRootUserConfig(userConfig);

        setPartName(userConfig.getDn().toString());
    }

    public UserConfig getUserConfig() {
        return userConfig;
    }

    public void setUserConfig(UserConfig userConfig) {
        this.userConfig = userConfig;
    }
}
