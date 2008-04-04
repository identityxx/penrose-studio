package org.safehaus.penrose.studio.logger;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.graphics.Point;
import org.safehaus.penrose.studio.PenroseStudioPlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.logger.log4j.AppenderConfig;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author Endi S. Dewata
 */
public class AppenderSelectionDialog extends Dialog {

    public final static int CANCEL = 0;
    public final static int OK     = 1;

    Shell shell;

    Table appendersTable;

    private int action;

    String appenderName;

    public AppenderSelectionDialog(Shell parent, int style) {
        super(parent, style);

        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);

        createControl(shell);
    }

    public void open () {

        Point size = new Point(400, 300);
        shell.setSize(size);

        Point l = getParent().getLocation();
        Point s = getParent().getSize();

        shell.setLocation(l.x + (s.x - size.x)/2, l.y + (s.y - size.y)/2);

        shell.setText(getText());
        shell.setImage(PenroseStudioPlugin.getImage(PenroseImage.LOGGER));
        shell.open();

        Display display = getParent().getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
    }

    public void createControl(final Shell parent) {
        parent.setLayout(new GridLayout());

        appendersTable = new Table(parent, SWT.BORDER | SWT.FULL_SELECTION);
        appendersTable.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite buttons = new Composite(parent, SWT.NONE);
        buttons.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        buttons.setLayout(new RowLayout());

        Button okButton = new Button(buttons, SWT.PUSH);
        okButton.setText("OK");

        okButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                if (appendersTable.getSelectionCount() == 0) return;

                TableItem item = appendersTable.getSelection()[0];
                appenderName = item.getText();

                action = AppenderSelectionDialog.OK;
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

    public void setAppenderConfigs(Collection appenderConfigs) {
        appendersTable.clearAll();
        for (Iterator i=appenderConfigs.iterator(); i.hasNext(); ) {
            AppenderConfig appenderConfig = (AppenderConfig)i.next();
            String name = appenderConfig.getName();

            TableItem item = new TableItem(appendersTable, SWT.NONE);
            item.setText(name);
        }
    }

    public String getAppenderName() {
        return appenderName;
    }
}
