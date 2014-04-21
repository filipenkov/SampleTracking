/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.config.properties;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.local.LegacyJiraMockTestCase;

public class TestApplicationProperties extends LegacyJiraMockTestCase
{
    public TestApplicationProperties(String s)
    {
        super(s);
    }

    public void testGettingProperties()
    {
        final ApplicationProperties applicationProperties = ComponentAccessor.getApplicationProperties();
        assertNull(applicationProperties.getString(APKeys.JIRA_TIMETRACKING_DAYS_PER_WEEK));
        assertNull(applicationProperties.getString(APKeys.JIRA_TIMETRACKING_HOURS_PER_DAY));

        assertEquals("7", applicationProperties.getDefaultBackedString(APKeys.JIRA_TIMETRACKING_DAYS_PER_WEEK));
        assertEquals("24", applicationProperties.getDefaultBackedString(APKeys.JIRA_TIMETRACKING_HOURS_PER_DAY));
    }
}
