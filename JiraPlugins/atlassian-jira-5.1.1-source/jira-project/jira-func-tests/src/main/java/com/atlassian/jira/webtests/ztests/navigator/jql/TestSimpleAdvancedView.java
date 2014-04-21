package com.atlassian.jira.webtests.ztests.navigator.jql;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.Splitable;
import com.atlassian.jira.functest.framework.admin.TimeTracking;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.navigation.IssueNavigatorNavigation;
import com.atlassian.jira.functest.framework.navigator.AssigneeCondition;
import com.atlassian.jira.functest.framework.navigator.ComponentCondition;
import com.atlassian.jira.functest.framework.navigator.GenericQueryCondition;
import com.atlassian.jira.functest.framework.navigator.IssueTypeCondition;
import com.atlassian.jira.functest.framework.navigator.NavigatorCondition;
import com.atlassian.jira.functest.framework.navigator.NavigatorSearch;
import com.atlassian.jira.functest.framework.navigator.NavigatorSearchBuilder;
import com.atlassian.jira.functest.framework.navigator.PriorityCondition;
import com.atlassian.jira.functest.framework.navigator.ProjectCondition;
import com.atlassian.jira.functest.framework.navigator.QuerySearchCondition;
import com.atlassian.jira.functest.framework.navigator.ReporterCondition;
import com.atlassian.jira.functest.framework.navigator.ResolutionCondition;
import com.atlassian.jira.functest.framework.navigator.StatusCondition;
import com.atlassian.jira.functest.framework.navigator.VersionCondition;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.functest.framework.util.url.URLUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Test switching from the basic, form-based view to the advanced, JQL-based view
 * @since v4.0
 */
@Splitable
@WebTest ({ Category.FUNC_TEST, Category.JQL })
public class TestSimpleAdvancedView extends FuncTestCase
{
    // this isn't really the best behaviour. ideally we'd populate JQL with the bad test. but we want
    // to at least verify that we aren't generating unparseable JQL.
    public void testSystemFieldErrors() throws Exception
    {
        administration.restoreBlankInstance();

        assertJql(new NavigatorSearchBuilder()
                .addQueryString("*start")
                .addQueryField(QuerySearchCondition.QueryField.SUMMARY).createSearch(),
                "summary ~ \"*start\"", "The text query '*start' for field 'summary' is not allowed to start with '*'.");
        assertJql(new NavigatorSearchBuilder()
                .addQueryString("*start")
                .addQueryField(QuerySearchCondition.QueryField.DESCRIPTION).createSearch(),
                "description ~ \"*start\"", "The text query '*start' for field 'description' is not allowed to start with '*'.");
        assertJql(new NavigatorSearchBuilder()
                .addQueryString("*start")
                .addQueryField(QuerySearchCondition.QueryField.COMMENTS).createSearch(),
                "comment ~ \"*start\"", "The text query '*start' for field 'comment' is not allowed to start with '*'.");
        assertJql(new NavigatorSearchBuilder()
                .addQueryString("*start")
                .addQueryField(QuerySearchCondition.QueryField.ENVIRONMENT).createSearch(),
                "environment ~ \"*start\"", "The text query '*start' for field 'environment' is not allowed to start with '*'.");

        assertJqlWarning(new NavigatorSearch(new ReporterCondition().setUser("INVALID")), "reporter = INVALID", "The value 'INVALID' does not exist for the field 'reporter'.");
        assertJqlWarning(new NavigatorSearch(new AssigneeCondition().setUser("INVALID")), "assignee = INVALID", "The value 'INVALID' does not exist for the field 'assignee'.");

        assertJql(new NavigatorSearch(new GenericQueryCondition("created:before").setQuery("17/FAKE/09")), "created <= \"17/FAKE/09\"", "Date value '17/FAKE/09' for field 'created' is invalid. Valid formats include: 'yyyy/MM/dd HH:mm', 'yyyy-MM-dd HH:mm', 'yyyy/MM/dd', 'yyyy-MM-dd', or a period format e.g. '-5d', '4w 2d'");
        assertJql(new NavigatorSearch(new GenericQueryCondition("created:after").setQuery("17/FAKE/09")), "created >= \"17/FAKE/09\"", "Date value '17/FAKE/09' for field 'created' is invalid. Valid formats include: 'yyyy/MM/dd HH:mm', 'yyyy-MM-dd HH:mm', 'yyyy/MM/dd', 'yyyy-MM-dd', or a period format e.g. '-5d', '4w 2d'");
        assertJql(new NavigatorSearch(new GenericQueryCondition("created:previous").setQuery("17/FAKE/09")), "created >= \"17/FAKE/09\"", "Date value '17/FAKE/09' for field 'created' is invalid. Valid formats include: 'yyyy/MM/dd HH:mm', 'yyyy-MM-dd HH:mm', 'yyyy/MM/dd', 'yyyy-MM-dd', or a period format e.g. '-5d', '4w 2d'");
        assertJql(new NavigatorSearch(new GenericQueryCondition("created:next").setQuery("17/FAKE/09")), "created <= \"17/FAKE/09\"", "Date value '17/FAKE/09' for field 'created' is invalid. Valid formats include: 'yyyy/MM/dd HH:mm', 'yyyy-MM-dd HH:mm', 'yyyy/MM/dd', 'yyyy-MM-dd', or a period format e.g. '-5d', '4w 2d'");
        
        assertJql(new NavigatorSearch(new GenericQueryCondition("updated:before").setQuery("17/FAKE/09")), "updated <= \"17/FAKE/09\"", "Date value '17/FAKE/09' for field 'updated' is invalid. Valid formats include: 'yyyy/MM/dd HH:mm', 'yyyy-MM-dd HH:mm', 'yyyy/MM/dd', 'yyyy-MM-dd', or a period format e.g. '-5d', '4w 2d'");
        assertJql(new NavigatorSearch(new GenericQueryCondition("updated:after").setQuery("17/FAKE/09")), "updated >= \"17/FAKE/09\"", "Date value '17/FAKE/09' for field 'updated' is invalid. Valid formats include: 'yyyy/MM/dd HH:mm', 'yyyy-MM-dd HH:mm', 'yyyy/MM/dd', 'yyyy-MM-dd', or a period format e.g. '-5d', '4w 2d'");
        assertJql(new NavigatorSearch(new GenericQueryCondition("updated:previous").setQuery("17/FAKE/09")), "updated >= \"17/FAKE/09\"", "Date value '17/FAKE/09' for field 'updated' is invalid. Valid formats include: 'yyyy/MM/dd HH:mm', 'yyyy-MM-dd HH:mm', 'yyyy/MM/dd', 'yyyy-MM-dd', or a period format e.g. '-5d', '4w 2d'");
        assertJql(new NavigatorSearch(new GenericQueryCondition("updated:next").setQuery("17/FAKE/09")), "updated <= \"17/FAKE/09\"", "Date value '17/FAKE/09' for field 'updated' is invalid. Valid formats include: 'yyyy/MM/dd HH:mm', 'yyyy-MM-dd HH:mm', 'yyyy/MM/dd', 'yyyy-MM-dd', or a period format e.g. '-5d', '4w 2d'");
        
        assertJql(new NavigatorSearch(new GenericQueryCondition("duedate:before").setQuery("17/FAKE/09")), "due <= \"17/FAKE/09\"", "Date value '17/FAKE/09' for field 'due' is invalid. Valid formats include: 'YYYY/MM/DD', 'YYYY-MM-DD', or a period format e.g. '-5d', '4w 2d'");
        assertJql(new NavigatorSearch(new GenericQueryCondition("duedate:after").setQuery("17/FAKE/09")), "due >= \"17/FAKE/09\"", "Date value '17/FAKE/09' for field 'due' is invalid. Valid formats include: 'YYYY/MM/DD', 'YYYY-MM-DD', or a period format e.g. '-5d', '4w 2d'");
        assertJql(new NavigatorSearch(new GenericQueryCondition("duedate:previous").setQuery("17/FAKE/09")), "due >= \"17/FAKE/09\"", "Date value '17/FAKE/09' for field 'due' is invalid. Valid formats include: 'YYYY/MM/DD', 'YYYY-MM-DD', or a period format e.g. '-5d', '4w 2d'");
        assertJql(new NavigatorSearch(new GenericQueryCondition("duedate:next").setQuery("17/FAKE/09")), "due <= \"17/FAKE/09\"", "Date value '17/FAKE/09' for field 'due' is invalid. Valid formats include: 'YYYY/MM/DD', 'YYYY-MM-DD', or a period format e.g. '-5d', '4w 2d'");
        
        assertJql(new NavigatorSearch(new GenericQueryCondition("resolutiondate:before").setQuery("17/FAKE/09")), "resolved <= \"17/FAKE/09\"", "Date value '17/FAKE/09' for field 'resolved' is invalid. Valid formats include: 'yyyy/MM/dd HH:mm', 'yyyy-MM-dd HH:mm', 'yyyy/MM/dd', 'yyyy-MM-dd', or a period format e.g. '-5d', '4w 2d'");
        assertJql(new NavigatorSearch(new GenericQueryCondition("resolutiondate:after").setQuery("17/FAKE/09")), "resolved >= \"17/FAKE/09\"", "Date value '17/FAKE/09' for field 'resolved' is invalid. Valid formats include: 'yyyy/MM/dd HH:mm', 'yyyy-MM-dd HH:mm', 'yyyy/MM/dd', 'yyyy-MM-dd', or a period format e.g. '-5d', '4w 2d'");
        assertJql(new NavigatorSearch(new GenericQueryCondition("resolutiondate:previous").setQuery("17/FAKE/09")), "resolved >= \"17/FAKE/09\"", "Date value '17/FAKE/09' for field 'resolved' is invalid. Valid formats include: 'yyyy/MM/dd HH:mm', 'yyyy-MM-dd HH:mm', 'yyyy/MM/dd', 'yyyy-MM-dd', or a period format e.g. '-5d', '4w 2d'");
        assertJql(new NavigatorSearch(new GenericQueryCondition("resolutiondate:next").setQuery("17/FAKE/09")), "resolved <= \"17/FAKE/09\"", "Date value '17/FAKE/09' for field 'resolved' is invalid. Valid formats include: 'yyyy/MM/dd HH:mm', 'yyyy-MM-dd HH:mm', 'yyyy/MM/dd', 'yyyy-MM-dd', or a period format e.g. '-5d', '4w 2d'");
        assertJql(new NavigatorSearch(
                new ReporterCondition().setUser("INVALID"),
                new AssigneeCondition().setUser("INVALID"),
                new GenericQueryCondition("created:before").setQuery("17/FAKE/09"),
                new GenericQueryCondition("updated:after").setQuery("17/FAKE/09"),
                new GenericQueryCondition("duedate:previous").setQuery("17/FAKE/09"),
                new GenericQueryCondition("resolutiondate:next").setQuery("17/FAKE/09")),
                "assignee = INVALID AND reporter = INVALID AND due >= \"17/FAKE/09\" AND created <= \"17/FAKE/09\" AND updated >= \"17/FAKE/09\" AND resolved <= \"17/FAKE/09\"",
                "Date value '17/FAKE/09' for field 'due' is invalid. Valid formats include: 'YYYY/MM/DD', 'YYYY-MM-DD', or a period format e.g. '-5d', '4w 2d'.",
                "Date value '17/FAKE/09' for field 'created' is invalid. Valid formats include: 'yyyy/MM/dd HH:mm', 'yyyy-MM-dd HH:mm', 'yyyy/MM/dd', 'yyyy-MM-dd', or a period format e.g. '-5d', '4w 2d'.",
                "Date value '17/FAKE/09' for field 'updated' is invalid. Valid formats include: 'yyyy/MM/dd HH:mm', 'yyyy-MM-dd HH:mm', 'yyyy/MM/dd', 'yyyy-MM-dd', or a period format e.g. '-5d', '4w 2d'.",
                "Date value '17/FAKE/09' for field 'resolved' is invalid. Valid formats include: 'yyyy/MM/dd HH:mm', 'yyyy-MM-dd HH:mm', 'yyyy/MM/dd', 'yyyy-MM-dd', or a period format e.g. '-5d', '4w 2d'."
                );
        
        assertJqlWarning(new NavigatorSearch(
                new ReporterCondition().setUser("INVALID"),
                new AssigneeCondition().setUser("INVALID")),
                "assignee = INVALID AND reporter = INVALID",
                "The value 'INVALID' does not exist for the field 'assignee'.",
                "The value 'INVALID' does not exist for the field 'reporter'."
                );
    }

    public void testSystemFieldsCombo() throws Exception
    {
        administration.restoreBlankInstance();

        final QuerySearchCondition searchCondition = new QuerySearchCondition("query");
        searchCondition.addField(QuerySearchCondition.QueryField.SUMMARY);
        searchCondition.addField(QuerySearchCondition.QueryField.ENVIRONMENT);
        searchCondition.addField(QuerySearchCondition.QueryField.COMMENTS);
        searchCondition.addField(QuerySearchCondition.QueryField.DESCRIPTION);

        final NavigatorCondition[] conditions = {
                new ProjectCondition().addProject("homosapien").addProject("monkey"),
                new IssueTypeCondition().addIssueType(IssueTypeCondition.IssueType.BUG),
                new ReporterCondition().setUser(ADMIN_USERNAME),
                new AssigneeCondition().setCurrentUser(),
                new StatusCondition().addStatus(StatusCondition.Type.OPEN).addStatus(StatusCondition.Type.REOPENED),
                new ResolutionCondition().addResolution(ResolutionCondition.Type.CANNOT_REPRODUCE).addResolution(ResolutionCondition.Type.DUPLICATE),
                new GenericQueryCondition("created:before").setQuery("17/Jul/09"),
                new GenericQueryCondition("updated:next").setQuery("-1h"),
                new GenericQueryCondition("duedate:previous").setQuery("-1h"),
                new GenericQueryCondition("resolutiondate:after").setQuery("17/Jul/09"),
                searchCondition
        };

        executeBasicAssertJql("project in (HSP, MKY) "
                + "AND (summary ~ query OR description ~ query OR comment ~ query OR environment ~ query) "
                + "AND issuetype = Bug "
                + "AND resolution in (Duplicate, \"Cannot Reproduce\") "
                + "AND assignee = currentUser() "
                + "AND reporter = admin "
                + "AND due >= -1h "
                + "AND status in (Open, Reopened) "
                + "AND created <= 2009-07-17 "
                + "AND updated <= -1h "
                + "AND resolved >= 2009-07-17",
                new String[] {
                        "Query", "\"query\"", "in", "Description", "Environment", "Summary", "Comments",
                        "Project", "homosapien", "monkey",
                        "Issue Type", "Bug",
                        "Reporter", ADMIN_USERNAME,
                        "Assignee", "Current User",
                        "Status", "Open", "Reopened",
                        "Resolutions", "Duplicate", "Cannot Reproduce",
                        "Created", "Before", "17/Jul/09",
                        "Due", "Date", "From", "1 hour ago", "to", "anytime",
                        "Resolved", "After", "17/Jul/09" },
                new NavigatorSearch(conditions));
        
        navigation.issueNavigator().gotoEditMode(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);
        tester.setWorkingForm("issue-filter");

        // this is what the filterform should look like after switching back
        Map<String, String[]> expectedForm = new HashMap<String, String[]>();
        expectedForm.put("summary", new String[] {"true"});
        expectedForm.put("body", new String[] {"true"});
        expectedForm.put("query", new String[] {"query"});
        expectedForm.put("duedate:next", new String[] {""});
        expectedForm.put("updated:after", new String[] {""});
        expectedForm.put("show", new String[] {});
        expectedForm.put("resolution", new String[] {"3", "5"});
        expectedForm.put("type", new String[] {"1"});
        expectedForm.put("created:previous", new String[] {""});
        expectedForm.put("updated:before", new String[] {""});
        expectedForm.put("reporter", new String[] { ADMIN_USERNAME });
        expectedForm.put("reporterSelect", new String[] { "specificuser" });
        expectedForm.put("updated:previous", new String[] { "" });
        expectedForm.put("hide", new String[] {});
        expectedForm.put("description", new String[] { "true" });
        expectedForm.put("priority", new String[] {});
        expectedForm.put("reset", new String[] { "update" });
        expectedForm.put("updated:next", new String[] { "-1h" });
        expectedForm.put("resolutiondate:previous", new String[] { "" });
        expectedForm.put("refreshFilter", new String[] { "false" });
        expectedForm.put("resolutiondate:before", new String[] { "" });
        expectedForm.put("created:before", new String[] { "17/Jul/09" });
        expectedForm.put("duedate:after", new String[] { "" });
        expectedForm.put("status", new String[] { "1", "4" });
        expectedForm.put("resolutiondate:next", new String[] { "" });
        expectedForm.put("assignee", new String[] { "" });
        expectedForm.put("pid", new String[] { "10000", "10001" });
        expectedForm.put("created:next", new String[] { "" });
        expectedForm.put("resolutiondate:after", new String[] { "17/Jul/09" });
        expectedForm.put("duedate:previous", new String[] { "-1h" });
        expectedForm.put("assigneeSelect", new String[] { "issue_current_user" });
        expectedForm.put("workratio:min", new String[] { "" });
        expectedForm.put("environment", new String[] { "true" });
        expectedForm.put("created:after", new String[] { "" });
        expectedForm.put("duedate:before", new String[] { "" });
        expectedForm.put("workratio:max", new String[] { "" });
        expectedForm.put("usercreated", new String[] { "true" });
        expectedForm.put("labels", new String[] { "" });
        expectedForm.put("atl_token", new String[] { URLUtil.decode(page.getXsrfToken()) });


        final String[] parameterNames = tester.getDialog().getForm().getParameterNames();
        for (String name : parameterNames)
        {
            final String[] expected = expectedForm.get(name);
            final String[] actual = tester.getDialog().getForm().getParameterValues(name);
            if (!Arrays.equals(expected, actual))
            {
                fail(String.format("%s: form parameter values %s did not match expected values %s", name, Arrays.toString(actual), Arrays.toString(expected)));
            }
        }
    }

    public void testFieldsWithIdValues() throws Exception
    {
        administration.restoreBlankInstance();
        final String singleVersionCFId = administration.customFields().addCustomField("com.atlassian.jira.plugin.system.customfieldtypes:version", "Single Version Picker");
        final String multiVersionCFId = administration.customFields().addCustomField("com.atlassian.jira.plugin.system.customfieldtypes:multiversion", "Multi Version Picker");

        final ComponentCondition componentCondition = new ComponentCondition();
        componentCondition.addOption("New Component 1");

        final VersionCondition fixForVersionCondition = new VersionCondition("fixfor");
        fixForVersionCondition.addOption("New Version 1");

        final VersionCondition singleVersionCondition = new VersionCondition(singleVersionCFId);
        singleVersionCondition.addOption("New Version 1");

        final VersionCondition multiVersionCondition = new VersionCondition(multiVersionCFId);
        multiVersionCondition.addOption("New Version 1");

        final VersionCondition affectedVersionCondition = new VersionCondition("version");
        affectedVersionCondition.addOption("New Version 1");

        final NavigatorCondition[] conditions = {
                new ProjectCondition().addProject("homosapien"),
                new IssueTypeCondition().addIssueType("Bug"),
                new PriorityCondition().addPriority(PriorityCondition.Type.CRITICAL),
                new StatusCondition().addStatus(StatusCondition.Type.OPEN),
                new ResolutionCondition().addResolution(ResolutionCondition.Type.DUPLICATE),
                fixForVersionCondition,
                affectedVersionCondition,
                componentCondition,
                singleVersionCondition,
                multiVersionCondition
        };

        final String expectedJql = "project = HSP "
                + "AND issuetype = Bug "
                + "AND priority = Critical "
                + "AND resolution = Duplicate "
                + "AND affectedVersion = 10000 "
                + "AND fixVersion = 10000 "
                + "AND component = \"New Component 1\" "
                + "AND status = Open "
                + "AND \"Multi Version Picker\" = \"New Version 1\" "
                + "AND \"Single Version Picker\" = \"New Version 1\"";

        final String[] expectedSummary = {
                "Project", "homosapien",
                "Issue Type", "Bug",
                "Fix For:", "New Version 1",
                "Components", "New Component 1",
                "Affects Versions:", "New Version 1",
                "Status", "Open",
                "Resolutions", "Duplicate",
                "Priorities:", "Critical",
                "Multi Version Picker:", "New Version 1",
                "Single Version Picker:", "New Version 1"
        };

        executeBasicAssertJql(expectedJql, expectedSummary, new NavigatorSearch(conditions));

        navigation.issueNavigator().gotoEditMode(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);
        tester.setWorkingForm("issue-filter");

        // this is what the filterform should look like after switching back
        Map<String, String[]> expectedForm = new LinkedHashMap<String, String[]>();
        expectedForm.put("component", new String[] {"10000"});
        expectedForm.put("resolution", new String[] {"3"});
        expectedForm.put("type", new String[] {"1"});
        expectedForm.put("priority", new String[] {"2"});
        expectedForm.put("status", new String[] { "1" });
        expectedForm.put("pid", new String[] { "10000" });
        expectedForm.put("fixfor", new String[] { "10000" });
        expectedForm.put("version", new String[] { "10000" });
        expectedForm.put(multiVersionCFId, new String[] { "10000" });
        expectedForm.put(singleVersionCFId, new String[] { "10000" });

        final Collection<String> parameterNames = expectedForm.keySet();
        for (String name : parameterNames)
        {
            final String[] expected = expectedForm.get(name);
            final String[] actual = tester.getDialog().getForm().getParameterValues(name);
            if (!Arrays.equals(expected, actual))
            {
                fail(String.format("%s: form parameter values %s did not match expected values %s", name, Arrays.toString(actual), Arrays.toString(expected)));
            }
        }
    }

    public void testCustomFieldsSimple() throws Exception
    {
        // this XML has all custom fields present in global context
        administration.restoreData("TestCustomFieldDefaultSortOrderings.xml");

        // 10000 = Cascading Select
        executeBasicAssertJql("CSF in cascadeOption(10000, 10001)",
                new String[] { "CSF (parent)", "parent", "CSF (child)", "child" },
                new NavigatorSearch(
                        new GenericQueryCondition("customfield_10000").setQuery("10000"),
                        new GenericQueryCondition("customfield_10000:1").setQuery("10001")
        ));

        // 10001 = Date Picker
        executeBasicAssertJql("DP <= 2009-7-17",
                new String[] {"DP (before)", "17/Jul/09"},
                new NavigatorSearch(new GenericQueryCondition("customfield_10001:before").setQuery("17/Jul/09")));
        executeBasicAssertJql("DP >= 2009-7-17",
                new String[] {"DP (after)", "17/Jul/09"},
                new NavigatorSearch(new GenericQueryCondition("customfield_10001:after").setQuery("17/Jul/09")));
        executeBasicAssertJql("DP >= -1h",
                new String[] {"DP", "From 1 hour ago", "to", "anytime"},
                new NavigatorSearch(new GenericQueryCondition("customfield_10001:previous").setQuery("-1h")));
        executeBasicAssertJql("DP <= 1h",
                new String[] {"DP", "From anytime to 1 hour from now"},
                new NavigatorSearch(new GenericQueryCondition("customfield_10001:next").setQuery("1h")));

        // 10002 = Date Time
        executeBasicAssertJql("DT <= \"2009-07-21 11:07\"",
                new String[] {"DT (before)", "21/Jul/09 11:07 AM"},
                new NavigatorSearch(new GenericQueryCondition("customfield_10002:before").setQuery("21/Jul/09 11:07 AM")));
        executeBasicAssertJql("DT >= \"2009-07-21 11:07\"",
                new String[] {"DT (after)", "21/Jul/09 11:07 AM"},
                new NavigatorSearch(new GenericQueryCondition("customfield_10002:after").setQuery("21/Jul/09 11:07 AM")));
        executeBasicAssertJql("DT >= \"-2d 30m\"",
                new String[] {"DT", "From 2 days, 30 minutes ago", "to", "anytime"},
                new NavigatorSearch(new GenericQueryCondition("customfield_10002:previous").setQuery("-2d 30m")));
        executeBasicAssertJql("DT <= \"2d 30m\"",
                new String[] {"DT", "From anytime to 2 days, 30 minutes from now"},
                new NavigatorSearch(new GenericQueryCondition("customfield_10002:next").setQuery("2d 30m")));

        // 10003 = Free Text Field
        executeBasicAssertJql("FTF ~ query",
                new String[] {"FTF", "query"},
                new NavigatorSearch(new GenericQueryCondition("customfield_10003").setQuery("query")));

        // 10004 = Group Picker
        executeBasicAssertJql("GP = jira-administrators",
                new String[] {"GP", "jira-administrators"},
                new NavigatorSearch(new GenericQueryCondition("customfield_10004").setQuery("jira-administrators")));

        // 10005 = Import ID
        executeBasicAssertJql("II = \"10\"",
                new String[] {"II", "10"},
                new NavigatorSearch(new GenericQueryCondition("customfield_10005").setQuery("10")));

        // 10006 = Multi checkboxes
        executeBasicAssertJql("MC = opt1",
                new String[] {"MC", "opt1"},
                new NavigatorSearch(new GenericQueryCondition("customfield_10006").setQuery("10002")));

        // 10007 = Multi group picker
        executeBasicAssertJql("MGP = jira-users",
                new String[] { "MGP", "jira-users"},
                new NavigatorSearch(new GenericQueryCondition("customfield_10007").setQuery("jira-users")));

        // 10008 = Multi select
        executeBasicAssertJql("MS = select1",
                new String[] { "MS", "select1" },
                new NavigatorSearch(new GenericQueryCondition("customfield_10008").setQuery("10004")));

        // 10009 = Multi user picker
        executeBasicAssertJql("MUP = admin",
                new String[] { "MUP", ADMIN_USERNAME },
                new NavigatorSearch(new GenericQueryCondition("customfield_10009").setQuery(ADMIN_USERNAME)));

        // 10010 = number field
        executeBasicAssertJql("NF = \"10\"",
                new String[] {"NF", "10"},
                new NavigatorSearch(new GenericQueryCondition("customfield_10010").setQuery("10")));

        // 10011 = Project Picker
        executeBasicAssertJql("PP = EIGHT",
                new String[] { "PP", "eight" },
                new NavigatorSearch(new GenericQueryCondition("customfield_10011").setQuery("10017")));

        // 10012 = Radio buttons
        executeBasicAssertJql("RB = rad1",
                new String[] { "RB", "rad1" },
                new NavigatorSearch(new GenericQueryCondition("customfield_10012").setQuery("10006")));

        // 10013 = read only text field
        executeBasicAssertJql("ROTF ~ query",
                new String[] {"ROTF", "query"},
                new NavigatorSearch(new GenericQueryCondition("customfield_10013").setQuery("query")));

        // 10014 = select list
        executeBasicAssertJql("SL = select1",
                new String[] { "SL", "select1" },
                new NavigatorSearch(new GenericQueryCondition("customfield_10014").setQuery("10008")));

        // 10016 = text field
        executeBasicAssertJql("TF ~ query",
                new String[] {"TF", "query"},
                new NavigatorSearch(new GenericQueryCondition("customfield_10016").setQuery("query")));

        // 10017 = URL field
        executeBasicAssertJql("URL = \"http://query\"",
                new String[] {"URL", "http://query"},
                new NavigatorSearch(new GenericQueryCondition("customfield_10017").setQuery("http://query")));

        // 10018 = user picker
        executeBasicAssertJql("UP = admin",
                new String[] { "UP", ADMIN_USERNAME },
                new NavigatorSearch(new GenericQueryCondition("customfield_10018").setQuery(ADMIN_USERNAME)));
    }

    public void testCustomFieldsWithNonUniqueNames() throws Exception
    {
        // this XML has all custom fields present in global context
        administration.restoreData("TestCustomFieldsWithNonUniqueNames.xml");

        // 10000 = Cascading Select
        _testCustomFieldWithNonUniqueNames("10000",
                new NavigatorSearch(
                        new GenericQueryCondition("customfield_10000").setQuery("10000"),
                        new GenericQueryCondition("customfield_10000:1").setQuery("10001")
                ),
                "%s in cascadeOption(10000, 10001)",
                new String[] { "%s (parent)", "parent", "%s (child)", "child" });


        // 10001 = Date Picker
        _testCustomFieldWithNonUniqueNames("10001",
                new NavigatorSearch(new GenericQueryCondition("customfield_10001:before").setQuery("17/Jul/09")),
                "%s <= 2009-7-17",
                new String[] {"%s (before)", "17/Jul/09"});

        // 10002 = Date Time
        _testCustomFieldWithNonUniqueNames("10002",
                new NavigatorSearch(new GenericQueryCondition("customfield_10002:before").setQuery("21/Jul/09 11:07 AM")),
                "%s <= \"2009-07-21 11:07\"",
                new String[] {"%s (before)", "21/Jul/09 11:07 AM"});

        // 10003 = Free Text Field
        _testCustomFieldWithNonUniqueNames("10003",
                new NavigatorSearch(new GenericQueryCondition("customfield_10003").setQuery("query")),
                "%s ~ query",
                new String[] {"%s", "query"});

        // 10004 = Group Picker
        _testCustomFieldWithNonUniqueNames("10004",
                new NavigatorSearch(new GenericQueryCondition("customfield_10004").setQuery("jira-administrators")),
                "%s = jira-administrators",
                new String[] {"%s", "jira-administrators"});

        // 10005 = Import ID
        _testCustomFieldWithNonUniqueNames("10005",
                new NavigatorSearch(new GenericQueryCondition("customfield_10005").setQuery("10")),
                "%s = \"10\"",
                new String[] {"%s", "10"});

        // 10006 = Multi checkboxes
        _testCustomFieldWithNonUniqueNames("10006",
                new NavigatorSearch(new GenericQueryCondition("customfield_10006").setQuery("10002")),
                "%s = opt1",
                new String[] {"%s", "opt1"});

        // 10007 = Multi group picker
        _testCustomFieldWithNonUniqueNames("10007",
                new NavigatorSearch(new GenericQueryCondition("customfield_10007").setQuery("jira-users")),
                "%s = jira-users",
                new String[] {"%s", "jira-users"});

        // 10008 = Multi select
        _testCustomFieldWithNonUniqueNames("10008",
                new NavigatorSearch(new GenericQueryCondition("customfield_10008").setQuery("10004")),
                "%s = select1",
                new String[] {"%s", "select1"});

        // 10009 = Multi user picker
        _testCustomFieldWithNonUniqueNames("10009",
                new NavigatorSearch(new GenericQueryCondition("customfield_10009").setQuery(ADMIN_USERNAME)),
                "%s = admin",
                new String[] { "%s", ADMIN_USERNAME });

        // 10010 = number field
        _testCustomFieldWithNonUniqueNames("10010",
                new NavigatorSearch(new GenericQueryCondition("customfield_10010").setQuery("10")),
                "%s = \"10\"",
                new String[] {"%s", "10"});

        // 10011 = Project Picker
        _testCustomFieldWithNonUniqueNames("10011",
                new NavigatorSearch(new GenericQueryCondition("customfield_10011").setQuery("10017")),
                "%s = EIGHT",
                new String[] {"%s", "eight"});

        // 10012 = Radio buttons
        _testCustomFieldWithNonUniqueNames("10012",
                new NavigatorSearch(new GenericQueryCondition("customfield_10012").setQuery("10006")),
                "%s = rad1",
                new String[] {"%s", "rad1"});

        // 10013 = read only text field
        _testCustomFieldWithNonUniqueNames("10013",
                new NavigatorSearch(new GenericQueryCondition("customfield_10013").setQuery("query")),
                "%s ~ query",
                new String[] {"%s", "query"});

        // 10014 = select list
        _testCustomFieldWithNonUniqueNames("10014",
                new NavigatorSearch(new GenericQueryCondition("customfield_10014").setQuery("10008")),
                "%s = select1",
                new String[] {"%s", "select1"});

        // 10015 = single version picker
        VersionCondition versionCondition = new VersionCondition("customfield_10015");
        versionCondition.addOption("v1");
        _testCustomFieldWithNonUniqueNames("10015",
                new NavigatorSearch(new ProjectCondition().addProject("sixteen"), versionCondition),
                "project = SIXTEEN AND %s = v1",
                new String[] {"Project", "sixteen", "%s", "v1"});

        // 10016 = text field
        _testCustomFieldWithNonUniqueNames("10016",
                new NavigatorSearch(new GenericQueryCondition("customfield_10016").setQuery("query")),
                "%s ~ query",
                new String[] {"%s", "query"});

        // 10017 = URL field
        _testCustomFieldWithNonUniqueNames("10017",
                new NavigatorSearch(new GenericQueryCondition("customfield_10017").setQuery("http://query")),
                "%s = \"http://query\"",
                new String[] {"%s", "http://query"});

        // 10018 = user picker
        _testCustomFieldWithNonUniqueNames("10018",
                new NavigatorSearch(new GenericQueryCondition("customfield_10018").setQuery(ADMIN_USERNAME)),
                "%s = admin",
                new String[] { "%s", ADMIN_USERNAME });

        // 10019 = version picker
        versionCondition = new VersionCondition("customfield_10019");
        versionCondition.addOption("v1");
        _testCustomFieldWithNonUniqueNames("10019",
                new NavigatorSearch(new ProjectCondition().addProject("twenty"), versionCondition),
                "project = TWENTY AND %s = v1",
                new String[] {"Project", "twenty", "%s", "v1"});
    }

    private void _testCustomFieldWithNonUniqueNames(final String numericCustomFieldId, final NavigatorSearch search, final String jqlFormat, final String[] expectedSummary)
    {
        navigation.login(ADMIN_USERNAME);

        final List<String> expectedEnvironmentSummary = new ArrayList<String>();
        final List<String> expectedOtherSummary = new ArrayList<String>();
        for (String s : expectedSummary)
        {
            if (s.contains("%s"))
            {
                expectedEnvironmentSummary.add(String.format(s, "environment"));
                expectedOtherSummary.add(String.format(s, "OTHER"));
            }
            else
            {
                expectedEnvironmentSummary.add(s);
                expectedOtherSummary.add(s);
            }
        }

        // rename to a system field
        final String oldName = administration.customFields().renameCustomField(numericCustomFieldId, "environment");

        // assert JQL uses cf[] notation
        final String clauseNameCFNotation = "cf[" + numericCustomFieldId + "]";
        executeBasicAssertJql(String.format(jqlFormat, clauseNameCFNotation), expectedEnvironmentSummary, search);

        // rename to another custom field
        administration.customFields().renameCustomField(numericCustomFieldId, "OTHER");

        // assert JQL uses cf[] notation
        executeBasicAssertJql(String.format(jqlFormat, clauseNameCFNotation), expectedOtherSummary, search);

        navigation.login(FRED_USERNAME);

        // assert JQL uses regular notation because fred can't see OTHER field
        executeBasicAssertJql(String.format(jqlFormat, "OTHER"), expectedOtherSummary, search);

        // finally login as admin and clean up by renaming the thing back to normal
        navigation.login(ADMIN_USERNAME);

        administration.customFields().renameCustomField(numericCustomFieldId, oldName);
    }

    public void testSystemFieldsSimple() throws Exception
    {
        administration.restoreBlankInstance();
        administration.timeTracking().enable(TimeTracking.Mode.LEGACY);
        administration.generalConfiguration().enableVoting();
        administration.generalConfiguration().enableWatching();

        executeBasicAssertJql("project = MKY", new String[] {"Project", "monkey"}, new NavigatorSearch(new ProjectCondition().addProject("monkey")));
        executeBasicAssertJql("project in (HSP, MKY)", new String[] {"Project", "homosapien", "monkey"}, new NavigatorSearch(new ProjectCondition().addProject("homosapien").addProject("monkey")));

        executeBasicAssertJql("issuetype = Bug",
                new String[] {"Issue Type", "Bug"}, new NavigatorSearch(new IssueTypeCondition().addIssueType("Bug")));
        executeBasicAssertJql("issuetype in (Bug, Task)",
                new String[] {"Issue Type", "Bug", "Task"},
                new NavigatorSearch(new IssueTypeCondition().addIssueType("Bug").addIssueType("Task")));

        final QuerySearchCondition summarySearchCondition = new QuerySearchCondition("query");
        summarySearchCondition.addField(QuerySearchCondition.QueryField.SUMMARY);
        executeBasicAssertJql("summary ~ query",
                new String[] {"Query", "\"query\"", "Summary"},
                new NavigatorSearch(summarySearchCondition));

        final QuerySearchCondition descriptionSearchCondition = new QuerySearchCondition("query");
        descriptionSearchCondition.addField(QuerySearchCondition.QueryField.DESCRIPTION);
        executeBasicAssertJql("description ~ query",
                new String[] {"Query", "\"query\"", "Description"},
                new NavigatorSearch(descriptionSearchCondition));

        final QuerySearchCondition commentsSearchCondition = new QuerySearchCondition("query");
        commentsSearchCondition.addField(QuerySearchCondition.QueryField.COMMENTS);
        executeBasicAssertJql("comment ~ query",
                new String[] {"Query", "\"query\"", "Comments"},
                new NavigatorSearch(commentsSearchCondition));

        final QuerySearchCondition environmentSearchCondition = new QuerySearchCondition("query");
        environmentSearchCondition.addField(QuerySearchCondition.QueryField.ENVIRONMENT);
        executeBasicAssertJql("environment ~ query",
                new String[] {"Query", "\"query\"", "Environment"},
                new NavigatorSearch(environmentSearchCondition));

        executeBasicAssertJql("summary ~ query OR description ~ query OR comment ~ query OR environment ~ query",
                new String[] { "Query", "\"query\"", "in", "Description", "Environment", "Summary", "Comments" },
                new NavigatorSearchBuilder()
                        .addQueryString("query")
                        .addQueryField(QuerySearchCondition.QueryField.SUMMARY)
                        .addQueryField(QuerySearchCondition.QueryField.DESCRIPTION)
                        .addQueryField(QuerySearchCondition.QueryField.COMMENTS)
                        .addQueryField(QuerySearchCondition.QueryField.ENVIRONMENT)
                        .createSearch());

        executeBasicAssertJql("reporter = admin",
                new String[] { "Reporter", ADMIN_USERNAME },
                new NavigatorSearch(new ReporterCondition().setUser(ADMIN_USERNAME)));
        executeBasicAssertJql("",
                new String[] {""},
                new NavigatorSearch(new ReporterCondition().setAnyUser()));
        executeBasicAssertJql("reporter is EMPTY",
                new String[] {"Reporter", "No Reporter"},
                new NavigatorSearch(new ReporterCondition().setNoReporter()));
        executeBasicAssertJql("reporter = currentUser()",
                new String[] {"Reporter", "Current User"},
                new NavigatorSearch(new ReporterCondition().setCurrentUser()));

        executeBasicAssertJql("assignee = admin",
                new String[] { "Assignee", ADMIN_USERNAME },
                new NavigatorSearch(new AssigneeCondition().setUser(ADMIN_USERNAME)));
        executeBasicAssertJql("",
                new String[] {""},
                new NavigatorSearch(new AssigneeCondition().setAnyUser()));
        executeBasicAssertJql("assignee is EMPTY",
                new String[] {"Assignee", "Unassigned"},
                new NavigatorSearch(new AssigneeCondition().setNoReporter()));
        executeBasicAssertJql("assignee = currentUser()",
                new String[] {"Assignee", "Current User"},
                new NavigatorSearch(new AssigneeCondition().setCurrentUser()));

        executeBasicAssertJql("status = Open",
                new String[] {"Status", "Open"},
                new NavigatorSearch(new StatusCondition().addStatus(StatusCondition.Type.OPEN)));
        executeBasicAssertJql("status in (Open, Reopened)",
                new String[] {"Status", "Open", ",", "Reopened"},
                new NavigatorSearch(new StatusCondition().addStatus(StatusCondition.Type.OPEN).addStatus(StatusCondition.Type.REOPENED)));

        executeBasicAssertJql("resolution = Unresolved",
                new String[] {"Resolution", "Unresolved"},
                new NavigatorSearch(new ResolutionCondition().addResolution(ResolutionCondition.Type.UNRESOLVED)));
        executeBasicAssertJql("resolution = \"Cannot Reproduce\"",
                new String[] {"Resolution", "Cannot Reproduce"},
                new NavigatorSearch(new ResolutionCondition().addResolution(ResolutionCondition.Type.CANNOT_REPRODUCE)));
        executeBasicAssertJql("resolution in (Duplicate, \"Cannot Reproduce\")",
                new String[] {"Resolutions", "Duplicate", ",", "Cannot Reproduce"},
                new NavigatorSearch(new ResolutionCondition().addResolution(ResolutionCondition.Type.CANNOT_REPRODUCE).addResolution(ResolutionCondition.Type.DUPLICATE)));

        executeBasicAssertJql("priority = Blocker",
                new String[] {"Priorities", "Blocker"},
                new NavigatorSearch(new PriorityCondition().addPriority(PriorityCondition.Type.BLOCKER)));
        executeBasicAssertJql("priority in (Blocker, Critical)",
                new String[] {"Priorities", "Blocker", ",", "Critical"},
                new NavigatorSearch(new PriorityCondition().addPriority(PriorityCondition.Type.BLOCKER).addPriority(PriorityCondition.Type.CRITICAL)));

        executeBasicAssertJql("created <= 2009-07-17",
                new String[] {"Created Before", "17/Jul/09"},
                new NavigatorSearch(new GenericQueryCondition("created:before").setQuery("17/Jul/09")));
        executeBasicAssertJql("created >= 2009-07-17",
                new String[] {"Created After", "17/Jul/09"},
                new NavigatorSearch(new GenericQueryCondition("created:after").setQuery("17/Jul/09")));
        executeBasicAssertJql("created >= -1h",
                new String[] {"Created", "From 1 hour ago", "to", "anytime"},
                new NavigatorSearch(new GenericQueryCondition("created:previous").setQuery("-1h")));
        executeBasicAssertJql("created <= -1h",
                new String[] {"Created", "From anytime to 1 hour ago"},
                new NavigatorSearch(new GenericQueryCondition("created:next").setQuery("-1h")));

        executeBasicAssertJql("updated <= 2009-07-17",
                new String[] {"Updated Before", "17/Jul/09"},
                new NavigatorSearch(new GenericQueryCondition("updated:before").setQuery("17/Jul/09")));
        executeBasicAssertJql("updated >= 2009-07-17",
                new String[] {"Updated After", "17/Jul/09"},
                new NavigatorSearch(new GenericQueryCondition("updated:after").setQuery("17/Jul/09")));
        executeBasicAssertJql("updated >= -1h",
                new String[] {"Updated", "From 1 hour ago", "to", "anytime"},
                new NavigatorSearch(new GenericQueryCondition("updated:previous").setQuery("-1h")));
        executeBasicAssertJql("updated <= -1h",
                new String[] {"Updated", "From anytime to 1 hour ago"},
                new NavigatorSearch(new GenericQueryCondition("updated:next").setQuery("-1h")));

        executeBasicAssertJql("due <= 2009-7-17",
                new String[] {"Due Before", "17/Jul/09"},
                new NavigatorSearch(new GenericQueryCondition("duedate:before").setQuery("17/Jul/09")));
        executeBasicAssertJql("due >= 2009-7-17",
                new String[] {"Due After", "17/Jul/09"},
                new NavigatorSearch(new GenericQueryCondition("duedate:after").setQuery("17/Jul/09")));
        executeBasicAssertJql("due >= -1h",
                new String[] {"Due Date", "From 1 hour ago", "to", "anytime"},
                new NavigatorSearch(new GenericQueryCondition("duedate:previous").setQuery("-1h")));
        executeBasicAssertJql("due <= -1h",
                new String[] {"Due Date", "From anytime to 1 hour ago"},
                new NavigatorSearch(new GenericQueryCondition("duedate:next").setQuery("-1h")));

        executeBasicAssertJql("resolved <= 2009-07-17",
                new String[] {"Resolved Before", "17/Jul/09"},
                new NavigatorSearch(new GenericQueryCondition("resolutiondate:before").setQuery("17/Jul/09")));
        executeBasicAssertJql("resolved >= 2009-07-17",
                new String[] {"Resolved After", "17/Jul/09"},
                new NavigatorSearch(new GenericQueryCondition("resolutiondate:after").setQuery("17/Jul/09")));
        executeBasicAssertJql("resolved >= -1h",
                new String[] {"Resolved", "From 1 hour ago", "to", "anytime"},
                new NavigatorSearch(new GenericQueryCondition("resolutiondate:previous").setQuery("-1h")));
        executeBasicAssertJql("resolved <= -1h",
                new String[] {"Resolved", "From anytime to 1 hour ago"},
                new NavigatorSearch(new GenericQueryCondition("resolutiondate:next").setQuery("-1h")));

        executeBasicAssertJql("workratio >= \"10\"",
                new String[] { "Work Ratio Min", "10%" },
                new NavigatorSearch(new GenericQueryCondition("workratio:min").setQuery("10")));
        executeBasicAssertJql("workratio <= \"10\"",
                new String[] { "Work Ratio Max", "10%" },
                new NavigatorSearch(new GenericQueryCondition("workratio:max").setQuery("10")));
        executeBasicAssertJql("workratio >= \"10\" AND workratio <= \"50\"",
                new String[] { "Work Ratio Min", "10%", "Work Ratio Max", "50%" },
                new NavigatorSearch(
                        new GenericQueryCondition("workratio:min").setQuery("10"),
                        new GenericQueryCondition("workratio:max").setQuery("50")));
    }

    private String extractCFId(final String multiVersionCFId)
    {
        return multiVersionCFId.substring(multiVersionCFId.indexOf('_') + 1);
    }

    private void executeBasicAssertJql(final String expectedJqlQuery, final String[] expectedSummary, final NavigatorSearch search)
    {
        navigation.issueNavigator().createSearch(search);
        navigation.issueNavigator().gotoEditMode(IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        assertions.getIssueNavigatorAssertions().assertNoJqlErrors();
        assertions.getIssueNavigatorAssertions().assertAdvancedSearch(tester, expectedJqlQuery);

        tester.clickLink("viewfilter");
        // make sure that you can save the filter
        assertions.assertNodeExists("//a[@id='filtersavenew']");

        // verify that the summary is what we expect
        assertions.getTextAssertions().assertTextSequence(new XPathLocator(tester, "//div[@id='filter-summary']"), expectedSummary);
    }

    private void executeBasicAssertJql(final String expectedJqlQuery, final List<String> expectedSummary, final NavigatorSearch search)
    {
        final String[] expectedSummaryArray = new String[expectedSummary.size()];
        executeBasicAssertJql(expectedJqlQuery, expectedSummary.toArray(expectedSummaryArray), search);
    }
    
    private void assertJql(final NavigatorSearch search, String expectedJql, String... expectedJqlErrors)
    {
        doSearch(search);
        assertions.getIssueNavigatorAssertions().assertJqlErrors(expectedJqlErrors);
        assertExpectedJql(expectedJql);
    }
    
    private void assertJqlWarning(final NavigatorSearch search, String expectedJql, String... expectedJqlWarnings)
    {
        doSearch(search);
        assertions.getIssueNavigatorAssertions().assertJqlWarnings(expectedJqlWarnings);
        assertExpectedJql(expectedJql);
    }

    private void doSearch(final NavigatorSearch search)
    {
        navigation.issueNavigator().createSearch(search);
        navigation.issueNavigator().gotoEditMode(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);
        tester.clickLink("switchnavtype");
        navigation.issueNavigator().gotoEditMode(IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
    }
    
    private void assertExpectedJql(String expectedJql)
    {
        final String jqlValue = tester.getDialog().getFormParameterValue("jqlQuery");
        assertEquals(expectedJql, jqlValue);
    }
}
