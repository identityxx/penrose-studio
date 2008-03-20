package org.safehaus.penrose.studio.federation.nis;

import org.safehaus.penrose.partition.*;
import org.safehaus.penrose.source.*;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.federation.Federation;
import org.safehaus.penrose.federation.repository.Repository;
import org.safehaus.penrose.federation.repository.GlobalRepository;
import org.safehaus.penrose.federation.repository.LDAPRepository;
import org.safehaus.penrose.federation.repository.NISDomain;
import org.safehaus.penrose.studio.federation.ldap.LDAPFederation;
import org.safehaus.penrose.connection.ConnectionConfig;
import org.safehaus.penrose.management.PenroseClient;
import org.safehaus.penrose.management.PartitionClient;
import org.safehaus.penrose.management.ConnectionClient;
import org.apache.tools.ant.filters.ExpandProperties;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.FilterChain;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;

import java.util.*;
import java.io.File;

/**
 * @author Endi Sukma Dewata
 */
public class NISFederation {

    public Logger log = Logger.getLogger(getClass());

    public final static String YP                    = "yp";
    public final static String NIS                   = "nis";
    public final static String NSS                   = "nss";
    public final static String DB                    = "db";

    public final static String NIS_TOOL              = "nis_tool";
    public final static String YP_TEMPLATE           = "federation_yp";
    public final static String NIS_TEMPLATE          = "federation_nis";
    public final static String NSS_TEMPLATE          = "federation_nss";
    public final static String DB_TEMPLATE           = "federation_db";

    public final static String CACHE_USERS           = "cache_users";
    public final static String CACHE_GROUPS          = "cache_groups";
    public final static String CACHE_CONNECTION_NAME = "Cache";

    public final static String CHANGE_USERS          = "change_users";
    public final static String CHANGE_GROUPS         = "change_groups";

    public final static String LDAP_CONNECTION_NAME  = "LDAP";

    Project project;
    Partition partition;
    Federation federation;
/*
    protected Source actions;
    protected Source hosts;
    protected Source files;
    protected Source changes;
    protected Source users;
    protected Source groups;
*/
    public NISFederation(Federation federation) throws Exception {
        this.federation = federation;
        //this.partition = federation.getPartition();
        this.project = federation.getProject();
    }

    public void load(IProgressMonitor monitor) throws Exception {
        log.debug("Starting NIS Federation tool.");

        Collection<Repository> list = federation.getRepositories("NIS");

        monitor.beginTask("Loading NIS repositories...", list.size() == 1 ? IProgressMonitor.UNKNOWN : list.size());
/*
        actions   = partition.getSource("penrose_actions");
        hosts     = partition.getSource("penrose_hosts");
        files     = partition.getSource("penrose_files");
        changes   = partition.getSource("penrose_changes");
        users     = partition.getSource("penrose_users");
        groups    = partition.getSource("penrose_groups");
*/

        for (Repository rep : list) {

            if (monitor.isCanceled()) throw new InterruptedException();

            NISDomain domain = (NISDomain)rep;
            String name = domain.getName();

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // Creating NIS Partition
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////

            PartitionConfig nisPartitionConfig = project.getPartitionConfigs().getPartitionConfig(name+"_"+NISFederation.NIS);

            if (nisPartitionConfig == null) { // create missing partition config during start

                monitor.subTask("Creating "+name+"_"+NISFederation.NIS+"...");

                createNisPartition(domain);
            }

            monitor.subTask("Loading "+name+"_"+NISFederation.NIS+"...");

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // Creating YP Partition
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////

            PartitionConfig ypPartitionConfig = project.getPartitionConfigs().getPartitionConfig(name+"_"+NISFederation.YP);
            boolean createPartitionConfig = ypPartitionConfig == null;

            if (createPartitionConfig) { // create missing partition configs during start

                monitor.subTask("Creating "+name+"_"+NISFederation.YP+"...");

                createYpPartition(domain);
            }

            monitor.subTask("Loading "+name+"_"+NISFederation.YP+"...");

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // Creating DB Partition
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
/*
            PartitionConfig dbPartitionConfig = project.getPartitionConfigs().getPartitionConfig(name+"_"+NISFederation.DB);

            if (dbPartitionConfig == null) { // create missing partition config during start
                dbPartitionConfig = createDbPartition(domain);
                project.upload("partitions/"+dbPartitionConfig.getName());
                penroseClient.startPartition(dbPartitionConfig.getName());
            }

            monitor.subTask("Loading "+dbPartitionConfig.getName()+"...");

            loadPartition(dbPartitionConfig);
*/
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // Creating NSS Partition
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////

            PartitionConfig nssPartitionConfig = project.getPartitionConfigs().getPartitionConfig(name+"_"+NISFederation.NSS);

            if (nssPartitionConfig == null) { // create missing partition config during start

                monitor.subTask("Creating "+name+"_"+NISFederation.NSS+"...");

                createNssPartition(domain);
            }

            monitor.subTask("Loading "+name+"_"+NISFederation.NSS+"...");

            monitor.worked(1);
        }
    }

    public void createPartitions(NISDomain domain) throws Exception {
        createNisPartition(domain);
        createYpPartition(domain);
        createNssPartition(domain);
    }

    public void removePartitions(NISDomain domain) throws Exception {
        removeNisPartition(domain);
        removeYpPartition(domain);
        removeNssPartition(domain);
    }

    public void createNisPartition(NISDomain domain) throws Exception {

        if (!domain.isNisEnabled()) return;

        String name = domain.getName();
        String partitionName = name+"_"+NISFederation.NIS;

        log.debug("Creating partition "+partitionName+".");

        File workDir = project.getWorkDir();

        File sampleDir = new File(workDir, "samples/"+ NIS_TEMPLATE);
        if (!sampleDir.exists()) project.download("samples/"+ NIS_TEMPLATE);

        File partitionDir = new File(workDir, "partitions"+File.separator+ partitionName);

        GlobalRepository globalRepository = federation.getGlobalRepository();

        org.apache.tools.ant.Project antProject = new org.apache.tools.ant.Project();

        antProject.setProperty("DOMAIN",        name);
        antProject.setProperty("NIS_DOMAIN",    domain.getFullName());

        antProject.setProperty("LDAP_URL",      globalRepository.getUrl());
        antProject.setProperty("LDAP_USER",     globalRepository.getUser());
        antProject.setProperty("LDAP_PASSWORD", globalRepository.getPassword());
        antProject.setProperty("LDAP_SUFFIX",   domain.getNisSuffix());

        Copy copy = new Copy();
        copy.setOverwrite(true);
        copy.setProject(antProject);

        FileSet fs = new FileSet();
        fs.setDir(sampleDir);
        fs.setIncludes("**/*");
        copy.addFileset(fs);

        copy.setTodir(partitionDir);

        FilterChain filterChain = copy.createFilterChain();
        ExpandProperties expandProperties = new ExpandProperties();
        expandProperties.setProject(antProject);
        filterChain.addExpandProperties(expandProperties);

        copy.execute();

        PartitionConfigs partitionConfigs = project.getPartitionConfigs();
        PartitionConfig partitionConfig = partitionConfigs.load(partitionDir);
        partitionConfigs.addPartitionConfig(partitionConfig);

        PenroseClient penroseClient = project.getClient();

        project.upload("partitions/"+partitionConfig.getName());
        penroseClient.startPartition(partitionConfig.getName());
    }

    public void removeNisPartition(NISDomain domain) throws Exception {

        PartitionConfig nisPartitionConfig = getPartitionConfig(domain.getName()+"_"+NISFederation.NIS);
        if (nisPartitionConfig == null) return;

        removePartition(domain.getName()+"_"+NISFederation.NIS);
    }

    public void createYpPartition(NISDomain domain) throws Exception {

        if (!domain.isYpEnabled()) return;

        String name = domain.getName();
        String partitionName = name+"_"+NISFederation.YP;

        log.debug("Creating partition "+partitionName+".");

        File workDir = project.getWorkDir();

        File sampleDir = new File(workDir, "samples/"+ YP_TEMPLATE);
        if (!sampleDir.exists()) project.download("samples/"+ YP_TEMPLATE);

        File partitionDir = new File(workDir, "partitions"+File.separator+ partitionName);

        org.apache.tools.ant.Project antProject = new org.apache.tools.ant.Project();

        antProject.setProperty("DOMAIN",         name);

        antProject.setProperty("NIS_DOMAIN",     domain.getFullName());
        antProject.setProperty("NIS_URL",        domain.getUrl());

        antProject.setProperty("YP_SUFFIX",      domain.getYpSuffix());

        Copy copy = new Copy();
        copy.setOverwrite(true);
        copy.setProject(antProject);

        FileSet fs = new FileSet();
        fs.setDir(sampleDir);
        fs.setIncludes("**/*");
        copy.addFileset(fs);

        copy.setTodir(partitionDir);

        FilterChain filterChain = copy.createFilterChain();
        ExpandProperties expandProperties = new ExpandProperties();
        expandProperties.setProject(antProject);
        filterChain.addExpandProperties(expandProperties);

        copy.execute();

        PartitionConfigs partitionConfigs = project.getPartitionConfigs();
        PartitionConfig partitionConfig = partitionConfigs.load(partitionDir);
        partitionConfigs.addPartitionConfig(partitionConfig);

        PenroseClient penroseClient = project.getClient();
        
        project.upload("partitions/"+partitionConfig.getName());
        penroseClient.startPartition(partitionConfig.getName());
    }

    public void removeYpPartition(NISDomain domain) throws Exception {
        
        PartitionConfig ypPartitionConfig = getPartitionConfig(domain.getName()+"_"+NISFederation.YP);
        if (ypPartitionConfig == null) return;

        removePartition(domain.getName()+"_"+NISFederation.YP);
    }

    public PartitionConfig createDbPartition(NISDomain domain) throws Exception {

        String name = domain.getName();
        String partitionName = name+"_"+NISFederation.DB;

        log.debug("Creating partition "+partitionName+".");

        File sampleDir = new File(project.getWorkDir(), "samples/"+ DB_TEMPLATE);

        if (!sampleDir.exists()) {
            project.download("samples/"+ DB_TEMPLATE);
        }

        File partitionDir = new File(project.getWorkDir(), "partitions"+File.separator+ partitionName);

        PenroseClient client = project.getClient();

        PartitionClient partitionClient   = client.getPartitionClient(Federation.FEDERATION);
        ConnectionClient connectionClient = partitionClient.getConnectionClient(Federation.JDBC);
        ConnectionConfig connectionConfig = connectionClient.getConnectionConfig();

        String dbUrl      = connectionConfig.getParameter("url");
        String dbUser     = connectionConfig.getParameter("user");
        String dbPassword = connectionConfig.getParameter("password");

        org.apache.tools.ant.Project antProject = new org.apache.tools.ant.Project();

        antProject.setProperty("DOMAIN",           name);

        antProject.setProperty("DB_URL",           dbUrl);
        antProject.setProperty("DB_USER",          dbUser);
        antProject.setProperty("DB_PASSWORD",      dbPassword);

        String dbSuffix = domain.getDbSuffix();

        antProject.setProperty("DB_SUFFIX",        dbSuffix);

        Copy copy = new Copy();
        copy.setOverwrite(true);
        copy.setProject(antProject);

        FileSet fs = new FileSet();
        fs.setDir(sampleDir);
        fs.setIncludes("**/*");
        copy.addFileset(fs);

        copy.setTodir(partitionDir);

        FilterChain filterChain = copy.createFilterChain();
        ExpandProperties expandProperties = new ExpandProperties();
        expandProperties.setProject(antProject);
        filterChain.addExpandProperties(expandProperties);

        copy.execute();

        PartitionConfigs partitionConfigs = project.getPartitionConfigs();
        PartitionConfig partitionConfig = partitionConfigs.load(partitionDir);
        partitionConfigs.addPartitionConfig(partitionConfig);

        PenroseClient penroseClient = project.getClient();

        project.upload("partitions/"+partitionConfig.getName());
        penroseClient.startPartition(partitionConfig.getName());

        return partitionConfig;
    }

    public void createNssPartition(NISDomain domain) throws Exception {

        if (!domain.isNssEnabled()) return;

        String name = domain.getName();
        String partitionName = name+"_"+NISFederation.NSS;

        log.debug("Creating partition "+partitionName+".");

        File sampleDir = new File(project.getWorkDir(), "samples/"+ NSS_TEMPLATE);
        if (!sampleDir.exists()) project.download("samples/"+ NSS_TEMPLATE);

        File partitionDir = new File(project.getWorkDir(), "partitions"+File.separator+partitionName);

        org.apache.tools.ant.Project antProject = new org.apache.tools.ant.Project();

        antProject.setProperty("DOMAIN",           name);

        antProject.setProperty("NIS_DOMAIN",       domain.getFullName());

        LDAPFederation ldapFederation = federation.getLdapFederation();
        LDAPRepository adRepository = ldapFederation.getRepository("ad");

        antProject.setProperty("AD_URL",           adRepository.getUrl());
        antProject.setProperty("AD_USER",          adRepository.getUser());
        antProject.setProperty("AD_PASSWORD",      adRepository.getPassword());
        antProject.setProperty("AD_SUFFIX",        adRepository.getSuffix());

        GlobalRepository globalRepository = federation.getGlobalRepository();

        antProject.setProperty("LDAP_URL",         globalRepository.getUrl());
        antProject.setProperty("LDAP_USER",        globalRepository.getUser());
        antProject.setProperty("LDAP_PASSWORD",    globalRepository.getPassword());

        antProject.setProperty("NSS_SUFFIX",       domain.getNssSuffix());
        antProject.setProperty("NIS_SUFFIX",       domain.getNisSuffix());
        antProject.setProperty("GLOBAL_SUFFIX",    globalRepository.getSuffix());

        Copy copy = new Copy();
        copy.setOverwrite(true);
        copy.setProject(antProject);

        FileSet fs = new FileSet();
        fs.setDir(sampleDir);
        fs.setIncludes("**/*");
        copy.addFileset(fs);

        copy.setTodir(partitionDir);

        FilterChain filterChain = copy.createFilterChain();
        ExpandProperties expandProperties = new ExpandProperties();
        expandProperties.setProject(antProject);
        filterChain.addExpandProperties(expandProperties);

        copy.execute();

        PartitionConfigs partitionConfigs = project.getPartitionConfigs();
        PartitionConfig partitionConfig = partitionConfigs.load(partitionDir);
        partitionConfigs.addPartitionConfig(partitionConfig);

        PenroseClient penroseClient = project.getClient();

        project.upload("partitions/"+partitionConfig.getName());
        penroseClient.startPartition(partitionConfig.getName());
    }

    public void removeNssPartition(NISDomain domain) throws Exception {
        
        PartitionConfig nssPartitionConfig = getPartitionConfig(domain.getName()+"_"+NISFederation.NSS);
        if (nssPartitionConfig == null) return;

        removePartition(domain.getName()+"_"+NISFederation.NSS);
    }

    public void addRepository(NISDomain repository) throws Exception {
        federation.addRepository(repository);
        federation.update();
    }

    public void updateRepository(NISDomain repository) throws Exception {

        removePartitions(repository);

        federation.removeRepository(repository.getName());
        federation.addRepository(repository);
        federation.update();

        createPartitions(repository);
    }

    public void removeRepository(String name) throws Exception {
        federation.removeRepository(name);
        federation.update();
    }

    public PartitionConfig getPartitionConfig(String name) throws Exception {
        return federation.getPartitionConfig(name);
    }

    public PartitionConfig removePartition(String name) throws Exception {
        return federation.removePartition(name);
    }
/*
    public Source getActions() {
        return actions;
    }

    public void setActions(Source actions) {
        this.actions = actions;
    }

    public Source getHosts() {
        return hosts;
    }

    public void setHosts(Source hosts) {
        this.hosts = hosts;
    }

    public Source getFiles() {
        return files;
    }

    public void setFiles(Source files) {
        this.files = files;
    }

    public Source getChanges() {
        return changes;
    }

    public void setChanges(Source changes) {
        this.changes = changes;
    }

    public Source getUsers() {
        return users;
    }

    public void setUsers(Source users) {
        this.users = users;
    }

    public Source getGroups() {
        return groups;
    }

    public void setGroups(Source groups) {
        this.groups = groups;
    }
*/
    public NISDomain getRepository(String name) {
        return (NISDomain)federation.getFederationConfig().getRepository(name);
    }
    
    public Collection<String> getRepositoryNames() {
        return federation.getFederationConfig().getRepositoryNames();
    }
    
    public Collection<NISDomain> getRepositories() {
        Collection<NISDomain> list = new ArrayList<NISDomain>();
        for (Repository repository : federation.getRepositories("NIS")) {
            list.add((NISDomain)repository);
        }
        return list;
    }

    public Partition getPartition() {
        return partition;
    }

    public void setPartition(Partition partition) {
        this.partition = partition;
    }


    public Collection<SourceConfig> getCacheConfigs(SourceConfigs sourceConfigs, SourceConfig sourceConfig) {

        Collection<SourceConfig> cacheConfigs = new ArrayList<SourceConfig>();

        SourceSyncConfig sourceSyncConfig = sourceConfigs.getSourceSyncConfig(sourceConfig.getName());
        if (sourceSyncConfig == null) return cacheConfigs;

        for (String name : sourceSyncConfig.getDestinations()) {
            SourceConfig cacheConfig = sourceConfigs.getSourceConfig(name);
            cacheConfigs.add(cacheConfig);
        }

        return cacheConfigs;
    }

    public void  createDatabase(NISDomain domain, PartitionConfig nisPartitionConfig) throws Exception {

        log.debug("Creating database "+domain.getName()+".");

        PenroseClient client = project.getClient();

        PartitionClient partitionClient = client.getPartitionClient(Federation.FEDERATION);

        ConnectionClient connectionClient = partitionClient.getConnectionClient(Federation.JDBC);
        //JDBCConnection connection = (JDBCConnection)partition.getConnection(Federation.JDBC);

        try {
            connectionClient.invoke("createDatabase", new Object[] { domain.getName() }, new String[] { String.class.getName() });
            //connection.createDatabase(domain.getName());
        } catch (Exception e) {
            log.debug(e.getMessage());
        }

        //PartitionConfig nisPartitionConfig = project.getPartitionConfigs().getPartitionConfig(domain.getName()+" "+NISFederation.NIS);
        SourceConfigs sourceConfigs = nisPartitionConfig.getSourceConfigs();

        for (SourceConfig sourceConfig : sourceConfigs.getSourceConfigs()) {
            if (!CACHE_CONNECTION_NAME.equals(sourceConfig.getConnectionName())) continue;

            try {
                connectionClient.invoke("createTable", new Object[] { sourceConfig }, new String[] { SourceConfig.class.getName() });
                //connection.createTable(sourceConfig);
            } catch (Exception e) {
                log.debug(e.getMessage());
            }
        }
    }

    public void removeDatabase(NISDomain domain) throws Exception {

        log.debug("Removing cache "+domain.getName()+".");

        PenroseClient client = project.getClient();

        PartitionClient partitionClient = client.getPartitionClient(Federation.FEDERATION);
        ConnectionClient connectionClient = partitionClient.getConnectionClient(Federation.JDBC);

        connectionClient.invoke("dropDatabase", new Object[] { domain.getName() }, new String[] { String.class.getName() });

        //JDBCConnection connection = (JDBCConnection)partition.getConnection(Federation.JDBC);
        //connection.dropDatabase(domain.getName());
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
