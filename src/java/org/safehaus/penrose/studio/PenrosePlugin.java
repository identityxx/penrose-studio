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

import java.util.*;

public class PenrosePlugin extends AbstractUIPlugin {

    Logger log = Logger.getLogger(getClass());

	public static PenrosePlugin instance;
    public static HashMap<String,Image> images = new HashMap<String,Image>();

	private ResourceBundle resourceBundle;
	
	private BundleContext bundleContext;
	
	public PenrosePlugin() {

		instance = this;

        try {
			resourceBundle = ResourceBundle.getBundle("org.safehaus.penrose.rcp.PenrosePluginResources");
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
	}

    public BundleContext getBundleContext(){
        return bundleContext;
    }
	
	public void start(BundleContext bundleContext) throws Exception {
        //log.debug("Starting PenrosePlugin");
        super.start(bundleContext);
        this.bundleContext = bundleContext;
        
        try {
            //IWorkbench workbench = PlatformUI.getWorkbench();
            //IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
            //workbench.showPerspective(PenroseStudioPerspective.class.getName(), window);

            //IWorkbenchPage page = window.getActivePage();
            //page.openEditor(new WelcomeEditorInput(), WelcomeEditor.class.getName());

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
	}

	public void stop(BundleContext bundleContext) throws Exception {
        //log.debug("Stopping PenrosePlugin");
		super.stop(bundleContext);
	}

	public static PenrosePlugin getInstance() {
		return instance;
	}

	public static String getResourceString(String key) {
		ResourceBundle bundle = PenrosePlugin.getInstance().getResourceBundle();
		try {
			return (bundle != null) ? bundle.getString(key) : key;
		} catch (MissingResourceException e) {
			return key;
		}
	}

	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}

    public static ImageDescriptor getImageDescriptor(String path) {
        return AbstractUIPlugin.imageDescriptorFromPlugin("org.safehaus.penrose", path);
    }

	public static Image getImage(String path) {
		Image image = images.get(path);
        if (image == null) {
            ImageDescriptor descriptor = PenrosePlugin.getImageDescriptor(path);
            image = descriptor.createImage();
            images.put(path, image);
        }
        return image;
	}
}
