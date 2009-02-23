/**
 * Copyright (c) 2000-2006, Identyx Corporation.
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
package org.safehaus.penrose.studio.source.editor;

import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.core.runtime.IProgressMonitor;
import org.safehaus.penrose.ldap.*;
import org.safehaus.penrose.source.SourceClient;
import org.safehaus.penrose.source.SourceManagerClient;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.studio.source.wizard.SourceBrowserOptionsWizard;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.partition.PartitionClient;

import java.lang.reflect.InvocationTargetException;

/**
 * @author Endi S. Dewata
 */
public class SourceBrowsePage extends SourceEditorPage {

    Button refreshButton;

    Tree tree;
    Table table;

    Long sizeLimit;

    public SourceBrowsePage(SourceEditor editor) throws Exception {
        super(editor, "BROWSE", "Browse");
    }

    public SourceClient getSourceClient() throws Exception {
        PenroseClient client = server.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
        PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
        SourceManagerClient sourceManagerClient = partitionClient.getSourceManagerClient();
        return sourceManagerClient.getSourceClient(sourceConfig.getName());
    }

    public void createFormContent(IManagedForm managedForm) {
        super.createFormContent(managedForm);

        ScrolledForm form = managedForm.getForm();

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section directorySection = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        directorySection.setText("Directory");
        directorySection.setLayoutData(new GridData(GridData.FILL_BOTH));

        Control directoryControl = createDirectoryControl(directorySection);
        directorySection.setClient(directoryControl);

        Section entrySection = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        entrySection.setText("Entry");
        entrySection.setLayoutData(new GridData(GridData.FILL_BOTH));

        Control entryControl = createEntryControl(entrySection);
        entrySection.setClient(entryControl);

        reset();
    }

    public Composite createDirectoryControl(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        Composite leftControl = createDirectoryLeftControl(composite);
        leftControl.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite rightControl = createDirectoryRightControl(composite);
        GridData gd = new GridData(GridData.FILL_VERTICAL);
        gd.widthHint = 100;
        rightControl.setLayoutData(gd);

        return composite;
    }

    public Composite createDirectoryLeftControl(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);

        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);

        tree = toolkit.createTree(composite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.heightHint = 100;
        tree.setLayoutData(gd);

        tree.addTreeListener(new TreeAdapter() {
            public void treeExpanded(TreeEvent event) {
                try {
                    if (event.item == null) return;

                    TreeItem item = (TreeItem)event.item;
                    expand(item);

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        tree.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    table.removeAll();

                    TreeItem item = tree.getSelection()[0];
                    DN dn = (DN)item.getData();
                    if (dn == null) return;

                    SourceClient sourceClient = getSourceClient();
                    SearchResult entry = sourceClient.find(dn);
                    if (entry == null) return;
                    
                    showAttributes(entry);

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        return composite;
    }

    public Composite createDirectoryRightControl(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);

        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);

        Button settingsButton = new Button(composite, SWT.PUSH);
        settingsButton.setText("Settings...");
        settingsButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        settingsButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    SourceBrowserOptionsWizard wizard = new SourceBrowserOptionsWizard();
                    wizard.setSizeLimit(sizeLimit);

                    IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                    WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
                    dialog.setPageSize(600, 300);
                    int rc = dialog.open();

                    if (rc == Window.CANCEL) return;

                    sizeLimit = wizard.getSizeLimit();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });

        return composite;
    }

    public Composite createEntryControl(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        Composite leftControl = createEntryLeftControl(composite);
        leftControl.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite rightControl = createEntryRightControl(composite);
        GridData gd = new GridData(GridData.FILL_VERTICAL);
        gd.widthHint = 100;
        rightControl.setLayoutData(gd);

        return composite;
    }

    public Composite createEntryLeftControl(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);

        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);

        table = toolkit.createTable(composite, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.heightHint = 100;
        table.setLayoutData(gd);

        TableColumn tc = new TableColumn(table, SWT.LEFT);
        tc.setText("Name");
        tc.setWidth(200);

        tc = new TableColumn(table, SWT.LEFT);
        tc.setText("Value");
        tc.setWidth(300);

        return composite;
    }

    public Composite createEntryRightControl(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);

        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);

        return composite;
    }

    public void reset() {
        tree.removeAll();
        table.removeAll();

        TreeItem item = new TreeItem(tree, SWT.NONE);
        item.setText("Root");
        item.setData(new DN());

        new TreeItem(item, SWT.NONE);
    }

    public void expand(TreeItem item) throws Exception {

        for (TreeItem ti : item.getItems()) ti.dispose();

        try {
            DN baseDn = (DN)item.getData();

            final SearchRequest request = new SearchRequest();
            request.setDn(baseDn);
            request.setScope(SearchRequest.SCOPE_ONE);
            if (sizeLimit != null) request.setSizeLimit(sizeLimit);

            final SearchResponse response = new SearchResponse();

            IProgressService progressService = PlatformUI.getWorkbench().getProgressService();

            progressService.busyCursorWhile(new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor) throws InvocationTargetException {
                    try {
                        monitor.beginTask("Retrieving data...", IProgressMonitor.UNKNOWN);

                        SourceClient sourceClient = getSourceClient();
                        sourceClient.search(request, response);

                    } catch (Exception e) {
                        throw new InvocationTargetException(e);

                    } finally {
                        monitor.done();
                    }
                }
            });

            while (response.hasNext()) {
                SearchResult result = response.next();

                DN dn = result.getDn();
                String label = baseDn.isEmpty() ? dn.toString() : dn.getRdn().toString();

                TreeItem ti = new TreeItem(item, SWT.NONE);
                ti.setText(label);
                ti.setData(dn);

                new TreeItem(ti, SWT.NONE);
            }

        } catch (Exception e) {
            TreeItem ti = new TreeItem(item, SWT.NONE);
            ti.setText("Error: "+e.getMessage());
        }
    }

    public void showAttributes(SearchResult entry) throws Exception {

        Attributes attributes = entry.getAttributes();
        for (Attribute attribute : attributes.getAll()) {
            String name = attribute.getName();

            for (Object value : attribute.getValues()) {
                String label = value instanceof byte[] ? "(binary)" : value.toString();

                TableItem ti = new TableItem(table, SWT.NONE);
                ti.setText(0, name);
                ti.setText(1, label);
                ti.setData(value);
            }
        }
    }
}
