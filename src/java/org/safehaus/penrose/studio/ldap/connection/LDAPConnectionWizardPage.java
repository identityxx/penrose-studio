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
package org.safehaus.penrose.studio.ldap.connection;

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
import org.safehaus.penrose.ldap.*;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.studio.jndi.connection.JNDIConnectionURLDialog;

import javax.naming.Context;
import java.util.*;

/**
 * @author Endi S. Dewata
 */
public class LDAPConnectionWizardPage extends WizardPage implements ModifyListener {

    Logger log = Logger.getLogger(getClass());

    public final static String NAME = "Connection Info";

    private Collection<String> urls = new ArrayList<String>();

    private String suffix;
    private String bindDn;
    private String bindPassword;

    List urlList;

    Combo suffixCombo;
    Text bindDnText;
    Text bindPasswordText;

    public LDAPConnectionWizardPage() {
        super(NAME);
        setDescription("Enter LDAP connection parameters.");
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
                dialog.setBindDn(getBindDn());
                dialog.setBindPassword(getBindPassword());
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
            public void widgetSelected(SelectionEvent event) {
                LDAPClient client = null;
                try {
                    Map<String,String> properties = new HashMap<String,String>();
                    properties.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
                    properties.put(Context.PROVIDER_URL, getProviderUrl());
                    properties.put(Context.SECURITY_PRINCIPAL, getBindDn());
                    properties.put(Context.SECURITY_CREDENTIALS, getBindPassword());

                    client = new LDAPClient(properties);

                    suffixCombo.removeAll();

                    SearchResult rootDse = client.getRootDSE();

                    Attribute namingContexts = rootDse.getAttributes().get("namingContexts");
                    if (namingContexts != null) {
                        for (Object value : namingContexts.getValues()) {
                            String namingContext = (String)value;
                            suffixCombo.add(namingContext);
                        }
                    }

                    suffixCombo.select(0);

                } catch (Exception ex) {
                    log.debug(ex.getMessage(), ex);
                    ErrorDialog.open(ex);

                } finally {
                    if (client != null) try { client.close(); } catch (Exception e) { log.error(e.getMessage(), e); }
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

        bindDnText.addModifyListener(this);

        Label passwordLabel = new Label(composite, SWT.NONE);
        passwordLabel.setText("Password:");

        bindPasswordText = new Text(composite, SWT.BORDER  | SWT.PASSWORD);
        bindPasswordText.setText(bindPassword == null ? "" : bindPassword);

        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        bindPasswordText.setLayoutData(gd);

        bindPasswordText.addModifyListener(this);

        new Label(composite, SWT.NONE);

        Button testButton = new Button(composite, SWT.PUSH);
        testButton.setText("Test Connection");

        testButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {

                LDAPClient client = null;

                try {
                    String providerUrl = getProviderUrl();
                    String bindDn = getBindDn();
                    byte[] bindPassword = getBindPassword().getBytes("UTF-8");

                    BindRequest request = new BindRequest();
                    request.setDn(bindDn);
                    request.setPassword(bindPassword);

                    BindResponse response = new BindResponse();

                    client = new LDAPClient(providerUrl);
                    client.bind(request, response);

                    MessageDialog.openInformation(parent.getShell(), "Test Connection Result", "Connection successful!");

                } catch (Exception ex) {
                    log.error(ex.getMessage(), ex);
                    ErrorDialog.open(ex);

                } finally {
                    if (client != null) try { client.close(); } catch (Exception e) { log.error(e.getMessage(), e); }
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

    public String getSuffix() {
        return suffixCombo.getText();
    }

    public String getBindDn() {
        return bindDnText.getText();
    }

    public String getBindPassword() {
        return bindPasswordText.getText();
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
}
