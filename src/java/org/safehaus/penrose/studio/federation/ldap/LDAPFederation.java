package org.safehaus.penrose.studio.federation.ldap;

import org.apache.log4j.Logger;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.federation.Federation;
import org.safehaus.penrose.studio.federation.Repository;
import org.safehaus.penrose.studio.federation.GlobalRepository;
import org.safehaus.penrose.studio.federation.event.FederationEventListener;
import org.safehaus.penrose.studio.federation.event.FederationEvent;
import org.safehaus.penrose.studio.util.FileUtil;
import org.safehaus.penrose.partition.*;
import org.safehaus.penrose.source.SourceConfig;
import org.safehaus.penrose.management.PenroseClient;
import org.safehaus.penrose.connection.ConnectionConfig;
import org.eclipse.core.runtime.IProgressMonitor;

import javax.naming.Context;
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
    private Partition partition;
    private Federation federation;

    private Map<String,LDAPRepository> repositories = new TreeMap<String,LDAPRepository>();

    protected Collection<FederationEventListener> listeners = new ArrayList<FederationEventListener>();

    public LDAPFederation(Federation federation) throws Exception {
        this.federation = federation;
        this.partition = federation.getPartition();
        this.project = federation.getProject();
    }

    public void load(IProgressMonitor monitor) throws Exception {
        log.debug("Starting LDAP Federation tool.");

        for (Repository rep : federation.getRepositories("LDAP")) {

            if (monitor.isCanceled()) break;

            LDAPRepository repository = (LDAPRepository)rep;
            String name = repository.getName();

            repositories.put(name, repository);

            PartitionConfig partitionConfig = project.getPartitionConfigs().getPartitionConfig(name);

            if (partitionConfig == null) { // create missing partition configs during start

                monitor.subTask("Creating "+name+"...");

                partitionConfig = createPartitionConfig(repository);
                project.upload("partitions/"+repository.getName());

                PenroseClient penroseClient = project.getClient();
                penroseClient.startPartition(repository.getName());
            }

            monitor.subTask("Loading "+name+"...");

            loadPartition(partitionConfig);

            monitor.worked(1);
        }
    }

    public PartitionConfig createPartitionConfig(LDAPRepository repository) throws Exception {

        String name = repository.getName();
        log.debug("Creating partition "+name+".");

        File sampleDir = new File(project.getWorkDir(), "samples/"+ TEMPLATE);

        if (!sampleDir.exists()) {
            project.download("samples/"+ TEMPLATE);
        }

        PartitionConfigs partitionConfigs = project.getPartitionConfigs();
        PartitionConfig partitionConfig = (PartitionConfig)partitionConfigs.load(sampleDir).clone();
        partitionConfig.setName(name);
        
        File partitionDir = new File(project.getWorkDir(), "partitions"+File.separator+ name);

        log.debug("Replacing parameter values.");

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Local Connection
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        ConnectionConfig localConnection = partitionConfig.getConnectionConfigs().getConnectionConfig(LOCAL_CONNECTION);

        String url = repository.getUrl();
        localConnection.setParameter(Context.PROVIDER_URL, url);

        String user = repository.getUser();
        localConnection.setParameter(Context.SECURITY_PRINCIPAL, user);

        String password = repository.getPassword();
        localConnection.setParameter(Context.SECURITY_CREDENTIALS, password);

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Local Source
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        SourceConfig sourceConfig = partitionConfig.getSourceConfigs().getSourceConfig(LOCAL_SOURCE);

        String suffix = repository.getSuffix();
        sourceConfig.setParameter("baseDn", suffix);

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Global Connection
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        ConnectionConfig globalConnection = partitionConfig.getConnectionConfigs().getConnectionConfig(GLOBAL_CONNECTION);

        GlobalRepository globalRepository = federation.getGlobalRepository();

        String globalUrl = globalRepository.getUrl();
        globalConnection.setParameter(Context.PROVIDER_URL, globalUrl);

        String globalUser = globalRepository.getUser();
        globalConnection.setParameter(Context.SECURITY_PRINCIPAL, globalUser);

        String globalPassword = globalRepository.getPassword();
        globalConnection.setParameter(Context.SECURITY_CREDENTIALS, globalPassword);

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Global Source
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        SourceConfig globalSource = partitionConfig.getSourceConfigs().getSourceConfig(GLOBAL_SOURCE);

        String globalSuffix = globalRepository.getSuffix();
        globalSource.setParameter("baseDn", globalSuffix);

        partitionConfigs.store(partitionDir, partitionConfig);

        partitionConfigs.addPartitionConfig(partitionConfig);

        return partitionConfig;
    }

    public void loadPartition(PartitionConfig partitionConfig) throws Exception {
        federation.loadPartition(partitionConfig);
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

    public Partitions getPartitions() {
        return federation.getPartitions();
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Partition getPartition() {
        return partition;
    }

    public void setPartition(Partition partition) {
        this.partition = partition;
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
