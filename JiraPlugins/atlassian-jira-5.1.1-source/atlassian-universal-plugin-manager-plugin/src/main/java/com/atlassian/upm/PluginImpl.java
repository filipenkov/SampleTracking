package com.atlassian.upm;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginInformation;
import com.atlassian.plugin.PluginState;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.upm.api.license.entity.PluginLicense;
import com.atlassian.upm.api.util.Option;
import com.atlassian.upm.license.internal.PluginLicenseRepository;
import com.atlassian.upm.spi.Plugin;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.any;

/**
 * A wrapper class around a {@code com.atlassian.plugin.Plugin} for easier use.
 */
public class PluginImpl implements Plugin
{
    private final com.atlassian.plugin.Plugin plugin;
    private final I18nResolver i18nResolver;
    private final ImmutableMap<String, Module> modules;
    private final Option<PluginLicense> license;
    private final Option<Boolean> updateAvailable;

    private static final Logger log = LoggerFactory.getLogger(PluginImpl.class);

    PluginImpl(com.atlassian.plugin.Plugin plugin,
               I18nResolver i18nResolver,
               PluginFactory pluginFactory,
               PluginLicenseRepository licenseRepository,
               Option<Boolean> updateAvailable)
    {
        this.plugin = checkNotNull(plugin, "plugin");
        this.i18nResolver = checkNotNull(i18nResolver, "i18nResolver");

        Builder<String, Module> builder = ImmutableMap.builder();
        for (ModuleDescriptor<?> moduleDescriptor : plugin.getModuleDescriptors())
        {
            if (moduleDescriptor.getKey() != null)
            {
                builder.put(moduleDescriptor.getKey(), pluginFactory.createModule(moduleDescriptor, this));
            }
            else
            {
                log.warn("Found plugin (with key '" + plugin.getKey() + "') containing module with null key. Ignoring module.");
            }
        }
        this.modules = builder.build();
        this.license = licenseRepository.getPluginLicense(getKey());
        this.updateAvailable = updateAvailable;
    }

    public com.atlassian.plugin.Plugin getPlugin()
    {
        return plugin;
    }

    public String getName()
    {
        String i18nNameKey = plugin.getI18nNameKey();
        if (i18nNameKey != null && i18nResolver.getText(i18nNameKey) != null && !i18nResolver.getText(i18nNameKey).equals(i18nNameKey))
        {
            return i18nResolver.getText(i18nNameKey);
        }
        else
        {
            return plugin.getName();
        }
    }

    public String getKey()
    {
        return plugin.getKey();
    }

    public Iterable<Module> getModules()
    {
        return modules.values();
    }

    public Module getModule(String key)
    {
        return modules.get(key);
    }

    public boolean isEnabledByDefault()
    {
        return plugin.isEnabledByDefault();
    }

    public PluginInformation getPluginInformation()
    {
        return plugin.getPluginInformation();
    }

    public PluginState getPluginState()
    {
        return plugin.getPluginState();
    }

    public Option<PluginLicense> getLicense()
    {
        return license;
    }

    public boolean isStaticPlugin()
    {
        return !plugin.isDynamicallyLoaded();
    }

    public boolean isUninstallable()
    {
        return plugin.isUninstallable();
    }

    public boolean isBundledPlugin()
    {
        return plugin.isBundledPlugin();
    }

    public Option<Boolean> isUpdateAvailable()
    {
        return updateAvailable;
    }

    public String toString()
    {
        return plugin.getKey();
    }

    public String getVersion()
    {
        return plugin.getPluginInformation().getVersion();
    }

    public boolean hasUnrecognisedModuleTypes()
    {
        return any(modules.values(), hasUnrecognisableType);
    }

    private static Predicate<Module> hasUnrecognisableType = new Predicate<Module>()
    {
        public boolean apply(Module module)
        {
            return !module.hasRecognisableType();
        }
    };
}
