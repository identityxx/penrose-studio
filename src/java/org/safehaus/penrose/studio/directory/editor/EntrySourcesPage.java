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
package org.safehaus.penrose.studio.directory.editor;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.window.Window;
import org.safehaus.penrose.directory.EntryConfig;
import org.safehaus.penrose.directory.EntryFieldConfig;
import org.safehaus.penrose.directory.EntrySourceConfig;
import org.safehaus.penrose.mapping.Expression;
import org.safehaus.penrose.studio.directory.wizard.EntrySourceWizard;
import org.safehaus.penrose.studio.directory.wizard.SourceFieldsWizard;

import java.util.Collection;

/**
 * @author Endi S. Dewata
 */
public class EntrySourcesPage extends FormPage { //implements ModifyListener {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    Table sourcesTable;
    Table fieldsTable;

    CTabFolder tabFolder;

    EntryEditor editor;
    EntryConfig entryConfig;

    public EntrySourcesPage(EntryEditor editor) {
        super(editor, "SOURCES", "  Sources  ");

        this.editor = editor;
        this.entryConfig = editor.entryConfig;
    }

    public void createFormContent(IManagedForm managedForm) {
        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText("Entry Editor");

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section sourceSection = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        sourceSection.setText("Sources");
        sourceSection.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite sourceComponent = createSourcesSection(sourceSection);
        sourceSection.setClient(sourceComponent);

        Section fieldsSection = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        fieldsSection.setText("Fields");
        fieldsSection.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite fieldsComponent = createFieldsSection(fieldsSection);
        fieldsSection.setClient(fieldsComponent);

        refresh();
	}

    public Composite createSourcesSection(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        Composite leftControl = createSourcesLeftControl(composite);
        leftControl.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite rightControl = createSourcesRightControl(composite);
        GridData gd = new GridData(GridData.FILL_VERTICAL);
        gd.widthHint = 100;
        rightControl.setLayoutData(gd);

        return composite;
    }

    public Composite createSourcesLeftControl(final Composite parent) {

        sourcesTable = new Table(parent, SWT.BORDER | SWT.FULL_SELECTION);
        sourcesTable.setHeaderVisible(true);
        sourcesTable.setLinesVisible(true);

        sourcesTable.setLayoutData(new GridData(GridData.FILL_BOTH));

        TableColumn tc = new TableColumn(sourcesTable, SWT.LEFT);
        tc.setText("Source");
        tc.setWidth(350);

        tc = new TableColumn(sourcesTable, SWT.LEFT);
        tc.setText("Alias");
        tc.setWidth(150);

        sourcesTable.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                refreshFields();
            }
        });

        return sourcesTable;
    }

    public Composite createSourcesRightControl(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);

        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);

        Button editButton = new Button(composite, SWT.PUSH);
		editButton.setText("Edit");
        editButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        editButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    EntrySourceWizard wizard = new EntrySourceWizard();
                    wizard.setServer(editor.getServer());
                    wizard.setPartitionName(editor.getPartitionName());
                    wizard.setEntryConfig(entryConfig);

                    WizardDialog dialog = new WizardDialog(editor.getSite().getShell(), wizard);
                    dialog.setPageSize(600, 300);

                    int rc = dialog.open();
                    if (rc == Window.CANCEL) return;

                    editor.store();
                    refresh();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });

        return composite;
    }

    public Composite createFieldsSection(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        Composite leftControl = createFieldsLeftControl(composite);
        leftControl.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite rightControl = createFieldsRightControl(composite);
        GridData gd = new GridData(GridData.FILL_VERTICAL);
        gd.widthHint = 100;
        rightControl.setLayoutData(gd);

        return composite;
    }

    public Composite createFieldsLeftControl(final Composite parent) {

        fieldsTable = new Table(parent, SWT.BORDER | SWT.FULL_SELECTION);
        fieldsTable.setHeaderVisible(true);
        fieldsTable.setLinesVisible(true);

        fieldsTable.setLayoutData(new GridData(GridData.FILL_BOTH));

        TableColumn tc = new TableColumn(fieldsTable, SWT.LEFT);
        tc.setText("Field");
        tc.setWidth(150);

        tc = new TableColumn(fieldsTable, SWT.LEFT);
        tc.setText("Value/Expression");
        tc.setWidth(350);

        return fieldsTable;
    }

    public Composite createFieldsRightControl(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);

        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);

        Button editButton = new Button(composite, SWT.PUSH);
		editButton.setText("Edit");
        editButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        editButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    if (sourcesTable.getSelectionCount() != 1) return;

                    TableItem item = sourcesTable.getSelection()[0];
                    EntrySourceConfig sourceConfig = (EntrySourceConfig)item.getData();

                    SourceFieldsWizard wizard = new SourceFieldsWizard();
                    wizard.setServer(editor.getServer());
                    wizard.setPartitionName(editor.getPartitionName());
                    wizard.setEntryConfig(entryConfig);
                    wizard.setSourceConfig(sourceConfig);

                    WizardDialog dialog = new WizardDialog(editor.getSite().getShell(), wizard);
                    dialog.setPageSize(600, 300);

                    int rc = dialog.open();
                    if (rc == Window.CANCEL) return;

                    editor.store();
                    refresh();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });

        return composite;
    }
/*
    public Composite createSourcesSection(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);

        tabFolder = new CTabFolder(composite, SWT.NONE);
        tabFolder.setBorderVisible(true);
        tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));
        tabFolder.setLayout(new FillLayout(SWT.VERTICAL | SWT.HORIZONTAL));
        tabFolder.setBackground(composite.getDisplay().getSystemColor(SWT.COLOR_WHITE));

        Composite buttons = toolkit.createComposite(composite);
        buttons.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        buttons.setLayout(new GridLayout());

        Button addButton = new Button(buttons, SWT.PUSH);
		addButton.setText("Add");

        addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    Server project = editor.getServer();
                    PenroseClient client = project.getClient();
                    PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
                    PartitionClient partitionClient = partitionManagerClient.getPartitionClient(editor.getPartitionName());
                    SourceManagerClient sourceManagerClient = partitionClient.getSourceManagerClient();

                    Collection<String> sourceNames = sourceManagerClient.getSourceNames();

                    //PartitionConfig partitionConfig = editor.getLoggerConfig();
                    //Collection<SourceConfig> sourceConfigManager = partitionConfig.getSourceConfigManager().getSourceConfigManager();

                    if (sourceNames.isEmpty()) {
                        System.out.println("There is no sources defined.");
                        return;
                    }

                    Collection<SourceConfig> sourceConfigs = new ArrayList<SourceConfig>();
                    for (String sourceName : sourceNames) {
                        SourceClient sourceClient = sourceManagerClient.getSourceClient(sourceName);
                        SourceConfig sourceConfig = sourceClient.getSourceConfig();
                        sourceConfigs.add(sourceConfig);
                    }

                    EntrySourceConfig source = new EntrySourceConfig();
                    SourceDialog dialog = new SourceDialog(editor.getParent().getShell(), SWT.NONE);
                    dialog.setSourceConfigs(sourceConfigs);
                    dialog.setSourceMapping(source);
                    dialog.setText("Add source...");

                    dialog.open();

                    if (!dialog.isSaved()) return;

                    entryConfig.addSourceConfig(source);

                    createSourceTab(tabFolder, source);
                    tabFolder.setSelection(tabFolder.getItemCount()-1);

                    checkDirty();

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
                try {
                    CTabItem item = tabFolder.getSelection();
                    if (item == null) return;

                    Server project = editor.getServer();
                    PenroseClient client = project.getClient();
                    PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
                    PartitionClient partitionClient = partitionManagerClient.getPartitionClient(editor.getPartitionName());
                    SourceManagerClient sourceManagerClient = partitionClient.getSourceManagerClient();

                    Collection<String> sourceNames = sourceManagerClient.getSourceNames();

                    //PartitionConfig partitionConfig = editor.getLoggerConfig();
                    //Collection<SourceConfig> sourceConfigManager = partitionConfig.getSourceConfigManager().getSourceConfigManager();

                    if (sourceNames.isEmpty()) {
                        System.out.println("There is no sources defined.");
                        return;
                    }

                    Collection<SourceConfig> sourceConfigs = new ArrayList<SourceConfig>();
                    for (String sourceName : sourceNames) {
                        SourceClient sourceClient = sourceManagerClient.getSourceClient(sourceName);
                        SourceConfig sourceConfig = sourceClient.getSourceConfig();
                        sourceConfigs.add(sourceConfig);
                    }

                    EntrySourceConfig source = (EntrySourceConfig)item.getData();
                    SourceDialog dialog = new SourceDialog(editor.getParent().getShell(), SWT.NONE);
                    dialog.setSourceConfigs(sourceConfigs);
                    dialog.setSourceMapping(source);
                    dialog.setText("Edit source...");

                    dialog.open();

                    if (!dialog.isSaved()) return;

                    refresh();
                    int i = entryConfig.getSourceConfigIndex(source);
                    tabFolder.setSelection(i);

                    checkDirty();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });

        Button removeButton = new Button(buttons, SWT.PUSH);
		removeButton.setText("Remove");

        removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                CTabItem item = tabFolder.getSelection();
                if (item == null) return;

                EntrySourceConfig source = (EntrySourceConfig)item.getData();
                entryConfig.removeSourceConfig(source.getAlias());
                item.dispose();

                checkDirty();
            }
        });

        new Label(buttons, SWT.NONE);

        Button moveUpButton = new Button(buttons, SWT.PUSH);
		moveUpButton.setText("Move Up");

        moveUpButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        moveUpButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                CTabItem item = tabFolder.getSelection();
                if (item == null) return;

                EntrySourceConfig source = (EntrySourceConfig)item.getData();
                int i = entryConfig.getSourceConfigIndex(source);
                if (i == 0) return;

                entryConfig.setSourceIndex(source, i-1);
                refresh();
                tabFolder.setSelection(i-1);

                checkDirty();
            }
        });

        Button moveDownButton = new Button(buttons, SWT.PUSH);
		moveDownButton.setText("Move Down");

        moveDownButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        moveDownButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                CTabItem item = tabFolder.getSelection();
                if (item == null) return;

                EntrySourceConfig source = (EntrySourceConfig)item.getData();
                int i = entryConfig.getSourceConfigIndex(source);
                if (i >= entryConfig.getSourceConfigs().size()-1) return;

                entryConfig.setSourceIndex(source, i+1);
                refresh();
                tabFolder.setSelection(i+1);

                checkDirty();
            }
        });

        return composite;
    }

    public void createSourceTab(CTabFolder tabFolder, final EntrySourceConfig sourceMapping) {
        final CTabItem item = new CTabItem(tabFolder, SWT.NONE);
        item.setData(sourceMapping);
        item.setText(sourceMapping.getAlias());
        //item.setText(" "+source.getConnectionName()+" - "+source.getSourceName()+" ["+source.getName()+"]");
        //item.setFont(boldFont);

        item.setImage(PenroseStudio.getImage(PenroseImage.SOURCE));

        Composite composite = toolkit.createComposite(tabFolder);
        composite.setLayout(new GridLayout(2, false));

        Label sourceLabel = toolkit.createLabel(composite, "Source:");
        GridData gd = new GridData();
        gd.widthHint = 50;
        sourceLabel.setLayoutData(gd);

        Label sourceName = toolkit.createLabel(composite, sourceMapping.getSourceName());
        gd = new GridData(GridData.FILL_HORIZONTAL);
        sourceName.setLayoutData(gd);

        final Table fieldTable = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION);
        //editor.fieldTableMap.put(item, fieldTable);

        fieldTable.setLinesVisible(true);
        fieldTable.setHeaderVisible(true);

        gd = new GridData(GridData.FILL_BOTH);
        gd.horizontalSpan = 2;
        fieldTable.setLayoutData(gd);

        fieldTable.addMouseListener(new MouseAdapter() {
            public void mouseDoubleClick(MouseEvent event) {
                try {
                    if (fieldTable.getSelectionCount() == 0) return;
                    TableItem item = fieldTable.getSelection()[0];
                    EntryFieldConfig field = (EntryFieldConfig)item.getData();
                    String name = item.getText(0);

                    if (field == null) {
                        field = new EntryFieldConfig();
                        field.setName(name);
                    }

                    ExpressionDialog dialog = new ExpressionDialog(editor.getParent().getShell(), SWT.NONE);
                    dialog.setText("Edit field value/expression...");

                    for (EntryAttributeConfig attributeMapping : entryConfig.getAttributeConfigs()) {
                        dialog.addVariable(attributeMapping.getName());
                    }

                    for (EntrySourceConfig sourceMapping : entryConfig.getSourceConfigs()) {
                        for (EntryFieldConfig fieldMapping : sourceMapping.getFieldConfigs()) {
                            dialog.addVariable(sourceMapping.getAlias()+"."+fieldMapping.getName());
                        }
                    }

                    dialog.setFieldMapping(field);

                    System.out.println("const: "+field.getConstant());
                    System.out.println("var: "+field.getVariable());
                    System.out.println("exp: "+field.getExpression());

                    dialog.open();

                    if (dialog.getAction() == ExpressionDialog.CANCEL) return;

                    System.out.println("const: "+field.getConstant());
                    System.out.println("var: "+field.getVariable());
                    System.out.println("exp: "+field.getExpression());

                    //sourceMapping.addFieldMapping(field);

                    refreshFields(sourceMapping, fieldTable);
                    checkDirty();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        TableColumn tableColumn = new TableColumn(fieldTable, SWT.LEFT);
        tableColumn.setText("Field");
        tableColumn.setWidth(150);

        tableColumn = new TableColumn(fieldTable, SWT.LEFT);
        tableColumn.setText("Value/Expression");
        tableColumn.setWidth(350);

        Composite actions = toolkit.createComposite(composite);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        actions.setLayoutData(gd);
        actions.setLayout(new RowLayout());

        Hyperlink addField = toolkit.createHyperlink(actions, "Add field", SWT.NONE);

        addField.addHyperlinkListener(new HyperlinkAdapter() {
            public void linkActivated(HyperlinkEvent event) {
                try {
                    FieldSelectionDialog dialog = new SelectFieldDialog(editor.getParent().getShell(), SWT.NONE);
                    dialog.setText("Add field...");

                    Server project = editor.getServer();
                    PenroseClient client = project.getClient();
                    PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
                    PartitionClient partitionClient = partitionManagerClient.getPartitionClient(editor.getPartitionName());
                    SourceManagerClient sourceManagerClient = partitionClient.getSourceManagerClient();

                    SourceClient sourceClient = sourceManagerClient.getSourceClient(sourceMapping.getSourceName());
                    SourceConfig sourceConfig = sourceClient.getSourceConfig();

                    //PartitionConfig partitionConfig = editor.getLoggerConfig();
                    //SourceConfig sourceConfig = partitionConfig.getSourceConfigManager().getSourceConfig(sourceMapping.getSourceName());

                    Collection<String> fieldNames = new ArrayList<String>();
                    for (FieldConfig fieldConfig : sourceConfig.getFieldConfigs()) {
                        fieldNames.add(fieldConfig.getName());
                    }
                    dialog.setFieldNames(fieldNames);

                    dialog.open();

                    for (String fieldName : dialog.getSelections()) {

                        EntryFieldConfig fieldMapping = new EntryFieldConfig(fieldName);
                        sourceMapping.addFieldConfig(fieldMapping);
                    }

                    refreshFields(sourceMapping, fieldTable);
                    checkDirty();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });

        Hyperlink removeField = toolkit.createHyperlink(actions, "Remove field", SWT.NONE);

        removeField.addHyperlinkListener(new HyperlinkAdapter() {
            public void linkActivated(HyperlinkEvent event) {
                if (fieldTable.getSelectionCount() == 0) return;

                TableItem items[] = fieldTable.getSelection();
                for (TableItem item : items) {
                    EntryFieldConfig fieldMapping = (EntryFieldConfig) item.getData();
                    sourceMapping.removeFieldConfig(fieldMapping);
                }

                refreshFields(sourceMapping, fieldTable);
                checkDirty();
            }
        });

        item.setControl(composite);

        refreshFields(sourceMapping, fieldTable);
    }

    public void refreshFields(EntrySourceConfig sourceMapping, Table table) {
        try {
            table.removeAll();

            Server project = editor.getServer();
            PenroseClient client = project.getClient();
            PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();

            String partitionName = sourceMapping.getPartitionName();
            if (partitionName == null) partitionName = editor.getPartitionName();
            PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
            SourceManagerClient sourceManagerClient = partitionClient.getSourceManagerClient();

            SourceClient sourceClient = sourceManagerClient.getSourceClient(sourceMapping.getSourceName());
            SourceConfig sourceConfig = sourceClient.getSourceConfig();

            //PartitionConfig partitionConfig = editor.getLoggerConfig();
            //SourceConfig sourceConfig = partitionConfig.getSourceConfigManager().getSourceConfig(sourceMapping.getSourceName());

            log.debug("Source "+sourceMapping.getAlias()+" "+sourceConfig.getName()+":");
            for (FieldConfig fieldConfig : sourceConfig.getFieldConfigs()) {
                String fieldName = fieldConfig.getName();

                Collection<EntryFieldConfig> fieldMappings = sourceMapping.getFieldConfigs(fieldName);
                if (fieldMappings == null) {
                    log.debug("Field " + fieldName + " is not used in the mapping.");
                    continue;
                }

                for (EntryFieldConfig fieldMapping : fieldMappings) {

                    String value;

                    Object constant = fieldMapping.getConstant();
                    if (constant != null) {
                        if (constant instanceof byte[]) {
                            value = "(binary)";
                        } else {
                            value = "\"" + constant + "\"";
                        }

                    } else {
                        value = fieldMapping.getVariable();
                    }

                    if (value == null) {
                        Expression expression = fieldMapping.getExpression();
                        value = expression == null ? null : expression.getScript();
                    }

                    log.debug(" - " + fieldName + ": " + value);

                    TableItem item = new TableItem(table, SWT.CHECK);
                    item.setChecked(fieldConfig.isPrimaryKey());
                    item.setImage(PenroseStudio.getImage(fieldConfig.isPrimaryKey() ? PenroseImage.KEY : PenroseImage.NOKEY));
                    item.setText(0, fieldName);
                    item.setText(1, value == null ? "" : value);
                    item.setData(fieldMapping);
                }
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }
*/

    public void refresh() {
        int[] indices = sourcesTable.getSelectionIndices();

        sourcesTable.removeAll();

        Collection<EntrySourceConfig> sources = entryConfig.getSourceConfigs();
        for (EntrySourceConfig source : sources) {

            String name = source.getSourceName();
            String alias = source.getAlias();
            TableItem item = new TableItem(sourcesTable, SWT.CHECK);
            item.setText(0, name);
            item.setText(1, alias == null || alias.equals(name) ? "" : alias);
            item.setData(source);
        }

        sourcesTable.setSelection(indices);

        refreshFields();
    }

    public void refreshFields() {

        fieldsTable.removeAll();

        if (sourcesTable.getSelectionCount() != 1) return;

        TableItem item = sourcesTable.getSelection()[0];
        EntrySourceConfig source = (EntrySourceConfig)item.getData();

        for (EntryFieldConfig field : source.getFieldConfigs()) {

            String value;

            Object constant = field.getConstant();
            if (constant != null) {
                if (constant instanceof byte[]) {
                    value = "(binary)";
                } else {
                    value = "\"" + constant + "\"";
                }

            } else {
                value = field.getVariable();
            }

            if (value == null) {
                Expression expression = field.getExpression();
                value = expression == null ? null : expression.getScript();
            }

            TableItem ti = new TableItem(fieldsTable, SWT.CHECK);
            ti.setText(0, field.getName());
            ti.setText(1, value == null ? "" : value);
            ti.setData(field);
        }
    }
}
