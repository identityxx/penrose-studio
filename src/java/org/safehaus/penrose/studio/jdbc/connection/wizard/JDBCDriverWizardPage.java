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

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;
import org.safehaus.penrose.studio.PenroseStudioPlugin;
import org.safehaus.penrose.studio.driver.Driver;
import org.safehaus.penrose.studio.driver.DriverReader;
import org.safehaus.penrose.studio.driver.DriverWizard;
import org.safehaus.penrose.studio.driver.DriverWriter;
import org.safehaus.penrose.config.Parameter;

/**
 * @author Endi S. Dewata
 */
public class JDBCDriverWizardPage extends WizardPage implements SelectionListener {

    Logger log = Logger.getLogger(getClass());

    public final static String NAME = "JDBC Driver";

    Table adapterTable;

    Map<String,Driver> drivers = new TreeMap<String,Driver>();
    String driverClass;

    public JDBCDriverWizardPage() {
        super(NAME);
        setDescription("Select the JDBC driver.");

        try {
        	DriverReader reader = new DriverReader("conf/drivers.xml");
            Collection<Driver> list = reader.getDrivers();
            
            for (Driver driver : list) {
                if (!"JDBC".equals(driver.getAdapterName())) continue;
                addDriver(driver);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    
    private Collection loadLibraries() throws Exception {

		// bundles will be children of the lib directory. eg: conf/lib/mysql
		File f = new File("conf/lib");

		log.debug("Dynamically Loading libraries on " + f);

		String[] dirs = f.list();
        for (String dir1 : dirs) {
            File dir = new File(f, dir1);


            log.debug("Installing bundle for " + dir);

            try {

                this.installBundle("file://" + dir.getAbsolutePath());

            } catch (BundleException e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException(e.getMessage(), e);

            } catch (ClassCastException e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException(e.getMessage(), e);
            }


            Driver d = new Driver();


        }
        // Class cl = Class.forName("com.mysql.jdbc.Driver");
		// com.mysql.jdbc.Driver d = (com.mysql.jdbc.Driver)cl.newInstance();
		return new ArrayList();
	}
    
    private void installBundle(String dir) throws BundleException, ClassNotFoundException {
    	BundleContext context = PenroseStudioPlugin.getInstance().getBundleContext();
    	
    	Bundle bundle = context.installBundle(dir);
		
		/*
		 * Enumeration e = bundle.getHeaders().elements();
		 * while(e.hasMoreElements()){ System.out.println(e.nextElement()); }
		 */

		ServiceReference packageAdminRef = context.getServiceReference(PackageAdmin.class.getName());
		PackageAdmin packageAdmin = null;
		if (packageAdminRef != null) {
			packageAdmin = (PackageAdmin) context.getService(packageAdminRef);

			packageAdmin.refreshPackages(new Bundle[] { bundle });

		}

		Class c = bundle.loadClass("com.mysql.jdbc.Driver");

		Method[] m = c.getMethods();
        for (Method method : m) {
            System.out.println(method.getName());

        }
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
                    int rc = dialog.open();

                    if (rc == Window.CANCEL) return;

                    Driver driver = wizard.getDriver();
                    if (driver == null) return;

                    addDriver(driver);
                    saveDrivers();

                    refresh();
                    

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e.getMessage(), e);
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
                    int rc = dialog.open();

                    if (rc == Window.CANCEL) return;

                    saveDrivers();
                    refresh();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e.getMessage(), e);
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
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });

        refresh();

        if (driverClass != null) {
            for (TableItem item : adapterTable.getItems()) {
                Driver driver = (Driver)item.getData();

                Parameter parameter = driver.getParameter("driver");
                if (parameter == null) continue;
                if (!driverClass.equals(parameter.getDefaultValue())) continue;

                adapterTable.setSelection(item);
                break;
            }
        }

        setPageComplete(validatePage());
    }

    public void refresh() {
        adapterTable.removeAll();

        for (String name : drivers.keySet()) {
            Driver driver = drivers.get(name);

            TableItem item = new TableItem(adapterTable, SWT.NONE);
            item.setText(name);
            item.setData(driver);
        }
    }

    public Collection<Driver> getDrivers() {
        return drivers.values();
    }

    public void addDriver(Driver driver) {
        drivers.put(driver.getName(), driver);
    }

    public Driver removeDriver(String name) {
        return drivers.remove(name);
    }

    public void saveDrivers() throws Exception {
        DriverWriter writer = new DriverWriter("conf/drivers.xml");
        for (Driver driver : drivers.values()) {
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
        return getDriver() != null;
    }

    public void widgetSelected(SelectionEvent event) {
        setPageComplete(validatePage());
    }

    public void widgetDefaultSelected(SelectionEvent event) {
    }

    public String getDriverClass() {
        return driverClass;
    }

    public void setDriverClass(String driverClass) {
        this.driverClass = driverClass;
    }
}
