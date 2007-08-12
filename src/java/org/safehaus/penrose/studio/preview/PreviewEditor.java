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
package org.safehaus.penrose.studio.preview;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.TreeAdapter;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.*;
import org.eclipse.core.runtime.IProgressMonitor;
import org.safehaus.penrose.Penrose;
import org.safehaus.penrose.PenroseFactory;
import org.safehaus.penrose.ldap.*;
import org.safehaus.penrose.config.PenroseConfig;
import org.safehaus.penrose.session.Session;
import org.safehaus.penrose.session.SessionManager;
import org.safehaus.penrose.session.SessionContext;
import org.safehaus.penrose.studio.project.ProjectConfig;

import java.io.File;

public class PreviewEditor extends EditorPart {

    private Logger log = Logger.getLogger(getClass());

    ProjectConfig projectConfig;
    PenroseConfig penroseConfig;
    File workDir;

    Text baseDnText;
    Text bindDnText;

    Tree tree;
    Table table;

    Penrose penrose;
    Session session;
    byte[] password;

    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        PreviewEditorInput ei = (PreviewEditorInput)input;
        projectConfig = ei.getProject();
        workDir = ei.getWorkDir();
        penroseConfig = ei.getPenroseConfig();

        setSite(site);
        setInput(input);
        setPartName(projectConfig.getName());
    }

    public void dispose() {
        try {
            if (session != null) session.close();
            if (penrose != null) penrose.stop();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            MessageDialog.openError(getSite().getShell(), "Error", e.getMessage());
        }
    }

    public void createPartControl(final Composite parent) {

        parent.setLayout(new GridLayout());

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        composite.setLayout(new GridLayout(5, false));

        Label baseDnLabel = new Label(composite, SWT.NONE);
        baseDnLabel.setText("Base DN:");

        baseDnText = new Text(composite, SWT.BORDER);
        baseDnText.setEnabled(false);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        baseDnText.setLayoutData(gd);

        Label bindDnLabel = new Label(composite, SWT.NONE);
        bindDnLabel.setText("Bind DN:");

        bindDnText = new Text(composite, SWT.BORDER);
        bindDnText.setEnabled(false);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        bindDnText.setLayoutData(gd);

        Button changeButton = new Button(composite, SWT.PUSH);
        changeButton.setText("Change...");

        changeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    PreviewDialog dialog = new PreviewDialog(parent.getShell(), SWT.NONE);
                    dialog.setBaseDn(baseDnText.getText());
                    dialog.setBindDn(bindDnText.getText());
                    dialog.setBindPassword(new String(password));
                    dialog.open();

                    if (dialog.getAction() == PreviewDialog.CANCEL) return;

                    String baseDn = dialog.getBaseDn();
                    String bindDn = dialog.getBindDn();
                    byte[] password = dialog.getBindPassword().getBytes();

                    open(baseDn, bindDn, password);

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    MessageDialog.openError(getSite().getShell(), "Error", e.getMessage());
                }
            }
        });

        SashForm sash = new SashForm(parent, SWT.VERTICAL);
        sash.setLayoutData(new GridData(GridData.FILL_BOTH));

        tree = new Tree(sash, SWT.BORDER);

        tree.addTreeListener(new TreeAdapter() {
            public void treeExpanded(TreeEvent event) {
                try {
                    if (event.item == null) return;

                    TreeItem treeItem = (TreeItem)event.item;
                    showChildren(treeItem);

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    MessageDialog.openError(getSite().getShell(), "Error", e.getMessage());
                }
            }
        });

        tree.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    if (tree.getSelectionCount() == 0) return;

                    TreeItem treeItem = tree.getSelection()[0];
                    showEntry(treeItem);

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    MessageDialog.openError(getSite().getShell(), "Error", e.getMessage());
                }
            }
        });

        table = new Table(sash, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        TableColumn tc = new TableColumn(table, SWT.NONE, 0);
        tc.setText("Name");
        tc.setWidth(200);

        tc = new TableColumn(table, SWT.NONE, 1);
        tc.setText("Value");
        tc.setWidth(400);

        try {
            open("", penroseConfig.getRootDn().toString(), penroseConfig.getRootPassword());

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            MessageDialog.openError(getSite().getShell(), "Error", e.getMessage());
        }
    }

    public void setFocus() {
    }

    public void open(String baseDn, String bindDn, byte[] password) throws Exception {

        tree.removeAll();

        if (session != null) session.close();
        if (penrose != null) penrose.stop();

        baseDnText.setText(baseDn == null ? "" : baseDn);
        bindDnText.setText(bindDn == null ? "" : bindDn);
        this.password = password;

        PenroseFactory penroseFactory = PenroseFactory.getInstance();
        penrose = penroseFactory.createPenrose(workDir);
        penrose.start();

        SessionContext sessionContext = penrose.getSessionContext();
        SessionManager sessionManager = sessionContext.getSessionManager();
        session = sessionManager.newSession();
        session.bind(bindDn, password);

        baseDn = baseDn == null ? "" : baseDn;
        String name = "".equals(baseDn) ? "Root DSE" : baseDn;

        TreeItem treeItem = new TreeItem(tree, SWT.NONE);
        treeItem.setText(name);
        treeItem.setData(new DN(baseDn));

        showChildren(treeItem);
        showEntry(treeItem);

        treeItem.setExpanded(true);

        tree.setSelection(new TreeItem[] { treeItem });
    }

    public void showChildren(final TreeItem parentItem) throws Exception {

        TreeItem items[] = parentItem.getItems();
        for (TreeItem item : items) item.dispose();

        DN parentDn = (DN)parentItem.getData();

        if (parentDn.isEmpty()) {

            SearchRequest request = new SearchRequest();
            request.setDn("");
            request.setScope(SearchRequest.SCOPE_BASE);
            request.setAttributes(new String[] { "*", "+" });

            SearchResponse<SearchResult> response = new SearchResponse<SearchResult>();

            session.search(request, response);

            SearchResult parentEntry = response.next();

            Attribute namingContexts = parentEntry.getAttributes().get("namingContexts");
            if (namingContexts == null) return;

            for (Object o : namingContexts.getValues()) {
                String namingContext = o.toString();

                TreeItem treeItem = new TreeItem(parentItem, SWT.NONE);
                treeItem.setText(namingContext);
                treeItem.setData(new DN(namingContext));

                new TreeItem(treeItem, SWT.NONE);
            }

        } else {

            SearchRequest request = new SearchRequest();
            request.setDn(parentDn.toString());
            request.setScope(SearchRequest.SCOPE_ONE);
            request.setAttributes(new String[] { "*", "+" });

            SearchResponse<SearchResult> response = new SearchResponse<SearchResult>();

            session.search(request, response);

            while (response.hasNext()) {
                SearchResult entry = response.next();
                DN dn = entry.getDn();
                String rdn = dn.getRdn().toString();

                TreeItem treeItem = new TreeItem(parentItem, SWT.NONE);
                treeItem.setText(rdn);
                treeItem.setData(dn);

                new TreeItem(treeItem, SWT.NONE);
            }

            if (response.getReturnCode() != LDAP.SUCCESS) {
                TreeItem treeItem = new TreeItem(parentItem, SWT.NONE);
                treeItem.setText(LDAP.getMessage(response.getReturnCode()));
            }
        }
    }

    public void showEntry(TreeItem treeItem) throws Exception {

        table.removeAll();

        DN dn = (DN)treeItem.getData();

        SearchRequest request = new SearchRequest();
        request.setDn(dn);
        request.setScope(SearchRequest.SCOPE_BASE);
        request.setAttributes(new String[] { "*", "+" });

        SearchResponse<SearchResult> response = new SearchResponse<SearchResult>();

        session.search(request, response);

        if (!response.hasNext()) return;

        SearchResult entry = response.next();

        Attributes attributes = entry.getAttributes();

        for (Attribute attribute : attributes.getAll()) {
            String name = attribute.getName();

            for (Object object : attribute.getValues()) {
                String value = object instanceof byte[] ? "(binary)" : object.toString();

                TableItem tableItem = new TableItem(table, SWT.NONE);
                tableItem.setText(0, name);
                tableItem.setText(1, value);
            }
        }
    }

    public void doSave(IProgressMonitor iProgressMonitor) {
    }

    public void doSaveAs() {
    }

    public boolean isDirty() {
        return false;
    }

    public boolean isSaveAsAllowed() {
        return false;
    }

}

