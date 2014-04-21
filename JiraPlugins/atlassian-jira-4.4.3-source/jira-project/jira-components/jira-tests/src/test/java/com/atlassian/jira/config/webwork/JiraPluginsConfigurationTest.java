package com.atlassian.jira.config.webwork;

import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.plugin.MockPlugin;
import com.atlassian.jira.plugin.webwork.WebworkModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.event.PluginEventManager;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

public class JiraPluginsConfigurationTest extends MockControllerTestCase
{

    private PluginAccessor pluginAccessor;
    private PluginEventManager pluginEventManager;
    private JiraPluginsConfiguration jiraPluginsConfiguration;

    @Before
    public void setUp() throws Exception
    {
        //
        // I know I know its an awful lot of mocking for a simple test but this is what I am left with
        //
        pluginEventManager = createMock(PluginEventManager.class);
        pluginEventManager.register(this.<Object>anyObject());
        expectLastCall();

        pluginAccessor = createMock(PluginAccessor.class);
        WebworkModuleDescriptor webworkModuleDescriptor = new WebworkModuleDescriptor(null, null, null)
        {
            @Override
            public Object getImpl(String aName) throws IllegalArgumentException
            {
                return aName;
            }
        };
        expect(pluginAccessor.getEnabledModuleDescriptorsByClass(WebworkModuleDescriptor.class)).andStubReturn(ImmutableList.of(webworkModuleDescriptor));

        Collection<? extends Plugin> mockPlugins = Lists.newArrayList(new MockPlugin("name", "key", null));
        //noinspection unchecked
        expect(pluginAccessor.getPlugins()).andStubReturn((Collection<Plugin>) mockPlugins);
        replay();

        jiraPluginsConfiguration = new JiraPluginsConfiguration()
        {
            @Override
            PluginAccessor getPluginAccessor()
            {
                return pluginAccessor;
            }

            @Override
            PluginEventManager getPluginEventManager()
            {
                return pluginEventManager;
            }
        };
    }

    @Test
    public void testGetImpl() throws Exception
    {
        try
        {
            jiraPluginsConfiguration.getImpl("webwork.anything");
            Assert.fail("Should not except webwork prefixed parameters");
        }
        catch (IllegalArgumentException expected)
        {
        }

        Object value = jiraPluginsConfiguration.getImpl("something.else");
        Assert.assertEquals(value, "something.else");

    }

}
