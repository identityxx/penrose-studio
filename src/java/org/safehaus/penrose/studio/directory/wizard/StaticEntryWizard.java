/**
 * Copyright (c) 2000-2005, Identyx Corporation.
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
import org.safehaus.penrose.studio.mapping.wizard.StaticEntryRDNWizardPage;
import org.safehaus.penrose.util.EntryUtil;
import org.apache.log4j.Logger;

import java.util.Collection;

/**
 * @author Endi S. Dewata
 */
public class StaticEntryWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    private Partition partition;
    private EntryMapping parentMapping;
    private EntryMapping entryMapping;

    public StaticEntryRDNWizardPage rdnPage;
    public ObjectClassWizardPage ocPage;
    public AttributeValueWizardPage attrPage;

    public StaticEntryWizard(Partition partition, EntryMapping parentMapping) {
        this.partition = partition;
        this.parentMapping = parentMapping;
        setWindowTitle("Adding static entry");

        rdnPage = new StaticEntryRDNWizardPage(partition, parentMapping);
        ocPage = new ObjectClassWizardPage();
        attrPage = new AttributeValueWizardPage(partition);
    }

    public boolean canFinish() {
        if (!rdnPage.isPageComplete()) return false;
        if (!ocPage.isPageComplete()) return false;
        if (!attrPage.isPageComplete()) return false;
        return true;
    }

    public void addPages() {
        addPage(rdnPage);
        addPage(ocPage);
        addPage(attrPage);
    }

    public IWizardPage getNextPage(IWizardPage page) {
        if (rdnPage == page) {
            String rdn = rdnPage.getRdn();
            attrPage.setRdn(EntryUtil.getRdn(rdn));

        } else if (ocPage == page) {
            Collection objectClasses = ocPage.getSelectedObjectClasses();
            attrPage.setObjectClasses(objectClasses);
        }

        return super.getNextPage(page);
    }

    public boolean performFinish() {
        try {
            entryMapping = new EntryMapping();
            entryMapping.setRdn(rdnPage.getRdn());
            entryMapping.setParentDn(rdnPage.getParentDn());
            entryMapping.addObjectClasses(ocPage.getSelectedObjectClasses());
            entryMapping.addAttributeMappings(attrPage.getAttributeMappings());

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

    public EntryMapping getParentMapping() {
        return parentMapping;
    }

    public void setParentMapping(EntryMapping parentMapping) {
        this.parentMapping = parentMapping;
    }
}
