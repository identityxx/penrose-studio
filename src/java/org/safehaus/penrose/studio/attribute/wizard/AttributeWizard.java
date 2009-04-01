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
 */
package org.safehaus.penrose.studio.attribute.wizard;

import org.apache.log4j.Logger;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.safehaus.penrose.directory.*;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.expression.wizard.ExpressionWizardPage;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.schema.SchemaManagerClient;
import org.safehaus.penrose.schema.Schema;
import org.safehaus.penrose.mapping.Expression;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.source.SourceManagerClient;
import org.safehaus.penrose.source.SourceClient;

import java.util.Collection;
import java.util.ArrayList;

/**
 * @author Endi S. Dewata
 */
public class AttributeWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    private Server server;
    private String partitionName;

    private Collection<EntrySourceConfig> entrySourceConfigs = new ArrayList<EntrySourceConfig>();
    private EntryAttributeConfig attributeConfig;

    public AttributeWizardPage attributePage;
    public ExpressionWizardPage expressionPage;

    public AttributeWizard() {
    }

    public void addPages() {
        try {
            PenroseClient client = server.getClient();

            SchemaManagerClient schemaManagerClient = client.getSchemaManagerClient();
            Schema schema = schemaManagerClient.getSchema();

            PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
            PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
            SourceManagerClient sourceManagerClient = partitionClient.getSourceManagerClient();

            Collection<String> variables = new ArrayList<String>();

            for (EntrySourceConfig entrySourceConfig : entrySourceConfigs) {

                String alias = entrySourceConfig.getAlias();
                alias = alias == null ? entrySourceConfig.getSourceName() : alias;

                SourceClient sourceClient = sourceManagerClient.getSourceClient(entrySourceConfig.getSourceName());

                variables.add(alias);

                for (String fieldName : sourceClient.getFieldNames()) {
                    variables.add(alias+ "."+fieldName);
                }
            }

            attributePage = new AttributeWizardPage();
            attributePage.setSchema(schema);
            attributePage.setAttributeName(attributeConfig.getName());

            addPage(attributePage);

            expressionPage = new ExpressionWizardPage();
            expressionPage.setVariables(variables);

            Object constant = attributeConfig.getConstant();
            String variable = attributeConfig.getVariable();
            Expression expression = attributeConfig.getExpression();

            if (constant != null) {
                if (constant instanceof byte[]) {
                    expressionPage.setType(ExpressionWizardPage.BINARY);
                    expressionPage.setBinary((byte[])constant);
                } else {
                    expressionPage.setType(ExpressionWizardPage.TEXT);
                    expressionPage.setText((String)constant);
                }

            } else if (variable != null) {
                expressionPage.setType(ExpressionWizardPage.VARIABLE);
                expressionPage.setVariable(variable);

            } else if (expression != null) {
                expressionPage.setType(ExpressionWizardPage.EXPRESSION);
                expressionPage.setExpression(expression);

            } else {
                expressionPage.setType(ExpressionWizardPage.TEXT);
            }

            addPage(expressionPage);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public boolean canFinish() {
        if (!attributePage.isPageComplete()) return false;

        return true;
    }

    public IWizardPage getNextPage(IWizardPage page) {
        return super.getNextPage(page);
    }

    public boolean performFinish() {
        try {
            attributeConfig.setName(attributePage.getAttributeName());

            int type = expressionPage.getType();

            Object constant = null;
            String variable = null;
            Expression expression = null;

            if (ExpressionWizardPage.TEXT == type) {
                constant = expressionPage.getText();

            } else if (ExpressionWizardPage.BINARY == type) {
                constant = expressionPage.getBinary();

            } else if (ExpressionWizardPage.VARIABLE == type) {
                variable = expressionPage.getVariable();

            } else if (ExpressionWizardPage.EXPRESSION == type) {
                expression = new Expression();
                expression.copy(expressionPage.getExpression());
            }

            attributeConfig.setConstant(constant);
            attributeConfig.setVariable(variable);
            attributeConfig.setExpression(expression);

            return true;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
            return false;
        }
    }

    public boolean needsPreviousAndNextButtons() {
        return true;
    }

    public EntryAttributeConfig getAttributeConfig() {
        return attributeConfig;
    }

    public void setAttributeConfig(EntryAttributeConfig attributeConfig) {
        this.attributeConfig = attributeConfig;
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

    public Collection<EntrySourceConfig> getEntrySourceConfigs() {
        return entrySourceConfigs;
    }

    public void setEntrySourceConfigs(Collection<EntrySourceConfig> entrySourceConfigs) {
        if (this.entrySourceConfigs == entrySourceConfigs) return;
        this.entrySourceConfigs.clear();
        if (entrySourceConfigs == null) return;
        this.entrySourceConfigs.addAll(entrySourceConfigs);
    }
}