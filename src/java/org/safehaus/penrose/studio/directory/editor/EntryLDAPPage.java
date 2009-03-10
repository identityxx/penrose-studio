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
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.window.Window;
import org.safehaus.penrose.directory.EntryAttributeConfig;
import org.safehaus.penrose.directory.EntryConfig;
import org.safehaus.penrose.ldap.DN;
import org.safehaus.penrose.ldap.DNBuilder;
import org.safehaus.penrose.ldap.RDN;
import org.safehaus.penrose.ldap.RDNBuilder;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.schema.SchemaManagerClient;
import org.safehaus.penrose.mapping.Expression;
import org.safehaus.penrose.schema.ObjectClass;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.directory.wizard.EntryRDNWizard;
import org.safehaus.penrose.studio.directory.wizard.ObjectClassWizard;
import org.safehaus.penrose.studio.attribute.wizard.AttributesWizard;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * @author Endi S. Dewata
 */
public class EntryLDAPPage extends FormPage {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    Label rdnText;
    Label parentDnText;

    Table objectClassTable;
    Table attributeTable;

    EntryEditor editor;
    EntryConfig entryConfig;

    public EntryLDAPPage(EntryEditor editor) {
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

            Control entrySection = createDNSection(section);
            section.setClient(entrySection);

            section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
            section.setText("Object Classes");
            section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

            Control ocSection = createObjectClassesSection(section);
            section.setClient(ocSection);

            section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
            section.setText("Attributes");
            section.setLayoutData(new GridData(GridData.FILL_BOTH));

            Control atSection = createAttributesControl(section);
            section.setClient(atSection);

            refresh();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public Composite createDNSection(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        Composite leftControl = createDNLeftControl(composite);
        leftControl.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite rightControl = createDNRightControl(composite);
        GridData gd = new GridData(GridData.FILL_VERTICAL);
        gd.widthHint = 100;
        rightControl.setLayoutData(gd);

        return composite;
    }

    public Composite createDNLeftControl(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        Label rdnLabel = toolkit.createLabel(composite, "RDN:");
        GridData gd = new GridData();
        gd.widthHint = 100;
        rdnLabel.setLayoutData(gd);

        rdnText = toolkit.createLabel(composite, "", SWT.NONE);
        //rdnText.setText(entryConfig.getRdn() == null ? "" : entryConfig.getRdn().toString());
        rdnText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        //rdnText.setEditable(false);
        //rdnText.setEnabled(false);

        Label parentDnLabel = toolkit.createLabel(composite, "Parent DN:");
        parentDnLabel.setLayoutData(new GridData());

        parentDnText = toolkit.createLabel(composite, "", SWT.NONE);
        //parentDnText.setText(entryConfig.getParentDn() == null ? "" : entryConfig.getParentDn().toString());
        parentDnText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
/*
        parentDnText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                entryConfig.setDn(getDn());
                checkDirty();
            }
        });
*/
        return composite;
    }

    public Composite createDNRightControl(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);

        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);

        Button editButton = new Button(composite, SWT.PUSH);
		editButton.setText("Edit");
        editButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        editButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    EntryRDNWizard wizard = new EntryRDNWizard();
                    wizard.setEntryConfig(entryConfig);
                    wizard.setServer(editor.server);
                    wizard.setPartitionName(editor.getPartitionName());
                    wizard.setRdn(entryConfig.getRdn());
                    wizard.setParentDn(entryConfig.getParentDn());

                    WizardDialog dialog = new WizardDialog(getSite().getShell(), wizard);
                    dialog.setPageSize(600, 300);

                    int rc = dialog.open();
                    if (rc == Window.CANCEL) return;

                    editor.store();
                    refresh();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });

        return composite;
    }

    public Composite createObjectClassesSection(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        Composite leftControl = createObjectClassesLeftControl(composite);
        leftControl.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite rightControl = createObjectClassesRightControl(composite);
        GridData gd = new GridData(GridData.FILL_VERTICAL);
        gd.widthHint = 100;
        rightControl.setLayoutData(gd);

        return composite;
    }

    public Composite createObjectClassesLeftControl(final Composite parent) {

        objectClassTable = toolkit.createTable(parent, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.heightHint = 100;
        objectClassTable.setLayoutData(gd);
        objectClassTable.setLayout(new FillLayout());

        return objectClassTable;
    }

    public Composite createObjectClassesRightControl(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);

        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);

        Button editButton = new Button(composite, SWT.PUSH);
		editButton.setText("Edit");
        editButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        editButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    ObjectClassWizard wizard = new ObjectClassWizard();
                    wizard.setServer(editor.getServer());
                    wizard.setEntryConfig(entryConfig);

                    WizardDialog dialog = new WizardDialog(editor.getSite().getShell(), wizard);
                    dialog.setPageSize(600, 300);

                    int rc = dialog.open();
                    if (rc == Window.CANCEL) return;

                    editor.store();
                    refresh();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });

        return composite;
    }

    public Composite createAttributesControl(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        Composite leftControl = createAttributesLeftControl(composite);
        leftControl.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite rightControl = createAttributesRightControl(composite);
        GridData gd = new GridData(GridData.FILL_VERTICAL);
        gd.widthHint = 100;
        rightControl.setLayoutData(gd);

        return composite;
    }

    public Composite createAttributesLeftControl(final Composite parent) {

        attributeTable = new Table(parent, SWT.BORDER | SWT.FULL_SELECTION);
        attributeTable.setHeaderVisible(true);
        attributeTable.setLinesVisible(true);

        TableColumn tc = new TableColumn(attributeTable, SWT.LEFT);
        tc.setText("Attribute");
        tc.setWidth(150);

        tc = new TableColumn(attributeTable, SWT.LEFT);
        tc.setText("Value/Expression");
        tc.setWidth(350);

        tc = new TableColumn(attributeTable, SWT.LEFT);
        tc.setText("RDN");
        tc.setWidth(50);

        return attributeTable;
    }

    public Composite createAttributesRightControl(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);

        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);

        Button editButton = new Button(composite, SWT.PUSH);
		editButton.setText("Edit");
        editButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        editButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    AttributesWizard wizard = new AttributesWizard();
                    wizard.setServer(editor.getServer());
                    wizard.setPartitionName(editor.getPartitionName());
                    wizard.setEntryConfig(entryConfig);

                    WizardDialog dialog = new WizardDialog(editor.getSite().getShell(), wizard);
                    dialog.setPageSize(600, 300);

                    int rc = dialog.open();
                    if (rc == Window.CANCEL) return;

                    editor.store();
                    refresh();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });

        return composite;
    }

    public void refresh() throws Exception {

        DN dn = entryConfig.getDn();
        rdnText.setText(dn.isEmpty() ? "" : dn.getRdn().toString());
        parentDnText.setText(dn.isEmpty() ? "" : dn.getParentDn().toString());

        objectClassTable.removeAll();
        attributeTable.removeAll();

        Map<String,ObjectClass> objectClasses = getObjectClasses(entryConfig.getObjectClasses());

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
            item.setText(0, attributeConfig.getName());
            item.setText(1, value == null ? "" : value);
            item.setText(2, attributeConfig.isRdn() ? "Yes" : "");
            item.setData(attributeConfig);
        }
    }

    public void checkDirty() {
        editor.checkDirty();
    }

    public Map<String,ObjectClass> getObjectClasses(Collection<String> ocNames) throws Exception {

        Server server = editor.getServer();
        PenroseClient client = server.getClient();
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

    public void updateRdn() throws Exception {

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
        DN parentDn = entryConfig.getParentDn();

        DN dn = rdn.append(parentDn);
        entryConfig.setDn(dn);
    }

    public DN getDn() throws Exception {
        DNBuilder db = new DNBuilder();
        db.append(rdnText.getText());

        if (!"".equals(parentDnText.getText())) {
            db.append(parentDnText.getText());
        }

        return db.toDn();
    }
}
