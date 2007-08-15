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

import org.eclipse.core.runtime.IPlatformRunnable;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.SWT;
import org.eclipse.ui.PlatformUI;
import org.safehaus.penrose.studio.util.ApplicationConfig;
import org.safehaus.penrose.studio.util.ChangeListener;
import org.safehaus.penrose.studio.license.LicenseDialog;
import org.safehaus.penrose.studio.welcome.action.EnterLicenseKeyAction;
import org.safehaus.penrose.studio.plugin.*;
import org.safehaus.penrose.studio.nis.NISPlugin;
import com.identyx.license.*;
import org.safehaus.penrose.log4j.Log4jConfigReader;
import org.safehaus.penrose.log4j.Log4jConfig;
import org.safehaus.penrose.log4j.Log4jConfigWriter;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import javax.crypto.Cipher;

public class PenroseStudio implements IPlatformRunnable {

    public Logger log = LoggerFactory.getLogger(getClass());
    public boolean debug = log.isDebugEnabled();

    public static String PRODUCT_NAME;
    public static String PRODUCT_VERSION;
    public static String VENDOR_NAME;

    public final static DateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy");
    public final static String RELEASE_DATE    = "12/01/2007";

    public final static String FEATURE_NOT_AVAILABLE = "This feature is only available in the commercial version.";

    public static PenroseStudio instance;

    File homeDir;

    ApplicationConfig applicationConfig = new ApplicationConfig();
    PluginManager pluginManager = new PluginManager();

    PenroseStudioWorkbenchAdvisor workbenchAdvisor;
    ArrayList<ChangeListener> changeListeners = new ArrayList<ChangeListener>();

    License license;
    Log4jConfig loggingConfig;

    boolean dirty = false;

    static {
        try {
            Package pkg = PenroseStudio.class.getPackage();

            PRODUCT_NAME    = pkg.getImplementationTitle() == null ? PRODUCT_NAME : pkg.getImplementationTitle();
            PRODUCT_VERSION = pkg.getImplementationVersion() == null ? PRODUCT_VERSION : pkg.getImplementationVersion();
            VENDOR_NAME     = pkg.getImplementationVendor() == null ? VENDOR_NAME : pkg.getImplementationVendor();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public PenroseStudio() throws Exception {

        File dir = new File(System.getProperty("user.dir"));
        File log4jXml = new File(dir, "conf"+File.separator+"log4j.xml");

        if (log4jXml.exists()) {
            DOMConfigurator.configure(log4jXml.getAbsolutePath());
        }

        log.warn("Starting "+PRODUCT_NAME+" "+PRODUCT_VERSION+".");

        String javaVersion = System.getProperty("java.version");
        log.info("Java version: "+javaVersion);

        String javaVendor = System.getProperty("java.vendor");
        log.info("Java vendor: "+javaVendor);

        String javaHome = System.getProperty("java.home");
        log.info("Java home: "+javaHome);

        String userDir = System.getProperty("user.dir");
        log.info("Current directory: "+userDir);

        File userHome = new File(System.getProperty("user.home"));

        homeDir = new File(userHome, ".penrose");
        homeDir.mkdirs();

        workbenchAdvisor = new PenroseStudioWorkbenchAdvisor();

        instance = this;

        PluginConfig pluginConfig = new PluginConfig();
        pluginConfig.setName("JDBC");
        pluginConfig.setClassName(JDBCPlugin.class.getName());
        pluginManager.init(pluginConfig);

        pluginConfig = new PluginConfig();
        pluginConfig.setName("LDAP");
        pluginConfig.setClassName(LDAPPlugin.class.getName());
        pluginManager.init(pluginConfig);

        pluginConfig = new PluginConfig();
        pluginConfig.setName("NIS");
        pluginConfig.setClassName(NISPlugin.class.getName());
        pluginManager.init(pluginConfig);
    }

    public static PenroseStudio getInstance() {
        return instance;
    }

    public Object run(Object args) {

        Display display = PlatformUI.createDisplay();

        try {
            int rc = PlatformUI.createAndRunWorkbench(display, workbenchAdvisor);
            if (rc == PlatformUI.RETURN_RESTART) return IPlatformRunnable.EXIT_RESTART;

            return IPlatformRunnable.EXIT_OK;

        } finally {
            display.dispose();
        }
    }

	public void saveApplicationConfig() {
		try {
			File file = new File(homeDir, "config.xml");
			applicationConfig.save(file);
		} catch (Exception ex) {
			log.debug(ex.toString(), ex);
		}
	}
	
	public void loadApplicationConfig() {
		File file = new File(homeDir, "config.xml");

        if (!file.exists()) {
            saveApplicationConfig();
        }

		try {
			applicationConfig.load(file);
		} catch (Exception ex) {
			log.debug(ex.toString(), ex);
		}
	}
	
	public void addChangeListener(ChangeListener changeListener) {
		changeListeners.add(changeListener);
	}

	public void notifyChangeListeners() {
        for (ChangeListener listener : changeListeners) {
            listener.handleChange(null);
        }
    }

	public ApplicationConfig getApplicationConfig() {
		return applicationConfig;
	}

/*
    public void initSchemaManager(String dir) throws Exception {

        schemaManager = penroseContext.getSchemaManager();

        for (Iterator i=penroseConfig.getSchemaConfigs().iterator(); i.hasNext(); ) {
            SchemaConfig schemaConfig = (SchemaConfig)i.next();
            schemaManager.init(dir, schemaConfig);
        }

    }
*/
    public void loadLoggingConfig(String dir) throws Exception {
        loadLoggingConfig(new File(dir));
    }

    public void loadLoggingConfig(File dir) throws Exception {
        try {
            Log4jConfigReader reader = new Log4jConfigReader(new File(dir, "/conf/log4j.xml"));
            loggingConfig = reader.read();
        } catch (Exception e) {
            log.error("ERROR: "+e.getMessage());
            loggingConfig = new Log4jConfig();
        }
    }
/*
    public void validatePartitions() throws Exception {

        Collection<PartitionValidationResult> results = new ArrayList<PartitionValidationResult>();

        PartitionValidator partitionValidator = new PartitionValidator();
        partitionValidator.setPenroseConfig(penroseConfig);
        partitionValidator.setPenroseContext(penroseContext);

        for (PartitionConfig partitionConfig : partitionConfigs.getPartitionConfigs()) {

            Collection<PartitionValidationResult> list = partitionValidator.validate(partitionConfig);

            for (PartitionValidationResult resultPartition : list) {

                if (resultPartition.getType().equals(PartitionValidationResult.ERROR)) {
                    log.error("ERROR: " + resultPartition.getMessage() + " [" + resultPartition.getSource() + "]");
                } else {
                    log.warn("WARNING: " + resultPartition.getMessage() + " [" + resultPartition.getSource() + "]");
                }
            }

            results.addAll(list);
        }

        IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        if (!results.isEmpty()) {
            activePage.showView(ValidationView.class.getName());
        }

        ValidationView view = (ValidationView)activePage.findView(ValidationView.class.getName());
        if (view != null) {
            view.setResults(results);
            view.refresh();
        }
    }
*/
    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public void saveLoggingConfig(File dir) throws Exception {
        Log4jConfigWriter writer = new Log4jConfigWriter(new File(dir, "/conf/log4j.xml"));
        writer.write(loggingConfig);
    }

    public Collection<String> listFiles(File directory, String path) throws Exception {
        Collection<String> results = new ArrayList<String>();
        listFiles(directory, path, results);
        return results;
    }

    public void listFiles(File directory, String path, Collection<String> results) throws Exception {
        File children[] = directory.listFiles();
        for (File file : children) {
            String filename = (path == null ? "" : path + "/") + file.getName();
            if (file.isDirectory()) {
                listFiles(file, filename, results);
            } else {
                results.add(filename);
            }
        }
    }

    public boolean isCommercial() {
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

        PenroseStudio penroseStudio = getInstance();

        String filename = "penrose.license";

        File file = new File(filename);

        LicenseManager licenseManager = LicenseManager.getInstance();
        Licenses licenses = licenseManager.loadLicenses(file);
        licenseManager.addLicenses(licenses);
        License license = licenses.getLicense("Penrose Studio");

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

    public PenroseStudioWorkbenchAdvisor getWorkbenchAdvisor() {
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

    public Log4jConfig getLoggingConfig() {
        return loggingConfig;
    }

    public void setLoggingConfig(Log4jConfig loggingConfig) {
        this.loggingConfig = loggingConfig;
    }

    public PluginManager getPluginManager() {
        return pluginManager;
    }

    public void setPluginManager(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }
}