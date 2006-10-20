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
package org.safehaus.penrose.studio.config;

import java.util.*;

import org.apache.log4j.Logger;
import org.safehaus.penrose.studio.server.ServerConfig;

public class PenroseStudioConfig {

    Logger log = Logger.getLogger(getClass());

    Map serverConfigs = new TreeMap();

    public void addServerConfig(ServerConfig serverConfig) {
        serverConfigs.put(serverConfig.getName(), serverConfig);
    }

    public ServerConfig getServerConfig(String name) {
        return (ServerConfig)serverConfigs.get(name);
    }

    public void removeServerConfig(String name) {
        serverConfigs.remove(name);
    }

    public Collection getServerConfigs() {
        return serverConfigs.values();
    }
}
