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
package org.safehaus.penrose.studio.engine.editor;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.jface.resource.ImageDescriptor;
import org.safehaus.penrose.engine.EngineConfig;
import org.safehaus.penrose.studio.server.Server;

/**
 * @author Endi S. Dewata
 */
public class EngineEditorInput implements IEditorInput {

    private Server server;
    private EngineConfig engineConfig;

    public boolean exists() {
        return true;
    }

    public ImageDescriptor getImageDescriptor() {
        return null;
    }

    public String getName() {
        return "["+server.getName()+"] Engine - "+engineConfig.getName();
    }

    public IPersistableElement getPersistable() {
        return null;
    }

    public String getToolTipText() {
        return getName();
    }

    public Object getAdapter(Class aClass) {
        return null;
    }

    boolean equals(Object o1, Object o2) {
        if (o1 == null && o2 == null) return true;
        if (o1 != null) return o1.equals(o2);
        return o2.equals(o1);
    }

    public boolean equals(Object object) {
        if (object == null || object.getClass() != getClass()) return false;

        EngineEditorInput ei = (EngineEditorInput)object;

        if (!equals(engineConfig, ei.engineConfig)) return false;

        return true;
    }

    public int hashCode() {
        return (server == null ? 0 : server.hashCode()) +
                (engineConfig == null ? 0 : engineConfig.hashCode());
    }

    public EngineConfig getEngineConfig() {
        return engineConfig;
    }

    public void setEngineConfig(EngineConfig engineConfig) {
        this.engineConfig = engineConfig;
    }

    public Server getProject() {
        return server;
    }

    public void setProject(Server server) {
        this.server = server;
    }
}
