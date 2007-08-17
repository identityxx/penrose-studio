package org.safehaus.penrose.studio.jndi.source;

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
import org.safehaus.penrose.partition.*;
import org.safehaus.penrose.connection.Connection;
import org.safehaus.penrose.connection.ConnectionConfig;
import org.safehaus.penrose.ldap.*;
import org.safehaus.penrose.source.Source;
import org.safehaus.penrose.source.FieldConfig;
import org.safehaus.penrose.config.PenroseConfig;
import org.safehaus.penrose.naming.PenroseContext;
import org.safehaus.penrose.studio.source.editor.SourceEditorPage;

import java.util.Collection;
import java.util.Iterator;

public class JNDISourceBrowsePage extends SourceEditorPage {

    Button refreshButton;
    Text maxSizeText;

    Table table;

    public JNDISourceBrowsePage(JNDISourceEditor editor) throws Exception {
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

    public Composite createFieldsSection(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout());

        Composite buttons = toolkit.createComposite(composite);
        buttons.setLayout(new GridLayout(3, false));
        buttons.setLayoutData(new GridData());

        Label label = toolkit.createLabel(buttons, "Max:");
        label.setLayoutData(new GridData());

        maxSizeText = toolkit.createText(buttons, "100", SWT.BORDER);
        GridData gd = new GridData();
        gd.widthHint = 50;
        maxSizeText.setLayoutData(gd);

        refreshButton = new Button(buttons, SWT.PUSH);
        refreshButton.setText("Refresh");
        gd = new GridData();
        gd.horizontalIndent = 5;
        gd.widthHint = 80;
        refreshButton.setLayoutData(gd);

        refreshButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                refresh();
            }
        });

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

        return composite;
    }

    public void refresh() {

        final Collection fields = sourceConfig.getFieldConfigs();

        table.removeAll();

        SearchRequest sc = new SearchRequest();

        int size = Integer.parseInt(maxSizeText.getText());
        sc.setSizeLimit(size);

        SearchResponse<SearchResult> sr = new SearchResponse<SearchResult>() {
            public void add(SearchResult searchResult) throws Exception {
                super.add(searchResult);

                Attributes attributes = searchResult.getAttributes();
                //log.debug(" - "+av);

                TableItem item = new TableItem(table, SWT.NONE);
                int counter = 0;
                for (Iterator i=fields.iterator(); i.hasNext(); counter++) {
                    FieldConfig fieldDefinition = (FieldConfig)i.next();

                    Attribute attribute = attributes.get(fieldDefinition.getName());
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
            }
        };

        try {
            PenroseConfig penroseConfig = project.getPenroseConfig();
            PenroseContext penroseContext = project.getPenroseContext();

            PartitionContext partitionContext = new PartitionContext();
            partitionContext.setPenroseConfig(penroseConfig);
            partitionContext.setPenroseContext(penroseContext);

            Partition partition = new Partition();
            partition.init(partitionConfig, partitionContext);

            Connection connection = partition.getConnection(sourceConfig.getConnectionName());

            Source source = partition.createSource(sourceConfig, connection);

            source.search(sc, sr);

            partition.stop();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
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
