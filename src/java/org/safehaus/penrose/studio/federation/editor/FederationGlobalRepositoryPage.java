package org.safehaus.penrose.studio.federation.editor;

import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.dialogs.MessageDialog;
import org.apache.log4j.Logger;
import org.safehaus.penrose.studio.federation.Federation;
import org.safehaus.penrose.studio.federation.GlobalRepository;
import org.safehaus.penrose.studio.federation.wizard.GlobalRepositoryWizard;
import org.safehaus.penrose.studio.dialog.ErrorDialog;

import javax.naming.Context;
import java.util.Map;
import java.util.LinkedHashMap;

/**
 * @author Endi S. Dewata
 */
public class FederationGlobalRepositoryPage extends FormPage {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    FederationEditor editor;
    Federation federation;

    Label urlText;
    Label bindDnText;
    Label bindPasswordText;

    public FederationGlobalRepositoryPage(FederationEditor editor, Federation federation) {
        super(editor, "GLOBAL_REPOSITORY", "  Global Repository  ");

        this.editor = editor;
        this.federation = federation;
    }

    public void createFormContent(IManagedForm managedForm) {
        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText("Global Repository");

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section ldapSection = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        ldapSection.setText("Global Repository");
        ldapSection.setLayoutData(new GridData(GridData.FILL_BOTH));

        Control ldapControl = createLDAPControl(ldapSection);
        ldapSection.setClient(ldapControl);

        refresh();
    }

    public Composite createLDAPControl(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        composite.setLayout(layout);

        Composite leftPanel = createLeftPanel(composite);
        leftPanel.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite rightPanel = createRightPanel(composite);
        rightPanel.setLayoutData(new GridData(GridData.FILL_VERTICAL));

        return composite;
    }

    public void refresh() {
        try {
            GlobalRepository globalRepository = federation.getGlobalRepository();
            if (globalRepository == null) return;

            String url = globalRepository.getUrl();
            if (url != null) url = url+globalRepository.getSuffix();
            urlText.setText(url == null ? "" : url);

            String bindDn = globalRepository.getUser();
            bindDnText.setText(bindDn == null ? "" : bindDn);

            String bindPassword = globalRepository.getPassword();
            bindPasswordText.setText(bindPassword == null ? "" : "*****");

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }

    public Composite createLeftPanel(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        composite.setLayout(layout);

        Label urlLabel = toolkit.createLabel(composite, "URL:");
        GridData gd = new GridData();
        gd.widthHint = 100;
        urlLabel.setLayoutData(gd);

        urlText = toolkit.createLabel(composite, "");
        urlText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label bindDnLabel = toolkit.createLabel(composite, "Bind DN:");
        bindDnLabel.setLayoutData(new GridData());

        bindDnText = toolkit.createLabel(composite, "");
        bindDnText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label bindPasswordLabel = toolkit.createLabel(composite, "Password:");
        bindPasswordLabel.setLayoutData(new GridData());

        bindPasswordText = toolkit.createLabel(composite, "");
        bindPasswordText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        return composite;
    }

    public Composite createRightPanel(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout());

        Button editButton = toolkit.createButton(composite, "Edit", SWT.PUSH);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.widthHint = 100;
        editButton.setLayoutData(gd);

        editButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    Map<String,String> parameters = new LinkedHashMap<String,String>();

                    GlobalRepository globalRepository = federation.getGlobalRepository();
                    if (globalRepository != null) {
                        String url = globalRepository.getUrl();
                        if (url != null) url = url+globalRepository.getSuffix();

                        parameters.put(Context.PROVIDER_URL, url);
                        parameters.put(Context.SECURITY_PRINCIPAL, globalRepository.getUser());
                        parameters.put(Context.SECURITY_CREDENTIALS, globalRepository.getPassword());
                    }

                    GlobalRepositoryWizard wizard = new GlobalRepositoryWizard();
                    wizard.setParameters(parameters);
                    wizard.init(federation);

                    IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

                    WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
                    dialog.setPageSize(600, 300);

                    if (dialog.open() != Window.OK) return;

                    parameters = wizard.getParameters();

                    if (globalRepository == null) {
                        globalRepository = new GlobalRepository();
                    } else {
                        federation.removeGlobalPartition();
                    }

                    globalRepository.setUrl(parameters.get(Context.PROVIDER_URL));
                    globalRepository.setUser(parameters.get(Context.SECURITY_PRINCIPAL));
                    globalRepository.setPassword(parameters.get(Context.SECURITY_CREDENTIALS));

                    federation.setGlobalRepository(globalRepository);
                    federation.createGlobalPartition(globalRepository);

                    refresh();
                    
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        return composite;
    }

}
