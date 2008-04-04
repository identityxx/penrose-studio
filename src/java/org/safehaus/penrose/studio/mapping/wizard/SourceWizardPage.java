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
package org.safehaus.penrose.studio.mapping.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.partition.PartitionConfig;
import org.safehaus.penrose.studio.mapping.SourceDialog;
import org.safehaus.penrose.directory.EntryConfig;
import org.safehaus.penrose.directory.SourceMapping;

import java.util.Collection;

/**
 * @author Endi S. Dewata
 */
public class SourceWizardPage extends WizardPage implements SelectionListener, ModifyListener {

    public final static String NAME = "Data sources";

    Partition partition;
    EntryConfig entryConfig;
    Table sourceTable;

    public SourceWizardPage(Partition partition, EntryConfig entry) {
        super(NAME);
        this.partition = partition;
        this.entryConfig = entry;
    }

    public void createControl(final Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        GridLayout sectionLayout = new GridLayout();
        sectionLayout.numColumns = 2;
        composite.setLayout(sectionLayout);

        sourceTable = new Table(composite, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
        sourceTable.setHeaderVisible(true);
        sourceTable.setLinesVisible(true);
        sourceTable.setLayoutData(new GridData(GridData.FILL_BOTH));

        TableColumn tc = new TableColumn(sourceTable, SWT.NONE);
        tc.setText("Source");
        tc.setWidth(200);

        tc = new TableColumn(sourceTable, SWT.NONE);
        tc.setText("Alias");
        tc.setWidth(200);

        Composite buttons = new Composite(composite, SWT.NONE);
        buttons.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        buttons.setLayout(new GridLayout());

        Button addButton = new Button(buttons, SWT.PUSH);
        addButton.setText("Add");
        addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {

                PartitionConfig partitionConfig = partition.getPartitionConfig();
                Collection sources = partitionConfig.getSourceConfigManager().getSourceConfigs();
                if (sources.size() == 0) {
                    System.out.println("There is no sources defined.");
                    return;
                }

                SourceMapping source = new SourceMapping();
                SourceDialog dialog = new SourceDialog(parent.getShell(), SWT.NONE);
                dialog.setSourceConfigs(sources);
                dialog.setSourceMapping(source);
                dialog.setText("Select source...");

                dialog.open();

                if (!dialog.isSaved()) return;

                entryConfig.addSourceMapping(source);

                TableItem item = new TableItem(sourceTable, SWT.NONE);
                item.setText(0, source.getSourceName());
                item.setText(1, source.getName());
                item.setData(source);
            }
        });

        Button removeButton = new Button(buttons, SWT.PUSH);
        removeButton.setText("Remove");
        removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                TableItem items[] = sourceTable.getSelection();
                for (int i=0; i<items.length; i++) {
                    TableItem item = items[i];
                    SourceMapping source = (SourceMapping)item.getData();
                    entryConfig.removeSourceMapping(source.getName());
                    item.dispose();
                }
            }
        });

        setPageComplete(validatePage());
    }

    public boolean validatePage() {
        return true;
    }

    public void widgetSelected(SelectionEvent event) {
        setPageComplete(validatePage());
    }

    public void widgetDefaultSelected(SelectionEvent event) {
    }

    public void modifyText(ModifyEvent event) {
        setPageComplete(validatePage());
    }
}
