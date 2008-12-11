package org.safehaus.penrose.studio.dialog;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.custom.CLabel;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;

/**
 * @author Endi Sukma Dewata
 */
public abstract class ProgressBarDialog extends Dialog {

    Shell shell;

    Label processMessageLabel;
    Button cancelButton;
    Composite cancelComposite;
    Composite progressBarComposite;
    CLabel message;
    ProgressBar progressBar;

    boolean closed;

    int executeTime = 50;
    String processMessage = "process......";
    boolean mayCancel = true;
    int processBarStyle = SWT.SMOOTH;

    public ProgressBarDialog(Shell parent) {
        super(parent);

        shell = new Shell(parent, SWT.TITLE | SWT.PRIMARY_MODAL);
        //shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);

        createControl();
    }

    public void open() {

        Point size = new Point(400, 200);
        shell.setSize(size);

        Point l = getParent().getLocation();
        Point s = getParent().getSize();

        shell.setLocation(l.x + (s.x - size.x)/2, l.y + (s.y - size.y)/2);

        shell.setText(getText());
        shell.setImage(PenroseStudio.getImage(PenroseImage.LOGO));

        shell.open();
        //shell.layout();

        Display display = getParent().getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
    }

    public void createControl() {

        shell.setLayout(new GridLayout());

        Composite composite = new Composite(shell, SWT.NONE);
        composite.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
        composite.setLayout(new GridLayout());

        message = new CLabel(composite, SWT.NONE);
        message.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
        message.setText(processMessage);

        progressBarComposite = new Composite(shell, SWT.NONE);
        progressBarComposite.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));
        progressBarComposite.setLayout(new FillLayout());

        progressBar = new ProgressBar(progressBarComposite, processBarStyle);
        progressBar.setMaximum(executeTime);

        processMessageLabel = new Label(shell, SWT.NONE);
        processMessageLabel.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));

        Label separator = new Label(shell, SWT.HORIZONTAL | SWT.SEPARATOR);
        separator.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));

        cancelComposite = new Composite(shell, SWT.NONE);
        cancelComposite.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));

        cancelComposite.setLayout(new GridLayout(2, false));

        cancelButton = new Button(cancelComposite, SWT.NONE);
        cancelButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                closed = true;
            }
        });

        cancelButton.setLayoutData(new GridData(78, SWT.DEFAULT));
        cancelButton.setText("cancel");
        cancelButton.setEnabled(this.mayCancel);
    }

    public abstract void initGauge();

    protected abstract String process(int times);


    public void setMayCancel(boolean mayCancel) {
        this.mayCancel = mayCancel;
    }

    public void setExecuteTime(int executeTime) {
        this.executeTime = executeTime;
    }

    public void setProcessMessage(String processInfo) {
        this.processMessage = processInfo;
    }

    public void cleanUp() {
    }

    public void doBefore() {
    }

    public void doAfter() {
    }

    public void setProcessBarStyle(boolean style) {
        if (style)
            this.processBarStyle = SWT.SMOOTH;
        else
            this.processBarStyle = SWT.NONE;
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    public void setProgressBar(ProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    public Label getProcessMessageLabel() {
        return processMessageLabel;
    }

    public void setProcessMessageLabel(Label processMessageLabel) {
        this.processMessageLabel = processMessageLabel;
    }

    public Shell getShell() {
        return shell;
    }

    public void setShell(Shell shell) {
        this.shell = shell;
    }

    public void setMessage(String message) {
        processMessageLabel.setText(message);
    }

    public void setSelection(int selection) {
        progressBar.setSelection(selection);
    }

    public void update() {
        progressBar.update();
    }
}