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
package org.safehaus.penrose.studio.driver;

import org.eclipse.jface.wizard.Wizard;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author Endi S. Dewata
 */
public class DriverWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    private Driver driver;

    public DriverPropertyPage propertyPage = new DriverPropertyPage();
    public DriverParameterPage parameterPage = new DriverParameterPage();

    public DriverWizard() {
        setWindowTitle("New Driver");
    }

    public DriverWizard(Driver driver) {
        this.driver = driver;
        setWindowTitle("Edit Driver");
    }

    public boolean canFinish() {
        if (!propertyPage.isPageComplete()) return false;
        if (!parameterPage.isPageComplete()) return false;

        return true;
    }

    public boolean performFinish() {
        try {

            if (driver == null) driver = new Driver();

            driver.setName(propertyPage.getDriverName());
            driver.setAdapterName(propertyPage.getAdapterName());

            driver.removeParameters();

            Collection list = parameterPage.getParameters();
            for (Iterator i=list.iterator(); i.hasNext(); ) {
                Parameter parameter = (Parameter)i.next();
                driver.addParameter(parameter);
            }

            return true;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    public Driver getDriver() {
        return driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    public void addPages() {
        addPage(propertyPage);
        addPage(parameterPage);
    }

    public boolean needsPreviousAndNextButtons() {
        return true;
    }

}
