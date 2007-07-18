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
package org.safehaus.penrose.studio.source.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.safehaus.penrose.connection.ConnectionConfig;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.ldap.LDAPClient;
import org.safehaus.penrose.ldap.DN;
import org.apache.log4j.Logger;

import javax.naming.directory.SearchResult;
import java.util.*;

/**
 * @author Endi S. Dewata
 */
public class JNDITreeWizardPage extends WizardPage implements SelectionListener, TreeListener {

    Logger log = Logger.getLogger(getClass());

    public final static String NAME = "LDAP Subtree";

    Tree baseDnTree;
    Text baseDnText;
    Text filterText;
    Combo scopeCombo;
    Text objectClassesText;

    Partition partition;
    ConnectionConfig connectionConfig;
    LDAPClient client;

    public JNDITreeWizardPage() {
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

        Label scopeLabel = new Label(composite, SWT.NONE);
        scopeLabel.setText("Scope:");
        scopeLabel.setLayoutData(new GridData(GridData.FILL));

        scopeCombo = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
        scopeCombo.add("OBJECT");
        scopeCombo.add("ONELEVEL");
        scopeCombo.add("SUBTREE");
        scopeCombo.select(1);
        scopeCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label objectClassesLabel = new Label(composite, SWT.NONE);
        objectClassesLabel.setText("Object classes:");
        objectClassesLabel.setLayoutData(new GridData(GridData.FILL));

        objectClassesText = new Text(composite, SWT.BORDER);
        objectClassesText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        setPageComplete(validatePage());
    }

    public void setConnectionConfig(Partition partition, ConnectionConfig connectionConfig) {
        this.partition = partition;
        this.connectionConfig = connectionConfig;

        try {
            if (client == null) {
                client = new LDAPClient(connectionConfig.getParameters());
            }

        } catch (Exception e) {
            log.debug(e.getMessage(), e);
        }
    }

    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) init();
    }

    public void init() {
        try {
            baseDnTree.removeAll();

            TreeItem item = new TreeItem(baseDnTree, SWT.NONE);
            String suffix = client.getSuffix().isEmpty() ? "Root DSE" : client.getSuffix().toString();
            item.setText(suffix);
            item.setData("");

            Collection results = client.getChildren("");

            for (Iterator i=results.iterator(); i.hasNext(); ) {
                SearchResult entry = (SearchResult)i.next();
                String dn = entry.getName();

                TreeItem it = new TreeItem(item, SWT.NONE);
                it.setText(dn);
                it.setData(dn);

                new TreeItem(it, SWT.NONE);
            }

            item.setExpanded(true);

        } catch (Exception e) {
            log.debug(e.getMessage(), e);
        }
    }

    public void treeCollapsed(TreeEvent event) {
    }

    public void treeExpanded(TreeEvent event) {
        try {
            if (event.item == null) return;

            TreeItem item = (TreeItem)event.item;
            String baseDn = (String)item.getData();

            TreeItem items[] = item.getItems();
            for (int i=0; i<items.length; i++) {
                items[i].dispose();
            }

            Collection results = client.getChildren(baseDn);

            for (Iterator i=results.iterator(); i.hasNext(); ) {
                SearchResult entry = (SearchResult)i.next();
                String dn = entry.getName();
                String rdn = new DN(dn).getRdn().toString();

                TreeItem it = new TreeItem(item, SWT.NONE);
                it.setText(rdn);
                it.setData(dn);

                new TreeItem(it, SWT.NONE);
            }
        } catch (Exception e) {
            log.debug(e.getMessage(), e);
        }
    }

    public String getBaseDn() {
        return baseDnText.getText().trim();
    }

    public String getFilter() {
        return filterText.getText().trim();
    }

    public String getScope() {
        return scopeCombo.getText().trim();
    }

    public String getObjectClasses() {
        return objectClassesText.getText().trim();
    }

    public boolean validatePage() {
        if (getBaseDn() == null) return false;
        if ("".equals(getFilter())) return false;
        if ("".equals(getScope())) return false;
        return true;
    }

    public void widgetSelected(SelectionEvent event) {
        setPageComplete(validatePage());
    }

    public void widgetDefaultSelected(SelectionEvent event) {
        setPageComplete(validatePage());
    }
}
