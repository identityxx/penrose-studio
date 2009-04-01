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
package org.safehaus.penrose.studio.ldap.connection.wizard;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.List;
import org.safehaus.penrose.ldap.*;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.studio.ldap.connection.LDAPConnectionURLDialog;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.partition.PartitionConfig;
import org.safehaus.penrose.connection.ConnectionManagerClient;
import org.safehaus.penrose.connection.ConnectionConfig;
import org.ietf.ldap.*;

import javax.naming.Context;
import java.util.*;

/**
 * @author Endi S. Dewata
 */
public class LDAPConnectionSettingsWizardPage extends WizardPage implements ModifyListener {

    Logger log = Logger.getLogger(getClass());

    public final static String NAME = "LDAP Connection Settings";

    private ArrayList<String> urls = new ArrayList<String>();

    private Server server;
    private String suffix;
    private String bindDn;
    private String bindPassword;

    List urlList;

    Combo suffixCombo;
    Text bindDnText;
    Text bindPasswordText;

    public LDAPConnectionSettingsWizardPage() {
        super(NAME);
        setDescription("Enter LDAP connection settings.");
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

                LDAPConnectionURLDialog dialog = new LDAPConnectionURLDialog(getShell(), SWT.NONE);
                dialog.setBindDn(getBindDn());
                dialog.setBindPassword(getBindPassword());
                dialog.open();

                int action = dialog.getAction();
                if (action == LDAPConnectionURLDialog.CANCEL) return;

                urls.add(dialog.getURL());
                updateUrl();

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
                for (String url : urlList.getSelection()) {
                    urls.remove(url);
                }
                updateUrl();

                setPageComplete(validatePage());
            }
        });

        Button moveUpButton = new Button(rightButtons, SWT.PUSH);
        moveUpButton.setText("Move Up");

        moveUpButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                int i = urlList.getSelectionIndex();
                if (i == 0) return;

                String url = urls.get(i);
                urls.remove(i);
                urls.add(i-1, url);

                updateUrl();

                urlList.setSelection(i-1);
            }
        });

        Button moveDownButton = new Button(rightButtons, SWT.PUSH);
        moveDownButton.setText("Move Down");

        moveDownButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                int i = urlList.getSelectionIndex();
                if (i == urls.size()-1) return;

                String url = urls.get(i);
                urls.remove(i);
                urls.add(i+1, url);

                updateUrl();

                urlList.setSelection(i+1);
            }
        });

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

        suffixCombo.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                suffix = suffixCombo.getText().trim();
                suffix = "".equals(suffix) ? null : suffix;
            }
        });

        suffixCombo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                suffix = suffixCombo.getText().trim();
                suffix = "".equals(suffix) ? null : suffix;
            }
        });

        Button fetchButton = new Button(composite, SWT.PUSH);
        fetchButton.setText("Fetch Base DNs");

        fetchButton.setLayoutData(new GridData());

        fetchButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    suffixCombo.removeAll();

                    for (String dn : fetchSuffixes()) {
                        suffixCombo.add(dn);
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
        bindDnText.setText(bindDn == null ? "" : bindDn);

        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        bindDnText.setLayoutData(gd);

        bindDnText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                bindDn = bindDnText.getText().trim();
                bindDn = "".equals(bindDn) ? null : bindDn;
            }
        });

        Label passwordLabel = new Label(composite, SWT.NONE);
        passwordLabel.setText("Password:");

        bindPasswordText = new Text(composite, SWT.BORDER  | SWT.PASSWORD);
        bindPasswordText.setText(bindPassword == null ? "" : bindPassword);

        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        bindPasswordText.setLayoutData(gd);

        bindPasswordText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                bindPassword = bindPasswordText.getText().trim();
                bindPassword = "".equals(bindPassword) ? null : bindPassword;
            }
        });

        new Label(composite, SWT.NONE);

        Button testButton = new Button(composite, SWT.PUSH);
        testButton.setText("Test Connection");

        testButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    testConnection();

                    MessageDialog.openInformation(parent.getShell(), "Test Connection Result", "Connection successful!");

                } catch (Exception ex) {
                    log.error(ex.getMessage(), ex);
                    ErrorDialog.open(ex.getMessage());
                }
            }
        });

        return composite;
    }

    public void connect(LDAPConnection connection) throws Exception {

        Exception exception = null;

        for (String url : urls) {
            log.debug("Connecting to "+url);
            try {
                LDAPUrl ldapUrl = new LDAPUrl(url);
                connection.connect(ldapUrl.getHost(), ldapUrl.getPort());
                exception = null;
                break;

            } catch (Exception e) {
                exception = e;
            }
        }

        if (exception != null) throw exception;

        if (bindDn != null && bindPassword != null) {
            connection.bind(3, bindDn, bindPassword.getBytes());
        }
    }

    public Collection<String> fetchSuffixes() throws Exception {

        Collection<String> list = new ArrayList<String>();

        if (server == null) {

            LDAPConnection connection = new LDAPConnection();

            try {
                connect(connection);

                LDAPSearchResults sr = connection.search(
                        "",
                        LDAPConnection.SCOPE_BASE,
                        "(objectClass=*)",
                        new String[] { "*", "+" },
                        false
                );

                LDAPEntry rootDse = sr.next();

                LDAPAttribute namingContexts = rootDse.getAttribute("namingContexts");

                if (namingContexts != null) {
                    for (Enumeration e = namingContexts.getStringValues(); e.hasMoreElements(); ) {
                        String dn = (String)e.nextElement();
                        list.add(dn);
                    }
                }

            } finally {
                connection.disconnect();
            }

        } else {
            PenroseClient client = server.getClient();
            PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
            PartitionClient partitionClient = partitionManagerClient.getPartitionClient(PartitionConfig.ROOT);
            ConnectionManagerClient connectionManagerClient = partitionClient.getConnectionManagerClient();

            ConnectionConfig connectionConfig = new ConnectionConfig();
            connectionConfig.setName("Test");
            connectionConfig.setAdapterName("LDAP");

            Map<String,String> parameters = new HashMap<String,String>();
            parameters.put(Context.PROVIDER_URL, getProviderUrl());
            parameters.put(Context.SECURITY_PRINCIPAL, getBindDn());
            parameters.put(Context.SECURITY_CREDENTIALS, getBindPassword());

            connectionConfig.setParameters(parameters);

            for (DN dn : connectionManagerClient.getNamingContexts(connectionConfig)) {
                list.add(dn.toString());
            }
        }

        return list;
    }

    public void testConnection() throws Exception {
        if (server == null) {

            LDAPConnection connection = new LDAPConnection();

            try {
                connect(connection);

            } finally {
                connection.disconnect();
            }

        } else {
            PenroseClient client = server.getClient();
            PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
            PartitionClient partitionClient = partitionManagerClient.getPartitionClient(PartitionConfig.ROOT);
            ConnectionManagerClient connectionManagerClient = partitionClient.getConnectionManagerClient();

            ConnectionConfig connectionConfig = new ConnectionConfig();
            connectionConfig.setName("Test");
            connectionConfig.setAdapterName("LDAP");

            Map<String,String> parameters = new HashMap<String,String>();
            parameters.put(Context.PROVIDER_URL, getProviderUrl());
            parameters.put(Context.SECURITY_PRINCIPAL, getBindDn());
            parameters.put(Context.SECURITY_CREDENTIALS, getBindPassword());

            connectionConfig.setParameters(parameters);

            connectionManagerClient.validateConnection(connectionConfig);
        }
    }

    public String getProviderUrl() {

        if (urlList.getItemCount() == 0) return null;

        StringBuilder sb = new StringBuilder();
        for (String url : urlList.getItems()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(url);
        }

        return sb.toString();
    }

    public void updateUrl() {
        urlList.removeAll();
        for (String url : urls) urlList.add(url);
    }

    public String getSuffix() {
        return suffix;
    }

    public String getBindDn() {
        return bindDn;
    }

    public String getBindPassword() {
        return bindPassword;
    }

    public String getURL() {
        if (urlList.getItemCount() == 0) return null;
        return urlList.getItem(0);
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
        map.put(Context.SECURITY_PRINCIPAL, getBindDn());
        map.put(Context.SECURITY_CREDENTIALS, getBindPassword());

        return map;
    }

    public void setParameters(Map<String,String> parameters) {
        setProviderUrl(parameters.get(Context.PROVIDER_URL));
        setBindDn(parameters.get(Context.SECURITY_PRINCIPAL));
        setBindPassword(parameters.get(Context.SECURITY_CREDENTIALS));
    }

    public void setProviderUrl(String providerUrl) {
        if (providerUrl == null) return;

        StringTokenizer st = new StringTokenizer(providerUrl);
        while (st.hasMoreTokens()) {
            String url = st.nextToken();
            urls.add(url);
        }
    }

    public void setBindDn(String bindDn) {
        this.bindDn = bindDn;
    }

    public void setBindPassword(String bindPassword) {
        this.bindPassword = bindPassword;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }
}
