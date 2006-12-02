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
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.*;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.IManagedForm;
import org.safehaus.penrose.partition.SourceConfig;
import org.safehaus.penrose.studio.source.editor.JDBCSourceEditor;

/**
 * @author Endi S. Dewata
 */
public class JDBCSourceCachePage extends FormPage {

    FormToolkit toolkit;

    Text filterCacheSizeText;
    Text filterCacheExpirationText;

    Text dataCacheSizeText;
    Text dataCacheExpirationText;

    Text sizeLimitText;
    Combo loadingMethodCombo;

    JDBCSourceEditor editor;
    SourceConfig sourceConfig;

    public JDBCSourceCachePage(JDBCSourceEditor editor) {
        super(editor, "CACHE", "  Cache  ");

        this.editor = editor;
        this.sourceConfig = editor.sourceConfig;
    }

    public void createFormContent(IManagedForm managedForm) {
        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText("Source Editor");

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
        section.setLayoutData(new GridData(GridData.FILL_BOTH));

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

        String value = sourceConfig.getParameter(SourceConfig.QUERY_CACHE_SIZE);
        value = value == null ? ""+SourceConfig.DEFAULT_QUERY_CACHE_SIZE : value;
        filterCacheSizeText = toolkit.createText(composite, value, SWT.BORDER);

        gd = new GridData(GridData.FILL);
        gd.widthHint = 200;
        filterCacheSizeText.setLayoutData(gd);

        filterCacheSizeText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                if ("".equals(filterCacheSizeText.getText())) {
                    sourceConfig.removeParameter(SourceConfig.QUERY_CACHE_SIZE);
                } else {
                    sourceConfig.setParameter(SourceConfig.QUERY_CACHE_SIZE, filterCacheSizeText.getText());
                }
                checkDirty();
            }
        });

        Label filterCacheExpirationLabel = toolkit.createLabel(composite, "Expiration (minutes):");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        filterCacheExpirationLabel.setLayoutData(gd);

        value = sourceConfig.getParameter(SourceConfig.QUERY_CACHE_EXPIRATION);
        value = value == null ? ""+SourceConfig.DEFAULT_QUERY_CACHE_EXPIRATION : value;
        filterCacheExpirationText = toolkit.createText(composite, value, SWT.BORDER);

        gd = new GridData(GridData.FILL);
        gd.widthHint = 200;
        filterCacheExpirationText.setLayoutData(gd);

        filterCacheExpirationText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                if ("".equals(filterCacheExpirationText.getText())) {
                    sourceConfig.removeParameter(SourceConfig.QUERY_CACHE_EXPIRATION);
                } else {
                    sourceConfig.setParameter(SourceConfig.QUERY_CACHE_EXPIRATION, filterCacheExpirationText.getText());
                }
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

        String value = sourceConfig.getParameter(SourceConfig.DATA_CACHE_SIZE);
        value = value == null ? ""+SourceConfig.DEFAULT_DATA_CACHE_SIZE : value;
        dataCacheSizeText = toolkit.createText(composite, value, SWT.BORDER);

        gd = new GridData(GridData.FILL);
        gd.widthHint = 200;
        dataCacheSizeText.setLayoutData(gd);

        dataCacheSizeText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                if ("".equals(dataCacheSizeText.getText())) {
                    sourceConfig.removeParameter(SourceConfig.DATA_CACHE_SIZE);
                } else {
                    sourceConfig.setParameter(SourceConfig.DATA_CACHE_SIZE, dataCacheSizeText.getText());
                }
                checkDirty();
            }
        });

        Label dataCacheExpirationLabel = toolkit.createLabel(composite, "Expiration (minutes):");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        dataCacheExpirationLabel.setLayoutData(gd);

        value = sourceConfig.getParameter(SourceConfig.DATA_CACHE_EXPIRATION);
        value = value == null ? ""+SourceConfig.DEFAULT_DATA_CACHE_EXPIRATION : value;
        dataCacheExpirationText = toolkit.createText(composite, value, SWT.BORDER);

        gd = new GridData(GridData.FILL);
        gd.widthHint = 200;
        dataCacheExpirationText.setLayoutData(gd);

        dataCacheExpirationText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                if ("".equals(dataCacheExpirationText.getText())) {
                    sourceConfig.removeParameter(SourceConfig.DATA_CACHE_EXPIRATION);
                } else {
                    sourceConfig.setParameter(SourceConfig.DATA_CACHE_EXPIRATION, dataCacheExpirationText.getText());
                }
                checkDirty();
            }
        });

        return composite;
    }

    public Composite createDataLoadingSection(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        Label sizeLimitLabel = toolkit.createLabel(composite, "Size limit:");
        GridData gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        sizeLimitLabel.setLayoutData(gd);

        String value = sourceConfig.getParameter(SourceConfig.SIZE_LIMIT);
        value = value == null ? ""+SourceConfig.DEFAULT_SIZE_LIMIT : value;
        sizeLimitText = toolkit.createText(composite, value, SWT.BORDER);

        gd = new GridData(GridData.FILL);
        gd.widthHint = 200;
        sizeLimitText.setLayoutData(gd);

        sizeLimitText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                if ("".equals(sizeLimitText.getText())) {
                    sourceConfig.removeParameter(SourceConfig.SIZE_LIMIT);
                } else {
                    sourceConfig.setParameter(SourceConfig.SIZE_LIMIT, sizeLimitText.getText());
                }
                checkDirty();
            }
        });

        Label loadingMethodLabel = toolkit.createLabel(composite, "Loading method:");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        loadingMethodLabel.setLayoutData(gd);

        loadingMethodCombo = new Combo(composite, SWT.READ_ONLY);
        loadingMethodCombo.add("");
        loadingMethodCombo.add(SourceConfig.LOAD_ALL);
        loadingMethodCombo.add(SourceConfig.SEARCH_AND_LOAD);

        value = sourceConfig.getParameter(SourceConfig.LOADING_METHOD);
        value = value == null ? SourceConfig.DEFAULT_LOADING_METHOD : value;
        loadingMethodCombo.setText(value);

        gd = new GridData(GridData.FILL);
        gd.widthHint = 200;
        loadingMethodCombo.setLayoutData(gd);

        loadingMethodCombo.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                if ("".equals(loadingMethodCombo.getText())) {
                    sourceConfig.removeParameter(SourceConfig.LOADING_METHOD);
                } else {
                    sourceConfig.setParameter(SourceConfig.LOADING_METHOD, loadingMethodCombo.getText());
                }
                checkDirty();
            }
        });

        return composite;
    }

    public void checkDirty() {
        editor.checkDirty();
    }
}
