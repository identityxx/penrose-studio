package org.safehaus.penrose.studio.nis.source.editor;

import org.safehaus.penrose.studio.source.editor.*;
import org.safehaus.penrose.studio.config.editor.ParametersPage;
import org.safehaus.penrose.studio.dialog.ErrorDialog;

public class NISSourceEditor extends SourceEditor {

    public void addPages() {
        try {
            addPage(new SourcePropertiesPage(this));
            addPage(new NISSourcePropertyPage(this));
            addPage(new NISSourceFieldsPage(this));

            ParametersPage parametersPage = new SourceParametersPage(this);
            parametersPage.setParameters(sourceConfig.getParameters());
            addPage(parametersPage);

            addPage(new SourceBrowsePage(this));

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }
}
