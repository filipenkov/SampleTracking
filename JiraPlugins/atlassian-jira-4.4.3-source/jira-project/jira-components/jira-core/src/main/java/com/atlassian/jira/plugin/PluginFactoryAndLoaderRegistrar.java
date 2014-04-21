package com.atlassian.jira.plugin;

import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.factories.PluginFactory;
import com.atlassian.plugin.factories.XmlDynamicPluginFactory;
import com.atlassian.plugin.loaders.BundledPluginLoader;
import com.atlassian.plugin.loaders.DirectoryPluginLoader;
import com.atlassian.plugin.loaders.PluginLoader;
import com.atlassian.plugin.loaders.SinglePluginLoader;
import com.atlassian.plugin.osgi.container.OsgiContainerManager;
import com.atlassian.plugin.osgi.factory.OsgiBundleFactory;
import com.atlassian.plugin.osgi.factory.OsgiPluginFactory;
import com.atlassian.plugin.osgi.factory.UnloadableStaticPluginFactory;
import com.atlassian.plugin.servlet.ServletContextFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * A simple registrar of plugin factories and plugin loaders
 *
 * @since v4.4
 */
class PluginFactoryAndLoaderRegistrar
{
    private static final Logger log = Logger.getLogger(PluginFactoryAndLoaderRegistrar.class);

    public static final String APPLICATION_KEY = "jira";
    private static final String BUNDLED_PLUGIN_ZIP_LOCATION = "/WEB-INF/classes/atlassian-bundled-plugins.zip";

    private final PluginEventManager pluginEventManager;
    private final OsgiContainerManager osgiContainerManager;
    private final PluginPath pathFactory;
    private final ServletContextFactory servletContextFactory;

    PluginFactoryAndLoaderRegistrar(PluginEventManager pluginEventManager, OsgiContainerManager osgiContainerManager, PluginPath pathFactory, ServletContextFactory servletContextFactory)
    {
        this.pluginEventManager = pluginEventManager;
        this.osgiContainerManager = osgiContainerManager;
        this.pathFactory = pathFactory;
        this.servletContextFactory = servletContextFactory;
    }

    /**
     * This allows every plugin found to be loaded
     *
     * @return a list of plugin factories which is in fact a singleton of the {@link MasterPluginFactory}
     */
    public List<PluginFactory> getDefaultPluginFactories()
    {
        final ArrayList<Pattern> everyPluginWhiteList = Lists.newArrayList(Pattern.compile(".*"));
        return getDefaultPluginFactories(everyPluginWhiteList);
    }

    /**
     * This allows only a select list of plugins found to be loaded
     *
     * @param pluginWhitelist the whitelist of plugins deployment units that are allowed to be loaded
     * @return a list of plugin factories which is in fact a singleton of the {@link MasterPluginFactory}
     */
    public List<PluginFactory> getDefaultPluginFactories(final List<Pattern> pluginWhitelist)
    {
        // this loads Atlassian Plugins (as OSGi bundles) transforming them into full OSGi bundles if necessary
        final PluginFactory osgiPluginFactory = new OsgiPluginFactory(
                PluginAccessor.Descriptor.FILENAME,
                Sets.newHashSet(APPLICATION_KEY),
                pathFactory.getOsgiPersistentCache(),
                osgiContainerManager,
                pluginEventManager);

        // this loads OSGi bundles
        final PluginFactory osgiBundleFactory = new OsgiBundleFactory(osgiContainerManager, pluginEventManager);

        // this loads just-XML-files that describe a plugin
        final PluginFactory xmlDynamicFactory = new XmlDynamicPluginFactory("com.atlassian.jira");

        // this loads "UnloadablePlugins" in the case that the user drops a Plugins 1 plugin into the plugins 2 installation directory.
        final UnloadableStaticPluginFactory unloadableStaticPluginFactory = new UnloadableStaticPluginFactory(PluginAccessor.Descriptor.FILENAME);

        final ArrayList<PluginFactory> pluginFactories = Lists.newArrayList(xmlDynamicFactory, osgiPluginFactory, osgiBundleFactory, unloadableStaticPluginFactory);
        final MasterPluginFactory masterPluginFactory = new MasterPluginFactory(pluginFactories, pluginWhitelist);
        return Lists.<PluginFactory>newArrayList(masterPluginFactory);
    }

    public PluginLoader getBundledPluginsLoader(List<PluginFactory> pluginFactories)
    {
        try
        {
            final URL bundledPluginZip = servletContextFactory.getServletContext().getResource(BUNDLED_PLUGIN_ZIP_LOCATION);
            if (bundledPluginZip == null)
            {
                throw new IllegalStateException("Can't find bundled plugins ZIP file in servlet context: " + BUNDLED_PLUGIN_ZIP_LOCATION);
            }

            return new BundledPluginLoader(bundledPluginZip, pathFactory.getBundledPluginsDirectory(), pluginFactories, pluginEventManager);
        }
        catch (MalformedURLException e)
        {
            throw new IllegalStateException("Can't form url to bundled plugins ZIP file at: " + BUNDLED_PLUGIN_ZIP_LOCATION, e);
        }
    }

    public List<PluginLoader> getDefaultSystemPluginLoaders()
    {
        return Lists.<PluginLoader>newArrayList(
                new SinglePluginLoader("system-workflow-plugin.xml"),
                new SinglePluginLoader("system-customfieldtypes-plugin.xml"),
                new SinglePluginLoader("system-project-plugin.xml"),
                new SinglePluginLoader("system-reports-plugin.xml"),
                new SinglePluginLoader("system-portlets-plugin.xml"),
                new SinglePluginLoader("system-webwork1-plugin.xml"),

                //load the link resolvers and renderer components before the renderers get loaded.
                new SinglePluginLoader("system-contentlinkresolvers-plugin.xml"),
                new SinglePluginLoader("system-renderercomponentfactories-plugin.xml"),
                new SinglePluginLoader("system-renderers-plugin.xml"),
                new SinglePluginLoader("system-macros-plugin.xml"),
                new SinglePluginLoader("system-issueoperations-plugin.xml"),
                new SinglePluginLoader("system-issuetabpanels-plugin.xml"),
                new SinglePluginLoader("webfragment/system-user-nav-bar-sections.xml"),
                new SinglePluginLoader("webfragment/system-admin-sections.xml"),
                new SinglePluginLoader("webfragment/system-preset-filters-sections.xml"),
                new SinglePluginLoader("webfragment/system-view-project-operations-sections.xml"),
                new SinglePluginLoader("webfragment/system-user-profile-links.xml"),
                new SinglePluginLoader("webfragment/system-hints.xml"),
                new SinglePluginLoader("system-issueviews-plugin.xml"),
                new SinglePluginLoader("system-projectroleactors-plugin.xml"),
                new SinglePluginLoader("system-webresources-plugin.xml"),
                new SinglePluginLoader("system-top-navigation-plugin.xml"),
                new SinglePluginLoader("system-footer-plugin.xml"),
                new SinglePluginLoader("system-user-format-plugin.xml"),
                new SinglePluginLoader("system-user-profile-panels.xml"),
                new SinglePluginLoader("system-jql-function-plugin.xml"),
                new SinglePluginLoader("system-keyboard-shortcuts-plugin.xml")
        );
    }

    public List<PluginLoader> getBootstrapSystemPluginLoaders()
    {
        return Lists.<PluginLoader>newArrayList(
                new SinglePluginLoader("system-webresources-plugin.xml")
        );
    }

    public PluginLoader getDirectoryPluginLoader(List<PluginFactory> pluginFactories)
    {
        return new DirectoryPluginLoader(pathFactory.getInstalledPluginsDirectory(), pluginFactories, pluginEventManager);
    }
}
