package com.atlassian.jira.plugin.report.impl;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.user.MockUser;

import java.util.Map;

public class TestUserSubTaskIncludeValuesGenerator extends LegacyJiraMockTestCase
{
    public void testSubTasksEnabled()
    {
        UserSubTaskIncludeValuesGenerator generator = new UserSubTaskIncludeValuesGenerator();

        Map generatorParams = EasyMap.build("User", new MockUser("TestUserSubTaskIncludeValuesGenerator"));
        assertEquals(2, generator.getValues(generatorParams).size());
    }
}