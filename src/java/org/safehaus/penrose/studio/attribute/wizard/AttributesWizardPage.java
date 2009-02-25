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
package org.safehaus.penrose.studio.attribute.wizard;

import org.apache.log4j.Logger;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.safehaus.penrose.directory.EntryAttributeConfig;
import org.safehaus.penrose.directory.EntrySourceConfig;
import org.safehaus.penrose.ldap.RDN;
import org.safehaus.penrose.mapping.Expression;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.server.Server;

import java.util.*;

/**
 * @author Endi S. Dewata
 */
public class AttributesWizardPage extends WizardPage implements SelectionListener {

    Logger log = Logger.getLogger(getClass());

    public final static String NAME = "Attributes";

    public final static int CONSTANT   = 0;
    public final static int VARIABLE   = 1;
    public final static int EXPRESSION = 2;

    Table attributesTable;

    private Server server;
    private String partitionName;

    private Collection<EntrySourceConfig> entrySourceConfigs;
    private Collection<String> objectClasses = new ArrayList<String>();

    private Collection<EntryAttributeConfig> attributeConfigs = new ArrayList<EntryAttributeConfig>();

    private int defaultType = CONSTANT;

    private boolean needRdn = false;

    public AttributesWizardPage() {
        super(NAME);
        setDescription("Check attributes that will be used as RDN.");
    }

    public void createControl(final Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        composite.setLayout(new GridLayout(2, false));

        attributesTable = new Table(composite, SWT.BORDER | SWT.CHECK | SWT.FULL_SELECTION);
        attributesTable.setHeaderVisible(true);
        attributesTable.setLinesVisible(true);
        attributesTable.setLayoutData(new GridData(GridData.FILL_BOTH));

        attributesTable.addMouseListener(new MouseAdapter() {
            public void mouseDoubleClick(MouseEvent event) {
                try {
                    editAttribute();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
            public void mouseUp(MouseEvent event) {
                updateImages();
                setPageComplete(validatePage());
            }
        });

        TableColumn tc = new TableColumn(attributesTable, SWT.NONE);
        tc.setText("Attribute");
        tc.setWidth(200);

        tc = new TableColumn(attributesTable, SWT.NONE);
        tc.setText("Value/Expression");
        tc.setWidth(300);

        Composite buttons = new Composite(composite, SWT.NONE);
        buttons.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        buttons.setLayout(new GridLayout());

        Button addButton = new Button(buttons, SWT.PUSH);
        addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        addButton.setText("Add");

        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    addAttribute();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });

        Button editButton = new Button(buttons, SWT.PUSH);
        editButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        editButton.setText("Edit");

        editButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    editAttribute();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });

        Button removeButton = new Button(buttons, SWT.PUSH);
        removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        removeButton.setText("Remove");

        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    removeAttribute();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });

        setPageComplete(validatePage());
    }

    public void addAttribute() throws Exception {

        EntryAttributeConfig attributeConfig = new EntryAttributeConfig();

        AttributeWizard wizard = new AttributeWizard();
        wizard.setWindowTitle("Add Attribute");
        wizard.setServer(server);
        wizard.setPartitionName(partitionName);
        wizard.setEntrySourceConfigs(entrySourceConfigs);
        wizard.setAttributeConfig(attributeConfig);

        WizardDialog dialog = new WizardDialog(getShell(), wizard);
        dialog.setPageSize(600, 300);

        int rc = dialog.open();
        if (rc == Window.CANCEL) return;

        addAttributeConfig(attributeConfig);

        refresh();
    }

    public void editAttribute() throws Exception {
        if (attributesTable.getSelectionCount() == 0) return;

        TableItem item = attributesTable.getSelection()[0];
        EntryAttributeConfig attributeConfig = (EntryAttributeConfig)item.getData();

        AttributeWizard wizard = new AttributeWizard();
        wizard.setWindowTitle("Edit Attribute");
        wizard.setServer(server);
        wizard.setPartitionName(partitionName);
        wizard.setEntrySourceConfigs(entrySourceConfigs);
        wizard.setAttributeConfig(attributeConfig);

        WizardDialog dialog = new WizardDialog(getShell(), wizard);
        dialog.setPageSize(600, 300);

        int rc = dialog.open();
        if (rc == Window.CANCEL) return;

        refresh();
    }

    public void removeAttribute() throws Exception {
        if (attributesTable.getSelectionCount() == 0) return;

        TableItem items[] = attributesTable.getSelection();
        for (TableItem item : items) {
            EntryAttributeConfig ad = (EntryAttributeConfig) item.getData();
            removeAttributeConfig(ad);
        }

        refresh();
    }

    public void addAttributeConfig(EntryAttributeConfig attributeConfig) {
        attributeConfigs.add(attributeConfig);
    }

    public void removeAttributeConfig(EntryAttributeConfig attributeConfig) {
        attributeConfigs.remove(attributeConfig);
    }

    public void setRdn(RDN rdn) {

        needRdn = !rdn.isEmpty();

        attributeConfigs.clear();

        for (String name : rdn.getNames()) {
            String value = (String) rdn.get(name);

            EntryAttributeConfig ad = new EntryAttributeConfig();
            ad.setName(name);
            ad.setRdn(true);

            if (!"...".equals(value)) ad.setConstant(value);

            addAttributeConfig(ad);
        }
    }

    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) refresh();
    }

    public void refresh() {
        attributesTable.removeAll();

        log.debug("Attributes:");
        for (EntryAttributeConfig attributeConfig : attributeConfigs) {

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

            log.debug(" - " + attributeConfig.getName() + ": " + value);

            TableItem it = new TableItem(attributesTable, SWT.CHECK);
            it.setImage(PenroseStudio.getImage(attributeConfig.isRdn() ? PenroseImage.KEY : PenroseImage.NOKEY));
            it.setText(0, attributeConfig.getName());
            it.setText(1, value == null ? "" : value);
            it.setChecked(attributeConfig.isRdn());
            it.setData(attributeConfig);
        }
    }

    public boolean validatePage() {
        return true;
    }

    public void updateImages() {
        TableItem items[] = attributesTable.getItems();
        for (TableItem item : items) {
            EntryAttributeConfig ad = (EntryAttributeConfig) item.getData();
            ad.setRdn(item.getChecked());
            item.setImage(PenroseStudio.getImage(item.getChecked() ? PenroseImage.KEY : PenroseImage.NOKEY));
        }
    }

    public void widgetSelected(SelectionEvent event) {
        updateImages();
        setPageComplete(validatePage());
    }

    public void widgetDefaultSelected(SelectionEvent event) {
    }

    public int getDefaultType() {
        return defaultType;
    }

    public void setDefaultType(int defaultType) {
        this.defaultType = defaultType;
    }

    public Collection<EntrySourceConfig> getEntrySourceConfigs() {
        return entrySourceConfigs;
    }

    public void setEntrySourceConfigs(Collection<EntrySourceConfig> entrySourceConfigs) {
        this.entrySourceConfigs = entrySourceConfigs;
    }

    public Collection<EntryAttributeConfig> getAttributeConfigs() {
        return attributeConfigs;
    }

    public void setAttributeConfigs(Collection<EntryAttributeConfig> attributeConfigs) throws Exception {
        removeAttributeConfigs();
        for (EntryAttributeConfig attributeConfig : attributeConfigs) {
            addAttributeConfig((EntryAttributeConfig)attributeConfig.clone());
        }
    }

    public void removeAttributeConfigs() {
        attributeConfigs.clear();
    }

    public Collection getObjectClasses() {
        return objectClasses;
    }

    public void setObjectClasses(Collection<String> objectClasses) {
        if (this.objectClasses == objectClasses) return;
        this.objectClasses.clear();
        this.objectClasses.addAll(objectClasses);
    }

    public boolean isNeedRdn() {
        return needRdn;
    }

    public void setNeedRdn(boolean needRdn) {
        this.needRdn = needRdn;
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
}
