package org.safehaus.penrose.studio.source.editor;

import org.apache.log4j.Logger;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.safehaus.penrose.partition.*;
import org.safehaus.penrose.studio.PenroseApplication;
import org.safehaus.penrose.ldap.*;
import org.safehaus.penrose.naming.PenroseContext;
import org.safehaus.penrose.source.SourceManager;
import org.safehaus.penrose.source.Source;
import org.safehaus.penrose.util.LDAPUtil;

import java.util.Iterator;
import java.util.Collection;
import java.util.ArrayList;

public class JDBCSourceBrowsePage extends FormPage {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    Text maxSizeText;

    Table table;

    JDBCSourceEditor editor;
    Partition partition;
    SourceConfig sourceConfig;
    Source source;

    public JDBCSourceBrowsePage(JDBCSourceEditor editor) {
        super(editor, "BROWSE", "  Browse  ");

        this.editor = editor;
        this.partition = editor.partition;
        this.sourceConfig = editor.sourceConfig;

        PenroseApplication penroseApplication = PenroseApplication.getInstance();
        PenroseContext penroseContext = penroseApplication.getPenroseContext();
        SourceManager sourceManager = penroseContext.getSourceManager();
        source = sourceManager.getSource(partition, sourceConfig.getName());
    }

    public void createFormContent(IManagedForm managedForm) {
        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText("Source Editor");

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Rows");
        section.setLayoutData(new GridData(GridData.FILL_BOTH));

        Control fieldsSection = createFieldsSection(section);
        section.setClient(fieldsSection);

        refresh();
    }

    public Composite createFieldsSection(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        Composite options = toolkit.createComposite(composite);
        options.setLayout(new GridLayout(2, false));
        GridData gd = new GridData();
        gd.horizontalSpan = 2;
        options.setLayoutData(gd);

        Label label = toolkit.createLabel(options, "Max:");
        label.setLayoutData(new GridData());

        maxSizeText = toolkit.createText(options, "100", SWT.BORDER);
        gd = new GridData();
        gd.widthHint = 50;
        maxSizeText.setLayoutData(gd);

        table = toolkit.createTable(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        table.setLayoutData(new GridData(GridData.FILL_BOTH));

        Collection fields = sourceConfig.getFieldConfigs();
        for (Iterator i=fields.iterator(); i.hasNext(); ) {
            FieldConfig fieldDefinition = (FieldConfig)i.next();

            TableColumn tc = new TableColumn(table, SWT.NONE);
            tc.setText(fieldDefinition.getName());
            tc.setWidth(100);
        }

        Composite buttons = toolkit.createComposite(composite);
        buttons.setLayout(new GridLayout());
        buttons.setLayoutData(new GridData(GridData.FILL_VERTICAL));

        Button addButton = new Button(buttons, SWT.PUSH);
        addButton.setText("Add");
        gd = new GridData();
        gd.widthHint = 80;
        addButton.setLayoutData(gd);

        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    SearchResultDialog dialog = new SearchResultDialog(parent.getShell(), SWT.NONE);
                    dialog.setSourceConfig(sourceConfig);
                    dialog.open();

                    if (dialog.getAction() == SearchResultDialog.CANCEL) return;

                    RDN rdn = dialog.getRdn();
                    DN dn = new DN(rdn);
                    Attributes attributes = dialog.getAttributes();

                    source.add(dn, attributes);

                    refresh();

                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                    String message = e.toString();
                    if (message.length() > 500) {
                        message = message.substring(0, 500) + "...";
                    }
                    MessageDialog.openError(editor.getSite().getShell(), "Browse Failed", message);
                }
            }
        });

        Button editButton = new Button(buttons, SWT.PUSH);
        editButton.setText("Edit");
        gd = new GridData();
        gd.widthHint = 80;
        editButton.setLayoutData(gd);

        editButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    if (table.getSelectionCount() == 0) return;

                    int index = table.getSelectionIndex();
                    TableItem item = table.getSelection()[0];
                    SearchResult searchResult = (SearchResult)item.getData();

                    SearchResultDialog dialog = new SearchResultDialog(parent.getShell(), SWT.NONE);
                    dialog.setSourceConfig(sourceConfig);
                    dialog.setRdn(searchResult.getDn().getRdn());
                    dialog.setAttributes(searchResult.getAttributes());
                    dialog.open();

                    if (dialog.getAction() == SearchResultDialog.CANCEL) return;

                    DN dn = searchResult.getDn();
                    Collection<Modification> modifications = LDAPUtil.createModifications(
                            searchResult.getAttributes(),
                            dialog.getAttributes()
                    );

                    source.modify(dn, modifications);

                    refresh();

                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                    String message = e.toString();
                    if (message.length() > 500) {
                        message = message.substring(0, 500) + "...";
                    }
                    MessageDialog.openError(editor.getSite().getShell(), "Browse Failed", message);
                }
            }
        });

        Button deleteButton = new Button(buttons, SWT.PUSH);
        deleteButton.setText("Delete");
        gd = new GridData();
        gd.widthHint = 80;
        deleteButton.setLayoutData(gd);

        deleteButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    if (table.getSelectionCount() == 0) return;

                    int index = table.getSelectionIndex();
                    TableItem item = table.getSelection()[0];
                    SearchResult searchResult = (SearchResult)item.getData();

                    DN dn = searchResult.getDn();
                    source.delete(dn);

                    refresh();

                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                    String message = e.toString();
                    if (message.length() > 500) {
                        message = message.substring(0, 500) + "...";
                    }
                    MessageDialog.openError(editor.getSite().getShell(), "Browse Failed", message);
                }
            }
        });

        new Label(buttons, SWT.NONE);
        
        Button refreshButton = new Button(buttons, SWT.PUSH);
        refreshButton.setText("Refresh");
        gd = new GridData();
        gd.widthHint = 80;
        refreshButton.setLayoutData(gd);

        refreshButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                refresh();
            }
        });

        return composite;
    }

    public void refresh() {
        table.removeAll();

        try {
            SearchResponse<SearchResult> sr = new SearchResponse<SearchResult>();
            SearchRequest sc = new SearchRequest();

            int size = Integer.parseInt(maxSizeText.getText());
            sc.setSizeLimit(size);

            source.search(sc, sr);

            Collection fields = sourceConfig.getFieldConfigs();

            //log.debug("Results:");
            while (sr.hasNext()) {
                SearchResult entry = (SearchResult)sr.next();
                Attributes attributes = entry.getAttributes();
                //log.debug(" - "+av);

                TableItem item = new TableItem(table, SWT.NONE);
                int counter = 0;
                for (Iterator i=fields.iterator(); i.hasNext(); counter++) {
                    FieldConfig fieldConfig = (FieldConfig)i.next();

                    Attribute attribute = attributes.get(fieldConfig.getName());
                    if (attribute == null) continue;

                    Collection values = attribute.getValues();

                    String value;
                    if (values == null) {
                        value = null;
                    } else if (values.size() > 1) {
                        value = values.toString();
                    } else {
                        value = values.iterator().next().toString();
                    }

                    item.setText(counter, value == null ? "" : value);
                }
                item.setData(entry);
            }

        } catch (Exception e) {
            log.debug(e.getMessage(), e);
            String message = e.toString();
            if (message.length() > 500) {
                message = message.substring(0, 500) + "...";
            }
            MessageDialog.openError(editor.getSite().getShell(), "Browse Failed", message);
        }
    }

    public void checkDirty() {
        editor.checkDirty();
    }
}
