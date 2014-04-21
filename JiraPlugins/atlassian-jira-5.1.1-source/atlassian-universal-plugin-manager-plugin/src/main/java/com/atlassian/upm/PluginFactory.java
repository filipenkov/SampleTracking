package com.atlassian.upm;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugins.domain.model.plugin.PluginVersion;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.upm.license.internal.PluginLicenseRepository;
import com.atlassian.upm.spi.Plugin;
import com.atlassian.upm.spi.Plugin.Module;

import com.google.common.base.Function;

import static com.atlassian.upm.api.util.Option.none;
import static com.atlassian.upm.api.util.Option.some;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.contains;
import static com.google.common.collect.Iterables.transform;

/**
 * Factory to produce {@code Plugin} and {@code Module} objects.
 */
public class PluginFactory
{
    private final I18nResolver i18nResolver;
    private final PluginLicenseRepository licenseRepository;

    public PluginFactory(I18nResolver i18nResolver, PluginLicenseRepository licenseRepository)
    {
        this.i18nResolver = checkNotNull(i18nResolver, "i18nResolver");
        this.licenseRepository = checkNotNull(licenseRepository, "licenseRepository");
    }

    /**
     * Create a {@code Plugin} (without information about available updates)
     *
     * @param plugin plugins API version of a Plugin
     * @return UPM version of a Plugin
     */
    public Plugin createPlugin(com.atlassian.plugin.Plugin plugin)
    {
        checkNotNull(plugin, "plugin");
        return new PluginImpl(plugin, i18nResolver, this, licenseRepository, none(Boolean.class));
    }

    /**
     * Create a {@code Plugin} (with information about available updates)
     *
     * @param plugin plugins API version of a Plugin
     * @param updateAvailable true if this plugin has an available update, false if not
     * @return UPM version of a Plugin
     */
    public Plugin createPlugin(com.atlassian.plugin.Plugin plugin, boolean updateAvailable)
    {
        checkNotNull(plugin, "plugin");
        return new PluginImpl(plugin, i18nResolver, this, licenseRepository, some(updateAvailable));
    }

    /**
     * Create multiple {@code Plugin}s (without information about available updates)
     *
     * @param plugins plugins API version of a Plugin
     * @return UPM version of a Plugin
     */
    public Iterable<Plugin> createPlugins(Iterable<com.atlassian.plugin.Plugin> plugins)
    {
        return transform(plugins, new Function<com.atlassian.plugin.Plugin, Plugin>()
        {
            public Plugin apply(com.atlassian.plugin.Plugin plugin)
            {
                return createPlugin(plugin);
            }
        });
    }

    /**
     * Create multiple {@code Plugin}s (with information about available updates)
     *
     * @param plugins plugins API version of a Plugin
     * @param availablePluginUpdates plugins which are available to update
     * @return UPM version of a Plugin
     */
    public Iterable<Plugin> createPlugins(Iterable<com.atlassian.plugin.Plugin> plugins, Iterable<PluginVersion> availablePluginUpdates)
    {
        final Iterable<String> updatablePluginKeys = transform(availablePluginUpdates, new Function<PluginVersion, String>()
        {
            @Override
            public String apply(PluginVersion pluginVersion)
            {
                return pluginVersion.getPlugin().getPluginKey();
            }
        });
        
        return transform(plugins, new Function<com.atlassian.plugin.Plugin, Plugin>()
        {
            public Plugin apply(com.atlassian.plugin.Plugin plugin)
            {
                return createPlugin(plugin, contains(updatablePluginKeys, plugin.getKey()));
            }
        });
    }

    /**
     * Create a {@code Module}
     *
     * @param module plugins API version of a ModuleDescriptor
     * @return UPM version of a Module
     */
    public Module createModule(ModuleDescriptor<?> module)
    {
        return createModule(module, createPlugin(module.getPlugin()));
    }

    /**
     * Create a {@code Module} belonging to the specified plugin
     *
     * @param module plugins API version of a ModuleDescriptor
     * @param plugin the plugin parent of the module
     * @return UPM version of a Module
     */
    public Module createModule(ModuleDescriptor<?> module, Plugin plugin)
    {
        checkNotNull(module, "module");
        checkNotNull(plugin, "plugin");
        return new PluginModuleImpl(module, i18nResolver, plugin);
    }

    /**
     * Create multiple {@code Module}s
     *
     * @param modules plugins API version of a ModuleDescriptor
     * @return UPM version of a Module
     */
    public Iterable<Module> createModules(Iterable<ModuleDescriptor<?>> modules)
    {
        return transform(modules, new Function<ModuleDescriptor<?>, Module>()
        {
            public Module apply(ModuleDescriptor<?> module)
            {
                return createModule(module);
            }
        });
    }
}
