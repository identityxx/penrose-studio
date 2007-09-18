package org.safehaus.penrose.studio.project;

import org.safehaus.penrose.management.PenroseClient;
import org.safehaus.penrose.config.PenroseConfig;
import org.safehaus.penrose.config.PenroseConfigReader;
import org.safehaus.penrose.config.PenroseConfigWriter;
import org.safehaus.penrose.naming.PenroseContext;
import org.safehaus.penrose.partition.PartitionConfigs;
import org.safehaus.penrose.partition.PartitionConfig;
import org.safehaus.penrose.service.ServiceConfigs;
import org.safehaus.penrose.studio.logger.LoggerManager;
import org.safehaus.penrose.studio.util.FileUtil;
import org.safehaus.penrose.log4j.Log4jConfig;
import org.safehaus.penrose.log4j.Log4jConfigReader;
import org.safehaus.penrose.schema.SchemaManager;
import org.safehaus.penrose.directory.DirectoryConfig;
import org.safehaus.penrose.mapping.MappingWriter;
import org.safehaus.penrose.source.SourceWriter;
import org.safehaus.penrose.source.SourceConfigs;
import org.safehaus.penrose.connection.ConnectionWriter;
import org.safehaus.penrose.connection.ConnectionConfigs;
import org.safehaus.penrose.module.ModuleWriter;
import org.safehaus.penrose.module.ModuleConfigs;
import org.apache.log4j.Logger;

import java.io.File;

/**
 * @author Endi Sukma Dewata
 */
public class Project {

    public Logger log = Logger.getLogger(getClass());
    public boolean debug = log.isDebugEnabled();

    protected ProjectConfig projectConfig;

    protected PenroseClient client;
    protected File workDir;

    protected PenroseConfig penroseConfig;
    protected PenroseContext penroseContext;

    protected PartitionConfigs partitionConfigs;
    protected ServiceConfigs serviceConfigs;
    protected LoggerManager loggerManager = new LoggerManager();

    protected Log4jConfig loggingConfig;

    boolean connected;

    public Project(ProjectConfig projectConfig) {
        this.projectConfig = projectConfig;
    }

    public String getName() {
        return projectConfig.getName();
    }

    public void connect() throws Exception {

        log.debug("-------------------------------------------------------------------------------------");
        log.debug("Opening "+ projectConfig.getName()+".");

        client = new PenroseClient(projectConfig.getType(), projectConfig.getHost(), projectConfig.getPort(), projectConfig.getUsername(), projectConfig.getPassword());
        client.connect();

        File userHome = new File(System.getProperty("user.home"));
        File homeDir = new File(userHome, ".penrose");
        homeDir.mkdirs();

        workDir = new File(homeDir, "Servers"+File.separator+projectConfig.getName());
        FileUtil.delete(workDir);

        log.debug("----------------------------------------------------------------------------------------------");
        client.download(workDir, "conf");

        log.debug("----------------------------------------------------------------------------------------------");
        client.download(workDir, "schema");

        PenroseConfigReader penroseConfigReader = new PenroseConfigReader(new File(workDir, "conf"+File.separator+"server.xml"));
        penroseConfig = penroseConfigReader.read();

        penroseContext = new PenroseContext(workDir);
        penroseContext.init(penroseConfig);

        File partitionsDir = new File(workDir, "partitions");
        partitionConfigs = new PartitionConfigs(partitionsDir);

        log.debug("----------------------------------------------------------------------------------------------");
        client.download(workDir, "partitions");

        for (String partitionName : partitionConfigs.getAvailablePartitionNames()) {

            if (debug) log.debug("----------------------------------------------------------------------------------");

            PartitionConfig partitionConfig = partitionConfigs.load(partitionName);
            partitionConfigs.addPartitionConfig(partitionConfig);
        }

        File servicesDir = new File(workDir, "services");
        serviceConfigs = new ServiceConfigs(servicesDir);
/*
        File servicesDir = new File(workDir, "services");
        if (!servicesDir.isDirectory()) return;

        for (File dir : servicesDir.listFiles()) {
            if (!dir.isDirectory()) continue;

            if (debug) log.debug("----------------------------------------------------------------------------------");

            serviceConfigs.load(dir);
        }
*/
        //initSystemProperties();
        //initSchemaManager(base);
        //loadPartitions(base);
        //validatePartitions();

        Log4jConfigReader reader = new Log4jConfigReader(new File(workDir, "conf/log4j.xml"));
        loggingConfig = reader.read();
        //loadLoggers();

        connected = true;
    }

    public void save() throws Exception {

        log.debug("-------------------------------------------------------------------------------------");
        log.debug("Saving "+ projectConfig.getName()+".");

        File file = new File(workDir, "conf/server.xml");

        PenroseConfigWriter serverConfigWriter = new PenroseConfigWriter();
        serverConfigWriter.write(file, penroseConfig);

        for (PartitionConfig partitionConfig : partitionConfigs.getPartitionConfigs()) {
            File partitionDir = new File(workDir, "partitions/"+partitionConfig.getName());
            partitionConfigs.store(partitionDir, partitionConfig);
        }
    }

    public void save(PartitionConfig partitionConfig) throws Exception {
        File partitionDir = new File(workDir, "partitions/"+partitionConfig.getName());
        partitionConfigs.store(partitionDir, partitionConfig);

        upload("partitions/"+partitionConfig.getName());
    }

    public void save(PartitionConfig partitionConfig, ConnectionConfigs connectionConfigs) throws Exception {
        ConnectionWriter connectionWriter = new ConnectionWriter();

        File file = new File(workDir, "partitions/"+partitionConfig.getName()+"/DIR-INF/connections.xml");
        connectionWriter.write(file, connectionConfigs);

        upload("partitions/"+partitionConfig.getName()+"/DIR-INF/connections.xml");
    }

    public void save(PartitionConfig partitionConfig, SourceConfigs sourceConfigs) throws Exception {
        SourceWriter sourceWriter = new SourceWriter();

        File file = new File(workDir, "partitions/"+partitionConfig.getName()+"/DIR-INF/sources.xml");
        sourceWriter.write(file, sourceConfigs);

        upload("partitions/"+partitionConfig.getName()+"/DIR-INF/sources.xml");
    }

    public void save(PartitionConfig partitionConfig, DirectoryConfig directoryConfig) throws Exception {
        MappingWriter mappingWriter = new MappingWriter();

        File file = new File(workDir, "partitions/"+partitionConfig.getName()+"/DIR-INF/mapping.xml");
        mappingWriter.write(file, directoryConfig);

        upload("partitions/"+partitionConfig.getName()+"/DIR-INF/mapping.xml");
    }

    public void save(PartitionConfig partitionConfig, ModuleConfigs moduleConfigs) throws Exception {
        ModuleWriter moduleWriter = new ModuleWriter();

        File file = new File(workDir, "partitions/"+partitionConfig.getName()+"/DIR-INF/modules.xml");
        moduleWriter.write(file, moduleConfigs);

        upload("partitions/"+partitionConfig.getName()+"/DIR-INF/modules.xml");
    }

    public void close() throws Exception {

        log.debug("-------------------------------------------------------------------------------------");
        log.debug("Closing "+ projectConfig.getName()+".");

        client.close();
        connected = false;
    }

    public boolean isConnected() {
        return connected;
    }
    
    public void initSystemProperties() throws Exception {
        for (String name : penroseConfig.getSystemPropertyNames()) {
            String value = penroseConfig.getSystemProperty(name);

            System.setProperty(name, value);
        }
    }

    public ProjectConfig getProjectConfig() {
        return projectConfig;
    }

    public void setProjectConfig(ProjectConfig projectConfig) {
        this.projectConfig = projectConfig;
    }

    public PenroseClient getClient() {
        return client;
    }

    public void setClient(PenroseClient client) {
        this.client = client;
    }

    public File getWorkDir() {
        return workDir;
    }

    public void setWorkDir(File workDir) {
        this.workDir = workDir;
    }

    public PenroseConfig getPenroseConfig() {
        return penroseConfig;
    }

    public void setPenroseConfig(PenroseConfig penroseConfig) {
        this.penroseConfig = penroseConfig;
    }

    public PenroseContext getPenroseContext() {
        return penroseContext;
    }

    public void setPenroseContext(PenroseContext penroseContext) {
        this.penroseContext = penroseContext;
    }

    public PartitionConfigs getPartitionConfigs() {
        return partitionConfigs;
    }

    public void setPartitionConfigs(PartitionConfigs partitionConfigs) {
        this.partitionConfigs = partitionConfigs;
    }

    public ServiceConfigs getServiceConfigs() {
        return serviceConfigs;
    }

    public void setServiceConfigs(ServiceConfigs serviceConfigs) {
        this.serviceConfigs = serviceConfigs;
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

    public SchemaManager getSchemaManager() {
        return penroseContext == null ? null : penroseContext.getSchemaManager();
    }

    public void upload() throws Exception {
        client.upload(workDir);
    }

    public void upload(String path) throws Exception {
        log.debug("----------------------------------------------------------------------------------------------");
        client.upload(workDir, path);
    }

    public void download(String path) throws Exception {
        log.debug("----------------------------------------------------------------------------------------------");
        client.download(workDir, path);
    }

    public void removeDirectory(String path) throws Exception {
        client.removeDirectory(path);
    }

    public int hashCode() {
        return projectConfig == null ? 0 : projectConfig.hashCode();
    }

    boolean equals(Object o1, Object o2) {
        if (o1 == null && o2 == null) return true;
        if (o1 != null) return o1.equals(o2);
        return o2.equals(o1);
    }

    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null) return false;
        if (object.getClass() != this.getClass()) return false;

        Project ei = (Project)object;
        if (!equals(projectConfig, ei.projectConfig)) return false;

        return true;
    }
}
