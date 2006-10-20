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

import org.eclipse.jface.action.*;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.swt.SWT;
import org.safehaus.penrose.studio.object.ObjectsView;
import org.safehaus.penrose.studio.project.ProjectNode;
import org.safehaus.penrose.studio.event.SelectionListener;
import org.safehaus.penrose.studio.event.SelectionEvent;
import org.safehaus.penrose.studio.event.ChangeListener;
import org.safehaus.penrose.studio.event.ChangeEvent;
import org.safehaus.penrose.studio.action.PenroseStudioActions;
import org.apache.log4j.Logger;

public class PenroseActionBarAdvisor
        extends ActionBarAdvisor
        implements ChangeListener, SelectionListener {

    Logger log = Logger.getLogger(getClass());

    MenuManager partitionMenu;
    MenuManager helpMenu;

    IToolBarManager standardToolBar;
    IToolBarManager previewToolBar;

    public PenroseActionBarAdvisor(IActionBarConfigurer configurer) {
        super(configurer);

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.addChangeListener(this);
        penroseStudio.addSelectionListener(this);
    }

    protected void makeActions(final IWorkbenchWindow window) {
        super.makeActions(window);

        try {
            PenroseStudio penroseStudio = PenroseStudio.getInstance();
            PenroseStudioActions penroseStudioActions = penroseStudio.getActions();

            register(penroseStudioActions.getNewAction());
            register(penroseStudioActions.getOpenAction());
            register(penroseStudioActions.getCloseAction());
            register(penroseStudioActions.getCloseAllAction());
            register(penroseStudioActions.getSaveAction());
            register(penroseStudioActions.getUploadAction());
            register(penroseStudioActions.getExitAction());

            register(penroseStudioActions.getCutAction());
            register(penroseStudioActions.getCopyAction());
            register(penroseStudioActions.getPasteAction());
            register(penroseStudioActions.getDeleteAction());

            setConnected(false);

        } catch (Exception e) {
            log.debug(e.getMessage(), e);
        }
    }

    public void setConnected(boolean connected) {
        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        PenroseStudioActions penroseStudioActions = penroseStudio.getActions();

        penroseStudioActions.setConnected(connected);
    }

    public void fillPartitionMenu() {

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        PenroseStudioActions penroseStudioActions = penroseStudio.getActions();

        partitionMenu.removeAll();

        partitionMenu.add(penroseStudioActions.getNewPartitionAction());
        partitionMenu.add(penroseStudioActions.getImportPartitionAction());

        if (penroseStudioActions.getShowCommercialFeaturesAction().isChecked()) {
            partitionMenu.add(new Separator());
            partitionMenu.add(penroseStudioActions.getNewLDAPSnapshotPartitionAction());
            partitionMenu.add(penroseStudioActions.getNewLDAPProxyPartitionAction());
        }
    }

    public void fillHelpMenu() {

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        PenroseStudioActions penroseStudioActions = penroseStudio.getActions();

        helpMenu.removeAll();

        helpMenu.add(penroseStudioActions.getWelcomeAction());
        helpMenu.add(new Separator());
        helpMenu.add(penroseStudioActions.getEnterLicenseKeyAction());
        helpMenu.add(new Separator());
        helpMenu.add(penroseStudioActions.getAboutAction());
    }

    protected void fillMenuBar(IMenuManager menuBar) {
        super.fillMenuBar(menuBar);

        try {
            PenroseStudio penroseStudio = PenroseStudio.getInstance();
            PenroseStudioActions penroseStudioActions = penroseStudio.getActions();

            MenuManager fileMenu = new MenuManager("&File", "file");
            menuBar.add(fileMenu);

            fileMenu.add(penroseStudioActions.getNewAction());
            fileMenu.add(penroseStudioActions.getOpenAction());
            fileMenu.add(penroseStudioActions.getCloseAction());
            fileMenu.add(penroseStudioActions.getCloseAllAction());
            fileMenu.add(new Separator());
            fileMenu.add(penroseStudioActions.getSaveAction());
            fileMenu.add(new Separator());
            fileMenu.add(penroseStudioActions.getUploadAction());
            fileMenu.add(penroseStudioActions.getRestartAction());

            fileMenu.add(new Separator());
            fileMenu.add(penroseStudioActions.getExitAction());

            MenuManager editMenu = new MenuManager("&Edit", "edit");
            menuBar.add(editMenu);

            editMenu.add(penroseStudioActions.getCutAction());
            editMenu.add(penroseStudioActions.getCopyAction());
            editMenu.add(penroseStudioActions.getPasteAction());
            editMenu.add(penroseStudioActions.getDeleteAction());
            editMenu.add(new Separator());
            editMenu.add(penroseStudioActions.getPropertiesAction());

            partitionMenu = new MenuManager("&Partition", "partition");
            menuBar.add(partitionMenu);

            fillPartitionMenu();

            MenuManager schemaMenu = new MenuManager("&Schema", "schema");
            menuBar.add(schemaMenu);

            schemaMenu.add(penroseStudioActions.getNewSchemaAction());
            schemaMenu.add(penroseStudioActions.getImportSchemaAction());

            MenuManager serviceMenu = new MenuManager("S&ervice", "service");
            menuBar.add(serviceMenu);

            serviceMenu.add(penroseStudioActions.getNewServiceAction());

            MenuManager toolsMenu = new MenuManager("&Tools", "tools");
            menuBar.add(toolsMenu);

            toolsMenu.add(penroseStudioActions.getPreviewAction());
            toolsMenu.add(penroseStudioActions.getBrowserAction());

            MenuManager windowMenu = new MenuManager("&Window", "window");
            menuBar.add(windowMenu);

            windowMenu.add(penroseStudioActions.getObjectsAction());
            windowMenu.add(penroseStudioActions.getValidationAction());
            windowMenu.add(penroseStudioActions.getConsoleAction());

            helpMenu = new MenuManager("&Help", "help");
            menuBar.add(helpMenu);

            fillHelpMenu();

        } catch (Exception e) {
            log.debug(e.getMessage(), e);
        }
    }
    
    protected void fillCoolBar(ICoolBarManager coolBar) {
        super.fillCoolBar(coolBar);

        try {
            PenroseStudio penroseStudio = PenroseStudio.getInstance();
            PenroseStudioActions penroseStudioActions = penroseStudio.getActions();

            standardToolBar = new ToolBarManager(SWT.FLAT | SWT.RIGHT);
            coolBar.add(new ToolBarContributionItem(standardToolBar, "standard"));

            ActionContributionItem newProjectCI = new ActionContributionItem(penroseStudioActions.getNewAction());
            standardToolBar.add(newProjectCI);

            ActionContributionItem openProjectCI = new ActionContributionItem(penroseStudioActions.getOpenAction());
            standardToolBar.add(openProjectCI);

            ActionContributionItem closeProjectCI = new ActionContributionItem(penroseStudioActions.getCloseAction());
            standardToolBar.add(closeProjectCI);

            ActionContributionItem saveCI = new ActionContributionItem(penroseStudioActions.getSaveAction());
            standardToolBar.add(saveCI);

            ActionContributionItem uploadCI = new ActionContributionItem(penroseStudioActions.getUploadAction());
            standardToolBar.add(uploadCI);

            ActionContributionItem restartCI = new ActionContributionItem(penroseStudioActions.getRestartAction());
            standardToolBar.add(restartCI);

            previewToolBar = new ToolBarManager(SWT.FLAT | SWT.RIGHT);
            coolBar.add(new ToolBarContributionItem(previewToolBar, "preview"));

            ActionContributionItem previewCI = new ActionContributionItem(penroseStudioActions.getPreviewAction());
            previewToolBar.add(previewCI);

            ActionContributionItem browserCI = new ActionContributionItem(penroseStudioActions.getBrowserAction());
            previewToolBar.add(browserCI);

        } catch (Exception e) {
            log.debug(e.getMessage(), e);
        }
    }

    public void objectChanged(ChangeEvent event) {
        update();
    }

    public void objectSelected(SelectionEvent event) {
        update();
    }

    public void update() {
        try {
            IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

            IWorkbenchPage page = window.getActivePage();
            ObjectsView objectsView = (ObjectsView)page.showView(ObjectsView.class.getName());

            ProjectNode projectNode = objectsView.getSelectedProjectNode();
            if (projectNode == null) {
                setConnected(false);
            } else {
                setConnected(projectNode.isConnected());
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
