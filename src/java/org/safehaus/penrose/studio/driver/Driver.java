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
package org.safehaus.penrose.studio.driver;

import org.safehaus.penrose.studio.driver.Parameter;

import java.util.Map;
import java.util.Collection;
import java.util.LinkedHashMap;

/**
 * @author Endi S. Dewata
 */
public class Driver {

    private String name;
    private String adapterName;

    private Map parameters = new LinkedHashMap();

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

    public void removeParameters() {
        parameters.clear();
    }

    public Parameter getParameter(String name) {
        return (Parameter)parameters.get(name);
    }
    
    public Collection getParameters() {
        return parameters.values();
    }

    public String getAdapterName() {
        return adapterName;
    }

    public void setAdapterName(String adapterName) {
        this.adapterName = adapterName;
    }
}
