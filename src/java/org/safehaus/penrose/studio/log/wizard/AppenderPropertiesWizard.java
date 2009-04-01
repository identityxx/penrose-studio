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
import org.safehaus.penrose.log.log4j.AppenderConfig;

/**
 * @author Endi S. Dewata
 */
public class AppenderPropertiesWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    public AppenderPropertiesWizardPage propertiesPage;

    protected AppenderConfig appenderConfig;

    public AppenderPropertiesWizard() {
        setWindowTitle("Appender");
    }

    public void addPages() {

        propertiesPage = new AppenderPropertiesWizardPage();
        propertiesPage.setAppenderName(appenderConfig.getName());
        propertiesPage.setClassName(appenderConfig.getAppenderClass());

        addPage(propertiesPage);
    }

    public boolean needsPreviousAndNextButtons() {
        return true;
    }

    public boolean canFinish() {

        if (!propertiesPage.isPageComplete()) return false;

        return true;
    }

    public boolean performFinish() {
        try {
            appenderConfig.setName(propertiesPage.getAppenderName());
            appenderConfig.setAppenderClass(propertiesPage.getClassName());

            return true;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
            return false;
        }
    }

    public AppenderConfig getAppenderConfig() {
        return appenderConfig;
    }

    public void setAppenderConfig(AppenderConfig appenderConfig) {
        this.appenderConfig = appenderConfig;
    }
}