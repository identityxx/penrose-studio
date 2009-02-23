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
package org.safehaus.penrose.studio.ldap.connection.wizard;

import org.eclipse.jface.wizard.Wizard;
import org.safehaus.penrose.studio.ldap.connection.wizard.LDAPConnectionOptionsWizardPage;
import org.apache.log4j.Logger;

/**
 * @author Endi S. Dewata
 */
public class LDAPConnectionBrowserOptionsWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    private Long sizeLimit;

    public LDAPConnectionOptionsWizardPage optionsPage;

    public LDAPConnectionBrowserOptionsWizard() {
        setWindowTitle("Edit Connection Browser Options");
    }

    public void addPages() {

        optionsPage = new LDAPConnectionOptionsWizardPage();
        optionsPage.setDescription("Enter connection browser options.");
        optionsPage.setSizeLimit(sizeLimit);

        addPage(optionsPage);
    }

    public boolean canFinish() {
        if (!optionsPage.isPageComplete()) return false;

        return true;
    }

    public boolean performFinish() {
        try {
            sizeLimit = optionsPage.getSizeLimit();

            return true;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public boolean needsPreviousAndNextButtons() {
        return true;
    }

    public Long getSizeLimit() {
        return sizeLimit;
    }

    public void setSizeLimit(Long sizeLimit) {
        this.sizeLimit = sizeLimit;
    }
}