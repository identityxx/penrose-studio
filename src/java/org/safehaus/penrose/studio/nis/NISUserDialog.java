package org.safehaus.penrose.studio.nis;

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
import org.safehaus.penrose.partition.SourceConfig;
import org.safehaus.penrose.ldap.Attributes;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseImage;

import java.util.*;

/**
 * @author Endi S. Dewata
 */
public class NISUserDialog extends Dialog {

    Logger log = Logger.getLogger(getClass());

    public final static int CANCEL = 0;
    public final static int ADD    = 1;
    public final static int REMOVE = 2;

    Shell shell;

    SourceConfig sourceConfig;

    private Attributes attributes = new Attributes();
    private Collection newUidNumbers = new ArrayList();

    Label domainText;
    Label uidText;
    Label origUidNumberText;
    Label newUidNumbersText;

    Button addButton;
    Text newUidNumberText;
    Button removeButton;
    Combo newUidNumbersCombo;
    Text messageText;

    int action;
    Object uidNumber;
    String message;

    public NISUserDialog(Shell parent, int style) {
		super(parent, style);
    }

    public void open() {

        setText("NIS User Editor");
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

        Object uid = attributes.getValue("uid");
        uidText.setText(uid == null ? "" : uid.toString());

        Object uidNumber = attributes.getValue("uidNumber");
        origUidNumberText.setText(uidNumber == null ? "" : uidNumber.toString());

        if (newUidNumbers.isEmpty()) {

            removeButton.setEnabled(false);
            newUidNumbersCombo.setEnabled(false);

        } else {
            StringBuilder sb = new StringBuilder();
            for (Iterator i = newUidNumbers.iterator(); i.hasNext(); ) {
                Object newUidNumber = i.next();
                if (sb.length() > 0) sb.append(", ");
                sb.append(newUidNumber);

                newUidNumbersCombo.add(newUidNumber.toString());
                newUidNumbersCombo.setData(newUidNumber.toString(), newUidNumber);
            }

            newUidNumbersText.setText(sb.toString());
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
        uidLabel.setText("UID:");
        uidLabel.setLayoutData(new GridData());

        uidText = new Label(composite, SWT.NONE);
        uidText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label origUidNumberLabel = new Label(composite, SWT.NONE);
        origUidNumberLabel.setText("Orig. UID Number:");
        origUidNumberLabel.setLayoutData(new GridData());

        origUidNumberText = new Label(composite, SWT.NONE);
        origUidNumberText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label newUidNumbersLabel = new Label(composite, SWT.NONE);
        newUidNumbersLabel.setText("New UID Numbers:");
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

        addButton = new Button(composite, SWT.RADIO);
        addButton.setLayoutData(new GridData());
        addButton.setText("Add new UID number:");
        addButton.setSelection(true);

        newUidNumberText = new Text(composite, SWT.BORDER);
        gd = new GridData();
        gd.widthHint = 100;
        newUidNumberText.setLayoutData(gd);

        newUidNumberText.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent focusEvent) {
                addButton.setSelection(true);
                removeButton.setSelection(false);
            }
        });

        removeButton = new Button(composite, SWT.RADIO);
        removeButton.setText("Remove UID number:");
        removeButton.setLayoutData(new GridData());

        newUidNumbersCombo = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
        gd = new GridData();
        gd.widthHint = 100;
        newUidNumbersCombo.setLayoutData(gd);

        newUidNumbersCombo.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent focusEvent) {
                addButton.setSelection(false);
                removeButton.setSelection(true);
            }
        });


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
                if (addButton.getSelection()) {
                    action = ADD;
                    uidNumber = newUidNumberText.getText();
                } else {
                    action = REMOVE;
                    String s = newUidNumbersCombo.getText();
                    uidNumber = newUidNumbersCombo.getData(s);
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

    public Collection getNewUidNumbers() {
        return newUidNumbers;
    }

    public void addNewUidNumber(Object uidNumber) {
        newUidNumbers.add(uidNumber);
    }

    public void setNewUidNumbers(Collection newUidNumbers) {
        if (this.newUidNumbers == newUidNumbers) return;
        this.newUidNumbers.clear();
        this.newUidNumbers.addAll(newUidNumbers);
    }

    public Object getUidNumber() {
        return uidNumber;
    }

    public void setUidNumber(Object uidNumber) {
        this.uidNumber = uidNumber;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
