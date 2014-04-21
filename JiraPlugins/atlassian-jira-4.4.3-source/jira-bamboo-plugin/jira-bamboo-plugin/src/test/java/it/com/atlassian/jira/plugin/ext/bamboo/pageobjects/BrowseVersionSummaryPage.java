package it.com.atlassian.jira.plugin.ext.bamboo.pageobjects;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import org.openqa.selenium.By;

public class BrowseVersionSummaryPage extends AbstractJiraPage
{
    private static final String URI = "/browse/%s/fixforversion/%s";

    public enum Actions
    {
        ReleaseTab
    }


    private BrowsableVersion browsableVersion;

    public BrowseVersionSummaryPage()
    {
    }

    public BrowseVersionSummaryPage(final BrowsableVersion browsableVersion)
    {
        this.browsableVersion = browsableVersion;
    }

    public BrowsableVersion getBrowsableVersion()
    {
        return browsableVersion;
    }

    public String getUrl()
    {
        return String.format(URI, browsableVersion.getProjectKey(), browsableVersion.getId());
    }

    public boolean hasTab(String tabId)
    {
        return elementFinder.find(By.cssSelector(".tabs #" + tabId)) != null;
    }

    public ReleaseManagementTabPanel getReleaseManagementTabPanel()
    {
        elementFinder.find(By.id("bamboo-release-tabpanel-panel")).click();
        return pageBinder.navigateToAndBind(ReleaseManagementTabPanel.class, this);
    }
}
