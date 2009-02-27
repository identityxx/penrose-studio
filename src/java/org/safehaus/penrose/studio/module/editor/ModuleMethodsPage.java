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
package org.safehaus.penrose.studio.module.editor;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.ui.PlatformUI;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.core.runtime.IProgressMonitor;
import org.safehaus.penrose.client.BaseClient;
import org.safehaus.penrose.studio.dialog.ErrorDialog;

import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import java.lang.reflect.InvocationTargetException;

public class ModuleMethodsPage extends FormPage {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;
    Table methodsTable;
    Table parametersTable;

    boolean dirty;

    BaseClient client;

    public ModuleMethodsPage(FormEditor editor, BaseClient client) {
        super(editor, "METHODS", "  Methods  ");

        this.client = client;
    }

    public void createFormContent(IManagedForm managedForm) {

        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText("Module Editor");

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section methodsSection = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        methodsSection.setText("Methods");
        methodsSection.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite methodsComponent = createMethodsControl(methodsSection);
        methodsSection.setClient(methodsComponent);

        Section parametersSection = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        parametersSection.setText("Parameters");
        parametersSection.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite parametersComponent = createParametersControl(parametersSection);
        parametersSection.setClient(parametersComponent);
	}

    public Composite createMethodsControl(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        Composite leftControl = createMethodsLeftControl(composite);
        leftControl.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite rightControl = createMethodsRightControl(composite);
        GridData gd = new GridData(GridData.FILL_VERTICAL);
        gd.widthHint = 100;
        rightControl.setLayoutData(gd);

        return composite;
    }

    public Composite createMethodsLeftControl(final Composite parent) {

        methodsTable = toolkit.createTable(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        methodsTable.setHeaderVisible(true);
        methodsTable.setLinesVisible(true);
        methodsTable.setLayoutData(new GridData(GridData.FILL_BOTH));

        TableColumn tc = new TableColumn(methodsTable, SWT.NONE);
        tc.setText("Name");
        tc.setWidth(250);

        tc = new TableColumn(methodsTable, SWT.NONE);
        tc.setText("Return Type");
        tc.setWidth(300);

        methodsTable.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                
                if (methodsTable.getSelectionCount() != 1) return;

                TableItem item = methodsTable.getSelection()[0];
                MBeanOperationInfo operationInfo = (MBeanOperationInfo)item.getData();
                showParameters(operationInfo);
            }
        });

        return methodsTable;
    }

    public Composite createMethodsRightControl(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);

        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);

        Button executeButton = new Button(composite, SWT.PUSH);
		executeButton.setText("Execute");

        executeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        executeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    if (methodsTable.getSelectionCount() != 1) return;

                    TableItem item = methodsTable.getSelection()[0];
                    MBeanOperationInfo operationInfo = (MBeanOperationInfo)item.getData();

                    invokeMethod(operationInfo);

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        return composite;
    }

    public void invokeMethod(final MBeanOperationInfo operationInfo) throws Exception {

        final String name = operationInfo.getName();
        log.debug("Executing: "+name);

        MBeanParameterInfo[] parameterInfos = operationInfo.getSignature();
        if (parameterInfos == null || parameterInfos.length > 0) return;

        IProgressService progressService = PlatformUI.getWorkbench().getProgressService();

        progressService.busyCursorWhile(new IRunnableWithProgress() {
            public void run(final IProgressMonitor monitor) throws InvocationTargetException {
                try {
                    monitor.beginTask("Executing method "+name+"...", IProgressMonitor.UNKNOWN);

                    Object result = client.invoke(name);
                    log.info(result);

                } catch (InterruptedException e) {
                    // ignore

                } catch (Exception e) {
                    throw new InvocationTargetException(e);

                } finally {
                     monitor.done();
                }
            }
        });
    }

    public Composite createParametersControl(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

		parametersTable = toolkit.createTable(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        parametersTable.setHeaderVisible(true);
        parametersTable.setLinesVisible(true);
        parametersTable.setLayoutData(new GridData(GridData.FILL_BOTH));

        TableColumn tc = new TableColumn(parametersTable, SWT.NONE);
        tc.setText("Name");
        tc.setWidth(100);

        tc = new TableColumn(parametersTable, SWT.NONE);
        tc.setText("Type");
        tc.setWidth(300);

        tc = new TableColumn(parametersTable, SWT.NONE);
        tc.setText("Description");
        tc.setWidth(200);

        return composite;
	}

    public void setActive(boolean b) {
        if (b) refresh();
        super.setActive(b);
    }
    
    public void refresh() {

        methodsTable.removeAll();

        try {
            log.debug("Getting module methods:");

            for (MBeanOperationInfo operationInfo : client.getOperations()) {
                String name = operationInfo.getName();
                String type = operationInfo.getReturnType();

                log.debug(" - "+name+": "+type);

                TableItem item = new TableItem(methodsTable, SWT.NONE);
                item.setText(0, name == null ? "" : name);
                item.setText(1, type == null ? "" : type);
                item.setData(operationInfo);
            }

        } catch (Exception e) {
            ErrorDialog.open(e);
        }
    }

    public void showParameters(MBeanOperationInfo operationInfo) {

        parametersTable.removeAll();
        
        log.debug("Getting method parameters:");

        for (MBeanParameterInfo parameterInfo : operationInfo.getSignature()) {
            String name = parameterInfo.getName();
            String type = parameterInfo.getType();
            String description = parameterInfo.getDescription();

            log.debug(" - "+name+": "+type);

            TableItem item = new TableItem(parametersTable, SWT.NONE);
            item.setText(0, name == null ? "" : name);
            item.setText(1, type == null ? "" : type);
            item.setText(2, description == null ? "" : description);
            item.setData(parameterInfo);
        }
    }
}