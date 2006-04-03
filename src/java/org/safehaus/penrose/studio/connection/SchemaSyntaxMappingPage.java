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
package org.safehaus.penrose.studio.connection;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.safehaus.penrose.schema.Schema;
import org.safehaus.penrose.schema.AttributeType;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * @author Endi S. Dewata
 */
public class SchemaSyntaxMappingPage extends WizardPage implements ModifyListener {

    Logger log = Logger.getLogger(getClass());

    public final static String NAME = "Schema Syntax Mapping";

    Table table;

    private Map syntaxMapping = new LinkedHashMap();
    Schema schema;

    public SchemaSyntaxMappingPage(Schema schema) {
        super(NAME);

        this.schema = schema;

        // Object(DN-DN) => DN
        syntaxMapping.put("2.5.5.1", "1.3.6.1.4.1.1466.115.121.1.12");

        // String(Object-Identifier) => OID
        syntaxMapping.put("2.5.5.2", "1.3.6.1.4.1.1466.115.121.1.38");

        // Case-Sensitive String => Directory String
        syntaxMapping.put("2.5.5.3", "1.3.6.1.4.1.1466.115.121.1.15");

        // CaseIgnoreString(Teletex) => Directory String
        syntaxMapping.put("2.5.5.4", "1.3.6.1.4.1.1466.115.121.1.15");

        // String(Printable), String(IA5) => IA5 String
        syntaxMapping.put("2.5.5.5", "1.3.6.1.4.1.1466.115.121.1.26");

        // String(Numeric) => Numeric String
        syntaxMapping.put("2.5.5.6", "1.3.6.1.4.1.1466.115.121.1.36");

        // Object(DN-Binary) => Binary
        syntaxMapping.put("2.5.5.7", "1.3.6.1.4.1.1466.115.121.1.5");

        // Boolean => Boolean
        syntaxMapping.put("2.5.5.8", "1.3.6.1.4.1.1466.115.121.1.7");

        // Integer => INTEGER
        syntaxMapping.put("2.5.5.9", "1.3.6.1.4.1.1466.115.121.1.27");

        // String(Octet) => Binary
        syntaxMapping.put("2.5.5.10", "1.3.6.1.4.1.1466.115.121.1.5");

        // String(Generalized-Time) => Generalized Time
        syntaxMapping.put("2.5.5.11", "1.3.6.1.4.1.1466.115.121.1.24");

        // String(Unicode) => Directory String
        syntaxMapping.put("2.5.5.12", "1.3.6.1.4.1.1466.115.121.1.15");

        // Object(Presentation-Address) => Presentation Address
        syntaxMapping.put("2.5.5.13", "1.3.6.1.4.1.1466.115.121.1.43");

        // Object(DN-String) => Binary
        syntaxMapping.put("2.5.5.14", "1.3.6.1.4.1.1466.115.121.1.5");

        // String(NT-Sec-Desc) => Binary
        syntaxMapping.put("2.5.5.15", "1.3.6.1.4.1.1466.115.121.1.5");

        // LargeInteger => INTEGER
        syntaxMapping.put("2.5.5.16", "1.3.6.1.4.1.1466.115.121.1.27");

        // SID => Binary
        syntaxMapping.put("2.5.5.17", "1.3.6.1.4.1.1466.115.121.1.5");

        setDescription("Specify syntax mapping.");
    }

    public void createControl(Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        composite.setLayout(new GridLayout(1, false));

        table = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        table.setLayoutData(new GridData(GridData.FILL_BOTH));

        TableColumn tc = new TableColumn(table, SWT.LEFT);
        tc.setText("Old Syntax");
        tc.setWidth(250);

        tc = new TableColumn(table, SWT.LEFT);
        tc.setText("New Syntax");
        tc.setWidth(250);

        for (Iterator i=syntaxMapping.keySet().iterator(); i.hasNext(); ) {
            String oldSyntax = (String)i.next();
            String newSyntax = (String)syntaxMapping.get(oldSyntax);

            TableItem ti = new TableItem(table, SWT.NONE);
            ti.setText(0, oldSyntax);
            ti.setText(1, newSyntax);
        }

        setPageComplete(validatePage());
    }

    public boolean validatePage() {
        return true;
    }

    public void modifyText(ModifyEvent event) {
        setPageComplete(validatePage());
    }

    public Map getSyntaxMapping() {
        return syntaxMapping;
    }

    public void setSyntaxMapping(Map syntaxMapping) {
        this.syntaxMapping = syntaxMapping;
    }
}
