/**
 * Copyright (c) 2000-2006, Identyx Corporation.
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
package org.safehaus.penrose.studio.server;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.server.ServerConfig;
import org.safehaus.penrose.studio.PenroseImage;
import org.apache.log4j.Logger;

public class ServerDialog extends Dialog {

    Logger log = Logger.getLogger(getClass());

    public final static int CANCEL = 0;
    public final static int OK     = 1;

    Image penroseImage;
    Image connectImage;
    Image deleteImage;

    Shell shell;

    Table projectTable;

    private ServerConfig serverConfig;

    private int action;

	public ServerDialog(Shell parent, int style) throws Exception {
		super(parent, style);

        penroseImage = PenrosePlugin.getImage(PenroseImage.LOGO16);
        connectImage = PenrosePlugin.getImage(PenroseImage.OPEN);
        deleteImage = PenrosePlugin.getImage(PenroseImage.DELETE);

        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);

        createControl(shell);
    }

    public ServerDialog(Shell parent) throws Exception {
        super(parent);

        penroseImage = PenrosePlugin.getImage(PenroseImage.LOGO16);
        connectImage = PenrosePlugin.getImage(PenroseImage.OPEN);
        deleteImage = PenrosePlugin.getImage(PenroseImage.DELETE);

        shell = parent;

        createControl(shell);
    }

    public void dispose() {
    }

    public void open() {

        Point size = new Point(400, 300);
        shell.setSize(size);

        Display display = shell.getDisplay();
        Rectangle b;

        if (shell == getParent()) {
            b = display.getBounds();

        } else {
            b = getParent().getBounds();
        }

        shell.setLocation(b.x + (b.width - size.x)/2, b.y + (b.height - size.y)/2);

        shell.setText("Connect to Penrose Server");
        shell.setImage(penroseImage);
        
        shell.open();

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
    }

    public void createControl(final Shell parent) {
        try {
            parent.setLayout(new GridLayout());

            Composite composite = createHeadSection(parent);
            composite.setLayoutData(new GridData(GridData.FILL_BOTH));

            PenroseStudio penroseStudio = PenroseStudio.getInstance();

            Collection list = penroseStudio.getPenroseStudioConfig().getServerConfigs();
            for (Iterator i=list.iterator(); i.hasNext(); ) {
                ServerConfig serverConfig = (ServerConfig)i.next();

                TableItem item = new TableItem(projectTable, SWT.NONE);
				item.setText(serverConfig.getName());
                item.setImage(penroseImage);
                item.setData(serverConfig);
			}

        } catch (Exception e) {
            log.debug(e.getMessage(), e);
        }
	}

    public Composite createLogoSection(final Composite parent) throws Exception {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout());

        final Canvas canvas = new Canvas(composite, SWT.BORDER);
        canvas.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
        canvas.setLayoutData(new GridData(GridData.FILL_BOTH));

        canvas.addPaintListener(new PaintListener() {
            public void paintControl(PaintEvent event) {
                Rectangle imageBounds = penroseImage.getBounds();
                Rectangle canvasBounds = canvas.getBounds();

                GC gc = new GC(canvas);
                gc.drawImage(penroseImage, (canvasBounds.width-imageBounds.width)/2, (canvasBounds.height-imageBounds.height)/2);
                gc.dispose();
            }
        });

        return composite;
    }

	public Composite createHeadSection(final Composite parent) throws Exception {

		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);

		projectTable = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION);
		projectTable.setLayoutData(new GridData(GridData.FILL_BOTH));

		projectTable.addMouseListener(new MouseAdapter() {
            public void mouseDoubleClick(MouseEvent e) {
                connectSession();
            }
        });

        Menu menu = new Menu(parent.getShell(), SWT.POP_UP);
        projectTable.setMenu(menu);

        MenuItem connectMenuItem = new MenuItem(menu, SWT.NONE);
        connectMenuItem.setText("Connect");
        connectMenuItem.setImage(connectImage);
        connectMenuItem.setAccelerator(SWT.ALT | 'C');

        connectMenuItem.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                try {
                    connectSession();
                } catch (Exception ex) {
                    log.debug(ex.getMessage(), ex);
                }
            }
        });

        new MenuItem(menu, SWT.SEPARATOR);

        MenuItem copyMenuItem = new MenuItem(menu, SWT.NONE);
        copyMenuItem.setText("Copy");
        copyMenuItem.setAccelerator(SWT.CONTROL | 'C');

        copyMenuItem.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                try {
                    if (projectTable.getSelectionCount() == 0) return;

                    TableItem item = projectTable.getSelection()[0];
                    serverConfig = (ServerConfig)item.getData();

                } catch (Exception ex) {
                    log.debug(ex.getMessage(), ex);
                }
            }
        });

        MenuItem pasteMenuItem = new MenuItem(menu, SWT.NONE);
        pasteMenuItem.setText("Paste");
        pasteMenuItem.setAccelerator(SWT.CONTROL | 'V');

        pasteMenuItem.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                try {
                    if (serverConfig == null) return;

                    int counter = 2;
                    String newName = serverConfig.getName()+" ("+counter+")";

                    PenroseStudio penroseStudio = PenroseStudio.getInstance();
                    while (penroseStudio.getPenroseStudioConfig().getServerConfig(newName) != null) {
                        counter++;
                        newName = serverConfig.getName()+" ("+counter+")";
                    }

                    ServerConfig newServerConfig = new ServerConfig(serverConfig);
                    newServerConfig.setName(newName);

                    penroseStudio.addServer(newServerConfig);
                    penroseStudio.save();

                    TableItem item = new TableItem(projectTable, SWT.NONE);
                    item.setText(newServerConfig.getName());
                    item.setImage(penroseImage);
                    item.setData(newServerConfig);

                    serverConfig = null;

                } catch (Exception ex) {
                    log.debug(ex.getMessage(), ex);
                }
            }
        });

        MenuItem deleteMenuItem = new MenuItem(menu, SWT.NONE);
        deleteMenuItem.setText("Delete");
        deleteMenuItem.setImage(deleteImage);
        deleteMenuItem.setAccelerator(SWT.ALT | 'D');

        deleteMenuItem.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                try {
                    deleteProject();
                } catch (Exception ex) {
                    log.debug(ex.getMessage(), ex);
                }
            }
        });

        new MenuItem(menu, SWT.SEPARATOR);

        MenuItem propertiesMenuItem = new MenuItem(menu, SWT.NONE);
        propertiesMenuItem.setText("Properties");

        propertiesMenuItem.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                try {
                    editProject();
                } catch (Exception ex) {
                    log.debug(ex.getMessage(), ex);
                }
            }
        });

        Composite buttons = new Composite(composite, SWT.NONE);
        buttons.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        buttons.setLayout(new GridLayout());

        Button connectButton = new Button(buttons, SWT.PUSH);
        //connectButton.setImage(connectImage);
        connectButton.setText("Connect");

        GridData gc = new GridData(GridData.FILL_HORIZONTAL);
        gc.widthHint = 80;
        connectButton.setLayoutData(gc);

        connectButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                try {
                    connectSession();
                } catch (Exception ex) {
                    log.debug(ex.getMessage(), ex);
                }
            }
        });

        new Label(buttons, SWT.NONE);

        Button newButton = new Button(buttons, SWT.PUSH);
        newButton.setText("New");
        newButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        newButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                try {
                    newSession();
                } catch (Exception ex) {
                    log.debug(ex.getMessage(), ex);
                }
            }
        });

        Button editButton = new Button(buttons, SWT.PUSH);
        editButton.setText("Edit");
        editButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        editButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                try {
                    editProject();
                } catch (Exception ex) {
                    log.debug(ex.getMessage(), ex);
                }
            }
        });

        Button deleteButton = new Button(buttons, SWT.PUSH);
        //deleteButton.setImage(deleteImage);
        deleteButton.setText("Delete");
        deleteButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        deleteButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
                try {
    				deleteProject();
                } catch (Exception ex) {
                    log.debug(ex.getMessage(), ex);
                }
			}
		});

        new Label(buttons, SWT.NONE);

        Button cancelButton = new Button(buttons, SWT.PUSH);
        cancelButton.setText("Cancel");
        cancelButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        cancelButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                shell.close();
            }
        });

		return composite;
	}

	public void connectSession() {

        if (projectTable.getSelectionCount() == 0) return;

        TableItem item = projectTable.getSelection()[0];
        ServerConfig serverConfig = (ServerConfig)item.getData();
        String server = serverConfig.getHost()+(serverConfig.getPort() == 0 ? "" : ":"+serverConfig.getPort());

        try {
/*
            PenroseStudio penroseApplication = PenroseStudio.getInstance();
            penroseApplication.connect(server);
            penroseApplication.disconnect();

            PenroseClient client = new PenroseClient(serverConfig.getHost(), server.getPort(), projectConfig.getUsername(), serverConfig.getPassword());
            client.connect();
            client.close();
*/
            this.serverConfig = serverConfig;

            action = OK;
            shell.close();

        } catch (Exception ex) {
            MessageDialog.openError(shell, "Can't connect to "+server, "Error: "+ex.getMessage());
        }
	}
	
	public void newSession() throws Exception {
		ServerConfig serverConfig = new ServerConfig();
        serverConfig.setHost("localhost");
        serverConfig.setPort(1099);

		ServerEditorDialog dialog = new ServerEditorDialog(shell, SWT.NONE);
        dialog.setServerConfig(serverConfig);
		dialog.open();

        if (dialog.getAction() == ServerEditorDialog.CANCEL) return;

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.addServer(serverConfig);
        penroseStudio.save();

        TableItem item = new TableItem(projectTable, SWT.NONE);
        item.setText(serverConfig.getName());
        item.setImage(penroseImage);
        item.setData(serverConfig);
	}
	
	public void deleteProject() throws Exception {
        if (projectTable.getSelectionCount() == 0) return;

		TableItem item = projectTable.getSelection()[0];
        ServerConfig serverConfig = (ServerConfig)item.getData();

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
		penroseStudio.removeServer(serverConfig.getName());
        penroseStudio.save();

		item.dispose();
	}
	
	public void editProject() throws Exception {
        if (projectTable.getSelectionCount() == 0) return;

        TableItem item = projectTable.getSelection()[0];
		ServerConfig serverConfig = (ServerConfig)item.getData();
        String oldProjectName = serverConfig.getName();
        System.out.println("Editing server: "+oldProjectName);

		ServerEditorDialog dialog = new ServerEditorDialog(shell, SWT.NONE);
        dialog.setServerConfig(serverConfig);
		dialog.open();

        if (dialog.getAction() == ServerEditorDialog.CANCEL) return;

        PenroseStudio penroseStudio = PenroseStudio.getInstance();

        if (!oldProjectName.equals(serverConfig.getName())) {
            penroseStudio.removeServer(oldProjectName);
            penroseStudio.addServer(serverConfig);
        }

        penroseStudio.save();

        item.setText(serverConfig.getName());
        projectTable.redraw();
    }

    public ServerConfig getProject() {
        return serverConfig;
    }

    public void setProject(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }
}
