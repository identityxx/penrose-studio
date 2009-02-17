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
package org.safehaus.penrose.studio.directory.wizard;

import org.apache.log4j.Logger;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.safehaus.penrose.ldap.*;
import org.safehaus.penrose.ldap.connection.LDAPConnectionClient;
import org.safehaus.penrose.studio.server.Server;

import java.util.Collection;
import java.util.ArrayList;

/**
 * @author Endi S. Dewata
 */
public class ImportTreeWizardPage extends WizardPage implements SelectionListener, TreeListener {

    Logger log = Logger.getLogger(getClass());

    public final static String NAME = "LDAP Subtree";

    Tree baseDnTree;
    Text baseDnText;
    Text filterText;
    Text depthText;

    Server server;
    String partitionName;
    String connectionName;

    String baseDn;
    String filter;
    Integer depth;

    public ImportTreeWizardPage() {
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

        baseDnText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                baseDn = baseDnText.getText().trim();
                baseDn = "".equals(baseDn) ? null : baseDn;
            }
        });

        baseDnTree = new Tree(composite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.heightHint = 100;
        gd.horizontalSpan = 2;
        baseDnTree.setLayoutData(gd);

        baseDnTree.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                if (baseDnTree.getSelectionCount() == 0) return;

                TreeItem item = baseDnTree.getSelection()[0];
                DN dn = (DN)item.getData();
                baseDnText.setText(dn.toString());

                setPageComplete(validatePage());
            }
        });

        baseDnTree.addTreeListener(this);

        Label filterLabel = new Label(composite, SWT.NONE);
        filterLabel.setText("Filter:");
        filterLabel.setLayoutData(new GridData(GridData.FILL));

        filterText = new Text(composite, SWT.BORDER);
        filterText.setText("(objectClass=*)");
        filterText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        filterText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                filter = filterText.getText().trim();
                filter = "".equals(filter) ? null : filter;

                setPageComplete(validatePage());
            }
        });

        Label depthLabel = new Label(composite, SWT.NONE);
        depthLabel.setText("Depth:");
        depthLabel.setLayoutData(new GridData(GridData.FILL));

        depthText = new Text(composite, SWT.BORDER);
        depthText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        depthText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                String s = depthText.getText().trim();
                depth = "".equals(s) ? null : Integer.parseInt(s);

                setPageComplete(validatePage());
            }
        });

        baseDnText.setText(baseDn == null ? "" : baseDn);
        filterText.setText(filter == null ? "" : filter);
        depthText.setText(depth == null ? "" : depth.toString());

        setPageComplete(validatePage());
    }

    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) refresh();
    }

    public void refresh() {
        try {
            baseDnTree.removeAll();

            LDAPConnectionClient connectionClient = new LDAPConnectionClient(
                    server.getClient(),
                    partitionName,
                    connectionName
            );

            DN rootDn = new DN();
            SearchResult root = connectionClient.find(rootDn);
            if (root == null) return;

            TreeItem item = new TreeItem(baseDnTree, SWT.NONE);
            item.setText("Root");
            item.setData(rootDn);

            expand(item);

            item.setExpanded(true);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public void treeCollapsed(TreeEvent event) {
    }

    public void treeExpanded(TreeEvent event) {
        try {
            if (event.item == null) return;

            TreeItem item = (TreeItem)event.item;
            expand(item);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public String getBaseDn() {
        return baseDn;
    }

    public String getFilter() {
        return filter;
    }

    public Integer getDepth() {
        return depth;
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

    public void setDepth(Integer depth) {
        this.depth = depth;
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

    public String getConnectionName() {
        return connectionName;
    }

    public void setConnectionName(String connectionName) {
        this.connectionName = connectionName;
    }

    public Collection<SearchResult> expand(TreeItem item) throws Exception {

        for (TreeItem ti : item.getItems()) {
            ti.dispose();
        }

        LDAPConnectionClient connectionClient = new LDAPConnectionClient(
                server.getClient(),
                partitionName,
                connectionName
        );

        DN baseDn = (DN)item.getData();

        Collection<SearchResult> list = new ArrayList<SearchResult>();

        if (baseDn.isEmpty()) {

            SearchRequest req = new SearchRequest();
            req.setScope(SearchRequest.SCOPE_BASE);
            req.setAttributes(new String[] { "*", "+" });

            SearchResponse response = new SearchResponse();

            SearchResponse res = connectionClient.search(req, response);
            SearchResult rootDse = res.next();

            Attributes attributes = rootDse.getAttributes();
            Attribute attribute = attributes.get("namingContexts");

            for (Object value : attribute.getValues()) {
                String dn = (String)value;

                SearchResult entry = connectionClient.find(dn);
                list.add(entry);
            }

        } else {

            SearchRequest req = new SearchRequest();
            req.setDn(baseDn);
            req.setScope(SearchRequest.SCOPE_ONE);

            SearchResponse response = new SearchResponse();
            response = connectionClient.search(req, response);

            for (SearchResult result : response.getResults()) {
                list.add(result);
            }
        }

        for (SearchResult result : list) {
            DN dn = result.getDn();
            String label = baseDn.isEmpty() ? dn.toString() : dn.getRdn().toString();

            TreeItem ti = new TreeItem(item, SWT.NONE);
            ti.setText(label);
            ti.setData(dn);

            new TreeItem(ti, SWT.NONE);
        }

        return list;
    }
}