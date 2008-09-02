package org.safehaus.penrose.studio.mapping.wizard;

import org.apache.log4j.Logger;
import org.eclipse.jface.wizard.Wizard;
import org.safehaus.penrose.mapping.MappingRuleConfig;
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

    public MappingRuleConfig ruleConfig = new MappingRuleConfig();

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
            ruleConfig.setName(fieldName);

            int type = valuePage.getType();
            switch (type) {
                case FieldValueWizardPage.TEXT:
                    String text = valuePage.getText();
                    ruleConfig.setConstant(text);
                    break;
                case FieldValueWizardPage.BINARY:
                    byte[] binary = valuePage.getBinary();
                    ruleConfig.setBinary(binary);
                    break;
                case FieldValueWizardPage.VARIABLE:
                    String variable = valuePage.getVariable();
                    ruleConfig.setVariable(variable);
                    break;
                case FieldValueWizardPage.EXPRESSION:
                    Expression expression = valuePage.getExpression();
                    ruleConfig.setExpression(expression);
                    break;
            }

            boolean required = conditionPage.isRequired();
            ruleConfig.setRequired(required);

            String condition = conditionPage.getCondition();
            ruleConfig.setCondition(condition);

            return true;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
            return false;
        }
    }

    public MappingRuleConfig getFieldConfig() {
        return ruleConfig;
    }
}