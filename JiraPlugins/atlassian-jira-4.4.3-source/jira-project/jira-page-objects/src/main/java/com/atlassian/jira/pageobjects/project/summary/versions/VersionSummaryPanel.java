package com.atlassian.jira.pageobjects.project.summary.versions;

import com.atlassian.jira.pageobjects.project.summary.AbstractSummaryPanel;
import com.atlassian.jira.pageobjects.project.versions.Version;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.google.common.collect.Lists;
import org.openqa.selenium.By;

import java.util.List;

/**
 * Represents the "Version" panel on the summary page of the project configuration.
 *
 * @since v4.4
 */
public class VersionSummaryPanel extends AbstractSummaryPanel
{
    @ElementBy(id = "project-config-webpanel-summary-versions")
    private PageElement versionBase;

    public List<Version> getVersions()
    {
        final List<Version> versions = Lists.newArrayList();
        final List<PageElement> versionsElements = versionBase.findAll(By.cssSelector(".project-config-list > li"));

        for (final PageElement versionElement : versionsElements)
        {
            final PageElement nameElement = versionElement.find(By.cssSelector(".project-config-list-label"));
            final PageElement iconElement = nameElement.find(By.cssSelector("span"));
            final PageElement releaseDateElement = versionElement.find(By.cssSelector(".project-config-list-value"));
            final String versionName = nameElement.getText();

            final SummaryPanelVersion version = new SummaryPanelVersion(versionName);
            version.setReleased(iconElement.hasClass("project-config-icon-version-released"));
            version.setArchieved(iconElement.hasClass("project-config-icon-version-archived"));
            if (releaseDateElement.isPresent())
            {
                version.setReleaseDate(releaseDateElement.getText());
                version.setOverdue(releaseDateElement.hasClass("project-config-version-overdue"));
            }
            versions.add(version);
        }
        return versions;
    }

    public boolean hasVersion(final String versionName)
    {
        final List<Version> versions = getVersions();

        for (final Version version : versions)
        {
            if(versionName.equals(version.getName()))
            {
                return true;
            }
        }
        return false;
    }

    public String getNoVersionsText()
    {
        final PageElement element = versionBase.find(By.cssSelector(".project-config-list-empty span"));
        return element.isPresent() ? element.getText() : null;
    }

    public String getNoVersionsLinkText()
    {
        final PageElement link = versionBase.find(getNoLinkLocator());
        return link.isPresent() ? link.getText() : null;
    }
    public String getNoVersionsLinkUrl()
    {
        final PageElement link = versionBase.find(getNoLinkLocator());
        return link.isPresent() ? link.getAttribute("href") : null;
    }

    public String getSomeVersionsText()
    {
        final PageElement element = versionBase.find(By.cssSelector(".project-config-list-note span"));
        return element.isPresent() ? element.getText() : null;
    }

    public String getSomeVersionsLinkText()
    {
        final PageElement element = versionBase.find(getSomeLinkLocator());
        return element.isPresent() ? element.getText() : null;
    }

    public String getSomeVersionsLinkUrl()
    {
        final PageElement element = versionBase.find(getSomeLinkLocator());
        return element.isPresent() ? element.getAttribute("href") : null;
    }

    private By getNoLinkLocator()
    {
        return By.cssSelector(".project-config-list-empty a");
    }

    private By getSomeLinkLocator()
    {
        return By.cssSelector(".project-config-list-note a");
    }
}
