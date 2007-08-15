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

import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

public class PenroseStudioWorkbenchAdvisor extends WorkbenchAdvisor {

    private PenroseStudioWorkbenchWindowAdvisor workbenchWindowAdvisor;

    public PenroseStudioWorkbenchAdvisor() {
    }

    public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
        return workbenchWindowAdvisor = new PenroseStudioWorkbenchWindowAdvisor(configurer);
    }

	public String getInitialWindowPerspectiveId() {
        return PenroseStudioPerspective.PERSPECTIVE_ID;
	}

    public PenroseStudioWorkbenchWindowAdvisor getWorkbenchWindowAdvisor() {
        return workbenchWindowAdvisor;
    }
}
