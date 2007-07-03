package org.safehaus.penrose.studio.plugin;

import java.util.Map;
import java.util.HashMap;

/**
 * @author Endi S. Dewata
 */
public class PluginManager {

    public Map<String,Plugin> plugins = new HashMap<String,Plugin>();

    public void init(PluginConfig pluginConfig) throws Exception {
        Plugin plugin = (Plugin)plugins.get(pluginConfig.getName());
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
        return (Plugin)plugins.get(name);
    }
}
