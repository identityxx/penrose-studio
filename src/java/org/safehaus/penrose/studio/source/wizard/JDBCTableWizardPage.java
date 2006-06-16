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
package org.safehaus.penrose.studio.source.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.safehaus.penrose.util.JDBCClient;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.source.wizard.JDBCSourceWizard;
import org.safehaus.penrose.connector.JDBCAdapter;
import org.safehaus.penrose.partition.ConnectionConfig;
import org.safehaus.penrose.partition.FieldConfig;
import org.safehaus.penrose.partition.TableConfig;
import org.apache.log4j.Logger;

import java.util.*;
import java.io.StringWriter;
import java.io.PrintWriter;

/**
 * @author Endi S. Dewata
 */
public class JDBCTableWizardPage extends WizardPage implements SelectionListener, ModifyListener {

    Logger log = Logger.getLogger(getClass());

    public final static String NAME = "Database Table";

    Combo catalogCombo;
    Combo schemaCombo;
    Text tableText;

    Button refreshButton;

    Table tableTable;
    Table fieldTable;

    private Collection fields;
    ConnectionConfig connectionConfig;

    public JDBCTableWizardPage() {
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
                    log.debug(e.getMessage(), e);

                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    String message = sw.toString();
                    if (message.length() > 500) {
                        message = message.substring(0, 500) + "...";
                    }
                    MessageDialog.openError(getShell(), "Error", "Error: "+message);
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
                    log.debug(e.getMessage(), e);

                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    String message = sw.toString();
                    if (message.length() > 500) {
                        message = message.substring(0, 500) + "...";
                    }
                    MessageDialog.openError(getShell(), "Error", "Error: "+message);
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
                    log.debug(e.getMessage(), e);

                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    String message = sw.toString();
                    if (message.length() > 500) {
                        message = message.substring(0, 500) + "...";
                    }
                    MessageDialog.openError(getShell(), "Error", "Error: "+message);
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

    public void setConnectionConfig(ConnectionConfig connectionConfig) {
        this.connectionConfig = connectionConfig;
    }

    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) init();
    }

    public void init() {
        try {
            showCatalogsAndSchemas();

            String driver   = connectionConfig.getParameter(JDBCAdapter.DRIVER);
            String username = connectionConfig.getParameter(JDBCAdapter.USER);

            if ("oracle.jdbc.driver.OracleDriver".equals(driver)) {
                schemaCombo.setText(username.toUpperCase());
            }

            showTableNames();
            setPageComplete(validatePage());

        } catch (Exception e) {
            log.debug(e.getMessage(), e);

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String message = sw.toString();
            if (message.length() > 500) {
                message = message.substring(0, 500) + "...";
            }
            MessageDialog.openError(getShell(), "Error", "Error: "+message);
        }
    }

    public void showCatalogsAndSchemas() throws Exception {
        catalogCombo.removeAll();
        schemaCombo.removeAll();

        JDBCClient client = new JDBCClient(connectionConfig.getParameters());
        client.connect();

        Collection catalogs = client.getCatalogs();
        Collection schemas = client.getSchemas();

        client.close();

        for (Iterator i=catalogs.iterator(); i.hasNext(); ) {
            String catalog = (String)i.next();
            catalogCombo.add(catalog);
        }

        for (Iterator i=schemas.iterator(); i.hasNext(); ) {
            String schema = (String)i.next();
            schemaCombo.add(schema);
        }
    }

    public void showTableNames() throws Exception {
        tableTable.removeAll();

        JDBCClient client = new JDBCClient(connectionConfig.getParameters());

        client.connect();

        Collection tables = client.getTables(getCatalog(), getSchema());

        for (Iterator i=tables.iterator(); i.hasNext(); ) {
            TableConfig tableConfig = (TableConfig)i.next();
            String tableName = tableConfig.getName();

            TableItem item = new TableItem(tableTable, SWT.NONE);
            item.setText(tableName);
            item.setData(tableConfig);
        }

        client.close();
    }

    public void showFieldNames() throws Exception {
        fieldTable.removeAll();

        if (getTableName() == null) return;

        JDBCClient client = new JDBCClient(connectionConfig.getParameters());

        client.connect();

        fields = client.getColumns(getCatalog(), getSchema(), getTableName());

        for (Iterator i=fields.iterator(); i.hasNext(); ) {
            FieldConfig field = (FieldConfig)i.next();

            TableItem it = new TableItem(fieldTable, SWT.NONE);
            it.setImage(PenrosePlugin.getImage(field.isPrimaryKey() ? PenroseImage.KEY : PenroseImage.NOKEY));
            it.setText(0, field.getName());
            it.setText(1, field.getType());
        }

        client.close();
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

    public TableConfig getTableConfig() {
        if (tableTable.getSelectionCount() == 0) {
            TableConfig tableConfig = new TableConfig(getTableName());
            tableConfig.setCatalog(getCatalog());
            tableConfig.setSchema(getSchema());
            return tableConfig;
        }
        
        TableItem ti = tableTable.getSelection()[0];
        return (TableConfig)ti.getData();
    }

    public boolean validatePage() {
        if (getTableName() == null) return false;
        return true;
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
            log.debug(e.getMessage(), e);

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String message = sw.toString();
            if (message.length() > 500) {
                message = message.substring(0, 500) + "...";
            }
            MessageDialog.openError(getShell(), "Error", "Error: "+message);
        }
    }

    public void widgetDefaultSelected(SelectionEvent event) {
    }

    public Collection getFields() {
        return fields;
    }

    public void setFields(Collection fields) {
        this.fields = fields;
    }

    public void modifyText(ModifyEvent event) {
        setPageComplete(validatePage());
    }
}
