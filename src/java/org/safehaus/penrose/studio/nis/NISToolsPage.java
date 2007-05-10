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
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.jface.dialogs.MessageDialog;
import org.apache.log4j.Logger;
import org.safehaus.penrose.source.Source;
import org.safehaus.penrose.source.SourceManager;
import org.safehaus.penrose.studio.PenroseApplication;
import org.safehaus.penrose.naming.PenroseContext;
import org.safehaus.penrose.ldap.SearchRequest;
import org.safehaus.penrose.ldap.SearchResult;
import org.safehaus.penrose.ldap.SearchResponse;
import org.safehaus.penrose.ldap.Attributes;
import org.safehaus.penrose.agent.client.FindClient;
import org.safehaus.penrose.agent.client.FindResult;
import org.safehaus.penrose.agent.AgentResults;
import org.safehaus.penrose.nis.NISDomain;

import java.util.Map;
import java.util.LinkedHashMap;
import java.util.StringTokenizer;

/**
 * @author Endi S. Dewata
 */
public class NISToolsPage extends FormPage {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    Combo actionsCombo;
    List hostsList;
    Text parametersText;

    Label messageLabel;
    Table table;

    NISEditor editor;
    NISDomain domain;

    Map<String,String> actions = new LinkedHashMap<String,String>();

    Source hosts;

    public NISToolsPage(NISEditor editor) {
        super(editor, "TOOLS", "  Tools  ");

        this.editor = editor;
        this.domain = editor.getDomain();

        actions.put("Find files by UID number", "findByUid");
        actions.put("Find files by GID number", "findByGid");
        actions.put("Change file UID number", "changeUid");
        actions.put("Change file GID number", "changeGid");

        PenroseApplication penroseApplication = PenroseApplication.getInstance();
        PenroseContext penroseContext = penroseApplication.getPenroseContext();
        SourceManager sourceManager = penroseContext.getSourceManager();

        hosts = sourceManager.getSource("DEFAULT", "hosts");
    }

    public void createFormContent(IManagedForm managedForm) {
        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText("NIS Tools");

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

        Label actionLabel = toolkit.createLabel(composite, "Action:");
        actionLabel.setLayoutData(new GridData());

        actionsCombo = new Combo(composite, SWT.READ_ONLY);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        actionsCombo.setLayoutData(gd);

        for (String title : actions.keySet()) {
            String value = actions.get(title);
            actionsCombo.add(title);
            actionsCombo.setData(title, value);
        }

        actionsCombo.select(0);

        Label hostLabel = toolkit.createLabel(composite, "Hosts:");
        gd = new GridData();
        gd.verticalAlignment = GridData.BEGINNING;
        gd.widthHint = 100;
        hostLabel.setLayoutData(gd);

        hostsList = new List(composite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.heightHint = 80;
        hostsList.setLayoutData(gd);

        Composite links = toolkit.createComposite(composite);
        links.setLayout(new GridLayout());
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

        Label parametersLabel = toolkit.createLabel(composite, "Parameters:");
        parametersLabel.setLayoutData(new GridData());

        parametersText = toolkit.createText(composite, "", SWT.BORDER);
        parametersText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Button runButton = new Button(composite, SWT.PUSH);
        runButton.setText("  Run  ");
        gd = new GridData();
        gd.horizontalAlignment = GridData.END;
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
        tc.setWidth(500);

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
                String path = (String)attributes.getValue("path");

                String host = name+":"+path;
                hostsList.add(host);
                hostsList.setData(host, result);
            }
        };

        hosts.search(request, response);
    }

    public void run() throws Exception {

        messageLabel.setText("Running...");

        table.removeAll();

        int counter = 0;

        String title = actionsCombo.getText();
        String action = (String)actionsCombo.getData(title);
        String parameters = parametersText.getText();

        for (String host : hostsList.getSelection()) {
            SearchResult result = (SearchResult) hostsList.getData(host);
            Attributes attributes = result.getAttributes();

            final String hostname = (String)attributes.getValue("name");
            String path = (String)attributes.getValue("path");
            Integer port = (Integer) attributes.getValue("port");

            FindClient client = new FindClient(hostname, port);

            if ("findByUid".equals(action)) {

                AgentResults<FindResult> results = new AgentResults<FindResult>() {
                    public void add(FindResult result) {
                        System.out.println(result.getUid()+" "+result.getGid()+" "+result.getPath());
                        TableItem ti = new TableItem(table, SWT.NONE);
                        ti.setText(0, hostname);
                        ti.setText(1, result.getPath());
                    }
                };

                client.findByUid(path, new Integer(parameters), results);

                counter += results.getTotalCount();

                messageLabel.setText("Found "+counter+" file(s).");

            } else if ("findByGid".equals(action)) {

                AgentResults<FindResult> results = new AgentResults<FindResult>() {
                    public void add(FindResult result) {
                        System.out.println(result.getUid()+" "+result.getGid()+" "+result.getPath());
                        TableItem ti = new TableItem(table, SWT.NONE);
                        ti.setText(0, hostname);
                        ti.setText(1, result.getPath());
                    }
                };

                client.findByGid(path, new Integer(parameters), results);

                counter += results.getTotalCount();

                messageLabel.setText("Found "+counter+" file(s).");

            } else if ("changeUid".equals(action)) {
                StringTokenizer st = new StringTokenizer(parameters);
                int rc = client.changeUid(path, new Integer(st.nextToken()), new Integer(st.nextToken()));

                messageLabel.setText(rc == 0 ? "Operation succeeded." : "Operation failed. RC: "+rc);

            } else if ("changeGid".equals(action)) {
                StringTokenizer st = new StringTokenizer(parameters);
                int rc = client.changeGid(path, new Integer(st.nextToken()), new Integer(st.nextToken()));

                messageLabel.setText(rc == 0 ? "Operation succeeded." : "Operation failed. RC: "+rc);
            }
        }
    }
}
