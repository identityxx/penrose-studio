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
package org.safehaus.penrose.studio.mapping;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.ui.forms.widgets.*;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.IManagedForm;
import org.safehaus.penrose.studio.PenroseApplication;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.mapping.*;
import org.safehaus.penrose.schema.ObjectClass;
import org.safehaus.penrose.schema.SchemaManager;
import org.safehaus.penrose.partition.SourceConfig;
import org.safehaus.penrose.partition.FieldConfig;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * @author Endi S. Dewata
 */
public class LDAPPage extends FormPage {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    Text parentDnText;
    Text rdnText;

    Table objectClassTable;
    Table attributeTable;

    MappingEditor editor;
    EntryMapping entry;

    public LDAPPage(MappingEditor editor) {
        super(editor, "LDAP", "  LDAP  ");

        this.editor = editor;
        this.entry = editor.entry;
    }

    public void createFormContent(IManagedForm managedForm) {
        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText("Entry Editor");

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Distinguished Name");
        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Control entrySection = createEntrySection(section);
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
    }

    public Composite createEntrySection(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(3, false));

        Label parentDnLabel = toolkit.createLabel(composite, "Parent DN:");
        GridData gd = new GridData();
        gd.widthHint = 100;
        parentDnLabel.setLayoutData(gd);

        parentDnText = toolkit.createText(composite, "", SWT.BORDER);
        parentDnText.setText(entry.getParentDn() == null ? "" : entry.getParentDn());
        gd = new GridData(GridData.FILL_HORIZONTAL);
        parentDnText.setLayoutData(gd);

        parentDnText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                entry.setParentDn("".equals(parentDnText.getText()) ? null : parentDnText.getText());
                checkDirty();
            }
        });

        Button browseButton = toolkit.createButton(composite, "Browse...", SWT.PUSH);
        gd = new GridData();
        gd.widthHint = 100;
        browseButton.setLayoutData(gd);

        browseButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                EntrySelectionDialog dialog = new EntrySelectionDialog(parent.getShell(), SWT.NONE);
                dialog.setText("Select parent entry...");
                dialog.setPartition(editor.getPartition());
                dialog.open();

                EntryMapping parentEntry = dialog.getEntryMapping();
                if (parentEntry == null) return;

                parentDnText.setText(parentEntry.getDn());
            }
        });

        toolkit.createLabel(composite, "RDN:");

        rdnText = toolkit.createText(composite, "", SWT.BORDER);
        rdnText.setText(entry.getRdn());
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        rdnText.setLayoutData(gd);

        rdnText.setEditable(false);
        rdnText.setEnabled(false);

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

        Button addButton = toolkit.createButton(buttons, "Add", SWT.PUSH);
        addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    ObjectClassSelectionDialog dialog = new ObjectClassSelectionDialog(editor.getParent().getShell(), SWT.NONE);
                    dialog.setText("Add object classes...");

                    PenroseApplication penroseApplication = PenroseApplication.getInstance();
                    SchemaManager schemaManager = penroseApplication.getSchemaManager();

                    Collection ocNames = new ArrayList();
                    for (Iterator i=schemaManager.getObjectClasses().iterator(); i.hasNext(); ) {
                        ObjectClass objectClass = (ObjectClass)i.next();
                        ocNames.add(objectClass.getName());
                    }
                    dialog.setObjectClasses(ocNames);

                    dialog.open();

                    for (Iterator i=dialog.getSelections().iterator(); i.hasNext(); ) {
                        String objectClass = (String)i.next();
                        entry.addObjectClass(objectClass);
                    }

                    refresh();
                    checkDirty();

                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                }
            }
        });

        Button removeButton = toolkit.createButton(buttons, "Remove", SWT.PUSH);
        removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                if (objectClassTable.getSelectionCount() == 0) return;

                TableItem items[] = objectClassTable.getSelection();
                for (int i=0; i<items.length; i++) {
                    String objectClass = (String)items[i].getData();
                    entry.removeObjectClass(objectClass);
                }

                refresh();
                checkDirty();
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
                        AttributeMapping ad = (AttributeMapping)item.getData();

                        item.setImage(PenrosePlugin.getImage(item.getChecked() ? PenroseImage.KEY : PenroseImage.NOKEY));
                        ad.setRdn(item.getChecked()+"");
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
                        AttributeMapping ad = (AttributeMapping)item.getData();

                        item.setImage(PenrosePlugin.getImage(item.getChecked() ? PenroseImage.KEY : PenroseImage.NOKEY));
                        ad.setRdn(item.getChecked()+"");
                    }
                    refreshRdn();
                    checkDirty();
                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
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

        Button addButton = toolkit.createButton(buttons, "Add", SWT.PUSH);
        addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    AttributeTypeSelectionDialog dialog = new AttributeTypeSelectionDialog(editor.getParent().getShell(), SWT.NONE);
                    dialog.setText("Add attributes...");

                    PenroseApplication penroseApplication = PenroseApplication.getInstance();
                    dialog.setSchemaManager(penroseApplication.getSchemaManager());

                    dialog.open();
                    if (dialog.getAction() == AttributeTypeSelectionDialog.CANCEL) return;

                    for (Iterator i=dialog.getSelections().iterator(); i.hasNext(); ) {
                        String name = (String)i.next();
                        AttributeMapping ad = new AttributeMapping();
                        ad.setName(name);
                        entry.addAttributeMapping(ad);
                    }

                    refresh();
                    checkDirty();

                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                }
            }
        });

        Button editButton = toolkit.createButton(buttons, "Edit", SWT.PUSH);
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

        Button removeButton = toolkit.createButton(buttons, "Remove", SWT.PUSH);
        removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                if (attributeTable.getSelectionCount() == 0) return;

                TableItem items[] = attributeTable.getSelection();
                for (int i=0; i<items.length; i++) {
                    AttributeMapping ad = (AttributeMapping)items[i].getData();
                    entry.removeAttributeMapping(ad);
                }

                refresh();
                checkDirty();
            }
        });

        return composite;
    }

    public void editAttribute() throws Exception {
        if (attributeTable.getSelectionCount() == 0) return;

        TableItem item = attributeTable.getSelection()[0];
        AttributeMapping ad = (AttributeMapping)item.getData();

        ExpressionDialog dialog = new ExpressionDialog(editor.getParent().getShell(), SWT.NONE);
        dialog.setText("Edit attribute value/expression...");

        Collection sources = entry.getSourceMappings();
        for (Iterator i=sources.iterator(); i.hasNext(); ) {
            SourceMapping source = (SourceMapping)i.next();
            SourceConfig sourceConfig = editor.getPartition().getSourceConfig(source.getSourceName());
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
        refreshRdn();
        checkDirty();
    }

    public void refresh() {
        objectClassTable.removeAll();
        attributeTable.removeAll();
/*
        Map attributes = new TreeMap();

        for (Iterator i=entry.getAttributeMappings().iterator(); i.hasNext(); ) {
            AttributeMapping ad = (AttributeMapping)i.next();
            attributes.put(ad.getName(), ad);
        }
*/
        Map objectClasses = getObjectClasses(entry.getObjectClasses());
        //completeAttributeTypes(objectClasses, attributes);

        for (Iterator i=objectClasses.values().iterator(); i.hasNext(); ) {
            ObjectClass objectClass = (ObjectClass)i.next();
            String ocName = objectClass.getName();
            entry.addObjectClass(ocName);
        }

        for (Iterator i=entry.getObjectClasses().iterator(); i.hasNext(); ) {
            String ocName = (String)i.next();
        
            TableItem item = new TableItem(objectClassTable, SWT.CHECK);
            item.setText(ocName);
            item.setData(ocName);
        }

        for (Iterator i=entry.getAttributeMappings().iterator(); i.hasNext(); ) {
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

    public void checkDirty() {
        editor.checkDirty();
    }

    public Map getObjectClasses(Collection ocNames) {

        PenroseApplication penroseApplication = PenroseApplication.getInstance();
        SchemaManager schemaManager = penroseApplication.getSchemaManager();

        Map objectClasses = new TreeMap();

        for (Iterator i=ocNames.iterator(); i.hasNext(); ) {
            String ocName = (String)i.next();
            Collection ocs = schemaManager.getAllObjectClasses(ocName);

            for (Iterator j=ocs.iterator(); j.hasNext(); ) {
                ObjectClass oc = (ObjectClass)j.next();
                objectClasses.put(oc.getName(), oc);
            }
        }

        return objectClasses;
    }

    public void completeAttributeTypes(Map objectClasses, Map attributes) {

        for (Iterator i=objectClasses.values().iterator(); i.hasNext(); ) {
            ObjectClass oc = (ObjectClass)i.next();

            Collection attrs = new TreeSet();
            attrs.addAll(oc.getRequiredAttributes());
            //attrs.addAll(oc.getOptionalAttributes());

            for (Iterator j=attrs.iterator(); j.hasNext(); ) {
                String atName = (String)j.next();
                //System.out.println(" - "+atName);

                if (attributes.containsKey(atName)) continue;

                AttributeMapping ad = new AttributeMapping();
                ad.setName(atName);
                attributes.put(atName, ad);
            }
        }
    }

    public void refreshRdn() {
        Row rdn = new Row();

        //log.debug("Rdn:");

        for (Iterator i=entry.getAttributeMappings().iterator(); i.hasNext(); ) {
            AttributeMapping ad = (AttributeMapping)i.next();
            if (!"true".equals(ad.getRdn())) continue;
            String name = ad.getName();
            Object constant = ad.getConstant();
            if (constant != null) {
                //log.debug(" - constant "+name+": "+constant);
                rdn.set(name, constant);
                continue;
            }

            String variable = ad.getVariable();
            if (variable != null) {
                //log.debug(" - variable "+name+": "+variable);
                rdn.set(ad.getName(), "...");
                continue;
            }

            //log.debug(" - expression "+name+": "+ad.getExpression());
            rdn.set(name, "...");
            continue;
        }

        entry.setRdn(rdn.toString());
        rdnText.setText(rdn.toString());
    }

    public String getDn() {
        String dn = rdnText.getText();

        if (!"".equals(parentDnText.getText())) {
            dn = dn + "," + parentDnText.getText();
        }

        return dn;
    }
}
