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
import org.safehaus.penrose.util.EntryUtil;
import org.safehaus.penrose.connection.ConnectionConfig;
import org.safehaus.penrose.source.SourceConfig;
import org.apache.log4j.Logger;

import javax.naming.Context;

/**
 * @author Endi S. Dewata
 */
public class CreateLDAPProxyWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    public SelectSourceWizardPage sourcePage;

    Partition partition;
    EntryMapping parentMapping;

    public CreateLDAPProxyWizard(Partition partition) {
        this(partition, null);
    }

    public CreateLDAPProxyWizard(Partition partition, EntryMapping parentMapping) {
        this.partition = partition;
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
            ConnectionConfig connectionConfig = partition.getConnectionConfig(sourceConfig.getConnectionName());

            String url = (String)connectionConfig.getParameter(Context.PROVIDER_URL);

            int index = url.indexOf("://");
            index = url.indexOf("/", index+3);

            String suffix = "";
            if (index >= 0) {
                suffix = url.substring(index+1);
            }

            String baseDn = sourceConfig.getParameter("baseDn");
            String dn = EntryUtil.append(baseDn, suffix);

            log.debug("DN: "+dn);

            EntryMapping entryMapping = new EntryMapping();
            if (parentMapping == null) {
                entryMapping.setDn(dn);

            } else {
                Row rdn = EntryUtil.getRdn(dn);
                entryMapping.setRdn(rdn.toString());
                entryMapping.setParentDn(parentMapping.getDn());
            }

            SourceMapping sourceMapping = new SourceMapping("DEFAULT", sourceConfig.getName());
            sourceMapping.setProxy(true);
            entryMapping.addSourceMapping(sourceMapping);

            partition.addEntryMapping(entryMapping);

            return true;

        } catch (Exception e) {
            log.debug(e.getMessage(), e);
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
