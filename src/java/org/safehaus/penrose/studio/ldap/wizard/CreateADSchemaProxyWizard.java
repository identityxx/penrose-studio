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
package org.safehaus.penrose.studio.ldap.wizard;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.safehaus.penrose.studio.connection.wizard.SelectConnectionWizardPage;
import org.safehaus.penrose.studio.schema.wizard.SelectSchemaWizardPage;
import org.safehaus.penrose.studio.util.ADUtil;
import org.safehaus.penrose.studio.util.SchemaUtil;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.connection.ConnectionConfig;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.partition.PartitionClient;
import org.apache.log4j.Logger;

/**
 * @author Endi S. Dewata
 */
public class CreateADSchemaProxyWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    SelectConnectionWizardPage connectionPage;
    SelectSchemaWizardPage schemaPage;

    private Server project;
    private String partitionName;

    public CreateADSchemaProxyWizard() {
        setWindowTitle("New Active Directory Schema Proxy");

        connectionPage = new SelectConnectionWizardPage();
        connectionPage.setPartitionName(partitionName);
        connectionPage.setAdapterType("LDAP");

        connectionPage.setDescription(
                "Select Active Directory connection. "+
                "The connection URL should point to the Root DSE (empty base DN)."
        );

        schemaPage = new SelectSchemaWizardPage();
    }

    public boolean canFinish() {
        if (!connectionPage.isPageComplete()) return false;
        if (!schemaPage.isPageComplete()) return false;
        return true;
    }

    public void addPages() {
        addPage(connectionPage);
        addPage(schemaPage);
    }

    public IWizardPage getNextPage(IWizardPage page) {
        if (page == connectionPage) {
            ConnectionConfig connectionConfig = connectionPage.getConnectionConfig();
            schemaPage.setConnectionConfig(connectionConfig);
        }

        return super.getNextPage(page);
    }

    public boolean performFinish() {
        try {
            PenroseClient client = project.getClient();
            PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
            PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);

            ConnectionConfig connectionConfig = connectionPage.getConnectionConfig();
            String sourceSchemaDn = schemaPage.getSourceSchemaDn();
            String destSchemaDn = schemaPage.getDestSchemaDn();
            String schemaFormat = schemaPage.getSchemaFormat();

            if (SelectSchemaWizardPage.LDAP_SCHEMA.equals(schemaFormat)) {
                ADUtil util = new ADUtil();
                util.createSchemaProxy(partitionClient, connectionConfig.getName(), sourceSchemaDn, destSchemaDn);

            } else {
                SchemaUtil util = new SchemaUtil();
                util.createSchemaProxy(partitionClient, connectionConfig.getName(), sourceSchemaDn, destSchemaDn);
            }

            partitionClient.store();

            return true;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public boolean needsPreviousAndNextButtons() {
        return true;
    }

    public Server getProject() {
        return project;
    }

    public void setProject(Server project) {
        this.project = project;
    }

    public String getPartitionName() {
        return partitionName;
    }

    public void setPartitionName(String partitionName) {
        this.partitionName = partitionName;
    }
}
