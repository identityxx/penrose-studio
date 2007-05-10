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
        DN dn = new DN(rb.toRdn());

        Attributes attributes = new Attributes();
        attributes.setValue("domain", domain.getName());
        attributes.setValue("name", dialog.getName());
        attributes.setValue("port", dialog.getPort());
        attributes.setValue("paths", dialog.getPaths());

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
        dialog.setPort((Integer) attributes.getValue("port"));
        dialog.setPaths((String) attributes.getValue("paths"));
        dialog.open();

        int action = dialog.getAction();
        if (action == NISUserDialog.CANCEL) return;

        RDNBuilder rb = new RDNBuilder();
        rb.set("domain", domain.getName());
        rb.set("name", dialog.getName());
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

        modifications.add(new Modification(
                Modification.REPLACE,
                new Attribute("paths", dialog.getPaths())
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

            hosts.modify(result.getDn(), modifications);

            Runnable runnable = new UpdateFilesRunnable(result, hosts, files);
            new Thread(runnable).start();
            
            //Display display = getSite().getShell().getDisplay();
            //display.asyncExec(runnable);
        }

        refresh();
    }
}
