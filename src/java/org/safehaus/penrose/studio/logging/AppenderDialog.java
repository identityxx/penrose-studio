package org.safehaus.penrose.studio.logging;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.graphics.Point;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.parameter.ParameterDialog;
import org.safehaus.penrose.log4j.AppenderConfig;
import org.safehaus.penrose.log4j.LayoutConfig;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author Endi S. Dewata
 */
public class AppenderDialog extends Dialog {

    public final static int CANCEL = 0;
    public final static int OK     = 1;

    Shell shell;

    Text nameText;
    Text appenderClassText;
    Text layoutClassText;

    Table parametersTable;

    private int action;

    AppenderConfig appenderConfig;

    public AppenderDialog(Shell parent, int style) {
        super(parent, style);

        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);

        createControl(shell);
    }

    public void open () {

        Point size = new Point(600, 400);
        shell.setSize(size);

        Point l = getParent().getLocation();
        Point s = getParent().getSize();

        shell.setLocation(l.x + (s.x - size.x)/2, l.y + (s.y - size.y)/2);

        shell.setText(getText());
        shell.setImage(PenrosePlugin.getImage(PenroseImage.LOGGER));
        shell.open();

        Display display = getParent().getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
    }

    public void createControl(final Shell parent) {
        parent.setLayout(new GridLayout());

        TabFolder folder = new TabFolder(parent, SWT.NONE);
        folder.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite appenderPanel = createAppenderControl(folder);

        TabItem item = new TabItem(folder, SWT.NONE);
        item.setText("Appender");
        item.setControl(appenderPanel);

        Composite parametersPanel = createLayoutControl(folder);

        item = new TabItem(folder, SWT.NONE);
        item.setText("Layout");
        item.setControl(parametersPanel);

        Composite buttons = new Composite(parent, SWT.NONE);
        buttons.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        buttons.setLayout(new RowLayout());

        Button saveButton = new Button(buttons, SWT.PUSH);
        saveButton.setText("Save");

        saveButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {

            	appenderConfig.setName(getAppenderName());
                appenderConfig.setAppenderClass(getAppenderClass());

                LayoutConfig layoutConfig = appenderConfig.getLayoutConfig();

                String layoutClass = getLayoutClass();

                if (layoutClass != null || parametersTable.getItemCount() > 0) {

                    if (layoutConfig == null) {
                        layoutConfig = new LayoutConfig();
                        appenderConfig.setLayoutConfig(layoutConfig);
                    }

                    layoutConfig.setLayoutClass(layoutClass);

                    layoutConfig.clearParameters();

                    TableItem items[] = parametersTable.getItems();
                    for (int i=0; i<items.length; i++) {
                        String name = items[i].getText(0);
                        String value = items[i].getText(1);

                        layoutConfig.setParameter(name, value);
                    }

                } else {
                    appenderConfig.setLayoutConfig(null);
                }

                action = AppenderDialog.OK;
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

    public Composite createAppenderControl(Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));

        Label nameLabel = new Label(composite, SWT.NONE);
        nameLabel.setText("Name:");

        nameText = new Text(composite, SWT.BORDER);
        nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label appenderClassLabel = new Label(composite, SWT.NONE);
        appenderClassLabel.setText("Class:");

        appenderClassText = new Text(composite, SWT.BORDER);
        appenderClassText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        return composite;
    }

    public Composite createLayoutControl(Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(3, false));

        Label layoutClassLabel = new Label(composite, SWT.NONE);
        layoutClassLabel.setText("Class:");

        layoutClassText = new Text(composite, SWT.BORDER);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        layoutClassText.setLayoutData(gd);

        Label separatorLabel = new Label(composite, SWT.NONE);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 3;
        separatorLabel.setLayoutData(gd);

        Label parametersLabel = new Label(composite, SWT.NONE);
        parametersLabel.setText("Parameters:");
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 3;
        parametersLabel.setLayoutData(gd);

        parametersTable = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION);
        parametersTable.setHeaderVisible(true);
        parametersTable.setLinesVisible(true);
        gd = new GridData(GridData.FILL_BOTH);
        gd.horizontalSpan = 2;
        parametersTable.setLayoutData(gd);

        TableColumn tc = new TableColumn(parametersTable, SWT.NONE);
        tc.setText("Name");
        tc.setWidth(150);

        tc = new TableColumn(parametersTable, SWT.NONE);
        tc.setText("Value");
        tc.setWidth(250);

        parametersTable.addMouseListener(new MouseAdapter() {
            public void mouseDoubleClick(MouseEvent mouseEvent) {

                if (parametersTable.getSelectionCount() == 0) return;

                TableItem item = parametersTable.getSelection()[0];

                ParameterDialog dialog = new ParameterDialog(shell, SWT.NONE);
                dialog.setText("Edit parameter...");
                dialog.setName(item.getText(0));
                dialog.setValue(item.getText(1));
                dialog.open();

                if (dialog.getAction() == ParameterDialog.CANCEL) return;

                String name = dialog.getName();
                String value = dialog.getValue();

                item.setText(0, name);
                item.setText(1, value);
            }
        });

        Composite buttons = new Composite(composite, SWT.NONE);
        buttons.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        buttons.setLayout(new GridLayout());

        Button addButton = new Button(buttons, SWT.PUSH);
        addButton.setText("Add");
        addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {

                ParameterDialog dialog = new ParameterDialog(shell, SWT.NONE);
                dialog.setText("Add parameter...");
                dialog.open();

                if (dialog.getAction() == ParameterDialog.CANCEL) return;

                String name = dialog.getName();
                String value = dialog.getValue();

                TableItem item = new TableItem(parametersTable, SWT.NONE);
                item.setText(0, name);
                item.setText(1, value);
            }
        });

        Button removeButton = new Button(buttons, SWT.PUSH);
        removeButton.setText("Remove");
        removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                if (parametersTable.getSelectionCount() == 0) return;

                TableItem items[] = parametersTable.getSelection();
                for (int i=0; i<items.length; i++) {
                    items[i].dispose();
                }
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

    public void setAppenderName(String name) {
        nameText.setText(name == null ? "" : name);
        nameText.setEnabled(name == null);
    }

    public String getAppenderName() {
        return "".equals(nameText.getText()) ? null : nameText.getText();
    }

    public void setAppenderClass(String appenderClass) {
        appenderClassText.setText(appenderClass == null ? "" : appenderClass);
    }

    public String getAppenderClass() {
        return "".equals(appenderClassText.getText()) ? null : appenderClassText.getText();
    }

    public void setLayoutClass(String layoutClass) {
        layoutClassText.setText(layoutClass == null ? "" : layoutClass);
    }

    public String getLayoutClass() {
        return "".equals(layoutClassText.getText()) ? null : layoutClassText.getText();
    }

    public void setAppenderConfig(AppenderConfig appenderConfig) {
        this.appenderConfig = appenderConfig;

        setAppenderName(appenderConfig.getName());
        setAppenderClass(appenderConfig.getAppenderClass());

        LayoutConfig layoutConfig = appenderConfig.getLayoutConfig();
        if (layoutConfig != null) {
            setLayoutClass(layoutConfig.getLayoutClass());

            Collection parameterNames = layoutConfig.getParameterNames();
            for (Iterator i=parameterNames.iterator(); i.hasNext(); ) {
                String name = (String)i.next();
                String value = layoutConfig.getParameter(name);

                TableItem item = new TableItem(parametersTable, SWT.NONE);
                item.setText(0, name);
                item.setText(1, value);
            }
        }
    }
}
