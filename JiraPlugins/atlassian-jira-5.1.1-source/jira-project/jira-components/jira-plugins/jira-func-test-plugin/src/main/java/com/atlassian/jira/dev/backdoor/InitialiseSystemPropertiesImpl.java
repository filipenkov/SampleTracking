package com.atlassian.jira.dev.backdoor;

import com.atlassian.jira.extension.Startable;

/**
 *
 * @since v5.0
 */
public class InitialiseSystemPropertiesImpl implements InitialiseSystemProperties, Startable
{
    @Override
    public void start() throws Exception
    {
        //disable the what's new dialog to ensure that
        System.setProperty("atlassian.dev.jira.whatsnew.show", "false");
        System.setProperty("atlassian.disable.issue.collector", "true");
    }
}
