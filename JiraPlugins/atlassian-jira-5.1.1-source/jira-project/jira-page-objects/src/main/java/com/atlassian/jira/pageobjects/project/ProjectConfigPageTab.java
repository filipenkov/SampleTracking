package com.atlassian.jira.pageobjects.project;

import com.atlassian.pageobjects.Page;

/**
 * Represents a project config page with tabs.
 *
 * @since v4.4
 */
public interface ProjectConfigPageTab extends Page
{
    String getProjectKey();
    long getProjectId();
    ProjectConfigTabs getTabs();
    ProjectConfigHeader getProjectHeader();
    ProjectConfigActions openOperations();
}
