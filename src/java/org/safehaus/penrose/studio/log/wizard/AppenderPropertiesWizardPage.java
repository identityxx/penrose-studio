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
package org.safehaus.penrose.studio.log.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;

/**
 * @author Endi S. Dewata
 */
public class AppenderPropertiesWizardPage extends WizardPage {

    public final static String NAME = "Appender Properties";

    Text nameText;
    Combo classCombo;

    private String name;
    private String className;

    public AppenderPropertiesWizardPage() {
        super(NAME);
        setDescription("Enter the appender properties.");
    }

    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        composite.setLayout(new GridLayout(2, false));

        Label nameLabel = new Label(composite, SWT.NONE);
        nameLabel.setText("Name:");

        nameText = new Text(composite, SWT.BORDER);
        nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        nameText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                name = nameText.getText().trim();
                name = "".equals(name) ? null : name;
                setPageComplete(validatePage());
            }
        });

        Label classLabel = new Label(composite, SWT.NONE);
        classLabel.setText("Class:");

        classCombo = new Combo(composite, SWT.BORDER);
        classCombo.add("");
        classCombo.add("org.apache.log4j.ConsoleAppender");
        classCombo.add("org.apache.log4j.RollingFileAppender");
        classCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        classCombo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                className = classCombo.getText().trim();
                className = "".equals(className) ? null : className;
                setPageComplete(validatePage());
            }
        });

        setPageComplete(validatePage());
    }

    public void setVisible(boolean b) {
        super.setVisible(b);
        if (b) refresh();
    }

    public void refresh() {
        nameText.setText(name == null ? "" : name);
        classCombo.setText(className == null ? "" : className);
    }

    public boolean validatePage() {
        return true;
    }

    public String getAppenderName() {
        return name;
    }

    public void setAppenderName(String name) {
        this.name = name;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}