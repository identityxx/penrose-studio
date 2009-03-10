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

import org.eclipse.jface.wizard.Wizard;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.acl.wizard.ACLWizardPage;
import org.safehaus.penrose.studio.attribute.wizard.AttributesWizardPage;
import org.safehaus.penrose.studio.directory.wizard.ObjectClassWizardPage;
import org.safehaus.penrose.studio.directory.wizard.EntryDNWizardPage;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.acl.ACI;
import org.safehaus.penrose.directory.EntryConfig;
import org.safehaus.penrose.directory.DirectoryClient;
import org.safehaus.penrose.directory.EntryAttributeConfig;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.partition.PartitionClient;
import org.apache.log4j.Logger;

/**
 * @author Endi S. Dewata
 */
public class DefaultSchemaWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    public EntryDNWizardPage dnPage;
    public ObjectClassWizardPage objectClassesPage;
    public AttributesWizardPage attributesPage;
    public ACLWizardPage aclPage;

    private Server server;
    private String partitionName;

    EntryConfig entryConfig;

    public DefaultSchemaWizard() {
        setWindowTitle("New Default Schema");

        entryConfig = new EntryConfig("cn=Subschema");

        entryConfig.setEntryClass("org.safehaus.penrose.schema.directory.SchemaEntry");

        entryConfig.addObjectClass("subentry");
        entryConfig.addObjectClass("subschema");
        entryConfig.addObjectClass("extensibleObject");

        entryConfig.addAttributeConfig(new EntryAttributeConfig("cn", "Subschema", true));

        entryConfig.addACI(new ACI("rs"));
    }

    public void addPages() {
        try {
            dnPage = new EntryDNWizardPage();
            dnPage.setDn(entryConfig.getDn().toString());

            addPage(dnPage);

            objectClassesPage = new ObjectClassWizardPage();
            objectClassesPage.setServer(server);
            objectClassesPage.setSelecteObjectClasses(entryConfig.getObjectClasses());

            addPage(objectClassesPage);

            attributesPage = new AttributesWizardPage();
            attributesPage.setServer(server);
            attributesPage.setPartitionName(partitionName);
            attributesPage.setAttributeConfigs(entryConfig.getAttributeConfigs());

            addPage(attributesPage);

            aclPage = new ACLWizardPage();
            aclPage.setACL(entryConfig.getACL());

            addPage(aclPage);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public boolean canFinish() {
        if (!objectClassesPage.isPageComplete()) return false;
        if (!attributesPage.isPageComplete()) return false;
        return aclPage.isPageComplete();
    }

    public boolean performFinish() {
        try {
            entryConfig.setDn(dnPage.getDn());

            entryConfig.setObjectClasses(objectClassesPage.getSelectedObjectClasses());
            entryConfig.setAttributeConfigs(attributesPage.getAttributeConfigs());

            entryConfig.setACL(aclPage.getACL());

            PenroseClient client = server.getClient();
            PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
            PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);

            DirectoryClient directoryClient = partitionClient.getDirectoryClient();
            directoryClient.createEntry(entryConfig);

            partitionClient.store();

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