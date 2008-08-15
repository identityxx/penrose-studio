package org.safehaus.penrose.studio.federation.nis.database;

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
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.jface.dialogs.MessageDialog;
import org.apache.log4j.Logger;
import org.safehaus.penrose.federation.NISDomain;
import org.safehaus.penrose.federation.NISFederationClient;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.management.PenroseClient;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.source.SourceClient;
import org.safehaus.penrose.partition.PartitionManagerClient;

import java.util.*;

/**
 * @author Endi S. Dewata
 */
public class NISDatabaseTablesPage extends FormPage {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    Table table;

    NISDatabaseEditor editor;
    NISDomain domain;
    NISFederationClient nisFederation;

    Project project;
    PenroseClient penroseClient;
    PartitionClient partitionClient;

    Collection<String> sources = new ArrayList<String>();

    public NISDatabaseTablesPage(NISDatabaseEditor editor) throws Exception {
        super(editor, "TABLES", "  Tables  ");

        this.editor = editor;

        project = editor.project;
        nisFederation = editor.getNisFederation();
        domain = editor.getDomain();

        penroseClient = project.getClient();
        PartitionManagerClient partitionManagerClient = penroseClient.getPartitionManagerClient();
        partitionClient = partitionManagerClient.getPartitionClient(domain.getName()+"_"+ NISDomain.DB);

        sources.addAll(partitionClient.getSourceNames());
    }

    public void createFormContent(IManagedForm managedForm) {
        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText("Tables");

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Tables");
        section.setLayoutData(new GridData(GridData.FILL_BOTH));

        Control mainSection = createMainSection(section);
        section.setClient(mainSection);
    }

    public void setActive(boolean b) {
        super.setActive(b);
        if (b) refresh();
    }

    public Composite createMainSection(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        Composite leftPanel = toolkit.createComposite(composite);
        leftPanel.setLayout(new GridLayout());
        leftPanel.setLayoutData(new GridData(GridData.FILL_BOTH));

        table = new Table(leftPanel, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        table.setLayoutData(new GridData(GridData.FILL_BOTH));
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        TableColumn tc = new TableColumn(table, SWT.NONE);
        tc.setWidth(250);
        tc.setText("Name");

        tc = new TableColumn(table, SWT.NONE);
        tc.setWidth(100);
        tc.setText("Entries");
        tc.setAlignment(SWT.RIGHT);

        Composite links = toolkit.createComposite(leftPanel);
        links.setLayout(new RowLayout());
        links.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Hyperlink selectAllLink = toolkit.createHyperlink(links, "Select All", SWT.NONE);

        selectAllLink.addHyperlinkListener(new HyperlinkAdapter() {
            public void linkActivated(HyperlinkEvent event) {
                table.selectAll();
            }
        });

        Hyperlink selectNoneLink = toolkit.createHyperlink(links, "Select None", SWT.NONE);

        selectNoneLink.addHyperlinkListener(new HyperlinkAdapter() {
            public void linkActivated(HyperlinkEvent event) {
                table.deselectAll();
            }
        });

        Composite rightPanel = toolkit.createComposite(composite);
        rightPanel.setLayout(new GridLayout());
        GridData gd = new GridData(GridData.FILL_VERTICAL);
        gd.widthHint = 120;
        rightPanel.setLayoutData(gd);

        Button createButton = new Button(rightPanel, SWT.PUSH);
        createButton.setText("Create");
        createButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        createButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                try {
                    if (table.getSelectionCount() == 0) return;

                    boolean confirm = MessageDialog.openQuestion(
                            editor.getSite().getShell(),
                            "Creating Table",
                            "Are you sure?"
                    );

                    if (!confirm) return;

                    TableItem[] items = table.getSelection();

                    for (TableItem item : items) {
                        String sourceName = (String)item.getData();
                        
                        SourceClient sourceClient = partitionClient.getSourceClient(sourceName);
                        sourceClient.create();

                        item.setText(1, getStatus(sourceClient));
                    }

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        Button clearButton = new Button(rightPanel, SWT.PUSH);
        clearButton.setText("Clear");
        clearButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        clearButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                try {
                    if (table.getSelectionCount() == 0) return;

                    boolean confirm = MessageDialog.openQuestion(
                            editor.getSite().getShell(),
                            "Clearing Table",
                            "Are you sure?"
                    );

                    if (!confirm) return;

                    TableItem[] items = table.getSelection();

                    for (TableItem item : items) {
                        String sourceName = (String)item.getData();

                        SourceClient sourceClient = partitionClient.getSourceClient(sourceName);
                        sourceClient.clear();

                        item.setText(1, getStatus(sourceClient));
                    }

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        Button removeButton = new Button(rightPanel, SWT.PUSH);
        removeButton.setText("Remove");
        removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                try {
                    if (table.getSelectionCount() == 0) return;

                    boolean confirm = MessageDialog.openQuestion(
                            editor.getSite().getShell(),
                            "Removing Table",
                            "Are you sure?"
                    );

                    if (!confirm) return;

                    TableItem[] items = table.getSelection();

                    for (TableItem item : items) {
                        String sourceName = (String)item.getData();

                        SourceClient sourceClient = partitionClient.getSourceClient(sourceName);
                        sourceClient.drop();

                        item.setText(1, getStatus(sourceClient));
                    }

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        new Label(rightPanel, SWT.NONE);

        Button refreshButton = new Button(rightPanel, SWT.PUSH);
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
            int[] indices = table.getSelectionIndices();

            table.removeAll();

            for (String sourceName : sources) {
                log.debug("Checking table for "+sourceName+".");

                SourceClient sourceClient = partitionClient.getSourceClient(sourceName);

                TableItem item = new TableItem(table, SWT.NONE);
                item.setText(0, sourceName);
                item.setText(1, getStatus(sourceClient));
                item.setData(sourceName);
            }

            table.select(indices);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }

    public String getStatus(SourceClient sourceClient) {
        try {
            return sourceClient.getCount().toString();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return "N/A";
        }
    }
}
