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
import org.safehaus.penrose.studio.config.wizard.ParametersWizardPage;
import org.safehaus.penrose.log.log4j.AppenderConfig;
import org.safehaus.penrose.log.log4j.LayoutConfig;

/**
 * @author Endi S. Dewata
 */
public class AppenderWizard extends Wizard {

    public Logger log = Logger.getLogger(getClass());

    AppenderPropertiesWizardPage propertiesPage;
    LayoutPropertiesWizardPage layoutPropertiesPage;
    ParametersWizardPage layoutParametersPage;
    ParametersWizardPage parametersPage;

    AppenderConfig appenderConfig = new AppenderConfig();
    LayoutConfig layoutConfig = new LayoutConfig();

    public AppenderWizard() {
        setWindowTitle("Appender");
    }

    public void addPages() {

        propertiesPage = new AppenderPropertiesWizardPage();
        propertiesPage.setAppenderName(appenderConfig.getName());
        propertiesPage.setClassName(appenderConfig.getAppenderClass());

        addPage(propertiesPage);

        layoutPropertiesPage = new LayoutPropertiesWizardPage();
        layoutPropertiesPage.setClassName(layoutConfig.getLayoutClass());

        addPage(layoutPropertiesPage);

        layoutParametersPage = new ParametersWizardPage();
        layoutParametersPage.setDescription("Enter the layout parameters.");
        layoutParametersPage.setParameters(layoutConfig.getParameters());

        addPage(layoutParametersPage);

        parametersPage = new ParametersWizardPage();
        parametersPage.setDescription("Enter the appender parameters.");
        parametersPage.setParameters(appenderConfig.getParameters());

        addPage(parametersPage);
    }

    public boolean needsPreviousAndNextButtons() {
        return true;
    }

    public boolean canFinish() {

        if (!propertiesPage.isPageComplete()) return false;
        if (!layoutPropertiesPage.isPageComplete()) return false;
        if (!layoutParametersPage.isPageComplete()) return false;
        if (!parametersPage.isPageComplete()) return false;

        return true;
    }

    public boolean performFinish() {
        try {
            appenderConfig.setName(propertiesPage.getAppenderName());
            appenderConfig.setAppenderClass(propertiesPage.getClassName());

            layoutConfig.setLayoutClass(layoutPropertiesPage.getClassName());
            layoutConfig.setParameters(layoutParametersPage.getParameters());

            if (layoutConfig.getLayoutClass() != null || !layoutConfig.getParameters().isEmpty()) {
                appenderConfig.setLayoutConfig(layoutConfig);
            }

            appenderConfig.setParameters(parametersPage.getParameters());

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

    public LayoutConfig getLayoutConfig() {
        return layoutConfig;
    }

    public void setLayoutConfig(LayoutConfig layoutConfig) {
        this.layoutConfig = layoutConfig;
    }
}