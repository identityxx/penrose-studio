package org.safehaus.penrose.studio.nis.connection;

import org.safehaus.penrose.studio.connection.editor.ConnectionEditor;

/**
 * @author Endi S. Dewata
 */
public class NISConnectionEditor extends ConnectionEditor {

    public void addPages() {
        try {
            addPage(new NISConnectionPropertiesPage(this));

            //PenroseStudio penroseStudio = PenroseStudio.getInstance();
            //PenroseStudioWorkbenchAdvisor workbenchAdvisor = penroseStudio.getWorkbenchAdvisor();
            //PenroseStudioWorkbenchWindowAdvisor workbenchWindowAdvisor = workbenchAdvisor.getWorkbenchWindowAdvisor();
            //PenroseStudioActionBarAdvisor actionBarAdvisor = workbenchWindowAdvisor.getActionBarAdvisor();

            //if (actionBarAdvisor.getShowCommercialFeaturesAction().isChecked()) {

                //addPage(new LDAPConnectionBrowserPage(this));
                //addPage(new LDAPConnectionSchemaPage(this));
            //}

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
