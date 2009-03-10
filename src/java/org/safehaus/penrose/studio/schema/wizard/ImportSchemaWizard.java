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
package org.safehaus.penrose.studio.schema.wizard;

import org.apache.log4j.Logger;
import org.eclipse.jface.wizard.Wizard;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.schema.SchemaManagerClient;
import org.safehaus.penrose.schema.Schema;
import org.safehaus.penrose.schema.SchemaReader;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.dialog.ErrorDialog;

import java.io.File;

/**
 * @author Endi S. Dewata
 */
public class ImportSchemaWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    private Server server;

    public SchemaNameWizardPage namePage = new SchemaNameWizardPage();
    public SchemaFileWizardPage filePage = new SchemaFileWizardPage();

    public ImportSchemaWizard(Server server) {
        setWindowTitle("Import Schema");

        this.server = server;
    }

    public boolean canFinish() {

        if (!namePage.isPageComplete()) return false;
        if (!filePage.isPageComplete()) return false;

        return true;
    }

    public boolean performFinish() {
        try {
            File file = new File(filePage.getFilename());

            SchemaReader reader = new SchemaReader();
            Schema schema = reader.read(file);

            PenroseClient client = server.getClient();
            SchemaManagerClient schemaManagerClient = client.getSchemaManagerClient();
            schemaManagerClient.createSchema(schema);

            return true;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
            return false;
        }
    }

    public void addPages() {
        addPage(namePage);
        addPage(filePage);
    }

    public boolean needsPreviousAndNextButtons() {
        return true;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }
}
