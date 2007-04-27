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
import java.util.Map;
import java.util.HashMap;

/**
 * @author Endi S. Dewata
 */
public class NISUsersPage extends FormPage {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    Combo actionCombo;
    List domainsList;

    Label message;
    Table table;

    NISEditor editor;

    public NISUsersPage(NISEditor editor) {
        super(editor, "USERS", "  Users  ");

        this.editor = editor;
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

           PenroseApplication penroseApplication = PenroseApplication.getInstance();
           PenroseContext penroseContext = penroseApplication.getPenroseContext();
           SourceManager sourceManager = penroseContext.getSourceManager();

           Source actionsSource = sourceManager.getSource("DEFAULT", "actions");

           SearchRequest request = new SearchRequest();
           request.setFilter("(type=users)");

           SearchResponse<SearchResult> response = new SearchResponse<SearchResult>() {
               public void add(SearchResult result) throws Exception {
                   Attributes attributes = result.getAttributes();
                   String actionName = (String)attributes.getValue("name");
                   String className = (String)attributes.getValue("className");

                   actionCombo.add(actionName);
                   actionCombo.setData(actionName, className);
               }
           };

           actionsSource.search(request, response);

           actionCombo.select(0);

           Source domainsSource = sourceManager.getSource("DEFAULT", "domains");

           request = new SearchRequest();
           response = new SearchResponse<SearchResult>() {
               public void add(SearchResult result) throws Exception {
                   Attributes attributes = result.getAttributes();
                   String domainName = (String)attributes.getValue("name");
                   String partitionName = (String)attributes.getValue("partition");
                   domainsList.add(domainName);
                   domainsList.setData(domainName, partitionName);
               }
           };

           domainsSource.search(request, response);

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

        domainsList = new List(composite, SWT.BORDER | SWT.MULTI);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        gd.heightHint = 100;
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

        message = toolkit.createLabel(composite, "");
        message.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        table = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION);
        table.setLayoutData(new GridData(GridData.FILL_BOTH));

        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        TableColumn tc = new TableColumn(table, SWT.NONE);
        tc.setText("Domain 1");
        tc.setWidth(100);

        tc = new TableColumn(table, SWT.NONE);
        tc.setText("User");
        tc.setWidth(100);

        tc = new TableColumn(table, SWT.NONE);
        tc.setText("UID");
        tc.setWidth(100);

        tc = new TableColumn(table, SWT.NONE);
        tc.setText("Domain 2");
        tc.setWidth(100);

        tc = new TableColumn(table, SWT.NONE);
        tc.setText("User");
        tc.setWidth(100);

        tc = new TableColumn(table, SWT.NONE);
        tc.setText("UID");
        tc.setWidth(100);

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
                    String domainName = (String)item.getData("domain1");
                    String partitionName = (String)item.getData("partition1");
                    Source source = (Source)item.getData("source1");
                    Object uid = item.getData("uid1");
                    Object uidNumber = item.getData("uidNumber1");
                    edit(domainName, partitionName, source, uid, uidNumber);

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
                    String domainName = (String)item.getData("domain2");
                    String partitionName = (String)item.getData("partition2");
                    Source source = (Source)item.getData("source2");
                    Object uid = item.getData("uid2");
                    Object uidNumber = item.getData("uidNumber2");
                    edit(domainName, partitionName, source, uid, uidNumber);

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
        String className = (String)actionCombo.getData(actionName);

        Class clazz = Class.forName(className);
        NISAction action = (NISAction)clazz.newInstance();

        NISActionRequest request = new NISActionRequest();

        String[] domains = domainsList.getSelection();
        for (int i=0; i<domains.length; i++) {
            request.addPartition(domains[i]);
        }

        NISActionResponse response = new NISActionResponse() {

            int counter = 0;

            public void add(Object object) {
                Attributes attributes = (Attributes)object;

                log.debug("Displaying result #"+counter);

                String domain1Name = (String)attributes.getValue("domain1");
                String partition1Name = (String)attributes.getValue("partition1");
                Source source1 = (Source)attributes.getValue("source1");
                Object uid1 = attributes.getValue("uid1");
                Object uidNumber1 = attributes.getValue("uidNumber1");

                String domain2Name = (String)attributes.getValue("domain2");
                String partition2Name = (String)attributes.getValue("partition2");
                Source source2 = (Source)attributes.getValue("source2");
                Object uid2 = attributes.getValue("uid2");
                Object uidNumber2 = attributes.getValue("uidNumber2");

                TableItem ti = new TableItem(table, SWT.NONE);
                ti.setText(0, domain1Name);
                ti.setText(1, ""+uid1);
                ti.setText(2, ""+uidNumber1);
                ti.setText(3, domain2Name);
                ti.setText(4, ""+uid2);
                ti.setText(5, ""+uidNumber2);

                ti.setData("domain1", domain1Name);
                ti.setData("partition1", partition1Name);
                ti.setData("source1", source1);
                ti.setData("uid1", uid1);
                ti.setData("uidNumber1", uidNumber1);

                ti.setData("domain2", domain2Name);
                ti.setData("partition2", partition2Name);
                ti.setData("source2", source2);
                ti.setData("uid2", uid2);
                ti.setData("uidNumber2", uidNumber2);

                counter++;
            }

            public void close() {
                message.setText("Found "+counter+" result(s).");
            }
        };

        action.execute(request, response);
    }

    public void edit(
            String domainName,
            String partitionName,
            Source source,
            Object uid,
            Object uidNumber
    ) throws Exception {

        PenroseApplication penroseApplication = PenroseApplication.getInstance();
        PenroseContext penroseContext = penroseApplication.getPenroseContext();
        SourceManager sourceManager = penroseContext.getSourceManager();

        Attributes attributes = new Attributes();
        attributes.setValue("domain", domainName);
        attributes.setValue("uid", uid);
        attributes.setValue("uidNumber", uidNumber);

        NISUserDialog dialog = new NISUserDialog(getSite().getShell(), SWT.NONE);
        dialog.setAttributes(attributes);

        Source sourceUidNumber = sourceManager.getSource(partitionName, "users_uidNumber");
        dialog.setSourceConfig(sourceUidNumber.getSourceConfig());

        SearchRequest request = new SearchRequest();
        request.setFilter("(uid="+uid+")");
        SearchResponse<SearchResult> response = new SearchResponse<SearchResult>();

        sourceUidNumber.search(request, response);
        while (response.hasNext()) {
            SearchResult result = (SearchResult)response.next();
            Attributes attrs = result.getAttributes();
            Object un = attrs.getValue("uidNumber");
            dialog.addNewUidNumber(un);
        }

        dialog.open();

        int action = dialog.getAction();

        if (action == NISUserDialog.CANCEL) return;

        Object un = dialog.getUidNumber();
        String message = dialog.getMessage();

        RDNBuilder rb = new RDNBuilder();
        rb.set("uid", uid);
        rb.set("uidNumber", un);
        DN dn = new DN(rb.toRdn());

        String changes;

        if (action == NISUserDialog.ADD) {

            checkUidNumber(un);

            if (dialog.getNewUidNumbers().size() == 0) {
                addOriginalUidNumber(sourceUidNumber, uid, uidNumber);
            }

            Attributes attrs = new Attributes();
            attrs.setValue("uid", uid);
            attrs.setValue("uidNumber", un);

            sourceUidNumber.add(dn, attrs);

            changes = "add uidNumber "+un;

        } else { // if (action == NISUserDialog.REMOVE) {

            sourceUidNumber.delete(dn);

            Collection uidNumbers = dialog.getNewUidNumbers();
            if (uidNumbers.size() == 2 && uidNumbers.contains(uidNumber) && !uidNumber.equals(un)) {
                removeOriginalUidNumber(sourceUidNumber, uid, uidNumber);
            }

            changes = "delete uidNumber "+un;
        }

        Source changeLog = sourceManager.getSource("DEFAULT", "changelog");

        Attributes attrs = new Attributes();
        attrs.setValue("domain", domainName);
        attrs.setValue("target", "uid="+uid);
        attrs.setValue("type", "users");
        attrs.setValue("changes", changes);
        attrs.setValue("message", message);

        changeLog.add(new DN(), attrs);
    }

    public void checkUidNumber(Object uidNumber) throws Exception {

        PenroseApplication penroseApplication = PenroseApplication.getInstance();
        PenroseContext penroseContext = penroseApplication.getPenroseContext();
        SourceManager sourceManager = penroseContext.getSourceManager();

        String domainNames[] = domainsList.getItems();
        for (int i=0; i<domainNames.length; i++) {
            String domainName = domainNames[i];
            String partitionName = (String)domainsList.getData(domainName);

            SearchRequest request = new SearchRequest();
            request.setFilter("(uidNumber="+uidNumber+")");

            SearchResponse<SearchResult> response = new SearchResponse<SearchResult>();

            Source users = sourceManager.getSource(partitionName, "cache.users");
            users.search(request, response);

            if (response.hasNext()) {
                throw new Exception("uidNumber "+uidNumber+" already exists in domain "+domainName);
            }

            response = new SearchResponse<SearchResult>();

            Source uidNumbers = sourceManager.getSource(partitionName, "users_uidNumber");
            uidNumbers.search(request, response);

            if (response.hasNext()) {
                throw new Exception("uidNumber "+uidNumber+" already exists in domain "+domainName);
            }
        }
    }

    public void addOriginalUidNumber(
            Source sourceUidNumber,
            Object uid,
            Object uidNumber
    ) throws Exception {
        
        RDNBuilder rb = new RDNBuilder();
        rb.set("uid", uid);
        rb.set("uidNumber", uidNumber);
        DN dn = new DN(rb.toRdn());

        Attributes attrs = new Attributes();
        attrs.setValue("uid", uid);
        attrs.setValue("uidNumber", uidNumber);

        sourceUidNumber.add(dn, attrs);
    }

    public void removeOriginalUidNumber(
            Source sourceUidNumber,
            Object uid,
            Object uidNumber
    ) throws Exception {

        RDNBuilder rb = new RDNBuilder();
        rb.set("uid", uid);
        rb.set("uidNumber", uidNumber);
        DN dn = new DN(rb.toRdn());

        sourceUidNumber.delete(dn);
    }
}
