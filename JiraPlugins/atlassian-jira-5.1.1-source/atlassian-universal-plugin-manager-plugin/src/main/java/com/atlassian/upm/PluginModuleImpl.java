package com.atlassian.upm;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.descriptors.CannotDisable;
import com.atlassian.plugin.descriptors.UnrecognisedModuleDescriptor;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.upm.spi.Plugin;
import com.atlassian.upm.spi.Plugin.Module;

import static com.google.common.base.Preconditions.checkNotNull;

public class PluginModuleImpl implements Module
{
    private final ModuleDescriptor<?> module;
    private final I18nResolver i18nResolver;
    private final Plugin plugin;

    PluginModuleImpl(ModuleDescriptor<?> module, I18nResolver i18nResolver, Plugin plugin)
    {
        this.module = checkNotNull(module, "module");
        this.i18nResolver = checkNotNull(i18nResolver, "i18nResolver");
        this.plugin = checkNotNull(plugin, "plugin");
    }

    public ModuleDescriptor<?> getModuleDescriptor()
    {
        return module;
    }

    public String getCompleteKey()
    {
        return module.getCompleteKey();
    }

    public String getDescription()
    {
        return module.getDescription();
    }

    public String getKey()
    {
        return module.getKey();
    }

    public String getName()
    {
        String i18nNameKey = module.getI18nNameKey();

        if (i18nNameKey != null && i18nResolver.getText(i18nNameKey) != null && !i18nResolver.getText(i18nNameKey).equals(i18nNameKey))
        {
            return i18nResolver.getText(i18nNameKey);
        }
        else
        {
            return module.getName();
        }
    }

    public Plugin getPlugin()
    {
        return plugin;
    }

    public String getPluginKey()
    {
        return module.getPluginKey();
    }

    public String toString()
    {
        return getCompleteKey();
    }

    public boolean canNotBeDisabled()
    {
        return getModuleDescriptor().getClass().isAnnotationPresent(CannotDisable.class);
    }

    public boolean hasRecognisableType()
    {
        return !(module instanceof UnrecognisedModuleDescriptor);
    }
}
