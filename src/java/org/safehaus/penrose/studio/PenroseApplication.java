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

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IPlatformRunnable;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.SWT;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchPage;
import org.safehaus.penrose.config.*;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.util.ApplicationConfig;
import org.safehaus.penrose.studio.util.ChangeListener;
import org.safehaus.penrose.studio.util.FileUtil;
import org.safehaus.penrose.studio.validation.ValidationView;
import org.safehaus.penrose.studio.logger.LoggerManager;
import org.safehaus.penrose.studio.license.LicenseDialog;
import org.safehaus.penrose.studio.welcome.action.EnterLicenseKeyAction;
import org.safehaus.penrose.schema.*;
import org.safehaus.penrose.management.PenroseClient;
import org.safehaus.penrose.partition.*;
import com.identyx.license.License;
import com.identyx.license.LicenseUtil;
import com.identyx.license.LicenseManager;
import com.identyx.license.LicenseReader;
import org.safehaus.penrose.log4j.Log4jConfigReader;
import org.safehaus.penrose.log4j.Log4jConfig;
import org.safehaus.penrose.log4j.Log4jConfigWriter;

import javax.crypto.Cipher;

public class PenroseApplication implements IPlatformRunnable {

    Logger log = Logger.getLogger(getClass());

    public static String PRODUCT_NAME    = "Penrose Studio";
    public static String PRODUCT_VERSION = "1.1.2";
    public static String VENDOR_NAME     = "Identyx Corporation";

    public final static DateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy");
    public final static String RELEASE_DATE    = "09/11/2006";

    public final static String FEATURE_NOT_AVAILABLE = "This feature is only available in the commercial version.";

    public static PenroseApplication instance;

    String workDir;
    File homeDir;

    ApplicationConfig applicationConfig = new ApplicationConfig();
    PenroseConfig penroseConfig = new PenroseConfig();
    PenroseClient client;
    SchemaManager schemaManager;
    PartitionManager partitionManager;
    LoggerManager loggerManager = new LoggerManager();

    PenroseWorkbenchAdvisor workbenchAdvisor;
    ArrayList changeListeners = new ArrayList();

    License license;
    Log4jConfig loggingConfig;

    boolean dirty = false;

    static {
        try {
            Package pkg = PenroseApplication.class.getPackage();

            PRODUCT_NAME    = pkg.getImplementationTitle() == null ? PRODUCT_NAME : pkg.getImplementationTitle();
            PRODUCT_VERSION = pkg.getImplementationVersion() == null ? PRODUCT_VERSION : pkg.getImplementationVersion();
            VENDOR_NAME     = pkg.getImplementationVendor() == null ? VENDOR_NAME : pkg.getImplementationVendor();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public PenroseApplication() throws Exception {

        String userHome = System.getProperties().getProperty("user.home", "/tmp");
        homeDir = new File(userHome, ".penrose");
        homeDir.mkdirs();

        String dir = System.getProperty("user.dir");
        workDir = dir+File.separator+"work";

        workbenchAdvisor = new PenroseWorkbenchAdvisor();

        PenroseApplication.instance = this;
    }

    public static PenroseApplication getInstance() {
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

	public PenroseConfig getPenroseConfig() {
		return penroseConfig;
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

        log.debug("Loading projects from "+file.getAbsolutePath());

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
		for (int i=0; i<changeListeners.size(); i++) {
			ChangeListener listener = (ChangeListener)changeListeners.get(i);
			listener.handleChange(null);
		}
	}

	public ApplicationConfig getApplicationConfig() {
		return applicationConfig;
	}


    public void open(String dir) throws Exception {

        FileUtil.delete(dir);

        log.debug("-------------------------------------------------------------------------------------");
        log.debug("Downloading configuration to "+dir);

        downloadFolder("conf", dir);
        downloadFolder("schema", dir);
        downloadFolder("partitions", dir);

        log.debug("Opening project from "+dir);

        PenroseConfigReader penroseConfigReader = new PenroseConfigReader(dir+"/conf/server.xml");
        penroseConfig = penroseConfigReader.read();

        initSystemProperties();
        initSchemaManager(dir);
        loadPartitions(dir);
        validatePartitions();

        loadLoggingConfig(dir);
        //loadLoggers();

        notifyChangeListeners();

        log.debug("Project opened.");
    }

    public void loadLoggers() throws Exception {
        loggerManager.clear();

        for (Iterator i=client.getLoggerNames().iterator(); i.hasNext(); ) {
            String loggerName = (String)i.next();
            loggerManager.addLogger(loggerName);
        }

    }
    public void initSystemProperties() throws Exception {
        for (Iterator i=penroseConfig.getSystemPropertyNames().iterator(); i.hasNext(); ) {
            String name = (String)i.next();
            String value = penroseConfig.getSystemProperty(name);

            System.setProperty(name, value);
        }
    }

    public void initSchemaManager(String dir) throws Exception {

        schemaManager = new SchemaManager();

        for (Iterator i=penroseConfig.getSchemaConfigs().iterator(); i.hasNext(); ) {
            SchemaConfig schemaConfig = (SchemaConfig)i.next();
            schemaManager.load(dir, schemaConfig);
        }
    }

    public void loadPartitions(String dir) throws Exception {

        partitionManager = new PartitionManager();
        partitionManager.setSchemaManager(schemaManager);

        partitionManager.load(dir, penroseConfig.getPartitionConfigs());
    }

    public void loadLoggingConfig(String dir) throws Exception {
        try {
            Log4jConfigReader reader = new Log4jConfigReader(new File(dir+"/conf/log4j.xml"));
            loggingConfig = reader.read();
        } catch (Exception e) {
            log.error("ERROR: "+e.getMessage());
            loggingConfig = new Log4jConfig();
        }
    }

    public void validatePartitions() throws Exception {

        PartitionValidator partitionValidator = new PartitionValidator();
        partitionValidator.setPenroseConfig(penroseConfig);
        partitionValidator.setSchemaManager(schemaManager);

        Collection results = new ArrayList();

        for (Iterator i=penroseConfig.getPartitionConfigs().iterator(); i.hasNext(); ) {
            PartitionConfig partitionConfig = (PartitionConfig)i.next();

            Partition partition = partitionManager.getPartition(partitionConfig.getName());
            Collection list = partitionValidator.validate(partition);

            for (Iterator j=list.iterator(); j.hasNext(); ) {
                PartitionValidationResult resultPartition = (PartitionValidationResult)j.next();

                if (resultPartition.getType().equals(PartitionValidationResult.ERROR)) {
                    log.error("ERROR: "+resultPartition.getMessage()+" ["+resultPartition.getSource()+"]");
                } else {
                    log.warn("WARNING: "+resultPartition.getMessage()+" ["+resultPartition.getSource()+"]");
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

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public void connect() throws Exception {
        PenroseApplication penroseApplication = PenroseApplication.getInstance();
        Project project = penroseApplication.getApplicationConfig().getCurrentProject();
        connect(project);
    }

    public void connect(Project project) throws Exception {
        client = new PenroseClient(project.getType(), project.getHost(), project.getPort(), project.getUsername(), project.getPassword());
        client.connect();
    }

    public void disconnect() throws Exception {
        //client.close();
    }

    public void save(String dir) throws Exception {

        File file = new File(dir);
        file.mkdirs();

        log.debug("-------------------------------------------------------------------------------------");
        log.debug("Saving configuration to "+dir);

        PenroseConfigWriter serverConfigWriter = new PenroseConfigWriter(dir+"/conf/server.xml");
        serverConfigWriter.write(penroseConfig);

        saveLoggingConfig(dir);

        partitionManager.store(dir, penroseConfig.getPartitionConfigs());

        PenroseApplication penroseApplication = PenroseApplication.getInstance();
        penroseApplication.setDirty(false);

        validatePartitions();
    }

    public void saveLoggingConfig(String dir) throws Exception {
        Log4jConfigWriter writer = new Log4jConfigWriter(dir+"/conf/log4j.xml");
        writer.write(loggingConfig);
    }

    public void upload(String dir) throws Exception {
        log.debug("Uploading configuration from "+dir);

        uploadFolder(dir);

    }

    public void downloadFolder(String remotePath, String localDir) throws Exception {
        File outputDir = new File(localDir);
        outputDir.mkdirs();

        Collection filenames = client.listFiles(remotePath);
        for (Iterator i = filenames.iterator(); i.hasNext(); ) {
            String filename = (String)i.next();
            download(filename, outputDir);
        }
    }

    public void download(String filename, File localDir) throws Exception {
        log.debug("Downloading "+filename);

        byte content[] = client.download(filename);
        if (content == null) return;

        File file = new File(localDir, filename);
        file.getParentFile().mkdirs();

        FileOutputStream out = new FileOutputStream(file);
		out.write(content);

		out.close();
	}

    public Collection listFiles(String directory) throws Exception {
        Collection results = new ArrayList();
        listFiles(null, new File(directory), results);
        return results;
    }

    public void listFiles(String prefix, File directory, Collection results) throws Exception {
        File children[] = directory.listFiles();
        for (int i=0; i<children.length; i++) {
            if (children[i].isDirectory()) {
                listFiles((prefix == null ? "" : prefix+"/")+children[i].getName(), children[i], results);
            } else {
                results.add((prefix == null ? "" : prefix+"/")+children[i].getName());
            }
        }
    }

    public void uploadFolder(String localDir) throws Exception {
        File inputDir = new File(localDir);

        Collection files = listFiles(localDir);
        for (Iterator i=files.iterator(); i.hasNext(); ) {
            String filename = (String)i.next();
            upload(inputDir, filename);
        }
    }

    public void upload(File localDir, String filename) throws Exception {
        log.debug("Uploading "+filename);

        File file = new File(localDir, filename);
        FileInputStream in = new FileInputStream(file);

        byte content[] = new byte[(int)file.length()];
        in.read(content);

        in.close();

		client.upload(filename, content);
	}

    public PartitionManager getPartitionManager() throws Exception {
        return partitionManager;
    }

    public SchemaManager getSchemaManager() {
        return schemaManager;
    }

    public void setSchemaManager(SchemaManager schemaManager) {
        this.schemaManager = schemaManager;
    }

    public String getWorkDir() {
        return workDir;
    }

    public void setWorkDir(String workDir) {
        this.workDir = workDir;
    }

    public PenroseClient getClient() {
        return client;
    }

    public void setClient(PenroseClient client) {
        this.client = client;
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

        PenroseApplication penroseApplication = PenroseApplication.getInstance();
        PublicKey publicKey = penroseApplication.getPublicKey();

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

        penroseApplication.setLicense(license);
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

    public LoggerManager getLoggerManager() {
        return loggerManager;
    }

    public void setLoggerManager(LoggerManager loggerManager) {
        this.loggerManager = loggerManager;
    }

    public Log4jConfig getLoggingConfig() {
        return loggingConfig;
    }

    public void setLoggingConfig(Log4jConfig loggingConfig) {
        this.loggingConfig = loggingConfig;
    }
}