package org.safehaus.penrose.studio.federation.nis.yp;

import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.ui.PlatformUI;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.core.runtime.IProgressMonitor;
import org.apache.log4j.Logger;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.federation.nis.NISFederation;
import org.safehaus.penrose.studio.federation.nis.NISDomain;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.management.*;
import org.safehaus.penrose.ldap.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Endi S. Dewata
 */
public class NISYPPage extends FormPage {

    public DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    Project project;
    NISYPEditor editor;
    NISFederation nisFederation;
    NISDomain domain;

    Table table;

    PartitionClient partitionClient;

    public NISYPPage(NISYPEditor editor) throws Exception {
        super(editor, "Content", "  Content  ");

        this.editor = editor;
        this.nisFederation = editor.getNisFederation();
        this.project = this.nisFederation.getProject();
        this.domain = editor.getDomain();

        PenroseClient penroseClient = project.getClient();
        partitionClient = penroseClient.getPartitionClient(domain.getName()+"_"+NISFederation.YP);
    }

    public void createFormContent(IManagedForm managedForm) {
        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText("NIS YP Server");

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section baseSection = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        baseSection.setText("Base");
        baseSection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Control baseControl = createBaseSection(baseSection);
        baseSection.setClient(baseControl);

        Section contentSection = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        contentSection.setText("Content");
        contentSection.setLayoutData(new GridData(GridData.FILL_BOTH));

        Control contentControl = createContentControl(contentSection);
        contentSection.setClient(contentControl);

        refresh();
    }

    public Composite createBaseSection(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        Composite leftPanel = toolkit.createComposite(composite);
        leftPanel.setLayout(new GridLayout(2, false));
        leftPanel.setLayoutData(new GridData(GridData.FILL_BOTH));

        Label suffixLabel = toolkit.createLabel(leftPanel, "Base DN:");
        GridData gd = new GridData();
        gd.widthHint = 80;
        suffixLabel.setLayoutData(gd);

        Label suffixText = toolkit.createLabel(leftPanel, domain.getYpSuffix());
        suffixText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Composite rightPanel = toolkit.createComposite(composite);
        rightPanel.setLayout(new GridLayout());
        gd = new GridData(GridData.FILL_VERTICAL);
        gd.verticalSpan = 2;
        gd.widthHint = 120;
        rightPanel.setLayoutData(gd);

        Button createBaseButton = new Button(rightPanel, SWT.PUSH);
        createBaseButton.setText("Create Base");
        createBaseButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        createBaseButton.setEnabled(false);

        Button removeBaseButton = new Button(rightPanel, SWT.PUSH);
        removeBaseButton.setText("Remove Base");
        removeBaseButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        removeBaseButton.setEnabled(false);

        return composite;
    }

    public Composite createContentControl(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        Composite leftPanel = toolkit.createComposite(composite);
        leftPanel.setLayout(new GridLayout());
        leftPanel.setLayoutData(new GridData(GridData.FILL_BOTH));

        table = new Table(leftPanel, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        table.setLayoutData(new GridData(GridData.FILL_BOTH));
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        TableColumn tc = new TableColumn(table, SWT.NONE);
        tc.setWidth(150);
        tc.setText("Name");

        tc = new TableColumn(table, SWT.NONE);
        tc.setWidth(100);
        tc.setText("Entries");
        tc.setAlignment(SWT.RIGHT);

        Composite links = toolkit.createComposite(leftPanel);
        links.setLayout(new RowLayout());
        links.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Hyperlink selectAllLink = toolkit.createHyperlink(links, "Select All", SWT.NONE);

        selectAllLink.addHyperlinkListener(new HyperlinkAdapter() {
            public void linkActivated(HyperlinkEvent event) {
                table.selectAll();
            }
        });

        Hyperlink selectNoneLink = toolkit.createHyperlink(links, "Select None", SWT.NONE);

        selectNoneLink.addHyperlinkListener(new HyperlinkAdapter() {
            public void linkActivated(HyperlinkEvent event) {
                table.deselectAll();
            }
        });

        Composite rightPanel = toolkit.createComposite(composite);
        rightPanel.setLayout(new GridLayout());
        GridData gd = new GridData(GridData.FILL_VERTICAL);
        gd.verticalSpan = 2;
        gd.widthHint = 120;
        rightPanel.setLayoutData(gd);

        Button refreshButton = new Button(rightPanel, SWT.PUSH);
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
            int[] indices = table.getSelectionIndices();
            TableItem[] items = table.getSelection();

            final Collection<String> mapNames = new ArrayList<String>();

            if (items.length == 0) {
                for (String mapName : nisFederation.getMapNames()) {
                    mapNames.add(mapName);
                }

                table.removeAll();

            } else {
                for (TableItem ti : items) {
                    String mapName = (String)ti.getData("mapName");
                    mapNames.add(mapName);
                }
            }

            final Map<String,String> statuses = new TreeMap<String,String>();

            IProgressService progressService = PlatformUI.getWorkbench().getProgressService();

            progressService.busyCursorWhile(new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor) throws InvocationTargetException {
                    try {
                        monitor.beginTask("Refreshing...", nisFederation.getMapNames().size());

                        for (String mapName : mapNames) {

                            if (monitor.isCanceled()) throw new InterruptedException();

                            monitor.subTask("Checking "+mapName+"...");

                            String status = getStatus(mapName);
                            statuses.put(mapName, status);

                            monitor.worked(1);
                        }

                    } catch (InterruptedException e) {
                        // ignore

                    } catch (Exception e) {
                        throw new InvocationTargetException(e);

                    } finally {
                        monitor.done();
                    }
                }
            });

            if (items.length == 0) {
                for (String mapName : mapNames) {

                    String status = statuses.get(mapName);

                    TableItem ti = new TableItem(table, SWT.NONE);
                    ti.setText(0, mapName);
                    ti.setText(1, status);

                    ti.setData("mapName", mapName);
                }

            } else {
                for (TableItem ti : items) {
                    String mapName = (String)ti.getData("mapName");

                    String status = statuses.get(mapName);
                    ti.setText(1, status);
                }
            }

            table.select(indices);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }

    public DN getDn(String mapName) throws Exception {
        RDNBuilder rb = new RDNBuilder();
        rb.set("ou", mapName);
        RDN rdn = rb.toRdn();

        DNBuilder db = new DNBuilder();
        db.append(rdn);

        DN suffix = partitionClient.getSuffixes().iterator().next();
        db.append(suffix);

        return db.toDn();
    }

    public String getStatus(String mapName) {
        try {
            SearchRequest request = new SearchRequest();
            request.setDn(getDn(mapName));
            request.setScope(SearchRequest.SCOPE_ONE);
            request.setAttributes(new String[] { "dn" });
            request.setTypesOnly(true);

            SearchResponse response = new SearchResponse();

            partitionClient.search(request, response);

            if (response.getReturnCode() != LDAP.SUCCESS) {
                return "N/A";
            }

            return ""+response.getTotalCount();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }
}
