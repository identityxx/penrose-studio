package org.safehaus.penrose.studio.handler;

import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.core.runtime.IProgressMonitor;
import org.apache.log4j.Logger;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.handler.HandlerConfig;

/**
 * @author Endi S. Dewata
 */
public class HandlerEditor extends MultiPageEditorPart {

    Logger log = Logger.getLogger(getClass());

	HandlerConfig handlerConfig;
    HandlerConfig origHandlerConfig;

    boolean dirty;

    HandlerPropertyPage propertyPage;

    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        HandlerEditorInput ei = (HandlerEditorInput)input;
        origHandlerConfig = ei.getHandlerConfig();

        try {
            handlerConfig = (HandlerConfig)origHandlerConfig.clone();
        } catch (Exception e) {
            throw new PartInitException(e.getMessage(), e);
        }

        setSite(site);
        setInput(input);
        setPartName("Handler - "+handlerConfig.getName());
    }

    protected void createPages() {
        try {
            propertyPage = new HandlerPropertyPage(this);
            addPage(propertyPage.createControl());
            setPageText(0, "  Properties  ");

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

        origHandlerConfig.copy(handlerConfig);

        setPartName("Handler - "+handlerConfig.getName());

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();

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

            if (!origHandlerConfig.equals(handlerConfig)) {
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
