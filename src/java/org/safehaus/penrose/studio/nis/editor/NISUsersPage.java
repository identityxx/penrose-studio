package org.safehaus.penrose.studio.nis.editor;

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
import org.safehaus.penrose.studio.nis.NISTool;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.source.SourceConfig;
import org.safehaus.penrose.nis.NISDomain;
import org.safehaus.penrose.jdbc.adapter.JDBCAdapter;
import org.safehaus.penrose.jdbc.JDBCClient;
import org.safehaus.penrose.jdbc.Assignment;
import org.safehaus.penrose.jdbc.QueryResponse;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.partition.PartitionConfigs;
import org.safehaus.penrose.partition.PartitionConfig;
import org.safehaus.penrose.connection.Connection;

import java.util.*;
import java.sql.ResultSet;

/**
 * @author Endi S. Dewata
 */
public class NISUsersPage extends FormPage {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    Label entriesText;

    NISUsersEditor editor;
    NISDomain domain;
    NISTool nisTool;

    public NISUsersPage(NISUsersEditor editor) {
        super(editor, "USERS", "  Users  ");

        this.editor = editor;
        domain = editor.getDomain();
        nisTool = editor.getNisTool();
    }

    public void createFormContent(IManagedForm managedForm) {
        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText("NIS Users");

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
            Project project = nisTool.getProject();
            PartitionConfigs partitionConfigs = project.getPartitionConfigs();

            PartitionConfig partitionConfig = partitionConfigs.getPartitionConfig(domain.getName());
            SourceConfig sourceConfig = partitionConfig.getSourceConfigs().getSourceConfig(NISTool.CACHE_USERS);

            Partition partition = nisTool.getNisPartition();
            Connection connection = partition.getConnection(NISTool.NIS_CONNECTION_NAME);
            JDBCAdapter adapter = (JDBCAdapter)connection.getAdapter();
            JDBCClient client = adapter.getClient();

            String table = client.getTableName(sourceConfig);
            String sql = "select count(*) from "+table;

            Collection<Assignment> assignments = new ArrayList<Assignment>();

            QueryResponse queryResponse = new QueryResponse() {
                public void add(Object object) throws Exception {
                    ResultSet rs = (ResultSet)object;
                    Integer count = rs.getInt(1);
                    entriesText.setText(count.toString());
                }
            };

            client.executeQuery(sql, assignments, queryResponse);

        } catch (Exception e) {
            MessageDialog.openError(editor.getSite().getShell(), "Refresh Failed", e.getMessage());
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
