package com.atlassian.jira.startup;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginInformation;
import com.atlassian.plugin.PluginState;
import com.atlassian.plugin.impl.UnloadablePlugin;
import com.atlassian.plugin.metadata.PluginMetadataManager;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * This provides information about the plugins that are provided in JIRA
 *
 * @since v4.3
 */
public class PluginInfoProviderImpl implements PluginInfoProvider
{
    private final PluginAccessor pluginAccessor;
    private final PluginMetadataManager pluginMetadataManager;

    public PluginInfoProviderImpl(final PluginAccessor pluginAccessor, final PluginMetadataManager pluginMetadataManager)
    {
        this.pluginAccessor = pluginAccessor;
        this.pluginMetadataManager = pluginMetadataManager;
    }

    private class SystemPluginPredicate implements Predicate<Plugin>
    {
        @Override
        public boolean apply(@Nullable Plugin input)
        {
            return !pluginMetadataManager.isUserInstalled(input);
        }
    }

    @Override
    public Collection<Info> getSystemPlugins()
    {
        final Collection<Plugin> plugins = pluginAccessor.getPlugins();
        final Iterable<Plugin> systemPlugins = Iterables.filter(plugins, new SystemPluginPredicate());
        return toPluginInfo(systemPlugins);
    }

    @Override
    public Collection<Info> getUserPlugins()
    {
        final Collection<Plugin> plugins = pluginAccessor.getPlugins();
        final Iterable<Plugin> userPlugins = Iterables.filter(plugins, Predicates.not(new SystemPluginPredicate()));
        return toPluginInfo(userPlugins);
    }

    @Override
    public Collection<Info> filterOutTrulyBuiltInPlugins(Collection<Info> pluginInfos)
    {
        final String[] IGNORE_THOSE_STARTING_WITH = {
                "jira.webfragments",
                "com.atlassian.jira.plugin.wiki",
                "com.atlassian.jira.plugin.system",
                "jira.issueviews",
                "jira.footer",
                "jira.top.navigation.bar",
                "jira.webresources",
        };
        List<Info> filtered = new ArrayList<Info>(pluginInfos.size());
        for (Info pluginInfo : pluginInfos)
        {
            final String pluginKey = pluginInfo.getKey();
            boolean add = true;
            for (String partialKey : IGNORE_THOSE_STARTING_WITH)
            {
                if (StringUtils.defaultString(pluginKey).startsWith(partialKey))
                {
                    add = false;
                    break;
                }
            }
            if (add)
            {
                filtered.add(pluginInfo);
            }
        }
        return filtered;
    }


    private Collection<Info> toPluginInfo(Iterable<Plugin> plugins)
    {
        final Iterable<Info> pluginInfoIterable = Iterables.transform(plugins, new Function<Plugin, Info>()
        {
            @Override
            public Info apply(final Plugin plugin)
            {
                final PluginInformation pluginInformation = plugin.getPluginInformation();
                final PluginState state = plugin.getPluginState();
                final boolean isSystemPlugin = !pluginMetadataManager.isUserInstalled(plugin);
                final boolean unloadablePlugin = plugin instanceof UnloadablePlugin;
                final String unloadbleReason = unloadablePlugin ? ((UnloadablePlugin) plugin).getErrorText() : "";
                return new Info()
                {
                    private String nvl(Object o)
                    {
                        return o == null ? "" : String.valueOf(o);
                    }

                    @Override
                    public int getPluginsVersion()
                    {
                        return plugin.getPluginsVersion();
                    }

                    @Override
                    public String getKey()
                    {
                        return nvl(plugin.getKey());
                    }

                    @Override
                    public String getName()
                    {
                        return nvl(plugin.getName());
                    }

                    @Override
                    public PluginInformation getPluginInformation()
                    {
                        return pluginInformation;
                    }

                    @Override
                    public String getUnloadableReason()
                    {
                        return nvl(unloadbleReason);
                    }

                    @Override
                    public boolean isUnloadable()
                    {
                        return unloadablePlugin;
                    }

                    @Override
                    public boolean isEnabled()
                    {
                        return state == PluginState.ENABLED;
                    }

                    @Override
                    public boolean isSystemPlugin()
                    {
                        return isSystemPlugin;
                    }
                };
            }
        });
        List<Info> pluginInfos = new ArrayList<Info>();
        Iterables.addAll(pluginInfos, pluginInfoIterable);
        Collections.sort(pluginInfos, new Comparator<Info>()
        {
            @Override
            public int compare(Info p1, Info p2)
            {
                int rc = p1.getName().compareTo(p2.getName());
                if (rc == 0)
                {
                    rc = p1.getKey().compareTo(p2.getKey());
                }
                return rc;
            }
        });
        return pluginInfos;

    }
}
