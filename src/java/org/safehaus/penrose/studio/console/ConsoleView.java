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
package org.safehaus.penrose.studio.console;

import org.eclipse.ui.part.ViewPart;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.WriterAppender;

/**
 * @author Endi S. Dewata
 */
public class ConsoleView extends ViewPart {

    Composite parent;

    private Font font;
    private Text text;

    private ConsoleWriter writer;

    public ConsoleView() {
        writer = new ConsoleWriter();
        WriterAppender appender = new WriterAppender(new PatternLayout("%-20C{1} [%4L] %m%n"), writer);

        BasicConfigurator.configure(appender);
    }

    public void createPartControl(Composite parent) {
        this.parent = parent;
        parent.setLayout(new FillLayout());

        font = new Font(parent.getDisplay(), "Courier", 8, SWT.NONE);

        text = new Text(parent, SWT.MULTI | SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL);
        text.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
        text.setFont(font);
        text.setTextLimit(Text.LIMIT);

        writer.setText(text);
    }

    public void setFocus() {
        parent.setFocus();
    }

    public void dispose() {
        writer.setText(null);
        text.dispose();
        font.dispose();
    }

    public Text getText() {
        return text;
    }

    public void setText(Text text) {
        this.text = text;
    }
}
