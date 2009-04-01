/**
 * Copyright 2009 Red Hat, Inc.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.safehaus.penrose.studio.log.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.log.log4j.LoggerConfig;
import org.safehaus.penrose.log.log4j.RootLoggerConfig;
import org.safehaus.penrose.log.log4j.Log4jConfig;

import java.util.Collection;
import java.util.ArrayList;

/**
 * @author Endi S. Dewata
 */
public class LoggerDialog extends Dialog {

    public final static int CANCEL = 0;
    public final static int OK     = 1;

    Shell shell;

    Text nameText;
    Combo levelCombo;
    Button additivityCheckbox;
    Table appenderNamesTable;

    private int action;

    LoggerConfig loggerConfig;
    RootLoggerConfig rootConfig;

    public LoggerDialog(Shell parent, int style) {
        super(parent, style);

        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);

        createControl(shell);
    }

    public void open() {

        Point size = new Point(600, 400);
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
    }

    public void createControl(final Shell parent) {
        parent.setLayout(new GridLayout());

        TabFolder folder = new TabFolder(parent, SWT.NONE);
        folder.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite loggerPanel = createLoggerControl(folder);

        TabItem item = new TabItem(folder, SWT.NONE);
        item.setText("Logger");
        item.setControl(loggerPanel);

        Composite appendersPanel = createAppendersControl(folder);

        item = new TabItem(folder, SWT.NONE);
        item.setText("Appenders");
        item.setControl(appendersPanel);

        Composite buttons = new Composite(parent, SWT.NONE);
        buttons.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        buttons.setLayout(new RowLayout());

        Button saveButton = new Button(buttons, SWT.PUSH);
        saveButton.setText("Save");

        saveButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {

                if (loggerConfig != null) {
                    loggerConfig.setName(getLoggerName());
                    loggerConfig.setLevel(getLoggerLevel());
                    loggerConfig.setAdditivity(getAdditivity());
                    loggerConfig.setAppenderNames(getAppenders());
                }

                if (rootConfig != null) {
                    rootConfig.setLevel(getLoggerLevel());
                    rootConfig.setAppenderNames(getAppenders());
                }

                action = OK;
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

    public Composite createLoggerControl(Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));

        Label nameLabel = new Label(composite, SWT.NONE);
        nameLabel.setText("Name:");

        nameText = new Text(composite, SWT.BORDER);
        nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label typeLabel = new Label(composite, SWT.NONE);
        typeLabel.setText("Level:");

        levelCombo = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
        levelCombo.add("");
        levelCombo.add("OFF");
        levelCombo.add("FATAL");
        levelCombo.add("ERROR");
        levelCombo.add("WARN");
        levelCombo.add("INFO");
        levelCombo.add("DEBUG");
        levelCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label additivityLevel = new Label(composite, SWT.NONE);
        additivityLevel.setText("Additivity:");

        additivityCheckbox = new Button(composite, SWT.CHECK);
        additivityCheckbox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        return composite;
    }

    public Composite createAppendersControl(Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));

        appenderNamesTable = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION);
        appenderNamesTable.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite buttons = new Composite(composite, SWT.NONE);
        buttons.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        buttons.setLayout(new GridLayout());

        Button addButton = new Button(buttons, SWT.PUSH);
        addButton.setText("Add");
        addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {

                PenroseStudio penroseStudio = PenroseStudio.getInstance();
                Log4jConfig loggingConfig = penroseStudio.getLoggingConfig();
                Collection appenderConfigs = loggingConfig.getAppenderConfigs();

                AppenderSelectionDialog dialog = new AppenderSelectionDialog(shell, SWT.NONE);
                dialog.setText("Appenders");
                dialog.setAppenderNames(appenderConfigs);
                dialog.open();

                if (dialog.getAction() == LoggerDialog.CANCEL) return;

                String appenderName = dialog.getAppenderName();

                TableItem item = new TableItem(appenderNamesTable, SWT.NONE);
                item.setText(appenderName);
                item.setData(appenderName);
            }
        });

        Button removeButton = new Button(buttons, SWT.PUSH);
        removeButton.setText("Remove");
        removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                if (appenderNamesTable.getSelectionCount() == 0) return;

                TableItem items[] = appenderNamesTable.getSelection();
                for (TableItem item : items) {
                    item.dispose();
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

    public void setLoggerName(String name) {
        nameText.setText(name == null ? "" : name);
        nameText.setEnabled(name == null);
    }

    public String getLoggerName() {
        return "".equals(nameText.getText()) ? null : nameText.getText();
    }

    public void setLoggerLevel(String level) {
        levelCombo.setText(level == null ? "" : level);
    }

    public String getLoggerLevel() {
        return "".equals(levelCombo.getText()) ? null : levelCombo.getText();
    }

    public void setAppenders(Collection<String> appenders) {
        appenderNamesTable.clearAll();

        for (String appenderName : appenders) {

            TableItem item = new TableItem(appenderNamesTable, SWT.NONE);
            item.setText(appenderName);
            item.setData(appenderName);
        }
    }

    public Collection<String> getAppenders() {
        Collection<String> appenders = new ArrayList<String>();

        TableItem items[] = appenderNamesTable.getItems();
        for (TableItem item : items) {
            String appenderName = (String) item.getData();
            appenders.add(appenderName);
        }

        return appenders;
    }

    public void setLoggerConfig(LoggerConfig loggerConfig) {
        this.loggerConfig = loggerConfig;
        
        setLoggerName(loggerConfig.getName());
        setLoggerLevel(loggerConfig.getLevel());
        setAdditivity(loggerConfig.getAdditivity());
        setAppenders(loggerConfig.getAppenderNames());
    }

    public void setAdditivity(boolean additivity) {
        additivityCheckbox.setSelection(additivity);
    }

    public boolean getAdditivity() {
        return additivityCheckbox.getSelection();
    }

    public void setRootConfig(RootLoggerConfig rootConfig) {
        this.rootConfig = rootConfig;

        setLoggerName("Root Logger");
        setLoggerLevel(rootConfig.getLevel());
        additivityCheckbox.setEnabled(false);
        setAppenders(rootConfig.getAppenderNames());
    }
}
