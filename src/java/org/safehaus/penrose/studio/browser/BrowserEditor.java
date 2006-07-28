/**
 * Copyright (c) 2000-2005, Identyx Corporation.
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
package org.safehaus.penrose.studio.browser;

import java.util.Enumeration;
import java.util.Iterator;

import org.apache.log4j.Logger;
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
import org.eclipse.jface.dialogs.MessageDialog;
import org.ietf.ldap.*;
import org.safehaus.penrose.util.EntryUtil;
import org.safehaus.penrose.studio.PenroseApplication;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.util.ApplicationConfig;
import org.safehaus.penrose.config.PenroseConfig;
import org.safehaus.penrose.user.UserConfig;
import org.safehaus.penrose.service.ServiceConfig;
import org.safehaus.penrose.config.PenroseServerConfig;

public class BrowserEditor extends EditorPart {

    private Logger log = Logger.getLogger(getClass());

    Text urlText;
    Text bindDnText;

    Tree tree;
    Table table;

    LDAPConnection connection = new LDAPConnection();
    String password;

    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        setSite(site);
        setInput(input);
    }

    public void dispose() {
    }

    public void createPartControl(final Composite parent) {

        parent.setLayout(new GridLayout());

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        composite.setLayout(new GridLayout(5, false));

        Label urlLabel = new Label(composite, SWT.NONE);
        urlLabel.setText("URL:");

        urlText = new Text(composite, SWT.BORDER);
        urlText.setEnabled(false);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        urlText.setLayoutData(gd);

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
                    LDAPUrl url = new LDAPUrl(urlText.getText());

                    BrowserDialog dialog = new BrowserDialog(parent.getShell(), SWT.NONE);
                    dialog.setHostname(url.getHost());
                    dialog.setPort(url.getPort());
                    dialog.setBaseDn(url.getDN());
                    dialog.setBindDn(bindDnText.getText());
                    dialog.setBindPassword(password);
                    dialog.open();

                    if (dialog.getAction() == BrowserDialog.CANCEL) return;

                    String hostname = dialog.getHostname();
                    int port = dialog.getPort();
                    String baseDn = dialog.getBaseDn();
                    String bindDn = dialog.getBindDn();
                    String password = dialog.getBindPassword();

                    open(hostname, port, baseDn, bindDn, password);

                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
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
                    log.debug(e.getMessage(), e);
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
                    log.debug(e.getMessage(), e);
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
            PenroseApplication penroseApplication = PenroseApplication.getInstance();

            ApplicationConfig applicationConfig = penroseApplication.getApplicationConfig();
            Project project = applicationConfig.getCurrentProject();
            String hostname = project.getHost();

            PenroseServerConfig penroseServerConfig = penroseApplication.getPenroseServerConfig();
            PenroseConfig penroseConfig = penroseApplication.getPenroseConfig();

            ServiceConfig serviceConfig = penroseServerConfig.getServiceConfig("LDAP");
            String s = serviceConfig.getParameter("ldapPort");
            int port = s == null ? 10389 : Integer.parseInt(s);

            UserConfig rootUserConfig = penroseConfig.getRootUserConfig();

            open(hostname, port, "", rootUserConfig.getDn(), rootUserConfig.getPassword());

        } catch (Exception e) {
            log.debug(e.getMessage(), e);
            MessageDialog.openError(getSite().getShell(), "Error", e.getMessage());
        }
    }

    public void setFocus() {
    }

    public void open(String hostname, int port, String baseDn, String bindDn, String password) throws Exception {

        tree.removeAll();

        LDAPUrl ldapUrl = new LDAPUrl(hostname, port, baseDn);

        urlText.setText(ldapUrl.toString());
        bindDnText.setText(bindDn == null ? "" : bindDn);
        this.password = password;

        connection.connect(hostname, port);
        connection.bind(3, bindDn, password.getBytes());

        baseDn = baseDn == null ? "" : baseDn;
        String name = "".equals(baseDn) ? "Root DSE" : baseDn;

        TreeItem treeItem = new TreeItem(tree, SWT.NONE);
        treeItem.setText(name);
        treeItem.setData(baseDn);

        showChildren(treeItem);
        showEntry(treeItem);

        treeItem.setExpanded(true);

        tree.setSelection(new TreeItem[] { treeItem });
    }

    public void showChildren(TreeItem parentItem) throws Exception {

        TreeItem items[] = parentItem.getItems();
        for (int i=0; i<items.length; i++) items[i].dispose();

        String parentDn = (String)parentItem.getData();

        if ("".equals(parentDn)) {
            LDAPSearchResults sr = connection.search("", LDAPConnection.SCOPE_BASE, "(objectClass=*)", new String[0], false);
            LDAPEntry parentEntry = sr.next();

            LDAPAttribute namingContexts = parentEntry.getAttribute("namingContexts");

            for (Enumeration e = namingContexts.getStringValues(); e.hasMoreElements(); ) {
                String namingContext = (String)e.nextElement();

                TreeItem treeItem = new TreeItem(parentItem, SWT.NONE);
                treeItem.setText(namingContext);
                treeItem.setData(namingContext);

                new TreeItem(treeItem, SWT.NONE);
            }

        } else {

            LDAPSearchResults sr = connection.search(parentDn, LDAPConnection.SCOPE_ONE, "(objectClass=*)", new String[0], true);

            while (sr.hasMore()) {
                try {
                    LDAPEntry entry = sr.next();
                    String dn = entry.getDN();
                    String rdn = EntryUtil.getRdn(dn).toString();

                    TreeItem treeItem = new TreeItem(parentItem, SWT.NONE);
                    treeItem.setText(rdn);
                    treeItem.setData(dn);

                    new TreeItem(treeItem, SWT.NONE);

                } catch (Exception e) {
                    TreeItem treeItem = new TreeItem(parentItem, SWT.NONE);
                    treeItem.setText(e.getMessage());
                }
            }
        }
    }

    public void showEntry(TreeItem treeItem) throws Exception {

        table.removeAll();

        String dn = (String)treeItem.getData();
        if (dn == null) return;

        LDAPSearchResults sr = connection.search(dn, LDAPConnection.SCOPE_BASE, "(objectClass=*)", new String[0], false);
        if (!sr.hasMore()) return;

        LDAPEntry entry = sr.next();

        LDAPAttributeSet attributes = entry.getAttributeSet();

        for (Iterator i = attributes.iterator(); i.hasNext(); ) {
            LDAPAttribute attribute = (LDAPAttribute)i.next();
            String name = attribute.getName();

            for (Enumeration e = attribute.getStringValues(); e.hasMoreElements(); ) {
                String value = (String)e.nextElement();

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

