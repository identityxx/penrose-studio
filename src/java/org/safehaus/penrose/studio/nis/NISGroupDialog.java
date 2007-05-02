package org.safehaus.penrose.studio.nis;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.graphics.Point;
import org.apache.log4j.Logger;
import org.safehaus.penrose.partition.SourceConfig;
import org.safehaus.penrose.ldap.Attributes;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseImage;

/**
 * @author Endi S. Dewata
 */
public class NISGroupDialog extends Dialog {

    Logger log = Logger.getLogger(getClass());

    public final static int CANCEL = 0;
    public final static int SET    = 1;
    public final static int CHANGE = 2;
    public final static int REMOVE = 3;

    Shell shell;

    SourceConfig sourceConfig;

    Attributes attributes = new Attributes();
    Object newGidNumber;

    Label domainText;
    Label cnText;
    Label origGidNumberText;
    Label newGidNumberText;

    Button setButton;
    Text gidNumberText;
    Button revertButton;
    Text messageText;

    int action;
    Object gidNumber;
    String message;

    public NISGroupDialog(Shell parent, int style) {
		super(parent, style);
    }

    public void open() {

        setText("NIS Group Editor");
        shell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);

        init();
        reset();

        Point size = new Point(600, 400);
        shell.setSize(size);

        Point l = getParent().getLocation();
        Point s = getParent().getSize();

        shell.setLocation(l.x + (s.x - size.x)/2, l.y + (s.y - size.y)/2);

        shell.setText(getText());
        shell.setImage(PenrosePlugin.getImage(PenroseImage.LOGO16));
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
        String domainName = (String)attributes.getValue("domain");
        domainText.setText(domainName == null ? "" : domainName.toString());

        Object cn = attributes.getValue("cn");
        cnText.setText(cn == null ? "" : cn.toString());

        Object gidNumber = attributes.getValue("gidNumber");
        origGidNumberText.setText(gidNumber == null ? "" : gidNumber.toString());

        if (newGidNumber == null) {
            revertButton.setEnabled(false);

        } else {
            newGidNumberText.setText(newGidNumber.toString());
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

        new Label(composite, SWT.NONE);

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
        uidLabel.setText("Group:");
        uidLabel.setLayoutData(new GridData());

        cnText = new Label(composite, SWT.NONE);
        cnText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label origUidNumberLabel = new Label(composite, SWT.NONE);
        origUidNumberLabel.setText("Original GID Number:");
        origUidNumberLabel.setLayoutData(new GridData());

        origGidNumberText = new Label(composite, SWT.NONE);
        origGidNumberText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label newUidNumberLabel = new Label(composite, SWT.NONE);
        newUidNumberLabel.setText("New GID Number:");
        newUidNumberLabel.setLayoutData(new GridData());

        newGidNumberText = new Label(composite, SWT.NONE);
        newGidNumberText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

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
        setButton.setText("Set new GID number:");
        setButton.setSelection(true);

        gidNumberText = new Text(composite, SWT.BORDER);
        gd = new GridData();
        gd.widthHint = 100;
        gidNumberText.setLayoutData(gd);

        gidNumberText.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent focusEvent) {
                setButton.setSelection(true);
                revertButton.setSelection(false);
            }
        });

        revertButton = new Button(composite, SWT.RADIO);
        revertButton.setText("Revert to the original GID number.");
        revertButton.setLayoutData(new GridData());

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
                    if (newGidNumber == null) {
                        action = SET;
                    } else {
                        action = CHANGE;
                    }
                    gidNumber = gidNumberText.getText();
                } else {
                    action = REMOVE;
                    gidNumber = attributes.getValue("gidNumber");
                }

                message = messageText.getText();

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

    public SourceConfig getSourceConfig() {
        return sourceConfig;
    }

    public void setSourceConfig(SourceConfig sourceConfig) {
        this.sourceConfig = sourceConfig;
    }

    public Attributes getAttributes() {
        return attributes;
    }

    public void setAttributes(Attributes attributes) {
        this.attributes.set(attributes);
    }

    public Object getNewGidNumber() {
        return newGidNumber;
    }

    public void setNewGidNumber(Object newGidNumber) {
        this.newGidNumber = newGidNumber;
    }

    public Object getGidNumber() {
        return gidNumber;
    }

    public void setGidNumber(Object gidNumber) {
        this.gidNumber = gidNumber;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
