package com.atlassian.jira.pageobjects.pages.project;

/**
 * 'Issues' project tab,
 *
 * @since v4.4
 */
public class IssuesTab extends AbstractProjectTab
{
    public static String LINK_ID = "summary-panel-panel";

    public IssuesTab(String projectKey)
    {
        super(LINK_ID, projectKey);
    }

    // TODO contents
}
