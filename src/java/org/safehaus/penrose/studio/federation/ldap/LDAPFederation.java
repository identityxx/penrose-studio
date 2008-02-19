package org.safehaus.penrose.studio.federation.ldap;

import org.apache.log4j.Logger;
import org.apache.tools.ant.filters.ExpandProperties;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.FilterChain;
import org.eclipse.core.runtime.IProgressMonitor;
import org.safehaus.penrose.federation.repository.GlobalRepository;
import org.safehaus.penrose.federation.repository.LDAPRepository;
import org.safehaus.penrose.federation.repository.NISDomain;
import org.safehaus.penrose.federation.repository.Repository;
import org.safehaus.penrose.management.PenroseClient;
import org.safehaus.penrose.partition.PartitionConfig;
import org.safehaus.penrose.partition.PartitionConfigs;
import org.safehaus.penrose.studio.federation.Federation;
import org.safehaus.penrose.studio.federation.nis.NISFederation;
import org.safehaus.penrose.studio.project.Project;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Endi Sukma Dewata
 */
public class LDAPFederation {

    public final static String TEMPLATE = "federation_ldap";

    public final static String LOCAL_CONNECTION = "LDAP";
    public final static String LOCAL_SOURCE = "Local";

    public final static String GLOBAL_CONNECTION = "Global";
    public final static String GLOBAL_SOURCE = "Global";

    public Logger log = Logger.getLogger(getClass());

    private Project project;
    private Federation federation;

    public LDAPFederation(Federation federation) throws Exception {
        this.federation = federation;
        this.project = federation.getProject();
    }

    public void load(IProgressMonitor monitor) throws Exception {
        log.debug("Starting LDAP Federation tool.");

        Collection<Repository> list = federation.getRepositories("LDAP");

        monitor.beginTask("Loading LDAP repositories...", list.size() == 1 ? IProgressMonitor.UNKNOWN : list.size());

        for (Repository rep : list) {

            if (monitor.isCanceled()) throw new InterruptedException();

            LDAPRepository repository = (LDAPRepository)rep;
            String name = repository.getName();

            monitor.subTask("Loading "+name+"...");

            PartitionConfig partitionConfig = project.getPartitionConfigs().getPartitionConfig(name);

            if (partitionConfig == null) { // create missing partition configs during start

                monitor.subTask("Creating "+name+"...");

                createPartitions(repository);
            }

            monitor.worked(1);
        }
    }

    public PartitionConfig createPartitions(LDAPRepository repository) throws Exception {

        String name = repository.getName();

        log.debug("Creating partition "+name+".");

        File sampleDir = new File(project.getWorkDir(), "samples/"+ TEMPLATE);
        if (!sampleDir.exists()) project.download("samples/"+ TEMPLATE);

        File partitionDir = new File(project.getWorkDir(), "partitions"+File.separator+ name);

        String ldapUrl = repository.getUrl();
        String ldapUser = repository.getUser();
        String ldapPassword = repository.getPassword();
        String ldapSuffix = repository.getSuffix();

        GlobalRepository globalRepository = federation.getGlobalRepository();

        String globalUrl = globalRepository.getUrl();
        String globalUser = globalRepository.getUser();
        String globalPassword = globalRepository.getPassword();
        String globalSuffix = globalRepository.getSuffix();

        log.debug("Replacing parameter values.");

        org.apache.tools.ant.Project antProject = new org.apache.tools.ant.Project();

        antProject.setProperty("DOMAIN",          name);

        antProject.setProperty("LDAP_URL",        ldapUrl);
        antProject.setProperty("LDAP_USER",       ldapUser);
        antProject.setProperty("LDAP_PASSWORD",   ldapPassword);
        antProject.setProperty("LDAP_SUFFIX",     ldapSuffix);

        antProject.setProperty("GLOBAL_URL",      globalUrl);
        antProject.setProperty("GLOBAL_USER",     globalUser);
        antProject.setProperty("GLOBAL_PASSWORD", globalPassword);
        antProject.setProperty("GLOBAL_SUFFIX",   globalSuffix);

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

/*
        PartitionConfigs partitionConfigs = project.getPartitionConfigs();
        PartitionConfig partitionConfig = (PartitionConfig)partitionConfigs.load(sampleDir).clone();
        partitionConfig.setName(partitionName);

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Local Connection
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        ConnectionConfig localConnection = partitionConfig.getConnectionConfigs().getConnectionConfig(LOCAL_CONNECTION);
        localConnection.setParameter(Context.PROVIDER_URL, url);
        localConnection.setParameter(Context.SECURITY_PRINCIPAL, user);
        localConnection.setParameter(Context.SECURITY_CREDENTIALS, password);

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Local Source
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        SourceConfig sourceConfig = partitionConfig.getSourceConfigs().getSourceConfig(LOCAL_SOURCE);
        sourceConfig.setParameter("baseDn", suffix);

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Global Connection
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        ConnectionConfig globalConnection = partitionConfig.getConnectionConfigs().getConnectionConfig(GLOBAL_CONNECTION);
        globalConnection.setParameter(Context.PROVIDER_URL, globalUrl);
        globalConnection.setParameter(Context.SECURITY_PRINCIPAL, globalUser);
        globalConnection.setParameter(Context.SECURITY_CREDENTIALS, globalPassword);

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Global Source
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        SourceConfig globalSource = partitionConfig.getSourceConfigs().getSourceConfig(GLOBAL_SOURCE);
        globalSource.setParameter("baseDn", globalSuffix);
        partitionConfigs.store(partitionDir, partitionConfig);
        partitionConfigs.addPartitionConfig(partitionConfig);
*/
        project.upload("partitions/"+name);

        PenroseClient penroseClient = project.getClient();
        penroseClient.startPartition(name);

        return partitionConfig;
    }

    public PartitionConfig getPartitionConfig(String name) throws Exception {
        return federation.getPartitionConfig(name);
    }

    public void removePartitions(LDAPRepository repository) throws Exception {
        federation.removePartition(repository.getName());
    }

    public LDAPRepository getRepository(String name) {
        return (LDAPRepository)federation.getFederationConfig().getRepository(name);
    }

    public Collection<LDAPRepository> getRepositories() {
        Collection<LDAPRepository> list = new ArrayList<LDAPRepository>();
        for (Repository repository : federation.getRepositories("LDAP")) {
            list.add((LDAPRepository)repository);
        }
        return list;
    }

    public void addRepository(LDAPRepository repository) throws Exception {
        federation.addRepository(repository);
        federation.update();
    }

    public void updateRepository(LDAPRepository repository) throws Exception {

        removePartitions(repository);

        federation.removeRepository(repository.getName());
        federation.addRepository(repository);
        federation.update();

        NISFederation nisFederation = federation.getNisFederation();
        for (NISDomain nisDomain : nisFederation.getRepositories()) {
            nisFederation.createPartitions(nisDomain);
        }

        createPartitions(repository);
    }

    public void removeRepository(String name) throws Exception {
        federation.removeRepository(name);
        federation.update();
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Federation getFederation() {
        return federation;
    }

    public void setFederation(Federation federation) {
        this.federation = federation;
    }
}
