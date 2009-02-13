package org.safehaus.penrose.studio.log.dialog;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.graphics.Point;
import org.eclipse.jface.window.Window;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;

import java.util.Collection;

/**
 * @author Endi S. Dewata
 */
public class AppenderSelectionDialog extends Dialog {

    Shell shell;

    Table appenderNamesTable;

    private int action = Window.CANCEL;

    String appenderName;

    public AppenderSelectionDialog(Shell parent, int style) {
        super(parent, style);

        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);

        createControl(shell);
    }

    public int open () {

        Point size = new Point(400, 300);
        shell.setSize(size);

        Point l = getParent().getLocation();
        Point s = getParent().getSize();

        shell.setLocation(l.x + (s.x - size.x)/2, l.y + (s.y - size.y)/2);

        shell.setText(getText());
        shell.setImage(PenroseStudio.getImage(PenroseImage.LOGGER));
        shell.open();

        Display display = getParent().getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }

        return action;
    }

    public void createControl(final Shell parent) {
        parent.setLayout(new GridLayout());

        appenderNamesTable = new Table(parent, SWT.BORDER | SWT.FULL_SELECTION);
        appenderNamesTable.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite buttons = new Composite(parent, SWT.NONE);
        buttons.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        buttons.setLayout(new RowLayout());

        Button okButton = new Button(buttons, SWT.PUSH);
        okButton.setText("OK");

        okButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                if (appenderNamesTable.getSelectionCount() == 0) return;

                TableItem item = appenderNamesTable.getSelection()[0];
                appenderName = item.getText();

                action = Window.OK;
                shell.close();
            }
        });

        Button cancelButton = new Button(buttons, SWT.PUSH);
        cancelButton.setText("Cancel");

        cancelButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                shell.close();
            }
        });
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public void setAppenderNames(Collection<String> appenderNames) {
        appenderNamesTable.clearAll();
        for (String appenderName : appenderNames) {

            TableItem item = new TableItem(appenderNamesTable, SWT.NONE);
            item.setText(appenderName);
        }
    }

    public String getAppenderName() {
        return appenderName;
    }
}
