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
package org.safehaus.penrose.studio.util;

import java.io.File;
import java.io.FileWriter;
import java.util.*;

import org.apache.commons.digester.Digester;
import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.dom4j.tree.DefaultElement;
import org.safehaus.penrose.studio.server.ServerConfig;
import org.safehaus.penrose.util.FileUtil;

public class ApplicationConfig {
	
	private Logger log = Logger.getLogger(getClass().getName());

	Map<String, ServerConfig> projects = new TreeMap<String, ServerConfig>();
	
    ServerConfig currentProjectConfig;

	public ApplicationConfig() {
		super();
	}
	
	public void load(File file) throws Exception {
		log.debug("Loading configuration from "+file+".");
		try {
			Digester digester = new Digester();
			digester.addObjectCreate("config/project", ServerConfig.class);
			digester.addSetProperties("config/project");
			digester.addSetNext("config/project", "addProject");
			digester.setValidating(false);
	        digester.setClassLoader(this.getClass().getClassLoader());
			digester.push(this);
			digester.parse(file);
            
		} catch (Exception ex) {
			log.debug(file.getAbsoluteFile()+" exists? "+file.exists());
			log.error(ex.getMessage(), ex);
			String s = FileUtil.getContent(file);
			log.debug(s);
		}
	}
	
	public void save(File file) throws Exception {
		FileWriter fw = new FileWriter(file);
		OutputFormat format = OutputFormat.createPrettyPrint();
        format.setTrimText(false);

		XMLWriter writer = new XMLWriter(fw, format);
		writer.startDocument();
		writer.write(toElement());
		writer.close();
	}
	
	public void addProject(ServerConfig projectConfig) {
		projects.put(projectConfig.getName(), projectConfig);
	}

    public ServerConfig getProject(String name) {
        return projects.get(name);
    }
    
    public void removeProject(String name) {
        projects.remove(name);
    }

	public Element toElement() {
		Element element = new DefaultElement("config");

        for (ServerConfig projectConfig : projects.values()) {
            element.add(projectConfig.toElement());
        }

        return element;
	}
	
	public Collection<ServerConfig> getProjects() {
		return projects.values();
	}
	
    public void setCurrentProject(ServerConfig projectConfig) {
        this.currentProjectConfig = projectConfig;
    }

	public ServerConfig getCurrentProject() {
		return currentProjectConfig;
	}
}
