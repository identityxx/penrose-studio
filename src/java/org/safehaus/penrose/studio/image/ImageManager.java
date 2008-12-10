package org.safehaus.penrose.studio.image;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.swt.graphics.Image;

import java.util.Properties;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Endi Sukma Dewata
 */
public class ImageManager {

    public String pluginName;
    public Properties properties;

    public Map<String,Image> images = new HashMap<String,Image>();

    public ImageManager() {
        pluginName = "org.safehaus.penrose.studio";
    }

    public void init() throws Exception {
    }

    public void destroy() {
        for (Image image : images.values()) {
            image.dispose();
        }
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public ImageDescriptor getImageDescriptor(String name) {
        String path = properties.getProperty(name);
        if (path == null) return ImageDescriptor.getMissingImageDescriptor();
        
        return AbstractUIPlugin.imageDescriptorFromPlugin(pluginName, path);
    }

    public Image createImage(String path) {
        ImageDescriptor descriptor = getImageDescriptor(path);
        return descriptor.createImage();
    }

    public Image getImage(String path) {
        Image image = images.get(path);

        if (image == null) {
            image = createImage(path);
            images.put(path, image);
        }

        return image;
    }
}
