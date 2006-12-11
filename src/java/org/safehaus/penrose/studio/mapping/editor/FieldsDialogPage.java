package org.safehaus.penrose.studio.mapping.editor;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.jface.dialogs.MessageDialog;
import org.safehaus.penrose.studio.event.SelectionEvent;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.mapping.SourceMapping;
import org.safehaus.penrose.mapping.FieldMapping;
import org.safehaus.penrose.mapping.Expression;

import java.util.Iterator;

/**
 * @author Endi S. Dewata
 */
public class FieldsDialogPage extends MappingDialogPage {

    Combo sourceCombo;
    Table fieldsTable;

    public FieldsDialogPage(MappingDialog dialog, Composite parent, int style) {
        super(dialog, parent, style);
    }

    public void init() {
        setLayout(new GridLayout(2, false));

        Label sourceLabel = new Label(this, SWT.NONE);
        sourceLabel.setText("Source:");

        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.heightHint = 100;
        sourceLabel.setLayoutData(gd);

        sourceCombo = new Combo(this, SWT.NONE);
        sourceCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        sourceCombo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                try {
                    String name = sourceCombo.getText();
                    name = name.substring(name.indexOf(" ("));

                    SourceMapping sourceMapping = entryMapping.getSourceMapping(name);
                    showFieldMappings(sourceMapping);

                } catch (Exception e) {
                    MessageDialog.openError(
                            getShell(),
                            "ERROR",
                            e.getMessage()
                    );
                }
            }
        });

        fieldsTable = new Table(this, SWT.BORDER | SWT.FULL_SELECTION);
        fieldsTable.setLinesVisible(true);
        fieldsTable.setHeaderVisible(true);

        gd = new GridData(GridData.FILL_BOTH);
        gd.horizontalSpan = 2;
        fieldsTable.setLayoutData(gd);

        TableColumn tableColumn = new TableColumn(fieldsTable, SWT.LEFT);
        tableColumn.setText("Field");
        tableColumn.setWidth(150);

        tableColumn = new TableColumn(fieldsTable, SWT.LEFT);
        tableColumn.setText("Value/Expression");
        tableColumn.setWidth(350);
    }

    public void refresh() {
        sourceCombo.removeAll();

        for (Iterator i=entryMapping.getSourceMappings().iterator(); i.hasNext(); ) {
            SourceMapping sourceMapping = (SourceMapping)i.next();
            String name = sourceMapping.getName();
            String sourceName = sourceMapping.getSourceName();

            sourceCombo.add(name+" ("+sourceName+")");
        }
    }

    public void showFieldMappings(SourceMapping sourceMapping) {
        fieldsTable.removeAll();

        for (Iterator i=sourceMapping.getFieldMappings().iterator(); i.hasNext(); ) {
            FieldMapping fieldMapping = (FieldMapping)i.next();

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

            TableItem item = new TableItem(fieldsTable, SWT.NONE);
            item.setText(0, fieldMapping.getName());
            item.setText(1, value == null ? "" : value);
            item.setData(fieldMapping);
        }
    }
}
