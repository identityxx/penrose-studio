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
import org.apache.log4j.Logger;
import org.safehaus.penrose.ldap.SearchRequest;
import org.safehaus.penrose.ldap.SearchResult;
import org.safehaus.penrose.ldap.SearchResponse;
import org.safehaus.penrose.ldap.Attributes;
//import org.safehaus.penrose.agent.client.FindClient;
import org.safehaus.penrose.federation.NISRepositoryClient;
import org.safehaus.penrose.federation.FederationRepositoryConfig;
import org.safehaus.penrose.federation.Federation;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.source.SourceClient;
import org.safehaus.penrose.source.SourceManagerClient;
import org.safehaus.penrose.partition.PartitionManagerClient;

import java.util.*;

/**
 * @author Endi S. Dewata
 */
public class NISScriptsPage extends FormPage {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    protected Combo actionsCombo;
    protected List hostsList;
    protected Text fromText;
    protected Text toText;

    Label messageLabel;
    Table table;

    NISFilesEditor editor;

    Server project;
    NISRepositoryClient nisFederation;
    FederationRepositoryConfig domain;

    Map<String,String> actions = new LinkedHashMap<String,String>();

    public NISScriptsPage(NISFilesEditor editor) {
        super(editor, "SCRIPTS", "  Scripts  ");

        this.editor = editor;
        this.project = editor.project;
        this.domain = editor.getDomain();
        this.nisFederation = editor.getNisTool();

        actions.put("Change file UID number", "changeUid");
        actions.put("Change file GID number", "changeGid");
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
           hostsList.removeAll();

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

           PenroseClient penroseClient = project.getClient();
           PartitionManagerClient partitionManagerClient = penroseClient.getPartitionManagerClient();
           PartitionClient partitionClient = partitionManagerClient.getPartitionClient(Federation.FEDERATION);
           SourceManagerClient sourceManagerClient = partitionClient.getSourceManagerClient();
           SourceClient sourceClient = sourceManagerClient.getSourceClient("penrose_hosts");

           sourceClient.search(request, response);
           //nisFederation.getHosts().search(request, response);

           hostsList.selectAll();

       } catch (Exception e) {
           log.error(e.getMessage(), e);
           ErrorDialog.open(e);
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

        Label fromLabel = toolkit.createLabel(composite, "From:");
        fromLabel.setLayoutData(new GridData());

        fromText = toolkit.createText(composite, "", SWT.BORDER);
        gd = new GridData();
        gd.horizontalSpan = 2;
        gd.widthHint = 80;
        fromText.setLayoutData(gd);

        Label toLabel = toolkit.createLabel(composite, "To:");
        toLabel.setLayoutData(new GridData());

        toText = toolkit.createText(composite, "", SWT.BORDER);
        gd = new GridData();
        gd.horizontalSpan = 2;
        gd.widthHint = 80;
        toText.setLayoutData(gd);

        Label hostLabel = toolkit.createLabel(composite, "Hosts:");
        gd = new GridData();
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
        runButton.setEnabled(false);
        gd = new GridData();
        gd.horizontalAlignment = GridData.END;
        gd.widthHint = 80;
        runButton.setLayoutData(gd);

        runButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    run();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
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

    public void run() throws Exception {

        messageLabel.setText("Running...");

        table.removeAll();

        String title = actionsCombo.getText();
        String action = (String)actionsCombo.getData(title);
        String parameters = fromText.getText();

        for (String host : hostsList.getSelection()) {
            SearchResult result = (SearchResult) hostsList.getData(host);
            Attributes attributes = result.getAttributes();

            final String hostname = (String)attributes.getValue("name");
            String s = (String)attributes.getValue("paths");
            Integer port = (Integer) attributes.getValue("port");

            Collection<String> paths = new ArrayList<String>();
            StringTokenizer st = new StringTokenizer(s, ",");
            while (st.hasMoreTokens()) paths.add(st.nextToken());
/*
            FindClient client = new FindClient(hostname, port);

            if ("changeUid".equals(action)) {
                st = new StringTokenizer(parameters);
                int rc = client.changeUid(paths, new Integer(st.nextToken()), new Integer(st.nextToken()));

                messageLabel.setText(rc == 0 ? "Operation succeeded." : "Operation failed. RC: "+rc);

            } else if ("changeGid".equals(action)) {
                st = new StringTokenizer(parameters);
                int rc = client.changeGid(paths, new Integer(st.nextToken()), new Integer(st.nextToken()));

                messageLabel.setText(rc == 0 ? "Operation succeeded." : "Operation failed. RC: "+rc);
            }
*/
        }
    }

    public Combo getActionsCombo() {
        return actionsCombo;
    }

    public void setActionsCombo(Combo actionsCombo) {
        this.actionsCombo = actionsCombo;
    }

    public List getHostsList() {
        return hostsList;
    }

    public void setHostsList(List hostsList) {
        this.hostsList = hostsList;
    }

    public Text getFromText() {
        return fromText;
    }

    public void setFromText(Text fromText) {
        this.fromText = fromText;
    }

    public Text getToText() {
        return toText;
    }

    public void setToText(Text toText) {
        this.toText = toText;
    }
}
