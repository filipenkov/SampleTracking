package it.com.atlassian.jira.plugin.ext.bamboo.pageobjects;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;

public class BrowseProjectPage extends AbstractJiraPage
{
    private static final String URI = "/browse/%s";
    private String projectKey;

    public BrowseProjectPage(final String projectKey)
    {
        this.projectKey = projectKey;
    }

    public String getUrl()
    {
        return String.format(URI, projectKey);
    }

    public BrowseVersionsPanel getVersionsPanel()
    {
        return pageBinder.navigateToAndBind(BrowseVersionsPanel.class, projectKey);
    }
}
