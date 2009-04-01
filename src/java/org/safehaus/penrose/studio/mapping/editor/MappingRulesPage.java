/**
 * Copyright 2009 Red Hat, Inc.
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
 */package org.safehaus.penrose.studio.mapping.editor;

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.safehaus.penrose.mapping.Expression;
import org.safehaus.penrose.mapping.MappingRuleConfig;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.studio.mapping.wizard.MappingRulesWizard;

/**
 * @author Endi S. Dewata
 */
public class MappingRulesPage extends MappingEditorPage {

    Table rulesTable;

    public MappingRulesPage(MappingEditor editor) {
        super(editor, "RULES", "Rules");
    }

    public void createFormContent(IManagedForm managedForm) {
        super.createFormContent(managedForm);

        ScrolledForm form = managedForm.getForm();

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Rules");
        section.setLayoutData(new GridData(GridData.FILL_BOTH));

        Control atSection = createRulesSection(section);
        section.setClient(atSection);
    }

    public Composite createRulesSection(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        Composite leftControl = createRulesLeftSection(composite);
        leftControl.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite rightControl = createRulesRightSection(composite);
        GridData gd = new GridData(GridData.FILL_VERTICAL);
        gd.widthHint = 100;
        rightControl.setLayoutData(gd);

        return composite;
    }

    public Composite createRulesLeftSection(final Composite parent) {

        rulesTable = new Table(parent, SWT.BORDER | SWT.FULL_SELECTION);
        rulesTable.setHeaderVisible(true);
        rulesTable.setLinesVisible(true);

        rulesTable.setLayoutData(new GridData(GridData.FILL_BOTH));

        TableColumn tc = new TableColumn(rulesTable, SWT.LEFT);
        tc.setText("Field");
        tc.setWidth(140);

        tc = new TableColumn(rulesTable, SWT.LEFT);
        tc.setText("Value");
        tc.setWidth(200);

        tc = new TableColumn(rulesTable, SWT.LEFT);
        tc.setText("Required");
        tc.setWidth(75);

        tc = new TableColumn(rulesTable, SWT.LEFT);
        tc.setText("Condition");
        tc.setWidth(150);

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

        return rulesTable;
    }

    public Composite createRulesRightSection(final Composite parent) {

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
                    editRule();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        return composite;
    }

    public void editRule() throws Exception {

        MappingRulesWizard wizard = new MappingRulesWizard();
        wizard.setMappingConfig(mappingConfig);

        WizardDialog dialog = new WizardDialog(getSite().getShell(), wizard);
        dialog.setPageSize(600, 300);
        int rc = dialog.open();

        if (rc == WizardDialog.CANCEL) return;

        editor.store();
        refresh();
    }

    public void refresh() {

        rulesTable.removeAll();
        
        for (MappingRuleConfig ruleConfig : mappingConfig.getRuleConfigs()) {
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
}