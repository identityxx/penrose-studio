package org.safehaus.penrose.studio.nis.dialog;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.graphics.Point;

import org.apache.log4j.Logger;

import org.safehaus.penrose.studio.PenroseStudioPlugin;
import org.safehaus.penrose.studio.PenroseImage;

/**
 * @author Endi S. Dewata
 */
public class NISUserDialog extends Dialog {

    Logger log = Logger.getLogger(getClass());

    public final static int CANCEL = 0;
    public final static int SET    = 1;
    public final static int CHANGE = 2;
    public final static int REMOVE = 3;

    Shell shell;

    Label domainText;
    Label uidText;
    Label origUidNumberText;
    Label newUidNumbersText;

    Button setButton;
    Text newUidNumberText;
    Button revertButton;
    Text messageText;

    int action;

    private String domain;
    private String uid;
    private Integer origUidNumber;
    private Integer newUidNumber;

    Integer uidNumber;
    String message;

    public NISUserDialog(Shell parent, int style) {
		super(parent, style);
    }

    public void open() {

        setText("NIS User Editor");
        shell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);

        init();

        Point size = new Point(600, 400);
        shell.setSize(size);

        Point l = getParent().getLocation();
        Point s = getParent().getSize();

        shell.setLocation(l.x + (s.x - size.x)/2, l.y + (s.y - size.y)/2);

        shell.setText(getText());
        shell.setImage(PenroseStudioPlugin.getImage(PenroseImage.LOGO16));
        shell.open();

        refresh();

        Display display = getParent().getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
    }

    public void init() {
        createControl(shell);
    }

    public void refresh() {
        domainText.setText(domain == null ? "" : domain);
        uidText.setText(uid == null ? "" : uid);
        origUidNumberText.setText(origUidNumber == null ? "" : origUidNumber.toString());

        if (newUidNumber == null) {
            revertButton.setEnabled(false);

        } else {
            newUidNumbersText.setText(newUidNumber.toString());
        }
    }

    public void createControl(Shell parent) {
        parent.setLayout(new GridLayout());

        Composite composite = createInfoPanel(parent);
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        new Label(composite, SWT.NONE);

        composite = createActionPanel(parent);
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        composite = createMessagePanel(parent);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        composite = createButtonsPanel(parent);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalAlignment = GridData.END;
        composite.setLayoutData(gd);
    }

    public Composite createInfoPanel(Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));

        Label domainLabel = new Label(composite, SWT.NONE);
        domainLabel.setText("Domain:");
        GridData gd = new GridData();
        gd.widthHint = 150;
        domainLabel.setLayoutData(gd);

        domainText = new Label(composite, SWT.NONE);
        domainText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label uidLabel = new Label(composite, SWT.NONE);
        uidLabel.setText("UID:");
        uidLabel.setLayoutData(new GridData());

        uidText = new Label(composite, SWT.NONE);
        uidText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label origUidNumberLabel = new Label(composite, SWT.NONE);
        origUidNumberLabel.setText("Original UID number:");
        origUidNumberLabel.setLayoutData(new GridData());

        origUidNumberText = new Label(composite, SWT.NONE);
        origUidNumberText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label newUidNumbersLabel = new Label(composite, SWT.NONE);
        newUidNumbersLabel.setText("New UID number:");
        newUidNumbersLabel.setLayoutData(new GridData());

        newUidNumbersText = new Label(composite, SWT.NONE);
        newUidNumbersText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        return composite;
    }

    public Composite createActionPanel(Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));

        Label actionLabel = new Label(composite, SWT.NONE);
        actionLabel.setText("Action:");
        GridData gd = new GridData();
        gd.horizontalSpan = 2;
        actionLabel.setLayoutData(gd);

        setButton = new Button(composite, SWT.RADIO);
        setButton.setLayoutData(new GridData());
        setButton.setText("Set new UID number:");
        setButton.setSelection(true);

        newUidNumberText = new Text(composite, SWT.BORDER);
        gd = new GridData();
        gd.widthHint = 100;
        newUidNumberText.setLayoutData(gd);

        newUidNumberText.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent focusEvent) {
                setButton.setSelection(true);
                revertButton.setSelection(false);
            }
        });

        revertButton = new Button(composite, SWT.RADIO);
        revertButton.setText("Revert to the original UID number.");
        gd = new GridData();
        gd.horizontalSpan = 2;
        revertButton.setLayoutData(gd);

        return composite;
    }

    public Composite createMessagePanel(Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout());

        Label messageLabel = new Label(composite, SWT.NONE);
        messageLabel.setText("Message:");
        messageLabel.setLayoutData(new GridData());

        messageText = new Text(composite, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        messageText.setLayoutData(new GridData(GridData.FILL_BOTH));

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
                if (setButton.getSelection()) {
                    if (newUidNumber == null) {
                        action = SET;
                    } else {
                        action = CHANGE;
                    }
                    uidNumber = new Integer(newUidNumberText.getText());
                } else {
                    action = REMOVE;
                    uidNumber = origUidNumber;
                }

                message = "".equals(messageText.getText()) ? null : messageText.getText();

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

    public Integer getNewUidNumber() {
        return newUidNumber;
    }

    public void setNewUidNumber(Integer newUidNumber) {
        this.newUidNumber = newUidNumber;
    }

    public Integer getUidNumber() {
        return uidNumber;
    }

    public void setUidNumber(Integer uidNumber) {
        this.uidNumber = uidNumber;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Integer getOrigUidNumber() {
        return origUidNumber;
    }

    public void setOrigUidNumber(Integer origUidNumber) {
        this.origUidNumber = origUidNumber;
    }
}
