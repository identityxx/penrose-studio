package org.safehaus.penrose.studio.project;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.object.ObjectsView;
import org.safehaus.penrose.studio.partition.PartitionsNode;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.preview.PreviewEditorInput;
import org.safehaus.penrose.studio.preview.PreviewEditor;
import org.safehaus.penrose.studio.validation.ValidationView;
import org.safehaus.penrose.studio.util.FileUtil;
import org.safehaus.penrose.studio.browser.BrowserEditorInput;
import org.safehaus.penrose.studio.browser.BrowserEditor;
import org.safehaus.penrose.studio.properties.SystemPropertiesNode;
import org.safehaus.penrose.studio.user.AdministratorNode;
import org.safehaus.penrose.studio.connector.ConnectorNode;
import org.safehaus.penrose.studio.engine.EnginesNode;
import org.safehaus.penrose.studio.logging.LoggingNode;
import org.safehaus.penrose.studio.cache.CachesNode;
import org.safehaus.penrose.studio.service.ServicesNode;
import org.safehaus.penrose.studio.schema.SchemasNode;
import org.safehaus.penrose.config.PenroseConfig;
import org.safehaus.penrose.config.PenroseConfigReader;
import org.safehaus.penrose.config.PenroseConfigWriter;
import org.safehaus.penrose.service.ServiceConfig;
import org.safehaus.penrose.management.PenroseClient;
import org.safehaus.penrose.schema.SchemaManager;
import org.safehaus.penrose.schema.SchemaConfig;
import org.safehaus.penrose.partition.*;
import org.safehaus.penrose.log4j.Log4jConfigReader;
import org.safehaus.penrose.log4j.Log4jConfig;
import org.safehaus.penrose.log4j.Log4jConfigWriter;
import org.safehaus.penrose.user.UserConfig;
import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.SWT;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchActionConstants;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;

/**
 * @author Endi S. Dewata
 */
public class ProjectNode extends Node {

    Logger log = Logger.getLogger(getClass());

    public final static String LDAP_PORT             = "ldapPort";
    public final static int DEFAULT_LDAP_PORT        = 10389;

    ObjectsView view;

    private PenroseClient client;
    private PenroseConfig penroseConfig;
    private SchemaManager schemaManager;
    private PartitionManager partitionManager;
    private Log4jConfig log4jConfig;

    private Collection children = new ArrayList();

    private PartitionsNode partitionsNode;
    private SchemasNode schemasNode;
    private ServicesNode servicesNode;
    private CachesNode cachesNode;
    private LoggingNode loggingNode;

    public ProjectNode(ObjectsView view, String name, String type, Image image, Object object, Node parent) {
        super(name, type, image, object, parent);
        this.view = view;
    }

    public void showMenu(IMenuManager manager) {

        manager.add(new Action("Open", PenrosePlugin.getImageDescriptor(PenroseImage.CONNECT)) {
            public void run() {
                try {
                    open();
                } catch (Exception e) {
                    log.debug(e.getMessage(), e);

                    IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                    Shell shell = window.getShell();

                    MessageDialog.openError(
                            shell,
                            "ERROR",
                            "Failed opening "+((Project)getObject()).getName()+".\n"+
                                    "See penrose-studio-log.txt for details."
                    );
                }
            }
        });

        manager.add(new Action("Close") {
            public void run() {
                try {
                    close();
                } catch (Exception e) {
                    log.debug(e.getMessage(), e);

                    IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                    Shell shell = window.getShell();

                    MessageDialog.openError(
                            shell,
                            "ERROR",
                            "Failed closing "+((Project)getObject()).getName()+".\n"+
                                    "See penrose-studio-log.txt for details."
                    );
                }
            }
        });

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(new Action("Save") {
            public void run() {
                try {
                    save();
                } catch (Exception e) {
                    log.debug(e.getMessage(), e);

                    IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                    Shell shell = window.getShell();

                    MessageDialog.openError(
                            shell,
                            "ERROR",
                            "Failed saving "+((Project)getObject()).getName()+".\n"+
                                    "See penrose-studio-log.txt for details."
                    );
                }
            }
        });

        manager.add(new Action("Upload") {
            public void run() {
                try {
                    upload();
                } catch (Exception e) {
                    log.debug(e.getMessage(), e);

                    IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                    Shell shell = window.getShell();

                    MessageDialog.openError(
                            shell,
                            "ERROR",
                            "Failed uploading "+((Project)getObject()).getName()+".\n"+
                                    "See penrose-studio-log.txt for details."
                    );
                }
            }
        });

        manager.add(new Action("Restart") {
            public void run() {
                try {
                    restart();
                } catch (Exception e) {
                    log.debug(e.getMessage(), e);

                    IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                    Shell shell = window.getShell();

                    MessageDialog.openError(
                            shell,
                            "ERROR",
                            "Failed restarting "+((Project)getObject()).getName()+".\n"+
                                    "See penrose-studio-log.txt for details."
                    );
                }
            }
        });

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(new Action("Preview") {
            public void run() {
                try {
                    preview();
                } catch (Exception e) {
                    log.debug(e.getMessage(), e);

                    IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                    Shell shell = window.getShell();

                    MessageDialog.openError(
                            shell,
                            "ERROR",
                            "Failed previewing "+((Project)getObject()).getName()+".\n"+
                                    "See penrose-studio-log.txt for details."
                    );
                }
            }
        });

        manager.add(new Action("Browse") {
            public void run() {
                try {
                    browse();
                } catch (Exception e) {
                    log.debug(e.getMessage(), e);

                    IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                    Shell shell = window.getShell();

                    MessageDialog.openError(
                            shell,
                            "ERROR",
                            "Failed browsing "+((Project)getObject()).getName()+".\n"+
                                    "See penrose-studio-log.txt for details."
                    );
                }
            }
        });

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(new Action("Edit") {
            public void run() {
                try {
                    edit();
                } catch (Exception e) {
                    log.debug(e.getMessage(), e);

                    IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                    Shell shell = window.getShell();

                    MessageDialog.openError(
                            shell,
                            "ERROR",
                            "Failed editing "+((Project)getObject()).getName()+".\n"+
                                    "See penrose-studio-log.txt for details."
                    );
                }
            }
        });

/*
        manager.add(new Action("Copy") {
            public void run() {
                try {
                    copy();
                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                }
            }
        });

        manager.add(new Action("Paste") {
            public void run() {
                try {
                    paste();
                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                }
            }
        });
*/

        manager.add(new Action("Delete", PenrosePlugin.getImageDescriptor(PenroseImage.DELETE)) {
            public void run() {
                try {
                    remove();
                } catch (Exception e) {
                    log.debug(e.getMessage(), e);

                    IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                    Shell shell = window.getShell();

                    MessageDialog.openError(
                            shell,
                            "ERROR",
                            "Failed deleting "+((Project)getObject()).getName()+".\n"+
                                    "See penrose-studio-log.txt for details."
                    );
                }
            }
        });
    }

    public void open() throws Exception {

        Project project = getProject();

        log.debug("-------------------------------------------------------------------------------------");
        log.debug("Opening project "+project.getName());

        client = new PenroseClient(
                project.getType(),
                project.getHost(),
                project.getPort(),
                project.getUsername(),
                project.getPassword()
        );

        client.connect();

        String dir = getWorkDir();
        FileUtil.delete(dir);

        log.debug("Downloading configuration to "+dir);

        downloadFolder("conf", dir);
        downloadFolder("schema", dir);
        downloadFolder("partitions", dir);

        log.debug("Opening project from "+dir);

        PenroseConfigReader penroseConfigReader = new PenroseConfigReader(dir +"/conf/server.xml");
        penroseConfig = penroseConfigReader.read();

        initSystemProperties();
        initSchemaManager(dir);
        loadPartitions(dir);
        validate();

        loadLoggingConfig(dir);
        //loadLoggers();

        partitionsNode = new PartitionsNode(
                view,
                ObjectsView.PARTITIONS,
                ObjectsView.PARTITIONS,
                PenrosePlugin.getImage(PenroseImage.FOLDER),
                ObjectsView.PARTITIONS,
                this);
        children.add(partitionsNode);

        schemasNode = new SchemasNode(
                view,
                ObjectsView.SCHEMAS,
                ObjectsView.SCHEMAS,
                PenrosePlugin.getImage(PenroseImage.FOLDER),
                ObjectsView.SCHEMAS,
                this);
        children.add(schemasNode);

        servicesNode = new ServicesNode(
                view,
                ObjectsView.SERVICES,
                ObjectsView.SERVICES,
                PenrosePlugin.getImage(PenroseImage.FOLDER),
                ObjectsView.SERVICES,
                this);
        children.add(servicesNode);

        cachesNode = new CachesNode(
                view,
                ObjectsView.CACHES,
                ObjectsView.CACHES,
                PenrosePlugin.getImage(PenroseImage.FOLDER),
                ObjectsView.CACHES,
                this);
        children.add(cachesNode);

        loggingNode = new LoggingNode(
                view,
                ObjectsView.LOGGING,
                ObjectsView.LOGGING,
                PenrosePlugin.getImage(PenroseImage.FOLDER),
                ObjectsView.LOGGING,
                this);
        children.add(loggingNode);

        children.add(new EnginesNode(
                view,
                ObjectsView.ENGINES,
                ObjectsView.ENGINES,
                PenrosePlugin.getImage(PenroseImage.FOLDER),
                ObjectsView.ENGINES,
                this
        ));

        children.add(new ConnectorNode(
                view,
                ObjectsView.CONNECTOR,
                ObjectsView.CONNECTOR,
                PenrosePlugin.getImage(PenroseImage.CONNECTOR),
                ObjectsView.CONNECTOR,
                this
        ));

        children.add(new AdministratorNode(
                view,
                ObjectsView.ADMINISTRATOR,
                ObjectsView.ADMINISTRATOR,
                PenrosePlugin.getImage(PenroseImage.ADMINISTRATOR),
                ObjectsView.ADMINISTRATOR,
                this
        ));

        children.add(new SystemPropertiesNode(
                view,
                ObjectsView.SYSTEM_PROPERTIES,
                ObjectsView.SYSTEM_PROPERTIES,
                PenrosePlugin.getImage(PenroseImage.SYSTEM_PROPERTIES),
                ObjectsView.SYSTEM_PROPERTIES,
                this
        ));

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();

        log.debug("Project opened.");
    }

    public void downloadFolder(String remotePath, String localDir) throws Exception {
        File outputDir = new File(localDir);
        outputDir.mkdirs();

        Collection filenames = client.listFiles(remotePath);
        for (Iterator i = filenames.iterator(); i.hasNext(); ) {
            String filename = (String)i.next();
            download(filename, outputDir);
        }
    }

    public void download(String filename, File localDir) throws Exception {
        log.debug("Downloading "+filename);

        byte content[] = client.download(filename);
        if (content == null) return;

        File file = new File(localDir, filename);
        file.getParentFile().mkdirs();

        FileOutputStream out = new FileOutputStream(file);
        out.write(content);

        out.close();
    }

    public void initSystemProperties() throws Exception {
        for (Iterator i=penroseConfig.getSystemPropertyNames().iterator(); i.hasNext(); ) {
            String name = (String)i.next();
            String value = penroseConfig.getSystemProperty(name);

            System.setProperty(name, value);
        }
    }

    public void initSchemaManager(String dir) throws Exception {

        schemaManager = new SchemaManager();

        for (Iterator i=penroseConfig.getSchemaConfigs().iterator(); i.hasNext(); ) {
            SchemaConfig schemaConfig = (SchemaConfig)i.next();
            schemaManager.load(dir, schemaConfig);
        }
    }

    public void loadPartitions(String dir) throws Exception {

        partitionManager = new PartitionManager();
        partitionManager.setSchemaManager(schemaManager);

        partitionManager.load(dir, penroseConfig.getPartitionConfigs());
    }

    public void validate() throws Exception {
        if (client == null) return;

        PartitionValidator partitionValidator = new PartitionValidator();
        partitionValidator.setPenroseConfig(penroseConfig);
        partitionValidator.setSchemaManager(schemaManager);

        Collection results = new ArrayList();

        for (Iterator i=penroseConfig.getPartitionConfigs().iterator(); i.hasNext(); ) {
            PartitionConfig partitionConfig = (PartitionConfig)i.next();

            Partition partition = partitionManager.getPartition(partitionConfig.getName());
            Collection list = partitionValidator.validate(partition);

            for (Iterator j=list.iterator(); j.hasNext(); ) {
                PartitionValidationResult resultPartition = (PartitionValidationResult)j.next();

                if (resultPartition.getType().equals(PartitionValidationResult.ERROR)) {
                    log.error("ERROR: "+resultPartition.getMessage()+" ["+resultPartition.getSource()+"]");
                } else {
                    log.warn("WARNING: "+resultPartition.getMessage()+" ["+resultPartition.getSource()+"]");
                }
            }

            results.addAll(list);
        }

        IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        if (!results.isEmpty()) {
            activePage.showView(ValidationView.class.getName());
        }

        ValidationView view = (ValidationView)activePage.findView(ValidationView.class.getName());
        if (view != null) {
            view.setResults(results);
            view.refresh();
        }
    }

    public void loadLoggingConfig(String dir) throws Exception {
        try {
            Log4jConfigReader reader = new Log4jConfigReader(new File(dir+"/conf/log4j.xml"));
            log4jConfig = reader.read();
        } catch (Exception e) {
            log.error("ERROR: "+e.getMessage(), e);
            log4jConfig = new Log4jConfig();
        }
    }

    public void save() throws Exception {

        File workFolder = new File(getWorkDir());
        File tmpFolder = new File(getWorkDir()+".tmp");
        File backupFolder = new File(getWorkDir()+".bak");

        FileUtil.delete(tmpFolder);
        FileUtil.copyFolder(workFolder, tmpFolder);

        String dir = tmpFolder.getAbsolutePath();

        File file = new File(dir);
        file.mkdirs();

        log.debug("-------------------------------------------------------------------------------------");
        log.debug("Saving configuration to "+dir);

        PenroseConfigWriter serverConfigWriter = new PenroseConfigWriter(dir+"/conf/server.xml");
        serverConfigWriter.write(penroseConfig);

        saveLoggingConfig(dir);

        partitionManager.store(dir, penroseConfig.getPartitionConfigs());

        FileUtil.delete(backupFolder);
        workFolder.renameTo(backupFolder);
        tmpFolder.renameTo(workFolder);

        validate();
    }

    public void saveLoggingConfig(String dir) throws Exception {
        Log4jConfigWriter writer = new Log4jConfigWriter(dir+"/conf/log4j.xml");
        writer.write(log4jConfig);
    }

    public void upload() throws Exception {

        String dir = getWorkDir();

        log.debug("Uploading configuration from "+dir);

        uploadFolder(dir);
    }

    public void uploadFolder(String localDir) throws Exception {
        File inputDir = new File(localDir);

        Collection files = listFiles(localDir);
        for (Iterator i=files.iterator(); i.hasNext(); ) {
            String filename = (String)i.next();
            upload(inputDir, filename);
        }
    }

    public void upload(File localDir, String filename) throws Exception {
        log.debug("Uploading "+filename);

        File file = new File(localDir, filename);
        FileInputStream in = new FileInputStream(file);

        byte content[] = new byte[(int)file.length()];
        in.read(content);

        in.close();

        client.upload(filename, content);
    }

    public Collection listFiles(String directory) throws Exception {
        Collection results = new ArrayList();
        listFiles(null, new File(directory), results);
        return results;
    }

    public void listFiles(String prefix, File directory, Collection results) throws Exception {
        File children[] = directory.listFiles();
        for (int i=0; i<children.length; i++) {
            if (children[i].isDirectory()) {
                listFiles((prefix == null ? "" : prefix+"/")+children[i].getName(), children[i], results);
            } else {
                results.add((prefix == null ? "" : prefix+"/")+children[i].getName());
            }
        }
    }

    public void restart() throws Exception {
        client.restart();
    }

    public void close() throws Exception {

        client.close();

        children.clear();

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();

        log.debug("Project closed.");
    }

    public void edit() throws Exception {

        Project project = getProject();

        String oldProjectName = project.getName();
        System.out.println("Editing project: "+oldProjectName);

        ProjectEditorDialog dialog = new ProjectEditorDialog(view.getSite().getShell(), SWT.NONE);
        dialog.setProject(project);
        dialog.open();

        if (dialog.getAction() == ProjectEditorDialog.CANCEL) return;

        PenroseStudio penroseStudio = PenroseStudio.getInstance();

        if (!oldProjectName.equals(project.getName())) {
            penroseStudio.getApplicationConfig().removeProject(oldProjectName);
            penroseStudio.getApplicationConfig().addProject(project);
        }

        penroseStudio.saveApplicationConfig();

        log.debug("Project updated.");
    }

    public void preview() throws Exception {

        UserConfig rootUserConfig = penroseConfig.getRootUserConfig();

        PreviewEditorInput ei = new PreviewEditorInput();
        ei.setProjectNode(this);
        ei.setBaseDn("");
        ei.setBindDn(rootUserConfig.getDn());
        ei.setBindPassword(rootUserConfig.getPassword());

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage activePage = window.getActivePage();
        activePage.openEditor(ei, PreviewEditor.class.getName());
    }

    public void browse() throws Exception {

        Project project = getProject();

        String hostname = project.getHost();

        ServiceConfig serviceConfig = penroseConfig.getServiceConfig("LDAP");
        String s = serviceConfig.getParameter(LDAP_PORT);
        int port = s == null ? DEFAULT_LDAP_PORT : Integer.parseInt(s);

        UserConfig rootUserConfig = penroseConfig.getRootUserConfig();

        BrowserEditorInput ei = new BrowserEditorInput();
        ei.setProject(project);
        ei.setHostname(hostname);
        ei.setPort(port);
        ei.setBaseDn("");
        ei.setBindDn(rootUserConfig.getDn());
        ei.setBindPassword(rootUserConfig.getPassword());

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage activePage = window.getActivePage();
        activePage.openEditor(ei, BrowserEditor.class.getName());
    }

    public void remove() throws Exception {

        Project project = getProject();

        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

        boolean confirm = MessageDialog.openQuestion(
                shell,
                "Confirmation",
                "Remove Project \""+project.getName()+"\"?");

        if (!confirm) return;

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.getApplicationConfig().removeProject(project.getName());
        penroseStudio.saveApplicationConfig();

        view.removeProjectNode(project.getName());

        penroseStudio.notifyChangeListeners();
    }

    public void copy() throws Exception {
        view.setClipboard(getObject());
    }

    public void paste() throws Exception {

        Object clipboard = view.getClipboard();

        view.setClipboard(null);
    }

    public boolean hasChildren() throws Exception {
        return !children.isEmpty();
    }

    public Collection getChildren() throws Exception {
        return children;
    }

    public Project getProject() {
        return (Project)getObject();
    }

    public PartitionsNode getPartitionsNode() {
        return partitionsNode;
    }

    public SchemasNode getSchemasNode() {
        return schemasNode;
    }

    public ServicesNode getServicesNode() {
        return servicesNode;
    }

    public CachesNode getCachesNode() {
        return cachesNode;
    }

    public LoggingNode getLoggingNode() {
        return loggingNode;
    }

    public PenroseClient getClient() {
        return client;
    }

    public void setClient(PenroseClient client) {
        this.client = client;
    }

    public PenroseConfig getPenroseConfig() {
        return penroseConfig;
    }

    public void setPenroseConfig(PenroseConfig penroseConfig) {
        this.penroseConfig = penroseConfig;
    }

    public SchemaManager getSchemaManager() {
        return schemaManager;
    }

    public void setSchemaManager(SchemaManager schemaManager) {
        this.schemaManager = schemaManager;
    }

    public PartitionManager getPartitionManager() {
        return partitionManager;
    }

    public void setPartitionManager(PartitionManager partitionManager) {
        this.partitionManager = partitionManager;
    }

    public Log4jConfig getLog4jConfig() {
        return log4jConfig;
    }

    public void setLog4jConfig(Log4jConfig log4jConfig) {
        this.log4jConfig = log4jConfig;
    }

    public void setChildren(Collection children) {
        this.children = children;
    }

    public void setPartitionsNode(PartitionsNode partitionsNode) {
        this.partitionsNode = partitionsNode;
    }

    public void setSchemasNode(SchemasNode schemasNode) {
        this.schemasNode = schemasNode;
    }

    public void setServicesNode(ServicesNode servicesNode) {
        this.servicesNode = servicesNode;
    }

    public void setCachesNode(CachesNode cachesNode) {
        this.cachesNode = cachesNode;
    }

    public void setLoggingNode(LoggingNode loggingNode) {
        this.loggingNode = loggingNode;
    }

    public String getWorkDir() {
        return System.getProperty("user.workDir")+File.separator+"work"+File.separator+getName();
    }
}
