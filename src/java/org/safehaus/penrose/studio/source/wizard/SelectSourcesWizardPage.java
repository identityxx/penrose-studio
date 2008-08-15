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
package org.safehaus.penrose.studio.source.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.safehaus.penrose.directory.EntrySourceConfig;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.management.PenroseClient;
import org.safehaus.penrose.source.SourceClient;
import org.safehaus.penrose.source.SourceConfig;
import org.safehaus.penrose.studio.directory.dialog.SourceDialog;
import org.safehaus.penrose.studio.project.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Endi S. Dewata
 */
public class SelectSourcesWizardPage extends WizardPage implements SelectionListener, ModifyListener {

    public Logger log = LoggerFactory.getLogger(getClass());

    public final static String NAME = "Data sources";

    private Project project;
    private String partitionName;

    Table sourceTable;

    public SelectSourcesWizardPage() {
        super(NAME);
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
                try {
                    PenroseClient client = project.getClient();
                    PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
                    PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);

                    Collection<String> sourceNames = partitionClient.getSourceNames();
                    if (sourceNames.isEmpty()) {
                        System.out.println("There is no sources defined.");
                        return;
                    }

                    Collection<SourceConfig> sourceConfigs = new ArrayList<SourceConfig>();
                    for (String sourceName : sourceNames) {
                        SourceClient sourceClient = partitionClient.getSourceClient(sourceName);
                        SourceConfig sourceConfig = sourceClient.getSourceConfig();
                        sourceConfigs.add(sourceConfig);
                    }
/*
                    Collection<SourceConfig> sourceConfigManager = partitionConfig.getSourceConfigManager().getSourceConfigManager();
                    if (sourceConfigManager.isEmpty()) {
                        System.out.println("There is no sources defined.");
                        return;
                    }
*/
                    EntrySourceConfig sourceMapping = new EntrySourceConfig();
                    SourceDialog dialog = new SourceDialog(parent.getShell(), SWT.NONE);
                    dialog.setSourceConfigs(sourceConfigs);
                    dialog.setSourceMapping(sourceMapping);
                    dialog.setText("Select source...");

                    dialog.open();

                    if (!dialog.isSaved()) return;

                    TableItem item = new TableItem(sourceTable, SWT.NONE);
                    item.setText(0, sourceMapping.getSourceName());
                    item.setText(1, sourceMapping.getAlias());
                    item.setData(sourceMapping);

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
                TableItem items[] = sourceTable.getSelection();
                for (TableItem item : items) {
                    item.dispose();
                }
            }
        });

        setPageComplete(validatePage());
    }

    public Collection<EntrySourceConfig> getSourceMappings() {
        Collection<EntrySourceConfig> results = new ArrayList<EntrySourceConfig>();
        TableItem items[] = sourceTable.getItems();
        for (TableItem item : items) {
            EntrySourceConfig sourceMapping = (EntrySourceConfig) item.getData();
            results.add(sourceMapping);
        }
        return results;
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

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public String getPartitionName() {
        return partitionName;
    }

    public void setPartitionName(String partitionName) {
        this.partitionName = partitionName;
    }
}
