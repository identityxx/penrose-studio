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
package org.safehaus.penrose.studio.connection.wizard;

import org.eclipse.jface.wizard.Wizard;
import org.apache.log4j.Logger;
import org.safehaus.penrose.connection.ConnectionConfig;
import org.safehaus.penrose.studio.dialog.ErrorDialog;

/**
 * @author Endi S. Dewata
 */
public class ConnectionPropertiesWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    private ConnectionConfig connectionConfig;

    public ConnectionPropertiesWizardPage propertiesPage;

    public ConnectionPropertiesWizard() {
        setWindowTitle("Edit Connection Properties");
    }

    public void addPages() {

        propertiesPage = new ConnectionPropertiesWizardPage();

        propertiesPage.setConnectionName(connectionConfig.getName());
        propertiesPage.setClassName(connectionConfig.getConnectionClass());
        propertiesPage.setEnabled(connectionConfig.isEnabled());
        propertiesPage.setConnectionDescription(connectionConfig.getDescription());

        addPage(propertiesPage);
    }

    public boolean canFinish() {
        if (!propertiesPage.isPageComplete()) return false;

        return true;
    }

    public boolean performFinish() {
        try {
            connectionConfig.setName(propertiesPage.getConnectionName());
            connectionConfig.setConnectionClass(propertiesPage.getClassName());
            connectionConfig.setEnabled(propertiesPage.isEnabled());
            connectionConfig.setDescription(propertiesPage.getConnectionDescription());

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

    public ConnectionConfig getConnectionConfig() {
        return connectionConfig;
    }

    public void setConnectionConfig(ConnectionConfig connectionConfig) {
        this.connectionConfig = connectionConfig;
    }
}