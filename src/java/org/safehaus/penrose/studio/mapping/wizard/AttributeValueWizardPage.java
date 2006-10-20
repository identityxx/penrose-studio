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
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchPage;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.mapping.AttributeTypeSelectionDialog;
import org.safehaus.penrose.studio.mapping.ExpressionDialog;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.server.ServerNode;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.object.ObjectsView;
import org.safehaus.penrose.schema.ObjectClass;
import org.safehaus.penrose.schema.SchemaManager;
import org.safehaus.penrose.mapping.*;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.partition.SourceConfig;
import org.safehaus.penrose.partition.FieldConfig;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * @author Endi S. Dewata
 */
public class AttributeValueWizardPage extends WizardPage implements SelectionListener {

    Logger log = Logger.getLogger(getClass());

    public final static String NAME = "Attribute values";

    public final static int CONSTANT   = 0;
    public final static int VARIABLE   = 1;
    public final static int EXPRESSION = 2;

    Partition partition;
    Table attributeTable;

    private Collection sourceMappings;

    private Collection objectClasses;
    private Map attributeMappings = new TreeMap();

    private int defaultType = CONSTANT;

    private boolean needRdn = false;

    public AttributeValueWizardPage(Partition partition) {
        super(NAME);
        this.partition = partition;
        setDescription("Double-click the attribute to enter the values. Select attributes that will be used as RDN.");
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
                    AttributeMapping ad = (AttributeMapping)item.getData();

                    ExpressionDialog dialog = new ExpressionDialog(parent.getShell(), SWT.NONE);
                    dialog.setText("Edit attribute value/expression...");

                    if (defaultType == VARIABLE) {

                        if (sourceMappings != null) {
                            for (Iterator i=sourceMappings.iterator(); i.hasNext(); ) {
                                SourceMapping sourceMapping = (SourceMapping)i.next();
                                SourceConfig sourceConfig = partition.getSourceConfig(sourceMapping.getSourceName());
                                dialog.addVariable(sourceMapping.getName());

                                Collection fields = sourceConfig.getFieldConfigs();
                                for (Iterator j=fields.iterator(); j.hasNext(); ) {
                                    FieldConfig field = (FieldConfig)j.next();
                                    dialog.addVariable(sourceMapping.getName()+"."+field.getName());
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
                    log.debug(e.getMessage(), e);
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
                    AttributeTypeSelectionDialog dialog = new AttributeTypeSelectionDialog(parent.getShell(), SWT.NONE);
                    dialog.setText("Add attributes...");

                    IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                    IWorkbenchPage page = window.getActivePage();
                    ObjectsView objectsView = (ObjectsView)page.showView(ObjectsView.class.getName());

                    ServerNode serverNode = objectsView.getSelectedProjectNode();
                    if (serverNode == null) return;

                    Server server = serverNode.getProject();
                    dialog.setSchemaManager(server.getSchemaManager());

                    dialog.open();
                    if (dialog.getAction() == AttributeTypeSelectionDialog.CANCEL) return;

                    for (Iterator i=dialog.getSelections().iterator(); i.hasNext(); ) {
                        String name = (String)i.next();
                        AttributeMapping ad = new AttributeMapping();
                        ad.setName(name);
                        addAttributeMapping(ad);
                    }

                    refresh();

                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                }
            }
        });

        Button removeButton = new Button(buttons, SWT.PUSH);
        removeButton.setText("Remove");

        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                if (attributeTable.getSelectionCount() == 0) return;

                TableItem items[] = attributeTable.getSelection();
                for (int i=0; i<items.length; i++) {
                    AttributeMapping ad = (AttributeMapping)items[i].getData();
                    removeAttributeMapping(ad);
                }

                refresh();
            }
        });

        setPageComplete(validatePage());
    }

    public void addAttributeMapping(AttributeMapping attributeMapping) {
        String name = attributeMapping.getName().toLowerCase();
        Collection list = (Collection)attributeMappings.get(name);
        if (list == null) {
            list = new ArrayList();
            attributeMappings.put(name, list);
        }
        list.add(attributeMapping);
    }

    public void removeAttributeMapping(AttributeMapping attributeMapping) {
        String name = attributeMapping.getName().toLowerCase();
        Collection list = (Collection)attributeMappings.get(name);
        if (list == null) return;

        list.remove(attributeMapping);
    }

    public void setRdn(Row rdn) {

        needRdn = !rdn.isEmpty();

        attributeMappings.clear();

        for (Iterator i=rdn.getNames().iterator(); i.hasNext(); ) {
            String name = (String)i.next();
            String value = (String)rdn.get(name);

            AttributeMapping ad = new AttributeMapping();
            ad.setName(name);
            ad.setRdn(true+"");

            if (!"...".equals(value)) ad.setConstant(value);

            addAttributeMapping(ad);
        }
    }

    public void setVisible(boolean visible) {
        super.setVisible(visible);

        if (visible) init();
    }

    public void init() {
        try {
            IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            IWorkbenchPage page = window.getActivePage();
            ObjectsView objectsView = (ObjectsView)page.showView(ObjectsView.class.getName());

            ServerNode serverNode = objectsView.getSelectedProjectNode();
            if (serverNode == null) return;

            Server server = serverNode.getProject();
            SchemaManager schemaManager = server.getSchemaManager();

            System.out.println("Object classes:");
            for (Iterator i=objectClasses.iterator(); i.hasNext(); ) {
                String objectClass = (String)i.next();

                Collection allOcs = schemaManager.getAllObjectClasses(objectClass);
                for (Iterator j=allOcs.iterator(); j.hasNext(); ) {
                    ObjectClass oc = (ObjectClass)j.next();
                    System.out.println(" - "+oc.getName());

                    Collection reqAttrs = oc.getRequiredAttributes();
                    for (Iterator k=reqAttrs.iterator(); k.hasNext(); ) {
                        String attrName = (String)k.next();
                        System.out.println("   - (req): "+attrName);

                        if (attributeMappings.containsKey(attrName.toLowerCase())) continue;

                        AttributeMapping ad = new AttributeMapping();
                        ad.setName(attrName);

                        addAttributeMapping(ad);
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
            log.debug(e.getMessage(), e);
        }
    }

    public void refresh() {
        attributeTable.removeAll();

        //System.out.println("Attributes:");
        for (Iterator i=attributeMappings.values().iterator(); i.hasNext(); ) {
            Collection list = (Collection)i.next();

            for (Iterator j=list.iterator(); j.hasNext(); ) {
                AttributeMapping ad = (AttributeMapping)j.next();

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

                //System.out.println(" - "+ad.getName());

                TableItem it = new TableItem(attributeTable, SWT.CHECK);
                it.setImage(PenrosePlugin.getImage("true".equals(ad.getRdn()) ? PenroseImage.KEY : PenroseImage.NOKEY));
                it.setText(0, ad.getName());
                it.setText(1, value == null ? "" : value);
                it.setChecked("true".equals(ad.getRdn()));
                it.setData(ad);
            }
        }
    }

    public boolean validatePage() {
        if (!needRdn) return true;

        for (Iterator i=attributeMappings.values().iterator(); i.hasNext(); ) {
            Collection list = (Collection)i.next();

            for (Iterator j=list.iterator(); j.hasNext(); ) {
                AttributeMapping ad = (AttributeMapping)j.next();
                if ("true".equals(ad.getRdn())) return true;
            }
        }

        return false;
    }

    public void updateImages() {
        TableItem items[] = attributeTable.getItems();
        for (int i=0; i<items.length; i++) {
            TableItem item = items[i];
            AttributeMapping ad = (AttributeMapping)item.getData();
            ad.setRdn(item.getChecked()+"");
            item.setImage(PenrosePlugin.getImage(item.getChecked() ? PenroseImage.KEY : PenroseImage.NOKEY));
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

    public Collection getSourceMappings() {
        return sourceMappings;
    }

    public void setSourceMappings(Collection sourceMappings) {
        this.sourceMappings = sourceMappings;
    }

    public Collection getAttributeMappings() {
        Collection results = new ArrayList();
        for (Iterator i=attributeMappings.values().iterator(); i.hasNext(); ) {
            Collection list = (Collection)i.next();
            results.addAll(list);
        }
        return results;
    }

    public void setAttributeMappings(Map attributeMappings) {
        this.attributeMappings = attributeMappings;
    }

    public Collection getObjectClasses() {
        return objectClasses;
    }

    public void setObjectClasses(Collection objectClasses) {
        this.objectClasses = objectClasses;
    }

    public boolean isNeedRdn() {
        return needRdn;
    }

    public void setNeedRdn(boolean needRdn) {
        this.needRdn = needRdn;
    }
}
