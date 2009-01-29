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
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.mapping.MappingRuleConfig;
import org.safehaus.penrose.mapping.Expression;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.List;

/**
 * @author Endi S. Dewata
 */
public class MappingRulesWizardPage extends WizardPage {

    Logger log = Logger.getLogger(getClass());

    public final static String NAME = "Mapping Rules";

    Table rulesTable;

    List<MappingRuleConfig> ruleConfigs = new LinkedList<MappingRuleConfig>();

    public MappingRulesWizardPage() {
        super(NAME);
        setDescription("Enter mapping rules.");
    }

    public void createControl(final Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        composite.setLayout(new GridLayout(2, false));

        rulesTable = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION);
        rulesTable.setHeaderVisible(true);
        rulesTable.setLinesVisible(true);

        rulesTable.setLayoutData(new GridData(GridData.FILL_BOTH));

        TableColumn tc = new TableColumn(rulesTable, SWT.LEFT);
        tc.setText("Field");
        tc.setWidth(100);

        tc = new TableColumn(rulesTable, SWT.LEFT);
        tc.setText("Value");
        tc.setWidth(180);

        tc = new TableColumn(rulesTable, SWT.LEFT);
        tc.setText("Required");
        tc.setWidth(80);

        tc = new TableColumn(rulesTable, SWT.LEFT);
        tc.setText("Condition");
        tc.setWidth(100);

        rulesTable.addMouseListener(new MouseAdapter() {
            public void mouseDoubleClick(MouseEvent event) {
                try {
                    editRule();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        Menu menu = new Menu(rulesTable);
        rulesTable.setMenu(menu);

        MenuItem mi = new MenuItem(menu, SWT.PUSH);
        mi.setText("Edit");

        mi.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    editRule();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        Composite buttons = new Composite(composite, SWT.NONE);
        buttons.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        buttons.setLayout(new GridLayout());

        Button addButton = new Button(buttons, SWT.PUSH);
        addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        addButton.setText("Add");

        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    addRule();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        Button editButton = new Button(buttons, SWT.PUSH);
        editButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        editButton.setText("Edit");

        editButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    editRule();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        Button removeButton = new Button(buttons, SWT.PUSH);
        removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        removeButton.setText("Remove");

        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    removeRule();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        new Label(buttons, SWT.NONE);

        Button moveUpButton = new Button(buttons, SWT.PUSH);
		moveUpButton.setText("Move Up");

        moveUpButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        moveUpButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    moveUpRule();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        Button moveDown = new Button(buttons, SWT.PUSH);
		moveDown.setText("Move Down");

        moveDown.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        moveDown.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    moveDownRule();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        setPageComplete(validatePage());
    }

    public void addRule() throws Exception {

        AddFieldMappingWizard wizard = new AddFieldMappingWizard();
        WizardDialog dialog = new WizardDialog(getShell(), wizard);
        dialog.setPageSize(600, 300);
        int rc = dialog.open();

        if (rc == Window.CANCEL) return;

        MappingRuleConfig ruleConfig = wizard.getFieldConfig();
        ruleConfigs.add(ruleConfig);

        refresh();
    }

    public void editRule() throws Exception {
        if (rulesTable.getSelectionCount() == 0) return;

        TableItem ti = rulesTable.getSelection()[0];
        MappingRuleConfig ruleConfig = (MappingRuleConfig)ti.getData();

        EditFieldMappingWizard wizard = new EditFieldMappingWizard(ruleConfig);
        WizardDialog dialog = new WizardDialog(getShell(), wizard);
        dialog.setPageSize(600, 300);
        int rc = dialog.open();

        if (rc == Window.CANCEL) return;

        refresh();
    }

    public void removeRule() throws Exception {
        if (rulesTable.getSelectionCount() == 0) return;

        TableItem ti = rulesTable.getSelection()[0];
        MappingRuleConfig ruleConfig = (MappingRuleConfig)ti.getData();

        ruleConfigs.remove(ruleConfig);

        refresh();
    }

    public void moveUpRule() throws Exception {
        if (rulesTable.getSelectionCount() == 0) return;

        TableItem ti = rulesTable.getSelection()[0];
        MappingRuleConfig ruleConfig = (MappingRuleConfig)ti.getData();

        int i = ruleConfigs.indexOf(ruleConfig);
        if (i == 0) return;

        ruleConfigs.remove(ruleConfig);

        i--;

        ruleConfigs.add(i, ruleConfig);

        refresh();

        rulesTable.select(i);
    }

    public void moveDownRule() throws Exception {
        if (rulesTable.getSelectionCount() == 0) return;

        TableItem ti = rulesTable.getSelection()[0];
        MappingRuleConfig ruleConfig = (MappingRuleConfig)ti.getData();

        int i = ruleConfigs.indexOf(ruleConfig);
        if (i == ruleConfigs.size()-1) return;

        ruleConfigs.remove(ruleConfig);

        i++;

        ruleConfigs.add(i, ruleConfig);

        refresh();

        rulesTable. select(i);
    }

    public void setVisible(boolean b) {
        super.setVisible(b);
        if (b) refresh();
    }

    public void refresh() {

        rulesTable.removeAll();

        for (MappingRuleConfig ruleConfig : ruleConfigs) {
            String value;

            Object constant = ruleConfig.getConstant();
            if (constant != null) {
                if (constant instanceof byte[]) {
                    value = "(binary)";
                } else {
                    value = "\"" + constant + "\"";
                }

            } else {
                value = ruleConfig.getVariable();
            }

            if (value == null) {
                Expression expression = ruleConfig.getExpression();
                value = expression == null ? null : expression.getScript();
            }

            boolean required = ruleConfig.isRequired();
            String condition = ruleConfig.getCondition();

            TableItem item = new TableItem(rulesTable, SWT.NONE);
            item.setText(0, ruleConfig.getName());
            item.setText(1, value == null ? "" : value);
            item.setText(2, required ? "Yes" : "");
            item.setText(3, condition == null ? "" : condition);
            item.setData(ruleConfig);
        }
    }

    public boolean validatePage() {
        return true;
    }

    public void setRuleConfigs(Collection<MappingRuleConfig> ruleConfigs) {
        this.ruleConfigs.clear();
        for (MappingRuleConfig ruleConfig : ruleConfigs) {
            try {
                this.ruleConfigs.add((MappingRuleConfig)ruleConfig.clone());
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    public Collection<MappingRuleConfig> getRuleConfigs() {
        return ruleConfigs;
    }
}