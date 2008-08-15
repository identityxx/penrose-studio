package org.safehaus.penrose.studio.federation.ldap.editor;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.graphics.Point;
import org.apache.log4j.Logger;
import org.safehaus.penrose.federation.LDAPRepository;
import org.safehaus.penrose.studio.PenroseStudioPlugin;
import org.safehaus.penrose.studio.PenroseImage;

/**
 * @author Endi S. Dewata
 */
public class LDAPRepositoryDialog extends Dialog {

    Logger log = Logger.getLogger(getClass());

    public final static int CANCEL = 0;
    public final static int OK     = 1;

    Shell shell;

    Text urlText;
    Text suffixText;
    Text userText;
    Text passwordText;

    int action;

    private LDAPRepository repository;

    public LDAPRepositoryDialog(Shell parent, int style) {
		super(parent, style);
    }

    public void open() {

        setText("LDAP Repository Editor");
        shell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);

        init();
        reset();

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

    public void reset() {
        String url = repository.getParameter(LDAPRepository.LDAP_URL);
        urlText.setText(url == null ? "" : url);

        String suffix = repository.getParameter(LDAPRepository.LDAP_SUFFIX);
        suffixText.setText(suffix == null ? "" : suffix);

        String user = repository.getParameter(LDAPRepository.LDAP_USER);
        userText.setText(user == null ? "" : user);

        String password = repository.getParameter(LDAPRepository.LDAP_PASSWORD);
        passwordText.setText(password == null ? "" : password);
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

        Label urlLabel = new Label(composite, SWT.NONE);
        urlLabel.setText("URL:");
        GridData gd = new GridData();
        gd.widthHint = 100;
        urlLabel.setLayoutData(gd);

        urlText = new Text(composite, SWT.BORDER);
        urlText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label suffixLabel = new Label(composite, SWT.NONE);
        suffixLabel.setText("Suffix:");
        suffixLabel.setLayoutData(new GridData());

        suffixText = new Text(composite, SWT.BORDER);
        suffixText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label userLabel = new Label(composite, SWT.NONE);
        userLabel.setText("Bind DN:");
        userLabel.setLayoutData(new GridData());

        userText = new Text(composite, SWT.BORDER);
        userText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label passwordLabel = new Label(composite, SWT.NONE);
        passwordLabel.setText("Password:");
        passwordLabel.setLayoutData(new GridData());

        passwordText = new Text(composite, SWT.BORDER | SWT.PASSWORD);
        passwordText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

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

                String url = urlText.getText();
                repository.setParameter(LDAPRepository.LDAP_URL, "".equals(url) ? null : url);

                String suffix = suffixText.getText();
                repository.setParameter(LDAPRepository.LDAP_SUFFIX, "".equals(suffix) ? null : suffix);

                String user = userText.getText();
                repository.setParameter(LDAPRepository.LDAP_USER, "".equals(user) ? null : user);

                String password = passwordText.getText();
                repository.setParameter(LDAPRepository.LDAP_PASSWORD, "".equals(password) ? null : password);

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

    public void setRepository(LDAPRepository repository) {
        this.repository = repository;
    }

    public LDAPRepository getRepository() {
        return repository;
    }
}
