/**
 * Copyright 2009 Red Hat, Inc.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.safehaus.penrose.studio.directory.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TreeAdapter;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;
import org.safehaus.penrose.ldap.DN;
import org.safehaus.penrose.directory.EntryClient;
import org.safehaus.penrose.directory.DirectoryClient;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.studio.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Endi S. Dewata
 */
public class SelectEntryDialog extends Dialog {

    public Logger log = LoggerFactory.getLogger(getClass());
    Shell shell;

    Tree tree;
    Button saveButton;

    Server server;
    String partitionName;
    DN dn;

	public SelectEntryDialog(Shell parent, int style) {
		super(parent, style);

        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);

        createControl(shell);
    }

    public void open () throws Exception {

        PenroseClient client = server.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
        PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
        DirectoryClient directoryClient = partitionClient.getDirectoryClient();

        for (String id : directoryClient.getRootEntryNames()) {
            EntryClient entryClient = directoryClient.getEntryClient(id);
            DN dn = entryClient.getDn();

            String label = dn.isEmpty() ? "Root DSE" : dn.toString();

            TreeItem item = new TreeItem(tree, SWT.NONE);
            item.setText(label);
            item.setData(entryClient);

            new TreeItem(item, SWT.NONE);
        }

        Point size = new Point(400, 300);
        shell.setSize(size);

        Point l = getParent().getLocation();
        Point s = getParent().getSize();

        shell.setLocation(l.x + (s.x - size.x)/2, l.y + (s.y - size.y)/2);

        shell.setText(getText());
        shell.setImage(PenroseStudio.getImage(PenroseImage.LOGO));
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
                try {
                    if (event.item == null) return;

                    TreeItem item = (TreeItem)event.item;
                    EntryClient entryClient = (EntryClient)item.getData();

                    TreeItem items[] = item.getItems();
                    for (TreeItem ti : items) ti.dispose();

                    PenroseClient client = server.getClient();
                    PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
                    PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
                    DirectoryClient directoryClient = partitionClient.getDirectoryClient();

                    for (String id : entryClient.getChildNames()) {
                        EntryClient childClient = directoryClient.getEntryClient(id);

                        TreeItem it = new TreeItem(item, SWT.NONE);
                        it.setText(childClient.getDn().getRdn().toString());
                        it.setData(childClient);

                        new TreeItem(it, SWT.NONE);
                    }

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
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
			public void widgetSelected(SelectionEvent event) {
                try {
                    TreeItem item = tree.getSelection()[0];
                    EntryClient entryClient = (EntryClient)item.getData();
                    dn = entryClient.getDn();
                    shell.close();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
		});

		Button cancelButton = new Button(buttons, SWT.PUSH);
        cancelButton.setText("Cancel");
		cancelButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
                dn = null;
                shell.close();
			}
		});
	}

    public DN getDn() {
        return dn;
    }

    public void setDn(DN dn) {
        this.dn = dn;
    }

    public String getPartitionName() {
        return partitionName;
    }

    public void setPartitionName(String partitionName) throws Exception {
        this.partitionName = partitionName;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }
}
