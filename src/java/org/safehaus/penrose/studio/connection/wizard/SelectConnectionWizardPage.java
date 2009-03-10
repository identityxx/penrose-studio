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
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.dialog.ErrorDialog;

import javax.naming.Context;
import java.util.Collection;
import java.util.ArrayList;

/**
 * @author Endi S. Dewata
 */
public class SelectConnectionWizardPage extends WizardPage {

    Logger log = Logger.getLogger(getClass());

    public final static String NAME = "Connection";

    Table connectionTable;
    Table infoTable;

    Server server;
    String partitionName;
    String adapterType;
    String connectionName;

    public SelectConnectionWizardPage() {
        super(NAME);
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
                TableItem item = connectionTable.getSelection()[0];
                ConnectionConfig connectionConfig = (ConnectionConfig)item.getData();
                showConnectionInfo(connectionConfig);
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

        for (ConnectionConfig connectionConfig : getConnectionConfigs()) {
            TableItem item = new TableItem(connectionTable, SWT.NONE);
            item.setText(connectionConfig.getName());
            item.setData(connectionConfig);

            if (connectionName != null && connectionName.equals(connectionConfig.getName())) {
                connectionTable.setSelection(item);
                showConnectionInfo(connectionConfig);
            }
        }

        setPageComplete(validatePage());
    }

    public void showConnectionInfo(ConnectionConfig connectionConfig) {

        infoTable.removeAll();

        String adapterName = connectionConfig.getAdapterName();

        TableItem item = new TableItem(infoTable, SWT.NONE);
        item.setText(0, "Adapter:");
        item.setText(1, adapterName);

        if ("JDBC".equals(adapterName)) {
            item = new TableItem(infoTable, SWT.NONE);
            item.setText(0, "Driver:");
            item.setText(1, connectionConfig.getParameter("driver"));

            item = new TableItem(infoTable, SWT.NONE);
            item.setText(0, "URL:");
            item.setText(1, connectionConfig.getParameter("url"));

            String username = connectionConfig.getParameter("user");
            item = new TableItem(infoTable, SWT.NONE);
            item.setText(0, "Username:");
            item.setText(1, username == null ? "" : username);

            item = new TableItem(infoTable, SWT.NONE);
            item.setText(0, "Password:");
            item.setText(1, "********");

        } else if ("LDAP".equals(adapterName)) {
            item = new TableItem(infoTable, SWT.NONE);
            item.setText(0, "URL:");
            item.setText(1, connectionConfig.getParameter(Context.PROVIDER_URL));

            String bindDn = connectionConfig.getParameter(Context.SECURITY_PRINCIPAL);
            item = new TableItem(infoTable, SWT.NONE);
            item.setText(0, "Bind DN:");
            item.setText(1, bindDn == null ? "" : bindDn);

            item = new TableItem(infoTable, SWT.NONE);
            item.setText(0, "Password:");
            item.setText(1, "********");

        } else if ("NIS".equals(adapterName)) {
            item = new TableItem(infoTable, SWT.NONE);
            item.setText(0, "URL:");
            item.setText(1, connectionConfig.getParameter(Context.PROVIDER_URL));
        }
    }

    public void setVisible(boolean b) {
        super.setVisible(b);
        if (b) refresh();
    }

    public void refresh() {
    }

    public Collection<ConnectionConfig> getConnectionConfigs() {

        Collection<ConnectionConfig> list = new ArrayList<ConnectionConfig>();

        try {
            PenroseClient client = server.getClient();
            PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
            PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
            ConnectionManagerClient connectionManagerClient = partitionClient.getConnectionManagerClient();

            for (String connectionName : connectionManagerClient.getConnectionNames()) {
                ConnectionClient connectionClient = connectionManagerClient.getConnectionClient(connectionName);
                ConnectionConfig connectionConfig = connectionClient.getConnectionConfig();

                String adapterName = connectionConfig.getAdapterName();
                if (adapterType != null && !adapterType.equals(adapterName)) continue;

                list.add(connectionConfig);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }

        return list;
    }

    public String getConnectionName() {
        if (connectionTable.getSelectionCount() == 0) return null;

        TableItem item = connectionTable.getSelection()[0];
        return item.getText();
    }

    public ConnectionConfig getConnectionConfig() {
        if (connectionTable.getSelectionCount() == 0) return null;

        TableItem item = connectionTable.getSelection()[0];
        return (ConnectionConfig)item.getData();
    }

    public boolean validatePage() {
        return getConnectionName() != null;
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

    public void setConnectionName(String connectionName) {
        this.connectionName = connectionName;
    }

    public String getAdapterType() {
        return adapterType;
    }

    public void setAdapterType(String adapterType) {
        this.adapterType = adapterType;
    }
}
