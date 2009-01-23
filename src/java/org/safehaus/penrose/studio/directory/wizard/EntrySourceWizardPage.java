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
package org.safehaus.penrose.studio.directory.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.safehaus.penrose.directory.EntrySourceConfig;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.source.SourceClient;
import org.safehaus.penrose.source.SourceConfig;
import org.safehaus.penrose.source.SourceManagerClient;
import org.safehaus.penrose.studio.directory.dialog.SourceDialog;
import org.safehaus.penrose.studio.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Endi S. Dewata
 */
public class EntrySourceWizardPage extends WizardPage implements SelectionListener, ModifyListener {

    public Logger log = LoggerFactory.getLogger(getClass());

    public final static String NAME = "Sources";

    Table sourcesTable;

    private Server server;
    private String partitionName;
    private List<EntrySourceConfig> entrySourceConfigs = new ArrayList<EntrySourceConfig>();

    public EntrySourceWizardPage() {
        super(NAME);
    }

    public void createControl(final Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        composite.setLayout(new GridLayout(2, false));

        sourcesTable = new Table(composite, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
        sourcesTable.setHeaderVisible(true);
        sourcesTable.setLinesVisible(true);
        sourcesTable.setLayoutData(new GridData(GridData.FILL_BOTH));

        TableColumn tc = new TableColumn(sourcesTable, SWT.NONE);
        tc.setText("Source");
        tc.setWidth(250);

        tc = new TableColumn(sourcesTable, SWT.NONE);
        tc.setText("Alias");
        tc.setWidth(150);

        Composite buttons = new Composite(composite, SWT.NONE);
        buttons.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        buttons.setLayout(new GridLayout());

        Button addButton = new Button(buttons, SWT.PUSH);
        addButton.setText("Add");
        addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    PenroseClient client = server.getClient();
                    PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
                    PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
                    SourceManagerClient sourceManagerClient = partitionClient.getSourceManagerClient();

                    Collection<String> sourceNames = sourceManagerClient.getSourceNames();
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

                    EntrySourceConfig sourceConfig = new EntrySourceConfig();

                    SourceDialog dialog = new SourceDialog(parent.getShell(), SWT.NONE);
                    dialog.setSourceConfigs(sourceConfigs);
                    dialog.setSourceConfig(sourceConfig);
                    dialog.setText("Select source...");

                    dialog.open();

                    if (!dialog.isSaved()) return;

                    entrySourceConfigs.add(sourceConfig);

                    refresh();
                    sourcesTable.setSelection(sourcesTable.getItemCount()-1);

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
                    if (sourcesTable.getSelectionCount() != 1) return;

                    TableItem item = sourcesTable.getSelection()[0];
                    int indices[] = sourcesTable.getSelectionIndices();

                    PenroseClient client = server.getClient();
                    PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
                    PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
                    SourceManagerClient sourceManagerClient = partitionClient.getSourceManagerClient();

                    Collection<String> sourceNames = sourceManagerClient.getSourceNames();
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

                    EntrySourceConfig sourceConfig = (EntrySourceConfig)item.getData();
                    SourceDialog dialog = new SourceDialog(parent.getShell(), SWT.NONE);
                    dialog.setSourceConfigs(sourceConfigs);
                    dialog.setSourceConfig(sourceConfig);
                    dialog.setText("Edit source...");

                    dialog.open();

                    if (!dialog.isSaved()) return;

                    refresh();
                    sourcesTable.setSelection(indices);

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
                TableItem items[] = sourcesTable.getSelection();
                int indices[] = sourcesTable.getSelectionIndices();

                for (TableItem item : items) {
                    EntrySourceConfig sourceConfig = (EntrySourceConfig)item.getData();
                    entrySourceConfigs.remove(sourceConfig);
                }

                refresh();
                sourcesTable.setSelection(indices);
            }
        });

        new Label(buttons, SWT.NONE);

        Button moveUpButton = new Button(buttons, SWT.PUSH);
		moveUpButton.setText("Move Up");

        moveUpButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        moveUpButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                if (sourcesTable.getSelectionCount() != 1) return;

                TableItem item = sourcesTable.getSelection()[0];

                EntrySourceConfig sourceConfig = (EntrySourceConfig)item.getData();
                int i = entrySourceConfigs.indexOf(sourceConfig);
                if (i == 0) return;

                entrySourceConfigs.remove(i);
                entrySourceConfigs.add(i-1, sourceConfig);

                refresh();
                sourcesTable.setSelection(i-1);
            }
        });

        Button moveDownButton = new Button(buttons, SWT.PUSH);
		moveDownButton.setText("Move Down");

        moveDownButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        moveDownButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                if (sourcesTable.getSelectionCount() != 1) return;

                TableItem item = sourcesTable.getSelection()[0];

                EntrySourceConfig sourceConfig = (EntrySourceConfig)item.getData();
                int i = entrySourceConfigs.indexOf(sourceConfig);
                if (i >= entrySourceConfigs.size()-1) return;

                entrySourceConfigs.remove(i);
                entrySourceConfigs.add(i+1, sourceConfig);

                refresh();
                sourcesTable.setSelection(i+1);
            }
        });

        refresh();

        setPageComplete(validatePage());
    }

    public Collection<EntrySourceConfig> getEntrySourceConfigs() {
        return entrySourceConfigs;
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

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public String getPartitionName() {
        return partitionName;
    }

    public void setPartitionName(String partitionName) {
        this.partitionName = partitionName;
    }

    public void setEntrySourceConfigs(Collection<EntrySourceConfig> entrySourceConfigs) {
        try {
            this.entrySourceConfigs.clear();

            for (EntrySourceConfig entrySourceConfig : entrySourceConfigs) {
                EntrySourceConfig esc = (EntrySourceConfig)entrySourceConfig.clone();
                this.entrySourceConfigs.add(esc);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public void refresh() {

        sourcesTable.removeAll();

        for (EntrySourceConfig sourceConfig : entrySourceConfigs) {
            String name = sourceConfig.getSourceName();
            String alias = sourceConfig.getAlias();

            TableItem item = new TableItem(sourcesTable, SWT.NONE);
            item.setText(0, name);
            item.setText(1, alias == null || alias.equals(name) ? "" : alias);
            item.setData(sourceConfig);
        }
    }
}
