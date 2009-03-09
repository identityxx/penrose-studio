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
package org.safehaus.penrose.studio.directory.wizard;

import org.apache.log4j.Logger;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.safehaus.penrose.directory.*;
import org.safehaus.penrose.studio.dialog.ErrorDialog;

/**
 * @author Endi S. Dewata
 */
public class EntryPropertiesWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    private EntryConfig entryConfig;

    public EntryPropertiesWizardPage propertiesPage;

    public EntryPropertiesWizard() {
        setWindowTitle("Edit Entry Properties");
    }

    public void addPages() {

        propertiesPage = new EntryPropertiesWizardPage();

        propertiesPage.setEntryName(entryConfig.getName());
        propertiesPage.setClassName(entryConfig.getEntryClass());
        propertiesPage.setEnabled(entryConfig.isEnabled());
        propertiesPage.setEntryDescription(entryConfig.getDescription());

        addPage(propertiesPage);
    }

    public boolean canFinish() {
        if (!propertiesPage.isPageComplete()) return false;

        return true;
    }

    public IWizardPage getNextPage(IWizardPage page) {
        return super.getNextPage(page);
    }

    public boolean performFinish() {
        try {
            entryConfig.setName(propertiesPage.getEntryName());
            entryConfig.setEntryClass(propertiesPage.getClassName());
            entryConfig.setEnabled(propertiesPage.isEnabled());
            entryConfig.setDescription(propertiesPage.getEntryDescription());

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

    public EntryConfig getEntryConfig() {
        return entryConfig;
    }

    public void setEntryConfig(EntryConfig entryConfig) {
        this.entryConfig = entryConfig;
    }
}