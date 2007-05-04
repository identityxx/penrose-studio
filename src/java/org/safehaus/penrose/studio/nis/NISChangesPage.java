package org.safehaus.penrose.studio.nis;

import org.eclipse.ui.forms.editor.FormPage;
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
import org.safehaus.penrose.studio.PenroseApplication;
import org.safehaus.penrose.naming.PenroseContext;
import org.safehaus.penrose.source.SourceManager;
import org.safehaus.penrose.source.Source;
import org.safehaus.penrose.ldap.SearchRequest;
import org.safehaus.penrose.ldap.SearchResult;
import org.safehaus.penrose.ldap.SearchResponse;
import org.safehaus.penrose.ldap.Attributes;

/**
 * @author Endi S. Dewata
 */
public class NISChangesPage extends FormPage {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    NISEditor editor;

    Table logsTable;
    Text messageText;
    Button activateButton;

    Source source;

    public NISChangesPage(NISEditor editor) throws Exception {
        super(editor, "CHANGES", "  Changes ");

        this.editor = editor;

        PenroseApplication penroseApplication = PenroseApplication.getInstance();
        PenroseContext penroseContext = penroseApplication.getPenroseContext();
        SourceManager sourceManager = penroseContext.getSourceManager();

        source = sourceManager.getSource("DEFAULT", "changes");
    }

    public void createFormContent(IManagedForm managedForm) {
        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText("Changes");

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
           logsTable.removeAll();

           SearchRequest request = new SearchRequest();
           SearchResponse<SearchResult> response = new SearchResponse<SearchResult>() {
               public void add(SearchResult result) throws Exception {
                   Attributes attributes = result.getAttributes();
                   
                   Object changeNumber = attributes.getValue("changeNumber");
                   String domain = (String)attributes.getValue("domain");
                   String type = (String)attributes.getValue("type");
                   String target = (String)attributes.getValue("target");
                   String field = (String)attributes.getValue("field");
                   String oldValue = (String)attributes.getValue("oldValue");
                   String newValue = (String)attributes.getValue("newValue");
                   String active = (String)attributes.getValue("active");

                   TableItem ti = new TableItem(logsTable, SWT.NONE);
                   ti.setText(0, changeNumber.toString());
                   ti.setText(1, domain);
                   ti.setText(2, field);
                   ti.setText(3, oldValue);
                   ti.setText(4, newValue);
                   ti.setText(5, target);
                   ti.setText(6, active != null && "1".equals(active) ? "Yes" : "");
                   ti.setData(attributes);
               }
           };

           source.search(request, response);

       } catch (Exception e) {
           log.debug(e.getMessage(), e);
           String message = e.toString();
           if (message.length() > 500) {
               message = message.substring(0, 500) + "...";
           }
           MessageDialog.openError(editor.getSite().getShell(), "Refresh Failed", message);
       }
   }

    public Composite createChangesSection(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        Composite leftPanel = toolkit.createComposite(composite);
        leftPanel.setLayout(new GridLayout());
        leftPanel.setLayoutData(new GridData(GridData.FILL_BOTH));

        logsTable = new Table(leftPanel, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        logsTable.setLayoutData(new GridData(GridData.FILL_BOTH));
        logsTable.setHeaderVisible(true);
        logsTable.setLinesVisible(true);

        TableColumn tc = new TableColumn(logsTable, SWT.NONE);
        tc.setWidth(30);
        tc.setText("#");

        tc = new TableColumn(logsTable, SWT.NONE);
        tc.setWidth(100);
        tc.setText("Domain");

        tc = new TableColumn(logsTable, SWT.NONE);
        tc.setWidth(80);
        tc.setText("Field");

        tc = new TableColumn(logsTable, SWT.NONE);
        tc.setWidth(80);
        tc.setText("Old Value");

        tc = new TableColumn(logsTable, SWT.NONE);
        tc.setWidth(80);
        tc.setText("New Value");

        tc = new TableColumn(logsTable, SWT.NONE);
        tc.setWidth(80);
        tc.setText("Owner");

        tc = new TableColumn(logsTable, SWT.NONE);
        tc.setWidth(50);
        tc.setText("Active");

        logsTable.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {

                if (logsTable.getSelectionCount() == 0) return;

                TableItem ti = logsTable.getSelection()[0];
                Attributes attributes = (Attributes)ti.getData();
                String message = (String)attributes.getValue("message");
                String active = (String)attributes.getValue("active");

                messageText.setText(message == null ? "" : message);

                if ("1".equals(active)) {
                    activateButton.setText("Deactivate");
                } else {
                    activateButton.setText("Activate");
                }
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
        gd.widthHint = 100;
        buttons.setLayoutData(gd);

        Button addButton = new Button(buttons, SWT.PUSH);
        addButton.setText("Add");
        addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                try {
                    if (logsTable.getSelectionCount() == 0) return;

                    TableItem item = logsTable.getSelection()[0];

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

        Button editButton = new Button(buttons, SWT.PUSH);
        editButton.setText("Edit");
        editButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        editButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                try {
                    if (logsTable.getSelectionCount() == 0) return;

                    TableItem item = logsTable.getSelection()[0];

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

        Button removeButton = new Button(buttons, SWT.PUSH);
        removeButton.setText("Remove");
        removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                try {
                    if (logsTable.getSelectionCount() == 0) return;

                    int index = logsTable.getSelectionIndex();
                    
                    TableItem[] items = logsTable.getSelection();
                    for (TableItem ti : items) {
                        Attributes attributes = (Attributes) ti.getData();

                        Object changeNumber = attributes.getValue("changeNumber");
                        source.delete("changeNumber=" + changeNumber);
                        ti.dispose();
                    }

                    logsTable.select(index);

                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                    String message = e.toString();
                    if (message.length() > 500) {
                        message = message.substring(0, 500) + "...";
                    }
                    MessageDialog.openError(editor.getSite().getShell(), "Delete Failed", message);
                }
            }
        });

        new Label(buttons, SWT.NONE);

        Button showFilesButton = new Button(buttons, SWT.PUSH);
        showFilesButton.setText("Show Files");
        showFilesButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        showFilesButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                try {
                    if (logsTable.getSelectionCount() == 0) return;

                    TableItem ti = logsTable.getSelection()[0];
                    Attributes attributes = (Attributes) ti.getData();
                    String type = (String)attributes.getValue("type");
                    String domain = (String)attributes.getValue("domain");
                    String oldValue = (String)attributes.getValue("oldValue");
                    String newValue = (String)attributes.getValue("newValue");

                    NISFilesPage page = (NISFilesPage)editor.setActivePage("FILES");

                    if ("user".equals(type)) {
                        page.actionsCombo.setText("Find files by UID number");
                        
                    } else if ("group".equals(type)) {
                        page.actionsCombo.setText("Find files by GID number");
                    }

                    page.domainsCombo.setText(domain);
                    page.updateHosts();
                    page.hostsList.selectAll();
                    page.parametersText.setText(oldValue);

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

        Button changeFilesButton = new Button(buttons, SWT.PUSH);
        changeFilesButton.setText("Change Files");
        changeFilesButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        changeFilesButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                try {
                    if (logsTable.getSelectionCount() == 0) return;

                    TableItem ti = logsTable.getSelection()[0];
                    Attributes attributes = (Attributes) ti.getData();
                    String type = (String)attributes.getValue("type");
                    String domain = (String)attributes.getValue("domain");
                    String oldValue = (String)attributes.getValue("oldValue");
                    String newValue = (String)attributes.getValue("newValue");

                    NISFilesPage page = (NISFilesPage)editor.setActivePage("FILES");

                    if ("user".equals(type)) {
                        page.actionsCombo.setText("Change file UID number");

                    } else if ("group".equals(type)) {
                        page.actionsCombo.setText("Change file GID number");
                    }

                    page.domainsCombo.setText(domain);
                    page.updateHosts();
                    page.hostsList.selectAll();
                    page.parametersText.setText(oldValue+" "+newValue);

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

        Button revertFilesButton = new Button(buttons, SWT.PUSH);
        revertFilesButton.setText("Revert Files");
        revertFilesButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        revertFilesButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                try {
                    if (logsTable.getSelectionCount() == 0) return;

                    TableItem ti = logsTable.getSelection()[0];
                    Attributes attributes = (Attributes) ti.getData();
                    String type = (String)attributes.getValue("type");
                    String domain = (String)attributes.getValue("domain");
                    String oldValue = (String)attributes.getValue("oldValue");
                    String newValue = (String)attributes.getValue("newValue");

                    NISFilesPage page = (NISFilesPage)editor.setActivePage("FILES");

                    if ("user".equals(type)) {
                        page.actionsCombo.setText("Change file UID number");

                    } else if ("group".equals(type)) {
                        page.actionsCombo.setText("Change file GID number");
                    }

                    page.domainsCombo.setText(domain);
                    page.updateHosts();
                    page.hostsList.selectAll();
                    page.parametersText.setText(newValue+" "+oldValue);

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

        activateButton = new Button(buttons, SWT.PUSH);
        activateButton.setText("Activate");
        activateButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        activateButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
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
