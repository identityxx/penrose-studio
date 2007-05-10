package org.safehaus.penrose.studio.nis;

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
import org.safehaus.penrose.studio.PenroseApplication;
import org.safehaus.penrose.naming.PenroseContext;
import org.safehaus.penrose.source.SourceManager;
import org.safehaus.penrose.source.Source;
import org.safehaus.penrose.ldap.*;
import org.safehaus.penrose.agent.client.FindClient;
import org.safehaus.penrose.agent.client.FindResult;
import org.safehaus.penrose.agent.AgentResults;
import org.safehaus.penrose.nis.NISDomain;

import java.util.*;

/**
 * @author Endi S. Dewata
 */
public class NISFilesPage extends FormPage {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    List hostsList;

    Label messageLabel;
    Table table;

    NISEditor editor;
    NISDomain domain;

    Source hosts;

    public NISFilesPage(NISEditor editor) {
        super(editor, "FILES", "  Files  ");

        this.editor = editor;
        this.domain = editor.getDomain();

        PenroseApplication penroseApplication = PenroseApplication.getInstance();
        PenroseContext penroseContext = penroseApplication.getPenroseContext();
        SourceManager sourceManager = penroseContext.getSourceManager();

        hosts = sourceManager.getSource("DEFAULT", "hosts");
    }

    public void createFormContent(IManagedForm managedForm) {
        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText("NIS Files");

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Action");
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
           updateHosts();

       } catch (Exception e) {
           log.debug(e.getMessage(), e);
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

        Label hostLabel = toolkit.createLabel(composite, "Hosts:");
        GridData gd = new GridData();
        gd.verticalAlignment = GridData.BEGINNING;
        gd.widthHint = 100;
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
        runButton.setText("  Run  ");
        gd = new GridData();
        gd.horizontalAlignment = GridData.END;
        gd.widthHint = 80;
        runButton.setLayoutData(gd);

        runButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    run();
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

    public void updateHosts() throws Exception {
        hostsList.removeAll();

        SearchRequest request = new SearchRequest();
        request.setFilter("(domain="+domain.getName()+")");

        SearchResponse<SearchResult> response = new SearchResponse<SearchResult>() {
            public void add(SearchResult result) throws Exception {
                Attributes attributes = result.getAttributes();
                String name = (String)attributes.getValue("name");
                String paths = (String)attributes.getValue("paths");

                String host = name+":"+ paths;
                hostsList.add(host);
                hostsList.setData(host, result);
            }
        };

        hosts.search(request, response);
    }

    public void run() throws Exception {

        messageLabel.setText("Refreshing...");
        
        table.removeAll();

        int counter = 0;

        for (String host : hostsList.getSelection()) {
            SearchResult result = (SearchResult) hostsList.getData(host);
            Attributes attributes = result.getAttributes();

            final String hostname = (String)attributes.getValue("name");
            String s = (String)attributes.getValue("paths");

            Collection<String> paths = new ArrayList<String>();
            StringTokenizer st = new StringTokenizer(s, ",");
            while (st.hasMoreTokens()) paths.add(st.nextToken());

            FindClient client = new FindClient(hostname);

            AgentResults<FindResult> results = new AgentResults<FindResult>() {
                public void add(FindResult result) {
                    System.out.println(result.getUid()+" "+result.getGid()+" "+result.getPath());
                    TableItem ti = new TableItem(table, SWT.NONE);
                    ti.setText(0, hostname);
                    ti.setText(1, result.getPath());
                    ti.setText(2, ""+result.getUid());
                    ti.setText(3, ""+result.getGid());
                }
            };

            client.find(paths, results);

            counter += results.getTotalCount();

            messageLabel.setText("Found "+counter+" file(s).");
        }
    }
}
