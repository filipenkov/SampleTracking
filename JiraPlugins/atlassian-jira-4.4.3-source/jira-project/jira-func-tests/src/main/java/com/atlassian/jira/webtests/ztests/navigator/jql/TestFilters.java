package com.atlassian.jira.webtests.ztests.navigator.jql;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.navigation.IssueNavigatorNavigation;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.functest.framework.util.dom.DomKit;
import org.w3c.dom.Node;

import java.util.Arrays;
import java.util.List;

/**
 * Test saved filter behaviour with JQL.
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.JQL })
public class TestFilters extends FuncTestCase
{
    public void testEditSomeOneElsesFilter() throws Exception
    {
        administration.restoreData("TestJqlFiltersUpdatingFilter.xml");

        navigation.login(FRED_USERNAME);

        // since we are modifying a filter which is not our own, we have less operations available
        loadModifyFilter(10000, "reporter = admin", new String[] { "Reporter", ADMIN_USERNAME },
                new String[] {"Reload"}, new String[] {"Rename or Share", "Save", "Save as", "Subscriptions"});
    }

    /* Update a saved filter with a new query and verify that the UI displays changes properly */
    public void testUpdatingFilters() throws Exception
    {
        administration.restoreData("TestJqlFiltersUpdatingFilter.xml");

        // simple -> simple
        loadModifyFilter(10000, "reporter = admin", new String[] { "Reporter", ADMIN_USERNAME });
        tester.clickLink("filtersave");

        text.assertTextPresent(new XPathLocator(tester, "//table[contains(@class, 'aui')]"), "Old Search Request");
        text.assertTextPresent(new XPathLocator(tester, "//table[contains(@class, 'aui')]"), "Updated Search Request");

        // simple -> advanced
        final String q1 = "reporter = currentUser() or not assignee = admin";
        loadModifyFilter(10000, q1, new String[] { "JQL Query", q1 });
        checkJqlFilterDiff(
                Arrays.asList(
                        new DiffCharSequence(DiffType.diffcontext, "reporter = currentUser()")
                ),
                Arrays.asList(
                        new DiffCharSequence(DiffType.diffcontext, "reporter = currentUser()"),
                        new DiffCharSequence(DiffType.diffaddedchars, "or not assignee = admin")
                )
        );

        // advanced -> simple
        loadModifyFilter(10001, "project = hsp", new String[] { "Project", "homosapien" });
        checkJqlFilterDiff(
                Arrays.asList(
                        new DiffCharSequence(DiffType.diffremovedchars, "reporter"),
                        new DiffCharSequence(DiffType.diffcontext, "="),
                        new DiffCharSequence(DiffType.diffremovedchars, "currentUser() or assignee in membersOf(\"jira-users\")")),
                Arrays.asList(
                        new DiffCharSequence(DiffType.diffaddedchars, "project"),
                        new DiffCharSequence(DiffType.diffcontext, "="),
                        new DiffCharSequence(DiffType.diffaddedchars, "hsp")));

        // advanced -> advanced
        final String q2 = "duedate = now() or assignee in (admin,fred)";
        loadModifyFilter(10001, q2, new String[] { "JQL Query", q2});
        checkJqlFilterDiff(
                Arrays.asList(
                        new DiffCharSequence(DiffType.diffremovedchars, "reporter"),
                        new DiffCharSequence(DiffType.diffcontext, "="),
                        new DiffCharSequence(DiffType.diffremovedchars, "currentUser"),
                        new DiffCharSequence(DiffType.diffcontext, "() or assignee in"),
                        new DiffCharSequence(DiffType.diffremovedchars, "membersOf"),
                        new DiffCharSequence(DiffType.diffcontext, "("),
                        new DiffCharSequence(DiffType.diffremovedchars, "\"jira-users\""),
                        new DiffCharSequence(DiffType.diffcontext, ")")
                ),
                Arrays.asList(
                        new DiffCharSequence(DiffType.diffaddedchars, "duedate"),
                        new DiffCharSequence(DiffType.diffcontext, "="),
                        new DiffCharSequence(DiffType.diffaddedchars, "now"),
                        new DiffCharSequence(DiffType.diffcontext, "() or assignee in"),
                        new DiffCharSequence(DiffType.diffcontext, "("),
                        new DiffCharSequence(DiffType.diffaddedchars, "admin,fred"),
                        new DiffCharSequence(DiffType.diffcontext, ")")
                )
        );
    }

    // JRA-19422
    public void testRunFilterWithCustomFieldOptionWithNoAssociatedConfig() throws Exception
    {
        administration.restoreData("TestCustomFieldOptionsNoConfig.xml");

        // Just load the filter and make sure the screen does not explode, this tests data that used to explode
        navigation.issueNavigator().loadFilter(10000, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        tester.assertTextPresent("No matching issues found.");
    }

    /**
     * Loads a filter in view mode, edits it and checks that the UI reflects the changes to the filter. Assumes that the
     * user modifying the filter is the author of the filter (i.e. checks for default operations).
     *
     * @param filterId the id of the filter to load
     * @param newQuery the JQL query to modify the filter with
     * @param summary the new summary to verify
     */
    private void loadModifyFilter(final int filterId, final String newQuery, final String[] summary)
    {
        loadModifyFilter(filterId, newQuery, summary,
                new String[] { "Rename or Share", "Save", "Save as", "Reload", "Subscriptions" }, new String[]{});
    }

    /**
     * Loads a filter in view mode, edits it and checks that the UI reflects the changes to the filter.
     *
     * @param filterId the id of the filter to load
     * @param newQuery the JQL query to modify the filter with
     * @param summary the new summary to verify
     * @param visibleOperations the visibleOperations that should be available after modifying the filter
     */
    private void loadModifyFilter(final int filterId, final String newQuery, final String[] summary, final String[] visibleOperations, final String[] hiddenOperations )
    {
        navigation.issueNavigator().loadFilter(filterId, null);

        // modify query with new JQL
        navigation.issueNavigator().gotoEditMode(IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        tester.setFormElement("jqlQuery", newQuery);
        tester.submit();

        // check for 'filter modified' warning
        final Locator editDescriptionLocator = new IdLocator(tester, "filter-description");
        text.assertTextPresent(editDescriptionLocator, "Filter modified since loading");

        // goto view mode and make sure the message appears there as well
        navigation.issueNavigator().gotoViewMode();
        final Locator viwDescriptionLocator = new IdLocator(tester, "filter-description");
        text.assertTextPresent(viwDescriptionLocator, "Filter modified since loading");

        // assert that the summary reflects the new query
        assertions.getTextAssertions().assertTextSequence(new XPathLocator(tester, "//div[@id='filter-summary']"), summary);

        // assert visibleOperations
        assertions.getTextAssertions().assertTextSequence(new IdLocator(tester, "filteroperations"),
                visibleOperations);

        // assert hiddenOperations
        for (String operation : hiddenOperations)
        {
            assertions.getTextAssertions().assertTextNotPresent(new IdLocator(tester, "filteroperations"),
                    operation);
        }
    }

    private void checkJqlFilterDiff(final List<DiffCharSequence> expectedOldSearchRequest, final List<DiffCharSequence> expectedUpdatedSearchRequest)
    {
        tester.clickLink("filtersave");
        assertDiffSequence(new XPathLocator(tester, "//*[@id=\"dbJqlQuery\"]/span"), expectedOldSearchRequest);
        assertDiffSequence(new XPathLocator(tester, "//*[@id=\"currentJqlQuery\"]/span"), expectedUpdatedSearchRequest);
    }

    private void assertDiffSequence(final XPathLocator xPathLocator, final List<DiffCharSequence> diffSequenceList)
    {
        final Node[] nodes = xPathLocator.getNodes();
        assertEquals(diffSequenceList.size(), nodes.length);
        for (int i = 0; i < nodes.length; i++)
        {
            final String expectedString = diffSequenceList.get(i).sequence;
            final String actualString = DomKit.getRawText(nodes[i]);
            final String whitespaceNormalized = actualString.replaceAll("[\\s\\xa0]+", " ").trim(); // non-breaking whitespace (0xA0) is apparently not whitespace according to java's regex
            assertEquals(expectedString, whitespaceNormalized);

            final String expectedType = diffSequenceList.get(i).type.toString();
            final String actualType = DomKit.getRawText(nodes[i].getAttributes().getNamedItem("class"));
            assertEquals(expectedType, actualType);
        }
    }

    enum DiffType
    {
        diffremovedchars, diffcontext, diffaddedchars
    }

    class DiffCharSequence
    {
        final DiffType type;
        final String sequence;

        public DiffCharSequence(final DiffType type, final String sequence)
        {
            this.type = type;
            this.sequence = sequence;
        }
    }

    /* Verify that if we load a filter that has become invalid (e.g. refers to a project that has since been deleted) we handle
     * it gracefully
     */
    public void testInvalidatedFilters() throws Exception
    {
        administration.restoreData("TestJqlFilters.xml");
    
        loadInvalidatedJqlFilter(10000, "project = homosapien", "The value 'homosapien' does not exist for the field 'project'.", IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        loadInvalidatedJqlFilter(10001, "issuetype = Bug", "The value 'Bug' does not exist for the field 'issuetype'.", IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        loadInvalidatedJqlFilter(10010, "resolution = Duplicate", "The value 'Duplicate' does not exist for the field 'resolution'.", IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        loadInvalidatedJqlFilter(10011, "votes > 0", "Field 'votes' does not exist or you do not have permission to view it.", IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        loadInvalidatedJqlFilter(10014, "workratio = 10", "Field 'workratio' does not exist or you do not have permission to view it.", IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        loadInvalidatedJqlFilter(10020, "affectedVersion = \"1.0\"", "The value '1.0' does not exist for the field 'affectedVersion'.", IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        loadInvalidatedJqlFilter(10021, "fixVersion = \"1.0\"", "The value '1.0' does not exist for the field 'fixVersion'.", IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        loadInvalidatedJqlFilter(10022, "status = \"Not Used\"", "The value 'Not Used' does not exist for the field 'status'.", IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        loadInvalidatedJqlFilter(10023, "key = \"MKY-1\"", "An issue with key 'MKY-1' does not exist for field 'key'.", IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        loadInvalidatedJqlFilter(10024, "component = CompA", "The value 'CompA' does not exist for the field 'component'.", IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);

        loadInvalidatedJqlFilter(10025, "issue in watchedIssues()", "Function 'watchedIssues' cannot be called as watching issues is currently disabled.", IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        loadInvalidatedJqlFilter(10026, "issue in votedIssues()", "Function 'votedIssues' cannot be called as voting on issues is currently disabled.", IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        loadInvalidatedJqlFilter(10027, "issue in linkedIssues(\"MKY-2\")", "Function 'linkedIssues' cannot be called as issue linking is currently disabled.", IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        loadInvalidatedJqlFilter(10028, "parent = \"MKY-2\"", "Field 'parent' does not exist or you do not have permission to view it.", IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        // these "fit" so we need a slightly different test
        loadInvalidatedFilter(10029, "type in standardIssueTypes()", "Function 'standardIssueTypes' is invalid as sub-tasks are currently disabled.",  IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        loadInvalidatedFilter(10030, "type in subtaskIssueTypes()", "Function 'subTaskIssueTypes' is invalid as sub-tasks are currently disabled.",  IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        // 100031 was deleted
        loadInvalidatedJqlFilter(10032, "filter = 10031", "A value with ID '10031' does not exist for the field 'filter'.", IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);

        loadInvalidatedJqlFilter(10034, "originalEstimate >= 5h", "Field 'originalEstimate' does not exist or you do not have permission to view it.", IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        loadInvalidatedJqlFilter(10035, "remainingEstimate > 5h", "Field 'remainingEstimate' does not exist or you do not have permission to view it.", IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        loadInvalidatedJqlFilter(10036, "timespent > 5h", "Field 'timespent' does not exist or you do not have permission to view it.", IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);

        loadInvalidatedJqlFilter(10038, "affectedVersion in releasedVersions(deleted)", "Could not resolve the project 'deleted' provided to function 'releasedVersions'.", IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        loadInvalidatedJqlFilter(10039, "affectedVersion in unreleasedVersions(deleted)", "Could not resolve the project 'deleted' provided to function 'unreleasedVersions'.", IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        loadInvalidatedJqlFilter(10040, "level = SL1", "The value 'SL1' does not exist for the field 'level'.", IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);

        loadInvalidatedJqlFilter(10012, "reporter = fred", "Could not find username: fred", IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);
        loadInvalidatedJqlFilter(10013, "assignee = fred", "Could not find username: fred", IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);
        loadInvalidatedJqlFilter(10033, "reporter in membersOf(\"jira-developers\")", "Could not find group: jira-developers", IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);
    }

    public void testIllegalFilters() throws Exception
    {
        administration.restoreData("TestJqlIllegalFilters.xml");

        //JQL: cs ~ cs
        navigation.issueNavigator().loadFilter(10000, null);
        //Make sure there are no results returned.
        assertions.getIssueNavigatorAssertions().assertExactIssuesInResults();
        navigation.issueNavigator().gotoEditMode(IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        assertions.getIssueNavigatorAssertions().assertJqlErrors("The operator '~' is not supported by the 'cs' field.");
    }

    public void testInvalidateFilterCascadeSelect() throws Exception
    {
        administration.restoreBlankInstance();

        addCustomField("myfield", "com.atlassian.jira.plugin.system.customfieldtypes:cascadingselect");
        navigation.issue().createIssue("monkey", "Bug", "bug 1");
        navigation.issueNavigator().createSearch("myfield in cascadeOption(\"None\")");
        final long cssFilterId = navigation.issueNavigator().saveCurrentAsNewFilter("css", "exact text search", false, null);

        navigation.issueNavigator().loadFilter(cssFilterId, null);
        assertions.getIssueNavigatorAssertions().assertExactIssuesInResults("MKY-1");

        addCustomField("myfield", "com.atlassian.jira.plugin.system.customfieldtypes:textfield");
        assertInvalidFilterWithIssueKeys(cssFilterId, "myfield in cascadeOption(\"None\")", "The operator 'in' is not supported by the 'myfield' field.", "MKY-1");
    }

    public void testInvalidateFilterOperands() throws Exception
    {
        administration.restoreBlankInstance();

        addCustomField("myfield", "com.atlassian.jira.plugin.system.customfieldtypes:url");

        final String issue = navigation.issue().createIssue("monkey", "Bug", "bug 1");
        navigation.issue().setFreeTextCustomField(issue, "customfield_10000", "http://www.example.com");

        // another issue just to ensure we aren't returning all issues.
        navigation.issue().createIssue("monkey", "Bug", "bug 2");

        navigation.issueNavigator().createSearch("myfield = 'http://www.example.com'");
        final long exactTextFilterId = navigation.issueNavigator().saveCurrentAsNewFilter("field", "exact text search", false, null);

        navigation.issueNavigator().loadFilter(exactTextFilterId, null);
        assertions.getIssueNavigatorAssertions().assertExactIssuesInResults("MKY-1");

        // add another customfield with the same name so that '=' is no longer a valid operator in the saved filter
        addCustomField("myfield", "com.atlassian.jira.plugin.system.customfieldtypes:textfield");
        assertInvalidFilterWithIssueKeys(exactTextFilterId, "myfield = 'http://www.example.com'", "The operator '=' is not supported by the 'myfield' field.", "MKY-1");

        //// now do the same thing with relational operators <, <=, >, >=

        addCustomField("myfield_2", "com.atlassian.jira.plugin.system.customfieldtypes:float");

        navigation.issue().setFreeTextCustomField(issue, "customfield_10002", "5");

        navigation.issueNavigator().createSearch("myfield_2 > 0");
        final long gtFilterId = navigation.issueNavigator().saveCurrentAsNewFilter("myfield2", "relational search", false, null);
        navigation.issueNavigator().createSearch("myfield_2 >= 0");
        final long gteFilterId = navigation.issueNavigator().saveCurrentAsNewFilter("myfield gte", "relational search", false, null);
        navigation.issueNavigator().createSearch("myfield_2 < 10");
        final long ltFilterId = navigation.issueNavigator().saveCurrentAsNewFilter("myfield lt", "relational search", false, null);
        navigation.issueNavigator().createSearch("myfield_2 <= 10");
        final long lteFilterId = navigation.issueNavigator().saveCurrentAsNewFilter("myfield lte", "relational search", false, null);

        navigation.issueNavigator().loadFilter(gtFilterId, null);
        assertions.getIssueNavigatorAssertions().assertExactIssuesInResults("MKY-1");
        navigation.issueNavigator().loadFilter(gteFilterId, null);
        assertions.getIssueNavigatorAssertions().assertExactIssuesInResults("MKY-1");
        navigation.issueNavigator().loadFilter(ltFilterId, null);
        assertions.getIssueNavigatorAssertions().assertExactIssuesInResults("MKY-1");
        navigation.issueNavigator().loadFilter(lteFilterId, null);
        assertions.getIssueNavigatorAssertions().assertExactIssuesInResults("MKY-1");

        addCustomField("myfield_2", "com.atlassian.jira.plugin.system.customfieldtypes:userpicker");
        assertInvalidFilterWithIssueKeys(gtFilterId, "myfield_2 > 0", "The operator '>' is not supported by the 'myfield_2' field.", "MKY-1");
        assertInvalidFilterWithIssueKeys(gteFilterId, "myfield_2 >= 0", "The operator '>=' is not supported by the 'myfield_2' field.", "MKY-1");
        assertInvalidFilterWithIssueKeys(ltFilterId, "myfield_2 < 10", "The operator '<' is not supported by the 'myfield_2' field.", "MKY-1");
        assertInvalidFilterWithIssueKeys(lteFilterId, "myfield_2 <= 10", "The operator '<=' is not supported by the 'myfield_2' field.", "MKY-1");

        //// now the ~ operator
        addCustomField("myfield_3", "com.atlassian.jira.plugin.system.customfieldtypes:textfield");

        navigation.issue().setFreeTextCustomField(issue, "customfield_10004", "freetext");

        navigation.issueNavigator().createSearch("myfield_3 ~ freetext");
        final long likeFilterId = navigation.issueNavigator().saveCurrentAsNewFilter("myfield3", "like search", false, null);

        navigation.issueNavigator().loadFilter(likeFilterId, null);
        assertions.getIssueNavigatorAssertions().assertExactIssuesInResults("MKY-1");
        
        addCustomField("myfield_3", "com.atlassian.jira.plugin.system.customfieldtypes:userpicker");
        assertInvalidFilterWithIssueKeys(likeFilterId, "myfield_3 ~ freetext", "The operator '~' is not supported by the 'myfield_3' field.", "MKY-1");
    }

    private void assertInvalidFilterWithIssueKeys(final long filterId, final String jql, final String errorMessage, final String... issueKeys)
    {
        loadInvalidatedJqlFilter((int)filterId, jql, errorMessage, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);

        navigation.issueNavigator().loadFilter(filterId, null);
        assertions.getIssueNavigatorAssertions().assertExactIssuesInResults(issueKeys);
    }

    private void addCustomField(final String fieldName, final String customFieldName)
    {
        administration.customFields().addCustomField(customFieldName, fieldName);
        administration.reIndex();
    }

    // ORDER BY


    public void testValidSimpleFiltersSummary() throws Exception
    {
        administration.restoreData("TestJqlFilters.xml");

        loadFilterAndCheckSummary(10050, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED, "Project", "monkey", "Reporter", ADMIN_USERNAME);
        loadFilterAndCheckSummary(10050, IssueNavigatorNavigation.NavigatorEditMode.SIMPLE, "Project", "monkey", "Reporter", ADMIN_USERNAME);
    }

    public void testValidComplexFiltersSummary() throws Exception
    {
        administration.restoreData("TestJqlFilters.xml");

        loadFilterAndCheckSummary(10060, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED, "JQL Query", "issuetype = 3 or fixVersion = 10030");
        loadFilterAndCheckSummary(10060, IssueNavigatorNavigation.NavigatorEditMode.SIMPLE, "JQL Query", "issuetype = 3 or fixVersion = 10030");
    }

    private void loadFilterAndCheckSummary(final int filterId, IssueNavigatorNavigation.NavigatorEditMode fromMode, final String... summary)
    {
        navigation.issueNavigator().gotoNewMode(fromMode);
        navigation.issueNavigator().loadFilter(filterId, null);
        assertEquals(IssueNavigatorNavigation.NavigatorMode.SUMMARY, navigation.issueNavigator().getCurrentMode());
        text.assertTextSequence(new IdLocator(tester, "filter-summary"), summary);
    }

    private void loadInvalidatedJqlFilter(final int filterId, final String jqlQuery, final String errorMessage, final IssueNavigatorNavigation.NavigatorEditMode expectedEditMode)
    {
        loadInvalidatedFilter(filterId, jqlQuery , errorMessage, expectedEditMode);
    }

    private void loadInvalidatedFilter(final int filterId, final String summary, final String errorMessage, final IssueNavigatorNavigation.NavigatorEditMode expectedEditMode)
    {
        navigation.issueNavigator().createSearch("");
        navigation.issueNavigator().gotoEditMode(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);
        navigation.issueNavigator().loadFilter(filterId, null);
        assertEquals(IssueNavigatorNavigation.NavigatorMode.SUMMARY, navigation.issueNavigator().getCurrentMode());
        text.assertTextSequence(new IdLocator(tester, "issue-filter"), "Edit", "the current filter to correct errors");
        tester.clickLink("editfilter");
        assertEquals(IssueNavigatorNavigation.NavigatorMode.EDIT, navigation.issueNavigator().getCurrentMode());
        assertEquals(expectedEditMode, navigation.issueNavigator().getCurrentEditMode());
        if (navigation.issueNavigator().getCurrentEditMode().equals(IssueNavigatorNavigation.NavigatorEditMode.ADVANCED))
        {
            assertions.getIssueNavigatorAssertions().assertJqlErrors(errorMessage);
            text.assertTextPresent(new XPathLocator(tester, "//textarea[@id='jqltext']"), summary);
        }
        else
        {
            text.assertTextPresent(tester.getDialog().getResponseText(), errorMessage);
        }
    }

}
