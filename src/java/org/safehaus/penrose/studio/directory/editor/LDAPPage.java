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
 */package org.safehaus.penrose.studio.directory.editor;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.safehaus.penrose.directory.EntryAttributeConfig;
import org.safehaus.penrose.directory.EntryConfig;
import org.safehaus.penrose.directory.EntrySourceConfig;
import org.safehaus.penrose.ldap.DN;
import org.safehaus.penrose.ldap.DNBuilder;
import org.safehaus.penrose.ldap.RDN;
import org.safehaus.penrose.ldap.RDNBuilder;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.schema.SchemaManagerClient;
import org.safehaus.penrose.source.SourceClient;
import org.safehaus.penrose.mapping.Expression;
import org.safehaus.penrose.schema.ObjectClass;
import org.safehaus.penrose.source.FieldConfig;
import org.safehaus.penrose.source.SourceConfig;
import org.safehaus.penrose.source.SourceManagerClient;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudioPlugin;
import org.safehaus.penrose.studio.directory.dialog.AttributeTypeSelectionDialog;
import org.safehaus.penrose.studio.directory.dialog.ExpressionDialog;
import org.safehaus.penrose.studio.directory.dialog.*;
import org.safehaus.penrose.studio.directory.dialog.ObjectClassSelectionDialog;
import org.safehaus.penrose.studio.project.Project;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * @author Endi S. Dewata
 */
public class LDAPPage extends FormPage {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    Text parentDnText;
    Text rdnText;
    Text classNameText;

    Table objectClassTable;
    Table attributeTable;

    EntryEditor editor;
    EntryConfig entryConfig;

    public LDAPPage(EntryEditor editor) {
        super(editor, "LDAP", "  LDAP  ");

        this.editor = editor;
        this.entryConfig = editor.entryConfig;
    }

    public void createFormContent(IManagedForm managedForm) {
        try {
            toolkit = managedForm.getToolkit();

            ScrolledForm form = managedForm.getForm();
            form.setText("Entry Editor");

            Composite body = form.getBody();
            body.setLayout(new GridLayout());

            Section section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
            section.setText("Distinguished Name");
            section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

            Control entrySection = createMainSection(section);
            section.setClient(entrySection);

            section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
            section.setText("Object Classes");
            section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

            Control ocSection = createObjectClassesSection(section);
            section.setClient(ocSection);

            section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
            section.setText("Attributes");
            section.setLayoutData(new GridData(GridData.FILL_BOTH));

            Control atSection = createAttributesSection(section);
            section.setClient(atSection);

            refresh();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public Composite createMainSection(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(3, false));

        Label parentDnLabel = toolkit.createLabel(composite, "Parent DN:");
        GridData gd = new GridData();
        gd.widthHint = 100;
        parentDnLabel.setLayoutData(gd);

        parentDnText = toolkit.createText(composite, "", SWT.BORDER);
        parentDnText.setText(entryConfig.getParentDn() == null ? "" : entryConfig.getParentDn().toString());
        gd = new GridData(GridData.FILL_HORIZONTAL);
        parentDnText.setLayoutData(gd);

        parentDnText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                entryConfig.setDn(getDn());
                checkDirty();
            }
        });

        Button browseButton = new Button(composite, SWT.PUSH);
		browseButton.setText("Browse...");

        gd = new GridData();
        gd.widthHint = 100;
        browseButton.setLayoutData(gd);

        browseButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    EntrySelectionDialog dialog = new EntrySelectionDialog(parent.getShell(), SWT.NONE);
                    dialog.setText("Select parent entry...");
                    dialog.setPartitionName(editor.getPartitionName());
                    dialog.setProject(editor.project);
                    dialog.open();

                    DN dn = dialog.getDn();
                    if (dn == null) return;

                    parentDnText.setText(dn.toString());

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });

        toolkit.createLabel(composite, "RDN:");

        rdnText = toolkit.createText(composite, "", SWT.BORDER);
        rdnText.setText(entryConfig.getRdn() == null ? "" : entryConfig.getRdn().toString());
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        rdnText.setLayoutData(gd);

        rdnText.setEditable(false);
        rdnText.setEnabled(false);

        toolkit.createLabel(composite, "Class:");

        classNameText = toolkit.createText(composite, "", SWT.BORDER);
        classNameText .setText(entryConfig.getEntryClass() == null ? "" : entryConfig.getEntryClass());
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        classNameText .setLayoutData(gd);

        classNameText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                entryConfig.setEntryClass("".equals(classNameText.getText()) ? null : classNameText.getText());
                checkDirty();
            }
        });

        return composite;
    }

    public Composite createObjectClassesSection(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        composite.setLayout(layout);

        objectClassTable = toolkit.createTable(composite, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.heightHint = 100;
        objectClassTable.setLayoutData(gd);
        objectClassTable.setLayout(new FillLayout());

        Composite buttons = toolkit.createComposite(composite);
        buttons.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        buttons.setLayout(new GridLayout());

        Button addButton = new Button(buttons, SWT.PUSH);
		addButton.setText("Add");

        addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    ObjectClassSelectionDialog dialog = new ObjectClassSelectionDialog(editor.getParent().getShell(), SWT.NONE);
                    dialog.setText("Add object classes...");

                    Project project = editor.getProject();
                    PenroseClient client = project.getClient();
                    SchemaManagerClient schemaManagerClient = client.getSchemaManagerClient();

                    Collection<String> ocNames = schemaManagerClient.getObjectClassNames();
                    dialog.setObjectClasses(ocNames);

                    dialog.open();

                    for (String objectClass : dialog.getSelections()) {
                        entryConfig.addObjectClass(objectClass);
                    }

                    refresh();
                    checkDirty();

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
                try {
                    if (objectClassTable.getSelectionCount() == 0) return;

                    TableItem items[] = objectClassTable.getSelection();
                    for (TableItem item : items) {
                        String objectClass = (String) item.getData();
                        entryConfig.removeObjectClass(objectClass);
                    }

                    refresh();
                    checkDirty();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });

        return composite;
    }

    public Composite createAttributesSection(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        composite.setLayout(layout);

        attributeTable = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.CHECK);
        attributeTable.setHeaderVisible(true);
        attributeTable.setLinesVisible(true);

        attributeTable.setLayoutData(new GridData(GridData.FILL_BOTH));

        attributeTable.addMouseListener(new MouseAdapter() {
            public void mouseDoubleClick(MouseEvent event) {
                try {
                    editAttribute();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }

            public void mouseUp(MouseEvent event) {
                try {
                    //boolean found = false;
                    for (int i=0; i<attributeTable.getItemCount(); i++) {
                        TableItem item = attributeTable.getItem(i);
                        EntryAttributeConfig ad = (EntryAttributeConfig)item.getData();

                        item.setImage(PenroseStudioPlugin.getImage(item.getChecked() ? PenroseImage.KEY : PenroseImage.NOKEY));
                        ad.setRdn(item.getChecked());
                    }
                    refreshRdn();
                    checkDirty();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }

        });

        attributeTable.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    for (int i=0; i<attributeTable.getItemCount(); i++) {
                        TableItem item = attributeTable.getItem(i);
                        EntryAttributeConfig ad = (EntryAttributeConfig)item.getData();

                        item.setImage(PenroseStudioPlugin.getImage(item.getChecked() ? PenroseImage.KEY : PenroseImage.NOKEY));
                        ad.setRdn(item.getChecked());
                    }
                    refreshRdn();
                    checkDirty();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
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
                    log.error(e.getMessage(), e);
                }
            }
        });

        Composite buttons = toolkit.createComposite(composite);
        buttons.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        buttons.setLayout(new GridLayout());

        Button addButton = new Button(buttons, SWT.PUSH);
		addButton.setText("Add");

        addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    AttributeTypeSelectionDialog dialog = new AttributeTypeSelectionDialog(editor.getParent().getShell(), SWT.NONE);
                    dialog.setText("Add attributes...");

                    Project project = editor.getProject();
                    PenroseClient client = project.getClient();
                    dialog.setSchemaManagerClient(client.getSchemaManagerClient());

                    dialog.open();
                    if (dialog.getAction() == AttributeTypeSelectionDialog.CANCEL) return;

                    for (String name : dialog.getSelections()) {
                        EntryAttributeConfig ad = new EntryAttributeConfig();
                        ad.setName(name);
                        entryConfig.addAttributeConfig(ad);
                    }

                    refresh();
                    checkDirty();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
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
                    log.error(e.getMessage(), e);
                }
            }
        });

        Button removeButton = new Button(buttons, SWT.PUSH);
		removeButton.setText("Remove");

        removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    if (attributeTable.getSelectionCount() == 0) return;

                    TableItem items[] = attributeTable.getSelection();
                    for (TableItem item : items) {
                        EntryAttributeConfig attributeMapping = (EntryAttributeConfig) item.getData();
                        entryConfig.removeAttributeConfig(attributeMapping);
                    }

                    refresh();
                    checkDirty();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });

        return composite;
    }

    public void editAttribute() throws Exception {
        if (attributeTable.getSelectionCount() == 0) return;

        TableItem item = attributeTable.getSelection()[0];
        EntryAttributeConfig ad = (EntryAttributeConfig)item.getData();

        ExpressionDialog dialog = new ExpressionDialog(editor.getParent().getShell(), SWT.NONE);
        dialog.setText("Edit attribute value/expression...");

        Project project = editor.getProject();
        PenroseClient client = project.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
        PartitionClient partitionClient = partitionManagerClient.getPartitionClient(editor.getPartitionName());
        SourceManagerClient sourceManagerClient = partitionClient.getSourceManagerClient();

        //PartitionConfig partitionConfig = editor.getPartitionConfig();
        Collection<EntrySourceConfig> sources = entryConfig.getSourceConfigs();

        for (EntrySourceConfig sourceMapping : sources) {

            SourceClient sourceClient = sourceManagerClient.getSourceClient(sourceMapping.getSourceName());
            SourceConfig sourceConfig = sourceClient.getSourceConfig();

            //SourceConfig sourceConfig = partitionConfig.getSourceConfigManager().getSourceConfig(sourceMapping.getSourceName());
            dialog.addVariable(sourceMapping.getAlias());

            for (FieldConfig fieldDefinition : sourceConfig.getFieldConfigs()) {
                dialog.addVariable(sourceMapping.getAlias() + "." + fieldDefinition.getName());
            }
        }

        dialog.setAttributeMapping(ad);

        dialog.open();

        if (dialog.getAction() == ExpressionDialog.CANCEL) return;

        //entry.addEntryAttributeConfig(ad);

        refresh();
        refreshRdn();
        checkDirty();
    }

    public void refresh() throws Exception {
        objectClassTable.removeAll();
        attributeTable.removeAll();
/*
        Map attributes = new TreeMap();

        for (Iterator i=entry.getAttributeMappings().iterator(); i.hasNext(); ) {
            AttributeMapping ad = (AttributeMapping)i.next();
            attributes.put(ad.getName(), ad);
        }
*/
        Map<String,ObjectClass> objectClasses = getObjectClasses(entryConfig.getObjectClasses());
        //completeAttributeTypes(objectClasses, attributes);

        for (ObjectClass objectClass : objectClasses.values()) {
            String ocName = objectClass.getName();
            entryConfig.addObjectClass(ocName);
        }

        for (String ocName : entryConfig.getObjectClasses()) {

            TableItem item = new TableItem(objectClassTable, SWT.CHECK);
            item.setText(ocName);
            item.setData(ocName);
        }

        for (EntryAttributeConfig attributeConfig : entryConfig.getAttributeConfigs()) {

            String value;

            Object constant = attributeConfig.getConstant();
            if (constant != null) {
                if (constant instanceof byte[]) {
                    value = "(binary)";
                } else {
                    value = "\"" + constant + "\"";
                }

            } else {
                value = attributeConfig.getVariable();
            }

            if (value == null) {
                Expression expression = attributeConfig.getExpression();
                value = expression == null ? null : expression.getScript();
            }

            TableItem item = new TableItem(attributeTable, SWT.CHECK);
            item.setChecked(attributeConfig.isRdn());
            item.setImage(PenroseStudioPlugin.getImage(item.getChecked() ? PenroseImage.KEY : PenroseImage.NOKEY));
            item.setText(0, attributeConfig.getName());
            item.setText(1, value == null ? "" : value);
            item.setData(attributeConfig);
        }
    }

    public void checkDirty() {
        editor.checkDirty();
    }

    public Map<String,ObjectClass> getObjectClasses(Collection<String> ocNames) throws Exception {

        Project project = editor.getProject();
        PenroseClient client = project.getClient();
        SchemaManagerClient schemaManagerClient = client.getSchemaManagerClient();

        Map<String,ObjectClass> objectClasses = new TreeMap<String,ObjectClass>();

        for (String ocName : ocNames) {
            Collection<ObjectClass> ocs = schemaManagerClient.getAllObjectClasses(ocName);

            for (ObjectClass oc : ocs) {
                objectClasses.put(oc.getName(), oc);
            }
        }

        return objectClasses;
    }

    public void completeAttributeTypes(Map<String,ObjectClass> objectClasses, Map<String, EntryAttributeConfig> attributes) {

        for (ObjectClass oc : objectClasses.values()) {

            Collection<String> attrs = new TreeSet<String>();
            attrs.addAll(oc.getRequiredAttributes());
            //attrs.addAll(oc.getOptionalAttributes());

            for (String atName : attrs) {
                //System.out.println(" - "+atName);

                if (attributes.containsKey(atName)) continue;

                EntryAttributeConfig ad = new EntryAttributeConfig();
                ad.setName(atName);
                attributes.put(atName, ad);
            }
        }
    }

    public void refreshRdn() {

        //log.debug("Rdn:");

        RDNBuilder rb = new RDNBuilder();
        for (EntryAttributeConfig ad : entryConfig.getAttributeConfigs()) {
            if (!ad.isRdn()) continue;
            String name = ad.getName();
            Object constant = ad.getConstant();
            if (constant != null) {
                //log.debug(" - constant "+name+": "+constant);
                rb.set(name, constant);
                continue;
            }

            String variable = ad.getVariable();
            if (variable != null) {
                //log.debug(" - variable "+name+": "+variable);
                rb.set(ad.getName(), "...");
                continue;
            }

            //log.debug(" - expression "+name+": "+ad.getExpression());
            rb.set(name, "...");
        }

        RDN rdn = rb.toRdn();
        rdnText.setText(rdn.toString());
        entryConfig.setDn(getDn());
    }

    public DN getDn() {
        DNBuilder db = new DNBuilder();
        db.append(rdnText.getText());

        if (!"".equals(parentDnText.getText())) {
            db.append(parentDnText.getText());
        }

        return db.toDn();
    }
}
