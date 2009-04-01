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
	
	Logger log = Logger.getLogger(getClass());

	Map<String,ServerConfig> serverConfigs = new TreeMap<String,ServerConfig>();
	
	public ApplicationConfig() {
		super();
	}
	
	public void load(File file) throws Exception {
		log.debug("Loading configuration from "+file+".");
		try {
			Digester digester = new Digester();
			digester.addObjectCreate("config/project", ServerConfig.class);
			digester.addSetProperties("config/project");
			digester.addSetNext("config/project", "addServerConfig");
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
	
    public ServerConfig getServerConfig(String name) {
        return serverConfigs.get(name);
    }

	public void addServerConfig(ServerConfig serverConfig) throws Exception {
        String name = serverConfig.getName();
        if (serverConfigs.containsKey(name)) {
            throw new Exception("Server \""+name+"\" already exists.");
        }

        serverConfigs.put(name, serverConfig);
	}

    public void updateServerConfig(String name, ServerConfig newServerConfig) throws Exception {
        String newName = newServerConfig.getName();
        if (name.equals(newName)) {
            removeServerConfig(name);
            addServerConfig(newServerConfig);

        } else {
            addServerConfig(newServerConfig);
            removeServerConfig(name);
        }
    }

    public void removeServerConfig(String name) throws Exception {
        ServerConfig serverConfig = serverConfigs.remove(name);
        if (serverConfig == null) {
            throw new Exception("Server \""+name+"\" not found.");
        }
    }

	public Element toElement() {
		Element element = new DefaultElement("config");

        for (ServerConfig serverConfig : serverConfigs.values()) {
            element.add(serverConfig.toElement());
        }

        return element;
	}
	
	public Collection<ServerConfig> getServerConfigs() {
		return serverConfigs.values();
	}
}
