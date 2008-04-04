package org.safehaus.penrose.studio.federation.nis.editor;

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
import org.apache.log4j.Logger;
import org.safehaus.penrose.ldap.*;
import org.safehaus.penrose.federation.repository.NISDomain;
import org.safehaus.penrose.studio.federation.Federation;
import org.safehaus.penrose.studio.federation.nis.NISFederation;
import org.safehaus.penrose.studio.federation.nis.ownership.NISHostDialog;
import org.safehaus.penrose.studio.federation.nis.ownership.NISFilesEditor;
import org.safehaus.penrose.studio.nis.dialog.NISUserDialog;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.management.*;
import org.safehaus.penrose.management.module.ModuleClient;
import org.safehaus.penrose.management.source.SourceClient;
import org.safehaus.penrose.management.partition.PartitionClient;
import org.safehaus.penrose.management.partition.PartitionManagerClient;

import java.util.Date;
import java.util.Collection;
import java.util.ArrayList;
import java.rmi.registry.Registry;

/**
 * @author Endi S. Dewata
 */
public class NISHostsPage extends FormPage {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    NISFilesEditor editor;
    NISDomain domain;
    NISFederation nisFederation;

    Table hostsTable;

    public NISHostsPage(NISFilesEditor editor) {
        super(editor, "HOSTS", "  Hosts ");

        this.editor = editor;
        domain = editor.getDomain();
        nisFederation = editor.getNisTool();
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

    public void setActive(boolean b) {
        super.setActive(b);
        if (b) refresh();
    }
    
    public void refresh() {
        try {
            int indices[] = hostsTable.getSelectionIndices();
            hostsTable.removeAll();

            SearchRequest request = new SearchRequest();
            request.setFilter("(domain="+domain.getName()+")");
            
            SearchResponse response = new SearchResponse() {
                public void add(SearchResult result) {
                    Attributes attributes = result.getAttributes();
                    String name = (String) attributes.getValue("name");
                    Integer port = (Integer) attributes.getValue("port");
                    String paths = (String) attributes.getValue("paths");
                    Integer files = (Integer) attributes.getValue("files");
                    Date lastUpdated = (Date) attributes.getValue("lastUpdated");
                    String status = (String) attributes.getValue("status");

                    TableItem ti = new TableItem(hostsTable, SWT.NONE);
                    ti.setText(0, name);
                    ti.setText(1, port == null ? ""+Registry.REGISTRY_PORT : ""+port);
                    ti.setText(2, paths == null ? "" : paths);
                    ti.setText(3, files == null ? "" : ""+files);
                    ti.setText(4, status == null ? (lastUpdated == null ? "" : ""+lastUpdated) : status);
                    ti.setData(result);
                }
            };

            Project project = nisFederation.getProject();
            PenroseClient client = project.getClient();
            PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
            PartitionClient partitionClient = partitionManagerClient.getPartitionClient(Federation.FEDERATION);
            SourceClient sourceClient = partitionClient.getSourceClient("penrose_hosts");

            sourceClient.search(request, response);
            //nisFederation.getHosts().search(request, response);

            hostsTable.select(indices);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
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
        tc.setText("Paths");

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
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
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
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
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
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
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
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
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
        DN dn = new DN(rb.toRdn());

        Attributes attributes = new Attributes();
        attributes.setValue("domain", domain.getName());
        attributes.setValue("name", dialog.getName());
        attributes.setValue("port", dialog.getPort());
        attributes.setValue("paths", dialog.getPaths());

        Project project = nisFederation.getProject();
        PenroseClient client = project.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
        PartitionClient partitionClient = partitionManagerClient.getPartitionClient(Federation.FEDERATION);
        SourceClient sourceClient = partitionClient.getSourceClient("penrose_hosts");

        sourceClient.add(dn, attributes);
        //nisFederation.getHosts().add(dn, attributes);
    }

    public void edit() throws Exception {
        
        if (hostsTable.getSelectionCount() == 0) return;

        TableItem ti = hostsTable.getSelection()[0];
        SearchResult result = (SearchResult) ti.getData();
        DN dn = result.getDn();
        Attributes attributes = result.getAttributes();

        NISHostDialog dialog = new NISHostDialog(getSite().getShell(), SWT.NONE);
        dialog.setName((String) attributes.getValue("name"));
        dialog.setPort((Integer) attributes.getValue("port"));
        dialog.setPaths((String) attributes.getValue("paths"));
        dialog.open();

        int action = dialog.getAction();
        if (action == NISUserDialog.CANCEL) return;

        RDNBuilder rb = new RDNBuilder();
        rb.set("domain", domain.getName());
        rb.set("name", dialog.getName());
        RDN newRdn = rb.toRdn();

        Project project = nisFederation.getProject();
        PenroseClient client = project.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
        PartitionClient partitionClient = partitionManagerClient.getPartitionClient(Federation.FEDERATION);
        SourceClient sourceClient = partitionClient.getSourceClient("penrose_hosts");

        if (!dn.getRdn().equals(newRdn)) {
            sourceClient.modrdn(dn, newRdn, true);
            //nisFederation.getHosts().modrdn(dn, newRdn, true);
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

        modifications.add(new Modification(
                Modification.REPLACE,
                new Attribute("paths", dialog.getPaths())
        ));

        sourceClient.modify(newDn, modifications);
        //nisFederation.getHosts().modify(newDn, modifications);

    }

    public void remove() throws Exception {
        if (hostsTable.getSelectionCount() == 0) return;

        Project project = nisFederation.getProject();
        PenroseClient client = project.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
        PartitionClient partitionClient = partitionManagerClient.getPartitionClient(Federation.FEDERATION);
        SourceClient sourceClient = partitionClient.getSourceClient("penrose_hosts");

        TableItem items[] = hostsTable.getSelection();
        for (TableItem ti : items) {
            SearchResult result = (SearchResult) ti.getData();
            DN dn = result.getDn();
            sourceClient.delete(dn);
            //nisFederation.getHosts().delete(dn);
        }
    }

    public void updateFiles() throws Exception {
        if (hostsTable.getSelectionCount() == 0) return;

        Project project = nisFederation.getProject();
        PenroseClient client = project.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
        PartitionClient domainClient = partitionManagerClient.getPartitionClient(domain.getName()+"_"+NISFederation.YP);
        ModuleClient moduleClient = domainClient.getModuleClient("NISModule");

        PartitionClient partitionClient = partitionManagerClient.getPartitionClient(Federation.FEDERATION);
        SourceClient sourceClient = partitionClient.getSourceClient("penrose_hosts");

        TableItem items[] = hostsTable.getSelection();

        for (final TableItem ti : items) {
            SearchResult result = (SearchResult) ti.getData();

            Collection<Modification> modifications = new ArrayList<Modification>();

            modifications.add(new Modification(
                    Modification.DELETE,
                    new Attribute("files")
            ));

            modifications.add(new Modification(
                    Modification.REPLACE,
                    new Attribute("status", "UPDATING")
            ));

            sourceClient.modify(result.getDn(), modifications);
            //nisFederation.getHosts().modify(result.getDn(), modifications);

            Attributes attributes = result.getAttributes();
            String hostname = (String)attributes.getValue("name");
            String[] paths = ((String)attributes.getValue("paths")).split(",");

            moduleClient.invoke(
                    "scan",
                    new Object[] { hostname, paths },
                    new String[] { String.class.getName(), String[].class.getName() }
            );

            //Runnable runnable = new UpdateFilesRunnable(result, nisFederation.getHosts(), nisFederation.getFiles());
            //runnable.run();

            //new Thread(runnable).start();
            
            //Display display = getSite().getShell().getDisplay();
            //display.asyncExec(runnable);
        }

        refresh();
    }
}
