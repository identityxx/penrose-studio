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
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.swt.SWT;
import org.safehaus.penrose.studio.preview.action.PreviewAction;
import org.safehaus.penrose.studio.preview.action.RestartAction;
import org.safehaus.penrose.studio.welcome.action.WelcomeAction;
import org.safehaus.penrose.studio.welcome.action.AboutAction;
import org.safehaus.penrose.studio.welcome.action.ShowCommercialFeaturesAction;
import org.safehaus.penrose.studio.welcome.action.EnterLicenseKeyAction;
import org.safehaus.penrose.studio.object.ObjectsAction;
import org.safehaus.penrose.studio.object.ObjectsView;
import org.safehaus.penrose.studio.validation.ValidationAction;
import org.safehaus.penrose.studio.console.ConsoleAction;
import org.safehaus.penrose.studio.partition.action.NewPartitionAction;
import org.safehaus.penrose.studio.partition.action.ImportPartitionAction;
import org.safehaus.penrose.studio.partition.action.NewLDAPSnapshotPartitionAction;
import org.safehaus.penrose.studio.partition.action.NewLDAPProxyPartitionAction;
import org.safehaus.penrose.studio.service.action.NewServiceAction;
import org.safehaus.penrose.studio.project.action.*;
import org.safehaus.penrose.studio.project.ProjectNode;
import org.safehaus.penrose.studio.schema.action.ImportSchemaAction;
import org.safehaus.penrose.studio.schema.action.NewSchemaAction;
import org.safehaus.penrose.studio.browser.action.BrowserAction;
import org.safehaus.penrose.studio.event.SelectionListener;
import org.safehaus.penrose.studio.event.SelectionEvent;
import org.safehaus.penrose.studio.event.ChangeListener;
import org.safehaus.penrose.studio.event.ChangeEvent;
import org.apache.log4j.Logger;

public class PenroseActionBarAdvisor
        extends ActionBarAdvisor
        implements ChangeListener, SelectionListener {

    Logger log = Logger.getLogger(getClass());

    NewProjectAction newProjectAction;
    OpenProjectAction openProjectAction;
    CloseProjectAction closeProjectAction;
    CloseAllProjectsAction closeAllProjectsAction;

    SaveProjectAction saveProjectAction;
    UploadProjectAction uploadProjectAction;
    IAction quitAction;

    CopyProjectAction copyProjectAction;
    PasteProjectAction pasteProjectAction;
    DeleteProjectAction deleteProjectAction;

    NewPartitionAction newPartitionAction;
    ImportPartitionAction importPartitionAction;
    NewLDAPSnapshotPartitionAction newLDAPSnapshotPartitionAction;
    NewLDAPProxyPartitionAction newLDAPProxyPartitionAction;

    NewSchemaAction newSchemaAction;
    ImportSchemaAction importSchemaAction;

    NewServiceAction newServiceAction;

    ObjectsAction objectsAction;
    ValidationAction validationAction;
    ConsoleAction consoleAction;

    BrowserAction browserAction;
    PreviewAction previewAction;
    RestartAction restartAction;

    WelcomeAction welcomeAction;

    ShowCommercialFeaturesAction showCommercialFeaturesAction;
    EnterLicenseKeyAction enterLicenseKeyAction;

    AboutAction aboutAction;

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
            newProjectAction = new NewProjectAction();
            register(newProjectAction);

            openProjectAction = new OpenProjectAction();
            register(openProjectAction);

            closeProjectAction = new CloseProjectAction();
            register(closeProjectAction);

            closeAllProjectsAction = new CloseAllProjectsAction();
            register(closeAllProjectsAction);

            saveProjectAction = new SaveProjectAction();
            register(saveProjectAction);

            uploadProjectAction = new UploadProjectAction();
            register(uploadProjectAction);

            quitAction = ActionFactory.QUIT.create(window);
            quitAction.setAccelerator(SWT.ALT | SWT.F4);
            register(quitAction);

            copyProjectAction = new CopyProjectAction();
            register(copyProjectAction);

            pasteProjectAction = new PasteProjectAction();
            register(pasteProjectAction);

            deleteProjectAction = new DeleteProjectAction();
            register(deleteProjectAction);

            newPartitionAction = new NewPartitionAction();
            importPartitionAction = new ImportPartitionAction();
            newLDAPSnapshotPartitionAction = new NewLDAPSnapshotPartitionAction();
            newLDAPProxyPartitionAction = new NewLDAPProxyPartitionAction();

            newSchemaAction = new NewSchemaAction();
            importSchemaAction = new ImportSchemaAction();

            newServiceAction = new NewServiceAction();

            objectsAction = new ObjectsAction();
            validationAction = new ValidationAction();
            consoleAction = new ConsoleAction();

            browserAction = new BrowserAction();
            previewAction = new PreviewAction();
            restartAction = new RestartAction();

            welcomeAction = new WelcomeAction();
            showCommercialFeaturesAction = new ShowCommercialFeaturesAction();
            enterLicenseKeyAction = new EnterLicenseKeyAction();
            aboutAction = new AboutAction();

            setEnabled(false);
            setConnected(false);

        } catch (Exception e) {
            log.debug(e.getMessage(), e);
        }
    }

    public void setEnabled(boolean enabled) {
        openProjectAction.setEnabled(enabled);
        copyProjectAction.setEnabled(enabled);
        pasteProjectAction.setEnabled(enabled);
        deleteProjectAction.setEnabled(enabled);
    }

    public void setConnected(boolean connected) {
        openProjectAction.setEnabled(!connected);
        closeProjectAction.setEnabled(connected);
        saveProjectAction.setEnabled(connected);
        uploadProjectAction.setEnabled(connected);
        deleteProjectAction.setEnabled(!connected);

        newPartitionAction.setEnabled(connected);
        importPartitionAction.setEnabled(connected);
        newLDAPSnapshotPartitionAction.setEnabled(connected);
        newLDAPProxyPartitionAction.setEnabled(connected);

        newSchemaAction.setEnabled(connected);
        importSchemaAction.setEnabled(connected);

        newServiceAction.setEnabled(connected);

        browserAction.setEnabled(connected);
        previewAction.setEnabled(connected);
        restartAction.setEnabled(connected);
    }

    public void fillPartitionMenu() {

        partitionMenu.removeAll();

        partitionMenu.add(newPartitionAction);
        partitionMenu.add(importPartitionAction);

        if (showCommercialFeaturesAction.isChecked()) {
            partitionMenu.add(new Separator());
            partitionMenu.add(newLDAPSnapshotPartitionAction);
            partitionMenu.add(newLDAPProxyPartitionAction);
        }
    }

    public void fillHelpMenu() {

        helpMenu.removeAll();

        helpMenu.add(welcomeAction);

        helpMenu.add(new Separator());

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        //if (penroseStudio.isFreeware()) {
        //    helpMenu.add(showCommercialFeaturesAction);
        //}

        helpMenu.add(enterLicenseKeyAction);

        helpMenu.add(new Separator());
        helpMenu.add(aboutAction);
    }

    protected void fillMenuBar(IMenuManager menuBar) {
        log.debug("fillMenuBar");
        super.fillMenuBar(menuBar);

        try {
            MenuManager fileMenu = new MenuManager("&File", "file");
            menuBar.add(fileMenu);

            fileMenu.add(newProjectAction);
            fileMenu.add(openProjectAction);
            fileMenu.add(closeProjectAction);
            fileMenu.add(closeAllProjectsAction);
            fileMenu.add(new Separator());
            fileMenu.add(saveProjectAction);
            fileMenu.add(new Separator());
            fileMenu.add(uploadProjectAction);
            fileMenu.add(restartAction);

            fileMenu.add(new Separator());
            fileMenu.add(quitAction);

            MenuManager editMenu = new MenuManager("&Edit", "edit");
            menuBar.add(editMenu);

            editMenu.add(copyProjectAction);
            editMenu.add(pasteProjectAction);
            editMenu.add(new Separator());
            editMenu.add(deleteProjectAction);

            partitionMenu = new MenuManager("&Partition", "partition");
            menuBar.add(partitionMenu);

            fillPartitionMenu();

            MenuManager schemaMenu = new MenuManager("&Schema", "schema");
            menuBar.add(schemaMenu);

            schemaMenu.add(newSchemaAction);
            schemaMenu.add(importSchemaAction);

            MenuManager serviceMenu = new MenuManager("S&ervice", "service");
            menuBar.add(serviceMenu);

            serviceMenu.add(newServiceAction);

            MenuManager toolsMenu = new MenuManager("&Tools", "tools");
            menuBar.add(toolsMenu);

            toolsMenu.add(previewAction);
            toolsMenu.add(browserAction);

            MenuManager windowMenu = new MenuManager("&Window", "window");
            menuBar.add(windowMenu);

            windowMenu.add(objectsAction);
            windowMenu.add(validationAction);
            windowMenu.add(consoleAction);

            helpMenu = new MenuManager("&Help", "help");
            menuBar.add(helpMenu);

            fillHelpMenu();

        } catch (Exception e) {
            log.debug(e.getMessage(), e);
        }
    }
    
    protected void fillCoolBar(ICoolBarManager coolBar) {
        log.debug("fillCoolBar");
        super.fillCoolBar(coolBar);

        try {
            standardToolBar = new ToolBarManager(SWT.FLAT | SWT.RIGHT);
            coolBar.add(new ToolBarContributionItem(standardToolBar, "standard"));

            ActionContributionItem newProjectCI = new ActionContributionItem(newProjectAction);
            standardToolBar.add(newProjectCI);

            ActionContributionItem openProjectCI = new ActionContributionItem(openProjectAction);
            standardToolBar.add(openProjectCI);

            ActionContributionItem closeProjectCI = new ActionContributionItem(closeProjectAction);
            standardToolBar.add(closeProjectCI);

            ActionContributionItem saveCI = new ActionContributionItem(saveProjectAction);
            standardToolBar.add(saveCI);

            ActionContributionItem uploadCI = new ActionContributionItem(uploadProjectAction);
            standardToolBar.add(uploadCI);

            ActionContributionItem restartCI = new ActionContributionItem(restartAction);
            standardToolBar.add(restartCI);

            previewToolBar = new ToolBarManager(SWT.FLAT | SWT.RIGHT);
            coolBar.add(new ToolBarContributionItem(previewToolBar, "preview"));

            ActionContributionItem previewCI = new ActionContributionItem(previewAction);
            previewToolBar.add(previewCI);

            ActionContributionItem browserCI = new ActionContributionItem(browserAction);
            previewToolBar.add(browserCI);

        } catch (Exception e) {
            log.debug(e.getMessage(), e);
        }
    }

    public ShowCommercialFeaturesAction getShowCommercialFeaturesAction() {
        return showCommercialFeaturesAction;
    }

    public void setShowCommercialFeaturesAction(ShowCommercialFeaturesAction showCommercialFeaturesAction) {
        this.showCommercialFeaturesAction = showCommercialFeaturesAction;
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
                setEnabled(false);
                setConnected(false);
            } else {
                setEnabled(true);
                setConnected(projectNode.isConnected());
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
