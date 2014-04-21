package com.atlassian.upm;

import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.upm.license.internal.PluginLicenseRepository;
import com.atlassian.upm.spi.Plugin;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.upm.api.util.Option.none;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PluginTest
{
    private static final String PLUGIN_NAME_KEY = "plugin.name.key";
    private static final String PLUGIN_NAME = "Plugin Name";
    private static final String I18N_PLUGIN_NAME = "Internationalized Plugin Name";

    @Mock I18nResolver i18nResolver;
    @Mock com.atlassian.plugin.Plugin oldPlugin;
    @Mock PluginFactory pluginFactory;
    @Mock PluginLicenseRepository licenseRepository;

    private Plugin newPlugin;

    @Before
    public void setUp()
    {
        when(oldPlugin.getName()).thenReturn(PLUGIN_NAME);
        when(oldPlugin.getI18nNameKey()).thenReturn(PLUGIN_NAME_KEY);

        newPlugin = new PluginImpl(oldPlugin, i18nResolver, pluginFactory, licenseRepository, none(Boolean.class));
    }

    @Test
    public void assertThatPluginWithValidI18nKeyReturnsResolvedI18nName()
    {
        when(i18nResolver.getText(PLUGIN_NAME_KEY)).thenReturn(I18N_PLUGIN_NAME);
        assertThat(newPlugin.getName(), is(equalTo(I18N_PLUGIN_NAME)));
    }

    @Test
    public void assertThatPluginWithUnresolvedI18nKeyReturnsName()
    {
        when(i18nResolver.getText(PLUGIN_NAME_KEY)).thenReturn(PLUGIN_NAME_KEY);
        assertThat(newPlugin.getName(), is(equalTo(PLUGIN_NAME)));
    }

    @Test
    public void assertThatPluginWithNullI18nKeyReturnsName()
    {
        when(i18nResolver.getText(PLUGIN_NAME_KEY)).thenReturn(null);
        assertThat(newPlugin.getName(), is(equalTo(PLUGIN_NAME)));
    }

    @Test
    public void assertThatPluginWithNoI18nKeyReturnsName()
    {
        when(oldPlugin.getI18nNameKey()).thenReturn(null);
        assertThat(newPlugin.getName(), is(equalTo(PLUGIN_NAME)));
    }
}
