package com.atlassian.jira.webtest.selenium.ajaxuserpicker;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.framework.dialogs.QuickCreateIssue;
import junit.framework.Test;

@WebTest({Category.SELENIUM_TEST })
public class TestIssueUserPicker extends AbstractTestAjaxUserPicker
{
    private static final String REPORTER = "reporter";
    private static final String REPORTER_KEY = REPORTER + ITEM_KEY;
    private static final String SINGLE_CUSTOM_FIELD = "customfield_10000";
    private static final String SINGLE_CUSTOM_FIELD_KEY = SINGLE_CUSTOM_FIELD + ITEM_KEY;

    private static final String NOUSERPICKERUSER = "nouserpickeruser";
    private static final String HSP_1 = "HSP-1";

    private QuickCreateIssue quickCreate;

    public static Test suite()
    {
        return suiteFor(TestIssueUserPicker.class);
    }

    @Override
    public void onSetUp()
    {
        super.onSetUp();
        quickCreate = new QuickCreateIssue(context());
        backdoor.darkFeatures().enableForSite("jira.no.frother.reporter.field");
    }

    @Override
    protected void onTearDown() throws Exception
    {
        backdoor.darkFeatures().disableForSite("jira.no.frother.reporter.field");
        super.onTearDown();
    }

    public void testUserPickerOnViewIssuePageShouldShowSuggestions()
    {
        openCreateIssuePage();
        client.type("summary", "A new bug");

        //clear the default reporter
        client.type(REPORTER, "");
        client.typeWithFullKeyEvents(REPORTER, "water");
        assertThat.visibleByTimeout(REPORTER_KEY + "rastusw", DROP_DOWN_WAIT);
        assertUsersShown(REPORTER_KEY, new String[] { "waterm", "rastusw" });

        //lets enter something into the single user picker
        client.typeWithFullKeyEvents(SINGLE_CUSTOM_FIELD, "water");
        assertThat.visibleByTimeout(SINGLE_CUSTOM_FIELD_KEY + "rastusw", DROP_DOWN_WAIT);
        assertUsersShown(SINGLE_CUSTOM_FIELD_KEY, new String[] { "waterm", "rastusw" });

        //lets enter something into the multi user picker
        client.typeWithFullKeyEvents(MULTI_CUSTOM_FIELD, "water");
        assertThat.visibleByTimeout(MULTI_CUSTOM_FIELD_KEY + "rastusw", DROP_DOWN_WAIT);
        assertUsersShown(MULTI_CUSTOM_FIELD_KEY, new String[] { "waterm", "rastusw" });
    }

    public void testUserPickerOnViewIssuePageShouldNotShowSuggestions()
    {
        openCreateIssuePage();
        client.type("summary", "A new bug");

        //clear the default reporter
        client.type(REPORTER, "");
        client.typeWithFullKeyEvents(REPORTER, "blargh");
        waitFor(DROP_DOWN_WAIT);
        //make sure none of the users show up.
        assertUsersShown(REPORTER_KEY, null);

        //lets enter something into the single user picker
        client.typeWithFullKeyEvents(SINGLE_CUSTOM_FIELD, "blargh");
        waitFor(DROP_DOWN_WAIT);
        //make sure none of the users show up.
        assertUsersShown(SINGLE_CUSTOM_FIELD_KEY, null);

        //lets enter something into the multi user picker
        client.typeWithFullKeyEvents(MULTI_CUSTOM_FIELD, "blargh");
        waitFor(DROP_DOWN_WAIT);
        //make sure none of the users show up.
        assertUsersShown(MULTI_CUSTOM_FIELD_KEY, null);
    }

    //test that a user without the global user picker permission cannot use the dropdown.
    public void testUserPickerOnViewIssuePageShouldNotShowSuggestionsForUsersWithNoPermission()
    {
        //grant permission to create issues and modify reporters.
        getAdministration().addPermissionForUser(NOUSERPICKERUSER, "Default Permission Scheme", "11");
        getAdministration().addPermissionForUser(NOUSERPICKERUSER, "Default Permission Scheme", "30");
        getNavigator().logout(getXsrfToken());

        login(NOUSERPICKERUSER);
        openCreateIssuePage();
        client.type("summary", "A new bug");

        //clear the default reporter
        client.type(REPORTER, "");
        client.typeWithFullKeyEvents(REPORTER, "water");
        waitFor(DROP_DOWN_WAIT);
        //make sure none of the users show up.
        assertUsersShown(REPORTER_KEY, null);

        //lets enter something into the single user picker
        client.typeWithFullKeyEvents(SINGLE_CUSTOM_FIELD, "water");
        waitFor(DROP_DOWN_WAIT);
        //make sure none of the users show up.
        assertUsersShown(SINGLE_CUSTOM_FIELD_KEY, null);

        //lets enter something into the multi user picker
        client.typeWithFullKeyEvents(MULTI_CUSTOM_FIELD, "water");
        waitFor(DROP_DOWN_WAIT);
        //make sure none of the users show up.
        assertUsersShown(MULTI_CUSTOM_FIELD_KEY, null);
    }

    public void testUserPickerSuggestionsAndSelectionWorksOnCreateIssuePage()
    {
        openCreateIssuePage();
        client.type("summary", "A new bug");

        //clear the default reporter
        client.type(REPORTER, "");
        client.typeWithFullKeyEvents(REPORTER, "fred");
        assertThat.visibleByTimeout(REPORTER_KEY + "fred", DROP_DOWN_WAIT);
        client.click(REPORTER_KEY + "fred");

        //lets enter something into the single user picker
        client.typeWithFullKeyEvents(SINGLE_CUSTOM_FIELD, "water");
        assertThat.visibleByTimeout(SINGLE_CUSTOM_FIELD_KEY + "rastusw", DROP_DOWN_WAIT);
        assertUsersShown(SINGLE_CUSTOM_FIELD_KEY, new String[] { "waterm", "rastusw" });
        client.click(SINGLE_CUSTOM_FIELD_KEY + "waterm");

        //lets enter something into the multi user picker
        client.typeWithFullKeyEvents(MULTI_CUSTOM_FIELD, "john");
        assertThat.visibleByTimeout(MULTI_CUSTOM_FIELD_KEY + "johnr", DROP_DOWN_WAIT);
        assertUsersShown(MULTI_CUSTOM_FIELD_KEY, new String[] { "johnr", "johnw" });
        client.click(MULTI_CUSTOM_FIELD_KEY + "johnr");

        client.typeWithFullKeyEvents(MULTI_CUSTOM_FIELD, "no", false);
        assertThat.visibleByTimeout(MULTI_CUSTOM_FIELD_KEY + "fred", DROP_DOWN_WAIT);
        assertUsersShown(MULTI_CUSTOM_FIELD_KEY, new String[] { "fred", "nouserpickeruser" });
        client.click(MULTI_CUSTOM_FIELD_KEY + "nouserpickeruser");

        client.click("Create", true);
        client.waitForPageToLoad();
        
        assertTrue(client.isTextPresent("Fred Normal"));
        assertTrue(client.isTextPresent("Water Seed"));
        assertTrue(client.isTextPresent("John Rotten"));
        assertTrue(client.isTextPresent("nouserpickeruser"));
    }

    public void testUserPickerSuggestionsAndSelectionWorksOnEditIssuePage()
    {
        getNavigator().editIssue(HSP_1);

        //clear the default reporter
        client.type(REPORTER, "");
        client.typeWithFullKeyEvents(REPORTER, "fred");
        assertThat.visibleByTimeout(REPORTER_KEY + "fred", DROP_DOWN_WAIT);
        client.click(REPORTER_KEY + "fred");

        //lets enter something into the single user picker
        client.typeWithFullKeyEvents(SINGLE_CUSTOM_FIELD, "water");
        assertThat.visibleByTimeout(SINGLE_CUSTOM_FIELD_KEY + "rastusw", DROP_DOWN_WAIT);
        assertUsersShown(SINGLE_CUSTOM_FIELD_KEY, new String[] { "waterm", "rastusw" });
        client.click(SINGLE_CUSTOM_FIELD_KEY + "waterm");

        //lets enter something into the multi user picker
        client.typeWithFullKeyEvents(MULTI_CUSTOM_FIELD, "john");
        assertThat.visibleByTimeout(MULTI_CUSTOM_FIELD_KEY + "johnr", DROP_DOWN_WAIT);
        assertUsersShown(MULTI_CUSTOM_FIELD_KEY, new String[] { "johnr", "johnw" });
        client.click(MULTI_CUSTOM_FIELD_KEY + "johnr");

        client.typeWithFullKeyEvents(MULTI_CUSTOM_FIELD, "no", false);
        assertThat.visibleByTimeout(MULTI_CUSTOM_FIELD_KEY + "fred", DROP_DOWN_WAIT);
        assertUsersShown(MULTI_CUSTOM_FIELD_KEY, new String[] { "fred", "nouserpickeruser" });
        client.click(MULTI_CUSTOM_FIELD_KEY + "nouserpickeruser");

        client.click("Update", true);
        assertThat.textPresent("Fred Normal");
        assertThat.textPresent("Water Seed");
        assertThat.textPresent("John Rotten");
        assertThat.textPresent("nouserpickeruser");
    }

    protected void login(String username)
    {
        getNavigator().login(username, username);
        client.waitForPageToLoad();
    }

    protected void openCreateIssuePage()
    {
        quickCreate.open();
        client.click(quickCreate.submitTriggerLocator());
        client.waitForPageToLoad();
    }
}
