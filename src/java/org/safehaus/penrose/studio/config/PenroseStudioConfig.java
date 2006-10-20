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
import org.safehaus.penrose.studio.project.ProjectConfig;

public class PenroseStudioConfig {

    Logger log = Logger.getLogger(getClass());

    Map projectConfigs = new TreeMap();

    public void addProjectConfig(ProjectConfig projectConfig) {
        projectConfigs.put(projectConfig.getName(), projectConfig);
    }

    public ProjectConfig getProjectConfig(String name) {
        return (ProjectConfig)projectConfigs.get(name);
    }

    public void removeProjectConfig(String name) {
        projectConfigs.remove(name);
    }

    public Collection getProjectConfigs() {
        return projectConfigs.values();
    }
}
