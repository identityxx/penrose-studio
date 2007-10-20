package org.safehaus.penrose.studio.federation.ldap.repository;

import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.SWT;
import org.apache.log4j.Logger;
import org.safehaus.penrose.studio.federation.ldap.LDAPRepository;

/**
 * @author Endi S. Dewata
 */
public class LDAPFederationSettingsPage extends FormPage {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    LDAPRepositoryEditor editor;
    LDAPRepository repository;

    public LDAPFederationSettingsPage(LDAPRepositoryEditor editor) {
        super(editor, "SETTINGS", "  Settings  ");

        this.editor = editor;
        this.repository = editor.getRepository();
    }

    public void createFormContent(IManagedForm managedForm) {
        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText("Settings");

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("LDAP Repository");
        section.setLayoutData(new GridData(GridData.FILL_BOTH));

        Control sourcesSection = createDomainsSection(section);
        section.setClient(sourcesSection);
    }

    public Composite createDomainsSection(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        Label urlLabel = toolkit.createLabel(composite, "URL:");
        urlLabel.setLayoutData(new GridData());
        GridData gd = new GridData();
        gd.widthHint = 100;
        urlLabel.setLayoutData(gd);

        Label urlText = toolkit.createLabel(composite, repository.getUrl());
        urlText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label suffixLabel = toolkit.createLabel(composite, "Suffix:");
        suffixLabel.setLayoutData(new GridData());

        String suffix = repository.getSuffix();
        if (suffix == null) suffix = "";

        Label suffixText = toolkit.createLabel(composite, suffix);
        suffixText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label bindDnLabel = toolkit.createLabel(composite, "Bind DN:");
        bindDnLabel.setLayoutData(new GridData());

        Label bindDnText = toolkit.createLabel(composite, repository.getUser());
        bindDnText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label bindPasswordLabel = toolkit.createLabel(composite, "Password:");
        bindPasswordLabel.setLayoutData(new GridData());

        Label bindPasswordText = toolkit.createLabel(composite, "*****");
        bindPasswordText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        return composite;
    }

}
