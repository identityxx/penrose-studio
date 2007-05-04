package org.safehaus.penrose.studio.nis;

import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
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
import org.eclipse.jface.dialogs.MessageDialog;
import org.apache.log4j.Logger;
import org.safehaus.penrose.studio.PenroseApplication;
import org.safehaus.penrose.studio.nis.action.*;
import org.safehaus.penrose.source.SourceManager;
import org.safehaus.penrose.source.Source;
import org.safehaus.penrose.naming.PenroseContext;
import org.safehaus.penrose.ldap.*;

import java.util.Collection;
import java.util.ArrayList;

/**
 * @author Endi S. Dewata
 */
public class NISUsersPage extends FormPage {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    Combo actionCombo;
    List domainsList;

    Label messageLabel;
    Table table;

    NISEditor editor;

    Source actions;
    Source domains;

    public NISUsersPage(NISEditor editor) {
        super(editor, "USERS", "  Users  ");

        this.editor = editor;

        PenroseApplication penroseApplication = PenroseApplication.getInstance();
        PenroseContext penroseContext = penroseApplication.getPenroseContext();
        SourceManager sourceManager = penroseContext.getSourceManager();

        actions = sourceManager.getSource("DEFAULT", "actions");
        domains = sourceManager.getSource("DEFAULT", "domains");

    }

    public void createFormContent(IManagedForm managedForm) {
        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText("NIS Users");

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

        init();
    }

    public void init() {
        try {
            actionCombo.removeAll();
            domainsList.removeAll();

            SearchRequest request = new SearchRequest();
            request.setFilter("(type=users)");

            SearchResponse<SearchResult> response = new SearchResponse<SearchResult>() {
                public void add(SearchResult result) throws Exception {
                    Attributes attributes = result.getAttributes();
                    String actionName = (String) attributes.getValue("name");
                    String className = (String) attributes.getValue("className");

                    actionCombo.add(actionName);
                    actionCombo.setData(actionName, className);
                }
            };

            actions.search(request, response);

            actionCombo.select(0);

            request = new SearchRequest();
            response = new SearchResponse<SearchResult>() {
                public void add(SearchResult result) throws Exception {
                    Attributes attributes = result.getAttributes();
                    String domain = (String) attributes.getValue("name");
                    String partition = (String) attributes.getValue("partition");
                    domainsList.add(domain);
                    domainsList.setData(domain, partition);
                }
            };

            domains.search(request, response);

            domainsList.selectAll();

        } catch (Exception e) {
            log.debug(e.getMessage(), e);
            String message = e.toString();
            if (message.length() > 500) {
                message = message.substring(0, 500) + "...";
            }
            MessageDialog.openError(editor.getSite().getShell(), "Init Failed", message);
        }
    }

    public Composite createActionSection(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(3, false));

        Label actionLabel = toolkit.createLabel(composite, "Action:");
        GridData gd = new GridData();
        gd.widthHint = 100;
        actionLabel.setLayoutData(gd);

        actionCombo = new Combo(composite, SWT.READ_ONLY);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        actionCombo.setLayoutData(gd);

        Label domainLabel = toolkit.createLabel(composite, "Domain:");
        gd = new GridData();
        gd.verticalAlignment = GridData.BEGINNING;
        gd.widthHint = 100;
        domainLabel.setLayoutData(gd);

        domainsList = new List(composite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        gd.heightHint = 80;
        domainsList.setLayoutData(gd);

        new Label(composite, SWT.NONE);

        Composite links = toolkit.createComposite(composite);
        links.setLayout(new RowLayout());
        links.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Hyperlink selectAllLink = toolkit.createHyperlink(links, "Select All", SWT.NONE);

        selectAllLink.addHyperlinkListener(new HyperlinkAdapter() {
            public void linkActivated(HyperlinkEvent event) {
                domainsList.selectAll();
            }
        });

        Hyperlink selectNoneLink = toolkit.createHyperlink(links, "Select None", SWT.NONE);

        selectNoneLink.addHyperlinkListener(new HyperlinkAdapter() {
            public void linkActivated(HyperlinkEvent event) {
                domainsList.deselectAll();
            }
        });

        Button runButton = new Button(composite, SWT.PUSH);
        runButton.setText("Run");
        gd = new GridData();
        gd.horizontalAlignment = GridData.END;
        gd.widthHint = 80;
        runButton.setLayoutData(gd);

        runButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    run();
                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                    String message = e.toString();
                    if (message.length() > 500) {
                        message = message.substring(0, 500) + "...";
                    }
                    MessageDialog.openError(editor.getSite().getShell(), "Action Failed", message);
                }
            }
        });

        return composite;
    }

    public Composite createResultsSection(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        composite.setLayout(new GridLayout());

        messageLabel = toolkit.createLabel(composite, "");
        messageLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        table = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION);
        table.setLayoutData(new GridData(GridData.FILL_BOTH));

        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        TableColumn tc = new TableColumn(table, SWT.NONE);
        tc.setText("Domain 1");
        tc.setWidth(120);

        tc = new TableColumn(table, SWT.NONE);
        tc.setText("User");
        tc.setWidth(100);

        tc = new TableColumn(table, SWT.NONE);
        tc.setText("UID");
        tc.setWidth(80);

        tc = new TableColumn(table, SWT.NONE);
        tc.setText("Domain 2");
        tc.setWidth(120);

        tc = new TableColumn(table, SWT.NONE);
        tc.setText("User");
        tc.setWidth(100);

        tc = new TableColumn(table, SWT.NONE);
        tc.setText("UID");
        tc.setWidth(80);

        Composite buttons = toolkit.createComposite(composite);
        buttons.setLayout(new RowLayout());
        buttons.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Button edit1Button = new Button(buttons, SWT.PUSH);
        edit1Button.setText("Edit user from domain 1");

        edit1Button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    if (table.getSelectionCount() == 0) return;

                    TableItem item = table.getSelection()[0];
                    String domain = (String) item.getData("domain1");
                    String partition = (String) item.getData("partition1");
                    Source source = (Source) item.getData("source1");
                    String uid = (String) item.getData("uid1");
                    Object uidNumber = item.getData("uidNumber1");
                    edit(domain, partition, source, uid, uidNumber);

                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                    String message = e.toString();
                    if (message.length() > 500) {
                        message = message.substring(0, 500) + "...";
                    }
                    MessageDialog.openError(editor.getSite().getShell(), "Edit Failed", message);
                }
            }
        });

        Button edit2Button = new Button(buttons, SWT.PUSH);
        edit2Button.setText("Edit user from domain 2");

        edit2Button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    if (table.getSelectionCount() == 0) return;

                    TableItem item = table.getSelection()[0];
                    String domain = (String) item.getData("domain2");
                    String partition = (String) item.getData("partition2");
                    Source source = (Source) item.getData("source2");
                    String uid = (String) item.getData("uid2");
                    Object uidNumber = item.getData("uidNumber2");
                    edit(domain, partition, source, uid, uidNumber);

                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                    String message = e.toString();
                    if (message.length() > 500) {
                        message = message.substring(0, 500) + "...";
                    }
                    MessageDialog.openError(editor.getSite().getShell(), "Edit Failed", message);
                }
            }
        });

        return composite;
    }

    public void run() throws Exception {

        table.removeAll();

        String actionName = actionCombo.getText();
        String className = (String) actionCombo.getData(actionName);

        Class clazz = Class.forName(className);
        NISAction action = (NISAction) clazz.newInstance();

        NISActionRequest request = new NISActionRequest();

        String[] domains = domainsList.getSelection();
        for (String domain : domains) {
            request.addPartition(domain);
        }

        NISActionResponse response = new NISActionResponse() {

            int counter = 0;

            public void add(Object object) {
                Attributes attributes = (Attributes) object;

                log.debug("Displaying result #" + counter);

                String domain1 = (String) attributes.getValue("domain1");
                String partition1 = (String) attributes.getValue("partition1");
                Source source1 = (Source) attributes.getValue("source1");
                String uid1 = (String) attributes.getValue("uid1");
                Object uidNumber1 = attributes.getValue("uidNumber1");

                String domain2 = (String) attributes.getValue("domain2");
                String partition2 = (String) attributes.getValue("partition2");
                Source source2 = (Source) attributes.getValue("source2");
                String uid2 = (String) attributes.getValue("uid2");
                Object uidNumber2 = attributes.getValue("uidNumber2");

                TableItem ti = new TableItem(table, SWT.NONE);
                ti.setText(0, domain1);
                ti.setText(1, "" + uid1);
                ti.setText(2, "" + uidNumber1);
                ti.setText(3, domain2);
                ti.setText(4, "" + uid2);
                ti.setText(5, "" + uidNumber2);

                ti.setData("domain1", domain1);
                ti.setData("partition1", partition1);
                ti.setData("source1", source1);
                ti.setData("uid1", uid1);
                ti.setData("uidNumber1", uidNumber1);

                ti.setData("domain2", domain2);
                ti.setData("partition2", partition2);
                ti.setData("source2", source2);
                ti.setData("uid2", uid2);
                ti.setData("uidNumber2", uidNumber2);

                counter++;
            }

            public void close() {
                messageLabel.setText("Found " + counter + " result(s).");
            }
        };

        action.execute(request, response);
    }

    public void edit(
            String domain,
            String partition,
            Source source,
            String uid,
            Object origUidNumber
    ) throws Exception {

        PenroseApplication penroseApplication = PenroseApplication.getInstance();
        PenroseContext penroseContext = penroseApplication.getPenroseContext();
        SourceManager sourceManager = penroseContext.getSourceManager();

        RDNBuilder rb = new RDNBuilder();
        rb.set("uid", uid);
        DN dn = new DN(rb.toRdn());

        NISUserDialog dialog = new NISUserDialog(getSite().getShell(), SWT.NONE);
        dialog.setDomain(domain);
        dialog.setUid(uid);
        dialog.setOrigUidNumber(origUidNumber);

        Source sourceUidNumber = sourceManager.getSource(partition, "users_uidNumber");
        dialog.setSourceConfig(sourceUidNumber.getSourceConfig());

        SearchRequest request = new SearchRequest();
        request.setDn(dn);
        SearchResponse<SearchResult> response = new SearchResponse<SearchResult>();

        sourceUidNumber.search(request, response);

        Object currentUidNumber;

        if (response.hasNext()) {
            SearchResult result = response.next();
            Attributes attributes = result.getAttributes();
            currentUidNumber = attributes.getValue("uidNumber");
            dialog.setNewUidNumber(currentUidNumber);

        } else {
            currentUidNumber = origUidNumber;
        }

        dialog.open();

        int action = dialog.getAction();

        if (action == NISUserDialog.CANCEL) return;

        Object newUidNumber = dialog.getUidNumber();
        String message = dialog.getMessage();

        if (action == NISUserDialog.SET) {

            if (!origUidNumber.equals(newUidNumber)) checkUidNumber(newUidNumber);

            Attributes attrs = new Attributes();
            attrs.setValue("uidNumber", newUidNumber);

            sourceUidNumber.add(dn, attrs);

        } else if (action == NISUserDialog.CHANGE) {

            if (!origUidNumber.equals(newUidNumber)) checkUidNumber(newUidNumber);

            Collection<Modification> modifications = new ArrayList<Modification>();
            modifications.add(new Modification(Modification.REPLACE, new Attribute("uidNumber", newUidNumber)));

            sourceUidNumber.modify(dn, modifications);

        } else { // if (action == NISUserDialog.REMOVE) {

            sourceUidNumber.delete(dn);
            newUidNumber = origUidNumber;
        }

        Source changes = sourceManager.getSource("DEFAULT", "changes");

        Attributes attributes = new Attributes();
        attributes.setValue("domain", domain);
        attributes.setValue("type", "user");
        attributes.setValue("target", uid);
        attributes.setValue("oldValue", currentUidNumber.toString());
        attributes.setValue("newValue", newUidNumber.toString());
        attributes.setValue("message", message);

        changes.add(new DN(), attributes);
    }

    public void checkUidNumber(Object uidNumber) throws Exception {

        PenroseApplication penroseApplication = PenroseApplication.getInstance();
        PenroseContext penroseContext = penroseApplication.getPenroseContext();
        SourceManager sourceManager = penroseContext.getSourceManager();

        for (String domainName : domainsList.getItems()) {
            String partitionName = (String) domainsList.getData(domainName);

            SearchRequest request = new SearchRequest();
            request.setFilter("(uidNumber=" + uidNumber + ")");

            SearchResponse<SearchResult> response = new SearchResponse<SearchResult>();

            Source users = sourceManager.getSource(partitionName, "cache.users");
            users.search(request, response);

            if (response.hasNext()) {
                throw new Exception("uidNumber " + uidNumber + " already exists in domain " + domainName);
            }

            response = new SearchResponse<SearchResult>();

            Source uidNumbers = sourceManager.getSource(partitionName, "users_uidNumber");
            uidNumbers.search(request, response);

            if (response.hasNext()) {
                throw new Exception("uidNumber " + uidNumber + " already exists in domain " + domainName);
            }
        }
    }
}
