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
package org.safehaus.penrose.studio.connector;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.connector.ConnectorConfig;
import org.apache.log4j.Logger;

/**
 * @author Endi S. Dewata
 */
public class ConnectorEditor extends MultiPageEditorPart {

    Logger log = Logger.getLogger(getClass());

    Server server;

    ConnectorConfig connectorConfig;
    ConnectorConfig origConnectorConfig;

    boolean dirty;

    ConnectorPropertyPage propertyPage;
    //ConnectorCachePage cachePage;

    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        setSite(site);
        setInput(input);

        ConnectorEditorInput ei = (ConnectorEditorInput)input;
        server = ei.getProject();
        origConnectorConfig = ei.getConnectorConfig();
        connectorConfig = (ConnectorConfig)origConnectorConfig.clone();

        setPartName(input.getName());
    }

    protected void createPages() {
        try {
            propertyPage = new ConnectorPropertyPage(this);
            addPage(propertyPage.createControl());
            setPageText(0, "  Properties  ");
/*
            cachePage = new ConnectorCachePage(this);
            addPage(cachePage.createControl());
            setPageText(1, "  Cache  ");
*/
            load();

        } catch (Exception e) {
            log.debug(e.getMessage(), e);
        }
    }

    public Composite getParent() {
        return getContainer();
    }

    public void dispose() {
        propertyPage.dispose();
        super.dispose();
    }

    public void load() throws Exception {
        propertyPage.load();
    }

    public void doSave(IProgressMonitor iProgressMonitor) {
        try {
            store();

        } catch (Exception e) {
            log.debug(e.getMessage(), e);
        }
    }

    public void doSaveAs() {
    }

    public void store() throws Exception {

        origConnectorConfig.copy(connectorConfig);

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.fireChangeEvent();

        checkDirty();
    }

    public boolean isDirty() {
        return dirty;
    }

    public boolean isSaveAsAllowed() {
        return false;
    }

    public void checkDirty() {
        try {
            dirty = false;

            if (!origConnectorConfig.equals(connectorConfig)) {
                dirty = true;
                return;
            }

        } catch (Exception e) {
            log.debug(e.getMessage(), e);

        } finally {
            firePropertyChange(PROP_DIRTY);
        }
    }
}
