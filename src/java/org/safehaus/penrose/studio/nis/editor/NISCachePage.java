package org.safehaus.penrose.studio.nis.editor;

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
import org.safehaus.penrose.nis.NISDomain;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.source.Source;
import org.safehaus.penrose.studio.nis.NISTool;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.management.PenroseClient;
import org.safehaus.penrose.management.PartitionClient;
import org.safehaus.penrose.management.SourceClient;

import java.util.*;

/**
 * @author Endi S. Dewata
 */
public class NISCachePage extends FormPage {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    Table cacheTable;

    NISDomainEditor editor;
    NISDomain domain;
    NISTool nisTool;

    Project project;

    Map<String,String> sourceNames = new TreeMap<String,String>();

    public NISCachePage(NISDomainEditor editor) {
        super(editor, "CACHE", "  Cache  ");

        this.editor = editor;

        domain = editor.getDomain();
        nisTool = editor.getNisTool();
        project = nisTool.getProject();

        sourceNames.put("Users", "nis_users");
        sourceNames.put("Shadows", "nis_shadow");
        sourceNames.put("Hosts", "nis_hosts");
        sourceNames.put("Groups", "nis_groups");
        sourceNames.put("Services", "nis_services");
        sourceNames.put("RPCs", "nis_rpcs");
        sourceNames.put("NetIDs", "nis_netids");
        sourceNames.put("Protocols", "nis_protocols");
        sourceNames.put("Aliases", "nis_aliases");
        sourceNames.put("Netgroups", "nis_netgroups");
        sourceNames.put("Ethernets", "nis_ethers");
        sourceNames.put("BootParams", "nis_bootparams");
        sourceNames.put("Networks", "nis_networks");
        sourceNames.put("Automounts", "nis_automounts");
    }

    public void createFormContent(IManagedForm managedForm) {
        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText("Cache");

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Cache");
        section.setLayoutData(new GridData(GridData.FILL_BOTH));

        Control mainSection = createMainSection(section);
        section.setClient(mainSection);
    }

    public void setActive(boolean b) {
        super.setActive(b);
        refresh();
    }

    public Composite createMainSection(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        Composite leftPanel = toolkit.createComposite(composite);
        leftPanel.setLayout(new GridLayout());
        leftPanel.setLayoutData(new GridData(GridData.FILL_BOTH));

        cacheTable = new Table(leftPanel, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        cacheTable.setLayoutData(new GridData(GridData.FILL_BOTH));
        cacheTable.setHeaderVisible(true);
        cacheTable.setLinesVisible(true);

        TableColumn tc = new TableColumn(cacheTable, SWT.NONE);
        tc.setWidth(150);
        tc.setText("Name");

        tc = new TableColumn(cacheTable, SWT.NONE);
        tc.setWidth(100);
        tc.setText("Entries");
        tc.setAlignment(SWT.RIGHT);

        Composite buttons = toolkit.createComposite(composite);
        buttons.setLayout(new GridLayout());
        GridData gd = new GridData(GridData.FILL_VERTICAL);
        gd.widthHint = 120;
        buttons.setLayoutData(gd);

        Button createButton = new Button(buttons, SWT.PUSH);
        createButton.setText("Create");
        createButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        createButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                try {
                    TableItem[] items = cacheTable.getSelection();
                    if (items == null || items.length == 0) {
                        items = cacheTable.getItems();
                    }

                    PenroseClient penroseClient = project.getClient();
                    PartitionClient partitionClient = penroseClient.getPartitionClient(domain.getName());

                    for (TableItem item : items) {
                        Source source = (Source)item.getData();
                        String name = source.getName();

                        SourceClient sourceClient = partitionClient.getSource(name);
                        sourceClient.createCache();
                    }

                    refresh();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    MessageDialog.openError(editor.getSite().getShell(), "Action Failed", e.getMessage());
                }
            }
        });

        Button loadButton = new Button(buttons, SWT.PUSH);
        loadButton.setText("Load");
        loadButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        loadButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                try {
                    TableItem[] items = cacheTable.getSelection();
                    if (items == null || items.length == 0) {
                        items = cacheTable.getItems();
                    }

                    PenroseClient penroseClient = project.getClient();
                    PartitionClient partitionClient = penroseClient.getPartitionClient(domain.getName());

                    for (TableItem item : items) {
                        Source source = (Source)item.getData();
                        String name = source.getName();

                        SourceClient sourceClient = partitionClient.getSource(name);
                        sourceClient.loadCache();
                    }

                    refresh();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    MessageDialog.openError(editor.getSite().getShell(), "Action Failed", e.getMessage());
                }
            }
        });

        Button clearButton = new Button(buttons, SWT.PUSH);
        clearButton.setText("Clear");
        clearButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        clearButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                try {
                    TableItem[] items = cacheTable.getSelection();
                    if (items == null || items.length == 0) {
                        items = cacheTable.getItems();
                    }

                    PenroseClient penroseClient = project.getClient();
                    PartitionClient partitionClient = penroseClient.getPartitionClient(domain.getName());

                    for (TableItem item : items) {
                        Source source = (Source)item.getData();
                        String name = source.getName();

                        SourceClient sourceClient = partitionClient.getSource(name);
                        sourceClient.cleanCache();
                    }

                    refresh();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    MessageDialog.openError(editor.getSite().getShell(), "Action Failed", e.getMessage());
                }
            }
        });

        Button removeButton = new Button(buttons, SWT.PUSH);
        removeButton.setText("Remove");
        removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                try {
                    TableItem[] items = cacheTable.getSelection();
                    if (items == null || items.length == 0) {
                        items = cacheTable.getItems();
                    }

                    PenroseClient penroseClient = project.getClient();
                    PartitionClient partitionClient = penroseClient.getPartitionClient(domain.getName());

                    for (TableItem item : items) {
                        Source source = (Source)item.getData();
                        String name = source.getName();

                        SourceClient sourceClient = partitionClient.getSource(name);
                        sourceClient.dropCache();
                    }

                    refresh();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    MessageDialog.openError(editor.getSite().getShell(), "Action Failed", e.getMessage());
                }
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

    public void refresh() {
        try {
            cacheTable.removeAll();

            Partition partition = nisTool.getPartitions().getPartition(domain.getName());

            for (final String label : sourceNames.keySet()) {
                final String sourceName = sourceNames.get(label);
                log.debug("Checking cache for "+label+" ("+sourceName+").");

                Source source = partition.getSource(sourceName);
                if (source == null) continue;

                Collection<Source> caches = nisTool.getCaches(partition, source);
                if (caches.isEmpty()) continue;

                Source cacheSource = caches.iterator().next();

                TableItem ti = new TableItem(cacheTable, SWT.NONE);
                ti.setText(0, label);
                ti.setData(source);

                try {
                    Long count = cacheSource.getCount();
                    ti.setText(1, count.toString());

                } catch (Exception e) {
                    ti.setText(1, "N/A");
                }
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            MessageDialog.openError(editor.getSite().getShell(), "Action Failed", e.getMessage());
        }
    }

}
