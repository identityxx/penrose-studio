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
package org.safehaus.penrose.studio.nis.source.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.safehaus.penrose.schema.Schema;
import org.safehaus.penrose.schema.AttributeType;
import org.safehaus.penrose.schema.ObjectClass;
import org.safehaus.penrose.connection.ConnectionConfig;
import org.safehaus.penrose.source.FieldConfig;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * @author Endi S. Dewata
 */
public class NISSourceFieldsWizardPage extends WizardPage {

    Logger log = Logger.getLogger(getClass());

    public final static String NAME = "Source Fields";

    Combo objectClassCombo;

    Table availableTable;
    Table selectedTable;

    ConnectionConfig connectionConfig;
    Collection<String> attributeNames;
    Schema schema;

    private Map<String,FieldConfig> availableFieldConfigs;
    private Map<String,FieldConfig> selectedFieldConfigs;

    public NISSourceFieldsWizardPage() {
        this(new ArrayList<String>());
    }

    public NISSourceFieldsWizardPage(Collection<String> attributeNames) {
        super(NAME);

        this.attributeNames = attributeNames;

        setDescription("Select attributes to be used as source fields.");
    }

    public void createControl(final Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        composite.setLayout(new GridLayout(3, false));

        Label objectClassLabel = new Label(composite, SWT.NONE);
        objectClassLabel.setText("Object Class:");
        objectClassLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        new Label(composite, SWT.NONE);

        Label label2 = new Label(composite, SWT.NONE);
        label2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        objectClassCombo = new Combo(composite, SWT.READ_ONLY);
        objectClassCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        objectClassCombo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                refreshAvailableFieldConfigs();
            }
        });

        new Label(composite, SWT.NONE);
        new Label(composite, SWT.NONE);

        new Label(composite, SWT.NONE);
        new Label(composite, SWT.NONE);
        new Label(composite, SWT.NONE);

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
                selectFieldConfigs(availableTable.getSelection());
                refreshSelectedFieldConfigs();
                setPageComplete(validatePage());
            }
        });

        Button removeButton = new Button(buttons, SWT.PUSH);
        removeButton.setText("<");
        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                unselectFieldConfigs(selectedTable.getSelection());
                objectClassCombo.select(0);
                refreshAvailableFieldConfigs();
                setPageComplete(validatePage());
            }
        });

        Button addAllButton = new Button(buttons, SWT.PUSH);
        addAllButton.setText(">>");
        addAllButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                selectFieldConfigs(availableTable.getItems());
                refreshSelectedFieldConfigs();
                setPageComplete(validatePage());
            }
        });

        Button removeAllButton = new Button(buttons, SWT.PUSH);
        removeAllButton.setText("<<");
        removeAllButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                unselectFieldConfigs(selectedTable.getItems());
                objectClassCombo.select(0);
                refreshAvailableFieldConfigs();
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
            objectClassCombo.removeAll();
            objectClassCombo.add("");

            Collection<String> ocNames = schema.getObjectClassNames();
            for (String ocName : ocNames) {
                objectClassCombo.add(ocName);
            }

            refreshAvailableFieldConfigs();
            refreshSelectedFieldConfigs();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }

    public void refreshAvailableFieldConfigs() {

        availableFieldConfigs.clear();

        String ocName = objectClassCombo.getText();

        if ("".equals(ocName)) {

            for (AttributeType attributeType : schema.getAttributeTypes()) {
                FieldConfig fieldConfig = convert(attributeType);
                availableFieldConfigs.put(fieldConfig.getName(), fieldConfig);
            }

        } else {

            for (ObjectClass objectClass : schema.getAllObjectClasses(ocName)) {

                Collection<String> reqAttrs = objectClass.getRequiredAttributes();
                for (String atName : reqAttrs) {
                    AttributeType attributeType = schema.getAttributeType(atName);
                    FieldConfig fieldConfig = convert(attributeType);
                    availableFieldConfigs.put(fieldConfig.getName(), fieldConfig);
                }

                Collection<String> optAttrs = objectClass.getOptionalAttributes();
                for (String atName : optAttrs) {
                    AttributeType attributeType = schema.getAttributeType(atName);
                    FieldConfig fieldConfig = convert(attributeType);
                    availableFieldConfigs.put(fieldConfig.getName(), fieldConfig);
                }
            }
        }

        availableTable.removeAll();

        for (FieldConfig fieldConfig : availableFieldConfigs.values()) {
            String name = fieldConfig.getName();
            if (selectedFieldConfigs.containsKey(name)) continue;

            TableItem item = new TableItem(availableTable, SWT.NONE);
            item.setImage(PenroseStudio.getImage(fieldConfig.isPrimaryKey() ? PenroseImage.KEY : PenroseImage.NOKEY));
            item.setText(name);
            item.setData(fieldConfig);
        }
    }

    public void refreshSelectedFieldConfigs() {
        selectedTable.removeAll();

        for (FieldConfig fieldConfig : selectedFieldConfigs.values()) {
            String name = fieldConfig.getName();

            TableItem item = new TableItem(selectedTable, SWT.NONE);
            item.setImage(PenroseStudio.getImage(fieldConfig.isPrimaryKey() ? PenroseImage.KEY : PenroseImage.NOKEY));
            item.setText(name);
            item.setData(fieldConfig);
        }
    }

    public void selectFieldConfigs(TableItem[] items) {
        for (TableItem item : items) {
            FieldConfig fieldConfig = (FieldConfig) item.getData();
            String name = fieldConfig.getName();

            selectedFieldConfigs.put(fieldConfig.getName(), fieldConfig);
            availableFieldConfigs.remove(name);

            item.dispose();
        }
    }

    public void unselectFieldConfigs(TableItem[] items) {
        for (TableItem item : items) {
            FieldConfig fieldConfig = (FieldConfig) item.getData();
            String name = fieldConfig.getName();

            availableFieldConfigs.put(fieldConfig.getName(), fieldConfig);
            selectedFieldConfigs.remove(name);

            item.dispose();
        }
    }

    public FieldConfig convert(AttributeType attributeType) {
        String name = attributeType.getName();
        //String syntax = attributeType.getSyntax();

        FieldConfig fieldConfig = new FieldConfig();
        fieldConfig.setName(name);

        return fieldConfig;
    }

    public boolean validatePage() {
        return true;
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

    public void setSchema(Schema schema) {
        this.schema = schema;
    }
}