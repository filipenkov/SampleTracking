package com.atlassian.upm;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.upm.spi.Plugin;
import com.atlassian.upm.spi.Plugin.Module;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PluginModuleTest
{
    private static final String PLUGIN_MODULE_NAME_KEY = "plugin.module.name.key";
    private static final String PLUGIN_MODULE_NAME = "Plugin Module Name";
    private static final String I18N_PLUGIN_MODULE_NAME = "Internationalized Plugin Module Name";

    @Mock I18nResolver i18nResolver;
    @Mock ModuleDescriptor<?> moduleDescriptor;
    @Mock Plugin plugin;

    private Module module;

    @Before
    public void setUp()
    {
        when(moduleDescriptor.getName()).thenReturn(PLUGIN_MODULE_NAME);
        when(moduleDescriptor.getI18nNameKey()).thenReturn(PLUGIN_MODULE_NAME_KEY);

        module = new PluginModuleImpl(moduleDescriptor, i18nResolver, plugin);
    }

    @Test
    public void assertThatPluginModuleWithValidI18nKeyReturnsResolvedI18nName()
    {
        when(i18nResolver.getText(PLUGIN_MODULE_NAME_KEY)).thenReturn(I18N_PLUGIN_MODULE_NAME);
        assertThat(module.getName(), is(equalTo(I18N_PLUGIN_MODULE_NAME)));
    }

    @Test
    public void assertThatPluginModuleWithUnresolvedI18nKeyReturnsName()
    {
        when(i18nResolver.getText(PLUGIN_MODULE_NAME_KEY)).thenReturn(PLUGIN_MODULE_NAME_KEY);
        assertThat(module.getName(), is(equalTo(PLUGIN_MODULE_NAME)));
    }

    @Test
    public void assertThatPluginModuleWithNullI18nKeyReturnsName()
    {
        when(i18nResolver.getText(PLUGIN_MODULE_NAME_KEY)).thenReturn(null);
        assertThat(module.getName(), is(equalTo(PLUGIN_MODULE_NAME)));
    }

    @Test
    public void assertThatPluginModuleWithNoI18nKeyReturnsName()
    {
        when(moduleDescriptor.getI18nNameKey()).thenReturn(null);
        assertThat(module.getName(), is(equalTo(PLUGIN_MODULE_NAME)));
    }

}
