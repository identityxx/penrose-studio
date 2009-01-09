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
import org.safehaus.penrose.connection.ConnectionConfig;
import org.safehaus.penrose.directory.EntryConfig;
import org.safehaus.penrose.directory.ProxyEntry;
import org.safehaus.penrose.directory.EntrySourceConfig;
import org.safehaus.penrose.directory.DirectoryClient;
import org.safehaus.penrose.ldap.DN;
import org.safehaus.penrose.ldap.DNBuilder;
import org.safehaus.penrose.ldap.RDN;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.connection.ConnectionClient;
import org.safehaus.penrose.connection.ConnectionManagerClient;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.source.SourceConfig;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.source.wizard.SelectSourceWizardPage;

import javax.naming.Context;

/**
 * @author Endi S. Dewata
 */
public class CreateLDAPProxyWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    public SelectSourceWizardPage sourcePage;

    private Server project;
    private String partitionName;

    private DN parentDn;

    public CreateLDAPProxyWizard(String partitionName) {
        this(partitionName, null);
    }

    public CreateLDAPProxyWizard(String partitionName, DN parentDn) {
        this.partitionName = partitionName;
        this.parentDn = parentDn;

        setWindowTitle("New LDAP Proxy");
    }

    public void addPages() {
        sourcePage = new SelectSourceWizardPage(partitionName);

        addPage(sourcePage);
    }

    public boolean canFinish() {
        if (!sourcePage.isPageComplete()) return false;
        return true;
    }

    public boolean performFinish() {
        try {
            SourceConfig sourceConfig = sourcePage.getSourceConfig();

            PenroseClient client = project.getClient();
            PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
            PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
            ConnectionManagerClient connectionManagerClient = partitionClient.getConnectionManagerClient();
            DirectoryClient directoryClient = partitionClient.getDirectoryClient();

            ConnectionClient connectionClient = connectionManagerClient.getConnectionClient(sourceConfig.getConnectionName());
            ConnectionConfig connectionConfig = connectionClient.getConnectionConfig();

            String url = connectionConfig.getParameter(Context.PROVIDER_URL);

            int index = url.indexOf("://");
            index = url.indexOf("/", index+3);

            String suffix = "";
            if (index >= 0) {
                suffix = url.substring(index+1);
            }

            DN baseDn = new DN(sourceConfig.getParameter("baseDn"));

            DNBuilder db = new DNBuilder();
            db.append(baseDn);
            db.append(suffix);
            DN dn = db.toDn();

            log.debug("DN: "+dn);

            EntryConfig entryConfig = new EntryConfig();
            if (parentDn == null) {
                entryConfig.setDn(dn);

            } else {
                RDN rdn = dn.getRdn();

                db.clear();
                db.append(rdn);
                db.append(parentDn);

                entryConfig.setDn(db.toDn());
            }

            EntrySourceConfig sourceMapping = new EntrySourceConfig("DEFAULT", sourceConfig.getName());
            entryConfig.addSourceConfig(sourceMapping);

            entryConfig.setEntryClass(ProxyEntry.class.getName());
/*
            DirectoryConfig directoryConfig = partitionConfig.getDirectoryConfig();
            directoryConfig.addEntryConfig(entryConfig);
            project.save(partitionConfig, directoryConfig);
*/
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

    public DN getParentDn() {
        return parentDn;
    }

    public void setParentDn(DN parentDn) {
        this.parentDn = parentDn;
    }
}
