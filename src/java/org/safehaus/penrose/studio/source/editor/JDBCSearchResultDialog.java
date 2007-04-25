package org.safehaus.penrose.studio.source.editor;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.graphics.Point;
import org.apache.log4j.Logger;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.ldap.*;
import org.safehaus.penrose.partition.SourceConfig;
import org.safehaus.penrose.partition.FieldConfig;
import org.safehaus.penrose.partition.Partition;

import java.util.Iterator;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;

/**
 * @author Endi S. Dewata
 */
public class JDBCSearchResultDialog extends Dialog {

    Logger log = Logger.getLogger(getClass());

    public final static int CANCEL = 0;
    public final static int OK     = 1;

    Shell shell;

    Map texts = new HashMap();

    private Partition partition;
    SourceConfig sourceConfig;

    private RDN rdn;
    private Attributes attributes = new Attributes();

    int action;

	public JDBCSearchResultDialog(Shell parent, int style) {
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
        shell.setImage(PenrosePlugin.getImage(PenroseImage.LOGO16));
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

        for (Iterator i=attributes.getAll().iterator(); i.hasNext(); ) {
            Attribute attribute = (Attribute)i.next();
            String name = attribute.getName();
            Object value = attributes.getValue(name);

            Text valueText = (Text) texts.get(name);
            if (valueText == null) continue;

            valueText.setText(value == null ? "" : value.toString());
        }

        if (rdn != null) {
            for (Iterator i=rdn.getNames().iterator(); i.hasNext(); ) {
                String name = (String)i.next();
                Object value = rdn.get(name);

                Text valueText = (Text) texts.get(name);
                if (valueText == null) continue;

                valueText.setText(value == null ? "" : value.toString());
            }
        }
    }

    public void createControl(final Shell parent) {
        parent.setLayout(new GridLayout());

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        Collection fieldConfigs = sourceConfig.getFieldConfigs();
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

            texts.put(name, valueText);
        }

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
        attributes.clear();

        RDNBuilder rb = new RDNBuilder();

        for (Iterator i= texts.keySet().iterator(); i.hasNext(); ) {
            String name = (String)i.next();
            FieldConfig fieldConfig = sourceConfig.getFieldConfig(name);

            Text valueText = (Text) texts.get(name);
            String value = valueText.getText();

            attributes.setValue(name, value);
            if (fieldConfig.isPrimaryKey()) rb.set(name, value);
        }

        rdn = rb.toRdn();
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

    public Partition getPartition() {
        return partition;
    }

    public void setPartition(Partition partition) {
        this.partition = partition;
    }
}
