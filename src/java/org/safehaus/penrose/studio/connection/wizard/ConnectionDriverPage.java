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
package org.safehaus.penrose.studio.connection.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.safehaus.penrose.studio.driver.DriverWizard;
import org.safehaus.penrose.studio.driver.DriverReader;
import org.safehaus.penrose.studio.driver.DriverWriter;
import org.safehaus.penrose.studio.driver.Driver;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.TreeMap;
import java.util.Iterator;
import java.util.Collection;

/**
 * @author Endi S. Dewata
 */
public class ConnectionDriverPage extends WizardPage implements SelectionListener {

    Logger log = Logger.getLogger(getClass());

    public final static String NAME = "Connection Type";

    Map drivers = new TreeMap();
    Table adapterTable;

    public ConnectionDriverPage() {
        super(NAME);
        setDescription("Select the type of the connection.");

        try {
            DriverReader reader = new DriverReader("conf/drivers.xml");
            Collection list = reader.getDrivers();

            for (Iterator i=list.iterator(); i.hasNext(); ) {
                Driver driver = (Driver)i.next();
                addDriver(driver);
            }

        } catch (Exception e) {
            log.debug(e.getMessage(), e);
            MessageDialog.openError(getShell(), "Error", e.getMessage());
        }
/*
        Driver jndi = new Driver("LDAP");
        jndi.setAdapterName("JNDI");
        jndi.addParameter(new HiddenParameter("java.naming.factory.initial", "Driver", "com.sun.jndi.ldap.LdapCtxFactory"));
        jndi.addParameter(new HiddenParameter("java.naming.provider.url", "URL", "ldap://${host}:${port}/${baseDn}"));
        jndi.addParameter(new RequiredParameter("host", "Host", "localhost"));
        jndi.addParameter(new RequiredParameter("port", "Port", "389"));
        jndi.addParameter(new RequiredParameter("baseDn", "Base DN"));
        jndi.addParameter(new Parameter("java.naming.security.principal", "User"));
        jndi.addParameter(new PasswordParameter("java.naming.security.credentials", "Password"));
        addDriver(jndi);

        Driver jdbc = new Driver("JDBC");
        jdbc.setAdapterName("JDBC");
        jdbc.addParameter(new RequiredParameter("driver", "Driver"));
        jdbc.addParameter(new RequiredParameter("url", "URL"));
        jdbc.addParameter(new Parameter("user", "User"));
        jdbc.addParameter(new PasswordParameter("password", "Password"));
        addDriver(jdbc);

        Driver odbc = new Driver("ODBC");
        odbc.setAdapterName("JDBC");
        odbc.addParameter(new HiddenParameter("driver", "Driver", "sun.jdbc.odbc.JdbcOdbcDriver"));
        odbc.addParameter(new HiddenParameter("url", "URL", "jdbc:odbc:${datasource}"));
        odbc.addParameter(new RequiredParameter("datasource", "Data Source"));
        odbc.addParameter(new Parameter("user", "Username"));
        odbc.addParameter(new PasswordParameter("password", "Password"));
        addDriver(odbc);

        Driver mysql = new Driver("MySQL");
        mysql.setAdapterName("JDBC");
        mysql.addParameter(new HiddenParameter("driver", "Driver", "com.mysql.jdbc.Driver"));
        mysql.addParameter(new HiddenParameter("url", "URL", "jdbc:mysql://${host}:${port}/${database}?autoReconnect=true"));
        mysql.addParameter(new RequiredParameter("host", "Host", "localhost"));
        mysql.addParameter(new RequiredParameter("port", "Port", "3306"));
        mysql.addParameter(new RequiredParameter("database", "Database"));
        mysql.addParameter(new Parameter("user", "Username"));
        mysql.addParameter(new PasswordParameter("password", "Password"));
        addDriver(mysql);

        Driver oracle = new Driver("Oracle");
        oracle.setAdapterName("JDBC");
        oracle.addParameter(new HiddenParameter("driver", "Driver", "oracle.jdbc.driver.OracleDriver"));
        oracle.addParameter(new HiddenParameter("url", "URL", "jdbc:oracle:thin:@${host}:${port}:${sid}"));
        oracle.addParameter(new RequiredParameter("host", "Host", "localhost"));
        oracle.addParameter(new RequiredParameter("port", "Port", "1521"));
        oracle.addParameter(new RequiredParameter("sid", "SID"));
        oracle.addParameter(new Parameter("user", "Username"));
        oracle.addParameter(new PasswordParameter("password", "Password"));
        addDriver(oracle);

        Driver postgres = new Driver("PostgreSQL");
        postgres.setAdapterName("JDBC");
        postgres.addParameter(new HiddenParameter("driver", "Driver", "org.postgresql.Driver"));
        postgres.addParameter(new HiddenParameter("url", "URL", "jdbc:postgresql://${host}:${port}/${database}"));
        postgres.addParameter(new RequiredParameter("host", "Host", "localhost"));
        postgres.addParameter(new RequiredParameter("port", "Port", "5432"));
        postgres.addParameter(new RequiredParameter("database", "Database"));
        postgres.addParameter(new Parameter("user", "Username"));
        postgres.addParameter(new PasswordParameter("password", "Password"));
        addDriver(postgres);

        Driver mssql = new Driver("Microsoft SQL Server");
        mssql.setAdapterName("JDBC");
        mssql.addParameter(new HiddenParameter("driver", "Driver", "com.microsoft.jdbc.sqlserver.SQLServerDriver"));
        mssql.addParameter(new HiddenParameter("url", "URL", "jdbc:microsoft:sqlserver://${host}:${port};databasename=${database}"));
        mssql.addParameter(new RequiredParameter("host", "Host", "localhost"));
        mssql.addParameter(new RequiredParameter("port", "Port", "1433"));
        mssql.addParameter(new RequiredParameter("database", "Database"));
        mssql.addParameter(new Parameter("user", "Username"));
        mssql.addParameter(new PasswordParameter("password", "Password"));
        addDriver(mssql);

        Driver sybase = new Driver("Sybase");
        sybase.setAdapterName("JDBC");
        sybase.addParameter(new HiddenParameter("driver", "Driver", "com.sybase.jdbc2.jdbc.SybDriver"));
        sybase.addParameter(new HiddenParameter("url", "URL", "jdbc:sybase:Tds:${host}:${port}/${database}"));
        sybase.addParameter(new RequiredParameter("host", "Host", "localhost"));
        sybase.addParameter(new RequiredParameter("port", "Port", "1403"));
        sybase.addParameter(new RequiredParameter("database", "Database"));
        sybase.addParameter(new Parameter("user", "Username"));
        sybase.addParameter(new PasswordParameter("password", "Password"));
        addDriver(sybase);
*/
    }

    public void createControl(final Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        GridLayout sectionLayout = new GridLayout();
        sectionLayout.numColumns = 2;
        composite.setLayout(sectionLayout);

        Label adapterLabel = new Label(composite, SWT.NONE);
        adapterLabel.setText("Driver:");
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        adapterLabel.setLayoutData(gd);

        adapterTable = new Table(composite, SWT.BORDER);
        adapterTable.setLayoutData(new GridData(GridData.FILL_BOTH));
        adapterTable.addSelectionListener(this);

        Composite buttons = new Composite(composite, SWT.NONE);
        buttons.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        buttons.setLayout(new GridLayout());

        Button addButton = new Button(buttons, SWT.PUSH);
        addButton.setText("Add");
        addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    DriverWizard wizard = new DriverWizard();
                    WizardDialog dialog = new WizardDialog(parent.getShell(), wizard);
                    dialog.setPageSize(600, 300);
                    dialog.open();

                    Driver driver = wizard.getDriver();
                    if (driver == null) return;

                    addDriver(driver);
                    saveDrivers();

                    refresh();

                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                }
            }
        });

        Button editButton = new Button(buttons, SWT.PUSH);
        editButton.setText("Edit");
        editButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        editButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    if (adapterTable.getSelectionCount() == 0) return;

                    TableItem item = adapterTable.getSelection()[0];
                    Driver driver = (Driver)item.getData();

                    DriverWizard wizard = new DriverWizard(driver);
                    WizardDialog dialog = new WizardDialog(parent.getShell(), wizard);
                    dialog.setPageSize(600, 300);
                    dialog.open();

                    saveDrivers();
                    refresh();

                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                }
            }
        });

        Button removeButton = new Button(buttons, SWT.PUSH);
        removeButton.setText("Remove");
        removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    if (adapterTable.getSelectionCount() == 0) return;

                    TableItem item = adapterTable.getSelection()[0];
                    String name = item.getText();

                    removeDriver(name);
                    saveDrivers();
                    refresh();
                    setPageComplete(validatePage());

                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                }
            }
        });

        refresh();

        setPageComplete(validatePage());
    }

    public void refresh() {
        adapterTable.removeAll();
        for (Iterator i=drivers.keySet().iterator(); i.hasNext(); ) {
            String name = (String)i.next();
            Driver driver = (Driver)drivers.get(name);

            TableItem item = new TableItem(adapterTable, SWT.NONE);
            item.setText(name);
            item.setData(driver);
        }
    }

    public void addDriver(Driver type) {
        drivers.put(type.getName(), type);
    }

    public Driver removeDriver(String name) {
        return (Driver)drivers.remove(name);
    }

    public void saveDrivers() throws Exception {
        DriverWriter writer = new DriverWriter("conf/drivers.xml");
        for (Iterator i=drivers.values().iterator(); i.hasNext(); ) {
            Driver driver = (Driver)i.next();
            writer.write(driver);
        }
        writer.close();
    }

    public Driver getDriver() {
        if (adapterTable.getSelectionCount() == 0) return null;

        TableItem item = adapterTable.getSelection()[0];
        return (Driver)item.getData();
    }

    public boolean validatePage() {
        if (getDriver() == null) return false;
        return true;
    }

    public void widgetSelected(SelectionEvent event) {
        setPageComplete(validatePage());
    }

    public void widgetDefaultSelected(SelectionEvent event) {
    }
}
