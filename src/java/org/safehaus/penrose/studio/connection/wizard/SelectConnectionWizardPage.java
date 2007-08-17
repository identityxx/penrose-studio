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
package org.safehaus.penrose.studio.connection.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.safehaus.penrose.partition.PartitionConfig;
import org.safehaus.penrose.connection.ConnectionConfig;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.project.Project;
import org.apache.log4j.Logger;

import javax.naming.Context;

/**
 * @author Endi S. Dewata
 */
public class SelectConnectionWizardPage extends WizardPage {

    Logger log = Logger.getLogger(getClass());

    public final static String NAME = "Connection";

    Table connectionTable;
    Table infoTable;

    private Project project;
    private PartitionConfig partitionConfig;
    String adapterType;

    public SelectConnectionWizardPage(PartitionConfig partitionConfig) {
        this(partitionConfig, null);
    }

    public SelectConnectionWizardPage(PartitionConfig partitionConfig, String adapterType) {
        super(NAME);

        this.partitionConfig = partitionConfig;
        this.adapterType = adapterType;

        setDescription("Select connection.");
    }

    public void createControl(final Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        composite.setLayout(new GridLayout(2, false));

        connectionTable = new Table(composite, SWT.BORDER);
        GridData gd = new GridData(GridData.FILL_VERTICAL);
        gd.widthHint = 100;
        connectionTable.setLayoutData(gd);

        connectionTable.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                TableItem ti = connectionTable.getSelection()[0];
                ConnectionConfig connectionConfig = (ConnectionConfig)ti.getData();
                String adapterName = connectionConfig.getAdapterName();
                infoTable.removeAll();

                ti = new TableItem(infoTable, SWT.NONE);
                ti.setText(0, "Adapter:");
                ti.setText(1, adapterName);

                if ("JDBC".equals(adapterName)) {
                    ti = new TableItem(infoTable, SWT.NONE);
                    ti.setText(0, "Driver:");
                    ti.setText(1, connectionConfig.getParameter("driver"));

                    ti = new TableItem(infoTable, SWT.NONE);
                    ti.setText(0, "URL:");
                    ti.setText(1, connectionConfig.getParameter("url"));

                    String username = connectionConfig.getParameter("user");
                    ti = new TableItem(infoTable, SWT.NONE);
                    ti.setText(0, "Username:");
                    ti.setText(1, username == null ? "" : username);

                    ti = new TableItem(infoTable, SWT.NONE);
                    ti.setText(0, "Password:");
                    ti.setText(1, "********");

                } else if ("LDAP".equals(adapterName)) {
                    ti = new TableItem(infoTable, SWT.NONE);
                    ti.setText(0, "URL:");
                    ti.setText(1, connectionConfig.getParameter(Context.PROVIDER_URL));

                    String bindDn = connectionConfig.getParameter(Context.SECURITY_PRINCIPAL);
                    ti = new TableItem(infoTable, SWT.NONE);
                    ti.setText(0, "Bind DN:");
                    ti.setText(1, bindDn == null ? "" : bindDn);

                    ti = new TableItem(infoTable, SWT.NONE);
                    ti.setText(0, "Password:");
                    ti.setText(1, "********");
                }

                setPageComplete(validatePage());
            }
        });

        infoTable = new Table(composite, SWT.BORDER | SWT.READ_ONLY | SWT.FULL_SELECTION);
        infoTable.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
        infoTable.setLayoutData(new GridData(GridData.FILL_BOTH));

        TableColumn tc = new TableColumn(infoTable, SWT.NONE);
        tc.setWidth(100);

        tc = new TableColumn(infoTable, SWT.NONE);
        tc.setWidth(300);

        Composite buttons = new Composite(composite, SWT.NONE);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        buttons.setLayoutData(gd);
        buttons.setLayout(new RowLayout());

        Button addButton = new Button(buttons, SWT.PUSH);
        addButton.setText("Add Connection");

        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {

                ConnectionWizard wizard = new ConnectionWizard(partitionConfig);
                wizard.setProject(project);

                WizardDialog dialog = new WizardDialog(parent.getShell(), wizard);
                dialog.setPageSize(600, 300);
                dialog.open();

                refresh();

                PenroseStudio penroseStudio = PenroseStudio.getInstance();
                penroseStudio.notifyChangeListeners();
            }
        });

        Button removeButton = new Button(buttons, SWT.PUSH);
        removeButton.setText("Delete Connection");

        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                if (connectionTable.getSelectionCount() == 0) return;

                TableItem ti = connectionTable.getSelection()[0];
                ConnectionConfig connectionConfig = (ConnectionConfig)ti.getData();

                boolean confirm = MessageDialog.openQuestion(
                        parent.getShell(),
                        "Confirmation",
                        "Remove Connection \""+connectionConfig.getName()+"\"?");

                if (!confirm) return;

                partitionConfig.getConnectionConfigs().removeConnectionConfig(connectionConfig.getName());

                refresh();

                PenroseStudio penroseStudio = PenroseStudio.getInstance();
                penroseStudio.notifyChangeListeners();
            }
        });

        refresh();
    }

    public void refresh() {
        connectionTable.removeAll();
        infoTable.removeAll();

        for (ConnectionConfig connectionConfig : partitionConfig.getConnectionConfigs().getConnectionConfigs()) {
            String adapterName = connectionConfig.getAdapterName();
            if (adapterType != null && !adapterType.equals(adapterName)) continue;

            TableItem item = new TableItem(connectionTable, SWT.NONE);
            item.setText(connectionConfig.getName());
            item.setData(connectionConfig);
        }
    }

    public ConnectionConfig getConnectionConfig() {
        if (connectionTable.getSelectionCount() == 0) return null;

        TableItem item = connectionTable.getSelection()[0];
        return (ConnectionConfig)item.getData();
    }

    public boolean validatePage() {
        return getConnectionConfig() != null;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public PartitionConfig getPartitionConfig() {
        return partitionConfig;
    }

    public void setPartitionConfig(PartitionConfig partitionConfig) {
        this.partitionConfig = partitionConfig;
    }
}
