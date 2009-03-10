package org.safehaus.penrose.studio.federation.global;

import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.ui.PlatformUI;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.core.runtime.IProgressMonitor;
import org.apache.log4j.Logger;
import org.safehaus.penrose.federation.*;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.studio.ldap.dialog.AttributeDialog;
import org.safehaus.penrose.ldap.*;
import org.safehaus.penrose.source.SourceClient;
import org.safehaus.penrose.source.SourceManagerClient;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.util.BinaryUtil;
import org.safehaus.penrose.filter.Filter;
import org.safehaus.penrose.filter.SimpleFilter;
import org.safehaus.penrose.filter.FilterTool;

import java.util.*;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Endi S. Dewata
 */
public class GroupsPage extends FormPage {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    public Table localTable;
    public Table localAttributeTable;

    public Table remoteTable;
    public Table remoteAttributeTable;

    ConflictDetectionEditor editor;

    Server server;
    FederationClient federationClient;

    SourceClient globalSourceClient;

    String objectClass;
    String attributeName;

    public GroupsPage(ConflictDetectionEditor editor) throws Exception {
        super(editor, "GROUPS", "  Groups  ");

        this.editor = editor;
        this.server = editor.server;
        this.federationClient = editor.getFederationClient();

        PartitionClient federationPartitionClient = federationClient.getPartitionClient();
        SourceManagerClient sourceManagerClient = federationPartitionClient.getSourceManagerClient();

        globalSourceClient = sourceManagerClient.getSourceClient("Global");
    }

    public void createFormContent(IManagedForm managedForm) {
        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText("Groups");

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Action");
        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Control sourcesSection = createActionSection(section);
        section.setClient(sourcesSection);

        section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Results");
        section.setLayoutData(new GridData(GridData.FILL_BOTH));

        Control resultsSection = createResultsSection(section);
        section.setClient(resultsSection);
    }

    public Composite createActionSection(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        Button runButton = new Button(composite, SWT.PUSH);
        runButton.setText("Search");
        GridData gd = new GridData();
        gd.horizontalAlignment = GridData.END;
        gd.widthHint = 80;
        runButton.setLayoutData(gd);

        runButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    searchConflicts();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        return composite;
    }

    public Composite createResultsSection(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        composite.setLayout(new GridLayout(2, false));

        createUpperLeftSection(composite);
        createUpperRightSection(composite);
        createBottomLeftSection(composite);
        createBottomRightSection(composite);

        return composite;
    }

    public void createUpperLeftSection(Composite composite) {

        localTable = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION);
        localTable.setLayoutData(new GridData(GridData.FILL_BOTH));

        localTable.setHeaderVisible(true);
        localTable.setLinesVisible(true);

        TableColumn tc = new TableColumn(localTable, SWT.NONE);
        tc.setText("DN");
        tc.setWidth(300);

        localTable.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    if (localTable.getSelectionCount() == 0) return;

                    TableItem item = localTable.getSelection()[0];

                    ConflictDetectionResult result = (ConflictDetectionResult)item.getData();
                    SearchResult localEntry = result.getEntry();

                    showAttributes(localAttributeTable, localEntry);
                    showConflicts(result.getConflicts());

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });
    }

    public void createUpperRightSection(Composite composite) {

        remoteTable = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION);
        remoteTable.setLayoutData(new GridData(GridData.FILL_BOTH));

        remoteTable.setHeaderVisible(true);
        remoteTable.setLinesVisible(true);

        TableColumn tc = new TableColumn(remoteTable, SWT.NONE);
        tc.setText("DN");
        tc.setWidth(300);

        remoteTable.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    if (remoteTable.getSelectionCount() == 0) return;

                    TableItem item = remoteTable.getSelection()[0];

                    SearchResult remoteEntry = (SearchResult)item.getData();

                    showAttributes(remoteAttributeTable, remoteEntry);

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });
    }

    public void createBottomLeftSection(final Composite composite) {

        localAttributeTable = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.heightHint = 200;
        localAttributeTable.setLayoutData(gd);

        localAttributeTable.setHeaderVisible(true);
        localAttributeTable.setLinesVisible(true);

        TableColumn attributeTableColumn = new TableColumn(localAttributeTable, SWT.NONE);
        attributeTableColumn.setText("Attribute");
        attributeTableColumn.setWidth(100);

        TableColumn valueTableColumn = new TableColumn(localAttributeTable, SWT.NONE);
        valueTableColumn.setText("Value");
        valueTableColumn.setWidth(200);

        Menu menu = new Menu(localAttributeTable);
        localAttributeTable.setMenu(menu);

        MenuItem mi = new MenuItem(menu, SWT.PUSH);
        mi.setText("Edit Value");

        mi.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    if (localAttributeTable.getSelectionCount() != 1) return;

                    TableItem entryItem = localTable.getSelection()[0];
                    ConflictDetectionResult result = (ConflictDetectionResult)entryItem.getData();
                    SearchResult localEntry = result.getEntry();
                    DN dn = localEntry.getDn();

                    TableItem attributeItem = localAttributeTable.getSelection()[0];
                    String name = attributeItem.getText(0);
                    Object value = attributeItem.getData();

                    AttributeDialog dialog = new AttributeDialog(composite.getShell(), SWT.NONE);
                    dialog.setText("Edit Attribute");
                    dialog.setName(name);
                    dialog.setValue(value);

                    if (dialog.open() == AttributeDialog.CANCEL) return;

                    ModifyRequest request = new ModifyRequest();
                    request.setDn(dn);
                    request.addModification(new Modification(Modification.REPLACE, new Attribute(name, dialog.getValue())));

                    ModifyResponse response = new ModifyResponse();

                    globalSourceClient.modify(request, response);

                    localEntry = globalSourceClient.find(dn);
                    result.setEntry(localEntry);

                    showAttributes(localAttributeTable, localEntry);

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });
    }

    public void createBottomRightSection(final Composite composite) {

        remoteAttributeTable = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.heightHint = 200;
        remoteAttributeTable.setLayoutData(gd);

        remoteAttributeTable.setHeaderVisible(true);
        remoteAttributeTable.setLinesVisible(true);

        TableColumn tc = new TableColumn(remoteAttributeTable, SWT.NONE);
        tc.setText("Attribute");
        tc.setWidth(100);

        tc = new TableColumn(remoteAttributeTable, SWT.NONE);
        tc.setText("Value");
        tc.setWidth(200);

        Menu menu = new Menu(remoteAttributeTable);
        remoteAttributeTable.setMenu(menu);

        MenuItem mi = new MenuItem(menu, SWT.PUSH);
        mi.setText("Edit Value");

        mi.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    if (remoteAttributeTable.getSelectionCount() != 1) return;

                    TableItem localEntryItem = localTable.getSelection()[0];
                    ConflictDetectionResult result = (ConflictDetectionResult)localEntryItem.getData();

                    TableItem remoteEntryItem = remoteTable.getSelection()[0];
                    SearchResult remoteEntry = (SearchResult)remoteEntryItem.getData();
                    DN dn = remoteEntry.getDn();

                    TableItem attributeItem = remoteAttributeTable.getSelection()[0];
                    String name = attributeItem.getText(0);
                    Object value = attributeItem.getData();

                    AttributeDialog dialog = new AttributeDialog(composite.getShell(), SWT.NONE);
                    dialog.setText("Edit Attribute");
                    dialog.setName(name);
                    dialog.setValue(value);

                    if (dialog.open() == AttributeDialog.CANCEL) return;

                    ModifyRequest request = new ModifyRequest();
                    request.setDn(dn);
                    request.addModification(new Modification(Modification.REPLACE, new Attribute(name, dialog.getValue())));

                    ModifyResponse response = new ModifyResponse();

                    globalSourceClient.modify(request, response);

                    remoteEntry = globalSourceClient.find(dn);
                    result.addConflict(remoteEntry);

                    showAttributes(remoteAttributeTable, remoteEntry);

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });
    }

    public void showConflicts(Collection<SearchResult> conflicts) throws Exception {

        remoteTable.removeAll();
        remoteAttributeTable.removeAll();

        for (SearchResult remoteEntry : conflicts) {
            DN remoteDn = remoteEntry.getDn();

            TableItem ti = new TableItem(remoteTable, SWT.NONE);
            ti.setText(0, remoteDn.toString());
            ti.setData(remoteEntry);
        }

        if (conflicts.size() == 1) {
            remoteTable.setSelection(0);
            SearchResult remoteEntry = conflicts.iterator().next();
            showAttributes(remoteAttributeTable, remoteEntry);
        }
    }

    public void searchConflicts() throws Exception {

        localTable.removeAll();
        localAttributeTable.removeAll();
        remoteTable.removeAll();
        remoteAttributeTable.removeAll();

        final Collection<ConflictDetectionResult> results = new LinkedList<ConflictDetectionResult>();

        IProgressService progressService = PlatformUI.getWorkbench().getProgressService();

        progressService.busyCursorWhile(new IRunnableWithProgress() {
            public void run(final IProgressMonitor monitor) throws InvocationTargetException {
                try {
                    monitor.setTaskName("Searching conflicts...");

                    Filter localFilter = new SimpleFilter("objectClass", "=", objectClass);

                    SearchRequest localRequest = new SearchRequest();
                    localRequest.setFilter(localFilter);

                    SearchResponse localResponse = new SearchResponse();

                    globalSourceClient.search(localRequest, localResponse);

                    monitor.beginTask("Searching conflicts...", (int)localResponse.getTotalCount());

                    log.debug("Results:");

                    while (localResponse.hasNext()) {

                        SearchResult localEntry = localResponse.next();
                        DN localDn = localEntry.getDn();

                        monitor.subTask(localDn.toString());
                        log.debug(" - "+localDn+":");

                        ConflictDetectionResult result = searchConflicts(localEntry);
                        if (!result.getConflicts().isEmpty()) results.add(result);

                        monitor.worked(1);
                    }

                } catch (Exception e) {
                    throw new InvocationTargetException(e);

                } finally {
                    monitor.done();
                }
            }
        });

        for (ConflictDetectionResult result : results) {

            TableItem ti = new TableItem(localTable, SWT.NONE);
            ti.setText(0, result.getEntry().getDn().toString());
            ti.setData(result);
        }
    }

    public ConflictDetectionResult searchConflicts(SearchResult localEntry) throws Exception {

        ConflictDetectionResult result = new ConflictDetectionResult(localEntry);

        DN localDn = localEntry.getDn();
        Attributes localAttributes = localEntry.getAttributes();

        Object value = localAttributes.getValue(attributeName);

        Filter objectClassFilter = new SimpleFilter("objectClass", "=", objectClass);
        Filter attributeFilter = new SimpleFilter(attributeName, "=", value);
        Filter remoteFilter = FilterTool.appendAndFilter(objectClassFilter, attributeFilter);

        SearchRequest remoteRequest = new SearchRequest();
        remoteRequest.setFilter(remoteFilter);

        SearchResponse remoteResponse = new SearchResponse();

        globalSourceClient.search(remoteRequest, remoteResponse);

        while (remoteResponse.hasNext()) {
            SearchResult remoteEntry = remoteResponse.next();
            DN remoteDn = remoteEntry.getDn();

            if (localDn.equals(remoteDn)) continue;

            log.debug("   - "+remoteDn);
            result.addConflict(remoteEntry);
        }

        return result;
    }

    public void showAttributes(Table table, SearchResult entry) throws Exception {

        boolean debug = log.isDebugEnabled();
        table.removeAll();

        Attributes attributes = entry.getAttributes();
        log.debug("Attributes:");

        for (Attribute attribute : attributes.getAll()) {
            String attributeName = attribute.getName();
            //String normalizedAttributeName = attributeName.toLowerCase();

            if (debug) attribute.print();

            for (Object value : attribute.getValues()) {

                TableItem attrItem = new TableItem(table, SWT.NONE);
                attrItem.setText(0, attributeName);

                String s;
/*
                if (guidAttributes.contains(normalizedAttributeName)) {
                    if (value instanceof String) {
                        s = ActiveDirectory.getGUID(((String)value).getBytes());

                    } else {
                        s = ActiveDirectory.getGUID((byte[])value);
                    }

                } else if (sidAttributes.contains(normalizedAttributeName)) {
                    s = ActiveDirectory.getSID((byte[])value);

                } else
*/
                if (value instanceof byte[]) {
                    s = BinaryUtil.encode(BinaryUtil.BIG_INTEGER, (byte[])value);

                } else {
                    s = value.toString();
                }

                attrItem.setText(1, s);
                attrItem.setData(value);
            }
        }
    }

    public String getObjectClass() {
        return objectClass;
    }

    public void setObjectClass(String objectClass) {
        this.objectClass = objectClass;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }
}