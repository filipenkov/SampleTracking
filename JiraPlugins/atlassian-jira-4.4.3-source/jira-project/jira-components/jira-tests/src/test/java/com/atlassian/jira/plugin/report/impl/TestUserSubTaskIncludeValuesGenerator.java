package com.atlassian.jira.plugin.report.impl;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.MockProviderAccessor;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.user.MockCrowdService;
import com.opensymphony.user.User;

import java.util.Map;

public class TestUserSubTaskIncludeValuesGenerator extends LegacyJiraMockTestCase
{
    public void testSubTasksEnabled()
    {
        UserSubTaskIncludeValuesGenerator generator = new UserSubTaskIncludeValuesGenerator();

        Map generatorParams = EasyMap.build("User", new User("TestUserSubTaskIncludeValuesGenerator", new MockProviderAccessor("TestUserSubTaskIncludeValuesGenerator", "test@example.com", "en_AU"), new MockCrowdService()));
        assertEquals(2, generator.getValues(generatorParams).size());
    }
}