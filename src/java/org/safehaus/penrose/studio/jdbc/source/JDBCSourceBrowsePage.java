package org.safehaus.penrose.studio.jdbc.source;

import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.safehaus.penrose.partition.*;
import org.safehaus.penrose.ldap.*;
import org.safehaus.penrose.source.Source;
import org.safehaus.penrose.source.FieldConfig;
import org.safehaus.penrose.ldap.LDAP;
import org.safehaus.penrose.connection.Connection;
import org.safehaus.penrose.config.PenroseConfig;
import org.safehaus.penrose.naming.PenroseContext;
import org.safehaus.penrose.studio.source.editor.SourceEditorPage;
import org.safehaus.penrose.studio.dialog.ErrorDialog;

import java.util.Iterator;
import java.util.Collection;

public class JDBCSourceBrowsePage extends SourceEditorPage {

    Text maxSizeText;

    Table table;

    public JDBCSourceBrowsePage(JDBCSourceEditor editor) {
        super(editor, "BROWSE", "  Browse  ");
    }

    public void createFormContent(IManagedForm managedForm) {
        super.createFormContent(managedForm);

        ScrolledForm form = managedForm.getForm();

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

        Collection<FieldConfig> fields = sourceConfig.getFieldConfigs();
        for (FieldConfig fieldConfig : fields) {
            TableColumn tc = new TableColumn(table, SWT.NONE);
            tc.setText(fieldConfig.getName());
            tc.setWidth(100);
        }

        table.addMouseListener(new MouseAdapter() {
            public void mouseDoubleClick(MouseEvent mouseEvent) {
                edit(parent.getShell());
            }
        });

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
                add(parent.getShell());
            }
        });

        Button editButton = new Button(buttons, SWT.PUSH);
        editButton.setText("Edit");
        gd = new GridData();
        gd.widthHint = 80;
        editButton.setLayoutData(gd);

        editButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                edit(parent.getShell());
            }
        });

        Button deleteButton = new Button(buttons, SWT.PUSH);
        deleteButton.setText("Delete");
        gd = new GridData();
        gd.widthHint = 80;
        deleteButton.setLayoutData(gd);

        deleteButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                delete(parent.getShell());
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

    public void add(final Shell parent) {
        try {
            JDBCSearchResultDialog dialog = new JDBCSearchResultDialog(parent.getShell(), SWT.NONE);
            dialog.setPartitionConfig(partitionConfig);
            dialog.setSourceConfig(sourceConfig);
            dialog.open();

            if (dialog.getAction() == JDBCSearchResultDialog.CANCEL) return;

            RDN rdn = dialog.getRdn();
            DN dn = new DN(rdn);
            Attributes attributes = dialog.getAttributes();

            PenroseConfig penroseConfig = project.getPenroseConfig();
            PenroseContext penroseContext = project.getPenroseContext();

            PartitionFactory partitionFactory = new PartitionFactory();
            partitionFactory.setPenroseConfig(penroseConfig);
            partitionFactory.setPenroseContext(penroseContext);

            Partition partition = partitionFactory.createPartition(partitionConfig);

            Connection connection = partition.getConnection(sourceConfig.getConnectionName());

            Source source = connection.createSource(sourceConfig);

            source.add(null, dn, attributes);

            partition.destroy();

            refresh();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }

    public void edit(final Shell parent) {
        try {
            if (table.getSelectionCount() == 0) return;

            TableItem item = table.getSelection()[0];
            SearchResult searchResult = (SearchResult)item.getData();

            DN dn = searchResult.getDn();
            RDN rdn = dn.getRdn();

            JDBCSearchResultDialog dialog = new JDBCSearchResultDialog(parent, SWT.NONE);
            dialog.setPartitionConfig(partitionConfig);
            dialog.setSourceConfig(sourceConfig);
            dialog.setRdn(rdn);
            dialog.setAttributes(searchResult.getAttributes());
            dialog.open();

            if (dialog.getAction() == JDBCSearchResultDialog.CANCEL) return;

            RDN newRdn = dialog.getRdn();

            Collection<Modification> modifications = LDAP.createModifications(
                    searchResult.getAttributes(),
                    dialog.getAttributes()
            );

            PenroseConfig penroseConfig = project.getPenroseConfig();
            PenroseContext penroseContext = project.getPenroseContext();

            PartitionFactory partitionFactory = new PartitionFactory();
            partitionFactory.setPenroseConfig(penroseConfig);
            partitionFactory.setPenroseContext(penroseContext);

            Partition partition = partitionFactory.createPartition(partitionConfig);

            Connection connection = partition.getConnection(sourceConfig.getConnectionName());

            Source source = connection.createSource(sourceConfig);

            if (!rdn.equals(newRdn)) {
                source.modrdn(null, dn, newRdn, true);

                DNBuilder db = new DNBuilder();
                db.append(newRdn);
                db.append(dn.getParentDn());
                dn = db.toDn();
            }

            source.modify(null, dn, modifications);

            partition.destroy();

            refresh();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }

    public void delete(final Shell parent) {
        try {
            if (table.getSelectionCount() == 0) return;

            int index = table.getSelectionIndex();
            TableItem item = table.getSelection()[0];
            SearchResult searchResult = (SearchResult)item.getData();

            DN dn = searchResult.getDn();

            PenroseConfig penroseConfig = project.getPenroseConfig();
            PenroseContext penroseContext = project.getPenroseContext();

            PartitionFactory partitionFactory = new PartitionFactory();
            partitionFactory.setPenroseConfig(penroseConfig);
            partitionFactory.setPenroseContext(penroseContext);

            Partition partition = partitionFactory.createPartition(partitionConfig);

            Connection connection = partition.getConnection(sourceConfig.getConnectionName());

            Source source = connection.createSource(sourceConfig);

            source.delete(null, dn);

            partition.destroy();

            refresh();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }

    public void refresh() {
        final Collection fields = sourceConfig.getFieldConfigs();

        table.removeAll();

        SearchRequest request = new SearchRequest();

        int size = Integer.parseInt(maxSizeText.getText());
        request.setSizeLimit(size);

        SearchResponse response = new SearchResponse() {
            public void add(SearchResult searchResult) throws Exception {
                super.add(searchResult);

                Attributes attributes = searchResult.getAttributes();
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

                item.setData(searchResult);
            }
        };

        try {
            PenroseConfig penroseConfig = project.getPenroseConfig();
            PenroseContext penroseContext = project.getPenroseContext();

            PartitionFactory partitionFactory = new PartitionFactory();
            partitionFactory.setPenroseConfig(penroseConfig);
            partitionFactory.setPenroseContext(penroseContext);

            Partition partition = partitionFactory.createPartition(partitionConfig);

            Connection connection = partition.getConnection(sourceConfig.getConnectionName());

            Source source = connection.createSource(sourceConfig);

            source.search(null, request, response);

            partition.destroy();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }
}
