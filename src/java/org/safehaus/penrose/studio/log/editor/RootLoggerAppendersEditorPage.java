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
package org.safehaus.penrose.studio.log.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.window.Window;
import org.safehaus.penrose.log.log4j.RootLoggerConfig;
import org.safehaus.penrose.studio.editor.EditorPage;
import org.safehaus.penrose.studio.log.wizard.RootLoggerAppendersWizard;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.client.PenroseClient;

public class RootLoggerAppendersEditorPage extends EditorPage {

    Table appenderNamesTable;

    RootLoggerEditor editor;
    RootLoggerConfig rootLoggerConfig;

    public RootLoggerAppendersEditorPage(RootLoggerEditor editor) {
        super(editor, "APPENDERS", "Logger Editor", "  Appenders  ");

        this.editor = editor;
        this.rootLoggerConfig = editor.rootLoggerConfig;
    }

    public void init() throws Exception {

        Composite body = getManagedForm().getForm().getBody();
        body.setLayout(new GridLayout());

        Section appendersSection = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        appendersSection.setText("Appenders");
        appendersSection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Composite appendersComponent = createAppendersControl(appendersSection);
        appendersSection.setClient(appendersComponent);
	}

    public Composite createAppendersControl(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        Composite leftControl = createPropertiesLeftControl(composite);
        leftControl.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite rightControl = createPropertiesRightControl(composite);
        GridData gd = new GridData(GridData.FILL_VERTICAL);
        gd.widthHint = 100;
        rightControl.setLayoutData(gd);

        return composite;
    }

    public Composite createPropertiesLeftControl(final Composite parent) {

        appenderNamesTable = toolkit.createTable(parent, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.heightHint = 100;
        appenderNamesTable.setLayoutData(gd);

        return appenderNamesTable;
    }

    public Composite createPropertiesRightControl(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);

        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);

        Button editButton = new Button(composite, SWT.PUSH);
		editButton.setText("Edit");
        editButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        editButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    PenroseClient client = editor.getServerNode().getServer().getClient();

                    RootLoggerAppendersWizard wizard = new RootLoggerAppendersWizard();
                    wizard.setLogManagerClient(client.getLogManagerClient());
                    wizard.setRootLoggerConfig(rootLoggerConfig);

                    WizardDialog dialog = new WizardDialog(getSite().getShell(), wizard);
                    dialog.setPageSize(600, 300);
                    int rc = dialog.open();

                    if (rc == Window.CANCEL) return;

                    editor.store();
                    refresh();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        return composite;
    }

    public void refresh() {

        appenderNamesTable.removeAll();

        for (String appenderName : rootLoggerConfig.getAppenderNames()) {
            TableItem item = new TableItem(appenderNamesTable, SWT.CHECK);
            item.setText(appenderName);
            item.setData(appenderName);
        }
    }
}