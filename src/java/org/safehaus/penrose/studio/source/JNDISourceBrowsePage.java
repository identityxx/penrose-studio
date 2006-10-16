package org.safehaus.penrose.studio.source;

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
import org.safehaus.penrose.partition.*;
import org.safehaus.penrose.studio.PenroseApplication;
import org.safehaus.penrose.config.PenroseConfig;
import org.safehaus.penrose.connector.AdapterConfig;
import org.safehaus.penrose.connector.Connection;
import org.safehaus.penrose.session.PenroseSearchResults;
import org.safehaus.penrose.session.PenroseSearchControls;
import org.safehaus.penrose.mapping.AttributeValues;

import java.util.Collection;
import java.util.Iterator;

public class JNDISourceBrowsePage extends FormPage {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    Button refreshButton;
    Text maxSizeText;

    Table table;

    JNDISourceEditor editor;
    Partition partition;
    SourceConfig sourceConfig;

    public JNDISourceBrowsePage(JNDISourceEditor editor) {
        super(editor, "BROWSE", "  Browse  ");

        this.editor = editor;
        this.partition = editor.partition;
        this.sourceConfig = editor.sourceConfig;
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

        Collection fields = sourceConfig.getFieldConfigs();
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
            PenroseApplication penroseApplication = PenroseApplication.getInstance();
            PenroseConfig penroseConfig = penroseApplication.getPenroseConfig();

            PartitionManager partitionManager = penroseApplication.getPartitionManager();
            Partition partition = partitionManager.getPartition(sourceConfig);
            ConnectionConfig connectionConfig = partition.getConnectionConfig(sourceConfig.getConnectionName());

            AdapterConfig adapterConfig = penroseConfig.getAdapterConfig(connectionConfig.getAdapterName());

            Connection connection = new Connection(connectionConfig, adapterConfig);
            connection.init();

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

            connection.close();

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
