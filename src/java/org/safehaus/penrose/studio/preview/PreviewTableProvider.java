/**
 * Copyright (c) 2000-2005, Identyx Corporation.
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
package org.safehaus.penrose.studio.preview;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;
import org.safehaus.penrose.studio.util.Pair;
import org.apache.log4j.Logger;
import org.ietf.ldap.LDAPEntry;
import org.ietf.ldap.LDAPAttributeSet;
import org.ietf.ldap.LDAPAttribute;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Enumeration;

public class PreviewTableProvider extends LabelProvider implements ITableLabelProvider, ITreeContentProvider, ISelectionChangedListener, ICellModifier {

    private Logger log = Logger.getLogger(getClass());

    PreviewNode previewNode;

    public PreviewTableProvider(PreviewEditor previewEditor) {
    }

    public Image getColumnImage(Object element, int columnIndex) {
        return null;
    }

    public String getColumnText(Object element, int columnIndex) {
        Pair p = (Pair) element;
        switch (columnIndex) {
            case 0:
                return p.getName();
            case 1:
                return p.getValue();
        }
        return null;
    }

    public Object[] getChildren(Object parentElement) {
        return null;
    }

    public Object getParent(Object element) {
        return null;
    }

    public boolean hasChildren(Object element) {
        return false;
    }

    public Object[] getElements(Object inputElement) {
        Object[] result = new Object[0];
        if (previewNode == null) return result;

        try {
            Collection attributes = new ArrayList();
            LDAPEntry entry = (LDAPEntry)previewNode.getObject();
            LDAPAttributeSet set = entry.getAttributeSet();
            for (Iterator i=set.iterator(); i.hasNext(); ) {
                LDAPAttribute attribute = (LDAPAttribute)i.next();
                String name = attribute.getName();
                for (Enumeration e=attribute.getStringValues(); e.hasMoreElements(); ) {
                    String value = (String)e.nextElement();
                    Pair pair = new Pair(name, value);
                    attributes.add(pair);
                }
            }

            result = attributes.toArray();

        } catch (Exception ex) {
            log.debug(ex.toString(), ex);
        }
        return result;
    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

    public void selectionChanged(SelectionChangedEvent event) {
    }

    public boolean canModify(Object element, String property) {
        return false;
    }

    public Object getValue(Object element, String property) {
        return getColumnText(element, propertyToInt(property));
    }

    public void modify(Object element, String property, Object value) {
    }

    public int propertyToInt(String p) {
        if ("name".equals(p)) return 0;
        if ("value".equals(p)) return 1;
        return 1;
    }
}
