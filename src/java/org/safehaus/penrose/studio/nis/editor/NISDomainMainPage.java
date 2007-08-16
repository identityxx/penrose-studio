package org.safehaus.penrose.studio.nis.editor;

import org.apache.log4j.Logger;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.safehaus.penrose.nis.NISDomain;

/**
 * @author Endi S. Dewata
 */
public class NISDomainMainPage extends FormPage {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    NISDomainEditor editor;
    NISDomain domain;

    public NISDomainMainPage(NISDomainEditor editor) {
        super(editor, "DOMAIN", "  Domain  ");

        this.editor = editor;
        this.domain = editor.getDomain();
    }

    public void createFormContent(IManagedForm managedForm) {
        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText("Domain");

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Domain");
        section.setLayoutData(new GridData(GridData.FILL_BOTH));

        Control sourcesSection = createDomainsSection(section);
        section.setClient(sourcesSection);
    }

    public Composite createDomainsSection(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        Label partitionLabel = toolkit.createLabel(composite, "Name:");
        GridData gd = new GridData();
        gd.widthHint = 100;
        partitionLabel.setLayoutData(gd);

        Label partitionText = toolkit.createLabel(composite, domain.getName());
        partitionText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label domainLabel = toolkit.createLabel(composite, "NIS Domain:");
        domainLabel.setLayoutData(new GridData());

        Label domainText = toolkit.createLabel(composite, domain.getFullName());
        domainText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label serverLabel = toolkit.createLabel(composite, "NIS Server:");
        serverLabel.setLayoutData(new GridData());

        Label serverText = toolkit.createLabel(composite, domain.getServer());
        serverText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label suffixLabel = toolkit.createLabel(composite, "LDAP Suffix:");
        suffixLabel.setLayoutData(new GridData());

        Label suffixText = toolkit.createLabel(composite, domain.getSuffix());
        suffixText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        return composite;
    }

}
