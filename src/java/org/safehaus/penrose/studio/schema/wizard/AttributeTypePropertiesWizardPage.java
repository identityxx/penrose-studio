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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.jface.wizard.WizardPage;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * @author Endi S. Dewata
 */
public class AttributeTypePropertiesWizardPage extends WizardPage {

    Logger log = Logger.getLogger(getClass());

    public final static String NAME = "Properties";

    Text oidText;
    Text namesText;
    Text descriptionText;

    Text superClassText;
    Text equalityText;
    Text orderingText;
    Text substringText;
    Text syntaxText;

    String oid;
    Collection<String> names = new ArrayList<String>();
    String description;

    String superClass;
    String equality;
    String ordering;
    String substring;
    String syntax;

    public AttributeTypePropertiesWizardPage() {
        super(NAME);
        setDescription("Enter the attribute type properties.");
    }

    public void createControl(Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        composite.setLayout(new GridLayout(2, false));

        Label oidLabel = new Label(composite, SWT.NONE);
        oidLabel.setText("OID:");
        GridData gd = new GridData();
        gd.widthHint = 100;
        oidLabel.setLayoutData(gd);

        oidText = new Text(composite, SWT.BORDER);
        oidText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        oidText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                oid = oidText.getText().trim();
                oid = "".equals(oid) ? null : oid;
            }
        });

        Label namesLabel = new Label(composite, SWT.NONE);
        namesLabel.setText("Names:");
        gd = new GridData();
        gd.widthHint = 100;
        namesLabel.setLayoutData(gd);

        namesText = new Text(composite, SWT.BORDER);
        namesText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        namesText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                names.clear();
                StringTokenizer st = new StringTokenizer(namesText.getText());
                while (st.hasMoreTokens()) {
                    names.add(st.nextToken());
                }
            }
        });

        Label descriptionLabel = new Label(composite, SWT.NONE);
        descriptionLabel.setText("Description:");
        gd = new GridData();
        gd.widthHint = 100;
        descriptionLabel.setLayoutData(gd);

        descriptionText = new Text(composite, SWT.BORDER);
        descriptionText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        descriptionText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                description = descriptionText.getText().trim();
                description = "".equals(description) ? null : description;
            }
        });

        Label separatorLabel = new Label(composite, SWT.NONE);
        gd = new GridData();
        gd.horizontalSpan = 2;
        separatorLabel.setLayoutData(gd);

        Label superClassLabel = new Label(composite, SWT.NONE);
        superClassLabel.setText("Super Class:");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        superClassLabel.setLayoutData(gd);

        superClassText = new Text(composite, SWT.BORDER);
        superClassText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        superClassText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                superClass = superClassText.getText().trim();
                superClass = "".equals(superClass) ? null : superClass;
            }
        });

        Label equalityLabel = new Label(composite, SWT.NONE);
        equalityLabel.setText("Equality:");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        equalityLabel.setLayoutData(gd);

        equalityText = new Text(composite, SWT.BORDER);
        equalityText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        equalityText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                equality = equalityText.getText().trim();
                equality = "".equals(equality) ? null : equality;
            }
        });

        Label orderingLabel = new Label(composite, SWT.NONE);
        orderingLabel.setText("Ordering:");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        orderingLabel.setLayoutData(gd);

        orderingText = new Text(composite, SWT.BORDER);
        orderingText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        orderingText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                ordering = orderingText.getText().trim();
                ordering = "".equals(ordering) ? null : ordering;
            }
        });

        Label substringLabel = new Label(composite, SWT.NONE);
        substringLabel.setText("Substring:");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        substringLabel.setLayoutData(gd);

        substringText = new Text(composite, SWT.BORDER);
        substringText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        substringText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                substring = substringText.getText().trim();
                substring = "".equals(substring) ? null : substring;
            }
        });

        Label syntaxLabel = new Label(composite, SWT.NONE);
        syntaxLabel.setText("Syntax:");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        syntaxLabel.setLayoutData(gd);

        syntaxText = new Text(composite, SWT.BORDER);
        syntaxText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        syntaxText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                syntax = syntaxText.getText().trim();
                syntax = "".equals(syntax) ? null : syntax;
            }
        });
    }

    public void setVisible(boolean b) {
        super.setVisible(b);
        if (b) refresh();
    }

    public void refresh() {

        oidText.setText(oid == null ? "" : oid);

        StringBuilder sb = new StringBuilder();
        for (String name : names) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(name);
        }
        namesText.setText(sb.toString());

        descriptionText.setText(description == null ? "" : description);

        superClassText.setText(superClass == null ? "" : superClass);
        equalityText.setText(equality == null ? "" : equality);
        orderingText.setText(ordering == null ? "" : ordering);
        substringText.setText(substring == null ? "" : substring);
        syntaxText.setText(syntax == null ? "" : syntax);
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public Collection<String> getNames() {
        return names;
    }

    public void setNames(Collection<String> names) {
        if (this.names == names) return;
        this.names.clear();
        this.names.addAll(names);
    }

    public String getAttributeTypeDescription() {
        return description;
    }

    public void setAttributeTypeDescription(String description) {
        this.description = description;
    }

    public String getSuperClass() {
        return superClass;
    }

    public void setSuperClass(String superClass) {
        this.superClass = superClass;
    }

    public String getEquality() {
        return equality;
    }

    public void setEquality(String equality) {
        this.equality = equality;
    }

    public String getOrdering() {
        return ordering;
    }

    public void setOrdering(String ordering) {
        this.ordering = ordering;
    }

    public String getSubstring() {
        return substring;
    }

    public void setSubstring(String substring) {
        this.substring = substring;
    }

    public String getSyntax() {
        return syntax;
    }

    public void setSyntax(String syntax) {
        this.syntax = syntax;
    }
}