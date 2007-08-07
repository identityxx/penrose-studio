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
import org.safehaus.penrose.studio.source.wizard.SelectSourceWizardPage;
import org.safehaus.penrose.partition.*;
import org.safehaus.penrose.mapping.*;
import org.safehaus.penrose.ldap.RDN;
import org.safehaus.penrose.ldap.DN;
import org.safehaus.penrose.ldap.DNBuilder;
import org.safehaus.penrose.source.SourceConfig;
import org.safehaus.penrose.connection.ConnectionConfig;
import org.apache.log4j.Logger;

import javax.naming.Context;

/**
 * @author Endi S. Dewata
 */
public class CreateLDAPProxyWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    public SelectSourceWizardPage sourcePage;

    PartitionConfig partitionConfig;
    EntryMapping parentMapping;

    public CreateLDAPProxyWizard(PartitionConfig partition) {
        this(partition, null);
    }

    public CreateLDAPProxyWizard(PartitionConfig partition, EntryMapping parentMapping) {
        this.partitionConfig = partition;
        this.parentMapping = parentMapping;

        sourcePage = new SelectSourceWizardPage(partition);

        setWindowTitle("New LDAP Proxy");
    }

    public boolean canFinish() {
        if (!sourcePage.isPageComplete()) return false;
        return true;
    }

    public boolean performFinish() {
        try {
            SourceConfig sourceConfig = sourcePage.getSourceConfig();
            ConnectionConfig connectionConfig = partitionConfig.getConnectionConfigs().getConnectionConfig(sourceConfig.getConnectionName());

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

            EntryMapping entryMapping = new EntryMapping();
            if (parentMapping == null) {
                entryMapping.setDn(dn);

            } else {
                RDN rdn = dn.getRdn();

                db.clear();
                db.append(rdn);
                db.append(parentMapping.getDn());

                entryMapping.setDn(db.toDn());
            }

            SourceMapping sourceMapping = new SourceMapping("DEFAULT", sourceConfig.getName());
            entryMapping.addSourceMapping(sourceMapping);

            entryMapping.setHandlerName("PROXY");

            partitionConfig.getDirectoryConfigs().addEntryMapping(entryMapping);

            return true;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    public void addPages() {
        addPage(sourcePage);
    }

    public boolean needsPreviousAndNextButtons() {
        return true;
    }
}
