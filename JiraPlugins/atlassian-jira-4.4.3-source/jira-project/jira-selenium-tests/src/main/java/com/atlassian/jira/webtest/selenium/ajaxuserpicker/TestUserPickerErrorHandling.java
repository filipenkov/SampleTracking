package com.atlassian.jira.webtest.selenium.ajaxuserpicker;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.Quarantine;
import com.atlassian.selenium.Browser;
import com.atlassian.selenium.SkipInBrowser;

/**
 * Tests the error handling logic of the Multi User Picker auto-complete.
 *
 * @since v4.1.1
 */
@SkipInBrowser(browsers={Browser.IE})  // JS Errors - Responsibility: JIRA Team
@WebTest({Category.SELENIUM_TEST })
@Quarantine
public class TestUserPickerErrorHandling extends AbstractTestAjaxUserPicker
{
    private static final long WAIT_FOR_USER_CLICK = 1000L;
    private static final long WAIT_FOR_ERROR_POPUP = 2000L;
    private static final String ERROR_POPUP_LOCATOR = "css=div.warningBox";

    @Override
    protected void restoreAjaxUserPickerData()
    {
        restoreData("TestAjaxUserPickerErrorHandling.xml");
    }

    public void testShowsErrorWhenValueMatchesFirstUser() throws Exception
    {
        // Matches exactly the username at the start of the line (no spaces), should show an error.
        _testWhereErrorMessageIsShown("fred", "fred", new String[]{"fred"});

        // Matches exactly the username at the start of the line (spaces), should show an error.
        _testWhereErrorMessageIsShown("  admin", "admin", new String[]{"admin_nouserpicker", "admin", "testadmin"});

        // Matches exactly the username at the end of the line (spaces after), should show an error.
        _testWhereErrorMessageIsShown("admin, fred   ", "fred", new String[]{"fred"});

        // Matches exactly a username in the middle of the field, should show an error.
        _testWhereErrorMessageIsShown("admin, fred, testadmin", "fred", new String[]{"fred"});

        // Matches exactly last username in the field, should show an error.
        _testWhereErrorMessageIsShown("admin, fred", "fred", new String[]{"fred"});

        // Partial Match with the first part of the username in the middle of the field, shouldn't show an error.
        _testWhereErrorMessageDoesntAppear("fred, admin_nouserpicker, johnw", "admin", new String[]{"admin_nouserpicker", "admin", "testadmin"});

        // Partial Match with the text within the last username in the field, shouldn't show an error.
        _testWhereErrorMessageDoesntAppear("fred, rastusw", "astus", new String[]{"astus"});

        // Partial Match with the last part of the username at the start of the field, shouldn't show an error.
        _testWhereErrorMessageDoesntAppear("testadmin", "admin", new String[]{"admin_nouserpicker", "admin", "testadmin"});
    }

    private void _testWhereErrorMessageIsShown(String initialTextToType, final String userToSelect, final String[] selectableUsers)
    {
        // go to Edit Issue screen
        getNavigator().editIssue(HSP_1);

        // we will type the initial text plus the name of the user we want to select
        final String fullTextToType = initialTextToType + ", " + userToSelect;
        client.typeWithFullKeyEvents(MULTI_CUSTOM_FIELD, fullTextToType);

        // assert that the user we wanted to select is visible as well as all the other possibilities
        assertThat.visibleByTimeout(MULTI_CUSTOM_FIELD_KEY + userToSelect, DROP_DOWN_WAIT);
        assertUsersShown(MULTI_CUSTOM_FIELD_KEY, selectableUsers);

        // select our user
        client.clickAndWaitForAjaxWithJquery(MULTI_CUSTOM_FIELD_KEY + userToSelect, WAIT_FOR_USER_CLICK);

        // user was previously entered - the error should be shown and the second occurrence of the username should be removed
        assertThat.visibleByTimeout(ERROR_POPUP_LOCATOR, WAIT_FOR_ERROR_POPUP);
        assertThat.elementDoesntHaveText("id=" + MULTI_CUSTOM_FIELD, fullTextToType);
        assertEquals(initialTextToType.trim() + ",", client.getValue("id=" + MULTI_CUSTOM_FIELD));

        // clear out the field so we can leave the page as it was
        client.type(MULTI_CUSTOM_FIELD, "");
    }

    private void _testWhereErrorMessageDoesntAppear(String initialTextToType, final String userToSelect, final String[] selectableUsers)
    {
        // go to Edit Issue screen
        getNavigator().editIssue(HSP_1);

        // we will type the initial text plus the name of the user we want to select
        final String fullTextToType = initialTextToType + ", " + userToSelect;
        client.typeWithFullKeyEvents(MULTI_CUSTOM_FIELD, fullTextToType);

        // assert that the user we wanted to select is visible as well as all the other possibilities
        assertThat.visibleByTimeout(MULTI_CUSTOM_FIELD_KEY + userToSelect, DROP_DOWN_WAIT);
        assertUsersShown(MULTI_CUSTOM_FIELD_KEY, selectableUsers);

        // select our user
        client.clickAndWaitForAjaxWithJquery(MULTI_CUSTOM_FIELD_KEY + userToSelect, WAIT_FOR_USER_CLICK);

        // user wasn't previously entered - no message should be displayed
        assertThat.elementNotPresentByTimeout(ERROR_POPUP_LOCATOR, WAIT_FOR_ERROR_POPUP);
        assertEquals(fullTextToType + ",", client.getValue("id=" + MULTI_CUSTOM_FIELD));

        // clear out the field so we can leave the page as it was
        client.type(MULTI_CUSTOM_FIELD, "");
    }
}
