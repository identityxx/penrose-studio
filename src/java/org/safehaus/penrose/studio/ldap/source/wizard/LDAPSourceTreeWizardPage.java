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
package org.safehaus.penrose.studio.ldap.source.wizard;

import org.apache.log4j.Logger;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.safehaus.penrose.connection.ConnectionConfig;
import org.safehaus.penrose.ldap.DN;
import org.safehaus.penrose.ldap.LDAPClient;
import org.safehaus.penrose.ldap.SearchResult;
import org.safehaus.penrose.studio.server.Server;

import java.util.Collection;

/**
 * @author Endi S. Dewata
 */
public class LDAPSourceTreeWizardPage extends WizardPage implements ModifyListener, SelectionListener, TreeListener {

    Logger log = Logger.getLogger(getClass());

    public final static String NAME = "LDAP Subtree";

    Tree baseDnTree;
    Text baseDnText;
    Text filterText;
    Combo scopeCombo;
    Text objectClassesText;

    Server server;
    String partitionName;

    ConnectionConfig connectionConfig;

    String baseDn;
    String filter;
    String scope;
    String objectClasses;

    public LDAPSourceTreeWizardPage() {
        super(NAME);
        setDescription("Select a subtree.");
    }

    public void createControl(final Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        GridLayout sectionLayout = new GridLayout();
        sectionLayout.numColumns = 2;
        composite.setLayout(sectionLayout);

        Label baseDnLabel = new Label(composite, SWT.NONE);
        baseDnLabel.setText("Base DN:");
        baseDnLabel.setLayoutData(new GridData(GridData.FILL));

        baseDnText = new Text(composite, SWT.BORDER);
        baseDnText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        baseDnText.addModifyListener(this);

        baseDnTree = new Tree(composite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.heightHint = 100;
        gd.horizontalSpan = 2;
        baseDnTree.setLayoutData(gd);

        baseDnTree.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent event) {
                if (baseDnTree.getSelectionCount() == 0) return;

                TreeItem item = baseDnTree.getSelection()[0];
                baseDnText.setText((String)item.getData());

                setPageComplete(validatePage());
            }
            public void widgetDefaultSelected(SelectionEvent event) {
            }
        });

        baseDnTree.addTreeListener(this);

        Label filterLabel = new Label(composite, SWT.NONE);
        filterLabel.setText("Filter:");
        filterLabel.setLayoutData(new GridData(GridData.FILL));

        filterText = new Text(composite, SWT.BORDER);
        filterText.setText("(objectClass=*)");
        filterText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        filterText.addModifyListener(this);

        Label scopeLabel = new Label(composite, SWT.NONE);
        scopeLabel.setText("Scope:");
        scopeLabel.setLayoutData(new GridData(GridData.FILL));

        scopeCombo = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
        scopeCombo.add("");
        scopeCombo.add("OBJECT");
        scopeCombo.add("ONELEVEL");
        scopeCombo.add("SUBTREE");
        scopeCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        scopeCombo.addModifyListener(this);

        Label objectClassesLabel = new Label(composite, SWT.NONE);
        objectClassesLabel.setText("Object classes:");
        objectClassesLabel.setLayoutData(new GridData(GridData.FILL));

        objectClassesText = new Text(composite, SWT.BORDER);
        objectClassesText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        objectClassesText.addModifyListener(this);

        baseDnText.setText(baseDn == null ? "" : baseDn);
        filterText.setText(filter == null ? "" : filter);
        scopeCombo.setText(scope == null ? "" : scope);
        objectClassesText.setText(objectClasses == null ? "" : objectClasses);

        setPageComplete(validatePage());
    }

    public void setConnectionConfig(ConnectionConfig connectionConfig) {
        this.connectionConfig = connectionConfig;
    }

    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) init();
    }

    public void init() {
        LDAPClient client = null;
        try {
            baseDnTree.removeAll();

            client = new LDAPClient(connectionConfig.getParameters());

            TreeItem item = new TreeItem(baseDnTree, SWT.NONE);
            String suffix = "Root DSE";
            item.setText(suffix);
            item.setData("");

            Collection<SearchResult> results = client.findChildren("");

            for (SearchResult entry : results) {
                String dn = entry.getDn().toString();

                TreeItem it = new TreeItem(item, SWT.NONE);
                it.setText(dn);
                it.setData(dn);

                new TreeItem(it, SWT.NONE);
            }

            item.setExpanded(true);

        } catch (Exception e) {
            log.error(e.getMessage(), e);

        } finally {
            if (client != null) try { client.close(); } catch (Exception e) { log.error(e.getMessage(), e); }                                
        }
    }

    public void treeCollapsed(TreeEvent event) {
    }

    public void treeExpanded(TreeEvent event) {
        LDAPClient client = null;
        try {
            if (event.item == null) return;

            TreeItem item = (TreeItem)event.item;
            String baseDn = (String)item.getData();

            TreeItem items[] = item.getItems();
            for (TreeItem child : items) {
                child.dispose();
            }

            client = new LDAPClient(connectionConfig.getParameters());
            Collection<SearchResult> results = client.findChildren(baseDn);

            for (SearchResult entry : results) {
                String dn = entry.getDn().toString();
                String rdn = new DN(dn).getRdn().toString();

                TreeItem it = new TreeItem(item, SWT.NONE);
                it.setText(rdn);
                it.setData(dn);

                new TreeItem(it, SWT.NONE);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);

        } finally {
            if (client != null) try { client.close(); } catch (Exception e) { log.error(e.getMessage(), e); }
        }
    }

    public String getBaseDn() {
        String s = baseDnText.getText().trim();
        return "".equals(s) ? null : s;
    }

    public String getFilter() {
        String s = filterText.getText().trim();
        return "".equals(s) ? null : s;
    }

    public String getScope() {
        String s = scopeCombo.getText().trim();
        return "".equals(s) ? null : s;
    }

    public String getObjectClasses() {
        String s = objectClassesText.getText().trim();
        return "".equals(s) ? null : s;
    }

    public boolean validatePage() {
        return true;
    }

    public void widgetSelected(SelectionEvent event) {
        setPageComplete(validatePage());
    }

    public void widgetDefaultSelected(SelectionEvent event) {
        setPageComplete(validatePage());
    }

    public void setBaseDn(String baseDn) {
        this.baseDn = baseDn;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public void setObjectClasses(String objectClasses) {
        this.objectClasses = objectClasses;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public String getPartitionName() {
        return partitionName;
    }

    public void setPartitionName(String partitionName) {
        this.partitionName = partitionName;
    }

    public void modifyText(ModifyEvent event) {
        setPageComplete(validatePage());
    }
}
