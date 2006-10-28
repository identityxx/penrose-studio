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
package org.safehaus.penrose.studio.source.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;
import org.safehaus.penrose.source.FieldConfig;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.apache.log4j.Logger;

import java.lang.reflect.Field;
import java.sql.Types;
import java.util.*;

/**
 * @author Endi S. Dewata
 */
public class FieldDialog extends Dialog {

    Logger log = Logger.getLogger(getClass());

    public final static int CANCEL = 0;
    public final static int OK     = 1;

    Shell shell;

    Text nameText;
    Text aliasText;

    Combo typeCombo;
    Text lengthText;
    Text precisionText;
    Button uniqueCheckbox;
    Button caseSensitiveCheckbox;

    FieldConfig fieldConfig;
    int action;

	public FieldDialog(Shell parent, int style) {
		super(parent, style);
        setText("Field Editor");

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

        Composite selectorPage = createFieldPage(tabFolder);
        selectorPage.setLayoutData(new GridData(GridData.FILL_BOTH));

        TabItem selectorTab = new TabItem(tabFolder, SWT.NONE);
        selectorTab.setText("Field");
        selectorTab.setControl(selectorPage);

        Composite propertiesPage = createPropertiesPage(tabFolder);
        propertiesPage.setLayoutData(new GridData(GridData.FILL_BOTH));

        TabItem propertiesTab = new TabItem(tabFolder, SWT.NONE);
        propertiesTab.setText("Properties");
        propertiesTab.setControl(propertiesPage);

        Composite buttons = createButtons(parent);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalAlignment = GridData.END;
        buttons.setLayoutData(gd);
    }

    public Composite createButtons(Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new RowLayout());

        Button saveButton = new Button(composite, SWT.PUSH);
        saveButton.setText("Save");

        saveButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                save();

                setAction(OK);
                shell.close();
            }
        });

        Button cancelButton = new Button(composite, SWT.PUSH);
        cancelButton.setText("Cancel");

        cancelButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                setAction(CANCEL);
                shell.close();
            }
        });

        return composite;
    }

    public void save()  {
        if ("".equals(getNameText().getText())) return;

        String originalName = "".equals(getAliasText().getText()) ? null : getNameText().getText();
        String name = "".equals(getAliasText().getText()) ? getNameText().getText() : getAliasText().getText();

        getFieldConfig().setOriginalName(originalName);
        getFieldConfig().setName(name);

        String type = getTypeCombo().getText();
        int length = "".equals(getLengthText().getText()) ? FieldConfig.DEFAULT_LENGTH : Integer.parseInt(getLengthText().getText());
        int precision = "".equals(getPrecisionText().getText()) ? FieldConfig.DEFAULT_PRECISION : Integer.parseInt(getPrecisionText().getText());

        getFieldConfig().setType(type);
        getFieldConfig().setLength(length);
        getFieldConfig().setPrecision(precision);
        getFieldConfig().setUnique(getUniqueCheckbox().getSelection());
        getFieldConfig().setCaseSensitive(getCaseSensitiveCheckbox().getSelection());
    }

    public Composite createFieldPage(Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));

        Label nameLabel = new Label(composite, SWT.NONE);
        nameLabel.setText("Name:");
        GridData gd = new GridData(GridData.FILL);
        gd.widthHint = 50;
        nameLabel.setLayoutData(gd);

        setNameText(new Text(composite, SWT.BORDER));
		gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
		getNameText().setLayoutData(gd);

        Label typeLabel = new Label(composite, SWT.NONE);
        typeLabel.setText("Alias:");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 50;
        typeLabel.setLayoutData(gd);

        setAliasText(new Text(composite, SWT.BORDER));
		gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
		getAliasText().setLayoutData(gd);

        return composite;
    }

    public Composite createPropertiesPage(Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));

        Label typeLabel = new Label(composite, SWT.NONE);
        typeLabel.setText("Type:");
        GridData gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        typeLabel.setLayoutData(gd);

        setTypeCombo(new Combo(composite, SWT.BORDER | SWT.READ_ONLY));
        Field fields[] = Types.class.getFields();
        Collection names = new TreeSet();
        for (int i=0; i<fields.length; i++) {
            Field field = fields[i];
            names.add(field.getName());
        }
        for (Iterator i=names.iterator(); i.hasNext(); ) {
            String name = (String)i.next();
            getTypeCombo().add(name);
        }
        gd = new GridData(GridData.FILL);
        gd.widthHint = 200;
        getTypeCombo().setLayoutData(gd);

        Label lengthLabel = new Label(composite, SWT.NONE);
        lengthLabel.setText("Length:");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        lengthLabel.setLayoutData(gd);

        setLengthText(new Text(composite, SWT.BORDER));
		gd = new GridData(GridData.FILL);
        gd.widthHint = 50;
		getLengthText().setLayoutData(gd);

        Label precisionLabel = new Label(composite, SWT.NONE);
        precisionLabel.setText("Precision:");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        precisionLabel.setLayoutData(gd);

        setPrecisionText(new Text(composite, SWT.BORDER));
		gd = new GridData(GridData.FILL);
        gd.widthHint = 50;
		getPrecisionText().setLayoutData(gd);

        Label uniqueLabel = new Label(composite, SWT.NONE);
        uniqueLabel.setText("Unique:");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        uniqueLabel.setLayoutData(gd);

        setUniqueCheckbox(new Button(composite, SWT.CHECK));
		gd = new GridData(GridData.FILL);
        gd.widthHint = 50;
		getUniqueCheckbox().setLayoutData(gd);

        Label caseSensitiveLabel = new Label(composite, SWT.NONE);
        caseSensitiveLabel.setText("Case Sensitive:");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        caseSensitiveLabel.setLayoutData(gd);

        setCaseSensitiveCheckbox(new Button(composite, SWT.CHECK));
        gd = new GridData(GridData.FILL);
        gd.widthHint = 50;
        getCaseSensitiveCheckbox().setLayoutData(gd);

        return composite;
	}

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public void setFieldConfig(FieldConfig fieldConfig) {
        this.fieldConfig = fieldConfig;

        String name = fieldConfig.getOriginalName();
        getNameText().setText(name);

        String alias = fieldConfig.getName().equals(fieldConfig.getOriginalName()) ? "" : fieldConfig.getName();
        getAliasText().setText(alias);

        String type = fieldConfig.getType() == null ? "" : fieldConfig.getType();
        getTypeCombo().setText(type);

        getLengthText().setText(""+fieldConfig.getLength());
        getPrecisionText().setText(""+fieldConfig.getPrecision());
        getUniqueCheckbox().setSelection(fieldConfig.isUnique());
        getCaseSensitiveCheckbox().setSelection(fieldConfig.isCaseSensitive());
    }

    public Text getNameText() {
        return nameText;
    }

    public void setNameText(Text nameText) {
        this.nameText = nameText;
    }

    public Text getAliasText() {
        return aliasText;
    }

    public void setAliasText(Text aliasText) {
        this.aliasText = aliasText;
    }

    public Combo getTypeCombo() {
        return typeCombo;
    }

    public void setTypeCombo(Combo typeCombo) {
        this.typeCombo = typeCombo;
    }

    public Text getLengthText() {
        return lengthText;
    }

    public void setLengthText(Text lengthText) {
        this.lengthText = lengthText;
    }

    public Text getPrecisionText() {
        return precisionText;
    }

    public void setPrecisionText(Text precisionText) {
        this.precisionText = precisionText;
    }

    public Button getUniqueCheckbox() {
        return uniqueCheckbox;
    }

    public void setUniqueCheckbox(Button uniqueCheckbox) {
        this.uniqueCheckbox = uniqueCheckbox;
    }

    public Button getCaseSensitiveCheckbox() {
        return caseSensitiveCheckbox;
    }

    public void setCaseSensitiveCheckbox(Button caseSensitiveCheckbox) {
        this.caseSensitiveCheckbox = caseSensitiveCheckbox;
    }

    public FieldConfig getFieldConfig() {
        return fieldConfig;
    }
}
