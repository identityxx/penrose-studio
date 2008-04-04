package org.safehaus.penrose.studio.federation;

import org.apache.log4j.Logger;
import org.apache.tools.ant.filters.ExpandProperties;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.FilterChain;
import org.eclipse.core.runtime.IProgressMonitor;
import org.safehaus.penrose.federation.FederationConfig;
import org.safehaus.penrose.federation.FederationReader;
import org.safehaus.penrose.federation.repository.GlobalRepository;
import org.safehaus.penrose.federation.repository.LDAPRepository;
import org.safehaus.penrose.federation.repository.NISDomain;
import org.safehaus.penrose.federation.repository.Repository;
import org.safehaus.penrose.management.PenroseClient;
import org.safehaus.penrose.management.module.ModuleClient;
import org.safehaus.penrose.management.partition.PartitionClient;
import org.safehaus.penrose.management.partition.PartitionManagerClient;
import org.safehaus.penrose.module.ModuleConfig;
import org.safehaus.penrose.partition.PartitionConfig;
import org.safehaus.penrose.partition.PartitionReader;
import org.safehaus.penrose.studio.federation.ldap.LDAPFederation;
import org.safehaus.penrose.studio.federation.nis.NISFederation;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.util.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Endi Sukma Dewata
 */
public class Federation {

    public Logger log = Logger.getLogger(getClass());

    public final static String FEDERATION = "federation";
    public final static String GLOBAL     = "global";

    public final static String JDBC       = "JDBC";
    public final static String LDAP       = "LDAP";

    public final static String GLOBAL_PARAMETERS     = "global_parameters";
    public final static String REPOSITORIES          = "repositories";
    public final static String REPOSITORY_PARAMETERS = "repository_parameters";

    private Project project;

    private LDAPFederation ldapFederation;
    private NISFederation nisFederation;

    private FederationConfig federationConfig;

    public Federation(Project project) throws Exception {
        this.project = project;
/*
        PenroseConfig penroseConfig = project.getPenroseConfig();
        PenroseContext penroseContext = project.getPenroseContext();
        Partitions partitions = penroseContext.getPartitionManager();

        File partitionsDir = new File(project.getWorkDir(), "partitions");

        PartitionFactory partitionFactory = new PartitionFactory();
        partitionFactory.setPartitionsDir(partitionsDir);
        partitionFactory.setPenroseConfig(penroseConfig);
        partitionFactory.setPenroseContext(penroseContext);

        PartitionConfig defaultPartitionconfig = project.getPartitionConfigManager().getPartitionConfig("DEFAULT");
        Partition defaultPartition = partitionFactory.createPartition(defaultPartitionconfig);

        partitions.addPartition(defaultPartition);
*/
    }

    public void createPartition() throws Exception {
    //public void createPartition(Map<String,String> allParameters, JDBCConnection connection) throws Exception {

        String partitionName = Federation.FEDERATION;

        log.debug("Creating partition "+partitionName +".");

        File workDir = project.getWorkDir();

        File sampleDir = new File(workDir, "samples/"+partitionName);
        if (!sampleDir.exists()) project.download("samples/"+partitionName);

        File partitionDir = new File(workDir, "partitions/"+partitionName);

        org.apache.tools.ant.Project antProject = new org.apache.tools.ant.Project();
/*
        String driver   = allParameters.get("driver");
        String server   = allParameters.get("host");
        String port     = allParameters.get("port");
        String user     = allParameters.get("user");
        String password = allParameters.get("password");

        antProject.setProperty("DRIVER",   driver);
        antProject.setProperty("SERVER",   server);
        antProject.setProperty("PORT",     port);
        antProject.setProperty("USER",     user);
        antProject.setProperty("PASSWORD", password);
*/
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
/*
        PartitionConfigManager partitionConfigManager = project.getPartitionConfigManager();
        PartitionConfig partitionConfig = partitionConfigManager.load(partitionDir);
        partitionConfigManager.addPartitionConfig(partitionConfig);

        project.save(partitionConfig);
        project.upload("partitions/"+partitionConfig.getName());

        PenroseClient client = project.getClient();
        client.startPartition(partitionConfig.getName());
*/
        PartitionConfig partitionConfig = new PartitionConfig(partitionName);
        partitionConfig.load(partitionDir);

        PenroseClient client = project.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
        partitionManagerClient.createPartition(partitionConfig);

        log.debug("Initializing partition "+Federation.FEDERATION +".");
    }

    public void removeGlobalPartition() throws Exception {
        nisFederation.removePartition(GLOBAL);
    }

    public void createGlobalPartition() throws Exception {

        GlobalRepository globalRepository = getGlobalRepository();
        if (globalRepository == null) return;

        String templateName = FEDERATION+"_"+GLOBAL;
        String partitionName = GLOBAL;

        //if (partitionConfigManager.getPartitionConfig(templateName) != null) return;

        log.debug("Creating global partition.");

        File sampleDir = new File(project.getWorkDir(), "samples/"+ templateName);
        if (!sampleDir.exists()) project.download("samples/"+ templateName);

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
/*
        PartitionConfigManager partitionConfigManager = project.getPartitionConfigManager();
        PartitionConfig partitionConfig = partitionConfigManager.load(partitionDir);
        partitionConfigManager.addPartitionConfig(partitionConfig);

        project.upload("partitions/"+partitionConfig.getName());

        PenroseClient penroseClient = project.getClient();
        penroseClient.startPartition(partitionConfig.getName());
*/
        PartitionConfig partitionConfig = new PartitionConfig(partitionName);
        partitionConfig.load(partitionDir);

        PenroseClient client = project.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
        partitionManagerClient.createPartition(partitionConfig);
    }

    public void load(IProgressMonitor monitor) throws Exception {
        try {
            log.debug("Starting Federation tool.");

            monitor.beginTask("Loading partitions...", IProgressMonitor.UNKNOWN);

            //loadConfigFromFile();
            loadConfigFromServer();

            ldapFederation = new LDAPFederation(this);
            nisFederation = new NISFederation(this);

        } finally {
            monitor.done();
        }
    }

    public void loadConfigFromFile() throws Exception {

        File file = new File(project.getWorkDir(), "conf"+File.separator+"federation.xml");

        if (file.exists()) {
            log.debug("Loading "+file);
            FederationReader reader = new FederationReader();
            federationConfig = reader.read(file);

        } else {
            federationConfig = new FederationConfig();
        }
    }

    public void loadConfigFromServer() throws Exception {

        PenroseClient client = project.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
        PartitionClient partitionClient = partitionManagerClient.getPartitionClient("DEFAULT");

        ModuleClient moduleClient = partitionClient.getModuleClient("FederationModule");
        
        if (moduleClient.exists()) {
            federationConfig = (FederationConfig)moduleClient.getAttribute("FederationConfig");

        } else {
            federationConfig = new FederationConfig();

            ModuleConfig moduleConfig = new ModuleConfig();
            moduleConfig.setName("FederationModule");
            moduleConfig.setModuleClass("org.safehaus.penrose.federation.module.FederationModule");

            partitionClient.createModule(moduleConfig);
            partitionClient.store();
        }
    }

    public void importFederationConfig(IProgressMonitor monitor) throws Exception {
        try {
            log.debug("Starting Federation tool.");

            monitor.beginTask("Importing Federation configuration...", IProgressMonitor.UNKNOWN);

            File file = new File(project.getWorkDir(), "conf"+File.separator+"federation.xml");

            monitor.subTask("Loading Federation configuration "+file+"...");
            loadConfigFromFile();
            monitor.worked(1);

            monitor.subTask("Uploading Federation configuration...");
            update();
            monitor.worked(1);

            monitor.subTask("Creating global partition...");
            createGlobalPartition();
            monitor.worked(1);

            for (LDAPRepository ldapRepository : ldapFederation.getRepositories()) {
                monitor.subTask("Creating "+ldapRepository.getName()+" partition...");
                ldapFederation.createPartitions(ldapRepository);
                monitor.worked(1);
            }

            for (NISDomain nisDomain : nisFederation.getRepositories()) {
                monitor.subTask("Creating "+nisDomain.getName()+" partition...");
                nisFederation.createPartitions(nisDomain);
                monitor.worked(1);
            }

        } finally {
            monitor.done();
        }
    }
/*
    public void loadConfigFromDatabase() throws Exception {

        Source globalParameters     = partition.getSource(GLOBAL_PARAMETERS);
        Source repositories         = partition.getSource(REPOSITORIES);
        Source repositoryParameters = partition.getSource(REPOSITORY_PARAMETERS);

        GlobalRepository globalRepository = new GlobalRepository();
        globalRepository.setName(GLOBAL);
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

        federationConfig.addRepository(globalRepository);

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

            Repository repository;

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

            federationConfig.addRepository(repository);
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
*/
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
    public Collection<Repository> getRepositories(String type) {

        Collection<Repository> list = new ArrayList<Repository>();

        for (Repository repository : federationConfig.getRepositories()) {
            if (type.equals(repository.getType())) {
                list.add(repository);
            }
        }
    
        return list;
    }

    public void addRepository(Repository repository) throws Exception {

        federationConfig.addRepository(repository);
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

        Repository repository = federationConfig.removeRepository(name);

        File dir = new File(project.getWorkDir(), "partitions"+File.separator+ repository.getName());
        FileUtil.delete(dir);
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
        return (GlobalRepository)federationConfig.getRepository(GLOBAL);
    }

    public void updateGlobalRepository(IProgressMonitor monitor, GlobalRepository repository) throws Exception {

        try {
            log.debug("Updating global repository.");

            monitor.beginTask("Updating global repository...", IProgressMonitor.UNKNOWN);

            federationConfig.addRepository(repository);

            monitor.subTask("Uploading Federation configuration...");
            update();
            monitor.worked(1);

            monitor.subTask("Updating global partition...");
            createGlobalPartition();
            monitor.worked(1);

            for (LDAPRepository ldapRepository : ldapFederation.getRepositories()) {
                monitor.subTask("Updating "+ldapRepository.getName()+" partition...");
                ldapFederation.createPartitions(ldapRepository);
                monitor.worked(1);
            }

            for (NISDomain nisDomain : nisFederation.getRepositories()) {
                monitor.subTask("Updating "+nisDomain.getName()+" partition...");
                nisFederation.createPartitions(nisDomain);
                monitor.worked(1);
            }

        } finally {
            monitor.done();
        }
    }

    public void removePartition(String name) throws Exception {

        log.debug("Removing partition "+name+".");

        PenroseClient penroseClient = project.getClient();
/*
        penroseClient.stopPartition(name);

        project.removeDirectory("partitions/"+name);

        PartitionConfig partitionConfig = project.getPartitionConfigManager().removePartitionConfig(name);

        File partitionDir = new File(project.getWorkDir(), "partitions"+File.separator+ name);
        FileUtil.delete(partitionDir);
*/
        PenroseClient client = project.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
        partitionManagerClient.removePartition(name);
    }

    public void update() throws Exception {

        PenroseClient client = project.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
        PartitionClient partitionClient = partitionManagerClient.getPartitionClient("DEFAULT");

        ModuleClient moduleClient = partitionClient.getModuleClient("FederationModule");
        moduleClient.setAttribute("FederationConfig", federationConfig);
        moduleClient.invoke("store");
    }

    public FederationConfig getFederationConfig() {
        return federationConfig;
    }

    public void setFederationConfig(FederationConfig federationConfig) {
        this.federationConfig = federationConfig;
    }
}
