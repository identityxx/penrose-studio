package org.safehaus.penrose.studio.source.editor;

public class NISSourceEditor extends SourceEditor {

    public void addPages() {
        try {
            addPage(new NISSourcePropertyPage(this));
            addPage(new NISSourceBrowsePage(this));
            //addPage(new JNDISourceCachePage(this));

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
