package com.atlassian.jira.webtest.webdriver.tests.common;

import com.atlassian.integrationtesting.runner.restore.RestoreOnce;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.pages.ConfigurePortalPages;
import com.atlassian.jira.pageobjects.pages.ManageFiltersPage;
import com.atlassian.jira.pageobjects.pages.UserAvatarDialog;
import com.atlassian.jira.pageobjects.pages.ViewProfilePage;
import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @since v5.0.3
 */

@WebTest ({ Category.WEBDRIVER_TEST, Category.DASHBOARDS, Category.UPGRADE_TASKS, Category.ISSUE_NAVIGATOR })
@RestoreOnce ("xml/TestCaseInsensitiveDashboards.zip")
public class TestUpgradeCaseForDashboardsAndAvatars extends BaseJiraWebTest
{
    @Test
    public void seeIfThereAre3Dashboards()
    {
        ConfigurePortalPages portalPages = jira.gotoLoginPage().login("fred", "fred", ConfigurePortalPages.class);
        List<ConfigurePortalPages.Dashboard> dashboards = portalPages.getDashboards(ConfigurePortalPages.Tab.MY);
        assertEquals(ImmutableList.of(new ConfigurePortalPages.Dashboard("My", "10010", true),
                new ConfigurePortalPages.Dashboard("Not favourite", "10012"),
                new ConfigurePortalPages.Dashboard("The other one", "10011", true)), dashboards);
    }

    @Test
    public void seeIfThereAre3SearchRequests()
    {
        ManageFiltersPage portalPages = jira.gotoLoginPage().login("fred", "fred", ManageFiltersPage.class);
        List<ManageFiltersPage.SearchRequest> dashboards = portalPages.getSearchRequests(ManageFiltersPage.Tab.MY);
        assertEquals(ImmutableList.of(new ManageFiltersPage.SearchRequest("Admins", "10002"),
                new ManageFiltersPage.SearchRequest("Hsp", "10000", true),
                new ManageFiltersPage.SearchRequest("Mky", "10001")), dashboards);
    }

    @Test
    public void seeIfThereAre2Avatars()
    {
        ViewProfilePage profilePage = jira.gotoLoginPage().login("fred", "fred", ViewProfilePage.class);
        UserAvatarDialog avatarDialog = profilePage.userAvatar();
        try
        {
            assertEquals(ImmutableList.of("10140", "10141"), avatarDialog.getCustomAvatars());
        }
        finally
        {
            avatarDialog.close();
        }
    }
}
