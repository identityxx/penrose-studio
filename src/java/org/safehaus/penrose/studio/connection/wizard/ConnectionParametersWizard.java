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
package org.safehaus.penrose.studio.connection.wizard;

import org.apache.log4j.Logger;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.safehaus.penrose.studio.config.wizard.ParametersWizardPage;
import org.safehaus.penrose.connection.ConnectionConfig;

/**
 * @author Endi S. Dewata
 */
public class ConnectionParametersWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    private ConnectionConfig connectionConfig;

    public ParametersWizardPage parametersPage;

    public ConnectionParametersWizard() {
        setWindowTitle("Edit Connection Parameters");
    }

    public void addPages() {

        parametersPage = new ParametersWizardPage();
        parametersPage.setDescription("Enter the parameters of the connection.");
        parametersPage.setParameters(connectionConfig.getParameters());

        addPage(parametersPage);
    }

    public boolean canFinish() {
        if (!parametersPage.isPageComplete()) return false;

        return true;
    }

    public IWizardPage getNextPage(IWizardPage page) {
        return super.getNextPage(page);
    }

    public boolean performFinish() {
        try {
            connectionConfig.setParameters(parametersPage.getParameters());

            return true;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
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