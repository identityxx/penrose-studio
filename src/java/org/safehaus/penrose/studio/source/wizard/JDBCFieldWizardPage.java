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

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.FillLayout;
import org.safehaus.penrose.jdbc.JDBCClient;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.source.FieldConfig;
import org.safehaus.penrose.source.TableConfig;
import org.safehaus.penrose.connection.ConnectionConfig;
import org.apache.log4j.Logger;

import java.util.*;
import java.io.StringWriter;
import java.io.PrintWriter;

/**
 * @author Endi S. Dewata
 */
public class JDBCFieldWizardPage extends WizardPage {

    Logger log = Logger.getLogger(getClass());

    public final static String NAME = "Database Fields";

    Table availableTable;
    Table selectedTable;

    Text filterText;

    ConnectionConfig connectionConfig;
    TableConfig tableConfig;

    public JDBCFieldWizardPage() {
        super(NAME);

        setDescription("Select fields. Enter SQL filter (optional).");
    }

    public void createControl(final Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        GridLayout sectionLayout = new GridLayout();
        sectionLayout.numColumns = 3;
        composite.setLayout(sectionLayout);

        availableTable = new Table(composite, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
        availableTable.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite buttons = new Composite(composite, SWT.NONE);
        buttons.setSize(50, 100);
        buttons.setLayout(new FillLayout(SWT.VERTICAL));

        Button addButton = new Button(buttons, SWT.PUSH);
        addButton.setText(">");
        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                if (availableTable.getSelectionCount() == 0) return;

                Map map = new TreeMap();
                TableItem items[] = selectedTable.getItems();
                for (int i=0; i<items.length; i++) {
                    FieldConfig field = (FieldConfig)items[i].getData();
                    map.put(field.getName(), field);
                }

                items = availableTable.getSelection();
                for (int i=0; i<items.length; i++) {
                    FieldConfig field = (FieldConfig)items[i].getData();
                    map.put(field.getName(), field);
                    items[i].dispose();
                }

                selectedTable.removeAll();
                for (Iterator i=map.values().iterator(); i.hasNext(); ) {
                    FieldConfig field = (FieldConfig)i.next();
                    TableItem item = new TableItem(selectedTable, SWT.NONE);
                    item.setImage(PenrosePlugin.getImage(field.isPrimaryKey() ? PenroseImage.KEY : PenroseImage.NOKEY));
                    item.setText(field.getName());
                    item.setData(field);
                }

                setPageComplete(validatePage());
            }
        });

        Button removeButton = new Button(buttons, SWT.PUSH);
        removeButton.setText("<");
        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                if (selectedTable.getSelectionCount() == 0) return;

                Map map = new TreeMap();
                TableItem items[] = availableTable.getItems();
                for (int i=0; i<items.length; i++) {
                    FieldConfig field = (FieldConfig)items[i].getData();
                    map.put(field.getName(), field);
                }

                items = selectedTable.getSelection();
                for (int i=0; i<items.length; i++) {
                    FieldConfig field = (FieldConfig)items[i].getData();
                    map.put(field.getName(), field);
                    items[i].dispose();
                }

                availableTable.removeAll();
                for (Iterator i=map.values().iterator(); i.hasNext(); ) {
                    FieldConfig field = (FieldConfig)i.next();
                    TableItem item = new TableItem(availableTable, SWT.NONE);
                    item.setImage(PenrosePlugin.getImage(field.isPrimaryKey() ? PenroseImage.KEY : PenroseImage.NOKEY));
                    item.setText(field.getName());
                    item.setData(field);
                }

                setPageComplete(validatePage());
            }
        });

        Button addAllButton = new Button(buttons, SWT.PUSH);
        addAllButton.setText(">>");
        addAllButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                Map map = new TreeMap();
                TableItem items[] = selectedTable.getItems();
                for (int i=0; i<items.length; i++) {
                    FieldConfig field = (FieldConfig)items[i].getData();
                    map.put(field.getName(), field);
                }

                items = availableTable.getItems();
                for (int i=0; i<items.length; i++) {
                    FieldConfig field = (FieldConfig)items[i].getData();
                    map.put(field.getName(), field);
                    items[i].dispose();
                }

                selectedTable.removeAll();
                for (Iterator i=map.values().iterator(); i.hasNext(); ) {
                    FieldConfig field = (FieldConfig)i.next();
                    TableItem item = new TableItem(selectedTable, SWT.NONE);
                    item.setImage(PenrosePlugin.getImage(field.isPrimaryKey() ? PenroseImage.KEY : PenroseImage.NOKEY));
                    item.setText(field.getName());
                    item.setData(field);
                }

                setPageComplete(validatePage());
            }
        });

        Button removeAllButton = new Button(buttons, SWT.PUSH);
        removeAllButton.setText("<<");
        removeAllButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                Map map = new TreeMap();
                TableItem items[] = availableTable.getItems();
                for (int i=0; i<items.length; i++) {
                    FieldConfig field = (FieldConfig)items[i].getData();
                    map.put(field.getName(), field);
                }

                items = selectedTable.getItems();
                for (int i=0; i<items.length; i++) {
                    FieldConfig field = (FieldConfig)items[i].getData();
                    map.put(field.getName(), field);
                    items[i].dispose();
                }

                availableTable.removeAll();
                for (Iterator i=map.values().iterator(); i.hasNext(); ) {
                    FieldConfig field = (FieldConfig)i.next();
                    TableItem item = new TableItem(availableTable, SWT.NONE);
                    item.setImage(PenrosePlugin.getImage(field.isPrimaryKey() ? PenroseImage.KEY : PenroseImage.NOKEY));
                    item.setText(field.getName());
                    item.setData(field);
                }

                setPageComplete(validatePage());
            }
        });

        selectedTable = new Table(composite, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
        selectedTable.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite filterComposite = new Composite(composite, SWT.NONE);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 3;
        filterComposite.setLayoutData(gd);
        filterComposite.setLayout(new GridLayout(2, false));

        Label filterLabel = new Label(filterComposite, SWT.NONE);
        filterLabel.setText("SQL Filter:");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        filterLabel.setLayoutData(gd);

        filterText = new Text(filterComposite, SWT.BORDER);
        filterText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        new Label(filterComposite, SWT.NONE);

        Label filterExample = new Label(filterComposite, SWT.NONE);
        filterExample.setText("Example: active = 'Y' and level < 5");

        setPageComplete(validatePage());
    }

    public void setTableConfig(ConnectionConfig connectionConfig, TableConfig tableConfig) {
        this.connectionConfig = connectionConfig;
        this.tableConfig = tableConfig;
    }

    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) init();
    }

    public void init() {
        try {
            String catalog = tableConfig.getCatalog();
            String schema = tableConfig.getSchema();
            String tableName = tableConfig.getName();

            JDBCClient client = new JDBCClient(connectionConfig.getParameters());

            Collection fields = client.getColumns(catalog, schema, tableName);
            client.close();

            if (fields == null) return;

            Set set = new HashSet();
            TableItem items[] = selectedTable.getItems();
            for (int i=0; i<items.length; i++) {
                FieldConfig field = (FieldConfig)items[i].getData();
                set.add(field.getName());
            }

            availableTable.removeAll();

            for (Iterator i=fields.iterator(); i.hasNext(); ) {
                FieldConfig field = (FieldConfig)i.next();
                if (set.contains(field.getName())) continue;

                TableItem item = new TableItem(availableTable, SWT.NONE);
                item.setImage(PenrosePlugin.getImage(field.isPrimaryKey() ? PenroseImage.KEY : PenroseImage.NOKEY));
                item.setText(field.getName());
                item.setData(field);
            }

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

    public Collection getSelectedFieldConfigs() {
        Collection fieldConfigs = new ArrayList();
        TableItem items[] = selectedTable.getItems();
        for (int i=0; i<items.length; i++) {
            FieldConfig fieldConfig = (FieldConfig)items[i].getData();
            fieldConfigs.add(fieldConfig);
        }
        return fieldConfigs;
    }

    public String getFilter() {
        return "".equals(filterText.getText()) ? null : filterText.getText();
    }

    public boolean validatePage() {
        return getSelectedFieldConfigs().size() > 0;
    }
}
