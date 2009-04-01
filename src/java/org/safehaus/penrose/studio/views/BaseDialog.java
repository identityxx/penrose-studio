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
package org.safehaus.penrose.studio.views;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

public class BaseDialog {
	
	protected Logger logger = Logger.getLogger(getClass());

	protected Shell parent;
	protected Shell shell;
	protected FormToolkit toolkit;
	protected ScrolledForm form;
	
	public BaseDialog(Shell parent) {
		this(parent, SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL);
	}
	
	public BaseDialog(Shell parent, int style) {
		this.parent = parent;
		// Shell
		shell = new Shell(parent, style);
		setSize(500, 500);
		shell.setLayout(new FillLayout());
	    // Toolkit and Form
		toolkit = new FormToolkit(shell.getDisplay());
		form = toolkit.createScrolledForm(shell);
	}
	
	public void open() {
		shell.open();
	}
	
	public void setSize(int x, int y) {
		Rectangle p = parent.getBounds();
		shell.setBounds(p.x + (p.width-x)/2, p.y + (p.height-y)/2, x, y);
		
	}
	
	

	public void addShellListener(ShellListener listener) {
		shell.addShellListener(listener);
	}
	public void addDisposeListener(DisposeListener listener) {
		shell.addDisposeListener(listener);
	}
}
