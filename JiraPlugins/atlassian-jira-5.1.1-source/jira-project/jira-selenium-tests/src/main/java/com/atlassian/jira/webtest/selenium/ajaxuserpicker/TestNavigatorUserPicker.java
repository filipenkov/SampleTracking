package com.atlassian.jira.webtest.selenium.ajaxuserpicker;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.selenium.Browser;
import com.atlassian.selenium.SkipInBrowser;
import junit.framework.Test;

@SkipInBrowser(browsers={Browser.IE}) //JS Errors - Responsibility: JIRA Team
@WebTest({Category.SELENIUM_TEST })
public class TestNavigatorUserPicker extends AbstractTestAjaxUserPicker
{
    private static final String NOUSERPICKERUSER = "nouserpickeruser";
    private static final String CF_MULTI = "searcher-customfield_10001";
    private static final String CF_SINGLE = "searcher-customfield_10000";
    private static final String REPORTER = "searcher-reporter";
    private static final String ASSIGNEE = "searcher-assignee";

    public static Test suite()
    {
        return suiteFor(TestNavigatorUserPicker.class);
    }

    public void testNavReporter()
    {
        getNavigator().gotoFindIssuesSimple().expandAllNavigatorSections();
        testUserGroupPicker(REPORTER);
        //submit the form so that we're not left with a half backed form!
        client.click("issue-filter-submit");
    }

    public void testNavAssignee()
    {
        getNavigator().gotoFindIssuesSimple().expandAllNavigatorSections();
        testUserGroupPicker(ASSIGNEE);
        //submit the form so that we're not left with a half backed form!
        client.click("issue-filter-submit");
    }

    public void testNavCustomMulti()
    {
        getNavigator().gotoFindIssuesSimple().expandAllNavigatorSections();
        testSimpleUserPicker(CF_MULTI);
        //submit the form so that we're not left with a half backed form!
        client.click("issue-filter-submit");
    }

    public void testNavCustomSingle()
    {
        getNavigator().gotoFindIssuesSimple().expandAllNavigatorSections();
        testSimpleUserPicker(CF_SINGLE);
        //submit the form so that we're not left with a half backed form!
        client.click("issue-filter-submit");
    }

    public void testNavReporterNoPermission()
    {
        getNavigator().logout(getXsrfToken());
        getNavigator().login(NOUSERPICKERUSER, NOUSERPICKERUSER);
        getNavigator().gotoFindIssuesSimple().expandAllNavigatorSections();
        testNotPermittedUserGroupPicker(REPORTER);
        //submit the form so that we're not left with a half backed form!
        client.click("issue-filter-submit");
    }

    public void testNavAssigneeNoPermission()
    {
        getNavigator().logout(getXsrfToken());
        getNavigator().login(NOUSERPICKERUSER, NOUSERPICKERUSER);
        getNavigator().gotoFindIssuesSimple().expandAllNavigatorSections();
        testNotPermittedUserGroupPicker(ASSIGNEE);
        //submit the form so that we're not left with a half backed form!
        client.click("issue-filter-submit");
    }

    public void testNavCustomMultiNoPermission()
    {
        getNavigator().logout(getXsrfToken());
        getNavigator().login(NOUSERPICKERUSER, NOUSERPICKERUSER);
        getNavigator().gotoFindIssuesSimple().expandAllNavigatorSections();
        testNotPermittedSimpleUserPicker(CF_MULTI);
        //submit the form so that we're not left with a half backed form!
        client.click("issue-filter-submit");
    }

    public void testNavCustomSingleNoPermission()
    {
        getNavigator().logout(getXsrfToken());
        getNavigator().login(NOUSERPICKERUSER, NOUSERPICKERUSER);
        getNavigator().gotoFindIssuesSimple().expandAllNavigatorSections();
        testNotPermittedSimpleUserPicker(CF_SINGLE);
        //submit the form so that we're not left with a half backed form!
        client.click("issue-filter-submit");
    }

    public void testNavReporterNoLogin()
    {
        setupNotLoggedInBrowseProjects();
        getNavigator().logout(getXsrfToken());
        getNavigator().gotoHome();
        getNavigator().gotoFindIssuesSimple().expandAllNavigatorSections();
        testNotPermittedUserGroupPicker(REPORTER);
        //submit the form so that we're not left with a half backed form!
        client.click("issue-filter-submit");
    }

    public void testNavAssigneeNoLogin()
    {
        setupNotLoggedInBrowseProjects();
        getNavigator().logout(getXsrfToken());
        getNavigator().gotoHome();
        getNavigator().gotoFindIssuesSimple().expandAllNavigatorSections();
        testNotPermittedUserGroupPicker(ASSIGNEE);
        //submit the form so that we're not left with a half backed form!
        client.click("issue-filter-submit");
    }


    public void testNavCustomSingleNoLogin()
    {
        setupNotLoggedInBrowseProjects();
        getNavigator().logout(getXsrfToken());
        getNavigator().gotoHome();
        getNavigator().gotoFindIssuesSimple().expandAllNavigatorSections();
        testNotPermittedSimpleUserPicker(CF_SINGLE);
        //submit the form so that we're not left with a half backed form!
        client.click("issue-filter-submit");
    }

    public void testNavCustomMultiNoLogin()
    {
        setupNotLoggedInBrowseProjects();
        getNavigator().logout(getXsrfToken());
        getNavigator().gotoHome();
        getNavigator().gotoFindIssuesSimple().expandAllNavigatorSections();
        testNotPermittedSimpleUserPicker(CF_MULTI);
        //submit the form so that we're not left with a half backed form!
        client.click("issue-filter-submit");
    }

    private void setupNotLoggedInBrowseProjects()
    {
        getNavigator().gotoAdmin();
        client.click("permission_schemes", true);
        client.click("0_edit", true);
        client.click("add_perm_10", true);
        client.click("type_group", false);
        client.click("group", false);
        client.click("//option[@value='']", false);
        client.click("document.jiraform.elements[' Add ']", true);
        client.click("link=Log Out", true);
    }

    private void testNotPermittedUserGroupPicker(String fieldId)
    {
        toggleUserGroupSelect(fieldId, "Specify User");
        notPermittedACAsserts(fieldId);
    }

    private void testUserGroupPicker(String fieldId)
    {
        // make sure they cant select a group
        toggleUserGroupSelect(fieldId, "Specify Group");
        commonNegativeACAsserts(fieldId);

        toggleUserGroupSelect(fieldId, "Specify User");
        commonPostiveACAsserts(fieldId);
    }

    private void toggleUserGroupSelect(String fieldId, String label)
    {
        client.select(fieldId + "Select", "label=" + label);
    }
}
