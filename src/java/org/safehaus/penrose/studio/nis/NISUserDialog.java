package org.safehaus.penrose.studio.nis;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.graphics.Point;
import org.apache.log4j.Logger;
import org.safehaus.penrose.partition.SourceConfig;
import org.safehaus.penrose.ldap.Attributes;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseImage;

import java.util.*;

/**
 * @author Endi S. Dewata
 */
public class NISUserDialog extends Dialog {

    Logger log = Logger.getLogger(getClass());

    public final static int CANCEL = 0;
    public final static int OK     = 1;

    Shell shell;

    SourceConfig sourceConfig;

    private Attributes attributes = new Attributes();
    private Collection uidNumbers = new ArrayList();

    Label domainText;
    Label uidText;
    Label uidNumberText;

    int action;

	public NISUserDialog(Shell parent, int style) {
		super(parent, style);
    }

    public void open() {

        setText("NIS User Editor");
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
        String domainName = (String)attributes.getValue("domain");
        domainText.setText(domainName == null ? "" : domainName.toString());

        Object uid = attributes.getValue("uid");
        uidText.setText(uid == null ? "" : uid.toString());

        Object uidNumber = attributes.getValue("uidNumber");
        uidNumberText.setText(uidNumber == null ? "" : uidNumber.toString());
    }

    public void createControl(Shell parent) {
        parent.setLayout(new GridLayout());

        Composite composite = createInfoPanel(parent);
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        composite = createUIDPanel(parent);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        composite = createButtonsPanel(parent);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalAlignment = GridData.END;
        composite.setLayoutData(gd);
    }

    public Composite createInfoPanel(Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));

        Label domainLabel = new Label(composite, SWT.NONE);
        domainLabel.setText("Domain:");
        GridData gd = new GridData();
        gd.widthHint = 100;
        domainLabel.setLayoutData(gd);

        domainText = new Label(composite, SWT.NONE);
        domainText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label uidLabel = new Label(composite, SWT.NONE);
        uidLabel.setText("UID:");
        uidLabel.setLayoutData(new GridData());

        uidText = new Label(composite, SWT.NONE);
        uidText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label uidNumberLabel = new Label(composite, SWT.NONE);
        uidNumberLabel.setText("UID Number:");
        uidNumberLabel.setLayoutData(new GridData());

        uidNumberText = new Label(composite, SWT.NONE);
        uidNumberText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        return composite;
    }

    public Composite createUIDPanel(Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));

        List newUidList = new List(composite, SWT.BORDER);
        newUidList.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite buttons = new Composite(composite, SWT.NONE);
        buttons.setLayout(new GridLayout());
        buttons.setLayoutData(new GridData(GridData.FILL_VERTICAL));

        Button addButton = new Button(buttons, SWT.PUSH);
        addButton.setText("Add");
        addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Button editButton = new Button(buttons, SWT.PUSH);
        editButton.setText("Edit");
        editButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Button removeButton = new Button(buttons, SWT.PUSH);
        removeButton.setText("Remove");
        removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        return composite;
    }

    public Composite createButtonsPanel(Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new RowLayout());

        Button closeButton = new Button(composite, SWT.PUSH);
        closeButton.setText("Close");

        closeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                action = OK;
                shell.close();
            }
        });

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

    public Attributes getAttributes() {
        return attributes;
    }

    public void setAttributes(Attributes attributes) {
        this.attributes.set(attributes);
    }

    public Collection getUidNumbers() {
        return uidNumbers;
    }

    public void addUidNumber(Object uidNumber) {
        uidNumbers.add(uidNumber);
    }

    public void setUidNumbers(Collection uidNumbers) {
        if (this.uidNumbers == uidNumbers) return;
        this.uidNumbers.clear();
        this.uidNumbers.addAll(uidNumbers);
    }
}
