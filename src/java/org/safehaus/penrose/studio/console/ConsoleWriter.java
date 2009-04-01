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

import org.eclipse.swt.widgets.Text;

import java.io.Writer;
import java.io.IOException;

/**
 * @author Endi S. Dewata
 */
public class ConsoleWriter extends Writer {

    StringBuilder sb = new StringBuilder();
    Text text;
    int length = 2000;

    public ConsoleWriter() {
    }

    public void write(char cbuf[], int off, int len) throws IOException {
        final String string = new String(cbuf, off, len);
        sb.append(string);
        sb.delete(0, sb.length() > length ? sb.length() - length : 0);

        if (text == null) return;

        text.getDisplay().asyncExec(
           new Runnable() {
              public void run(){
                  text.append(string);
              }
           }
        );
    }

    public void flush() {
    }

    public void close() {
    }

    public Text getText() {
        return text;
    }

    public void setText(final Text text) {
        this.text = text;

        text.getDisplay().asyncExec(
           new Runnable() {
              public void run(){
                  text.append(sb.toString());
              }
           }
        );
    }
}
