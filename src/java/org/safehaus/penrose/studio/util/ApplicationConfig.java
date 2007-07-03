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
import org.safehaus.penrose.studio.project.Project;

public class ApplicationConfig {
	
	private Logger log = Logger.getLogger(getClass().getName());

	Map projects = new TreeMap();
	
    Project currentProject;

	public ApplicationConfig() {
		super();
	}
	
	public void load(File file) throws Exception {
		log.debug("Loading project configurations file from: "+file.getAbsolutePath());
		try {
			Digester digester = new Digester();
			digester.addObjectCreate("config/project", Project.class);
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
	
	public void addProject(Project project) {
		projects.put(project.getName(), project);
	}

    public Project getProject(String name) {
        return (Project)projects.get(name);
    }
    
    public void removeProject(String name) {
        projects.remove(name);
    }

	public Element toElement() {
		Element element = new DefaultElement("config");

		for (Iterator i=projects.values().iterator(); i.hasNext(); ) {
			Project project = (Project)i.next();
			element.add(project.toElement());
		}

		return element;
	}
	
	public Collection getProjects() {
		return projects.values();
	}
	
    public void setCurrentProject(Project project) {
        this.currentProject = project;
    }

	public Project getCurrentProject() {
		return currentProject;
	}
}
