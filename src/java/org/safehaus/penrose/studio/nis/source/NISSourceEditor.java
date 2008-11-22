package org.safehaus.penrose.studio.nis.source;

import org.safehaus.penrose.studio.source.editor.SourceEditor;

public class NISSourceEditor extends SourceEditor {

    public void addPages() {
        try {
            addPage(new NISSourcePropertyPage(this));
            addPage(new NISSourceBrowsePage(this));
            //addPage(new LDAPSourceCachePage(this));

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
