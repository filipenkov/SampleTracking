package com.atlassian.jira.pageobjects.pages.project;

/**
* Summary project tab,
*
* @since v4.4
*/
public class SummaryTab extends AbstractProjectTab
{
    public static String LINK_ID = "summary-panel-panel";

    public SummaryTab(String projectKey)
    {
        super(LINK_ID, projectKey);
    }

    // TODO contents
}
