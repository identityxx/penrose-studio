package org.safehaus.penrose.studio.nis;

import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.jface.dialogs.MessageDialog;
import org.apache.log4j.Logger;
import org.safehaus.penrose.studio.PenroseApplication;
import org.safehaus.penrose.naming.PenroseContext;
import org.safehaus.penrose.source.SourceManager;
import org.safehaus.penrose.source.Source;
import org.safehaus.penrose.ldap.*;
import org.safehaus.penrose.nis.NISDomain;
import org.safehaus.penrose.agent.client.FindClient;
import org.safehaus.penrose.agent.client.FindResult;
import org.safehaus.penrose.agent.AgentResults;
import org.safehaus.penrose.agent.util.AgentClassServer;

import java.util.Date;
import java.util.Collection;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.rmi.registry.Registry;
import java.io.File;
import java.net.InetAddress;

/**
 * @author Endi S. Dewata
 */
public class NISHostsPage extends FormPage {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    NISEditor editor;
    NISDomain domain;

    Table hostsTable;

    Source hosts;
    Source files;

    public NISHostsPage(NISEditor editor) {
        super(editor, "HOSTS", "  Hosts ");

        this.editor = editor;
        this.domain = editor.getDomain();

        PenroseApplication penroseApplication = PenroseApplication.getInstance();
        PenroseContext penroseContext = penroseApplication.getPenroseContext();

        final SourceManager sourceManager = penroseContext.getSourceManager();

        hosts = sourceManager.getSource("DEFAULT", "hosts");
        files = sourceManager.getSource("DEFAULT", "files");
    }

    public void createFormContent(IManagedForm managedForm) {
        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText("NIS Hosts");

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Hosts");
        section.setLayoutData(new GridData(GridData.FILL_BOTH));

        Control sourcesSection = createHostsSection(section);
        section.setClient(sourcesSection);

        refresh();
    }

    public void refresh() {
        try {
            int indices[] = hostsTable.getSelectionIndices();
            hostsTable.removeAll();

            SearchRequest request = new SearchRequest();
            request.setFilter("(domain="+domain.getName()+")");
            
            SearchResponse<SearchResult> response = new SearchResponse<SearchResult>() {
                public void add(SearchResult result) {
                    Attributes attributes = result.getAttributes();
                    String name = (String) attributes.getValue("name");
                    String path = (String) attributes.getValue("path");
                    Integer port = (Integer) attributes.getValue("port");
                    Integer files = (Integer) attributes.getValue("files");
                    Date lastUpdated = (Date) attributes.getValue("lastUpdated");

                    TableItem ti = new TableItem(hostsTable, SWT.NONE);
                    ti.setText(0, name);
                    ti.setText(1, port == null ? ""+Registry.REGISTRY_PORT : ""+port);
                    ti.setText(2, path == null ? "" : path);
                    ti.setText(3, files == null ? "" : ""+files);
                    ti.setText(4, lastUpdated == null ? "" : ""+lastUpdated);
                    ti.setData(result);
                }
            };

            hosts.search(request, response);

            hostsTable.select(indices);

        } catch (Exception e) {
            log.debug(e.getMessage(), e);
            String message = e.toString();
            if (message.length() > 500) {
                message = message.substring(0, 500) + "...";
            }
            MessageDialog.openError(editor.getSite().getShell(), "Init Failed", message);
        }
    }

    public Composite createHostsSection(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        hostsTable = new Table(composite, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
        hostsTable.setLayoutData(new GridData(GridData.FILL_BOTH));
        hostsTable.setHeaderVisible(true);
        hostsTable.setLinesVisible(true);

        TableColumn tc = new TableColumn(hostsTable, SWT.NONE);
        tc.setWidth(150);
        tc.setText("Hostname");

        tc = new TableColumn(hostsTable, SWT.NONE);
        tc.setWidth(50);
        tc.setText("Port");

        tc = new TableColumn(hostsTable, SWT.NONE);
        tc.setWidth(120);
        tc.setText("Path");

        tc = new TableColumn(hostsTable, SWT.NONE);
        tc.setWidth(80);
        tc.setText("Files");

        tc = new TableColumn(hostsTable, SWT.NONE);
        tc.setWidth(130);
        tc.setText("Last Updated");

        Composite buttons = toolkit.createComposite(composite);
        buttons.setLayout(new GridLayout());
        GridData gd = new GridData(GridData.FILL_VERTICAL);
        gd.widthHint = 120;
        buttons.setLayoutData(gd);

        Button addButton = new Button(buttons, SWT.PUSH);
        addButton.setText("Add");
        addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    add();

                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                    String message = e.toString();
                    if (message.length() > 500) {
                        message = message.substring(0, 500) + "...";
                    }
                    MessageDialog.openError(editor.getSite().getShell(), "Add Failed", message);
                }

                refresh();
            }
        });

        Button editButton = new Button(buttons, SWT.PUSH);
        editButton.setText("Edit");
        editButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        editButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    edit();

                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                    String message = e.toString();
                    if (message.length() > 500) {
                        message = message.substring(0, 500) + "...";
                    }
                    MessageDialog.openError(editor.getSite().getShell(), "Edit Failed", message);
                }

                refresh();
            }
        });

        Button removeButton = new Button(buttons, SWT.PUSH);
        removeButton.setText("Remove");
        removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    remove();

                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                    String message = e.toString();
                    if (message.length() > 500) {
                        message = message.substring(0, 500) + "...";
                    }
                    MessageDialog.openError(editor.getSite().getShell(), "Remove Failed", message);
                }

                refresh();
            }
        });

        new Label(buttons, SWT.NONE);

        Button updateButton = new Button(buttons, SWT.PUSH);
        updateButton.setText("Update Files");
        updateButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        updateButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    updateFiles();

                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                    String message = e.toString();
                    if (message.length() > 500) {
                        message = message.substring(0, 500) + "...";
                    }
                    MessageDialog.openError(editor.getSite().getShell(), "Update Failed", message);
                }

                refresh();
            }
        });

        new Label(buttons, SWT.NONE);

        Button refreshButton = new Button(buttons, SWT.PUSH);
        refreshButton.setText("Refresh");
        refreshButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        refreshButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                refresh();
            }
        });

        return composite;
    }

    public void add() throws Exception {
        NISHostDialog dialog = new NISHostDialog(getSite().getShell(), SWT.NONE);

        dialog.open();

        int action = dialog.getAction();
        if (action == NISUserDialog.CANCEL) return;

        RDNBuilder rb = new RDNBuilder();
        rb.set("domain", domain.getName());
        rb.set("name", dialog.getName());
        rb.set("path", dialog.getPath());
        DN dn = new DN(rb.toRdn());

        Attributes attributes = new Attributes();
        attributes.setValue("domain", domain.getName());
        attributes.setValue("name", dialog.getName());
        attributes.setValue("path", dialog.getPath());
        attributes.setValue("port", dialog.getPort());

        hosts.add(dn, attributes);
    }

    public void edit() throws Exception {
        
        if (hostsTable.getSelectionCount() == 0) return;

        TableItem ti = hostsTable.getSelection()[0];
        SearchResult result = (SearchResult) ti.getData();
        DN dn = result.getDn();
        Attributes attributes = result.getAttributes();

        NISHostDialog dialog = new NISHostDialog(getSite().getShell(), SWT.NONE);
        dialog.setName((String) attributes.getValue("name"));
        dialog.setPath((String) attributes.getValue("path"));
        dialog.setPort((Integer) attributes.getValue("port"));
        dialog.open();

        int action = dialog.getAction();
        if (action == NISUserDialog.CANCEL) return;

        RDNBuilder rb = new RDNBuilder();
        rb.set("domain", domain.getName());
        rb.set("name", dialog.getName());
        rb.set("path", dialog.getPath());
        RDN newRdn = rb.toRdn();

        if (!dn.getRdn().equals(newRdn)) {
            hosts.modrdn(dn, newRdn, true);
        }

        DNBuilder db = new DNBuilder();
        db.append(newRdn);
        db.append(dn.getParentDn());
        DN newDn = db.toDn();

        Collection<Modification> modifications = new ArrayList<Modification>();
        modifications.add(new Modification(
                Modification.REPLACE,
                new Attribute("port", dialog.getPort())
        ));

        hosts.modify(newDn, modifications);

    }

    public void remove() throws Exception {
        if (hostsTable.getSelectionCount() == 0) return;

        TableItem items[] = hostsTable.getSelection();
        for (TableItem ti : items) {
            SearchResult result = (SearchResult) ti.getData();
            DN dn = result.getDn();
            hosts.delete(dn);
        }
    }

    public void updateFiles() throws Exception {
        if (hostsTable.getSelectionCount() == 0) return;
/*
        Collection<String> classpaths = new ArrayList<String>();

        String classpath = System.getProperty("java.class.path");
        StringTokenizer st = new StringTokenizer(classpath, File.pathSeparator);
        while (st.hasMoreTokens()) {
            String path = st.nextToken();
            classpaths.add(path);
        }

        String extdirs = System.getProperty("java.ext.dirs");
        st = new StringTokenizer(extdirs, File.pathSeparator);
        while (st.hasMoreTokens()) {
            String path = st.nextToken();
            classpaths.add(path);
        }

        AgentClassServer classServer = new AgentClassServer(classpaths);
        int httpPort = classServer.getPort();

        log.debug("Listening to port "+httpPort+" (HTTP).");

        String rmiServer = System.getProperty("java.rmi.server.hostname");
        if (rmiServer == null) rmiServer = InetAddress.getLocalHost().getHostAddress();

        String codebase = "http://"+rmiServer+":"+httpPort+"/";
        log.debug("Codebase: "+codebase);

        //System.out.println("-Djava.rmi.server.codebase="+codebase);
        System.setProperty("java.rmi.server.codebase", codebase);
*/
        
        TableItem items[] = hostsTable.getSelection();

        RDNBuilder rb = new RDNBuilder();

        for (final TableItem ti : items) {
            SearchResult result = (SearchResult) ti.getData();
            Attributes attributes = result.getAttributes();

            ti.setText(3, "");
            ti.setText(4, "Updating...");
            hostsTable.redraw();

            final String hostname = (String) attributes.getValue("name");
            String path = (String) attributes.getValue("path");
            Integer port = (Integer) attributes.getValue("port");

            rb.clear();
            rb.set("hostname", hostname);

            files.delete(new DN(rb.toRdn()));

            FindClient client = new FindClient(hostname, port);

            AgentResults<FindResult> results = new AgentResults<FindResult>() {
                public void add(FindResult result) throws Exception {
                    log.debug(result.getUid()+" "+result.getGid()+" "+result.getPath());

                    Attributes attr = new Attributes();
                    attr.setValue("hostname", hostname);
                    attr.setValue("path", result.getPath());
                    attr.setValue("uidNumber", result.getUid());
                    attr.setValue("gidNumber", result.getGid());

                    files.add(new DN(), attr);

                    totalCount++;
                }
            };

            client.find(path, results);

            Collection<Modification> modifications = new ArrayList<Modification>();
            
            modifications.add(new Modification(
                    Modification.REPLACE,
                    new Attribute("files", results.getTotalCount())
            ));

            modifications.add(new Modification(
                    Modification.REPLACE,
                    new Attribute("lastUpdated", new Date())
            ));

            hosts.modify(result.getDn(), modifications);

            hostsTable.redraw();
        }

        //classServer.close();
    }
}
