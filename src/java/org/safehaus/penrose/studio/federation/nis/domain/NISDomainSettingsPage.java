package org.safehaus.penrose.studio.federation.nis.domain;

import org.apache.log4j.Logger;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.jface.dialogs.MessageDialog;
import org.safehaus.penrose.federation.repository.NISDomain;
import org.safehaus.penrose.studio.federation.nis.NISFederation;
import org.safehaus.penrose.studio.federation.nis.editor.NISDomainDialog;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.studio.nis.dialog.NISUserDialog;

/**
 * @author Endi S. Dewata
 */
public class NISDomainSettingsPage extends FormPage {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    Label domainText;
    Label serverText;

    Label ypSuffixText;
    Label ypEnabledText;

    Label nisSuffixText;
    Label nisEnabledText;

    Label nssSuffixText;
    Label nssEnabledText;

    NISDomainEditor editor;
    NISFederation nisFederation;
    NISDomain domain;

    Project project;

    public NISDomainSettingsPage(NISDomainEditor editor) {
        super(editor, "SETTINGS", "  Settings  ");

        this.editor = editor;
        this.nisFederation = editor.getNisFederation();
        this.domain = editor.getDomain();

        this.project = nisFederation.getProject();
    }

    public void createFormContent(IManagedForm managedForm) {
        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText("Settings");

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section domainSection = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        domainSection.setText("NIS Domain");
        domainSection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Control domainControl = createDomainsSection(domainSection);
        domainSection.setClient(domainControl);

        new Label(body, SWT.NONE);

        Section ypSection = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        ypSection.setText("YP Partition");
        ypSection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Control ypControl = createYpPanel(ypSection);
        ypSection.setClient(ypControl);

        new Label(body, SWT.NONE);

        Section nisSection = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        nisSection.setText("NIS Partition");
        nisSection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Control nisControl = createNisControl(nisSection);
        nisSection.setClient(nisControl);

        new Label(body, SWT.NONE);

        Section nssSection = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        nssSection.setText("NSS Partition");
        nssSection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Control nssControl = createNssPanel(nssSection);
        nssSection.setClient(nssControl);

        refresh();
    }

    public Composite createDomainsSection(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        composite.setLayout(layout);

        Composite leftPanel = createSettingsLeftPanel(composite);
        leftPanel.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite rightPanel = createSettingsRightPanel(composite);
        rightPanel.setLayoutData(new GridData(GridData.FILL_VERTICAL));

        return composite;
    }

    public Composite createSettingsLeftPanel(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        composite.setLayout(layout);

        Label domainLabel = toolkit.createLabel(composite, "Domain:");
        domainLabel.setLayoutData(new GridData());
        GridData gd = new GridData();
        gd.widthHint = 100;
        domainLabel.setLayoutData(gd);

        domainText = toolkit.createLabel(composite, "");
        domainText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label serverLabel = toolkit.createLabel(composite, "Server:");
        serverLabel.setLayoutData(new GridData());

        serverText = toolkit.createLabel(composite, "");
        serverText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        return composite;
    }

    public Composite createSettingsRightPanel(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout());

        Button editButton = toolkit.createButton(composite, "Edit", SWT.PUSH);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.widthHint = 100;
        editButton.setLayoutData(gd);

        editButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    NISDomainDialog dialog = new NISDomainDialog(editor.getSite().getShell(), SWT.NONE);
                    dialog.setDomain(domain);
                    dialog.open();

                    int action = dialog.getAction();
                    if (action == NISUserDialog.CANCEL) return;

                    nisFederation.updateRepository(domain);

                    refresh();
                    
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        return composite;
    }

    public Composite createNisControl(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        composite.setLayout(layout);

        Composite left = createNisLeftPanel(composite);
        left.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite right = createNisRightPanel(composite);
        GridData gd = new GridData(GridData.FILL_VERTICAL);
        gd.widthHint = 100;
        right.setLayoutData(gd);

        return composite;
    }

    public Composite createNisLeftPanel(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        composite.setLayout(layout);


        Label enabledLabel = toolkit.createLabel(composite, "Enabled:");
        GridData gd = new GridData();
        gd.widthHint = 100;
        enabledLabel.setLayoutData(gd);

        nisEnabledText = toolkit.createLabel(composite, "");
        nisEnabledText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        toolkit.createLabel(composite, "Suffix:");

        nisSuffixText = toolkit.createLabel(composite, "");
        nisSuffixText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        return composite;
    }

    public Composite createNisRightPanel(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        composite.setLayout(layout);

        Button createButton = toolkit.createButton(composite, "Create", SWT.PUSH);
        createButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        createButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                try {
                    boolean confirm = MessageDialog.openQuestion(
                            editor.getSite().getShell(),
                            "Creating Partition",
                            "Are you sure?"
                    );

                    if (!confirm) return;

                    nisFederation.createNisPartition(domain);

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        Button removeButton = toolkit.createButton(composite, "Remove", SWT.PUSH);
        removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                try {
                    boolean confirm = MessageDialog.openQuestion(
                            editor.getSite().getShell(),
                            "Removing Partition",
                            "Are you sure?"
                    );

                    if (!confirm) return;

                    nisFederation.removeNisPartition(domain);

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        return composite;
    }

    public Composite createYpPanel(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        composite.setLayout(layout);

        Composite left = createYpLeftPanel(composite);
        left.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite right = createYpRightPanel(composite);
        GridData gd = new GridData(GridData.FILL_VERTICAL);
        gd.widthHint = 100;
        right.setLayoutData(gd);

        return composite;
    }

    public Composite createYpLeftPanel(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        composite.setLayout(layout);

        Label enabledLabel = toolkit.createLabel(composite, "Enabled:");
        GridData gd = new GridData();
        gd.widthHint = 100;
        enabledLabel.setLayoutData(gd);

        ypEnabledText = toolkit.createLabel(composite, "");
        ypEnabledText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        toolkit.createLabel(composite, "Suffix:");

        ypSuffixText = toolkit.createLabel(composite, "");
        ypSuffixText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        return composite;
    }

    public Composite createYpRightPanel(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        composite.setLayout(layout);

        Button createButton = toolkit.createButton(composite, "Create", SWT.PUSH);
        createButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        createButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                try {
                    boolean confirm = MessageDialog.openQuestion(
                            editor.getSite().getShell(),
                            "Creating Partition",
                            "Are you sure?"
                    );

                    if (!confirm) return;

                    nisFederation.createYpPartition(domain);

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        Button removeButton = toolkit.createButton(composite, "Remove", SWT.PUSH);
        removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                try {
                    boolean confirm = MessageDialog.openQuestion(
                            editor.getSite().getShell(),
                            "Removing Partition",
                            "Are you sure?"
                    );

                    if (!confirm) return;

                    nisFederation.removeYpPartition(domain);

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        return composite;
    }

    public Composite createNssPanel(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        composite.setLayout(layout);

        Composite left = createNssLeftPanel(composite);
        left.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite right = createNssRightPanel(composite);
        GridData gd = new GridData(GridData.FILL_VERTICAL);
        gd.widthHint = 100;
        right.setLayoutData(gd);

        return composite;
    }

    public Composite createNssLeftPanel(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        composite.setLayout(layout);

        Label enabledLabel = toolkit.createLabel(composite, "Enabled:");
        GridData gd = new GridData();
        gd.widthHint = 100;
        enabledLabel.setLayoutData(gd);

        nssEnabledText = toolkit.createLabel(composite, "");
        nssEnabledText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        toolkit.createLabel(composite, "Suffix:");

        nssSuffixText = toolkit.createLabel(composite, "");
        nssSuffixText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        return composite;
    }

    public Composite createNssRightPanel(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        composite.setLayout(layout);

        Button createButton = toolkit.createButton(composite, "Create", SWT.PUSH);
        createButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        createButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                try {
                    boolean confirm = MessageDialog.openQuestion(
                            editor.getSite().getShell(),
                            "Creating Partition",
                            "Are you sure?"
                    );

                    if (!confirm) return;

                    nisFederation.createNssPartition(domain);

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        Button removeButton = toolkit.createButton(composite, "Remove", SWT.PUSH);
        removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                try {
                    boolean confirm = MessageDialog.openQuestion(
                            editor.getSite().getShell(),
                            "Removing Partition",
                            "Are you sure?"
                    );

                    if (!confirm) return;

                    nisFederation.removeNssPartition(domain);

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        return composite;
    }

    public void refresh() {
        try {
            String fullName = domain.getFullName();
            domainText.setText(fullName == null ? "" : fullName);

            String server = domain.getServer();
            serverText.setText(server == null ? "" : server);

            String nisSuffix = domain.getNisSuffix();
            nisSuffixText.setText(nisSuffix == null ? "" : nisSuffix);

            nisEnabledText.setText(domain.isNisEnabled() ? "Yes" : "No");

            String ypSuffix = domain.getYpSuffix();
            ypSuffixText.setText(ypSuffix == null ? "" : ypSuffix);

            ypEnabledText.setText(domain.isYpEnabled() ? "Yes" : "No");

            String nssSuffix = domain.getNssSuffix();
            nssSuffixText.setText(nssSuffix == null ? "" : nssSuffix);

            nssEnabledText.setText(domain.isNssEnabled() ? "Yes" : "No");

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }
}
