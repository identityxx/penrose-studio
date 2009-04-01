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
package org.safehaus.penrose.studio.log.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.safehaus.penrose.studio.log.dialog.AppenderSelectionDialog;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.log.LogManagerClient;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Endi S. Dewata
 */
public class LoggerAppendersWizardPage extends WizardPage {

    public Logger log = Logger.getLogger(getClass());

    public final static String NAME = "Logger Properties";

    Table appenderNamesTable;

    LogManagerClient logManagerClient;
    Collection<String> appenderNames = new ArrayList<String>();

    public LoggerAppendersWizardPage() {
        super(NAME);
        setDescription("Enter the logger appenders.");
    }

    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

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
            public void widgetSelected(SelectionEvent event) {
                try {
                    AppenderSelectionDialog dialog = new AppenderSelectionDialog(getShell(), SWT.NONE);
                    dialog.setText("Appenders");
                    dialog.setAppenderNames(logManagerClient.getAppenderNames());
                    int rc = dialog.open();

                    if (rc == Window.CANCEL) return;

                    String appenderName = dialog.getAppenderName();
                    appenderNames.add(appenderName);

                    refresh();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        Button removeButton = new Button(buttons, SWT.PUSH);
        removeButton.setText("Remove");
        removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                if (appenderNamesTable.getSelectionCount() == 0) return;

                for (TableItem item : appenderNamesTable.getSelection()) {
                    String appenderName = (String)item.getData();
                    appenderNames.remove(appenderName);
                }

                refresh();
            }
        });

        setPageComplete(validatePage());
    }

    public void setVisible(boolean b) {
        super.setVisible(b);
        if (b) refresh();
    }

    public void refresh() {
        appenderNamesTable.removeAll();

        for (String appenderName : appenderNames) {
            TableItem item = new TableItem(appenderNamesTable, SWT.NONE);
            item.setText(appenderName);
            item.setData(appenderName);
        }
    }

    public boolean validatePage() {
        return true;
    }

    public Collection<String> getAppenderNames() {
        return appenderNames;
    }

    public void setAppenderNames(Collection<String> appenderNames) {
        if (appenderNames == this.appenderNames) return;
        this.appenderNames.clear();
        if (appenderNames == null) return;
        this.appenderNames.addAll(appenderNames);
    }

    public LogManagerClient getLogManagerClient() {
        return logManagerClient;
    }

    public void setLogManagerClient(LogManagerClient logManagerClient) {
        this.logManagerClient = logManagerClient;
    }
}