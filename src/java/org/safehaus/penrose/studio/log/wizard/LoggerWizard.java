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
package org.safehaus.penrose.studio.log.wizard;

import org.apache.log4j.Logger;
import org.eclipse.jface.wizard.Wizard;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.log.log4j.LoggerConfig;
import org.safehaus.penrose.log.LogManagerClient;

/**
 * @author Endi S. Dewata
 */
public class LoggerWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    LoggerPropertiesWizardPage propertiesPage;
    LoggerAppendersWizardPage appendersPage;

    LogManagerClient logManagerClient;
    LoggerConfig loggerConfig;

    public LoggerWizard() {
        setWindowTitle("Logger");
    }

    public void addPages() {

        propertiesPage = new LoggerPropertiesWizardPage();
        propertiesPage.setLoggerName(loggerConfig.getName());
        propertiesPage.setLevel(loggerConfig.getLevel());
        propertiesPage.setAdditivity(loggerConfig.getAdditivity());

        addPage(propertiesPage);

        appendersPage = new LoggerAppendersWizardPage();
        appendersPage.setLogManagerClient(logManagerClient);
        appendersPage.setAppenderNames(loggerConfig.getAppenderNames());

        addPage(appendersPage);
    }

    public boolean needsPreviousAndNextButtons() {
        return true;
    }

    public boolean canFinish() {

        if (!propertiesPage.isPageComplete()) return false;
        if (!appendersPage.isPageComplete()) return false;

        return true;
    }

    public boolean performFinish() {
        try {
            loggerConfig.setName(propertiesPage.getLoggerName());
            loggerConfig.setLevel(propertiesPage.getLevel());
            loggerConfig.setAdditivity(propertiesPage.getAdditivity());

            loggerConfig.setAppenderNames(appendersPage.getAppenderNames());

            return true;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
            return false;
        }
    }

    public LoggerConfig getLoggerConfig() {
        return loggerConfig;
    }

    public void setLoggerConfig(LoggerConfig loggerConfig) {
        this.loggerConfig = loggerConfig;
    }

    public LogManagerClient getLogManagerClient() {
        return logManagerClient;
    }

    public void setLogManagerClient(LogManagerClient logManagerClient) {
        this.logManagerClient = logManagerClient;
    }
}