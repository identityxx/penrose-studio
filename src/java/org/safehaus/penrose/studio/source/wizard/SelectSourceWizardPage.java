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
package org.safehaus.penrose.studio.source.wizard;

import org.apache.log4j.Logger;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.safehaus.penrose.connection.ConnectionConfig;
import org.safehaus.penrose.connection.ConnectionClient;
import org.safehaus.penrose.connection.ConnectionManagerClient;
import org.safehaus.penrose.source.SourceClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.source.SourceConfig;
import org.safehaus.penrose.source.SourceManagerClient;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.client.PenroseClient;

import javax.naming.Context;

/**
 * @author Endi S. Dewata
 */
public class SelectSourceWizardPage extends WizardPage {

    Logger log = Logger.getLogger(getClass());

    public final static String NAME = "Source";

    Table sourceTable;
    Table infoTable;

    private Server server;
    private String partitionName;

    public SelectSourceWizardPage() {
        super(NAME);
        setDescription("Select source.");
    }

    public void createControl(final Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        composite.setLayout(new GridLayout(2, false));

        sourceTable = new Table(composite, SWT.BORDER);
        GridData gd = new GridData(GridData.FILL_VERTICAL);
        gd.widthHint = 100;
        sourceTable.setLayoutData(gd);

        sourceTable.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    TableItem ti = sourceTable.getSelection()[0];
                    SourceConfig sourceConfig = (SourceConfig)ti.getData();

                    PenroseClient client = server.getClient();
                    PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
                    PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
                    ConnectionManagerClient connectionManagerClient = partitionClient.getConnectionManagerClient();

                    ConnectionClient connectionClient = connectionManagerClient.getConnectionClient(sourceConfig.getConnectionName());
                    ConnectionConfig connectionConfig = connectionClient.getConnectionConfig();

                    String baseDn = sourceConfig.getParameter("baseDn");
                    baseDn = baseDn == null ? "" : baseDn;

                    infoTable.removeAll();

                    ti = new TableItem(infoTable, SWT.NONE);
                    ti.setText(0, "Connection:");
                    ti.setText(1, connectionConfig.getName());

                    ti = new TableItem(infoTable, SWT.NONE);
                    ti.setText(0, "URL:");
                    ti.setText(1, connectionConfig.getParameter(Context.PROVIDER_URL));

                    ti = new TableItem(infoTable, SWT.NONE);
                    ti.setText(0, "Bind DN:");
                    ti.setText(1, connectionConfig.getParameter(Context.SECURITY_PRINCIPAL));

                    ti = new TableItem(infoTable, SWT.NONE);
                    ti.setText(0, "Password:");
                    ti.setText(1, "********");

                    ti = new TableItem(infoTable, SWT.NONE);
                    ti.setText(0, "Base DN:");
                    ti.setText(1, baseDn);

                    ti = new TableItem(infoTable, SWT.NONE);
                    ti.setText(0, "Scope:");
                    ti.setText(1, sourceConfig.getParameter("scope"));

                    ti = new TableItem(infoTable, SWT.NONE);
                    ti.setText(0, "Filter:");
                    ti.setText(1, sourceConfig.getParameter("filter"));

                    setPageComplete(validatePage());

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });

        infoTable = new Table(composite, SWT.BORDER | SWT.READ_ONLY | SWT.FULL_SELECTION);
        infoTable.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
        infoTable.setLayoutData(new GridData(GridData.FILL_BOTH));

        TableColumn tc = new TableColumn(infoTable, SWT.NONE);
        tc.setWidth(100);

        tc = new TableColumn(infoTable, SWT.NONE);
        tc.setWidth(300);
/*
        Composite buttons = new Composite(composite, SWT.NONE);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        buttons.setLayoutData(gd);
        buttons.setLayout(new RowLayout());

        Button addButton = new Button(buttons, SWT.PUSH);
        addButton.setText("Add Source");

        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    SourceWizard wizard = new SourceWizard(partitionName);
                    wizard.setServer(project);

                    WizardDialog dialog = new WizardDialog(parent.getShell(), wizard);
                    dialog.setPageSize(600, 300);
                    dialog.open();

                    refresh();

                    PenroseStudio penroseStudio = PenroseStudio.getInstance();
                    penroseStudio.notifyChangeListeners();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        Button removeButton = new Button(buttons, SWT.PUSH);
        removeButton.setText("Delete Source");

        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    SourceConfig sourceConfig = getSourceConfig();
                    if (sourceConfig == null) return;

                    boolean confirm = MessageDialog.openQuestion(parent.getShell(),
                            "Confirmation", "Remove selected source?");

                    if (!confirm) return;

                    PenroseClient client = project.getClient();
                    PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
                    PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
                    SourceManagerClient sourceManagerClient = partitionClient.getSourceManagerClient();

                    sourceManagerClient.removeSource(sourceConfig.getName());

                    refresh();

                    PenroseStudio penroseStudio = PenroseStudio.getInstance();
                    penroseStudio.notifyChangeListeners();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });
*/
        refresh();
    }

    public void refresh() {
        try {
            sourceTable.removeAll();
            infoTable.removeAll();

            PenroseClient client = server.getClient();
            PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
            PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
            ConnectionManagerClient connectionManagerClient = partitionClient.getConnectionManagerClient();
            SourceManagerClient sourceManagerClient = partitionClient.getSourceManagerClient();

            for (String name : sourceManagerClient.getSourceNames()) {
                SourceClient sourceClient = sourceManagerClient.getSourceClient(name);
                SourceConfig sourceConfig = sourceClient.getSourceConfig();

                ConnectionClient connectionClient = connectionManagerClient.getConnectionClient(sourceConfig.getConnectionName());
                ConnectionConfig connectionConfig = connectionClient.getConnectionConfig();

                if (!"LDAP".equals(connectionConfig.getAdapterName())) continue;

                TableItem item = new TableItem(sourceTable, SWT.NONE);
                item.setText(sourceConfig.getName());
                item.setData(sourceConfig);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public SourceConfig getSourceConfig() {
        if (sourceTable.getSelectionCount() == 0) return null;

        TableItem item = sourceTable.getSelection()[0];
        return (SourceConfig)item.getData();
    }

    public String getSourceName() {
        if (sourceTable.getSelectionCount() == 0) return null;

        TableItem item = sourceTable.getSelection()[0];
        return item.getText();
    }

    public boolean validatePage() {
        if (getSourceConfig() == null) return false;
        return true;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public String getPartitionName() {
        return partitionName;
    }

    public void setPartitionName(String partitionName) {
        this.partitionName = partitionName;
    }
}
