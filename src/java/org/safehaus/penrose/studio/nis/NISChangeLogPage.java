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
import org.safehaus.penrose.partition.PartitionManager;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.source.SourceManager;
import org.safehaus.penrose.source.Source;
import org.safehaus.penrose.ldap.SearchRequest;
import org.safehaus.penrose.ldap.SearchResult;
import org.safehaus.penrose.ldap.SearchResponse;
import org.safehaus.penrose.ldap.Attributes;

/**
 * @author Endi S. Dewata
 */
public class NISChangeLogPage extends FormPage {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    NISEditor editor;

    Table logsTable;

    public NISChangeLogPage(NISEditor editor) {
        super(editor, "CHANGELOG", "  Change Log ");

        this.editor = editor;
    }

    public void createFormContent(IManagedForm managedForm) {
        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText("Change Log");

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Change Log");
        section.setLayoutData(new GridData(GridData.FILL_BOTH));

        Control sourcesSection = createChangeLogSection(section);
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

           PenroseApplication penroseApplication = PenroseApplication.getInstance();
           PenroseContext penroseContext = penroseApplication.getPenroseContext();
           PartitionManager partitionManager = penroseContext.getPartitionManager();
           SourceManager sourceManager = penroseContext.getSourceManager();

           Partition partition = partitionManager.getPartition("DEFAULT");
           Source source = sourceManager.getSource(partition, "changelog");

           SearchRequest request = new SearchRequest();
           SearchResponse<SearchResult> response = new SearchResponse<SearchResult>() {
               public void add(SearchResult result) throws Exception {
                   Attributes attributes = result.getAttributes();
                   
                   Object changeNumber = attributes.getValue("changeNumber");
                   String domain = (String)attributes.getValue("domain");
                   String type = (String)attributes.getValue("type");
                   String target = (String)attributes.getValue("target");
                   String changes = (String)attributes.getValue("changes");
                   String message = (String)attributes.getValue("message");

                   TableItem ti = new TableItem(logsTable, SWT.NONE);
                   ti.setText(0, changeNumber.toString());
                   ti.setText(1, domain);
                   ti.setText(2, type);
                   ti.setText(3, target);
                   ti.setText(4, changes);
                   ti.setText(5, message);
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

    public Composite createChangeLogSection(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout());

        Button refreshButton = new Button(composite, SWT.PUSH);
        refreshButton.setText("Refresh");
        refreshButton.setLayoutData(new GridData());

        refreshButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                refresh();
            }
        });

        logsTable = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION);
        logsTable.setLayoutData(new GridData(GridData.FILL_BOTH));
        logsTable.setHeaderVisible(true);
        logsTable.setLinesVisible(true);

        TableColumn tc = new TableColumn(logsTable, SWT.NONE);
        tc.setWidth(50);
        tc.setText("#");

        tc = new TableColumn(logsTable, SWT.NONE);
        tc.setWidth(100);
        tc.setText("Domain");

        tc = new TableColumn(logsTable, SWT.NONE);
        tc.setWidth(80);
        tc.setText("Type");

        tc = new TableColumn(logsTable, SWT.NONE);
        tc.setWidth(100);
        tc.setText("Target");

        tc = new TableColumn(logsTable, SWT.NONE);
        tc.setWidth(200);
        tc.setText("Changes");

        tc = new TableColumn(logsTable, SWT.NONE);
        tc.setWidth(150);
        tc.setText("Message");

        return composite;
    }

}
