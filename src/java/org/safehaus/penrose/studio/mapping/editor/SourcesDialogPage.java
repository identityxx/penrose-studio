package org.safehaus.penrose.studio.mapping.editor;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.jface.dialogs.MessageDialog;
import org.safehaus.penrose.mapping.SourceMapping;

import java.util.Iterator;

/**
 * @author Endi S. Dewata
 */
public class SourcesDialogPage extends MappingDialogPage {

    Table sourcesTable;

    public SourcesDialogPage(MappingDialog dialog, Composite parent, int style) {
        super(dialog, parent, style);
    }

    public void init() {
        setLayout(new GridLayout(2, false));

        sourcesTable = new Table(this, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
        sourcesTable.setHeaderVisible(true);
        sourcesTable.setLinesVisible(true);

        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.heightHint = 100;
        sourcesTable.setLayoutData(gd);
        sourcesTable.setLayout(new FillLayout());

        TableColumn tc = new TableColumn(sourcesTable, SWT.LEFT);
        tc.setText("Alias");
        tc.setWidth(200);

        tc = new TableColumn(sourcesTable, SWT.LEFT);
        tc.setText("Source");
        tc.setWidth(350);

        Composite buttons = new Composite(this, SWT.NONE);
        buttons.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        buttons.setLayout(new GridLayout());

        Button addButton = new Button(buttons, SWT.PUSH);
        addButton.setText("Add");
        addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {

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
            }
        });
    }

    public void refresh() {
        sourcesTable.removeAll();

        for (Iterator i=entryMapping.getSourceMappings().iterator(); i.hasNext(); ) {
            SourceMapping sourceMapping = (SourceMapping)i.next();
            String name = sourceMapping.getName();
            String sourceName = sourceMapping.getSourceName();

            TableItem item = new TableItem(sourcesTable, SWT.CHECK);
            item.setText(0, name);
            item.setText(1, sourceName);
            item.setData(sourceMapping);
        }
    }
}
