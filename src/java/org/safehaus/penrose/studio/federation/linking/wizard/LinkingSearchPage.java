package org.safehaus.penrose.studio.federation.linking.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.apache.log4j.Logger;
import org.safehaus.penrose.ldap.SearchResult;
import org.safehaus.penrose.ldap.Attributes;
import org.safehaus.penrose.ldap.Attribute;
import org.safehaus.penrose.ldap.DN;
import org.safehaus.penrose.filter.Filter;
import org.safehaus.penrose.filter.SubstringFilter;
import org.safehaus.penrose.studio.federation.wizard.BrowserWizard;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.partition.PartitionClient;

import java.util.Collection;
import java.util.ArrayList;

/**
 * @author Endi Sukma Dewata
 */
public class LinkingSearchPage extends WizardPage {

    Logger log = Logger.getLogger(getClass());

    public final static String NAME = "Filter";

    Table attributesTable;

    Text baseDnText;
    Text filterText;
    Combo scopeCombo;

    private DN dn;
    private Filter filter;
    private SearchResult searchResult;
    private PartitionClient partitionClient;

    public LinkingSearchPage() {
        super(NAME);
        setDescription("Specify the parameters for searching the global entries.");
    }

    public void createControl(final Composite parent) {
        try {
            Composite composite = new Composite(parent, SWT.NONE);
            GridLayout layout = new GridLayout();
            layout.marginWidth = 0;
            composite.setLayout(layout);

            attributesTable = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
            GridData gd = new GridData(GridData.FILL_BOTH);
            gd.horizontalSpan = 2;
            attributesTable.setLayoutData(gd);

            attributesTable.setHeaderVisible(false);
            attributesTable.setLinesVisible(true);

            TableColumn tc = new TableColumn(attributesTable, SWT.NONE);
            tc.setText("Attribute");
            tc.setWidth(150);

            tc = new TableColumn(attributesTable, SWT.NONE);
            tc.setText("Value");
            tc.setWidth(400);

            TableItem item = new TableItem(attributesTable, SWT.NONE);
            item.setText(0, "dn");
            item.setText(1, searchResult.getDn().toString());

            Attributes attributes = searchResult.getAttributes();
            for (Attribute attribute : attributes.getAll()) {
                for (Object value : attribute.getValues()) {
                    TableItem ti = new TableItem(attributesTable, SWT.NONE);
                    ti.setText(0, attribute.getName());
                    ti.setText(1, value.toString());
                }
            }

            new Label(composite, SWT.NONE);

            Composite bottomPanel = createBottomPanel(composite);
            bottomPanel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

            setControl(composite);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }

    public Composite createBottomPanel(Composite parent) throws Exception {

        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(3, false);
        layout.marginWidth = 0;
        composite.setLayout(layout);

        Label baseDnLabel = new Label(composite, SWT.NONE);
        baseDnLabel.setText("Base DN:");
        baseDnLabel.setLayoutData(new GridData());

        baseDnText = new Text(composite, SWT.BORDER);
        baseDnText.setText(dn.toString());
        baseDnText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Button browseButton = new Button(composite, SWT.PUSH);
        browseButton.setText("  Browse  ");
        browseButton.setLayoutData(new GridData());

        browseButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    BrowserWizard wizard = new BrowserWizard();
                    wizard.setBaseDn(dn);
                    wizard.setDn(baseDnText.getText());
                    wizard.setPartitionClient(partitionClient);

                    IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                    WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
                    dialog.setPageSize(600, 300);

                    if (dialog.open() != Window.OK) return;

                    baseDnText.setText(wizard.getDn());

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });
        
        Label filterLabel = new Label(composite, SWT.NONE);
        filterLabel.setText("Filter:");
        filterLabel.setLayoutData(new GridData());

        filterText = new Text(composite, SWT.BORDER);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        filterText.setLayoutData(gd);

        Label scopeLabel = new Label(composite, SWT.NONE);
        scopeLabel.setText("Scope:");
        scopeLabel.setLayoutData(new GridData());

        String scope = "SUBTREE";

        scopeCombo = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
        scopeCombo.add("OBJECT");
        scopeCombo.add("ONELEVEL");
        scopeCombo.add("SUBTREE");
        scopeCombo.setText(scope);

        gd = new GridData();
        gd.widthHint = 100;
        scopeCombo.setLayoutData(gd);
/*
        Attributes attributes = searchResult.getAttributes();
        String uid = (String)attributes.getValue("uid");
        String cn = (String)attributes.getValue("cn");

        Filter filter = null;
        filter = FilterTool.appendOrFilter(filter, createFilter("uid", uid));
        filter = FilterTool.appendOrFilter(filter, createFilter("cn", cn));
*/
        filterText.setText(filter == null ? "(objectClass=*)" : filter.toString());

        return composite;
    }

    public Filter createFilter(String name, String s) {
        if (s == null || "".equals(s)) return null;

        Collection<Object> substrings = new ArrayList<Object>();
        substrings.add(SubstringFilter.STAR);

        StringBuilder sb = null;

        for (char c : s.toCharArray()) {
            if (Character.isLetter(c)) {
                if (sb == null) sb = new StringBuilder();
                sb.append(c);

            } else if (sb != null) {
                if (sb.length() >= 2) {
                    substrings.add(sb.toString());
                    substrings.add(SubstringFilter.STAR);
                }
                sb = null;
            }
        }

        if (sb != null) {
            substrings.add(sb.toString());
            substrings.add(SubstringFilter.STAR);
        }

        return new SubstringFilter(name, substrings);
    }

    public SearchResult getSearchResult() {
        return searchResult;
    }

    public void setSearchResult(SearchResult searchResult) {
        this.searchResult = searchResult;
    }

    public PartitionClient getPartitionClient() {
        return partitionClient;
    }

    public void setPartitionClient(PartitionClient partitionClient) {
        this.partitionClient = partitionClient;
    }

    public String getBaseDn() {
        return baseDnText.getText();
    }

    public String getFilter() {
        return filterText.getText();
    }

    public int getScope() {
        return scopeCombo.getSelectionIndex();
    }

    public DN getDn() {
        return dn;
    }

    public void setDn(DN dn) {
        this.dn = dn;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }
}
