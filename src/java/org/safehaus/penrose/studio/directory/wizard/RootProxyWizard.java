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
import org.eclipse.jface.wizard.Wizard;
import org.safehaus.penrose.directory.EntryConfig;
import org.safehaus.penrose.directory.EntrySourceConfig;
import org.safehaus.penrose.directory.DirectoryClient;
import org.safehaus.penrose.ldap.DN;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.source.wizard.SelectSourceWizardPage;
import org.safehaus.penrose.studio.acl.wizard.ACLWizardPage;
import org.safehaus.penrose.acl.ACI;

/**
 * @author Endi S. Dewata
 */
public class RootProxyWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    public EntryDNWizardPage dnPage;
    public SelectSourceWizardPage sourcePage;
    public ACLWizardPage aclPage;

    private Server server;
    private String partitionName;
    private DN parentDn;

    public RootProxyWizard() {
        setWindowTitle("New Root Proxy");
    }

    public void addPages() {

        dnPage = new EntryDNWizardPage();

        addPage(dnPage);

        sourcePage = new SelectSourceWizardPage();
        sourcePage.setServer(server);
        sourcePage.setPartitionName(partitionName);

        addPage(sourcePage);

        aclPage = new ACLWizardPage();
        aclPage.addACI(new ACI("rs"));

        addPage(aclPage);
    }

    public boolean canFinish() {
        if (!dnPage.isPageComplete()) return false;
        if (!sourcePage.isPageComplete()) return false;
        if (!aclPage.isPageComplete()) return false;
        return true;
    }

    public boolean performFinish() {
        try {
            PenroseClient client = server.getClient();
            PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
            PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);

            EntryConfig entryConfig = new EntryConfig(dnPage.getDn());
            entryConfig.setEntryClass("org.safehaus.penrose.directory.ProxyEntry");

            entryConfig.addSourceConfig(new EntrySourceConfig(sourcePage.getSourceName()));

            entryConfig.setACL(aclPage.getACL());

            DirectoryClient directoryClient = partitionClient.getDirectoryClient();
            directoryClient.createEntry(entryConfig);

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

    public DN getParentDn() {
        return parentDn;
    }

    public void setParentDn(DN parentDn) {
        this.parentDn = parentDn;
    }
}
