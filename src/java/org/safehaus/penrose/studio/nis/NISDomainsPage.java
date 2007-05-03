package org.safehaus.penrose.studio.nis;

import org.apache.log4j.Logger;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.jface.dialogs.MessageDialog;
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
public class NISDomainsPage extends FormPage {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    NISEditor editor;

    Table domainsTable;

    Source domains;

    public NISDomainsPage(NISEditor editor) {
        super(editor, "DOMAINS", "  Domains ");

        this.editor = editor;

        PenroseApplication penroseApplication = PenroseApplication.getInstance();
        PenroseContext penroseContext = penroseApplication.getPenroseContext();
        SourceManager sourceManager = penroseContext.getSourceManager();

        domains = sourceManager.getSource("DEFAULT", "domains");
    }

    public void createFormContent(IManagedForm managedForm) {
        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText("NIS Domains");

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Domains");
        section.setLayoutData(new GridData(GridData.FILL_BOTH));

        Control sourcesSection = createDomainsSection(section);
        section.setClient(sourcesSection);

        refresh();
    }

    public void refresh() {
       try {
           domainsTable.removeAll();

           SearchRequest request = new SearchRequest();
           SearchResponse<SearchResult> response = new SearchResponse<SearchResult>() {
               public void add(SearchResult result) throws Exception {
                   Attributes attributes = result.getAttributes();
                   String domain = (String)attributes.getValue("name");
                   String partition = (String)attributes.getValue("partition");

                   TableItem ti = new TableItem(domainsTable, SWT.NONE);
                   ti.setText(0, domain);
                   ti.setText(1, partition);
                   ti.setData(result);
               }
           };

           domains.search(request, response);

       } catch (Exception e) {
           log.debug(e.getMessage(), e);
           String message = e.toString();
           if (message.length() > 500) {
               message = message.substring(0, 500) + "...";
           }
           MessageDialog.openError(editor.getSite().getShell(), "Init Failed", message);
       }
   }

    public Composite createDomainsSection(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        domainsTable = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION);
        domainsTable.setLayoutData(new GridData(GridData.FILL_BOTH));
        domainsTable.setHeaderVisible(true);
        domainsTable.setLinesVisible(true);

        TableColumn tc = new TableColumn(domainsTable, SWT.NONE);
        tc.setWidth(200);
        tc.setText("Domain");

        tc = new TableColumn(domainsTable, SWT.NONE);
        tc.setWidth(200);
        tc.setText("Partition");

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
                    NISDomainDialog dialog = new NISDomainDialog(getSite().getShell(), SWT.NONE);

                    dialog.open();

                    int action = dialog.getAction();
                    if (action == NISUserDialog.CANCEL) return;

                    RDNBuilder rb = new RDNBuilder();
                    rb.set("name", dialog.getName());
                    DN dn = new DN(rb.toRdn());

                    Attributes attributes = new Attributes();
                    attributes.setValue("name", dialog.getName());
                    attributes.setValue("partition", dialog.getPartition());

                    domains.add(dn, attributes);

                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                    String message = e.toString();
                    if (message.length() > 500) {
                        message = message.substring(0, 500) + "...";
                    }
                    MessageDialog.openError(editor.getSite().getShell(), "Action Failed", message);
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
                    if (domainsTable.getSelectionCount() == 0) return;

                    TableItem ti = domainsTable.getSelection()[0];
                    SearchResult result = (SearchResult)ti.getData();
                    DN dn = result.getDn();
                    Attributes attributes = result.getAttributes();

                    NISDomainDialog dialog = new NISDomainDialog(getSite().getShell(), SWT.NONE);
                    dialog.setName((String)attributes.getValue("name"));
                    dialog.setPartition((String)attributes.getValue("partition"));
                    dialog.open();

                    int action = dialog.getAction();
                    if (action == NISUserDialog.CANCEL) return;

                    RDNBuilder rb = new RDNBuilder();
                    rb.set("name", dialog.getName());
                    RDN newRdn = rb.toRdn();

                    if (!dn.getRdn().equals(newRdn)) {
                        domains.modrdn(dn, newRdn, true);
                    }

                    DNBuilder db = new DNBuilder();
                    db.append(newRdn);
                    db.append(dn.getParentDn());
                    DN newDn = db.toDn();

                    Collection<Modification> modifications = new ArrayList<Modification>();
                    modifications.add(new Modification(
                            Modification.REPLACE,
                            new Attribute("partition", dialog.getPartition())
                    ));

                    domains.modify(newDn, modifications);

                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                    String message = e.toString();
                    if (message.length() > 500) {
                        message = message.substring(0, 500) + "...";
                    }
                    MessageDialog.openError(editor.getSite().getShell(), "Action Failed", message);
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
                    if (domainsTable.getSelectionCount() == 0) return;

                    TableItem items[] = domainsTable.getSelection();
                    for (TableItem ti : items) {
                        SearchResult result = (SearchResult)ti.getData();
                        DN dn = result.getDn();
                        domains.delete(dn);
                        ti.dispose();
                    }

                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                    String message = e.toString();
                    if (message.length() > 500) {
                        message = message.substring(0, 500) + "...";
                    }
                    MessageDialog.openError(editor.getSite().getShell(), "Action Failed", message);
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
