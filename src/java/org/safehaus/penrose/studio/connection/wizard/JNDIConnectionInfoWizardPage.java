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
package org.safehaus.penrose.studio.connection.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.safehaus.penrose.ldap.LDAPClient;
import org.apache.log4j.Logger;

import javax.naming.Context;
import java.util.*;
import java.io.StringWriter;
import java.io.PrintWriter;

/**
 * @author Endi S. Dewata
 */
public class JNDIConnectionInfoWizardPage extends WizardPage implements ModifyListener {

    Logger log = Logger.getLogger(getClass());

    public final static String NAME = "Connection Info";

    Combo protocolCombo;

    Text hostText;
    Text portText;

    Combo suffixCombo;

    Text bindDnText;
    Text passwordText;

    public JNDIConnectionInfoWizardPage() {
        super(NAME);
        setDescription("Enter connection information.");
    }

    public void createControl(final Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        composite.setLayout(new GridLayout(4, false));

        Label protocolLabel = new Label(composite, SWT.NONE);
        protocolLabel.setText("Protocol:");

        protocolCombo = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
        protocolCombo.add("ldap");
        protocolCombo.add("ldaps");
        protocolCombo.select(0);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 3;
        protocolCombo.setLayoutData(gd);
        protocolCombo.addModifyListener(this);

        Label hostLabel = new Label(composite, SWT.NONE);
        hostLabel.setText("Host:");

        hostText = new Text(composite, SWT.BORDER);
        hostText.setText("localhost");
        gd = new GridData(GridData.FILL_HORIZONTAL);
        hostText.setLayoutData(gd);
        hostText.addModifyListener(this);

        Label portLabel = new Label(composite, SWT.NONE);
        portLabel.setText("Port:");
        gd = new GridData();
        gd.widthHint = 50;
        portLabel.setLayoutData(gd);

        portText = new Text(composite, SWT.BORDER);
        portText.setText("389");
        gd = new GridData();
        gd.widthHint = 50;
        portText.setLayoutData(gd);
        portText.addModifyListener(this);

        Label suffixLabel = new Label(composite, SWT.NONE);
        suffixLabel.setText("Suffix:");

        suffixCombo = new Combo(composite, SWT.BORDER);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        suffixCombo.setLayoutData(gd);

        suffixCombo.addModifyListener(this);

        Button fetchButton = new Button(composite, SWT.PUSH);
        fetchButton.setText("Fetch Base DNs");
        gd = new GridData();
        gd.horizontalSpan = 2;
        gd.widthHint = 120;
        fetchButton.setLayoutData(gd);

        fetchButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                try {
                    Map<String,String> properties = new HashMap<String,String>();
                    properties.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
                    properties.put(Context.PROVIDER_URL, getURL());
                    properties.put(Context.SECURITY_PRINCIPAL, bindDnText.getText());
                    properties.put(Context.SECURITY_CREDENTIALS, passwordText.getText());

                    LDAPClient client = new LDAPClient(properties);
                    Collection baseDns = client.getNamingContexts();

                    suffixCombo.removeAll();
                    for (Iterator i=baseDns.iterator(); i.hasNext(); ) {
                        String baseDn = (String)i.next();
                        suffixCombo.add(baseDn);
                    }
                    suffixCombo.select(0);

                } catch (Exception ex) {
                    log.debug(ex.getMessage(), ex);
                    MessageDialog.openError(parent.getShell(), "Failed to fetch base DNs", "Error: "+ex.getMessage());
                }
            }
        });

        Label bindDnLabel = new Label(composite, SWT.NONE);
        bindDnLabel.setText("Bind DN:");

        bindDnText = new Text(composite, SWT.BORDER);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 3;
        bindDnText.setLayoutData(gd);

        bindDnText.addModifyListener(this);

        Label passwordLabel = new Label(composite, SWT.NONE);
        passwordLabel.setText("Password:");

        passwordText = new Text(composite, SWT.BORDER );
		passwordText.setEchoChar('*');
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 3;
        passwordText.setLayoutData(gd);

        passwordText.addModifyListener(this);

        new Label(composite, SWT.NONE);

        Button testButton = new Button(composite, SWT.PUSH);
        testButton.setText("Test Connection");

        testButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                String url = getURL()+"/"+getSuffix();

                Map<String,String> properties = new HashMap<String,String>();
                properties.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
                properties.put(Context.PROVIDER_URL, url);
                properties.put(Context.SECURITY_PRINCIPAL, bindDnText.getText());
                properties.put(Context.SECURITY_CREDENTIALS, passwordText.getText());

                try {
                    LDAPClient client = new LDAPClient(properties);
                    client.open().close();
                    MessageDialog.openInformation(parent.getShell(), "Test Connection Result", "Connection successful!");

                } catch (Exception ex) {
                    log.debug(ex.getMessage(), ex);
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    ex.printStackTrace(pw);
                    String message = sw.toString();
                    if (message.length() > 500) {
                        message = message.substring(0, 500) + "...";
                    }
                    MessageDialog.openError(parent.getShell(), "Test Connection Result", "Error: "+message);
                }
            }
        });

        setPageComplete(validatePage());
    }

    public String getProtocol() {
        return protocolCombo.getText();
    }

    public String getHost() {
        return hostText.getText();
    }

    public int getPort() {
        if ("".equals(portText.getText().trim())) return 0;
        return Integer.parseInt(portText.getText());
    }

    public String getSuffix() {
        return suffixCombo.getText();
    }

    public String getBindDN() {
        return bindDnText.getText();
    }

    public String getPassword() {
        return passwordText.getText();
    }

    public String getURL() {
        String protocol = getProtocol();
        String host = getHost();
        int port = getPort();

        StringBuffer sb = new StringBuffer();
        sb.append(protocol);
        sb.append("://");
        sb.append(host);

        if (port != 0 &&
                ("ldap".equals(protocol) && 389 != port ||
                "ldaps".equals(protocol) && 636 != port)
        ) {
            sb.append(":");
            sb.append(port);
        }

        return sb.toString();
    }

    public boolean validatePage() {
        if ("".equals(getHost())) return false;
        if ("".equals(getSuffix())) return false;
        return true;
    }

    public void modifyText(ModifyEvent event) {
        setPageComplete(validatePage());
    }
}
