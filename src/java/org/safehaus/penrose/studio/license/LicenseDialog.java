/**
 * Copyright (c) 2000-2005, Identyx Corporation.
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
package org.safehaus.penrose.studio.license;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.welcome.action.EnterLicenseKeyAction;
import org.apache.log4j.Logger;

import java.text.SimpleDateFormat;
import java.text.DateFormat;

public class LicenseDialog extends Dialog {

    Logger log = Logger.getLogger(getClass());

    public final static int CANCEL = 0;
    public final static int OK     = 1;

    Image penroseImage;
    Image connectImage;
    Image deleteImage;

    Shell shell;

    Table projectTable;

    FormToolkit toolkit;
    Font boldFont;

    Label messageLabel;

    private String filename;
    private int action;

    DateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");

    public LicenseDialog(Shell parent) {
        super(parent);

        penroseImage = PenrosePlugin.getImage(PenroseImage.LOGO16);
        connectImage = PenrosePlugin.getImage(PenroseImage.CONNECT);
        deleteImage = PenrosePlugin.getImage(PenroseImage.DELETE);

        shell = parent;

        createControl(shell);
    }

    public void dispose() {
        toolkit.dispose();
        boldFont.dispose();
    }

    public void open() {

        Point size = new Point(450, 180);
        shell.setSize(size);

        Display display = shell.getDisplay();
        Rectangle b;

        if (shell == getParent()) {
            b = display.getBounds();

        } else {
            b = getParent().getBounds();
        }

        shell.setLocation(b.x + (b.width - size.x)/2, b.y + (b.height - size.y)/2);

        shell.setText("License");
        shell.setImage(penroseImage);

        shell.open();

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }

        dispose();
    }

    public void createControl(final Shell parent) {
        try {
            Display display = parent.getDisplay();
            Color backgroundColor = display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);

            toolkit = new FormToolkit(display);
            //toolkit.setBackground(backgroundColor);

            boldFont = new Font(display, "Arial", 10, SWT.BOLD);

            parent.setLayout(new GridLayout());

            Label blank = new Label(parent, SWT.NONE);
            GridData gd = new GridData(GridData.FILL_HORIZONTAL);
            gd.heightHint = 25;
            blank.setLayoutData(gd);
/*
            Label titleLabel = new Label(parent, SWT.CENTER);
            titleLabel.setText(PenroseApplication.PRODUCT_NAME);
            titleLabel.setFont(boldFont);
            titleLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

            Label versionLabel = new Label(parent, SWT.CENTER);
            versionLabel.setText("Version "+ PenroseApplication.PRODUCT_VERSION);
            versionLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

            Label copyrightLabel = new Label(parent, SWT.CENTER);
            copyrightLabel.setText("Copyright (c) 2000-2006, Identyx Corporation.");
            copyrightLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
*/
            messageLabel = new Label(parent, SWT.CENTER);
            messageLabel.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_DARK_RED));
            messageLabel.setFont(boldFont);
            messageLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

            Link link = new Link(parent, SWT.CENTER);
            link.setText("To get an evaluation license please register at <a>http://www.identyx.com</a>.");
            gd = new GridData(GridData.FILL_HORIZONTAL);
            gd.horizontalAlignment = GridData.CENTER;
            link.setLayoutData(gd);

            link.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent selectionEvent) {
                    try {
                        String href = "http://www.identyx.com";
                        log.debug("Opening "+href);
                        Runtime.getRuntime().exec( new String[] {
                                "cmd.exe", "/c", "start", href
                        });
                    } catch (Exception e) {
                        log.error(e.getMessage());
                    }
                }
            });

            new Label(parent, SWT.NONE);

            Composite buttonsPanel = new Composite(parent, SWT.NONE);
            gd = new GridData(GridData.FILL_HORIZONTAL);
            gd.horizontalAlignment = SWT.CENTER;
            buttonsPanel.setLayoutData(gd);
            buttonsPanel.setLayout(new RowLayout());

            Button licenseButton = new Button(buttonsPanel, SWT.PUSH);
            licenseButton.setText("   Enter License Key   ");

            licenseButton.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent event) {
                    try {
/*
                        EnterLicenseKeyAction a = new EnterLicenseKeyAction();
                        a.run();
                        filename = a.getFilename();

                        String dir = System.getProperty("user.dir");

                        FileDialog dialog = new FileDialog(parent);
                        dialog.setText("License");
                        dialog.setFilterPath(dir);
                        dialog.setFilterExtensions(new String[] { "*.license", "*.*" });

                        filename = dialog.open();

                        if (filename == null) return;
*/
                        action = OK;
                        shell.close();
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                }
            });
/*
            Button okButton = new Button(buttonsPanel, SWT.PUSH);
            okButton.setText("   Community Edition   ");

            okButton.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent event) {
                    action = OK;
                    shell.close();
                }
            });
*/
            Button closeButton = new Button(buttonsPanel, SWT.PUSH);
            closeButton.setText("   Close   ");

            closeButton.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent event) {
                    action = CANCEL;
                    shell.close();
                }
            });

            licenseButton.setFocus();

        } catch (Exception e) {
            log.debug(e.getMessage(), e);
        }
	}

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public void setText(String text) {
        messageLabel.setText(text);
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
