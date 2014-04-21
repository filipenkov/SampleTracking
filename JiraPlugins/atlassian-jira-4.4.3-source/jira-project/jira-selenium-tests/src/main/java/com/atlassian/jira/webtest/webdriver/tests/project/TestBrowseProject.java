package com.atlassian.jira.webtest.webdriver.tests.project;

import com.atlassian.integrationtesting.runner.restore.RestoreOnce;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.jira.pageobjects.pages.project.BrowseProjectPage;
import com.atlassian.jira.pageobjects.pages.project.VersionsTab;
import com.atlassian.jira.pageobjects.pages.project.browseversion.BrowseVersionPage;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import org.junit.Test;

/**
 * Test for the basic tab functionality in Browse Project.
 *
 * @since v5.0
 */
@WebTest ({ Category.WEBDRIVER_TEST, Category.BROWSE_PROJECT, Category.BROWSING })
@RestoreOnce ("xml/TestBrowseProjectVersionTab.xml")
public class TestBrowseProject extends BaseJiraWebTest
{

    @Test
    public void shouldGoToVersionAndBack()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);
        BrowseProjectPage browseProject = jira.visit(BrowseProjectPage.class, "HSP");
        BrowseVersionPage browseVersionPage  = browseProject.openTab(VersionsTab.class).getVersion("New Version 5").goToBrowseVersion();
    }
}
