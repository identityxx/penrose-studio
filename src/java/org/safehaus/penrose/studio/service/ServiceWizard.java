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
package org.safehaus.penrose.studio.service;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchPage;
import org.safehaus.penrose.service.ServiceConfig;
import org.safehaus.penrose.studio.project.ProjectNode;
import org.safehaus.penrose.studio.object.ObjectsView;
import org.safehaus.penrose.config.PenroseConfig;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.Iterator;

/**
 * @author Endi S. Dewata
 */
public class ServiceWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    private ServiceConfig serviceConfig;

    public ServiceWizardPage propertyPage = new ServiceWizardPage();
    public ServiceParameterPage parameterPage = new ServiceParameterPage();

    public ServiceWizard() {
        setWindowTitle("New Service");
    }

    public boolean canFinish() {
        if (!propertyPage.isPageComplete()) return false;
        if (!parameterPage.isPageComplete()) return false;

        return true;
    }

    public boolean performFinish() {
        try {
            serviceConfig = new ServiceConfig();
            serviceConfig.setName(propertyPage.getServiceName());
            serviceConfig.setServiceClass(propertyPage.getServiceClass());
            serviceConfig.setDescription(propertyPage.getDescription());
            serviceConfig.setEnabled(propertyPage.isEnabled());

            Map parameters = parameterPage.getParameters();
            for (Iterator i=parameters.keySet().iterator(); i.hasNext(); ) {
                String name = (String)i.next();
                String value = (String)parameters.get(name);

                serviceConfig.setParameter(name, value);
            }

            IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            IWorkbenchPage page = window.getActivePage();
            ObjectsView objectsView = (ObjectsView)page.showView(ObjectsView.class.getName());

            ProjectNode projectNode = objectsView.getSelectedProjectNode();
            if (projectNode == null) return false;

            PenroseConfig penroseConfig = projectNode.getPenroseConfig();
            penroseConfig.addServiceConfig(serviceConfig);

            return true;

        } catch (Exception e) {
            log.debug(e.getMessage(), e);
            return false;
        }
    }

    public void addPages() {
        addPage(propertyPage);
        addPage(parameterPage);
    }

    public boolean needsPreviousAndNextButtons() {
        return true;
    }

    public ServiceConfig getServiceConfig() {
        return serviceConfig;
    }

    public void setServiceConfig(ServiceConfig serviceConfig) {
        this.serviceConfig = serviceConfig;
    }
}
