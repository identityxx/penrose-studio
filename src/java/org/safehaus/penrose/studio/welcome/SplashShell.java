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

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenrosePlugin;

public class SplashShell {
	
	Logger logger = Logger.getLogger(getClass());
	
	private Shell shell;
    Image image;

	public SplashShell() {

        image = PenrosePlugin.getImage(PenroseImage.SPLASH);

		shell = new Shell(SWT.NONE | SWT.APPLICATION_MODAL);
		shell.setLayout(new FillLayout());

        final Canvas canvas = new Canvas(shell, SWT.NONE);

        canvas.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent event) {
                shell.dispose();
            }
        });

        canvas.addMouseListener(new MouseAdapter() {
            public void mouseDown(MouseEvent event) {
                shell.dispose();
            }
        });

        canvas.addPaintListener(new PaintListener() {
            public void paintControl(PaintEvent event) {
                Rectangle imageBounds = image.getBounds();
                Rectangle canvasBounds = canvas.getBounds();

                GC gc = new GC(canvas);
                gc.drawImage(image, (canvasBounds.width-imageBounds.width)/2, (canvasBounds.height-imageBounds.height)/2);
                gc.dispose();
            }
        });

		Display display = shell.getDisplay();
		Monitor monitor = display.getMonitors()[0];
		logger.debug(display.getBounds().toString());
		shell.setBounds(monitor.getBounds().width / 2
				- image.getBounds().width / 2, monitor.getBounds().height / 2
				- image.getBounds().height / 2, image.getBounds().width, image
				.getBounds().height);

	}

	public void open() {
		shell.open();
	}
	
	public void addDisposeListener(DisposeListener l) {
		shell.addDisposeListener(l);
	}
	
	public void dispose() {
		shell.dispose();
	}

    public Shell getShell() {
        return shell;
    }

    public void setShell(Shell shell) {
        this.shell = shell;
    }
}