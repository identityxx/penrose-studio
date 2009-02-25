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
package org.safehaus.penrose.studio.directory.wizard;

import org.apache.log4j.Logger;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.safehaus.penrose.directory.EntryAttributeConfig;
import org.safehaus.penrose.directory.EntrySourceConfig;
import org.safehaus.penrose.directory.EntryFieldConfig;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.mapping.Expression;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.expression.dialog.ExpressionDialog;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.directory.dialog.SelectFieldDialog;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.source.SourceManagerClient;
import org.safehaus.penrose.source.SourceClient;
import org.safehaus.penrose.source.SourceConfig;
import org.safehaus.penrose.source.FieldConfig;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Endi S. Dewata
 */
public class SourceFieldsWizardPage extends WizardPage implements SelectionListener {

    Logger log = Logger.getLogger(getClass());

    public final static String NAME = "Fields";

    public final static int CONSTANT   = 0;
    public final static int VARIABLE   = 1;
    public final static int EXPRESSION = 2;

    Table fieldsTable;

    private Server server;
    private String partitionName;
    EntrySourceConfig entrySourceConfig;

    private Collection<EntryAttributeConfig> attributeConfigs;

    private Collection<EntryFieldConfig> fieldConfigs = new ArrayList<EntryFieldConfig>();

    private int defaultType = CONSTANT;

    public SourceFieldsWizardPage() {
        super(NAME);
        setDescription("Enter the fields of the source.");
    }

    public void createControl(final Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        composite.setLayout(new GridLayout(2, false));

        fieldsTable = new Table(composite, SWT.BORDER | SWT.CHECK | SWT.FULL_SELECTION);
        fieldsTable.setHeaderVisible(true);
        fieldsTable.setLinesVisible(true);
        fieldsTable.setLayoutData(new GridData(GridData.FILL_BOTH));

        fieldsTable.addMouseListener(new MouseAdapter() {
            public void mouseDoubleClick(MouseEvent event) {
                edit();
            }
            public void mouseUp(MouseEvent event) {
                updateImages();
                setPageComplete(validatePage());
            }
        });

        TableColumn tc = new TableColumn(fieldsTable, SWT.NONE);
        tc.setText("Fields");
        tc.setWidth(150);

        tc = new TableColumn(fieldsTable, SWT.NONE);
        tc.setText("Value/Expression");
        tc.setWidth(350);

        Composite buttons = new Composite(composite, SWT.NONE);
        buttons.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        buttons.setLayout(new GridLayout());

        Button addButton = new Button(buttons, SWT.PUSH);
        addButton.setText("Add");
        addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    SelectFieldDialog dialog = new SelectFieldDialog(parent.getShell(), SWT.NONE);
                    dialog.setText("Add field...");

                    PenroseClient client = server.getClient();
                    PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
                    PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
                    SourceManagerClient sourceManagerClient = partitionClient.getSourceManagerClient();

                    SourceClient sourceClient = sourceManagerClient.getSourceClient(entrySourceConfig.getSourceName());
                    SourceConfig sourceConfig = sourceClient.getSourceConfig();

                    Collection<String> fieldNames = new ArrayList<String>();
                    for (FieldConfig fieldConfig : sourceConfig.getFieldConfigs()) {
                        fieldNames.add(fieldConfig.getName());
                    }
                    dialog.setFieldNames(fieldNames);

                    int rc = dialog.open();
                    if (rc == Window.CANCEL) return;

                    for (String name : dialog.getSelections()) {
                        EntryFieldConfig fieldConfig = new EntryFieldConfig();
                        fieldConfig.setName(name);
                        addFieldConfig(fieldConfig);
                    }

                    refresh();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });

        Button editButton = new Button(buttons, SWT.PUSH);
        editButton.setText("Edit");
        editButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        editButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                edit();
            }
        });

        Button removeButton = new Button(buttons, SWT.PUSH);
        removeButton.setText("Remove");
        removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                if (fieldsTable.getSelectionCount() == 0) return;

                TableItem items[] = fieldsTable.getSelection();
                for (TableItem item : items) {
                    EntryFieldConfig fieldConfig = (EntryFieldConfig) item.getData();
                    removeFieldConfig(fieldConfig);
                }

                refresh();
            }
        });

        setPageComplete(validatePage());
    }

    public void edit() {
        try {
            if (fieldsTable.getSelectionCount() == 0) return;

            TableItem item = fieldsTable.getSelection()[0];
            EntryFieldConfig fieldConfig = (EntryFieldConfig)item.getData();

            ExpressionDialog dialog = new ExpressionDialog(getShell(), SWT.NONE);
            dialog.setText("Edit field value/expression...");

            if (defaultType == VARIABLE) {

                if (attributeConfigs != null) {
                    for (EntryAttributeConfig attributeConfig : attributeConfigs) {
                        dialog.addVariable(attributeConfig.getName());
                    }
                }

                dialog.setType(ExpressionDialog.VARIABLE);

            } else {
                dialog.setType(ExpressionDialog.TEXT);
            }

            dialog.setFieldConfig(fieldConfig);

            dialog.open();

            if (dialog.getAction() == ExpressionDialog.CANCEL) return;

            refresh();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void addFieldConfig(EntryFieldConfig fieldConfig) {
        fieldConfigs.add(fieldConfig);
    }

    public void removeFieldConfig(EntryFieldConfig fieldConfig) {
        fieldConfigs.remove(fieldConfig);
    }

    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) init();
    }

    public void init() {
        try {
            refresh();

            setPageComplete(validatePage());

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void refresh() {
        fieldsTable.removeAll();

        log.debug("Fields:");
        for (EntryFieldConfig fieldConfig : fieldConfigs) {

            String value;

            Object constant = fieldConfig.getConstant();
            if (constant != null) {
                if (constant instanceof byte[]) {
                    value = "(binary)";
                } else {
                    value = "\"" + constant + "\"";
                }

            } else {
                value = fieldConfig.getVariable();
            }

            if (value == null) {
                Expression expression = fieldConfig.getExpression();
                value = expression == null ? null : expression.getScript();
            }

            log.debug(" - " + fieldConfig.getName() + ": " + value);

            TableItem it = new TableItem(fieldsTable, SWT.CHECK);
            it.setImage(PenroseStudio.getImage(fieldConfig.isPrimaryKey() ? PenroseImage.KEY : PenroseImage.NOKEY));
            it.setText(0, fieldConfig.getName());
            it.setText(1, value == null ? "" : value);
            it.setChecked(fieldConfig.isPrimaryKey());
            it.setData(fieldConfig);
        }

        setPageComplete(validatePage());
    }

    public boolean validatePage() {
        return true;
    }

    public void updateImages() {
        TableItem items[] = fieldsTable.getItems();
        for (TableItem item : items) {
            EntryAttributeConfig ad = (EntryAttributeConfig) item.getData();
            ad.setRdn(item.getChecked());
            item.setImage(PenroseStudio.getImage(item.getChecked() ? PenroseImage.KEY : PenroseImage.NOKEY));
        }
    }

    public void widgetSelected(SelectionEvent event) {
        updateImages();
        setPageComplete(validatePage());
    }

    public void widgetDefaultSelected(SelectionEvent event) {
    }

    public int getDefaultType() {
        return defaultType;
    }

    public void setDefaultType(int defaultType) {
        this.defaultType = defaultType;
    }

    public Collection<EntryAttributeConfig> getAttributeConfigs() {
        return attributeConfigs;
    }

    public void setAttributeConfigs(Collection<EntryAttributeConfig> attributeConfigs) {
        this.attributeConfigs = attributeConfigs;
    }

    public Collection<EntryFieldConfig> getFieldConfigs() {
        return fieldConfigs;
    }

    public void setSourceConfig(EntrySourceConfig sourceConfig) {
        entrySourceConfig = sourceConfig;
        setFieldConfigs(sourceConfig.getFieldConfigs());
    }

    public void setFieldConfigs(Collection<EntryFieldConfig> fieldConfigs) {
        removeFieldConfigs();
        for (EntryFieldConfig fieldConfig : fieldConfigs) {
            addFieldConfig(fieldConfig);
        }
    }

    public void removeFieldConfigs() {
        fieldConfigs.clear();
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