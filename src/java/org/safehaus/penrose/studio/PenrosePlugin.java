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
package org.safehaus.penrose.studio;

import org.eclipse.ui.plugin.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.osgi.framework.BundleContext;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.util.*;
import java.io.File;

public class PenrosePlugin extends AbstractUIPlugin {

    Logger log = Logger.getLogger(getClass());

	private static PenrosePlugin plugin;
	private ResourceBundle resourceBundle;
	
	public PenrosePlugin() {

        String dir = System.getProperty("user.dir");
        File log4jProperties = new File(dir+File.separator+"conf"+File.separator+"log4j.properties");
        if (log4jProperties.exists()) {
            PropertyConfigurator.configure(log4jProperties.getAbsolutePath());
        }

		plugin = this;
		try {
			resourceBundle = ResourceBundle.getBundle("org.safehaus.penrose.rcp.PenrosePluginResources");
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
        //log.debug("Starting PenrosePlugin");
        super.start(context);

        try {
            //IWorkbench workbench = PlatformUI.getWorkbench();
            //IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
            //workbench.showPerspective(PenrosePerspective.class.getName(), window);

            //IWorkbenchPage page = window.getActivePage();
            //page.openEditor(new WelcomeEditorInput(), WelcomeEditor.class.getName());

        } catch (Exception e) {
            log.debug(e.getMessage(), e);
        }
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
        //log.debug("Stopping PenrosePlugin");
		super.stop(context);
	}

	/**
	 * Returns the shared instance.
	 */
	public static PenrosePlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = PenrosePlugin.getDefault().getResourceBundle();
		try {
			return (bundle != null) ? bundle.getString(key) : key;
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}

    public static ImageDescriptor getImageDescriptor(String path) {
        return AbstractUIPlugin.imageDescriptorFromPlugin("org.safehaus.penrose", path);
    }

    static HashMap images = new HashMap();

	public static Image getImage(String path) {
		Image image = (Image)images.get(path);
        if (image == null) {
            ImageDescriptor descriptor = PenrosePlugin.getImageDescriptor(path);
            image = descriptor.createImage();
            images.put(path, image);
        }
        return image;
	}
}
