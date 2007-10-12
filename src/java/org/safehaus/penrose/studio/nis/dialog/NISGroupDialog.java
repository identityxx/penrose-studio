package org.safehaus.penrose.studio.nis.dialog;

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
import org.safehaus.penrose.source.SourceConfig;
import org.safehaus.penrose.studio.PenroseStudioPlugin;
import org.safehaus.penrose.studio.PenroseImage;

import java.util.Collection;
import java.util.ArrayList;

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

    Label domainText;
    Label nameText;
    Label origGidNumberText;
    Label newGidNumberText;
    List membersList;

    Button setButton;
    Text gidNumberText;
    Button revertButton;
    Text messageText;

    int action;

    private String domain;
    private String name;
    private Integer origGidNumber;
    private Integer newGidNumber;
    private Collection<String> members = new ArrayList<String>();

    Integer gidNumber;
    String message;

    public NISGroupDialog(Shell parent, int style) {
		super(parent, style);
    }

    public void open() {

        setText("NIS Group Editor");
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
        domainText.setText(getDomain() == null ? "" : getDomain());
        nameText.setText(getName() == null ? "" : getName());
        origGidNumberText.setText(getOrigGidNumber() == null ? "" : getOrigGidNumber().toString());

        for (String member : members) {
            membersList.add(member);
        }

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

        Composite leftPanel = new Composite(composite, SWT.NONE);
        leftPanel.setLayoutData(new GridData(GridData.FILL_BOTH));
        leftPanel.setLayout(new GridLayout(2, false));

        Label domainLabel = new Label(leftPanel, SWT.NONE);
        domainLabel.setText("Domain:");
        GridData gd = new GridData();
        gd.widthHint = 150;
        domainLabel.setLayoutData(gd);

        domainText = new Label(leftPanel, SWT.NONE);
        domainText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label uidLabel = new Label(leftPanel, SWT.NONE);
        uidLabel.setText("Group:");
        uidLabel.setLayoutData(new GridData());

        nameText = new Label(leftPanel, SWT.NONE);
        nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label origUidNumberLabel = new Label(leftPanel, SWT.NONE);
        origUidNumberLabel.setText("Original GID number:");
        origUidNumberLabel.setLayoutData(new GridData());

        origGidNumberText = new Label(leftPanel, SWT.NONE);
        origGidNumberText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label newUidNumberLabel = new Label(leftPanel, SWT.NONE);
        newUidNumberLabel.setText("New GID number:");
        newUidNumberLabel.setLayoutData(new GridData());

        newGidNumberText = new Label(leftPanel, SWT.NONE);
        newGidNumberText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Composite rightPanel = new Composite(composite, SWT.NONE);
        gd = new GridData(GridData.FILL_VERTICAL);
        gd.widthHint = 200;
        rightPanel.setLayoutData(gd);
        rightPanel.setLayout(new GridLayout());

        Label membersLabel = new Label(rightPanel, SWT.NONE);
        membersLabel.setText("Members:");
        membersLabel.setLayoutData(new GridData());

        membersList = new List(rightPanel, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
        membersList.setLayoutData(new GridData(GridData.FILL_BOTH));

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
                    if (newGidNumber == null) {
                        action = SET;
                    } else {
                        action = CHANGE;
                    }
                    gidNumber = new Integer(gidNumberText.getText());
                } else {
                    action = REMOVE;
                    gidNumber = origGidNumber;
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

    public Integer getNewGidNumber() {
        return newGidNumber;
    }

    public void setNewGidNumber(Integer newGidNumber) {
        this.newGidNumber = newGidNumber;
    }

    public Integer getGidNumber() {
        return gidNumber;
    }

    public void setGidNumber(Integer gidNumber) {
        this.gidNumber = gidNumber;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getOrigGidNumber() {
        return origGidNumber;
    }

    public void setOrigGidNumber(Integer origGidNumber) {
        this.origGidNumber = origGidNumber;
    }

    public Collection<String> getMembers() {
        return members;
    }

    public void addMember(String member) {
        members.add(member);
    }

    public void setMembers(Collection<String> members) {
        if (this.members == members) return;
        this.members.clear();
        this.members.addAll(members);
    }
}
