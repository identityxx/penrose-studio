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
package org.safehaus.penrose.studio;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.safehaus.penrose.studio.object.ObjectsView;
import org.safehaus.penrose.studio.mapping.editor.MappingEditor;
import org.safehaus.penrose.studio.validation.ValidationView;
import org.safehaus.penrose.studio.console.ConsoleView;

public class PenrosePerspective implements IPerspectiveFactory {
	
	public final static String PERSPECTIVE_ID = PenrosePerspective.class.getName();

	public final static String ID_MAPPING_VIEW     = MappingEditor.class.getName();
	public final static String ID_OBJECTS_VIEW     = ObjectsView.class.getName();
	public final static String ID_VALIDATION_VIEW  = ValidationView.class.getName();
    public final static String ID_CONSOLE_VIEW      = ConsoleView.class.getName();

	public void createInitialLayout(IPageLayout layout) {

		layout.setEditorAreaVisible(true);

        String editorArea = layout.getEditorArea();

        IFolderLayout left = layout.createFolder("left", IPageLayout.LEFT, 0.26f, editorArea);
        left.addView(ID_OBJECTS_VIEW);

        IFolderLayout lowerCenter = layout.createFolder("lowerCenter", IPageLayout.BOTTOM, 0.80f, editorArea);
        lowerCenter.addView(ID_VALIDATION_VIEW);
        lowerCenter.addView(ID_CONSOLE_VIEW);
	}

}
