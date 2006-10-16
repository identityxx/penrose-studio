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
package org.safehaus.penrose.studio.project.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.safehaus.penrose.studio.PenroseApplication;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.util.ApplicationConfig;
import org.safehaus.penrose.studio.util.FileUtil;
import org.apache.log4j.Logger;

import java.io.File;

public class SaveAction extends Action {

    Logger log = Logger.getLogger(getClass());

    public SaveAction() {
        setText("&Save");
        setImageDescriptor(PenrosePlugin.getImageDescriptor(PenroseImage.SAVE));
        setAccelerator(SWT.CTRL | 'S');
        setToolTipText("Save changes to disk");
        setId(getClass().getName());
    }

    public void run() {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        page.saveAllEditors(false);

        PenroseApplication penroseApplication = PenroseApplication.getInstance();
        ApplicationConfig applicationConfig = penroseApplication.getApplicationConfig();

        try {
            File workDir = new File(penroseApplication.getWorkDir());
            File workTmp = new File(penroseApplication.getWorkDir()+".tmp");
            File workBackup = new File(penroseApplication.getWorkDir()+".bak");

            FileUtil.delete(workTmp);
            copyFolder(workDir, workTmp);

            penroseApplication.save(workTmp.getAbsolutePath());

            FileUtil.delete(workBackup);
            workDir.renameTo(workBackup);
            workTmp.renameTo(workDir);

        } catch (Exception e) {
            log.error(e.getMessage(), e);

            Shell shell = window.getShell();

            MessageDialog.openError(
                    shell,
                    "ERROR",
                    "Failed saving "+applicationConfig.getCurrentProject().getName()+" configuration.\n"+
                            "See penrose-studio-log.txt for details."
            );
        }
    }

    public static void copyFolder(String dirname1, String dirname2) throws Exception {
        File dir1 = new File(dirname1);
        File dir2 = new File(dirname2);
        copyFolder(dir1, dir2);
    }

    public static void copyFolder(File dir1, File dir2) throws Exception {
        dir2.mkdirs();

        File files[] = dir1.listFiles();
        if (files != null) {
            for (int i=0; i<files.length; i++) {
                File target = new File(dir2, files[i].getName());

                if (files[i].isDirectory()) {
                    copyFolder(files[i], target);
                } else {
                    FileUtil.copy(files[i], target);
                }
            }
        }
    }
}