package org.safehaus.penrose.studio.mapping.wizard;

import org.apache.log4j.Logger;
import org.eclipse.jface.wizard.Wizard;
import org.safehaus.penrose.mapping.MappingFieldConfig;
import org.safehaus.penrose.mapping.Expression;
import org.safehaus.penrose.studio.dialog.ErrorDialog;

/**
 * @author Endi Sukma Dewata
 */
public class AddFieldMappingWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    public FieldPropertyWizardPage propertyPage;
    public FieldValueWizardPage valuePage;
    public FieldConditionWizardPage conditionPage;

    public MappingFieldConfig fieldConfig = new MappingFieldConfig();

    public AddFieldMappingWizard() {

        setWindowTitle("Add field mapping");

        propertyPage = new FieldPropertyWizardPage();
        propertyPage.setDescription("Enter the field name.");

        valuePage = new FieldValueWizardPage();
        valuePage.setDescription("Enter field value/expression.");

        conditionPage = new FieldConditionWizardPage();
        conditionPage.setDescription("Enter condition to evaluate the field.");
    }

    public boolean canFinish() {
        if (!propertyPage.isPageComplete()) return false;
        if (!valuePage.isPageComplete()) return false;
        if (!conditionPage.isPageComplete()) return false;
        return true;
    }

    public void addPages() {
        addPage(propertyPage);
        addPage(valuePage);
        addPage(conditionPage);
    }

    public boolean needsPreviousAndNextButtons() {
        return true;
    }

    public boolean performFinish() {
        try {
            String fieldName = propertyPage.getFieldName();
            fieldConfig.setName(fieldName);

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