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
package org.safehaus.penrose.studio.schema;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.events.*;
import org.eclipse.ui.forms.widgets.*;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.IManagedForm;
import org.safehaus.penrose.schema.SchemaConfig;
import org.apache.log4j.Logger;

/**
 * @author Endi S. Dewata
 */
public class SchemaPropertiesEditorPage extends FormPage {

    Logger log = Logger.getLogger(getClass());
    
    FormToolkit toolkit;

    Text nameText;
    Text pathText;

    SchemaEditor editor;
    SchemaConfig schemaConfig;

    public SchemaPropertiesEditorPage(SchemaEditor editor) {
        super(editor, "PROPERTIES", "  Properties  ");

        this.editor = editor;
        this.schemaConfig = editor.getSchema().getSchemaConfig();
    }

    public void createFormContent(IManagedForm managedForm) {
        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText("Schema Editor");

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Schema Properties");
        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Control nameSection = createSection(section);
        section.setClient(nameSection);

        refresh();
    }

    public Composite createSection(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        Label nameLabel = toolkit.createLabel(composite, "Name:");
        GridData gd = new GridData();
        gd.widthHint = 100;
        nameLabel.setLayoutData(gd);

        nameText = toolkit.createText(composite, schemaConfig.getName(), SWT.BORDER);
        nameText.setEnabled(false);
        nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        nameText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                schemaConfig.setName(nameText.getText());
                checkDirty();
            }
        });

        Label pathLabel = toolkit.createLabel(composite, "Path:");
        pathLabel.setLayoutData(new GridData());

        pathText = toolkit.createText(composite, schemaConfig.getPath(), SWT.BORDER);
        pathText.setEnabled(false);
        pathText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        pathText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                schemaConfig.setPath(pathText.getText());
                checkDirty();
            }
        });

        return composite;
    }

    public void refresh() {
    }

    public void checkDirty() {
        editor.checkDirty();
    }
}