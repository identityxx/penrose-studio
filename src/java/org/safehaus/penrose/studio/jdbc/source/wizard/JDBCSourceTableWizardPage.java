/**
 * Copyright 2009 Red Hat, Inc.
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
package org.safehaus.penrose.studio.jdbc.source.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.safehaus.penrose.jdbc.*;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.connection.ConnectionConfig;
import org.safehaus.penrose.connection.ConnectionManagerClient;
import org.safehaus.penrose.connection.ConnectionClient;
import org.safehaus.penrose.source.FieldConfig;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.partition.PartitionClient;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * @author Endi S. Dewata
 */
public class JDBCSourceTableWizardPage extends WizardPage implements SelectionListener, ModifyListener {

    Logger log = Logger.getLogger(getClass());

    public final static String NAME = "Database Table";

    Combo catalogCombo;
    Combo schemaCombo;
    Text tableText;

    Button refreshButton;

    Table tableTable;
    Table fieldTable;

    private Server server;
    private String partitionName;
    Collection<FieldConfig> fields;
    String connectionName;

    public JDBCSourceTableWizardPage() {
        super(NAME);
        setDescription("Select a database table.");
    }

    public void createControl(final Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout());
        setControl(composite);

        Composite top = new Composite(composite, SWT.NONE);
        top.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        top.setLayout(new GridLayout(3, false));

        Label catalogLabel = new Label(top, SWT.NONE);
        catalogLabel.setText("Catalog:");

        catalogCombo = new Combo(top, SWT.NONE);
        catalogCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        catalogCombo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    showTableNames();
                    showFieldNames();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        refreshButton = new Button(top, SWT.PUSH);
        refreshButton.setText("Refresh");

        GridData gd = new GridData(GridData.FILL, GridData.FILL, false, false, 1, 3);
        gd.widthHint = 100;
        refreshButton.setLayoutData(gd);

        refreshButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    showTableNames();
                    showFieldNames();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        Label schemaLabel = new Label(top, SWT.NONE);
        schemaLabel.setText("Schema:");

        schemaCombo = new Combo(top, SWT.NONE);
        schemaCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        schemaCombo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    showTableNames();
                    showFieldNames();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        Label tableLabel = new Label(top, SWT.NONE);
        tableLabel.setText("Table:");

        tableText = new Text(top, SWT.BORDER);
        tableText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        tableText.addModifyListener(this);

        Composite bottom = new Composite(composite, SWT.NONE);
        bottom.setLayoutData(new GridData(GridData.FILL_BOTH));
        bottom.setLayout(new GridLayout(2, false));

        tableTable = new Table(bottom, SWT.BORDER);
        gd = new GridData(GridData.FILL_BOTH);
        gd.widthHint = 100;
        tableTable.setLayoutData(gd);
        tableTable.addSelectionListener(this);

        fieldTable = new Table(bottom, SWT.BORDER | SWT.READ_ONLY | SWT.FULL_SELECTION);
        fieldTable.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
        fieldTable.setHeaderVisible(true);
        fieldTable.setLinesVisible(false);
        fieldTable.setLayoutData(new GridData(GridData.FILL_BOTH));

        TableColumn tc = new TableColumn(fieldTable, SWT.NONE);
        tc.setText("Field");
        tc.setWidth(200);

        tc = new TableColumn(fieldTable, SWT.NONE);
        tc.setText("Type");
        tc.setWidth(100);

        setPageComplete(validatePage());
    }

    public void setConnectionName(String connectionName) {
        this.connectionName = connectionName;
    }

    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) init();
    }

    public void init() {
        try {
            showCatalogsAndSchemas();
            showTableNames();
            setPageComplete(validatePage());

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }

    public void showCatalogsAndSchemas() throws Exception {
        catalogCombo.removeAll();
        schemaCombo.removeAll();

        PenroseClient client = server.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
        PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
        ConnectionManagerClient connectionManagerClient = partitionClient.getConnectionManagerClient();
        ConnectionClient connectionClient = connectionManagerClient.getConnectionClient(connectionName);

        Collection<String> catalogs = (Collection<String>)connectionClient.getAttribute("Catalogs");

        //JDBCClient client = new JDBCClient(connectionConfig.getParameters());
        //Collection<String> catalogs = client.getCatalogs();

        for (String catalog : catalogs) {
            catalogCombo.add(catalog);
        }

        Collection<String> schemas = (Collection<String>)connectionClient.getAttribute("Schemas");
        //Collection<String> schemas = client.getSchemas();

        for (String schema : schemas) {
            schemaCombo.add(schema);
        }

        ConnectionConfig connectionConfig = connectionClient.getConnectionConfig();
        String driver   = connectionConfig.getParameter(JDBC.DRIVER);
        String username = connectionConfig.getParameter(JDBC.USER);

        if ("oracle.jdbc.driver.OracleDriver".equals(driver)) {
            schemaCombo.setText(username.toUpperCase());
        }

        //client.close();
    }

    public void showTableNames() throws Exception {
        tableTable.removeAll();

        PenroseClient client = server.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
        PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
        ConnectionManagerClient connectionManagerClient = partitionClient.getConnectionManagerClient();
        ConnectionClient connectionClient = connectionManagerClient.getConnectionClient(connectionName);

        //JDBCClient client = new JDBCClient(connectionConfig.getParameters());

        try {
            Collection<org.safehaus.penrose.jdbc.Table> tables = (Collection<org.safehaus.penrose.jdbc.Table>)connectionClient.invoke(
                    "getTables",
                    new Object[] { getCatalog(), getSchema() },
                    new String[] { String.class.getName(), String.class.getName() }
            );
            //Collection<org.safehaus.penrose.jdbc.Table> tables = client.getTables(getCatalog(), getSchema());

            for (org.safehaus.penrose.jdbc.Table tableConfig : tables) {
                String tableName = tableConfig.getName();

                TableItem item = new TableItem(tableTable, SWT.NONE);
                item.setText(tableName);
                item.setData(tableConfig);
            }
            
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }

        //client.close();
    }

    public void showFieldNames() throws Exception {
        fieldTable.removeAll();

        if (getTableName() == null) return;

        PenroseClient client = server.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
        PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
        ConnectionManagerClient connectionManagerClient = partitionClient.getConnectionManagerClient();
        ConnectionClient connectionClient = connectionManagerClient.getConnectionClient(connectionName);

        //JDBCClient client = new JDBCClient(connectionConfig.getParameters());

        fields = (Collection<FieldConfig>)connectionClient.invoke(
                "getColumns",
                new Object[] { getCatalog(), getSchema(), getTableName() },
                new String[] { String.class.getName(), String.class.getName(), String.class.getName() }
        );
        //fields = client.getColumns(getCatalog(), getSchema(), getTableName());

        for (FieldConfig field : fields) {
            TableItem it = new TableItem(fieldTable, SWT.NONE);
            it.setImage(PenroseStudio.getImage(field.isPrimaryKey() ? PenroseImage.KEY : PenroseImage.NOKEY));
            it.setText(0, field.getName());
            it.setText(1, field.getType());
        }

        //client.close();
    }

    public String getCatalog() {
        return "".equals(catalogCombo.getText()) ? null : catalogCombo.getText();
    }

    public String getSchema() {
        return "".equals(schemaCombo.getText()) ? null : schemaCombo.getText();
    }

    public String getTableName() {
        return "".equals(tableText.getText()) ? null : tableText.getText();
    }

    public org.safehaus.penrose.jdbc.Table getTable() {
        if (tableTable.getSelectionCount() == 0) {
            org.safehaus.penrose.jdbc.Table table = new org.safehaus.penrose.jdbc.Table(getTableName());
            table.setCatalog(getCatalog());
            table.setSchema(getSchema());
            return table;
        }
        
        TableItem ti = tableTable.getSelection()[0];
        return (org.safehaus.penrose.jdbc.Table)ti.getData();
    }

    public boolean validatePage() {
        return getTableName() != null;
    }

    public void widgetSelected(SelectionEvent event) {
        if (tableTable.getSelectionCount() == 0) return;

        try {
            TableItem item = tableTable.getSelection()[0];
            String tableName = item.getText();
            tableText.setText(tableName);

            showFieldNames();
            setPageComplete(validatePage());

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }

    public void widgetDefaultSelected(SelectionEvent event) {
    }

    public Collection getFields() {
        return fields;
    }

    public void setFields(Collection<FieldConfig> fields) {
        this.fields = fields;
    }

    public void modifyText(ModifyEvent event) {
        setPageComplete(validatePage());
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
