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
 */package org.safehaus.penrose.studio.mapping.editor;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.safehaus.penrose.mapping.MappingConfig;

/**
 * @author Endi S. Dewata
 */
public class MappingEditorScriptsPage extends FormPage {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    Text preScriptText;
    Text postScriptText;

    MappingEditor editor;
    MappingConfig mappingConfig;

    public MappingEditorScriptsPage(MappingEditor editor) {
        super(editor, "SCRIPTS", "  Scripts  ");

        this.editor = editor;
        this.mappingConfig = editor.mappingConfig;
    }

    public void createFormContent(IManagedForm managedForm) {

        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText("Mapping Editor");

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section preScriptSection = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        preScriptSection.setText("Pre-Script");
        preScriptSection.setLayoutData(new GridData(GridData.FILL_BOTH));

        Control preScriptControl = createPreScriptSection(preScriptSection);
        preScriptSection.setClient(preScriptControl);

        Section postScriptSection = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        postScriptSection.setText("Post-Script");
        postScriptSection.setLayoutData(new GridData(GridData.FILL_BOTH));

        Control postScriptControl = createPostScriptSection(postScriptSection);
        postScriptSection.setClient(postScriptControl);

        refresh();
    }

    public Composite createPreScriptSection(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout());

        preScriptText = toolkit.createText(composite, "", SWT.BORDER | SWT.MULTI);
        preScriptText.setLayoutData(new GridData(GridData.FILL_BOTH));

        preScriptText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                String preScript = preScriptText.getText().trim();
                log.debug("Pre-script: ["+preScript+"]");
                mappingConfig.setPreScript(preScript.equals("") ? null : preScript);

                checkDirty();
            }
        });

        return composite;
    }

    public Composite createPostScriptSection(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout());

        postScriptText = toolkit.createText(composite, "", SWT.BORDER | SWT.MULTI);
        postScriptText.setLayoutData(new GridData(GridData.FILL_BOTH));

        postScriptText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                String postScript = postScriptText.getText().trim();
                log.debug("Post-script: ["+postScript+"]");
                mappingConfig.setPostScript(postScript.equals("") ? null : postScript);

                checkDirty();
            }
        });

        return composite;
    }

    public void refresh() {

        String preScript = mappingConfig.getPreScript();
        preScriptText.setText(preScript == null ? "" : preScript);

        String postScript = mappingConfig.getPostScript();
        postScriptText.setText(postScript == null ? "" : postScript);
    }

    public void checkDirty() {
        editor.checkDirty();
    }
}