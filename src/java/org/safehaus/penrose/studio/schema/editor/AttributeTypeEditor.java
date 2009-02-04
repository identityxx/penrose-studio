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
package org.safehaus.penrose.studio.schema.editor;

import java.util.Iterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.forms.widgets.*;
import org.eclipse.core.runtime.IProgressMonitor;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.schema.AttributeType;
import org.apache.log4j.Logger;

public class AttributeTypeEditor extends EditorPart implements ModifyListener, SelectionListener {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

	Text oidText;
    Text namesText;
	Text descriptionText;
    Combo typeCombo;
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
	
    boolean dirty;

    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        AttributeTypeEditorInput ei = (AttributeTypeEditorInput)input;
        attributeType = ei.getAttributeType();

        setSite(site);
        setInput(input);
        setPartName(ei.getName());
    }

    public void createPartControl(Composite parent) {
        try {
            toolkit = new FormToolkit(parent.getDisplay());

            ScrolledForm form = toolkit.createScrolledForm(parent);
            form.setText("Attribute Type Editor");

            form.getBody().setLayout(new GridLayout());

            Section section = createPropertiesSection(form.getBody());
            section.setLayoutData(new GridData(GridData.FILL_BOTH));

	    } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
	}

	public Section createPropertiesSection(final Composite parent) {
		Section section = toolkit.createSection(parent, Section.TITLE_BAR|Section.EXPANDED);
		section.setText("Properties");

		Composite composite = toolkit.createComposite(section);
        section.setClient(composite);

		composite.setLayout(new GridLayout(2, false));

		Label oidLabel = toolkit.createLabel(composite, "OID:");
        GridData gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        oidLabel.setLayoutData(gd);

		oidText = toolkit.createText(composite, "", SWT.BORDER);
        if (attributeType.getOid() != null) oidText.setText(attributeType.getOid());
		oidText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        oidText.setEnabled(false);
		oidText.addModifyListener(this);

        Label namesLabel = toolkit.createLabel(composite, "Names:");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        namesLabel.setLayoutData(gd);

        StringBuilder sb = new StringBuilder();
        for (Iterator i=attributeType.getNames().iterator(); i.hasNext(); ) {
            String name = (String)i.next();
            if (sb.length() > 0) sb.append(" ");
            sb.append(name);
        }

        namesText = toolkit.createText(composite, sb.toString(), SWT.BORDER);
        namesText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        namesText.setEnabled(false);
        namesText.addModifyListener(this);

        Label descriptionLabel = toolkit.createLabel(composite, "Description:");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        descriptionLabel.setLayoutData(gd);

        descriptionText = toolkit.createText(composite, "", SWT.BORDER);
        if (attributeType.getDescription() != null) descriptionText.setText(attributeType.getDescription());
        descriptionText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        descriptionText.setEnabled(false);
        descriptionText.addModifyListener(this);

        Label typeLabel = toolkit.createLabel(composite, "Type:");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        typeLabel.setLayoutData(gd);

        typeCombo = new Combo(composite, SWT.READ_ONLY);
        typeCombo.add(AttributeType.USER_APPLICATIONS);
        typeCombo.add(AttributeType.DIRECTORY_OPERATION);
        typeCombo.add(AttributeType.DISTRIBUTED_OPERATION);
        typeCombo.add(AttributeType.DSA_OPERATION);
        if (attributeType.getUsage() != null) typeCombo.setText(attributeType.getUsage());
        typeCombo.setEnabled(false);
        typeCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label superClassLabel = toolkit.createLabel(composite, "Super Class:");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        superClassLabel.setLayoutData(gd);

        superClassText = toolkit.createText(composite, sb.toString(), SWT.BORDER);
        if (attributeType.getSuperClass() != null) superClassText.setText(attributeType.getSuperClass());
        superClassText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        superClassText.setEnabled(false);
        superClassText.addModifyListener(this);

        Label equalityLabel = toolkit.createLabel(composite, "Equality:");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        equalityLabel.setLayoutData(gd);

        equalityText = toolkit.createText(composite, "", SWT.BORDER);
        if (attributeType.getEquality() != null) equalityText.setText(attributeType.getEquality());
        equalityText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        equalityText.setEnabled(false);
        equalityText.addModifyListener(this);

        Label orderingLabel = toolkit.createLabel(composite, "Ordering:");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        orderingLabel.setLayoutData(gd);

        orderingText = toolkit.createText(composite, "", SWT.BORDER);
        if (attributeType.getOrdering() != null) orderingText.setText(attributeType.getOrdering());
        orderingText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        orderingText.setEnabled(false);
        orderingText.addModifyListener(this);

        Label substringLabel = toolkit.createLabel(composite, "Substring:");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        substringLabel.setLayoutData(gd);

        substringText = toolkit.createText(composite, "", SWT.BORDER);
        if (attributeType.getSubstring() != null) substringText.setText(attributeType.getSubstring());
        substringText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        substringText.setEnabled(false);
        substringText.addModifyListener(this);

        Label syntaxLabel = toolkit.createLabel(composite, "Syntax:");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        syntaxLabel.setLayoutData(gd);

        syntaxText = toolkit.createText(composite, "", SWT.BORDER);
        if (attributeType.getSyntax() != null) syntaxText.setText(attributeType.getSyntax());
        syntaxText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        syntaxText.setEnabled(false);
        syntaxText.addModifyListener(this);

        Label singleValuedLabel = toolkit.createLabel(composite, "Single-valued:");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        singleValuedLabel.setLayoutData(gd);

        singleValuedCheckbox = new Button(composite, SWT.CHECK);
        singleValuedCheckbox.setText("");
        singleValuedCheckbox.setSelection(attributeType.isSingleValued());
        singleValuedCheckbox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        singleValuedCheckbox.setEnabled(false);
        singleValuedCheckbox.addSelectionListener(this);

        Label collectiveLabel = toolkit.createLabel(composite, "Collective:");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        collectiveLabel.setLayoutData(gd);

        collectiveCheckbox = new Button(composite, SWT.CHECK);
        collectiveCheckbox.setText("");
        collectiveCheckbox.setSelection(attributeType.isCollective());
        collectiveCheckbox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        collectiveCheckbox.setEnabled(false);
        collectiveCheckbox.addSelectionListener(this);

        Label modifiableLabel = toolkit.createLabel(composite, "Modifiable:");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        modifiableLabel.setLayoutData(gd);

        modifiableCheckbox = new Button(composite, SWT.CHECK);
        modifiableCheckbox.setText("");
        modifiableCheckbox.setSelection(attributeType.isModifiable());
        modifiableCheckbox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        modifiableCheckbox.setEnabled(false);
        modifiableCheckbox.addSelectionListener(this);

        Label obsoleteLabel = toolkit.createLabel(composite, "Obsolete:");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        obsoleteLabel.setLayoutData(gd);

        obsoleteCheckbox = new Button(composite, SWT.CHECK);
        obsoleteCheckbox.setText("");
        obsoleteCheckbox.setSelection(attributeType.isObsolete());
        obsoleteCheckbox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        obsoleteCheckbox.setEnabled(false);
        obsoleteCheckbox.addSelectionListener(this);

        return section;
    }

    public void doSave(IProgressMonitor iProgressMonitor) {
        try {
            store();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void doSaveAs() {
    }

    public boolean isSaveAsAllowed() {
        return false;
    }

    public void setFocus() {
    }

    public void store() throws Exception {

        //Schema schema = PenroseStudio.getSchema();

        boolean rename = !oidText.getText().equals(attributeType.getOid());
        if (rename) {
            //schema.removeObjectClass(objectClass.getName());
        }

        attributeType.setOid(oidText.getText());

        if (rename) {
            //schema.addObjectClass(objectClass);
        }

        setPartName("Attribute Type - "+attributeType.getName());

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();

        checkDirty();
    }

    public boolean isDirty() {
        return dirty;
    }

    public void checkDirty() {
        try {
            dirty = false;

            if (!oidText.getText().equals(attributeType.getOid())) {
                dirty = true;
                return;
            }

        } finally {
            firePropertyChange(PROP_DIRTY);
        }
    }

    public void modifyText(ModifyEvent event) {
        checkDirty();
    }

    public void widgetSelected(SelectionEvent event) {
    }

    public void widgetDefaultSelected(SelectionEvent event) {
    }
}
