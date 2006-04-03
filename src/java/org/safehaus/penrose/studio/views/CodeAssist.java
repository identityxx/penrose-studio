package org.safehaus.penrose.studio.views;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.FillLayout;

public class CodeAssist {

    Text text;

    Table table;

    Shell shell;

    boolean ctrlPressed;

    public CodeAssist(Shell parent, final Text text) {
        this.text = text;

        Point location = text.getCaretLocation();
        for (Control c=text; c != null; c=c.getParent()) {
            location = add(location, c.getLocation());
            if (c instanceof Shell) break;
        }

        if (shell == null || shell.isDisposed()) {

            shell = new Shell(parent, SWT.RESIZE);
            shell.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
            shell.setLayout(new FillLayout());

            shell.addShellListener(new ShellAdapter() {
                public void shellDeactivated(ShellEvent e) {
                    shell.dispose();
                }
            });

            table = new Table(shell, SWT.H_SCROLL | SWT.V_SCROLL);

            table.addKeyListener(new KeyListener() {
                public void keyPressed(KeyEvent e) {
                    switch (e.keyCode) {
                        case SWT.CTRL:
                            ctrlPressed = true;
                            break;
                    }
                }
                public void keyReleased(KeyEvent e) {
                    switch (e.keyCode) {
                        case SWT.CR:
                            // add selected item to expression at caret location
                            if (table.getSelectionCount() > 0) {
                                String s = table.getSelection()[0].getText();
                                text.insert(s);
                                shell.dispose();
                            }
                            break;
                        case SWT.CTRL:
                            ctrlPressed = false;
                            break;
                    }
                }
            });
        }

        location = add(location, new Point(10, 42));
        shell.setSize(200, 200);
        shell.setLocation(location);
        shell.open();
    }

    public void open() {
        shell.open();
    }

    public void removeAllHints() {
        table.removeAll();
    }

    public void addHint(Image icon, String text) {
        TableItem item = new TableItem(table, SWT.NONE);
        item.setText(text);
        item.setImage(icon);
    }

	Point add(Point a, Point b) {
		return new Point(a.x + b.x, a.y + b.y);
	}
}

