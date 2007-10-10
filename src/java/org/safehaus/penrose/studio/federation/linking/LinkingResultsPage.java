package org.safehaus.penrose.studio.federation.linking;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.apache.log4j.Logger;
import org.safehaus.penrose.ldap.SearchResult;
import org.safehaus.penrose.ldap.Attributes;
import org.safehaus.penrose.ldap.Attribute;
import org.safehaus.penrose.ldap.DN;
import org.safehaus.penrose.filter.Filter;
import org.safehaus.penrose.filter.SubstringFilter;
import org.safehaus.penrose.source.Source;

import java.util.Collection;
import java.util.ArrayList;

/**
 * @author Endi Sukma Dewata
 */
public class LinkingResultsPage extends WizardPage {

    Logger log = Logger.getLogger(getClass());

    public final static String NAME = "Filter";

    Table attributesTable;

    Table resultsTable;
    Table resultAttributesTable;

    private DN dn;
    private SearchResult entry;
    private Source source;

    private DN baseDn;
    private Collection<SearchResult> results;

    public LinkingResultsPage() {
        super(NAME);
        setDescription("Select the global entries to link with this entry.");
    }

    public void createControl(final Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        composite.setLayout(layout);

        Composite topPanel = createTopPanel(composite);
        topPanel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Composite bottomPanel = createBottomPanel(composite);
        bottomPanel.setLayoutData(new GridData(GridData.FILL_BOTH));

        setControl(composite);
    }

    public Composite createTopPanel(Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        composite.setLayout(layout);

        attributesTable = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        gd.heightHint = 200;
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
        item.setText(1, dn.toString());

        Attributes attributes = entry.getAttributes();
        for (Attribute attribute : attributes.getAll()) {
            for (Object value : attribute.getValues()) {
                TableItem ti = new TableItem(attributesTable, SWT.NONE);
                ti.setText(0, attribute.getName());
                ti.setText(1, value.toString());
            }
        }

        return composite;
    }

    public Composite createBottomPanel(Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        composite.setLayout(layout);

        resultsTable = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.CHECK);
        resultsTable.setLayoutData(new GridData(GridData.FILL_BOTH));

        resultsTable.setHeaderVisible(true);
        resultsTable.setLinesVisible(true);

        TableColumn tc = new TableColumn(resultsTable, SWT.NONE);
        tc.setText("DN");
        tc.setWidth(280);

        final Shell shell = getShell();

        resultsTable.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    resultAttributesTable.removeAll();

                    if (resultsTable.getSelectionCount() != 1) return;

                    TableItem item = resultsTable.getSelection()[0];
                    SearchResult result = (SearchResult)item.getData();

                    Attributes attributes = result.getAttributes();
                    for (Attribute attribute : attributes.getAll()) {
                        for (Object value : attribute.getValues()) {
                            TableItem ti = new TableItem(resultAttributesTable, SWT.NONE);
                            ti.setText(0, attribute.getName());
                            ti.setText(1, value.toString());
                        }
                    }

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    MessageDialog.openError(shell, "Action Failed", e.getMessage());
                }
            }
        });

        resultAttributesTable = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        resultAttributesTable.setLayoutData(new GridData(GridData.FILL_BOTH));

        resultAttributesTable.setHeaderVisible(true);
        resultAttributesTable.setLinesVisible(true);

        tc = new TableColumn(resultAttributesTable, SWT.NONE);
        tc.setText("Attribute");
        tc.setWidth(80);

        tc = new TableColumn(resultAttributesTable, SWT.NONE);
        tc.setText("Value");
        tc.setWidth(200);

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

    public SearchResult getEntry() {
        return entry;
    }

    public void setEntry(SearchResult entry) {
        this.entry = entry;
    }

    public Collection<SearchResult> getResults() {
        return results;
    }

    public void setResults(Collection<SearchResult> results) {
        this.results = results;
    }

    public void setVisible(boolean b) {
        super.setVisible(b);
        if (b) refresh();
    }

    public void refresh() {
        resultsTable.removeAll();

        if (results != null) {
            for (SearchResult result : results) {
                Attributes attributes = result.getAttributes();

                boolean linked = false;
                for (Object object : attributes.getValues("seeAlso")) {
                    if (!dn.matches(object.toString())) continue;
                    linked = true;
                    break;
                }

                if (linked) continue;

                TableItem item = new TableItem(resultsTable, SWT.NONE);
                item.setText(0, result.getDn().append(baseDn).toString());
                item.setData(result);
            }
        }
    }

    public Collection<SearchResult> getSelections() {
        Collection<SearchResult> list = new ArrayList<SearchResult>();
        for (TableItem item : resultsTable.getItems()) {
            if (!item.getChecked()) continue;
            SearchResult result = (SearchResult)item.getData();
            list.add(result);
        }
        return list;
    }

    public DN getDn() {
        return dn;
    }

    public void setDn(DN dn) {
        this.dn = dn;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public DN getBaseDn() {
        return baseDn;
    }

    public void setBaseDn(DN baseDn) {
        this.baseDn = baseDn;
    }
}
