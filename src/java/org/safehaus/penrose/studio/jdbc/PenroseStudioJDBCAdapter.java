package org.safehaus.penrose.studio.jdbc;

import org.safehaus.penrose.studio.source.editor.*;
import org.safehaus.penrose.studio.source.wizard.JDBCSourceWizard;
import org.safehaus.penrose.studio.connection.editor.*;
import org.safehaus.penrose.studio.adapter.PenroseStudioAdapter;
import org.safehaus.penrose.studio.jdbc.connection.ConnectionPropertiesPage;
import org.safehaus.penrose.studio.jdbc.connection.ConnectionTablesPage;
import org.safehaus.penrose.studio.jdbc.source.SourcePropertiesPage;
import org.safehaus.penrose.studio.jdbc.source.SourceCachePage;
import org.safehaus.penrose.studio.jdbc.source.SourceBrowsePage;

import java.util.Collection;
import java.util.ArrayList;

/**
 * @author Endi S. Dewata
 */
public class PenroseStudioJDBCAdapter extends PenroseStudioAdapter {

    public PenroseStudioJDBCAdapter() {
    }

    public PenroseStudioJDBCAdapter(String name) {
        super(name);
    }

    public String getSourceWizardClassName() {
        return JDBCSourceWizard.class.getName();
    }

    public Collection createConnectionEditorPages(ConnectionEditor editor) {
        Collection pages = new ArrayList();

        pages.add(new ConnectionPropertiesPage(editor));
        pages.add(new ConnectionTablesPage(editor));

        return pages;
    }

    public Collection createSourceEditorPages(SourceEditor editor) {
        Collection pages = new ArrayList();

        pages.add(new SourcePropertiesPage(editor));
        pages.add(new SourceBrowsePage(editor));
        pages.add(new SourceCachePage(editor));

        return pages;
    }
}
