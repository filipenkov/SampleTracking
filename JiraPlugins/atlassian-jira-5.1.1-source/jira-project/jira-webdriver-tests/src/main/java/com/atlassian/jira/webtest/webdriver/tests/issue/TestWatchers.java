package com.atlassian.jira.webtest.webdriver.tests.issue;

import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.jira.pageobjects.pages.viewissue.ViewIssuePage;
import com.atlassian.jira.pageobjects.pages.viewissue.watchers.WatchersComponent;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@WebTest ({ Category.WEBDRIVER_TEST, Category.ISSUES })
@Restore ("xml/TestWatchers.xml")
public class TestWatchers extends BaseJiraWebTest
{

    @Test
    public void testCurrentWatchersShow() {
        String key = backdoor.issues().createIssue("HSP", "xxx").key;
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);
        final ViewIssuePage issuePage = jira.goTo(ViewIssuePage.class, key);
        final WatchersComponent watchersComponent = issuePage.openWatchersDialog();
        assertEquals(watchersComponent.getWatchers(), asList("admin"));
    }

    @Test
    public void testAddingWatchers() {
        String key = backdoor.issues().createIssue("HSP", "xxx").key;
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);
        ViewIssuePage issuePage = jira.goTo(ViewIssuePage.class, key);
        WatchersComponent watchersComponent = issuePage.openWatchersDialog();
        watchersComponent.addWatcher("fred");
        assertTrue(watchersComponent.getWatchers().contains("fred"));
        assertTrue(watchersComponent.getWatchers().contains("admin"));
        issuePage = jira.goTo(ViewIssuePage.class, key);
        watchersComponent = issuePage.openWatchersDialog();
        assertTrue(watchersComponent.getWatchers().contains("fred"));
        assertTrue(watchersComponent.getWatchers().contains("admin"));
    }

    @Test
    public void testDeleteWatchers() {
        String key = backdoor.issues().createIssue("HSP", "xxx").key;
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);
        ViewIssuePage issuePage = jira.goTo(ViewIssuePage.class, key);
        WatchersComponent watchersComponent = issuePage.openWatchersDialog();
        watchersComponent.removeWatcher("admin");
        assertEquals(watchersComponent.getWatchers(), new ArrayList<String>());
        issuePage = jira.goTo(ViewIssuePage.class, key);
        watchersComponent = issuePage.openWatchersDialog();
        assertEquals(watchersComponent.getWatchers(), new ArrayList<String>());
    }


    @Test
    public void testCurrentWatchersShowWithNoBrowsePermission() {
        String key = backdoor.issues().createIssue("HSP", "xxx").key;
        jira.gotoLoginPage().login("jiradev", "jiradev", DashboardPage.class);
        final ViewIssuePage issuePage = jira.goTo(ViewIssuePage.class, key);
        final WatchersComponent watchersComponent = issuePage.openWatchersDialog();
        assertEquals(watchersComponent.getWatchers(), asList("admin"));
    }

    @Test
    public void testAddingWatchersWithNoBrowsePermission() {
        String key = backdoor.issues().createIssue("HSP", "xxx").key;
        jira.gotoLoginPage().login("jiradev", "jiradev", DashboardPage.class);
        ViewIssuePage issuePage = jira.goTo(ViewIssuePage.class, key);
        WatchersComponent watchersComponent = issuePage.openWatchersDialog();
        watchersComponent.addWatcher("fred");
        assertTrue(watchersComponent.getWatchers().contains("fred"));
        assertTrue(watchersComponent.getWatchers().contains("admin"));
        issuePage = jira.goTo(ViewIssuePage.class, key);
        watchersComponent = issuePage.openWatchersDialog();
        assertTrue(watchersComponent.getWatchers().contains("fred"));
        assertTrue(watchersComponent.getWatchers().contains("admin"));

    }

    @Test
    public void testDeleteWatchersWithNoBrowsePermission() {
        String key = backdoor.issues().createIssue("HSP", "xxx").key;
        jira.gotoLoginPage().login("jiradev", "jiradev", DashboardPage.class);
        ViewIssuePage issuePage = jira.goTo(ViewIssuePage.class, key);
        WatchersComponent watchersComponent = issuePage.openWatchersDialog();
        watchersComponent.removeWatcher("admin");
        assertEquals(watchersComponent.getWatchers(), new ArrayList<String>());
        issuePage = jira.goTo(ViewIssuePage.class, key);
        watchersComponent = issuePage.openWatchersDialog();
        assertEquals(watchersComponent.getWatchers(), new ArrayList<String>());
    }

    @Test
    public void testNoManagePermissionShowsReadOnly() {
        String key = backdoor.issues().createIssue("HSP", "xxx").key;
        jira.gotoLoginPage().login("nomanage", "nomanage", DashboardPage.class);
        ViewIssuePage issuePage = jira.goTo(ViewIssuePage.class, key);
        WatchersComponent watchersComponent = issuePage.openWatchersDialog();
        assertTrue(watchersComponent.isReadOnly());
        assertEquals(watchersComponent.getWatchers(), asList("admin"));
    }

}
