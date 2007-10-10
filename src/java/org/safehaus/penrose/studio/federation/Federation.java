package org.safehaus.penrose.studio.federation;

import org.safehaus.penrose.config.PenroseConfig;
import org.safehaus.penrose.naming.PenroseContext;
import org.safehaus.penrose.partition.PartitionConfig;
import org.safehaus.penrose.partition.PartitionFactory;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.partition.Partitions;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.federation.nis.NISFederation;
import org.safehaus.penrose.studio.federation.nis.NISRepository;
import org.safehaus.penrose.studio.federation.ldap.LDAPFederation;
import org.safehaus.penrose.studio.federation.ldap.LDAPRepository;
import org.safehaus.penrose.studio.util.FileUtil;
import org.safehaus.penrose.source.Source;
import org.safehaus.penrose.ldap.*;
import org.apache.log4j.Logger;

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

    private Source globalParameters;
    private Source repositories;
    private Source repositoryParameters;

    private LDAPFederation ldapFederation;
    private NISFederation nisFederation;

    protected Partitions partitions = new Partitions();

    public Federation(Project project) throws Exception {
        this.project = project;

        log.debug("Starting Federation tool.");

        File partitionsDir = new File(project.getWorkDir(), "partitions");
        PenroseConfig penroseConfig = project.getPenroseConfig();
        PenroseContext penroseContext = project.getPenroseContext();

        PartitionConfig partitionConfig = project.getPartitionConfigs().getPartitionConfig(PARTITION);

        PartitionFactory partitionFactory = new PartitionFactory();
        partitionFactory.setPartitionsDir(partitionsDir);
        partitionFactory.setPenroseConfig(penroseConfig);
        partitionFactory.setPenroseContext(penroseContext);

        partition = partitionFactory.createPartition(partitionConfig);

        globalParameters = partition.getSource(GLOBAL_PARAMETERS);
        repositories = partition.getSource(REPOSITORIES);
        repositoryParameters = partition.getSource(REPOSITORY_PARAMETERS);

        ldapFederation = new LDAPFederation(this);
        nisFederation = new NISFederation(this);
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

    public Collection<Repository> getRepositories(String type) throws Exception {

        Collection<Repository> list = new ArrayList<Repository>();

        SearchRequest request = new SearchRequest();
        request.setFilter("(type="+type+")");

        SearchResponse response = new SearchResponse();

        repositories.search(request, response);

        while (response.hasNext()) {
            SearchResult result = response.next();

            Attributes attributes = result.getAttributes();
            String name = (String)attributes.getValue("name");

            Map<String,String> parameters = getRepositoryParameters(name);

            Repository repository;

            if ("LDAP".equals(type)) {
                repository = new LDAPRepository();

            } else if ("NIS".equals(type)) {
                repository = new NISRepository();

            } else {
                throw new Exception("Unknown type: "+type);
            }

            repository.setName(name);
            repository.setType(type);
            repository.setParameters(parameters);

            list.add(repository);
        }

        return list;
    }

    public Map<String,String> getRepositoryParameters(String name) throws Exception {

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

    public void addRepository(Repository repository) throws Exception {

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
    }

    public void removeRepository(String name) throws Exception {

        RDNBuilder rb = new RDNBuilder();
        rb.set("repository", name);
        DN dn = new DN(rb.toRdn());

        repositoryParameters.delete(dn);

        rb = new RDNBuilder();
        rb.set("name", name);
        dn = new DN(rb.toRdn());

        repositories.delete(dn);
    }

    public GlobalRepository getGlobalRepository() throws Exception {

        GlobalRepository repository = new GlobalRepository();

        Map<String,String> parameters = new TreeMap<String,String>();

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

        repository.setParameters(parameters);

        return repository;
    }

    public void setGlobal(GlobalRepository repository) throws Exception {

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
    }

    public void removePartitionConfig(Repository repository) throws Exception {

        String name = repository.getName();
        log.debug("Removing partition "+name+".");

        project.getPartitionConfigs().removePartitionConfig(name);

        File partitionDir = new File(project.getWorkDir(), "partitions"+File.separator+ name);
        FileUtil.delete(partitionDir);
    }

    public void loadPartition(Repository repository) throws Exception {

        String name = repository.getName();
        log.debug("Loading partition "+name+".");

        File partitionsDir = new File(project.getWorkDir(), "partitions");
        PenroseConfig penroseConfig = project.getPenroseConfig();
        PenroseContext penroseContext = project.getPenroseContext();

        PartitionConfig partitionConfig = project.getPartitionConfigs().getPartitionConfig(name);
        if (partitionConfig == null) throw new Exception("Partition "+name+" not found.");

        PartitionFactory partitionFactory = new PartitionFactory();
        partitionFactory.setPartitionsDir(partitionsDir);
        partitionFactory.setPenroseConfig(penroseConfig);
        partitionFactory.setPenroseContext(penroseContext);

        Partition partition = partitionFactory.createPartition(partitionConfig);

        partitions.addPartition(partition);
    }

    public void removePartition(Repository repository) throws Exception {

        String name = repository.getName();
        log.debug("Removing partition config "+name+".");

        partitions.removePartition(name);
    }

    public Partitions getPartitions() {
        return partitions;
    }

    public void setPartitions(Partitions partitions) {
        this.partitions = partitions;
    }

}
