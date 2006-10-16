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
import org.eclipse.jface.wizard.IWizardPage;
import org.safehaus.penrose.partition.SourceConfig;
import org.safehaus.penrose.partition.ConnectionConfig;
import org.safehaus.penrose.partition.FieldConfig;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.studio.source.wizard.JNDITreeWizardPage;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author Endi S. Dewata
 */
public class JNDISourceWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    private Partition partition;
    private ConnectionConfig connectionConfig;
    private SourceConfig sourceConfig;

    public SourceWizardPage propertyPage;
    public JNDITreeWizardPage jndiTreePage;
    public JNDIAttributeWizardPage jndiAttributesPage;
    public JNDIFieldWizardPage jndiFieldsPage;

    public JNDISourceWizard(Partition partition, ConnectionConfig connectionConfig) throws Exception {
        this.partition = partition;
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
            jndiTreePage.setConnectionConfig(partition, connectionConfig);

        } else if (jndiTreePage == page) {
            jndiAttributesPage.setConnectionConfig(partition, connectionConfig);

        } else if (jndiAttributesPage == page) {
            Collection attributeTypes = jndiAttributesPage.getAttributeTypes();
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

            Collection fields = jndiFieldsPage.getFields();
            for (Iterator i=fields.iterator(); i.hasNext(); ) {
                FieldConfig field = (FieldConfig)i.next();
                sourceConfig.addFieldConfig(field);
            }

            partition.addSourceConfig(sourceConfig);

            return true;

        } catch (Exception e) {
            log.debug(e.getMessage(), e);
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

    public Partition getPartition() {
        return partition;
    }

    public void setPartition(Partition partition) {
        this.partition = partition;
    }
}
