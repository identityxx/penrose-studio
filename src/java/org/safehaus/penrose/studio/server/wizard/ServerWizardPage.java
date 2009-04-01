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
package org.safehaus.penrose.studio.server.wizard;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.widgets.*;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class ServerWizardPage extends WizardPage {

    Logger log = LoggerFactory.getLogger(getClass());

    public final static String NAME = "Server Properties";

	Text nameText;
    Combo typeCombo;
	Text hostText;
    Text portText;
	Text bindDnText;
	Text bindPasswordText;

	private String name;
    private String type;
    private String host;
    private String port;
    private String bindDn;
    private String bindPassword;

    public ServerWizardPage() {
        super(NAME);
        setDescription("Enter server properties.");
    }

    public void createControl(final Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        composite.setLayout(new GridLayout(2, false));

		Label nameLabel = new Label(composite, SWT.NONE);
        nameLabel.setText("Name:");

		nameText = new Text(composite, SWT.BORDER);
		nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label typeLabel = new Label(composite, SWT.NONE);
        typeLabel.setText("Type:");

        typeCombo = new Combo(composite, SWT.READ_ONLY);
        typeCombo.add(PenroseClient.PENROSE);
        typeCombo.add(PenroseClient.JBOSS);
        typeCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label urlLabel = new Label(composite, SWT.NONE);
        urlLabel.setText("Host:");

		hostText = new Text(composite, SWT.BORDER);
        hostText.setText("localhost");
        hostText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label portLabel = new Label(composite, SWT.NONE);
        portLabel.setText("Port:");

        portText = new Text(composite, SWT.BORDER);
        portText.setText("1099");
        portText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label usernameLabel = new Label(composite, SWT.NONE);
        usernameLabel.setText("Bind DN:");

		bindDnText = new Text(composite, SWT.BORDER);
        bindDnText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label passwordLabel = new Label(composite, SWT.NONE);
        passwordLabel.setText("Password:");

		bindPasswordText = new Text(composite, SWT.BORDER | SWT.PASSWORD);
		bindPasswordText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		new Label(composite, SWT.NONE);

		Button testButton = new Button(composite, SWT.PUSH);
        testButton.setText("Test Connection");

		testButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				try {
					PenroseClient client = new PenroseClient(
                            typeCombo.getText(),
                            hostText.getText(),
                            "".equals(portText.getText()) ? 0 : Integer.parseInt(portText.getText()),
                            bindDnText.getText(),
                            bindPasswordText.getText());
					client.connect();
                    client.close();
					MessageDialog.openInformation(parent.getShell(), "Test Connection Result", "Connect Successful!");

				} catch (Exception ex) {
                    log.debug(ex.getMessage(), ex);
					ErrorDialog.open(ex.getMessage());
				}
			}
		});

        nameText.setText(name == null ? "" : name);
        typeCombo.setText(type == null ? PenroseClient.PENROSE : type);
        hostText.setText(host == null ? "localhost" : host);
        portText.setText(port == null ? "" : port);
        bindDnText.setText(bindDn == null ? "" : bindDn);
        bindPasswordText.setText(bindPassword == null ? "" : bindPassword);

        setPageComplete(validatePage());
    }

    public boolean validatePage() {
        if (getServerName() == null) return false;
        if (getType() == null) return false;
        if (getHost() == null) return false;
        if (getPort() == null) return false;
        if (getBindDn() == null) return false;
        if (getBindPassword() == null) return false;
        return true;
    }

    public String getServerName() {
        String s = nameText.getText().trim();
        return "".equals(s) ? null : s;
    }

    public void setServerName(String name) {
        this.name = name;
    }

    public String getType() {
        String s = typeCombo.getText().trim();
        return "".equals(s) ? null : s;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getHost() {
        String s = hostText.getText().trim();
        return "".equals(s) ? null : s;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        String s = portText.getText().trim();
        return "".equals(s) ? null : s;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getBindDn() {
        String s = bindDnText.getText().trim();
        return "".equals(s) ? null : s;
    }

    public void setBindDn(String bindDn) {
        this.bindDn = bindDn;
    }

    public String getBindPassword() {
        String s = bindPasswordText.getText().trim();
        return "".equals(s) ? null : s;
    }

    public void setBindPassword(String bindPassword) {
        this.bindPassword = bindPassword;
    }
}