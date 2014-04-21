package com.atlassian.jira.plugin.util;

import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.Resources;
import com.atlassian.plugin.elements.ResourceDescriptor;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * This is a glorified list of plugin keys that code can use to track what plugins are involved in its caches
 * <p/>
 * On plugin events it can then ask if the event related to one of the tracked plugins
 * <p/>
 * This uses a {@link CopyOnWriteArraySet} under the covers to ensure that the list is as safe as possible.  The
 * assumption is that the reads and writes will be of low volume and the total number of plugins tracked will be
 * smallish.  In other words its anticipated that it will just work!
 *
 * @since v4.4
 */
public class InvolvedPluginsTracker
{
    /**
     * A simple class that contains plugin key and pluginVersion.
     */
    public static class PluginInfo implements Comparable<PluginInfo>
    {

        private final String pluginKey;
        private final String pluginVersion;

        public PluginInfo(String pluginKey, String pluginVersion)
        {
            this.pluginKey = Assertions.notNull("pluginKey", pluginKey);
            this.pluginVersion = Assertions.notNull("pluginVersion", pluginVersion);
        }

        public String getPluginKey()
        {
            return pluginKey;
        }

        public String getPluginVersion()
        {
            return pluginVersion;
        }

        @Override
        public int compareTo(PluginInfo that)
        {
            int rc = pluginKey.compareTo(that.pluginKey);
            if (rc == 0)
            {
                rc = pluginVersion.compareTo(that.pluginVersion);
            }
            return rc;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            PluginInfo that = (PluginInfo) o;

            return pluginKey.equals(that.pluginKey) && pluginVersion.equals(that.pluginVersion);

        }

        @Override
        public int hashCode()
        {
            int result = pluginKey.hashCode();
            result = 31 * result + pluginVersion.hashCode();
            return result;
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this);
        }
    }

    private final Set<PluginInfo> pluginsInvolved;


    public InvolvedPluginsTracker()
    {
        this.pluginsInvolved = new CopyOnWriteArraySet<PluginInfo>();
    }

    private PluginInfo toInfo(Plugin plugin)
    {
        return new PluginInfo(plugin.getKey(), plugin.getPluginInformation().getVersion());
    }

    /**
     * Tracks a plugin as being involved
     *
     * @param plugin the plugin in play
     */
    public void trackInvolvedPlugin(Plugin plugin)
    {
        pluginsInvolved.add(toInfo(notNull("plugin", plugin)));
    }

    /**
     * Tracks a plugin as being involved via its {@link ModuleDescriptor}
     *
     * @param moduleDescriptor the ModuleDescriptor of the plugin in play
     */
    public void trackInvolvedPlugin(ModuleDescriptor moduleDescriptor)
    {
        pluginsInvolved.add(toInfo(notNull("moduleDescriptor", moduleDescriptor).getPlugin()));
    }

    /**
     * Returns true if the plugin is being tracked
     *
     * @param plugin the plugin in play
     * @return true if the underlying plugin is being tracked
     */
    public boolean isPluginInvolved(Plugin plugin)
    {
        return pluginsInvolved.contains(toInfo(notNull("plugin", plugin)));
    }

    /**
     * Returns true if the plugin that this ModuleDescriptor belongs to is being tracked
     *
     * @param moduleDescriptor the ModuleDescriptor of the plugin in play
     * @return true if the underlying plugin is being tracked
     */
    public boolean isPluginInvolved(ModuleDescriptor moduleDescriptor)
    {
        return pluginsInvolved.contains(toInfo(notNull("moduleDescriptor", moduleDescriptor).getPlugin()));
    }

    /**
     * Returns true if the plugin pointed to by the moduleDescriptor contains in it a 1 or modules with the target
     * module descriptor class.
     *
     * @param moduleDescriptor the module descriptor in play (typically from a plugin event)
     * @param targetModuleClass the target capabilities you want to test
     * @return true if the underlying plugin has the designed module descriptor class
     */
    public boolean isPluginWithModuleDescriptor(ModuleDescriptor moduleDescriptor, Class<? extends ModuleDescriptor> targetModuleClass)
    {
        return isPluginWithModuleDescriptor(notNull("moduleDescriptor", moduleDescriptor).getPlugin(), targetModuleClass);
    }

    /**
     * Returns true if the plugin pointed to by the moduleDescriptor contains in it a 1 or module descriptors with the target
     * module descriptor class.
     *
     * @param plugin the plugin play (typically from a plugin event)
     * @param targetModuleClass the target capabilities you want to test
     * @return true if the underlying plugin has the designed module descriptor class
     */
    public boolean isPluginWithModuleDescriptor(Plugin plugin, Class<? extends ModuleDescriptor> targetModuleClass)
    {
        for (final ModuleDescriptor<?> moduleDescriptor : plugin.getModuleDescriptors())
        {
            final Class<?> moduleClass = moduleDescriptor.getClass();
            if (targetModuleClass.isAssignableFrom(moduleClass))
            {
                return true;
            }
        }
        return false;
    }


    /**
     * Returns true if the plugin contains resources of the specified type, for example "i18n" resource types
     *
     * @param plugin the plugin play (typically from a plugin event)
     * @param pluginResourceType the descriptive name of the resource type (for example "i18n")
     * @return true if the plugin cvontains resources of the specified type
     */
    public boolean isPluginWithResourceType(Plugin plugin, String pluginResourceType)
    {
        final Resources.TypeFilter filter = new Resources.TypeFilter(pluginResourceType);
        if (Iterables.any(plugin.getResourceDescriptors(), filter))
        {
            return true;
        }
        for (final ModuleDescriptor<?> moduleDescriptor : plugin.getModuleDescriptors())
        {
            if (Iterables.any(moduleDescriptor.getResourceDescriptors(), filter))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if the underlying plugin contains resources of the specified type, for example "i18n" resource
     * types
     *
     * @param moduleDescriptor the module descriptor of the plugin play (typically from a plugin event)
     * @param pluginResourceType the descriptive name of the resource type (for example "i18n")
     * @return true if the plugin cvontains resources of the specified type
     */
    public boolean isPluginWithResourceType(ModuleDescriptor moduleDescriptor, String pluginResourceType)
    {
        return isPluginWithResourceType(notNull("moduleDescriptor", moduleDescriptor).getPlugin(), pluginResourceType);
    }

    /**
     * @return a copy of the underlying tracked plugin keys
     */
    public Set<PluginInfo> getInvolvedPluginKeys()
    {
        return Sets.newLinkedHashSet(pluginsInvolved);
    }

    /**
     * Clear the underlying set of tracked plugins
     */
    public void clear()
    {
        pluginsInvolved.clear();
    }

    /**
     * The hashcode of this object will be stable so long it is tracking the same versions of the same plugins
     */
    @Override
    public int hashCode()
    {
        return pluginsInvolved.hashCode();
    }

    @Override
    public String toString()
    {
        return new StringBuilder(super.toString()).append(" ").append(pluginsInvolved.toString()).toString();
    }
}
