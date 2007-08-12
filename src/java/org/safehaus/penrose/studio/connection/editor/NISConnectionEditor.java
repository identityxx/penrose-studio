package org.safehaus.penrose.studio.connection.editor;

import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.PenroseWorkbenchAdvisor;
import org.safehaus.penrose.studio.PenroseWorkbenchWindowAdvisor;
import org.safehaus.penrose.studio.PenroseActionBarAdvisor;

/**
 * @author Endi S. Dewata
 */
public class NISConnectionEditor extends ConnectionEditor {

    public void addPages() {
        try {
            addPage(new NISConnectionPropertiesPage(this));

            //PenroseStudio penroseStudio = PenroseStudio.getInstance();
            //PenroseWorkbenchAdvisor workbenchAdvisor = penroseStudio.getWorkbenchAdvisor();
            //PenroseWorkbenchWindowAdvisor workbenchWindowAdvisor = workbenchAdvisor.getWorkbenchWindowAdvisor();
            //PenroseActionBarAdvisor actionBarAdvisor = workbenchWindowAdvisor.getActionBarAdvisor();

            //if (actionBarAdvisor.getShowCommercialFeaturesAction().isChecked()) {

                //addPage(new JNDIConnectionBrowserPage(this));
                //addPage(new JNDIConnectionSchemaPage(this));
            //}

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
