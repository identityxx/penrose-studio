package org.safehaus.penrose.studio.federation.nis.editor;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.graphics.Point;
import org.apache.log4j.Logger;
import org.safehaus.penrose.studio.PenroseStudioPlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.federation.NISDomain;

/**
 * @author Endi S. Dewata
 */
public class NISDomainDialog extends Dialog {

    Logger log = Logger.getLogger(getClass());

    public final static int CANCEL = 0;
    public final static int OK     = 1;

    Shell shell;

    Text domainText;
    Text serverText;

    Button ypEnabledButton;
    Text ypSuffixText;

    Button nisEnabledButton;
    Text nisSuffixText;

    Button nssEnabledButton;
    Text nssSuffixText;

    int action;

    private NISDomain domain;

    public NISDomainDialog(Shell parent, int style) {
		super(parent, style);
    }

    public void open() {

        setText("NIS Domain Editor");
        shell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);

        init();
        refresh();

        Point size = new Point(600, 400);
        shell.setSize(size);

        Point l = getParent().getLocation();
        Point s = getParent().getSize();

        shell.setLocation(l.x + (s.x - size.x)/2, l.y + (s.y - size.y)/2);

        shell.setText(getText());
        shell.setImage(PenroseStudioPlugin.getImage(PenroseImage.LOGO16));
        shell.open();

        Display display = getParent().getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
    }

    public void init() {
        createControl(shell);
    }

    public void refresh() {
        String fullName = domain.getParameter(NISDomain.NIS_DOMAIN);
        domainText.setText(fullName == null ? "" : fullName);

        String server = domain.getParameter(NISDomain.NIS_SERVER);
        serverText.setText(server == null ? "" : server);

        boolean ypEnabled = domain.getBooleanParameter(NISDomain.YP_ENABLED);
        ypEnabledButton.setSelection(ypEnabled);

        String ypSuffix = domain.getParameter(NISDomain.YP_SUFFIX);
        ypSuffixText.setText(ypSuffix == null ? "" : ypSuffix);

        boolean nisEnabled = domain.getBooleanParameter(NISDomain.NIS_ENABLED);
        nisEnabledButton.setSelection(nisEnabled);

        String nisSuffix = domain.getParameter(NISDomain.NIS_SUFFIX);
        nisSuffixText.setText(nisSuffix == null ? "" : nisSuffix);

        boolean nssEnabled = domain.getBooleanParameter(NISDomain.NSS_ENABLED);
        nssEnabledButton.setSelection(nssEnabled);

        String nssSuffix = domain.getParameter(NISDomain.NSS_SUFFIX);
        nssSuffixText.setText(nssSuffix == null ? "" : nssSuffix);
    }

    public void createControl(Shell parent) {
        parent.setLayout(new GridLayout());

        Composite composite = createMainPanel(parent);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        composite = createButtonsPanel(parent);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalAlignment = GridData.END;
        composite.setLayoutData(gd);
    }

    public Composite createMainPanel(Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));

        Label domainLabel = new Label(composite, SWT.NONE);
        domainLabel.setText("NIS Domain:");
        GridData gd = new GridData();
        gd.widthHint = 100;
        domainLabel.setLayoutData(gd);

        domainText = new Text(composite, SWT.BORDER);
        domainText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label serverLabel = new Label(composite, SWT.NONE);
        serverLabel.setText("NIS Server:");

        serverText = new Text(composite, SWT.BORDER);
        serverText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        new Label(composite, SWT.NONE);
        new Label(composite, SWT.NONE);

        Label ypEnabledLabel = new Label(composite, SWT.NONE);
        ypEnabledLabel.setText("YP Enabled:");

        ypEnabledButton = new Button(composite, SWT.CHECK);

        Label ypSuffixLabel = new Label(composite, SWT.NONE);
        ypSuffixLabel.setText("YP Suffix:");

        ypSuffixText = new Text(composite, SWT.BORDER);
        ypSuffixText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        new Label(composite, SWT.NONE);
        new Label(composite, SWT.NONE);

        Label nisEnabledLabel = new Label(composite, SWT.NONE);
        nisEnabledLabel.setText("NIS Enabled:");

        nisEnabledButton = new Button(composite, SWT.CHECK);

        Label nisSuffixLabel = new Label(composite, SWT.NONE);
        nisSuffixLabel.setText("NIS Suffix:");

        nisSuffixText = new Text(composite, SWT.BORDER);
        nisSuffixText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        new Label(composite, SWT.NONE);
        new Label(composite, SWT.NONE);
                
        Label nssEnabledLabel = new Label(composite, SWT.NONE);
        nssEnabledLabel.setText("NSS Enabled:");

        nssEnabledButton = new Button(composite, SWT.CHECK);

        Label nssSuffixLabel = new Label(composite, SWT.NONE);
        nssSuffixLabel.setText("NSS Suffix:");

        nssSuffixText = new Text(composite, SWT.BORDER);
        nssSuffixText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        return composite;
    }

    public Composite createButtonsPanel(Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new RowLayout());

        Button cancelButton = new Button(composite, SWT.PUSH);
        cancelButton.setText("  Cancel  ");

        cancelButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                action = CANCEL;
                shell.close();
            }
        });

        Button okButton = new Button(composite, SWT.PUSH);
        okButton.setText("  OK  ");

        okButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {

                String fullName = domainText.getText();
                domain.setParameter(NISDomain.NIS_DOMAIN, "".equals(fullName) ? null : fullName);

                String server = serverText.getText();
                domain.setParameter(NISDomain.NIS_SERVER, "".equals(server) ? null : server);

                domain.setParameter(NISDomain.NIS_ENABLED, nisEnabledButton.getSelection());

                String nisSuffix = nisSuffixText.getText();
                domain.setParameter(NISDomain.NIS_SUFFIX, "".equals(nisSuffix) ? null : nisSuffix);

                domain.setParameter(NISDomain.YP_ENABLED, ypEnabledButton.getSelection());

                String ypSuffix = ypSuffixText.getText();
                domain.setParameter(NISDomain.YP_SUFFIX, "".equals(ypSuffix) ? null : ypSuffix);

                domain.setParameter(NISDomain.NSS_ENABLED, nssEnabledButton.getSelection());

                String nssSuffix = nssSuffixText.getText();
                domain.setParameter(NISDomain.NSS_SUFFIX, "".equals(nssSuffix) ? null : nssSuffix);

                action = OK;
                shell.close();
            }
        });

        return composite;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public void setDomain(NISDomain domain) {
        this.domain = domain;
    }

    public NISDomain getDomain() {
        return domain;
    }
}
