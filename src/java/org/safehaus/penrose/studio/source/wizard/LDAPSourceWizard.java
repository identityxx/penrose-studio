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
package org.safehaus.penrose.studio.source.wizard;

import org.eclipse.jface.wizard.IWizardPage;
import org.safehaus.penrose.source.SourceConfig;
import org.safehaus.penrose.source.FieldConfig;
import org.safehaus.penrose.studio.source.wizard.LDAPTreeWizardPage;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author Endi S. Dewata
 */
public class LDAPSourceWizard extends SourceWizard {

    public SourceWizardPage propertyPage;
    public LDAPTreeWizardPage ldapTreePage;
    public LDAPAttributeWizardPage ldapAttributesPage;
    public LDAPFieldWizardPage ldapFieldsPage;

    public LDAPSourceWizard() throws Exception {
        propertyPage = new SourceWizardPage();
        ldapTreePage = new LDAPTreeWizardPage();
        ldapAttributesPage = new LDAPAttributeWizardPage();
        ldapFieldsPage = new LDAPFieldWizardPage();

        setWindowTitle("New Source");
    }

    public boolean canFinish() {
        if (!propertyPage.isPageComplete()) return false;

        if (!ldapTreePage.isPageComplete()) return false;
        if (!ldapAttributesPage.isPageComplete()) return false;
        if (!ldapFieldsPage.isPageComplete()) return false;

        return true;
    }

    public void addPages() {
        addPage(propertyPage);
        addPage(ldapTreePage);
        addPage(ldapAttributesPage);
        addPage(ldapFieldsPage);
    }

    public IWizardPage getNextPage(IWizardPage page) {
        if (propertyPage == page) {
            ldapTreePage.setConnectionConfig(partition, connectionConfig);

        } else if (ldapTreePage == page) {
            ldapAttributesPage.setConnectionConfig(partition, connectionConfig);

        } else if (ldapAttributesPage == page) {
            Collection attributeTypes = ldapAttributesPage.getAttributeTypes();
            ldapFieldsPage.setAttributeTypes(attributeTypes);
        }

        return super.getNextPage(page);
    }

    public boolean performFinish() {
        try {
            sourceConfig = new SourceConfig();
            sourceConfig.setName(propertyPage.getSourceName());
            sourceConfig.setConnectionName(connectionConfig.getName());

            sourceConfig.setParameter("baseDn", ldapTreePage.getBaseDn());
            sourceConfig.setParameter("filter", ldapTreePage.getFilter());
            sourceConfig.setParameter("scope", ldapTreePage.getScope());
            sourceConfig.setParameter("objectClasses", ldapTreePage.getObjectClasses());

            Collection fields = ldapFieldsPage.getFields();
            for (Iterator i=fields.iterator(); i.hasNext(); ) {
                FieldConfig field = (FieldConfig)i.next();
                sourceConfig.addFieldConfig(field);
            }

            partition.addSourceConfig(sourceConfig);

            return true;

        } catch (Exception e) {
            log.debug(e.getMessage(), e);
            return false;
        }
    }

    public boolean needsPreviousAndNextButtons() {
        return true;
    }
}
