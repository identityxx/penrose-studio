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
package org.safehaus.penrose.studio.jndi.source;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.safehaus.penrose.source.SourceConfig;
import org.safehaus.penrose.connection.ConnectionConfig;
import org.safehaus.penrose.source.FieldConfig;
import org.safehaus.penrose.source.SourceConfigs;
import org.safehaus.penrose.partition.PartitionConfig;
import org.safehaus.penrose.studio.source.wizard.SourceWizardPage;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.schema.AttributeType;
import org.apache.log4j.Logger;

import java.util.Collection;

/**
 * @author Endi S. Dewata
 */
public class JNDISourceWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    private Project project;
    private PartitionConfig partitionConfig;
    private ConnectionConfig connectionConfig;
    private SourceConfig sourceConfig;

    public SourceWizardPage propertyPage;
    public JNDITreeWizardPage jndiTreePage;
    public JNDIAttributeWizardPage jndiAttributesPage;
    public JNDIFieldWizardPage jndiFieldsPage;

    public JNDISourceWizard(PartitionConfig partitionConfig, ConnectionConfig connectionConfig) throws Exception {
        this.partitionConfig = partitionConfig;
        this.connectionConfig = connectionConfig;

        propertyPage = new SourceWizardPage();

        jndiTreePage = new JNDITreeWizardPage();
        jndiAttributesPage = new JNDIAttributeWizardPage();
        jndiFieldsPage = new JNDIFieldWizardPage();

        setWindowTitle(connectionConfig.getName()+" - New Source");
    }

    public boolean canFinish() {
        if (!propertyPage.isPageComplete()) return false;

        if (!jndiTreePage.isPageComplete()) return false;
        if (!jndiAttributesPage.isPageComplete()) return false;
        if (!jndiFieldsPage.isPageComplete()) return false;

        return true;
    }

    public void addPages() {
        addPage(propertyPage);
        addPage(jndiTreePage);
        addPage(jndiAttributesPage);
        addPage(jndiFieldsPage);
    }

    public IWizardPage getNextPage(IWizardPage page) {
        if (propertyPage == page) {
            jndiTreePage.setConnectionConfig(connectionConfig);

        } else if (jndiTreePage == page) {
            jndiAttributesPage.setConnectionConfig(connectionConfig);

        } else if (jndiAttributesPage == page) {
            Collection<AttributeType> attributeTypes = jndiAttributesPage.getAttributeTypes();
            jndiFieldsPage.setAttributeTypes(attributeTypes);
        }

        return super.getNextPage(page);
    }

    public boolean performFinish() {
        try {
            sourceConfig = new SourceConfig();
            sourceConfig.setName(propertyPage.getSourceName());
            sourceConfig.setConnectionName(connectionConfig.getName());

            sourceConfig.setParameter("baseDn", jndiTreePage.getBaseDn());
            sourceConfig.setParameter("filter", jndiTreePage.getFilter());
            sourceConfig.setParameter("scope", jndiTreePage.getScope());
            sourceConfig.setParameter("objectClasses", jndiTreePage.getObjectClasses());

            Collection<FieldConfig> fields = jndiFieldsPage.getFields();
            for (FieldConfig field : fields) {
                sourceConfig.addFieldConfig(field);
            }

            SourceConfigs sourceConfigs = partitionConfig.getSourceConfigs();
            sourceConfigs.addSourceConfig(sourceConfig);
            project.save(partitionConfig, sourceConfigs);

            return true;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    public SourceConfig getSourceConfig() {
        return sourceConfig;
    }

    public void setSourceConfig(SourceConfig connection) {
        this.sourceConfig = connection;
    }

    public boolean needsPreviousAndNextButtons() {
        return true;
    }

    public ConnectionConfig getConnectionConfig() {
        return connectionConfig;
    }

    public void setConnectionConfig(ConnectionConfig connectionConfig) {
        this.connectionConfig = connectionConfig;
    }

    public PartitionConfig getPartitionConfig() {
        return partitionConfig;
    }

    public void setPartitionConfig(PartitionConfig partitionConfig) {
        this.partitionConfig = partitionConfig;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
