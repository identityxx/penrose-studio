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

import java.io.*;
import java.util.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.security.PublicKey;
import java.rmi.RMISecurityManager;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IPlatformRunnable;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.SWT;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchPage;
import org.safehaus.penrose.studio.config.PenroseStudioConfig;
import org.safehaus.penrose.studio.config.PenroseStudioConfigReader;
import org.safehaus.penrose.studio.config.PenroseStudioConfigWriter;
import org.safehaus.penrose.studio.event.ChangeListener;
import org.safehaus.penrose.studio.event.ChangeEvent;
import org.safehaus.penrose.studio.event.SelectionEvent;
import org.safehaus.penrose.studio.event.SelectionListener;
import org.safehaus.penrose.studio.license.LicenseDialog;
import org.safehaus.penrose.studio.welcome.action.EnterLicenseKeyAction;
import org.safehaus.penrose.studio.action.PenroseStudioActions;
import org.safehaus.penrose.studio.server.ServerConfig;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.util.PenroseStudioClipboard;
import org.safehaus.penrose.studio.object.ObjectsView;
import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.adapter.PenroseStudioJDBCAdapter;
import org.safehaus.penrose.studio.adapter.PenroseStudioLDAPAdapter;
import org.safehaus.penrose.studio.adapter.PenroseStudioAdapter;
import com.identyx.license.License;
import com.identyx.license.LicenseUtil;
import com.identyx.license.LicenseManager;
import com.identyx.license.LicenseReader;

import javax.crypto.Cipher;

public class PenroseStudio implements IPlatformRunnable {

    Logger log = Logger.getLogger(getClass());

    public static String PRODUCT_NAME    = "Penrose Studio";
    public static String PRODUCT_VERSION = "1.2";
    public static String VENDOR_NAME     = "Identyx Corporation";

    public final static DateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy");
    public final static String RELEASE_DATE    = "09/11/2006";

    public final static String FEATURE_NOT_AVAILABLE = "This feature is only available in the commercial version.";

    public static PenroseStudio instance;

    File homeDir;

    PenroseStudioClipboard clipboard;
    PenroseStudioConfig penroseStudioConfig;
    PenroseStudioActions actions;

    PenroseWorkbenchAdvisor workbenchAdvisor;

    Map servers = new TreeMap();
    Map adapters = new TreeMap();

    Collection selectionListeners = new ArrayList();
    Collection changeListeners = new ArrayList();

    License license;

    static {
        try {
            Package pkg = PenroseStudio.class.getPackage();

            PRODUCT_NAME    = pkg.getImplementationTitle() == null ? PRODUCT_NAME : pkg.getImplementationTitle();
            PRODUCT_VERSION = pkg.getImplementationVersion() == null ? PRODUCT_VERSION : pkg.getImplementationVersion();
            VENDOR_NAME     = pkg.getImplementationVendor() == null ? VENDOR_NAME : pkg.getImplementationVendor();

            System.setProperty("java.rmi.server.codebase", "file:lib/penrose-shared-1.2.jar");
            System.setProperty("java.security.policy", "conf/penrose.policy");
            System.setSecurityManager(new RMISecurityManager());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public PenroseStudio() throws Exception {

        String userHome = System.getProperties().getProperty("user.home", "/tmp");
        homeDir = new File(userHome, ".penrose");
        homeDir.mkdirs();

        init();

        workbenchAdvisor = new PenroseWorkbenchAdvisor();

        PenroseStudio.instance = this;
    }

    public void init() throws Exception {

        actions = new PenroseStudioActions(this);

        File file = new File(homeDir, "config.xml");
        log.debug("Loading projects from "+file.getAbsolutePath());

        if (file.exists()) {
            PenroseStudioConfigReader reader = new PenroseStudioConfigReader(file);
            penroseStudioConfig = reader.read();

        } else {
            penroseStudioConfig = new PenroseStudioConfig();
        }

        Collection serverConfigs = penroseStudioConfig.getServerConfigs();

        for (Iterator i=serverConfigs.iterator(); i.hasNext(); ) {
            ServerConfig serverConfig = (ServerConfig)i.next();
            Server server = new Server(serverConfig);
            servers.put(serverConfig.getName(), server);
        }

        addAdapter(new PenroseStudioJDBCAdapter("JDBC"));
        addAdapter(new PenroseStudioLDAPAdapter("LDAP"));
    }

    public void addAdapter(PenroseStudioAdapter adapter) {
        adapters.put(adapter.getName(), adapter);
    }

    public PenroseStudioAdapter getAdapter(String name) {
        return (PenroseStudioAdapter)adapters.get(name);
    }

    public static PenroseStudio getInstance() {
        return instance;
    }

    public Object run(Object args) {

        Display display = PlatformUI.createDisplay();
        try {
            clipboard = new PenroseStudioClipboard(display);

            int rc = PlatformUI.createAndRunWorkbench(display, workbenchAdvisor);
            if (rc == PlatformUI.RETURN_RESTART) return IPlatformRunnable.EXIT_RESTART;

            return IPlatformRunnable.EXIT_OK;

        } finally {
            display.dispose();
        }
    }

    public void open(Server server) throws Exception {
        server.open();
        fireChangeEvent();
    }

    public void close(Server server) throws Exception {
        server.close();
        fireChangeEvent();
    }

    public void save() throws Exception {
        File file = new File(homeDir, "config.xml");
        log.debug("Saving projects into "+file.getAbsolutePath());

        if (penroseStudioConfig == null) return;

        PenroseStudioConfigWriter writer = new PenroseStudioConfigWriter(file);
        writer.write(penroseStudioConfig);

        fireChangeEvent();
	}

    public void addServer(ServerConfig serverConfig) {
        penroseStudioConfig.addServerConfig(serverConfig);
        servers.put(serverConfig.getName(), new Server(serverConfig));
        fireChangeEvent();
    }

    public void removeServer(String name) {
        penroseStudioConfig.removeServerConfig(name);
        servers.remove(name);
        fireChangeEvent();
    }

    public Server getServer(String name) {
        return (Server)servers.get(name);
    }

    public Collection getServers() {
        return servers.values();
    }

    public void addSelectionListener(SelectionListener listener) {
        selectionListeners.add(listener);
    }

    public void removeSelectionListener(SelectionListener listener) {
        selectionListeners.remove(listener);
    }

    public void fireSelectionEvent(SelectionEvent event) {
        for (Iterator i=selectionListeners.iterator(); i.hasNext(); ) {
            SelectionListener listener = (SelectionListener)i.next();
            listener.objectSelected(event);
        }
    }

	public void addChangeListener(ChangeListener listener) {
		changeListeners.add(listener);
	}

    public void removeChangeListener(ChangeListener listener) {
        changeListeners.remove(listener);
    }

	public void fireChangeEvent() {
        ChangeEvent event = new ChangeEvent(new Date(), null);

        for (Iterator i=changeListeners.iterator(); i.hasNext(); ) {
			ChangeListener listener = (ChangeListener)i.next();
			listener.objectChanged(event);
		}
	}

	public PenroseStudioConfig getPenroseStudioConfig() {
		return penroseStudioConfig;
	}

    public boolean checkCommercial() {
        if (license != null) return true;

        Shell shell = new Shell(SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);

        LicenseDialog licenseDialog = new LicenseDialog(shell);
        licenseDialog.setText(FEATURE_NOT_AVAILABLE);
        licenseDialog.open();

        if (licenseDialog.getAction() == LicenseDialog.CANCEL) return false;

        EnterLicenseKeyAction a = new EnterLicenseKeyAction();
        a.run();

        return license != null;
/*
        MessageDialog.openError(
                shell,
                "Feature Not Available",
                FEATURE_NOT_AVAILABLE
        );
*/
    }

    public boolean isFreeware() {
        if (license == null) return true;

        String type = license.getParameter("type");
        if (type != null && "FREEWARE".equals(type)) return true;

        return false;
    }

    public void loadLicense() throws Exception {

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        PublicKey publicKey = penroseStudio.getPublicKey();

        String filename = "penrose.license";

        File file = new File(filename);

        LicenseManager licenseManager = new LicenseManager(publicKey);
        LicenseReader licenseReader = new LicenseReader(licenseManager);
        licenseReader.read(file);

        License license = licenseManager.getLicense("Penrose Studio");

        boolean valid = licenseManager.isValid(license);
        if (!valid) throw new Exception("Invalid license.");

        String type = license.getParameter("type");
        if (type != null && "FREEWARE".equals(type)) throw new Exception("Invalid license.");

        penroseStudio.setLicense(license);
    }

    public License getLicense() {
        return license;
    }

    public void setLicense(License license) throws Exception {

        Date today = new Date();
        Date createDate = license.getCreateDate();
        Date expiryDate = license.getExpiryDate();

        if (expiryDate != null) {
            if (!today.before(expiryDate)) {
                throw new Exception("Expired license: "+DATE_FORMAT.format(expiryDate));
            }

        } else if (createDate != null) {
            Calendar firstYear = Calendar.getInstance();
            firstYear.setTime(createDate);
            firstYear.add(Calendar.YEAR, 1);

            Calendar releaseDate = Calendar.getInstance();
            releaseDate.setTime(DATE_FORMAT.parse(RELEASE_DATE));

            if (!releaseDate.before(firstYear)) {
                throw new Exception("Invalid license.");
            }
        }

        this.license = license;
    }

    public PenroseWorkbenchAdvisor getWorkbenchAdvisor() {
        return workbenchAdvisor;
    }

    public PublicKey getPublicKey() throws Exception {
        return (PublicKey)LicenseUtil.unwrap(getWrappedPublicKey(), "penrose", Cipher.PUBLIC_KEY);
    }

    byte[] getWrappedPublicKey() {
        return new byte[] {
              24,  81, -41, -32, -22, 110,-107, 123, 112,  93,  90, -16, -42,  60,-110,  57,
              98,  96, -18,-100, -48,  48, -33,  73, -66,  58, 122,  -4, -55, -91, -79,  44,
             -41, -24,  28, -52, 126, -55, -94,  90,  35, -20, -71,  -4, -19,-106, -94,-101,
            -112, 121, 126, 119,  87,  89, -81, -94, -24, -38, -17,-109,  -6,   1,  21,  61,
              21,  37,  58, 117,  91, -11,  93,   0,  16, -17,  53, -10,  56, -27, -32,  82,
             121, -61,  37,  21,-103,  34,  -6,  43,-105, -47,  86,  29,-116, 105, -93,-112,
              25,  46,-128,-114,  82, -80, -11, -61,  95,-125, 119, -86,  76,  69, -56,  56,
              69,  22, -83,  27,-125,  74, -71, -30,  19, 109,  67, -62,  73, 113,  -8,  45,
            -105, 121,   4, -72,-107, -36,  -1,  26,  70, 105, -51,  32,-111,  60,  -2,  33,
             -37, -80,  64,  33,   6,  76, -45,  69, 100,  85, -49,   9, -52, -35,  21,  23,
             -91, -29,  12, -55,  -3,  76,   9,-104,  17,  82,  29,  25, -71, -83, -19, -56
        };
    }

    public PenroseStudioActions getActions() {
        return actions;
    }

    public void setActions(PenroseStudioActions actions) {
        this.actions = actions;
    }

    public PenroseStudioClipboard getClipboard() {
        return clipboard;
    }

    public void setClipboard(PenroseStudioClipboard clipboard) {
        this.clipboard = clipboard;
    }

    public Node getSelectedNode() {

        try {
            IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            IWorkbenchPage page = window.getActivePage();
            ObjectsView objectsView = (ObjectsView)page.showView(ObjectsView.class.getName());

            return objectsView.getSelectedNode();

        } catch (Exception e) {
            return null;
        }
    }

    public Collection getSelectedNodes() {

        try {
            IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            IWorkbenchPage page = window.getActivePage();
            ObjectsView objectsView = (ObjectsView)page.showView(ObjectsView.class.getName());

            return objectsView.getSelectedNodes();
            
        } catch (Exception e) {
            return new ArrayList();
        }
    }

    public void show(Node node) throws Exception {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        ObjectsView objectsView = (ObjectsView)page.showView(ObjectsView.class.getName());
        objectsView.show(node);
    }
}