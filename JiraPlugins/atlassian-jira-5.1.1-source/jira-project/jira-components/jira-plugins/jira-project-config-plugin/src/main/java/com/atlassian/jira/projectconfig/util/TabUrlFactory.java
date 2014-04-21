package com.atlassian.jira.projectconfig.util;

/**
 * Generates URLs to the project configuration tabs.
 *
 * @since v4.4
 */
public interface TabUrlFactory
{
    String forSummary();
    String forComponents();
    String forVersions();
    String forNotifications();
    String forIssueTypes();
    String forIssueSecurity();
    String forPermissions();
    String forWorkflows();
    String forFields();
    String forScreens();
}
