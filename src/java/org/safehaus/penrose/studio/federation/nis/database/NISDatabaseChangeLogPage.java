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
import org.safehaus.penrose.studio.federation.nis.NISFederation;
import org.safehaus.penrose.studio.federation.nis.NISRepository;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.partition.Partitions;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.source.Source;
import org.safehaus.penrose.ldap.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * @author Endi S. Dewata
 */
public class NISDatabaseChangeLogPage extends FormPage {

    public DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    NISDatabaseEditor editor;
    NISFederation nisFederation;
    NISRepository domain;

    Table table;
    Text changesText;

    public NISDatabaseChangeLogPage(NISDatabaseEditor editor) {
        super(editor, "CHANGE_LOG", "  Change Log  ");

        this.editor = editor;
        this.nisFederation = editor.getNisTool();
        this.domain = editor.getDomain();
    }

    public void createFormContent(IManagedForm managedForm) {
        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText("Change Log");

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Change Log");
        section.setLayoutData(new GridData(GridData.FILL_BOTH));

        Control sourcesSection = createPartitionsSection(section);
        section.setClient(sourcesSection);

        refresh();
    }

    public Composite createPartitionsSection(Composite parent) {

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
        tc.setWidth(100);
        tc.setText("Number");

        tc = new TableColumn(table, SWT.NONE);
        tc.setWidth(350);
        tc.setText("Target DN");

        tc = new TableColumn(table, SWT.NONE);
        tc.setWidth(50);
        tc.setText("Type");

        table.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                showChanges();
            }
        });

        Composite links = toolkit.createComposite(leftPanel);
        links.setLayout(new RowLayout());
        links.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Hyperlink selectAllLink = toolkit.createHyperlink(links, "Select All", SWT.NONE);

        selectAllLink.addHyperlinkListener(new HyperlinkAdapter() {
            public void linkActivated(HyperlinkEvent event) {
                table.selectAll();
                showChanges();
            }
        });

        Hyperlink selectNoneLink = toolkit.createHyperlink(links, "Select None", SWT.NONE);

        selectNoneLink.addHyperlinkListener(new HyperlinkAdapter() {
            public void linkActivated(HyperlinkEvent event) {
                table.deselectAll();
                showChanges();
            }
        });

        changesText = toolkit.createText(leftPanel, "", SWT.BORDER | SWT.READ_ONLY  | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.heightHint = 150;
        changesText.setLayoutData(gd);

        Composite rightPanel = toolkit.createComposite(composite);
        rightPanel.setLayout(new GridLayout());
        gd = new GridData(GridData.FILL_VERTICAL);
        gd.verticalSpan = 2;
        gd.widthHint = 120;
        rightPanel.setLayoutData(gd);

        Button removeButton = new Button(rightPanel, SWT.PUSH);
        removeButton.setText("Remove");
        removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                try {
                    if (table.getSelectionCount() == 0) return;

                    boolean confirm = MessageDialog.openQuestion(
                            editor.getSite().getShell(),
                            "Removing Change Log",
                            "Are you sure?"
                    );

                    if (!confirm) return;

                    TableItem[] items = table.getSelection();

                    Partitions partitions = nisFederation.getPartitions();
                    Partition partition = partitions.getPartition(domain.getName());

                    Source changelog = partition.getSource("changelog");

                    for (TableItem ti : items) {
                        SearchResult result = (SearchResult)ti.getData();
                        try {
                            changelog.delete(result.getDn());
                            
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }
                    }

                    PenroseStudio penroseStudio = PenroseStudio.getInstance();
                    penroseStudio.notifyChangeListeners();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    MessageDialog.openError(editor.getSite().getShell(), "Action Failed", e.getMessage());
                }

                refresh();
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

            Partitions partitions = nisFederation.getPartitions();
            Partition partition = partitions.getPartition(domain.getName());

            Source changelog = partition.getSource("changelog");

            SearchRequest request = new SearchRequest();

            SearchResponse response = new SearchResponse() {
                public void add(SearchResult result) {

                    Attributes attributes = result.getAttributes();
                    String changeNumber = attributes.getValue("changeNumber").toString();
                    String targetDn = (String)attributes.getValue("targetDN");
                    String changeType = (String)attributes.getValue("changeType");

                    TableItem ti = new TableItem(table, SWT.NONE);

                    ti.setText(0, changeNumber);
                    ti.setText(1, targetDn);
                    ti.setText(2, changeType);

                    ti.setData(result);
                }
            };

            changelog.search(request, response);

            table.select(indices);

            showChanges();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            MessageDialog.openError(editor.getSite().getShell(), "Action Failed", e.getMessage());
        }
    }

    public void showChanges() {
        
        if (table.getSelectionCount() !=  1) {
            changesText.setText("");
            return;
        }

        TableItem ti = table.getSelection()[0];

        SearchResult result = (SearchResult)ti.getData();
        Attributes attributes = result.getAttributes();

        String changeType = (String)attributes.getValue("changeType");
        if ("add".equals(changeType)) {
            String changes = (String)attributes.getValue("changes");
            changesText.setText(changes);

        } else if ("modify".equals(changeType)) {
            String changes = (String)attributes.getValue("changes");
            changesText.setText(changes);

        } else if ("modrdn".equals(changeType)) {
            String newRdn = (String)attributes.getValue("newRDN");
            boolean deleteOldRdn = Boolean.parseBoolean((String)attributes.getValue("deleteOldRDN"));

            StringBuilder sb = new StringBuilder();
            sb.append("newRDN: ");
            sb.append(newRdn);
            sb.append("\n");
            sb.append("deleteOldRdn: ");
            sb.append(deleteOldRdn);

        } else if ("delete".equals(changeType)) {
            changesText.setText("");
        }
    }
}
