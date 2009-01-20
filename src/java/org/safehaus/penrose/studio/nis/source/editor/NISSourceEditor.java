package org.safehaus.penrose.studio.nis.source.editor;

import org.safehaus.penrose.studio.source.editor.SourceEditor;
import org.safehaus.penrose.studio.source.editor.SourceBrowsePage;
import org.safehaus.penrose.studio.source.editor.SourcePropertiesPage;
import org.safehaus.penrose.studio.source.editor.SourceFieldsPage;
import org.safehaus.penrose.studio.config.editor.ParametersPage;

public class NISSourceEditor extends SourceEditor {

    ParametersPage parametersPage;

    public void addPages() {
        try {
            addPage(new SourcePropertiesPage(this));
            addPage(new NISSourcePropertyPage(this));
            addPage(new SourceFieldsPage(this));

            parametersPage = new ParametersPage(this, "Source Editor");
            parametersPage.setParameters(sourceConfig.getParameters());
            addPage(parametersPage);

            addPage(new SourceBrowsePage(this));

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
