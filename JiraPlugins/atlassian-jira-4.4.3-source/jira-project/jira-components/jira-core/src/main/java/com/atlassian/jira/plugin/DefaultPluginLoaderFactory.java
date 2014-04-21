package com.atlassian.jira.plugin;

import com.atlassian.jira.config.properties.JiraSystemProperties;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.factories.PluginFactory;
import com.atlassian.plugin.loaders.ClassPathPluginLoader;
import com.atlassian.plugin.loaders.PluginLoader;
import com.atlassian.plugin.osgi.container.OsgiContainerManager;
import com.atlassian.plugin.servlet.ServletContextFactory;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * This is the default plugin loader factory of JIRA
 */
public class DefaultPluginLoaderFactory implements PluginLoaderFactory
{
    private static final Logger log = Logger.getLogger(DefaultPluginLoaderFactory.class);

    private final PluginFactoryAndLoaderRegistrar pluginFactoryAndLoaderRegistrar;

    public DefaultPluginLoaderFactory(final PluginEventManager pluginEventManager, final OsgiContainerManager osgiContainerManager, final PluginPath pathFactory, ServletContextFactory servletContextFactory)
    {
        this.pluginFactoryAndLoaderRegistrar = new PluginFactoryAndLoaderRegistrar(pluginEventManager, osgiContainerManager, pathFactory, servletContextFactory);
    }

    public List<PluginLoader> getPluginLoaders()
    {
        final List<PluginFactory> pluginFactories = pluginFactoryAndLoaderRegistrar.getDefaultPluginFactories();

        final CollectionBuilder<PluginLoader> pluginLoaderBuilder = CollectionBuilder.newBuilder();

        // plugin loaders for our system-xxx plugins
        pluginLoaderBuilder.addAll(pluginFactoryAndLoaderRegistrar.getDefaultSystemPluginLoaders());

        // for loading plugin1 plugins the old way (ie. via WEB-INF/lib)
        pluginLoaderBuilder.add(new ClassPathPluginLoader());

        if (JiraSystemProperties.isBundledPluginsDisabled())
        {
            log.warn("Bundled plugins have been disabled. Removing bundled plugin loader.");
        }
        else
        {
            pluginLoaderBuilder.add(pluginFactoryAndLoaderRegistrar.getBundledPluginsLoader(pluginFactories));
        }

        pluginLoaderBuilder.add(pluginFactoryAndLoaderRegistrar.getDirectoryPluginLoader(pluginFactories));

        return pluginLoaderBuilder.asList();
    }

}
