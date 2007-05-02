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
import org.safehaus.penrose.ldap.SearchRequest;
import org.safehaus.penrose.ldap.SearchResult;
import org.safehaus.penrose.ldap.SearchResponse;
import org.safehaus.penrose.ldap.Attributes;

/**
 * @author Endi S. Dewata
 */
public class NISHostsPage extends FormPage {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    NISEditor editor;

    Table hostsTable;

    public NISHostsPage(NISEditor editor) {
        super(editor, "HOSTS", "  Hosts ");

        this.editor = editor;
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

        init();
    }

    public void init() {
       try {
           hostsTable.removeAll();

           PenroseApplication penroseApplication = PenroseApplication.getInstance();
           PenroseContext penroseContext = penroseApplication.getPenroseContext();

           final SourceManager sourceManager = penroseContext.getSourceManager();

           Source domains = sourceManager.getSource("DEFAULT", "domains");

           SearchRequest request = new SearchRequest();
           SearchResponse<SearchResult> response = new SearchResponse<SearchResult>();

           domains.search(request, response);

           while (response.hasNext()) {
               SearchResult result = response.next();
               Attributes attributes = result.getAttributes();
               final String domainName = (String)attributes.getValue("name");
               String partitionName = (String)attributes.getValue("partition");

               Source hosts = sourceManager.getSource(partitionName, "cache.hosts");

               SearchRequest hostsRequest = new SearchRequest();
               SearchResponse<SearchResult> hostsResponse = new SearchResponse<SearchResult>() {
                   public void add(SearchResult result) throws Exception {
                       Attributes attributes = result.getAttributes();
                       String hostName = (String)attributes.getValue("cn");
                       String address = (String)attributes.getValue("ipHostNumber");

                       TableItem ti = new TableItem(hostsTable, SWT.NONE);
                       ti.setText(0, domainName);
                       ti.setText(1, hostName);
                       ti.setText(2, address);
                       ti.setData(attributes);
                   }
               };

               hosts.search(hostsRequest, hostsResponse);
           }

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
        tc.setWidth(150);
        tc.setText("IP Address");

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

        Button editButton = new Button(buttons, SWT.PUSH);
        editButton.setText("Edit");
        editButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        editButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
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

        Button removeButton = new Button(buttons, SWT.PUSH);
        removeButton.setText("Remove");
        removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
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

        return composite;
    }

}
