package org.safehaus.penrose.studio.federation.nis.domain;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
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
import org.safehaus.penrose.federation.NISDomain;
import org.safehaus.penrose.federation.NISFederationClient;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.studio.project.Project;

/**
 * @author Endi S. Dewata
 */
public class NISDomainPartitionsPage extends FormPage {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    Label ypEnabledText;
    Label ypSuffixText;
    Label ypTemplateText;

    Label nisEnabledText;
    Label nisSuffixText;
    Label nisTemplateText;

    Label nssEnabledText;
    Label nssSuffixText;
    Label nssTemplateText;

    NISDomainEditor editor;
    NISFederationClient nisFederation;
    NISDomain domain;

    Project project;

    public NISDomainPartitionsPage(NISDomainEditor editor) {
        super(editor, "PARTITIONS", "  Partitions  ");

        this.editor = editor;
        this.project = editor.project;
        this.nisFederation = editor.getNisFederation();
        this.domain = editor.getDomain();
    }

    public void createFormContent(IManagedForm managedForm) {
        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText("Settings");

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

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

        toolkit.createLabel(composite, "Template:");

        nisTemplateText = toolkit.createLabel(composite, "");
        nisTemplateText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

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

                    nisFederation.createNISPartition(domain.getName());

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

                    nisFederation.removeNISPartition(domain.getName());

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

        toolkit.createLabel(composite, "Template:");

        ypTemplateText = toolkit.createLabel(composite, "");
        ypTemplateText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

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

                    nisFederation.createYPPartition(domain.getName());

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

                    nisFederation.removeYPPartition(domain.getName());

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

        toolkit.createLabel(composite, "Template:");

        nssTemplateText = toolkit.createLabel(composite, "");
        nssTemplateText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

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

                    nisFederation.createNSSPartition(domain.getName());

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

                    nisFederation.removeNSSPartition(domain.getName());

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
            ypEnabledText.setText(domain.getBooleanParameter(NISDomain.YP_ENABLED) ? "Yes" : "No");

            String ypSuffix = domain.getParameter(NISDomain.YP_SUFFIX);
            ypSuffixText.setText(ypSuffix == null ? "" : ypSuffix);

            String ypTemplate = domain.getParameter(NISDomain.YP_TEMPLATE);
            ypTemplateText.setText(ypTemplate == null ? "" : ypTemplate);

            nisEnabledText.setText(domain.getBooleanParameter(NISDomain.NIS_ENABLED) ? "Yes" : "No");

            String nisSuffix = domain.getParameter(NISDomain.NIS_SUFFIX);
            nisSuffixText.setText(nisSuffix == null ? "" : nisSuffix);

            String nisTemplate = domain.getParameter(NISDomain.NIS_TEMPLATE);
            nisTemplateText.setText(nisTemplate == null ? "" : nisTemplate);

            nssEnabledText.setText(domain.getBooleanParameter(NISDomain.NSS_ENABLED) ? "Yes" : "No");

            String nssSuffix = domain.getParameter(NISDomain.NSS_SUFFIX);
            nssSuffixText.setText(nssSuffix == null ? "" : nssSuffix);

            String nssTemplate = domain.getParameter(NISDomain.NSS_TEMPLATE);
            nssTemplateText.setText(nssTemplate == null ? "" : nssTemplate);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }
}