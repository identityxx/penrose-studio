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
package org.safehaus.penrose.studio.log.wizard;

import org.apache.log4j.Logger;
import org.eclipse.jface.wizard.Wizard;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.log.log4j.RootLoggerConfig;
import org.safehaus.penrose.log.LogManagerClient;

/**
 * @author Endi S. Dewata
 */
public class RootLoggerAppendersWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    public LoggerAppendersWizardPage appendersPage;

    LogManagerClient logManagerClient;
    RootLoggerConfig rootLoggerConfig;

    public RootLoggerAppendersWizard() {
        setWindowTitle("Root Logger");
    }

    public void addPages() {

        appendersPage = new LoggerAppendersWizardPage();
        appendersPage.setLogManagerClient(logManagerClient);
        appendersPage.setAppenderNames(rootLoggerConfig.getAppenderNames());

        addPage(appendersPage);
    }

    public boolean needsPreviousAndNextButtons() {
        return true;
    }

    public boolean canFinish() {

        if (!appendersPage.isPageComplete()) return false;

        return true;
    }

    public boolean performFinish() {
        try {
            rootLoggerConfig.setAppenderNames(appendersPage.getAppenderNames());

            return true;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
            return false;
        }
    }

    public RootLoggerConfig getRootLoggerConfig() {
        return rootLoggerConfig;
    }

    public void setRootLoggerConfig(RootLoggerConfig rootLoggerConfig) {
        this.rootLoggerConfig = rootLoggerConfig;
    }

    public LogManagerClient getLogManagerClient() {
        return logManagerClient;
    }

    public void setLogManagerClient(LogManagerClient logManagerClient) {
        this.logManagerClient = logManagerClient;
    }
}