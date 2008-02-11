package org.safehaus.penrose.studio.federation;

import org.safehaus.penrose.config.PenroseConfig;
import org.safehaus.penrose.naming.PenroseContext;
import org.safehaus.penrose.partition.*;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.federation.nis.NISFederation;
import org.safehaus.penrose.studio.federation.nis.NISDomain;
import org.safehaus.penrose.studio.federation.ldap.LDAPFederation;
import org.safehaus.penrose.studio.federation.ldap.LDAPRepository;
import org.safehaus.penrose.studio.util.FileUtil;
import org.safehaus.penrose.source.Source;
import org.safehaus.penrose.ldap.*;
import org.safehaus.penrose.jdbc.connection.JDBCConnection;
import org.safehaus.penrose.management.PenroseClient;
import org.apache.log4j.Logger;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.FilterChain;
import org.apache.tools.ant.filters.ExpandProperties;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.ArrayList;

/**
 * @author Endi Sukma Dewata
 */
public class Federation {

    public Logger log = Logger.getLogger(getClass());

    public final static String PARTITION = "federation";
    public final static String JDBC      = "JDBC";
    public final static String LDAP      = "LDAP";

    public final static String GLOBAL_PARAMETERS     = "global_parameters";
    public final static String REPOSITORIES          = "repositories";
    public final static String REPOSITORY_PARAMETERS = "repository_parameters";

    private Project project;
    private Partition partition;

    private LDAPFederation ldapFederation;
    private NISFederation nisFederation;

    private FederationConfig federationConfig = new FederationConfig();

    public Federation(Project project) throws Exception {
        this.project = project;

        PenroseConfig penroseConfig = project.getPenroseConfig();
        PenroseContext penroseContext = project.getPenroseContext();
        Partitions partitions = penroseContext.getPartitions();

        File partitionsDir = new File(project.getWorkDir(), "partitions");

        PartitionFactory partitionFactory = new PartitionFactory();
        partitionFactory.setPartitionsDir(partitionsDir);
        partitionFactory.setPenroseConfig(penroseConfig);
        partitionFactory.setPenroseContext(penroseContext);

        PartitionConfig defaultPartitionconfig = project.getPartitionConfigs().getPartitionConfig("DEFAULT");
        Partition defaultPartition = partitionFactory.createPartition(defaultPartitionconfig);

        partitions.addPartition(defaultPartition);

    }

    public void create(Map<String,String> allParameters, JDBCConnection connection) throws Exception {

        log.debug("Creating partition "+Federation.PARTITION +".");

        File workDir = project.getWorkDir();

        File sampleDir = new File(workDir, "samples/"+Federation.PARTITION);
        if (!sampleDir.exists()) project.download("samples/"+Federation.PARTITION);

        File partitionDir = new File(workDir, "partitions/"+Federation.PARTITION);

        String driver   = allParameters.get("driver");
        String server   = allParameters.get("host");
        String port     = allParameters.get("port");
        String user     = allParameters.get("user");
        String password = allParameters.get("password");

        org.apache.tools.ant.Project antProject = new org.apache.tools.ant.Project();

        antProject.setProperty("DRIVER",   driver);
        antProject.setProperty("SERVER",   server);
        antProject.setProperty("PORT",     port);
        antProject.setProperty("USER",     user);
        antProject.setProperty("PASSWORD", password);

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
        project.save(partitionConfig);

        log.debug("Creating database tables in "+Federation.PARTITION +".");

        try {
            connection.createDatabase(Federation.PARTITION);
        } catch (Exception e) {
            log.debug(e.getMessage());
        }

        project.upload("partitions/"+partitionConfig.getName());

        PenroseClient penroseClient = project.getClient();
        penroseClient.startPartition(partitionConfig.getName());

        log.debug("Initializing partition "+Federation.PARTITION +".");

        PenroseConfig penroseConfig = project.getPenroseConfig();
        PenroseContext penroseContext = project.getPenroseContext();

        PartitionFactory partitionFactory = new PartitionFactory();
        partitionFactory.setPartitionsDir(partitionConfigs.getPartitionsDir());
        partitionFactory.setPenroseConfig(penroseConfig);
        partitionFactory.setPenroseContext(penroseContext);

        partitionFactory.createPartition(partitionConfig);

    }

    public void removeGlobalPartition() throws Exception {

        String partitionName = "federation_global";

        PenroseClient penroseClient = project.getClient();
        penroseClient.stopPartition(partitionName);
        nisFederation.removePartitionConfig(partitionName);
        project.removeDirectory("partitions/"+partitionName);
    }

    public void createGlobalPartition(GlobalRepository globalRepository) throws Exception {

        String partitionName = "federation_global";

        PartitionConfigs partitionConfigs = project.getPartitionConfigs();
        //if (partitionConfigs.getPartitionConfig(partitionName) != null) return;

        log.debug("Creating partition "+partitionName+".");

        File sampleDir = new File(project.getWorkDir(), "samples/"+partitionName);
        if (!sampleDir.exists()) project.download("samples/"+partitionName);

        File partitionDir = new File(project.getWorkDir(), "partitions"+File.separator+ partitionName);

        org.apache.tools.ant.Project antProject = new org.apache.tools.ant.Project();

        String url          = globalRepository.getUrl();
        String bindDn       = globalRepository.getUser();
        String bindPassword = globalRepository.getPassword();
        String suffix       = globalRepository.getSuffix();

        antProject.setProperty("LDAP_URL",      url);
        antProject.setProperty("LDAP_USER",     bindDn);
        antProject.setProperty("LDAP_PASSWORD", bindPassword);
        antProject.setProperty("LDAP_SUFFIX",   suffix);

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

        PartitionConfig partitionConfig = partitionConfigs.load(partitionDir);
        partitionConfigs.addPartitionConfig(partitionConfig);

        project.upload("partitions/"+partitionConfig.getName());

        PenroseClient penroseClient = project.getClient();
        penroseClient.startPartition(partitionConfig.getName());
    }

    public void load(IProgressMonitor monitor) throws Exception {
        try {
            log.debug("Starting Federation tool.");

            monitor.beginTask("Loading partitions...", 2);

            File partitionsDir = new File(project.getWorkDir(), "partitions");

            PenroseConfig penroseConfig = project.getPenroseConfig();
            PenroseContext penroseContext = project.getPenroseContext();
            Partitions partitions = penroseContext.getPartitions();

            PartitionFactory partitionFactory = new PartitionFactory();
            partitionFactory.setPartitionsDir(partitionsDir);
            partitionFactory.setPenroseConfig(penroseConfig);
            partitionFactory.setPenroseContext(penroseContext);

            PartitionConfigs partitionConfigs = project.getPartitionConfigs();
            PartitionConfig partitionConfig = partitionConfigs.getPartitionConfig(PARTITION);
            partition = partitionFactory.createPartition(partitionConfig);

            partitions.addPartition(partition);

            //loadFromFile();
            loadFromDatabase();

            if (partitionConfigs.getPartitionConfig(Federation.PARTITION+"_global") == null) {
                GlobalRepository globalRepository = getGlobalRepository();
                createGlobalPartition(globalRepository);
            }

            SubProgressMonitor ldapMonitor = new SubProgressMonitor(monitor, 1);
            ldapFederation = new LDAPFederation(this);
            ldapFederation.load(ldapMonitor);

            SubProgressMonitor nisMonitor = new SubProgressMonitor(monitor, 1);
            nisFederation = new NISFederation(this);
            nisFederation.load(nisMonitor);

        } finally {
            monitor.done();
        }
    }

    public void loadFromFile() throws Exception {

        File partitionsDir = new File(project.getWorkDir(), "partitions");
        File file = new File(partitionsDir, PARTITION+File.separator+"DIR-INF"+File.separator+"federation.xml");

        if (file.exists()) {
            log.debug("Loading "+file);
            FederationReader reader = new FederationReader();
            reader.read(file, federationConfig);
        }
    }

    public void loadFromDatabase() throws Exception {

        Source globalParameters     = partition.getSource(GLOBAL_PARAMETERS);
        Source repositories         = partition.getSource(REPOSITORIES);
        Source repositoryParameters = partition.getSource(REPOSITORY_PARAMETERS);

        GlobalRepository globalRepository = new GlobalRepository();
        globalRepository.setName("GLOBAL");
        globalRepository.setType("GLOBAL");

        Map<String,String> parameters = new TreeMap<String,String>();

        // load global repository
        SearchRequest request = new SearchRequest();
        SearchResponse response = new SearchResponse();

        globalParameters.search(request, response);

        while (response.hasNext()) {
            SearchResult result = response.next();

            Attributes attributes = result.getAttributes();
            String paramName = (String)attributes.getValue("name");
            String paramValue = (String)attributes.getValue("value");

            parameters.put(paramName, paramValue);
        }

        if (parameters.isEmpty()) return;

        globalRepository.setParameters(parameters);

        federationConfig.addRepositoryConfig(globalRepository);

        // load other repositories
        request = new SearchRequest();
        response = new SearchResponse();

        repositories.search(request, response);

        while (response.hasNext()) {
            SearchResult result = response.next();

            Attributes attributes = result.getAttributes();
            String name = (String)attributes.getValue("name");
            String type = (String)attributes.getValue("type");

            parameters = getRepositoryParameters(repositoryParameters, name);

            RepositoryConfig repository;

            if ("LDAP".equals(type)) {
                repository = new LDAPRepository();

            } else if ("NIS".equals(type)) {
                repository = new NISDomain();

                String ypSuffix = parameters.get("ypSuffix");
                if (ypSuffix == null) {

                    DN suffix = new DN(parameters.get("suffix"));

                    DNBuilder db = new DNBuilder();
                    db.append(suffix.get(0));
                    db.append("ou=yp");
                    db.append(suffix.getSuffix(2));
                    ypSuffix = db.toString();

                    RDNBuilder rb = new RDNBuilder();
                    rb.set("repository", name);
                    rb.set("name", "ypSuffix");
                    DN paramDn = new DN(rb.toRdn());

                    Attributes paramAttrs = new Attributes();
                    paramAttrs.setValue("repository", name);
                    paramAttrs.setValue("name", "ypSuffix");
                    paramAttrs.setValue("value", ypSuffix);

                    repositoryParameters.add(paramDn, paramAttrs);

                    parameters.put("ypSuffix", ypSuffix);
                }

            } else {
                throw new Exception("Unknown type: "+type);
            }

            repository.setName(name);
            repository.setType(type);
            repository.setParameters(parameters);

            federationConfig.addRepositoryConfig(repository);
        }
    }

    public Map<String,String> getRepositoryParameters(Source repositoryParameters, String name) throws Exception {

        Map<String,String> map = new TreeMap<String,String>();

        SearchRequest request = new SearchRequest();
        request.setFilter("(repository="+name+")");

        SearchResponse response = new SearchResponse();

        repositoryParameters.search(request, response);

        while (response.hasNext()) {
            SearchResult result = response.next();

            Attributes attributes = result.getAttributes();
            String paramName = (String)attributes.getValue("name");
            String paramValue = (String)attributes.getValue("value");

            map.put(paramName, paramValue);
        }

        return map;
    }

    public Partition getPartition() {
        return partition;
    }

    public void setPartition(Partition partition) {
        this.partition = partition;
    }

    public LDAPFederation getLdapFederation() {
        return ldapFederation;
    }

    public void setLdapFederation(LDAPFederation ldapFederation) {
        this.ldapFederation = ldapFederation;
    }

    public NISFederation getNisFederation() {
        return nisFederation;
    }

    public void setNisFederation(NISFederation nisFederation) {
        this.nisFederation = nisFederation;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
/*
    public Source getGlobalParameters() {
        return globalParameters;
    }

    public void setGlobalParameters(Source globalParameters) {
        this.globalParameters = globalParameters;
    }

    public Source getRepositories() {
        return repositories;
    }

    public void setRepositories(Source repositories) {
        this.repositories = repositories;
    }

    public Source getRepositoryParameters() {
        return repositoryParameters;
    }

    public void setRepositoryParameters(Source repositoryParameters) {
        this.repositoryParameters = repositoryParameters;
    }
*/
/*
    public Collection<String> getRepositoryNames(String type) throws Exception {

        Collection<String> list = new ArrayList<String>();

        SearchRequest request = new SearchRequest();
        request.setFilter("(type="+type+")");

        SearchResponse response = new SearchResponse();

        repositories.search(request, response);

        while (response.hasNext()) {
            SearchResult result = response.next();

            RDN rdn = result.getDn().getRdn();
            String name = (String)rdn.get("name");

            list.add(name);
        }

        return list;
    }
*/
    public Collection<RepositoryConfig> getRepositories(String type) throws Exception {

        Collection<RepositoryConfig> list = new ArrayList<RepositoryConfig>();

        for (RepositoryConfig repository : federationConfig.getRepositoryConfigs()) {
            if (type.equals(repository.getType())) {
                list.add(repository);
            }
        }
    
        return list;
    }

    public void addRepository(RepositoryConfig repository) throws Exception {

        federationConfig.addRepositoryConfig(repository);
        save();
/*
        RDNBuilder rb = new RDNBuilder();
        rb.set("name", repository.getName());
        DN dn = new DN(rb.toRdn());

        Attributes attributes = new Attributes();
        attributes.setValue("name", repository.getName());
        attributes.setValue("type", repository.getType());

        repositories.add(dn, attributes);

        Map<String,String> parameters = repository.getParameters();

        for (String paramName : parameters.keySet()) {
            String paramValue = parameters.get(paramName);

            rb = new RDNBuilder();
            rb.set("repository", repository.getName());
            rb.set("name", paramName);
            dn = new DN(rb.toRdn());

            attributes = new Attributes();
            attributes.setValue("repository", repository.getName());
            attributes.setValue("name", paramName);
            attributes.setValue("value", paramValue);

            repositoryParameters.add(dn, attributes);
        }
*/
    }

    public void removeRepository(String name) throws Exception {

        federationConfig.removeRepositoryConfig(name);
        save();
/*
        RDNBuilder rb = new RDNBuilder();
        rb.set("repository", name);
        DN dn = new DN(rb.toRdn());

        repositoryParameters.delete(dn);

        rb = new RDNBuilder();
        rb.set("name", name);
        dn = new DN(rb.toRdn());

        repositories.delete(dn);
*/
    }

    public GlobalRepository getGlobalRepository() throws Exception {
        return (GlobalRepository)federationConfig.getRepositoryConfig("GLOBAL");
    }

    public void setGlobalRepository(GlobalRepository repository) throws Exception {

        federationConfig.addRepositoryConfig(repository);
        save();
/*
        Map<String,String> parameters = repository.getParameters();

        globalParameters.delete(new DN());

        for (String paramName : parameters.keySet()) {
            String paramValue = parameters.get(paramName);

            RDNBuilder rb = new RDNBuilder();
            rb.set("name", paramName);
            DN dn = new DN(rb.toRdn());

            Attributes attributes = new Attributes();
            attributes.setValue("name", paramName);
            attributes.setValue("value", paramValue);

            globalParameters.add(dn, attributes);
        }
*/
    }

    public PartitionConfig getPartitionConfig(String name) throws Exception {
        return project.getPartitionConfigs().getPartitionConfig(name);
    }

    public PartitionConfig removePartitionConfig(String name) throws Exception {

        log.debug("Removing partition "+name+".");

        PartitionConfig partitionConfig = project.getPartitionConfigs().removePartitionConfig(name);

        File partitionDir = new File(project.getWorkDir(), "partitions"+File.separator+ name);
        FileUtil.delete(partitionDir);

        return partitionConfig;
    }

    public void removePartition(RepositoryConfig repository) throws Exception {

        String name = repository.getName();
        log.debug("Removing partition config "+name+".");

        PenroseContext penroseContext = project.getPenroseContext();
        Partitions partitions = penroseContext.getPartitions();
        partitions.removePartition(name);
    }

    public Partitions getPartitions() {
        PenroseContext penroseContext = project.getPenroseContext();
        return penroseContext.getPartitions();
    }

    public void save() throws Exception {

        File partitionsDir = new File(project.getWorkDir(), "partitions");
        File file = new File(partitionsDir, PARTITION+File.separator+"DIR-INF"+File.separator+"federation.xml");

        log.debug("Storing "+file);
        FederationWriter writer = new FederationWriter();
        writer.write(file, federationConfig);

        project.upload("partitions/"+PARTITION+"/DIR-INF/federation.xml");
    }

    public FederationConfig getFederationConfig() {
        return federationConfig;
    }

    public void setFederationConfig(FederationConfig federationConfig) {
        this.federationConfig = federationConfig;
    }
}
