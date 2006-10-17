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
package org.safehaus.penrose.studio.module.wizard;

import org.eclipse.jface.wizard.Wizard;
import org.safehaus.penrose.module.ModuleConfig;
import org.safehaus.penrose.module.ModuleMapping;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.partition.Partition;
import org.apache.log4j.Logger;

import java.util.Iterator;
import java.util.Map;
import java.util.Collection;

/**
 * @author Endi S. Dewata
 */
public class ModuleWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    Partition partition;
    ModuleConfig module;

    public ModuleWizardPage propertyPage = new ModuleWizardPage();
    public ModuleParameterWizardPage parameterPage = new ModuleParameterWizardPage();
    public ModuleMappingWizardPage mappingPage = new ModuleMappingWizardPage();

    public ModuleWizard(Partition partition) {
        this.partition = partition;
        setWindowTitle("New Module");
    }

    public boolean canFinish() {
        if (!propertyPage.isPageComplete()) return false;
        if (!parameterPage.isPageComplete()) return false;
        if (!mappingPage.isPageComplete()) return false;

        return true;
    }

    public boolean performFinish() {
        try {
            System.out.println("[ModuleWizard] performFinish");

            module = new ModuleConfig();
            module.setName(propertyPage.getModuleName());
            module.setModuleClass(propertyPage.getModuleClass());

            Map parameters = parameterPage.getParameters();
            for (Iterator i=parameters.keySet().iterator(); i.hasNext(); ) {
                String name = (String)i.next();
                module.setParameter(name, (String)parameters.get(name));
            }

            partition.addModuleConfig(module);

            Collection mappings = mappingPage.getMappings();
            for (Iterator i=mappings.iterator(); i.hasNext(); ) {
                ModuleMapping mapping = (ModuleMapping)i.next();
                mapping.setModuleName(propertyPage.getModuleName());
                partition.addModuleMapping(mapping);
            }

            PenroseStudio penroseStudio = PenroseStudio.getInstance();
            penroseStudio.fireChangeEvent();

            return true;

        } catch (Exception e) {
            log.debug(e.getMessage(), e);
            return false;
        }
    }

    public void addPages() {
        addPage(propertyPage);
        addPage(parameterPage);
        addPage(mappingPage);
    }

    public boolean needsPreviousAndNextButtons() {
        return true;
    }

}
