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
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.safehaus.penrose.directory.EntrySourceConfig;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.source.SourceClient;
import org.safehaus.penrose.mapping.Relationship;
import org.safehaus.penrose.source.FieldConfig;
import org.safehaus.penrose.source.SourceConfig;
import org.safehaus.penrose.source.SourceManagerClient;
import org.safehaus.penrose.studio.directory.dialog.RelationshipDialog;
import org.safehaus.penrose.studio.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Endi S. Dewata
 */
public class RelationshipWizardPage extends WizardPage implements SelectionListener, ModifyListener {

    public Logger log = LoggerFactory.getLogger(getClass());
    
    public final static String NAME = "Data source relationships";

    Server server;
    String partitionName;
    Table relationshipTable;

    private Collection<EntrySourceConfig> sourceMappings;

    public RelationshipWizardPage(Server server, String partitionName) {
        super(NAME);
        this.server = server;
        this.partitionName = partitionName;
        setDescription("Add data source relationships. This step is optional.");
    }

    public void createControl(final Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        GridLayout sectionLayout = new GridLayout();
        sectionLayout.numColumns = 2;
        composite.setLayout(sectionLayout);

        relationshipTable = new Table(composite, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
        relationshipTable.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite buttons = new Composite(composite, SWT.NONE);
        buttons.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        buttons.setLayout(new GridLayout());

        Button addButton = new Button(buttons, SWT.PUSH);
        addButton.setText("Add");
        addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    Relationship relationship = new Relationship();
                    RelationshipDialog dialog = new RelationshipDialog(parent.getShell(), SWT.NONE);
                    dialog.setRelationship(relationship);
                    dialog.setText("Add new relationship...");

                    PenroseClient client = server.getClient();
                    PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
                    PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
                    SourceManagerClient sourceManagerClient = partitionClient.getSourceManagerClient();

                    for (EntrySourceConfig sourceMapping : sourceMappings) {

                        SourceClient sourceClient = sourceManagerClient.getSourceClient(sourceMapping.getSourceName());
                        SourceConfig sourceConfig = sourceClient.getSourceConfig();

                        //SourceConfig sourceConfig = partitionConfig.getSourceConfigManager().getSourceConfig(sourceMapping.getSourceName());

                        Collection<FieldConfig> fields = sourceConfig.getFieldConfigs();
                        for (FieldConfig fieldConfig : fields) {
                            dialog.addField(sourceMapping.getAlias() + "." + fieldConfig.getName(), fieldConfig.isPrimaryKey());
                        }
                    }

                    dialog.open();
                    if (dialog.getAction() == RelationshipDialog.CANCEL) return;

                    TableItem item = new TableItem(relationshipTable, SWT.NONE);
                    item.setText(relationship.getExpression());
                    item.setData(relationship);

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
                TableItem items[] = relationshipTable.getSelection();
                for (TableItem item : items) {
                    item.dispose();
                }
            }
        });

        setPageComplete(validatePage());
    }

    public Collection<Relationship> getRelationships() {
        Collection<Relationship> results = new ArrayList<Relationship>();
        TableItem items[] = relationshipTable.getItems();
        for (TableItem item : items) {
            Relationship relationship = (Relationship) item.getData();
            results.add(relationship);
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

    public Collection getSourceMappings() {
        return sourceMappings;
    }

    public void setSourceMappings(Collection<EntrySourceConfig> sourceMappings) {
        this.sourceMappings = sourceMappings;
    }
}
