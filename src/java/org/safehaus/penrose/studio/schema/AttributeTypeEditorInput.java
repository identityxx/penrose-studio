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
package org.safehaus.penrose.studio.schema;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.jface.resource.ImageDescriptor;
import org.safehaus.penrose.schema.AttributeType;

/**
 * @author Endi S. Dewata
 */
public class AttributeTypeEditorInput implements IEditorInput {

    private AttributeType attributeType;

    public AttributeTypeEditorInput(AttributeType attributeType) {
        this.attributeType = attributeType;
    }

    public boolean exists() {
        return true;
    }

    public ImageDescriptor getImageDescriptor() {
        return null;
    }

    public String getName() {
        return attributeType.getName();
    }

    public IPersistableElement getPersistable() {
        return null;
    }

    public String getToolTipText() {
        return attributeType.getName();
    }

    public Object getAdapter(Class aClass) {
        return null;
    }

    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof AttributeTypeEditorInput)) return false;

        AttributeTypeEditorInput cei = (AttributeTypeEditorInput)o;
        return attributeType.equals(cei.attributeType);
    }

    public AttributeType getAttributeType() {
        return attributeType;
    }

    public void setAttributeType(AttributeType attributeType) {
        this.attributeType = attributeType;
    }
}
