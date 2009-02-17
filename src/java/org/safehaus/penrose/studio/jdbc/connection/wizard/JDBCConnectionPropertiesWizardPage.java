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
package org.safehaus.penrose.studio.jdbc.connection.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.safehaus.penrose.studio.util.Helper;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.config.Parameter;
import org.safehaus.penrose.jdbc.JDBC;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.connection.ConnectionManagerClient;
import org.safehaus.penrose.connection.ConnectionConfig;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import javax.management.MBeanException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedHashMap;

/**
 * @author Endi S. Dewata
 */
public class JDBCConnectionPropertiesWizardPage extends WizardPage implements ModifyListener {

    public Logger log = LoggerFactory.getLogger(getClass());
    
    public final static String NAME = "JDBC Connection Settings";

    Composite fieldComposite;
    Map<String,Text> fieldMap = new HashMap<String,Text>();

    private Server server;
    private String partitionName;
    
    private Map<String,Parameter> parameters = new LinkedHashMap<String,Parameter>();
    private Map<String,String> parameterValues = new LinkedHashMap<String,String>();

    public JDBCConnectionPropertiesWizardPage() {
        super(NAME);
        setDescription("Enter database connection settings.");
    }

    public void createControl(final Composite parent) {

        //boldFont = new Font(parent.getDisplay(), "Arial", 8, SWT.BOLD);

        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        GridLayout sectionLayout = new GridLayout();
        composite.setLayout(sectionLayout);

        fieldComposite = new Composite(composite, SWT.NONE);
        fieldComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
/*
        Composite buttons = new Composite(composite, SWT.NONE);
        buttons.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        buttons.setLayout(new RowLayout());

        Button testButton = new Button(buttons, SWT.PUSH);
        testButton.setText("Test Connection");

        testButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    Map<String,String> allParameters = getFieldValues();

                    String driverClass = allParameters.get(JDBCClient.DRIVER);
                    String url = allParameters.get(JDBCClient.URL);
                    String username = allParameters.get(JDBCClient.USER);
                    String password = allParameters.get(JDBCClient.PASSWORD);

                    url = Helper.replace(url, allParameters);
                    log.debug("Connecting to "+url);

                    Helper.testJdbcConnection(
                            parent.getShell(),
                            driverClass,
                            url,
                            username,
                            password
                    );

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });
*/
        setPageComplete(validatePage());
    }

    public void dispose() {
        //boldFont.dispose();
    }

    public void setVisible(boolean visible) {
        super.setVisible(visible);

        if (visible) init();
    }

    public void init() {
        try {
            Control child[] = fieldComposite.getChildren();
            for (Control control : child) {
                control.dispose();
            }

            fieldMap.clear();

            GridLayout layout = new GridLayout();
            layout.numColumns = 2;
            fieldComposite.setLayout(layout);

            if (parameters.isEmpty()) {
                addParameter(new Parameter("driver", "Driver", Parameter.TYPE_REQUIRED));
                addParameter(new Parameter("url", "URL", Parameter.TYPE_REQUIRED));
                addParameter(new Parameter("user", "User"));
                addParameter(new Parameter("password", "Password", Parameter.TYPE_PASSWORD));
            }

            for (Parameter parameter : parameters.values()) {
                String name = parameter.getName();
                int type = parameter.getType();

                String value = null;

                if (this.parameterValues != null) {
                    value = this.parameterValues.get(name);
                }

                if (value == null) {
                    value = parameter.getDefaultValue();
                }

                if (type == Parameter.TYPE_REQUIRED) {
                    Label label = new Label(fieldComposite, SWT.NONE);
                    label.setText(parameter.getDisplayName() + "*:");

                    GridData gd = new GridData(GridData.FILL);
                    gd.widthHint = 100;
                    label.setLayoutData(gd);
                    
                } else if (type == Parameter.TYPE_HIDDEN) {

                } else {
                    Label label = new Label(fieldComposite, SWT.NONE);
                    label.setText(parameter.getDisplayName() + ":");

                    GridData gd = new GridData(GridData.FILL);
                    gd.widthHint = 100;
                    label.setLayoutData(gd);
                }

                int style = SWT.BORDER;
                if (type == Parameter.TYPE_READ_ONLY) style |= SWT.READ_ONLY;
                if (type == Parameter.TYPE_PASSWORD) style |= SWT.PASSWORD;

                if (type == Parameter.TYPE_READ_ONLY) {
                    Label l = new Label(fieldComposite, SWT.NONE);
                    l.setText(parameter.getDefaultValue() == null ? "" : parameter.getDefaultValue());
                    l.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

                } else if (type == Parameter.TYPE_HIDDEN) {

                } else {
                    Text text = new Text(fieldComposite, style);
                    text.setText(value == null ? "" : value);
                    text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
                    text.addModifyListener(this);
                    fieldMap.put(parameter.getName(), text);
                }
            }

            new Label(fieldComposite, SWT.NONE);

            Button testButton = new Button(fieldComposite, SWT.PUSH);
            testButton.setText("Test Connection");

            testButton.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent event) {
                    try {
                        Map<String,String> fieldValues = getFieldValues();

                        log.debug("Fields:");
                        for (String name : fieldValues.keySet()) {
                            log.debug(" - "+name+": "+fieldValues.get(name));
                        }

                        Map<String,String> parameterValues = getParameterValues();
                        String url = parameterValues.get(JDBC.URL);
                        url = Helper.replace(url, fieldValues);
                        parameterValues.put(JDBC.URL, url);

                        PenroseClient client = server.getClient();
                        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
                        PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
                        ConnectionManagerClient connectionManagerClient = partitionClient.getConnectionManagerClient();

                        ConnectionConfig connectionConfig = new ConnectionConfig();
                        connectionConfig.setName("Test");
                        connectionConfig.setAdapterName("JDBC");
                        connectionConfig.setParameters(parameterValues);

                        connectionManagerClient.validateConnection(connectionConfig);

                        MessageDialog.openInformation(getShell(), "Test Connection Result", "Connection successful!");

                    } catch (MBeanException mbe) {
                        Throwable e = mbe.getCause();
                        log.error(e.getMessage(), e);
                        ErrorDialog.open(e.getMessage());

                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        ErrorDialog.open(e);
                    }
                }
            });

            fieldComposite.layout();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        setPageComplete(validatePage());
    }

    public String getParameter(String name) {
        Parameter parameter = parameters.get(name);
        int type = parameter.getType();
        if (type == Parameter.TYPE_READ_ONLY || type == Parameter.TYPE_HIDDEN) {
            //System.out.println("hidden "+name+": "+parameter.getDefaultValue());
            return parameter.getDefaultValue();
        }

        Text text = fieldMap.get(name);
        if (text == null) return null;

        String value = text.getText();
        //System.out.println(name+": "+s);
        if ("".equals(value)) return null;

        return value;
    }

    public Map<String,String> getParameterValues() {

        for (Parameter parameter : parameters.values()) {
            if (parameter.getType() == Parameter.TYPE_TEMP) continue;

            String name = parameter.getName();
            String value = getParameter(name);

            if (value == null) {
                parameterValues.remove(name);
            } else {
                parameterValues.put(name, value);
            }
        }

        return parameterValues;
    }

    public Map<String,String> getFieldValues() {
        Map<String,String> map = new HashMap<String,String>();

        for (Parameter parameter : parameters.values()) {
            String name = parameter.getName();
            String value = getParameter(name);

            if (value == null) {
                map.remove(name);
            } else {
                map.put(name, value);
            }
        }

        return map;
    }

    public boolean validatePage() {
        for (Parameter parameter : parameters.values()) {
            String name = parameter.getName();
            String value = getParameter(name);

            if (parameter.getType() == Parameter.TYPE_REQUIRED && value == null) return false;
        }

        return true;
    }

    public void modifyText(ModifyEvent event) {
        setPageComplete(validatePage());
    }

    public void addParameter(Parameter parameter) {
        parameters.put(parameter.getName(), parameter);
    }

    public void setParameters(Collection<Parameter> parameters) {
        this.parameters.clear();
        for (Parameter parameter : parameters) {
            this.parameters.put(parameter.getName(), parameter);
        }
    }

    public void setParameterValues(Map<String, String> parameterValues) {
        this.parameterValues.clear();
        this.parameterValues.putAll(parameterValues);
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
