package org.safehaus.penrose.studio.nis;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.object.ObjectsView;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.util.FileUtil;
import org.safehaus.penrose.ldap.*;
import org.safehaus.penrose.source.Source;
import org.safehaus.penrose.nis.NISDomain;
import org.safehaus.penrose.partition.*;
import org.safehaus.penrose.naming.PenroseContext;
import org.safehaus.penrose.config.PenroseConfig;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.apache.log4j.Logger;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.FilterChain;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.filters.ExpandProperties;

import java.util.Collection;
import java.util.ArrayList;
import java.io.File;

/**
 * @author Endi S. Dewata
 */
public class NISNode extends Node {

    Logger log = Logger.getLogger(getClass());

    ObjectsView view;
    Partition partition;

    public NISNode(ObjectsView view, String name, String type, Image image, Object object, Object parent) {
        super(name, type, image, object, parent);
        this.view = view;
    }

    public void showMenu(IMenuManager manager) throws Exception {

        manager.add(new Action("New Domain...") {
            public void run() {
                try {
                    newDomain();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(new Action("Refresh") {
            public void run() {
                try {
                    refresh();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });
    }

    public void open() throws Exception {
        if (partition == null) start();
    }

    public void start() throws Exception {
        PenroseStudio penroseStudio = PenroseStudio.getInstance();

        PenroseConfig penroseConfig = penroseStudio.getPenroseConfig();
        PenroseContext penroseContext = penroseStudio.getPenroseContext();

        PartitionConfigs partitionConfigs = penroseStudio.getPartitionConfigs();
        Partitions partitions = penroseStudio.getPartitions();

        File workDir = penroseStudio.getWorkDir();

        String name = "nis";
        File partitionDir = new File(workDir, "partitions"+File.separator+name);

        PartitionConfig partitionConfig = partitionConfigs.getPartitionConfig(name);

        PartitionContext partitionContext = new PartitionContext();
        partitionContext.setPath(partitionDir);
        partitionContext.setPenroseConfig(penroseConfig);
        partitionContext.setPenroseContext(penroseContext);

        partition = partitions.init(partitionConfig, partitionContext);
    }

    public boolean hasChildren() throws Exception {

        if (partition == null) return true;

        Source domains = partition.getSource("penrose_domains");
        if (domains == null) {
            log.debug("Source domains is missing.");
            return false;
        }

        SearchRequest request = new SearchRequest();
        SearchResponse<SearchResult> response = new SearchResponse<SearchResult>();

        domains.search(request, response);

        return response.getTotalCount() > 0;
    }

    public Collection<Node> getChildren() throws Exception {

        if (partition == null) start();

        Collection<Node> children = new ArrayList<Node>();

        Source domains = partition.getSource("penrose_domains");
        if (domains == null) return null;

        SearchRequest request = new SearchRequest();
        SearchResponse<SearchResult> response = new SearchResponse<SearchResult>();

        domains.search(request, response);

        while (response.hasNext()) {
            SearchResult result = response.next();
            Attributes attributes = result.getAttributes();
            
            String name = (String)attributes.getValue("name");
            String partitionName = (String)attributes.getValue("partition");
            String server = (String)attributes.getValue("server");
            String suffix = (String)attributes.getValue("suffix");

            NISDomain domain = new NISDomain();
            domain.setName(name);
            domain.setPartition(partitionName);
            domain.setServer(server);
            domain.setSuffix(suffix);

            NISDomainNode node = new NISDomainNode(
                    view,
                    partitionName,
                    ObjectsView.ENTRY,
                    PenrosePlugin.getImage(PenroseImage.NODE),
                    domain,
                    this
            );

            children.add(node);
        }

        return children;
    }

    public void newDomain() throws Exception {

        if (partition == null) start();

        Source domains = partition.getSource("penrose_domains");

        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        NISDomainDialog dialog = new NISDomainDialog(shell, SWT.NONE);

        dialog.open();

        int action = dialog.getAction();
        if (action == NISUserDialog.CANCEL) return;

        String domainName = dialog.getName();
        String partitionName = dialog.getPartition();
        String server = dialog.getServer();
        String suffix = dialog.getSuffix();
        
        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        File workDir = penroseStudio.getWorkDir();

        File oldDir = new File(workDir, "partitions"+File.separator+"nis_cache");
        File newDir = new File(workDir, "partitions"+File.separator+ partitionName);
        FileUtil.copy(oldDir, newDir);

        customizePartition(newDir, domainName, partitionName, server, suffix);

        PartitionConfigs partitionConfigs = penroseStudio.getPartitionConfigs();
        PartitionConfig newPartition = partitionConfigs.load(newDir);
        partitionConfigs.addPartitionConfig(newPartition);

        RDNBuilder rb = new RDNBuilder();
        rb.set("name", domainName);
        DN dn = new DN(rb.toRdn());

        Attributes attributes = new Attributes();
        attributes.setValue("name", domainName);
        attributes.setValue("partition", partitionName);
        attributes.setValue("server", server);
        attributes.setValue("suffix", suffix);

        domains.add(dn, attributes);

        penroseStudio.notifyChangeListeners();
    }

    public void customizePartition(
            File newDir,
            String domainName,
            String partitionName,
            String server,
            String suffix
    ) throws Exception {

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
    }

    public void refresh() throws Exception {
        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }
}
