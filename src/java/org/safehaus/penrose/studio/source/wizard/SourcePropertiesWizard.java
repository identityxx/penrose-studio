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
package org.safehaus.penrose.studio.source.wizard;

import org.eclipse.jface.wizard.Wizard;
import org.safehaus.penrose.source.SourceConfig;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.apache.log4j.Logger;

/**
 * @author Endi S. Dewata
 */
public class SourcePropertiesWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    private SourceConfig sourceConfig;

    public SourcePropertiesWizardPage propertiesPage;

    public SourcePropertiesWizard() {
        setWindowTitle("Edit Source Properties");
    }

    public void addPages() {

        propertiesPage = new SourcePropertiesWizardPage();

        propertiesPage.setSourceName(sourceConfig.getName());
        propertiesPage.setClassName(sourceConfig.getSourceClass());
        propertiesPage.setEnabled(sourceConfig.isEnabled());
        propertiesPage.setSourceDescription(sourceConfig.getDescription());

        addPage(propertiesPage);
    }

    public boolean canFinish() {
        if (!propertiesPage.isPageComplete()) return false;

        return true;
    }

    public boolean performFinish() {
        try {
            sourceConfig.setName(propertiesPage.getSourceName());
            sourceConfig.setSourceClass(propertiesPage.getClassName());
            sourceConfig.setEnabled(propertiesPage.isEnabled());
            sourceConfig.setDescription(propertiesPage.getSourceDescription());

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

    public SourceConfig getSourceConfig() {
        return sourceConfig;
    }

    public void setSourceConfig(SourceConfig sourceConfig) {
        this.sourceConfig = sourceConfig;
    }
}