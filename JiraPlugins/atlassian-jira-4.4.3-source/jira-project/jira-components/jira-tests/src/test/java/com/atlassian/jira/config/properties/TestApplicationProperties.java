/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.config.properties;

import com.atlassian.jira.local.LegacyJiraMockTestCase;

import com.atlassian.jira.ManagerFactory;

public class TestApplicationProperties extends LegacyJiraMockTestCase
{
    public TestApplicationProperties(String s)
    {
        super(s);
    }

    public void testGettingProperties()
    {
        final ApplicationProperties applicationProperties = ManagerFactory.getApplicationProperties();
        assertNull(applicationProperties.getString(APKeys.JIRA_TIMETRACKING_DAYS_PER_WEEK));
        assertNull(applicationProperties.getString(APKeys.JIRA_TIMETRACKING_HOURS_PER_DAY));

        assertEquals("7", applicationProperties.getDefaultBackedString(APKeys.JIRA_TIMETRACKING_DAYS_PER_WEEK));
        assertEquals("24", applicationProperties.getDefaultBackedString(APKeys.JIRA_TIMETRACKING_HOURS_PER_DAY));
        assertEquals(true, applicationProperties.getOption(APKeys.JIRA_OPTION_TRACKBACK_RECEIVE));
        assertEquals(false, applicationProperties.getOption(APKeys.JIRA_OPTION_TRACKBACK_SEND));
    }

    public void testApplicationPropertiesSetString()
    {
        final ApplicationProperties applicationProperties = ManagerFactory.getApplicationProperties();
        final String propertyKey = APKeys.JIRA_TRACKBACK_EXCLUDE_PATTERN;

        //initially null
        assertNull(applicationProperties.getString(propertyKey));

        //set some value
        applicationProperties.setString(propertyKey, "something");
        assertEquals("something", applicationProperties.getString(propertyKey));

        //set it to null (ie. remove the property)
        applicationProperties.setString(propertyKey, null);
        assertFalse(applicationProperties.exists(propertyKey));
        assertNull(applicationProperties.getString(propertyKey));

        //set it to empty string ""
        applicationProperties.setString(propertyKey, "");
        assertEquals("", applicationProperties.getString(propertyKey));
    }
}
