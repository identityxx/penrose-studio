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
package org.safehaus.penrose.studio.jndi.connection;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.List;
import org.safehaus.penrose.ldap.LDAPClient;
import org.safehaus.penrose.studio.dialog.ErrorDialog;

import javax.naming.Context;
import java.util.*;

/**
 * @author Endi S. Dewata
 */
public class JNDIConnectionInfoWizardPage extends WizardPage implements ModifyListener {

    Logger log = Logger.getLogger(getClass());

    public final static String NAME = "Connection Info";

    private Collection<String> urls = new ArrayList<String>();

    //private String protocol;
    //private String hostname;
    //private String port;
    //private String baseDn;

    private String suffix;
    private String bindDn;
    private String bindPassword;

    List urlList;

    //Combo protocolCombo;

    //Text hostText;
    //Text portText;

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

        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);

        Composite topControl = createTopControl(composite);
        topControl.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite bottomControl = createBottomControl(composite);
        bottomControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        setPageComplete(validatePage());
    }

    public Composite createTopControl(final Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));

        Label urlLabel = new Label(composite, SWT.NONE);
        urlLabel.setText("Servers:");

        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        urlLabel.setLayoutData(gd);

        urlList = new List(composite, SWT.BORDER | SWT.MULTI);

        for (String url : urls) {
            urlList.add(url);
        }
        
        gd = new GridData(GridData.FILL_BOTH);
        gd.horizontalSpan = 2;
        urlList.setLayoutData(gd);

        Composite leftButtons = new Composite(composite, SWT.NONE);
        leftButtons.setLayout(new RowLayout());

        gd = new GridData(GridData.FILL_HORIZONTAL);
        leftButtons.setLayoutData(gd);

        Button addButton = new Button(leftButtons, SWT.PUSH);
        addButton.setText("Add...");

        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {

                JNDIConnectionURLDialog dialog = new JNDIConnectionURLDialog(getShell(), SWT.NONE);
                dialog.setBindDn(getBindDN());
                dialog.setBindPassword(getPassword());
                dialog.open();

                int action = dialog.getAction();
                if (action == JNDIConnectionURLDialog.CANCEL) return;

                urlList.add(dialog.getURL());

                setPageComplete(validatePage());
            }
        });

        Composite rightButtons = new Composite(composite, SWT.NONE);
        rightButtons.setLayout(new RowLayout());

        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalAlignment = SWT.END;
        rightButtons.setLayoutData(gd);

        Button removeButton = new Button(rightButtons, SWT.PUSH);
        removeButton.setText("Remove");

        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                urlList.remove(urlList.getSelectionIndices());

                setPageComplete(validatePage());
            }
        });

        Button moveUpButton = new Button(rightButtons, SWT.PUSH);
        moveUpButton.setText("Move Up");

        moveUpButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                int i = urlList.getSelectionIndex();
                if (i == 0) return;

                String url = urlList.getItem(i);
                urlList.remove(i);
                urlList.add(url, i-1);
                urlList.setSelection(i-1);
            }
        });

        Button moveDownButton = new Button(rightButtons, SWT.PUSH);
        moveDownButton.setText("Move Down");

        moveDownButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                int i = urlList.getSelectionIndex();
                if (i == urlList.getItemCount()-1) return;

                String url = urlList.getItem(i);
                urlList.remove(i);
                urlList.add(url, i+1);
                urlList.setSelection(i+1);
            }
        });
/*
        Label protocolLabel = new Label(composite, SWT.NONE);
        protocolLabel.setText("URL:");

        protocolCombo = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
        protocolCombo.add("ldap");
        protocolCombo.add("ldaps");
        protocolCombo.setText(protocol);

        protocolCombo.setLayoutData(new GridData());
        protocolCombo.addModifyListener(this);

        Label hostLabel = new Label(composite, SWT.NONE);
        hostLabel.setText("://");

        hostText = new Text(composite, SWT.BORDER);
        hostText.setText(hostname);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        hostText.setLayoutData(gd);
        hostText.addModifyListener(this);

        Label portLabel = new Label(composite, SWT.NONE);
        portLabel.setText(":");
        portLabel.setLayoutData(new GridData());

        portText = new Text(composite, SWT.BORDER);
        portText.setText(port);
        gd = new GridData();
        gd.widthHint = 50;
        portText.setLayoutData(gd);
        portText.addModifyListener(this);

        Label suffixLabel = new Label(composite, SWT.NONE);
        suffixLabel.setText("Suffix:");

        suffixCombo = new Combo(composite, SWT.BORDER);
        if (baseDn != null) suffixCombo.setText(baseDn);

        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 3;
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
                    Collection<String> baseDns = client.getNamingContexts();

                    suffixCombo.removeAll();
                    for (String baseDn : baseDns) {
                        suffixCombo.add(baseDn);
                    }
                    suffixCombo.select(0);

                } catch (Exception ex) {
                    log.debug(ex.getMessage(), ex);
                    ErrorDialog.open(ex);
                }
            }
        });
*/
        return composite;
    }

    public Composite createBottomControl(final Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(3, false));

        Label suffixLabel = new Label(composite, SWT.NONE);
        suffixLabel.setText("Suffix:");

        suffixCombo = new Combo(composite, SWT.BORDER);
        suffixCombo.setText(suffix == null ? "" : suffix);
        suffixCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        suffixCombo.addModifyListener(this);

        Button fetchButton = new Button(composite, SWT.PUSH);
        fetchButton.setText("Fetch Base DNs");

        fetchButton.setLayoutData(new GridData());

        fetchButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                try {
                    Map<String,String> properties = new HashMap<String,String>();
                    properties.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
                    properties.put(Context.PROVIDER_URL, getProviderUrl());
                    properties.put(Context.SECURITY_PRINCIPAL, getBindDN());
                    properties.put(Context.SECURITY_CREDENTIALS, getPassword());

                    LDAPClient client = new LDAPClient(properties);
                    Collection<String> baseDns = client.getNamingContexts();
                    client.close();

                    suffixCombo.removeAll();
                    for (String baseDn : baseDns) {
                        suffixCombo.add(baseDn);
                    }
                    suffixCombo.select(0);

                } catch (Exception ex) {
                    log.debug(ex.getMessage(), ex);
                    ErrorDialog.open(ex);
                }
            }
        });

        Label bindDnLabel = new Label(composite, SWT.NONE);
        bindDnLabel.setText("Bind DN:");

        bindDnText = new Text(composite, SWT.BORDER);
        bindDnText.setText(bindDn);

        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        bindDnText.setLayoutData(gd);

        bindDnText.addModifyListener(this);

        Label passwordLabel = new Label(composite, SWT.NONE);
        passwordLabel.setText("Password:");

        passwordText = new Text(composite, SWT.BORDER  | SWT.PASSWORD);
        passwordText.setText(bindPassword);

        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        passwordText.setLayoutData(gd);

        passwordText.addModifyListener(this);

        new Label(composite, SWT.NONE);

        Button testButton = new Button(composite, SWT.PUSH);
        testButton.setText("Test Connection");

        testButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                try {
                    Map<String,String> properties = new HashMap<String,String>();
                    properties.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
                    properties.put(Context.PROVIDER_URL, getProviderUrl());
                    properties.put(Context.SECURITY_PRINCIPAL, getBindDN());
                    properties.put(Context.SECURITY_CREDENTIALS, getPassword());

                    LDAPClient client = new LDAPClient(properties);
                    client.connect();
                    client.close();

                    MessageDialog.openInformation(parent.getShell(), "Test Connection Result", "Connection successful!");

                } catch (Exception ex) {
                    log.debug(ex.getMessage(), ex);
                    ErrorDialog.open(ex);
                }
            }
        });

        return composite;
    }

    public String getProviderUrl() {
        StringBuilder sb = new StringBuilder();
        for (String url : urlList.getItems()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(url);
        }
        return sb.toString();
    }
/*
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
*/
    public String getSuffix() {
/*
        String url = getURL();
        int i = url.indexOf("://");
        int j = url.indexOf("/", i+3);
        return url.substring(j+1);
*/
        //return suffixCombo.getText();
        return suffixCombo.getText();
    }

    public String getBindDN() {
        return bindDnText.getText();
    }

    public String getPassword() {
        return passwordText.getText();
    }

    public String getURL() {
        if (urlList.getItemCount() == 0) return null;
        return urlList.getItem(0);
/*
        String protocol = getProtocol();
        String host = getHost();
        int port = getPort();

        StringBuilder sb = new StringBuilder();
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
*/
    }

    public boolean validatePage() {
        if (urlList.getItemCount() == 0) return false;
        return true;
    }

    public void modifyText(ModifyEvent event) {
        setPageComplete(validatePage());
    }

    public Map<String,String> getParameters() {
        Map<String,String> map = new HashMap<String,String>();

        map.put(Context.PROVIDER_URL, getProviderUrl());
        map.put(Context.SECURITY_PRINCIPAL, getBindDN());
        map.put(Context.SECURITY_CREDENTIALS, getPassword());

        return map;
    }

    public void setParameters(Map<String,String> parameters) {

        String providerUrl = parameters.get(Context.PROVIDER_URL);

        if (providerUrl != null) {
            StringTokenizer st = new StringTokenizer(providerUrl);
            while (st.hasMoreTokens()) {
                String url = st.nextToken();
                urls.add(url);
            }
        }
/*
        if (url == null) {
            protocol = "ldap";
            hostname = "localhost";
            port = "389";
            baseDn = "";

        } else {
            int i = url.indexOf("://");
            protocol = url.substring(0, i);

            int j = url.indexOf("/", i+3);
            String hostPort = url.substring(i+3, j);

            int k = hostPort.indexOf(":");
            if (k < 0) {
                hostname = hostPort;
                port = "389";
            } else {
                hostname = hostPort.substring(0, k);
                port = hostPort.substring(k+1);
            }

            baseDn = url.substring(j+1);
        }
*/
        bindDn = parameters.get(Context.SECURITY_PRINCIPAL);
        if (bindDn == null) bindDn = "";

        bindPassword = parameters.get(Context.SECURITY_CREDENTIALS);
        if (bindPassword == null) bindPassword = "";

    }
/*
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public void setBaseDn(String baseDn) {
        this.baseDn = baseDn;
    }
*/
    public void setBindDn(String bindDn) {
        this.bindDn = bindDn;
    }

    public void setBindPassword(String bindPassword) {
        this.bindPassword = bindPassword;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }
}
