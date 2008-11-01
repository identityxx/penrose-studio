package org.safehaus.penrose.studio.federation.nis.editor;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.safehaus.penrose.federation.FederationClient;
import org.safehaus.penrose.federation.NISFederationClient;
import org.safehaus.penrose.federation.FederationRepositoryConfig;
import org.safehaus.penrose.jdbc.QueryResponse;
import org.safehaus.penrose.source.SourceClient;
import org.safehaus.penrose.source.SourceManagerClient;
import org.safehaus.penrose.connection.ConnectionClient;
import org.safehaus.penrose.connection.ConnectionManagerClient;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.studio.federation.nis.conflict.NISGroupsEditor;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.client.PenroseClient;

import java.sql.ResultSet;

/**
 * @author Endi S. Dewata
 */
public class NISGroupsPage extends FormPage {

    Logger log = Logger.getLogger(getClass());

    public final static String SOURCE_NAME = "cache_groups";

    FormToolkit toolkit;

    Label entriesText;

    NISGroupsEditor editor;

    Project project;
    NISFederationClient nisFederation;
    FederationRepositoryConfig domain;

    public NISGroupsPage(NISGroupsEditor editor) {
        super(editor, "GROUPS", "  Groups  ");

        this.editor = editor;
        this.project = editor.getProject();
        this.nisFederation = editor.getNisTool();
        this.domain = editor.getDomain();
    }

    public void createFormContent(IManagedForm managedForm) {
        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText("NIS Groups");

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Summary");
        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Control sourcesSection = createSummarySection(section);
        section.setClient(sourcesSection);

        refresh();
    }

    public void refresh() {
        try {
            PenroseClient client = project.getClient();
            PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
            PartitionClient partitionClient = partitionManagerClient.getPartitionClient(domain.getName());

            ConnectionManagerClient connectionManagerClient = partitionClient.getConnectionManagerClient();
            ConnectionClient connectionClient = connectionManagerClient.getConnectionClient(FederationClient.JDBC);

            SourceManagerClient sourceManagerClient = partitionClient.getSourceManagerClient();
            SourceClient sourceClient = sourceManagerClient.getSourceClient(NISFederationClient.CACHE_GROUPS);
/*
            PartitionConfigManager partitionConfigManager = project.getPartitionConfigManager();

            PartitionConfig partitionConfig = partitionConfigManager.getPartitionConfig(domain.getName()+"_"+NISFederation.YP);
            SourceConfig sourceConfig = partitionConfig.getSourceConfigManager().getSourceConfig(NISFederation.CACHE_GROUPS);

            Partition partition = nisFederation.getPartition();
            JDBCConnection connection = (JDBCConnection)partition.getConnection(Federation.JDBC);
*/
            String table = (String)sourceClient.getAttribute("TableName");
            //String table = connection.getTableName(sourceConfig);
            String sql = "select count(*) from "+table;

            QueryResponse queryResponse = new QueryResponse() {
                public void add(Object object) throws Exception {
                    ResultSet rs = (ResultSet)object;
                    Integer count = rs.getInt(1);
                    entriesText.setText(count.toString());
                }
            };

            //connection.executeQuery(sql, queryResponse);
            connectionClient.invoke(
                    "executeQuery",
                    new Object[] {
                            sql,
                            queryResponse
                    },
                    new String[] {
                            String.class.getName(),
                            QueryResponse.class.getName()
                    }
            );

        } catch (Exception e) {
            ErrorDialog.open(e);
        }
    }

    public Composite createSummarySection(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        Label entriesLabel = toolkit.createLabel(composite, "Entries:");
        GridData gd = new GridData();
        gd.widthHint = 100;
        entriesLabel.setLayoutData(gd);

        entriesText = toolkit.createLabel(composite, "");
        entriesText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        new Label(composite, SWT.NONE);
        new Label(composite, SWT.NONE);

        Button refreshButton = new Button(composite, SWT.PUSH);
        refreshButton.setText("Refresh");
        gd = new GridData();
        gd.horizontalSpan = 2;
        refreshButton.setLayoutData(gd);

        refreshButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                refresh();
            }
        });

        return composite;
    }
}
