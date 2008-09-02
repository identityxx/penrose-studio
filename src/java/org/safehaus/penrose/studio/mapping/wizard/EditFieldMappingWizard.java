package org.safehaus.penrose.studio.mapping.wizard;

import org.apache.log4j.Logger;
import org.eclipse.jface.wizard.Wizard;
import org.safehaus.penrose.mapping.MappingRuleConfig;
import org.safehaus.penrose.mapping.Expression;
import org.safehaus.penrose.studio.dialog.ErrorDialog;

/**
 * @author Endi Sukma Dewata
 */
public class EditFieldMappingWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    public FieldValueWizardPage valuePage;
    public FieldConditionWizardPage conditionPage;

    public MappingRuleConfig ruleConfig;

    public EditFieldMappingWizard(MappingRuleConfig ruleConfig) {

        this.ruleConfig = ruleConfig;

        setWindowTitle("Edit field mapping");

        valuePage = new FieldValueWizardPage();
        valuePage.setDescription("Enter field value/expression.");

        Object constant = ruleConfig.getConstant();
        String variable = ruleConfig.getVariable();
        Expression expression = ruleConfig.getExpression();

        if (constant != null) {
            valuePage.setConstant(constant);

        } else if (variable != null) {
            valuePage.setVariable(variable);

        } else if (expression != null) {
            valuePage.setExpression(expression);
        }

        conditionPage = new FieldConditionWizardPage();
        conditionPage.setRequired(ruleConfig.isRequired());
        conditionPage.setCondition(ruleConfig.getCondition());
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
