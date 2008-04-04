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
package org.safehaus.penrose.studio.mapping.wizard;

import org.eclipse.jface.wizard.Wizard;
import org.apache.log4j.Logger;
import org.safehaus.penrose.studio.mapping.wizard.BinaryExportWizardPage;

import java.io.FileOutputStream;
import java.io.File;

/**
 * @author Endi S. Dewata
 */
public class BinaryExportWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    private byte[] bytes;

    public BinaryExportWizardPage page = new BinaryExportWizardPage();

    public BinaryExportWizard(byte bytes[]) {
        this.bytes = bytes;
        setWindowTitle("Export Binary Data");
    }

    public boolean canFinish() {
        if (!page.isPageComplete()) return false;
        return true;
    }

    public boolean performFinish() {
        try {

            String filename = page.getFileName();
            String directory = page.getDirectory();

            File file = new File(directory, filename);
            FileOutputStream out = new FileOutputStream(file);
            out.write(bytes);
            out.close();

            return true;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void addPages() {
        addPage(page);
    }

    public boolean needsPreviousAndNextButtons() {
        return true;
    }
}
