package com.atlassian.upm;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.upm.api.util.Option;
import com.atlassian.upm.license.internal.PluginLicenseRepository;
import com.atlassian.upm.spi.Plugin;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PluginImplTest
{
    @Mock com.atlassian.plugin.Plugin plugin;
    @Mock PluginFactory pluginFactory;
    @Mock PluginLicenseRepository licenseRepository;
    @Mock I18nResolver i18nResolver;
    @Mock Option<Boolean> updateAvailable;
    @Mock Plugin.Module module;

    PluginImpl pluginWrapper;

    @Test
    public void assertThatNullKeyedModulesDoNotThrowAnException()
    {
        ModuleDescriptor<?> moduleDescriptor = mock(ModuleDescriptor.class);
        when(moduleDescriptor.getKey()).thenReturn("some.key");

        ModuleDescriptor<?> moduleDescriptorWithNullKey = mock(ModuleDescriptor.class);

        List<ModuleDescriptor<?>> moduleDescriptors = new ArrayList<ModuleDescriptor<?>>();
        moduleDescriptors.add(moduleDescriptor);
        moduleDescriptors.add(moduleDescriptorWithNullKey);

        when(plugin.getModuleDescriptors()).thenReturn(moduleDescriptors);
        when(pluginFactory.createModule(any(ModuleDescriptor.class), any(Plugin.class))).thenReturn(module);
        pluginWrapper = new PluginImpl(plugin, i18nResolver, pluginFactory, licenseRepository, updateAvailable);
    }
}
