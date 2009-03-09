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
package org.safehaus.penrose.studio.mapping.wizard;

import org.eclipse.jface.wizard.Wizard;
import org.apache.log4j.Logger;
import org.safehaus.penrose.mapping.MappingConfig;
import org.safehaus.penrose.studio.dialog.ErrorDialog;

/**
 * @author Endi S. Dewata
 */
public class MappingPropertiesWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    private MappingConfig mappingConfig;

    public MappingPropertiesWizardPage propertiesPage;

    public MappingPropertiesWizard() {
        setWindowTitle("Edit Mapping Properties");
    }

    public void addPages() {

        propertiesPage = new MappingPropertiesWizardPage();
        propertiesPage.setMappingName(mappingConfig.getName());
        propertiesPage.setClassName(mappingConfig.getMappingClass());
        propertiesPage.setEnabled(mappingConfig.isEnabled());
        propertiesPage.setMappingDescription(mappingConfig.getDescription());

        addPage(propertiesPage);
    }

    public boolean canFinish() {
        if (!propertiesPage.isPageComplete()) return false;

        return true;
    }

    public boolean performFinish() {
        try {
            mappingConfig.setName(propertiesPage.getMappingName());
            mappingConfig.setMappingClass(propertiesPage.getClassName());
            mappingConfig.setEnabled(propertiesPage.isEnabled());
            mappingConfig.setDescription(propertiesPage.getMappingDescription());

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

    public MappingConfig getMappingConfig() {
        return mappingConfig;
    }

    public void setMappingConfig(MappingConfig mappingConfig) {
        this.mappingConfig = mappingConfig;
    }
}