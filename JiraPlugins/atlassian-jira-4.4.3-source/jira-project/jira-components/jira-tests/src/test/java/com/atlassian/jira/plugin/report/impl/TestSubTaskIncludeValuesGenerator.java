package com.atlassian.jira.plugin.report.impl;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.MockProviderAccessor;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.user.MockCrowdService;
import com.opensymphony.user.User;

import java.util.Map;

public class TestSubTaskIncludeValuesGenerator extends LegacyJiraMockTestCase
{
    public void testSubTasksEnabled()
    {
        SubTaskIncludeValuesGenerator generator = new SubTaskIncludeValuesGenerator();

        Map generatorParams = EasyMap.build("User", new User("TestSubTaskIncludeValuesGenerator", new MockProviderAccessor("TestSubTaskIncludeValuesGenerator", "test@example.com", "en_AU"), new MockCrowdService()));
        assertEquals(3, generator.getValues(generatorParams).size());
    }
}