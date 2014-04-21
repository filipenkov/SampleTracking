package com.atlassian.jira.webtests.ztests.navigator.jql;

import com.atlassian.jira.functest.framework.navigation.IssueNavigatorNavigation;
import com.atlassian.jira.functest.framework.navigator.ComponentCondition;
import com.atlassian.jira.functest.framework.navigator.NavigatorSearch;
import com.atlassian.jira.functest.framework.navigator.ProjectCondition;
import com.atlassian.jira.functest.framework.navigator.VersionCondition;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * Tests around selecting a context in the filter form, which enables you to select specific searcher values, and then
 * changing the context.
 *
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.JQL })
public class TestIssueNavigatorContextSwitching extends AbstractJqlFuncTest
{
    public void testAffectedVersion() throws Exception
    {
        administration.restoreData("TestIssueNavigatorContextSwitching.xml");

        ProjectCondition projectCondition = new ProjectCondition().addProject("homosapien");
        final VersionCondition versionCondition = new VersionCondition("version");
        versionCondition.addOption("New Version 1");

        final NavigatorSearch search = new NavigatorSearch(
                projectCondition,
                versionCondition
        );
        navigation.issueNavigator().createSearch(search);

        assertIssues("HSP-1");

        // modify project to be monkey
        navigation.issueNavigator().gotoEditMode(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);
        tester.setWorkingForm("issue-filter");
        new ProjectCondition().addProject("monkey").setForm(tester);
        tester.clickButton("issue-filter-submit");

        assertIssues();
        assertJqlQueryInTextArea("project = MKY AND affectedVersion = 10000");
        assertions.getIssueNavigatorAssertions().assertJqlTooComplex();

        // create a new search where we swap from homosapien to All Projects
        navigation.issueNavigator().createSearch(search);
        assertIssues("HSP-1");

        navigation.issueNavigator().gotoEditMode(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);
        tester.setWorkingForm("issue-filter");
        new ProjectCondition().addProject("All projects").setForm(tester);
        tester.clickButton("issue-filter-submit");

        assertIssues("MKY-1", "HSP-1");
        assertJqlQueryInTextArea("affectedVersion = \"New Version 1\"");
        assertions.getIssueNavigatorAssertions().assertJqlTooComplex();

        // create a new search where we clear the project selection entirely
        navigation.issueNavigator().createSearch(search);
        assertIssues("HSP-1");

        navigation.issueNavigator().gotoEditMode(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);
        tester.setWorkingForm("issue-filter");
        new ProjectCondition().setForm(tester);
        tester.clickButton("issue-filter-submit");

        assertIssues("MKY-1", "HSP-1");
        assertJqlQueryInTextArea("affectedVersion = \"New Version 1\"");
        assertions.getIssueNavigatorAssertions().assertJqlTooComplex();
    }

    public void testFixForVersion() throws Exception
    {
        administration.restoreData("TestIssueNavigatorContextSwitching.xml");

        ProjectCondition projectCondition = new ProjectCondition().addProject("homosapien");
        final VersionCondition versionCondition = new VersionCondition("fixfor");
        versionCondition.addOption("New Version 1");

        final NavigatorSearch search = new NavigatorSearch(
                projectCondition,
                versionCondition
        );
        navigation.issueNavigator().createSearch(search);

        assertIssues("HSP-1");

        // modify project to be monkey
        navigation.issueNavigator().gotoEditMode(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);
        tester.setWorkingForm("issue-filter");
        new ProjectCondition().addProject("monkey").setForm(tester);
        tester.clickButton("issue-filter-submit");

        assertIssues();
        assertJqlQueryInTextArea("project = MKY AND fixVersion = 10000");
        assertions.getIssueNavigatorAssertions().assertJqlTooComplex();

        // create a new search where we swap from homosapien to All Projects
        navigation.issueNavigator().createSearch(search);
        assertIssues("HSP-1");

        navigation.issueNavigator().gotoEditMode(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);
        tester.setWorkingForm("issue-filter");
        new ProjectCondition().addProject("All projects").setForm(tester);
        tester.clickButton("issue-filter-submit");

        assertIssues("MKY-1", "HSP-1");
        assertJqlQueryInTextArea("fixVersion = \"New Version 1\"");
        assertions.getIssueNavigatorAssertions().assertJqlTooComplex();

        // create a new search where we clear the project selection entirely
        navigation.issueNavigator().createSearch(search);
        assertIssues("HSP-1");

        navigation.issueNavigator().gotoEditMode(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);
        tester.setWorkingForm("issue-filter");
        new ProjectCondition().setForm(tester);
        tester.clickButton("issue-filter-submit");

        assertIssues("MKY-1", "HSP-1");
        assertJqlQueryInTextArea("fixVersion = \"New Version 1\"");
        assertions.getIssueNavigatorAssertions().assertJqlTooComplex();
    }

    public void testComponents() throws Exception
    {
        administration.restoreData("TestIssueNavigatorContextSwitching.xml");

        ProjectCondition projectCondition = new ProjectCondition().addProject("homosapien");
        final ComponentCondition componentCondition = new ComponentCondition();
        componentCondition.addOption("New Component 1");

        final NavigatorSearch search = new NavigatorSearch(
                projectCondition,
                componentCondition
        );
        navigation.issueNavigator().createSearch(search);

        assertIssues("HSP-1");

        // modify project to be monkey
        navigation.issueNavigator().gotoEditMode(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);
        tester.setWorkingForm("issue-filter");
        new ProjectCondition().addProject("monkey").setForm(tester);
        tester.clickButton("issue-filter-submit");

        assertIssues();
        assertJqlQueryInTextArea("project = MKY AND component = 10000");
        assertions.getIssueNavigatorAssertions().assertJqlTooComplex();

        // create a new search where we swap from homosapien to All Projects
        navigation.issueNavigator().createSearch(search);
        assertIssues("HSP-1");

        navigation.issueNavigator().gotoEditMode(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);
        tester.setWorkingForm("issue-filter");
        new ProjectCondition().addProject("All projects").setForm(tester);
        tester.clickButton("issue-filter-submit");

        assertIssues("MKY-1", "HSP-1");
        assertJqlQueryInTextArea("component = \"New Component 1\"");
        assertions.getIssueNavigatorAssertions().assertJqlTooComplex();

        // create a new search where we clear the project selection entirely
        navigation.issueNavigator().createSearch(search);
        assertIssues("HSP-1");

        navigation.issueNavigator().gotoEditMode(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);
        tester.setWorkingForm("issue-filter");
        new ProjectCondition().setForm(tester);
        tester.clickButton("issue-filter-submit");

        assertIssues("MKY-1", "HSP-1");
        assertJqlQueryInTextArea("component = \"New Component 1\"");
        assertions.getIssueNavigatorAssertions().assertJqlTooComplex();
    }

    public void testVersionCF() throws Exception
    {
        administration.restoreData("TestIssueNavigatorContextSwitching.xml");

        ProjectCondition projectCondition = new ProjectCondition().addProject("homosapien");
        final VersionCondition versionCondition = new VersionCondition("customfield_10000");
        versionCondition.addOption("New Version 1");

        final NavigatorSearch search = new NavigatorSearch(
                projectCondition,
                versionCondition
        );
        navigation.issueNavigator().createSearch(search);

        assertIssues("HSP-1");

        // modify project to be monkey
        navigation.issueNavigator().gotoEditMode(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);
        tester.setWorkingForm("issue-filter");
        new ProjectCondition().addProject("monkey").setForm(tester);
        tester.clickButton("issue-filter-submit");

        assertIssues();
        assertJqlQueryInTextArea("project = MKY AND \"Version CF\" = 10000");
        assertions.getIssueNavigatorAssertions().assertJqlTooComplex();

        // create a new search where we swap from homosapien to All Projects
        navigation.issueNavigator().createSearch(search);
        assertIssues("HSP-1");

        navigation.issueNavigator().gotoEditMode(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);
        tester.setWorkingForm("issue-filter");
        new ProjectCondition().addProject("All projects").setForm(tester);
        tester.clickButton("issue-filter-submit");

        assertIssues("MKY-1", "HSP-1");
        assertJqlQueryInTextArea("\"Version CF\" = \"New Version 1\"");
        assertions.getIssueNavigatorAssertions().assertJqlTooComplex();

        // create a new search where we clear the project selection entirely
        navigation.issueNavigator().createSearch(search);
        assertIssues("HSP-1");

        navigation.issueNavigator().gotoEditMode(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);
        tester.setWorkingForm("issue-filter");
        new ProjectCondition().setForm(tester);
        tester.clickButton("issue-filter-submit");

        assertIssues("MKY-1", "HSP-1");
        assertJqlQueryInTextArea("\"Version CF\" = \"New Version 1\"");
        assertions.getIssueNavigatorAssertions().assertJqlTooComplex();
    }

    public void testMultiVersionCF() throws Exception
    {
        administration.restoreData("TestIssueNavigatorContextSwitching.xml");

        ProjectCondition projectCondition = new ProjectCondition().addProject("homosapien");
        final VersionCondition versionCondition = new VersionCondition("customfield_10001");
        versionCondition.addOption("New Version 1");

        final NavigatorSearch search = new NavigatorSearch(
                projectCondition,
                versionCondition
        );
        navigation.issueNavigator().createSearch(search);

        assertIssues("HSP-1");

        // modify project to be monkey
        navigation.issueNavigator().gotoEditMode(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);
        tester.setWorkingForm("issue-filter");
        new ProjectCondition().addProject("monkey").setForm(tester);
        tester.clickButton("issue-filter-submit");

        assertIssues();
        assertJqlQueryInTextArea("project = MKY AND \"Multi Version CF\" = 10000");
        assertions.getIssueNavigatorAssertions().assertJqlTooComplex();

        // create a new search where we swap from homosapien to All Projects
        navigation.issueNavigator().createSearch(search);
        assertIssues("HSP-1");

        navigation.issueNavigator().gotoEditMode(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);
        tester.setWorkingForm("issue-filter");
        new ProjectCondition().addProject("All projects").setForm(tester);
        tester.clickButton("issue-filter-submit");

        assertIssues("MKY-1", "HSP-1");
        assertJqlQueryInTextArea("\"Multi Version CF\" = \"New Version 1\"");
        assertions.getIssueNavigatorAssertions().assertJqlTooComplex();

        // create a new search where we clear the project selection entirely
        navigation.issueNavigator().createSearch(search);
        assertIssues("HSP-1");

        navigation.issueNavigator().gotoEditMode(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);
        tester.setWorkingForm("issue-filter");
        new ProjectCondition().setForm(tester);
        tester.clickButton("issue-filter-submit");

        assertIssues("MKY-1", "HSP-1");
        assertJqlQueryInTextArea("\"Multi Version CF\" = \"New Version 1\"");
        assertions.getIssueNavigatorAssertions().assertJqlTooComplex();
    }

}
