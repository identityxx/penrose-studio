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
package org.safehaus.penrose.studio.mapping;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;
import org.safehaus.penrose.source.SourceConfig;
import org.safehaus.penrose.mapping.SourceMapping;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseImage;

import java.util.Collection;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Endi S. Dewata
 */
public class SourceDialog extends Dialog {

    Shell shell;

    Tree sourceTree;
    Text aliasText;

    Map items = new HashMap();

    private SourceMapping source;

    Button requiredCheckbox;
    Button addCheckbox;
    Button modifyCheckbox;
    Button modrdnCheckbox;
    Button deleteCheckbox;
    Button proxyCheckbox;

    Button saveButton;

    private boolean saved;

	public SourceDialog(Shell parent, int style) {
		super(parent, style);

        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);

        createControl(shell);
    }

    public void open () {

        Point size = new Point(400, 400);
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

    public Composite createSelectorPage(Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));

        sourceTree = new Tree(composite, SWT.BORDER | SWT.MULTI);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.horizontalSpan = 2;
        sourceTree.setLayoutData(gd);

        sourceTree.addMouseListener(new MouseAdapter() {
            public void mouseDown(MouseEvent event) {
                if (sourceTree.getSelectionCount() == 0) return;

                TreeItem item = sourceTree.getSelection()[0];
                if (item.getData() != null) {
                    SourceConfig sourceDefinition = (SourceConfig)item.getData();
                    aliasText.setText(sourceDefinition.getName());
                }

                aliasText.setEnabled(canEnterAlias());
                saveButton.setEnabled(canSave());
            }
        });

        Label aliasLabel = new Label(composite, SWT.NONE);
        aliasLabel.setText("Alias:");

        aliasText = new Text(composite, SWT.BORDER);
        aliasText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        aliasText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                saveButton.setEnabled(canSave());
            }
        });

        return composite;
    }

    public Composite createPropertiesPage(Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new RowLayout(SWT.VERTICAL));

        requiredCheckbox = new Button(composite, SWT.CHECK);
        requiredCheckbox.setText("Required on search");

        new Label(composite, SWT.NONE);

        addCheckbox = new Button(composite, SWT.CHECK);
        addCheckbox.setText("Include on add");

        modifyCheckbox = new Button(composite, SWT.CHECK);
        modifyCheckbox.setText("Include on modify");

        modrdnCheckbox = new Button(composite, SWT.CHECK);
        modrdnCheckbox.setText("Include on modrdn");

        deleteCheckbox = new Button(composite, SWT.CHECK);
        deleteCheckbox.setText("Include on delete");

        new Label(composite, SWT.NONE);

        proxyCheckbox = new Button(composite, SWT.CHECK);
        proxyCheckbox.setText("Proxy");

        return composite;
    }

    public void createControl(final Shell parent) {
        parent.setLayout(new GridLayout());

        TabFolder tabFolder = new TabFolder(parent, SWT.NONE);
        tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite selectorPage = createSelectorPage(tabFolder);
        selectorPage.setLayoutData(new GridData(GridData.FILL_BOTH));

        TabItem selectorTab = new TabItem(tabFolder, SWT.NONE);
        selectorTab.setText("Source");
        selectorTab.setControl(selectorPage);

        Composite propertiesPage = createPropertiesPage(tabFolder);
        propertiesPage.setLayoutData(new GridData(GridData.FILL_BOTH));

        TabItem propertiesTab = new TabItem(tabFolder, SWT.NONE);
        propertiesTab.setText("Properties");
        propertiesTab.setControl(propertiesPage);

        Composite buttons = new Composite(parent, SWT.NONE);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalAlignment = GridData.END;
        buttons.setLayoutData(gd);
        buttons.setLayout(new RowLayout());

		saveButton = new Button(buttons, SWT.PUSH);
        saveButton.setText("OK");

		saveButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
                if ("".equals(aliasText.getText())) return;
                if (sourceTree.getSelectionCount() == 0) return;

                TreeItem item = sourceTree.getSelection()[0];
                if (item.getData() == null) return;

                SourceConfig sourceConfig = (SourceConfig)item.getData();

                source.setName(aliasText.getText());
                source.setSourceName(sourceConfig.getName());

                source.setRequired(requiredCheckbox.getSelection());
                source.setIncludeOnAdd(addCheckbox.getSelection());
                source.setIncludeOnModify(modifyCheckbox.getSelection());
                source.setIncludeOnModRdn(modrdnCheckbox.getSelection());
                source.setIncludeOnDelete(deleteCheckbox.getSelection());
                source.setProxy(proxyCheckbox.getSelection());

                saved = true;
                shell.close();
			}
		});

		Button cancelButton = new Button(buttons, SWT.PUSH);
        cancelButton.setText("Cancel");
		cancelButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
                shell.close();
			}
		});
	}

    public boolean canEnterAlias() {
        boolean enabled = false;

        if (sourceTree.getSelectionCount() != 0) {
            TreeItem item = sourceTree.getSelection()[0];
            if (item.getData() != null) {
                enabled = true;
            }
        }

        return enabled;
    }

    public boolean canSave() {
        boolean enabled = false;

        if (sourceTree.getSelectionCount() != 0) {
            TreeItem item = sourceTree.getSelection()[0];
            if (item.getData() != null && !"".equals(aliasText.getText())) {
                enabled = true;
            }
        }

        return enabled;
    }

    public void setSourceConfigs(Collection sources) {
        sourceTree.removeAll();
        items.clear();

        for (Iterator i=sources.iterator(); i.hasNext(); ) {
            SourceConfig sourceDefinition = (SourceConfig)i.next();
            TreeItem ti = new TreeItem(sourceTree, SWT.NONE);
            ti.setText(sourceDefinition.getName());
            ti.setData(sourceDefinition);
            ti.setImage(PenrosePlugin.getImage(PenroseImage.SOURCE));
            items.put(sourceDefinition.getName(), ti);
        }
    }

    public boolean isSaved() {
        return saved;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }

    public SourceMapping getSourceMapping() {
        return source;
    }

    public void setSourceMapping(SourceMapping source) {
        this.source = source;

        TreeItem item = (TreeItem)items.get(source.getSourceName());
        if (item != null) {
            sourceTree.setSelection(new TreeItem[] { item });
        }
        aliasText.setText(source.getName() == null ? "" : source.getName());

        requiredCheckbox.setSelection(source.isRequired());
        addCheckbox.setSelection(source.isIncludeOnAdd());
        modifyCheckbox.setSelection(source.isIncludeOnAdd());
        modrdnCheckbox.setSelection(source.isIncludeOnModRdn());
        deleteCheckbox.setSelection(source.isIncludeOnDelete());
        proxyCheckbox.setSelection(source.isProxy());

        aliasText.setEnabled(canEnterAlias());
        saveButton.setEnabled(canSave());
    }
}
