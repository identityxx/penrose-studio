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

import java.util.Collection;
import java.util.ArrayList;

/**
 * @author Endi S. Dewata
 */
public class NISHostsPage extends FormPage {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    NISEditor editor;

    Table hostsTable;

    Source hosts;

    public NISHostsPage(NISEditor editor) {
        super(editor, "HOSTS", "  Hosts ");

        this.editor = editor;

        PenroseApplication penroseApplication = PenroseApplication.getInstance();
        PenroseContext penroseContext = penroseApplication.getPenroseContext();

        final SourceManager sourceManager = penroseContext.getSourceManager();

        hosts = sourceManager.getSource("DEFAULT", "hosts");
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
           hostsTable.removeAll();

           SearchRequest request = new SearchRequest();
           SearchResponse<SearchResult> response = new SearchResponse<SearchResult>() {
               public void add(SearchResult result) {
                   Attributes attributes = result.getAttributes();
                   String name = (String)attributes.getValue("name");
                   String domain = (String)attributes.getValue("domain");
                   String address = (String)attributes.getValue("address");
                   String path = (String)attributes.getValue("path");

                   TableItem ti = new TableItem(hostsTable, SWT.NONE);
                   ti.setText(0, domain);
                   ti.setText(1, name);
                   ti.setText(2, address == null ? "" : address);
                   ti.setText(3, path == null ? "" : path);
                   ti.setData(result);
               }
           };

           hosts.search(request, response);

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

        hostsTable = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION);
        hostsTable.setLayoutData(new GridData(GridData.FILL_BOTH));
        hostsTable.setHeaderVisible(true);
        hostsTable.setLinesVisible(true);

        TableColumn tc = new TableColumn(hostsTable, SWT.NONE);
        tc.setWidth(150);
        tc.setText("Domain");

        tc = new TableColumn(hostsTable, SWT.NONE);
        tc.setWidth(150);
        tc.setText("Hostname");

        tc = new TableColumn(hostsTable, SWT.NONE);
        tc.setWidth(100);
        tc.setText("IP Address");

        tc = new TableColumn(hostsTable, SWT.NONE);
        tc.setWidth(100);
        tc.setText("Path");

        Composite buttons = toolkit.createComposite(composite);
        buttons.setLayout(new GridLayout());
        GridData gd = new GridData(GridData.FILL_VERTICAL);
        gd.widthHint = 100;
        buttons.setLayoutData(gd);

        Button addButton = new Button(buttons, SWT.PUSH);
        addButton.setText("Add");
        addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    NISHostDialog dialog = new NISHostDialog(getSite().getShell(), SWT.NONE);

                    dialog.open();

                    int action = dialog.getAction();
                    if (action == NISUserDialog.CANCEL) return;

                    RDNBuilder rb = new RDNBuilder();
                    rb.set("name", dialog.getName());
                    rb.set("domain", dialog.getDomain());
                    DN dn = new DN(rb.toRdn());

                    Attributes attributes = new Attributes();
                    attributes.setValue("domain", dialog.getDomain());
                    attributes.setValue("name", dialog.getName());
                    attributes.setValue("address", dialog.getAddress());
                    attributes.setValue("path", dialog.getPath());

                    hosts.add(dn, attributes);

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
                    if (hostsTable.getSelectionCount() == 0) return;

                    TableItem ti = hostsTable.getSelection()[0];
                    SearchResult result = (SearchResult)ti.getData();
                    DN dn = result.getDn();
                    Attributes attributes = result.getAttributes();

                    NISHostDialog dialog = new NISHostDialog(getSite().getShell(), SWT.NONE);
                    dialog.setDomain((String)attributes.getValue("domain"));
                    dialog.setName((String)attributes.getValue("name"));
                    dialog.setAddress((String)attributes.getValue("address"));
                    dialog.setPath((String)attributes.getValue("path"));
                    dialog.open();

                    int action = dialog.getAction();
                    if (action == NISUserDialog.CANCEL) return;

                    RDNBuilder rb = new RDNBuilder();
                    rb.set("name", dialog.getName());
                    rb.set("domain", dialog.getDomain());
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
                            new Attribute("address", dialog.getAddress())
                    ));
                    modifications.add(new Modification(
                            Modification.REPLACE,
                            new Attribute("path", dialog.getPath())
                    ));

                    hosts.modify(newDn, modifications);

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
                    if (hostsTable.getSelectionCount() == 0) return;
                    
                    TableItem items[] = hostsTable.getSelection();
                    for (TableItem ti : items) {
                        SearchResult result = (SearchResult)ti.getData();
                        DN dn = result.getDn();
                        hosts.delete(dn);
                        ti.dispose();
                    }

                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                    String message = e.toString();
                    if (message.length() > 500) {
                        message = message.substring(0, 500) + "...";
                    }
                    MessageDialog.openError(editor.getSite().getShell(), "Remove Failed", message);
                }
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

}
