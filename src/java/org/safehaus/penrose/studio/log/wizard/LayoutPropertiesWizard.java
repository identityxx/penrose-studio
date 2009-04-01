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
import org.safehaus.penrose.log.log4j.LayoutConfig;

/**
 * @author Endi S. Dewata
 */
public class LayoutPropertiesWizard extends Wizard {

    public Logger log = Logger.getLogger(getClass());

    LayoutPropertiesWizardPage propertiesPage;

    LayoutConfig layoutConfig;

    public LayoutPropertiesWizard() {
        setWindowTitle("Layout");
    }

    public void addPages() {

        propertiesPage = new LayoutPropertiesWizardPage();
        propertiesPage.setClassName(layoutConfig.getLayoutClass());

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
            layoutConfig.setLayoutClass(propertiesPage.getClassName());

            return true;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
            return false;
        }
    }

    public LayoutConfig getLayoutConfig() {
        return layoutConfig;
    }

    public void setLayoutConfig(LayoutConfig layoutConfig) {
        this.layoutConfig = layoutConfig;
    }
}