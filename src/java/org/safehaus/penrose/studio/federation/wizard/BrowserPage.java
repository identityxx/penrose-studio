package org.safehaus.penrose.studio.federation.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.TreeAdapter;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.ui.PlatformUI;
import org.eclipse.core.runtime.IProgressMonitor;
import org.apache.log4j.Logger;
import org.safehaus.penrose.ldap.*;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.source.SourceClient;

import java.lang.reflect.InvocationTargetException;

/**
 * @author Endi Sukma Dewata
 */
public class BrowserPage extends WizardPage {

    Logger log = Logger.getLogger(getClass());

    public final static String NAME = "Browser";

    Tree tree;
    Text dnText;

    private DN baseDn;
    private PartitionClient partitionClient;
    private SourceClient sourceClient;

    DN dn;

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
                try {
                    if (event.item == null) return;

                    TreeItem parent = (TreeItem)event.item;
                    expand(parent);

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });

        tree.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                if (tree.getSelectionCount() != 1) return;

                TreeItem item = tree.getSelection()[0];
                DN dn = (DN)item.getData();

                dnText.setText(dn.toString());
            }
        });

        TreeItem item = new TreeItem(tree, SWT.NONE);
        item.setText(baseDn.toString());
        item.setData(baseDn);

        try {
            expand(item);
            
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }

        item.setExpanded(true);

        dnText = new Text(composite, SWT.BORDER);
        dnText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        setControl(composite);
    }

    public void expand(TreeItem parent) throws Exception {
        parent.removeAll();

        final DN parentDn = (DN)parent.getData();

        final SearchRequest request = new SearchRequest();
        request.setDn(parentDn);
        request.setScope(SearchRequest.SCOPE_ONE);
        request.setAttributes(new String[] { "dn" });

        final SearchResponse response = new SearchResponse();

        IProgressService progressService = PlatformUI.getWorkbench().getProgressService();

        progressService.busyCursorWhile(new IRunnableWithProgress() {
            public void run(final IProgressMonitor monitor) throws InvocationTargetException {
                try {
                    monitor.beginTask("Searching "+parentDn, IProgressMonitor.UNKNOWN);

                    sourceClient.search(request, response);

                    monitor.worked(1);

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

            TreeItem item = new TreeItem(parent, SWT.NONE);
            item.setText(dn.getRdn().toString());
            item.setData(dn);

            new TreeItem(item, SWT.NONE);
        }
    }

    public DN getBaseDn() {
        return baseDn;
    }

    public void setBaseDn(DN baseDn) {
        this.baseDn = baseDn;
    }

    public PartitionClient getPartitionClient() {
        return partitionClient;
    }

    public void setPartitionClient(PartitionClient partitionClient) {
        this.partitionClient = partitionClient;
    }

    public SourceClient getSourceClient() {
        return sourceClient;
    }

    public void setSourceClient(SourceClient sourceClient) {
        this.sourceClient = sourceClient;
    }

    public String getDn() {
        return dnText.getText();
    }

    public void setDn(String dn) {
        this.dn = new DN(dn);
    }
}
