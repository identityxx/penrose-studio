package org.safehaus.penrose.studio.mapping.editor;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.graphics.Point;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.mapping.EntryMapping;
import org.safehaus.penrose.partition.Partition;

/**
 * @author Endi S. Dewata
 */
public class MappingDialog extends Dialog {

    public final static int CANCEL = 0;
    public final static int OK     = 1;

    Shell shell;

    Button saveButton;

    private int action = CANCEL;

    private Server server;
    private Partition partition;
    private EntryMapping entryMapping;

    public MappingDialog(Shell parent, int style) {
        super(parent, style);
        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);
    }

    public void open () {

        init();

        Point size = new Point(800, 600);
        shell.setSize(size);

        Point l = getParent().getLocation();
        Point s = getParent().getSize();

        shell.setLocation(l.x + (s.x - size.x)/2, l.y + (s.y - size.y)/2);

        shell.setText(entryMapping.getDn());
        shell.setImage(PenrosePlugin.getImage(PenroseImage.LOGO16));
        shell.open();

        Display display = getParent().getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
    }

    public void init() {
        shell.setLayout(new GridLayout());

        final TabFolder tabFolder = new TabFolder(shell, SWT.NONE);
        tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));

        tabFolder.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                TabItem items[] = tabFolder.getSelection();
                if (items.length == 0) return;

                MappingDialogPage page = (MappingDialogPage)items[0].getControl();
                if (page == null) return;
                page.refresh();
            }
        });

        Composite dnPage = new DNDialogPage(this, tabFolder, SWT.NONE);
        dnPage.setLayoutData(new GridData(GridData.FILL_BOTH));

        TabItem dnTab = new TabItem(tabFolder, SWT.NONE);
        dnTab.setText("DN");
        dnTab.setControl(dnPage);

        Composite objectClassesPage = new ObjectClassesDialogPage(this, tabFolder, SWT.NONE);
        objectClassesPage.setLayoutData(new GridData(GridData.FILL_BOTH));

        TabItem objectClassesTab = new TabItem(tabFolder, SWT.NONE);
        objectClassesTab.setText("Object Classes");
        objectClassesTab.setControl(objectClassesPage);

        Composite attributesPage = new AttributesDialogPage(this, tabFolder, SWT.NONE);
        attributesPage.setLayoutData(new GridData(GridData.FILL_BOTH));

        TabItem attributesTab = new TabItem(tabFolder, SWT.NONE);
        attributesTab.setText("Attributes");
        attributesTab.setControl(attributesPage);

        Composite sourcesPage = new SourcesDialogPage(this, tabFolder, SWT.NONE);
        sourcesPage.setLayoutData(new GridData(GridData.FILL_BOTH));

        TabItem sourcesTab = new TabItem(tabFolder, SWT.NONE);
        sourcesTab.setText("Sources");
        sourcesTab.setControl(sourcesPage);

        Composite fieldsPage = new FieldsDialogPage(this, tabFolder, SWT.NONE);
        fieldsPage.setLayoutData(new GridData(GridData.FILL_BOTH));

        TabItem fieldsTab = new TabItem(tabFolder, SWT.NONE);
        fieldsTab.setText("Fields");
        fieldsTab.setControl(fieldsPage);

        Composite relationshipsPage = new RelationshipsDialogPage(this, tabFolder, SWT.NONE);
        relationshipsPage.setLayoutData(new GridData(GridData.FILL_BOTH));

        TabItem relationshipsTab = new TabItem(tabFolder, SWT.NONE);
        relationshipsTab.setText("Relationships");
        relationshipsTab.setControl(relationshipsPage);

        Composite buttons = new Composite(shell, SWT.NONE);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalAlignment = GridData.END;
        buttons.setLayoutData(gd);
        buttons.setLayout(new RowLayout());

        saveButton = new Button(buttons, SWT.PUSH);
        saveButton.setText("OK");

        saveButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {

                action = OK;
                shell.close();
            }
        });

        Button cancelButton = new Button(buttons, SWT.PUSH);
        cancelButton.setText("Cancel");
        cancelButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                shell.close();
            }
        });
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public Partition getPartition() {
        return partition;
    }

    public void setPartition(Partition partition) {
        this.partition = partition;
    }

    public EntryMapping getEntryMapping() {
        return entryMapping;
    }

    public void setEntryMapping(EntryMapping entryMapping) {
        this.entryMapping = entryMapping;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }
}
