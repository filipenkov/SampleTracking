package com.atlassian.jira.plugin.myjirahome;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.atlassian.plugin.web.descriptors.WebPanelModuleDescriptor;
import com.atlassian.plugin.web.model.WebLink;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(JUnit4.class)
public class MyJiraHomeLinkerImplTest
{
    public static final String MY_PLUGIN_MODULE_KEY = "my.plugin.module";

    private final User mockUser = mock(User.class);
    
    private final PluginAccessor mockPluginAccessor = mock(PluginAccessor.class);
    private final MyJiraHomePreference mockMyJiraHomePreference = mock(MyJiraHomePreference.class);

    private MyJiraHomeLinkerImpl resolver = new MyJiraHomeLinkerImpl(mockPluginAccessor, mockMyJiraHomePreference);

    @Test
    public void testGetHomeAsLinkNoUserLoggedIn()
    {
        expectPluginModuleIsEnabled();

        final String result = resolver.getHomeLink(null);
        assertThat(result, is(MyJiraHomeLinkerImpl.DEFAULT_HOME));
    }

    @Test
    public void testGetHomeAsLinkHomeIsEmpty()
    {
        when(mockMyJiraHomePreference.findHome(mockUser)).thenReturn("");
        
        final String result = resolver.getHomeLink(mockUser);
        assertThat(result, is(MyJiraHomeLinkerImpl.DEFAULT_HOME));
    }

    @Test
    public void testGetHomeAsLinkHomePluginModuleUnknown()
    {
        expectPreferenceReturnMyPluginModuleKey();
        doThrow(new IllegalArgumentException("unknown")).when(mockPluginAccessor).isPluginModuleEnabled(MY_PLUGIN_MODULE_KEY);
        
        final String result = resolver.getHomeLink(mockUser);
        assertThat(result, is(MyJiraHomeLinkerImpl.DEFAULT_HOME));
    }

    @Test
    public void testGetHomeAsLinkHomePluginModuleNotEnabled()
    {
        expectPreferenceReturnMyPluginModuleKey();
        when(mockPluginAccessor.isPluginModuleEnabled(MY_PLUGIN_MODULE_KEY)).thenReturn(Boolean.FALSE);

        final String result = resolver.getHomeLink(mockUser);
        assertThat(result, is(MyJiraHomeLinkerImpl.DEFAULT_HOME));
    }

    @Test
    public void testGetHomeAsLinkHomePluginModuleNotAWebItem()
    {
        final ModuleDescriptor mockWebPanelModuleDescription = mock(WebPanelModuleDescriptor.class);

        expectPluginModuleIsEnabled();
        expectPreferenceReturnMyPluginModuleKey();
        when(mockPluginAccessor.getPluginModule(MY_PLUGIN_MODULE_KEY)).thenReturn(mockWebPanelModuleDescription);

        final String result = resolver.getHomeLink(mockUser);
        assertThat(result, is(MyJiraHomeLinkerImpl.DEFAULT_HOME));
    }

    @Test
    public void testGetHomeAsLinkHome()
    {
        final WebItemModuleDescriptor mockWebItemModuleDescriptor = mock(WebItemModuleDescriptor.class);
        final WebLink mockWebLink = mock(WebLink.class);

        expectPluginModuleIsEnabled();
        expectPreferenceReturnMyPluginModuleKey();
        when(mockPluginAccessor.getPluginModule(MY_PLUGIN_MODULE_KEY)).thenReturn((ModuleDescriptor) mockWebItemModuleDescriptor);
        when(mockWebItemModuleDescriptor.getLink()).thenReturn(mockWebLink);
        when(mockWebLink.getRenderedUrl(anyMap())).thenReturn("/my-home");

        final String result = resolver.getHomeLink(mockUser);
        assertThat(result, is("/my-home"));
    }

    private void expectPreferenceReturnMyPluginModuleKey()
    {
        when(mockMyJiraHomePreference.findHome(mockUser)).thenReturn(MY_PLUGIN_MODULE_KEY);
    }

    private void expectPluginModuleIsEnabled()
    {
        when(mockPluginAccessor.isPluginModuleEnabled(MY_PLUGIN_MODULE_KEY)).thenReturn(Boolean.TRUE);
    }
}
