package org.safehaus.penrose.studio.source.editor;

import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.safehaus.penrose.ldap.*;
import org.safehaus.penrose.source.SourceClient;
import org.safehaus.penrose.source.SourceManagerClient;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.partition.PartitionClient;

public class SourceBrowsePage extends SourceEditorPage implements TreeListener {

    Button refreshButton;
    Text maxSizeText;

    Tree tree;
    Table table;

    public SourceBrowsePage(SourceEditor editor) throws Exception {
        super(editor, "BROWSE", "Browse");
    }

    public void createFormContent(IManagedForm managedForm) {
        super.createFormContent(managedForm);

        ScrolledForm form = managedForm.getForm();

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Actions");
        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Control actionsSection = createActionsSection(section);
        section.setClient(actionsSection);

        section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Entries");
        section.setLayoutData(new GridData(GridData.FILL_BOTH));

        Control entriesSection = createEntriesSection(section);
        section.setClient(entriesSection);

        section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Attributes");
        section.setLayoutData(new GridData(GridData.FILL_BOTH));

        Control attributesSection = createAttributesSection(section);
        section.setClient(attributesSection);

        refresh();
    }

    public Composite createActionsSection(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new RowLayout());

        Hyperlink refreshSchema = toolkit.createHyperlink(composite, "Refresh", SWT.NONE);

        refreshSchema.addHyperlinkListener(new HyperlinkAdapter() {
            public void linkActivated(HyperlinkEvent event) {
                refresh();
            }
        });

        return composite;
    }

    public Composite createEntriesSection(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout());

        tree = toolkit.createTree(composite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.heightHint = 100;
        tree.setLayoutData(gd);

        tree.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    TreeItem item = tree.getSelection()[0];
                    DN entryDn = (DN)item.getData();

                    PenroseClient client = server.getClient();
                    PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
                    PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
                    SourceManagerClient sourceManagerClient = partitionClient.getSourceManagerClient();

                    SourceClient sourceClient = sourceManagerClient.getSourceClient(sourceConfig.getName());
                    SearchResult entry = sourceClient.find(entryDn);
                    
                    showEntry(entry);

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        tree.addTreeListener(this);

        return composite;
    }

    public Composite createAttributesSection(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout());

        table = toolkit.createTable(composite, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.heightHint = 100;
        table.setLayoutData(gd);

        TableColumn tc = new TableColumn(table, SWT.LEFT);
        tc.setText("Attribute Name");
        tc.setWidth(200);

        tc = new TableColumn(table, SWT.LEFT);
        tc.setText("Attribute Value");
        tc.setWidth(300);

        return composite;
    }

    public void showEntry(SearchResult entry) throws Exception {
        table.removeAll();

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

    public void refresh() {

        tree.removeAll();

        try {
            PenroseClient client = server.getClient();
            PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
            PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
            SourceManagerClient sourceManagerClient = partitionClient.getSourceManagerClient();

            SourceClient sourceClient = sourceManagerClient.getSourceClient(sourceConfig.getName());

            DN rootDn = new DN();
            SearchResult root = sourceClient.find(rootDn);
            if (root == null) return;

            TreeItem item = new TreeItem(tree, SWT.NONE);
            item.setText("Root");
            item.setData(rootDn);

            log.debug("Naming contexts:");

            SearchRequest request = new SearchRequest();
            request.setDn(rootDn);
            request.setScope(SearchRequest.SCOPE_ONE);

            SearchResponse response = new SearchResponse();

            sourceClient.search(request, response);

            while (response.hasNext()) {

                SearchResult child = response.next();
                DN childDn = child.getDn();
                log.debug(" - "+childDn);

                TreeItem it = new TreeItem(item, SWT.NONE);
                it.setText(childDn.toString());
                it.setData(childDn);

                new TreeItem(it, SWT.NONE);
            }

            showEntry(root);

            item.setExpanded(true);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }

    public void treeCollapsed(TreeEvent event) {
    }

    public void treeExpanded(TreeEvent event) {
        try {
            if (event.item == null) return;

            TreeItem item = (TreeItem)event.item;
            DN entryDn = (DN)item.getData();
            log.debug("Expanding "+entryDn);

            TreeItem items[] = item.getItems();
            for (TreeItem item1 : items) {
                item1.dispose();
            }

            PenroseClient client = server.getClient();
            PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
            PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
            SourceManagerClient sourceManagerClient = partitionClient.getSourceManagerClient();

            SourceClient sourceClient = sourceManagerClient.getSourceClient(sourceConfig.getName());

            SearchRequest request = new SearchRequest();
            request.setDn(entryDn);
            request.setScope(SearchRequest.SCOPE_ONE);

            SearchResponse response = new SearchResponse();

            sourceClient.search(request, response);

            while (response.hasNext()) {

                SearchResult child = response.next();
                DN childDn = child.getDn();
                log.debug(" - "+childDn);

                TreeItem it = new TreeItem(item, SWT.NONE);
                it.setText(entryDn.isEmpty() ? childDn.toString() : childDn.getRdn().toString());
                it.setData(childDn);

                new TreeItem(it, SWT.NONE);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }
}
