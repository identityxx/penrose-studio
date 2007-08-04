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

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.xmlrules.DigesterLoader;
import org.apache.log4j.Logger;

import java.io.File;
import java.net.URL;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Endi S. Dewata
 */
public class DriverReader {

    public Logger log = Logger.getLogger(DriverReader.class);

    Collection<Driver> drivers = new ArrayList<Driver>();
    Iterator iterator;

    public DriverReader(String filename) throws Exception{
        File file = new File(filename);
        log.debug("Loading drivers from: "+file);

        ClassLoader cl = getClass().getClassLoader();
        URL url = cl.getResource("org/safehaus/penrose/studio/driver/drivers-digester-rules.xml");

        Digester digester = DigesterLoader.createDigester(url);
        digester.setValidating(false);
        digester.setClassLoader(cl);
        digester.push(this);
        digester.parse(file);
    }

    public void addDriver(Driver driver) {
        drivers.add(driver);
    }

	public Collection<Driver> getDrivers() {
        return drivers;
	}
}
