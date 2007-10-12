package org.safehaus.penrose.studio.federation;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Endi Sukma Dewata
 */
public class FederationPlugin extends AbstractUIPlugin {

    public Logger log = LoggerFactory.getLogger(getClass());

    public void start(BundleContext bundleContext) throws Exception {
        log.debug("Starting Federation Plugin");
        super.start(bundleContext);
    }

    public void stop(BundleContext bundleContext) throws Exception {
        log.debug("Stopping Federation Plugin");
        super.stop(bundleContext);
    }
}
