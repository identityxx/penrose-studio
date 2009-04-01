/**
 * Copyright 2009 Red Hat, Inc.
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
import org.safehaus.penrose.schema.AttributeType;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.apache.log4j.Logger;

/**
 * @author Endi S. Dewata
 */
public class AttributeTypeWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    AttributeTypePropertiesWizardPage propertiesPage;
    AttributeTypeOptionsWizardPage optionsPage;

    AttributeType attributeType;

	public AttributeTypeWizard() {
    }

    public void addPages() {

        propertiesPage = new AttributeTypePropertiesWizardPage();

        propertiesPage.setOid(attributeType.getOid());
        propertiesPage.setNames(attributeType.getNames());
        propertiesPage.setAttributeTypeDescription(attributeType.getDescription());

        propertiesPage.setSuperClass(attributeType.getSuperClass());
        propertiesPage.setEquality(attributeType.getEquality());
        propertiesPage.setOrdering(attributeType.getOrdering());
        propertiesPage.setSubstring(attributeType.getSubstring());
        propertiesPage.setSyntax(attributeType.getSyntax());

        addPage(propertiesPage);

        optionsPage = new AttributeTypeOptionsWizardPage();

        optionsPage.setUsage(attributeType.getUsage());
        optionsPage.setSingleValued(attributeType.isSingleValued());
        optionsPage.setCollective(attributeType.isCollective());
        optionsPage.setModifiable(attributeType.isModifiable());
        optionsPage.setObsolete(attributeType.isObsolete());

        addPage(optionsPage);
    }

    public boolean canFinish() {

        if (!propertiesPage.isPageComplete()) return false;
        if (!optionsPage.isPageComplete()) return false;

        return true;
    }
    
    public boolean performFinish() {
        try {
            attributeType.setOid(propertiesPage.getOid());
            attributeType.setNames(propertiesPage.getNames());
            attributeType.setDescription(propertiesPage.getAttributeTypeDescription());

            attributeType.setSuperClass(propertiesPage.getSuperClass());
            attributeType.setEquality(propertiesPage.getEquality());
            attributeType.setOrdering(propertiesPage.getOrdering());
            attributeType.setSubstring(propertiesPage.getSubstring());
            attributeType.setSyntax(propertiesPage.getSyntax());

            attributeType.setUsage(optionsPage.getUsage());
            attributeType.setSingleValued(optionsPage.isSingleValued());
            attributeType.setCollective(optionsPage.isCollective());
            attributeType.setModifiable(optionsPage.isModifiable());
            attributeType.setObsolete(optionsPage.isObsolete());

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

    public AttributeType getAttributeType() {
        return attributeType;
    }

    public void setAttributeType(AttributeType attributeType) {
        this.attributeType = attributeType;
    }
}