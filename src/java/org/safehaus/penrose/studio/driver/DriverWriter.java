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
package org.safehaus.penrose.studio.driver;

import java.util.Iterator;
import java.io.FileWriter;

import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.dom4j.tree.DefaultElement;
import org.dom4j.tree.DefaultText;
import org.safehaus.penrose.config.Parameter;

/**
 * @author Endi S. Dewata
 */
public class DriverWriter {

    XMLWriter writer;
    Element root;

    public DriverWriter(String filename) throws Exception {
		FileWriter fw = new FileWriter(filename);
		OutputFormat format = OutputFormat.createPrettyPrint();
        format.setTrimText(false);

		writer = new XMLWriter(fw, format);
		writer.startDocument();

        /*
        writer.startDTD("server",
                "-//Penrose/Penrose Server Configuration DTD 1.0//EN",
                "http://penrose.safehaus.org/dtd/penrose-server-config-1.0.dtd");
                */

        root = new DefaultElement("drivers");
    }

	public void write(Driver driver) throws Exception {
		Element element = new DefaultElement("driver");
        element.addAttribute("name", driver.getName());

        Element adapterName = new DefaultElement("adapter-name");
        adapterName.add(new DefaultText(driver.getAdapterName()));
        element.add(adapterName);

        for (Iterator iter = driver.getParameters().iterator(); iter.hasNext();) {
            Parameter parameter = (Parameter)iter.next();

            Element parameterElement = new DefaultElement("parameter");
            parameterElement.addAttribute("name", parameter.getName());
            parameterElement.addAttribute("type", parameter.getTypeAsString());

            Element paramName = new DefaultElement("display-name");
            paramName.add(new DefaultText(parameter.getDisplayName()));
            parameterElement.add(paramName);

            Element paramValue = new DefaultElement("default-value");
            paramValue.add(new DefaultText(parameter.getDefaultValue()));
            parameterElement.add(paramValue);

            element.add(parameterElement);
        }

        root.add(element);
	}

    public void close() throws Exception {
        writer.write(root);
        writer.close();
    }

}
