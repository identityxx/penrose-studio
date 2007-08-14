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
import org.safehaus.penrose.nis.NISDomain;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.source.Source;
import org.safehaus.penrose.jdbc.adapter.JDBCAdapter;
import org.safehaus.penrose.jdbc.JDBCClient;
import org.safehaus.penrose.jdbc.Assignment;
import org.safehaus.penrose.jdbc.QueryResponse;
import org.safehaus.penrose.studio.nis.NISTool;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.sql.ResultSet;

/**
 * @author Endi S. Dewata
 */
public class NISCachePage extends FormPage {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    Table cacheTable;

    NISDomainEditor editor;
    NISDomain domain;
    NISTool nisTool;

    Map<String,String> sourceNames = new TreeMap<String,String>();

    public NISCachePage(NISDomainEditor editor) {
        super(editor, "CACHE", "  Cache  ");

        this.editor = editor;
        this.domain = editor.getDomain();
        this.nisTool = editor.getNisTool();

        sourceNames.put("Users", "cache_users");
        sourceNames.put("Shadows", "cache_shadow");
        sourceNames.put("Hosts", "cache_hosts");
        sourceNames.put("Groups", "cache_groups");
        sourceNames.put("Services", "cache_services");
        sourceNames.put("RPCs", "cache_rpcs");
        sourceNames.put("NetIDs", "cache_netids");
        sourceNames.put("Protocols", "cache_protocols");
        sourceNames.put("Aliases", "cache_aliases");
        sourceNames.put("Netgroups", "cache_netgroups");
        sourceNames.put("Ethernets", "cache_ethers");
        sourceNames.put("BootParams", "cache_bootparams");
        sourceNames.put("Networks", "cache_networks");
        sourceNames.put("AutomountMaps", "cache_automountMap");
        sourceNames.put("Automounts", "cache_automount");
    }

    public void createFormContent(IManagedForm managedForm) {
        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText("Cache");

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Cache");
        section.setLayoutData(new GridData(GridData.FILL_BOTH));

        Control mainSection = createMainSection(section);
        section.setClient(mainSection);
    }

    public void setActive(boolean b) {
        super.setActive(b);
        refresh();
    }

    public Composite createMainSection(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        Composite leftPanel = toolkit.createComposite(composite);
        leftPanel.setLayout(new GridLayout());
        leftPanel.setLayoutData(new GridData(GridData.FILL_BOTH));

        cacheTable = new Table(leftPanel, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        cacheTable.setLayoutData(new GridData(GridData.FILL_BOTH));
        cacheTable.setHeaderVisible(true);
        cacheTable.setLinesVisible(true);

        TableColumn tc = new TableColumn(cacheTable, SWT.NONE);
        tc.setWidth(150);
        tc.setText("Name");

        tc = new TableColumn(cacheTable, SWT.NONE);
        tc.setWidth(100);
        tc.setText("Entries");
        tc.setAlignment(SWT.RIGHT);

        Composite buttons = toolkit.createComposite(composite);
        buttons.setLayout(new GridLayout());
        GridData gd = new GridData(GridData.FILL_VERTICAL);
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

        return composite;
    }

    public void refresh() {
        try {
            cacheTable.removeAll();
            
            Partition partition = nisTool.getPartitions().getPartition(domain.getPartition());

            for (final String label : sourceNames.keySet()) {
                String sourceName = sourceNames.get(label);
                log.debug("Checking cache "+label+" ("+sourceName+").");

                Source source = partition.getSource(sourceName);
                if (source == null) continue;

                JDBCAdapter adapter = (JDBCAdapter)source.getConnection().getAdapter();
                JDBCClient client = adapter.getClient();

                String table = client.getTableName(source);
                String sql = "select count(*) from "+table;

                Collection<Assignment> assignments = new ArrayList<Assignment>();

                QueryResponse queryResponse = new QueryResponse() {
                    public void add(Object object) throws Exception {
                        ResultSet rs = (ResultSet)object;
                        Integer count = rs.getInt(1);

                        TableItem ti = new TableItem(cacheTable, SWT.NONE);
                        ti.setText(0, label);
                        ti.setText(1, count.toString());
                    }
                };

                client.executeQuery(sql, assignments, queryResponse);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            MessageDialog.openError(editor.getSite().getShell(), "Refresh Failed", e.getMessage());
        }
    }

}
