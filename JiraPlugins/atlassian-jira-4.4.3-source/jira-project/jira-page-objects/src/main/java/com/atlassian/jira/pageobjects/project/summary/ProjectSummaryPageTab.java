package com.atlassian.jira.pageobjects.project.summary;

import com.atlassian.jira.pageobjects.project.AbstractProjectConfigPageTab;
import com.atlassian.jira.pageobjects.project.ProjectConfigPageTab;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.webdriver.utils.by.ByJquery;
import com.google.common.collect.Lists;
import org.openqa.selenium.By;

import java.util.Collection;
import java.util.List;

/**
 * Represents the summary page. Use {@link #openPanel(Class)} to obtain
 * objects for individual panels.
 *
 * @since v4.4
 */
public class ProjectSummaryPageTab extends AbstractProjectConfigPageTab implements ProjectConfigPageTab
{
    public static final String TAB_LINK_ID = "view_project_summary_tab";

    private static final String URI_TEMPLATE = "/plugins/servlet/project-config/%s";

    private final String uri;

    @ElementBy(id = "project-config-panel-summary")
    private PageElement projectConfigDiv;

    @ElementBy(id = "project-config-header")
    private PageElement projectConfigHeader;

    @ElementBy(id = "project-config-actions")
    private PageElement projectActions;

    @Override
    public TimedCondition isAt()
    {
        return projectConfigDiv.timed().isPresent();
    }

    public ProjectSummaryPageTab(final String projectKey)
    {
        this.uri = String.format(URI_TEMPLATE, projectKey);
    }

    public ProjectSummaryPageTab()
    {
        this.uri = null;
    }

    @Override
    public String getUrl()
    {
        if (uri == null)
        {
            throw new IllegalStateException("Use the constructor with the project key argument.");
        }
        return uri;
    }

    public <T extends SummaryPanel> T openPanel(final Class<T> summaryPanelClass)
    {
        T bind = pageBinder.bind(summaryPanelClass);
        bind.setProject(projectInfoLocator.getProjectKey(), projectInfoLocator.getProjectId());
        return bind;
    }

    public Collection<String> getPanelHeadingTexts()
    {
        final List<String> headingTexts = Lists.newArrayList();

        final List<PageElement> headings = projectConfigDiv.findAll(By.className("toggle-title"));
        for (final PageElement heading : headings)
        {
            headingTexts.add(heading.getText());
        }

        return headingTexts;
    }

    public ProjectSummaryPageTab expandPanelContent(final String panelHeading)
    {
        if(isPanelContentCollapsed(panelHeading))
        {
            getPanelHeading(panelHeading).click();
        }
        return this;
    }

    public ProjectSummaryPageTab collapsePanelContent(final String panelHeading)
    {
        if(isPanelContentExpanded(panelHeading))
        {
            getPanelHeading(panelHeading).click();
        }
        return this;
    }

    public boolean isPanelContentExpanded(final String panelHeading)
    {
        return getPanelContent(panelHeading)
                .find(By.className("mod-content")).isVisible();
    }

    public boolean isPanelContentCollapsed(final String panelHeading)
    {
        return !isPanelContentExpanded(panelHeading);
    }

    private PageElement getPanelContent(final String panelHeading)
    {
        return projectConfigDiv.find(ByJquery.$(".project-config-webpanel:has(h3:contains('" + panelHeading + "'))"));
    }

    private PageElement getPanelHeading(final String panelHeadingText)
    {
        return projectConfigDiv.find(ByJquery.$("h3.toggle-title:contains('" + panelHeadingText +"')"));
    }
    

}
