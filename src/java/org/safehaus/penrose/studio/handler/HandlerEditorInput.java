package org.safehaus.penrose.studio.handler;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.jface.resource.ImageDescriptor;
import org.safehaus.penrose.handler.HandlerConfig;

/**
 * @author Endi S. Dewata
 */
public class HandlerEditorInput implements IEditorInput {

    private HandlerConfig handlerConfig;

    public HandlerEditorInput(HandlerConfig handlerConfig) {
        this.handlerConfig = handlerConfig;
    }

    public boolean exists() {
        return true;
    }

    public ImageDescriptor getImageDescriptor() {
        return null;
    }

    public String getName() {
        return handlerConfig.getName();
    }

    public IPersistableElement getPersistable() {
        return null;
    }

    public String getToolTipText() {
        return handlerConfig.getName();
    }

    public Object getAdapter(Class aClass) {
        return null;
    }

    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null) return false;
        if (object.getClass() != this.getClass()) return false;

        HandlerEditorInput cei = (HandlerEditorInput)object;
        return handlerConfig.equals(cei.handlerConfig);
    }

    public HandlerConfig getHandlerConfig() {
        return handlerConfig;
    }

    public void setHandlerConfig(HandlerConfig handlerConfig) {
        this.handlerConfig = handlerConfig;
    }
}
