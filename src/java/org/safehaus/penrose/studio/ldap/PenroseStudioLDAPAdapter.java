package org.safehaus.penrose.studio.ldap;

import org.safehaus.penrose.studio.source.editor.*;
import org.safehaus.penrose.studio.source.wizard.LDAPSourceWizard;
import org.safehaus.penrose.studio.connection.editor.*;
import org.safehaus.penrose.studio.adapter.PenroseStudioAdapter;
import org.safehaus.penrose.studio.ldap.connection.ConnectionBrowserPage;
import org.safehaus.penrose.studio.ldap.connection.ConnectionPropertiesPage;
import org.safehaus.penrose.studio.ldap.connection.ConnectionSchemaPage;
import org.safehaus.penrose.studio.ldap.source.SourceBrowsePage;
import org.safehaus.penrose.studio.ldap.source.SourcePropertiesPage;

import java.util.Collection;
import java.util.ArrayList;

/**
 * @author Endi S. Dewata
 */
public class PenroseStudioLDAPAdapter extends PenroseStudioAdapter {

    public PenroseStudioLDAPAdapter() {
    }

    public PenroseStudioLDAPAdapter(String name) {
        super(name);
    }

    public String getSourceWizardClassName() {
        return LDAPSourceWizard.class.getName();
    }

    public Collection createConnectionEditorPages(ConnectionEditor editor) {
        Collection pages = new ArrayList();

        pages.add(new ConnectionPropertiesPage(editor));
        pages.add(new ConnectionBrowserPage(editor));
        pages.add(new ConnectionSchemaPage(editor));

        return pages;
    }

    public Collection createSourceEditorPages(SourceEditor editor) {
        Collection pages = new ArrayList();

        pages.add(new SourcePropertiesPage(editor));
        pages.add(new SourceBrowsePage(editor));

        return pages;
    }
}
