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
package org.safehaus.penrose.studio.welcome;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.SWT;
import org.eclipse.ui.forms.widgets.*;
import org.eclipse.ui.part.*;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenrosePlugin;

/**
 * @author Endi S. Dewata
 */
public class WelcomeEditor extends EditorPart {

	FormToolkit toolkit;
    Image image;
    Font font;

    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        setSite(site);
        setInput(input);
    }

	public void createPartControl(Composite parent) {
		toolkit = new FormToolkit(parent.getDisplay());

        ScrolledForm form  = toolkit.createScrolledForm(parent);
        //form.setText("Welcome to Penrose Studio");

		TableWrapLayout layout = new TableWrapLayout();
		form.getBody().setLayout(layout);

        //GridLayout layout = new GridLayout();
        //layout.marginHeight = 10;
        //layout.marginWidth = 10;
        //parent.setLayout(layout);

        //ScrolledFormText text = new ScrolledFormText(parent, true);
        //text.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));

        //GridData gd = new GridData(GridData.FILL);
        //text.setLayoutData(gd);

        //FormText ft = text.getFormText();
        FormText ft = toolkit.createFormText(form.getBody(), true);

        TableWrapData twd = new TableWrapData(TableWrapData.FILL, TableWrapData.FILL);
        ft.setLayoutData(twd);

        image = PenrosePlugin.getImage(PenroseImage.WELCOME);
        ft.setImage("welcome", image);

        font = new Font(parent.getDisplay(), "Arial", 16, SWT.BOLD);
        ft.setFont("font", font);

        StringBuffer sb = new StringBuffer();
        sb.append("<form>");
        sb.append("<p><span font=\"font\">Welcome to Penrose Studio</span></p>");
        sb.append("<p><img href=\"welcome\"/></p>");
        sb.append("</form>");
        ft.setText(sb.toString(), true, true);
        //text.setText(sb.toString());
	}

	public void setFocus() {
	}

	public void dispose() {
        if (font != null) font.dispose();
		if (toolkit != null) toolkit.dispose();
		super.dispose();
	}

    public void doSave(IProgressMonitor iProgressMonitor) {
    }

    public void doSaveAs() {
    }

    public boolean isDirty() {
        return false;
    }

    public boolean isSaveAsAllowed() {
        return false;
    }
}

