package com.atlassian.jira.webtests.ztests.project;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * @since v4.0
 */
@WebTest ( { Category.FUNC_TEST, Category.PROJECTS })
public class TestAddProject extends FuncTestCase
{
    public void testNoPermission()
    {
        administration.restoreBlankInstance();

        navigation.logout();
        navigation.login(FRED_USERNAME, FRED_PASSWORD);

        //try to create a project without the right permissions.
        tester.gotoPage(page.addXsrfToken("secure/admin/AddProject.jspa?name=newproject&key=NEW&lead=admin"));

        tester.assertTextPresent("Welcome to jWebTest JIRA installation");
        tester.assertTextNotPresent("Project: newproject");
        tester.assertTextNotPresent("Add a new project");
    }
}
