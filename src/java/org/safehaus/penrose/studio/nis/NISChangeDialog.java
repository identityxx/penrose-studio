package org.safehaus.penrose.studio.nis;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.graphics.Point;
import org.apache.log4j.Logger;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseImage;

/**
 * @author Endi S. Dewata
 */
public class NISChangeDialog extends Dialog {

    Logger log = Logger.getLogger(getClass());

    public final static int CANCEL = 0;
    public final static int OK     = 1;

    Shell shell;

    Text domainText;
    Text typeText;
    Text targetText;
    Text oldValueText;
    Text newValueText;
    Text messageText;

    int action;

    private String domain;
    private String type;
    private String target;
    private String oldValue;
    private String newValue;
    private String message;

    public NISChangeDialog(Shell parent, int style) {
		super(parent, style);
    }

    public void open() {

        setText("NIS Change Editor");
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
        domainText.setText(domain == null ? "" : domain);
        typeText.setText(type == null ? "" : type);
        targetText.setText(target == null ? "" : target);
        oldValueText.setText(oldValue == null ? "" : oldValue);
        newValueText.setText(newValue == null ? "" : newValue);
        messageText.setText(message == null ? "" : message);
    }

    public void createControl(Shell parent) {
        parent.setLayout(new GridLayout());

        Composite composite = createMainPanel(parent);
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        composite = createMessagePanel(parent);
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
        domainLabel.setText("Domain:");
        GridData gd = new GridData();
        gd.widthHint = 100;
        domainLabel.setLayoutData(gd);

        domainText = new Text(composite, SWT.BORDER);
        domainText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label typeLabel = new Label(composite, SWT.NONE);
        typeLabel.setText("Type:");
        typeLabel.setLayoutData(new GridData());

        typeText = new Text(composite, SWT.BORDER);
        typeText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label targetLabel = new Label(composite, SWT.NONE);
        targetLabel.setText("Target:");
        targetLabel.setLayoutData(new GridData());

        targetText = new Text(composite, SWT.BORDER);
        targetText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label oldValueLabel = new Label(composite, SWT.NONE);
        oldValueLabel.setText("Old value:");
        oldValueLabel.setLayoutData(new GridData());

        oldValueText = new Text(composite, SWT.BORDER);
        oldValueText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label newValueLabel = new Label(composite, SWT.NONE);
        newValueLabel.setText("New value:");
        newValueLabel.setLayoutData(new GridData());

        newValueText = new Text(composite, SWT.BORDER);
        newValueText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

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
                domain = domainText.getText().equals("") ? null : domainText.getText();
                type = typeText.getText().equals("") ? null : typeText.getText();
                target = targetText.getText().equals("") ? null : targetText.getText();
                oldValue = oldValueText.getText().equals("") ? null : oldValueText.getText();
                newValue = newValueText.getText().equals("") ? null : newValueText.getText();
                message = messageText.getText().equals("") ? null : messageText.getText();
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

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
