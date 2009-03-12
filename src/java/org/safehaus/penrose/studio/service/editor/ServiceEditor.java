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
package org.safehaus.penrose.studio.service.editor;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.service.ServiceManagerClient;
import org.safehaus.penrose.service.ServiceConfig;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.studio.config.editor.ParametersPage;
import org.safehaus.penrose.studio.server.Server;

/**
 * @author Endi S. Dewata
 */
public class ServiceEditor extends FormEditor {

    Logger log = Logger.getLogger(getClass());

    Server server;
    ServiceConfig origServiceConfig;
    ServiceConfig serviceConfig;

    boolean dirty;

    ServicePropertiesPage propertiesPage;

    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        ServiceEditorInput ei = (ServiceEditorInput)input;

        server = ei.getServer();
        origServiceConfig = ei.getServiceConfig();

        try {
            serviceConfig = (ServiceConfig)origServiceConfig.clone();
        } catch (Exception e) {
            throw new PartInitException(e.getMessage(), e);
        }

        setSite(site);
        setInput(input);
        setPartName(ei.getName());
    }

    protected void addPages() {
        try {
            propertiesPage = new ServicePropertiesPage(this);
            addPage(propertiesPage);

            ParametersPage parametersPage = new ServiceParametersPage(this);
            parametersPage.setParameters(serviceConfig.getParameters());
            addPage(parametersPage);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }

    public void doSave(IProgressMonitor iProgressMonitor) {
        try {
            store();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }

    public void doSaveAs() {
    }

    public void store() throws Exception {

        PenroseClient client = server.getClient();
        ServiceManagerClient serviceManagerClient = client.getServiceManagerClient();
        serviceManagerClient.updateService(origServiceConfig.getName(), serviceConfig);

        origServiceConfig.copy(serviceConfig);

        setPartName(serviceConfig.getName());

        checkDirty();
    }

    public boolean isDirty() {
        return dirty;
    }

    public boolean isSaveAsAllowed() {
        return false;
    }

    public void checkDirty() {
        try {
            dirty = false;
/*
            if (!origServiceConfig.equals(serviceConfig)) {
                dirty = true;
                return;
            }
*/
        } catch (Exception e) {
            log.error(e.getMessage(), e);

        } finally {
            firePropertyChange(PROP_DIRTY);
        }
    }
}
