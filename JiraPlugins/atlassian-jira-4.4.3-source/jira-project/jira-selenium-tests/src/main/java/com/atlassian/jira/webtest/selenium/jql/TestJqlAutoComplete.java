package com.atlassian.jira.webtest.selenium.jql;

import com.atlassian.jira.functest.framework.admin.plugins.EchoFunction;
import com.atlassian.jira.functest.framework.admin.plugins.ReferencePlugin;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.harness.util.DirtyFilterHandler;
import com.atlassian.selenium.Browser;
import com.atlassian.selenium.SkipInBrowser;
import com.atlassian.webtest.ui.keys.KeySequence;
import com.atlassian.webtest.ui.keys.TypeMode;

import static com.atlassian.webtest.ui.keys.Sequences.charsBuilder;

/**
 * Selenium tests for the JQL autocomplete.
 *
 * @since v4.0
 */
@SkipInBrowser (browsers = { Browser.IE }) // Unsaved changes confirmation - Responsibility: Hamish
@WebTest ( { Category.SELENIUM_TEST })
public class TestJqlAutoComplete extends AbstractTestJqlAutoComplete
{
    private static final String[] VERSION_VALUE_SUGGESTIONS = new String[] { "New Version 1", "New Version 2", "New Version 4", "New Version 5" };
    private static final String[] USER_VALUE_SUGGESTIONS = new String[] { "Administrator - admin@example.com (admin)", "Fred Normal - fred@example.com (fred)" };
    private static final String[] COMPONENT_VALUE_SUGGESTIONS = new String[] { "New Component 1", "New Component 2", "New Component 3", "New Component 4" };
    private static final String[] ISSUE_TYPE_VALUE_SUGGESTIONS = new String[] { "Bug", "Improvement", "New Feature", "Sub-task", "Task" };
    private static final String[] SEC_LEVEL_VALUE_SUGGESTIONS = new String[] { "Example Level 1", "Example Level 2", "Example Level 3" };
    private static final String[] MULTI_SELECT_VALUE_SUGGESTIONS = new String[] { "opt 1", "opt 2" };
    private static final String[] SAVED_FILTER_VALUE_SUGGESTIONS = new String[] { "Cross Project Filter for affectedVersion", "Cross Project Filter for component", "Cross Project Filter for fixVersion", "Cross Project Filter for level", "Cross Project Filter for parent",
            "Cross Project Filter for project", "Cross Project Filter for Project Picker CF", "Cross Project Filter for Version Picker CF", "Filter for affectedVersion", "Filter for assignee", "Filter for Cascading Select CF", "Filter for component",
            "Filter for description", "Filter for duedate", "Filter for environment" };
    private static final String[] SAVED_FILTER_BOLD_VALUE_SUGGESTIONS = new String[] { "Cross Project Filter for affectedVersion", "Cross Project Filter for component", "Cross Project Filter for fixVersion", "Cross Project Filter for level", "Cross Project Filter for parent",
            "Cross Project Filter for project", "Cross Project Filter for Project Picker CF", "Cross Project Filter for Version Picker CF" };
    private static final String[] CHECKBOX_VALUE_SUGGESTIONS = new String[] { "check 1", "check 2" };
    private static final String[] PRIORITY_VALUE_SUGGESTIONS = new String[] { "Blocker", "Critical", "Major", "Minor", "Trivial" };
    private static final String[] PROJECT_VALUE_SUGGESTIONS = new String[] { "homosapien (HSP)", "Invisible (INI)", "monkey (MKY)" };
    private static final String[] RADIO_VALUE_SUGGESTIONS = new String[] { "Radio Option 1", "Radio Option 2" };
    private static final String[] RESOLUTION_VALUE_SUGGESTIONS = new String[] { "Cannot Reproduce", "Duplicate", "Fixed", "Incomplete", "Unresolved", "Won't Fix" };
    private static final String[] SELECT_LIST_VALUE_SUGGESTIONS = new String[] { "option 1", "option 2", "option 3" };
    private static final String[] STATUS_VALUE_SUGGESTIONS = new String[] { "Closed", "In Progress", "Open", "Reopened", "Resolved" };
    private static final String[] LOGICAL_OPERATOR_VALUE_SUGGESTIONS = new String[] { "AND", "OR" };
    private static final String[] LOGICAL_OPERATORS_AND_ORDER_BY_VALUE_SUGGESTIONS = new String[] { "AND", "OR", "ORDER BY" };
    private static final String[] GROUP_VALUE_SUGGESTIONS = new String[] { "jira-administrators", "jira-developers", "jira-users" };
    private static final String[] CATEGORY_VALUE_SUGGESTIONS = new String[] { "Category 1", "Category 2", "Test Category" };

    private static final String[] VERSION_FUNCTION_SUGGESTIONS = new String[] { "releasedVersions()", "unreleasedVersions()" };
    private static final String[] ISSUE_TYPE_FUNCTION_SUGGESTIONS = new String[] { "standardIssueTypes()", "subTaskIssueTypes()" };
    private static final String[] CASCADE_FUNCTION_SUGGESTIONS = new String[] { "cascadeOption(\"\")" };
    private static final String[] COMPONENT_FUNCTION_SUGGESTIONS = new String[] { "componentsLeadByUser()" };
    private static final String[] PROJECT_FUNCTION_SUGGESTIONS = new String[] { "projectsLeadByUser()", "projectsWhereUserHasPermission(\"\")", "projectsWhereUserHasRole(\"\")" };
    private static final String[] USER_EQUALITY_FUNCTION_SUGGESTIONS = new String[] { "currentUser()" };
    private static final String[] USER_IN_FUNCTION_SUGGESTIONS = new String[] { "membersOf(\"\")" };
    private static final String[] ISSUE_FUNCTION_SUGGESTIONS = new String[] { "issueHistory()", "linkedIssues(\"\")", "votedIssues()", "watchedIssues()" };
    private static final String[] GROUP_FUNCTION_SUGGESTIONS = new String[] { };
    private static final String[] DATE_FUNCTION_SUGGESTIONS = new String[] { "currentLogin()", "endOfDay()", "endOfMonth()", "endOfWeek()", "endOfYear()", "lastLogin()", "now()", "startOfDay()", "startOfMonth()", "startOfWeek()", "startOfYear()" };

    private static final String JQLTEXT_INPUT = JQL_INPUT;
    private static final String CONTROL_CODE = "\\17";

    private boolean restoreHasRun = false;

    public void onSetUp()
    {
        super.onSetUp();
        // Lets only run the restore once since this whole TestClass is read-only and uses the same data set
        if (!restoreHasRun)
        {
            restoreData("TestSearchConstrainedByConfiguration.xml");
            restoreHasRun = true;
            getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
        }
        getNavigator().gotoFindIssuesAdvanced();
    }

    private KeySequence fastChars(String sequence)
    {
        return charsBuilder(sequence).typeMode(TypeMode.INSERT_WITH_EVENT).build();
    }

    public void testRandomBugFixes() throws Exception
    {
        _testNoSuggestionAfterAListOfValues();
        _testNoSuggestionsAfterLogicalOperators();
        _testNeedASpaceBetweenOrderByAndField();
        _testNoSuggestionsAfterCompletedValueInAList();
        _testFunctionInAListCanBeReplaced();
        _testFieldSuggestionsAfterOrLogicalOperator();
        _testSuggestionsAfterLogicalOperatorsOneSpace();
        _testValueSuggestionsInSingleQuotesWorks();
        _testCurrentUserNotShownWhenLoggedOut();
        _testPredicateAutoCompleteAfterWasIn();
        _testPredicateAutoCompleteAfterWasNotIn();
    }

      public void testPredicates() throws Exception
    {
        _testSuggestionsAfterPredicate();
        _testMultiplePredicates();
    }

    public void testCompletionWorks() throws Exception
    {
        _testOrderByCompletesCorrectly();

        // Lets build a few queries using the completion
        _testLongQueryNoNesting();
        _testLongQueryWithNesting();
        _testLongQueryWithLogicalNots();
        _testQueryWithSomeSpacesBewtweenStuff();
        _testQueryWithNoSpacesBewtweenStuff();
        _testQueryWithCustomFieldIds();
        _testSuggestionsKeyNavigation();
        _testQueryWithNots();

        //don't leave the form in a dirty state!
        client.click("jqlrunquery", true);
    }

    public void testFieldSuggestions() throws Exception
    {
        _testFieldSuggestions("a", new String[] { "affectedVersion", "assignee" });
        _testFieldSuggestions("c", new String[] { "Cascading Select CF - cf[10001]", "category", "comment", "component", "created", "createdDate" });
        _testFieldSuggestions("d", new String[] { "description", "due", "duedate" });
        _testFieldSuggestions("e", new String[] { "environment" });
        _testFieldSuggestions("f", new String[] { "filter", "fixVersion", "Free Text Field CF - cf[10010]" });
        _testFieldSuggestions("g", new String[] { "Group Picker CF - cf[10011]" });
        _testFieldSuggestions("i", new String[] { "id", "issue", "issuekey", "issuetype" });
        _testFieldSuggestions("k", new String[] { "key" });
        _testFieldSuggestions("l", new String[] { "labels", "level" });
        _testFieldSuggestions("m", new String[] { "Multi Checkboxes CF - cf[10012]", "Multi Group Picker CF - cf[10013]", "Multi Select CF - cf[10014]", "Multi User Picker CF - cf[10015]" });
        _testFieldSuggestions("n", new String[] { "Number Field CF - cf[10003]" });
        // Lets also check that we are suggested the NOT operator
        assertThat.elementNotPresent(FUNC_ID_PREFIX + 1);
        assertFunctionSuggestion(0, "NOT", "N");
        _testFieldSuggestions("o", new String[] { "originalEstimate" });
        _testFieldSuggestions("p", new String[] { "parent", "priority", "project", "Project Picker CF - cf[10016]" });
        _testFieldSuggestions("r", new String[] { "Radio Buttons CF - cf[10017]", "remainingEstimate", "reporter", "request", "resolution", "resolutiondate", "resolved" });
        _testFieldSuggestions("s", new String[] { "savedfilter", "searchrequest", "Select List CF - cf[10000]", "Single Version Picker CF - cf[10018]", "status", "summary" });
        _testFieldSuggestions("t", new String[] { "text", "Text Field 255 - cf[10019]", "timeestimate", "timeoriginalestimate", "timespent", "type" });
        _testFieldSuggestions("u", new String[] { "updated", "updatedDate", "URL Field CF - cf[10020]", "User Picker CF - cf[10002]" });
        _testFieldSuggestions("v", new String[] { "Version Picker CF - cf[10021]", "voter", "votes" });
        _testFieldSuggestions("w", new String[] { "watcher", "watchers", "workratio" });

        //don't leave the form in a dirty state!
        client.click("jqlrunquery", true);
    }

    public void testOperatorSuggestions() throws Exception
    {
        client.typeWithFullKeyEvents(JQLTEXT_INPUT, "affectedVersion ");
        assertSuggestionsOpen();
        assertThat.elementNotPresent(VALUE_ID_PREFIX + 10);

        assertValueSuggestion(0, "=");
        assertValueSuggestion(1, "!=");
        assertValueSuggestion(2, "<=");
        assertValueSuggestion(3, ">=");
        assertValueSuggestion(4, ">");
        assertValueSuggestion(5, "<");
        assertValueSuggestion(6, "is not");
        assertValueSuggestion(7, "is");
        assertValueSuggestion(8, "not in");
        assertValueSuggestion(9, "in");

        // Test correct bolding
        jqlLocator.element().type(fastChars("affectedVersion >"));
        assertSuggestionsOpen();
        assertThat.elementNotPresent(VALUE_ID_PREFIX + 2);
        assertValueSuggestion(0, ">=", ">");
        assertValueSuggestion(1, ">", ">");

        jqlLocator.element().clear().type(fastChars("affectedVersion <"));
        assertSuggestionsOpen();
        assertThat.elementNotPresent(VALUE_ID_PREFIX + 2);
        assertValueSuggestion(0, "<=", "<");
        assertValueSuggestion(1, "<", "<");

        jqlLocator.element().clear().type(fastChars("affectedVersion is"));
        assertSuggestionsOpen();
        assertThat.elementNotPresent(VALUE_ID_PREFIX + 2);
        assertValueSuggestion(0, "is not", "is");
        assertValueSuggestion(0, "is", "is");

        jqlLocator.element().clear().type(fastChars("affectedVersion !"));
        assertSuggestionsOpen();
        assertThat.elementNotPresent(VALUE_ID_PREFIX + 1);
        assertValueSuggestion(0, "!=", "!");

        // Lets have a go with a text based field
        jqlLocator.element().clear().type(fastChars("description "));
        assertSuggestionsOpen();
        assertThat.elementNotPresent(VALUE_ID_PREFIX + 4);

        assertValueSuggestion(0, "~");
        assertValueSuggestion(1, "!~");
        assertValueSuggestion(2, "is not");
        assertValueSuggestion(3, "is");

        // Test correct bolding
        client.typeWithFullKeyEvents(JQLTEXT_INPUT, "description i");
        assertSuggestionsOpen();
        assertThat.elementNotPresent(VALUE_ID_PREFIX + 2);
        assertValueSuggestion(0, "is not", "i");
        assertValueSuggestion(1, "is", "i");

        // NOTE: must use type because selenium will not send !
        client.type(JQLTEXT_INPUT, "description !");
        // Send a right arrow with an event to kick the js in
        client.keyPress(JQLTEXT_INPUT, "\\16");

        assertSuggestionsOpen();
        assertThat.elementNotPresent(VALUE_ID_PREFIX + 1);
        assertValueSuggestion(0, "!~", "!");


        // Try it with a field that does not exist so we have no limitations
        client.typeWithFullKeyEvents(JQLTEXT_INPUT, "blah ", true);
        assertSuggestionsOpen();
        assertThat.elementNotPresent(VALUE_ID_PREFIX + 15);

        assertValueSuggestion(0, "=");
        assertValueSuggestion(1, "!=");
        assertValueSuggestion(2, "~");
        assertValueSuggestion(3, "<=");
        assertValueSuggestion(4, ">=");
        assertValueSuggestion(5, ">");
        assertValueSuggestion(6, "<");
        assertValueSuggestion(7, "!~");
        assertValueSuggestion(8, "is not");
        assertValueSuggestion(9, "is");
        assertValueSuggestion(10, "not in");
        assertValueSuggestion(11, "in");
        assertValueSuggestion(12, "was");
        assertValueSuggestion(13, "was not");
        assertValueSuggestion(14, "was in");

        // Test correct bolding
        jqlLocator.element().clear().type(fastChars("blah >"));
        assertSuggestionsOpen();
        assertThat.elementNotPresent(VALUE_ID_PREFIX + 2);
        assertValueSuggestion(0, ">=", ">");
        assertValueSuggestion(1, ">", ">");

        jqlLocator.element().clear().type(fastChars("blah <"));
        assertSuggestionsOpen();
        assertThat.elementNotPresent(VALUE_ID_PREFIX + 2);
        assertValueSuggestion(0, "<=", "<");
        assertValueSuggestion(1, "<", "<");

        jqlLocator.element().clear().type(fastChars("blah is"));
        assertSuggestionsOpen();
        assertThat.elementNotPresent(VALUE_ID_PREFIX + 2);
        assertValueSuggestion(0, "is not", "is");
        assertValueSuggestion(1, "is", "is");

        jqlLocator.element().clear().type(fastChars("blah !"));
        assertSuggestionsOpen();
        assertThat.elementNotPresent(VALUE_ID_PREFIX + 2);
        assertValueSuggestion(0, "!=", "!");
        assertValueSuggestion(1, "!~", "!");

        _testStausWas();

        //don't leave the form in a dirty state!
        client.click("jqlrunquery", true);
    }

    public void testOperandSuggestions() throws Exception
    {
        ReferencePlugin referencePlugin = getWebUnitTest().getAdministration().plugins().referencePlugin();
        EchoFunction echoFunction = referencePlugin.getEchoFunction();

        //The reference plugin has some functions which causes problems for this test. We disable the functions for the
        //remainder of this test.
        final boolean referenceEnabled = referencePlugin.isInstalled() && echoFunction.isEnabled();
        if (referenceEnabled)
        {
            echoFunction.disable();
        }

        try
        {
            // Test that 'is' and 'is not' suggests empty only
            //_testEmptyOperandSuggestion("is");
            _testEmptyOperandSuggestion("is not");

            // NOTE: this also tests that the suggestions only show what you have permission to see, there is a project
            // in the backup that contains data that would be shown in these suggestions if the admin user had the browse
            // permission for that project.

            _testSuggestions("affectedVersion", VERSION_VALUE_SUGGESTIONS, VERSION_FUNCTION_SUGGESTIONS, "new", VERSION_VALUE_SUGGESTIONS);
            _testSuggestions("assignee", "=", USER_VALUE_SUGGESTIONS, USER_EQUALITY_FUNCTION_SUGGESTIONS, "ad", new String[] { USER_VALUE_SUGGESTIONS[0] });
            _testSuggestions("reporter", "in", USER_VALUE_SUGGESTIONS, USER_IN_FUNCTION_SUGGESTIONS);
            _testSuggestions("\"Cascading Select CF\"", new String[] { }, CASCADE_FUNCTION_SUGGESTIONS);
            _testSuggestions("category", CATEGORY_VALUE_SUGGESTIONS, NO_FUNCTION_SUGGESTIONS, "test", new String[] { CATEGORY_VALUE_SUGGESTIONS[2] });
            _testSuggestions("comment", "~", NO_VALUE_SUGGESTIONS, NO_FUNCTION_SUGGESTIONS);
            _testSuggestions("component", COMPONENT_VALUE_SUGGESTIONS, COMPONENT_FUNCTION_SUGGESTIONS, "new", COMPONENT_VALUE_SUGGESTIONS);
            _testSuggestions("created", "=", NO_VALUE_SUGGESTIONS, DATE_FUNCTION_SUGGESTIONS);
            _testSuggestions("createdDate", "=", NO_VALUE_SUGGESTIONS, DATE_FUNCTION_SUGGESTIONS);
            _testSuggestions("description", "~",  NO_VALUE_SUGGESTIONS, NO_FUNCTION_SUGGESTIONS);
            _testSuggestions("due", "=", NO_VALUE_SUGGESTIONS, DATE_FUNCTION_SUGGESTIONS);
            _testSuggestions("duedate", "=", NO_VALUE_SUGGESTIONS, DATE_FUNCTION_SUGGESTIONS);
            _testSuggestions("environment", "~",  NO_VALUE_SUGGESTIONS, NO_FUNCTION_SUGGESTIONS);
            _testSuggestions("filter", SAVED_FILTER_VALUE_SUGGESTIONS, NO_FUNCTION_SUGGESTIONS, "cross", SAVED_FILTER_BOLD_VALUE_SUGGESTIONS);
            _testSuggestions("fixVersion", VERSION_VALUE_SUGGESTIONS, VERSION_FUNCTION_SUGGESTIONS, "new", VERSION_VALUE_SUGGESTIONS);
            _testSuggestions("\"Free Text Field CF\"", "~", NO_VALUE_SUGGESTIONS, NO_FUNCTION_SUGGESTIONS);
            _testSuggestions("\"Group Picker CF\"", GROUP_VALUE_SUGGESTIONS, GROUP_FUNCTION_SUGGESTIONS, "jira-", GROUP_VALUE_SUGGESTIONS);
            _testSuggestions("id", NO_VALUE_SUGGESTIONS, ISSUE_FUNCTION_SUGGESTIONS);
            _testSuggestions("issue", NO_VALUE_SUGGESTIONS, ISSUE_FUNCTION_SUGGESTIONS);
            _testSuggestions("issuekey", NO_VALUE_SUGGESTIONS, ISSUE_FUNCTION_SUGGESTIONS);
            _testSuggestions("issuetype", ISSUE_TYPE_VALUE_SUGGESTIONS, ISSUE_TYPE_FUNCTION_SUGGESTIONS, "imp", new String[] { "Improvement" });
            _testSuggestions("key", NO_VALUE_SUGGESTIONS, ISSUE_FUNCTION_SUGGESTIONS);
            _testSuggestions("level", SEC_LEVEL_VALUE_SUGGESTIONS, NO_FUNCTION_SUGGESTIONS, "example", SEC_LEVEL_VALUE_SUGGESTIONS);
            _testSuggestions("\"Multi Checkboxes CF\"", CHECKBOX_VALUE_SUGGESTIONS, NO_FUNCTION_SUGGESTIONS, "check", CHECKBOX_VALUE_SUGGESTIONS);
            _testSuggestions("\"Multi Group Picker CF\"", GROUP_VALUE_SUGGESTIONS, GROUP_FUNCTION_SUGGESTIONS, "jira-", GROUP_VALUE_SUGGESTIONS);
            _testSuggestions("\"Multi Select CF\"", MULTI_SELECT_VALUE_SUGGESTIONS, NO_FUNCTION_SUGGESTIONS, "opt", MULTI_SELECT_VALUE_SUGGESTIONS);
            _testSuggestions("\"Multi User Picker CF\"", "=", USER_VALUE_SUGGESTIONS, USER_EQUALITY_FUNCTION_SUGGESTIONS, "ad", new String[] { USER_VALUE_SUGGESTIONS[0] });
            _testSuggestions("\"Multi User Picker CF\"", "in", USER_VALUE_SUGGESTIONS, USER_IN_FUNCTION_SUGGESTIONS, "ad", new String[] { USER_VALUE_SUGGESTIONS[0] });
            _testSuggestions("\"Number Field CF\"", NO_VALUE_SUGGESTIONS, NO_FUNCTION_SUGGESTIONS);
            _testSuggestions("parent", NO_VALUE_SUGGESTIONS, ISSUE_FUNCTION_SUGGESTIONS);
            _testSuggestions("priority", PRIORITY_VALUE_SUGGESTIONS, NO_FUNCTION_SUGGESTIONS, "m", new String[] { "Major", "Minor" });
            _testSuggestions("project", PROJECT_VALUE_SUGGESTIONS, PROJECT_FUNCTION_SUGGESTIONS, "i", new String[] { PROJECT_VALUE_SUGGESTIONS[1] });
            _testSuggestions("\"Project Picker CF\"", PROJECT_VALUE_SUGGESTIONS, PROJECT_FUNCTION_SUGGESTIONS, "i", new String[] { PROJECT_VALUE_SUGGESTIONS[1] });
            _testSuggestions("\"Radio Buttons CF\"", RADIO_VALUE_SUGGESTIONS, NO_FUNCTION_SUGGESTIONS, "radio", RADIO_VALUE_SUGGESTIONS);
            _testSuggestions("reporter", "=", USER_VALUE_SUGGESTIONS, USER_EQUALITY_FUNCTION_SUGGESTIONS, "ad", new String[] { USER_VALUE_SUGGESTIONS[0] });
            _testSuggestions("request", SAVED_FILTER_VALUE_SUGGESTIONS, NO_FUNCTION_SUGGESTIONS, "cross", SAVED_FILTER_BOLD_VALUE_SUGGESTIONS);
            _testSuggestions("resolution", RESOLUTION_VALUE_SUGGESTIONS, NO_FUNCTION_SUGGESTIONS, "can", new String[] { RESOLUTION_VALUE_SUGGESTIONS[0] });
            _testSuggestions("resolutiondate", "=", NO_VALUE_SUGGESTIONS, DATE_FUNCTION_SUGGESTIONS);
            _testSuggestions("resolved", "=", NO_VALUE_SUGGESTIONS, DATE_FUNCTION_SUGGESTIONS);
            _testSuggestions("savedfilter", SAVED_FILTER_VALUE_SUGGESTIONS, NO_FUNCTION_SUGGESTIONS, "cross", SAVED_FILTER_BOLD_VALUE_SUGGESTIONS);
            _testSuggestions("searchrequest", SAVED_FILTER_VALUE_SUGGESTIONS, NO_FUNCTION_SUGGESTIONS, "cross", SAVED_FILTER_BOLD_VALUE_SUGGESTIONS);
            _testSuggestions("\"Select List CF\"", SELECT_LIST_VALUE_SUGGESTIONS, NO_FUNCTION_SUGGESTIONS, "opt", SELECT_LIST_VALUE_SUGGESTIONS);
            _testSuggestions("\"Single Version Picker CF\"", VERSION_VALUE_SUGGESTIONS, VERSION_FUNCTION_SUGGESTIONS, "new", VERSION_VALUE_SUGGESTIONS);
            _testSuggestions("status", STATUS_VALUE_SUGGESTIONS, NO_FUNCTION_SUGGESTIONS, "re", new String[] { "Reopened", "Resolved" });
            _testSuggestions("summary", "~",  NO_VALUE_SUGGESTIONS, NO_FUNCTION_SUGGESTIONS);
            _testSuggestions("text", "~",  NO_VALUE_SUGGESTIONS, NO_FUNCTION_SUGGESTIONS);
            _testSuggestions("\"Text Field 255\"", "~",  NO_VALUE_SUGGESTIONS, NO_FUNCTION_SUGGESTIONS);
            _testSuggestions("type", ISSUE_TYPE_VALUE_SUGGESTIONS, ISSUE_TYPE_FUNCTION_SUGGESTIONS, "imp", new String[] { "Improvement" });
            _testSuggestions("updated", "=", NO_VALUE_SUGGESTIONS, DATE_FUNCTION_SUGGESTIONS);
            _testSuggestions("updatedDate", "=", NO_VALUE_SUGGESTIONS, DATE_FUNCTION_SUGGESTIONS);
            _testSuggestions("\"URL Field CF\"", NO_VALUE_SUGGESTIONS, NO_FUNCTION_SUGGESTIONS);
            _testSuggestions("\"User Picker CF\"", "=", USER_VALUE_SUGGESTIONS, USER_EQUALITY_FUNCTION_SUGGESTIONS, "ad", new String[] { USER_VALUE_SUGGESTIONS[0] });
            _testSuggestions("\"User Picker CF\"", "in", USER_VALUE_SUGGESTIONS, USER_IN_FUNCTION_SUGGESTIONS, "ad", new String[] { USER_VALUE_SUGGESTIONS[0] });
            _testSuggestions("\"Version Picker CF\"", VERSION_VALUE_SUGGESTIONS, VERSION_FUNCTION_SUGGESTIONS, "new", VERSION_VALUE_SUGGESTIONS);
            _testSuggestions("votes", NO_VALUE_SUGGESTIONS, NO_FUNCTION_SUGGESTIONS);
        }
        finally
        {
            if (referenceEnabled)
            {
                echoFunction.enable();
            }
        }

        //don't leave the form in a dirty state!
        client.click("jqlrunquery", true);
    }

    public void testLogicalOperatorsAndOrderBySuggestions() throws Exception
    {
        _testSuggestions("affectedVersion = \"My Version\" ", LOGICAL_OPERATORS_AND_ORDER_BY_VALUE_SUGGESTIONS, SUCCESS);
        _testSuggestions("((affectedVersion = \"My Version\" ", LOGICAL_OPERATOR_VALUE_SUGGESTIONS, PARSE_FAIL);
        _testSuggestions("((affectedVersion = \"My Version\" AND bla > blee) ", LOGICAL_OPERATOR_VALUE_SUGGESTIONS, PARSE_FAIL);
        _testSuggestions("(((affectedVersion = \"My Version\" AND bla > blee) ", LOGICAL_OPERATOR_VALUE_SUGGESTIONS, PARSE_FAIL);
        _testSuggestions("affectedVersion = \"My Version\" o", new String[] { "OR", "ORDER BY" }, PARSE_FAIL);
        _testSuggestions("affectedVersion = \"My Version\" or", new String[] { "OR", "ORDER BY" }, PARSE_FAIL);

        //don't leave the form in a dirty state!
        client.click("jqlrunquery", true);
    }

    public void testOrderBySuggestions() throws Exception
    {
        _testOrderByBoldSuggestions("a", new String[] { "affectedVersion", "assignee" });
        _testOrderByBoldSuggestions("c", new String[] { "Cascading Select CF - cf[10001]", "component", "created", "createdDate" });
        _testOrderByBoldSuggestions("d", new String[] { "description", "due", "duedate" });
        _testOrderByBoldSuggestions("e", new String[] { "environment" });
        _testOrderByBoldSuggestions("f", new String[] { "fixVersion", "Free Text Field CF - cf[10010]" });
        _testOrderByBoldSuggestions("g", new String[] { "Group Picker CF - cf[10011]" });
        _testOrderByBoldSuggestions("i", new String[] { "id", "issue", "issuekey", "issuetype" });
        _testOrderByBoldSuggestions("k", new String[] { "key" });
        _testOrderByBoldSuggestions("l", new String[] { "labels", "level" });
        _testOrderByBoldSuggestions("m", new String[] { "Multi Checkboxes CF - cf[10012]", "Multi Group Picker CF - cf[10013]", "Multi Select CF - cf[10014]", "Multi User Picker CF - cf[10015]" });
        _testOrderByBoldSuggestions("n", new String[] { "Number Field CF - cf[10003]" });
        _testOrderByBoldSuggestions("o", new String[] { "originalEstimate" });
        _testOrderByBoldSuggestions("p", new String[] { "priority", "progress", "project", "Project Picker CF - cf[10016]" });
        _testOrderByBoldSuggestions("r", new String[] { "Radio Buttons CF - cf[10017]", "remainingEstimate", "reporter", "resolution", "resolutiondate", "resolved" });
        _testOrderByBoldSuggestions("s", new String[] { "Select List CF - cf[10000]", "Single Version Picker CF - cf[10018]", "status", "subtasks", "summary" });
        _testOrderByBoldSuggestions("t", new String[] { "Text Field 255 - cf[10019]", "timeestimate", "timeoriginalestimate", "timespent", "type" });
        _testOrderByBoldSuggestions("u", new String[] { "updated", "updatedDate", "URL Field CF - cf[10020]", "User Picker CF - cf[10002]" });
        _testOrderByBoldSuggestions("v", new String[] { "Version Picker CF - cf[10021]", "votes" });
        _testOrderByBoldSuggestions("w", new String[] { "watchers", "workratio" });

        //don't leave the form in a dirty state!
        client.click("jqlrunquery", true);
    }

    // tests ((status in (Resolved,Reopened  ) OR savedfilter != "Cross Project Filter for component" ) OR project not in ( homosapien )) OR key in votedIssues() ORDER BY votes
    private void _testLongQueryWithNesting()
    {
        client.type(JQLTEXT_INPUT, "");

        // ((
        client.type(JQLTEXT_INPUT, "((");
        // status
        typeInStringAndComplete("s", 4);
        // in
        typeInStringAndComplete(" i", 2);
        // (Resolved
        typeInStringAndComplete(" r", 1);
        // ,
        client.typeWithFullKeyEvents(JQLTEXT_INPUT, ",", false);
        // Reopened
        typeInStringAndComplete("r", 0);
        // )
        client.typeWithFullKeyEvents(JQLTEXT_INPUT, ")", false);
        waitFor(2500);
        assertSuggestionsClosed();
        // OR
        typeInStringAndComplete(" ", 1);
        // savedfilter
        typeInStringAndComplete(" s", 0);
        // !=
        typeInStringAndComplete(" ", 1);
        // "Cross Project Filter for component"
        typeInStringAndComplete(" ", 1);
        // )
        client.typeWithFullKeyEvents(JQLTEXT_INPUT, " )", false);
        // OR
        typeInStringAndComplete(" ", 1);
        // project
        typeInStringAndComplete(" pr", 1);
        // not in
        typeInStringAndComplete(" n", 0);
        // (homosapien
        typeInStringAndComplete(" ", 0);
        // )
        client.typeWithFullKeyEvents(JQLTEXT_INPUT, ")", false);
        assertSuggestionsClosed();
        // )
        client.typeWithFullKeyEvents(JQLTEXT_INPUT, ")", false);
        waitFor(2500);
        assertSuggestionsClosed();
        // OR
        typeInStringAndComplete(" ", 1);
        // key
        typeInStringAndComplete(" k", 0);
        // in
        typeInStringAndComplete(" i", 2);
        // votedIssues()
        typeInStringAndCompleteFunction(" v", 0);
        // ORDER BY
        typeInStringAndComplete(" o", 1);
        // votes
        typeInStringAndComplete(" v", 1);
        // DESC
        typeInStringAndComplete(" ", 1);
        assertEquals("((status in (Resolved,Reopened) OR savedfilter != \"Cross Project Filter for component\" ) OR project not in (homosapien)) OR key in votedIssues() ORDER BY votes DESC", client.getValue(JQLTEXT_INPUT));
        // Lets verify that the query does parse
        SUCCESS.check();

        // Now lets change some stuff in the existing string

        // Lets change a field's value
        client.setCursorPosition(JQLTEXT_INPUT, "108");
        client.focus(JQLTEXT_INPUT);
        pressControl();
        assertSuggestionsOpen();
        // Choose the Invisible project in place of the homospien
        client.click(VALUE_ID_PREFIX + 1);
        assertEquals("((status in (Resolved,Reopened) OR savedfilter != \"Cross Project Filter for component\" ) OR project not in (Invisible)) OR key in votedIssues() ORDER BY votes DESC", client.getValue(JQLTEXT_INPUT));
        // Lets verify that the query does parse
        SUCCESS.check();

        // Lets change a field name
        client.setCursorPosition(JQLTEXT_INPUT, "36");
        client.focus(JQLTEXT_INPUT);
        pressControl();
        assertSuggestionsOpen();
        // Change savedfilter to searchrequest
        client.click(VALUE_ID_PREFIX + 1);
        assertEquals("((status in (Resolved,Reopened) OR searchrequest != \"Cross Project Filter for component\" ) OR project not in (Invisible)) OR key in votedIssues() ORDER BY votes DESC", client.getValue(JQLTEXT_INPUT));
        // Lets verify that the query does parse
        SUCCESS.check();

        // Lets change a logical operator
        client.setCursorPosition(JQLTEXT_INPUT, "32");
        client.focus(JQLTEXT_INPUT);
        pressControl();
        assertSuggestionsOpen();
        // Change the OR to AND
        client.click(VALUE_ID_PREFIX + 0);
        assertEquals("((status in (Resolved,Reopened) AND searchrequest != \"Cross Project Filter for component\" ) OR project not in (Invisible)) OR key in votedIssues() ORDER BY votes DESC", client.getValue(JQLTEXT_INPUT));
        // Lets verify that the query does parse
        SUCCESS.check();

        // Lets change a field value in a list with multiple values in that list
        client.setCursorPosition(JQLTEXT_INPUT, "13");
        client.focus(JQLTEXT_INPUT);
        pressControl();
        assertSuggestionsOpen();
        // Change the Resolved to Open
        client.click(VALUE_ID_PREFIX + 2);
        assertEquals("((status in (Open,Reopened) AND searchrequest != \"Cross Project Filter for component\" ) OR project not in (Invisible)) OR key in votedIssues() ORDER BY votes DESC", client.getValue(JQLTEXT_INPUT));
        // Lets verify that the query does parse
        SUCCESS.check();

    }

    private void pressControl()
    {
        client.keyPress(JQLTEXT_INPUT, CONTROL_CODE);
    }

    // tests not fixVersion >= "New Version 5" AND not ( not comment !~ now())
    private void _testLongQueryWithLogicalNots()
    {
        client.type(JQLTEXT_INPUT, "");
        // NOT
        typeInStringAndCompleteFunction("n", 0);
        // fixVersion
        typeInStringAndComplete(" f", 1);
        // >=
        typeInStringAndComplete(" >", 0);
        // "New Version 5"
        typeInStringAndComplete(" ", 3);
        // AND
        typeInStringAndComplete(" ", 0);
        // NOT
        typeInStringAndCompleteFunction(" n", 0);
        // (
        // ( - NOTE this is done this way because selenium sucks and does not type ( when you ask it to.
        client.type(JQLTEXT_INPUT, "NOT fixVersion >= \"New Version 5\" AND NOT (");
        // NOT
        typeInStringAndCompleteFunction(" n", 0);
        // comment
        typeInStringAndComplete(" c", 2);
        // !~
        typeInStringAndComplete(" ", 1);
        // "text does not exist"
        // NOTE this is done this way because selenium sucks and does not type " when you ask it to.
        client.type(JQLTEXT_INPUT, "NOT fixVersion >= \"New Version 5\" AND NOT ( NOT comment !~ \"text does not exist\"");
        // )
        client.typeWithFullKeyEvents(JQLTEXT_INPUT, ")", false);
        assertEquals("NOT fixVersion >= \"New Version 5\" AND NOT ( NOT comment !~ \"text does not exist\")", client.getValue(JQLTEXT_INPUT));
        // Lets verify that the query does parse
        SUCCESS.check();

        // Now lets change some stuff in the existing string

        // Lets change a field name
        client.setCursorPosition(JQLTEXT_INPUT, "48");
        client.typeWithFullKeyEvents(JQLTEXT_INPUT, "du", false);
        assertSuggestionsOpen();
        // Change comment to duedate
        client.click(VALUE_ID_PREFIX + 1);
        assertEquals("NOT fixVersion >= \"New Version 5\" AND NOT ( NOT duedate !~ \"text does not exist\")", client.getValue(JQLTEXT_INPUT));
        // Lets verify that the query does parse
        SUCCESS.check();

        // Lets change a logical operator
        client.setCursorPosition(JQLTEXT_INPUT, "34");
        client.focus(JQLTEXT_INPUT);
        pressControl();
        assertSuggestionsOpen();
        // Change the AND to OR
        client.click(VALUE_ID_PREFIX + 1);
        assertEquals("NOT fixVersion >= \"New Version 5\" OR NOT ( NOT duedate !~ \"text does not exist\")", client.getValue(JQLTEXT_INPUT));
        // Lets verify that the query does parse
        SUCCESS.check();

        // Lets change one of the not operators to a suggested value and see that the query will no longer parse
        client.setCursorPosition(JQLTEXT_INPUT, "43");
        client.focus(JQLTEXT_INPUT);
        pressControl();
        assertSuggestionsOpen();
        // Change the not to affectedVersion
        client.click(VALUE_ID_PREFIX + 0);
        assertEquals("NOT fixVersion >= \"New Version 5\" OR NOT ( affectedVersion duedate !~ \"text does not exist\")", client.getValue(JQLTEXT_INPUT));
        // Lets verify that the query does not parse
        PARSE_FAIL.check();

    }

    // Tests the query affectedVersion >= "New Version 1" OR "Multi Select CF" = "opt 1" ORDER BY key DESC, "Multi Select CF" ASC
    private void _testLongQueryNoNesting()
    {
        client.type(JQLTEXT_INPUT, "");
        // affectedVersion
        typeInStringAndComplete("aff", 0);
        // >=
        typeInStringAndComplete(" >", 0);
        // "New Version 1"
        typeInStringAndComplete(" ", 0);
        // OR
        typeInStringAndComplete(" o", 0);
        // "Multi Select CF"
        typeInStringAndComplete(" multi", 2);
        // =
        client.typeWithFullKeyEvents(JQLTEXT_INPUT, " =", false);
        waitFor(2500);
        assertSuggestionsClosed();
        // "opt 1"
        typeInStringAndComplete(" ", 0);
        // ORDER BY
        typeInStringAndComplete(" o", 1);
        // key
        typeInStringAndComplete(" k", 0);
        // DESC
        typeInStringAndComplete(" ", 1);
        // , "Multi Select CF"
        typeInStringAndComplete(", multi", 2);
        // ASC
        typeInStringAndComplete(" ", 0);
        assertEquals("affectedVersion >= \"New Version 1\" OR \"Multi Select CF\" = \"opt 1\" ORDER BY key DESC, \"Multi Select CF\" ASC", client.getValue(JQLTEXT_INPUT));
        // Lets verify that the query does parse
        SUCCESS.check();

        // Now lets change some stuff in the existing string

        // Lets change an order by direction
        client.setCursorPosition(JQLTEXT_INPUT, "79");
        client.focus(JQLTEXT_INPUT);
        pressControl();
        assertSuggestionsOpen();
        client.click(VALUE_ID_PREFIX + 0);
        assertEquals("affectedVersion >= \"New Version 1\" OR \"Multi Select CF\" = \"opt 1\" ORDER BY key ASC, \"Multi Select CF\" ASC", client.getValue(JQLTEXT_INPUT));
        // Lets verify that the query does parse
        SUCCESS.check();

        // Lets change an order by field
        client.setCursorPosition(JQLTEXT_INPUT, "75");
        client.focus(JQLTEXT_INPUT);
        pressControl();
        assertSuggestionsOpen();
        client.click(VALUE_ID_PREFIX + 1);
        assertEquals("affectedVersion >= \"New Version 1\" OR \"Multi Select CF\" = \"opt 1\" ORDER BY assignee ASC, \"Multi Select CF\" ASC", client.getValue(JQLTEXT_INPUT));
        // Lets verify that the query does parse
        SUCCESS.check();

        // Lets change a field's value
        client.setCursorPosition(JQLTEXT_INPUT, "23");
        client.focus(JQLTEXT_INPUT);
        pressControl();
        assertSuggestionsOpen();
        client.click(VALUE_ID_PREFIX + 2);
        assertEquals("affectedVersion >= \"New Version 4\" OR \"Multi Select CF\" = \"opt 1\" ORDER BY assignee ASC, \"Multi Select CF\" ASC", client.getValue(JQLTEXT_INPUT));
        // Lets verify that the query does parse
        SUCCESS.check();

        // Lets change a field name
        client.setCursorPosition(JQLTEXT_INPUT, "1");
        client.focus(JQLTEXT_INPUT);
        pressControl();
        assertSuggestionsOpen();
        client.click(VALUE_ID_PREFIX + 1);
        assertEquals("assignee >= \"New Version 4\" OR \"Multi Select CF\" = \"opt 1\" ORDER BY assignee ASC, \"Multi Select CF\" ASC", client.getValue(JQLTEXT_INPUT));
        // Lets verify that the query does parse
        SUCCESS.check();

        // Lets change a logical operator
        client.setCursorPosition(JQLTEXT_INPUT, "28");
        client.focus(JQLTEXT_INPUT);
        pressControl();
        assertSuggestionsOpen();
        client.click(VALUE_ID_PREFIX + 0);
        assertEquals("assignee >= \"New Version 4\" AND \"Multi Select CF\" = \"opt 1\" ORDER BY assignee ASC, \"Multi Select CF\" ASC", client.getValue(JQLTEXT_INPUT));
        // Lets verify that the query does parse
        SUCCESS.check();
    }

    // tests not fixVersion>="New Version 5" AND comment!~now()
    private void _testQueryWithSomeSpacesBewtweenStuff()
    {
        client.type(JQLTEXT_INPUT, "");

        // NOT
        typeInStringAndCompleteFunction("n", 0);
        // fixVersion
        typeInStringAndComplete(" f", 1);
        // >=
        typeInStringAndComplete(">", 0);
        // "New Version 5"
        typeInStringAndComplete("n", 3);
        // AND
        typeInStringAndComplete(" ", 0);
        // created
        typeInStringAndComplete(" c", 4);
        // >=
        typeInStringAndComplete(">", 0);
        // test
        client.typeWithFullKeyEvents(JQLTEXT_INPUT, "test", false);
        assertEquals("NOT fixVersion>=\"New Version 5\" AND created>=test", client.getValue(JQLTEXT_INPUT));
        // Lets verify that the query does parse
        SUCCESS.check();

        // Now lets change some stuff in the existing string

        // Lets change a field's value
        client.setCursorPosition(JQLTEXT_INPUT, "45");
        client.typeWithFullKeyEvents(JQLTEXT_INPUT, " ", false);
        assertSuggestionsOpen();
        // Choose the now() function in place of the string
        client.click(FUNC_ID_PREFIX + 6);
        assertEquals("NOT fixVersion>=\"New Version 5\" AND created>= now()", client.getValue(JQLTEXT_INPUT));
        // Lets verify that the query does parse
        SUCCESS.check();

        // Lets change the string a bit for testing
        client.type(JQLTEXT_INPUT, "NOT fixVersion>=\"New Version 5\" AND created>now()");

        // Lets change a operator
        client.setCursorPosition(JQLTEXT_INPUT, "44");
        client.focus(JQLTEXT_INPUT);
        pressControl();
        assertSuggestionsOpen();
        // Change > to >=
        client.click(VALUE_ID_PREFIX + 0);
        assertEquals("NOT fixVersion>=\"New Version 5\" AND created>=now()", client.getValue(JQLTEXT_INPUT));
        // Lets verify that the query does parse
        SUCCESS.check();

        // Lets change a field name
        client.setCursorPosition(JQLTEXT_INPUT, "4");
        client.typeWithFullKeyEvents(JQLTEXT_INPUT, "a", false);
        assertSuggestionsOpen();
        // Change the fixVersion to affectedVersion
        client.click(VALUE_ID_PREFIX + 0);
        assertEquals("NOT affectedVersion>=\"New Version 5\" AND created>=now()", client.getValue(JQLTEXT_INPUT));
        // Lets verify that the query does parse
        SUCCESS.check();
    }

    // tests !fixVersion>="New Version 5"&&comment>=test
    private void _testQueryWithNoSpacesBewtweenStuff()
    {
        // !
        client.type(JQLTEXT_INPUT, "!");

        // fixVersion
        typeInStringAndComplete("f", 1);
        // >=
        typeInStringAndComplete(">", 0);
        // "New Version 5"
        typeInStringAndComplete("n", 3);
        // &&
        client.type(JQLTEXT_INPUT, "!fixVersion>=\"New Version 5\"&&");
        // created
        typeInStringAndComplete("c", 4);
        // >=
        typeInStringAndComplete(">", 0);
        // test
        client.typeWithFullKeyEvents(JQLTEXT_INPUT, "test", false);
        assertEquals("!fixVersion>=\"New Version 5\"&&created>=test", client.getValue(JQLTEXT_INPUT));
        // Lets verify that the query does parse
        SUCCESS.check();

        // Now lets change some stuff in the existing string

        // Lets change a field's value
        client.setCursorPosition(JQLTEXT_INPUT, "39");
        client.typeWithFullKeyEvents(JQLTEXT_INPUT, " ", false);
        assertSuggestionsOpen();
        // Choose the now() function in place of the string
        client.click(FUNC_ID_PREFIX + 6);
        assertEquals("!fixVersion>=\"New Version 5\"&&created>= now()", client.getValue(JQLTEXT_INPUT));
        // Lets verify that the query does parse
        SUCCESS.check();

        // Lets change the string a bit for testing
        client.type(JQLTEXT_INPUT, "!fixVersion>=\"New Version 5\"&&created>now()");

        // Lets change a operator
        client.setCursorPosition(JQLTEXT_INPUT, "38");
        client.focus(JQLTEXT_INPUT);
        pressControl();
        assertSuggestionsOpen();
        // Change > to >=
        client.click(VALUE_ID_PREFIX + 0);
        assertEquals("!fixVersion>=\"New Version 5\"&&created>=now()", client.getValue(JQLTEXT_INPUT));
        // Lets verify that the query does parse
        SUCCESS.check();

        // Lets change a field name
        client.setCursorPosition(JQLTEXT_INPUT, "1");
        client.typeWithFullKeyEvents(JQLTEXT_INPUT, "a", false);
        assertSuggestionsOpen();
        // Change the fixVersion to affectedVersion
        client.click(VALUE_ID_PREFIX + 0);
        assertEquals("!affectedVersion>=\"New Version 5\"&&created>=now()", client.getValue(JQLTEXT_INPUT));
        // Lets verify that the query does parse
        SUCCESS.check();

        // Lets change a logical operator
        client.setCursorPosition(JQLTEXT_INPUT, "33");
        client.typeWithFullKeyEvents(JQLTEXT_INPUT, " ", false);
        assertSuggestionsOpen();
        // Change the && to AND
        client.click(VALUE_ID_PREFIX + 0);
        assertEquals("!affectedVersion>=\"New Version 5\" ANDcreated>=now()", client.getValue(JQLTEXT_INPUT));
        // Lets verify that the query does not parse
        PARSE_FAIL.check();
    }

    // tests cf[10018] = "New Version 5" AND cf[10016] = homosapien
    private void _testQueryWithCustomFieldIds()
    {
        // cf[10018] =
        client.type(JQLTEXT_INPUT, "cf[10018] = ");
        // Send a right arrow with an event to kick the js in
        client.keyPress(JQLTEXT_INPUT, "\\16");
        waitFor(2500);
        // "New Version 5"
        typeInStringAndComplete("n", 3);
        // AND
        typeInStringAndComplete(" ", 0);

        // cf[10016]
        client.typeWithFullKeyEvents(JQLTEXT_INPUT, " cf[10016] =", false);
        // homosapien
        typeInStringAndComplete(" ", 0);
        assertEquals("cf[10018] = \"New Version 5\" AND cf[10016] = homosapien", client.getValue(JQLTEXT_INPUT));
        // Lets verify that the query does parse
        SUCCESS.check();

        // Now lets change some stuff in the existing string

        // Lets change a field's value
        client.setCursorPosition(JQLTEXT_INPUT, "44");
        client.focus(JQLTEXT_INPUT);
        pressControl();
        assertSuggestionsOpen();
        // Choose the Invisible project
        client.click(VALUE_ID_PREFIX + 1);
        assertEquals("cf[10018] = \"New Version 5\" AND cf[10016] = Invisible", client.getValue(JQLTEXT_INPUT));
        // Lets verify that the query does parse
        SUCCESS.check();

        // Lets change a operator
        client.setCursorPosition(JQLTEXT_INPUT, "42");
        client.focus(JQLTEXT_INPUT);
        pressControl();
        assertSuggestionsOpen();
        // Change = to !=
        client.click(VALUE_ID_PREFIX + 1);
        assertEquals("cf[10018] = \"New Version 5\" AND cf[10016] != Invisible", client.getValue(JQLTEXT_INPUT));
        // Lets verify that the query does parse
        SUCCESS.check();

        // Lets change a field name
        client.setCursorPosition(JQLTEXT_INPUT, "33");
        client.focus(JQLTEXT_INPUT);
        pressControl();
        assertSuggestionsOpen();
        // Change the cf[10016] to category
        client.click(VALUE_ID_PREFIX + 1);
        assertEquals("cf[10018] = \"New Version 5\" AND category != Invisible", client.getValue(JQLTEXT_INPUT));
        // Lets verify that the query does parse
        SUCCESS.check();
    }

    // NOT fixVersion >= "New Version 5" AND (NOT
    private void _testQueryWithNots()
    {
        client.type(JQLTEXT_INPUT, "");
        // NOT
        typeInStringAndCompleteFunction("n", 0);
        // fixVersion
        typeInStringAndComplete(" f", 1);
        // >=
        typeInStringAndComplete(" >", 0);
        // "New Version 5"
        typeInStringAndComplete(" n", 3);
        // AND (
        client.type(JQLTEXT_INPUT, "NOT fixVersion >= \"New Version 5\" AND (");
        // Send a right arrow with an event to kick the js in
        client.keyPress(JQLTEXT_INPUT, "\\16");
        waitFor(2500);
        // NOT
        typeInStringAndCompleteFunction("n", 0);
        assertEquals("NOT fixVersion >= \"New Version 5\" AND (NOT", client.getValue(JQLTEXT_INPUT));
        // Lets verify that the query does not parse
        PARSE_FAIL.check();
    }

    private void _testNeedASpaceBetweenOrderByAndField() throws Exception
    {
        // we do not want to suggest fields after the order by until the user types a space, in fact it should not parse
        client.type(JQLTEXT_INPUT, "affectedVersion = \"New Version 1\" order bya");
        // Send a right arrow with an event to kick the js in
        client.keyPress(JQLTEXT_INPUT, "\\16");
        waitFor(2500);
        assertSuggestionsClosed();
        // Lets verify that the query does parse
        PARSE_FAIL.check();
    }

    public void testTabWhenNoSuggestionsSelectedCloseSuggestions() throws Exception
    {
        // We want the suggestion dropdown to close when a tab is pressed even if no item is selected
        // TODO should also check focus when we can get tabbing to work reliably
        // we do not want to suggest fields after the order by until the user types a space, in fact it should not parse
        client.type(JQLTEXT_INPUT, "aff");
        // Send a right arrow with an event to kick the js in
        client.keyPress(JQLTEXT_INPUT, "\\16");
        waitFor(2500);
        assertSuggestionsOpen();
        client.keyDown(JQLTEXT_INPUT, "\\9");
        client.seleniumKeyPress(JQLTEXT_INPUT, "\\9");
        assertSuggestionsClosed();
        // Lets verify that the query does parse
        PARSE_FAIL.check();
    }


    private void _testNoSuggestionsAfterCompletedValueInAList() throws Exception
    {
        // we do not want to suggest values after we have completed the value in a list or nested parens
        client.type(JQLTEXT_INPUT, "status in (Resolved,Reopened  ");
        // Send a right arrow with an event to kick the js in
        client.keyPress(JQLTEXT_INPUT, "\\16");
        waitFor(2500);
        assertSuggestionsClosed();
        // Lets verify that the query does parse
        PARSE_FAIL.check();
    }

    private void _testFunctionInAListCanBeReplaced() throws Exception
    {
        // we do not want to suggest values after we have completed the value in a list or nested parens
        client.type(JQLTEXT_INPUT, "parent in (\"TST-1\", issueHistory())");
        // Send a right arrow with an event to kick the js in
        client.setCursorPosition(JQLTEXT_INPUT, "20");
        client.keyPress(JQLTEXT_INPUT, "\\16");
        waitFor(2500);
        assertSuggestionsOpen();
        client.click(FUNC_ID_PREFIX + 2);
        // Lets make sure the string has changed correctly
        assertEquals("parent in (\"TST-1\", votedIssues())", client.getValue(JQLTEXT_INPUT));
        // Lets verify that the query does parse
        SUCCESS.check();
    }

    private void _testNoSuggestionsAfterLogicalOperators() throws Exception
    {
        // Fixes JRA-18092 - we do not want to suggest fields after a logical operator until the user types a space
        client.type(JQLTEXT_INPUT, "affectedVersion is not empty and");
        // Send a right arrow with an event to kick the js in
        client.keyPress(JQLTEXT_INPUT, "\\16");
        waitFor(2500);
        assertSuggestionsClosed();
        // Lets verify that the query does parse
        PARSE_FAIL.check();
    }

    private void _testSuggestionsAfterLogicalOperatorsOneSpace() throws Exception
    {
        // Fixes JRA-18383 - we want to suggest fields after a logical operator after a user types one space
        client.type(JQLTEXT_INPUT, "affectedVersion is not empty and ");
        // Send a right arrow with an event to kick the js in
        client.keyPress(JQLTEXT_INPUT, "\\16");
        waitFor(2500);
        assertSuggestionsOpen();
        // Lets verify that the query does parse
        PARSE_FAIL.check();

        // Test again with an OR
        client.type(JQLTEXT_INPUT, "affectedVersion is not empty or ");
        // Send a right arrow with an event to kick the js in
        client.keyPress(JQLTEXT_INPUT, "\\16");
        waitFor(2500);
        assertSuggestionsOpen();
        // Lets verify that the query does parse
        PARSE_FAIL.check();

        // Test again with a NOT
        client.type(JQLTEXT_INPUT, "affectedVersion is not empty and not ");
        // Send a right arrow with an event to kick the js in
        client.keyPress(JQLTEXT_INPUT, "\\16");
        waitFor(2500);
        assertSuggestionsOpen();
        // Lets verify that the query does parse
        PARSE_FAIL.check();
    }

    private void _testFieldSuggestionsAfterOrLogicalOperator() throws Exception
    {
        // we want to suggest fields after an OR logical operator, NOT ORDER BY
        client.type(JQLTEXT_INPUT, "a = b or ");
        // Send a right arrow with an event to kick the js in
        client.keyPress(JQLTEXT_INPUT, "\\16");
        waitFor(2500);
        assertSuggestionsOpen();
        assertValueSuggestion(0, "affectedVersion");
        // Lets verify that the query does parse
        PARSE_FAIL.check();
    }

    private void _testNoSuggestionAfterAListOfValues() throws Exception
    {
        // Tests a bug that used to exist
        // NOTE: must use type because selenium will not send ( or "
        client.type(JQLTEXT_INPUT, "affectedVersion in (\"New Version 2\", \"New Version 4\")");
        // Send a right arrow with an event to kick the js in
        client.keyPress(JQLTEXT_INPUT, "\\16");
        waitFor(2500);
        assertSuggestionsClosed();
        // Lets verify that the query does parse
        SUCCESS.check();
    }

    private void _testValueSuggestionsInSingleQuotesWorks() throws Exception
    {
        // JRA-18379 - single quotes should behave the same as double quotes
        _testSuggestions("affectedVersion = 'New Ve", VERSION_VALUE_SUGGESTIONS, PARSE_FAIL);
    }

    private void _testOrderByCompletesCorrectly()
    {
        // JRA-18007 - make sure the order by completes correctly
        client.type(JQLTEXT_INPUT, "");

        client.typeWithFullKeyEvents(JQLTEXT_INPUT, "afield = avalue order");
        assertSuggestionsOpen();
        assertThat.elementNotPresent(VALUE_ID_PREFIX + 1);
        assertValueSuggestion(0, "ORDER BY", "ORDER");
        // Lets complete the order by
        client.click(VALUE_ID_PREFIX + 0);
        assertSuggestionsClosed();
        assertEquals("afield = avalue ORDER BY", client.getValue(JQLTEXT_INPUT));
    }

    private void _testCurrentUserNotShownWhenLoggedOut() throws InterruptedException
    {
        // JRA-18377 - should not show the currentUser function when you are not logged in
//        client.getEval("this.browserbot.getCurrentWindow().onbeforeunload = null");
        client.selectWindow(null);
        new DirtyFilterHandler(context()).resetDirtyFilter();
        getNavigator().logout(getXsrfToken()).gotoHome().gotoFindIssues();
        _testSuggestions("assignee", "=", new String[] { }, new String[] { });
    }

    private void _testPredicateAutoCompleteAfterWasIn()
    {
        // JRADEV-6372 No autocomplaet after was in
        client.typeWithFullKeyEvents(JQLTEXT_INPUT, "status was in (Closed, Open)");
        // currentLogin()
        typeInStringAndComplete(" b", 0);
        assertEquals("status was in (Closed, Open) BEFORE", client.getValue(JQLTEXT_INPUT));
        // Lets verify that the query does not parse
        PARSE_FAIL.check();
    }

    private void _testPredicateAutoCompleteAfterWasNotIn()
    {
        // JRADEV-6372 No autocomplaet after was in
        client.typeWithFullKeyEvents(JQLTEXT_INPUT, "status was not in (Closed, Open)");
        // currentLogin()
        typeInStringAndComplete(" b", 1);
        assertEquals("status was not in (Closed, Open) BY", client.getValue(JQLTEXT_INPUT));
        // Lets verify that the query does not parse
        PARSE_FAIL.check();
    }

    private void _testSuggestionsKeyNavigation()
    {
        // Lets start with 'a' which will give us suggestions
        client.typeWithFullKeyEvents(JQLTEXT_INPUT, "a");
        assertSuggestionsOpen();
        // We should never select anything by default
        assertThat.elementNotPresent("//li[@id='" + VALUE_ID_PREFIX + 0 + "' and @class='active']");

        // Make sure that pressing enter will submit the form even through the suggestions are open, this is since no suggestion is highlighted
        client.seleniumKeyPress(JQLTEXT_INPUT, "\\13");
        client.waitForPageToLoad(PAGE_LOAD_WAIT);

        // Start over typing 'a'
        client.typeWithFullKeyEvents(JQLTEXT_INPUT, "a");
        assertSuggestionsOpen();
        // We should never select anything by default
        assertThat.elementNotPresent("//li[@id='" + VALUE_ID_PREFIX + 0 + "' and @class='active']");

        // Now send the down arrow and make sure we are on the selection
        client.keyPress(JQLTEXT_INPUT, "\\40");
        assertThat.elementPresentByTimeout("//li[@id='" + VALUE_ID_PREFIX + 0 + "' and @class='active']", DROP_DOWN_WAIT);

        // Now send the up arrow and make sure we go off of the selections again
        client.keyPress(JQLTEXT_INPUT, "\\38");
        assertThat.elementNotPresentByTimeout("//li[@id='" + VALUE_ID_PREFIX + 0 + "' and @class='active']", DROP_DOWN_WAIT);

        // Now go to the bottom element by pressing the up arrow (around the world)
        client.keyPress(JQLTEXT_INPUT, "\\38");
        assertThat.elementPresentByTimeout("//li[@id='" + VALUE_ID_PREFIX + 1 + "' and @class='active']", DROP_DOWN_WAIT);

        // Now go through the bottom and get back to the top (around the world the other way)
        client.keyPress(JQLTEXT_INPUT, "\\40");
        client.keyPress(JQLTEXT_INPUT, "\\40");
        assertThat.elementPresentByTimeout("//li[@id='" + VALUE_ID_PREFIX + 0 + "' and @class='active']", DROP_DOWN_WAIT);

        // Select the highlighted selection
        client.keyPress(JQLTEXT_INPUT, "\\13");
        assertSuggestionsClosed();
        assertEquals("affectedVersion", client.getValue(JQLTEXT_INPUT));
    }

    private void _testEmptyOperandSuggestion(final String operator)
    {
        client.type(JQLTEXT_INPUT, "affectedVersion " + operator + " ");
        // Send a right arrow with an event to kick the js in
        client.keyPress(JQLTEXT_INPUT, "\\16");
        // We need a little delay
        waitFor(2500);
        assertSuggestionsOpen();

        assertThat.elementNotPresent(VALUE_ID_PREFIX + 0);
        assertThat.elementNotPresent(FUNC_ID_PREFIX + 1);
        assertFunctionSuggestion(0, "EMPTY");
        // Lets verify that the query does not parse
        PARSE_FAIL.check();
    }

    private void _testStausWas()
    {

        jqlLocator.element().clear().type(fastChars("status was"));

        assertSuggestionsOpen();
        assertThat.elementNotPresent(OPERATOR_ID_PREFIX + 4);
        assertOperatorSuggestion(0, "was", "was");
        assertOperatorSuggestion(1, "was not", "was");
        assertOperatorSuggestion(2, "was in", "was");
        assertOperatorSuggestion(3, "was not in", "was");

    }

    private void _testMultiplePredicates()
    {
        client.typeWithFullKeyEvents(JQLTEXT_INPUT, "assignee was admin after '2010/09/10'");
        // currentLogin()
        typeInStringAndComplete(" b", 0);
        // now()
        typeInStringAndCompleteFunction(" n", 0);
        assertEquals("assignee was admin after '2010/09/10' BEFORE now()", client.getValue(JQLTEXT_INPUT));
        // Lets verify that the query does  parse
        SUCCESS.check();
        typeInStringAndComplete(" d", 0);
        assertEquals("assignee was admin after '2010/09/10' BEFORE now() DURING", client.getValue(JQLTEXT_INPUT));
        // should fail to parse now
        PARSE_FAIL.check();

    }

    private void _testSuggestionsAfterPredicate()
    {
        client.typeWithFullKeyEvents(JQLTEXT_INPUT, "status was open after");
        // currentLogin()
        typeInStringAndCompleteFunction(" c", 0);
        assertEquals("status was open after currentLogin()", client.getValue(JQLTEXT_INPUT));
        // Lets verify that the query does  parse
        SUCCESS.check();
    }

}
