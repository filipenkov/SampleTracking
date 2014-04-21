package it.com.atlassian.jira.plugin.ext.bamboo.pageobjects;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.PageElement;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import org.openqa.selenium.By;

import javax.annotation.Nullable;
import java.util.Collection;

public class BrowseVersionsPanel extends AbstractJiraPage
{
    private final String URI = "/browse/%s?selectedTab=com.atlassian.jira.plugin.system.project:versions-panel";

    private String projectKey;

    public BrowseVersionsPanel(final String projectKey)
    {
        this.projectKey = projectKey;
    }

    public String getUrl()
    {
        return String.format(URI, projectKey);
    }

    public Collection<BrowsableVersion> getVersions()
    {
        return Collections2.transform(elementFinder.findAll(By.cssSelector("#versions_panel .summary")), new Function<PageElement, BrowsableVersion>()
        {
            public BrowsableVersion apply(@Nullable final PageElement from)
            {
                String id = from.getAttribute("id").replace("version_", "");
                final String name = from.getText();
                return pageBinder.bind(BrowsableVersion.class, id, name, projectKey);
            }
        });
    }
}
