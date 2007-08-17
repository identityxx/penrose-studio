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
import org.safehaus.penrose.module.ModuleConfigs;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.partition.PartitionConfig;
import org.apache.log4j.Logger;

import java.util.Iterator;
import java.util.Map;
import java.util.Collection;

/**
 * @author Endi S. Dewata
 */
public class ModuleWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    private Project project;
    private PartitionConfig partitionConfig;
    ModuleConfig module;

    public ModuleWizardPage propertyPage = new ModuleWizardPage();
    public ModuleParameterWizardPage parameterPage = new ModuleParameterWizardPage();
    public ModuleMappingWizardPage mappingPage = new ModuleMappingWizardPage();

    public ModuleWizard() {
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
            ModuleConfigs moduleConfigs = partitionConfig.getModuleConfigs();

            module = new ModuleConfig();
            module.setName(propertyPage.getModuleName());
            module.setModuleClass(propertyPage.getModuleClass());

            Map parameters = parameterPage.getParameters();
            for (Iterator i=parameters.keySet().iterator(); i.hasNext(); ) {
                String name = (String)i.next();
                module.setParameter(name, (String)parameters.get(name));
            }

            moduleConfigs.addModuleConfig(module);

            Collection mappings = mappingPage.getMappings();
            for (Iterator i=mappings.iterator(); i.hasNext(); ) {
                ModuleMapping mapping = (ModuleMapping)i.next();
                mapping.setModuleName(propertyPage.getModuleName());
                moduleConfigs.addModuleMapping(mapping);
            }

            project.save(partitionConfig, moduleConfigs);
            
            PenroseStudio penroseStudio = PenroseStudio.getInstance();
            penroseStudio.notifyChangeListeners();

            return true;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
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

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public PartitionConfig getPartitionConfig() {
        return partitionConfig;
    }

    public void setPartitionConfig(PartitionConfig partitionConfig) {
        this.partitionConfig = partitionConfig;
    }
}
