package com.atlassian.jira.pageobjects.project.summary.screens;

import com.atlassian.jira.pageobjects.project.summary.AbstractSummaryPanel;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.google.common.collect.Lists;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.openqa.selenium.By;

import java.util.List;

/**
 * Represents the screens panel in the project configuration page.
 *
 * @since v4.4
 */
public class ScreensPanel extends AbstractSummaryPanel
{

    @ElementBy(id = "project-config-webpanel-summary-screens")
    private PageElement screensSummaryPanel;

    public List<ScreenSchemeListItem> getScreenSchemes()
    {
        final List<ScreenSchemeListItem> screenSchemesListItems = Lists.newArrayList();
        final List<PageElement> screenSchemesElements = screensSummaryPanel.findAll(getScreenSchemesListLocator());

        for (final PageElement screenSchemesElement : screenSchemesElements)
        {
            final PageElement screenScheme = screenSchemesElement.find(getScreenSchemeNameLocator());
            final PageElement screenSchemeUrlElement = screenSchemesElement.find(getScreenSchemeLinkLocator());

            final String screenSchemeName = screenScheme.getText();
            final String screenSchemeUrl = screenSchemeUrlElement.isPresent() ?
                    screenSchemeUrlElement.getAttribute("href") : null;
            final boolean isDefaultScreenScheme = screenSchemesElement.find(getScreenSchemeIsDefaultLocator()).isPresent();

            screenSchemesListItems.add(new ScreenSchemeListItem(screenSchemeName, screenSchemeUrl, isDefaultScreenScheme));
        }

        return screenSchemesListItems;
    }

    public String getIssueTypeScreenSchemeEditLinkText()
    {
        return screensSummaryPanel.find(getSchemeLinkLocator()).getText();
    }

    public String getIssueTypeScreenSchemeEditLinkUrl()
    {
        return screensSummaryPanel.find(getSchemeLinkLocator()).getAttribute("href");
    }

    private static By getScreenSchemeIsDefaultLocator()
    {
        return By.className("project-config-list-default");
    }

    private static By getSchemeLinkLocator()
    {
        return By.cssSelector(".project-config-summary-scheme > a");
    }

    private static By getScreenSchemesListLocator()
    {
        return By.cssSelector(".project-config-list > li");
    }

    private static By getScreenSchemeNameLocator()
    {
        return By.cssSelector(".project-config-screenscheme-name");
    }

    private static By getScreenSchemeLinkLocator()
    {
        return By.cssSelector("a.project-config-screenscheme-name");
    }

    /**
     * Represents an item in an issue types scheme as shown in the issue types panel on the project configuration summary
     * page
     *
     * @since v4.4
     */
    public static class ScreenSchemeListItem
    {
        private final String name;
        private final String url;
        private final boolean defaultScreenScheme;

        public ScreenSchemeListItem(final String name, final String url, final boolean defaultScreenScheme)
        {
            this.name = name;
            this.url = url;
            this.defaultScreenScheme = defaultScreenScheme;
        }

        public String getName()
        {
            return name;
        }

        public String getUrl()
        {
            return url;
        }

        public boolean isDefaultScreenScheme()
        {
            return defaultScreenScheme;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            ScreenSchemeListItem that = (ScreenSchemeListItem) o;

            if (defaultScreenScheme != that.defaultScreenScheme) { return false; }
            if (name != null ? !name.equals(that.name) : that.name != null) { return false; }
            if (url != null ? !url.equals(that.url) : that.url != null) { return false; }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + (url != null ? url.hashCode() : 0);
            result = 31 * result + (defaultScreenScheme ? 1 : 0);
            return result;
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }

}
