package com.atlassian.jira.webtests.ztests.navigator.jql;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.Splitable;
import com.atlassian.jira.functest.framework.navigator.ColumnsCondition;
import com.atlassian.jira.functest.framework.navigator.ContainsIssueKeysCondition;
import com.atlassian.jira.functest.framework.navigator.NumberOfIssuesCondition;
import com.atlassian.jira.functest.framework.navigator.SearchResultsCondition;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Some more tests for column context.
 *
 * @since v4.0
 */
@Splitable
@WebTest ({ Category.FUNC_TEST, Category.JQL })
public class TestContextColumns extends FuncTestCase
{
    private static boolean needsrestore = true;

    private static enum Project
    {
        ONE, TWO, THREE, FOUR
    }

    private static enum IssueType
    {
        BUG, IMPROVEMENT, FEATURE, TASK, SUBTASK
    }

    @Override
    protected void setUpTest()
    {
        if (needsrestore)
        {
            // do not use restoreData - it causes subtle cache issues
            administration.restoreDataSlowOldWay("TestJqlContextFields.xml");
            needsrestore = false;
        }
    }

    //
    // Test to check that the columns implied by the project clause are correct.
    //
    public void testProjectField() throws Exception
    {
        assertJqlColumns("project = one", new Context().addProject(Project.ONE), getIssuesForProjects(Project.ONE));
        assertJqlColumns("project = two", new Context().addProject(Project.TWO), getIssuesForProjects(Project.TWO));
        assertJqlColumns("project = three", new Context().addProject(Project.THREE), getIssuesForProjects(Project.THREE));

        assertJqlColumns("project != two", new Context().addProjects(getProjectsAndRemove(Project.TWO)), getIssuesAndRemoveProject(Project.TWO));
        assertJqlColumns("project != four", new Context().addProjects(getProjectsAndRemove(Project.FOUR)), getIssuesAndRemoveProject(Project.FOUR));
        assertJqlColumns("project != one", new Context().addProjects(getProjectsAndRemove(Project.ONE)), getIssuesAndRemoveProject(Project.ONE));

        assertJqlColumns("project in (one, three)", new Context().addProjects(Project.ONE, Project.THREE), getIssuesForProjects(Project.ONE, Project.THREE));
        assertJqlColumns("project = one or project = three", new Context().addProjects(Project.ONE, Project.THREE), getIssuesForProjects(Project.ONE, Project.THREE));

        assertJqlColumns("project not in (one, two)", new Context().addProjects(getProjectsAndRemove(Project.ONE, Project.TWO)), getIssuesAndRemoveProject(Project.ONE, Project.TWO));
        assertJqlColumns("project != one and project != two", new Context().addProjects(getProjectsAndRemove(Project.ONE, Project.TWO)), getIssuesAndRemoveProject(Project.ONE, Project.TWO));
        assertJqlColumns("project not in (one, two, three)", new Context().addProjects(getProjectsAndRemove(Project.ONE, Project.TWO, Project.THREE)), getIssuesAndRemoveProject(Project.ONE, Project.THREE, Project.TWO));
        assertJqlColumns("project != one and project != two and project != three", new Context().addProjects(getProjectsAndRemove(Project.ONE, Project.TWO, Project.THREE)), getIssuesAndRemoveProject(Project.ONE, Project.THREE, Project.TWO));

        assertJqlColumns("project in (empty, two)", new Context().addProject(Project.TWO), getIssuesForProjects(Project.TWO));
        assertJqlColumns("project = empty or project = two", new Context().addProject(Project.TWO), getIssuesForProjects(Project.TWO));

        assertJqlColumns("project is not empty", Context.GLOBAL, Issue.ALL_ISSUES);
        assertJqlColumns("project != empty", Context.GLOBAL, Issue.ALL_ISSUES);
        assertJqlColumns("project != null", Context.GLOBAL, Issue.ALL_ISSUES);
        assertJqlColumns("project not in (empty)", Context.GLOBAL, Issue.ALL_ISSUES);
        assertJqlColumns("project not in (empty, three)", new Context().addProjects(getProjectsAndRemove(Project.THREE)), getIssuesAndRemoveProject(Project.THREE));
        assertJqlColumns("project != empty and project != three", new Context().addProjects(getProjectsAndRemove(Project.THREE)), getIssuesAndRemoveProject(Project.THREE));

        //Something a little more complex.
        assertJqlColumns("(project != one or project != two)", new Context().addProjects(Project.values()), Issue.ALL_ISSUES);
        assertJqlColumns("project != one or project = one", new Context().addProjects(Project.values()), Issue.ALL_ISSUES);
        assertJqlColumns("project in (one, two) and project = one", new Context().addProject(Project.ONE), getIssuesForProjects(Project.ONE));
        assertJqlColumns("project in (one, two) and project not in (three, one)", new Context().addProject(Project.TWO), getIssuesForProjects(Project.TWO));

        //Lets make sure that invalid filter is correctly calculated.
        assertFilterColumns(10020, new Context().addProject(Project.ONE), Issue.ONE1);

        //Check for someone who cannot see all projects. Fred can't see project three.
        navigation.login(FRED_USERNAME);
        assertJqlColumns("project != two", new Context().addProjects(getProjectsAndRemove(Project.THREE, Project.TWO)), Issue.ONE1, Issue.FOUR3);
    }

    //
    // Test to check that the columns implied by the issue type clause are correct.
    //
    public void testIssueType() throws Exception
    {
        assertJqlColumns("type = bug", new Context().addType(IssueType.BUG), Issue.THREE2, Issue.ONE1, Issue.FOUR3);
        assertJqlColumns("type = task", new Context().addType(IssueType.TASK), Issue.TWO1);
        assertJqlColumns("type = new\\ feature", new Context().addType(IssueType.FEATURE), Issue.THREE1);

        assertJqlColumns("type != bug", new Context().addTypes(getIssueTypesAndRemove(IssueType.BUG)), getIssuesAndRemoveIssues(Issue.THREE2, Issue.ONE1, Issue.FOUR3));
        assertJqlColumns("type != task", new Context().addTypes(getIssueTypesAndRemove(IssueType.TASK)), getIssuesAndRemoveIssues(Issue.TWO1));
        assertJqlColumns("type != new\\ feature", new Context().addTypes(getIssueTypesAndRemove(IssueType.FEATURE)), getIssuesAndRemoveIssues(Issue.THREE1));

        assertJqlColumns("type in (bug, task)", new Context().addTypes(IssueType.BUG, IssueType.TASK), Issue.TWO1, Issue.THREE2, Issue.ONE1, Issue.FOUR3);
        assertJqlColumns("type = bug or type = task", new Context().addTypes(IssueType.BUG, IssueType.TASK), Issue.TWO1, Issue.THREE2, Issue.ONE1, Issue.FOUR3);
        assertJqlColumns("type in ('new feature', task)", new Context().addTypes(IssueType.FEATURE, IssueType.TASK), Issue.TWO1, Issue.THREE1);
        assertJqlColumns("type = 'new feature' or type = task", new Context().addTypes(IssueType.FEATURE, IssueType.TASK), Issue.TWO1, Issue.THREE1);

        assertJqlColumns("type not in (bug, task)", new Context().addTypes(getIssueTypesAndRemove(IssueType.BUG, IssueType.TASK)), Issue.TWO2, Issue.THREE1, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns("not (type = bug or type = task)", new Context().addTypes(getIssueTypesAndRemove(IssueType.BUG, IssueType.TASK)), Issue.TWO2, Issue.THREE1, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns("type not in ('new feature', task)", new Context().addTypes(getIssueTypesAndRemove(IssueType.FEATURE, IssueType.TASK)), Issue.TWO2, Issue.THREE2, Issue.ONE1, Issue.FOUR3, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns("type != 'new feature' and not type = task", new Context().addTypes(getIssueTypesAndRemove(IssueType.FEATURE, IssueType.TASK)), Issue.TWO2, Issue.THREE2, Issue.ONE1, Issue.FOUR3, Issue.FOUR2, Issue.FOUR1);

        assertJqlColumns("type in (empty, bug)", new Context().addType(IssueType.BUG), Issue.THREE2, Issue.ONE1, Issue.FOUR3);
        assertJqlColumns("type = empty or type = bug", new Context().addType(IssueType.BUG), Issue.THREE2, Issue.ONE1, Issue.FOUR3);

        assertJqlColumns("type is not empty", Context.GLOBAL, Issue.ALL_ISSUES);
        assertJqlColumns("type != empty", Context.GLOBAL, Issue.ALL_ISSUES);
        assertJqlColumns("type not in (empty)", Context.GLOBAL, Issue.ALL_ISSUES);
        assertJqlColumns("type not in (empty, bug)", new Context().addTypes(getIssueTypesAndRemove(IssueType.BUG)), getIssuesAndRemoveIssues(Issue.THREE2, Issue.ONE1, Issue.FOUR3));
        assertJqlColumns("type is not empty and type != bug", new Context().addTypes(getIssueTypesAndRemove(IssueType.BUG)), getIssuesAndRemoveIssues(Issue.THREE2, Issue.ONE1, Issue.FOUR3));

        //Lets make sure that invalid filter is correctly calculated.
        assertFilterColumns(10030, new Context().addTypes(IssueType.BUG, IssueType.TASK), Issue.TWO1, Issue.THREE2, Issue.ONE1, Issue.FOUR3);

        //Complex examples.
        assertJqlColumns("type in (bug, task) and type = task", new Context().addTypes(IssueType.TASK), Issue.TWO1);
        assertJqlColumns("type in (bug, task) and type not in (task, 'new feature')", new Context().addTypes(IssueType.BUG), Issue.THREE2, Issue.ONE1, Issue.FOUR3);
    }

    //
    //Test for fields that actually don't have any context.
    //
    public void testFieldsWithNoContext() throws Exception
    {
        //Check the assignee field.
        List<Issue> issues = getIssuesAndRemoveIssues(Issue.ONE1);
        assertJqlColumns("assignee = admin", Context.GLOBAL, issues);
        assertJqlColumns("assignee != fred", Context.GLOBAL, issues);
        assertJqlColumns("assignee is not empty", Context.GLOBAL, issues);
        assertJqlColumns("assignee is empty", Context.GLOBAL, Issue.ONE1);
        assertJqlColumns("assignee in (admin, empty)", Context.GLOBAL, Issue.ALL_ISSUES);
        assertJqlColumns("assignee not in (fred, empty)", Context.GLOBAL, issues);

        assertJqlColumns("comment ~ donkey", Context.GLOBAL, Issue.THREE1);
        assertJqlColumns("comment !~ JIRA order by key asc", Context.GLOBAL, Issue.FOUR2, Issue.THREE1);

        assertJqlColumns("created < 7d", Context.GLOBAL, Issue.ALL_ISSUES);
        assertJqlColumns("created <= 7d", Context.GLOBAL, Issue.ALL_ISSUES);
        assertJqlColumns("created > 1000", Context.GLOBAL, Issue.ALL_ISSUES);
        assertJqlColumns("created >= 1000", Context.GLOBAL, Issue.ALL_ISSUES);
        assertJqlColumns("created != now()", Context.GLOBAL, Issue.ALL_ISSUES);
        assertJqlColumns("created is not empty", Context.GLOBAL, Issue.ALL_ISSUES);
        assertJqlColumns("created != null", Context.GLOBAL, Issue.ALL_ISSUES);
        assertJqlColumns("created not in (1881, 34883)", Context.GLOBAL, Issue.ALL_ISSUES);
        assertJqlColumns("created not in (empty, 47458)", Context.GLOBAL, Issue.ALL_ISSUES);

        issues = Arrays.asList(Issue.TWO1, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns("duedate = empty", Context.GLOBAL, getIssuesAndRemoveIssues(issues));
        assertJqlColumns("duedate is empty", Context.GLOBAL, getIssuesAndRemoveIssues(issues));
        assertJqlColumns("duedate in (empty)", Context.GLOBAL, getIssuesAndRemoveIssues(issues));
        assertJqlColumns("duedate < 7d", Context.GLOBAL, issues);
        assertJqlColumns("duedate <= 7d", Context.GLOBAL, issues);
        assertJqlColumns("duedate > 1000", Context.GLOBAL, issues);
        assertJqlColumns("duedate >= 1000", Context.GLOBAL, issues);
        assertJqlColumns("duedate != now()", Context.GLOBAL, issues);
        assertJqlColumns("duedate is not empty", Context.GLOBAL, issues);
        assertJqlColumns("duedate != null", Context.GLOBAL, issues);
        assertJqlColumns("duedate not in (1881, 34883)", Context.GLOBAL, issues);
        assertJqlColumns("duedate not in (empty, 47458)", Context.GLOBAL, issues);

        assertJqlColumns("description ~ suns", Context.GLOBAL, Issue.THREE1);
        assertJqlColumns("description !~ suns", Context.GLOBAL, Issue.ONE1);
        assertJqlColumns("description is empty", Context.GLOBAL, getIssuesAndRemoveIssues(Issue.THREE1, Issue.ONE1));
        assertJqlColumns("description is not empty", Context.GLOBAL, Issue.THREE1, Issue.ONE1);

        assertJqlColumns("environment ~ jira", Context.GLOBAL, Issue.ONE1);
        assertJqlColumns("environment !~ jira", Context.GLOBAL, Issue.TWO1);
        assertJqlColumns("environment is empty", Context.GLOBAL, getIssuesAndRemoveIssues(Issue.TWO1, Issue.ONE1));
        assertJqlColumns("environment is not empty", Context.GLOBAL, Issue.TWO1, Issue.ONE1);

        assertJqlColumns("originalEstimate = 5m", Context.GLOBAL, Issue.TWO1);
        assertJqlColumns("originalEstimate != 5m", Context.GLOBAL, Issue.THREE1);
        assertJqlColumns("originalEstimate in (5m, '5h 3m')", Context.GLOBAL, Issue.TWO1);
        assertJqlColumns("originalEstimate not in (5m, '5h 3m')", Context.GLOBAL, Issue.THREE1);
        assertJqlColumns("originalEstimate is empty", Context.GLOBAL, getIssuesAndRemoveIssues(Issue.TWO1, Issue.THREE1));
        assertJqlColumns("originalEstimate = empty", Context.GLOBAL, getIssuesAndRemoveIssues(Issue.TWO1, Issue.THREE1));
        assertJqlColumns("originalEstimate in (empty, 5m)", Context.GLOBAL, getIssuesAndRemoveIssues(Issue.THREE1));
        assertJqlColumns("originalEstimate is not empty", Context.GLOBAL, Issue.TWO1, Issue.THREE1);
        assertJqlColumns("originalEstimate != empty", Context.GLOBAL, Issue.TWO1, Issue.THREE1);
        assertJqlColumns("originalEstimate not in (empty, 5m)", Context.GLOBAL, Issue.THREE1);
        assertJqlColumns("originalEstimate < 1d", Context.GLOBAL, Issue.TWO1);
        assertJqlColumns("originalEstimate <= 1d", Context.GLOBAL, Issue.TWO1);
        assertJqlColumns("originalEstimate > 5d", Context.GLOBAL, Issue.THREE1);
        assertJqlColumns("originalEstimate >= 5d", Context.GLOBAL, Issue.THREE1);

        issues = Arrays.asList(Issue.TWO2, Issue.THREE2, Issue.ONE1, Issue.FOUR3, Issue.FOUR2);
        assertJqlColumns("priority = major", Context.GLOBAL, issues);
        assertJqlColumns("priority != major", Context.GLOBAL, getIssuesAndRemoveIssues(issues));
        issues = Arrays.asList(Issue.TWO2, Issue.THREE2, Issue.THREE1, Issue.ONE1, Issue.FOUR3, Issue.FOUR2);
        assertJqlColumns("priority in (major, critical)", Context.GLOBAL, issues);
        assertJqlColumns("priority not in (major, critical)", Context.GLOBAL, getIssuesAndRemoveIssues(issues));
        issues = Arrays.asList(Issue.TWO2, Issue.THREE2, Issue.ONE1, Issue.FOUR3, Issue.FOUR2);
        assertJqlColumns("priority in (major, empty)", Context.GLOBAL, issues);
        assertJqlColumns("priority is not empty", Context.GLOBAL, Issue.ALL_ISSUES);
        assertJqlColumns("priority != empty", Context.GLOBAL, Issue.ALL_ISSUES);
        assertJqlColumns("priority not in (empty, trivial)", Context.GLOBAL, getIssuesAndRemoveIssues(Issue.TWO1));
        assertJqlColumns("priority >= major", Context.GLOBAL, getIssuesAndRemoveIssues(Issue.TWO1, Issue.FOUR1));
        assertJqlColumns("priority > major", Context.GLOBAL, Issue.THREE1);
        assertJqlColumns("priority < major", Context.GLOBAL, Issue.TWO1, Issue.FOUR1);
        assertJqlColumns("priority <= major", Context.GLOBAL, getIssuesAndRemoveIssues(Issue.THREE1));

        assertJqlColumns("remainingEstimate = 4m", Context.GLOBAL, Issue.TWO1);
        assertJqlColumns("remainingEstimate != 4m", Context.GLOBAL, Issue.THREE1);
        assertJqlColumns("remainingEstimate in (4m, '5h 3m')", Context.GLOBAL, Issue.TWO1);
        assertJqlColumns("remainingEstimate not in (4m, '5h 3m')", Context.GLOBAL, Issue.THREE1);
        assertJqlColumns("remainingEstimate is empty", Context.GLOBAL, getIssuesAndRemoveIssues(Issue.TWO1, Issue.THREE1));
        assertJqlColumns("remainingEstimate = empty", Context.GLOBAL, getIssuesAndRemoveIssues(Issue.TWO1, Issue.THREE1));
        assertJqlColumns("remainingEstimate in (empty, 4m)", Context.GLOBAL, getIssuesAndRemoveIssues(Issue.THREE1));
        assertJqlColumns("remainingEstimate is not empty", Context.GLOBAL, Issue.TWO1, Issue.THREE1);
        assertJqlColumns("remainingEstimate != empty", Context.GLOBAL, Issue.TWO1, Issue.THREE1);
        assertJqlColumns("remainingEstimate not in (empty, 4m)", Context.GLOBAL, Issue.THREE1);
        assertJqlColumns("remainingEstimate < 1d", Context.GLOBAL, Issue.TWO1);
        assertJqlColumns("remainingEstimate <= 1d", Context.GLOBAL, Issue.TWO1);
        assertJqlColumns("remainingEstimate > 5d", Context.GLOBAL, Issue.THREE1);
        assertJqlColumns("remainingEstimate >= 5d", Context.GLOBAL, Issue.THREE1);

        assertJqlColumns("reporter = admin", Context.GLOBAL, Issue.TWO2, Issue.TWO1, Issue.THREE2, Issue.FOUR3, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns("reporter != admin", Context.GLOBAL, Issue.THREE1);
        assertJqlColumns("reporter in (admin, fred)", Context.GLOBAL, Issue.TWO2, Issue.TWO1, Issue.THREE2, Issue.THREE1, Issue.FOUR3, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns("reporter not in (fred, dylan)", Context.GLOBAL, Issue.TWO2, Issue.TWO1, Issue.THREE2, Issue.FOUR3, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns("reporter is empty", Context.GLOBAL, Issue.ONE1);
        assertJqlColumns("reporter = empty", Context.GLOBAL, Issue.ONE1);
        assertJqlColumns("reporter in (empty, fred)", Context.GLOBAL, Issue.THREE1, Issue.ONE1);
        assertJqlColumns("reporter is not empty", Context.GLOBAL, getIssuesAndRemoveIssues(Issue.ONE1));
        assertJqlColumns("reporter != empty", Context.GLOBAL, getIssuesAndRemoveIssues(Issue.ONE1));
        assertJqlColumns("reporter not in (empty, fred)", Context.GLOBAL, Issue.TWO2, Issue.TWO1, Issue.THREE2, Issue.FOUR3, Issue.FOUR2, Issue.FOUR1);

        assertJqlColumns("resolution = \"Won't Fix\"", Context.GLOBAL, Issue.ONE1);
        assertJqlColumns("resolution != \"Won't Fix\"", Context.GLOBAL, Issue.TWO1);
        assertJqlColumns("resolution in (\"Won't Fix\", fixed)", Context.GLOBAL, Issue.ONE1);
        assertJqlColumns("resolution not in (duplicate, fixed)", Context.GLOBAL, Issue.ONE1);
        assertJqlColumns("resolution is empty", Context.GLOBAL, getIssuesAndRemoveIssues(Issue.ONE1, Issue.TWO1));
        assertJqlColumns("resolution = empty", Context.GLOBAL, getIssuesAndRemoveIssues(Issue.ONE1, Issue.TWO1));
        assertJqlColumns("resolution in (empty, duplicate)", Context.GLOBAL, getIssuesAndRemoveIssues(Issue.ONE1));
        assertJqlColumns("resolution is NOT empty", Context.GLOBAL, Issue.TWO1, Issue.ONE1);
        assertJqlColumns("resolution != empty", Context.GLOBAL, Issue.TWO1, Issue.ONE1);
        assertJqlColumns("resolution not in (empty, duplicate)", Context.GLOBAL, Issue.ONE1);

        assertJqlColumns("resolutionDate < '2009/08/02'", Context.GLOBAL, Issue.ONE1);
        assertJqlColumns("resolutionDate <= '2009/08/02'", Context.GLOBAL, Issue.ONE1);
        assertJqlColumns("resolutionDate > '2009/08/02'", Context.GLOBAL, Issue.TWO1);
        assertJqlColumns("resolutionDate >= '2009/08/02'", Context.GLOBAL, Issue.TWO1);
        assertJqlColumns("resolutionDate != now()", Context.GLOBAL, Issue.TWO1, Issue.ONE1);
        assertJqlColumns("resolutionDate not in (now(), '2009/02/20')", Context.GLOBAL, Issue.TWO1, Issue.ONE1);
        assertJqlColumns("resolutionDate is empty", Context.GLOBAL, getIssuesAndRemoveIssues(Issue.ONE1, Issue.TWO1));
        assertJqlColumns("resolutionDate = empty", Context.GLOBAL, getIssuesAndRemoveIssues(Issue.ONE1, Issue.TWO1));
        assertJqlColumns("resolutionDate in (empty, now())", Context.GLOBAL, getIssuesAndRemoveIssues(Issue.ONE1, Issue.TWO1));
        assertJqlColumns("resolutionDate is not empty", Context.GLOBAL, Issue.TWO1, Issue.ONE1);
        assertJqlColumns("resolutionDate != null", Context.GLOBAL, Issue.TWO1, Issue.ONE1);
        assertJqlColumns("resolutionDate not in (null, now())", Context.GLOBAL, Issue.TWO1, Issue.ONE1);

        assertJqlColumns("summary ~ suns", Context.GLOBAL, Issue.THREE1);
        assertJqlColumns("summary !~ suns order by key desc", Context.GLOBAL, getIssuesAndRemoveIssues(Issue.THREE1));
        assertJqlColumns("summary is not empty order by key desc", Context.GLOBAL, Issue.ALL_ISSUES);

        assertJqlColumns("timeSpent = 1m", Context.GLOBAL, Issue.TWO1);
        assertJqlColumns("timeSpent != 1m", Context.GLOBAL, Issue.THREE1);
        assertJqlColumns("timeSpent in (1m, '5h 3m')", Context.GLOBAL, Issue.TWO1);
        assertJqlColumns("timeSpent not in (1m, '5h 3m')", Context.GLOBAL, Issue.THREE1);
        assertJqlColumns("timeSpent is empty", Context.GLOBAL, getIssuesAndRemoveIssues(Issue.TWO1, Issue.THREE1));
        assertJqlColumns("timeSpent = empty", Context.GLOBAL, getIssuesAndRemoveIssues(Issue.TWO1, Issue.THREE1));
        assertJqlColumns("timeSpent in (empty, 1m)", Context.GLOBAL, getIssuesAndRemoveIssues(Issue.THREE1));
        assertJqlColumns("timeSpent is not empty", Context.GLOBAL, Issue.TWO1, Issue.THREE1);
        assertJqlColumns("timeSpent != empty", Context.GLOBAL, Issue.TWO1, Issue.THREE1);
        assertJqlColumns("timeSpent not in (empty, 1m)", Context.GLOBAL, Issue.THREE1);
        assertJqlColumns("timeSpent < 1d", Context.GLOBAL, Issue.TWO1);
        assertJqlColumns("timeSpent <= 1d", Context.GLOBAL, Issue.TWO1);
        assertJqlColumns("timeSpent > 5d", Context.GLOBAL, Issue.THREE1);
        assertJqlColumns("timeSpent >= 5d", Context.GLOBAL, Issue.THREE1);

        assertJqlColumns("updated < 7d", Context.GLOBAL, Issue.ALL_ISSUES);
        assertJqlColumns("updated <= 7d", Context.GLOBAL, Issue.ALL_ISSUES);
        assertJqlColumns("updated > 1000", Context.GLOBAL, Issue.ALL_ISSUES);
        assertJqlColumns("updated >= 1000", Context.GLOBAL, Issue.ALL_ISSUES);
        assertJqlColumns("updated != 2004-08-10", Context.GLOBAL, Issue.ALL_ISSUES);
        assertJqlColumns("updated is not empty", Context.GLOBAL, Issue.ALL_ISSUES);
        assertJqlColumns("updated != null", Context.GLOBAL, Issue.ALL_ISSUES);
        assertJqlColumns("updated not in (1881, 34883)", Context.GLOBAL, Issue.ALL_ISSUES);
        assertJqlColumns("updated not in (empty, 47458)", Context.GLOBAL, Issue.ALL_ISSUES);

        assertJqlColumns("votes = 0", Context.GLOBAL, getIssuesAndRemoveIssues(Issue.TWO1));
        assertJqlColumns("votes != 0", Context.GLOBAL, Issue.TWO1);
        assertJqlColumns("votes in (0, 2, 4)", Context.GLOBAL, getIssuesAndRemoveIssues(Issue.TWO1));
        assertJqlColumns("votes not in (0, 2, 4)", Context.GLOBAL, Issue.TWO1);
        assertJqlColumns("votes < 1", Context.GLOBAL, getIssuesAndRemoveIssues(Issue.TWO1));
        assertJqlColumns("votes <= 1", Context.GLOBAL, Issue.ALL_ISSUES);
        assertJqlColumns("votes > 0", Context.GLOBAL, Issue.TWO1);
        assertJqlColumns("votes >= 0", Context.GLOBAL, Issue.ALL_ISSUES);

        assertJqlColumns("workRatio = 20", Context.GLOBAL, Issue.TWO1);
        assertJqlColumns("workRatio != 20", Context.GLOBAL, Issue.THREE1);
        assertJqlColumns("workRatio in (20, 21)", Context.GLOBAL, Issue.TWO1);
        assertJqlColumns("workRatio not in (20, 39393)", Context.GLOBAL, Issue.THREE1);
        assertJqlColumns("workRatio is empty", Context.GLOBAL, getIssuesAndRemoveIssues(Issue.TWO1, Issue.THREE1));
        assertJqlColumns("workRatio = empty", Context.GLOBAL, getIssuesAndRemoveIssues(Issue.TWO1, Issue.THREE1));
        assertJqlColumns("workRatio in (empty, 20)", Context.GLOBAL, getIssuesAndRemoveIssues(Issue.THREE1));
        assertJqlColumns("workRatio is not empty", Context.GLOBAL, Issue.TWO1, Issue.THREE1);
        assertJqlColumns("workRatio != empty", Context.GLOBAL, Issue.TWO1, Issue.THREE1);
        assertJqlColumns("workRatio not in (empty, 20)", Context.GLOBAL, Issue.THREE1);
        assertJqlColumns("workRatio < 10", Context.GLOBAL, Issue.THREE1);
        assertJqlColumns("workRatio <= 10", Context.GLOBAL, Issue.THREE1);
        assertJqlColumns("workRatio > 10", Context.GLOBAL, Issue.TWO1);
        assertJqlColumns("workRatio >= 10", Context.GLOBAL, Issue.TWO1);
    }

    //
    // Test the context for the category clause.
    //
    public void testCategoryContext() throws Exception
    {
        /*
            cat:catone -> {proj:one, proj:three}
            cat:catthree -> {}
            cat:cattwo -> {proj:four}
         */

        assertJqlColumns("category = catone", new Context().addProjects(Project.ONE, Project.THREE), getIssuesForProjects(Project.ONE, Project.THREE));
        assertJqlColumns("category = cattwo", new Context().addProject(Project.FOUR), getIssuesForProjects(Project.FOUR));

        assertJqlColumns("category != catone", new Context().addProjects(Project.FOUR), getIssuesForProjects(Project.FOUR));
        assertJqlColumns("category != catthree", new Context().addProjects(Project.ONE, Project.THREE, Project.FOUR), getIssuesForProjects(Project.ONE, Project.THREE, Project.FOUR));

        assertJqlColumns("category in (catone, cattwo)", new Context().addProjects(Project.ONE, Project.THREE, Project.FOUR), getIssuesForProjects(Project.ONE, Project.THREE, Project.FOUR));
        assertJqlColumns("category = catone or category = cattwo", new Context().addProjects(Project.ONE, Project.THREE, Project.FOUR), getIssuesForProjects(Project.ONE, Project.THREE, Project.FOUR));

        assertJqlColumns("category not in (cattwo, catthree)", new Context().addProjects(Project.ONE, Project.THREE), getIssuesForProjects(Project.ONE, Project.THREE));
        assertJqlColumns("not category = cattwo and category != catthree", new Context().addProjects(Project.ONE, Project.THREE), getIssuesForProjects(Project.ONE, Project.THREE));

        assertJqlColumns("category is empty", new Context().addProject(Project.TWO), getIssuesForProjects(Project.TWO));
        assertJqlColumns("category = empty", new Context().addProject(Project.TWO), getIssuesForProjects(Project.TWO));
        assertJqlColumns("category in (empty)", new Context().addProject(Project.TWO), getIssuesForProjects(Project.TWO));
        assertJqlColumns("category in (empty, catone)", new Context().addProjects(Project.TWO, Project.ONE, Project.THREE), getIssuesForProjects(Project.TWO, Project.ONE, Project.THREE));
        assertJqlColumns("category = empty or category = catone", new Context().addProjects(Project.TWO, Project.ONE, Project.THREE), getIssuesForProjects(Project.TWO, Project.ONE, Project.THREE));
        assertJqlColumns("category in (empty, catone, catthree)", new Context().addProjects(Project.TWO, Project.ONE, Project.THREE), getIssuesForProjects(Project.TWO, Project.ONE, Project.THREE));

        assertJqlColumns("category is not empty", new Context().addProjects(Project.ONE, Project.THREE, Project.FOUR), getIssuesForProjects(Project.ONE, Project.THREE, Project.FOUR));
        assertJqlColumns("category != empty", new Context().addProjects(Project.ONE, Project.THREE, Project.FOUR), getIssuesForProjects(Project.ONE, Project.THREE, Project.FOUR));
        assertJqlColumns("category not in (empty)", new Context().addProjects(Project.ONE, Project.THREE, Project.FOUR), getIssuesForProjects(Project.ONE, Project.THREE, Project.FOUR));
        assertJqlColumns("category not in (empty, catthree)", new Context().addProjects(Project.ONE, Project.THREE, Project.FOUR), getIssuesForProjects(Project.ONE, Project.THREE, Project.FOUR));
        assertJqlColumns("category != empty and category != catthree", new Context().addProjects(Project.ONE, Project.THREE, Project.FOUR), getIssuesForProjects(Project.ONE, Project.THREE, Project.FOUR));
        assertJqlColumns("category not in (empty, catone)", new Context().addProjects(Project.FOUR), getIssuesForProjects(Project.FOUR));
        assertJqlColumns("not (category is empty or category = catone)", new Context().addProjects(Project.FOUR), getIssuesForProjects(Project.FOUR));

        //Lets make sure that invalid filter is correctly calculated.
        assertFilterColumns(10040, new Context().addProjects(Project.ONE, Project.THREE), getIssuesForProjects(Project.ONE, Project.THREE));

        navigation.login(FRED_USERNAME);
        assertJqlColumns("category = catone", new Context().addProject(Project.ONE), getIssuesForProjects(Project.ONE));
        assertJqlColumns("category != catthree", new Context().addProjects(Project.ONE, Project.FOUR), Issue.ONE1, Issue.FOUR3);
    }

    //
    // Test the context for the affectedVersion clause.
    //
    public void testAffectedVersion() throws Exception
    {
        assertSystemVersionField("affectedVersion", 10050);
    }

    //
    // Test the context for fixVersion clause.
    //
    public void testFixVersion()
    {
        assertSystemVersionField("fixVersion", 10060);
    }

    //
    // Test for the component system field.
    //
    public void testComponent() throws Exception
    {
        /*
            Test data contains:

            proj:one -> comp:{one, two, three}
            proj:two -> comp:{two, twoonly}
            proj:three -> comp:{three}
            proj:four -> comp:empty

         */

        assertJqlColumns("component = one", new Context().addProject(Project.ONE), Issue.ONE1);
        assertJqlColumns("component = two", new Context().addProjects(Project.ONE, Project.TWO), Issue.ONE1);
        assertJqlColumns("component = three", new Context().addProjects(Project.ONE, Project.THREE), Issue.THREE2, Issue.THREE1, Issue.ONE1);
        assertJqlColumns("component = twoonly", new Context().addProjects(Project.TWO), Issue.TWO1);
        assertJqlColumns("component != one", new Context().addProjects(Project.ONE, Project.TWO, Project.THREE), Issue.TWO1, Issue.THREE2, Issue.THREE1);
        assertJqlColumns("component != two", new Context().addProjects(Project.ONE, Project.TWO, Project.THREE), Issue.TWO1, Issue.THREE2, Issue.THREE1);
        assertJqlColumns("component != three", new Context().addProjects(Project.ONE, Project.TWO), Issue.TWO1);
        assertJqlColumns("component != twoonly", new Context().addProjects(Project.ONE, Project.TWO, Project.THREE), Issue.THREE2, Issue.THREE1, Issue.ONE1);

        assertJqlColumns("component in (one, two)", new Context().addProjects(Project.ONE, Project.TWO), Issue.ONE1);
        assertJqlColumns("component = one or component = two", new Context().addProjects(Project.ONE, Project.TWO), Issue.ONE1);
        assertJqlColumns("component in (one, three)", new Context().addProjects(Project.ONE, Project.THREE), Issue.THREE2, Issue.THREE1, Issue.ONE1);
        assertJqlColumns("component = one or component = three", new Context().addProjects(Project.ONE, Project.THREE), Issue.THREE2, Issue.THREE1, Issue.ONE1);
        assertJqlColumns("component in (twoonly)", new Context().addProjects(Project.TWO), Issue.TWO1);
        assertJqlColumns("component = twoonly", new Context().addProjects(Project.TWO), Issue.TWO1);

        assertJqlColumns("component not in (one, two)", new Context().addProjects(Project.ONE, Project.TWO, Project.THREE), Issue.TWO1, Issue.THREE2, Issue.THREE1);
        assertJqlColumns("not (component = one or component = two)", new Context().addProjects(Project.ONE, Project.TWO, Project.THREE), Issue.TWO1, Issue.THREE2, Issue.THREE1);
        assertJqlColumns("component not in (twoonly, two)", new Context().addProjects(Project.ONE, Project.TWO, Project.THREE), Issue.THREE2, Issue.THREE1);
        assertJqlColumns("component != twoonly and component != two", new Context().addProjects(Project.ONE, Project.TWO, Project.THREE), Issue.THREE2, Issue.THREE1);
        assertJqlColumns("component not in (one, two, three)", new Context().addProjects(Project.ONE, Project.TWO), Issue.TWO1);
        assertJqlColumns("component != one and not (component = two or component = three)", new Context().addProjects(Project.ONE, Project.TWO), Issue.TWO1);

        assertJqlColumns("component is empty", Context.GLOBAL, Issue.TWO2, Issue.FOUR3, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns("component = empty", Context.GLOBAL, Issue.TWO2, Issue.FOUR3, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns("component in (empty, twoonly)", new Context().addProject(Project.TWO), Issue.TWO2, Issue.TWO1, Issue.FOUR3, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns("component is empty or component = twoonly", new Context().addProject(Project.TWO), Issue.TWO2, Issue.TWO1, Issue.FOUR3, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns("component in (empty, two)", new Context().addProjects(Project.ONE, Project.TWO), Issue.TWO2, Issue.ONE1, Issue.FOUR3, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns("not (component is not empty and component != two)", new Context().addProjects(Project.ONE, Project.TWO), Issue.TWO2, Issue.ONE1, Issue.FOUR3, Issue.FOUR2, Issue.FOUR1);

        assertJqlColumns("component is not empty", Context.GLOBAL, Issue.TWO1, Issue.THREE2, Issue.THREE1, Issue.ONE1);
        assertJqlColumns("component != null", Context.GLOBAL, Issue.TWO1, Issue.THREE2, Issue.THREE1, Issue.ONE1);
        assertJqlColumns("component not in (empty, three)", new Context().addProjects(Project.ONE, Project.TWO), Issue.TWO1);
        assertJqlColumns("not (component = empty or component = three)", new Context().addProjects(Project.ONE, Project.TWO), Issue.TWO1);
        assertJqlColumns("component not in (empty, two, three, one)", new Context().addProjects(Project.ONE, Project.TWO), Issue.TWO1);
        assertJqlColumns("component is not empty and component != two and component != three and component != one", new Context().addProjects(Project.ONE, Project.TWO), Issue.TWO1);

        assertFilterColumns(10070, new Context().addProjects(Project.ONE, Project.TWO), Issue.ONE1);
    }

    //
    // Test for the issuekey system field.
    //
    public void testIssueContext() throws Exception
    {
        assertJqlColumns("issuekey = 'one-1'", new Context().addContext(Project.ONE, IssueType.BUG), Issue.ONE1);
        assertJqlColumns("issuekey = 'two-1'", new Context().addContext(Project.TWO, IssueType.TASK), Issue.TWO1);
        assertJqlColumns("issuekey = 'three-1'", new Context().addContext(Project.THREE, IssueType.FEATURE), Issue.THREE1);
        assertJqlColumns("issuekey = 'three-2'", new Context().addContext(Project.THREE, IssueType.BUG), Issue.THREE2);
        assertJqlColumns("issuekey = 'four-1'", new Context().addContext(Project.FOUR, IssueType.IMPROVEMENT), Issue.FOUR1);

        assertJqlColumns("issuekey != 'one-1'", Context.GLOBAL, getIssuesAndRemoveIssues(Issue.ONE1));
        assertJqlColumns("issuekey != 'two-1'", Context.GLOBAL, getIssuesAndRemoveIssues(Issue.TWO1));
        assertJqlColumns("issuekey != 'three-1'", Context.GLOBAL, getIssuesAndRemoveIssues(Issue.THREE1));
        assertJqlColumns("issuekey != 'three-2'", Context.GLOBAL, getIssuesAndRemoveIssues(Issue.THREE2));
        assertJqlColumns("issuekey != 'four-1'", Context.GLOBAL, getIssuesAndRemoveIssues(Issue.FOUR1));

        assertJqlColumns("issuekey in ('one-1', 'two-1')", new Context().addContext(Project.ONE, IssueType.BUG).addContext(Project.TWO, IssueType.TASK), Issue.TWO1, Issue.ONE1);
        assertJqlColumns("issuekey = 'one-1' or key = 'two-1'", new Context().addContext(Project.ONE, IssueType.BUG).addContext(Project.TWO, IssueType.TASK), Issue.TWO1, Issue.ONE1);
        assertJqlColumns("issuekey in ('three-2', 'three-1')", new Context().addContext(Project.THREE, IssueType.FEATURE).addContext(Project.THREE, IssueType.BUG), Issue.THREE2, Issue.THREE1);
        assertJqlColumns("issuekey = 'three-2' or key = 'three-1'", new Context().addContext(Project.THREE, IssueType.FEATURE).addContext(Project.THREE, IssueType.BUG), Issue.THREE2, Issue.THREE1);

        assertJqlColumns("issuekey not in ('one-1', 'two-1')", Context.GLOBAL, getIssuesAndRemoveIssues(Issue.ONE1, Issue.TWO1));
        assertJqlColumns("issuekey not in ('four-1', 'three-1')", Context.GLOBAL, getIssuesAndRemoveIssues(Issue.FOUR1, Issue.THREE1));

        assertJqlColumns("issuekey in (empty, \"one-1\")", new Context().addContext(Project.ONE, IssueType.BUG), Issue.ONE1);
        assertJqlColumns("issuekey = empty or key = \"one-1\"", new Context().addContext(Project.ONE, IssueType.BUG), Issue.ONE1);

        assertJqlColumns("issuekey is not empty", Context.GLOBAL, getIssuesAndRemoveIssues());
        assertJqlColumns("issuekey != empty", Context.GLOBAL, getIssuesAndRemoveIssues());
        assertJqlColumns("issuekey not in (empty, 'one-1')", Context.GLOBAL, getIssuesAndRemoveIssues(Issue.ONE1));

        assertJqlColumns("key > 'three-1'", new Context().addProject(Project.THREE), Issue.THREE2);
        assertJqlColumns("key < 'three-2'", new Context().addProject(Project.THREE), Issue.THREE1);
        assertJqlColumns("key >= 'three-1'", new Context().addProject(Project.THREE), Issue.THREE2, Issue.THREE1);
        assertJqlColumns("key >= 'one-1'", new Context().addProject(Project.ONE), Issue.ONE1);
        assertJqlColumns("key <= 'three-1'", new Context().addProject(Project.THREE), Issue.THREE1);
        assertJqlColumns("key <= 'two-1'", new Context().addProject(Project.TWO), Issue.TWO1);

        assertFilterColumns(10071, new Context().addContext(Project.TWO, IssueType.TASK), Issue.TWO1);
    }

    //
    // Test for the "level" field.
    //
    public void testLevelContext() throws Exception
    {
        /*
            proj:one -> level:{oneadmin}
            proj:two -> level:{twoonly}
            proj:three -> level:{oneadmin, threeonly}
            proj:four -> level:{fouronly}
            
         */

        assertJqlColumns("level = oneadmin", new Context().addProjects(Project.ONE, Project.THREE), Issue.ONE1);
        assertJqlColumns("level = threeonly", new Context().addProjects(Project.THREE), Issue.THREE2);
        assertJqlColumns("level = fouronly", new Context().addProjects(Project.FOUR), Issue.FOUR2, Issue.FOUR1);

        assertJqlColumns("level != oneadmin", new Context().addProjects(Project.TWO, Project.THREE, Project.FOUR), Issue.THREE2, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns("level != threeonly", new Context().addProjects(Project.ONE, Project.THREE, Project.TWO, Project.FOUR), Issue.ONE1, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns("level != fouronly", new Context().addProjects(Project.ONE, Project.TWO, Project.THREE), Issue.THREE2, Issue.ONE1);

        assertJqlColumns("level in (oneadmin, threeonly)", new Context().addProjects(Project.ONE, Project.THREE), Issue.THREE2, Issue.ONE1);
        assertJqlColumns("level = oneadmin or level = threeonly", new Context().addProjects(Project.ONE, Project.THREE), Issue.THREE2, Issue.ONE1);
        assertJqlColumns("level in (fouronly)", new Context().addProjects(Project.FOUR), Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns("level = fouronly", new Context().addProjects(Project.FOUR), Issue.FOUR2, Issue.FOUR1);

        assertJqlColumns("level not in (oneadmin, threeonly)", new Context().addProjects(Project.TWO, Project.FOUR, Project.THREE), Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns("level != oneadmin and level != threeonly", new Context().addProjects(Project.TWO, Project.FOUR, Project.THREE), Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns("level not in (oneadmin, fouronly)", new Context().addProjects(Project.TWO, Project.THREE), Issue.THREE2);
        assertJqlColumns("level != oneadmin and level != fouronly", new Context().addProjects(Project.TWO, Project.THREE), Issue.THREE2);

        assertJqlColumns("level is empty", Context.GLOBAL, Issue.TWO2, Issue.TWO1, Issue.THREE1, Issue.FOUR3);
        assertJqlColumns("level = empty", Context.GLOBAL, Issue.TWO2, Issue.TWO1, Issue.THREE1, Issue.FOUR3);
        assertJqlColumns("level in (empty)", Context.GLOBAL, Issue.TWO2, Issue.TWO1, Issue.THREE1, Issue.FOUR3);
        assertJqlColumns("level in (empty, threeonly)", new Context().addProject(Project.THREE), Issue.TWO2, Issue.TWO1, Issue.THREE2, Issue.THREE1, Issue.FOUR3);
        assertJqlColumns("level is empty or level = threeonly", new Context().addProject(Project.THREE), Issue.TWO2, Issue.TWO1, Issue.THREE2, Issue.THREE1, Issue.FOUR3);

        assertJqlColumns("level is not empty", Context.GLOBAL, Issue.THREE2, Issue.ONE1, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns("level != empty", Context.GLOBAL, Issue.THREE2, Issue.ONE1, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns("level not in (empty)", Context.GLOBAL, Issue.THREE2, Issue.ONE1, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns("level not in (empty, empty)", Context.GLOBAL, Issue.THREE2, Issue.ONE1, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns("level not in (empty, threeonly)", new Context().addProjects(Project.ONE, Project.TWO, Project.THREE, Project.FOUR), Issue.ONE1, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns("level != empty and level != threeonly", new Context().addProjects(Project.ONE, Project.TWO, Project.THREE, Project.FOUR), Issue.ONE1, Issue.FOUR2, Issue.FOUR1);

        assertFilterColumns(10072, new Context().addProject(Project.FOUR), Issue.FOUR2, Issue.FOUR1);

        navigation.login(FRED_USERNAME);
        assertJqlColumns("level = oneadmin", new Context().addProject(Project.ONE), Issue.ONE1);
    }

    //
    // Test for the "parent" field.
    //
    public void testParentContext() throws Exception
    {
        /*
            parent:two-2 = two-1 => (proj:two, issetype: task)
            parent:four-2 = four-1 => (project:four, issuetype: improvement}
         */

        assertJqlColumns("parent = 'two-1'", new Context().addContext(Project.TWO, IssueType.TASK), Issue.TWO2);
        assertJqlColumns("parent = 'four-1'", new Context().addContext(Project.FOUR, IssueType.IMPROVEMENT), Issue.FOUR2);

        assertJqlColumns("parent != 'four-1'", Context.GLOBAL, getIssuesAndRemoveIssues(Issue.FOUR2));
        assertJqlColumns("parent != 'three-1'", Context.GLOBAL, getIssuesAndRemoveIssues());
        assertJqlColumns("parent != 'two-1'", Context.GLOBAL, getIssuesAndRemoveIssues(Issue.TWO2));

        assertJqlColumns("parent in ('two-1', 'three-1')", new Context().addContext(Project.TWO, IssueType.TASK).addContext(Project.THREE, IssueType.FEATURE), Issue.TWO2);
        assertJqlColumns("parent = 'two-1' or parent = 'three-1'", new Context().addContext(Project.TWO, IssueType.TASK).addContext(Project.THREE, IssueType.FEATURE), Issue.TWO2);
        assertJqlColumns("parent = 'two-1' or parent = 'four-1'", new Context().addContext(Project.TWO, IssueType.TASK).addContext(Project.FOUR, IssueType.IMPROVEMENT), Issue.TWO2, Issue.FOUR2);

        assertJqlColumns("parent not in ('two-1', 'three-1')", Context.GLOBAL, getIssuesAndRemoveIssues(Issue.TWO2));
        assertJqlColumns("parent != 'two-1' and  parent != 'three-1'", Context.GLOBAL, getIssuesAndRemoveIssues(Issue.TWO2));
        assertJqlColumns("parent not in ('two-1', 'four-1')", Context.GLOBAL, getIssuesAndRemoveIssues(Issue.FOUR2, Issue.TWO2));
        assertJqlColumns("parent != 'two-1' and  parent != 'four-1'", Context.GLOBAL, getIssuesAndRemoveIssues(Issue.FOUR2, Issue.TWO2));
        assertJqlColumns("parent not in ('one-1', 'three-1')", Context.GLOBAL, getIssuesAndRemoveIssues());
        assertJqlColumns("not parent = 'one-1' and parent != 'three-1'", Context.GLOBAL, getIssuesAndRemoveIssues());

        assertFilterColumns(10080, new Context().addContext(Project.FOUR, IssueType.IMPROVEMENT), Issue.FOUR2);
    }

    public void testSavedFilter() throws Exception
    {
        /*
            filter:onefilter = { (issuekey = 'one-1')[admin not shared], ( project = two) [fred shared globally] }
            filter:taskfilter = { (type = task)[admin not shared] }
            filter:threefilter = { (issuekey =< 'three-1')[admin shared globally] }
         */

        assertJqlColumns("savedFilter = taskfilter", new Context().addType(IssueType.TASK), Issue.TWO1);
        assertJqlColumns("savedFilter = onefilter", new Context().addProject(Project.TWO).addContext(Project.ONE, IssueType.BUG), Issue.TWO2, Issue.TWO1, Issue.ONE1);
        assertJqlColumns("savedFilter = threefilter", new Context().addProject(Project.THREE), Issue.THREE1);

        assertJqlColumns("savedFilter != taskfilter", new Context().addTypes(getIssueTypesAndRemove(IssueType.TASK)), getIssuesAndRemoveIssues(Issue.TWO1));
        assertJqlColumns("savedFilter != onefilter", new Context().addProjects(getProjectsAndRemove(Project.TWO)), getIssuesAndRemoveIssues(Issue.TWO2, Issue.TWO1, Issue.ONE1));
        assertJqlColumns("savedFilter != threefilter", new Context().addProject(Project.THREE), getIssuesAndRemoveIssues(Issue.THREE1));

        assertJqlColumns("savedFilter in (taskfilter, threefilter)", new Context().addProject(Project.THREE).addType(IssueType.TASK), Issue.TWO1, Issue.THREE1);
        assertJqlColumns("savedFilter = taskfilter or filter = threefilter", new Context().addProject(Project.THREE).addType(IssueType.TASK), Issue.TWO1, Issue.THREE1);
        assertJqlColumns("savedFilter in (onefilter, threefilter)", new Context().addProjects(Project.THREE, Project.TWO).addContext(Project.ONE, IssueType.BUG), Issue.TWO2, Issue.TWO1, Issue.THREE1, Issue.ONE1);
        assertJqlColumns("savedFilter = onefilter or filter = threefilter", new Context().addProjects(Project.THREE, Project.TWO).addContext(Project.ONE, IssueType.BUG), Issue.TWO2, Issue.TWO1, Issue.THREE1, Issue.ONE1);

        assertJqlColumns("savedFilter not in (taskfilter, threefilter)", new Context().addContexts(Project.THREE, getIssueTypesAndRemove(IssueType.TASK)), getIssuesAndRemoveIssues(Issue.THREE1, Issue.TWO1));
        assertJqlColumns("savedFilter != taskfilter and savedFilter != threefilter", new Context().addContexts(Project.THREE, getIssueTypesAndRemove(IssueType.TASK)), getIssuesAndRemoveIssues(Issue.THREE1, Issue.TWO1));
        assertJqlColumns("savedFilter not   in (onefilter, threefilter)", new Context().addProjects(Project.THREE), Issue.THREE2, Issue.FOUR3, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns("savedFilter != onefilter and filter != threefilter", new Context().addProjects(Project.THREE), Issue.THREE2, Issue.FOUR3, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns("savedFilter not   in (onefilter, taskfilter)", new Context().addContexts(getProjectsAndRemove(Project.TWO), getIssueTypesAndRemove(IssueType.TASK)), Issue.THREE2, Issue.THREE1, Issue.FOUR3, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns("savedFilter != onefilter and savedFilter != taskfilter", new Context().addContexts(getProjectsAndRemove(Project.TWO), getIssueTypesAndRemove(IssueType.TASK)), Issue.THREE2, Issue.THREE1, Issue.FOUR3, Issue.FOUR2, Issue.FOUR1);

        assertFilterColumns(10090, new Context().addContext(Project.ONE, IssueType.BUG).addProject(Project.TWO).addType(IssueType.TASK), Issue.TWO2, Issue.TWO1, Issue.ONE1);

        navigation.login(FRED_USERNAME);
        assertJqlColumns("savedFilter = onefilter", new Context().addProject(Project.TWO), Issue.TWO2, Issue.TWO1);
    }

    public void testStatusContext() throws Exception
    {
        /*
            status:two -> {(proj:two, type:task)}
            status:three -> {(proj:three, *), (proj:four, *)}
            status:open -> {(*, *)}
         */

        assertJqlColumns("status = open", Context.GLOBAL, Issue.TWO2, Issue.THREE2, Issue.FOUR2);
        assertJqlColumns("status = two", new Context().addContext(Project.TWO, IssueType.TASK), Issue.TWO1);
        assertJqlColumns("status = three", new Context().addProjects(Project.FOUR, Project.THREE), Issue.THREE1, Issue.FOUR3, Issue.FOUR1);

        assertJqlColumns("status != open", Context.GLOBAL, Issue.TWO1, Issue.THREE1, Issue.ONE1, Issue.FOUR3, Issue.FOUR1);
        assertJqlColumns("status != two", Context.GLOBAL, Issue.TWO2, Issue.THREE2, Issue.THREE1, Issue.ONE1, Issue.FOUR3, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns("status != three", Context.GLOBAL, Issue.TWO2, Issue.TWO1, Issue.THREE2, Issue.ONE1, Issue.FOUR2);

        assertJqlColumns("status in (open, two)", new Context().addContext(Project.TWO, IssueType.TASK), Issue.TWO2, Issue.TWO1, Issue.THREE2, Issue.FOUR2);
        assertJqlColumns("status = open or status = two", new Context().addContext(Project.TWO, IssueType.TASK), Issue.TWO2, Issue.TWO1, Issue.THREE2, Issue.FOUR2);
        assertJqlColumns("status in (two, three)", new Context().addContext(Project.TWO, IssueType.TASK).addProjects(Project.THREE, Project.FOUR), Issue.TWO1, Issue.THREE1, Issue.FOUR3, Issue.FOUR1);
        assertJqlColumns("status = two or status = three", new Context().addContext(Project.TWO, IssueType.TASK).addProjects(Project.THREE, Project.FOUR), Issue.TWO1, Issue.THREE1, Issue.FOUR3, Issue.FOUR1);

        assertJqlColumns("status not in (open, two)", Context.GLOBAL, Issue.THREE1, Issue.ONE1, Issue.FOUR3, Issue.FOUR1);
        assertJqlColumns("status != open and status != two", Context.GLOBAL, Issue.THREE1, Issue.ONE1, Issue.FOUR3, Issue.FOUR1);
        assertJqlColumns("status not in (three, two)", Context.GLOBAL, Issue.TWO2, Issue.THREE2, Issue.ONE1, Issue.FOUR2);
        assertJqlColumns("status != three and status != two", Context.GLOBAL, Issue.TWO2, Issue.THREE2, Issue.ONE1, Issue.FOUR2);
        assertJqlColumns("status not in (three, two, open)", Context.GLOBAL, Issue.ONE1);
        assertJqlColumns("not (status = three or status = two or status = open)", Context.GLOBAL, Issue.ONE1);

        assertJqlColumns("status in (empty, two)", new Context().addContext(Project.TWO, IssueType.TASK), Issue.TWO1);
        assertJqlColumns("status = empty or status = two", new Context().addContext(Project.TWO, IssueType.TASK), Issue.TWO1);

        assertJqlColumns("status is not empty", Context.GLOBAL, Issue.TWO2, Issue.TWO1, Issue.THREE2, Issue.THREE1, Issue.ONE1, Issue.FOUR3, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns("status not in (empty)", Context.GLOBAL, Issue.TWO2, Issue.TWO1, Issue.THREE2, Issue.THREE1, Issue.ONE1, Issue.FOUR3, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns("status not in (empty, three, two)", Context.GLOBAL, Issue.TWO2, Issue.THREE2, Issue.ONE1, Issue.FOUR2);
        assertJqlColumns("not (status = empty or status = three or status = two)", Context.GLOBAL, Issue.TWO2, Issue.THREE2, Issue.ONE1, Issue.FOUR2);

        //status in (dontexist) or status = open or status = (two, me)
        assertFilterColumns(10091, Context.GLOBAL, Issue.TWO2, Issue.THREE2, Issue.FOUR2);

        navigation.login(FRED_USERNAME);
        assertJqlColumns("status = three", new Context().addProject(Project.FOUR), Issue.FOUR3);
    }

    public void testDatePicker() throws Exception
    {
        /*
            DatePickerBoth -> {("one", IssueType.BUG)}
            DatePickerComplex -> {(*, task), (one, *), (two, improvement)}
            DatePickerGlobal -> {(*, *)}
            DatePickerProject -> {(two, *), (three, *)}
            DatePickerType -> {(*, IssueType.IMPROVEMENT)}
         */
        assertDateCustomField("datepickerglobal", "2009-08-15",
                Context.GLOBAL,
                Issue.TWO1, Issue.ONE1, Issue.FOUR1);

        assertDateCustomField("DatePickerComplex", "2009-08-15",
                new Context().addType(IssueType.TASK).addProject(Project.ONE).addContext(Project.TWO, IssueType.IMPROVEMENT),
                Issue.ONE1);

        assertDateCustomField("DatePickerBoth", "2009-08-15", new Context().addContext(Project.ONE, IssueType.BUG),
                Issue.ONE1);

        assertDateCustomField("DatePickerProject", "2009-08-15", new Context().addProject(Project.TWO).addProject(Project.THREE),
                Issue.TWO1, Issue.THREE1);

        assertDateCustomField("DatePickerType", "2009-08-15", new Context().addType(IssueType.IMPROVEMENT),
                Issue.FOUR1);

        //Check an invalid filter.
        assertFilterColumns(10100,
                new Context().addType(IssueType.TASK).addProject(Project.ONE).addContext(Project.TWO, IssueType.IMPROVEMENT),
                Issue.TWO1, Issue.ONE1, Issue.FOUR1);

        navigation.login(FRED_USERNAME);
        assertJqlColumns("DatePickerProject = 2009-08-15",
                new Context().addProject(Project.TWO),
                Issue.TWO1);
    }

    public void testDateTimePicker() throws Exception
    {
        /*
            DateTimeBoth -> {(Project.FOUR, IssueType.BUG)}
            DateTimeComplex -> {(Project.FOUR, IssueType.IMPROVEMENT), (Project.THREE, *), (*, IssueType.TASK)}
            DateTimeGlobal ->  {(*, *)}
            DateTimeProject -> {(Project.ONE, *)};
            DateTimeType -> {(*, IssueType.FEATURE)};
        */
        assertDateCustomField("DateTimeGlobal", "2009-08-07",
                Context.GLOBAL,
                Issue.ONE1, Issue.FOUR3);

        assertDateCustomField("DateTimeComplex", "2009-08-07",
                new Context().addContext(Project.FOUR, IssueType.IMPROVEMENT).addProject(Project.THREE).addType(IssueType.TASK),
                Issue.TWO1);

        assertDateCustomField("DateTimeBoth", "2009-08-07", new Context().addContext(Project.FOUR, IssueType.BUG),
                Issue.FOUR3);

        assertDateCustomField("DateTimeProject", "2009-08-07", new Context().addProject(Project.ONE),
                Issue.ONE1);

        assertDateCustomField("DateTimeType", "2009-08-07", new Context().addType(IssueType.FEATURE),
                Issue.THREE1);

        //Check an invalid filter.
        assertFilterColumns(10101,
                new Context().addContext(Project.FOUR, IssueType.BUG),
                Issue.FOUR3);

        navigation.login(FRED_USERNAME);
        assertJqlColumns("DateTimeComplex = 2009-08-07",
                new Context().addContext(Project.FOUR, IssueType.IMPROVEMENT).addType(IssueType.TASK),
                Issue.TWO1);
    }

    public void testFreeTextField() throws Exception
    {
        /*
            FreeTextBoth -> {(Project.TWO, IssueType.TASK)}
            FreeTextComplex -> {(Project.ONE, IssueType.FEATURE), (Project.FOUR, *), (*, IssueType.BUG)}
            FreeTextGlobal -> {(*, *)}
            FreeTextProject -> {(Project.FOUR, *), (Project.THREE, *)}
            FreeTextType -> {(*, IssueType.BUG)}
         */

        assertTextField("freetextglobal", Context.GLOBAL, Issue.THREE1);
        assertTextField("FreeTextBoth", new Context().addContext(Project.TWO, IssueType.TASK), Issue.TWO1);
        assertTextField("FreeTextProject", new Context().addProjects(Project.FOUR, Project.THREE), Issue.THREE2, Issue.FOUR3, Issue.FOUR1);
        assertTextField("FreeTextType", new Context().addType(IssueType.BUG), Issue.THREE2, Issue.ONE1);
        assertTextField("FreeTextComplex", new Context().addType(IssueType.BUG).addProject(Project.FOUR).addContext(Project.ONE, IssueType.FEATURE), Issue.FOUR1);

        //Check an invalid filter.
        assertFilterColumns(10102,
                new Context().addProjects(Project.FOUR, Project.THREE),
                Issue.THREE1);

        navigation.login(FRED_USERNAME);
        assertJqlColumns("freetextproject ~ match order by key desc", new Context().addProject(Project.FOUR), Issue.FOUR3);
    }

    public void testTextField() throws Exception
    {
        /*
            TextBoth -> {(Project.TWO, IssueType.SUBTASK)}
            TextComplex -> {(Project.THREE, IssueType.FEATURE), (*, IssueType.TASK)}
            TextGlobal -> {(*, *)};
            TextProject -> {(Project.ONE, *)}
            TextType -> {(*, IssueType.IMPROVEMENT)}
        */

        assertTextField("textglobal", Context.GLOBAL, Issue.TWO2, Issue.ONE1);
        assertTextField("TextBoth", new Context().addContext(Project.TWO, IssueType.SUBTASK), Issue.TWO2);
        assertTextField("TextProject", new Context().addProjects(Project.ONE), Issue.ONE1);
        assertTextField("TextType", new Context().addType(IssueType.IMPROVEMENT), Issue.FOUR1);
        assertTextField("TextComplex", new Context().addType(IssueType.TASK).addContext(Project.THREE, IssueType.FEATURE), Issue.TWO1);

        //Check an invalid filter.
        assertFilterColumns(10103,
                new Context().addType(IssueType.IMPROVEMENT),
                Issue.TWO2, Issue.ONE1);

        navigation.login(FRED_USERNAME);
        assertJqlColumns("TextComplex ~ match order by key desc", new Context().addType(IssueType.TASK), Issue.TWO1);
    }

    public void testAllTextClause() throws Exception
    {
        final List<Field> fieldsInSystem = getDefaultFields();
        final List<Field> textFields = getTextFields();
        fieldsInSystem.removeAll(textFields);

        for (Field field : textFields)
        {
            administration.fieldConfigurations().defaultFieldConfiguration().hideFields(field.getFieldName());
        }

        administration.reIndex();

        // assert the columns for just the system text fields (global)
        final String jqlClause = "text ~ 'match' order by key desc";
        navigation.issueNavigator().createSearch(jqlClause);
        assertColumns(jqlClause, calculateColumnsForContext(Context.GLOBAL, fieldsInSystem), Issue.ONE1);

        // unhide ReadTextBoth because it has a project specific context
        administration.fieldConfigurations().defaultFieldConfiguration().showFields(Field.READ_TEXT_BOTH.getFieldName());
        administration.reIndex();
        fieldsInSystem.add(fieldsInSystem.indexOf(Field.CHECKBOX_COMPLEX), Field.READ_TEXT_BOTH);

        // assert the columns
        navigation.issueNavigator().createSearch(jqlClause);
        List<String> columnNames = calculateColumnsForContext(new Context().addContext(Field.READ_TEXT_BOTH.getFieldContext()), fieldsInSystem);
        assertColumns(jqlClause, columnNames, Issue.THREE2, Issue.ONE1);
        assertFilterColumns(10140, columnNames, Issue.THREE1);

        // fred can't see project three, so we want to check that ReadTextBoth's context is not included
        navigation.login(FRED_USERNAME);
        fieldsInSystem.remove(Field.READ_TEXT_BOTH);

        // assert the columns
        navigation.issueNavigator().createSearch(jqlClause);
        assertColumns(jqlClause, calculateColumnsForContext(Context.GLOBAL, fieldsInSystem), Issue.ONE1);

        // go back to admin user
        navigation.login(ADMIN_USERNAME);

        // hide ReadTextBoth because we no longer care about it
        administration.fieldConfigurations().defaultFieldConfiguration().hideFields(Field.READ_TEXT_BOTH.getFieldName());

        // unhide FreeTextBoth
        administration.fieldConfigurations().defaultFieldConfiguration().showFields(Field.FREE_TEXT_BOTH.getFieldName());
        administration.reIndex();
        fieldsInSystem.add(fieldsInSystem.indexOf(Field.SELECT_LIST_COMPLEX), Field.FREE_TEXT_BOTH);
        final Context compoundContext = new Context().addContext(Field.FREE_TEXT_BOTH.getFieldContext());

        // assert the columns
        navigation.issueNavigator().createSearch(jqlClause);
        columnNames = calculateColumnsForContext(compoundContext, fieldsInSystem);
        assertColumns(jqlClause, columnNames, Issue.TWO1, Issue.ONE1);
        assertFilterColumns(10140, columnNames, Issue.THREE1);

        // unhide FreeTextComplex
        administration.fieldConfigurations().defaultFieldConfiguration().showFields(Field.FREE_TEXT_COMPLEX.getFieldName());
        administration.reIndex();
        fieldsInSystem.add(fieldsInSystem.indexOf(Field.SELECT_LIST_COMPLEX), Field.FREE_TEXT_COMPLEX);
        compoundContext.addContext(Field.FREE_TEXT_COMPLEX.getFieldContext());

        // assert the columns
        navigation.issueNavigator().createSearch(jqlClause);
        columnNames = calculateColumnsForContext(compoundContext, fieldsInSystem);
        assertColumns(jqlClause, columnNames, Issue.TWO1, Issue.ONE1, Issue.FOUR1);
        assertFilterColumns(10140, columnNames, Issue.THREE1);

        // unhide FreeTextGlobal
        administration.fieldConfigurations().defaultFieldConfiguration().showFields(Field.FREE_TEXT_GLOBAL.getFieldName());
        administration.reIndex();
        fieldsInSystem.add(fieldsInSystem.indexOf(Field.SELECT_LIST_COMPLEX), Field.FREE_TEXT_GLOBAL);

        // assert the columns
        navigation.issueNavigator().createSearch(jqlClause);
        columnNames = calculateColumnsForContext(compoundContext, fieldsInSystem);
        assertColumns(jqlClause, columnNames, Issue.TWO1, Issue.THREE1, Issue.ONE1, Issue.FOUR1);
        assertFilterColumns(10140, columnNames, Issue.THREE1);

        // unhide FreeTextProject
        administration.fieldConfigurations().defaultFieldConfiguration().showFields(Field.FREE_TEXT_PROJECT.getFieldName());
        administration.reIndex();
        fieldsInSystem.add(fieldsInSystem.indexOf(Field.SELECT_LIST_COMPLEX), Field.FREE_TEXT_PROJECT);
        compoundContext.addContext(Field.FREE_TEXT_PROJECT.getFieldContext());

        // assert the columns
        navigation.issueNavigator().createSearch(jqlClause);
        columnNames = calculateColumnsForContext(compoundContext, fieldsInSystem);
        assertColumns(jqlClause, columnNames, Issue.TWO1, Issue.THREE2, Issue.THREE1, Issue.ONE1, Issue.FOUR3, Issue.FOUR1);
        assertFilterColumns(10140, columnNames, Issue.THREE1);

        // unhide FreeTextType
        administration.fieldConfigurations().defaultFieldConfiguration().showFields(Field.FREE_TEXT_TYPE.getFieldName());
        administration.reIndex();
        fieldsInSystem.add(fieldsInSystem.indexOf(Field.SELECT_LIST_COMPLEX), Field.FREE_TEXT_TYPE);
        compoundContext.addContext(Field.FREE_TEXT_TYPE.getFieldContext());

        // assert the columns
        navigation.issueNavigator().createSearch(jqlClause);
        columnNames = calculateColumnsForContext(compoundContext, fieldsInSystem);
        assertColumns(jqlClause, columnNames, Issue.TWO1, Issue.THREE2, Issue.THREE1, Issue.ONE1, Issue.FOUR3, Issue.FOUR1);
        assertFilterColumns(10140, columnNames, Issue.THREE1);

        needsrestore = true;
    }

    public void testUrlField() throws Exception
    {
        /*
            UrlBoth -> {(Project.FOUR, IssueType.SUBTASK)}
            UrlComplex -> {(Project.THREE, *), (*, IssueType.IMPROVEMENT)}
            UrlGlobal -> {(Context.GLOBAL)}
            UrlProject -> {(Project.TWO, *), (Project.THREE, *)}
            UrlType ->  {(*, IssueType.BUG)}
        */

        assertUrlField("urlglobal", Context.GLOBAL, Issue.FOUR2);
        assertUrlField("urlBoth", new Context().addContext(Project.FOUR, IssueType.SUBTASK), Issue.FOUR2);
        assertUrlField("urlProject", new Context().addProjects(Project.TWO, Project.THREE), Issue.TWO1, Issue.THREE1);
        assertUrlField("urlType", new Context().addType(IssueType.BUG), Issue.ONE1);
        assertUrlField("urlComplex", new Context().addType(IssueType.IMPROVEMENT).addProject(Project.THREE), Issue.THREE1);

        //Check an invalid filter.
        assertFilterColumns(10104,
                new Context().addContext(Project.FOUR, IssueType.SUBTASK),
                Issue.FOUR2);

        navigation.login(FRED_USERNAME);
        assertJqlColumns("urlProject = 'http://match.com'", new Context().addProject(Project.TWO), Issue.TWO1);
    }

    public void testReadOnlyField() throws Exception
    {
        /*
            ReadTextBoth -> {(Project.THREE, IssueType.BUG)}
            ReadTextComplex -> {(Project.THREE, *), (Project.TWO, *), (*, IssueType.IMPROVEMENT)}
            ReadTextGlobal ->  {(*, *)}
            ReadTextProject -> {(Project.FOUR, *)}
            ReadTextType -> {(*, IssueType.FEATURE), (*, IssueType.TASK)}
        */
        assertTextField("readtextglobal", Context.GLOBAL, Issue.FOUR1);
        assertTextField("readtextBoth", new Context().addContext(Project.THREE, IssueType.BUG), Issue.THREE2);
        assertTextField("readtextProject", new Context().addProjects(Project.FOUR), Issue.FOUR1);
        assertTextField("readtextType", new Context().addTypes(IssueType.FEATURE, IssueType.TASK), Issue.TWO1);
        assertTextField("readtextComplex", new Context().addType(IssueType.IMPROVEMENT).addProjects(Project.THREE, Project.TWO), Issue.TWO2);

        //Check an invalid filter.
        assertFilterColumns(10105,
                new Context().addType(IssueType.IMPROVEMENT).addProjects(Project.THREE, Project.TWO),
                Issue.TWO2);

        navigation.login(FRED_USERNAME);
        assertJqlColumns("ReadTextComplex ~ 'match'", new Context().addType(IssueType.IMPROVEMENT).addProject(Project.TWO), Issue.TWO2);
    }

    public void testUserPickerField() throws Exception
    {
        /*
            UserBoth -> {(Project.TWO, IssueType.SUBTASK)}
            UserComplex -> {(Project.TWO, IssueType.TASK), (*,IssueType.BUG)}
            UserGlobal -> {(*, *)}
            UserProject -> {(Project.ONE, *), (Project.THREE, *)}
            UserType -> {(*, IssueType.FEATURE)}
        */
        assertUserField("UserGlobal", Context.GLOBAL, Issue.ONE1);
        assertUserField("userBoth", new Context().addContext(Project.TWO, IssueType.SUBTASK), Issue.TWO2);
        assertUserField("userProject", new Context().addProjects(Project.ONE, Project.THREE), Issue.ONE1);
        assertUserField("userType", new Context().addTypes(IssueType.FEATURE), Issue.THREE1);
        assertUserField("userComplex", new Context().addContext(Project.TWO, IssueType.TASK).addType(IssueType.BUG), Issue.ONE1);

        //Check an invalid filter.
        assertFilterColumns(10106,
                new Context().addContext(Project.TWO, IssueType.SUBTASK),
                Issue.ONE1);

        navigation.login(FRED_USERNAME);
        assertJqlColumns("userProject = 'admin'", new Context().addProject(Project.ONE), Issue.ONE1);
    }

    public void testMultiUserPickerField() throws Exception
    {
        /*
            MultiUserBoth -> {(Project.FOUR, IssueType.BUG)}
            MultiUserComplex {(Project.THREE, *), (Project.ONE, IssueType.BUG)}
            MultiUserGlobal -> {(*, *)}
            MultiUserProject -> {(Project.TWO, *), (Project.THREE, *)}
            MultiUserType -> {(*, IssueType.TASK)}
         */
        assertUserField("MultiUserGlobal", Context.GLOBAL, Issue.TWO1, Issue.FOUR3);
        assertUserField("multiuserBoth", new Context().addContext(Project.FOUR, IssueType.BUG), Issue.FOUR3);
        assertUserField("multiuserProject", new Context().addProjects(Project.TWO, Project.THREE), Issue.TWO1);
        assertUserField("multiuserType", new Context().addTypes(IssueType.TASK), Issue.TWO1);
        assertUserField("multiuserComplex", new Context().addContext(Project.ONE, IssueType.BUG).addProject(Project.THREE), Issue.THREE2);

        assertFilterColumns(10107,
                new Context().addContext(Project.FOUR, IssueType.BUG),
                Issue.TWO1, Issue.FOUR3);

        navigation.login(FRED_USERNAME);
        assertJqlColumns("multiuserProject = 'admin'", new Context().addProject(Project.TWO), Issue.TWO1);
    }

    public void testGroupPickerField() throws Exception
    {
        /*
            GroupBoth -> {(Project.THREE, IssueType.BUG)}
            GroupComplex -> {(Project.THREE, *), (Project.ONE, IssueType.BUG)}
            GroupGlobal -> {(*, *)}
            GroupProject -> {(Project.FOUR, *)}
            GroupType -> {(*, IssueType.FEATURE)}
        */

        assertGroupField("GroupGlobal", Context.GLOBAL, Issue.TWO2);
        assertGroupField("GroupBoth", new Context().addContext(Project.THREE, IssueType.BUG), Issue.THREE2);
        assertGroupField("GroupProject", new Context().addProjects(Project.FOUR), Issue.FOUR1);
        assertGroupField("GroupType", new Context().addTypes(IssueType.FEATURE), Issue.THREE1);
        assertGroupField("GroupComplex", new Context().addContext(Project.ONE, IssueType.BUG).addProject(Project.THREE), Issue.THREE2, Issue.ONE1);

        assertFilterColumns(10108,
                new Context().addType(IssueType.FEATURE),
                Issue.THREE1);

        navigation.login(FRED_USERNAME);
        assertJqlColumns("GroupComplex = 'jira-developers'", new Context().addContext(Project.ONE, IssueType.BUG), Issue.ONE1);
    }

    public void testMultiGroupPickerField() throws Exception
    {
        /*
            MultiGroupBoth -> {(Project.THREE, IssueType.BUG)}
            MultiGroupComplex -> {(Project.THREE, *), (Project.ONE, IssueType.BUG)}
            MultiGroupGlobal -> {(*, *)}
            MultiGroupProject -> {(Project.FOUR, *)}
            MultiGroupType -> {(*, IssueType.FEATURE)}
        */

        assertGroupField("MultiGroupGlobal", Context.GLOBAL, Issue.TWO1);
        assertGroupField("MultiGroupBoth", new Context().addContext(Project.THREE, IssueType.BUG), Issue.THREE2);
        assertGroupField("MultiGroupProject", new Context().addProjects(Project.FOUR), Issue.FOUR3);
        assertGroupField("MultiGroupType", new Context().addTypes(IssueType.FEATURE), Issue.THREE1);
        assertGroupField("MultiGroupComplex", new Context().addContext(Project.ONE, IssueType.BUG).addProject(Project.THREE), Issue.ONE1);

        assertFilterColumns(10109,
                new Context().addContext(Project.THREE, IssueType.BUG),
                Issue.TWO1);

        navigation.login(FRED_USERNAME);
        assertJqlColumns("GroupComplex = 'jira-developers'", new Context().addContext(Project.ONE, IssueType.BUG), Issue.ONE1);
    }

    public void testNumberField() throws Exception
    {
        /*
            NumberBoth -> {(Project.FOUR, IssueType.SUBTASK)}
            NumberComplex -> {(Project.THREE, IssueType.FEATURE), (Project.ONE, IssueType.BUG)}
            NumberGlobal -> {(Context.GLOBAL)}
            NumberProject -> {(Project.FOUR, *), (Project.THREE, *)}
            NumberType -> {(*, IssueType.FEATURE)}
         */
        assertNumberField("NumberGlobal", 67, Context.GLOBAL, Issue.THREE1);
        assertNumberField("NumberBoth", 67, new Context().addContext(Project.FOUR, IssueType.SUBTASK), Issue.FOUR2);
        assertNumberField("NumberProject", 67, new Context().addProjects(Project.FOUR, Project.THREE), Issue.THREE1);
        assertNumberField("NumberType", 67, new Context().addTypes(IssueType.FEATURE), Issue.THREE1);
        assertNumberField("NumberComplex", 67, new Context().addContext(Project.THREE, IssueType.FEATURE).addContext(Project.ONE, IssueType.BUG), Issue.THREE1, Issue.ONE1);

        assertFilterColumns(10110,
                new Context().addContext(Project.THREE, IssueType.FEATURE).addContext(Project.ONE, IssueType.BUG),
                Issue.THREE1);

        navigation.login(FRED_USERNAME);
        assertJqlColumns("NumberComplex = 67", new Context().addContext(Project.ONE, IssueType.BUG), Issue.ONE1);
    }

    public void testImportIdField()
    {
        /*
            ImportBoth -> {(Project.TWO, IssueType.TASK)}
            ImportComplex -> {(Project.THREE, IssueType.BUG), (Project.FOUR, *)}
            ImportGlobal -> {(*, *)}
            ImportProject -> {(Project.ONE, *)}
            ImportType -> {(*, IssueType.BUG)}
         */

        assertNumberField("ImportGlobal", 48, Context.GLOBAL, Issue.TWO1);
        assertNumberField("ImportBoth", 48, new Context().addContext(Project.TWO, IssueType.TASK), Issue.TWO1);
        assertNumberField("ImportProject", 48, new Context().addProjects(Project.ONE), Issue.ONE1);
        assertNumberField("ImportType", 48, new Context().addTypes(IssueType.BUG), Issue.ONE1);
        assertNumberField("ImportComplex", 48, new Context().addContext(Project.THREE, IssueType.BUG).addProject(Project.FOUR), Issue.FOUR3);

        assertFilterColumns(10111,
                new Context().addProject(Project.ONE),
                Issue.TWO1);

        navigation.login(FRED_USERNAME);
        assertJqlColumns("ImportComplex = 48", new Context().addProject(Project.FOUR), Issue.FOUR3);

    }

    public void testProjectPicker() throws Exception
    {
        /*
            ProjectBoth -> {(Project.THREE, IssueType.FEATURE)}
            ProjectComplex -> {(Project.TWO, IssueType.SUBTASK), (Project.FOUR, *)}
            ProjectGlobal -> {(*, *)}
            ProjectProject -> {(Project.THREE, *), (Project.ONE, *)}
            ProjectType -> {(*, IssueType.SUBTASK)}
        */

        assertProjectPicker("projectglobal", Context.GLOBAL, Issue.THREE1);
        assertProjectPicker("projectBoth", new Context().addContext(Project.THREE, IssueType.FEATURE), Issue.THREE1);
        assertProjectPicker("ProjectProject", new Context().addProjects(Project.ONE, Project.THREE), Issue.THREE1, Issue.ONE1);
        assertProjectPicker("PROJECTType", new Context().addTypes(IssueType.SUBTASK), Issue.TWO2);
        assertProjectPicker("PROJectComplex", new Context().addContext(Project.TWO, IssueType.SUBTASK).addProject(Project.FOUR), Issue.TWO2);

        assertFilterColumns(10112,
                new Context().addContext(Project.THREE, IssueType.FEATURE),
                Issue.THREE1);

        navigation.login(FRED_USERNAME);
        assertJqlColumns("ProjectProject = one", new Context().addProject(Project.ONE), Issue.ONE1);
    }

    public void testSingleVersionPicker() throws Exception
    {
        /*
            SingleVersionGlobal -> {(*, *)}
            SingleVersionProject -> {(Three, *), (One, *)}
            SingleVersionType -> {(*, BUG)}
            SingleVersionBoth ->{(Project.THREE, IssueType.BUG)}
            SingleVersionComplex -> {(Project.TWO, *), (*, IssueType.SUBTASK)}
         */

        //Test an invalid filter.
        assertFilterColumns(10113,
                new Context().addType(IssueType.BUG),
                Issue.TWO2, Issue.FOUR3);

        assertSingleVersionPicker("SingleVersionGlobal", Context.GLOBAL);
        assertSingleVersionPicker("SingleVersionProject", new Context().addProjects(Project.ONE, Project.THREE));
        assertSingleVersionPicker("SingleVersionType", new Context().addType(IssueType.BUG));
        assertSingleVersionPicker("SingleVersionBoth", new Context().addContext(Project.THREE, IssueType.BUG));
        assertSingleVersionPicker("SingleVersionComplex", new Context().addProject(Project.TWO).addType(IssueType.SUBTASK));

        navigation.login(FRED_USERNAME);
        assertJqlColumns("singleversionproject = one", new Context().addProject(Project.ONE), Issue.ONE1);
    }

    public void testMultipleVersionPicker()
    {
        /*
            MultiVersionBoth -> {(Project.THREE, IssueType.FEATURE)}
            MultiVersionComplex -> {(Project.ONE, IssueType.BUG), (*, IssueType.IMPROVEMENT)}
            MultiVersionGlobal -> {(*, *)}
            MultiVersionProject -> {(Project.ONE, *), (Project.TWO, *), (Project.FOUR, *)}
            MultiVersionType ->, {(* ,ssueType.BUG)}
        */

        //Test an invalid filter.
        assertFilterColumns(10120,
                Field.MULTI_VERSION_COMPLEX.getFieldContext(),
                Issue.THREE1);

        assertMultiVersionField(Field.MULTI_VERSION_GLOBAL.getFieldContext(), Field.MULTI_VERSION_GLOBAL.getFieldName());
        assertMultiVersionField(Field.MULTI_VERSION_BOTH.getFieldContext(), Field.MULTI_VERSION_BOTH.getFieldName());
        assertMultiVersionField(Field.MULTI_VERSION_PROJECT.getFieldContext(), Field.MULTI_VERSION_PROJECT.getFieldName());
        assertMultiVersionField(Field.MULTI_VERSION_TYPE.getFieldContext(), Field.MULTI_VERSION_TYPE.getFieldName());
        assertMultiVersionField(Field.MULTI_VERSION_COMPLEX.getFieldContext(), Field.MULTI_VERSION_COMPLEX.getFieldName());
    }

    public void testSelectSingleOption()
    {
        assertFilterColumns(10130,
                Field.SELECT_LIST_COMPLEX.getFieldContext(),
                Issue.THREE1);

        assertCustomFieldOption("SelectList");
    }

    public void testRadioOption()
    {
        assertFilterColumns(10131,
                Field.RADIO_COMPLEX.getFieldContext(),
                Issue.THREE1);

        assertCustomFieldOption("Radio");
    }

    public void testCheckboxOption()
    {
        assertFilterColumns(10132,
                Field.CHECKBOX_COMPLEX.getFieldContext(),
                Issue.THREE1);

        assertCustomFieldOption("Checkbox");
    }

    public void testMultiSelectOption()
    {
        assertFilterColumns(10133,
                Field.MULTI_SELECT_COMPLEX.getFieldContext(),
                Issue.THREE1);

        assertCustomFieldOption("Multiselect");
    }

    public void testCascadingSelect() throws Exception
    {
        /*
            CascasingSelectComplex = { (Project.FOUR, IssueType.SUBTASK){one}, (Project.THREE, *){[one, oneone], [one, onetwo], two}, (Project.TWO, *){[one, oneone], [one, onetwo], two}
         */

        assertJqlColumns("CascasingSelectComplex = one",
                Field.CASCADING_SELECT_COMPLEX.getFieldContext(),
                Issue.TWO2, Issue.TWO1, Issue.FOUR2);
        assertJqlColumns("CascasingSelectComplex = onetwo",
                new Context().addProjects(Project.THREE, Project.TWO),
                Issue.TWO1);
        assertJqlColumns("CascasingSelectComplex = two",
                new Context().addProjects(Project.THREE, Project.TWO),
                Issue.THREE2);

        assertJqlColumns("CascasingSelectComplex != one",
                new Context().addProjects(Project.THREE, Project.TWO),
                Issue.THREE2);
        assertJqlColumns("CascasingSelectComplex != onetwo",
                Field.CASCADING_SELECT_COMPLEX.getFieldContext(),
                Issue.TWO2, Issue.THREE2, Issue.FOUR2);
        assertJqlColumns("CascasingSelectComplex != oneone",
                Field.CASCADING_SELECT_COMPLEX.getFieldContext(),
                Issue.TWO2, Issue.TWO1, Issue.THREE2, Issue.FOUR2);
        assertJqlColumns("CascasingSelectComplex != two",
                Field.CASCADING_SELECT_COMPLEX.getFieldContext(),
                Issue.TWO2, Issue.TWO1, Issue.FOUR2);

        assertJqlColumns("CascasingSelectComplex in (one, two)",
                Field.CASCADING_SELECT_COMPLEX.getFieldContext(),
                Issue.TWO2, Issue.TWO1, Issue.THREE2, Issue.FOUR2);
        assertJqlColumns("CascasingSelectComplex = one or  CascasingSelectComplex = two",
                Field.CASCADING_SELECT_COMPLEX.getFieldContext(),
                Issue.TWO2, Issue.TWO1, Issue.THREE2, Issue.FOUR2);

        assertJqlColumns("CascasingSelectComplex not in (two, oneone)",
                Field.CASCADING_SELECT_COMPLEX.getFieldContext(),
                Issue.TWO2, Issue.TWO1, Issue.FOUR2);
        assertJqlColumns("CascasingSelectComplex != two and not CascasingSelectComplex = oneone",
                Field.CASCADING_SELECT_COMPLEX.getFieldContext(),
                Issue.TWO2, Issue.TWO1, Issue.FOUR2);

        assertJqlColumns("CascasingSelectCOMPlex is empty",
                Field.CASCADING_SELECT_COMPLEX.getFieldContext(),
                Issue.THREE1);
        assertJqlColumns("CascasingSelectCOMPlex = empty",
                Field.CASCADING_SELECT_COMPLEX.getFieldContext(),
                Issue.THREE1);
        assertJqlColumns("CascasingSelectCOMPlex in (empty)",
                Field.CASCADING_SELECT_COMPLEX.getFieldContext(),
                Issue.THREE1);
        assertJqlColumns("CascasingSelectCOMPlex in (empty, oneone)",
                new Context().addProjects(Project.TWO, Project.THREE),
                Issue.THREE1);
        assertJqlColumns("CascasingSelectCOMPlex is empty or CascasingSelectCOMPlex = oneone",
                Field.CASCADING_SELECT_COMPLEX.getFieldContext(),
                Issue.THREE1);
        assertJqlColumns("CascasingSelectCOMPlex in (empty, onetwo)",
                new Context().addProjects(Project.TWO, Project.THREE),
                Issue.TWO1, Issue.THREE1);
        assertJqlColumns("CascasingSelectCOMPlex = empty or CascasingSelectCOMPlex = onetwo",
                Field.CASCADING_SELECT_COMPLEX.getFieldContext(),
                Issue.TWO1, Issue.THREE1);

        assertJqlColumns("CascasingSelectCOMPlex is not empty",
                Field.CASCADING_SELECT_COMPLEX.getFieldContext(),
                Issue.TWO2, Issue.TWO1, Issue.THREE2, Issue.FOUR2);
        assertJqlColumns("CascasingSelectCOMPlex != empty",
                Field.CASCADING_SELECT_COMPLEX.getFieldContext(),
                Issue.TWO2, Issue.TWO1, Issue.THREE2, Issue.FOUR2);
        assertJqlColumns("CascasingSelectCOMPlex not in (empty)",
                Field.CASCADING_SELECT_COMPLEX.getFieldContext(),
                Issue.TWO2, Issue.TWO1, Issue.THREE2, Issue.FOUR2);
        assertJqlColumns("CascasingSelectCOMPlex not in (empty, one)",
                new Context().addProjects(Project.TWO, Project.THREE),
                Issue.THREE2);
        assertJqlColumns("CascasingSelectCOMPlex != empty and CascasingSelectCOMPlex != one",
                new Context().addProjects(Project.TWO, Project.THREE),
                Issue.THREE2);

        /*
            CascadingSelectProject = { (two, *) {[two, two]}, (three,*) {[three, two]}, (four, *) {[four, one], [four, two]}, (one, *){one}}
         */
        assertJqlColumns("CascadingSelectProject = one",
                new Context().addProjects(Project.ONE, Project.FOUR),
                Issue.ONE1);
        assertJqlColumns("CascadingSelectProject = two",
                new Context().addProjects(Project.TWO, Project.THREE, Project.FOUR),
                Issue.TWO2, Issue.TWO1, Issue.THREE1, Issue.FOUR2);
        assertJqlColumns("CascadingSelectProject = three",
                new Context().addProjects(Project.THREE),
                Issue.THREE2, Issue.THREE1);
        assertJqlColumns("CascadingSelectProject = four",
                new Context().addProjects(Project.FOUR),
                Issue.FOUR2, Issue.FOUR1);

        assertJqlColumns("CascadingSelectProject != one",
                new Context().addProjects(Project.TWO, Project.THREE, Project.FOUR),
                Issue.TWO2, Issue.TWO1, Issue.THREE2, Issue.THREE1, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns("CascadingSelectProject != two",
                new Context().addProjects(Project.ONE, Project.THREE, Project.FOUR),
                Issue.THREE2, Issue.ONE1, Issue.FOUR1);
        assertJqlColumns("CascadingSelectProject != three",
                new Context().addProjects(Project.ONE, Project.TWO, Project.FOUR),
                Issue.TWO2, Issue.TWO1, Issue.ONE1, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns("CascadingSelectProject != four",
                new Context().addProjects(Project.ONE, Project.TWO, Project.THREE),
                Issue.TWO2, Issue.TWO1, Issue.THREE2, Issue.THREE1, Issue.ONE1);

        assertJqlColumns("CascadingSelectProject in (one, three)",
                new Context().addProjects(Project.ONE, Project.THREE, Project.FOUR),
                Issue.THREE2, Issue.THREE1, Issue.ONE1);
        assertJqlColumns("CascadingSelectProject = one or CascadingSelectProject = three",
                new Context().addProjects(Project.ONE, Project.THREE, Project.FOUR),
                Issue.THREE2, Issue.THREE1, Issue.ONE1);
        assertJqlColumns("CascadingSelectProject in (cascadeoption(one), three)",
                new Context().addProjects(Project.ONE, Project.THREE),
                Issue.THREE2, Issue.THREE1, Issue.ONE1);
        assertJqlColumns("CascadingSelectProject in cascadeoption(one) or CascadingSelectProject = three",
                new Context().addProjects(Project.ONE, Project.THREE),
                Issue.THREE2, Issue.THREE1, Issue.ONE1);

        assertJqlColumns("cascadingselectproject not in (one, two)",
                new Context().addProjects(Project.THREE, Project.FOUR),
                Issue.THREE2, Issue.FOUR1);
        assertJqlColumns("not cascadingselectproject = one and cascadingselectproject != two",
                new Context().addProjects(Project.THREE, Project.FOUR),
                Issue.THREE2, Issue.FOUR1);

        assertJqlColumns("cascadingselectproject is empty",
                Field.CASCADING_SELECT_PROJECT.getFieldContext(),
                Issue.FOUR3);
        assertJqlColumns("cascadingselectproject = empty",
                Field.CASCADING_SELECT_PROJECT.getFieldContext(),
                Issue.FOUR3);
        assertJqlColumns("cascadingselectproject in (empty)",
                Field.CASCADING_SELECT_PROJECT.getFieldContext(),
                Issue.FOUR3);
        assertJqlColumns("cascadingselectproject in (empty, cascadeoption(one))",
                new Context().addProjects(Project.ONE),
                Issue.ONE1, Issue.FOUR3);
        assertJqlColumns("cascadingselectproject is empty or cascadingselectproject in       cascadeoption(one)",
                Field.CASCADING_SELECT_PROJECT.getFieldContext(),
                Issue.ONE1, Issue.FOUR3);

        assertJqlColumns("cascadingselectproject is not empty",
                Field.CASCADING_SELECT_PROJECT.getFieldContext(),
                Issue.TWO2, Issue.TWO1, Issue.THREE2, Issue.THREE1, Issue.ONE1, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns("cascadingselectproject != empty",
                Field.CASCADING_SELECT_PROJECT.getFieldContext(),
                Issue.TWO2, Issue.TWO1, Issue.THREE2, Issue.THREE1, Issue.ONE1, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns("cascadingselectproject not in (empty)",
                Field.CASCADING_SELECT_PROJECT.getFieldContext(),
                Issue.TWO2, Issue.TWO1, Issue.THREE2, Issue.THREE1, Issue.ONE1, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns("cascadingselectproject not in (empty, one)",
                new Context().addProjects(Project.TWO, Project.THREE, Project.FOUR),
                Issue.TWO2, Issue.TWO1, Issue.THREE2, Issue.THREE1, Issue.FOUR2, Issue.FOUR1);

        //
        // Test the cascading option function
        //
        assertJqlColumns("CascadingSelectProject IN cascadeoption(four, none)",
                new Context().addProjects(Project.FOUR),
                Issue.FOUR1);
        assertJqlColumns("CascadingSelectProject IN cascadeoption(two, none)",
                new Context().addProjects(Project.TWO),
                Issue.TWO2);
        assertJqlColumns("CascadingSelectProject IN cascadeoption(four, two)",
                new Context().addProjects(Project.FOUR),
                Issue.FOUR2);
        assertJqlColumns("CascadingSelectProject IN cascadeoption(two)",
                new Context().addProjects(Project.TWO),
                Issue.TWO2, Issue.TWO1);
        assertJqlColumns("CascadingSelectProject NOT IN cascadeoption(four, none)",
                new Context().addProjects(Project.values()),
                Issue.TWO2, Issue.TWO1, Issue.THREE2, Issue.THREE1, Issue.ONE1, Issue.FOUR2);
        assertJqlColumns("CascadingSelectProject NOT IN cascadeoption(one, none)",
                new Context().addProjects(Project.TWO, Project.FOUR, Project.THREE),
                Issue.TWO2, Issue.TWO1, Issue.THREE2, Issue.THREE1, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns("CascadingSelectProject NOT IN cascadeoption(four, two)",
                new Context().addProjects(Project.values()),
                Issue.TWO2, Issue.TWO1, Issue.THREE2, Issue.THREE1, Issue.ONE1, Issue.FOUR1);
        assertJqlColumns("CascadingSelectProject NOT IN cascadeoption(two, two)",
                new Context().addProjects(Project.values()),
                Issue.TWO2, Issue.THREE2, Issue.THREE1, Issue.ONE1, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns("CascadingSelectProject NOT IN cascadeoption(two)",
                new Context().addProjects(Project.ONE, Project.THREE, Project.FOUR),
                Issue.THREE2, Issue.THREE1, Issue.ONE1, Issue.FOUR2, Issue.FOUR1);

        assertFilterColumns(10150, Field.CASCADING_SELECT_COMPLEX.getFieldContext(), Issue.THREE1);

        navigation.login(FRED_USERNAME);
        assertJqlColumns("CascadingSelectProject != one",
                new Context().addProjects(Project.TWO, Project.FOUR),
                Issue.TWO2, Issue.TWO1);
    }

    public void testInvalidField() throws Exception
    {
        assertFilterColumns(10161, new Context().addProjects(Project.THREE), Issue.THREE1);
        assertFilterColumns(10160, new Context().addProjects(Project.THREE, Project.TWO), Issue.TWO2, Issue.TWO1, Issue.THREE1);

        navigation.login(FRED_USERNAME);
        assertFilterColumns(10160, new Context().addProjects(Project.TWO), Issue.TWO2, Issue.TWO1);
    }

    //
    // Test for the logical operator combining rules.
    //

    public void testLogicalOperatorsAndContext() throws Exception
    {
        /*
            All the combinations of context.

            (*, *) OP (*, *)
            (P, *) OP (*, *)
            (*, T) OP (*, *)
            (P, T) OP (*, *)

            (*, *) OP (P, *)
            (P, *) OP (P, *)
            (*, T) OP (P, *)
            (P, T) OP (P, *)

            (*, *) OP (*, T)
            (P, *) OP (*, T)
            (*, T) OP (*, T)
            (P, T) OP (*, T)

            (*, *) OP (P, T)
            (P, *) OP (P, T)
            (*, T) OP (P, T)
            (P, T) OP (P, T)

         */

        //
        // Test the "AND" operator.
        //

        //(*, *) AND (*, *)
        assertJqlColumns("summary ~ suns and comment ~ suns order by key desc", Context.GLOBAL, Issue.THREE1);
        //(P, *) AND (*, *)
        assertJqlColumns("project = three and summary ~ suns order by key desc", new Context().addProject(Project.THREE), Issue.THREE1);
        //(*, T) AND (*, *)
        assertJqlColumns("type = 'new feature' and summary ~ suns order by key desc", new Context().addType(IssueType.FEATURE), Issue.THREE1);
        //(P, T) AND (*, *)
        assertJqlColumns("key = 'three-1' and summary ~ suns order by key desc", new Context().addContext(Project.THREE, IssueType.FEATURE), Issue.THREE1);

        //(*, *) AND (P, *)
        assertJqlColumns("summary ~ suns and project = three order by key desc", new Context().addProject(Project.THREE), Issue.THREE1);
        //(*, T) AND (P, *)
        assertJqlColumns("type = 'task' and project = two", new Context().addContext(Project.TWO, IssueType.TASK), Issue.TWO1);
        //(P, T) AND (P, *)
        assertJqlColumns("key = one-1 and project = one", new Context().addContext(Project.ONE, IssueType.BUG), Issue.ONE1);

        //(*, *) AND (*, T)
        assertJqlColumns("summary ~ suns and type = 'new feature' order by key desc", new Context().addType(IssueType.FEATURE), Issue.THREE1);
        //(P, *) AND (*, T)
        assertJqlColumns("project = two and type = 'task'", new Context().addContext(Project.TWO, IssueType.TASK), Issue.TWO1);
        //(P, T) AND (*, T)
        assertJqlColumns("key = one-1 and type = 'bug'", new Context().addContext(Project.ONE, IssueType.BUG), Issue.ONE1);

        //(*, *) AND (P, T)
        assertJqlColumns("summary ~ suns and key = three-1 order by key desc", new Context().addContext(Project.THREE, IssueType.FEATURE), Issue.THREE1);
        //(P, *) AND (P, T)
        assertJqlColumns("project = two and key = two-1", new Context().addContext(Project.TWO, IssueType.TASK), Issue.TWO1);
        //(*, T) AND (P, T)
        assertJqlColumns("type = task and key = two-1", new Context().addContext(Project.TWO, IssueType.TASK), Issue.TWO1);
        //(P, T) AND (P, T)
        assertJqlColumns("key = two-1 and key = two-1", new Context().addContext(Project.TWO, IssueType.TASK), Issue.TWO1);

        //
        //Test the "OR" operator.
        //

        //(*, *) OR (*, *)
        assertJqlColumns("summary ~ suns or comment ~ suns order by key desc", Context.GLOBAL, Issue.THREE1);
        //(P, *) OR (*, *)
        assertJqlColumns("project = three or summary ~ suns order by key desc", new Context().addProject(Project.THREE), Issue.THREE2, Issue.THREE1);
        //(*, T) OR (*, *)
        assertJqlColumns("type = 'new feature' or summary ~ suns order by key desc", new Context().addType(IssueType.FEATURE), Issue.THREE1);
        //(P, T) OR (*, *)
        assertJqlColumns("key = 'three-1' or summary ~ suns order by key desc", new Context().addContext(Project.THREE, IssueType.FEATURE), Issue.THREE1);

        //(*, *) OR (P, *)
        assertJqlColumns("summary ~ suns or project = three order by key desc", new Context().addProject(Project.THREE), Issue.THREE2, Issue.THREE1);
        //(P, *) OR (P, *)
        assertJqlColumns("project = four or category = cattwo", new Context().addProject(Project.FOUR), Issue.FOUR3, Issue.FOUR2, Issue.FOUR1);
        //(*, T) OR (P, *)
        assertJqlColumns("type = 'task' or project = two", new Context().addType(IssueType.TASK).addProject(Project.TWO), Issue.TWO2, Issue.TWO1);
        //(P, T) OR (P, *)
        assertJqlColumns("key = one-1 or project = one", new Context().addContext(Project.ONE, IssueType.BUG).addProject(Project.ONE), Issue.ONE1);

        //(*, *) OR (*, T)
        assertJqlColumns("summary ~ suns or type = 'new feature' order by key desc", new Context().addType(IssueType.FEATURE), Issue.THREE1);
        //(P, *) OR (*, T)
        assertJqlColumns("project = two or type = 'task'", new Context().addProject(Project.TWO).addType(IssueType.TASK), Issue.TWO2, Issue.TWO1);
        //(*, T), (*, T)
        assertJqlColumns("type = task or type = 'bug'", new Context().addTypes(IssueType.TASK, IssueType.BUG), Issue.TWO1, Issue.THREE2, Issue.ONE1, Issue.FOUR3);
        //(P, T) OR (*, T)
        assertJqlColumns("key = one-1 or type = 'task'", new Context().addContext(Project.ONE, IssueType.BUG).addType(IssueType.TASK), Issue.TWO1, Issue.ONE1);

        //(*, *) OR (P, T)
        assertJqlColumns("summary ~ suns or key = three-1 order by key desc", new Context().addContext(Project.THREE, IssueType.FEATURE), Issue.THREE1);
        //(P, *) OR (P, T)
        assertJqlColumns("project = two or key = two-1", new Context().addContext(Project.TWO, IssueType.TASK).addProject(Project.TWO), Issue.TWO2, Issue.TWO1);
        //(*, T) OR (P, T)
        assertJqlColumns("type = task or key = two-1", new Context().addContext(Project.TWO, IssueType.TASK).addType(IssueType.TASK), Issue.TWO1);
        //(P, T) OR (P, T)
        assertJqlColumns("key = two-1 or key = one-1", new Context().addContext(Project.TWO, IssueType.TASK).addContext(Project.ONE, IssueType.BUG), Issue.TWO1, Issue.ONE1);

        //
        //Lets test some logic rules.
        //

        // a and b or c and d = (a and b) or (c and d) = b and a or c and d = d and c or a and b
        Context ctx = new Context().addContext(Project.ONE, IssueType.BUG).addContext(Project.TWO, IssueType.TASK);
        List<Issue> issues = Arrays.asList(Issue.TWO1, Issue.ONE1);
        assertJqlColumns("project = one and type = bug or type = task and project = two", ctx, issues);
        assertJqlColumns("(project = one and type = bug) or (type = task and project = two)", ctx, issues);
        assertJqlColumns("type = bug and project = one or type = task and project = two", ctx, issues);
        assertJqlColumns("(project = two and type = task  ) or (project = one and type = bug)", ctx, issues);

        // a and (b or c) = a and b or a and c
        ctx = new Context().addContext(Project.ONE, IssueType.BUG).addContext(Project.THREE, IssueType.BUG);
        issues = Arrays.asList(Issue.THREE2, Issue.ONE1);
        assertJqlColumns("type = bug and (project = one or key >= three-1)", ctx, issues);
        assertJqlColumns("type = bug and project = one or type = bug and issue >= three-1", ctx, issues);

        //
        //Some complex examples.
        //
        assertJqlColumns("project = one and type = bug or type = task", new Context().addContext(Project.ONE, IssueType.BUG).addType(IssueType.TASK), Issue.TWO1, Issue.ONE1);
        assertJqlColumns("project = one and type = bug or type = task and project = two", new Context().addContext(Project.ONE, IssueType.BUG).addContext(Project.TWO, IssueType.TASK), Issue.TWO1, Issue.ONE1);
        assertJqlColumns("project in (one, three) and type = bug or type = \"New Feature\"", new Context().addContext(Project.ONE, IssueType.BUG).addContext(Project.THREE, IssueType.BUG).addType(IssueType.FEATURE), Issue.THREE2, Issue.THREE1, Issue.ONE1);

        Set<IssueType> types = getIssueTypesAndRemove(IssueType.BUG);
        ctx = new Context().addContexts(Arrays.asList(Project.ONE, Project.THREE), types);
        assertJqlColumns("project in (one, three) and type != 'bug'", ctx, Issue.THREE1);

        types = EnumSet.of(IssueType.FEATURE, IssueType.IMPROVEMENT, IssueType.TASK);
        ctx = new Context().addContexts(Arrays.asList(Project.ONE, Project.THREE), types);
        assertJqlColumns("project in (one, three) and type in ('new feature', Improvement, task)", ctx, Issue.THREE1);

        types = getIssueTypesAndRemove(IssueType.BUG);
        ctx = new Context().addContexts(Arrays.asList(Project.ONE, Project.TWO), types);
        assertJqlColumns("project in (one, two) and type not in (bug)", ctx, Issue.TWO2, Issue.TWO1);

        ctx = new Context().addContexts(getProjectsAndRemove(Project.ONE), getIssueTypesAndRemove(IssueType.BUG));
        assertJqlColumns("project != one and type != bug", ctx, Issue.TWO2, Issue.TWO1, Issue.THREE1, Issue.FOUR2, Issue.FOUR1);

        //Fred can't see project three.
        navigation.login(FRED_USERNAME);
        ctx = new Context().addContexts(getProjectsAndRemove(Project.ONE, Project.THREE), getIssueTypesAndRemove(IssueType.BUG));
        assertJqlColumns("project != one and type != bug", ctx, Issue.TWO2, Issue.TWO1);
    }

    private void assertTextField(String fieldName, Context context, Issue... issues)
    {
        assertJqlColumns(String.format("%s ~ 'match' order by key desc", fieldName), context, issues);

        final Collection<Issue> contextIssues = getIssuesInContext(context);
        contextIssues.removeAll(Arrays.asList(issues));
        if (!contextIssues.isEmpty())
        {
            assertJqlColumns(String.format("%s ~ empty order by key desc", fieldName), context, contextIssues);
            assertJqlColumns(String.format("%s is empty order by key desc", fieldName), context, contextIssues);
            assertJqlColumns(String.format("%s ~ null order by key desc", fieldName), context, contextIssues);
        }

        assertJqlColumns(String.format("%s !~ empty order by key desc", fieldName), context, issues);
        assertJqlColumns(String.format("%s is not empty order by key desc", fieldName), context, issues);
        assertJqlColumns(String.format("%s is not null order by key desc", fieldName), context, issues);
    }

    private void assertProjectPicker(String fieldName, Context context, Issue... issues)
    {
        assertStringEqualsField(fieldName, "one", "two", context, issues);
    }

    private void assertUserField(String fieldName, Context context, Issue... issues)
    {
        assertStringEqualsField(fieldName, ADMIN_USERNAME, FRED_USERNAME, context, issues);
    }

    private void assertGroupField(String fieldName, Context context, Issue... issues)
    {
        assertStringEqualsField(fieldName, "jira-developers", "jira-administrators", context, issues);
    }

    private void assertUrlField(String fieldName, Context context, Issue... issues)
    {
        assertStringEqualsField(fieldName, "http://match.com", "other", context, issues);
    }

    private void assertStringEqualsField(String fieldName, String match, String notMatch, Context context, Issue... issues)
    {
        assertJqlColumns(String.format("%s = '%s'", fieldName, match), context, issues);
        assertJqlColumns(String.format("%1$s in ('%2$s', '%3$s')", fieldName, match, notMatch), context, issues);
        assertJqlColumns(String.format("%1$s = '%2$s' or %1$s = '%3$s'", fieldName, match, notMatch), context, issues);

        assertJqlColumns(String.format("%s != '%s'", fieldName, notMatch), context, issues);
        assertJqlColumns(String.format("%1$s not in ('%2$s')", fieldName, notMatch), context, issues);

        final Collection<Issue> contextIssues = getIssuesInContext(context);
        contextIssues.removeAll(Arrays.asList(issues));
        if (!contextIssues.isEmpty())
        {
            assertJqlColumns(String.format("%s is empty", fieldName), context, contextIssues);
            assertJqlColumns(String.format("%s = empty", fieldName), context, contextIssues);
            assertJqlColumns(String.format("%s = null", fieldName), context, contextIssues);
        }

        assertJqlColumns(String.format("%s != empty", fieldName), context, issues);
        assertJqlColumns(String.format("%s is not empty", fieldName), context, issues);
        assertJqlColumns(String.format("%s is not null", fieldName), context, issues);
    }

    private List<Issue> getIssuesInContext(Context context)
    {
        return getIssuesInContext(Issue.ALL_ISSUES, context);
    }

    private List<Issue> getIssuesInContext(Collection<Issue> input, Context ctx)
    {
        List<Issue> issues = new ArrayList<Issue>();
        for (Issue issue : input)
        {
            if (issue.inContext(ctx))
            {
                issues.add(issue);
            }
        }
        return issues;
    }

    private void assertCustomFieldOption(String fieldName)
    {
        /*
            $fieldName$ListComplex -> {(Project.TWO, IssueType.TASK)[one], (Project.ONE, *)[one, two, global], (Project.THREE, *)[one, two, global]}
            $fieldName$ComplexGlobal -> {(Project.TREE, *)[two, three, global], (*,*)[global]}
            $fieldName$Type -> {(*, IssueType.IMPROVEMENT)[one]}.
         */

        assertJqlColumns(String.format("%scomplex = one", fieldName), new Context().addContext(Project.TWO, IssueType.TASK).addProjects(Project.ONE, Project.THREE), Issue.TWO1, Issue.ONE1);
        assertJqlColumns(String.format("%scomplex = two", fieldName), new Context().addProjects(Project.ONE, Project.THREE), Issue.THREE1);
        assertJqlColumns(String.format("%sComplex = global", fieldName), new Context().addProjects(Project.ONE, Project.THREE), Issue.THREE2);
        assertJqlColumns(String.format("%sType = one", fieldName), new Context().addType(IssueType.IMPROVEMENT), Issue.FOUR1);
        assertJqlColumns(String.format("%sProjectGlobal = two", fieldName), new Context().addProjects(Project.THREE), Issue.THREE1);
        assertJqlColumns(String.format("%sProjectGlobal = three", fieldName), new Context().addProjects(Project.THREE), Issue.THREE2);
        assertJqlColumns(String.format("%sProjectGlobal = global", fieldName), Context.GLOBAL, Issue.TWO2);

        assertJqlColumns(String.format("%scomplex != one", fieldName), new Context().addProjects(Project.THREE, Project.ONE), Issue.THREE2, Issue.THREE1);
        assertJqlColumns(String.format("%scomplex != two", fieldName), new Context().addContext(Project.TWO, IssueType.TASK).addProjects(Project.ONE, Project.THREE), Issue.TWO1, Issue.THREE2, Issue.ONE1);
        assertJqlColumns(String.format("%sProjectGlobal != global", fieldName), new Context().addProjects(Project.THREE), Issue.THREE2, Issue.THREE1);
        assertJqlColumns(String.format("%sProjectGlobal != three", fieldName), Context.GLOBAL, Issue.TWO2, Issue.THREE1);
        assertJqlColumns(String.format("%sProjectGlobal != two", fieldName), Context.GLOBAL, Issue.TWO2, Issue.THREE2);

        assertJqlColumnsForIn(String.format("%sProjectGlobal", fieldName), Arrays.asList("two", "global"), new Context().addProjects(Project.THREE), Issue.TWO2, Issue.THREE1);

        assertJqlColumnsForNotIn(String.format("%scomplex", fieldName), Arrays.asList("one", "two"), new Context().addProjects(Project.ONE, Project.THREE), Issue.THREE2);
        assertJqlColumnsForNotIn(String.format("%sProjectGlobal", fieldName), Arrays.asList("global", "two"), new Context().addProjects(Project.THREE), Issue.THREE2);

        assertJqlColumns(String.format("%sProjectGlobal is empty", fieldName), Context.GLOBAL, Issue.TWO1, Issue.ONE1, Issue.FOUR3, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns(String.format("%sProjectGlobal = empty", fieldName), Context.GLOBAL, Issue.TWO1, Issue.ONE1, Issue.FOUR3, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns(String.format("%sProjectGlobal in (empty)", fieldName), Context.GLOBAL, Issue.TWO1, Issue.ONE1, Issue.FOUR3, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumnsForIn(String.format("%sProjectGlobal", fieldName), Arrays.asList("empty", "two"), new Context().addProject(Project.THREE), Issue.TWO1, Issue.THREE1, Issue.ONE1, Issue.FOUR3, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumnsForIn(String.format("%scomplex", fieldName), Arrays.asList("empty", "global"), new Context().addContext(Project.TWO, IssueType.TASK).addProjects(Project.ONE, Project.THREE), Issue.THREE2);

        assertJqlColumns(String.format("%sProjectGlobal is not empty", fieldName), Context.GLOBAL, Issue.TWO2, Issue.THREE2, Issue.THREE1);
        assertJqlColumns(String.format("%sProjectGlobal != empty", fieldName), Context.GLOBAL, Issue.TWO2, Issue.THREE2, Issue.THREE1);
        assertJqlColumns(String.format("%sProjectGlobal not in (empty)", fieldName), Context.GLOBAL, Issue.TWO2, Issue.THREE2, Issue.THREE1);
        assertJqlColumnsForNotIn(String.format("%sProjectGlobal", fieldName), Arrays.asList("empty", "global"), new Context().addProject(Project.THREE), Issue.THREE2, Issue.THREE1);
        assertJqlColumnsForNotIn(String.format("%sProjectGlobal", fieldName), Arrays.asList("empty", "two"), Context.GLOBAL, Issue.TWO2, Issue.THREE2);
        assertJqlColumnsForNotIn(String.format("%scomplex", fieldName), Arrays.asList("empty", "global"), new Context().addContext(Project.TWO, IssueType.TASK).addProjects(Project.ONE, Project.THREE), Issue.TWO1, Issue.THREE1, Issue.ONE1);
        assertJqlColumnsForNotIn(String.format("%scomplex", fieldName), Arrays.asList("empty", "one"), new Context().addProjects(Project.ONE, Project.THREE), Issue.THREE2, Issue.THREE1);

        navigation.login(FRED_USERNAME);
        assertJqlColumns(String.format("%scomplex != two", fieldName), new Context().addContext(Project.TWO, IssueType.TASK).addProjects(Project.ONE), Issue.TWO1, Issue.ONE1);
    }

    private void assertSystemVersionField(String fieldName, long badFilterId)
    {
        //Check an invalid filter.
        assertFilterColumns(badFilterId, new Context().addProjects(Project.TWO, Project.THREE, Project.ONE), Issue.THREE2, Issue.ONE1);

        //Check global context.
        assertMultiVersionField(Context.GLOBAL, fieldName);
    }

    private void assertMultiVersionField(Context context, String fieldName)
    {
        /*
            In the test data the versions map like:

            proj:one -> version:{one, two, three}
            proj:two -> version:{two, twoonly}
            proj:three -> version:{three, threeonly, one}
            proj:four -> version:{fouronly}

         */

        assertJqlColumnsContext(context, String.format("%s = twoonly", fieldName), new Context().addProject(Project.TWO), Issue.TWO1);
        assertJqlColumnsContext(context, String.format("%s = two", fieldName), new Context().addProjects(Project.TWO, Project.ONE), Issue.TWO1, Issue.ONE1);

        assertJqlColumnsContext(context, String.format("%s != two", fieldName), new Context().addProjects(Project.ONE, Project.TWO, Project.THREE, Project.FOUR), Issue.THREE2, Issue.THREE1);
        assertJqlColumnsContext(context, String.format("%s != fouronly", fieldName), new Context().addProjects(Project.ONE, Project.TWO, Project.THREE), Issue.TWO1, Issue.THREE2, Issue.THREE1, Issue.ONE1);

        assertJqlColumnsContext(context, String.format("%s in (fouronly, three)", fieldName), new Context().addProjects(Project.ONE, Project.FOUR, Project.THREE), Issue.THREE2, Issue.ONE1);
        assertJqlColumnsContext(context, String.format("%1$s = fouronly or %1$s = three", fieldName), new Context().addProjects(Project.ONE, Project.FOUR, Project.THREE), Issue.THREE2, Issue.ONE1);
        assertJqlColumnsContext(context, String.format("%s in (fouronly, threeonly)", fieldName), new Context().addProjects(Project.FOUR, Project.THREE), Issue.THREE1);
        assertJqlColumnsContext(context, String.format("(%1$s  = fouronly or %1$s = threeonly)", fieldName), new Context().addProjects(Project.FOUR, Project.THREE), Issue.THREE1);

        assertJqlColumnsContext(context, String.format("%s not in (fouronly, threeonly)", fieldName), new Context().addProjects(Project.THREE, Project.ONE, Project.TWO), Issue.TWO1, Issue.THREE2, Issue.ONE1);
        assertJqlColumnsContext(context, String.format("not(%1$s = fouronly or %1$s = threeonly)", fieldName), new Context().addProjects(Project.THREE, Project.ONE, Project.TWO), Issue.TWO1, Issue.THREE2, Issue.ONE1);
        assertJqlColumnsContext(context, String.format("%s not in (one, two, twoonly)", fieldName), new Context().addProjects(Project.THREE, Project.FOUR, Project.ONE, Project.TWO), Issue.THREE1);
        assertJqlColumnsContext(context, String.format("not %1$s = one and %1$s != two and %1$s != twoonly", fieldName), new Context().addProjects(Project.THREE, Project.FOUR, Project.ONE, Project.TWO), Issue.THREE1);

        assertJqlColumnsContext(context, String.format("%s is empty", fieldName), Context.GLOBAL, Issue.TWO2, Issue.FOUR3, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumnsContext(context, String.format("%s = empty", fieldName), Context.GLOBAL, Issue.TWO2, Issue.FOUR3, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumnsContext(context, String.format("%s in (empty, two)", fieldName), new Context().addContext(Context.GLOBAL).addProjects(Project.ONE, Project.TWO), Issue.TWO2, Issue.TWO1, Issue.ONE1, Issue.FOUR3, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumnsContext(context, String.format("%1$s = empty or %1$s = two", fieldName), new Context().addContext(Context.GLOBAL).addProjects(Project.ONE, Project.TWO), Issue.TWO2, Issue.TWO1, Issue.ONE1, Issue.FOUR3, Issue.FOUR2, Issue.FOUR1);

        assertJqlColumnsContext(context, String.format("%s is not empty", fieldName), Context.GLOBAL, Issue.TWO1, Issue.THREE2, Issue.THREE1, Issue.ONE1);
        assertJqlColumnsContext(context, String.format("%s != null", fieldName), Context.GLOBAL, Issue.TWO1, Issue.THREE2, Issue.THREE1, Issue.ONE1);
        assertJqlColumnsContext(context, String.format("%s not in (null, one, two, three)", fieldName), new Context().addProjects(Project.ONE, Project.TWO, Project.THREE, Project.FOUR), Issue.THREE1);
        assertJqlColumnsContext(context, String.format("%1$s not in (null, one) and %1$s != two and not %1$s = three", fieldName), new Context().addProjects(Project.ONE, Project.TWO, Project.THREE, Project.FOUR), Issue.THREE1);

        assertJqlColumnsContext(context, String.format("%s > one", fieldName), new Context().addProject(Project.ONE), Issue.ONE1);
        assertJqlColumnsContext(context, String.format("%s > two", fieldName), new Context().addProjects(Project.ONE, Project.TWO), Issue.TWO1, Issue.ONE1);
        assertJqlColumnsContext(context, String.format("%s > three", fieldName), new Context().addProjects(Project.THREE), Issue.THREE2, Issue.THREE1);
        assertJqlColumnsContext(context, String.format("%s > threeonly", fieldName), new Context().addProjects(Project.THREE), Issue.THREE2);
        assertJqlColumnsContext(context, String.format("%s < one", fieldName), new Context().addProjects(Project.THREE), Issue.THREE2, Issue.THREE1);
        assertJqlColumnsContext(context, String.format("%s < two", fieldName), new Context().addProjects(Project.ONE), Issue.ONE1);
        assertJqlColumnsContext(context, String.format("%s < three", fieldName), new Context().addProjects(Project.ONE), Issue.ONE1);
        assertJqlColumnsContext(context, String.format("%s < twoonly", fieldName), new Context().addProjects(Project.TWO), Issue.TWO1);
        assertJqlColumnsContext(context, String.format("%s < threeonly", fieldName), new Context().addProjects(Project.THREE), Issue.THREE2);

        assertJqlColumnsContext(context, String.format("%s >= one", fieldName), new Context().addProjects(Project.ONE, Project.THREE), Issue.THREE2, Issue.ONE1);
        assertJqlColumnsContext(context, String.format("%s >= two", fieldName), new Context().addProjects(Project.ONE, Project.TWO), Issue.TWO1, Issue.ONE1);
        assertJqlColumnsContext(context, String.format("%s >= three", fieldName), new Context().addProjects(Project.THREE, Project.ONE), Issue.THREE2, Issue.THREE1, Issue.ONE1);
        assertJqlColumnsContext(context, String.format("%s >= twoonly", fieldName), new Context().addProjects(Project.TWO), Issue.TWO1);
        assertJqlColumnsContext(context, String.format("%s >= threeonly", fieldName), new Context().addProjects(Project.THREE), Issue.THREE2, Issue.THREE1);
        assertJqlColumnsContext(context, String.format("%s <= one", fieldName), new Context().addProjects(Project.THREE, Project.ONE), Issue.THREE2, Issue.THREE1, Issue.ONE1);
        assertJqlColumnsContext(context, String.format("%s <= two", fieldName), new Context().addProjects(Project.ONE, Project.TWO), Issue.TWO1, Issue.ONE1);
        assertJqlColumnsContext(context, String.format("%s <= three", fieldName), new Context().addProjects(Project.ONE, Project.THREE), Issue.THREE2, Issue.ONE1);
        assertJqlColumnsContext(context, String.format("%s <= twoonly", fieldName), new Context().addProjects(Project.TWO), Issue.TWO1);
        assertJqlColumnsContext(context, String.format("%s <= threeonly", fieldName), new Context().addProjects(Project.THREE), Issue.THREE2, Issue.THREE1);

        navigation.login(FRED_USERNAME);
        assertJqlColumnsContext(context, String.format("%s >= three", fieldName), new Context().addProject(Project.ONE), Issue.ONE1);
        navigation.login(ADMIN_USERNAME);
    }

    private void assertSingleVersionPicker(String fieldName, Context context) throws Exception
    {
        /*
           ver:one -> three-1, one-1
           ver:two -> two-1
           ver:three -> {}
           ver:twonly -> {}
           ver:threeonly -> three-2
           ver:fouronly -> four-2, four-1
        */
        assertJqlColumnsContext(context, String.format("%s = one", fieldName), new Context().addProjects(Project.ONE, Project.THREE), Issue.THREE1, Issue.ONE1);
        assertJqlColumnsContext(context, String.format("%s = two", fieldName), new Context().addProjects(Project.ONE, Project.TWO), Issue.TWO1);
        assertJqlColumnsContext(context, String.format("%s = threeonly", fieldName), new Context().addProjects(Project.THREE), Issue.THREE2);
        assertJqlColumnsContext(context, String.format("%s = fouronly", fieldName), new Context().addProjects(Project.FOUR), Issue.FOUR2, Issue.FOUR1);

        assertJqlColumnsContext(context, String.format("%s != one", fieldName), new Context().addProjects(Project.values()), Issue.TWO1, Issue.THREE2, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumnsContext(context, String.format("%s != two", fieldName), new Context().addProjects(Project.values()), Issue.THREE2, Issue.THREE1, Issue.ONE1, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumnsContext(context, String.format("%s != three", fieldName), new Context().addProjects(Project.values()), Issue.TWO1, Issue.THREE2, Issue.THREE1, Issue.ONE1, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumnsContext(context, String.format("%s != threeonly", fieldName), new Context().addProjects(Project.values()), Issue.TWO1, Issue.THREE1, Issue.ONE1, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumnsContext(context, String.format("%s != fouronly", fieldName), new Context().addProjects(getProjectsAndRemove(Project.FOUR)), Issue.TWO1, Issue.THREE2, Issue.THREE1, Issue.ONE1);

        assertJqlColumnsContext(context, String.format("%s = empty", fieldName), Context.GLOBAL, Issue.TWO2, Issue.FOUR3);
        assertJqlColumnsContext(context, String.format("%s is empty", fieldName), Context.GLOBAL, Issue.TWO2, Issue.FOUR3);
        assertJqlColumnsContext(context, String.format("%s is empty", fieldName), Context.GLOBAL, Issue.TWO2, Issue.FOUR3);

        assertJqlColumnsForInContext(context, fieldName, Arrays.asList("one", "two"), new Context().addProjects(Project.ONE, Project.TWO, Project.THREE), Issue.TWO1, Issue.THREE1, Issue.ONE1);
        assertJqlColumnsForInContext(context, fieldName, Arrays.asList("one", "two", "threeonly"), new Context().addProjects(Project.ONE, Project.TWO, Project.THREE), Issue.TWO1, Issue.THREE2, Issue.THREE1, Issue.ONE1);
        assertJqlColumnsForInContext(context, fieldName, Arrays.asList("one", "two", "fouronly"), new Context().addProjects(Project.ONE, Project.TWO, Project.THREE, Project.FOUR), Issue.TWO1, Issue.THREE1, Issue.ONE1, Issue.FOUR2, Issue.FOUR1);

        assertJqlColumnsForNotInContext(context, fieldName, Arrays.asList("one", "two"), new Context().addProjects(Project.values()), Issue.THREE2, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumnsForNotInContext(context, fieldName, Arrays.asList("two", "twoonly", "fouronly"), new Context().addProjects(getProjectsAndRemove(Project.FOUR)), Issue.THREE2, Issue.THREE1, Issue.ONE1);

        assertJqlColumnsContext(context, String.format("%s = empty", fieldName), Context.GLOBAL, Issue.TWO2, Issue.FOUR3);
        assertJqlColumnsContext(context, String.format("%s is empty", fieldName), Context.GLOBAL, Issue.TWO2, Issue.FOUR3);
        assertJqlColumnsContext(context, String.format("%s in (empty)", fieldName), Context.GLOBAL, Issue.TWO2, Issue.FOUR3);
        assertJqlColumnsForInContext(context, fieldName, Arrays.asList("one", "empty"), new Context().addProjects(Project.ONE, Project.THREE), Issue.TWO2, Issue.THREE1, Issue.ONE1, Issue.FOUR3);

        assertJqlColumnsContext(context, String.format("%s != empty", fieldName), Context.GLOBAL, Issue.TWO1, Issue.THREE2, Issue.THREE1, Issue.ONE1, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumnsContext(context, String.format("%s is not empty", fieldName), Context.GLOBAL, Issue.TWO1, Issue.THREE2, Issue.THREE1, Issue.ONE1, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumnsContext(context, String.format("%s not in (empty)", fieldName), Context.GLOBAL, Issue.TWO1, Issue.THREE2, Issue.THREE1, Issue.ONE1, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumnsForNotInContext(context, fieldName, Arrays.asList("one", "empty"), new Context().addProjects(Project.values()), Issue.TWO1, Issue.THREE2, Issue.FOUR2, Issue.FOUR1);

        assertJqlColumnsContext(context, String.format("%s < two", fieldName), new Context().addProjects(Project.ONE), Issue.ONE1);
        assertJqlColumnsContext(context, String.format("%s > three", fieldName), new Context().addProjects(Project.THREE), Issue.THREE2, Issue.THREE1);
        assertJqlColumnsContext(context, String.format("%s <= fouronly", fieldName), new Context().addProjects(Project.FOUR), Issue.FOUR2, Issue.FOUR1);
        assertJqlColumnsContext(context, String.format("%s >= two", fieldName), new Context().addProjects(Project.TWO, Project.ONE), Issue.TWO1);
    }

    private void assertJqlColumnsContext(Context andContext, String jqlQuery, Context ctx, Issue... issues)
    {
        Context actualContext = Context.intersect(andContext, ctx);
        List<Issue> actualIssues = getIssuesInContext(Arrays.asList(issues), andContext);
        if (!actualIssues.isEmpty())
        {
            assertJqlColumns(jqlQuery, actualContext, actualIssues);
        }
    }

    private void assertJqlColumnsForInContext(Context andContext, String fieldName, Collection<String> values, Context ctx, Issue... issues)
    {
        Context actualContext = Context.intersect(andContext, ctx);
        List<Issue> actualIssues = getIssuesInContext(Arrays.asList(issues), andContext);
        if (!actualIssues.isEmpty())
        {
            assertJqlColumnsForIn(fieldName, values, actualContext, actualIssues);
        }
    }

    private void assertJqlColumnsForIn(String fieldName, Collection<String> values, Context ctx, Issue... issues)
    {
        assertJqlColumnsForIn(fieldName, values, ctx, Arrays.asList(issues));
    }

    private void assertJqlColumnsForIn(String fieldName, Collection<String> values, Context ctx, Collection<Issue> issues)
    {
        StringBuilder inBuilder = new StringBuilder(fieldName).append(" in (");
        StringBuilder orBuilder = new StringBuilder();

        for (Iterator<String> i = values.iterator(); i.hasNext();)
        {
            final String value = i.next();
            inBuilder.append(value);
            orBuilder.append(fieldName).append(" = ").append(value);
            if (i.hasNext())
            {
                inBuilder.append(", ");
                orBuilder.append(" or ");
            }
        }
        inBuilder.append(")");

        assertJqlColumns(inBuilder.toString(), ctx, issues);
        assertJqlColumns(orBuilder.toString(), ctx, issues);
    }

    private void assertJqlColumnsForNotInContext(Context andContext, String fieldName, Collection<String> values, Context ctx, Issue... issues)
    {
        Context actualContext = Context.intersect(andContext, ctx);
        List<Issue> actualIssues = getIssuesInContext(Arrays.asList(issues), andContext);
        if (!actualIssues.isEmpty())
        {
            assertJqlColumnsForNotIn(fieldName, values, actualContext, actualIssues);
        }
    }

    private void assertJqlColumnsForNotIn(String fieldName, Collection<String> values, Context ctx, Issue... issues)
    {
        assertJqlColumnsForNotIn(fieldName, values, ctx, Arrays.asList(issues));
    }

    private void assertJqlColumnsForNotIn(String fieldName, Collection<String> values, Context ctx, Collection<Issue> issues)
    {
        StringBuilder inBuilder = new StringBuilder(fieldName).append(" not in (");
        StringBuilder orBuilder = new StringBuilder();

        for (Iterator<String> i = values.iterator(); i.hasNext();)
        {
            final String value = i.next();
            inBuilder.append(value);
            orBuilder.append(fieldName).append(" != ").append(value);
            if (i.hasNext())
            {
                inBuilder.append(", ");
                orBuilder.append(" aNd ");
            }
        }
        inBuilder.append(")");

        assertJqlColumns(inBuilder.toString(), ctx, issues);
        assertJqlColumns(orBuilder.toString(), ctx, issues);
    }

    private void assertDateCustomField(String fieldName, String date, Context ctx, Issue... includeIssues)
            throws ParseException
    {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        final Date parsedDate = dateFormat.parse(date);

        Calendar cal = Calendar.getInstance();
        cal.setTime(parsedDate);
        cal.add(Calendar.MONTH, -1);
        String previousDate = dateFormat.format(cal.getTime());

        cal.setTime(parsedDate);
        cal.add(Calendar.MONTH, 1);
        String nextDate = dateFormat.format(cal.getTime());

        assertRangeField(fieldName, previousDate, date, nextDate, ctx, includeIssues);
    }

    private void assertNumberField(String fieldName, int value, Context ctx, Issue... issues)
    {
        assertRangeField(fieldName, String.valueOf(value - 2), String.valueOf(value), String.valueOf(value + 100), ctx, issues);
    }

    private void assertRangeField(String fieldName, String prev, String value, String next, Context ctx, Issue... issues)
    {
        assertJqlColumns(String.format("%s = %s", fieldName, value), ctx, issues);
        assertJqlColumns(String.format("%s != %s", fieldName, prev), ctx, issues);
        assertJqlColumns(String.format("%s >= %s", fieldName, value), ctx, issues);
        assertJqlColumns(String.format("%s <= %s", fieldName, value), ctx, issues);
        assertJqlColumns(String.format("%s < %s", fieldName, next), ctx, issues);
        assertJqlColumns(String.format("%s > %s", fieldName, prev), ctx, issues);

        assertJqlColumns(String.format("%s in (%s)", fieldName, value), ctx, issues);
        assertJqlColumns(String.format("%s in (%s, %s)", fieldName, value, next), ctx, issues);
        assertJqlColumns(String.format("%1$s = %2$s or %1$s = %3$s", fieldName, value, next), ctx, issues);

        assertJqlColumns(String.format("%s not in (%s)", fieldName, prev), ctx, issues);
        assertJqlColumns(String.format("%s not in (%s, %s)", fieldName, prev, next), ctx, issues);
        assertJqlColumns(String.format("%1$s != %2$s and %1$s != %3$s", fieldName, prev, next), ctx, issues);

        final Collection<Issue> emptyIssues = getIssuesInContext(ctx);
        emptyIssues.removeAll(Arrays.asList(issues));

        if (!emptyIssues.isEmpty())
        {
            assertJqlColumns(String.format("%s is empty", fieldName), ctx, emptyIssues);
            assertJqlColumns(String.format("%s = empty", fieldName), ctx, emptyIssues);
            assertJqlColumns(String.format("%s = null", fieldName), ctx, emptyIssues);
            assertJqlColumns(String.format("%s in (empty, %s)", fieldName, prev), ctx, emptyIssues);
            assertJqlColumns(String.format("%1$s is empty or %1$s = %2$s", fieldName, prev), ctx, emptyIssues);
        }

        assertJqlColumns(String.format("%s is not empty", fieldName), ctx, issues);
        assertJqlColumns(String.format("%s != empty", fieldName), ctx, issues);
        assertJqlColumns(String.format("%s != null", fieldName), ctx, issues);
        assertJqlColumns(String.format("%s not in (empty, %s)", fieldName, prev), ctx, issues);
        assertJqlColumns(String.format("%1$s is not empty and %1$s != %2$s", fieldName, prev), ctx, issues);
    }

    private Set<IssueType> getIssueTypesAndRemove(final IssueType... remove)
    {
        Set<IssueType> types = EnumSet.allOf(IssueType.class);
        types.removeAll(Arrays.asList(remove));
        return types;
    }

    private Set<Project> getProjectsAndRemove(Project... remove)
    {
        Set<Project> projects = EnumSet.allOf(Project.class);
        projects.removeAll(Arrays.asList(remove));
        return projects;
    }

    private List<Issue> getIssuesAndRemoveIssues(Issue... remove)
    {
        List<Issue> issues = new ArrayList<Issue>(Issue.ALL_ISSUES);
        issues.removeAll(Arrays.asList(remove));
        return issues;
    }

    private List<Issue> getIssuesAndRemoveIssues(Collection<Issue> remove)
    {
        List<Issue> issues = new ArrayList<Issue>(Issue.ALL_ISSUES);
        issues.removeAll(remove);
        return issues;
    }

    private List<Issue> getIssuesAndRemoveProject(Project... projects)
    {
        if (projects.length == 0)
        {
            return Issue.ALL_ISSUES;
        }

        Set<Project> projectCheck = EnumSet.copyOf(Arrays.asList(projects));
        List<Issue> issues = new ArrayList<Issue>(Issue.ALL_ISSUES.size());

        for (Issue issue : Issue.ALL_ISSUES)
        {
            if (!projectCheck.contains(issue.getProject()))
            {
                issues.add(issue);
            }
        }
        return issues;
    }

    private List<Issue> getIssuesForProjects(Project... projects)
    {
        if (projects.length == 0)
        {
            return Issue.ALL_ISSUES;
        }

        Set<Project> projectCheck = EnumSet.copyOf(Arrays.asList(projects));
        List<Issue> issues = new ArrayList<Issue>(Issue.ALL_ISSUES.size());

        for (Issue issue : Issue.ALL_ISSUES)
        {
            if (projectCheck.contains(issue.getProject()))
            {
                issues.add(issue);
            }
        }
        return issues;
    }

    private void assertFilterColumns(long filterId, Context ctx, Issue... issues)
    {
        navigation.issueNavigator().loadFilter(filterId, null);
        assertColumns(String.format("Filter: %d", filterId), ctx, issues);
    }

    private void assertFilterColumns(long filterId, List<String> columns, Issue... issues)
    {
        navigation.issueNavigator().loadFilter(filterId, null);
        assertColumns(String.format("Filter: %d", filterId), columns, issues);
    }

    private void assertFilterColumns(long filterId, Context ctx, Collection<Issue> issues)
    {
        assertFilterColumns(filterId, ctx, issues.toArray(new Issue[issues.size()]));
    }

    private void assertJqlColumns(String jqlQuery, Context ctx, Collection<Issue> issues)
    {
        assertJqlColumns(jqlQuery, ctx, issues.toArray(new Issue[issues.size()]));
    }

    private void assertJqlColumns(String jqlQuery, Context ctx, Issue... issues)
    {
        navigation.issueNavigator().createSearch(jqlQuery);
        assertColumns(jqlQuery, ctx, issues);
    }

    private void assertColumns(String msg, Context ctx, Issue... issues)
    {
        assertColumns(msg, calculateColumnsForContext(ctx), issues);
    }

    private void assertColumns(String msg, List<String> columnNames, Issue... issues)
    {
        final List<SearchResultsCondition> condition = new ArrayList<SearchResultsCondition>();
        condition.add(new ColumnsCondition(columnNames));
        condition.add(new ContainsIssueKeysCondition(text, issuesToKeys(issues)));
        condition.add(new NumberOfIssuesCondition(text, issues.length));

        log(String.format("Checking that columns '%s' are visible for '%s'", columnNames, msg));

        assertions.getIssueNavigatorAssertions().assertSearchResults(condition);
    }

    private static String[] issuesToKeys(Issue... issues)
    {
        String[] keys = new String[issues.length];
        int i = 0;
        for (Issue issue : issues)
        {
            keys[i++] = issue.getKey();
        }
        return keys;
    }

    private List<String> calculateColumnsForContext(Context ctx)
    {
        return calculateColumnsForContext(ctx, getDefaultFields());
    }

    private List<String> calculateColumnsForContext(final Context ctx, final List<Field> defaultFields)
    {
        final List<String> column = new ArrayList<String>();
        column.addAll(Arrays.asList("T", "Key", "Status"));

        for (Field defaultField : defaultFields)
        {
            if (defaultField.isVisible(ctx))
            {
                column.add(defaultField.getFieldName());
            }
        }

        return column;
    }

    private List<Field> getTextFields()
    {
        final List<Field> fields = new ArrayList<Field>();

        fields.add(Field.FREE_TEXT_BOTH);
        fields.add(Field.FREE_TEXT_COMPLEX);
        fields.add(Field.FREE_TEXT_GLOBAL);
        fields.add(Field.FREE_TEXT_PROJECT);
        fields.add(Field.FREE_TEXT_TYPE);
        fields.add(Field.TEXT_BOTH);
        fields.add(Field.TEXT_COMPLEX);
        fields.add(Field.TEXT_GLOBAL);
        fields.add(Field.TEXT_PROJECT);
        fields.add(Field.TEXT_TYPE);
        fields.add(Field.READ_TEXT_BOTH);
        fields.add(Field.READ_TEXT_COMPLEX);
        fields.add(Field.READ_TEXT_GLOBAL);
        fields.add(Field.READ_TEXT_PROJECT);
        fields.add(Field.READ_TEXT_TYPE);

        return fields;
    }

    private List<Field> getDefaultFields()
    {
        List<Field> fields = new ArrayList<Field>();

        fields.add(Field.DATE_PICKER_BOTH);
        fields.add(Field.DATE_PICKER_COMPLEX);
        fields.add(Field.DATE_PICKER_GLOBAL);
        fields.add(Field.DATE_PICKER_PROJECT);
        fields.add(Field.DATE_PICKER_TYPE);

        fields.add(Field.DATE_TIME_BOTH);
        fields.add(Field.DATE_TIME_COMPLEX);
        fields.add(Field.DATE_TIME_GLOBAL);
        fields.add(Field.DATE_TIME_PROJECT);
        fields.add(Field.DATE_TIME_TYPE);

        fields.add(Field.URL_BOTH);
        fields.add(Field.URL_COMPLEX);
        fields.add(Field.URL_GLOBAL);
        fields.add(Field.URL_PROJECT);
        fields.add(Field.URL_TYPE);

        fields.add(Field.USER_BOTH);
        fields.add(Field.USER_COMPLEX);
        fields.add(Field.USER_GLOBAL);
        fields.add(Field.USER_PROJECT);
        fields.add(Field.USER_TYPE);

        fields.add(Field.MULTI_USER_BOTH);
        fields.add(Field.MULTI_USER_COMPLEX);
        fields.add(Field.MULTI_USER_GLOBAL);
        fields.add(Field.MULTI_USER_PROJECT);
        fields.add(Field.MULTI_USER_TYPE);

        fields.add(Field.GROUP_BOTH);
        fields.add(Field.GROUP_COMPLEX);
        fields.add(Field.GROUP_GLOBAL);
        fields.add(Field.GROUP_PROJECT);
        fields.add(Field.GROUP_TYPE);

        fields.add(Field.MULTI_GROUP_BOTH);
        fields.add(Field.MULTI_GROUP_COMPLEX);
        fields.add(Field.MULTI_GROUP_GLOBAL);
        fields.add(Field.MULTI_GROUP_PROJECT);
        fields.add(Field.MULTI_GROUP_TYPE);

        fields.add(Field.NUMBER_BOTH);
        fields.add(Field.NUMBER_COMPLEX);
        fields.add(Field.NUMBER_GLOBAL);
        fields.add(Field.NUMBER_PROJECT);
        fields.add(Field.NUMBER_TYPE);

        fields.add(Field.IMPORT_BOTH);
        fields.add(Field.IMPORT_COMPLEX);
        fields.add(Field.IMPORT_GLOBAL);
        fields.add(Field.IMPORT_PROJECT);
        fields.add(Field.IMPORT_TYPE);

        fields.add(Field.PROJECT_BOTH);
        fields.add(Field.PROJECT_COMPLEX);
        fields.add(Field.PROJECT_GLOBAL);
        fields.add(Field.PROJECT_PROJECT);
        fields.add(Field.PROJECT_TYPE);

        fields.add(Field.SINGLE_VERSION_BOTH);
        fields.add(Field.SINGLE_VERSION_COMPLEX);
        fields.add(Field.SINGLE_VERSION_GLOBAL);
        fields.add(Field.SINGLE_VERSION_PROJECT);
        fields.add(Field.SINGLE_VERSION_TYPE);

        fields.add(Field.MULTI_VERSION_BOTH);
        fields.add(Field.MULTI_VERSION_COMPLEX);
        fields.add(Field.MULTI_VERSION_GLOBAL);
        fields.add(Field.MULTI_VERSION_PROJECT);
        fields.add(Field.MULTI_VERSION_TYPE);

        fields.add(Field.FREE_TEXT_BOTH);
        fields.add(Field.FREE_TEXT_COMPLEX);
        fields.add(Field.FREE_TEXT_GLOBAL);
        fields.add(Field.FREE_TEXT_PROJECT);
        fields.add(Field.FREE_TEXT_TYPE);

        fields.add(Field.SELECT_LIST_COMPLEX);
        fields.add(Field.SELECT_LIST_PROJECT_GLOBAL);
        fields.add(Field.SELECT_LIST_TYPE);

        fields.add(Field.RADIO_COMPLEX);
        fields.add(Field.RADIO_PROJECT_GLOBAL);
        fields.add(Field.RADIO_TYPE);

        fields.add(Field.READ_TEXT_BOTH);
        fields.add(Field.READ_TEXT_COMPLEX);
        fields.add(Field.READ_TEXT_GLOBAL);
        fields.add(Field.READ_TEXT_PROJECT);
        fields.add(Field.READ_TEXT_TYPE);

        fields.add(Field.TEXT_BOTH);
        fields.add(Field.TEXT_COMPLEX);
        fields.add(Field.TEXT_GLOBAL);
        fields.add(Field.TEXT_PROJECT);
        fields.add(Field.TEXT_TYPE);

        fields.add(Field.CHECKBOX_COMPLEX);
        fields.add(Field.CHECKBOX_PROJECT_GLOBAL);
        fields.add(Field.CHECKBOX_TYPE);

        fields.add(Field.MULTI_SELECT_COMPLEX);
        fields.add(Field.MULTI_SELECT_PROJECT_GLOBAL);
        fields.add(Field.MULTI_SELECT_TYPE);

        fields.add(Field.CASCADING_SELECT_COMPLEX);
        fields.add(Field.CASCADING_SELECT_PROJECT);

        fields.add(Field.INVISIBLE_FIELD);

        return fields;
    }

    private static class Issue implements Comparable<Issue>
    {
        private static final Issue TWO2 = new Issue("TWO-2", Project.TWO, IssueType.SUBTASK);
        private static final Issue TWO1 = new Issue("TWO-1", Project.TWO, IssueType.TASK);
        private static final Issue THREE2 = new Issue("THREE-2", Project.THREE, IssueType.BUG);
        private static final Issue THREE1 = new Issue("THREE-1", Project.THREE, IssueType.FEATURE);
        private static final Issue ONE1 = new Issue("ONE-1", Project.ONE, IssueType.BUG);
        private static final Issue FOUR3 = new Issue("FOUR-3", Project.FOUR, IssueType.BUG);
        private static final Issue FOUR2 = new Issue("FOUR-2", Project.FOUR, IssueType.SUBTASK);
        private static final Issue FOUR1 = new Issue("FOUR-1", Project.FOUR, IssueType.IMPROVEMENT);

        private static final List<Issue> ALL_ISSUES = Arrays.asList(Issue.TWO2, Issue.TWO1, Issue.THREE2, Issue.THREE1, Issue.ONE1, Issue.FOUR3, Issue.FOUR2, Issue.FOUR1);

        private final String key;
        private final Project project;
        private final IssueType type;

        private Issue(final String key, final Project project, final IssueType type)
        {
            this.key = key;
            this.project = project;
            this.type = type;
        }

        public String getKey()
        {
            return key;
        }

        public Project getProject()
        {
            return project;
        }

        public IssueType getType()
        {
            return type;
        }

        @Override
        public String toString()
        {
            return key;
        }

        public boolean inContext(Context ctx)
        {
            return ctx.matches(new ContextEntry(project, type));
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            final Issue issue = (Issue) o;

            return !(key != null ? !key.equals(issue.key) : issue.key != null);

        }

        @Override
        public int hashCode()
        {
            return key != null ? key.hashCode() : 0;
        }

        public int compareTo(final Issue o)
        {
            return key.compareTo(o.key);
        }
    }

    private static class Field
    {
        private static final Field DATE_PICKER_BOTH = new Field("DatePickerBoth", new Context().addContext(Project.ONE, IssueType.BUG));
        private static final Field DATE_PICKER_COMPLEX = new Field("DatePickerComplex", new Context().addType(IssueType.TASK).addProject(Project.ONE).addContext(Project.TWO, IssueType.IMPROVEMENT));
        private static final Field DATE_PICKER_GLOBAL = new Field("DatePickerGlobal", Context.GLOBAL);
        private static final Field DATE_PICKER_PROJECT = new Field("DatePickerProject", new Context().addProject(Project.TWO).addProject(Project.THREE));
        private static final Field DATE_PICKER_TYPE = new Field("DatePickerType", new Context().addType(IssueType.IMPROVEMENT));

        private static final Field DATE_TIME_BOTH = new Field("DateTimeBoth", new Context().addContext(Project.FOUR, IssueType.BUG));
        private static final Field DATE_TIME_COMPLEX = new Field("DateTimeComplex", new Context().addContext(Project.FOUR, IssueType.IMPROVEMENT).addProject(Project.THREE).addType(IssueType.TASK));
        private static final Field DATE_TIME_GLOBAL = new Field("DateTimeGlobal", Context.GLOBAL);
        private static final Field DATE_TIME_PROJECT = new Field("DateTimeProject", new Context().addProject(Project.ONE));
        private static final Field DATE_TIME_TYPE = new Field("DateTimeType", new Context().addType(IssueType.FEATURE));

        private static final Field FREE_TEXT_BOTH = new Field("FreeTextBoth", new Context().addContext(Project.TWO, IssueType.TASK));
        private static final Field FREE_TEXT_COMPLEX = new Field("FreeTextComplex", new Context().addContext(Project.ONE, IssueType.FEATURE).addProject(Project.FOUR).addType(IssueType.BUG));
        private static final Field FREE_TEXT_GLOBAL = new Field("FreeTextGlobal", Context.GLOBAL);
        private static final Field FREE_TEXT_PROJECT = new Field("FreeTextProject", new Context().addProjects(Project.THREE, Project.FOUR));
        private static final Field FREE_TEXT_TYPE = new Field("FreeTextType", new Context().addType(IssueType.BUG));

        private static final Field TEXT_BOTH = new Field("TextBoth", new Context().addContext(Project.TWO, IssueType.SUBTASK));
        private static final Field TEXT_COMPLEX = new Field("TextComplex", new Context().addContext(Project.THREE, IssueType.FEATURE).addType(IssueType.TASK));
        private static final Field TEXT_GLOBAL = new Field("TextGlobal", Context.GLOBAL);
        private static final Field TEXT_PROJECT = new Field("TextProject", new Context().addProjects(Project.ONE));
        private static final Field TEXT_TYPE = new Field("TextType", new Context().addType(IssueType.IMPROVEMENT));

        private static final Field URL_BOTH = new Field("UrlBoth", new Context().addContext(Project.FOUR, IssueType.SUBTASK));
        private static final Field URL_COMPLEX = new Field("UrlComplex", new Context().addProject(Project.THREE).addType(IssueType.IMPROVEMENT));
        private static final Field URL_GLOBAL = new Field("UrlGlobal", Context.GLOBAL);
        private static final Field URL_PROJECT = new Field("UrlProject", new Context().addProjects(Project.TWO, Project.THREE));
        private static final Field URL_TYPE = new Field("UrlType", new Context().addType(IssueType.BUG));

        private static final Field READ_TEXT_COMPLEX = new Field("ReadTextComplex", new Context().addProjects(Project.THREE, Project.TWO).addType(IssueType.IMPROVEMENT));
        private static final Field READ_TEXT_BOTH = new Field("ReadTextBoth", new Context().addContext(Project.THREE, IssueType.BUG));
        private static final Field READ_TEXT_GLOBAL = new Field("ReadTextGlobal", Context.GLOBAL);
        private static final Field READ_TEXT_PROJECT = new Field("ReadTextProject", new Context().addProjects(Project.FOUR));
        private static final Field READ_TEXT_TYPE = new Field("ReadTextType", new Context().addTypes(IssueType.FEATURE, IssueType.TASK));

        private static final Field USER_BOTH = new Field("UserBoth", new Context().addContext(Project.TWO, IssueType.SUBTASK));
        private static final Field USER_COMPLEX = new Field("UserComplex", new Context().addContext(Project.TWO, IssueType.TASK).addType(IssueType.BUG));
        private static final Field USER_GLOBAL = new Field("UserGlobal", Context.GLOBAL);
        private static final Field USER_PROJECT = new Field("UserProject", new Context().addProjects(Project.ONE, Project.THREE));
        private static final Field USER_TYPE = new Field("UserType", new Context().addTypes(IssueType.FEATURE));

        private static final Field MULTI_USER_BOTH = new Field("MultiUserBoth", new Context().addContext(Project.FOUR, IssueType.BUG));
        private static final Field MULTI_USER_COMPLEX = new Field("MultiUserComplex", new Context().addProject(Project.THREE).addContext(Project.ONE, IssueType.BUG));
        private static final Field MULTI_USER_GLOBAL = new Field("MultiUserGlobal", Context.GLOBAL);
        private static final Field MULTI_USER_PROJECT = new Field("MultiUserProject", new Context().addProjects(Project.TWO, Project.THREE));
        private static final Field MULTI_USER_TYPE = new Field("MultiUserType", new Context().addTypes(IssueType.TASK));

        private static final Field GROUP_BOTH = new Field("GroupBoth", new Context().addContext(Project.THREE, IssueType.BUG));
        private static final Field GROUP_COMPLEX = new Field("GroupComplex", new Context().addProject(Project.THREE).addContext(Project.ONE, IssueType.BUG));
        private static final Field GROUP_GLOBAL = new Field("GroupGlobal", Context.GLOBAL);
        private static final Field GROUP_PROJECT = new Field("GroupProject", new Context().addProjects(Project.FOUR));
        private static final Field GROUP_TYPE = new Field("GroupType", new Context().addTypes(IssueType.FEATURE));

        private static final Field MULTI_GROUP_BOTH = new Field("MultiGroupBoth", new Context().addContext(Project.THREE, IssueType.BUG));
        private static final Field MULTI_GROUP_COMPLEX = new Field("MultiGroupComplex", new Context().addProject(Project.THREE).addContext(Project.ONE, IssueType.BUG));
        private static final Field MULTI_GROUP_GLOBAL = new Field("MultiGroupGlobal", Context.GLOBAL);
        private static final Field MULTI_GROUP_PROJECT = new Field("MultiGroupProject", new Context().addProjects(Project.FOUR));
        private static final Field MULTI_GROUP_TYPE = new Field("MultiGroupType", new Context().addTypes(IssueType.FEATURE));

        private static final Field NUMBER_BOTH = new Field("NumberBoth", new Context().addContext(Project.FOUR, IssueType.SUBTASK));
        private static final Field NUMBER_COMPLEX = new Field("NumberComplex", new Context().addContext(Project.THREE, IssueType.FEATURE).addContext(Project.ONE, IssueType.BUG));
        private static final Field NUMBER_GLOBAL = new Field("NumberGlobal", Context.GLOBAL);
        private static final Field NUMBER_PROJECT = new Field("NumberProject", new Context().addProjects(Project.FOUR, Project.THREE));
        private static final Field NUMBER_TYPE = new Field("NumberType", new Context().addTypes(IssueType.FEATURE));

        private static final Field IMPORT_BOTH = new Field("ImportBoth", new Context().addContext(Project.TWO, IssueType.TASK));
        private static final Field IMPORT_COMPLEX = new Field("ImportComplex", new Context().addContext(Project.THREE, IssueType.BUG).addProject(Project.FOUR));
        private static final Field IMPORT_GLOBAL = new Field("ImportGlobal", Context.GLOBAL);
        private static final Field IMPORT_PROJECT = new Field("ImportProject", new Context().addProjects(Project.ONE));
        private static final Field IMPORT_TYPE = new Field("ImportType", new Context().addTypes(IssueType.BUG));

        private static final Field PROJECT_BOTH = new Field("ProjectBoth", new Context().addContext(Project.THREE, IssueType.FEATURE));
        private static final Field PROJECT_COMPLEX = new Field("ProjectComplex", new Context().addContext(Project.TWO, IssueType.SUBTASK).addProject(Project.FOUR));
        private static final Field PROJECT_GLOBAL = new Field("ProjectGlobal", Context.GLOBAL);
        private static final Field PROJECT_PROJECT = new Field("ProjectProject", new Context().addProjects(Project.THREE, Project.ONE));
        private static final Field PROJECT_TYPE = new Field("ProjectType", new Context().addTypes(IssueType.SUBTASK));

        private static final Field SINGLE_VERSION_BOTH = new Field("SingleVersionBoth", new Context().addContext(Project.THREE, IssueType.BUG));
        private static final Field SINGLE_VERSION_COMPLEX = new Field("SingleVersionComplex", new Context().addProject(Project.TWO).addType(IssueType.SUBTASK));
        private static final Field SINGLE_VERSION_GLOBAL = new Field("SingleVersionGlobal", Context.GLOBAL);
        private static final Field SINGLE_VERSION_PROJECT = new Field("SingleVersionProject", new Context().addProjects(Project.THREE, Project.ONE));
        private static final Field SINGLE_VERSION_TYPE = new Field("SingleVersionType", new Context().addType(IssueType.BUG));

        private static final Field MULTI_VERSION_BOTH = new Field("MultiVersionBoth", new Context().addContext(Project.THREE, IssueType.FEATURE));
        private static final Field MULTI_VERSION_COMPLEX = new Field("MultiVersionComplex", new Context().addContext(Project.ONE, IssueType.BUG).addType(IssueType.IMPROVEMENT));
        private static final Field MULTI_VERSION_GLOBAL = new Field("MultiVersionGlobal", Context.GLOBAL);
        private static final Field MULTI_VERSION_PROJECT = new Field("MultiVersionProject", new Context().addProjects(Project.ONE, Project.TWO, Project.FOUR));
        private static final Field MULTI_VERSION_TYPE = new Field("MultiVersionType", new Context().addType(IssueType.BUG));

        private static final Field SELECT_LIST_COMPLEX = new Field("SelectListComplex", new Context().addContext(Project.TWO, IssueType.TASK).addProjects(Project.ONE, Project.THREE));
        private static final Field SELECT_LIST_PROJECT_GLOBAL = new Field("SelectListProjectGlobal", new Context().addProjects(Project.THREE).addContext(Context.GLOBAL));
        private static final Field SELECT_LIST_TYPE = new Field("SelectListType", new Context().addType(IssueType.IMPROVEMENT));

        private static final Field RADIO_COMPLEX = new Field("RadioComplex", new Context().addContext(Project.TWO, IssueType.TASK).addProjects(Project.ONE, Project.THREE));
        private static final Field RADIO_PROJECT_GLOBAL = new Field("RadioProjectGlobal", new Context().addProjects(Project.THREE).addContext(Context.GLOBAL));
        private static final Field RADIO_TYPE = new Field("RadioType", new Context().addType(IssueType.IMPROVEMENT));

        private static final Field CHECKBOX_COMPLEX = new Field("CheckboxComplex", new Context().addContext(Project.TWO, IssueType.TASK).addProjects(Project.ONE, Project.THREE));
        private static final Field CHECKBOX_PROJECT_GLOBAL = new Field("CheckboxProjectGlobal", new Context().addProjects(Project.THREE).addContext(Context.GLOBAL));
        private static final Field CHECKBOX_TYPE = new Field("CheckboxType", new Context().addType(IssueType.IMPROVEMENT));

        private static final Field MULTI_SELECT_COMPLEX = new Field("MultiSelectComplex", new Context().addContext(Project.TWO, IssueType.TASK).addProjects(Project.ONE, Project.THREE));
        private static final Field MULTI_SELECT_PROJECT_GLOBAL = new Field("MultiSelectProjectGlobal", new Context().addProjects(Project.THREE).addContext(Context.GLOBAL));
        private static final Field MULTI_SELECT_TYPE = new Field("MultiSelectType", new Context().addType(IssueType.IMPROVEMENT));

        private static final Field CASCADING_SELECT_COMPLEX = new Field("CascasingSelectComplex", new Context().addContext(Project.FOUR, IssueType.SUBTASK).addProjects(Project.THREE, Project.TWO));
        private static final Field CASCADING_SELECT_PROJECT = new Field("CascadingSelectProject", new Context().addProjects(Project.values()));

        private static final Field INVISIBLE_FIELD = new Field("InvisibleField", new Context().addProjects(Project.THREE));

        private final String fieldName;
        private final Context fieldContext;

        private Field(String fieldName, Context fieldContext)
        {
            this.fieldName = fieldName;
            this.fieldContext = new Context(fieldContext, true);
        }

        boolean isVisible(Context contex)
        {
            return fieldContext.matches(contex);
        }

        public String getFieldName()
        {
            return fieldName;
        }

        public Context getFieldContext()
        {
            return fieldContext;
        }

        @Override
        public String toString()
        {
            return String.format("Field Config: %s {%s}.%n", fieldName, fieldContext);
        }
    }

    private static class Context
    {
        private static final Context GLOBAL = new Context().addEntry(ContextEntry.GLOBAL);

        private final Set<ContextEntry> entries;

        private Context()
        {
            this.entries = new HashSet<ContextEntry>();
        }

        private Context(Context context, boolean lock)
        {
            final HashSet<ContextEntry> copyEntries = new HashSet<ContextEntry>(context.entries);
            this.entries = lock ? Collections.unmodifiableSet(copyEntries) : copyEntries;
        }

        private Context addEntry(ContextEntry entry)
        {
            this.entries.add(entry);
            return this;
        }

        private Context addContexts(Collection<Project> projects, Collection<IssueType> types)
        {
            for (Project project : projects)
            {
                addContexts(project, types);
            }
            return this;
        }

        private Context addContexts(Project project, Collection<IssueType> types)
        {
            for (IssueType type : types)
            {
                addContext(project, type);
            }
            return this;
        }

        private Context addContext(Project project, IssueType type)
        {
            this.entries.add(new ContextEntry(project, type));
            return this;
        }

        private Context addContext(Context context)
        {
            this.entries.addAll(context.entries);
            return this;
        }

        private Context addProjects(Project... projects)
        {
            for (Project project : projects)
            {
                addProject(project);
            }
            return this;
        }

        private Context addProjects(Collection<Project> projects)
        {
            for (Project project : projects)
            {
                addProject(project);
            }
            return this;
        }

        private Context addProject(Project project)
        {
            this.entries.add(ContextEntry.forProject(project));
            return this;
        }

        private Context addTypes(IssueType... types)
        {
            for (IssueType type : types)
            {
                addType(type);
            }
            return this;
        }

        private Context addTypes(Collection<IssueType> types)
        {
            for (IssueType type : types)
            {
                addType(type);
            }
            return this;
        }

        private Context addType(IssueType type)
        {
            this.entries.add(ContextEntry.forType(type));
            return this;
        }

        private Set<ContextEntry> getEntries()
        {
            return this.entries;
        }

        private boolean matches(Context context)
        {
            for (ContextEntry otherEntry : context.getEntries())
            {
                if (matches(otherEntry))
                {
                    return true;
                }
            }
            return false;
        }

        private boolean matches(final ContextEntry checkEntry)
        {
            boolean containsExplicitProject = false;
            boolean implicitProjectMatch = false;
            for (ContextEntry entry : entries)
            {
                final boolean projectMatches = entry.projectMatches(checkEntry);
                final boolean typeMatches = entry.typeMatches(checkEntry);

                if (projectMatches)
                {
                    final boolean explicitProject = !entry.isAnyProject();
                    if (typeMatches)
                    {
                        if (explicitProject)
                        {
                            return true;
                        }
                        else
                        {
                            implicitProjectMatch = true;
                        }
                    }
                    else
                    {
                        containsExplicitProject |= explicitProject;
                    }
                }
            }

            return implicitProjectMatch && !containsExplicitProject;
        }

        private static Context intersect(final Context ctx, final Context otherCtx)
        {
            Context newCtx = new Context();
            for (ContextEntry entry : ctx.getEntries())
            {
                for (ContextEntry otherEntry : otherCtx.getEntries())
                {
                    if (shouldInterset(entry, otherEntry))
                    {
                        newCtx.addEntry(combine(entry, otherEntry));
                    }
                }
            }

            return newCtx;
        }

        private static boolean shouldInterset(ContextEntry one, ContextEntry two)
        {
            // If either the issue type contexts are all OR ids are the same
            if ((one.isAnyType() || two.isAnyType()) || one.getType() == two.getType())
            {
                // If either the project contexts are all OR ids are the same
                if ((one.isAnyProject() || two.isAnyProject()) || one.getProject() == two.getProject())
                {
                    return true;
                }
            }
            return false;
        }

        private static ContextEntry combine(ContextEntry one, ContextEntry two)
        {
            final IssueType type;
            if (one.isAnyType())
            {
                if (two.isAnyType())
                {
                    type = null;
                }
                else
                {
                    type = two.getType();
                }
            }
            else
            {
                type = one.getType();
            }

            final Project project;
            if (one.isAnyProject())
            {
                if (two.isAnyProject())
                {
                    project = null;
                }
                else
                {
                    project = two.getProject();
                }
            }
            else
            {
                project = one.getProject();
            }

            return new ContextEntry(project, type);
        }

        @Override
        public String toString()
        {
            return "Context [" + entries + "]";
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            final Context context = (Context) o;
            return entries.equals(context.entries);

        }

        @Override
        public int hashCode()
        {
            return entries.hashCode();
        }
    }

    private static class ContextEntry
    {
        private final static ContextEntry GLOBAL = new ContextEntry(null, null);

        private final Project project;
        private final IssueType type;

        private ContextEntry(Project project, IssueType type)
        {
            this.project = project;
            this.type = type;
        }

        private boolean projectMatches(ContextEntry entry)
        {
            return this.project == null || this.project == entry.getProject();
        }

        private boolean typeMatches(ContextEntry entry)
        {
            return this.type == null || this.type == entry.getType();
        }

        private Project getProject()
        {
            return project;
        }

        public boolean isAnyProject()
        {
            return project == null;
        }

        private IssueType getType()
        {
            return type;
        }

        private boolean isAnyType()
        {
            return type == null;
        }

        @Override
        public String toString()
        {
            return String.format("[Project: %s, Type: %s]", project, type);
        }

        private static ContextEntry forProject(Project project)
        {
            return new ContextEntry(project, null);
        }

        private static ContextEntry forType(IssueType type)
        {
            return new ContextEntry(null, type);
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            final ContextEntry that = (ContextEntry) o;

            if (project != that.project)
            {
                return false;
            }
            if (type != that.type)
            {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = project != null ? project.hashCode() : 0;
            result = 31 * result + (type != null ? type.hashCode() : 0);
            return result;
        }
    }
}
