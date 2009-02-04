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
package org.safehaus.penrose.studio.schema.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.schema.ObjectClass;

import java.util.*;

/**
 * @author Endi S. Dewata
 */
public class ObjectClassDialog extends Dialog {

    public final static int CANCEL = 0;
    public final static int OK     = 1;

    Shell shell;

    Text oidText;
    Text namesText;
    Text descriptionText;
    Combo typeCombo;
    Text superClassesText;
    Button obsoleteCheckbox;

	Table requiredAttributesTable;
    Table optionalAttributesTable;

	ObjectClass objectClass;

    int action;

	public ObjectClassDialog(Shell parent, int style) {
		super(parent, style);
        setText("Object Class");

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
        shell.setImage(PenroseStudio.getImage(PenroseImage.LOGO));
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

        Composite propertiesPage = createAttributesPage(tabFolder);
        propertiesPage.setLayoutData(new GridData(GridData.FILL_BOTH));

        TabItem propertiesTab = new TabItem(tabFolder, SWT.NONE);
        propertiesTab.setText("Attributes");
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

        Label typeLabel = new Label(composite, SWT.NONE);
        typeLabel.setText("Type:");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        typeLabel.setLayoutData(gd);

        typeCombo = new Combo(composite, SWT.READ_ONLY);
        typeCombo.add(ObjectClass.STRUCTURAL);
        typeCombo.add(ObjectClass.ABSTRACT);
        typeCombo.add(ObjectClass.AUXILIARY);
        typeCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label superClassesLabel = new Label(composite, SWT.NONE);
        superClassesLabel.setText("Super Classes:");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        superClassesLabel.setLayoutData(gd);

        superClassesText = new Text(composite, SWT.BORDER);
        superClassesText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        superClassesText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
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

    public Composite createAttributesPage(Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));

        Label requiredLabel = new Label(composite, SWT.NONE);
        requiredLabel.setText("Required:");
        GridData gd = new GridData();
        gd.horizontalSpan = 2;
        requiredLabel.setLayoutData(gd);

        requiredAttributesTable = new Table(composite, SWT.BORDER|SWT.FULL_SELECTION);
        requiredAttributesTable.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite buttons = new Composite(composite, SWT.NONE);
        buttons.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        buttons.setLayout(new GridLayout());

        Button addButton = new Button(buttons, SWT.PUSH);
        addButton.setText("Add");
        addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
            }
        });

        Button removeButton = new Button(buttons, SWT.PUSH);
        removeButton.setText("Remove");
        removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
            }
        });

        Label optionalLabel = new Label(composite, SWT.NONE);
        optionalLabel.setText("Optional:");
        gd = new GridData();
        gd.horizontalSpan = 2;
        optionalLabel.setLayoutData(gd);

        optionalAttributesTable = new Table(composite, SWT.BORDER|SWT.FULL_SELECTION);
        optionalAttributesTable.setLayoutData(new GridData(GridData.FILL_BOTH));

        buttons = new Composite(composite, SWT.NONE);
        buttons.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        buttons.setLayout(new GridLayout());

        addButton = new Button(buttons, SWT.PUSH);
        addButton.setText("Add");
        addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
            }
        });

        removeButton = new Button(buttons, SWT.PUSH);
        removeButton.setText("Remove");
        removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        removeButton.addSelectionListener(new SelectionAdapter() {
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
        objectClass.setOid("".equals(oidText.getText()) ? null : oidText.getText());

        objectClass.removeNames();
        StringTokenizer st = new StringTokenizer(namesText.getText());
        while (st.hasMoreTokens()) {
            objectClass.addName(st.nextToken());
        }

        objectClass.setDescription("".equals(descriptionText.getText()) ? null : descriptionText.getText());

        objectClass.setType(typeCombo.getText());

        objectClass.removeSuperClasses();
        st = new StringTokenizer(superClassesText.getText());
        while (st.hasMoreTokens()) {
            objectClass.addSuperClass(st.nextToken());
        }

        objectClass.setObsolete(obsoleteCheckbox.getSelection());

        objectClass.removeRequiredAttributes();
        TableItem[] items = requiredAttributesTable.getItems();
        for (int i=0; i<items.length; i++) {
            TableItem item = items[i];
            objectClass.addRequiredAttribute(item.getText());
        }

        objectClass.removeOptionalAttributes();
        items = optionalAttributesTable.getItems();
        for (int i=0; i<items.length; i++) {
            TableItem item = items[i];
            objectClass.addOptionalAttribute(item.getText());
        }
    }

    public void setObjectClass(ObjectClass objectClass) {
        this.objectClass = objectClass;

        if (objectClass.getOid() != null) oidText.setText(objectClass.getOid());

        StringBuilder sb = new StringBuilder();
        for (Iterator i=objectClass.getNames().iterator(); i.hasNext(); ) {
            String name = (String)i.next();
            if (sb.length() > 0) sb.append(" ");
            sb.append(name);
        }
        namesText.setText(sb.toString());

        if (objectClass.getDescription() != null) descriptionText.setText(objectClass.getDescription());

        if (objectClass.getType() != null) typeCombo.setText(objectClass.getType());

        sb = new StringBuilder();
        for (Iterator i=objectClass.getSuperClasses().iterator(); i.hasNext(); ) {
            String superClass = (String)i.next();
            if (sb.length() > 0) sb.append(" ");
            sb.append(superClass);
        }
        superClassesText.setText(sb.toString());

        obsoleteCheckbox.setSelection(objectClass.isObsolete());

        for (Iterator i=objectClass.getRequiredAttributes().iterator(); i.hasNext(); ) {
            String name = (String)i.next();

            TableItem tableItem = new TableItem(requiredAttributesTable, SWT.NONE);
            tableItem.setText(name);
        }

        for (Iterator i=objectClass.getOptionalAttributes().iterator(); i.hasNext(); ) {
            String name = (String)i.next();

            TableItem tableItem = new TableItem(optionalAttributesTable, SWT.NONE);
            tableItem.setText(name);
        }
    }
}
