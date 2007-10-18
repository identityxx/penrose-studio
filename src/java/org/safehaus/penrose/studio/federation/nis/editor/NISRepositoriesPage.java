package org.safehaus.penrose.studio.federation.nis.editor;

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
import org.eclipse.jface.wizard.WizardDialog;
import org.apache.log4j.Logger;
import org.safehaus.penrose.studio.federation.nis.NISFederation;
import org.safehaus.penrose.studio.federation.nis.NISDomain;
import org.safehaus.penrose.studio.federation.nis.wizard.NISRepositoryWizard;
import org.safehaus.penrose.studio.nis.dialog.NISUserDialog;
import org.safehaus.penrose.studio.nis.dialog.NISDomainDialog;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.management.PenroseClient;

/**
 * @author Endi S. Dewata
 */
public class NISRepositoriesPage extends FormPage {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    NISEditor editor;
    NISFederation nisFederation;

    Table table;

    public NISRepositoriesPage(NISEditor editor, NISFederation nisFederation) {
        super(editor, "DOMAINS", "  Domains  ");

        this.editor = editor;
        this.nisFederation = nisFederation;
    }

    public void createFormContent(IManagedForm managedForm) {
        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText("Domains");

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Domains");
        section.setLayoutData(new GridData(GridData.FILL_BOTH));

        Control sourcesSection = createDomainsSection(section);
        section.setClient(sourcesSection);

        refresh();
    }

    public Composite createDomainsSection(Composite parent) {

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
        tc.setText("Name");

        tc = new TableColumn(table, SWT.NONE);
        tc.setWidth(150);
        tc.setText("NIS Domain Name");

        tc = new TableColumn(table, SWT.NONE);
        tc.setWidth(250);
        tc.setText("LDAP Suffix");

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
        gd.verticalSpan = 2;
        gd.widthHint = 120;
        rightPanel.setLayoutData(gd);

        Button addButton = new Button(rightPanel, SWT.PUSH);
        addButton.setText("Add");
        addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                try {
                    NISRepositoryWizard wizard = new NISRepositoryWizard(nisFederation);
                    WizardDialog dialog = new WizardDialog(editor.getSite().getShell(), wizard);
                    dialog.setPageSize(600, 300);
                    dialog.open();

                    PenroseStudio penroseStudio = PenroseStudio.getInstance();
                    penroseStudio.notifyChangeListeners();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    MessageDialog.openError(editor.getSite().getShell(), "Action Failed", e.getMessage());
                }

                refresh();
            }
        });

        Button editButton = new Button(rightPanel, SWT.PUSH);
        editButton.setText("Edit");
        editButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        editButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                try {
                    if (table.getSelectionCount() == 0) return;

                    TableItem item = table.getSelection()[0];

                    NISDomain domain = (NISDomain)item.getData();
                    String domainName = domain.getName();

                    NISDomainDialog dialog = new NISDomainDialog(editor.getSite().getShell(), SWT.NONE);
                    dialog.setDomain(domain);
                    dialog.open();

                    int action = dialog.getAction();
                    if (action == NISUserDialog.CANCEL) return;

                    nisFederation.updateRepository(domain);

                    PenroseStudio penroseStudio = PenroseStudio.getInstance();
                    penroseStudio.notifyChangeListeners();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    MessageDialog.openError(editor.getSite().getShell(), "Action Failed", e.getMessage());
                }

                refresh();
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
                            "Removing Repository",
                            "Are you sure?"
                    );

                    if (!confirm) return;

                    Project project = nisFederation.getProject();
                    PenroseClient penroseClient = project.getClient();

                    int index = table.getSelectionIndex();

                    TableItem[] items = table.getSelection();
                    for (TableItem ti : items) {
                        NISDomain repository = (NISDomain)ti.getData();

                        try {
                            penroseClient.stopPartition(repository.getName());
                            nisFederation.removePartition(repository);

                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }

                        try {
                            nisFederation.removeDatabase(repository);

                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }

                        try {
                            nisFederation.removePartitionConfig(repository);
                            project.removeDirectory("partitions/"+repository.getName());

                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }

                        try {
                            nisFederation.removeRepository(repository.getName());

                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }
                    }

                    PenroseStudio penroseStudio = PenroseStudio.getInstance();
                    penroseStudio.notifyChangeListeners();

                    table.select(index);

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

            for (NISDomain domain : nisFederation.getRepositories()) {

                TableItem ti = new TableItem(table, SWT.NONE);

                ti.setText(0, domain.getName());
                ti.setText(1, domain.getFullName());
                ti.setText(2, domain.getSuffix());

                ti.setData(domain);
            }

            table.select(indices);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            MessageDialog.openError(editor.getSite().getShell(), "Action Failed", e.getMessage());
        }
    }

}
