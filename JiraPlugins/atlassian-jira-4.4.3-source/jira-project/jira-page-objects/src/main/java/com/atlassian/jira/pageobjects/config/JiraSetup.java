package com.atlassian.jira.pageobjects.config;

import com.atlassian.pageobjects.elements.query.TimedCondition;

/**
 * Component responsible for JIRA setup operations.
 *
 * @since v4.4s
 */
public interface JiraSetup
{

    /**
     * Checks if JIRA is set up already.
     *
     * @return <code>true</code>, if JIRA is set up
     */
    TimedCondition isSetUp();

    /**
     * Perform JIRA setup.
     *
     */
    void performSetUp();

    /**
     * Perform JIRA setup with custom data.
     *
     * @param jiraName name of JIRA
     * @param username admin username
     * @param password admin password
     */
    void performSetUp(String jiraName, String username, String password);

    /**
     * Reset JIRA to non-setup instance.
     *
     */
    void resetSetup();
}
