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
package org.safehaus.penrose.studio.browser;

import java.util.Enumeration;

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
import org.safehaus.penrose.ldap.DN;
import org.safehaus.penrose.studio.dialog.ErrorDialog;

public class BrowserEditor extends EditorPart {

	private Logger log = Logger.getLogger(getClass());

    public final static String LDAP_PORT             = "ldapPort";
    public final static int DEFAULT_LDAP_PORT        = 10389;

    Text urlText;
    Text bindDnText;

    Tree tree;
    Table table;

    LDAPConnection connection = new LDAPConnection();

    String hostname;
    int port;
    String bindDn;
    byte[] password;

    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        BrowserEditorInput ei = (BrowserEditorInput)input;
        hostname = ei.getHostname();
        port = ei.getPort();
        bindDn = ei.getBindDn();
        password = ei.getPassword();

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
                    dialog.setBindDn(bindDn);
                    dialog.setBindPassword(new String(password));
                    dialog.open();

                    if (dialog.getAction() == BrowserDialog.CANCEL) return;

                    String hostname = dialog.getHostname();
                    int port = dialog.getPort();
                    String baseDn = dialog.getBaseDn();

                    bindDn = dialog.getBindDn();
                    password = dialog.getBindPassword().getBytes();

                    open(hostname, port, baseDn);

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
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
                    ErrorDialog.open(e);
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
                    ErrorDialog.open(e);
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
            open(hostname, port, "");

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
	}

	public void setFocus() {
	}

    public void open(String hostname, int port, String baseDn) throws Exception {

        tree.removeAll();
        connection.disconnect();

        LDAPUrl ldapUrl = new LDAPUrl(hostname, port, baseDn);

        urlText.setText(ldapUrl.toString());
        bindDnText.setText(bindDn == null ? "" : bindDn);

        connection.connect(hostname, port);
        connection.bind(3, bindDn, password);

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

    public void showChildren(TreeItem parentItem) throws Exception {

        TreeItem items[] = parentItem.getItems();
        for (TreeItem item : items) item.dispose();

        DN parentDn = (DN)parentItem.getData();

        if (parentDn.isEmpty()) {
            LDAPSearchResults sr = connection.search("", LDAPConnection.SCOPE_BASE, "(objectClass=*)", new String[] { "*", "+" }, false);
            LDAPEntry parentEntry = sr.next();

            LDAPAttribute namingContexts = parentEntry.getAttribute("namingContexts");

            for (Enumeration e = namingContexts.getStringValues(); e.hasMoreElements(); ) {
                String namingContext = (String)e.nextElement();

                TreeItem treeItem = new TreeItem(parentItem, SWT.NONE);
                treeItem.setText(namingContext);
                treeItem.setData(new DN(namingContext));

                new TreeItem(treeItem, SWT.NONE);
            }

        } else {

            LDAPSearchResults sr = connection.search(parentDn.toString(), LDAPConnection.SCOPE_ONE, "(objectClass=*)", new String[] { "*", "+" }, true);

            while (sr.hasMore()) {
                try {
                    LDAPEntry entry = sr.next();
                    DN dn = new DN(entry.getDN());
                    String rdn = dn.getRdn().toString();

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

        DN dn = (DN)treeItem.getData();

        LDAPSearchResults sr = connection.search(
                dn.toString(),
                LDAPConnection.SCOPE_BASE,
                "(objectClass=*)",
                new String[] { "*", "+" },
                false
        );

        if (!sr.hasMore()) return;

        LDAPEntry entry = sr.next();

        LDAPAttributeSet attributes = entry.getAttributeSet();

        for (Object object : attributes) {
            LDAPAttribute attribute = (LDAPAttribute) object;
            String name = attribute.getName();

            for (Enumeration e = attribute.getStringValues(); e.hasMoreElements();) {
                String value = (String) e.nextElement();

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

