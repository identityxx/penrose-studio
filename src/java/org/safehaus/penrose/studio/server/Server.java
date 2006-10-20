package org.safehaus.penrose.studio.server;

import org.safehaus.penrose.management.PenroseClient;
import org.safehaus.penrose.config.PenroseConfig;
import org.safehaus.penrose.config.PenroseConfigReader;
import org.safehaus.penrose.config.PenroseConfigWriter;
import org.safehaus.penrose.schema.SchemaManager;
import org.safehaus.penrose.schema.SchemaConfig;
import org.safehaus.penrose.partition.*;
import org.safehaus.penrose.log4j.Log4jConfig;
import org.safehaus.penrose.log4j.Log4jConfigReader;
import org.safehaus.penrose.log4j.Log4jConfigWriter;
import org.safehaus.penrose.studio.util.FileUtil;
import org.safehaus.penrose.studio.validation.ValidationView;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;

/**
 * @author Endi S. Dewata
 */
public class Server {

    Logger log = LoggerFactory.getLogger(getClass());

    private ServerConfig serverConfig;

    private PenroseClient client;
    private PenroseConfig penroseConfig;
    private SchemaManager schemaManager;
    private PartitionManager partitionManager;
    private Log4jConfig log4jConfig;

    public Server(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    public String getName() {
        return serverConfig.getName();
    }

    public ServerConfig getServerConfig() {
        return serverConfig;
    }

    public void setServerConfig(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    public PenroseClient getClient() {
        return client;
    }

    public void setClient(PenroseClient client) {
        this.client = client;
    }

    public PenroseConfig getPenroseConfig() {
        return penroseConfig;
    }

    public void setPenroseConfig(PenroseConfig penroseConfig) {
        this.penroseConfig = penroseConfig;
    }

    public SchemaManager getSchemaManager() {
        return schemaManager;
    }

    public void setSchemaManager(SchemaManager schemaManager) {
        this.schemaManager = schemaManager;
    }

    public PartitionManager getPartitionManager() {
        return partitionManager;
    }

    public void setPartitionManager(PartitionManager partitionManager) {
        this.partitionManager = partitionManager;
    }

    public Log4jConfig getLog4jConfig() {
        return log4jConfig;
    }

    public void setLog4jConfig(Log4jConfig log4jConfig) {
        this.log4jConfig = log4jConfig;
    }

    public boolean isConnected() {
        return client != null;
    }

    public String getWorkDir() {
        return System.getProperty("user.dir")+File.separator+"work"+File.separator+serverConfig.getName();
    }

    public void open() throws Exception {
        if (isConnected()) return;

        log.debug("-------------------------------------------------------------------------------------");
        log.debug("Opening server "+serverConfig.getName());

        client = new PenroseClient(
                serverConfig.getType(),
                serverConfig.getHost(),
                serverConfig.getPort(),
                serverConfig.getUsername(),
                serverConfig.getPassword()
        );

        client.connect();

        String dir = getWorkDir();
        FileUtil.delete(dir);

        log.debug("Downloading configuration to "+dir);

        downloadFolder("conf", dir);
        downloadFolder("schema", dir);
        downloadFolder("partitions", dir);

        log.debug("Opening server from "+dir);

        PenroseConfigReader penroseConfigReader = new PenroseConfigReader(dir +"/conf/server.xml");
        penroseConfig = penroseConfigReader.read();

        initSystemProperties();
        initSchemaManager(dir);
        loadPartitions(dir);
        validate();

        loadLoggingConfig(dir);

        log.debug("Server opened.");
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

    public void validate() throws Exception {
        if (client == null) return;

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

    public void loadLoggingConfig(String dir) throws Exception {
        try {
            Log4jConfigReader reader = new Log4jConfigReader(new File(dir+"/conf/log4j.xml"));
            log4jConfig = reader.read();
        } catch (Exception e) {
            log.error("ERROR: "+e.getMessage(), e);
            log4jConfig = new Log4jConfig();
        }
    }

    public void save() throws Exception {

        File workFolder = new File(getWorkDir());
        File tmpFolder = new File(getWorkDir()+".tmp");
        File backupFolder = new File(getWorkDir()+".bak");

        FileUtil.delete(tmpFolder);
        FileUtil.copyFolder(workFolder, tmpFolder);

        String dir = tmpFolder.getAbsolutePath();

        File file = new File(dir);
        file.mkdirs();

        log.debug("-------------------------------------------------------------------------------------");
        log.debug("Saving configuration to "+dir);

        PenroseConfigWriter serverConfigWriter = new PenroseConfigWriter(dir+"/conf/server.xml");
        serverConfigWriter.write(penroseConfig);

        saveLoggingConfig(dir);

        partitionManager.store(dir, penroseConfig.getPartitionConfigs());

        FileUtil.delete(backupFolder);
        workFolder.renameTo(backupFolder);
        tmpFolder.renameTo(workFolder);

        validate();

        log.debug("Server saved.");
    }

    public void saveLoggingConfig(String dir) throws Exception {
        Log4jConfigWriter writer = new Log4jConfigWriter(dir+"/conf/log4j.xml");
        writer.write(log4jConfig);
    }

    public void upload() throws Exception {

        String dir = getWorkDir();

        log.debug("Uploading configuration from "+dir);

        uploadFolder(dir);
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

    public void restart() throws Exception {
        client.restart();

        log.debug("Server restarted.");
    }

    public void close() throws Exception {

        if (!isConnected()) return;

        client.close();
        client = null;

        log.debug("Server closed.");
    }

}
