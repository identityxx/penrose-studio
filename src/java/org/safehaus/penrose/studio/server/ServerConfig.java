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
package org.safehaus.penrose.studio.server;

import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import org.safehaus.penrose.client.PenroseClient;

import java.io.Serializable;

public class ServerConfig implements Serializable, Cloneable {

	private String name;
    private String type = PenroseClient.PENROSE;
	private String host = "localhost";
    private int port = 0;
    private String username;
    private String password;

    public ServerConfig() {
    }

	public Element toElement() {
		Element element = new DefaultElement("project");
		element.addAttribute("name", name);
        element.addAttribute("type", type);
		element.addAttribute("host", host);
        if (port > 0) element.addAttribute("port", ""+port);
		element.addAttribute("username", username);
		element.addAttribute("password", password);
		return element;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int hashCode() {
        return name == null ? 0 : name.hashCode();
    }

    boolean equals(Object o1, Object o2) {
        if (o1 == null && o2 == null) return true;
        if (o1 != null) return o1.equals(o2);
        return o2.equals(o1);
    }

    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null) return false;
        if (object.getClass() != this.getClass()) return false;

        ServerConfig serverConfig = (ServerConfig)object;
        if (!equals(name, serverConfig.name)) return false;
        if (!equals(type, serverConfig.type)) return false;
        if (!equals(host, serverConfig.host)) return false;
        if (!equals(port, serverConfig.port)) return false;
        if (!equals(username, serverConfig.username)) return false;
        if (!equals(password, serverConfig.password)) return false;

        return true;
    }

    public void copy(ServerConfig serverConfig) {
        name = serverConfig.name;
        type = serverConfig.type;
        host = serverConfig.host;
        port = serverConfig.port;
        username = serverConfig.username;
        password = serverConfig.password;
    }

    public Object clone() throws CloneNotSupportedException {
        ServerConfig serverConfig = (ServerConfig)super.clone();
        serverConfig.copy(this);
        return serverConfig;
    }
}

