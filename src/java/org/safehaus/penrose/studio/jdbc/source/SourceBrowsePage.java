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
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.source.editor.SourceEditorPage;
import org.safehaus.penrose.studio.source.editor.SourceEditor;
import org.safehaus.penrose.adapter.AdapterConfig;
import org.safehaus.penrose.connection.Connection;
import org.safehaus.penrose.connection.ConnectionConfig;
import org.safehaus.penrose.config.PenroseConfig;
import org.safehaus.penrose.session.PenroseSearchControls;
import org.safehaus.penrose.session.PenroseSearchResults;
import org.safehaus.penrose.mapping.AttributeValues;
import org.safehaus.penrose.source.FieldConfig;
import org.safehaus.penrose.source.SourceConfig;

import java.util.Iterator;
import java.util.Collection;

public class SourceBrowsePage extends SourceEditorPage {

    Button refreshButton;
    Text maxSizeText;

    Table table;

    public SourceBrowsePage(SourceEditor editor) {
        super(editor, "BROWSE", "  Browse  ");
    }

    public void createFormContent(IManagedForm managedForm) {
        super.createFormContent(managedForm);

        ScrolledForm form = managedForm.getForm();
        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section section = getToolkit().createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Rows");
        section.setLayoutData(new GridData(GridData.FILL_BOTH));

        Control fieldsSection = createFieldsSection(section);
        section.setClient(fieldsSection);
    }

    public Composite createFieldsSection(Composite parent) {

        Composite composite = getToolkit().createComposite(parent);
        composite.setLayout(new GridLayout());

        Composite buttons = getToolkit().createComposite(composite);
        buttons.setLayout(new GridLayout(3, false));
        buttons.setLayoutData(new GridData());

        Label label = getToolkit().createLabel(buttons, "Max:");
        label.setLayoutData(new GridData());

        maxSizeText = getToolkit().createText(buttons, "100", SWT.BORDER);
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

        table = getToolkit().createTable(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        table.setLayoutData(new GridData(GridData.FILL_BOTH));

        Collection fields = getSourceConfig().getFieldConfigs();
        for (Iterator i=fields.iterator(); i.hasNext(); ) {
            FieldConfig fieldDefinition = (FieldConfig)i.next();

            TableColumn tc = new TableColumn(table, SWT.NONE);
            tc.setText(fieldDefinition.getName());
            tc.setWidth(100);
        }

        return composite;
    }

    public void refresh() {
        table.removeAll();

        try {
            SourceConfig sourceConfig = getSourceConfig();
            SourceEditor editor = (SourceEditor)getEditor();
            Server server = editor.getServer();

            PenroseConfig penroseConfig = server.getPenroseConfig();
            PartitionManager partitionManager = server.getPartitionManager();
            Partition partition = partitionManager.getPartition(sourceConfig);
            ConnectionConfig connectionConfig = partition.getConnectionConfig(sourceConfig.getConnectionName());

            AdapterConfig adapterConfig = penroseConfig.getAdapterConfig(connectionConfig.getAdapterName());

            Connection connection = new Connection(connectionConfig, adapterConfig);
            connection.start();

            PenroseSearchResults sr = new PenroseSearchResults();
            PenroseSearchControls sc = new PenroseSearchControls();

            int size = Integer.parseInt(maxSizeText.getText());
            sc.setSizeLimit(size);

            connection.load(sourceConfig, null, null, sc, sr);

            sr.close();

            Collection fields = sourceConfig.getFieldConfigs();

            //log.debug("Results:");
            while (sr.hasNext()) {
                AttributeValues av = (AttributeValues)sr.next();
                //log.debug(" - "+av);

                TableItem item = new TableItem(table, SWT.NONE);
                int counter = 0;
                for (Iterator i=fields.iterator(); i.hasNext(); counter++) {
                    FieldConfig fieldDefinition = (FieldConfig)i.next();
                    Collection values = av.get(fieldDefinition.getName());

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

            connection.stop();

        } catch (Exception e) {
            log.debug(e.getMessage(), e);
            String message = e.toString();
            if (message.length() > 500) {
                message = message.substring(0, 500) + "...";
            }
            MessageDialog.openError(getEditor().getSite().getShell(), "Browse Failed", message);
        }
    }
}
