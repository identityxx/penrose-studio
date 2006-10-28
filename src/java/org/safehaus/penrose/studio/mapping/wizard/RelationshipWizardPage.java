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
import org.safehaus.penrose.mapping.*;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.source.SourceConfig;
import org.safehaus.penrose.source.FieldConfig;
import org.safehaus.penrose.studio.mapping.RelationshipDialog;

import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;

/**
 * @author Endi S. Dewata
 */
public class RelationshipWizardPage extends WizardPage implements SelectionListener, ModifyListener {

    public final static String NAME = "Data source relationships";

    Partition partition;
    Table relationshipTable;

    private Collection sourceMappings;

    public RelationshipWizardPage(Partition partition) {
        super(NAME);
        this.partition = partition;
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

                Relationship relationship = new Relationship();
                RelationshipDialog dialog = new RelationshipDialog(parent.getShell(), SWT.NONE);
                dialog.setRelationship(relationship);
                dialog.setText("Add new relationship...");

                for (Iterator i=sourceMappings.iterator(); i.hasNext(); ) {
                    SourceMapping source = (SourceMapping)i.next();

                    SourceConfig sourceDefinition = partition.getSourceConfig(source.getSourceName());

                    Collection fields = sourceDefinition.getFieldConfigs();
                    for (Iterator j=fields.iterator(); j.hasNext(); ) {
                        FieldConfig field = (FieldConfig)j.next();
                        dialog.addField(source.getName()+"."+field.getName(), field.isPK());
                    }
                }

                dialog.open();
                if (dialog.getAction() == RelationshipDialog.CANCEL) return;

                TableItem item = new TableItem(relationshipTable, SWT.NONE);
                item.setText(relationship.getExpression());
                item.setData(relationship);
            }
        });

        Button removeButton = new Button(buttons, SWT.PUSH);
        removeButton.setText("Remove");
        removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                TableItem items[] = relationshipTable.getSelection();
                for (int i=0; i<items.length; i++) {
                    TableItem item = items[i];
                    item.dispose();
                }
            }
        });

        setPageComplete(validatePage());
    }

    public Collection getRelationships() {
        Collection results = new ArrayList();
        TableItem items[] = relationshipTable.getItems();
        for (int i=0; i<items.length; i++) {
            TableItem item = items[i];
            Relationship relationship = (Relationship)item.getData();
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

    public void setSourceMappings(Collection sourceMappings) {
        this.sourceMappings = sourceMappings;
    }
}
