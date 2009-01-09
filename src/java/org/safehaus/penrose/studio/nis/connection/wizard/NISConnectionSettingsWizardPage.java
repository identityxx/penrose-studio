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
package org.safehaus.penrose.studio.nis.connection.wizard;

import org.apache.log4j.Logger;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import javax.naming.Context;
import java.util.*;

/**
 * @author Endi S. Dewata
 */
public class NISConnectionSettingsWizardPage extends WizardPage implements ModifyListener {

    Logger log = Logger.getLogger(getClass());

    public final static String NAME = "NIS Connection Settings";

    private String hostname;
    private String domain;

    Text hostnameText;
    Text domainText;

    public NISConnectionSettingsWizardPage() {
        super(NAME);
        setDescription("Enter NIS connection settings.");
    }

    public void createControl(final Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        composite.setLayout(new GridLayout(2, false));

        Label bindDnLabel = new Label(composite, SWT.NONE);
        bindDnLabel.setText("Server:");

        hostnameText = new Text(composite, SWT.BORDER);
        hostnameText.setText(hostname == null ? "" : hostname);

        hostnameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        hostnameText.addModifyListener(this);

        Label passwordLabel = new Label(composite, SWT.NONE);
        passwordLabel.setText("Domain:");

        domainText = new Text(composite, SWT.BORDER);
        domainText.setText(domain == null ? "" : domain);

        domainText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        domainText.addModifyListener(this);

        setPageComplete(validatePage());
    }

    public String getHostname() {
        String s = hostnameText.getText();
        return "".equals(s) ? null : s;
    }

    public String getDomain() {
        String s = domainText.getText();
        return "".equals(s) ? null : s;
    }


    public String getURL() {
        String hostname = hostnameText.getText();
        String domain = domainText.getText();

        return "nis://" + hostname + "/" + domain;
    }

    public String[] parseURL(String url) {
        String s[] = new String[2];

        int i = url.indexOf("/", 6);
        s[0] = url.substring(6, i);
        s[1] = url.substring(i+1);

        return s;
    }

    public boolean validatePage() {
        return true;
    }

    public void modifyText(ModifyEvent event) {
        setPageComplete(validatePage());
    }

    public Map<String,String> getParameters() {
        Map<String,String> map = new LinkedHashMap<String,String>();

        map.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.nis.NISCtxFactory");
        map.put(Context.PROVIDER_URL, getURL());
        map.put("com.sun.jndi.nis.mailaliases", "nonull");
        map.put("method", "yp");

        return map;
    }

    public void setParameters(Map<String,String> parameters) {
        String url = parameters.get(Context.PROVIDER_URL);

        if (url != null) {
            String s[] = parseURL(url);
            setHostname(s[0]);
            setDomain(s[1]);
        }
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }
}