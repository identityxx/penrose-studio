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

    public final static String NIS_CONNECTION_NAME = "MySQL";
    public final static String NIS_SOURCE_NAME     = "nis";

    public final static String CACHE_CONNECTION_NAME = "Cache";
    public final static String CACHE_PARTITION_NAME  = "nis_cache";

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

    protected PartitionConfigs partitionConfigs = new PartitionConfigs();
    protected Partitions partitions = new Partitions();

    public NISTool() {
    }

    public void init(Project project) throws Exception {
        this.project = project;

        File partitionDir = new File(project.getWorkDir(), "partitions"+File.separator+NIS_SOURCE_NAME);
        PartitionConfig partitionConfig = project.getPartitionConfigs().getPartitionConfig(NIS_SOURCE_NAME);

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
            String partitionName = (String)attributes.getValue("partition");
            String server = (String)attributes.getValue("server");
            String suffix = (String)attributes.getValue("suffix");

            NISDomain domain = new NISDomain();
            domain.setName(domainName);
            domain.setPartition(partitionName);
            domain.setServer(server);
            domain.setSuffix(suffix);

            nisDomains.put(domainName, domain);

            PartitionConfig partitionConfig = project.getPartitionConfigs().getPartitionConfig(partitionName);
            partitionConfigs.addPartitionConfig(partitionConfig);

            initPartition(domain);
        }
    }

    public void createPartitionConfig(NISDomain domain) throws Exception {

        String domainName = domain.getName();
        String partitionName = domain.getPartition();
        String server = domain.getServer();
        String suffix = domain.getSuffix();

        log.debug("Creating partition config "+partitionName+".");
        log.debug(" - domain: "+domainName);
        log.debug(" - server: "+server);
        log.debug(" - suffix: "+suffix);

        File oldDir = new File(project.getWorkDir(), "partitions"+File.separator+CACHE_PARTITION_NAME);
        File newDir = new File(project.getWorkDir(), "partitions"+File.separator+ partitionName);
        FileUtil.copy(oldDir, newDir);

        org.apache.tools.ant.Project project = new org.apache.tools.ant.Project();

        project.setProperty("nis.server", server);
        project.setProperty("nis.domain", domainName);

        project.setProperty("cache.database", partitionName);

        project.setProperty("enabled", "true");
        project.setProperty("partition", partitionName);
        project.setProperty("suffix", suffix);

        Copy copy = new Copy();
        copy.setOverwrite(true);
        copy.setProject(project);

        FileSet fs = new FileSet();
        fs.setDir(new File(newDir, "template"));
        fs.setIncludes("**/*");
        copy.addFileset(fs);

        copy.setTodir(new File(newDir, "DIR-INF"));

        FilterChain filterChain = copy.createFilterChain();
        ExpandProperties expandProperties = new ExpandProperties();
        expandProperties.setProject(project);
        filterChain.addExpandProperties(expandProperties);

        copy.execute();

        PartitionConfig partitionConfig = partitionConfigs.load(newDir);
        partitionConfigs.addPartitionConfig(partitionConfig);
    }

    public void initPartition(NISDomain domain) throws Exception {

        String partitionName = domain.getPartition();
        log.debug("Initializing partition "+partitionName+".");

        File newDir = new File(project.getWorkDir(), "partitions"+File.separator+ partitionName);

        PartitionConfig partitionConfig = partitionConfigs.getPartitionConfig(partitionName);

        PartitionContext partitionContext = new PartitionContext();
        partitionContext.setPath(newDir);
        partitionContext.setPenroseConfig(project.getPenroseConfig());
        partitionContext.setPenroseContext(project.getPenroseContext());

        Partition partition = new Partition();
        partition.init(partitionConfig, partitionContext);
        partitions.addPartition(partition);
    }

    public void createDomain(NISDomain domain) throws Exception {

        String domainName = domain.getName();
        log.debug("Creating domain "+domainName+".");

        RDNBuilder rb = new RDNBuilder();
        rb.set("name", domainName);
        DN dn = new DN(rb.toRdn());

        Attributes attributes = new Attributes();
        attributes.setValue("name", domainName);
        attributes.setValue("partition", domain.getPartition());
        attributes.setValue("server", domain.getServer());
        attributes.setValue("suffix", domain.getSuffix());

        domains.add(dn, attributes);

        nisDomains.put(domainName, domain);
    }

    public void updateDomain(String oldDomainName, NISDomain domain) throws Exception {

        String newDomainName = domain.getName();

        RDNBuilder rb = new RDNBuilder();
        rb.set("name", oldDomainName);
        RDN rdn = rb.toRdn();
        DN dn = new DN(rdn);

        rb.set("name", newDomainName);
        RDN newRdn = rb.toRdn();

        if (!dn.getRdn().equals(newRdn)) {
            domains.modrdn(dn, newRdn, true);

            nisDomains.remove(oldDomainName);
            nisDomains.put(newDomainName, domain);
        }

        DNBuilder db = new DNBuilder();
        db.append(newRdn);
        db.append(dn.getParentDn());
        DN newDn = db.toDn();

        Collection<Modification> modifications = new ArrayList<Modification>();

        modifications.add(new Modification(
                Modification.REPLACE,
                new Attribute("server", domain.getServer())
        ));

        modifications.add(new Modification(
                Modification.REPLACE,
                new Attribute("suffix", domain.getSuffix())
        ));

        domains.modify(newDn, modifications);
    }

    public void removePartitionConfig(NISDomain domain) throws Exception {

        String partitionName = domain.getPartition();
        log.debug("Removing partition "+partitionName+".");

        partitionConfigs.removePartitionConfig(partitionName);

        File partitionDir = new File(project.getWorkDir(), "partitions"+File.separator+ partitionName);
        FileUtil.delete(partitionDir);
    }

    public void removePartition(NISDomain domain) throws Exception {

        String partitionName = domain.getPartition();
        log.debug("Removing partition config "+partitionName+".");

        partitions.removePartition(partitionName);
    }

    public void removeDomain(NISDomain domain) throws Exception {

        String domainName = domain.getName();
        log.debug("Removing domain "+domainName+".");

        nisDomains.remove(domainName);

        File dir = new File(project.getWorkDir(), "partitions"+File.separator+ domain.getPartition());
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

        log.debug("Creating database "+domain.getPartition()+".");

        Connection connection = nisPartition.getConnection(NIS_CONNECTION_NAME);
        JDBCAdapter adapter = (JDBCAdapter)connection.getAdapter();
        JDBCClient client = adapter.getClient();
        client.createDatabase(domain.getPartition());

        PartitionConfig partitionConfig = partitionConfigs.getPartitionConfig(domain.getPartition());
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

        log.debug("Loading cache "+domain.getPartition()+".");

        Partition partition = partitions.getPartition(domain.getPartition());
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

        log.debug("Clearing cache "+domain.getPartition()+".");

        Partition partition = partitions.getPartition(domain.getPartition());
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

        log.debug("Removing cache "+domain.getPartition()+".");

        Connection connection = nisPartition.getConnection(NIS_CONNECTION_NAME);
        JDBCAdapter adapter = (JDBCAdapter)connection.getAdapter();
        JDBCClient client = adapter.getClient();
        client.dropDatabase(domain.getPartition());
    }

    public void removeCache(NISDomain domain, Source source) throws Exception {

        log.debug("Removing cache for "+source.getName()+".");

        Partition partition = source.getPartition();
        for (Source cache : getCaches(partition, source)) {
            cache.drop();
        }
    }
}
