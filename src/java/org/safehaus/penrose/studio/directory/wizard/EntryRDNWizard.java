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
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.safehaus.penrose.directory.*;
import org.safehaus.penrose.ldap.DN;
import org.safehaus.penrose.ldap.DNBuilder;
import org.safehaus.penrose.ldap.RDN;
import org.safehaus.penrose.studio.server.Server;

/**
 * @author Endi S. Dewata
 */
public class EntryRDNWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    private Server server;
    private String partitionName;
    private RDN rdn;
    private DN parentDn;
    private EntryConfig entryConfig;

    public EntryRDNWizardPage dnPage;

    public EntryRDNWizard() {
        setWindowTitle("Edit Entry DN");
    }

    public void addPages() {

        dnPage = new EntryRDNWizardPage();
        dnPage.setDescription("Enter the RDN of the entry.");
        dnPage.setServer(server);
        dnPage.setPartitionName(partitionName);
        dnPage.setRdn(rdn);
        dnPage.setParentDn(parentDn);

        addPage(dnPage);
    }

    public boolean canFinish() {
        if (!dnPage.isPageComplete()) return false;

        return true;
    }

    public IWizardPage getNextPage(IWizardPage page) {
        return super.getNextPage(page);
    }

    public boolean performFinish() {
        try {
            DNBuilder db = new DNBuilder();
            db.append(dnPage.getRdn());
            db.append(parentDn);
            //db.append(dnPage.getParentDn());
            entryConfig.setDn(db.toDn());

            return true;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public boolean needsPreviousAndNextButtons() {
        return true;
    }

    public RDN getRdn() {
        return rdn;
    }

    public DN getParentDn() {
        return parentDn;
    }

    public void setRdn(RDN rdn) {
        this.rdn = rdn;
    }

    public void setParentDn(DN parentDn) {
        this.parentDn = parentDn;
    }

    public EntryConfig getEntryConfig() {
        return entryConfig;
    }

    public void setEntryConfig(EntryConfig entryConfig) {
        this.entryConfig = entryConfig;
    }

    public String getPartitionName() {
        return partitionName;
    }

    public void setPartitionName(String partitionName) {
        this.partitionName = partitionName;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }
}