/**
 * Copyright (c) 2000-2005, Identyx Corporation.
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
package org.safehaus.penrose.studio.connection.wizard;

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
import org.safehaus.penrose.connector.JDBCAdapter;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Iterator;
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

    Map fieldMap = new HashMap();

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

                    String driver = getParameter(JDBCAdapter.DRIVER);
                    String url = getParameter(JDBCAdapter.URL);
                    String username = getParameter(JDBCAdapter.USER);
                    String password = getParameter(JDBCAdapter.PASSWORD);

                    url = Helper.replace(url, getParameters());
                    System.out.println("Connecting to "+url);

                    Helper.testJdbcConnection(
                            parent.getShell(),
                            driver,
                            url,
                            username,
                            password
                    );

                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                }
            }
        });

        setPageComplete(validatePage());
    }

    public void dispose() {
        //boldFont.dispose();
    }

    public void setVisible(boolean visible) {
        System.out.println("[JDBCConnectionWizardPage] setVisible: "+visible);
        super.setVisible(visible);

        if (visible) init();
    }

    public void init() {
        System.out.println("[JDBCConnectionWizardPage] init");
        try {
            ConnectionDriverPage page = (ConnectionDriverPage)getWizard().getPage(ConnectionDriverPage.NAME);
            Driver type = page.getDriver();
            if (type == null) return;

            setDescription("Enter "+type.getName()+" connection properties.");

            Control child[] = fieldComposite.getChildren();
            for (int i=0; i<child.length; i++) {
                child[i].dispose();
            }

            fieldMap.clear();

            GridLayout layout = new GridLayout();
            layout.numColumns = 2;
            fieldComposite.setLayout(layout);

            Collection fields = type.getParameters();
            for (Iterator i=fields.iterator(); i.hasNext(); ) {
                Parameter parameter = (Parameter)i.next();

                Label label = new Label(fieldComposite, SWT.NONE);
                if (parameter.getType() == Parameter.TYPE_REQUIRED) {
                    label.setText(parameter.getDisplayName()+"*:");
                } else {
                    label.setText(parameter.getDisplayName()+":");
                }
                GridData gd = new GridData(GridData.FILL);
                gd.widthHint = 100;
                label.setLayoutData(gd);

                int style = SWT.BORDER;
                if (parameter.getType() == Parameter.TYPE_HIDDEN) style |= SWT.READ_ONLY;
                if (parameter.getType() == Parameter.TYPE_PASSWORD) style |= SWT.PASSWORD;

                if (parameter.getType() == Parameter.TYPE_HIDDEN) {
                    Label l = new Label(fieldComposite, SWT.NONE);
                    l.setText(parameter.getDefaultValue() == null ? "" : parameter.getDefaultValue());
                    l.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
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
            log.debug(e.getMessage(), e);
        }
    }

    public String getParameter(String name) {
        ConnectionDriverPage page = (ConnectionDriverPage)getWizard().getPage(ConnectionDriverPage.NAME);
        Driver type = page.getDriver();
        if (type == null) return null;

        Parameter parameter = type.getParameter(name);
        if (parameter.getType() == Parameter.TYPE_HIDDEN) {
            //System.out.println("hidden "+name+": "+parameter.getDefaultValue());
            return parameter.getDefaultValue();
        }

        Text text = (Text)fieldMap.get(name);
        if (text == null) return null;

        String s = text.getText();
        //System.out.println(name+": "+s);
        if ("".equals(s)) return null;

        return s;
    }

    public Map getParameters() {
        Map map = new HashMap();

        ConnectionDriverPage page = (ConnectionDriverPage)getWizard().getPage(ConnectionDriverPage.NAME);
        Driver type = page.getDriver();
        if (type == null) return map;

        Collection parameters = type.getParameters();
        for (Iterator i=parameters.iterator(); i.hasNext(); ) {
            Parameter parameter = (Parameter)i.next();
            String name = parameter.getName();
            String value = getParameter(name);
            if (value == null) continue;

            map.put(name, value);
        }

        return map;
    }

    public boolean validatePage() {
        System.out.println("[JDBCConnectionWizardPage] validatePage");

        ConnectionDriverPage page = (ConnectionDriverPage)getWizard().getPage(ConnectionDriverPage.NAME);
        Driver type = page.getDriver();
        if (type == null) return false;

        Collection parameters = type.getParameters();
        for (Iterator i=parameters.iterator(); i.hasNext(); ) {
            Parameter parameter = (Parameter)i.next();
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
