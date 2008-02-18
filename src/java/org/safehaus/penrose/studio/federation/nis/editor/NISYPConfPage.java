package org.safehaus.penrose.studio.federation.nis.editor;

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
import org.apache.log4j.Logger;
import org.safehaus.penrose.studio.federation.nis.NISFederation;
import org.safehaus.penrose.federation.repository.NISDomain;
import org.safehaus.penrose.studio.dialog.ErrorDialog;

/**
 * @author Endi S. Dewata
 */
public class NISYPConfPage extends FormPage {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    NISEditor editor;
    NISFederation nisFederation;

    Text text;

    public NISYPConfPage(NISEditor editor, NISFederation nisFederation) {
        super(editor, "YP_CONF", "  yp.conf  ");

        this.editor = editor;
        this.nisFederation = nisFederation;
    }

    public void createFormContent(IManagedForm managedForm) {
        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText("yp.conf");

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("/etc/yp.conf");
        section.setLayoutData(new GridData(GridData.FILL_BOTH));

        Control sourcesSection = createSection(section);
        section.setClient(sourcesSection);

        refresh();
    }

    public Composite createSection(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        text = toolkit.createText(composite, "", SWT.BORDER | SWT.MULTI | SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL);
        text.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite rightPanel = toolkit.createComposite(composite);
        rightPanel.setLayout(new GridLayout());
        GridData gd = new GridData(GridData.FILL_VERTICAL);
        gd.verticalSpan = 2;
        gd.widthHint = 120;
        rightPanel.setLayoutData(gd);

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
            StringBuilder sb = new StringBuilder();
            
            sb.append("# /etc/yp.conf - ypbind configuration file\n");
            sb.append("# Valid entries are\n");
            sb.append("#\n");
            sb.append("# domain NISDOMAIN server HOSTNAME\n");
            sb.append("#       Use server HOSTNAME for the domain NISDOMAIN.\n");
            sb.append("#\n");
            sb.append("# domain NISDOMAIN broadcast\n");
            sb.append("#       Use  broadcast  on  the local net for domain NISDOMAIN\n");
            sb.append("#\n");
            sb.append("# domain NISDOMAIN slp\n");
            sb.append("#       Query local SLP server for ypserver supporting NISDOMAIN\n");
            sb.append("#\n");
            sb.append("# ypserver HOSTNAME\n");
            sb.append("#       Use server HOSTNAME for the  local  domain.  The\n");
            sb.append("#       IP-address of server must be listed in /etc/hosts.\n");
            sb.append("#\n");
            sb.append("# broadcast\n");
            sb.append("#       If no server for the default domain is specified or\n");
            sb.append("#       none of them is rechable, try a broadcast call to\n");
            sb.append("#       find a server.\n");
            sb.append("#\n");
            sb.append("\n");
            
            for (NISDomain domain : nisFederation.getRepositories()) {
                sb.append("domain ");
                sb.append(domain.getFullName());
                sb.append(" server ");
                sb.append(domain.getServer());
                sb.append("\n");
            }

            text.setText(sb.toString());

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }

}
