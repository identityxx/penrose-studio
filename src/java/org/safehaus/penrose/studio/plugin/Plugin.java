package org.safehaus.penrose.studio.plugin;

import org.safehaus.penrose.studio.source.editor.SourceEditorInput;
import org.safehaus.penrose.studio.source.editor.SourceEditor;
import org.safehaus.penrose.studio.connection.editor.ConnectionEditorInput;
import org.safehaus.penrose.studio.connection.editor.ConnectionEditor;

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
        return ConnectionEditor.class.getName();
    }

    public SourceEditorInput createSourceEditorInput() {
        return new SourceEditorInput();
    }

    public String getSourceEditorClass() {
        return SourceEditor.class.getName();
    }

    public PluginConfig getPluginConfig() {
        return pluginConfig;
    }

    public void setPluginConfig(PluginConfig pluginConfig) {
        this.pluginConfig = pluginConfig;
    }
}
