package org.safehaus.penrose.studio.mapping.editor;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.jface.dialogs.MessageDialog;
import org.safehaus.penrose.mapping.Relationship;
import org.safehaus.penrose.mapping.SourceMapping;
import org.safehaus.penrose.source.SourceConfig;
import org.safehaus.penrose.source.FieldConfig;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author Endi S. Dewata
 */
public class RelationshipsDialogPage extends MappingDialogPage {

    Table relationshipsTable;

    public RelationshipsDialogPage(MappingDialog dialog, Composite parent, int style) {
        super(dialog, parent, style);
    }

    public void init() {
        setLayout(new GridLayout(2, false));

        relationshipsTable = new Table(this, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.heightHint = 100;
        relationshipsTable.setLayoutData(gd);
        relationshipsTable.setLayout(new FillLayout());

        relationshipsTable.addMouseListener(new MouseAdapter() {
            public void mouseDoubleClick(MouseEvent event) {
                if (relationshipsTable.getSelectionCount() == 0) return;

                TableItem item = relationshipsTable.getSelection()[0];
                Relationship relationship = (Relationship)item.getData();

                RelationshipDialog dialog = new RelationshipDialog(getShell(), SWT.NONE);
                dialog.setRelationship(relationship);
                dialog.setText("Edit relationship...");

                Collection dataSources = partition.getEffectiveSourceMappings(entryMapping);

                for (Iterator i=dataSources.iterator(); i.hasNext(); ) {
                    SourceMapping source = (SourceMapping)i.next();

                    SourceConfig sourceDefinition = partition.getSourceConfig(source.getSourceName());

                    Collection fields = sourceDefinition.getFieldConfigs();
                    for (Iterator j=fields.iterator(); j.hasNext(); ) {
                        FieldConfig field = (FieldConfig)j.next();
                        dialog.addField(source.getName()+"."+field.getName(), field.isPK());
                    }
                }
/*
                CTabItem tabItems[] = tabFolder.getItems();
                for (int i=0; i<tabItems.length; i++) {
                    CTabItem tabItem = tabItems[i];
                    Source source = (Source)tabItem.getData();

                    ConnectionConfig connectionConfig = config.getConnectionConfig(source.getConnectionName());
                    SourceConfig sourceDefinition = connectionConfig.getSourceDefinition(source.getSourceName());

                    Text aliasText = (Text)editor.aliasTextMap.get(tabItem);
                    Table fieldTable = (Table)editor.fieldTableMap.get(tabItem);

                    TableItem tableItems[] = fieldTable.getItems();
                    for (int j=0; j<tableItems.length; j++) {
                        String fieldName = tableItems[j].getText(0);
                        FieldConfig fd = sourceDefinition.getFieldDefinition(fieldName);

                        System.out.println("["+source.getConnectionName()+"/"+source.getSourceName()+"] "+aliasText.getText()+"."+fieldName+": "+fd);
                        dialog.addField(aliasText.getText()+"."+fieldName, fd.isPrimaryKey());
                    }
                }
*/
                dialog.setRelationship(relationship);

                dialog.open();
                if (dialog.getAction() == RelationshipDialog.CANCEL) return;

                refresh();
            }
        });

        Composite buttons = new Composite(this, SWT.NONE);
        buttons.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        buttons.setLayout(new GridLayout());

        Button addButton = new Button(buttons, SWT.PUSH);
        addButton.setText("Add");
        addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    Relationship relationship = new Relationship();
                    RelationshipDialog dialog = new RelationshipDialog(getShell(), SWT.NONE);
                    dialog.setRelationship(relationship);
                    dialog.setText("Add new relationship...");

                    Collection dataSources = partition.getEffectiveSourceMappings(entryMapping);

                    for (Iterator i=dataSources.iterator(); i.hasNext(); ) {
                        SourceMapping source = (SourceMapping)i.next();

                        SourceConfig sourceDefinition = partition.getSourceConfig(source.getSourceName());

                        Collection fields = sourceDefinition.getFieldConfigs();
                        for (Iterator j=fields.iterator(); j.hasNext(); ) {
                            FieldConfig field = (FieldConfig)j.next();
                            dialog.addField(source.getName()+"."+field.getName(), field.isPK());
                        }
                    }
/*
                    CTabItem tabItems[] = tabFolder.getItems();
                    for (int i=0; i<tabItems.length; i++) {
                        CTabItem tabItem = tabItems[i];
                        Source source = (Source)tabItem.getData();

                        ConnectionConfig connectionConfig = config.getConnectionConfig(source.getConnectionName());
                        SourceConfig sourceDefinition = connectionConfig.getSourceDefinition(source.getSourceName());

                        Text aliasText = (Text)editor.aliasTextMap.get(tabItem);
                        Table fieldTable = (Table)editor.fieldTableMap.get(tabItem);

                        TableItem tableItems[] = fieldTable.getItems();
                        for (int j=0; j<tableItems.length; j++) {
                            TableItem tableItem = tableItems[j];
                            String fieldName = tableItem.getText(0);
                            FieldConfig fd = sourceDefinition.getFieldDefinition(fieldName);

                            System.out.println("["+source.getConnectionName()+"/"+source.getSourceName()+"] "+aliasText.getText()+"."+fieldName+": "+fd);
                            dialog.addField(aliasText.getText()+"."+fieldName, fd.isPrimaryKey());
                        }
                    }
*/
                    dialog.open();
                    if (dialog.getAction() == RelationshipDialog.CANCEL) return;

                    entryMapping.addRelationship(relationship);

                    refresh();

                } catch (Exception e) {
                    MessageDialog.openError(
                            getShell(),
                            "ERROR",
                            e.getMessage()
                    );
                }
            }
        });

        Button removeButton = new Button(buttons, SWT.PUSH);
        removeButton.setText("Remove");
        removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                TableItem items[] = relationshipsTable.getSelection();
                for (int i=0; i<items.length; i++) {
                    Relationship relationship = (Relationship)items[i].getData();
                    entryMapping.removeRelationship(relationship);
                    items[i].dispose();
                }
            }
        });
    }

    public void refresh() {
        relationshipsTable.removeAll();

        Collection relationships = entryMapping.getRelationships();
        for (Iterator i=relationships.iterator(); i.hasNext(); ) {
            Relationship relationship = (Relationship)i.next();

            TableItem item = new TableItem(relationshipsTable, SWT.NONE);
            item.setText(relationship.getExpression());
            item.setData(relationship);
        }
    }
}
