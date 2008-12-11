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
import org.osgi.framework.BundleContext;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class PenroseStudioPlugin extends AbstractUIPlugin {

    public Logger log = LoggerFactory.getLogger(getClass());

	public static PenroseStudioPlugin instance;

    private BundleContext bundleContext;
	
	public PenroseStudioPlugin() {
		instance = this;
	}

	public void start(BundleContext bundleContext) throws Exception {
        this.bundleContext = bundleContext;

        log.debug("Starting "+PenroseStudio.PRODUCT_NAME+".");
        super.start(bundleContext);
	}

	public void stop(BundleContext bundleContext) throws Exception {
        log.debug("Stopping "+PenroseStudio.PRODUCT_NAME+".");
		super.stop(bundleContext);
	}

	public static PenroseStudioPlugin getInstance() {
		return instance;
	}

    public BundleContext getBundleContext(){
        return bundleContext;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }
}
