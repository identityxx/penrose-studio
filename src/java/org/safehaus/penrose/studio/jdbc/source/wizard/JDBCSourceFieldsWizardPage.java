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
package org.safehaus.penrose.studio.jdbc.source.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.FillLayout;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.source.FieldConfig;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * @author Endi S. Dewata
 */
public class JDBCSourceFieldsWizardPage extends WizardPage {

    Logger log = Logger.getLogger(getClass());

    public final static String NAME = "Source Fields";

    Table availableTable;
    Table selectedTable;

    private Map<String,FieldConfig> availableFieldConfigs;
    private Map<String,FieldConfig> selectedFieldConfigs;

    public JDBCSourceFieldsWizardPage() {
        super(NAME);

        setDescription("Select columns to be used as source fields.");
    }

    public void createControl(final Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        composite.setLayout(new GridLayout(3, false));

        Label adapterLabel = new Label(composite, SWT.NONE);
        adapterLabel.setText("Available:");

        new Label(composite, SWT.NONE);

        Label infoLabel = new Label(composite, SWT.NONE);
        infoLabel.setText("Selected:");

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

                for (TableItem item : availableTable.getSelection()) {
                    FieldConfig fieldConfig = (FieldConfig) item.getData();

                    selectedFieldConfigs.put(fieldConfig.getName(), fieldConfig);
                    availableFieldConfigs.remove(fieldConfig.getName());
                }

                refresh();
                setPageComplete(validatePage());
            }
        });

        Button removeButton = new Button(buttons, SWT.PUSH);
        removeButton.setText("<");
        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                if (selectedTable.getSelectionCount() == 0) return;

                for (TableItem item : selectedTable.getSelection()) {
                    FieldConfig fieldConfig = (FieldConfig) item.getData();

                    availableFieldConfigs.put(fieldConfig.getName(), fieldConfig);
                    selectedFieldConfigs.remove(fieldConfig.getName());
                }

                refresh();
                setPageComplete(validatePage());
            }
        });

        Button addAllButton = new Button(buttons, SWT.PUSH);
        addAllButton.setText(">>");
        addAllButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                selectedFieldConfigs.putAll(availableFieldConfigs);
                availableFieldConfigs.clear();

                refresh();
                setPageComplete(validatePage());
            }
        });

        Button removeAllButton = new Button(buttons, SWT.PUSH);
        removeAllButton.setText("<<");
        removeAllButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                availableFieldConfigs.putAll(selectedFieldConfigs);
                selectedFieldConfigs.clear();

                refresh();
                setPageComplete(validatePage());
            }
        });

        selectedTable = new Table(composite, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
        selectedTable.setLayoutData(new GridData(GridData.FILL_BOTH));

        setPageComplete(validatePage());
    }

    public void setVisible(boolean b) {
        super.setVisible(b);
        if (b) refresh();
    }

    public void refresh() {
        try {
            availableTable.removeAll();

            for (FieldConfig fieldConfig : availableFieldConfigs.values()) {
                String name = fieldConfig.getName();
                if (selectedFieldConfigs.containsKey(name)) continue;

                TableItem item = new TableItem(availableTable, SWT.NONE);
                item.setImage(PenroseStudio.getImage(fieldConfig.isPrimaryKey() ? PenroseImage.KEY : PenroseImage.NOKEY));
                item.setText(name);
                item.setData(fieldConfig);
            }

            selectedTable.removeAll();

            for (FieldConfig fieldConfig : selectedFieldConfigs.values()) {
                String name = fieldConfig.getName();

                TableItem item = new TableItem(selectedTable, SWT.NONE);
                item.setImage(PenroseStudio.getImage(fieldConfig.isPrimaryKey() ? PenroseImage.KEY : PenroseImage.NOKEY));
                item.setText(name);
                item.setData(fieldConfig);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }

    public boolean validatePage() {
        return getSelectedFieldConfigs().size() > 0;
    }

    public Map<String, FieldConfig> getAvailableFieldConfigs() {
        return availableFieldConfigs;
    }

    public void setAvailableFieldConfigs(Map<String, FieldConfig> availableFieldConfigs) {
        this.availableFieldConfigs = availableFieldConfigs;
    }

    public Map<String, FieldConfig> getSelectedFieldConfigs() {
        return selectedFieldConfigs;
    }

    public void setSelectedFieldConfigs(Map<String, FieldConfig> selectedFieldConfigs) {
        this.selectedFieldConfigs = selectedFieldConfigs;
    }
}
