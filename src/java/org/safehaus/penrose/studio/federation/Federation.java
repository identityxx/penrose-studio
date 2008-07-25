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

    public final static String LINKING_LOCAL_ATTRIBUTE  = "linkingLocalAttribute";
    public final static String LINKING_GLOBAL_ATTRIBUTE = "linkingGlobalAttribute";

    public final static String IMPORT_MAPPING_NAME   = "importMappingName";
    public final static String IMPORT_MAPPING_PREFIX = "importMappingPrefix";

    private Project project;

    private LDAPFederation ldapFederation;
    private NISFederation nisFederation;

    private FederationConfig federationConfig;

    public Federation(Project project) throws Exception {
        this.project = project;
    }

    public void createPartition() throws Exception {

        String partitionName = Federation.FEDERATION;

        log.debug("Creating partition "+partitionName +".");

        File workDir = project.getWorkDir();

        File sampleDir = new File(workDir, "samples/"+partitionName);
        if (!sampleDir.exists()) project.download("samples/"+partitionName);

        File partitionDir = new File(workDir, "partitions/"+partitionName);

        org.apache.tools.ant.Project antProject = new org.apache.tools.ant.Project();

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

        PartitionConfig partitionConfig = new PartitionConfig(partitionName);
        partitionConfig.load(partitionDir);

        PenroseClient client = project.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
        partitionManagerClient.createPartition(partitionConfig);

        log.debug("Initializing partition "+Federation.FEDERATION +".");
    }

    public void createGlobalPartition() throws Exception {

        GlobalRepository globalRepository = getGlobalRepository();
        if (globalRepository == null) return;

        String templateName = FEDERATION+"_"+GLOBAL;
        String partitionName = GLOBAL;

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
        
        antProject.setProperty("SUFFIX",        suffix);

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

        PartitionConfig partitionConfig = new PartitionConfig(partitionName);
        partitionConfig.load(partitionDir);

        PenroseClient client = project.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
        partitionManagerClient.createPartition(partitionConfig);
    }

    public void removeGlobalPartition() throws Exception {
        removePartition(GLOBAL);
    }

    public GlobalRepository getGlobalRepository() throws Exception {
        return (GlobalRepository)federationConfig.getRepository(GLOBAL);
    }

    public void updateGlobalRepository(GlobalRepository repository) throws Exception {

        log.debug("Updating global repository.");

        if (getGlobalRepository() != null) {
            removeGlobalPartition();
        }

        federationConfig.addRepository(repository);

        update();

        createGlobalPartition();
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
    }

    public void removeRepository(String name) throws Exception {

        Repository repository = federationConfig.removeRepository(name);

        File dir = new File(project.getWorkDir(), "partitions"+File.separator+ repository.getName());
        FileUtil.delete(dir);
    }

    public void removePartition(String name) throws Exception {

        log.debug("Removing partition "+name+".");

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
