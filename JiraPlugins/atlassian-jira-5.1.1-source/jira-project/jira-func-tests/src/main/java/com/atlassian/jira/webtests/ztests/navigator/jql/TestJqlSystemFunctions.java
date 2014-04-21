package com.atlassian.jira.webtests.ztests.navigator.jql;

import com.atlassian.jira.functest.framework.Splitable;
import com.atlassian.jira.functest.framework.assertions.IssueNavigatorAssertions;
import com.atlassian.jira.functest.framework.navigation.IssueNavigatorNavigation;
import com.atlassian.jira.functest.framework.navigator.AssigneeCondition;
import com.atlassian.jira.functest.framework.navigator.NavigatorSearch;
import com.atlassian.jira.functest.framework.navigator.ReporterCondition;
import com.atlassian.jira.functest.framework.navigator.UserGroupPicker;
import com.atlassian.jira.functest.framework.sharing.SharedEntityInfo;
import com.atlassian.jira.functest.framework.sharing.TestSharingPermissionUtils;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import junit.framework.Assert;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Test for JQL included JQL system functions.
 *
 * @since v4.0
 */
@Splitable
@WebTest ({ Category.FUNC_TEST, Category.JQL })
public class TestJqlSystemFunctions extends AbstractJqlFuncTest
{
    public void testCascadeOption()
    {
        administration.restoreData("TestCascadeOptionFunction.xml");

        createSearchAndAssertIssues("project in (HSP, MKY) and CS1 in cascadeOption(a)", "MKY-2", "HSP-4", "HSP-2");
        // this is too complex to fit because a is not unique
        assertTooComplex();

        String query = "project in (MKY) and CS1 in cascadeOption(10011)";
        createSearchAndAssertIssues(query, "MKY-4", "MKY-3");
        assertions.getIssueNavigatorAssertions().assertJqlFitsInFilterForm(query, createParam("customfield_10000", "10004"), createParam("customfield_10000:1", ""));

        query = "project in (MKY) and CS1 in cascadeOption(10011, child)";
        createSearchAndAssertIssues(query, "MKY-3");
        assertions.getIssueNavigatorAssertions().assertJqlFitsInFilterForm(query, createParam("customfield_10000", "10004"), createParam("customfield_10000:1", "10006"));

        createSearchAndAssertIssues("project in (HSP, MKY) and CS1 in cascadeOption(a, None)", "MKY-2", "HSP-2");
        // the none option for a child is too complex to fit in the simple query editor
        assertTooComplex();

        query = "project in (MKY) and CS1 in cascadeOption(10011, '\"none\"')";
        createSearchAndAssertIssues(query, "MKY-4");
        assertions.getIssueNavigatorAssertions().assertJqlFitsInFilterForm(query, createParam("customfield_10000", "10004"), createParam("customfield_10000:1", "10008"));

        query = "project in (MKY) and CS1 in cascadeOption('\"none\"')";
        createSearchAndAssertIssues(query, "MKY-1");
        assertions.getIssueNavigatorAssertions().assertJqlFitsInFilterForm(query, createParam("customfield_10000", "10005"));

        createSearchAndAssertIssues("project in (MKY) and CS1 in cascadeOption(none)", "MKY-5");
        assertTooComplex();

        // verify that going from simple to advanced results in generating the function
        navigation.issueNavigator().gotoNewMode(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);
        tester.selectOption("pid", "homosapien");
        tester.submit("show");
        tester.selectOption("customfield_10000", "a");
        tester.selectOption("customfield_10000:1", "ac");
        tester.submit("show");
        tester.clickLink("switchnavtype");
        String jql = tester.getDialog().getFormParameterValue("jqlQuery");
        Assert.assertEquals("project = HSP AND CS1 in cascadeOption(10000, 10009)", jql);

        // validation checks
        navigation.issueNavigator().createSearch("CS1 in cascadeOption(notaparent)");
        assertions.getIssueNavigatorAssertions().assertJqlErrors("The option 'notaparent' specified in function 'cascadeOption' is not a valid parent option.");

        navigation.issueNavigator().createSearch("CS1 in cascadeOption(99999)");
        assertions.getIssueNavigatorAssertions().assertJqlErrors("The option '99999' specified in function 'cascadeOption' is not a valid parent option.");

        navigation.issueNavigator().createSearch("CS1 in cascadeOption(a, fakechild)");
        assertions.getIssueNavigatorAssertions().assertJqlErrors("The option 'fakechild' is not a child of option 'a' in function 'cascadeOption'.");

        navigation.issueNavigator().createSearch("CS1 in cascadeOption(a, 999999)");
        assertions.getIssueNavigatorAssertions().assertJqlErrors("The option '999999' is not a child of option 'a' in function 'cascadeOption'.");

        navigation.issueNavigator().createSearch("CS2 in cascadeOption(a, ac)");
        assertions.getIssueNavigatorAssertions().assertJqlErrors("The option 'a' specified in function 'cascadeOption' is not a valid parent option.");
        
        navigation.issueNavigator().createSearch("CS2 in cascadeOption(a)");
        assertions.getIssueNavigatorAssertions().assertJqlErrors("The option 'a' specified in function 'cascadeOption' is not a valid parent option.");

        navigation.issueNavigator().createSearch("CS2 in cascadeOption(None, a)");
        assertions.getIssueNavigatorAssertions().assertJqlErrors("The option 'None' specified in function 'cascadeOption' is not a valid parent option.");

        navigation.issueNavigator().createSearch("issue in cascadeOption(a)");
        assertions.getIssueNavigatorAssertions().assertJqlErrors("The field 'issue' is not supported by the function 'cascadeOption'.");
    }

    public void testAllReleasedVersions() throws Exception
    {
        administration.restoreData("TestJqlReleasedVersionsFunctions.xml");

        // test correctness of results
        createSearchAndAssertIssues("fixVersion in releasedVersions()", "NUMBER-2", "MKY-2", "MK-2", "HSP-3");
        createSearchAndAssertIssues("affectedVersion in releasedVersions()", "NUMBER-1", "MKY-1", "MK-1", "HSP-1");
        createSearchAndAssertIssues("VP in releasedVersions()", "NUMBER-1", "MKY-1", "MK-1", "HSP-1");

        // resolution of project argument happens in the following order: key, name, id; case-insensitive
        createSearchAndAssertIssues("VP in releasedVersions(HSP)", "HSP-1");
        createSearchAndAssertIssues("VP in releasedVersions(hsp)", "HSP-1");
        createSearchAndAssertIssues("VP in releasedVersions(homosapien)", "HSP-1");
        createSearchAndAssertIssues("VP in releasedVersions(HOMOSAPIEN)", "HSP-1");
        createSearchAndAssertIssues("VP in releasedVersions(10000)", "NUMBER-1");
        createSearchAndAssertIssues("VP in releasedVersions(monkey)", "MKY-1");
        createSearchAndAssertIssues("VP in releasedVersions(MKY)", "MKY-1");
        createSearchAndAssertIssues("VP in releasedVersions(MK)", "MK-1");
        createSearchAndAssertIssues("VP in releasedVersions(10001)", "MKY-1");

        // filter specifies "HSP" as function argument; admin can see HSP, so no sanitisation
        navigation.issueNavigator().loadFilter(10000, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        tester.assertTextPresent("VP in releasedVersions(HSP)");

        // fred cannot see project HSP
        navigation.logout();
        navigation.login(FRED_USERNAME);
        createSearchAndAssertIssues("fixVersion in releasedVersions()", "NUMBER-2", "MKY-2", "MK-2");
        createSearchAndAssertIssues("affectedVersion in releasedVersions()", "NUMBER-1", "MKY-1", "MK-1");

        // filter specifies "HSP" as function argument, but fred cannot see HSP, so it will be sanitised to "10000"
        navigation.issueNavigator().loadFilter(10000, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        tester.assertTextPresent("VP in releasedVersions(10000)");

        // verify that going from simple to advanced results in generating the releasedVersions function
        tester.clickLink("find_link");
        tester.clickLink("new_filter");
        tester.clickLink("switchnavtype");
        tester.selectOption("pid", "10000");
        tester.submit("show");
        tester.selectOption("fixfor", "Released Versions");
        tester.submit("show");
        tester.clickLink("switchnavtype");
        String jql = tester.getDialog().getFormParameterValue("jqlQuery");
        Assert.assertEquals("project = \"NUMBER\" AND fixVersion in releasedVersions()", jql);

        tester.clickLink("find_link");
        tester.clickLink("new_filter");
        tester.clickLink("switchnavtype");
        tester.selectOption("pid", "10000");
        tester.submit("show");
        tester.selectOption("version", "Released Versions");
        tester.submit("show");
        tester.clickLink("switchnavtype");
        jql = tester.getDialog().getFormParameterValue("jqlQuery");
        Assert.assertEquals("project = \"NUMBER\" AND affectedVersion in releasedVersions()", jql);
    }

    public void testLatestReleasedVersion() throws Exception
    {
        administration.restoreData("TestJqlReleasedVersionsFunctions.xml");

        // test correctness of results
        createSearchAndAssertIssues("fixVersion = latestReleasedVersion(MKY)", "MKY-2");
        createSearchAndAssertIssues("affectedVersion = latestReleasedVersion(MKY)", "MKY-1");
        createSearchAndAssertIssues("VP = latestReleasedVersion(MKY)", "MKY-1");
        createSearchAndAssertIssues("VP = latestReleasedVersion(MK)", "MK-1");

        // resolution of project argument happens in the following order: key, name, id; case-insensitive
        createSearchAndAssertIssues("VP = latestReleasedVersion(HSP)", "HSP-1");
        createSearchAndAssertIssues("VP = latestReleasedVersion(hsp)", "HSP-1");
        createSearchAndAssertIssues("VP = latestReleasedVersion(homosapien)", "HSP-1");
        createSearchAndAssertIssues("VP = latestReleasedVersion(HOMOSAPIEN)", "HSP-1");
        createSearchAndAssertIssues("VP = latestReleasedVersion(10000)", "NUMBER-1");
        createSearchAndAssertIssues("VP = latestReleasedVersion(monkey)", "MKY-1");
        createSearchAndAssertIssues("VP = latestReleasedVersion(MKY)", "MKY-1");
        createSearchAndAssertIssues("VP = latestReleasedVersion(MK)", "MK-1");
        createSearchAndAssertIssues("VP = latestReleasedVersion(10001)", "MKY-1");

        // Test with no argument (all projects)
        createSearchAndAssertIssues("VP = latestReleasedVersion()", "NUMBER-1", "MKY-1", "MK-1", "HSP-1");

        // Test with multiple projects
        createSearchAndAssertIssues("VP = latestReleasedVersion(HSP, MKY)", "MKY-1", "HSP-1");
        createSearchAndAssertIssues("VP = latestReleasedVersion(HSP, MKY, MK)", "MKY-1", "MK-1", "HSP-1");
        createSearchAndAssertIssues("VP = latestReleasedVersion(HSP, 10001)", "MKY-1", "HSP-1");


        // filter specifies "HSP" as function argument; admin can see HSP, so no sanitisation
        navigation.issueNavigator().loadFilter(10020, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        tester.assertTextPresent("VP = latestReleasedVersion(HSP)");

        // fred cannot see project HSP
        navigation.logout();
        navigation.login(FRED_USERNAME);
        navigation.issueNavigator().createSearch("VP = latestReleasedVersion(HSP)");
        assertions.getIssueNavigatorAssertions().assertJqlErrors("Could not resolve the project 'HSP' provided to function 'latestReleasedVersion'.");

        // filter specifies "HSP" as function argument, but fred cannot see HSP, so it will be sanitised to "10000"
        navigation.issueNavigator().loadFilter(10020, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        tester.assertTextPresent("VP = latestReleasedVersion(10000)");
    }

    public void testAllUnreleasedVersions() throws Exception
    {
        administration.restoreData("TestJqlReleasedVersionsFunctions.xml");

        // test correctness of results
        createSearchAndAssertIssues("fixVersion in unreleasedVersions()", "NUMBER-1", "MKY-1", "MK-1", "HSP-2", "HSP-1");
        createSearchAndAssertIssues("affectedVersion in unreleasedVersions()", "NUMBER-2", "MKY-2", "MK-2", "HSP-3", "HSP-2");
        createSearchAndAssertIssues("VP in unreleasedVersions()", "NUMBER-2", "MKY-2", "MK-2", "HSP-3", "HSP-2");

        // resolution of project argument happens in the following order: key, name, id; case-insensitive
        createSearchAndAssertIssues("VP in unreleasedVersions(HSP)", "HSP-3", "HSP-2");
        createSearchAndAssertIssues("VP in unreleasedVersions(hsp)", "HSP-3", "HSP-2");
        createSearchAndAssertIssues("VP in unreleasedVersions(homosapien)", "HSP-3", "HSP-2");
        createSearchAndAssertIssues("VP in unreleasedVersions(HOMOSAPIEN)", "HSP-3", "HSP-2");
        createSearchAndAssertIssues("VP in unreleasedVersions(10000)", "NUMBER-2");
        createSearchAndAssertIssues("VP in unreleasedVersions(monkey)", "MKY-2");
        createSearchAndAssertIssues("VP in unreleasedVersions(MKY)", "MKY-2");
        createSearchAndAssertIssues("VP in unreleasedVersions(MK)", "MK-2");
        createSearchAndAssertIssues("VP in unreleasedVersions(10001)", "MKY-2");

        // filter specifies "HSP" as function argument; admin can see HSP, so no sanitisation
        navigation.issueNavigator().loadFilter(10010, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        tester.assertTextPresent("VP in unreleasedVersions(HSP)");

        // fred cannot see project HSP
        navigation.logout();
        navigation.login(FRED_USERNAME);
        createSearchAndAssertIssues("fixVersion in unreleasedVersions()", "NUMBER-1", "MKY-1", "MK-1");
        createSearchAndAssertIssues("affectedVersion in unreleasedVersions()", "NUMBER-2", "MKY-2", "MK-2");

        // filter specifies "HSP" as function argument, but fred cannot see HSP, so it will be sanitised to "10000"
        navigation.issueNavigator().loadFilter(10010, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        tester.assertTextPresent("VP in unreleasedVersions(10000)");

        // verify that going from simple to advanced results in generating the unreleasedVersions function
        tester.clickLink("find_link");
        tester.clickLink("new_filter");
        tester.clickLink("switchnavtype");
        tester.selectOption("pid", "10000");
        tester.submit("show");
        tester.selectOption("fixfor", "Unreleased Versions");
        tester.submit("show");
        tester.clickLink("switchnavtype");
        String jql = tester.getDialog().getFormParameterValue("jqlQuery");
        Assert.assertEquals("project = \"NUMBER\" AND fixVersion in unreleasedVersions()", jql);

        tester.clickLink("find_link");
        tester.clickLink("new_filter");
        tester.clickLink("switchnavtype");
        tester.selectOption("pid", "10000");
        tester.submit("show");
        tester.selectOption("version", "Unreleased Versions");
        tester.submit("show");
        tester.clickLink("switchnavtype");
        jql = tester.getDialog().getFormParameterValue("jqlQuery");
        Assert.assertEquals("project = \"NUMBER\" AND affectedVersion in unreleasedVersions()", jql);
    }

    public void testEarliestUnreleasedVersion() throws Exception
    {
        administration.restoreData("TestJqlReleasedVersionsFunctions.xml");

        // test correctness of results
        createSearchAndAssertIssues("fixVersion = earliestUnreleasedVersion(MKY)", "MKY-1");
        createSearchAndAssertIssues("affectedVersion = earliestUnreleasedVersion(MKY)", "MKY-2");
        createSearchAndAssertIssues("VP = earliestUnreleasedVersion(MKY)", "MKY-2");
        createSearchAndAssertIssues("VP = earliestUnreleasedVersion(MK)", "MK-2");

        // resolution of project argument happens in the following order: key, name, id; case-insensitive
        createSearchAndAssertIssues("VP = earliestUnreleasedVersion(HSP)", "HSP-3");
        createSearchAndAssertIssues("VP = earliestUnreleasedVersion(hsp)", "HSP-3");
        createSearchAndAssertIssues("VP = earliestUnreleasedVersion(homosapien)", "HSP-3");
        createSearchAndAssertIssues("VP = earliestUnreleasedVersion(HOMOSAPIEN)", "HSP-3");
        createSearchAndAssertIssues("VP = earliestUnreleasedVersion(10000)", "NUMBER-2");
        createSearchAndAssertIssues("VP = earliestUnreleasedVersion(monkey)", "MKY-2");
        createSearchAndAssertIssues("VP = earliestUnreleasedVersion(MKY)", "MKY-2");
        createSearchAndAssertIssues("VP = earliestUnreleasedVersion(MK)", "MK-2");
        createSearchAndAssertIssues("VP = earliestUnreleasedVersion(10001)", "MKY-2");

        // Test with no argument (all projects)
        createSearchAndAssertIssues("VP = earliestUnreleasedVersion()", "NUMBER-2", "MKY-2", "MK-2", "HSP-3");

        // Test with multiple projects
        createSearchAndAssertIssues("VP = earliestUnreleasedVersion(HSP, MKY)", "MKY-2", "HSP-3");
        createSearchAndAssertIssues("VP = earliestUnreleasedVersion(HSP, MKY, MK)", "MKY-2", "MK-2", "HSP-3");
        createSearchAndAssertIssues("VP = earliestUnreleasedVersion(HSP, 10001)", "MKY-2", "HSP-3");

        // filter specifies "HSP" as function argument; admin can see HSP, so no sanitisation
        navigation.issueNavigator().loadFilter(10030, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        tester.assertTextPresent("VP = earliestUnreleasedVersion(HSP)");

        // fred cannot see project HSP
        navigation.logout();
        navigation.login(FRED_USERNAME);
        navigation.issueNavigator().createSearch("VP = earliestUnreleasedVersion(HSP)");
        assertions.getIssueNavigatorAssertions().assertJqlErrors("Could not resolve the project 'HSP' provided to function 'earliestUnreleasedVersion'.");

        // filter specifies "HSP" as function argument, but fred cannot see HSP, so it will be sanitised to "10000"
        navigation.issueNavigator().loadFilter(10030, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        tester.assertTextPresent("VP = earliestUnreleasedVersion(10000)");
    }


    public void testIssueTypesFunctions()
    {
        administration.restoreData("TestJqlStandardIssueTypesFunctions.xml");
        administration.subtasks().disable();
        
        // cant use the function when subtasks are disabled
        navigation.issueNavigator().createSearch("type in standardIssueTypes()");
        assertions.getIssueNavigatorAssertions().assertJqlErrors("Function 'standardIssueTypes' is invalid as sub-tasks are currently disabled.");
        navigation.issueNavigator().createSearch("type in subTaskIssueTypes()");
        assertions.getIssueNavigatorAssertions().assertJqlErrors("Function 'subTaskIssueTypes' is invalid as sub-tasks are currently disabled.");

        // when enabled everything is okay
        administration.subtasks().enable();
        createSearchAndAssertIssues("type in standardIssueTypes()", "HSP-4", "HSP-3", "HSP-2", "HSP-1");
        createSearchAndAssertIssues("type in subTaskIssueTypes()");

        // create a subtask
        final String subTaskKey = navigation.issue().createSubTask("HSP-4", "Sub-task", "SUBSUBSUB", "");
        createSearchAndAssertIssues("type in subTaskIssueTypes()", subTaskKey);

        // function accepts no arguments
        navigation.issueNavigator().createSearch("type in standardIssueTypes(arg)");
        assertions.getIssueNavigatorAssertions().assertJqlErrors("Function 'standardIssueTypes' expected '0' arguments but received '1'.");
        navigation.issueNavigator().createSearch("type in subTaskIssueTypes(arg)");
        assertions.getIssueNavigatorAssertions().assertJqlErrors("Function 'subTaskIssueTypes' expected '0' arguments but received '1'.");

        // verify that going from simple to advanced results in generating the function
        tester.clickLink("find_link");
        tester.clickLink("new_filter");
        tester.clickLink("switchnavtype");
        tester.selectOption("type", "Standard Issue Types");
        tester.submit("show");
        tester.clickLink("switchnavtype");
        String jql = tester.getDialog().getFormParameterValue("jqlQuery");
        Assert.assertEquals("issuetype in standardIssueTypes()", jql);

        tester.clickLink("find_link");
        tester.clickLink("new_filter");
        tester.clickLink("switchnavtype");
        tester.selectOption("type", "Sub-Task Issue Types");
        tester.submit("show");
        tester.clickLink("switchnavtype");
        jql = tester.getDialog().getFormParameterValue("jqlQuery");
        Assert.assertEquals("issuetype in subTaskIssueTypes()", jql);
    }

    // this test uses hand-crafted XML that has filters saved with bogus arguments to JQL functions. We want to
    // verify that these don't just throw 500 pages at users.
    public void testIllegalJqlFunctionParameters()
    {
        administration.restoreData("TestIllegalJqlFunctionParameters.xml");

        // these are the expected error messages for the various crap filters saved in the data
        String[] errorMessages = new String[] {
                "Could not resolve the project 'random argument' provided to function 'releasedVersions'.",
                "Could not resolve the project 'random argument' provided to function 'unreleasedVersions'.",
                createFunctionArgumentError("standardIssueTypes", 0, 1),
                createFunctionArgumentError("subTaskIssueTypes", 0, 1),
                "Incorrect number of arguments specified for the function 'cascadeOption'. Usages: cascadeOption(parentOption), cascadeOption(parentOption, childOption), cascadeOption(parentOption, \"None\").",
                "The option 'random argument' specified in function 'cascadeOption' is not a valid parent option.",
                "The option 'random argument' is not a child of option 'parent' in function 'cascadeOption'.",
                "Incorrect number of arguments specified for the function 'cascadeOption'. Usages: cascadeOption(parentOption), cascadeOption(parentOption, childOption), cascadeOption(parentOption, \"None\").",
                createFunctionArgumentError("currentUser", 0, 1),
                createFunctionArgumentError("issueHistory", 0, 1),
                "Incorrect number of arguments for function 'linkedIssues'. Usage: 'linkedIssues ( issuekey [, linkDescription ]* )'.",
                "Issue 'random argument' could not be found in function 'linkedIssues'.",
                "Issue link type 'random argument' could not be found in function 'linkedIssues'.",
                createFunctionArgumentError("membersOf", 1, 0),
                "Function 'membersOf' can not generate a list of usernames for group 'random argument'; the group does not exist.",
                createFunctionArgumentError("now", 0, 1),
                createFunctionArgumentError("votedIssues", 0, 1),
                createFunctionArgumentError("watchedIssues", 0, 1),
                createFunctionArgumentError("lastLogin", 0, 1),
                createFunctionArgumentError("currentLogin", 0, 1),
        };

        for (int i = 0; i < errorMessages.length; i++)
        {
            String errorMessage = errorMessages[i];
            navigation.issueNavigator().loadFilter(10000 + i, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
            assertions.getIssueNavigatorAssertions().assertJqlErrors(errorMessage);
        }
    }

    public void testVotedIssuesFunction()
    {
        administration.restoreData("TestVotedAndWatchedIssuesFunction.xml");
        navigation.login(FRED_USERNAME);
        createSearchAndAssertIssues("issue IN votedIssues()", "HSP-2", "HSP-1");

        assertTooComplex();

        navigation.issueNavigator().createSearch("issue in votedIssues(arg)");
        assertions.getIssueNavigatorAssertions().assertJqlErrors(createFunctionArgumentError("votedIssues", 0, 1));

        navigation.logout();
        navigation.gotoDashboard();
        navigation.issueNavigator().createSearch("issue in votedIssues()");
        assertions.getIssueNavigatorAssertions().assertJqlErrors("Function 'votedIssues' cannot be called as anonymous user.");

        navigation.issueNavigator().loadFilter(10000, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        assertions.getIssueNavigatorAssertions().assertJqlErrors("Function 'votedIssues' cannot be called as anonymous user.");

        // disable voting and ensure that validation fails
        navigation.login(ADMIN_USERNAME);
        administration.generalConfiguration().disableVoting();

        navigation.login(FRED_USERNAME);
        navigation.issueNavigator().createSearch("issue in votedIssues()");
        assertions.getIssueNavigatorAssertions().assertJqlErrors("Function 'votedIssues' cannot be called as voting on issues is currently disabled.");

        navigation.issueNavigator().loadFilter(10000, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        assertions.getIssueNavigatorAssertions().assertJqlErrors("Function 'votedIssues' cannot be called as voting on issues is currently disabled.");
    }

    public void testWatchedIssuesFunction()
    {
        administration.restoreData("TestVotedAndWatchedIssuesFunction.xml");
        navigation.login(FRED_USERNAME);
        createSearchAndAssertIssues("issue IN watchedIssues()", "HSP-2", "HSP-1");

        assertTooComplex();

        navigation.issueNavigator().createSearch("issue in watchedIssues(arg)");
        assertions.getIssueNavigatorAssertions().assertJqlErrors(createFunctionArgumentError("watchedIssues", 0, 1));

        navigation.logout();
        navigation.gotoDashboard();
        navigation.issueNavigator().createSearch("issue in watchedIssues()");
        assertions.getIssueNavigatorAssertions().assertJqlErrors("Function 'watchedIssues' cannot be called as anonymous user.");

        navigation.issueNavigator().loadFilter(10001, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        assertions.getIssueNavigatorAssertions().assertJqlErrors("Function 'watchedIssues' cannot be called as anonymous user.");

        // disable watching and ensure that validation fails
        navigation.login(ADMIN_USERNAME);
        administration.generalConfiguration().disableWatching();

        navigation.login(FRED_USERNAME);
        navigation.issueNavigator().createSearch("issue in watchedIssues()");
        assertions.getIssueNavigatorAssertions().assertJqlErrors("Function 'watchedIssues' cannot be called as watching issues is currently disabled.");

        navigation.issueNavigator().loadFilter(10001, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        assertions.getIssueNavigatorAssertions().assertJqlErrors("Function 'watchedIssues' cannot be called as watching issues is currently disabled.");
    }

    // fred has MKY-1 in his view issue history but he no longer has permission to see the MKY project. it shouldn't show up
    // in the results from issueHistory()
    public void testIssueHistoryFunction()
    {
        administration.restoreData("TestIssueHistoryFunction.xml");
        navigation.login(FRED_USERNAME);
        navigation.issue().viewIssue("HSP-1");
        createSearchAndAssertIssues("issue in issueHistory()", "HSP-1");

        navigation.issue().viewIssue("HSP-2");
        createSearchAndAssertIssues("issue in issueHistory()", "HSP-2", "HSP-1");

        assertTooComplex();

        navigation.issueNavigator().createSearch("issue in issueHistory(arg)");
        assertions.getIssueNavigatorAssertions().assertJqlErrors(createFunctionArgumentError("issueHistory", 0, 1));

        // check that history is persisted
        navigation.logout();
        navigation.login(FRED_USERNAME);
        createSearchAndAssertIssues("issue in issueHistory()", "HSP-2", "HSP-1");

        // check that a filter only shows us the two issues we are supposed to see in our history
        navigation.issueNavigator().loadFilter(10000, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        assertIssues("HSP-2", "HSP-1");

        navigation.logout();
        navigation.gotoDashboard();
        navigation.issue().viewIssue("HSP-2");
        createSearchAndAssertIssues("issue in issueHistory()", "HSP-2");

        // destroy anonymous' session
        navigation.login(FRED_USERNAME);
        navigation.logout();

        navigation.gotoDashboard();
        navigation.issue().viewIssue("HSP-1");
        createSearchAndAssertIssues("issue in issueHistory()", "HSP-1");
    }

    public void testLinkedIssues()
    {
        administration.restoreData("TestLinkedIssuesFunction.xml");

        // positive test cases for correctness
        // note: case is no longer important for link descriptions
        createSearchAndAssertIssues("issue in linkedIssues('HSP-1')", "MKY-1", "HSP-4", "HSP-3", "HSP-2");
        createSearchAndAssertIssues("issue in linkedIssues(10000)", "MKY-1", "HSP-4", "HSP-3", "HSP-2");
        createSearchAndAssertIssues("issue in linkedIssues('HSP-1', 'blocks')", "MKY-1", "HSP-4", "HSP-3", "HSP-2");
        createSearchAndAssertIssues("issue in linkedIssues('HSP-1', 'blOCKS')", "MKY-1", "HSP-4", "HSP-3", "HSP-2");
        createSearchAndAssertIssues("issue in linkedIssues('HSP-1', 'is blocked by')");
        createSearchAndAssertIssues("issue in linkedIssues('HSP-1', 'duplicates')", "HSP-3", "HSP-2");
        createSearchAndAssertIssues("issue in linkedIssues('HSP-1', 'DUPLICATES')", "HSP-3", "HSP-2");
        createSearchAndAssertIssues("issue in linkedIssues('HSP-1', 'is duplicated by')", "MKY-1");
        createSearchAndAssertIssues("issue in linkedIssues('HSP-1', 'duplicates', 'is duplicated by')", "MKY-1", "HSP-3", "HSP-2");
        createSearchAndAssertIssues("issue in linkedIssues('HSP-1', 'duplICAtes', 'IS duplicated BY')", "MKY-1", "HSP-3", "HSP-2");

        // check for invalid link type descriptions
        navigation.issueNavigator().createSearch("issue in linkedIssues('HSP-1', 'duplicates', 'fred')");
        assertions.getIssueNavigatorAssertions().assertJqlErrors("Issue link type 'fred' could not be found in function 'linkedIssues'.");

        // invalid issue key or issue id
        navigation.issueNavigator().createSearch("issue in linkedIssues('HSP-5')");
        assertions.getIssueNavigatorAssertions().assertJqlErrors("Issue 'HSP-5' could not be found in function 'linkedIssues'.");
        navigation.issueNavigator().createSearch("issue in linkedIssues(99999)");
        assertions.getIssueNavigatorAssertions().assertJqlErrors("Issue '99999' could not be found in function 'linkedIssues'.");

        // check that the function doesn't fit on the simple editing page
        createSearchAndAssertIssues("issue in linkedIssues('HSP-1', 'is blocked by')");
        assertTooComplex();

        // ensure that the function only operates when issue linking is enabled
        administration.issueLinking().disable();
        navigation.issueNavigator().createSearch("issue in linkedIssues('10000')");
        assertions.getIssueNavigatorAssertions().assertJqlErrors("Function 'linkedIssues' cannot be called as issue linking is currently disabled.");
        administration.issueLinking().enable();

        // check that fred only sees the issues he is supposed to
        navigation.login(FRED_USERNAME);
        createSearchAndAssertIssues("issue in linkedIssues('HSP-1')", "HSP-4", "HSP-3", "HSP-2");
        navigation.issueNavigator().createSearch("issue in linkedIssues('MKY-1')");
        assertions.getIssueNavigatorAssertions().assertJqlErrors("Issue 'MKY-1' could not be found in function 'linkedIssues'.");
        navigation.issueNavigator().createSearch("issue in linkedIssues('10001')");
        assertions.getIssueNavigatorAssertions().assertJqlErrors("Issue '10001' could not be found in function 'linkedIssues'.");

        // check that a filter sanitizes the issue key
        navigation.issueNavigator().loadFilter(10000, null);
        assertEquals(IssueNavigatorNavigation.NavigatorMode.SUMMARY, navigation.issueNavigator().getCurrentMode());
        tester.clickLink("editfilter");
        assertions.getIssueNavigatorAssertions().assertJqlErrors("Issue '10001' could not be found in function 'linkedIssues'.");
    }

    public void _testDateFunction(String function, Calendar calendar) throws Exception
    {
        // Make the calendar real before we try manipulating it.
        calendar.getTimeInMillis();
    	// Set MKY-1 dates to 2 days before the given date
    	calendar.add(Calendar.DAY_OF_MONTH, -2);
    	String mky1Date = new Timestamp(calendar.getTimeInMillis()).toString();
    	// Set MKY-2 dates to 2 days after the given date
    	calendar.add(Calendar.DAY_OF_MONTH, 4);
    	String mky2Date = new Timestamp(calendar.getTimeInMillis()).toString();
        //Set up data with current relevant dates.
        Map<String, String> map = new HashMap<String, String>();
        map.put("@@MKY-1_CREATED_DATE@@", mky1Date);
        map.put("@@MKY-1_UPDATED_DATE@@", mky1Date);
        map.put("@@MKY-2_CREATED_DATE@@", mky2Date);
        map.put("@@MKY-2_UPDATED_DATE@@", mky2Date);

        administration.restoreDataWithReplacedTokens("TestJqlNowFunction.xml", map);

        final IssueNavigatorNavigation navigatorNavigation = navigation.issueNavigator();
        final IssueNavigatorAssertions navAssertions = assertions.getIssueNavigatorAssertions();

        //Make sure we find the correct issues in the future.

        createSearchAndAssertIssues("created > " + function, "MKY-2");

        //Make sure we find the issues in the past.
        createSearchAndAssertIssues("created < " + function, "MKY-1");

        //Make sure that the function does not fit.
        navigatorNavigation.createSearch("created > " + function);
        assertTooComplex();
    }

    public void testNowFunction() throws Exception
    {
        final IssueNavigatorAssertions navAssertions = assertions.getIssueNavigatorAssertions();
        final Calendar calendar = Calendar.getInstance();
        _testDateFunction("now()", calendar);

        //Make sure that we are still able to run the search saved as 'created > now('blah')'
        final IssueNavigatorNavigation navigatorNavigation = navigation.issueNavigator();
        navigatorNavigation.loadFilter( 10000, null);
        assertEquals(IssueNavigatorNavigation.NavigatorMode.SUMMARY, navigation.issueNavigator().getCurrentMode());

        tester.clickLink("editfilter");
        navAssertions.assertJqlErrors(createFunctionArgumentError("now", 0, 1));

        //Now make sure that the function with argument returns an error.
        navigatorNavigation.createSearch("created > now ('hrehjre')");
        navAssertions.assertJqlErrors(createFunctionArgumentError("now", 0, 1));
    }

    public void testStartOfDayFunction() throws Exception
    {
    	// Test issue dates are set to be 2 days either side of the passed in calendar
    	Calendar calendar = Calendar.getInstance();
        _testDateFunction("startOfDay()", calendar);
        calendar = Calendar.getInstance();
        _testDateFunction("startOfDay(1)", calendar);
        calendar = Calendar.getInstance();
        _testDateFunction("startOfDay(-1)", calendar);
        calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, 1);
        _testDateFunction("startOfDay(\"+1M\")", calendar);
    }

    public void testStartOfWeekFunction() throws Exception
    {
    	// Test issue dates are set to be 2 days either side of the passed in calendar
    	Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        _testDateFunction("startOfWeek()", calendar);

        calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        _testDateFunction("startOfWeek(1d)", calendar);
        calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        _testDateFunction("startOfWeek(-1d)", calendar);

        calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, 1);
        // Need to get the time to  force the calendar to compute the date properly as
        // DAY_OF_WEEK confuses things.
        calendar.getTimeInMillis();
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        calendar.getTimeInMillis();
        _testDateFunction("startOfWeek(\"+1M\")", calendar);
    }

    public void testStartOfMonthFunction() throws Exception
    {
    	// Test issue dates are set to be 2 days either side of the passed in calendar
    	Calendar calendar = Calendar.getInstance();
    	calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
    	_testDateFunction("startOfMonth()", calendar);

    	calendar = Calendar.getInstance();
    	calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
    	_testDateFunction("startOfMonth(1d)", calendar);
    	calendar = Calendar.getInstance();
    	calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
    	_testDateFunction("startOfMonth(-1d)", calendar);

    	calendar = Calendar.getInstance();
    	calendar.add(Calendar.MONTH, 1);
    	calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
    	_testDateFunction("startOfMonth(\"+1M\")", calendar);
    }

    public void testStartOfYearFunction() throws Exception
    {
    	// Test issue dates are set to be 2 days either side of the passed in calendar
    	Calendar calendar = Calendar.getInstance();
    	calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMinimum(Calendar.DAY_OF_YEAR));
    	_testDateFunction("startOfYear()", calendar);

    	calendar = Calendar.getInstance();
    	calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMinimum(Calendar.DAY_OF_YEAR));
    	_testDateFunction("startOfYear(1d)", calendar);
    	calendar = Calendar.getInstance();
    	calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMinimum(Calendar.DAY_OF_YEAR));
    	_testDateFunction("startOfYear(-1d)", calendar);

    	calendar = Calendar.getInstance();
    	calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMinimum(Calendar.DAY_OF_YEAR));
    	calendar.add(Calendar.MONTH, 1);
    	_testDateFunction("startOfYear(\"+1M\")", calendar);
    }

    public void testEndOfDayFunction() throws Exception
    {
    	// Test issue dates are set to be 2 days either side of the passed in calendar
    	Calendar calendar = Calendar.getInstance();
    	_testDateFunction("endOfDay()", calendar);
    	calendar = Calendar.getInstance();
    	_testDateFunction("endOfDay(1)", calendar);
    	calendar = Calendar.getInstance();
    	_testDateFunction("endOfDay(-1)", calendar);
    	calendar = Calendar.getInstance();
    	calendar.add(Calendar.MONTH, 1);
    	_testDateFunction("endOfDay(\"+1M\")", calendar);
    }

    public void testEndOfWeekFunction() throws Exception
    {
        int lastDayOfWeek;
        Calendar calendar = new GregorianCalendar();
        if (calendar.getFirstDayOfWeek() == Calendar.MONDAY)
        {
            lastDayOfWeek = Calendar.SUNDAY;
        }
        else
        {
            lastDayOfWeek = Calendar.SATURDAY;
        }    	// Test issue dates are set to be 2 days either side of the passed in calendar
    	calendar.setTimeInMillis(System.currentTimeMillis());
    	calendar.set(Calendar.DAY_OF_WEEK, lastDayOfWeek);
    	_testDateFunction("endOfWeek()", calendar);

        calendar.setTimeInMillis(System.currentTimeMillis());
    	calendar.set(Calendar.DAY_OF_WEEK, lastDayOfWeek);
    	_testDateFunction("endOfWeek(1d)", calendar);
        calendar.setTimeInMillis(System.currentTimeMillis());
    	calendar.set(Calendar.DAY_OF_WEEK, lastDayOfWeek);
    	_testDateFunction("endOfWeek(-1d)", calendar);

        calendar.setTimeInMillis(System.currentTimeMillis());
    	calendar.add(Calendar.MONTH, 1);
        // Need to get the time to  force the calendar to compute the date properly as
        // DAY_OF_WEEK confuses things.
        calendar.getTimeInMillis();
    	calendar.set(Calendar.DAY_OF_WEEK, lastDayOfWeek);
    	_testDateFunction("endOfWeek(\"+1M\")", calendar);
    }

    public void testEndOfMonthFunction() throws Exception
    {
    	// Test issue dates are set to be 2 days either side of the passed in calendar
    	Calendar calendar = Calendar.getInstance();
    	calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
    	_testDateFunction("endOfMonth()", calendar);

    	calendar = Calendar.getInstance();
    	calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
    	_testDateFunction("endOfMonth(1d)", calendar);
    	calendar = Calendar.getInstance();
    	calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
    	_testDateFunction("endOfMonth(-1d)", calendar);

    	calendar = Calendar.getInstance();
    	calendar.add(Calendar.MONTH, 1);
    	calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
    	_testDateFunction("endOfMonth(\"+1M\")", calendar);
    }

    public void testEndOfYearFunction() throws Exception
    {
    	// Test issue dates are set to be 2 days either side of the passed in calendar
    	Calendar calendar = Calendar.getInstance();
    	calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR));
    	_testDateFunction("endOfYear()", calendar);

    	calendar = Calendar.getInstance();
    	calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR));
    	_testDateFunction("endOfYear(1d)", calendar);
    	calendar = Calendar.getInstance();
    	calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR));
    	_testDateFunction("endOfYear(-1d)", calendar);

    	calendar = Calendar.getInstance();
    	calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR));
    	calendar.add(Calendar.MONTH, 1);
    	_testDateFunction("endOfYear(\"+1M\")", calendar);
    }

    public void testLastLogin() throws Exception
    {
        administration.restoreData("TestSystemJqlFunctions.xml");
        final IssueNavigatorNavigation navigatorNavigation = navigation.issueNavigator();
        final IssueNavigatorAssertions navAssertions = assertions.getIssueNavigatorAssertions();

        // on our very first login our "previous login" is empty...so the entire clause should be false
        createSearchAndAssertIssues("created > lastLogin()");
        createSearchAndAssertIssues("created < lastLogin()");

        // login again to get a "real" record for some history
        navigation.logout();
        navigation.login(ADMIN_USERNAME);
        createSearchAndAssertIssues("created >= lastLogin()");
        // create an issue after our login
        final String issue = navigation.issue().createIssue("homosapien", "Bug", "created after login");
        createSearchAndAssertIssues("created < lastLogin()", "HSP-4", "HSP-3", "HSP-2", "HSP-1");
        createSearchAndAssertIssues("created >= lastLogin()", issue);

        //Now make sure that the function with argument returns an error.
        navigatorNavigation.createSearch("created > lastLogin('hrehjre')");
        navAssertions.assertJqlErrors(createFunctionArgumentError("lastLogin", 0, 1));

        //Make sure that the function does not fit.
        navigatorNavigation.createSearch("created > lastLogin()");
        assertTooComplex();

        // make sure that anonymous users don't get anything
        navigation.logout();
        navigation.gotoDashboard();
        createSearchAndAssertIssues("created >= lastLogin()");
        createSearchAndAssertIssues("created <= lastLogin()");
    }

    public void testCurrentLogin() throws Exception
    {
        administration.restoreData("TestSystemJqlFunctions.xml");
        final IssueNavigatorNavigation navigatorNavigation = navigation.issueNavigator();
        final IssueNavigatorAssertions navAssertions = assertions.getIssueNavigatorAssertions();

        createSearchAndAssertIssues("created < currentLogin()", "HSP-4", "HSP-3", "HSP-2", "HSP-1");
        createSearchAndAssertIssues("created >= currentLogin()");

        // create an issue after our login
        final String issue = navigation.issue().createIssue("homosapien", "Bug", "created after login");
        createSearchAndAssertIssues("created < currentLogin()", "HSP-4", "HSP-3", "HSP-2", "HSP-1");
        createSearchAndAssertIssues("created >= currentLogin()", issue);

        navigation.logout();
        navigation.login(ADMIN_USERNAME);
        createSearchAndAssertIssues("created > currentLogin()");
        createSearchAndAssertIssues("created <= currentLogin()", issue, "HSP-4", "HSP-3", "HSP-2", "HSP-1");

        //Now make sure that the function with argument returns an error.
        navigatorNavigation.createSearch("created > currentLogin('hrehjre')");
        navAssertions.assertJqlErrors(createFunctionArgumentError("currentLogin", 0, 1));

        //Make sure that the function does not fit.
        navigatorNavigation.createSearch("created > currentLogin()");
        assertTooComplex();

        // make sure that anonymous users don't get anything
        navigation.logout();
        navigation.gotoDashboard();
        createSearchAndAssertIssues("created >= currentLogin()");
        createSearchAndAssertIssues("created <= currentLogin()");
    }

    //Test to make sure that the current user function.
    public void testCurrentUser() throws Exception
    {
        administration.restoreData("TestSystemJqlFunctions.xml");

        final IssueNavigatorNavigation navigatorNavigation = navigation.issueNavigator();
        final IssueNavigatorAssertions navAssertions = assertions.getIssueNavigatorAssertions();

        //Check some searches against the admin user.
        createSearchAndAssertIssues("assignee = currentUser()", "HSP-4", "HSP-2");
        createSearchAndAssertIssues("reporter = currentUser()", "HSP-2", "HSP-1");

        //Make sure an invalid function argument is ignored. The saved filter is "assignee = currentUser('ignoreMe')".
        navigatorNavigation.loadFilter(10001, null);
        assertEquals(IssueNavigatorNavigation.NavigatorMode.SUMMARY, navigation.issueNavigator().getCurrentMode());
        tester.clickLink("editfilter");
        navAssertions.assertJqlErrors(createFunctionArgumentError("currentUser", 0, 1));

        //Check some searches against the Fred user.
        navigation.login(FRED_USERNAME, FRED_PASSWORD);

        createSearchAndAssertIssues("assignee = currentUser()", "HSP-3", "HSP-1");
        createSearchAndAssertIssues("reporter = CURRENTUSER()", "HSP-4", "HSP-3");

        final long reporterFilterId = navigatorNavigation.saveCurrentAsNewFilter(new SharedEntityInfo("AllFilter", null, false, TestSharingPermissionUtils.createPublicPermissions()));

        //Now make sure that the function with argument returns an error when used with arguments.
        navigatorNavigation.createSearch("assignee = currentuser ('hrehjre')");
        navAssertions.assertJqlErrors(createFunctionArgumentError("currentuser", 0, 1));

        //Make sure that the 'currentUser' is used for custom fields.
        NavigatorSearch currentSearch = new NavigatorSearch(new UserGroupPicker("customfield_10000").setCurrentUser());
        navigatorNavigation.createSearch(currentSearch);
        navigatorNavigation.gotoEditMode(IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        navAssertions.assertAdvancedSearch(tester, "cf[10000] = currentUser()");
        navigatorNavigation.gotoEditMode(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);
        navAssertions.assertSimpleSearch(currentSearch, tester);

        //Make sure the user function works with the system fields.
        currentSearch = new NavigatorSearch(new AssigneeCondition().setCurrentUser(), new ReporterCondition().setCurrentUser());
        navigatorNavigation.createSearch(currentSearch);
        navigatorNavigation.gotoEditMode(IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        navAssertions.assertAdvancedSearch(tester, "assignee = currentUser()", "reporter = currentUser()");
        navigatorNavigation.gotoEditMode(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);
        navAssertions.assertSimpleSearch(currentSearch, tester);

        //Make sure that it does not fit if the searcher is not enabled.
        navigatorNavigation.createSearch("cf[10001]     =        currentUser()");
        assertTooComplex();

        navigation.logout();
        navigation.gotoDashboard();
        navigatorNavigation.createSearch("assignee = currentuser()");
        assertions.getIssueNavigatorAssertions().assertExactIssuesInResults();
        assertions.getIssueNavigatorAssertions().assertNoJqlErrors();

        //Make sure you can run a saved filter 'reporter = CURRENTUSER()' when not logged in. Should not get any results
        //though.
        navigatorNavigation.loadFilter(reporterFilterId, null);
        assertions.getIssueNavigatorAssertions().assertExactIssuesInResults();
        assertions.getIssueNavigatorAssertions().assertNoJqlErrors();
    }

    public void testMembersOf() throws Exception
    {
        administration.restoreData("TestSystemJqlFunctionsMembersOf.xml");
        final IssueNavigatorNavigation navigatorNavigation = navigation.issueNavigator();
        final IssueNavigatorAssertions navAssertions = assertions.getIssueNavigatorAssertions();

        //Check some searches against the admin user.
        //note: function is case-insensitive
        createSearchAndAssertIssues("assignee in membersOf('jira-users')", "HSP-5", "HSP-4", "HSP-3", "HSP-2", "HSP-1");
        createSearchAndAssertIssues("assignee in membersOf('JIRA-users')", "HSP-5", "HSP-4", "HSP-3", "HSP-2", "HSP-1");
        createSearchAndAssertIssues("reporter in membersOf('jira-administrators')", "HSP-2", "HSP-1");
        createSearchAndAssertIssues("reporter in membersOf('jira-ADMINistrators')", "HSP-2", "HSP-1");
        createSearchAndAssertIssues("reporter in membersOf('empty-group')");

        // JIRA used to be case-sensitive for Group and User names, and no longer is, so these tests don't really anything different to above
        createSearchAndAssertIssues("reporter in membersOf('jira-developers')", "HSP-4", "HSP-3", "HSP-2", "HSP-1");
        createSearchAndAssertIssues("reporter in membersOf('JIRA-DEVELOPERS')", "HSP-4", "HSP-3", "HSP-2", "HSP-1");
        createSearchAndAssertIssues("reporter in membersOf('JIRA-DEVELOPERS-1')", "HSP-5", "HSP-4", "HSP-3", "HSP-2", "HSP-1");

        //Lets check what happens with the query "assignee in membersOf()". It should return nothing.
        navigatorNavigation.loadFilter(10003, null);
        assertEquals(IssueNavigatorNavigation.NavigatorMode.SUMMARY, navigation.issueNavigator().getCurrentMode());
        tester.clickLink("editfilter");
        navAssertions.assertJqlErrors(createFunctionArgumentError("membersOf", 1, 0));

        //The query "reporter in membersOf('jira-administrators', 'jira-users') should only return jira-administrators. 
        navigatorNavigation.loadFilter(10004, null);
        assertEquals(IssueNavigatorNavigation.NavigatorMode.SUMMARY, navigation.issueNavigator().getCurrentMode());
        tester.clickLink("editfilter");
        navAssertions.assertJqlErrors(createFunctionArgumentError("membersOf", 1, 2));

        //Now make sure that the function with incorrect number of arguments does not work.
        navigatorNavigation.createSearch("assignee in membersOf ()");
        navAssertions.assertJqlErrors(createFunctionArgumentError("membersOf", 1, 0));

        navigatorNavigation.createSearch("assignee in membersOf (8689, 38383)");
        navAssertions.assertJqlErrors(createFunctionArgumentError("membersOf", 1, 2));

        navigatorNavigation.createSearch("assignee in membersof ('notAGroupDoesNotExist')");
        navAssertions.assertJqlErrors("Function 'membersof' can not generate a list of usernames for group 'notAGroupDoesNotExist'; the group does not exist.");

        //Make sure that the 'membersOf' is used for custom fields.
        NavigatorSearch currentSearch = new NavigatorSearch(new UserGroupPicker("customfield_10000").setGroup("jira-users"));
        navigatorNavigation.createSearch(currentSearch);
        navigatorNavigation.gotoEditMode(IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        navAssertions.assertAdvancedSearch(tester, "cf[10000] in membersOf(jira-users)");
        navigatorNavigation.gotoEditMode(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);
        navAssertions.assertSimpleSearch(currentSearch, tester);

        //Make sure the 'memberOf' works with the system fields.
        currentSearch = new NavigatorSearch(new AssigneeCondition().setGroup("jira-users"), new ReporterCondition().setGroup("empty-group"));
        navigatorNavigation.createSearch(currentSearch);
        navigatorNavigation.gotoEditMode(IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        navAssertions.assertAdvancedSearch(tester, "assignee in membersOf(jira-users)", "reporter in membersOf(empty-group)");
        navigatorNavigation.gotoEditMode(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);
        navAssertions.assertSimpleSearch(currentSearch, tester);

        //Make sure that it does not fit if the searcher is not enabled.
        navigatorNavigation.createSearch("cf[10001]     in        membersOf(\"jira-users\")");
        assertTooComplex();
    }

    public void testProjectsLeadByUserFunction()
    {
        administration.restoreData("TestProjectsLeadByUserFunction.xml");
        navigation.login(FRED_USERNAME);
        createSearchAndAssertIssues("project IN projectsLeadByUser()", "FKY-1");
        createSearchAndAssertIssues("project IN projectsLeadByUser(fred)", "FKY-1");
        createSearchAndAssertIssues("project IN projectsLeadByUser(bill)", "BKY-2", "BKY-1");
        createSearchAndAssertIssues("project IN projectsLeadByUser(admin)", "MKY-1", "HSP-2", "HSP-1");
        createSearchAndAssertIssues("project IN projectsLeadByUser(mary)");

        assertTooComplex();

        navigation.issueNavigator().createSearch("project IN projectsLeadByUser(user1, user2)");
        assertions.getIssueNavigatorAssertions().assertJqlErrors(createFunctionArgumentError("projectsLeadByUser", 0, 1, 2));

        navigation.logout();
        navigation.gotoDashboard();
        navigation.issueNavigator().createSearch("project IN projectsLeadByUser()");
        assertions.getIssueNavigatorAssertions().assertJqlErrors("Function 'projectsLeadByUser' cannot be called as anonymous user.");
        navigation.issueNavigator().createSearch("project IN projectsLeadByUser(admin)");
        assertIssues("MKY-1", "HSP-2", "HSP-1");

        navigation.issueNavigator().loadFilter(10000, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        assertions.getIssueNavigatorAssertions().assertJqlErrors("Function 'projectsLeadByUser' cannot be called as anonymous user.");
        navigation.issueNavigator().loadFilter(10001, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        assertIssues("MKY-1", "HSP-2", "HSP-1");

    }

    public void testProjectsWhereUserHasPermissionFunction()
    {
        administration.restoreData("TestProjectsWhereUserHasPermissionFunction.xml");
        navigation.login(FRED_USERNAME);
        createSearchAndAssertIssues("project IN projectsWhereUserHasPermission(\"Create Issues\")", "MKY-2", "MKY-1", "HSP-5", "HSP-4", "HSP-2", "HSP-1");
        createSearchAndAssertIssues("project IN projectsWhereUserHasPermission(\"Resolve Issues\")", "HSP-5", "HSP-4", "HSP-2", "HSP-1");
        // Lower case should also work
        createSearchAndAssertIssues("project IN projectsWhereUserHasPermission(\"create issues\")", "MKY-2", "MKY-1", "HSP-5", "HSP-4", "HSP-2", "HSP-1");
        createSearchAndAssertIssues("project IN projectsWhereUserHasPermission(\"resolve issues\")", "HSP-5", "HSP-4", "HSP-2", "HSP-1");

        assertTooComplex();

        navigation.issueNavigator().createSearch("project IN projectsWhereUserHasPermission()");
        assertions.getIssueNavigatorAssertions().assertJqlErrors(createFunctionArgumentError("projectsWhereUserHasPermission", 1, 0));

        navigation.issueNavigator().createSearch("project IN projectsWhereUserHasPermission(\"Create Issues\", bill)");
        assertions.getIssueNavigatorAssertions().assertJqlErrors(createFunctionArgumentError("projectsWhereUserHasPermission", 1, 2));

        navigation.issueNavigator().loadFilter(10000, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        assertIssues("HSP-5", "HSP-4", "HSP-2", "HSP-1");

        // Anonymous test
        navigation.logout();
        navigation.gotoDashboard();
        navigation.issueNavigator().createSearch("project IN projectsWhereUserHasPermission(\"Create Issues\")");
        assertions.getIssueNavigatorAssertions().assertJqlErrors("Function 'projectsWhereUserHasPermission' cannot be called as anonymous user.");

        navigation.issueNavigator().loadFilter(10000, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        assertions.getIssueNavigatorAssertions().assertJqlErrors("Function 'projectsWhereUserHasPermission' cannot be called as anonymous user.");
    }

    public void testProjectsWhereUserHasRoleFunction()
    {
        administration.restoreData("TestProjectsWhereUserHasRoleFunction.xml");
        navigation.login(FRED_USERNAME);
        createSearchAndAssertIssues("project IN projectsWhereUserHasRole(\"Developers\")", "HSP-5", "HSP-4", "HSP-2", "HSP-1");
        createSearchAndAssertIssues("project IN projectsWhereUserHasRole(\"Users\")", "MKY-2", "MKY-1");

        assertTooComplex();

        navigation.issueNavigator().createSearch("project IN projectsWhereUserHasRole()");
        assertions.getIssueNavigatorAssertions().assertJqlErrors(createFunctionArgumentError("projectsWhereUserHasRole", 1, 0));

        navigation.issueNavigator().createSearch("project IN projectsWhereUserHasRole(\"Developers\", admin)");
        assertions.getIssueNavigatorAssertions().assertJqlErrors(createFunctionArgumentError("projectsWhereUserHasRole", 1, 2));

        navigation.issueNavigator().loadFilter(10000, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        assertIssues("HSP-5", "HSP-4", "HSP-2", "HSP-1");

        // Anonymous test
        navigation.logout();
        navigation.gotoDashboard();
        navigation.issueNavigator().createSearch("project IN projectsWhereUserHasRole(\"Developers\")");
        assertions.getIssueNavigatorAssertions().assertJqlErrors("Function 'projectsWhereUserHasRole' cannot be called as anonymous user.");

        navigation.issueNavigator().loadFilter(10000, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        assertions.getIssueNavigatorAssertions().assertJqlErrors("Function 'projectsWhereUserHasRole' cannot be called as anonymous user.");
    }

    public void testComponentsLeadByUserFunction()
    {
        administration.restoreData("TestComponentsLeadByUserFunction.xml");
        navigation.login(FRED_USERNAME);
        createSearchAndAssertIssues("component IN componentsLeadByUser()", "HSP-5");
        createSearchAndAssertIssues("component IN componentsLeadByUser(fred)", "HSP-5");
        createSearchAndAssertIssues("component IN componentsLeadByUser(bill)", "MKY-1", "HSP-4", "HSP-2", "HSP-1");
        createSearchAndAssertIssues("component IN componentsLeadByUser(admin)");
        createSearchAndAssertIssues("component IN componentsLeadByUser(mary)");

        assertTooComplex();

        navigation.issueNavigator().createSearch("component IN componentsLeadByUser(user1, user2)");
        assertions.getIssueNavigatorAssertions().assertJqlErrors(createFunctionArgumentError("componentsLeadByUser", 0, 1, 2));

        navigation.logout();
        navigation.gotoDashboard();
        navigation.issueNavigator().createSearch("component IN componentsLeadByUser()");
        assertions.getIssueNavigatorAssertions().assertJqlErrors("Function 'componentsLeadByUser' cannot be called as anonymous user.");
        navigation.issueNavigator().createSearch("component IN componentsLeadByUser(fred)");
        assertIssues("HSP-5");

        navigation.issueNavigator().loadFilter(10000, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        assertions.getIssueNavigatorAssertions().assertJqlErrors("Function 'componentsLeadByUser' cannot be called as anonymous user.");
        navigation.issueNavigator().loadFilter(10001, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        assertIssues("HSP-5");

    }

    //Test to ensure that functions do not display values when displaying errors.
    public void testJqlFunctionMessages() throws Exception
    {
        administration.restoreData("TestJqlFunctionErrors.xml");

        //Enumerated functions return the same thing.
        assertJqlFunctionErrorEnumeratedValues("affectedVersion");
        assertJqlFunctionErrorEnumeratedValues("category");
        assertJqlFunctionErrorEnumeratedValues("component");
        assertJqlFunctionErrorEnumeratedValues("fixVersion");
        assertJqlFunctionErrorEnumeratedValues("level");
        assertJqlFunctionErrorEnumeratedValues("priority");
        assertJqlFunctionErrorEnumeratedValues("project");
        assertJqlFunctionErrorEnumeratedValues("resolution");
        assertJqlFunctionErrorEnumeratedValues("savedFilter");
        assertJqlFunctionErrorEnumeratedValues("status");
        assertJqlFunctionErrorEnumeratedValues("type");
        assertJqlFunctionErrorEnumeratedValues("PP");
        assertJqlFunctionErrorEnumeratedValues("SVP");
        assertJqlFunctionErrorEnumeratedValues("VP");

        final IssueNavigatorNavigation navigatorNavigation = navigation.issueNavigator();
        final IssueNavigatorAssertions assertions = this.assertions.getIssueNavigatorAssertions();

        //Check the vote clause.
        navigatorNavigation.createSearch("votes = currentUser()");
        assertions.assertJqlErrors("A value provided by the function 'currentUser' is invalid for the field 'votes'. Votes must be a positive whole number.");

        //Time tracking.
        assertJqlFunctionErrorTimeTracking("originalEstimate");
        assertJqlFunctionErrorTimeTracking("remainingEstimate");
        assertJqlFunctionErrorTimeTracking("timeSpent");

        //Check out the workRatio searcher.
        navigatorNavigation.createSearch("workRatio < currentUser()");
        assertions.assertJqlErrors("A value provided by the function 'currentUser' for the field 'workRatio' is not an integer.");

        //Issue Fields
        assertJqlFunctionErrorIssueKey("issue");
        assertJqlFunctionErrorIssueKey("parent");

        //UserFields.
        assertJqlFunctionWarningUser("assignee");
        assertJqlFunctionWarningUser("reporter");
        assertJqlFunctionWarningUser("MUP");
        assertJqlFunctionWarningUser("UP");

        //Date based fields.
        assertJqlFunctionErrorDateValues("updated");
        assertJqlFunctionErrorDateValues("created");
        assertJqlFunctionErrorDateValues("due");
        assertJqlFunctionErrorDateValues("resolved");
        assertJqlFunctionErrorDateValues("DP");
        assertJqlFunctionErrorDateValues("DT");

        //Text Fields
        assertJqlFunctionErrorTextValues("comment");
        assertJqlFunctionErrorTextValues("description");
        assertJqlFunctionErrorTextValues("environment");
        assertJqlFunctionErrorTextValues("summary");
        assertJqlFunctionErrorTextValues("FTF");
        assertJqlFunctionErrorTextValues("ROTF");
        assertJqlFunctionErrorTextValues("TF");

        //Test for the number fields.
        assertJqlFunctionErrorNumber("II");
        assertJqlFunctionErrorNumber("NF");

        //Test for the group pickers.
        assertJqlFunctionErrorGroup("GP");
        assertJqlFunctionErrorGroup("MGP");

        //Lets work with the option based custom fields.
        assertJqlFunctionErrorOption("CSF");
        assertJqlFunctionErrorOption("MC");
        assertJqlFunctionErrorOption("MS");
        assertJqlFunctionErrorOption("RB");
        assertJqlFunctionErrorOption("SL");
    }

    //Test for functions that don't exist. Also checks what happens when the plugin is disabled.
    public void testBadFunctions() throws Exception
    {
        administration.restoreData("TestJqlFunctionDisabled.xml");

        try
        {
            // Disable the echo function
            administration.plugins().disablePluginModule("jira.jql.function","jira.jql.function:echo-jql-function");

            //Run the query project = echo('HSP') where function echo is disabled.
            final IssueNavigatorNavigation navigation = this.navigation.issueNavigator();
            navigation.loadFilter(10000, null);
            assertEquals(IssueNavigatorNavigation.NavigatorMode.SUMMARY, navigation.getCurrentMode());
            tester.clickLink("editfilter");
            assertions.getIssueNavigatorAssertions().assertJqlErrors("Unable to find JQL function 'echo(HSP)'.");

            //Run the query project = dontExist(HSP).
            navigation.loadFilter(10001, null);
            assertEquals(IssueNavigatorNavigation.NavigatorMode.SUMMARY, navigation.getCurrentMode());
            tester.clickLink("editfilter");
            assertions.getIssueNavigatorAssertions().assertJqlErrors("Unable to find JQL function 'dontExist(HSP)'.");
        }
        finally
        {
            // Always re-enable the function
            administration.plugins().enablePluginModule("jira.jql.function","jira.jql.function:echo-jql-function");
        }
    }

    private void assertJqlFunctionErrorOption(final String fieldName)
    {
        navigation.issueNavigator().createSearch(String.format("%s = EcHo('notAnOption')", fieldName));
        assertions.getIssueNavigatorAssertions().assertJqlErrors(String.format("An option provided by the function 'EcHo' for the field '%s' does not exist.", fieldName));
    }

    private void assertJqlFunctionErrorTextValues(final String fieldName)
    {
        navigation.issueNavigator().createSearch(String.format("%s ~ ECHO('?illegal')", fieldName));
        assertions.getIssueNavigatorAssertions().assertJqlErrors(String.format("The text query given by the function 'ECHO' is not valid for the field '%s' as it starts with '?'.", fieldName));

        navigation.issueNavigator().createSearch(String.format("%s ~ ECHO('ru [20002')", fieldName));
        assertions.getIssueNavigatorAssertions().assertJqlErrors(String.format("The field '%s' is unable to parse the text given to it by the function 'ECHO'.", fieldName));
    }

    private void assertJqlFunctionErrorEnumeratedValues(String fieldName)
    {
        navigation.issueNavigator().createSearch(String.format("%s = CuRReNTuseR()", fieldName));
        assertions.getIssueNavigatorAssertions().assertJqlErrors(String.format("A value provided by the function 'CuRReNTuseR' is invalid for the field '%s'.", fieldName));
    }

    private void assertJqlFunctionErrorDateValues(String fieldName)
    {
        navigation.issueNavigator().createSearch(String.format("%s = currentUSER()", fieldName));
        assertions.getIssueNavigatorAssertions().assertJqlErrors(String.format("A date for the field '%s' provided by the function 'currentUSER' is not valid.", fieldName));
    }

    private void assertJqlFunctionErrorTimeTracking(String fieldName)
    {
        navigation.issueNavigator().createSearch(String.format("%s = cuRRENTUSER()", fieldName));
        assertions.getIssueNavigatorAssertions().assertJqlErrors(String.format("A value provided by the function 'cuRRENTUSER' for the field '%s' is not a positive duration.", fieldName));
    }

    private void assertJqlFunctionErrorIssueKey(String fieldName)
    {
        navigation.issueNavigator().createSearch(String.format("%s = currentuser()", fieldName));
        assertions.getIssueNavigatorAssertions().assertJqlErrors(String.format("A value provided by the function 'currentuser' for the field '%s' is not a valid issue key.", fieldName));
    }

    private void assertJqlFunctionErrorUser(String fieldName)
    {
        navigation.issueNavigator().createSearch(String.format("%s = now()", fieldName));
        assertions.getIssueNavigatorAssertions().assertJqlErrors(String.format("A value provided by the function 'now' is invalid for the field '%s'.", fieldName));
    }

    private void assertJqlFunctionWarningUser(String fieldName)
    {
        navigation.issueNavigator().createSearch(String.format("%s = now()", fieldName));
        assertions.getIssueNavigatorAssertions().assertJqlWarnings(String.format("A value provided by the function 'now' is invalid for the field '%s'.", fieldName));
    }

    private void assertJqlFunctionErrorGroup(String fieldName)
    {
        navigation.issueNavigator().createSearch(String.format("%s = now()", fieldName));
        assertions.getIssueNavigatorAssertions().assertJqlErrors(String.format("A group provided by the function 'now' for the field '%s' does not exist.", fieldName));
    }

    private void assertJqlFunctionErrorNumber(String fieldName)
    {
        navigation.issueNavigator().createSearch(String.format("%s = echo('bad')", fieldName));
        assertions.getIssueNavigatorAssertions().assertJqlErrors(String.format("A value provided by the function 'echo' for the field '%s' is not a valid number.", fieldName));

    }

    private void createSearchAndAssertIssues(String jqlQuery, String...keys)
    {
        //Make sure we find the issues in the past.
        navigation.issueNavigator().createSearch(jqlQuery);
        assertIssues(keys);
    }

    private void assertTooComplex()
    {
        assertions.getIssueNavigatorAssertions().assertJqlTooComplex();
    }

    private String createFunctionArgumentError(final Object funcName, final int expectedArgs, final int actualArgs)
    {
        return String.format("Function '%s' expected '%d' arguments but received '%d'.", funcName, expectedArgs, actualArgs);
    }

    private String createFunctionArgumentError(final Object funcName, final int minArgs, final int maxArgs, final int actualArgs)
    {
        return String.format("Function '%s' expected between '%d' and '%d' arguments but received '%d'.", funcName, minArgs, maxArgs, actualArgs);
    }

    private static IssueNavigatorAssertions.FilterFormParam createParam(final String name, final String... values)
    {
        return AbstractJqlFuncTest.createFilterFormParam(name, values);
    }
}
