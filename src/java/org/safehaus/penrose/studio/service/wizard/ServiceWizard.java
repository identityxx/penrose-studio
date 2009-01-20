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
package org.safehaus.penrose.studio.service.wizard;

import org.eclipse.jface.wizard.Wizard;
import org.safehaus.penrose.service.ServiceConfig;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * @author Endi S. Dewata
 */
public class ServiceWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    private ServiceConfig serviceConfig;

    public ServicePropertiesWizardPage propertiesPage = new ServicePropertiesWizardPage();
    public ServiceParametersWizardPage parametersPage = new ServiceParametersWizardPage();

    public ServiceWizard() {
        setWindowTitle("New Service");
    }

    public boolean canFinish() {
        if (!propertiesPage.isPageComplete()) return false;
        if (!parametersPage.isPageComplete()) return false;

        return true;
    }

    public boolean performFinish() {
        try {
            serviceConfig = new ServiceConfig();
            serviceConfig.setName(propertiesPage.getServiceName());
            serviceConfig.setServiceClass(propertiesPage.getClassName());
            serviceConfig.setEnabled(propertiesPage.isEnabled());
            serviceConfig.setDescription(propertiesPage.getServiceDescription());

            Map<String,String> parameters = parametersPage.getParameters();
            for (String name : parameters.keySet()) {
                String value = parameters.get(name);

                serviceConfig.setParameter(name, value);
            }

            return true;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    public void addPages() {
        addPage(propertiesPage);
        addPage(parametersPage);
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
