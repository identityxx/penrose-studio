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
import org.eclipse.jface.wizard.IWizardPage;
import org.safehaus.penrose.mapping.EntryMapping;
import org.safehaus.penrose.mapping.Row;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.studio.mapping.wizard.ObjectClassWizardPage;
import org.safehaus.penrose.studio.mapping.wizard.AttributeValueWizardPage;
import org.safehaus.penrose.studio.mapping.wizard.StaticEntryDNWizardPage;
import org.safehaus.penrose.util.EntryUtil;
import org.safehaus.penrose.acl.ACI;
import org.apache.log4j.Logger;

import java.util.Collection;

/**
 * @author Endi S. Dewata
 */
public class RootEntryWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    private Partition partition;
    private EntryMapping entryMapping;

    public StaticEntryDNWizardPage dnPage;
    public ObjectClassWizardPage ocPage;
    public AttributeValueWizardPage attrPage;

    public RootEntryWizard(Partition partition) {
        this.partition = partition;

        dnPage = new StaticEntryDNWizardPage(partition);
        ocPage = new ObjectClassWizardPage();
        attrPage = new AttributeValueWizardPage(partition);
        
        setWindowTitle("Adding root entry");
    }

    public boolean canFinish() {
        if (!dnPage.isPageComplete()) return false;
        if (!ocPage.isPageComplete()) return false;
        if (!attrPage.isPageComplete()) return false;
        return true;
    }

    public void addPages() {
        addPage(dnPage);
        addPage(ocPage);
        addPage(attrPage);
    }

    public IWizardPage getNextPage(IWizardPage page) {
        if (ocPage == page) {
            Collection objectClasses = ocPage.getSelectedObjectClasses();
            attrPage.setObjectClasses(objectClasses);

            if (!objectClasses.isEmpty()) {
                String dn = dnPage.getDn();
                Row rdn = EntryUtil.getRdn(dn);
                attrPage.setRdn(rdn);
            }
        }

        return super.getNextPage(page);
    }

    public boolean performFinish() {
        try {
            entryMapping = new EntryMapping();
            entryMapping.setDn(dnPage.getDn());
            entryMapping.addObjectClasses(ocPage.getSelectedObjectClasses());
            entryMapping.addAttributeMappings(attrPage.getAttributeMappings());

            entryMapping.addACI(new ACI("rs"));

            partition.addEntryMapping(entryMapping);

            return true;

        } catch (Exception e) {
            log.debug(e.getMessage(), e);
            return false;
        }
    }

    public boolean needsPreviousAndNextButtons() {
        return true;
    }

    public EntryMapping getEntryMapping() {
        return entryMapping;
    }

    public void setEntryMapping(EntryMapping entryMapping) {
        this.entryMapping = entryMapping;
    }

    public Partition getPartition() {
        return partition;
    }

    public void setPartition(Partition partition) {
        this.partition = partition;
    }
}
