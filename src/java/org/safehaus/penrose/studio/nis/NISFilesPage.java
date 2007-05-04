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
import org.safehaus.penrose.ldap.*;
import org.safehaus.penrose.nis.agent.NISAgentClient;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;

/**
 * @author Endi S. Dewata
 */
public class NISFilesPage extends FormPage {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    Combo hostsCombo;
    Combo actionsCombo;
    Text parametersText;

    Label messageLabel;
    Table table;

    NISEditor editor;

    Map<String,String> actions = new LinkedHashMap<String,String>();

    public NISFilesPage(NISEditor editor) {
        super(editor, "FILES", "  Files  ");

        this.editor = editor;

        actions.put("Find files by UID number", "findByUid");
        actions.put("Find files by GID number", "findByGid");
    }

    public void createFormContent(IManagedForm managedForm) {
        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText("NIS Files");

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Action");
        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Control sourcesSection = createActionSection(section);
        section.setClient(sourcesSection);

        section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Results");
        section.setLayoutData(new GridData(GridData.FILL_BOTH));

        Control resultsSection = createResultsSection(section);
        section.setClient(resultsSection);

        init();
    }

    public void init() {
       try {
           hostsCombo.removeAll();

           PenroseApplication penroseApplication = PenroseApplication.getInstance();
           PenroseContext penroseContext = penroseApplication.getPenroseContext();
           SourceManager sourceManager = penroseContext.getSourceManager();

           Source hosts = sourceManager.getSource("DEFAULT", "hosts");

           SearchRequest request = new SearchRequest();
           SearchResponse<SearchResult> response = new SearchResponse<SearchResult>() {
               public void add(SearchResult result) throws Exception {
                   Attributes attributes = result.getAttributes();
                   String domain = (String)attributes.getValue("domain");
                   String name = (String)attributes.getValue("name");
                   String path = (String)attributes.getValue("path");

                   String host = domain+" "+name+" "+path;
                   hostsCombo.add(host);
                   hostsCombo.setData(host, result);
               }
           };

           hosts.search(request, response);

           hostsCombo.select(0);

       } catch (Exception e) {
           log.debug(e.getMessage(), e);
           String message = e.toString();
           if (message.length() > 500) {
               message = message.substring(0, 500) + "...";
           }
           MessageDialog.openError(editor.getSite().getShell(), "Init Failed", message);
       }
   }

    public Composite createActionSection(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        Label hostLabel = toolkit.createLabel(composite, "Host:");
        GridData gd = new GridData();
        gd.widthHint = 100;
        hostLabel.setLayoutData(gd);

        hostsCombo = new Combo(composite, SWT.READ_ONLY);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        hostsCombo.setLayoutData(gd);

        Label actionLabel = toolkit.createLabel(composite, "Action:");
        actionLabel.setLayoutData(new GridData());

        actionsCombo = new Combo(composite, SWT.READ_ONLY);
        actionsCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        for (String title : actions.keySet()) {
            String value = actions.get(title);
            actionsCombo.add(title);
            actionsCombo.setData(title, value);
        }

        actionsCombo.select(0);
        
        Label parametersLabel = toolkit.createLabel(composite, "Parameters:");
        parametersLabel.setLayoutData(new GridData());

        parametersText = toolkit.createText(composite, "", SWT.BORDER);
        parametersText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Button runButton = new Button(composite, SWT.PUSH);
        runButton.setText("  Run  ");
        gd = new GridData();
        gd.horizontalSpan = 2;
        gd.horizontalAlignment = GridData.END;
        runButton.setLayoutData(gd);

        runButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    run();
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

        return composite;
    }

    public Composite createResultsSection(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        composite.setLayout(new GridLayout());

        messageLabel = toolkit.createLabel(composite, "");
        messageLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        table = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION);
        table.setLayoutData(new GridData(GridData.FILL_BOTH));

        return composite;
    }

    public void run() throws Exception {

        table.removeAll();

        String host = hostsCombo.getText();
        SearchResult result = (SearchResult)hostsCombo.getData(host);
        Attributes attributes = result.getAttributes();
        String hostname = (String)attributes.getValue("name");
        String path = (String)attributes.getValue("path");

        String title = actionsCombo.getText();
        String action = (String)actionsCombo.getData(title);
        String parameters = parametersText.getText();

        NISAgentClient client = new NISAgentClient(hostname);

        Collection<String> list;
        if ("findByUid".equals(action)) {
            list = client.findByUid(path, new Integer(parameters));

        } else if ("findByGid".equals(action)) {
            list = client.findByGid(path, new Integer(parameters));
        } else {
            list = new ArrayList<String>();
        }

        for (String file : list) {
            TableItem ti = new TableItem(table, SWT.NONE);
            ti.setText(file);
        }

        messageLabel.setText("Found "+list.size()+" file(s).");
    }
}
