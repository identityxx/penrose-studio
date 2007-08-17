package org.safehaus.penrose.studio.nis;

import org.safehaus.penrose.partition.*;
import org.safehaus.penrose.nis.NISDomain;
import org.safehaus.penrose.source.*;
import org.safehaus.penrose.ldap.*;
import org.safehaus.penrose.studio.util.FileUtil;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.connection.Connection;
import org.safehaus.penrose.jdbc.adapter.JDBCAdapter;
import org.safehaus.penrose.jdbc.JDBCClient;
import org.apache.tools.ant.filters.ExpandProperties;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.FilterChain;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.log4j.Logger;

import java.util.*;
import java.io.File;

/**
 * @author Endi Sukma Dewata
 */
public class NISTool {

    public Logger log = Logger.getLogger(getClass());

    public final static String NIS_PARTITION_NAME  = "nis_cache";
    public final static String NIS_CONNECTION_NAME = "NIS";

    public final static String CACHE_USERS  = "cache_users";
    public final static String CACHE_GROUPS = "cache_groups";

    Project project;

    protected Partition nisPartition;

    protected Source domains;
    protected Source actions;
    protected Source hosts;
    protected Source files;
    protected Source changes;
    protected Source users;
    protected Source groups;

    protected Map<String,NISDomain> nisDomains = new TreeMap<String,NISDomain>();

    protected Partitions partitions = new Partitions();

    public NISTool() {
    }

    public void init(Project project) throws Exception {
        this.project = project;

        File partitionDir = new File(project.getWorkDir(), "partitions"+File.separator+NIS_PARTITION_NAME);
        PartitionConfig partitionConfig = project.getPartitionConfigs().getPartitionConfig(NIS_PARTITION_NAME);

        PartitionContext partitionContext = new PartitionContext();
        partitionContext.setPath(partitionDir);
        partitionContext.setPenroseConfig(project.getPenroseConfig());
        partitionContext.setPenroseContext(project.getPenroseContext());

        nisPartition = new Partition();
        nisPartition.init(partitionConfig, partitionContext);

        domains = nisPartition.getSource("penrose_domains");
        actions = nisPartition.getSource("penrose_actions");
        hosts   = nisPartition.getSource("penrose_hosts");
        files   = nisPartition.getSource("penrose_files");
        changes = nisPartition.getSource("penrose_changes");
        users   = nisPartition.getSource("penrose_users");
        groups  = nisPartition.getSource("penrose_groups");

        initNisDomains();
    }

    public void initNisDomains() throws Exception {

        SearchRequest searchRequest = new SearchRequest();
        SearchResponse<SearchResult> searchResponse = new SearchResponse<SearchResult>();

        domains.search(searchRequest, searchResponse);

        while (searchResponse.hasNext()) {
            SearchResult searchResult = searchResponse.next();
            Attributes attributes = searchResult.getAttributes();

            String domainName = (String)attributes.getValue("name");
            String fullName = (String)attributes.getValue("fullName");
            String server = (String)attributes.getValue("server");
            String suffix = (String)attributes.getValue("suffix");

            NISDomain domain = new NISDomain();
            domain.setName(domainName);
            domain.setFullName(fullName);
            domain.setServer(server);
            domain.setSuffix(suffix);

            nisDomains.put(domainName, domain);

            PartitionConfig partitionConfig = project.getPartitionConfigs().getPartitionConfig(domainName);
            if (partitionConfig == null) continue;

            loadPartition(domain);
        }
    }

    public void createPartitionConfig(NISDomain domain) throws Exception {

        String domainName = domain.getName();
        String fullName = domain.getFullName();
        String server = domain.getServer();
        String suffix = domain.getSuffix();

        log.debug("Creating partition "+domainName+".");
        log.debug(" - domain: "+fullName);
        log.debug(" - server: "+server);
        log.debug(" - suffix: "+suffix);

        File oldDir = new File(project.getWorkDir(), "partitions"+File.separator+ NIS_PARTITION_NAME);
        File newDir = new File(project.getWorkDir(), "partitions"+File.separator+ domainName);
        FileUtil.copy(oldDir, newDir);

        org.apache.tools.ant.Project antProject = new org.apache.tools.ant.Project();

        antProject.setProperty("nis.server", server);
        antProject.setProperty("nis.domain", fullName);

        antProject.setProperty("domain", domainName);
        antProject.setProperty("suffix", suffix);

        Copy copy = new Copy();
        copy.setOverwrite(true);
        copy.setProject(antProject);

        FileSet fs = new FileSet();
        fs.setDir(new File(newDir, "template"));
        fs.setIncludes("**/*");
        copy.addFileset(fs);

        copy.setTodir(new File(newDir, "DIR-INF"));

        FilterChain filterChain = copy.createFilterChain();
        ExpandProperties expandProperties = new ExpandProperties();
        expandProperties.setProject(antProject);
        filterChain.addExpandProperties(expandProperties);

        copy.execute();

        PartitionConfigs partitionConfigs = project.getPartitionConfigs();
        PartitionConfig partitionConfig = partitionConfigs.load(newDir);
        partitionConfigs.addPartitionConfig(partitionConfig);
    }

    public void loadPartition(NISDomain domain) throws Exception {

        String domainName = domain.getName();
        log.debug("Loading partition "+domainName+".");

        File newDir = new File(project.getWorkDir(), "partitions"+File.separator+ domainName);

        PartitionConfig partitionConfig = project.getPartitionConfigs().getPartitionConfig(domainName);

        PartitionContext partitionContext = new PartitionContext();
        partitionContext.setPath(newDir);
        partitionContext.setPenroseConfig(project.getPenroseConfig());
        partitionContext.setPenroseContext(project.getPenroseContext());

        Partition partition = new Partition();
        partition.init(partitionConfig, partitionContext);
        partitions.addPartition(partition);
    }

    public void unloadPartition(String domainName) throws Exception {
        partitions.removePartition(domainName);
    }

    public void createDomain(NISDomain domain) throws Exception {

        String domainName = domain.getName();
        log.debug("Creating domain "+domainName+".");

        RDNBuilder rb = new RDNBuilder();
        rb.set("name", domainName);
        DN dn = new DN(rb.toRdn());

        Attributes attributes = new Attributes();
        attributes.setValue("name", domainName);
        attributes.setValue("fullName", domain.getFullName());
        attributes.setValue("server", domain.getServer());
        attributes.setValue("suffix", domain.getSuffix());

        domains.add(dn, attributes);

        nisDomains.put(domainName, domain);
    }

    public void updateDomain(String oldDomainName, NISDomain domain) throws Exception {

        RDNBuilder rb = new RDNBuilder();
        rb.set("name", domain.getName());
        RDN rdn = rb.toRdn();
        DN dn = new DN(rdn);

        Collection<Modification> modifications = new ArrayList<Modification>();

        modifications.add(new Modification(
                Modification.REPLACE,
                new Attribute("fullName", domain.getFullName())
        ));

        modifications.add(new Modification(
                Modification.REPLACE,
                new Attribute("server", domain.getServer())
        ));

        modifications.add(new Modification(
                Modification.REPLACE,
                new Attribute("suffix", domain.getSuffix())
        ));

        domains.modify(dn, modifications);
    }

    public void removePartitionConfig(NISDomain domain) throws Exception {

        String domainName = domain.getName();
        log.debug("Removing partition "+domainName+".");

        project.getPartitionConfigs().removePartitionConfig(domainName);

        File partitionDir = new File(project.getWorkDir(), "partitions"+File.separator+ domainName);
        FileUtil.delete(partitionDir);
    }

    public void removePartition(NISDomain domain) throws Exception {

        String domainName = domain.getName();
        log.debug("Removing partition config "+domainName+".");

        partitions.removePartition(domainName);
    }

    public void removeDomain(NISDomain domain) throws Exception {

        String domainName = domain.getName();
        log.debug("Removing domain "+domainName+".");

        nisDomains.remove(domainName);

        File dir = new File(project.getWorkDir(), "partitions"+File.separator+ domain.getName());
        FileUtil.delete(dir);

        RDNBuilder rb = new RDNBuilder();
        rb.set("name", domainName);
        RDN rdn = rb.toRdn();
        DN dn = new DN(rdn);

        domains.delete(dn);
    }

    public Source getDomains() {
        return domains;
    }

    public void setDomains(Source domains) {
        this.domains = domains;
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

    public Map<String, NISDomain> getNisDomains() {
        return nisDomains;
    }

    public void setNisDomains(Map<String, NISDomain> nisDomains) {
        this.nisDomains = nisDomains;
    }

    public Partitions getPartitions() {
        return partitions;
    }

    public void setPartitions(Partitions partitions) {
        this.partitions = partitions;
    }

    public Partition getNisPartition() {
        return nisPartition;
    }

    public void setNisPartition(Partition nisPartition) {
        this.nisPartition = nisPartition;
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

    public Collection<Source> getCaches(Partition partition, Source source) {

        Collection<Source> caches = new ArrayList<Source>();

        PartitionConfig partitionConfig = partition.getPartitionConfig();
        SourceConfigs sourceConfigs = partitionConfig.getSourceConfigs();

        for (SourceConfig cacheConfig : getCacheConfigs(sourceConfigs, source.getSourceConfig())) {
            Source cache = partition.getSource(cacheConfig.getName());
            caches.add(cache);
        }

        return caches;
    }

    public void createDatabase(NISDomain domain) throws Exception {

        log.debug("Creating database "+domain.getName()+".");

        Connection connection = nisPartition.getConnection(NIS_CONNECTION_NAME);
        JDBCAdapter adapter = (JDBCAdapter)connection.getAdapter();
        JDBCClient client = adapter.getClient();
        client.createDatabase(domain.getName());

        PartitionConfig partitionConfig = project.getPartitionConfigs().getPartitionConfig(domain.getName());
        SourceConfigs sourceConfigs = partitionConfig.getSourceConfigs();
        for (SourceConfig sourceConfig : sourceConfigs.getSourceConfigs()) {
            for (SourceConfig cache : getCacheConfigs(sourceConfigs, sourceConfig)) {
                client.createTable(cache);
            }
        }
    }

    public void createCache(NISDomain domain, Source source) throws Exception {

        log.debug("Creating cache for "+source.getName()+".");
        
        Partition partition = source.getPartition();
        for (Source cache : getCaches(partition, source)) {
            cache.create();
        }
    }

    public void loadCache(NISDomain domain) throws Exception {

        log.debug("Loading cache "+domain.getName()+".");

        Partition partition = partitions.getPartition(domain.getName());
        Collection<SourceSync> sourceSyncs = partition.getSourceSyncs();
        for (SourceSync sourceSync : sourceSyncs) {
            sourceSync.load();
        }
    }

    public void loadCache(NISDomain domain, Source source) throws Exception {

        log.debug("Loading cache for "+source.getName()+".");

        Partition partition = source.getPartition();
        SourceSync sourceSync = partition.getSourceSync(source.getName());
        sourceSync.load();
    }

    public void clearCache(NISDomain domain) throws Exception {

        log.debug("Clearing cache "+domain.getName()+".");

        Partition partition = partitions.getPartition(domain.getName());
        Collection<SourceSync> caches = partition.getSourceSyncs();
        for (SourceSync sourceSync : caches) {
            sourceSync.clean();
        }
    }

    public void clearCache(NISDomain domain, Source source) throws Exception {

        log.debug("Clearing cache for "+source.getName()+".");

        Partition partition = source.getPartition();
        SourceSync sourceSync = partition.getSourceSync(source.getName());
        sourceSync.clean();
    }

    public void removeCache(NISDomain domain) throws Exception {

        log.debug("Removing cache "+domain.getName()+".");

        Connection connection = nisPartition.getConnection(NIS_CONNECTION_NAME);
        JDBCAdapter adapter = (JDBCAdapter)connection.getAdapter();
        JDBCClient client = adapter.getClient();
        client.dropDatabase(domain.getName());
    }

    public void removeCache(NISDomain domain, Source source) throws Exception {

        log.debug("Removing cache for "+source.getName()+".");

        Partition partition = source.getPartition();
        for (Source cache : getCaches(partition, source)) {
            cache.drop();
        }
    }
}
