package com.atlassian.jira.webtest.selenium.jql;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * These tests manipulate JIRA data and require an XML data restore for each method.
 *
 * @since v4.0
 */
@WebTest({Category.SELENIUM_TEST })
public class TestJqlAutoCompleteManipulatesJiraData extends AbstractTestJqlAutoComplete
{
/*
//Disabled until http://jdog.atlassian.com/browse/JRADEV-2602 is resolved

    public void testEnablingDisablingJqlAutocomplete() throws Exception
    {
        restoreData("TestEnablingDisablingJqlAutocomplete.xml");

        getNavigator().gotoFindIssuesAdvanced(getXsrfToken());

        // at the beginning, user and system have autocomplete enabled
        assertAutocompleteSuggestsSomething();
        new FormCleaner(client).cleanUpPage();

        // turn off autocomplete for the user
        client.click("toggleAutocompletePref");
        client.waitForPageToLoad();
        new FormCleaner(client).cleanUpPage();
        assertAutocompleteSuggestsNothing();

        // turn on autocomplete for the user
        client.click("toggleAutocompletePref");
        client.waitForPageToLoad();
        assertAutocompleteSuggestsSomething();

        // turn off autocomplete globally
        getWebUnitTest().getAdministration().generalConfiguration().setJqlAutocomplete(false);

        // the user no longer gets the option to choose their preference
        getNavigator().gotoFindIssues();
        assertAutocompleteSuggestsNothing();
        assertThat.elementNotPresent("toggleAutocompletePref");

        // turn on autocomplete globally
        getWebUnitTest().getAdministration().generalConfiguration().setJqlAutocomplete(true);

        // turn off autocomplete for the user
        getNavigator().gotoFindIssues();
        client.click("toggleAutocompletePref");
        client.waitForPageToLoad();

        // anonymous user should still be able to autocomplete because it is globally on
        getNavigator()
                .logout(getXsrfToken())
                .gotoHome()
                .gotoFindIssues();

        assertAutocompleteSuggestsSomething();

        // but anonymous cant toggle their preference
        assertThat.elementNotPresent("toggleAutocompletePref");

        // log in again as admin
        getNavigator()
                .login(ADMIN_USERNAME)
                .gotoFindIssues();

        // his preference is remembered, and he can alter it
        assertAutocompleteSuggestsNothing();
        assertThat.elementPresent("toggleAutocompletePref");
        client.click("id=jqlrunquery");

    }
*/
    private void assertAutocompleteSuggestsSomething() throws Exception
    {
        final String[] resolutionSuggestions = { "Cannot Reproduce", "Duplicate", "Fixed", "Incomplete", "Unresolved", "Won't Fix" };
        _testSuggestions("resolution", "=", resolutionSuggestions, NO_FUNCTION_SUGGESTIONS, "u", new String[] { resolutionSuggestions[4] });
    }

    private void assertAutocompleteSuggestsNothing()
    {
        // Test version auto complete
        client.type("jqltext", "resolution = ");
        // Send a right arrow with an event to kick the js in
        client.keyPress("jqltext", "\\16");
        // We need a little delay
        waitFor(2500);
        assertThat.elementNotPresentByTimeout("//div[@class='suggestions']", DROP_DOWN_WAIT);
    }

    public void testResolutionValuesEscaping() throws Exception
    {
        restoreBlankInstance();
        getNavigator().gotoFindIssuesAdvanced();

        getWebUnitTest().getAdministration().resolutions().addResolution("unRESOLVED");
        getWebUnitTest().getAdministration().resolutions().addResolution("\"UNresolved\"");
        final String[] resolutionSuggestions = new String[] { "\"\"UNresolved\"\"", "\"unRESOLVED\"", "Cannot Reproduce", "Duplicate", "Fixed", "Incomplete", "Unresolved", "Won't Fix" };
        _testSuggestions("resolution", "=", resolutionSuggestions, NO_FUNCTION_SUGGESTIONS, "u", new String[] { resolutionSuggestions[6] });
        client.click("id=jqlrunquery");

    }

    // Tests for multiple custom fields having the same name, also custom fields having the same name as a system field - JRA-18032
    public void testCustomFieldsWithSameNameAndConflictingWithSystemFields() throws Exception
    {
        restoreData("TestJqlAutoCompleteCustomFieldsSameName.xml");
        getNavigator().gotoFindIssuesAdvanced();

        // Lets make sure that both the custom field that have the same names is in the list, but complete to id's
        _testFieldSuggestions("select", new String[] { "Select List CF - cf[10000]", "Select List CF - cf[10040]" });
        // Lets complete the first one
        client.click(VALUE_ID_PREFIX + 0);
        assertEquals("cf[10000]", client.getValue("jqltext"));
        // Try again with the other one
        _testFieldSuggestions("select", new String[] { "Select List CF - cf[10000]", "Select List CF - cf[10040]" });
        client.click(VALUE_ID_PREFIX + 1);
        assertEquals("cf[10040]", client.getValue("jqltext"));

        // Lets make sure that the custom field with the system field name is in the list, but complete to its id
        _testFieldSuggestions("proj", new String[] { "project", "project - cf[10050]", "Project Picker CF - cf[10016]" });
        client.click(VALUE_ID_PREFIX + 1);
        assertEquals("cf[10050]", client.getValue("jqltext"));
        client.click("id=jqlrunquery");

    }

    public void testAutocompleteSuggestsFullTypedValueWhenThereAreOthersAndWhenThereIsOne() throws Exception
    {
        restoreData("TestJqlAutoCompleteCustomFieldsSameName.xml");
        getNavigator().gotoFindIssuesAdvanced();

        // Lets make sure that the custom field with the system field name is in the list, but complete to its id
        _testFieldSuggestions("project", new String[] { "project", "project - cf[10050]", "Project Picker CF - cf[10016]" });
        client.click(VALUE_ID_PREFIX + 1);
        assertEquals("cf[10050]", client.getValue("jqltext"));

        // Lets make sure that we do not see a suggestion when we have fully typed a field that has only one suggestion
        _testFieldSuggestions("affectedVersion", new String [] {"affectedVersion"});

        // Lets test this behavior for a field value as well
        _testSuggestions("affectedVersion in \"new version 1", new String[] {"New Version 1", "New Version 1.1"}, PARSE_FAIL);
        // Make sure when there is only one it does not suggest
        _testSuggestions("component = \"New Component 1", new String[] {"New Component 1"}, PARSE_FAIL);

        //don't leave the form in a dirty state!
        client.click("jqlrunquery", true);
    }

    public void testWithCrossSiteScriptingData() throws Exception
    {
        restoreData("TestJqlAutocompleteXSS.xml");
        getNavigator().gotoFindIssuesAdvanced();

        // There is a field with a div as a name, lets make sure the div is not a real DOM element
        assertFalse(client.isElementPresent("fucked"));
        // This same field has some bad options, lets get them suggested and make sure they don't become part of the DOM either
        _testSuggestions("\"<div id='fucked'>hello</div>Goofed\"", "=", new String [] {"<div id='fucked_1'>hello</div>opt1", "<div id='fucked_2'>hello</div>opt2"}, NO_FUNCTION_SUGGESTIONS);
        assertFalse(client.isElementPresent("fucked_1"));
        assertFalse(client.isElementPresent("fucked_2"));

        // Now lets see how we handle & and reserved JQL words
        client.type("jqltext", "&");
        client.keyPress("jqltext", "\\17");
        assertSuggestionsOpen();
        // complete the field called &&
        client.click(VALUE_ID_PREFIX + 0);
        client.typeWithFullKeyEvents("jqltext", " = ", false);
        assertSuggestionsOpen();
        assertValueSuggestion(0, "and");
        assertValueSuggestion(1, "not");
        assertValueSuggestion(2, "or");
        client.click(VALUE_ID_PREFIX + 0);

        assertEquals("\"&&\" = \"and\"", client.getValue("jqltext"));
        // Lets verify that the query does parse
        SUCCESS.check();
        client.click("jqlrunquery", true);
    }

    public void testWithCrazyQuotedData() throws Exception
    {
        // Addresses JRA-18275

        restoreData("TestJqlAutocompleteXSS.xml");
        getNavigator().gotoFindIssuesAdvanced();

        // Lets see what we are suggested for "\", should be two fields
        client.type("jqltext", "\"\\\"");
        // Send a right arrow with an event to kick the js in
        client.keyPress("jqltext", "\\16");
        waitFor(5000);
        // We need a little delay
        assertSuggestionsOpen();
        assertThat.elementNotPresent(VALUE_ID_PREFIX + 2);
        assertValueSuggestion(0, "\"\"Version\"\" - cf[10041]", "\"");
        assertValueSuggestion(1, "\"Version\" - cf[10040]", "\"");

        // Narrow it down with "\"\" should only be one
        client.type("jqltext", "\"\\\"\\\"");
        // Send a right arrow with an event to kick the js in
        client.keyPress("jqltext", "\\16");
        waitFor(5000);
        // We need a little delay
        assertSuggestionsOpen();
        assertThat.elementNotPresent(VALUE_ID_PREFIX + 1);
        assertValueSuggestion(0, "\"\"Version\"\" - cf[10041]", "\"\"");
        // Select the value
        client.click(VALUE_ID_PREFIX + 0);

        assertSuggestionsClosed();
        assertEquals("\"\\\"\\\"Version\\\"\\\"\"", client.getValue("jqltext"));

        // Lets check the crazy quotes on the value side
        client.type("jqltext", "\"\\\"\\\"Version\\\"\\\"\" = \"\\\"");
        // Send a right arrow with an event to kick the js in
        client.keyPress("jqltext", "\\16");
        waitFor(5000);
        // We need a little delay
        assertSuggestionsOpen();
        assertThat.elementNotPresent(VALUE_ID_PREFIX + 2);
        // We should see two options
        assertValueSuggestion(0, "\"\"quotequote\"\"", "\"");
        assertValueSuggestion(1, "\"quote\"", "\"");

        // Type one more quote and we should only have the one with the doubles
        client.type("jqltext", "\"\\\"\\\"Version\\\"\\\"\" = \"\\\"\\\"");
        // Send a right arrow with an event to kick the js in
        client.keyPress("jqltext", "\\16");
        waitFor(5000);
        // We need a little delay
        assertSuggestionsOpen();
        assertThat.elementNotPresent(VALUE_ID_PREFIX + 1);
        // We should see one options
        assertValueSuggestion(0, "\"\"quotequote\"\"", "\"\"");
        // Select the value
        client.click(VALUE_ID_PREFIX + 0);

        assertSuggestionsClosed();
        assertEquals("\"\\\"\\\"Version\\\"\\\"\" = \"\\\"\\\"quotequote\\\"\\\"\"", client.getValue("jqltext"));
        client.click("id=jqlrunquery");

    }

}
