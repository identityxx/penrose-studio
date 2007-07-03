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
package org.safehaus.penrose.studio.schema;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.schema.AttributeType;

import java.util.*;

/**
 * @author Endi S. Dewata
 */
public class AttributeTypeDialog extends Dialog {

    public final static int CANCEL = 0;
    public final static int OK     = 1;

    Shell shell;

    Text oidText;
    Text namesText;
    Text descriptionText;
    Combo usageCombo;
    Text superClassText;

    Text equalityText;
    Text orderingText;
    Text substringText;
    Text syntaxText;

    Button singleValuedCheckbox;
    Button collectiveCheckbox;
    Button modifiableCheckbox;
    Button obsoleteCheckbox;

    AttributeType attributeType;

    int action;

	public AttributeTypeDialog(Shell parent, int style) {
		super(parent, style);
        setText("Attribute Type");

        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);

        createControl(shell);
    }

    public void open () {

        Point size = new Point(500, 400);
        shell.setSize(size);

        Point l = getParent().getLocation();
        Point s = getParent().getSize();

        shell.setLocation(l.x + (s.x - size.x)/2, l.y + (s.y - size.y)/2);

        shell.setText(getText());
        shell.setImage(PenrosePlugin.getImage(PenroseImage.LOGO16));
        shell.open();

        Display display = getParent().getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
    }

    public void createControl(final Shell parent) {
        parent.setLayout(new GridLayout());

        TabFolder tabFolder = new TabFolder(parent, SWT.NONE);
        tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite selectorPage = createMainPage(tabFolder);
        selectorPage.setLayoutData(new GridData(GridData.FILL_BOTH));

        TabItem selectorTab = new TabItem(tabFolder, SWT.NONE);
        selectorTab.setText("General");
        selectorTab.setControl(selectorPage);

        Composite propertiesPage = createPropertiesPage(tabFolder);
        propertiesPage.setLayoutData(new GridData(GridData.FILL_BOTH));

        TabItem propertiesTab = new TabItem(tabFolder, SWT.NONE);
        propertiesTab.setText("Properties");
        propertiesTab.setControl(propertiesPage);

        Composite buttons = getButtons(parent);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalAlignment = GridData.END;
        buttons.setLayoutData(gd);
    }

    public Composite getButtons(Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new RowLayout());

        Button saveButton = new Button(composite, SWT.PUSH);
        saveButton.setText("Save");

        saveButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                save();

                action = OK;
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

    public Composite createMainPage(Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
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
            }
        });

        return composite;
    }

    public Composite createPropertiesPage(Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));

        Label usageLabel = new Label(composite, SWT.NONE);
        usageLabel.setText("Usage:");
        GridData gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        usageLabel.setLayoutData(gd);

        usageCombo = new Combo(composite, SWT.READ_ONLY);
        usageCombo.add(AttributeType.USER_APPLICATIONS);
        usageCombo.add(AttributeType.DIRECTORY_OPERATION);
        usageCombo.add(AttributeType.DISTRIBUTED_OPERATION);
        usageCombo.add(AttributeType.DSA_OPERATION);
        usageCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label singleValuedLabel = new Label(composite, SWT.NONE);
        singleValuedLabel.setText("Single-valued:");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        singleValuedLabel.setLayoutData(gd);

        singleValuedCheckbox = new Button(composite, SWT.CHECK);
        singleValuedCheckbox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        singleValuedCheckbox.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
            }
        });

        Label collectiveLabel = new Label(composite, SWT.NONE);
        collectiveLabel.setText("Collective:");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        collectiveLabel.setLayoutData(gd);

        collectiveCheckbox = new Button(composite, SWT.CHECK);
        collectiveCheckbox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        collectiveCheckbox.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
            }
        });

        Label modifiableLabel = new Label(composite, SWT.NONE);
        modifiableLabel.setText("Modifiable:");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        modifiableLabel.setLayoutData(gd);

        modifiableCheckbox = new Button(composite, SWT.CHECK);
        modifiableCheckbox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        modifiableCheckbox.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
            }
        });

        Label obsoleteLabel = new Label(composite, SWT.NONE);
        obsoleteLabel.setText("Obsolete:");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        obsoleteLabel.setLayoutData(gd);

        obsoleteCheckbox = new Button(composite, SWT.CHECK);
        obsoleteCheckbox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        obsoleteCheckbox.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
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

    public void save()  {
        attributeType.setOid("".equals(oidText.getText()) ? null : oidText.getText());

        attributeType.removeNames();
        StringTokenizer st = new StringTokenizer(namesText.getText());
        while (st.hasMoreTokens()) {
            attributeType.addName(st.nextToken());
        }

        attributeType.setDescription("".equals(descriptionText.getText()) ? null : descriptionText.getText());

        attributeType.setUsage(usageCombo.getText());

        attributeType.setSuperClass("".equals(superClassText.getText()) ? null : superClassText.getText());
        attributeType.setEquality("".equals(equalityText.getText()) ? null : equalityText.getText());
        attributeType.setOrdering("".equals(orderingText.getText()) ? null : orderingText.getText());
        attributeType.setSubstring("".equals(substringText.getText()) ? null : substringText.getText());
        attributeType.setSyntax("".equals(syntaxText.getText()) ? null : syntaxText.getText());

        attributeType.setSingleValued(singleValuedCheckbox.getSelection());
        attributeType.setCollective(collectiveCheckbox.getSelection());
        attributeType.setModifiable(modifiableCheckbox.getSelection());
        attributeType.setObsolete(obsoleteCheckbox.getSelection());
    }

    public void setAttributeType(AttributeType attributeType) {
        this.attributeType = attributeType;

        if (attributeType.getOid() != null) oidText.setText(attributeType.getOid());

        StringBuffer sb = new StringBuffer();
        for (Iterator i=attributeType.getNames().iterator(); i.hasNext(); ) {
            String name = (String)i.next();
            if (sb.length() > 0) sb.append(" ");
            sb.append(name);
        }
        namesText.setText(sb.toString());

        if (attributeType.getDescription() != null) descriptionText.setText(attributeType.getDescription());

        if (attributeType.getUsage() != null) usageCombo.setText(attributeType.getUsage());

        if (attributeType.getSuperClass() != null) superClassText.setText(attributeType.getSuperClass());
        if (attributeType.getEquality() != null) equalityText.setText(attributeType.getEquality());
        if (attributeType.getOrdering() != null) orderingText.setText(attributeType.getOrdering());
        if (attributeType.getSubstring() != null) substringText.setText(attributeType.getSubstring());
        if (attributeType.getSyntax() != null) syntaxText.setText(attributeType.getSyntax());

        singleValuedCheckbox.setSelection(attributeType.isSingleValued());
        collectiveCheckbox.setSelection(attributeType.isCollective());
        modifiableCheckbox.setSelection(attributeType.isModifiable());
        obsoleteCheckbox.setSelection(attributeType.isObsolete());
    }
}
