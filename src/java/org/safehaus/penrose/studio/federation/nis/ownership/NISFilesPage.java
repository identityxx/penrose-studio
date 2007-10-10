package org.safehaus.penrose.studio.federation.nis.ownership;

import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.jface.dialogs.MessageDialog;
import org.apache.log4j.Logger;
import org.safehaus.penrose.ldap.*;
import org.safehaus.penrose.studio.federation.nis.NISRepository;
import org.safehaus.penrose.filter.OrFilter;
import org.safehaus.penrose.filter.SimpleFilter;
import org.safehaus.penrose.filter.AndFilter;
import org.safehaus.penrose.studio.federation.nis.NISFederation;

/**
 * @author Endi S. Dewata
 */
public class NISFilesPage extends FormPage implements Runnable {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    protected Text uidText;
    protected Text gidText;
    protected List hostsList;

    Label messageLabel;
    Table table;

    NISFilesEditor editor;
    NISRepository domain;
    NISFederation nisFederation;

    public NISFilesPage(NISFilesEditor editor) {
        super(editor, "FILES", "  Files  ");

        this.editor = editor;
        domain = editor.getDomain();
        nisFederation = editor.getNisTool();
    }

    public void createFormContent(IManagedForm managedForm) {
        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText("NIS Files");

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Search");
        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Control sourcesSection = createActionSection(section);
        section.setClient(sourcesSection);

        section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Results");
        section.setLayoutData(new GridData(GridData.FILL_BOTH));

        Control resultsSection = createResultsSection(section);
        section.setClient(resultsSection);

        init();
    }

    public void init() {
       try {
           SearchRequest request = new SearchRequest();
           request.setFilter("(domain="+domain.getName()+")");

           SearchResponse response = new SearchResponse() {
               public void add(SearchResult result) throws Exception {
                   Attributes attributes = result.getAttributes();
                   String name = (String)attributes.getValue("name");
                   String paths = (String)attributes.getValue("paths");

                   String host = name+":"+paths;
                   hostsList.add(host);
                   hostsList.setData(host, result);
               }
           };

           nisFederation.getHosts().search(request, response);

           hostsList.selectAll();

       } catch (Exception e) {
           log.error(e.getMessage(), e);
           String message = e.toString();
           if (message.length() > 500) {
               message = message.substring(0, 500) + "...";
           }
           MessageDialog.openError(editor.getSite().getShell(), "Init Failed", message);
       }
   }

    public Composite createActionSection(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(3, false));

        Label uidLabel = toolkit.createLabel(composite, "UID:");
        GridData gd = new GridData();
        gd.widthHint = 100;
        uidLabel.setLayoutData(gd);

        uidText = toolkit.createText(composite, "", SWT.BORDER);
        gd = new GridData();
        gd.horizontalSpan = 2;
        gd.widthHint = 100;
        uidText.setLayoutData(gd);

        Label gidLabel = toolkit.createLabel(composite, "GID:");
        gidLabel.setLayoutData(new GridData());

        gidText = toolkit.createText(composite, "", SWT.BORDER);
        gd = new GridData();
        gd.horizontalSpan = 2;
        gd.widthHint = 100;
        gidText.setLayoutData(gd);

        Label hostLabel = toolkit.createLabel(composite, "Hosts:");
        gd = new GridData();
        gd.verticalAlignment = GridData.BEGINNING;
        hostLabel.setLayoutData(gd);

        hostsList = new List(composite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        gd.heightHint = 80;
        hostsList.setLayoutData(gd);

        new Label(composite, SWT.NONE);

        Composite links = toolkit.createComposite(composite);
        links.setLayout(new RowLayout());
        links.setLayoutData(new GridData(GridData.FILL_VERTICAL));

        Hyperlink selectAllLink = toolkit.createHyperlink(links, "Select All", SWT.NONE);

        selectAllLink.addHyperlinkListener(new HyperlinkAdapter() {
            public void linkActivated(HyperlinkEvent event) {
                hostsList.selectAll();
            }
        });

        Hyperlink selectNoneLink = toolkit.createHyperlink(links, "Select None", SWT.NONE);

        selectNoneLink.addHyperlinkListener(new HyperlinkAdapter() {
            public void linkActivated(HyperlinkEvent event) {
                hostsList.deselectAll();
            }
        });

        Button runButton = new Button(composite, SWT.PUSH);
        runButton.setText("  Search  ");
        gd = new GridData();
        gd.horizontalAlignment = GridData.END;
        gd.widthHint = 80;
        runButton.setLayoutData(gd);

        final Runnable runnable = this;

        runButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                Display display = editor.getSite().getShell().getDisplay();
                display.asyncExec(runnable);
            }
        });

        return composite;
    }

    public Composite createResultsSection(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        composite.setLayout(new GridLayout());

        messageLabel = toolkit.createLabel(composite, "");
        messageLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        table = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION);
        table.setLayoutData(new GridData(GridData.FILL_BOTH));

        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        TableColumn tc = new TableColumn(table, SWT.NONE);
        tc.setText("Hostname");
        tc.setWidth(150);

        tc = new TableColumn(table, SWT.NONE);
        tc.setText("File");
        tc.setWidth(400);

        tc = new TableColumn(table, SWT.NONE);
        tc.setText("UID");
        tc.setWidth(50);

        tc = new TableColumn(table, SWT.NONE);
        tc.setText("GID");
        tc.setWidth(50);

        return composite;
    }

    public void run() {
        try {
            runImpl();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            String message = e.toString();
            if (message.length() > 500) {
                message = message.substring(0, 500) + "...";
            }
            MessageDialog.openError(editor.getSite().getShell(), "Action Failed", message);
        }
    }

    public void runImpl() throws Exception {

        table.removeAll();

        if (hostsList.getSelectionCount() == 0) return;
        
        long counter = 0;

        AndFilter filter = new AndFilter();

        OrFilter hostnameFilter = new OrFilter();

        for (String host : hostsList.getSelection()) {
            SearchResult result = (SearchResult) hostsList.getData(host);
            Attributes attributes = result.getAttributes();

            final String hostname = (String)attributes.getValue("name");
            hostnameFilter.addFilter(new SimpleFilter("hostname", "=", hostname));
        }

        filter.addFilter(hostnameFilter);

        if (!"".equals(uidText.getText())) {
            filter.addFilter(new SimpleFilter("uidNumber", "=", new Integer(uidText.getText())));
        }

        if (!"".equals(gidText.getText())) {
            filter.addFilter(new SimpleFilter("gidNumber", "=", new Integer(gidText.getText())));
        }

        SearchRequest request = new SearchRequest();
        request.setFilter(filter);
        request.setSizeLimit(100);

        SearchResponse response = new SearchResponse() {
            public void add(SearchResult result) {
                Attributes attributes = result.getAttributes();

                String hostname = (String)attributes.getValue("hostname");
                String path = (String)attributes.getValue("path");
                Integer uidNumber = (Integer)attributes.getValue("uidNumber");
                Integer gidNumber = (Integer)attributes.getValue("gidNumber");

                TableItem ti = new TableItem(table, SWT.NONE);
                ti.setText(0, hostname);
                ti.setText(1, path);
                ti.setText(2, ""+uidNumber);
                ti.setText(3, ""+gidNumber);

                totalCount++;
            }
        };

        nisFederation.getFiles().search(request, response);

        counter += response.getTotalCount();

        messageLabel.setText("Found "+counter+" file(s).");
    }

    public List getHostsList() {
        return hostsList;
    }

    public void setHostsList(List hostsList) {
        this.hostsList = hostsList;
    }

    public Text getUidText() {
        return uidText;
    }

    public void setUidText(Text uidText) {
        this.uidText = uidText;
    }

    public Text getGidText() {
        return gidText;
    }

    public void setGidText(Text gidText) {
        this.gidText = gidText;
    }
}
