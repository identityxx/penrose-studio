package org.safehaus.penrose.studio.plugin;

import org.safehaus.penrose.studio.source.editor.JDBCSourceEditorInput;
import org.safehaus.penrose.studio.source.editor.JDBCSourceEditor;
import org.safehaus.penrose.studio.source.editor.SourceEditorInput;

/**
 * @author Endi S. Dewata
 */
public class Plugin {

    private PluginConfig pluginConfig;

    public String getName() {
        return pluginConfig.getName();
    }
    
    public SourceEditorInput createSourceEditorInput() {
        return new JDBCSourceEditorInput();
    }

    public String getSourceEditorClass() {
        return JDBCSourceEditor.class.getName();
    }

    public PluginConfig getPluginConfig() {
        return pluginConfig;
    }

    public void setPluginConfig(PluginConfig pluginConfig) {
        this.pluginConfig = pluginConfig;
    }
}
