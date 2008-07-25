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
package org.safehaus.penrose.studio.directory.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.*;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.IManagedForm;
import org.safehaus.penrose.directory.EntryConfig;

/**
 * @author Endi S. Dewata
 */
public class EntryCachePage extends FormPage {

    FormToolkit toolkit;

    Text filterCacheSizeText;
    Text filterCacheExpirationText;

    Text dataCacheSizeText;
    Text dataCacheExpirationText;

    Text batchSizeText;

    EntryEditor editor;
	EntryConfig entry;

    public EntryCachePage(EntryEditor editor) {
        super(editor, "CACHE", "  Cache  ");

        this.editor = editor;
        this.entry = editor.entryConfig;
    }

    public void createFormContent(IManagedForm managedForm) {
        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText("Entry Editor");

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Filter Cache");
        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Control filterCacheSection = createFilterCacheSection(section);
        section.setClient(filterCacheSection);

        section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Data Cache");
        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Control dataCacheSection = createDataCacheSection(section);
        section.setClient(dataCacheSection);

        section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Data Loading");
        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Control dataLoadingSection = createDataLoadingSection(section);
        section.setClient(dataLoadingSection);
	}

	public Composite createFilterCacheSection(Composite parent) {

		Composite composite = toolkit.createComposite(parent);
		composite.setLayout(new GridLayout(2, false));

		Label filterCacheSizeLabel = toolkit.createLabel(composite, "Size (entries):");
        GridData gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        filterCacheSizeLabel.setLayoutData(gd);

		filterCacheSizeText = toolkit.createText(composite, "", SWT.BORDER);

        gd = new GridData(GridData.FILL);
        gd.widthHint = 200;
		filterCacheSizeText.setLayoutData(gd);

        filterCacheSizeText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                checkDirty();
            }
        });

        Label filterCacheExpirationLabel = toolkit.createLabel(composite, "Expiration (minutes):");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        filterCacheExpirationLabel.setLayoutData(gd);

        filterCacheExpirationText = toolkit.createText(composite, "", SWT.BORDER);

        gd = new GridData(GridData.FILL);
        gd.widthHint = 200;
        filterCacheExpirationText.setLayoutData(gd);

        filterCacheExpirationText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                checkDirty();
            }
        });

		return composite;
	}

    public Composite createDataCacheSection(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        Label dataCacheSizeLabel = toolkit.createLabel(composite, "Size (entries):");
        GridData gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        dataCacheSizeLabel.setLayoutData(gd);

        dataCacheSizeText = toolkit.createText(composite, "", SWT.BORDER);

        gd = new GridData(GridData.FILL);
        gd.widthHint = 200;
        dataCacheSizeText.setLayoutData(gd);

        dataCacheSizeText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                checkDirty();
            }
        });

        Label dataCacheExpirationLabel = toolkit.createLabel(composite, "Expiration (minutes):");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        dataCacheExpirationLabel.setLayoutData(gd);

        dataCacheExpirationText = toolkit.createText(composite, "", SWT.BORDER);

        gd = new GridData(GridData.FILL);
        gd.widthHint = 200;
        dataCacheExpirationText.setLayoutData(gd);

        dataCacheExpirationText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                checkDirty();
            }
        });

        return composite;
    }


    public Composite createDataLoadingSection(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        Label dataCacheSizeLabel = toolkit.createLabel(composite, "Size (entries):");
        GridData gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        dataCacheSizeLabel.setLayoutData(gd);

        batchSizeText = toolkit.createText(composite, "", SWT.BORDER);

        gd = new GridData(GridData.FILL);
        gd.widthHint = 200;
        batchSizeText.setLayoutData(gd);

        batchSizeText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                checkDirty();
            }
        });

        return composite;
    }

    public void checkDirty() {
        editor.checkDirty();
    }
}
