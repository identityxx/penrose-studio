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
package org.safehaus.penrose.studio.mapping;

import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.SWT;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.safehaus.penrose.mapping.*;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.partition.SourceConfig;
import org.safehaus.penrose.partition.FieldConfig;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;

/**
 * @author Endi S. Dewata
 */
public class SourcesPage extends FormPage implements ModifyListener {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    CTabFolder tabFolder;

    MappingEditor editor;
    EntryMapping entry;

    public SourcesPage(MappingEditor editor) {
        super(editor, "SOURCES", "  Sources  ");

        this.editor = editor;
        this.entry = editor.entry;
    }

    public void createFormContent(IManagedForm managedForm) {
        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText("Entry Editor");

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Sources");
        section.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite sourceSection = createSourcesSection(section);
        section.setClient(sourceSection);

        load();
	}

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

        Button addButton = toolkit.createButton(buttons, "Add", SWT.PUSH);
        addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                Collection sources = editor.getPartition().getSources().getSourceConfigs();
                if (sources.size() == 0) {
                    System.out.println("There is no sources defined.");
                    return;
                }

                SourceMapping source = new SourceMapping();
                SourceDialog dialog = new SourceDialog(editor.getParent().getShell(), SWT.NONE);
                dialog.setSourceConfigs(sources);
                dialog.setSourceMapping(source);
                dialog.setText("Add source...");

                dialog.open();

                if (!dialog.isSaved()) return;

                entry.addSourceMapping(source);

                createSourceTab(tabFolder, source);
                tabFolder.setSelection(tabFolder.getItemCount()-1);

                checkDirty();
            }
        });

        Button editButton = toolkit.createButton(buttons, "Edit", SWT.PUSH);
        editButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        editButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                CTabItem item = tabFolder.getSelection();
                if (item == null) return;

                Collection sources = editor.getPartition().getSources().getSourceConfigs();
                if (sources.size() == 0) {
                    System.out.println("There is no sources defined.");
                    return;
                }

                SourceMapping source = (SourceMapping)item.getData();
                SourceDialog dialog = new SourceDialog(editor.getParent().getShell(), SWT.NONE);
                dialog.setSourceConfigs(sources);
                dialog.setSourceMapping(source);
                dialog.setText("Edit source...");

                dialog.open();

                if (!dialog.isSaved()) return;

                refresh();
                int i = entry.getSourceMappingIndex(source);
                tabFolder.setSelection(i);

                checkDirty();
            }
        });

        Button removeButton = toolkit.createButton(buttons, "Remove", SWT.PUSH);
        removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                CTabItem item = tabFolder.getSelection();
                if (item == null) return;

                SourceMapping source = (SourceMapping)item.getData();
                entry.removeSourceMapping(source.getName());
                item.dispose();

                checkDirty();
            }
        });

        new Label(buttons, SWT.NONE);

        Button moveUpButton = toolkit.createButton(buttons, "Move Up", SWT.PUSH);
        moveUpButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        moveUpButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                CTabItem item = tabFolder.getSelection();
                if (item == null) return;

                SourceMapping source = (SourceMapping)item.getData();
                int i = entry.getSourceMappingIndex(source);
                if (i == 0) return;

                entry.setSourceIndex(source, i-1);
                refresh();
                tabFolder.setSelection(i-1);

                checkDirty();
            }
        });

        Button moveDownButton = toolkit.createButton(buttons, "Move Down", SWT.PUSH);
        moveDownButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        moveDownButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                CTabItem item = tabFolder.getSelection();
                if (item == null) return;

                SourceMapping source = (SourceMapping)item.getData();
                int i = entry.getSourceMappingIndex(source);
                if (i >= entry.getSourceMappings().size()-1) return;

                entry.setSourceIndex(source, i+1);
                refresh();
                tabFolder.setSelection(i+1);

                checkDirty();
            }
        });

        return composite;
    }

    public void createSourceTab(CTabFolder tabFolder, final SourceMapping sourceMapping) {
        final CTabItem item = new CTabItem(tabFolder, SWT.NONE);
        item.setData(sourceMapping);
        item.setText(sourceMapping.getName());
        //item.setText(" "+source.getConnectionName()+" - "+source.getSourceName()+" ["+source.getName()+"]");
        //item.setFont(boldFont);

        item.setImage(PenrosePlugin.getImage(PenroseImage.SOURCE));

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
                    FieldMapping field = (FieldMapping)item.getData();
                    String name = item.getText(0);

                    if (field == null) {
                        field = new FieldMapping();
                        field.setName(name);
                    }

                    ExpressionDialog dialog = new ExpressionDialog(editor.getParent().getShell(), SWT.NONE);
                    dialog.setText("Edit field value/expression...");

                    for (AttributeMapping attributeMapping : entry.getAttributeMappings()) {
                        dialog.addVariable(attributeMapping.getName());
                    }

                    for (SourceMapping sourceMapping : entry.getSourceMappings()) {
                        for (FieldMapping fieldMapping : sourceMapping.getFieldMappings()) {
                            dialog.addVariable(sourceMapping.getName()+"."+fieldMapping.getName());
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
                    log.debug(e.getMessage(), e);
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
                FieldSelectionDialog dialog = new FieldSelectionDialog(editor.getParent().getShell(), SWT.NONE);
                dialog.setText("Add field...");

                Partition partition = editor.getPartition();
                SourceConfig sourceConfig = partition.getSources().getSourceConfig(sourceMapping.getSourceName());
                Collection fieldNames = new ArrayList();
                for (Iterator i=sourceConfig.getFieldConfigs().iterator(); i.hasNext(); ) {
                    FieldConfig fieldConfig = (FieldConfig)i.next();
                    fieldNames.add(fieldConfig.getName());
                }
                dialog.setFieldNames(fieldNames);

                dialog.open();

                for (Iterator i=dialog.getSelections().iterator(); i.hasNext(); ) {
                    String fieldName = (String)i.next();

                    FieldMapping fieldMapping = new FieldMapping(fieldName);
                    sourceMapping.addFieldMapping(fieldMapping);
                }

                refreshFields(sourceMapping, fieldTable);
                checkDirty();
            }
        });

        Hyperlink removeField = toolkit.createHyperlink(actions, "Remove field", SWT.NONE);

        removeField.addHyperlinkListener(new HyperlinkAdapter() {
            public void linkActivated(HyperlinkEvent event) {
                if (fieldTable.getSelectionCount() == 0) return;

                TableItem items[] = fieldTable.getSelection();
                for (int i=0; i<items.length; i++) {
                    FieldMapping fieldMapping = (FieldMapping)items[i].getData();
                    sourceMapping.removeFieldMapping(fieldMapping);
                }

                refreshFields(sourceMapping, fieldTable);
                checkDirty();
            }
        });

        item.setControl(composite);

        refreshFields(sourceMapping, fieldTable);
    }

    public void refreshFields(SourceMapping sourceMapping, Table table) {
        table.removeAll();

        SourceConfig sourceConfig = editor.getPartition().getSources().getSourceConfig(sourceMapping.getSourceName());

        for (Iterator i=sourceConfig.getFieldConfigs().iterator(); i.hasNext(); ) {
            FieldConfig fieldConfig = (FieldConfig)i.next();

            Collection fieldMappings = sourceMapping.getFieldMappings(fieldConfig.getName());
            if (fieldMappings == null) continue;

            for (Iterator j=fieldMappings.iterator(); j.hasNext(); ) {
                FieldMapping fieldMapping = (FieldMapping)j.next();

                String value;

                Object constant = fieldMapping.getConstant();
                if (constant != null) {
                    if (constant instanceof byte[]) {
                        value = "(binary)";
                    } else {
                        value = "\""+constant+"\"";
                    }

                } else {
                    value = fieldMapping.getVariable();
                }

                if (value == null) {
                    Expression expression = fieldMapping.getExpression();
                    value = expression == null ? null : expression.getScript();
                }

                TableItem item = new TableItem(table, SWT.CHECK);
                item.setChecked(fieldConfig.isPrimaryKey());
                item.setImage(PenrosePlugin.getImage(fieldConfig.isPrimaryKey() ? PenroseImage.KEY : PenroseImage.NOKEY));
                item.setText(0, fieldConfig.getName());
                item.setText(1, value == null ? "" : value);
                item.setData(fieldMapping);
            }
        }

    }

    public void modifyText(ModifyEvent event) {
        checkDirty();
    }

    public void checkDirty() {
        editor.checkDirty();
    }

    public void load() {
        refresh();

        Collection sources = entry.getSourceMappings();
        if (sources.size() > 0) tabFolder.setSelection(0);
    }

    public void refresh() {
        CTabItem items[] = tabFolder.getItems();
        for (int i=0; i<items.length; i++) {
            items[i].dispose();
        }

        try {
            Collection sources = entry.getSourceMappings();
            for (Iterator i=sources.iterator(); i.hasNext(); ) {
                SourceMapping source = (SourceMapping)i.next();
                log.debug("Creating source tab for "+source.getName());
                createSourceTab(tabFolder, source);
            }
        } catch (Exception e) {
            log.debug(e.getMessage(), e);
        }
    }
}
