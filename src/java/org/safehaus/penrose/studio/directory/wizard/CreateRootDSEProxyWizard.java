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

import org.eclipse.jface.wizard.Wizard;
import org.safehaus.penrose.studio.connection.wizard.SelectConnectionWizardPage;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.partition.*;
import org.safehaus.penrose.mapping.*;
import org.safehaus.penrose.acl.ACI;
import org.safehaus.penrose.source.SourceConfig;
import org.safehaus.penrose.source.SourceConfigs;
import org.safehaus.penrose.connection.ConnectionConfig;
import org.safehaus.penrose.directory.DirectoryConfigs;
import org.apache.log4j.Logger;

/**
 * @author Endi S. Dewata
 */
public class CreateRootDSEProxyWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    public SelectConnectionWizardPage connectionPage;

    private Project project;
    private PartitionConfig partitionConfig;

    public CreateRootDSEProxyWizard() {
        setWindowTitle("New Root DSE Proxy");
    }

    public void addPages() {

        connectionPage = new SelectConnectionWizardPage(partitionConfig);
        connectionPage.setProject(project);

        addPage(connectionPage);
    }

    public boolean canFinish() {
        return connectionPage.isPageComplete();
    }

    public boolean performFinish() {
        try {
            ConnectionConfig connectionConfig = connectionPage.getConnectionConfig();

            SourceConfig sourceConfig = new SourceConfig();
            sourceConfig.setName(connectionConfig.getName()+" Root DSE");
            sourceConfig.setConnectionName(connectionConfig.getName());

            sourceConfig.setParameter("scope", "OBJECT");
            sourceConfig.setParameter("filter", "objectClass=*");

            SourceConfigs sourceConfigs = partitionConfig.getSourceConfigs();
            sourceConfigs.addSourceConfig(sourceConfig);

            EntryMapping entryMapping = new EntryMapping();

            SourceMapping sourceMapping = new SourceMapping("DEFAULT", sourceConfig.getName());
            entryMapping.addSourceMapping(sourceMapping);

            entryMapping.setHandlerName("PROXY");

            entryMapping.addACI(new ACI("rs"));

            DirectoryConfigs directoryConfigs = partitionConfig.getDirectoryConfigs();
            directoryConfigs.addEntryMapping(entryMapping);

            project.save(partitionConfig, sourceConfigs);
            project.save(partitionConfig, directoryConfigs);

            return true;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
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
