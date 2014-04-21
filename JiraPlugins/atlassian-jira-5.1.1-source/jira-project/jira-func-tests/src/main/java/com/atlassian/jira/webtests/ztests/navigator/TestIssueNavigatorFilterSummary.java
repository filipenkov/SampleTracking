package com.atlassian.jira.webtests.ztests.navigator;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

@WebTest ({ Category.FUNC_TEST, Category.FILTERS })
public class TestIssueNavigatorFilterSummary extends FuncTestCase
{
    @Override
    public void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestIssueNavigatorFilterSummary.xml");
    }

    @Override
    public void tearDownTest()
    {
        administration.restoreBlankInstance();
        super.tearDownTest();
    }

    public void testProjectInfoInSummaryPane()
    {
        navigation.issueNavigator().gotoNavigator();
        // Click Link 'Manage' (id='managefilters').
        tester.clickLink("managefilters");
        // Click Link 'All projects' (id='filterlink_10000').
        tester.clickLink("filterlink_10000");

        tester.assertLinkPresentWithText("\" onclick=\"alert(123)");
        tester.assertLinkPresentWithText("homosapien");
        tester.assertLinkPresentWithText("Invisible Project");
        tester.assertLinkPresentWithText("monkey");

        navigation.logout();
        navigation.login("blind", "blind");

        navigation.issueNavigator().gotoNavigator();
        // Click Link 'Manage' (id='managefilters').
        tester.clickLink("managefilters");
        // Click Link 'All projects' (id='filterlink_10000').
        tester.clickLink("filterlink_10000");

        tester.assertLinkNotPresentWithText("\" onclick=\"alert(123)");
        tester.assertLinkNotPresentWithText("homosapien");
        tester.assertLinkNotPresentWithText("Invisible Project");
        tester.assertLinkNotPresentWithText("monkey");
        tester.assertTextNotPresent("homosapien");
        tester.assertTextNotPresent("Invisible Project");
        tester.assertTextNotPresent("monkey");
        tester.assertTextNotPresent("Project (id=10000)</span>,"); // assert presence of comma as well
        tester.assertTextNotPresent("Project (id=10001)</span>,"); // assert presence of comma as well
        tester.assertTextNotPresent("Project (id=10010)");

        tester.assertTextNotPresent("JQL Query: project in (");
        text.assertRegexMatch(tester.getDialog().getResponseText(), "project in \\(.*10020.*\\)");
        text.assertRegexMatch(tester.getDialog().getResponseText(), "project in \\(.*10000.*\\)");
        text.assertRegexMatch(tester.getDialog().getResponseText(), "project in \\(.*10010.*\\)");
        text.assertRegexMatch(tester.getDialog().getResponseText(), "project in \\(.*10001.*\\)");

        navigation.logout();
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
    }
}
