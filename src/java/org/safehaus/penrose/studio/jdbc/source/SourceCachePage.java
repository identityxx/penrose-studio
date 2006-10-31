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
package org.safehaus.penrose.studio.jdbc.source;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.*;
import org.eclipse.ui.forms.IManagedForm;
import org.safehaus.penrose.source.SourceConfig;
import org.safehaus.penrose.studio.source.editor.SourceEditorPage;
import org.safehaus.penrose.studio.source.editor.SourceEditor;

/**
 * @author Endi S. Dewata
 */
public class SourceCachePage extends SourceEditorPage {

    Text filterCacheSizeText;
    Text filterCacheExpirationText;

    Text dataCacheSizeText;
    Text dataCacheExpirationText;

    Text sizeLimitText;
    Combo loadingMethodCombo;

    public SourceCachePage(SourceEditor editor) {
        super(editor, "CACHE", "  Cache  ");
    }

    public void createFormContent(IManagedForm managedForm) {
        super.createFormContent(managedForm);

        ScrolledForm form = managedForm.getForm();
        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section section = getToolkit().createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Filter Cache");
        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Control filterCacheSection = createFilterCacheSection(section);
        section.setClient(filterCacheSection);

        section = getToolkit().createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Data Cache");
        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Control dataCacheSection = createDataCacheSection(section);
        section.setClient(dataCacheSection);

        section = getToolkit().createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Data Loading");
        section.setLayoutData(new GridData(GridData.FILL_BOTH));

        Control dataLoadingSection = createDataLoadingSection(section);
        section.setClient(dataLoadingSection);
    }

    public void refresh() {
        String value = getSourceConfig().getParameter(SourceConfig.QUERY_CACHE_SIZE);
        filterCacheSizeText.setText(value == null ? "" : value);

        value = getSourceConfig().getParameter(SourceConfig.QUERY_CACHE_EXPIRATION);
        filterCacheExpirationText.setText(value == null ? "" : value);

        value = getSourceConfig().getParameter(SourceConfig.DATA_CACHE_SIZE);
        dataCacheSizeText.setText(value == null ? "" : value);

        value = getSourceConfig().getParameter(SourceConfig.DATA_CACHE_EXPIRATION);
        dataCacheExpirationText.setText(value == null ? "" : value);

        value = getSourceConfig().getParameter(SourceConfig.SIZE_LIMIT);
        sizeLimitText.setText(value == null ? "" : value);

        value = getSourceConfig().getParameter(SourceConfig.LOADING_METHOD);
        loadingMethodCombo.setText(value == null ? "" : value);
    }

    public Composite createFilterCacheSection(Composite parent) {

        Composite composite = getToolkit().createComposite(parent);
        composite.setLayout(new GridLayout(3, false));

        Label filterCacheSizeLabel = getToolkit().createLabel(composite, "Size (entries):");
        GridData gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        filterCacheSizeLabel.setLayoutData(gd);

        filterCacheSizeText = getToolkit().createText(composite, "", SWT.BORDER);

        gd = new GridData(GridData.FILL);
        gd.widthHint = 200;
        filterCacheSizeText.setLayoutData(gd);

        filterCacheSizeText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                if ("".equals(filterCacheSizeText.getText())) {
                    getSourceConfig().removeParameter(SourceConfig.QUERY_CACHE_SIZE);
                } else {
                    getSourceConfig().setParameter(SourceConfig.QUERY_CACHE_SIZE, filterCacheSizeText.getText());
                }
                checkDirty();
            }
        });

        getToolkit().createLabel(composite, "(Default: "+SourceConfig.DEFAULT_QUERY_CACHE_SIZE+")");

        Label filterCacheExpirationLabel = getToolkit().createLabel(composite, "Expiration (minutes):");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        filterCacheExpirationLabel.setLayoutData(gd);

        filterCacheExpirationText = getToolkit().createText(composite, "", SWT.BORDER);

        gd = new GridData(GridData.FILL);
        gd.widthHint = 200;
        filterCacheExpirationText.setLayoutData(gd);

        filterCacheExpirationText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                if ("".equals(filterCacheExpirationText.getText())) {
                    getSourceConfig().removeParameter(SourceConfig.QUERY_CACHE_EXPIRATION);
                } else {
                    getSourceConfig().setParameter(SourceConfig.QUERY_CACHE_EXPIRATION, filterCacheExpirationText.getText());
                }
                checkDirty();
            }
        });

        getToolkit().createLabel(composite, "(Default: "+SourceConfig.DEFAULT_QUERY_CACHE_EXPIRATION+")");

        return composite;
    }

    public Composite createDataCacheSection(Composite parent) {

        Composite composite = getToolkit().createComposite(parent);
        composite.setLayout(new GridLayout(3, false));

        Label dataCacheSizeLabel = getToolkit().createLabel(composite, "Size (entries):");
        GridData gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        dataCacheSizeLabel.setLayoutData(gd);

        dataCacheSizeText = getToolkit().createText(composite, "", SWT.BORDER);

        gd = new GridData(GridData.FILL);
        gd.widthHint = 200;
        dataCacheSizeText.setLayoutData(gd);

        dataCacheSizeText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                if ("".equals(dataCacheSizeText.getText())) {
                    getSourceConfig().removeParameter(SourceConfig.DATA_CACHE_SIZE);
                } else {
                    getSourceConfig().setParameter(SourceConfig.DATA_CACHE_SIZE, dataCacheSizeText.getText());
                }
                checkDirty();
            }
        });

        getToolkit().createLabel(composite, "(Default: "+SourceConfig.DEFAULT_DATA_CACHE_SIZE+")");

        Label dataCacheExpirationLabel = getToolkit().createLabel(composite, "Expiration (minutes):");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        dataCacheExpirationLabel.setLayoutData(gd);

        dataCacheExpirationText = getToolkit().createText(composite, "", SWT.BORDER);

        gd = new GridData(GridData.FILL);
        gd.widthHint = 200;
        dataCacheExpirationText.setLayoutData(gd);

        dataCacheExpirationText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                if ("".equals(dataCacheExpirationText.getText())) {
                    getSourceConfig().removeParameter(SourceConfig.DATA_CACHE_EXPIRATION);
                } else {
                    getSourceConfig().setParameter(SourceConfig.DATA_CACHE_EXPIRATION, dataCacheExpirationText.getText());
                }
                checkDirty();
            }
        });

        getToolkit().createLabel(composite, "(Default: "+SourceConfig.DEFAULT_DATA_CACHE_EXPIRATION+")");

        return composite;
    }

    public Composite createDataLoadingSection(Composite parent) {

        Composite composite = getToolkit().createComposite(parent);
        composite.setLayout(new GridLayout(3, false));

        Label sizeLimitLabel = getToolkit().createLabel(composite, "Size limit:");
        GridData gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        sizeLimitLabel.setLayoutData(gd);

        sizeLimitText = getToolkit().createText(composite, "", SWT.BORDER);

        gd = new GridData(GridData.FILL);
        gd.widthHint = 200;
        sizeLimitText.setLayoutData(gd);

        sizeLimitText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                if ("".equals(sizeLimitText.getText())) {
                    getSourceConfig().removeParameter(SourceConfig.SIZE_LIMIT);
                } else {
                    getSourceConfig().setParameter(SourceConfig.SIZE_LIMIT, sizeLimitText.getText());
                }
                checkDirty();
            }
        });

        getToolkit().createLabel(composite, "(Default: "+SourceConfig.DEFAULT_SIZE_LIMIT+")");

        Label loadingMethodLabel = getToolkit().createLabel(composite, "Loading method:");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        loadingMethodLabel.setLayoutData(gd);

        loadingMethodCombo = new Combo(composite, SWT.READ_ONLY);
        loadingMethodCombo.add("");
        loadingMethodCombo.add(SourceConfig.LOAD_ALL);
        loadingMethodCombo.add(SourceConfig.SEARCH_AND_LOAD);

        gd = new GridData(GridData.FILL);
        gd.widthHint = 200;
        loadingMethodCombo.setLayoutData(gd);

        loadingMethodCombo.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                if ("".equals(loadingMethodCombo.getText())) {
                    getSourceConfig().removeParameter(SourceConfig.LOADING_METHOD);
                } else {
                    getSourceConfig().setParameter(SourceConfig.LOADING_METHOD, loadingMethodCombo.getText());
                }
                checkDirty();
            }
        });

        getToolkit().createLabel(composite, "(Default: "+SourceConfig.DEFAULT_LOADING_METHOD+")");

        return composite;
    }
}
