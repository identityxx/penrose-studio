package org.safehaus.penrose.studio.plugin;

import org.safehaus.penrose.studio.source.editor.JDBCSourceEditor;
import org.safehaus.penrose.studio.source.editor.SourceEditorInput;
import org.safehaus.penrose.studio.connection.editor.ConnectionEditorInput;

/**
 * @author Endi S. Dewata
 */
public class Plugin {

    private PluginConfig pluginConfig;

    public String getName() {
        return pluginConfig.getName();
    }
    
    public ConnectionEditorInput createConnectionEditorInput() {
        return new ConnectionEditorInput();
    }

    public String getConnectionEditorClass() {
        return null;
    }

    public SourceEditorInput createSourceEditorInput() {
        return new SourceEditorInput();
    }

    public String getSourceEditorClass() {
        return null;
    }

    public PluginConfig getPluginConfig() {
        return pluginConfig;
    }

    public void setPluginConfig(PluginConfig pluginConfig) {
        this.pluginConfig = pluginConfig;
    }
}
