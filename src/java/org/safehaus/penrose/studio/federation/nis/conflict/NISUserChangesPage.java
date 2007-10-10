package org.safehaus.penrose.studio.federation.nis.conflict;

import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.jface.dialogs.MessageDialog;
import org.apache.log4j.Logger;
import org.safehaus.penrose.studio.federation.nis.NISRepository;
import org.safehaus.penrose.ldap.*;
import org.safehaus.penrose.studio.federation.nis.NISFederation;
import org.safehaus.penrose.studio.federation.nis.ownership.NISFilesPage;
import org.safehaus.penrose.studio.federation.nis.ownership.NISScriptsPage;
import org.safehaus.penrose.studio.nis.dialog.NISChangeDialog;

import java.util.Collection;
import java.util.ArrayList;

/**
 * @author Endi S. Dewata
 */
public class NISUserChangesPage extends FormPage {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    Table changesTable;
    Text messageText;
    Button activateButton;

    FormEditor editor;
    NISRepository domain;
    NISFederation nisFederation;

    String title;

    public NISUserChangesPage(FormEditor editor, NISFederation nisFederation) throws Exception {
        super(editor, "USERS", "  Users ");
        title = "Users";

        this.editor = editor;
        this.nisFederation = nisFederation;
    }

    public NISUserChangesPage(FormEditor editor, NISRepository domain, NISFederation nisFederation) throws Exception {
        super(editor, "CHANGES", "  Changes ");
        title = "Changes";

        this.editor = editor;
        this.domain = domain;
        this.nisFederation = nisFederation;
    }

    public void createFormContent(IManagedForm managedForm) {
        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText(title);

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Changes");
        section.setLayoutData(new GridData(GridData.FILL_BOTH));

        Control sourcesSection = createChangesSection(section);
        section.setClient(sourcesSection);

        refresh();
    }

    public void setActive(boolean b) {
        super.setActive(b);
        refresh();
    }

    public void refresh() {
        try {
            int indices[] = changesTable.getSelectionIndices();
            changesTable.removeAll();

            SearchRequest request = new SearchRequest();

            if (domain != null) {
                request.setFilter("(domain="+domain.getName()+")");
            }

            SearchResponse response = new SearchResponse() {
                public void add(SearchResult result) throws Exception {
                    Attributes attributes = result.getAttributes();

                    String domainName = (String)attributes.getValue("domain");
                    String uid = (String)attributes.getValue("uid");
                    Integer origUidNumber = (Integer)attributes.getValue("origUidNumber");
                    Integer uidNumber = (Integer)attributes.getValue("uidNumber");
                    Boolean active = (Boolean)attributes.getValue("active");

                    TableItem ti = new TableItem(changesTable, SWT.NONE);

                    int i = 0;
                    if (domain == null) {
                        ti.setText(i++, domainName);
                    }

                    ti.setText(i++, uid);
                    ti.setText(i++, ""+origUidNumber);
                    ti.setText(i++, ""+uidNumber);
                    ti.setText(i++, (active != null) && active ? "Yes" : "");
                    
                    ti.setData(result);
                }
            };

            nisFederation.getUsers().search(request, response);

            changesTable.select(indices);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            MessageDialog.openError(editor.getSite().getShell(), "Refresh Failed", e.getMessage());
        }

        update();
    }

    public void update() {
        try {
            if (changesTable.getSelectionCount() == 0) {
                messageText.setText("");
                activateButton.setText("Activate");
                return;
            }

            TableItem ti = changesTable.getSelection()[0];
            SearchResult result = (SearchResult)ti.getData();
            Attributes attributes = result.getAttributes();

            String message = (String)attributes.getValue("message");
            Boolean active = (Boolean)attributes.getValue("active");

            messageText.setText(message == null ? "" : message);

            if ((active != null) && active) {
                activateButton.setText("Deactivate");
            } else {
                activateButton.setText("Activate");
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            MessageDialog.openError(editor.getSite().getShell(), "Update Failed", e.getMessage());
        }
    }

    public Composite createChangesSection(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        Composite leftPanel = toolkit.createComposite(composite);
        leftPanel.setLayout(new GridLayout());
        leftPanel.setLayoutData(new GridData(GridData.FILL_BOTH));

        changesTable = new Table(leftPanel, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        changesTable.setLayoutData(new GridData(GridData.FILL_BOTH));
        changesTable.setHeaderVisible(true);
        changesTable.setLinesVisible(true);

        if (domain == null) {
            TableColumn tc = new TableColumn(changesTable, SWT.NONE);
            tc.setWidth(150);
            tc.setText("Domain");
        }

        TableColumn tc = new TableColumn(changesTable, SWT.NONE);
        tc.setWidth(100);
        tc.setText("User");

        tc = new TableColumn(changesTable, SWT.NONE);
        tc.setWidth(80);
        tc.setText("UID");

        tc = new TableColumn(changesTable, SWT.NONE);
        tc.setWidth(80);
        tc.setText("New UID");

        tc = new TableColumn(changesTable, SWT.NONE);
        tc.setWidth(50);
        tc.setText("Active");

        changesTable.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                update();
            }
        });

        toolkit.createLabel(leftPanel, "Message:", SWT.NONE);

        messageText = toolkit.createText(leftPanel, "", SWT.BORDER | SWT.READ_ONLY  | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.heightHint = 100;
        messageText.setLayoutData(gd);

        Composite buttons = toolkit.createComposite(composite);
        buttons.setLayout(new GridLayout());
        gd = new GridData(GridData.FILL_VERTICAL);
        gd.verticalSpan = 2;
        gd.widthHint = 120;
        buttons.setLayoutData(gd);

        if (domain != null) {
            Button addButton = new Button(buttons, SWT.PUSH);
            addButton.setText("Add");
            addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

            addButton.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent selectionEvent) {
                    try {
                        NISChangeDialog dialog = new NISChangeDialog(getSite().getShell(), SWT.NONE);
                        dialog.setTargetName("User");
                        dialog.setOldValueName("Old UID Number");
                        dialog.setNewValueName("New UID Number");
                        dialog.open();

                        int action = dialog.getAction();
                        if (action == NISChangeDialog.CANCEL) return;

                        RDNBuilder rb = new RDNBuilder();
                        rb.set("domain", domain.getName());
                        rb.set("uid", dialog.getTarget());
                        DN dn = new DN(rb.toRdn());

                        Attributes attributes = new Attributes();
                        attributes.setValue("domain", domain.getName());
                        attributes.setValue("uid", dialog.getTarget());
                        attributes.setValue("origUidNumber", dialog.getOldValue());
                        attributes.setValue("uidNumber", dialog.getNewValue());
                        attributes.setValue("message", dialog.getMessage());

                        nisFederation.getUsers().add(dn, attributes);

                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        MessageDialog.openError(editor.getSite().getShell(), "Edit Failed", e.getMessage());
                    }

                    refresh();
                }
            });
        }

        Button editButton = new Button(buttons, SWT.PUSH);
        editButton.setText("Edit");
        editButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        editButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                try {
                    if (changesTable.getSelectionCount() == 0) return;

                    TableItem item = changesTable.getSelection()[0];
                    SearchResult result = (SearchResult)item.getData();
                    Attributes attributes = result.getAttributes();

                    String uid = (String)attributes.getValue("uid");
                    Integer origUidNumber = (Integer)attributes.getValue("origUidNumber");
                    Integer uidNumber = (Integer)attributes.getValue("uidNumber");
                    String message = (String)attributes.getValue("message");

                    NISChangeDialog dialog = new NISChangeDialog(getSite().getShell(), SWT.NONE);
                    dialog.setTargetName("User");
                    dialog.setOldValueName("Old UID Number");
                    dialog.setNewValueName("New UID Number");
                    dialog.setTarget(uid);
                    dialog.setOldValue(""+origUidNumber);
                    dialog.setNewValue(""+uidNumber);
                    dialog.setMessage(message);
                    dialog.open();

                    int action = dialog.getAction();
                    if (action == NISChangeDialog.CANCEL) return;

                    uid = dialog.getTarget();
                    origUidNumber = new Integer(dialog.getOldValue());
                    uidNumber = new Integer(dialog.getNewValue());
                    message = dialog.getMessage();

                    Collection<Modification> modifications = new ArrayList<Modification>();
                    modifications.add(new Modification(Modification.REPLACE, new Attribute("uid", uid)));
                    modifications.add(new Modification(Modification.REPLACE, new Attribute("origUidNumber", origUidNumber)));
                    modifications.add(new Modification(Modification.REPLACE, new Attribute("uidNumber", uidNumber)));
                    modifications.add(new Modification(Modification.REPLACE, new Attribute("message", message)));

                    nisFederation.getUsers().modify(result.getDn(), modifications);

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    MessageDialog.openError(editor.getSite().getShell(), "Edit Failed", e.getMessage());
                }

                refresh();
            }
        });

        Button removeButton = new Button(buttons, SWT.PUSH);
        removeButton.setText("Remove");
        removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                try {
                    if (changesTable.getSelectionCount() == 0) return;

                    int index = changesTable.getSelectionIndex();

                    TableItem[] items = changesTable.getSelection();
                    for (TableItem ti : items) {
                        SearchResult result = (SearchResult)ti.getData();

                        nisFederation.getUsers().delete(result.getDn());
                    }

                    changesTable.select(index);

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    MessageDialog.openError(editor.getSite().getShell(), "Delete Failed", e.getMessage());
                }

                refresh();
            }
        });

        new Label(buttons, SWT.NONE);

        Button showFilesButton = new Button(buttons, SWT.PUSH);
        showFilesButton.setText("Show Files");
        showFilesButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        showFilesButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                try {
                    if (changesTable.getSelectionCount() == 0) return;

                    TableItem ti = changesTable.getSelection()[0];
                    SearchResult result = (SearchResult)ti.getData();
                    Attributes attributes = result.getAttributes();

                    Integer origUidNumber = (Integer)attributes.getValue("origUidNumber");

                    NISFilesPage page = (NISFilesPage)editor.setActivePage("FILES");
                    page.getHostsList().selectAll();
                    page.getUidText().setText(""+origUidNumber);
                    page.getGidText().setText("");

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    MessageDialog.openError(editor.getSite().getShell(), "Action Failed", e.getMessage());
                }
            }
        });

        Button changeFilesButton = new Button(buttons, SWT.PUSH);
        changeFilesButton.setText("Change Files");
        changeFilesButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        changeFilesButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                try {
                    if (changesTable.getSelectionCount() == 0) return;

                    TableItem ti = changesTable.getSelection()[0];
                    SearchResult result = (SearchResult)ti.getData();
                    Attributes attributes = result.getAttributes();

                    Integer origUidNumber = (Integer)attributes.getValue("origUidNumber");
                    Integer uidNumber = (Integer)attributes.getValue("uidNumber");

                    NISScriptsPage page = (NISScriptsPage)editor.setActivePage("TOOLS");
                    page.getActionsCombo().setText("Change file UID number");

                    page.getHostsList().selectAll();
                    page.getFromText().setText(""+origUidNumber);
                    page.getToText().setText(""+uidNumber);

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    MessageDialog.openError(editor.getSite().getShell(), "Action Failed", e.getMessage());
                }
            }
        });

        Button revertFilesButton = new Button(buttons, SWT.PUSH);
        revertFilesButton.setText("Revert Files");
        revertFilesButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        revertFilesButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                try {
                    if (changesTable.getSelectionCount() == 0) return;

                    TableItem ti = changesTable.getSelection()[0];
                    SearchResult result = (SearchResult)ti.getData();
                    Attributes attributes = result.getAttributes();

                    Integer origUidNumber = (Integer)attributes.getValue("origUidNumber");
                    Integer uidNumber = (Integer)attributes.getValue("uidNumber");

                    NISScriptsPage page = (NISScriptsPage)editor.setActivePage("TOOLS");
                    page.getActionsCombo().setText("Change file UID number");

                    page.getHostsList().selectAll();
                    page.getFromText().setText(""+uidNumber);
                    page.getToText().setText(""+origUidNumber);

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    MessageDialog.openError(editor.getSite().getShell(), "Action Failed", e.getMessage());
                }
            }
        });

        activateButton = new Button(buttons, SWT.PUSH);
        activateButton.setText("Activate");
        activateButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        activateButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                try {
                    if (changesTable.getSelectionCount() == 0) return;

                    TableItem ti = changesTable.getSelection()[0];
                    SearchResult result = (SearchResult)ti.getData();
                    Attributes attributes = result.getAttributes();

                    Boolean active = (Boolean)attributes.getValue("active");

                    Collection<Modification> modifications = new ArrayList<Modification>();

                    if ((active != null) && active) {
                        modifications.add(new Modification(Modification.DELETE, new Attribute("active")));
                    } else {
                        modifications.add(new Modification(Modification.REPLACE, new Attribute("active", true)));
                    }

                    nisFederation.getUsers().modify(result.getDn(), modifications);

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    MessageDialog.openError(editor.getSite().getShell(), "Action Failed", e.getMessage());
                }

                refresh();
            }
        });

        new Label(buttons, SWT.NONE);

        Button refreshButton = new Button(buttons, SWT.PUSH);
        refreshButton.setText("Refresh");
        refreshButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        refreshButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                refresh();
            }
        });

        return composite;
    }

}
