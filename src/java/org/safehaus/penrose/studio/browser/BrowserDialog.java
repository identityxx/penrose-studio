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
package org.safehaus.penrose.studio.browser;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.widgets.*;
import org.safehaus.penrose.studio.PenroseStudioPlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.apache.log4j.Logger;

public class BrowserDialog extends Dialog {

    Logger log = Logger.getLogger(getClass());

    public final static int CANCEL = 0;
    public final static int SAVE   = 1;

    Shell shell;

	Text hostnameText;
    Text portText;
    Text baseDnText;

	Text bindDnText;
	Text bindPasswordText;

    private int action;

    String hostname;
    int port;
    String baseDn;
    String bindDn;
    String bindPassword;

	public BrowserDialog(Shell parent, int style) {
        super(parent, style);

        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);

        createControl(shell);
    }

    public void open() {

        Point size = new Point(400, 300);
        shell.setSize(size);

        Point l = getParent().getLocation();
        Point s = getParent().getSize();

        shell.setLocation(l.x + (s.x - size.x)/2, l.y + (s.y - size.y)/2);

        shell.setText("Connect");
        shell.setImage(PenroseStudioPlugin.getImage(PenroseImage.BROWSER));

        shell.open();

        Display display = getParent().getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
    }

    public void createControl(final Shell parent) {
        parent.setLayout(new GridLayout());

        Composite composite = createForm(parent);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        composite = createButtons(parent);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalAlignment = GridData.END;
        composite.setLayoutData(gd);
    }

    public Composite createForm(final Shell parent) {

        Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));

		Label urlLabel = new Label(composite, SWT.NONE);
        urlLabel.setText("Hostname:");

		hostnameText = new Text(composite, SWT.BORDER);
        hostnameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label portLabel = new Label(composite, SWT.NONE);
        portLabel.setText("Port:");

        portText = new Text(composite, SWT.BORDER);
        portText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label baseDnLabel = new Label(composite, SWT.NONE);
        baseDnLabel.setText("Base DN:");

        baseDnText = new Text(composite, SWT.BORDER);
        baseDnText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        new Label(composite, SWT.NONE);

        new Label(composite, SWT.NONE);

        Label authenticationLabel = new Label(composite, SWT.NONE);
        authenticationLabel.setText("Authentication:");
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        authenticationLabel.setLayoutData(gd);
        
		Label bindDnLabel = new Label(composite, SWT.NONE);
        bindDnLabel.setText("Bind DN:");

		bindDnText = new Text(composite, SWT.BORDER);
        bindDnText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label passwordLabel = new Label(composite, SWT.NONE);
        passwordLabel.setText("Password:");

		bindPasswordText = new Text(composite, SWT.BORDER | SWT.PASSWORD);
		
        bindPasswordText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        return composite;
    }

    public Composite createButtons(final Shell parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new RowLayout());

        Button saveButton = new Button(composite, SWT.PUSH);
        saveButton.setText("Open");

        saveButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                action = SAVE;

                hostname = hostnameText.getText();
                port = Integer.parseInt(portText.getText());
                baseDn = "".equals(baseDnText.getText()) ? null : baseDnText.getText();
                bindDn = "".equals(bindDnText.getText()) ? null : bindDnText.getText();
                bindPassword = "".equals(bindPasswordText.getText()) ? null : bindPasswordText.getText();

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

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public void setHostname(String hostname) {
        hostnameText.setText(hostname == null ? "" : hostname);
    }

    public String getHostname() {
        return hostname;
    }

    public void setPort(int port) {
        portText.setText(""+port);
    }

    public int getPort() {
        return port;
    }

    public void setBaseDn(String baseDn) {
        baseDnText.setText(baseDn == null ? "" : baseDn);
    }

    public String getBaseDn() {
        return baseDn;
    }

    public void setBindDn(String bindDn) {
        bindDnText.setText(bindDn == null ? "" : bindDn);
    }

    public String getBindDn() {
        return bindDn;
    }

    public void setBindPassword(String bindPassword) {
        bindPasswordText.setText(bindPassword == null ? "" : bindPassword);
    }

    public String getBindPassword() {
        return bindPassword;
    }
}
