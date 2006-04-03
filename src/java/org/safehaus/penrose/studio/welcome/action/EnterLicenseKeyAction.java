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
package org.safehaus.penrose.studio.welcome.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchPage;
import org.apache.log4j.Logger;
import org.safehaus.penrose.studio.util.FileUtil;
import org.safehaus.penrose.studio.PenroseApplication;
import org.safehaus.penrose.studio.PenroseWorkbenchAdvisor;
import org.safehaus.penrose.studio.PenroseWorkbenchWindowAdvisor;
import org.safehaus.penrose.studio.PenroseActionBarAdvisor;
import org.safehaus.penrose.license.LicenseManager;
import org.safehaus.penrose.license.LicenseReader;
import org.safehaus.penrose.license.License;

/**
 * @author Endi S. Dewata
 */
public class EnterLicenseKeyAction extends Action {

    Logger log = Logger.getLogger(getClass());

    public EnterLicenseKeyAction() {
        setText("&Enter License Key...");
        setId(getClass().getName());
    }

	public void run() {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        Shell shell = window.getShell();

        try {
            String licenseFile = "penrose.license";
            String dir = System.getProperty("user.dir");

            FileDialog dialog = new FileDialog(shell);
            dialog.setText("License");
            dialog.setFilterPath(dir);
            dialog.setFilterExtensions(new String[] { "*.license", "*.*" });

            String filename = dialog.open();
            if (filename == null) return;

            FileUtil.copy(filename, licenseFile);

            PenroseApplication penroseApplication = PenroseApplication.getInstance();

            LicenseManager licenseManager = new LicenseManager(penroseApplication.getPublicKey());
            
            LicenseReader licenseReader = new LicenseReader(licenseManager);
            licenseReader.read(licenseFile);

            License license = licenseManager.getLicense("Penrose Studio");

            boolean valid = licenseManager.isValid(license);

            if (!valid) {
                throw new Exception("Invalid license.");
            }

            penroseApplication.setLicense(license);

            PenroseWorkbenchAdvisor workbenchAdvisor = penroseApplication.getWorkbenchAdvisor();
            PenroseWorkbenchWindowAdvisor workbenchWindowAdvisor = workbenchAdvisor.getWorkbenchWindowAdvisor();
            PenroseActionBarAdvisor actionBarAdvisor = workbenchWindowAdvisor.getActionBarAdvisor();

            actionBarAdvisor.getShowCommercialFeaturesAction().setChecked(true);
            
            actionBarAdvisor.fillPartitionMenu();
            actionBarAdvisor.fillHelpMenu();

        } catch (Exception e) {
            log.debug(e.getMessage(), e);
            MessageDialog.openError(shell, "Error", e.getMessage());
        }
	}
}