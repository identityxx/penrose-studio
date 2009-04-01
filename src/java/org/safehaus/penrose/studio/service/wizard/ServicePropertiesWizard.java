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
package org.safehaus.penrose.studio.service.wizard;

import org.eclipse.jface.wizard.Wizard;
import org.safehaus.penrose.service.ServiceConfig;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.apache.log4j.Logger;

/**
 * @author Endi S. Dewata
 */
public class ServicePropertiesWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    private ServiceConfig serviceConfig;

    public ServicePropertiesWizardPage propertiesPage;

    public ServicePropertiesWizard() {
        setWindowTitle("Edit Service Properties");
    }

    public void addPages() {

        propertiesPage = new ServicePropertiesWizardPage();

        propertiesPage.setServiceName(serviceConfig.getName());
        propertiesPage.setClassName(serviceConfig.getServiceClass());
        propertiesPage.setEnabled(serviceConfig.isEnabled());
        propertiesPage.setServiceDescription(serviceConfig.getDescription());

        addPage(propertiesPage);
    }

    public boolean canFinish() {
        if (!propertiesPage.isPageComplete()) return false;

        return true;
    }

    public boolean performFinish() {
        try {
            serviceConfig.setName(propertiesPage.getServiceName());
            serviceConfig.setServiceClass(propertiesPage.getClassName());
            serviceConfig.setEnabled(propertiesPage.isEnabled());
            serviceConfig.setDescription(propertiesPage.getServiceDescription());

            return true;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
            return false;
        }
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