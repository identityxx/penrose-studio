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

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.jface.resource.ImageDescriptor;
import org.safehaus.penrose.mapping.EntryMapping;
import org.safehaus.penrose.partition.Partition;

/**
 * @author Endi S. Dewata
 */
public class MappingEditorInput implements IEditorInput {

    private Partition partition;
    private EntryMapping entryDefinition;

    public MappingEditorInput(Partition partition, EntryMapping entryDefinition) {
        this.partition = partition;
        this.entryDefinition = entryDefinition;
    }

    public boolean exists() {
        return true;
    }

    public ImageDescriptor getImageDescriptor() {
        return null;
    }

    public String getName() {
        return entryDefinition.getDn().toString();
    }

    public IPersistableElement getPersistable() {
        return null;
    }

    public String getToolTipText() {
        return entryDefinition.getDn().toString();
    }

    public Object getAdapter(Class aClass) {
        return null;
    }

    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof MappingEditorInput)) return false;

        MappingEditorInput cei = (MappingEditorInput)o;
        return entryDefinition.equals(cei.entryDefinition);
    }

    public EntryMapping getEntryDefinition() {
        return entryDefinition;
    }

    public void setEntryDefinition(EntryMapping entryDefinition) {
        this.entryDefinition = entryDefinition;
    }

    public Partition getPartition() {
        return partition;
    }

    public void setPartition(Partition partition) {
        this.partition = partition;
    }
}
