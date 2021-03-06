/**
 * Copyright 2009 Red Hat, Inc.
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

import org.eclipse.core.runtime.IPlatformRunnable;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
import org.eclipse.jface.resource.ImageDescriptor;
import org.safehaus.penrose.studio.util.ApplicationConfig;
import org.safehaus.penrose.studio.plugin.*;
import org.safehaus.penrose.studio.nis.NISPlugin;
import org.safehaus.penrose.studio.image.ImageManager;
import org.safehaus.penrose.log.log4j.Log4jConfigWriter;
import org.safehaus.penrose.log.log4j.Log4jConfig;
import org.safehaus.penrose.log.log4j.Log4jConfigReader;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

public class PenroseStudio implements IPlatformRunnable {

    public Logger log = LoggerFactory.getLogger(getClass());

    public static String PRODUCT_NAME;
    public static String PRODUCT_VERSION;
    public static String VENDOR_NAME;
    public static String SPECIFICATION_VERSION;

    public static PenroseStudio instance;

    public File homeDir;
    public Properties properties;

    public ApplicationConfig applicationConfig = new ApplicationConfig();
    public PluginManager pluginManager = new PluginManager();
    public ImageManager imageManager;

    public PenroseStudioWorkbenchAdvisor workbenchAdvisor;

    public Log4jConfig loggingConfig;

    boolean dirty = false;

    static {
        try {
            Package pkg = PenroseStudio.class.getPackage();

            String value = pkg.getImplementationTitle();
            if (value != null) PRODUCT_NAME = value;

            value = pkg.getImplementationVersion();
            if (value != null) PRODUCT_VERSION = value;

            value = pkg.getImplementationVendor();
            if (value != null) VENDOR_NAME = value;

            value = pkg.getSpecificationVersion();
            if (value != null) SPECIFICATION_VERSION = value;

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

        properties = new Properties();

        File buildProperties = new File(dir, "build.properties");
        if (buildProperties.exists()) {
            properties.load(new FileInputStream(buildProperties));
        }

        imageManager = new ImageManager("org.safehaus.penrose.studio");
        imageManager.setProperties(properties);
        imageManager.init();

        load();
    }

    public static PenroseStudio getInstance() {
        return instance;
    }

    public ImageManager getImageManager() {
        return imageManager;
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

	public void store() {
		try {
			File file = new File(homeDir, "config.xml");
			applicationConfig.save(file);
		} catch (Exception ex) {
			log.debug(ex.toString(), ex);
		}
	}
	
	public void load() {
		File file = new File(homeDir, "config.xml");

        if (!file.exists()) {
            store();
        }

		try {
			applicationConfig.load(file);
		} catch (Exception ex) {
			log.debug(ex.toString(), ex);
		}
	}
	
	public ApplicationConfig getApplicationConfig() {
		return applicationConfig;
	}

/*
    public void initSchemaManager(String dir) throws Exception {

        schemaManager = penroseContext.getSchema();

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
            Log4jConfigReader reader = new Log4jConfigReader();
            loggingConfig = reader.read(new File(dir, "conf/log4j.xml"));
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

        for (PartitionConfig partitionConfig : partitionConfigManager.getPartitionConfigManager()) {

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
        Log4jConfigWriter writer = new Log4jConfigWriter();
        writer.write(new File(dir, "conf/log4j.xml"), loggingConfig);
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

    public PenroseStudioWorkbenchAdvisor getWorkbenchAdvisor() {
        return workbenchAdvisor;
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

    public static ImageDescriptor getImageDescriptor(String name) {
        ImageManager imageManager = instance.getImageManager();
        return imageManager.getImageDescriptor(name);
    }

    public static Image getImage(String name) {
        ImageManager imageManager = instance.getImageManager();
        return imageManager.getImage(name);
	}
}