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
package org.safehaus.penrose.studio.source;

import java.util.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.widgets.*;
import org.safehaus.penrose.studio.PenroseApplication;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.util.JNDIClient;
import org.safehaus.penrose.mapping.*;
import org.safehaus.penrose.partition.*;
import org.safehaus.penrose.schema.SchemaManager;
import org.safehaus.penrose.schema.Schema;
import org.safehaus.penrose.connector.ConnectionManager;
import org.apache.log4j.Logger;

public class JNDISourcePropertyPage {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

	Text sourceNameText;
    Combo connectionNameCombo;

	Text baseDnText;
	Text filterText;
	Combo scopeCombo;
	
	Table fieldTable;
	
    Button addButton;
    Button editButton;
    Button removeButton;

    JNDISourceEditor editor;
    Partition partition;
	SourceConfig source;
	
	JNDIClient client;
	
	String[] scopes = new String[] { "OBJECT", "ONELEVEL", "SUBTREE" };

    public JNDISourcePropertyPage(JNDISourceEditor editor) throws Exception {
        this.editor = editor;
        this.partition = editor.partition;
        this.source = editor.source;

        ConnectionConfig connectionConfig = partition.getConnectionConfig(source.getConnectionName());
        if (connectionConfig != null) {
            client = new JNDIClient(connectionConfig.getParameters());
        }
    }

    public Control createControl() {
        toolkit = new FormToolkit(editor.getParent().getDisplay());

        Form form = toolkit.createForm(editor.getParent());
        form.setText("Source Editor");

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Source Name");
        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Control sourceSection = createSourceSection(section);
        section.setClient(sourceSection);

        section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Directory Info");
        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Control directorySection = createDirectorySection(section);
        section.setClient(directorySection);

        section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Fields");
        section.setLayoutData(new GridData(GridData.FILL_BOTH));

        Control fieldsSection = createFieldsSection(section);
        section.setClient(fieldsSection);

        return form;
	}

	public Composite createSourceSection(Composite parent) {

		Composite composite = toolkit.createComposite(parent);
		composite.setLayout(new GridLayout(2, false));

		Label sourceNameLabel = toolkit.createLabel(composite, "Source Name:");
        GridData gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        sourceNameLabel.setLayoutData(gd);

        sourceNameText = toolkit.createText(composite, source.getName(), SWT.BORDER);
        gd = new GridData(GridData.FILL);
        gd.widthHint = 200;
		sourceNameText.setLayoutData(gd);

        sourceNameText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                source.setName(sourceNameText.getText());
                checkDirty();
            }
        });

		toolkit.createLabel(composite, "Connection Name:");

        connectionNameCombo = new Combo(composite, SWT.READ_ONLY);
        gd = new GridData(GridData.FILL);
        gd.widthHint = 200;
		connectionNameCombo.setLayoutData(gd);

        for (Iterator i=partition.getConnectionConfigs().iterator(); i.hasNext(); ) {
            ConnectionConfig connectionConfig = (ConnectionConfig)i.next();
            connectionNameCombo.add(connectionConfig.getName());
        }

        connectionNameCombo.setText(source.getConnectionName());

        connectionNameCombo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                source.setConnectionName(connectionNameCombo.getText());
                checkDirty();
            }
        });

        return composite;
	}
	
	public Composite createDirectorySection(Composite parent) {

		Composite composite = toolkit.createComposite(parent);
		composite.setLayout(new GridLayout(2, false));

		Label baseDnLabel = toolkit.createLabel(composite, "Base DN:");
        GridData gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        baseDnLabel.setLayoutData(gd);

        String s = source.getParameter("baseDn");
		baseDnText = toolkit.createText(composite, s == null ? "" : s, SWT.BORDER);
        gd = new GridData(GridData.FILL);
        gd.widthHint = 200;
        baseDnText.setLayoutData(gd);

        baseDnText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                if ("".equals(baseDnText.getText())) {
                    source.removeParameter("baseDn");
                } else {
                    source.setParameter("baseDn", baseDnText.getText());
                }
                checkDirty();
            }
        });

		toolkit.createLabel(composite, "Filter:");

        s = source.getParameter("filter");
		filterText = toolkit.createText(composite, s == null ? "" : s, SWT.BORDER);
        gd = new GridData(GridData.FILL);
        gd.widthHint = 200;
        filterText.setLayoutData(gd);

        filterText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                if ("".equals(filterText.getText())) {
                    source.removeParameter("filter");
                } else {
                    source.setParameter("filter", filterText.getText());
                }
                checkDirty();
            }
        });

		toolkit.createLabel(composite, "Scope:");

        s = source.getParameter("scope");
		scopeCombo = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
		for (int i=0; i<scopes.length; i++) {
			scopeCombo.add(scopes[i]);
            if (scopes[i].equals(s)) {
                scopeCombo.select(i);
            }
		}
        gd = new GridData(GridData.FILL);
        gd.widthHint = 200;
        scopeCombo.setLayoutData(gd);

        scopeCombo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                if ("".equals(scopeCombo.getText())) {
                    source.removeParameter("scope");
                } else {
                    source.setParameter("scope", scopeCombo.getText());
                }
                checkDirty();
            }
        });

        return composite;
    }

    public Composite createFieldsSection(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        fieldTable = toolkit.createTable(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.CHECK | SWT.MULTI);
        fieldTable.setHeaderVisible(true);
        fieldTable.setLinesVisible(true);

        fieldTable.setLayoutData(new GridData(GridData.FILL_BOTH));

		fieldTable.addMouseListener(new MouseAdapter() {
            public void mouseDoubleClick(MouseEvent event) {
                try {
                    if (fieldTable.getSelectionCount() == 0) return;

                    int index = fieldTable.getSelectionIndex();
                    TableItem item = fieldTable.getSelection()[0];
                    FieldConfig fieldDefinition = (FieldConfig)item.getData();
                    String oldName = fieldDefinition.getName();

                    Collection attributeTypes;
                    if (client == null) {
                        attributeTypes = new ArrayList();
                    } else {
                        Schema schema = client.getSchema();
                        attributeTypes = schema.getAttributeTypes();
                    }

                    JNDIFieldDialog dialog = new JNDIFieldDialog(editor.getParent().getShell(), SWT.NONE);
                    dialog.setAttributeTypes(attributeTypes);
                    dialog.setFieldConfig(fieldDefinition);
                    dialog.open();

                    if (dialog.getAction() == FieldDialog.CANCEL) return;

                    String newName = fieldDefinition.getName();

                    if (!oldName.equals(newName)) {
                        source.renameFieldConfig(oldName, newName);
                    }

                    refresh();
                    fieldTable.setSelection(index);
                    checkDirty();

                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                }
            }

			public void mouseUp(MouseEvent e) {
				for (int i=0; i<fieldTable.getItemCount(); i++) {
					TableItem item = fieldTable.getItem(i);
                    FieldConfig fieldDefinition = (FieldConfig)item.getData();
                    fieldDefinition.setPrimaryKey(item.getChecked());
					item.setImage(PenrosePlugin.getImage(item.getChecked() ? PenroseImage.KEY : PenroseImage.NOKEY));
				}

                checkDirty();
			}
		});

		fieldTable.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				for (int i=0; i<fieldTable.getItemCount(); i++) {
					TableItem item = fieldTable.getItem(i);
                    FieldConfig fieldDefinition = (FieldConfig)item.getData();
                    fieldDefinition.setPrimaryKey(item.getChecked());
					item.setImage(PenrosePlugin.getImage(item.getChecked() ? PenroseImage.KEY : PenroseImage.NOKEY));
				}

                checkDirty();
			}
		});

        TableColumn tc = new TableColumn(fieldTable, SWT.NONE);
        tc.setText("Name");
        tc.setWidth(250);

        tc = new TableColumn(fieldTable, SWT.NONE);
        tc.setText("Alias");
        tc.setWidth(250);

        tc = new TableColumn(fieldTable, SWT.NONE);
        tc.setText("Type");
        tc.setWidth(100);

        Composite buttons = toolkit.createComposite(composite);
        buttons.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        buttons.setLayout(new GridLayout());

        addButton = toolkit.createButton(buttons, "Add", SWT.PUSH);
        addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    FieldConfig fieldDefinition = new FieldConfig();

                    PenroseApplication penroseApplication = PenroseApplication.getInstance();
                    SchemaManager schemaManager = penroseApplication.getSchemaManager();
                    Collection attributeTypes = schemaManager.getAttributeTypes();

                    JNDIFieldDialog dialog = new JNDIFieldDialog(editor.getParent().getShell(), SWT.NONE);
                    dialog.setAttributeTypes(attributeTypes);
                    dialog.setFieldConfig(fieldDefinition);
                    dialog.open();

                    if (dialog.getAction() == FieldDialog.CANCEL) return;

                    source.addFieldConfig(fieldDefinition);

                    refresh();
                    checkDirty();

                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                }
            }
        });

        editButton = toolkit.createButton(buttons, "Edit", SWT.PUSH);
        editButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        editButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    if (fieldTable.getSelectionCount() == 0) return;

                    int index = fieldTable.getSelectionIndex();
                    TableItem item = fieldTable.getSelection()[0];
                    FieldConfig fieldDefinition = (FieldConfig)item.getData();
                    String oldName = fieldDefinition.getName();

                    PenroseApplication penroseApplication = PenroseApplication.getInstance();
                    SchemaManager schemaManager = penroseApplication.getSchemaManager();
                    Collection attributeTypes = schemaManager.getAttributeTypes();

                    JNDIFieldDialog dialog = new JNDIFieldDialog(editor.getParent().getShell(), SWT.NONE);
                    dialog.setAttributeTypes(attributeTypes);
                    dialog.setFieldConfig(fieldDefinition);
                    dialog.open();

                    if (dialog.getAction() == FieldDialog.CANCEL) return;

                    String newName = fieldDefinition.getName();

                    if (!oldName.equals(newName)) {
                        source.renameFieldConfig(oldName, newName);
                    }

                    refresh();
                    fieldTable.setSelection(index);
                    checkDirty();

                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                }
            }
        });

        removeButton = toolkit.createButton(buttons, "Remove", SWT.PUSH);
        removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    if (fieldTable.getSelectionCount() == 0) return;

                    TableItem items[] = fieldTable.getSelection();
                    for (int i=0; i<items.length; i++) {
                        FieldConfig fieldDefinition = (FieldConfig)items[i].getData();
                        source.removeFieldConfig(fieldDefinition);
                    }

                    refresh();
                    checkDirty();

                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                }
            }
        });

        refresh();

		return composite;
	}

    public void store() throws Exception {

        Partition partition = editor.getPartition();

        if (!sourceNameText.getText().equals(source.getName())) {

            String oldName = source.getName();
            String newName = sourceNameText.getText();

            Collection entries = partition.getEntryMappings();
            for (Iterator i=entries.iterator(); i.hasNext(); ) {
                EntryMapping entry = (EntryMapping)i.next();

                SourceMapping s = entry.removeSourceMapping(oldName);
                if (s == null) continue;

                s.setName(newName);
                entry.addSourceMapping(s);
            }

            partition.removeSourceConfig(oldName);
            source.setName(newName);
            partition.addSourceConfig(source);
        }

        source.setParameter("baseDn", baseDnText.getText());
        source.setParameter("filter", filterText.getText());
        source.setParameter("scope", scopeCombo.getText());

        TableItem[] items = fieldTable.getItems();
        source.getFieldConfigs().clear();

        for (int i=0; i<items.length; i++) {
            TableItem item = items[i];
            FieldConfig field = (FieldConfig)item.getData();
            field.setName("".equals(item.getText(1)) ? item.getText(0) : item.getText(1));
            field.setOriginalName(item.getText(0));
            field.setType(item.getText(2));
            field.setPrimaryKey(items[i].getChecked());
            source.addFieldConfig(field);
        }

        PenroseApplication penroseApplication = PenroseApplication.getInstance();
        penroseApplication.notifyChangeListeners();

        checkDirty();
    }

    public void refresh() {
        fieldTable.removeAll();

        Collection fields = source.getFieldConfigs();
        for (Iterator i=fields.iterator(); i.hasNext(); ) {
            FieldConfig fieldDefinition = (FieldConfig)i.next();

            TableItem item = new TableItem(fieldTable, SWT.CHECK);
            item.setChecked(fieldDefinition.isPrimaryKey());
            item.setImage(PenrosePlugin.getImage(fieldDefinition.isPrimaryKey() ? PenroseImage.KEY : PenroseImage.NOKEY));
            item.setText(0, fieldDefinition.getName().equals(fieldDefinition.getOriginalName()) ? fieldDefinition.getName() : fieldDefinition.getOriginalName());
            item.setText(1, fieldDefinition.getName().equals(fieldDefinition.getOriginalName()) ? "" : fieldDefinition.getName());
            item.setText(2, fieldDefinition.getType());
            item.setData(fieldDefinition);
        }
    }

    public void checkDirty() {
        editor.checkDirty();
    }

    public void load() {
        refresh();
    }

    public void dispose() {
        toolkit.dispose();
    }
}
