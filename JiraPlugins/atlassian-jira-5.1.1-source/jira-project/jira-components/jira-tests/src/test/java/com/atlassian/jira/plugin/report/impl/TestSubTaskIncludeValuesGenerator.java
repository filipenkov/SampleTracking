package com.atlassian.jira.plugin.report.impl;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.user.MockUser;

import java.util.Map;

public class TestSubTaskIncludeValuesGenerator extends LegacyJiraMockTestCase
{
    public void testSubTasksEnabled()
    {
        SubTaskIncludeValuesGenerator generator = new SubTaskIncludeValuesGenerator();

        Map generatorParams = EasyMap.build("User", new MockUser("TestSubTaskIncludeValuesGenerator"));
        assertEquals(3, generator.getValues(generatorParams).size());
    }
}