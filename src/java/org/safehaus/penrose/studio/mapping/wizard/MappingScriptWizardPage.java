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
package org.safehaus.penrose.studio.mapping.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.apache.log4j.Logger;

/**
 * @author Endi S. Dewata
 */
public class MappingScriptWizardPage extends WizardPage {

    Logger log = Logger.getLogger(getClass());

    public final static String NAME = "Mapping Script";

    Text scriptText;

    private String script;

    public MappingScriptWizardPage() {
        super(NAME);
        setDescription("Enter mapping script.");
    }

    public void createControl(final Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        composite.setLayout(new GridLayout(2, false));

        scriptText = new Text(composite, SWT.BORDER | SWT.MULTI);
        scriptText.setLayoutData(new GridData(GridData.FILL_BOTH));

        setPageComplete(validatePage());
    }

    public void setVisible(boolean b) {
        super.setVisible(b);
        if (b) refresh();
    }

    public void refresh() {
        scriptText.setText(script == null ? "" : script);
    }

    public boolean validatePage() {
        return true;
    }

    public String getScript() {
        String s = scriptText.getText();
        return "".equals(s) ? null : s;
    }

    public void setScript(String script) {
        this.script = script;
    }
}