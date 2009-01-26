package org.safehaus.penrose.studio.plugin;

import java.util.Map;
import java.util.HashMap;

/**
 * @author Endi S. Dewata
 */
public class PluginManager {

    public final static Plugin DEFAULT_PLUGIN = new Plugin();

    public Map<String,Plugin> plugins = new HashMap<String,Plugin>();

    public void init(PluginConfig pluginConfig) throws Exception {
        Plugin plugin = plugins.get(pluginConfig.getName());
        if (plugin != null) return;

        String className = pluginConfig.getClassName();
        Class clazz = Class.forName(className);

        plugin = (Plugin)clazz.newInstance();
        plugin.setPluginConfig(pluginConfig);

        addPlugin(plugin);
    }

    public void addPlugin(Plugin plugin) {
        plugins.put(plugin.getName(), plugin);
    }

    public Plugin getPlugin(String name) {
        Plugin plugin = plugins.get(name);
        if (plugin == null) return DEFAULT_PLUGIN;
        return plugin;
    }
}
