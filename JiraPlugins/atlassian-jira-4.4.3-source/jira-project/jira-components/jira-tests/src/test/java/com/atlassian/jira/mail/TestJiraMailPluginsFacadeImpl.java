package com.atlassian.jira.mail;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.plugin.PluginAccessor;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;

/**
 *
 */
public class TestJiraMailPluginsFacadeImpl extends ListeningTestCase
{
    private final String expectedKey = "someKey";
    private Mock mockPluginAccessor;

    @Before
    public void setUp() throws Exception
    {
        mockPluginAccessor = new Mock(PluginAccessor.class);
        mockPluginAccessor.setStrict(true);
    }

    @Test
    public void testIsPluginModuleEnabledFalse()
    {
        _testIsPluginModuleEnabled(false);
    }

    @Test
    public void testIsPluginModuleEnabledTrue()
    {
        _testIsPluginModuleEnabled(true);
    }

    private void _testIsPluginModuleEnabled(boolean param)
    {
        mockPluginAccessor.expectAndReturn("isPluginModuleEnabled", P.args(new IsEqual(expectedKey)), Boolean.valueOf(param));

        JiraMailPluginsHelperImpl facade = new JiraMailPluginsHelperImpl((PluginAccessor) mockPluginAccessor.proxy());

        boolean result = facade.isPluginModuleEnabled(expectedKey);
        assertEquals(param, result);

        mockPluginAccessor.verify();
    }
}
