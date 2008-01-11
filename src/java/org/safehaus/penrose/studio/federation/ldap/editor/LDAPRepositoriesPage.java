package org.safehaus.penrose.studio.federation.ldap.editor;

import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.window.Window;
import org.apache.log4j.Logger;
import org.safehaus.penrose.studio.federation.ldap.LDAPFederation;
import org.safehaus.penrose.studio.federation.ldap.LDAPRepository;
import org.safehaus.penrose.studio.federation.ldap.wizard.LDAPRepositoryWizard;
import org.safehaus.penrose.studio.federation.ldap.wizard.LDAPRepositoryEditorWizard;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.management.PenroseClient;

import javax.naming.Context;
import java.util.Map;
import java.util.LinkedHashMap;

/**
 * @author Endi S. Dewata
 */
public class LDAPRepositoriesPage extends FormPage {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    LDAPEditor editor;
    LDAPFederation ldapFederation;
    Project project;

    Table table;

    public LDAPRepositoriesPage(LDAPEditor editor, LDAPFederation ldapFederation) {
        super(editor, "REPOSITORIES", "  Repositories  ");

        this.editor = editor;
        this.ldapFederation = ldapFederation;
        this.project = ldapFederation.getProject();
    }

    public void createFormContent(IManagedForm managedForm) {
        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText("Repositories");

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Repositories");
        section.setLayoutData(new GridData(GridData.FILL_BOTH));

        Control sourcesSection = createRepositoriesSection(section);
        section.setClient(sourcesSection);

        refresh();
    }

    public Composite createRepositoriesSection(Composite parent) {

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
        tc.setText("Server");

        tc = new TableColumn(table, SWT.NONE);
        tc.setWidth(250);
        tc.setText("Suffix");

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
                    LDAPRepositoryWizard wizard = new LDAPRepositoryWizard(ldapFederation);
                    WizardDialog dialog = new WizardDialog(editor.getSite().getShell(), wizard);
                    dialog.setPageSize(600, 300);
                    dialog.open();

                    PenroseStudio penroseStudio = PenroseStudio.getInstance();
                    penroseStudio.notifyChangeListeners();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
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
                    if (table.getSelectionCount() != 1) return;

                    TableItem ti = table.getSelection()[0];

                    LDAPRepository repository = (LDAPRepository)ti.getData();

                    LDAPRepositoryEditorWizard wizard = new LDAPRepositoryEditorWizard();
                    wizard.setWindowTitle("LDAP Repository");

                    wizard.setSuffix(repository.getSuffix());

                    Map<String,String> parameters = new LinkedHashMap<String,String>();
                    parameters.put(Context.PROVIDER_URL, repository.getUrl());
                    parameters.put(Context.SECURITY_PRINCIPAL, repository.getUser());
                    parameters.put(Context.SECURITY_CREDENTIALS, repository.getPassword());
                    wizard.setParameters(parameters);

                    wizard.setProject(project);

                    IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

                    WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
                    dialog.setPageSize(600, 300);

                    if (dialog.open() != Window.OK) return;
/*
                    LDAPRepositoryDialog dialog = new LDAPRepositoryDialog(editor.getSite().getShell(), SWT.NONE);
                    dialog.setRepository(repository);
                    dialog.open();

                    int action = dialog.getAction();
                    if (action == LDAPRepositoryDialog.CANCEL) return;
*/
                    repository.setUrl(parameters.get(Context.PROVIDER_URL));
                    repository.setUser(parameters.get(Context.SECURITY_PRINCIPAL));
                    repository.setPassword(parameters.get(Context.SECURITY_CREDENTIALS));
                    repository.setSuffix(wizard.getSuffix());

                    PenroseClient penroseClient = project.getClient();

                    penroseClient.stopPartition(repository.getName());
                    ldapFederation.removePartition(repository);

                    ldapFederation.removePartitionConfig(repository.getName());
                    project.removeDirectory("partitions/"+repository.getName());

                    ldapFederation.updateRepository(repository);

                    ldapFederation.createPartitionConfig(repository);
                    project.upload("partitions/"+repository.getName());

                    penroseClient.startPartition(repository.getName());

                    PenroseStudio penroseStudio = PenroseStudio.getInstance();
                    penroseStudio.notifyChangeListeners();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
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

                    Project project = ldapFederation.getProject();
                    PenroseClient penroseClient = project.getClient();

                    int index = table.getSelectionIndex();

                    TableItem[] items = table.getSelection();
                    for (TableItem ti : items) {
                        LDAPRepository repository = (LDAPRepository)ti.getData();

                        penroseClient.stopPartition(repository.getName());
                        ldapFederation.removePartition(repository);

                        ldapFederation.removePartitionConfig(repository.getName());
                        project.removeDirectory("partitions/"+repository.getName());

                        ldapFederation.removeRepository(repository.getName());
                    }

                    PenroseStudio penroseStudio = PenroseStudio.getInstance();
                    penroseStudio.notifyChangeListeners();

                    table.select(index);

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
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

            for (LDAPRepository repository : ldapFederation.getRepositories()) {

                TableItem ti = new TableItem(table, SWT.NONE);

                ti.setText(0, repository.getName());
                ti.setText(1, repository.getServer());
                ti.setText(2, repository.getSuffix());

                ti.setData(repository);
            }

            table.select(indices);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }

}
