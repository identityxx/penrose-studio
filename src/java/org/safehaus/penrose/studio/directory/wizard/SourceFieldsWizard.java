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
package org.safehaus.penrose.studio.directory.wizard;

import org.apache.log4j.Logger;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.safehaus.penrose.directory.*;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.attribute.wizard.AttributesWizardPage;
import org.safehaus.penrose.studio.dialog.ErrorDialog;

/**
 * @author Endi S. Dewata
 */
public class SourceFieldsWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    private Server server;
    private String partitionName;
    private EntryConfig entryConfig;
    private EntrySourceConfig sourceConfig;

    public SourceFieldsWizardPage fieldsPage;

    public SourceFieldsWizard() {
        setWindowTitle("Edit Source Fields");
    }

    public void addPages() {

        fieldsPage = new SourceFieldsWizardPage();
        fieldsPage.setDescription("Enter the fields of the source.");
        fieldsPage.setServer(server);
        fieldsPage.setPartitionName(partitionName);

        fieldsPage.setAttributeConfigs(entryConfig.getAttributeConfigs());
        fieldsPage.setSourceConfig(sourceConfig);
        fieldsPage.setDefaultType(AttributesWizardPage.VARIABLE);

        addPage(fieldsPage);
    }

    public boolean canFinish() {
        if (!fieldsPage.isPageComplete()) return false;

        return true;
    }

    public IWizardPage getNextPage(IWizardPage page) {
        return super.getNextPage(page);
    }

    public boolean performFinish() {
        try {
            sourceConfig.setFieldConfigs(fieldsPage.getFieldConfigs());

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

    public EntryConfig getEntryConfig() {
        return entryConfig;
    }

    public void setEntryConfig(EntryConfig entryConfig) {
        this.entryConfig = entryConfig;
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

    public EntrySourceConfig getSourceConfig() {
        return sourceConfig;
    }

    public void setSourceConfig(EntrySourceConfig sourceConfig) {
        this.sourceConfig = sourceConfig;
    }
}