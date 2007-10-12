package org.safehaus.penrose.studio.jndi.source;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.graphics.Point;
import org.apache.log4j.Logger;
import org.safehaus.penrose.source.SourceConfig;
import org.safehaus.penrose.source.FieldConfig;
import org.safehaus.penrose.ldap.RDN;
import org.safehaus.penrose.ldap.Attributes;
import org.safehaus.penrose.ldap.Attribute;
import org.safehaus.penrose.ldap.RDNBuilder;
import org.safehaus.penrose.studio.PenroseStudioPlugin;
import org.safehaus.penrose.studio.PenroseImage;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Collection;

/**
 * @author Endi S. Dewata
 */
public class LDAPSearchResultDialog extends Dialog {

    Logger log = Logger.getLogger(getClass());

    public final static int CANCEL = 0;
    public final static int OK     = 1;

    Shell shell;

    Map primaryKeyTexts = new HashMap();
    Table table;

    SourceConfig sourceConfig;

    private RDN rdn;
    private Attributes attributes = new Attributes();

    int action;

	public LDAPSearchResultDialog(Shell parent, int style) {
		super(parent, style);
    }

    public void open() {

        setText("Record Editor");
        shell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);

        init();
        reset();

        Point size = new Point(500, 400);
        shell.setSize(size);

        Point l = getParent().getLocation();
        Point s = getParent().getSize();

        shell.setLocation(l.x + (s.x - size.x)/2, l.y + (s.y - size.y)/2);

        shell.setText(getText());
        shell.setImage(PenroseStudioPlugin.getImage(PenroseImage.LOGO16));
        shell.open();

        Display display = getParent().getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
    }

    public void init() {
        createControl(shell);
    }

    public void reset() {

        for (Iterator i=primaryKeyTexts.keySet().iterator(); i.hasNext(); ) {
            String name = (String)i.next();
            Text valueText = (Text)primaryKeyTexts.get(name);
            valueText.setText("");
        }

        table.removeAll();

        if (rdn != null) {

            for (Iterator i=rdn.getNames().iterator(); i.hasNext(); ) {
                String name = (String)i.next();
                Object value = rdn.get(name);

                Text valueText = (Text)primaryKeyTexts.get(name);
                if (valueText == null) continue;

                valueText.setText(value.toString());
            }
        }

        for (Iterator i=attributes.getAll().iterator(); i.hasNext(); ) {
            Attribute attribute = (Attribute)i.next();
            String name = attribute.getName();

            for (Iterator j=attribute.getValues().iterator(); j.hasNext(); ) {
                Object value = j.next();

                TableItem tc = new TableItem(table, SWT.NONE);
                tc.setText(0, name);
                tc.setText(1, value.toString());
                tc.setData("name", name);
                tc.setData("value", value);
            }
        }
    }

    public void createControl(final Shell parent) {
        parent.setLayout(new GridLayout());

        TabFolder tabFolder = new TabFolder(parent, SWT.NONE);
        tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite primaryKeysPage = createPrimaryKeysPage(tabFolder);
        primaryKeysPage.setLayoutData(new GridData(GridData.FILL_BOTH));

        TabItem primaryKeysTab = new TabItem(tabFolder, SWT.NONE);
        primaryKeysTab.setText("Primary Keys");
        primaryKeysTab.setControl(primaryKeysPage);

        Composite attributesPage = createAttributesPage(tabFolder);
        attributesPage.setLayoutData(new GridData(GridData.FILL_BOTH));

        TabItem attributesTab = new TabItem(tabFolder, SWT.NONE);
        attributesTab.setText("Attributes");
        attributesTab.setControl(attributesPage);

        Composite buttons = getButtons(parent);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalAlignment = GridData.END;
        buttons.setLayoutData(gd);
    }

    public Composite getButtons(Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new RowLayout());

        Button saveButton = new Button(composite, SWT.PUSH);
        saveButton.setText("Save");

        saveButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                save();

                action = OK;
                shell.close();
            }
        });

        Button cancelButton = new Button(composite, SWT.PUSH);
        cancelButton.setText("Cancel");

        cancelButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                action = CANCEL;
                shell.close();
            }
        });

        return composite;
    }

    public void save()  {
        attributes = new Attributes();

        TableItem[] items = table.getItems();
        for (int i=0; i<items.length; i++) {
            TableItem ti = items[i];
            String name = (String)ti.getData("name");
            Object value = ti.getData("value");
            attributes.addValue(name, value);
        }

        RDNBuilder rb = new RDNBuilder();

        for (Iterator i=primaryKeyTexts.keySet().iterator(); i.hasNext(); ) {
            String name = (String)i.next();
            Text valueText = (Text)primaryKeyTexts.get(name);
            String value = valueText.getText();
            rb.set(name, value);
            attributes.addValue(name, value);
        }

        rdn = rb.toRdn();
    }

    public Composite createPrimaryKeysPage(Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));

        Collection fieldConfigs = sourceConfig.getPrimaryKeyFieldConfigs();
        for (Iterator i=fieldConfigs.iterator(); i.hasNext(); ) {
            FieldConfig fieldConfig = (FieldConfig)i.next();
            String name = fieldConfig.getName();

            Label nameLabel = new Label(composite, SWT.NONE);
            nameLabel.setText(name+":");
            GridData gd = new GridData(GridData.FILL);
            gd.widthHint = 100;
            nameLabel.setLayoutData(gd);

            Text valueText = new Text(composite, SWT.BORDER);
            gd = new GridData(GridData.FILL);
            gd.widthHint = 300;
            valueText.setLayoutData(gd);

            primaryKeyTexts.put(name, valueText);
        }

        return composite;
    }

    public Composite createAttributesPage(Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));

        table = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        table.setLayoutData(new GridData(GridData.FILL_BOTH));

        TableColumn tc = new TableColumn(table, SWT.NONE);
        tc.setText("Name");
        tc.setWidth(100);

        tc = new TableColumn(table, SWT.NONE);
        tc.setText("Value");
        tc.setWidth(300);

        return composite;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public SourceConfig getSourceConfig() {
        return sourceConfig;
    }

    public void setSourceConfig(SourceConfig sourceConfig) {
        this.sourceConfig = sourceConfig;
    }

    public RDN getRdn() {
        return rdn;
    }

    public void setRdn(RDN rdn) {
        this.rdn = rdn;
    }

    public Attributes getAttributes() {
        return attributes;
    }

    public void setAttributes(Attributes attributes) {
        this.attributes.set(attributes);
    }
}
