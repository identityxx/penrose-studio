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

import org.apache.log4j.Logger;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;
import org.safehaus.penrose.directory.EntryAttributeConfig;
import org.safehaus.penrose.directory.EntrySourceConfig;
import org.safehaus.penrose.ldap.RDN;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.schema.SchemaManagerClient;
import org.safehaus.penrose.source.SourceClient;
import org.safehaus.penrose.mapping.Expression;
import org.safehaus.penrose.schema.ObjectClass;
import org.safehaus.penrose.source.FieldConfig;
import org.safehaus.penrose.source.SourceConfig;
import org.safehaus.penrose.source.SourceManagerClient;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.directory.dialog.SelectAttributeTypeDialog;
import org.safehaus.penrose.studio.directory.dialog.ExpressionDialog;

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

    Table attributeTable;

    private Server server;
    private String partitionName;

    private Collection<EntrySourceConfig> sourceConfigs;
    private Collection<String> objectClasses = new ArrayList<String>();

    private Map<String,Collection<EntryAttributeConfig>> attributeConfigs = new LinkedHashMap<String,Collection<EntryAttributeConfig>>();

    private int defaultType = CONSTANT;

    private boolean needRdn = false;

    public AttributesWizardPage() {
        super(NAME);
        setDescription("Double-click the attribute to enter the values. Check attributes that will be used as RDN.");
    }

    public void createControl(final Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        GridLayout sectionLayout = new GridLayout();
        sectionLayout.numColumns = 1;
        composite.setLayout(sectionLayout);

        Label adapterLabel = new Label(composite, SWT.NONE);
        adapterLabel.setText("Attributes:");

        attributeTable = new Table(composite, SWT.BORDER | SWT.CHECK | SWT.FULL_SELECTION);
        attributeTable.setHeaderVisible(true);
        attributeTable.setLinesVisible(true);
        attributeTable.setLayoutData(new GridData(GridData.FILL_BOTH));

        attributeTable.addMouseListener(new MouseListener() {
            public void mouseDoubleClick(MouseEvent event) {
                try {
                    if (attributeTable.getSelectionCount() == 0) return;

                    TableItem item = attributeTable.getSelection()[0];
                    EntryAttributeConfig ad = (EntryAttributeConfig)item.getData();

                    ExpressionDialog dialog = new ExpressionDialog(parent.getShell(), SWT.NONE);
                    dialog.setText("Edit attribute value/expression...");

                    if (defaultType == VARIABLE) {

                        if (sourceConfigs != null) {

                            PenroseClient client = server.getClient();
                            PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
                            PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
                            SourceManagerClient sourceManagerClient = partitionClient.getSourceManagerClient();

                            for (EntrySourceConfig sourceMapping : sourceConfigs) {

                                SourceClient sourceClient = sourceManagerClient.getSourceClient(sourceMapping.getSourceName());
                                SourceConfig sourceConfig = sourceClient.getSourceConfig();

                                //SourceConfig sourceConfig = partitionConfig.getSourceConfigManager().getSourceConfig(sourceMapping.getSourceName());
                                dialog.addVariable(sourceMapping.getAlias());

                                Collection<FieldConfig> fields = sourceConfig.getFieldConfigs();
                                for (FieldConfig field : fields) {
                                    dialog.addVariable(sourceMapping.getAlias() + "." + field.getName());
                                }
                            }
                        }

                        dialog.setType(ExpressionDialog.VARIABLE);

                    } else {
                        dialog.setType(ExpressionDialog.TEXT);
                    }

                    dialog.setAttributeMapping(ad);

                    dialog.open();

                    if (dialog.getAction() == ExpressionDialog.CANCEL) return;

                    refresh();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e.getMessage(), e);
                }
            }

            public void mouseDown(MouseEvent event) {
            }

            public void mouseUp(MouseEvent event) {
                updateImages();
                setPageComplete(validatePage());
            }
        });

        TableColumn tc = new TableColumn(attributeTable, SWT.NONE);
        tc.setText("Attribute");
        tc.setWidth(150);

        tc = new TableColumn(attributeTable, SWT.NONE);
        tc.setText("Value/Expression");
        tc.setWidth(350);

        Composite buttons = new Composite(composite, SWT.NONE);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalAlignment = GridData.END;
        buttons.setLayoutData(gd);
        buttons.setLayout(new RowLayout());

        Button addButton = new Button(buttons, SWT.PUSH);
        addButton.setText("Add");

        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    SelectAttributeTypeDialog dialog = new SelectAttributeTypeDialog(parent.getShell(), SWT.NONE);
                    dialog.setText("Add attributes...");

                    PenroseClient client = server.getClient();
                    dialog.setSchemaManagerClient(client.getSchemaManagerClient());

                    int rc = dialog.open();
                    if (rc == Window.CANCEL) return;

                    for (String name : dialog.getSelections()) {
                        EntryAttributeConfig ad = new EntryAttributeConfig();
                        ad.setName(name);
                        addAttributeConfig(ad);
                    }

                    refresh();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });

        Button removeButton = new Button(buttons, SWT.PUSH);
        removeButton.setText("Remove");

        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                if (attributeTable.getSelectionCount() == 0) return;

                TableItem items[] = attributeTable.getSelection();
                for (TableItem item : items) {
                    EntryAttributeConfig ad = (EntryAttributeConfig) item.getData();
                    removeAttributeMapping(ad);
                }

                refresh();
            }
        });

        setPageComplete(validatePage());
    }

    public void addAttributeConfig(EntryAttributeConfig attributeConfig) {
        String name = attributeConfig.getName().toLowerCase();
        Collection<EntryAttributeConfig> list = attributeConfigs.get(name);
        if (list == null) {
            list = new ArrayList<EntryAttributeConfig>();
            attributeConfigs.put(name, list);
        }
        list.add(attributeConfig);
    }

    public void removeAttributeMapping(EntryAttributeConfig attributeMapping) {
        String name = attributeMapping.getName().toLowerCase();
        Collection<EntryAttributeConfig> list = attributeConfigs.get(name);
        if (list == null) return;

        list.remove(attributeMapping);
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

        if (visible) init();
    }

    public void init() {
        try {

            PenroseClient client = server.getClient();
            SchemaManagerClient schemaManagerClient = client.getSchemaManagerClient();

            System.out.println("Object classes:");
            for (String objectClass : objectClasses) {

                Collection<ObjectClass> allOcs = schemaManagerClient.getAllObjectClasses(objectClass);
                for (ObjectClass oc : allOcs) {
                    System.out.println(" - " + oc.getName());

                    Collection<String> reqAttrs = oc.getRequiredAttributes();
                    for (String attrName : reqAttrs) {
                        System.out.println("   - (req): " + attrName);

                        if (attributeConfigs.containsKey(attrName.toLowerCase())) continue;

                        EntryAttributeConfig ad = new EntryAttributeConfig();
                        ad.setName(attrName);

                        addAttributeConfig(ad);
                    }
/*
                    Collection optAttrs = oc.getOptionalAttributes();
                    for (Iterator k=optAttrs.iterator(); k.hasNext(); ) {
                        String attrName = (String)k.next();
                        System.out.println("   - (opt): "+attrName);

                        if (entry.getAttributeDefinition(attrName) != null) continue;

                        AttributeDefinition ad = new AttributeDefinition();
                        ad.setName(attrName);

                        entry.addAttributeDefinition(ad);
                    }
*/
                }
            }
/*
            if (rdnAttr != null) {
                AttributeDefinition rdnAd = entry.getAttributeDefinition(rdnAttr);
                if (rdnAd != null) {
                    rdnAd.setRdn(true);
                    Expression expression = rdnAd.getExpression();
                    if (expression == null) {
                    	expression = new Expression();
                    	rdnAd.setExpression(expression);
                    }
                    expression.setScript("\""+rdnValue+"\"");
                }
            }
*/
            refresh();

            setPageComplete(validatePage());

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void refresh() {
        attributeTable.removeAll();

        log.debug("Attributes:");
        for (Collection<EntryAttributeConfig> list : attributeConfigs.values()) {

            for (EntryAttributeConfig ad : list) {

                String value;

                Object constant = ad.getConstant();
                if (constant != null) {
                    if (constant instanceof byte[]) {
                        value = "(binary)";
                    } else {
                        value = "\"" + constant + "\"";
                    }

                } else {
                    value = ad.getVariable();
                }

                if (value == null) {
                    Expression expression = ad.getExpression();
                    value = expression == null ? null : expression.getScript();
                }

                log.debug(" - " + ad.getName() + ": " + value);

                TableItem it = new TableItem(attributeTable, SWT.CHECK);
                it.setImage(PenroseStudio.getImage(ad.isRdn() ? PenroseImage.KEY : PenroseImage.NOKEY));
                it.setText(0, ad.getName());
                it.setText(1, value == null ? "" : value);
                it.setChecked(ad.isRdn());
                it.setData(ad);
            }
        }

        setPageComplete(validatePage());
    }

    public boolean validatePage() {
/*
        if (attributeMappings.isEmpty()) return false;

        boolean rdn = false;

        for (Collection<AttributeMapping> list : attributeMappings.values()) {

            for (AttributeMapping ad : list) {
                if (ad.isRdn()) rdn = true;

                if (ad.getText() != null) continue;
                if (ad.getBinary() != null) continue;
                if (ad.getVariable() != null) continue;
                if (ad.getExpression() != null) continue;

                log.debug("Attribute " + ad.getName() + " not set.");
                return false;
            }
        }

        if (needRdn & !rdn) {
            log.debug("RDN not found.");
            return false;
        }
*/
        return true;
    }

    public void updateImages() {
        TableItem items[] = attributeTable.getItems();
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

    public Collection<EntrySourceConfig> getSourceConfigs() {
        return sourceConfigs;
    }

    public void setSourceConfigs(Collection<EntrySourceConfig> sourceConfigs) {
        this.sourceConfigs = sourceConfigs;
    }

    public Collection<EntryAttributeConfig> getAttributeConfigs() {
        Collection<EntryAttributeConfig> results = new ArrayList<EntryAttributeConfig>();
        for (Collection<EntryAttributeConfig> list : attributeConfigs.values()) {
            results.addAll(list);
        }
        return results;
    }

    public void setAttributeConfigs(Collection<EntryAttributeConfig> attributeConfigs) {
        removeAttributeConfigs();
        for (EntryAttributeConfig attributeConfig : attributeConfigs) {
            addAttributeConfig(attributeConfig);
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
