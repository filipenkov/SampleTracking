package com.atlassian.jira.pageobjects.project;

import com.atlassian.jira.pageobjects.project.components.ComponentsPageTab;
import com.atlassian.jira.pageobjects.project.permissions.ProjectPermissionPageTab;
import com.atlassian.jira.pageobjects.project.summary.ProjectSummaryPageTab;
import com.atlassian.jira.pageobjects.project.versions.VersionPageTab;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.openqa.selenium.By;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Represents the tabs on the project configuration page.
 *
 * @since v4.4
 */
public class ProjectConfigTabs
{
    @Inject
    private PageBinder pageBinder;

    @Inject
    private PageElementFinder elementFinder;

    private ProjectInfoLocator locator;

    @Init
    public void init()
    {
        locator = pageBinder.bind(ProjectInfoLocator.class);
    }

    public String getProjectKey()
    {
        return locator.getProjectKey();
    }

    public List<Tab> getTabs()
    {
        PageElement container = getTabContainer();
        if (!container.isPresent())
        {
            return Collections.emptyList();
        }

        List<PageElement> elements = container.findAll(By.cssSelector(".menu-item a"));
        List<Tab> tabs = new ArrayList<Tab>(elements.size());
        for (PageElement element : elements)
        {
            tabs.add(new Tab(element));
        }
        return tabs;
    }

    public Tab getSelectedTab()
    {
        if (!getTabContainer().isPresent())
        {
            return null;
        }

        PageElement activeTab = getTabContainer().find(By.cssSelector(".active-tab a"));
        if (!activeTab.isPresent())
        {
            return null;
        }
        else
        {
            return new Tab(activeTab);
        }
    }

    public boolean isSummaryTabSelected()
    {
        return isTabSelected(ProjectSummaryPageTab.TAB_LINK_ID);
    }

    public ProjectSummaryPageTab gotoSummaryTab()
    {
        return gotoTab(ProjectSummaryPageTab.TAB_LINK_ID, ProjectSummaryPageTab.class, getProjectKey());
    }

    public boolean isVersionsTabSelected()
    {
        return isTabSelected(VersionPageTab.TAB_LINK_ID);
    }

    public VersionPageTab gotoVersionsTab()
    {
        return gotoTab(VersionPageTab.TAB_LINK_ID, VersionPageTab.class, getProjectKey());
    }

    public boolean isComponentsTabSelected()
    {
        return isTabSelected(ComponentsPageTab.TAB_LINK_ID);
    }

    public ComponentsPageTab gotoComponentsTab()
    {
        return gotoTab(ComponentsPageTab.TAB_LINK_ID, ComponentsPageTab.class, getProjectKey());
    }

    public boolean isProjectPermissionsTabSelected()
    {
        return isTabSelected(ProjectPermissionPageTab.TAB_LINK_ID);
    }

    public ProjectPermissionPageTab gotoProjectPermissionsTab()
    {
        return gotoTab(ProjectPermissionPageTab.TAB_LINK_ID, ProjectPermissionPageTab.class, getProjectKey());
    }

    public <T extends ProjectConfigPageTab> T gotoTab(String linkId, Class<T> type, Object...args)
    {
        assertTabContainer();
        Tab selectedTab = getSelectedTab();
        if (!selectedTab.getId().equals(linkId))
        {
            PageElement element = getTabContainer().find(By.id(linkId));
            assertTrue("Tab with id '" + linkId + "' does not exist.", element.isPresent());
            element.click();
        }
        return pageBinder.bind(type, args);
    }

    public boolean isTabSelected(String linkId)
    {
        Tab tab = getSelectedTab();
        return tab != null && tab.getId().equals(linkId);
    }

    private void assertTabContainer()
    {
        assertTrue("No tabs present on page.", getTabContainer().isPresent());
    }

    private PageElement getTabContainer()
    {
        return elementFinder.find(By.cssSelector("#admin-config-content .tabs-menu"));
    }

    public static class Tab
    {
        private final PageElement link;

        private Tab(PageElement link)
        {
            this.link = link;
        }

        public String getUrl()
        {
            return link.getAttribute("href");
        }

        public String getId()
        {
            return link.getAttribute("id");
        }

        public String getName()
        {
            return link.getText();
        }

        @Override
        public String toString()
        {
            return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                    .append("url", getUrl())
                    .append("id", getId())
                    .append("name", getName())
                    .toString();
        }
    }
}
