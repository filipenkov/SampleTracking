package com.atlassian.jira.pageobjects.pages.admin;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.Options;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.SelectElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.google.inject.Inject;
import org.openqa.selenium.By;

/**
 * Author: Geoffrey Wong
 * JIRA Administration page to browse all groups in JIRA instance
 */
public class GroupBrowserPage extends AbstractJiraPage
{
    @ElementBy (name = "max")
    SelectElement maxGroupsPerPage;

    @ElementBy (name = "nameFilter")
    PageElement nameFilter;

    @ElementBy (className = "aui-button")
    PageElement filterGroupsButton;
    
    @ElementBy (linkText = "Reset Filter")
    PageElement resetFilter;

    @ElementBy (id = "group_browser_table")
    PageElement groupBrowserTable;
    
    @Inject
    private PageElementFinder pageElementFinder;

    private String URI = "/secure/admin/user/GroupBrowser.jspa";

    public String getUrl()
    {
        return URI;
    }

    @Override
    public TimedCondition isAt()
    {
        return groupBrowserTable.timed().isPresent();
    }

    public GroupBrowserPage setMaxGroupsPerPage(String option)
    {
        maxGroupsPerPage.select(Options.text(option));
        return this;
    }

    public GroupBrowserPage setGroupNameFilter(String groupName)
    {
        nameFilter.clear().type(groupName);
        return this;
    }
    
    public GroupBrowserPage filterGroups()
    {
        filterGroupsButton.click();
        return pageBinder.bind(GroupBrowserPage.class);
    }

    public GroupBrowserPage resetFilter()
    {
        resetFilter.click();
        return this;
    }

    public BulkEditGroupMembersPage editMembersOfGroup(String group)
    {
        pageElementFinder.find(By.id("edit_members_of_" + group)).click();
        return pageBinder.bind(BulkEditGroupMembersPage.class, group);
    }


}
