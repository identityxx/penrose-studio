package org.safehaus.penrose.studio.federation.nis.ownership;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.safehaus.penrose.ldap.*;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.federation.NISRepositoryClient;
import org.safehaus.penrose.federation.FederationRepositoryConfig;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.source.SourceClient;
import org.safehaus.penrose.source.SourceManagerClient;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.filter.Filter;
import org.safehaus.penrose.filter.SimpleFilter;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.ArrayList;

/**
 * @author Endi S. Dewata
 */
public class GroupsPage extends FormPage {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    Table resultsTable;

    FormEditor editor;

    Server project;
    NISRepositoryClient nisFederationClient;
    FederationRepositoryConfig repositoryConfig;

    SourceClient localSourceClient;
    SourceClient globalSourceClient;

    String objectClass = "posixGroup";

    String rdnAttribute = "cn";

    String sourceAttribute = "gidNumber";
    String targetAttribute = "gidNumber";

    String linkingAttribute = "seeAlso";
    String linkingKey = "dn";

    public GroupsPage(OwnershipAlignmentEditor editor) throws Exception {
        super(editor, "GROUPS", "  Groups  ");

        this.editor = editor;
        this.project = editor.project;
        this.nisFederationClient = editor.nisFederationClient;
        this.repositoryConfig = editor.repositoryConfig;

        PenroseClient client = project.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();

        String federationName = nisFederationClient.getFederationClient().getFederationDomain();
        String localPartitionName = repositoryConfig.getName();

        PartitionClient localPartitionClient = partitionManagerClient.getPartitionClient(federationName+"_"+localPartitionName);
        localSourceClient = localPartitionClient.getSourceManagerClient().getSourceClient("LDAP");

        PartitionClient federationPartitionClient = nisFederationClient.getFederationClient().getPartitionClient();
        SourceManagerClient sourceManagerClient = federationPartitionClient.getSourceManagerClient();

        globalSourceClient = sourceManagerClient.getSourceClient("Global");

    }

    public void createFormContent(IManagedForm managedForm) {
        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText("Groups");

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Results");
        section.setLayoutData(new GridData(GridData.FILL_BOTH));

        Control sourcesSection = createResultsSection(section);
        section.setClient(sourcesSection);
    }

    public Collection<String[]> getChanges() throws Exception {

        Collection<String[]> results = new ArrayList<String[]>();

        Filter localFilter = new SimpleFilter("objectClass", "=", objectClass);

        SearchRequest localRequest = new SearchRequest();
        localRequest.setFilter(localFilter);

        SearchResponse localResponse = new SearchResponse();

        localSourceClient.search(localRequest, localResponse);

        log.debug("Results:");
        while (localResponse.hasNext()) {
            SearchResult localEntry = localResponse.next();

            DN localDn = localEntry.getDn();
            Attributes localAttributes = localEntry.getAttributes();

            Object localRdn = localAttributes.getValue(rdnAttribute);
            Object localAttribute = localAttributes.getValue(sourceAttribute);

            log.debug(" - "+localDn+": "+localAttribute);

            Object linkingValue = localAttributes.getValue(linkingAttribute);
            Object globalAttribute = null;

            if (linkingValue != null) {
                SearchResult globalEntry = null;

                try {
                    if ("dn".equals(linkingKey)) {
                        globalEntry = globalSourceClient.find(linkingValue.toString());

                    } else {
                        SearchRequest request = new SearchRequest();
                        request.setFilter(new SimpleFilter(linkingKey, "=", linkingValue));

                        SearchResponse response = new SearchResponse();

                        globalSourceClient.search(request, response);

                        if (response.hasNext()) {
                            globalEntry = response.next();
                        }
                    }

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }

                if (globalEntry == null) continue;

                Attributes globalAttributes = globalEntry.getAttributes();
                globalAttribute = globalAttributes.getValue(targetAttribute);
            }

            if (globalAttribute == null || localAttribute.equals(globalAttribute)) continue;

            String result[] = new String[3];
            result[0] = localRdn.toString();
            result[1] = localAttribute.toString();
            result[2] = globalAttribute.toString();

            results.add(result);
        }

        return results;
    }

    public void refresh() {
        try {
            int indices[] = resultsTable.getSelectionIndices();
            resultsTable.removeAll();

            for (String result[] : getChanges()) {
                TableItem ti = new TableItem(resultsTable, SWT.NONE);
                ti.setText(0, result[0]);
                ti.setText(1, result[1]);
                ti.setText(2, result[2]);
            }

            resultsTable.select(indices);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }

    public Composite createResultsSection(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        Composite leftPanel = toolkit.createComposite(composite);
        leftPanel.setLayout(new GridLayout());
        leftPanel.setLayoutData(new GridData(GridData.FILL_BOTH));

        resultsTable = new Table(leftPanel, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        resultsTable.setLayoutData(new GridData(GridData.FILL_BOTH));
        resultsTable.setHeaderVisible(true);
        resultsTable.setLinesVisible(true);

        TableColumn tc = new TableColumn(resultsTable, SWT.NONE);
        tc.setWidth(100);
        tc.setText("Group");

        tc = new TableColumn(resultsTable, SWT.NONE);
        tc.setWidth(80);
        tc.setText("GID");

        tc = new TableColumn(resultsTable, SWT.NONE);
        tc.setWidth(80);
        tc.setText("Global GID");

        Composite buttons = toolkit.createComposite(composite);
        buttons.setLayout(new GridLayout());
        GridData gd = new GridData(GridData.FILL_VERTICAL);
        gd.verticalSpan = 2;
        gd.widthHint = 120;
        buttons.setLayoutData(gd);

        Button refreshButton = new Button(buttons, SWT.PUSH);
        refreshButton.setText("Refresh");
        refreshButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        refreshButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                refresh();
            }
        });

        Button exportButton = new Button(buttons, SWT.PUSH);
        exportButton.setText("Export");
        exportButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        exportButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                try {
                    FileDialog dialog = new FileDialog(parent.getShell());
                    dialog.setText("Export");
                    dialog.setFileName(repositoryConfig.getName()+"-groups.txt");

                    String dir = System.getProperty("user.dir");
                    dialog.setFilterPath(dir);

                    String path = dialog.open();
                    if (path == null) return;

                    exportChanges(path);

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        return composite;
    }

    public void exportChanges(String path) throws Exception {
        FileWriter file = new FileWriter(path);
        PrintWriter out = new PrintWriter(file, true);

        for (String result[] : getChanges()) {
            String cn = result[0];
            String gidNumber = result[1];
            String globalGidNumber = result[2];

            out.println(cn+","+gidNumber+","+globalGidNumber);
        }

        out.close();
    }

    public String getObjectClass() {
        return objectClass;
    }

    public void setObjectClass(String objectClass) {
        this.objectClass = objectClass;
    }

    public String getRdnAttribute() {
        return rdnAttribute;
    }

    public void setRdnAttribute(String rdnAttribute) {
        this.rdnAttribute = rdnAttribute;
    }

    public String getSourceAttribute() {
        return sourceAttribute;
    }

    public void setSourceAttribute(String sourceAttribute) {
        this.sourceAttribute = sourceAttribute;
    }

    public String getLinkingAttribute() {
        return linkingAttribute;
    }

    public void setLinkingAttribute(String linkingAttribute) {
        this.linkingAttribute = linkingAttribute;
    }

    public String getTargetAttribute() {
        return targetAttribute;
    }

    public void setTargetAttribute(String targetAttribute) {
        this.targetAttribute = targetAttribute;
    }

    public String getLinkingKey() {
        return linkingKey;
    }

    public void setLinkingKey(String linkingKey) {
        this.linkingKey = linkingKey;
    }
}