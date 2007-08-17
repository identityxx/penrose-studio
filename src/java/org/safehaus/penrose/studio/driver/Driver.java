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

import java.util.Map;
import java.util.Collection;
import java.util.LinkedHashMap;

/**
 * @author Endi S. Dewata
 */
public class Driver implements Cloneable {

    private String name;
    private String adapterName;

    private Map<String,Parameter> parameters = new LinkedHashMap<String,Parameter>();

    public Driver() {
    }

    public Driver(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addParameter(Parameter parameter) {
        parameters.put(parameter.getName(), parameter);
    }

    public Parameter removeParameter(String name) {
        return parameters.remove(name);
    }

    public void removeParameters() {
        parameters.clear();
    }

    public Parameter getParameter(String name) {
        return parameters.get(name);
    }
    
    public Collection<Parameter> getParameters() {
        return parameters.values();
    }

    public String getAdapterName() {
        return adapterName;
    }

    public void setAdapterName(String adapterName) {
        this.adapterName = adapterName;
    }

    public Object clone() throws CloneNotSupportedException {

        Driver driver = (Driver)super.clone();

        driver.parameters = new LinkedHashMap<String,Parameter>();
        for (String key : parameters.keySet()) {
            Parameter parameter = parameters.get(key);
            driver.parameters.put(key, (Parameter)parameter.clone());
        }

        return driver;
    }
}
