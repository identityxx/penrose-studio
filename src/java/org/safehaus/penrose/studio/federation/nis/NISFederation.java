package org.safehaus.penrose.studio.federation.nis;

import org.safehaus.penrose.partition.*;
import org.safehaus.penrose.source.*;
import org.safehaus.penrose.studio.util.FileUtil;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.federation.event.FederationEventListener;
import org.safehaus.penrose.studio.federation.event.FederationEvent;
import org.safehaus.penrose.studio.federation.Federation;
import org.safehaus.penrose.studio.federation.Repository;
import org.safehaus.penrose.studio.federation.GlobalRepository;
import org.safehaus.penrose.studio.federation.ldap.LDAPFederation;
import org.safehaus.penrose.studio.federation.ldap.LDAPRepository;
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
    protected Map<String, NISDomain> repositories = new TreeMap<String, NISDomain>();

    protected Collection<FederationEventListener> listeners = new ArrayList<FederationEventListener>();

    Collection<String> mapNames = new TreeSet<String>();

    public NISFederation(Federation federation) throws Exception {
        this.federation = federation;
        this.partition = federation.getPartition();
        this.project = federation.getProject();
    }

    public void load(IProgressMonitor monitor) throws Exception {
        log.debug("Starting NIS Federation tool.");

        Collection<Repository> list = federation.getRepositories("NIS");

        monitor.beginTask("Loading NIS repositories...", list.size());

        mapNames.add("Users");
        mapNames.add("Groups");
        mapNames.add("Hosts");
        mapNames.add("Services");
        mapNames.add("RPCs");
        mapNames.add("NetIDs");
        mapNames.add("Protocols");
        mapNames.add("Aliases");
        mapNames.add("Netgroups");
        mapNames.add("Ethers");
        mapNames.add("BootParams");
        mapNames.add("Networks");
        mapNames.add("Automounts");
/*
        actions   = partition.getSource("penrose_actions");
        hosts     = partition.getSource("penrose_hosts");
        files     = partition.getSource("penrose_files");
        changes   = partition.getSource("penrose_changes");
        users     = partition.getSource("penrose_users");
        groups    = partition.getSource("penrose_groups");
*/
        PenroseClient penroseClient = project.getClient();

        for (Repository rep : list) {

            if (monitor.isCanceled()) throw new InterruptedException();

            NISDomain domain = (NISDomain)rep;
            String name = domain.getName();

            repositories.put(name, domain);

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // Creating NIS Partition
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////

            PartitionConfig nisPartitionConfig = project.getPartitionConfigs().getPartitionConfig(name);

            if (nisPartitionConfig == null) { // create missing partition config during start

                monitor.subTask("Creating "+name+"...");

                nisPartitionConfig = createNisPartitionConfig(domain);
            }

            monitor.subTask("Loading "+nisPartitionConfig.getName()+"...");

            loadPartition(nisPartitionConfig);

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // Creating YP Partition
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////

            PartitionConfig ypPartitionConfig = project.getPartitionConfigs().getPartitionConfig(name+"_"+NISFederation.YP);
            boolean createPartitionConfig = ypPartitionConfig == null;

            if (createPartitionConfig) { // create missing partition configs during start

                monitor.subTask("Creating "+name+"_"+NISFederation.YP+"...");

                ypPartitionConfig = createYpPartitionConfig(domain);
            }

            monitor.subTask("Loading "+ypPartitionConfig.getName()+"...");

            loadPartition(ypPartitionConfig);

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // Creating DB Partition
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
/*
            PartitionConfig dbPartitionConfig = project.getPartitionConfigs().getPartitionConfig(name+"_"+NISFederation.DB);

            if (dbPartitionConfig == null) { // create missing partition config during start
                dbPartitionConfig = createDbPartitionConfig(domain);
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

                nssPartitionConfig = createNssPartitionConfig(domain);
            }

            monitor.subTask("Loading "+nssPartitionConfig.getName()+"...");

            loadPartition(nssPartitionConfig);

            monitor.worked(1);
        }
    }

    public PartitionConfig createNisPartitionConfig(NISDomain domain) throws Exception {

        String name = domain.getName();
        String partitionName = name;

        log.debug("Creating partition "+partitionName+".");

        File sampleDir = new File(project.getWorkDir(), "samples/"+ NIS_TEMPLATE);
        if (!sampleDir.exists()) project.download("samples/"+ NIS_TEMPLATE);

        File partitionDir = new File(project.getWorkDir(), "partitions"+File.separator+ partitionName);

        String ldapSuffix = domain.getSuffix();

        GlobalRepository globalRepository = federation.getGlobalRepository();

        String ldapUrl          = globalRepository.getUrl();
        String ldapBindDn       = globalRepository.getUser();
        String ldapBindPassword = globalRepository.getPassword();

        org.apache.tools.ant.Project antProject = new org.apache.tools.ant.Project();

        antProject.setProperty("DOMAIN",        name);

        antProject.setProperty("LDAP_URL",      ldapUrl);
        antProject.setProperty("LDAP_USER",     ldapBindDn);
        antProject.setProperty("LDAP_PASSWORD", ldapBindPassword);
        antProject.setProperty("LDAP_SUFFIX",   ldapSuffix);

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

    public PartitionConfig createYpPartitionConfig(NISDomain domain) throws Exception {

        String name = domain.getName();
        String partitionName = name+"_"+NISFederation.YP;

        log.debug("Creating partition "+partitionName+".");

        File sampleDir = new File(project.getWorkDir(), "samples/"+ YP_TEMPLATE);

        if (!sampleDir.exists()) {
            project.download("samples/"+ YP_TEMPLATE);
        }

        File partitionDir = new File(project.getWorkDir(), "partitions"+File.separator+ partitionName);

        String nisUrl = domain.getUrl();
        String ypSuffix = domain.getYpSuffix();

        org.apache.tools.ant.Project antProject = new org.apache.tools.ant.Project();

        antProject.setProperty("DOMAIN",    name);

        antProject.setProperty("NIS_URL",   nisUrl);

        antProject.setProperty("YP_SUFFIX", ypSuffix);

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

    public PartitionConfig createDbPartitionConfig(NISDomain domain) throws Exception {

        String name = domain.getName();
        String partitionName = name+"_"+NISFederation.DB;

        log.debug("Creating partition "+partitionName+".");

        File sampleDir = new File(project.getWorkDir(), "samples/"+ DB_TEMPLATE);

        if (!sampleDir.exists()) {
            project.download("samples/"+ DB_TEMPLATE);
        }

        File partitionDir = new File(project.getWorkDir(), "partitions"+File.separator+ partitionName);

        PenroseClient client = project.getClient();

        PartitionClient partitionClient   = client.getPartitionClient(Federation.PARTITION);
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

    public PartitionConfig createNssPartitionConfig(NISDomain domain) throws Exception {

        String name = domain.getName();
        String partitionName = name+"_"+NISFederation.NSS;

        log.debug("Creating partition "+partitionName+".");

        File sampleDir = new File(project.getWorkDir(), "samples/"+ NSS_TEMPLATE);

        if (!sampleDir.exists()) {
            project.download("samples/"+ NSS_TEMPLATE);
        }

        File partitionDir = new File(project.getWorkDir(), "partitions"+File.separator+partitionName);

        org.apache.tools.ant.Project antProject = new org.apache.tools.ant.Project();

        antProject.setProperty("DOMAIN",           name);

        LDAPFederation ldapFederation = federation.getLdapFederation();
        LDAPRepository adRepository = ldapFederation.getRepository("ad");

        String adUrl      = adRepository.getUrl();
        String adUser     = adRepository.getUser();
        String adPassword = adRepository.getPassword();
        String adSuffix   = adRepository.getSuffix();

        antProject.setProperty("AD_URL",           adUrl);
        antProject.setProperty("AD_USER",          adUser);
        antProject.setProperty("AD_PASSWORD",      adPassword);
        antProject.setProperty("AD_SUFFIX",        adSuffix);

        GlobalRepository globalRepository = federation.getGlobalRepository();

        String ldapUrl          = globalRepository.getUrl();
        String ldapBindDn       = globalRepository.getUser();
        String ldapBindPassword = globalRepository.getPassword();

        antProject.setProperty("LDAP_URL",         ldapUrl);
        antProject.setProperty("LDAP_USER",        ldapBindDn);
        antProject.setProperty("LDAP_PASSWORD",    ldapBindPassword);

        String nisSuffix    = domain.getSuffix();
        String nssSuffix    = domain.getNssSuffix();
        String globalSuffix = globalRepository.getSuffix();

        antProject.setProperty("NSS_SUFFIX",       nssSuffix);
        antProject.setProperty("NIS_SUFFIX",       nisSuffix);
        antProject.setProperty("GLOBAL_SUFFIX",    globalSuffix);

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

    public void loadPartition(PartitionConfig partitionConfig) throws Exception {
        federation.loadPartition(partitionConfig);
    }

    public void addRepository(NISDomain repository) throws Exception {

        federation.addRepository(repository);
        repositories.put(repository.getName(), repository);

        FederationEvent event = new FederationEvent();
        event.setRepository(repository);
        
        for (FederationEventListener listener : listeners) {
            listener.repositoryAdded(event);
        }
    }

    public void updateRepository(NISDomain repository) throws Exception {

        federation.removeRepository(repository.getName());
        federation.addRepository(repository);

        FederationEvent event = new FederationEvent();
        event.setRepository(repository);

        for (FederationEventListener listener : listeners) {
            listener.repositoryModified(event);
        }
    }

    public void removeRepository(String name) throws Exception {

        NISDomain repository = repositories.remove(name);

        File dir = new File(project.getWorkDir(), "partitions"+File.separator+ repository.getName());
        FileUtil.delete(dir);

        federation.removeRepository(repository.getName());

        FederationEvent event = new FederationEvent();
        event.setRepository(repository);

        for (FederationEventListener listener : listeners) {
            listener.repositoryRemoved(event);
        }
    }

    public PartitionConfig getPartitionConfig(String name) throws Exception {
        return federation.getPartitionConfig(name);
    }

    public PartitionConfig removePartitionConfig(String name) throws Exception {
        return federation.removePartitionConfig(name);
    }

    public void removePartition(NISDomain repository) throws Exception {
        federation.removePartition(repository);
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
        return repositories.get(name);
    }
    
    public Collection<String> getRepositoryNames() {
        return repositories.keySet();
    }
    
    public Collection<NISDomain> getRepositories() {
        return repositories.values();
    }

    public void setRepositories(Map<String, NISDomain> repositories) {
        this.repositories = repositories;
    }

    public Partitions getPartitions() {
        return federation.getPartitions();
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

        PartitionClient partitionClient = client.getPartitionClient(Federation.PARTITION);

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

        PartitionClient partitionClient = client.getPartitionClient(Federation.PARTITION);
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

    public void addListener(FederationEventListener listener) {
        listeners.add(listener);
    }

    public void removeListener(FederationEventListener listener) {
        listeners.remove(listener);
    }

    public Collection<String> getMapNames() {
        return mapNames;
    }
}
