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
import org.safehaus.penrose.acl.ACI;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenrosePlugin;

/**
 * @author Endi S. Dewata
 */
public class ACIDialog extends Dialog {

    Shell shell;

    Combo subjectCombo;
    Text dnText;
    Combo targetCombo;
    Text attributesText;

    //Combo scopeCombo;
    Button propagateCheckbox;

    Combo actionCombo;

    Button readCheckbox;
    Button searchCheckbox;
    Button writeCheckbox;
    Button addCheckbox;
    Button deleteCheckbox;

    private String subject;
    private String dn;
    private String target;
    private String attributes;
    private String scope;
    private String action;
    private String permission;

    private boolean saved;

	public ACIDialog(Shell parent, int style) {
		super(parent, style);

        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);

        createControl(shell);
    }

    public void open () {

        Point size = new Point(400, 350);
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

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        composite.setLayout(new GridLayout(3, false));

        Label subjectLabel = new Label(composite, SWT.NONE);
        subjectLabel.setText("Subject:");

        GridData gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        subjectLabel.setLayoutData(gd);

        subjectCombo = new Combo(composite, SWT.READ_ONLY);
        subjectCombo.add(ACI.SUBJECT_ANYBODY);
        subjectCombo.add(ACI.SUBJECT_ANONYMOUS);
        subjectCombo.add(ACI.SUBJECT_AUTHENTICATED);
        subjectCombo.add(ACI.SUBJECT_SELF);
        subjectCombo.add(ACI.SUBJECT_USER);
        //subjectCombo.add(ACI.SUBJECT_GROUP);
        subjectCombo.setText(ACI.SUBJECT_ANYBODY);

        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        subjectCombo.setLayoutData(gd);

        subjectCombo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                if (ACI.SUBJECT_USER.equals(subjectCombo.getText()) || ACI.SUBJECT_GROUP.equals(subjectCombo.getText())) {
                    dnText.setEnabled(true);
                } else {
                    dnText.setEnabled(false);
                    dnText.setText("");
                }
            }
        });

        Label dnLabel = new Label(composite, SWT.NONE);
        dnLabel.setText("User/Group DN:");

        dnText = new Text(composite, SWT.BORDER);
        dnText.setEnabled(false);

        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        dnText.setLayoutData(gd);

        Label targetLabel = new Label(composite, SWT.NONE);
        targetLabel.setText("Target:");

        targetCombo = new Combo(composite, SWT.READ_ONLY);
        targetCombo.add(ACI.TARGET_OBJECT);
        targetCombo.add(ACI.TARGET_ATTRIBUTES);
        targetCombo.setText(ACI.TARGET_OBJECT);

        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        targetCombo.setLayoutData(gd);
/*
        targetCombo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                if (ACI.TARGET_ATTRIBUTES.equals(targetCombo.getText())) {
                    attributesText.setEnabled(true);
                } else {
                    attributesText.setText("");
                    attributesText.setEnabled(false);
                }
            }
        });
*/
        targetCombo.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                if (ACI.TARGET_ATTRIBUTES.equals(targetCombo.getText())) {
                    attributesText.setEnabled(true);
                } else {
                    attributesText.setText("");
                    attributesText.setEnabled(false);
                }
            }
        });

        Label attributesLabel = new Label(composite, SWT.NONE);
        attributesLabel.setText("Attributes:");

        attributesText = new Text(composite, SWT.BORDER);
        attributesText.setEnabled(false);

        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        attributesText.setLayoutData(gd);

        Label scopeLabel = new Label(composite, SWT.NONE);
        scopeLabel.setText("Propagate ACL:");
/*
        scopeCombo = new Combo(composite, SWT.READ_ONLY);
        scopeCombo.add(ACI.SCOPE_OBJECT);
        scopeCombo.add(ACI.SCOPE_SUBTREE);
        scopeCombo.setText(ACI.SCOPE_SUBTREE);

        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        scopeCombo.setLayoutData(gd);
*/
        propagateCheckbox = new Button(composite, SWT.CHECK);
        propagateCheckbox.setSelection(true);

        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        propagateCheckbox.setLayoutData(gd);

        Label actionLabel = new Label(composite, SWT.NONE);
        actionLabel.setText("Action:");

        actionCombo = new Combo(composite, SWT.READ_ONLY);
        actionCombo.add(ACI.ACTION_GRANT);
        actionCombo.add(ACI.ACTION_DENY);
        actionCombo.setText(ACI.ACTION_GRANT);

        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        actionCombo.setLayoutData(gd);

        Label permissionLabel = new Label(composite, SWT.NONE);
        permissionLabel.setText("Permission:");

        readCheckbox = new Button(composite, SWT.CHECK);
        readCheckbox.setText("Read");
        readCheckbox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        readCheckbox.setSelection(true);

        writeCheckbox = new Button(composite, SWT.CHECK);
        writeCheckbox.setText("Write");
        writeCheckbox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        new Label(composite, SWT.NONE);

        searchCheckbox = new Button(composite, SWT.CHECK);
        searchCheckbox.setText("Search");
        searchCheckbox.setSelection(true);

        addCheckbox = new Button(composite, SWT.CHECK);
        addCheckbox.setText("Add Children");

        new Label(composite, SWT.NONE);

        new Label(composite, SWT.NONE);

        deleteCheckbox = new Button(composite, SWT.CHECK);
        deleteCheckbox.setText("Delete");

        Composite buttons = new Composite(parent, SWT.NONE);
        buttons.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        buttons.setLayout(new RowLayout());

		Button saveButton = new Button(buttons, SWT.PUSH);
        saveButton.setText("Save");

		saveButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
                subject = subjectCombo.getText();
                dn = "".equals(dnText.getText()) ? null : dnText.getText();
                target = targetCombo.getText();
                attributes = "".equals(attributesText.getText()) ? null : attributesText.getText();

                if (propagateCheckbox.getSelection()) {
                    scope = ACI.SCOPE_SUBTREE;
                } else {
                    scope = ACI.SCOPE_OBJECT;
                }

                //scope = scopeCombo.getText();
                action = actionCombo.getText();

                StringBuffer sb = new StringBuffer();
                if (readCheckbox.getSelection()) sb.append(ACI.PERMISSION_READ);
                if (searchCheckbox.getSelection()) sb.append(ACI.PERMISSION_SEARCH);
                if (writeCheckbox.getSelection()) sb.append(ACI.PERMISSION_WRITE);
                if (addCheckbox.getSelection()) sb.append(ACI.PERMISSION_ADD);
                if (deleteCheckbox.getSelection()) sb.append(ACI.PERMISSION_DELETE);
                permission = sb.toString();

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

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
        subjectCombo.setText(subject);
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
        //scopeCombo.setText(scope);
        propagateCheckbox.setSelection(ACI.SCOPE_SUBTREE.equals(scope));
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
        actionCombo.setText(action);
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
        readCheckbox.setSelection(permission.indexOf(ACI.PERMISSION_READ) >= 0);
        searchCheckbox.setSelection(permission.indexOf(ACI.PERMISSION_SEARCH) >= 0);
        writeCheckbox.setSelection(permission.indexOf(ACI.PERMISSION_WRITE) >= 0);
        addCheckbox.setSelection(permission.indexOf(ACI.PERMISSION_ADD) >= 0);
        deleteCheckbox.setSelection(permission.indexOf(ACI.PERMISSION_DELETE) >= 0);
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
        targetCombo.setText(target);
    }

    public String getAttributes() {
        return attributes;
    }

    public void setAttributes(String attributes) {
        this.attributes = attributes;
        attributesText.setText(attributes == null ? "" : attributes);
    }

    public String getDn() {
        return dn;
    }

    public void setDn(String dn) {
        this.dn = dn;
        dnText.setText(dn == null ? "" : dn);
    }

    public boolean isSaved() {
        return saved;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }
}
