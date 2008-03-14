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
package org.safehaus.penrose.studio.schema.wizard;

import org.apache.log4j.Logger;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.safehaus.penrose.connection.ConnectionConfig;
import org.safehaus.penrose.ldap.Attribute;
import org.safehaus.penrose.ldap.LDAP;
import org.safehaus.penrose.ldap.LDAPClient;
import org.safehaus.penrose.ldap.SearchResult;

/**
 * @author Endi S. Dewata
 */
public class SelectSchemaWizardPage extends WizardPage {

    Logger log = Logger.getLogger(getClass());

    public final static String AD_SCHEMA   = "Active Directory";
    public final static String LDAP_SCHEMA = "LDAP";

    public final static String NAME = "Schema";

    List sourceSchemaList;
    Text destSchemaText;

    Button originalDnButton;
    Button newDnButton;

    Button adFormatButton;
    Button ldapFormatButton;

    private ConnectionConfig connectionConfig;

    public SelectSchemaWizardPage() {
        super(NAME);

        setDescription("Select schema.");
    }

    public void createControl(final Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        composite.setLayout(new GridLayout());

        Label sourceSchemaLabel = new Label(composite, SWT.NONE);
        sourceSchemaLabel.setText("Source Schema:");

        sourceSchemaList = new List(composite, SWT.BORDER);
        sourceSchemaList.setLayoutData(new GridData(GridData.FILL_BOTH));

        sourceSchemaList.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                String schemaDn = sourceSchemaList.getSelection()[0];
                destSchemaText.setText(schemaDn);
            }
        });

        new Label(composite, SWT.NONE);

        Composite destinationSchemaComposite = new Composite(composite, SWT.NONE);
        destinationSchemaComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        destinationSchemaComposite.setLayout(new GridLayout(3, false));

        Label destinationSchemaLabel = new Label(destinationSchemaComposite, SWT.NONE);
        GridData gd = new GridData();
        gd.widthHint = 100;
        destinationSchemaLabel.setLayoutData(gd);
        destinationSchemaLabel.setText("Destination DN:");

        originalDnButton = new Button(destinationSchemaComposite, SWT.RADIO);
        originalDnButton.setText("Same as source schema DN");
        originalDnButton.setSelection(true);
        gd = new GridData();
        gd.horizontalSpan = 2;
        originalDnButton.setLayoutData(gd);

        new Label(destinationSchemaComposite, SWT.NONE);

        newDnButton = new Button(destinationSchemaComposite, SWT.RADIO);

        destSchemaText = new Text(destinationSchemaComposite, SWT.BORDER);
        destSchemaText.setText(LDAP.SCHEMA_DN.toString());
        destSchemaText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        new Label(composite, SWT.NONE);

        Composite schemaFormatComposite = new Composite(composite, SWT.NONE);
        schemaFormatComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        schemaFormatComposite.setLayout(new GridLayout(2, false));

        Label formatLabel = new Label(schemaFormatComposite, SWT.NONE);
        formatLabel.setText("Schema Format:");
        gd = new GridData();
        gd.widthHint = 100;
        formatLabel.setLayoutData(gd);

        adFormatButton = new Button(schemaFormatComposite, SWT.RADIO);
        adFormatButton.setText("Active Directory");
        adFormatButton.setSelection(true);

        new Label(schemaFormatComposite, SWT.NONE);

        ldapFormatButton = new Button(schemaFormatComposite, SWT.RADIO);
        ldapFormatButton.setText("LDAP");
    }

    public void setVisible(boolean visible) {
        super.setVisible(visible);

        if (visible) refresh();
    }

    public void refresh() {
        sourceSchemaList.removeAll();
        if (connectionConfig == null) return;

        LDAPClient client = null;
        try {
            client = new LDAPClient(connectionConfig.getParameters());
            SearchResult rootDse = client.getRootDSE();

            Attribute schemaNamingContexts = rootDse.getAttributes().get("schemaNamingContext");
            if (schemaNamingContexts != null) {
                for (Object value : schemaNamingContexts.getValues()) {
                    String schemaNamingContext = (String)value;
                    sourceSchemaList.add(schemaNamingContext);
                }
            }

            sourceSchemaList.setSelection(0);
/*
            LDAPAttribute subschemaSubentries = rootDse.getAttribute("subschemaSubentry");
            if (subschemaSubentries != null) {
                for (Enumeration e = subschemaSubentries.getStringValues(); e.hasMoreElements(); ) {
                    String schemaNamingContext = (String)e.nextElement();

                    sourceSchemaList.add(schemaNamingContext);
                }
            }
*/
        } catch (Exception e) {
            log.error(e.getMessage(), e);

        } finally {
            if (client != null) try { client.close(); } catch (Exception e) { log.error(e.getMessage(), e); }
        }
    }

    public boolean validatePage() {
        return sourceSchemaList.getSelectionCount() != 0;
    }

    public String getSourceSchemaDn() {
        if (sourceSchemaList.getSelectionCount() == 0) return null;
        return sourceSchemaList.getSelection()[0];
    }

    public String getDestSchemaDn() {
        if (originalDnButton.getSelection()) {
            return getSourceSchemaDn();
        } else {
            return destSchemaText.getText();
        }
    }

    public String getSchemaFormat() {
        return adFormatButton.getSelection() ? AD_SCHEMA : LDAP_SCHEMA;
    }

    public void setConnectionConfig(ConnectionConfig connectionConfig) {
        this.connectionConfig = connectionConfig;
    }

    public ConnectionConfig getConnectionConfig() {
        return connectionConfig;
    }
}
