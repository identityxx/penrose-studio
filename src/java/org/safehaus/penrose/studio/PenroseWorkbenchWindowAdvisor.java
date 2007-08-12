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
package org.safehaus.penrose.studio;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.SWT;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.jface.dialogs.MessageDialog;
import org.safehaus.penrose.studio.welcome.WelcomeEditorInput;
import org.safehaus.penrose.studio.welcome.WelcomeEditor;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class PenroseWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

    Logger log = LoggerFactory.getLogger(getClass());

    private PenroseActionBarAdvisor actionBarAdvisor;

    public PenroseWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
        super(configurer);
    }

    public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {
        actionBarAdvisor = new PenroseActionBarAdvisor(configurer);
        return actionBarAdvisor;
    }

    public void openIntro() {
        // log.debug("openIntro");
    }

    public void preWindowOpen() {
        // log.debug("preWindowOpen");

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        try {
            penroseStudio.loadLicense();
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        try {
            Shell shell = new Shell(SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);

            IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
            configurer.setInitialSize(new Point(1024, 768));
            configurer.setTitle("Penrose Studio");
            //configurer.setShowCoolBar(true);
            //configurer.setShowStatusLine(true);
/*
            ProjectDialog dialog = new ProjectDialog(shell);
            dialog.open();

            if (dialog.getAction() == ProjectDialog.CANCEL) System.exit(0);

            Project project = dialog.getProjectConfig();

            penroseStudio.getApplicationConfig().setCurrentProject(project);
*/
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            System.exit(0);
        }
    }

    public void createWindowContents(Shell shell) {
        // log.debug("createWindowContents");
        super.createWindowContents(shell);

        Point size = new Point(1024, 768);
        //System.out.println("size: "+size);

        Display display = shell.getDisplay();
        Rectangle bounds = display.getBounds();
        //System.out.println("bounds: "+bounds);

        shell.setLocation(bounds.x + (bounds.width - size.x)/2, bounds.y + (bounds.height - size.y)/2);
    }

    public Control createEmptyWindowContents(Composite composite) {
        // log.debug("createEmptyWindowContents");
        return super.createEmptyWindowContents(composite);
    }

    public void postWindowCreate() {
        // log.debug("postWindowCreate");

        try {
            IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
            configurer.setTitle("Penrose Studio");

            IWorkbenchWindow window = configurer.getWindow();
            IWorkbenchPage page = window.getActivePage();
            page.openEditor(new WelcomeEditorInput(), WelcomeEditor.class.getName());

        } catch (Exception e) {
            log.error(e.getMessage(), e);

            IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            Shell shell = window.getShell();

            MessageDialog.openError(
                    shell,
                    "ERROR",
                    "Failed opening Penrose Studio.\n"+
                            "See penrose-studio.log for details."
            );
        }
/*
        try {
            penroseStudio.connect();
            penroseStudio.open();

        } catch (Exception e) {
            log.error(e.getMessage(), e);

            IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            Shell shell = window.getShell();

            MessageDialog.openError(
                    shell,
                    "ERROR",
                    "Failed opening Penrose Studio.\n"+
                            "See penrose-studio.log for details."
            );
        }
*/
    }

    public void postWindowOpen() {
        // log.debug("postWindowOpen");
        try {
            //PenroseStudio penroseStudio = PenroseStudio.getInstance();
            //penroseStudio.validatePartitions();
        } catch (Exception e) {
            log.error(e.getMessage(), e);

            String message = e.toString();
            if (message.length() > 500) {
                message = message.substring(0, 500) + "...";
            }

            IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            MessageDialog.openError(window.getShell(), "Open Failed", message);
        }
    }

    public PenroseActionBarAdvisor getActionBarAdvisor() {
        return actionBarAdvisor;
    }
}
