package com.atlassian.renderer.v2.macro.code;

import com.atlassian.plugin.PluginManager;

import java.util.*;

public class PluggableSourceCodeFormatterRepository implements SourceCodeFormatterRepository
{
    private PluginManager pluginManager;

    public PluggableSourceCodeFormatterRepository(PluginManager pluginManager)
    {
        this.pluginManager = pluginManager;
    }

    public SourceCodeFormatter getSourceCodeFormatter(String language)
    {
        List modules = pluginManager.getEnabledModuleDescriptorsByClass(SourceCodeFormatterModuleDescriptor.class);
        for (Iterator iterator = modules.iterator(); iterator.hasNext();)
        {
            SourceCodeFormatterModuleDescriptor descriptor = (SourceCodeFormatterModuleDescriptor) iterator.next();
            SourceCodeFormatter formatter = descriptor.getFormatter();
            if (supportsLanguage(formatter, language))
                return formatter;
        }

        return null;
    }

    public Collection getAvailableLanguages()
    {
        Set supportedLanguages = new TreeSet();

        List modules = pluginManager.getEnabledModuleDescriptorsByClass(SourceCodeFormatterModuleDescriptor.class);
        for (Iterator iterator = modules.iterator(); iterator.hasNext();)
        {
            SourceCodeFormatterModuleDescriptor descriptor = (SourceCodeFormatterModuleDescriptor) iterator.next();
            SourceCodeFormatter formatter = descriptor.getFormatter();
            supportedLanguages.addAll(Arrays.asList(formatter.getSupportedLanguages()));
        }

        return supportedLanguages;
    }

    private boolean supportsLanguage(SourceCodeFormatter formatter, String language)
    {
        if (formatter == null)
            return false;

        for (int i = 0; i < formatter.getSupportedLanguages().length; i++)
        {
            String supportedLanguage = formatter.getSupportedLanguages()[i];
            if (supportedLanguage.equals(language))
                return true;
        }

        return false;
    }

}
