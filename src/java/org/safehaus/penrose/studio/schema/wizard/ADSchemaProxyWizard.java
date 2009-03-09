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
package org.safehaus.penrose.studio.schema.wizard;

import org.apache.log4j.Logger;
import org.eclipse.jface.wizard.Wizard;
import org.safehaus.penrose.acl.ACI;
import org.safehaus.penrose.directory.*;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.acl.wizard.ACLWizardPage;
import org.safehaus.penrose.studio.directory.wizard.EntryDNWizardPage;
import org.safehaus.penrose.studio.connection.wizard.SelectConnectionWizardPage;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.source.SourceConfig;
import org.safehaus.penrose.source.SourceManagerClient;
import org.safehaus.penrose.source.FieldConfig;

/**
 * @author Endi S. Dewata
 */
public class ADSchemaProxyWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    public SelectConnectionWizardPage connectionPage;
    public EntryDNWizardPage dnPage;
    public SchemaFormatPage formatPage;
    public ACLWizardPage aclPage;

    private Server server;
    private String partitionName;

    public ADSchemaProxyWizard() {
        setWindowTitle("New AD Schema Proxy");
    }

    public void addPages() {

        connectionPage = new SelectConnectionWizardPage();
        connectionPage.setDescription("Select an Active Directory connection.");

        connectionPage.setServer(server);
        connectionPage.setPartitionName(partitionName);
        connectionPage.setAdapterType("LDAP");

        addPage(connectionPage);

        dnPage = new EntryDNWizardPage();
        dnPage.setDn("CN=Schema,CN=Configuration,dc=AD,dc=Example,dc=com");

        addPage(dnPage);

        formatPage = new SchemaFormatPage();

        addPage(formatPage);

        aclPage = new ACLWizardPage();
        aclPage.addACI(new ACI("rs"));

        addPage(aclPage);
    }

    public boolean canFinish() {
        if (!connectionPage.isPageComplete()) return false;
        if (!dnPage.isPageComplete()) return false;
        if (!formatPage.isPageComplete()) return false;
        if (!aclPage.isPageComplete()) return false;
        return true;
    }

    public boolean needsPreviousAndNextButtons() {
        return true;
    }

    public boolean performFinish() {
        try {
            PenroseClient client = server.getClient();
            PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
            PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);

            String format = formatPage.getFormat();

            if (SchemaFormatPage.ACTIVE_DIRECTORY.equals(format)) {
                createADSchemaProxy(partitionClient);

            } else {
                createLDAPSchemaProxy(partitionClient);
            }

            partitionClient.store();

            return true;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
            return false;
        }
    }

    public void createADSchemaProxy(PartitionClient partitionClient) throws Exception {

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Source
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////

        SourceConfig sourceConfig = new SourceConfig();
        sourceConfig.setName(connectionPage.getConnectionName()+"_schema");
        sourceConfig.setConnectionName(connectionPage.getConnectionName());
        sourceConfig.setParameter("baseDn", dnPage.getDn());

        SourceManagerClient sourceManagerClient = partitionClient.getSourceManagerClient();
        sourceManagerClient.createSource(sourceConfig);

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Entry
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////

        EntryConfig entryConfig = new EntryConfig();
        entryConfig.setDn(dnPage.getDn());
        entryConfig.setEntryClass("org.safehaus.penrose.directory.ProxyEntry");

        entryConfig.removeObjectClasses();

        entryConfig.removeSourceConfigs();
        entryConfig.addSourceConfig(new EntrySourceConfig(sourceConfig.getName()));

        entryConfig.setACL(aclPage.getACL());

        DirectoryClient directoryClient = partitionClient.getDirectoryClient();
        directoryClient.createEntry(entryConfig);
    }

    public void createLDAPSchemaProxy(PartitionClient partitionClient) throws Exception {

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Source
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////

        SourceConfig sourceConfig = new SourceConfig();
        sourceConfig.setName(connectionPage.getConnectionName()+"_schema");
        sourceConfig.setConnectionName(connectionPage.getConnectionName());

        sourceConfig.addFieldConfig(new FieldConfig("lDAPDisplayName", true));
        sourceConfig.addFieldConfig(new FieldConfig("objectClass"));
        sourceConfig.addFieldConfig(new FieldConfig("attributeID"));
        sourceConfig.addFieldConfig(new FieldConfig("adminDescription"));
        sourceConfig.addFieldConfig(new FieldConfig("attributeSyntax"));
        sourceConfig.addFieldConfig(new FieldConfig("isSingleValued"));
        sourceConfig.addFieldConfig(new FieldConfig("governsID"));
        sourceConfig.addFieldConfig(new FieldConfig("mustContain"));
        sourceConfig.addFieldConfig(new FieldConfig("systemMustContain"));
        sourceConfig.addFieldConfig(new FieldConfig("mayContain"));
        sourceConfig.addFieldConfig(new FieldConfig("systemMayContain"));

        sourceConfig.setParameter("baseDn", dnPage.getDn());
        sourceConfig.setParameter("scope", "ONELEVEL");

        SourceManagerClient sourceManagerClient = partitionClient.getSourceManagerClient();
        sourceManagerClient.createSource(sourceConfig);

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Entry
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////

        EntryConfig entryConfig = new EntryConfig();
        entryConfig.setDn(dnPage.getDn());
        entryConfig.setEntryClass("org.safehaus.penrose.activeDirectory.directory.ADSchemaEntry");

        entryConfig.removeObjectClasses();
        entryConfig.addObjectClass("subentry");
        entryConfig.addObjectClass("subschema");
        entryConfig.addObjectClass("extensibleObject");

        entryConfig.addAttributesFromRdn();

        entryConfig.removeSourceConfigs();
        entryConfig.addSourceConfig(new EntrySourceConfig(sourceConfig.getName()));

        entryConfig.setACL(aclPage.getACL());

        DirectoryClient directoryClient = partitionClient.getDirectoryClient();
        directoryClient.createEntry(entryConfig);

    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public String getPartitionName() {
        return partitionName;
    }

    public void setPartitionName(String partitionName) {
        this.partitionName = partitionName;
    }
}