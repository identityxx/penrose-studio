package org.safehaus.penrose.studio.federation.wizard;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.TreeAdapter;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.graphics.Point;
import org.safehaus.penrose.partition.PartitionConfig;
import org.safehaus.penrose.directory.EntryConfig;
import org.safehaus.penrose.studio.PenroseStudioPlugin;
import org.safehaus.penrose.studio.PenroseImage;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author Endi S. Dewata
 */
public class BrowserDialog extends Dialog {

    Shell shell;

    Tree tree;
    Button saveButton;

    private PartitionConfig partitionConfig;
    private EntryConfig entryConfig;

	public BrowserDialog(Shell parent, int style) {
		super(parent, style);

        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);

        createControl(shell);
    }

    public void open () {

        Point size = new Point(400, 300);
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

    public void createControl(final Shell parent) {
        parent.setLayout(new GridLayout());

        tree = new Tree(parent, SWT.BORDER);
        tree.setLayoutData(new GridData(GridData.FILL_BOTH));

        tree.addTreeListener(new TreeAdapter() {
            public void treeExpanded(TreeEvent event) {
                if (event.item == null) return;

                TreeItem item = (TreeItem)event.item;
                EntryConfig entry = (EntryConfig)item.getData();

                TreeItem items[] = item.getItems();
                for (int i=0; i<items.length; i++) {
                    items[i].dispose();
                }

                Collection children = partitionConfig.getDirectoryConfig().getChildren(entry);
                for (Iterator i=children.iterator(); i.hasNext(); ) {
                    EntryConfig child = (EntryConfig)i.next();

                    TreeItem it = new TreeItem(item, SWT.NONE);
                    it.setText(child.getRdn().toString());
                    it.setData(child);

                    new TreeItem(it, SWT.NONE);
                }
            }
        });

        tree.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                saveButton.setEnabled(true);
            }
        });

        Composite buttons = new Composite(parent, SWT.NONE);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalAlignment = GridData.END;
        buttons.setLayoutData(gd);
        buttons.setLayout(new RowLayout());

		saveButton = new Button(buttons, SWT.PUSH);
        saveButton.setText("Select");
        saveButton.setEnabled(false);

		saveButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
                TreeItem item = tree.getSelection()[0];
                entryConfig = (EntryConfig)item.getData();
                shell.close();
			}
		});

		Button cancelButton = new Button(buttons, SWT.PUSH);
        cancelButton.setText("Cancel");
		cancelButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
                entryConfig = null;
                shell.close();
			}
		});
	}

    public EntryConfig getEntryConfig() {
        return entryConfig;
    }

    public void setEntryConfig(EntryConfig entryConfig) {
        this.entryConfig = entryConfig;
    }

    public PartitionConfig getPartitionConfig() {
        return partitionConfig;
    }

    public void setPartitionConfig(PartitionConfig partitionConfig) {
        this.partitionConfig = partitionConfig;

        Collection rootEntries = partitionConfig.getDirectoryConfig().getRootEntryConfigs();
        for (Iterator i=rootEntries.iterator(); i.hasNext(); ) {
            EntryConfig entry = (EntryConfig)i.next();
            String dn = entry.getDn().isEmpty() ? "Root DSE" : entry.getDn().toString();

            TreeItem item = new TreeItem(tree, SWT.NONE);
            item.setText(dn);
            item.setData(entry);

            new TreeItem(item, SWT.NONE);
        }
    }
}
