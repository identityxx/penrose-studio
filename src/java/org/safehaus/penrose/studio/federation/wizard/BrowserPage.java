package org.safehaus.penrose.studio.federation.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.TreeAdapter;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.apache.log4j.Logger;
import org.safehaus.penrose.source.Source;
import org.safehaus.penrose.ldap.*;

/**
 * @author Endi Sukma Dewata
 */
public class BrowserPage extends WizardPage {

    Logger log = Logger.getLogger(getClass());

    public final static String NAME = "Browser";

    Tree tree;
    Text dnText;

    private String baseDn;
    private Source source;

    public BrowserPage() {
        super(NAME);
        setDescription("Select an entry from the LDAP tree or enter the DN manually.");
    }

    public void createControl(final Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout());

        tree = new Tree(composite, SWT.BORDER);
        tree.setLayoutData(new GridData(GridData.FILL_BOTH));

        tree.addTreeListener(new TreeAdapter() {
            public void treeExpanded(TreeEvent event) {
                if (event.item == null) return;

                TreeItem parent = (TreeItem)event.item;
                expand(parent);
            }
        });

        tree.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                if (tree.getSelectionCount() != 1) return;

                TreeItem item = tree.getSelection()[0];
                String dn = (String)item.getData();

                DNBuilder db = new DNBuilder();
                db.append(dn);
                db.append(baseDn);

                dnText.setText(db.toDn().toString());
            }
        });

        TreeItem item = new TreeItem(tree, SWT.NONE);
        item.setText(baseDn);
        item.setData("");

        expand(item);

        item.setExpanded(true);

        dnText = new Text(composite, SWT.BORDER);
        dnText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        setControl(composite);
    }

    public void expand(TreeItem parent) {
        try {
            parent.removeAll();

            String parentDn = (String)parent.getData();

            SearchResponse response = source.search(parentDn, null, SearchRequest.SCOPE_ONE);

            while (response.hasNext()) {
                SearchResult result = response.next();
                DN dn = result.getDn();

                TreeItem item = new TreeItem(parent, SWT.NONE);
                item.setText(dn.getRdn().toString());
                item.setData(dn.toString());

                new TreeItem(item, SWT.NONE);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            MessageDialog.openError(getShell(), "Action Failed", e.getMessage());
        }
    }

    public String getBaseDn() {
        return baseDn;
    }

    public void setBaseDn(String baseDn) {
        this.baseDn = baseDn;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public String getDn() {
        return dnText.getText();
    }
}
