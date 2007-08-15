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
package org.safehaus.penrose.studio.jdbc.connection;

import org.eclipse.jface.wizard.WizardPage;
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
import org.eclipse.swt.layout.RowLayout;
import org.safehaus.penrose.studio.util.Helper;
import org.safehaus.penrose.studio.driver.Parameter;
import org.safehaus.penrose.studio.driver.Driver;
import org.safehaus.penrose.studio.connection.wizard.ConnectionDriverPage;
import org.safehaus.penrose.jdbc.JDBCClient;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Endi S. Dewata
 */
public class JDBCConnectionWizardPage extends WizardPage implements ModifyListener {

    Logger log = Logger.getLogger(getClass());
    
    public final static String NAME = "JDBC Connection Property";

    //Font boldFont;
    Composite fieldComposite;

    Map<String,Text> fieldMap = new HashMap<String,Text>();

    public JDBCConnectionWizardPage() {
        super(NAME);
    }

    public void createControl(final Composite parent) {

        //boldFont = new Font(parent.getDisplay(), "Arial", 8, SWT.BOLD);

        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        GridLayout sectionLayout = new GridLayout();
        composite.setLayout(sectionLayout);

        fieldComposite = new Composite(composite, SWT.NONE);
        fieldComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite buttons = new Composite(composite, SWT.NONE);
        buttons.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        buttons.setLayout(new RowLayout());

        Button testButton = new Button(buttons, SWT.PUSH);
        testButton.setText("Test Connection");

        testButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    ConnectionDriverPage page = (ConnectionDriverPage)getWizard().getPage(ConnectionDriverPage.NAME);
                    Driver type = page.getDriver();
                    if (type == null) return;

                    Map<String,String> allParameters = getAllParameters();

                    String driver = allParameters.get(JDBCClient.DRIVER);
                    String url = allParameters.get(JDBCClient.URL);
                    String username = allParameters.get(JDBCClient.USER);
                    String password = allParameters.get(JDBCClient.PASSWORD);

                    url = Helper.replace(url, allParameters);
                    System.out.println("Connecting to "+url);

                    Helper.testJdbcConnection(
                            parent.getShell(),
                            driver,
                            url,
                            username,
                            password
                    );

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

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
            ConnectionDriverPage page = (ConnectionDriverPage)getWizard().getPage(ConnectionDriverPage.NAME);
            Driver driver = page.getDriver();
            if (driver == null) return;

            setDescription("Enter "+driver.getName()+" connection properties.");

            Control child[] = fieldComposite.getChildren();
            for (Control control : child) {
                control.dispose();
            }

            fieldMap.clear();

            GridLayout layout = new GridLayout();
            layout.numColumns = 2;
            fieldComposite.setLayout(layout);

            Collection<Parameter> parameters = driver.getParameters();
            for (Parameter parameter : parameters) {
                int type = parameter.getType();

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
                    text.setText(parameter.getDefaultValue() == null ? "" : parameter.getDefaultValue());
                    text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
                    text.addModifyListener(this);
                    fieldMap.put(parameter.getName(), text);
                }

            }

            fieldComposite.layout();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public String getParameter(String name) {
        ConnectionDriverPage page = (ConnectionDriverPage)getWizard().getPage(ConnectionDriverPage.NAME);
        Driver driver = page.getDriver();
        if (driver == null) return null;

        Parameter parameter = driver.getParameter(name);
        int type = parameter.getType();
        if (type == Parameter.TYPE_READ_ONLY || type == Parameter.TYPE_HIDDEN) {
            //System.out.println("hidden "+name+": "+parameter.getDefaultValue());
            return parameter.getDefaultValue();
        }

        Text text = fieldMap.get(name);
        if (text == null) return null;

        String s = text.getText();
        //System.out.println(name+": "+s);
        if ("".equals(s)) return null;

        return s;
    }

    public Map<String,String> getParameters() {
        Map<String,String> map = new HashMap<String,String>();

        ConnectionDriverPage page = (ConnectionDriverPage)getWizard().getPage(ConnectionDriverPage.NAME);
        Driver driver = page.getDriver();
        if (driver == null) return map;

        Collection<Parameter> parameters = driver.getParameters();
        for (Parameter parameter : parameters) {
            if (parameter.getType() == Parameter.TYPE_TEMP) continue;

            String name = parameter.getName();
            String value = getParameter(name);
            if (value == null) continue;

            map.put(name, value);
        }

        return map;
    }

    public Map<String,String> getAllParameters() {
        Map<String,String> map = new HashMap<String,String>();

        ConnectionDriverPage page = (ConnectionDriverPage)getWizard().getPage(ConnectionDriverPage.NAME);
        Driver driver = page.getDriver();
        if (driver == null) return map;

        Collection<Parameter> parameters = driver.getParameters();
        for (Parameter parameter : parameters) {
            String name = parameter.getName();
            String value = getParameter(name);
            if (value == null) continue;

            map.put(name, value);
        }

        return map;
    }

    public boolean validatePage() {
        ConnectionDriverPage page = (ConnectionDriverPage)getWizard().getPage(ConnectionDriverPage.NAME);
        Driver driver = page.getDriver();
        if (driver == null) return false;

        Collection<Parameter> parameters = driver.getParameters();
        for (Parameter parameter : parameters) {
            String name = parameter.getName();
            String value = getParameter(name);

            if (parameter.getType() == Parameter.TYPE_REQUIRED && value == null) return false;
        }

        return true;
    }

    public void modifyText(ModifyEvent event) {
        setPageComplete(validatePage());
    }
}
