package org.safehaus.penrose.studio.ldap.connection;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.graphics.Point;
import org.apache.log4j.Logger;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;

/**
 * @author Endi S. Dewata
 */
public class JNDIConnectionURLDialog extends Dialog {

    Logger log = Logger.getLogger(getClass());

    public final static int CANCEL = 0;
    public final static int OK     = 1;

    Shell shell;

    List urlList;

    Combo protocolCombo;

    Text hostText;
    Text portText;

    int action;

    String bindDn;
    String bindPassword;
    String url;

    public JNDIConnectionURLDialog(Shell parent, int style) {
		super(parent, style);
    }

    public void open() {

        setText("LDAP Server");
        shell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);

        init();
        reset();

        Point size = new Point(600, 400);
        shell.setSize(size);

        Point l = getParent().getLocation();
        Point s = getParent().getSize();

        shell.setLocation(l.x + (s.x - size.x)/2, l.y + (s.y - size.y)/2);

        shell.setText(getText());
        shell.setImage(PenroseStudio.getImage(PenroseImage.LOGO));
        shell.open();

        Display display = getParent().getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
    }

    public void init() {
        createControl(shell);
    }

    public void reset() {
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

        Label protocolLabel = new Label(composite, SWT.NONE);
        protocolLabel.setText("Protocol:");

        protocolCombo = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
        protocolCombo.add("ldap");
        protocolCombo.add("ldaps");
        protocolCombo.select(0);
        protocolCombo.setLayoutData(new GridData());

        Label hostLabel = new Label(composite, SWT.NONE);
        hostLabel.setText("Hostname:");

        hostText = new Text(composite, SWT.BORDER);
        hostText.setText("localhost");
        hostText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label portLabel = new Label(composite, SWT.NONE);
        portLabel.setText("Port:");

        portLabel.setLayoutData(new GridData());

        portText = new Text(composite, SWT.BORDER);
        portText.setText("389");
        
        GridData gd = new GridData();
        gd.widthHint = 50;
        portText.setLayoutData(gd);

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

                url = createURL();

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

    public String getBindDn() {
        return bindDn;
    }

    public void setBindDn(String bindDn) {
        this.bindDn = bindDn;
    }

    public String getBindPassword() {
        return bindPassword;
    }

    public void setBindPassword(String bindPassword) {
        this.bindPassword = bindPassword;
    }

    public String createURL() {
        String protocol = protocolCombo.getText();
        String host = hostText.getText();
        int port = Integer.parseInt(portText.getText());

        StringBuilder sb = new StringBuilder();
        sb.append(protocol);
        sb.append("://");
        sb.append(host);

        if (port != 0 &&
                ("ldap".equals(protocol) && 389 != port ||
                "ldaps".equals(protocol) && 636 != port)
        ) {
            sb.append(":");
            sb.append(port);
        }

        sb.append("/");

        return sb.toString();
    }

    public String getURL() {
        return url;
    }
}