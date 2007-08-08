package org.safehaus.penrose.studio.nis;

import org.safehaus.penrose.partition.*;
import org.safehaus.penrose.nis.NISDomain;
import org.safehaus.penrose.source.Source;
import org.safehaus.penrose.ldap.*;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.util.FileUtil;
import org.safehaus.penrose.config.PenroseConfig;
import org.safehaus.penrose.naming.PenroseContext;
import org.safehaus.penrose.connection.Connection;
import org.safehaus.penrose.jdbc.adapter.JDBCAdapter;
import org.safehaus.penrose.jdbc.JDBCClient;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.filters.ExpandProperties;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.FilterChain;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.TreeMap;
import java.util.Collection;
import java.util.ArrayList;
import java.io.File;

/**
 * @author Endi Sukma Dewata
 */
public class NISTool {

    public Logger log = Logger.getLogger(getClass());

    File workDir;
    PenroseConfig penroseConfig;
    PenroseContext penroseContext;

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

    public void init(PenroseStudio penroseStudio) throws Exception {

        penroseConfig = penroseStudio.getPenroseConfig();
        penroseContext = penroseStudio.getPenroseContext();

        PartitionConfigs partitionConfigs = penroseStudio.getPartitionConfigs();

        workDir = penroseStudio.getWorkDir();

        String name = "nis";
        File partitionDir = new File(workDir, "partitions"+File.separator+name);

        PartitionConfig partitionConfig = partitionConfigs.getPartitionConfig(name);

        PartitionContext partitionContext = new PartitionContext();
        partitionContext.setPath(partitionDir);
        partitionContext.setPenroseConfig(penroseConfig);
        partitionContext.setPenroseContext(penroseContext);

        nisPartition = new Partition();
        nisPartition.init(partitionConfig, partitionContext);

        domains = nisPartition.getSource("penrose_domains");
        actions = nisPartition.getSource("penrose_actions");
        hosts   = nisPartition.getSource("penrose_hosts");
        files   = nisPartition.getSource("penrose_files");
        changes = nisPartition.getSource("penrose_changes");
        users   = nisPartition.getSource("penrose_users");
        groups  = nisPartition.getSource("penrose_groups");

        initNisDomains(partitionConfigs);
    }

    public void initNisDomains(PartitionConfigs partitionConfigs) throws Exception {

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

            File pDir = new File(workDir, "partitions"+File.separator+partitionName);

            PartitionConfig partitionConfig = partitionConfigs.getPartitionConfig(partitionName);

            PartitionContext partitionContext = new PartitionContext();
            partitionContext.setPath(pDir);
            partitionContext.setPenroseConfig(penroseConfig);
            partitionContext.setPenroseContext(penroseContext);

            Partition partition = new Partition();
            partition.init(partitionConfig, partitionContext);
            partitions.addPartition(partition);
        }
    }

    public void createDomain(NISDomain domain) throws Exception {

        String domainName = domain.getName();
        String partitionName = domain.getPartition();
        String server = domain.getServer();
        String suffix = domain.getSuffix();

        PenroseStudio penroseStudio = PenroseStudio.getInstance();

        File oldDir = new File(workDir, "partitions"+File.separator+"nis_cache");
        File newDir = new File(workDir, "partitions"+File.separator+ partitionName);
        FileUtil.copy(oldDir, newDir);

        Project project = new Project();

        project.setProperty("nis.server", server);
        project.setProperty("nis.domain", domainName);
        project.setProperty("nis.client", "jndi");

        project.setProperty("cache.server", "localhost");
        project.setProperty("cache.database", partitionName);
        project.setProperty("cache.user", "penrose");
        project.setProperty("cache.password", "penrose");

        project.setProperty("partition", partitionName);
        project.setProperty("suffix", suffix);

        Copy copy = new Copy();
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

        PartitionConfigs partitionConfigs = penroseStudio.getPartitionConfigs();
        PartitionConfig partitionConfig = partitionConfigs.load(newDir);
        partitionConfigs.addPartitionConfig(partitionConfig);

        PartitionContext partitionContext = new PartitionContext();
        partitionContext.setPath(newDir);
        partitionContext.setPenroseConfig(penroseConfig);
        partitionContext.setPenroseContext(penroseContext);

        Partition partition = new Partition();
        partition.init(partitionConfig, partitionContext);
        partitions.addPartition(partition);

        RDNBuilder rb = new RDNBuilder();
        rb.set("name", domainName);
        DN dn = new DN(rb.toRdn());

        Attributes attributes = new Attributes();
        attributes.setValue("name", domainName);
        attributes.setValue("partition", partitionName);
        attributes.setValue("server", server);
        attributes.setValue("suffix", suffix);

        domains.add(dn, attributes);

        for (Source source : partition.getSources()) {
            String name = source.getName();
            if (!name.startsWith("cache_")) continue;
            createSource(partition, source);
        }

        nisDomains.put(domainName, domain);
    }

    public void updateDomain(NISDomain oldDomain, NISDomain newDomain) throws Exception {

        RDNBuilder rb = new RDNBuilder();
        rb.set("name", oldDomain.getName());
        RDN rdn = rb.toRdn();
        DN dn = new DN(rdn);

        rb.set("name", newDomain.getName());
        RDN newRdn = rb.toRdn();

        if (!dn.getRdn().equals(newRdn)) {
            domains.modrdn(dn, newRdn, true);
        }

        DNBuilder db = new DNBuilder();
        db.append(newRdn);
        db.append(dn.getParentDn());
        DN newDn = db.toDn();

        Collection<Modification> modifications = new ArrayList<Modification>();

        modifications.add(new Modification(
                Modification.REPLACE,
                new Attribute("partition", newDomain.getPartition())
        ));

        modifications.add(new Modification(
                Modification.REPLACE,
                new Attribute("server", newDomain.getServer())
        ));

        modifications.add(new Modification(
                Modification.REPLACE,
                new Attribute("suffix", newDomain.getSuffix())
        ));

        domains.modify(newDn, modifications);
    }

    public void removeDomain(NISDomain domain) throws Exception {

        String domainName = domain.getName();
        String partitionName = domain.getPartition();

        File dir = new File(workDir, "partitions"+File.separator+ partitionName);
        FileUtil.delete(dir);

        RDNBuilder rb = new RDNBuilder();
        rb.set("name", domainName);
        RDN rdn = rb.toRdn();
        DN dn = new DN(rdn);

        domains.delete(dn);

        nisDomains.remove(domainName);

        partitions.removePartition(partitionName);
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

    public void createDatabase(String database) throws Exception {

        log.debug("Creating database "+database+".");

        Connection connection = nisPartition.getConnection("MySQL");
        JDBCAdapter adapter = (JDBCAdapter)connection.getAdapter();
        JDBCClient client = adapter.getClient();
        client.createDatabase(database);
    }

    public void dropDatabase(String database) throws Exception {

        log.debug("Dropping database "+database+".");

        Connection connection = nisPartition.getConnection("MySQL");
        JDBCAdapter adapter = (JDBCAdapter)connection.getAdapter();
        JDBCClient client = adapter.getClient();
        client.dropDatabase(database);
    }

    public void createSource(Partition partition, Source source) throws Exception {
        Connection connection = partition.getConnection("Cache");
        JDBCAdapter adapter = (JDBCAdapter)connection.getAdapter();
        JDBCClient client = adapter.getClient();
        client.createTable(source);
    }

    public void dropSource(Partition partition, Source source) throws Exception {
        Connection connection = partition.getConnection("Cache");
        JDBCAdapter adapter = (JDBCAdapter)connection.getAdapter();
        JDBCClient client = adapter.getClient();
        client.dropTable(source);
    }
}
