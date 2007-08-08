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

import org.eclipse.jface.wizard.Wizard;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.util.FileUtil;
import org.safehaus.penrose.config.PenroseConfig;
import org.safehaus.penrose.schema.SchemaConfig;
import org.safehaus.penrose.schema.SchemaManager;
import org.apache.log4j.Logger;

import java.io.*;

/**
 * @author Endi S. Dewata
 */
public class ImportSchemaWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    public SchemaNameWizardPage namePage = new SchemaNameWizardPage();
    public SchemaFileWizardPage filePage = new SchemaFileWizardPage();

    public ImportSchemaWizard() {
        setWindowTitle("Import Schema");
    }

    public boolean canFinish() {

        if (!namePage.isPageComplete()) return false;
        if (!filePage.isPageComplete()) return false;

        return true;
    }

    public boolean performFinish() {
        try {
            String schemaExtDir = "schema/ext";
            
            String name = namePage.getSchemaName();
            String path = schemaExtDir+"/"+name+".schema";

            PenroseStudio penroseStudio = PenroseStudio.getInstance();
            File workDir = penroseStudio.getWorkDir();

            File file1 = new File(filePage.getFilename());
            File file2 = new File(workDir, path);
            FileUtil.copy(file1, file2);

            SchemaConfig schemaConfig = new SchemaConfig();
            schemaConfig.setName(name);
            schemaConfig.setPath(path);

            PenroseConfig penroseConfig = penroseStudio.getPenroseConfig();
            penroseConfig.addSchemaConfig(schemaConfig);

            SchemaManager schemaManager = penroseStudio.getSchemaManager();
            schemaManager.init(penroseStudio.getWorkDir(), schemaConfig);

            return true;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
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
}
