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
package org.safehaus.penrose.studio.federation.linking;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;
import org.safehaus.penrose.federation.IdentityLinkingException;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;

public class ImportErrorDialog extends Dialog {

    Logger log = Logger.getLogger(getClass());

    public final static int CANCEL = 0;
    public final static int SKIP = 1;
    public final static int EDIT = 2;

    Shell shell;

	Label messageText;

    private IdentityLinkingException exception;

    int action;

	public ImportErrorDialog(Shell parent, int style) {
        super(parent, style);
    }

    public int open() {

        shell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);
        createControl(shell);
        init();

        Point size = new Point(400, 200);
        shell.setSize(size);

        Point l = getParent().getLocation();
        Point s = getParent().getSize();

        shell.setLocation(l.x + (s.x - size.x)/2, l.y + (s.y - size.y)/2);

        shell.setText(getText());
        shell.setImage(PenroseStudio.getImage(PenroseImage.LOGO));

        shell.open();

        Display display = getParent().getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }

        return action;
    }

    public void createControl(final Shell parent) {
        parent.setLayout(new GridLayout());

        messageText = new Label(parent, SWT.NONE);
        GridData gd = new GridData(GridData.FILL_BOTH);
        messageText.setLayoutData(gd);

        new Label(parent, SWT.NONE);

        Composite buttonsPanel = createButtonsPanel(parent);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalAlignment = GridData.END;
        buttonsPanel.setLayoutData(gd);
    }

    public Composite createButtonsPanel(final Shell parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new RowLayout());

        Button editButton = new Button(composite, SWT.PUSH);
        editButton.setText("  Edit ");

        editButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                action = EDIT;
                shell.close();
            }
        });

        Button skipButton = new Button(composite, SWT.PUSH);
        skipButton.setText("  Skip  ");

        skipButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                action = SKIP;
                shell.close();
            }
        });

        Button cancelButton = new Button(composite, SWT.PUSH);
        cancelButton.setText("  Cancel  ");

        cancelButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                action = CANCEL;
                shell.close();
            }
        });

        return composite;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public IdentityLinkingException getException() {
        return exception;
    }

    public void setException(IdentityLinkingException exception) {
        this.exception = exception;
    }

    public void init() {
        StringBuilder sb = new StringBuilder();
        sb.append("Failed importing ");
        sb.append(exception.getSourceDn());
        sb.append(":\n");
        sb.append(exception.getReason());
        messageText.setText(sb.toString());
    }
}