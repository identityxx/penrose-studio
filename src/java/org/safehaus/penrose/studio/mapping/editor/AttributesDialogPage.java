package org.safehaus.penrose.studio.mapping.editor;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.jface.dialogs.MessageDialog;
import org.safehaus.penrose.mapping.AttributeMapping;
import org.safehaus.penrose.mapping.Expression;
import org.safehaus.penrose.mapping.SourceMapping;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.source.SourceConfig;
import org.safehaus.penrose.source.FieldConfig;

import java.util.Iterator;
import java.util.Collection;

/**
 * @author Endi S. Dewata
 */
public class AttributesDialogPage extends MappingDialogPage {

    Table attributeTable;

    public AttributesDialogPage(MappingDialog dialog, Composite parent, int style) {
        super(dialog, parent, style);
    }

    public void init() {
        setLayout(new GridLayout(2, false));

        attributeTable = new Table(this, SWT.BORDER | SWT.FULL_SELECTION | SWT.CHECK);
        attributeTable.setHeaderVisible(true);
        attributeTable.setLinesVisible(true);

        attributeTable.setLayoutData(new GridData(GridData.FILL_BOTH));

        attributeTable.addMouseListener(new MouseAdapter() {
            public void mouseDoubleClick(MouseEvent event) {
                try {
                    editAttribute();

                } catch (Exception e) {
                    MessageDialog.openError(
                            getShell(),
                            "ERROR",
                            e.getMessage()
                    );
                }
            }

            public void mouseUp(MouseEvent event) {
                try {
                    //boolean found = false;
                    for (int i=0; i<attributeTable.getItemCount(); i++) {
                        TableItem item = attributeTable.getItem(i);
                        AttributeMapping ad = (AttributeMapping)item.getData();

                        item.setImage(PenrosePlugin.getImage(item.getChecked() ? PenroseImage.KEY : PenroseImage.NOKEY));
                        ad.setRdn(item.getChecked()+"");
                    }

                } catch (Exception e) {
                    MessageDialog.openError(
                            getShell(),
                            "ERROR",
                            e.getMessage()
                    );
                }
            }

        });

        attributeTable.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    for (int i=0; i<attributeTable.getItemCount(); i++) {
                        TableItem item = attributeTable.getItem(i);
                        AttributeMapping ad = (AttributeMapping)item.getData();

                        item.setImage(PenrosePlugin.getImage(item.getChecked() ? PenroseImage.KEY : PenroseImage.NOKEY));
                        ad.setRdn(item.getChecked()+"");
                    }

                } catch (Exception e) {
                    MessageDialog.openError(
                            getShell(),
                            "ERROR",
                            e.getMessage()
                    );
                }
            }
        });

        TableColumn tc = new TableColumn(this.attributeTable, SWT.LEFT);
        tc.setText("Attribute");
        tc.setWidth(200);

        tc = new TableColumn(attributeTable, SWT.LEFT);
        tc.setText("Value/Expression");
        tc.setWidth(350);

        Menu menu = new Menu(attributeTable);
        attributeTable.setMenu(menu);

        MenuItem mi = new MenuItem(menu, SWT.PUSH);
        mi.setText("Edit...");

        mi.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    editAttribute();

                } catch (Exception e) {
                    MessageDialog.openError(
                            getShell(),
                            "ERROR",
                            e.getMessage()
                    );
                }
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
                    AttributeTypeSelectionDialog dialog = new AttributeTypeSelectionDialog(getShell(), SWT.NONE);
                    dialog.setText("Add attributes...");

                    dialog.setSchemaManager(server.getSchemaManager());

                    dialog.open();
                    if (dialog.getAction() == AttributeTypeSelectionDialog.CANCEL) return;

                    for (Iterator i=dialog.getSelections().iterator(); i.hasNext(); ) {
                        String name = (String)i.next();
                        AttributeMapping ad = new AttributeMapping();
                        ad.setName(name);
                        entryMapping.addAttributeMapping(ad);
                    }

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

        Button editButton = new Button(buttons, SWT.PUSH);
        editButton.setText("Edit");
        editButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        editButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    editAttribute();

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
                if (attributeTable.getSelectionCount() == 0) return;

                TableItem items[] = attributeTable.getSelection();
                for (int i=0; i<items.length; i++) {
                    AttributeMapping ad = (AttributeMapping)items[i].getData();
                    entryMapping.removeAttributeMapping(ad);
                }

                refresh();
            }
        });
    }

    public void refresh() {
        attributeTable.removeAll();

        for (Iterator i=entryMapping.getAttributeMappings().iterator(); i.hasNext(); ) {
            AttributeMapping ad = (AttributeMapping)i.next();

            String value;

            Object constant = ad.getConstant();
            if (constant != null) {
                if (constant instanceof byte[]) {
                    value = "(binary)";
                } else {
                    value = "\""+constant+"\"";
                }

            } else {
                value = ad.getVariable();
            }

            if (value == null) {
                Expression expression = ad.getExpression();
                value = expression == null ? null : expression.getScript();
            }

            TableItem item = new TableItem(attributeTable, SWT.CHECK);
            item.setChecked("true".equals(ad.getRdn()));
            item.setImage(PenrosePlugin.getImage(item.getChecked() ? PenroseImage.KEY : PenroseImage.NOKEY));
            item.setText(0, ad.getName());
            item.setText(1, value == null ? "" : value);
            item.setData(ad);
        }
    }

    public void editAttribute() throws Exception {
        if (attributeTable.getSelectionCount() == 0) return;

        TableItem item = attributeTable.getSelection()[0];
        AttributeMapping ad = (AttributeMapping)item.getData();

        ExpressionDialog dialog = new ExpressionDialog(getShell(), SWT.NONE);
        dialog.setText("Edit attribute value/expression...");

        Collection sources = entryMapping.getSourceMappings();
        for (Iterator i=sources.iterator(); i.hasNext(); ) {
            SourceMapping source = (SourceMapping)i.next();
            SourceConfig sourceConfig = partition.getSourceConfig(source.getSourceName());
            dialog.addVariable(source.getName());

            for (Iterator j=sourceConfig.getFieldConfigs().iterator(); j.hasNext(); ) {
                FieldConfig fieldDefinition = (FieldConfig)j.next();
                dialog.addVariable(source.getName()+"."+fieldDefinition.getName());
            }
        }

        dialog.setAttributeMapping(ad);

        dialog.open();

        if (dialog.getAction() == ExpressionDialog.CANCEL) return;

        //entry.addAttributeMapping(ad);

        refresh();
    }
}
