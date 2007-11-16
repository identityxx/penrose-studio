package org.safehaus.penrose.studio.federation.ldap;

import org.apache.log4j.Logger;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.FilterChain;
import org.apache.tools.ant.filters.ExpandProperties;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.federation.Federation;
import org.safehaus.penrose.studio.federation.Repository;
import org.safehaus.penrose.studio.federation.GlobalRepository;
import org.safehaus.penrose.studio.federation.event.FederationEventListener;
import org.safehaus.penrose.studio.federation.event.FederationEvent;
import org.safehaus.penrose.studio.util.FileUtil;
import org.safehaus.penrose.partition.*;
import org.safehaus.penrose.management.PenroseClient;
import org.eclipse.core.runtime.IProgressMonitor;

import java.util.Map;
import java.util.TreeMap;
import java.util.Collection;
import java.util.ArrayList;
import java.io.File;

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

    private Map<String,LDAPRepository> repositories = new TreeMap<String,LDAPRepository>();

    protected Collection<FederationEventListener> listeners = new ArrayList<FederationEventListener>();

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

            repositories.put(name, repository);

            PartitionConfig partitionConfig = project.getPartitionConfigs().getPartitionConfig(name);

            if (partitionConfig == null) { // create missing partition configs during start

                monitor.subTask("Creating "+name+"...");

                partitionConfig = createPartitionConfig(repository);
                project.upload("partitions/"+repository.getName());

                PenroseClient penroseClient = project.getClient();
                penroseClient.startPartition(repository.getName());
            }

            monitor.worked(1);
        }
    }

    public PartitionConfig createPartitionConfig(LDAPRepository repository) throws Exception {

        String name = repository.getName();
        String partitionName = name;

        log.debug("Creating partition "+partitionName+".");

        File sampleDir = new File(project.getWorkDir(), "samples/"+ TEMPLATE);
        if (!sampleDir.exists()) project.download("samples/"+ TEMPLATE);

        File partitionDir = new File(project.getWorkDir(), "partitions"+File.separator+ partitionName);

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
        return partitionConfig;
    }

    public void removePartition(LDAPRepository repository) throws Exception {
        federation.removePartition(repository);
    }

    public PartitionConfig getPartitionConfig(String name) throws Exception {
        return federation.getPartitionConfig(name);
    }

    public PartitionConfig removePartitionConfig(String name) throws Exception {
        return federation.removePartitionConfig(name);
    }

    public LDAPRepository getRepository(String name) {
        return repositories.get(name);
    }

    public Collection<LDAPRepository> getRepositories() {
        return repositories.values();
    }

    public void addRepository(LDAPRepository repository) throws Exception {
        federation.addRepository(repository);
        repositories.put(repository.getName(), repository);

        FederationEvent event = new FederationEvent();
        event.setRepository(repository);

        for (FederationEventListener listener : listeners) {
            listener.repositoryAdded(event);
        }
    }

    public void updateRepository(LDAPRepository repository) throws Exception {

        federation.removeRepository(repository.getName());
        federation.addRepository(repository);

        FederationEvent event = new FederationEvent();
        event.setRepository(repository);

        for (FederationEventListener listener : listeners) {
            listener.repositoryModified(event);
        }
    }

    public void removeRepository(String name) throws Exception {
        LDAPRepository repository = repositories.remove(name);

        File dir = new File(project.getWorkDir(), "partitions"+File.separator+ repository.getName());
        FileUtil.delete(dir);

        federation.removeRepository(name);

        FederationEvent event = new FederationEvent();
        event.setRepository(repository);

        for (FederationEventListener listener : listeners) {
            listener.repositoryRemoved(event);
        }
    }

    public void setRepositories(Map<String, LDAPRepository> repositories) {
        this.repositories = repositories;
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

    public void addListener(FederationEventListener listener) {
        listeners.add(listener);
    }

    public void removeListener(FederationEventListener listener) {
        listeners.remove(listener);
    }
}
