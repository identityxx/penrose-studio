package org.safehaus.penrose.studio.federation.nis;

import org.safehaus.penrose.partition.*;
import org.safehaus.penrose.source.*;
import org.safehaus.penrose.studio.util.FileUtil;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.project.ProjectConfig;
import org.safehaus.penrose.studio.federation.event.FederationEventListener;
import org.safehaus.penrose.studio.federation.event.FederationEvent;
import org.safehaus.penrose.studio.federation.Federation;
import org.safehaus.penrose.studio.federation.Repository;
import org.safehaus.penrose.studio.federation.GlobalRepository;
import org.safehaus.penrose.studio.federation.ldap.LDAPFederation;
import org.safehaus.penrose.studio.federation.ldap.LDAPRepository;
import org.safehaus.penrose.connection.Connection;
import org.safehaus.penrose.jdbc.adapter.JDBCAdapter;
import org.safehaus.penrose.jdbc.JDBCClient;
import org.safehaus.penrose.management.PenroseClient;
import org.safehaus.penrose.management.ServiceClient;
import org.safehaus.penrose.config.PenroseConfig;
import org.safehaus.penrose.service.ServiceConfig;
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

    public final static String NIS_TOOL              = "nis_tool";
    public final static String NIS_TEMPLATE          = "federation_nis";
    public final static String NSS_TEMPLATE          = "federation_nss";

    public final static String CACHE_USERS           = "cache_users";
    public final static String CACHE_GROUPS          = "cache_groups";
    public final static String CACHE_CONNECTION_NAME = "Cache";

    public final static String CHANGE_USERS          = "change_users";
    public final static String CHANGE_GROUPS         = "change_groups";

    public final static String LDAP_CONNECTION_NAME  = "LDAP";

    Project project;
    Partition partition;
    Federation federation;

    //protected Source schedules;
    protected Source actions;
    protected Source hosts;
    protected Source files;
    protected Source changes;
    protected Source users;
    protected Source groups;

    protected Map<String, NISDomain> repositories = new TreeMap<String, NISDomain>();

    protected Collection<FederationEventListener> listeners = new ArrayList<FederationEventListener>();

    Map<String,String> sourceLabels = new TreeMap<String,String>();

    public NISFederation(Federation federation) throws Exception {
        this.federation = federation;
        this.partition = federation.getPartition();
        this.project = federation.getProject();
    }

    public void load(IProgressMonitor monitor) throws Exception {
        log.debug("Starting NIS Federation tool.");

        sourceLabels.put("nis_users", "Users");
        sourceLabels.put("nis_groups", "Groups");
        sourceLabels.put("nis_hosts", "Hosts");
        sourceLabels.put("nis_services", "Services");
        sourceLabels.put("nis_rpcs", "RPCs");
        sourceLabels.put("nis_netids", "NetIDs");
        sourceLabels.put("nis_protocols", "Protocols");
        sourceLabels.put("nis_aliases", "Aliases");
        sourceLabels.put("nis_netgroups", "Netgroups");
        sourceLabels.put("nis_ethers", "Ethers");
        sourceLabels.put("nis_bootparams", "BootParams");
        sourceLabels.put("nis_networks", "Networks");
        sourceLabels.put("nis_automounts", "Automounts");

        //schedules = partition.getSource("penrose_schedules");
        actions   = partition.getSource("penrose_actions");
        hosts     = partition.getSource("penrose_hosts");
        files     = partition.getSource("penrose_files");
        changes   = partition.getSource("penrose_changes");
        users     = partition.getSource("penrose_users");
        groups    = partition.getSource("penrose_groups");

        PenroseClient penroseClient = project.getClient();

        for (Repository rep : federation.getRepositories("NIS")) {

            if (monitor.isCanceled()) break;

            NISDomain domain = (NISDomain)rep;
            String name = domain.getName();

            repositories.put(name, domain);

            PartitionConfig partitionConfig = project.getPartitionConfigs().getPartitionConfig(name);
            boolean createPartitionConfig = partitionConfig == null;

            if (createPartitionConfig) { // create missing partition configs during start

                monitor.subTask("Creating "+name+"...");

                partitionConfig = createPartitionConfig(domain);
            }

            // create missing databases/tables
            createDatabase(domain);

            if (createPartitionConfig) {
                project.upload("partitions/"+name);
                penroseClient.startPartition(name);
            }

            loadPartition(partitionConfig);

            PartitionConfig nssPartitionConfig = project.getPartitionConfigs().getPartitionConfig(name+"_nss");

            if (nssPartitionConfig == null) { // create missing partition configs during start
                nssPartitionConfig = createNssPartitionConfig(domain);
                project.upload("partitions/"+nssPartitionConfig.getName());
                penroseClient.startPartition(nssPartitionConfig.getName());
            }

            monitor.subTask("Loading "+name+"...");

            loadPartition(nssPartitionConfig);

            monitor.worked(1);
        }
    }

    public PartitionConfig createPartitionConfig(NISDomain domain) throws Exception {

        String name = domain.getName();
        log.debug("Creating partition "+name+".");

        File sampleDir = new File(project.getWorkDir(), "samples/"+ NIS_TEMPLATE);

        if (!sampleDir.exists()) {
            project.download("samples/"+ NIS_TEMPLATE);
        }

        File partitionDir = new File(project.getWorkDir(), "partitions"+File.separator+ name);

        String nisUrl = domain.getUrl();

        Connection dbConnection = partition.getConnection(Federation.JDBC);

        String dbUrl = dbConnection.getParameter("url");
        String dbUser = dbConnection.getParameter("user");
        String dbPassword = dbConnection.getParameter("password");

        ProjectConfig projectConfig = project.getProjectConfig();
        String penroseHost = projectConfig.getHost();

        PenroseClient client = project.getClient();
        ServiceClient serviceClient = client.getServiceClient("LDAP");
        ServiceConfig serviceConfig = serviceClient.getServiceConfig();
        String penrosePort = serviceConfig.getParameter("ldapPort");

        String penroseUrl = "ldap://"+penroseHost+":"+penrosePort+"/";

        PenroseConfig penroseConfig = project.getPenroseConfig();
        String penroseBindDn = penroseConfig.getRootDn().toString();
        String penroseBindPassword = new String(penroseConfig.getRootPassword());

        org.apache.tools.ant.Project antProject = new org.apache.tools.ant.Project();

        antProject.setProperty("DOMAIN",           name);

        antProject.setProperty("NIS_URL",          nisUrl);

        antProject.setProperty("DB_URL",           dbUrl);
        antProject.setProperty("DB_USER",          dbUser);
        antProject.setProperty("DB_PASSWORD",      dbPassword);

        antProject.setProperty("PENROSE_URL",      penroseUrl);
        antProject.setProperty("PENROSE_USER",     penroseBindDn);
        antProject.setProperty("PENROSE_PASSWORD", penroseBindPassword);

        GlobalRepository globalRepository = federation.getGlobalRepository();

        String ldapUrl = globalRepository.getUrl();
        String ldapBindDn = globalRepository.getUser();
        String ldapBindPassword = globalRepository.getPassword();

        antProject.setProperty("LDAP_URL",         ldapUrl);
        antProject.setProperty("LDAP_USER",        ldapBindDn);
        antProject.setProperty("LDAP_PASSWORD",    ldapBindPassword);

        String localSuffix = domain.getSuffix();
        String globalSuffix = globalRepository.getSuffix();

        antProject.setProperty("LOCAL_SUFFIX",     localSuffix);
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

        return partitionConfig;
    }

    public PartitionConfig createNssPartitionConfig(NISDomain domain) throws Exception {

        String name = domain.getName();
        log.debug("Creating partition "+name+".");

        File sampleDir = new File(project.getWorkDir(), "samples/"+ NSS_TEMPLATE);

        if (!sampleDir.exists()) {
            project.download("samples/"+ NSS_TEMPLATE);
        }

        File partitionDir = new File(project.getWorkDir(), "partitions"+File.separator+ name+"_nss");

        org.apache.tools.ant.Project antProject = new org.apache.tools.ant.Project();

        antProject.setProperty("DOMAIN",           name);

        LDAPFederation ldapFederation = federation.getLdapFederation();
        LDAPRepository adRepository = ldapFederation.getRepository("ad");

        String adUrl = adRepository.getUrl();
        String adUser = adRepository.getUser();
        String adPassword = adRepository.getPassword();
        String adSuffix = adRepository.getSuffix();

        antProject.setProperty("AD_URL",           adUrl);
        antProject.setProperty("AD_USER",          adUser);
        antProject.setProperty("AD_PASSWORD",      adPassword);
        antProject.setProperty("AD_SUFFIX",        adSuffix);

        GlobalRepository globalRepository = federation.getGlobalRepository();

        String ldapUrl = globalRepository.getUrl();
        String ldapBindDn = globalRepository.getUser();
        String ldapBindPassword = globalRepository.getPassword();

        antProject.setProperty("LDAP_URL",         ldapUrl);
        antProject.setProperty("LDAP_USER",        ldapBindDn);
        antProject.setProperty("LDAP_PASSWORD",    ldapBindPassword);

        String nisSuffix = domain.getSuffix();
        String nssSuffix = domain.getNssSuffix();
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

    public void  createDatabase(NISDomain domain) throws Exception {

        log.debug("Creating database "+domain.getName()+".");

        Connection connection = partition.getConnection(Federation.JDBC);
        JDBCAdapter adapter = (JDBCAdapter)connection.getAdapter();
        JDBCClient client = adapter.getClient();

        try {
            client.createDatabase(domain.getName());
        } catch (Exception e) {
            log.debug(e.getMessage());
        }

        PartitionConfig partitionConfig = project.getPartitionConfigs().getPartitionConfig(domain.getName());
        SourceConfigs sourceConfigs = partitionConfig.getSourceConfigs();

        for (SourceConfig sourceConfig : sourceConfigs.getSourceConfigs()) {
            if (!CACHE_CONNECTION_NAME.equals(sourceConfig.getConnectionName())) continue;

            try {
                client.createTable(sourceConfig);
            } catch (Exception e) {
                log.debug(e.getMessage());
            }
        }
    }

    public void removeDatabase(NISDomain domain) throws Exception {

        log.debug("Removing cache "+domain.getName()+".");

        Connection connection = partition.getConnection(Federation.JDBC);
        JDBCAdapter adapter = (JDBCAdapter)connection.getAdapter();
        JDBCClient client = adapter.getClient();
        client.dropDatabase(domain.getName());
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

    public Source getSchedules() {
        return null; //schedules;
    }

    public void setSchedules(Source schedules) {
        //this.schedules = schedules;
    }

    public Collection<String> getSourceNames() {
        return sourceLabels.keySet();
    }

    public String getSourceLabel(String sourceName) {
        return sourceLabels.get(sourceName);
    }
}
