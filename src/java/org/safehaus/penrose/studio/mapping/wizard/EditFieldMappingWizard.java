package org.safehaus.penrose.studio.mapping.wizard;

import org.apache.log4j.Logger;
import org.eclipse.jface.wizard.Wizard;
import org.safehaus.penrose.mapping.MappingFieldConfig;
import org.safehaus.penrose.mapping.Expression;
import org.safehaus.penrose.studio.dialog.ErrorDialog;

/**
 * @author Endi Sukma Dewata
 */
public class EditFieldMappingWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    public FieldValueWizardPage valuePage;
    public FieldConditionWizardPage conditionPage;

    public MappingFieldConfig fieldConfig;

    public EditFieldMappingWizard(MappingFieldConfig fieldConfig) {

        this.fieldConfig = fieldConfig;

        setWindowTitle("Edit field mapping");

        valuePage = new FieldValueWizardPage();
        valuePage.setDescription("Enter field value/expression.");

        Object constant = fieldConfig.getConstant();
        String variable = fieldConfig.getVariable();
        Expression expression = fieldConfig.getExpression();

        if (constant != null) {
            valuePage.setConstant(constant);

        } else if (variable != null) {
            valuePage.setVariable(variable);

        } else if (expression != null) {
            valuePage.setExpression(expression);
        }

        conditionPage = new FieldConditionWizardPage();
        conditionPage.setRequired(fieldConfig.isRequired());
        conditionPage.setCondition(fieldConfig.getCondition());
        conditionPage.setDescription("Enter condition to evaluate the field.");
    }

    public boolean canFinish() {
        if (!valuePage.isPageComplete()) return false;
        if (!conditionPage.isPageComplete()) return false;
        return true;
    }

    public void addPages() {
        addPage(valuePage);
        addPage(conditionPage);
    }

    public boolean needsPreviousAndNextButtons() {
        return true;
    }

    public boolean performFinish() {
        try {
            int type = valuePage.getType();
            switch (type) {
                case FieldValueWizardPage.TEXT:
                    String text = valuePage.getText();
                    fieldConfig.setConstant(text);
                    break;
                case FieldValueWizardPage.BINARY:
                    byte[] binary = valuePage.getBinary();
                    fieldConfig.setBinary(binary);
                    break;
                case FieldValueWizardPage.VARIABLE:
                    String variable = valuePage.getVariable();
                    fieldConfig.setVariable(variable);
                    break;
                case FieldValueWizardPage.EXPRESSION:
                    Expression expression = valuePage.getExpression();
                    fieldConfig.setExpression(expression);
                    break;
            }

            boolean required = conditionPage.isRequired();
            fieldConfig.setRequired(required);

            String condition = conditionPage.getCondition();
            fieldConfig.setCondition(condition);

            return true;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
            return false;
        }
    }

    public MappingFieldConfig getFieldConfig() {
        return fieldConfig;
    }
}
