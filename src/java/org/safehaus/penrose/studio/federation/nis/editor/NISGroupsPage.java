package org.safehaus.penrose.studio.federation.nis.editor;

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
import org.apache.log4j.Logger;
import org.safehaus.penrose.studio.federation.nis.NISFederation;
import org.safehaus.penrose.studio.federation.nis.conflict.NISGroupsEditor;
import org.safehaus.penrose.studio.federation.Federation;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.source.SourceConfig;
import org.safehaus.penrose.studio.federation.nis.NISDomain;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.jdbc.Assignment;
import org.safehaus.penrose.jdbc.QueryResponse;
import org.safehaus.penrose.jdbc.connection.JDBCConnection;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.partition.PartitionConfigs;
import org.safehaus.penrose.partition.PartitionConfig;

import java.util.Collection;
import java.util.ArrayList;
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
    NISDomain domain;
    NISFederation nisFederation;

    public NISGroupsPage(NISGroupsEditor editor) {
        super(editor, "GROUPS", "  Groups  ");

        this.editor = editor;
        domain = editor.getDomain();
        nisFederation = editor.getNisTool();
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
            Project project = nisFederation.getProject();
            PartitionConfigs partitionConfigs = project.getPartitionConfigs();

            PartitionConfig partitionConfig = partitionConfigs.getPartitionConfig(domain.getName()+"_"+NISFederation.YP);
            SourceConfig sourceConfig = partitionConfig.getSourceConfigs().getSourceConfig(NISFederation.CACHE_GROUPS);

            Partition partition = nisFederation.getPartition();
            JDBCConnection connection = (JDBCConnection)partition.getConnection(Federation.JDBC);

            String table = connection.getTableName(sourceConfig);
            String sql = "select count(*) from "+table;

            Collection<Assignment> assignments = new ArrayList<Assignment>();

            QueryResponse queryResponse = new QueryResponse() {
                public void add(Object object) throws Exception {
                    ResultSet rs = (ResultSet)object;
                    Integer count = rs.getInt(1);
                    entriesText.setText(count.toString());
                }
            };

            connection.executeQuery(sql, assignments, queryResponse);

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
