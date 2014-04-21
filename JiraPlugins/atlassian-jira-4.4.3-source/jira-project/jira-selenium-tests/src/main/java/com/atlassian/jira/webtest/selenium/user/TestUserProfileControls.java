package com.atlassian.jira.webtest.selenium.user;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.impl.selenium.condition.IsPresentCondition;
import com.atlassian.jira.webtest.selenium.auidialog.AbstractAuiDialogTest;
import com.atlassian.selenium.Browser;
import com.atlassian.selenium.SkipInBrowser;
import junit.framework.Test;

import static com.atlassian.jira.webtest.framework.core.ConditionAssertions.byDefaultTimeout;
import static com.atlassian.jira.webtest.framework.core.TimedAssertions.assertThat;
import static com.atlassian.jira.webtest.framework.core.condition.Conditions.not;

/**
 *
 * @since v4.1
 */
@SkipInBrowser(browsers={Browser.IE}) //Element not found - Responsibility: JIRA Team
@WebTest({Category.SELENIUM_TEST })
public class TestUserProfileControls extends AbstractAuiDialogTest
{
    private static final int TIMEOUT = 10000;

    private static final String EDIT_PROFILE_FORM_ID = "edit-profile";
    private static final String CHANGE_PASSWORD_FORM_ID = "change-password";
    private static final String EDIT_PREFERENCES_FORM_ID = "update-user-preferences";

    private static final String LOCALE_GERMANY_CODE = "de_DE";
    private static final String LOCALE_GERMANY_TEXT = "Deutsch (Deutschland)";
    private static final String LOCALE_DEFAULT_CODE = "-1";
    private static final String LOCALE_DEFAULT_TEXT = "English (Australia) [Default]";
    
    private static final String FILTERS_DROPDOWN_LINK = "css=#filters.aui-dd-link";
    private static final String FILTERS_DROPDOWN_PLACEHOLDER = "css=#filters.aui-dd-link > ajs-layer-placeholder";
    private static final String OPEN_DROPDOWN_LOCATOR = "css=#filters_drop.ajs-layer.active";

    private static final String SUMMARY_TAB_ID = "up_user-profile-summary-panel";

    public static Test suite()
    {
        return suiteFor(TestUserProfileControls.class);
    }

    public void onSetUp()
    {
        super.onSetUp();
        restoreData("TestViewProfile.xml");
    }

    public void testUserProfile()
    {
        _testProfileForUser(true, "admin", "Administrator", "currentUser()");
        _testProfileForUser(false, "fred", "Fred Normal", "fred");

    }

    private void _testProfileForUser(boolean currentUser, String username, String fullName, String searchString)
    {

        getNavigator().gotoPage("secure/IssueNavigator!switchView.jspa?navType=advanced", true);

        gotoProfilePage(currentUser, username);
        assertThat.elementContainsText("css=#up-user-title", "Profile: " + fullName);
        assertThat.elementContainsText("css=#up-tab-title", "Summary");

        openFiltersDropDown();

        gotoProfileTab(currentUser, username, "jira.user.profile.panels:user-profile-summary-panel q");
        assertThat.elementContainsText("css=#up-user-title", "Profile: " + fullName);
        assertThat.elementContainsText("css=#up-tab-title", "Summary");

        assertThat.elementPresentByTimeout("css=#up-d-username", TIMEOUT);
        assertThat.elementContainsText("css=#up-d-username", username);

        openFiltersDropDown();
        assertThat.elementContainsText("css=#ass", "Assigned");
        assertThat.elementContainsText("css=#ass_open", "Assigned & Open");
        assertThat.elementContainsText("css=#ass_inprog", "Assigned & In Progress");
        assertThat.elementContainsText("css=#rep", "Reported");
        assertThat.elementContainsText("css=#rep_open", "Reported & Open");

        client.click("css=#ass", true);
        assertThat.elementContainsText("css=#jqltext", "assignee = " + searchString);

        gotoProfilePage(currentUser, username);
        openFiltersDropDown();
        assertThat.visibleByTimeout("css=#filters_drop", TIMEOUT);
        client.click("css=#ass_open", true);
        assertThat.elementContainsText("css=#jqltext", "resolution = Unresolved AND assignee = " + searchString);

        gotoProfilePage(currentUser, username);
        openFiltersDropDown();
        client.click("css=#ass_inprog", true);
        assertThat.elementContainsText("css=#jqltext", "status = \"3\" AND assignee = " + searchString);

        gotoProfilePage(currentUser, username);
        openFiltersDropDown();
        client.click("css=#rep", true);
        assertThat.elementContainsText("css=#jqltext", "reporter = " + searchString);

        gotoProfilePage(currentUser, username);
        openFiltersDropDown();
        client.click("css=#rep_open", true);
        assertThat.elementContainsText("css=#jqltext", "resolution = Unresolved AND reporter = " + searchString);

    }

    private void openFiltersDropDown()
    {
        assertThat(filtersDropdownLinkPresent(), byDefaultTimeout());
        client.click(FILTERS_DROPDOWN_LINK);
        assertThat(filtersDropdownOpen(), byDefaultTimeout());
    }

    private TimedCondition filtersDropdownLinkPresent()
    {
        return IsPresentCondition.forContext(context()).defaultTimeout(TIMEOUT).locator(FILTERS_DROPDOWN_LINK).build();
    }

    private TimedCondition filtersDropdownOpen()
    {
        return IsPresentCondition.forContext(context()).defaultTimeout(TIMEOUT).locator(OPEN_DROPDOWN_LOCATOR).build();
    }

    private TimedCondition filtersDropdownClosed()
    {
        return not(filtersDropdownOpen());
    }

    private void switchToSummaryPanel()
    {
        switchToTab(SUMMARY_TAB_ID);
    }

    private void switchToTab(String tabId)
    {
        client.click(tabLinkLocator(tabId));
        assertThat(tabLoaded(tabId), byDefaultTimeout());
    }

    private TimedCondition tabLoaded(String tabId)
    {
        return IsPresentCondition.forContext(context()).locator(loadedTabContainerLocator(tabId))
                .defaultTimeout(TIMEOUT).build();
    }

    private String tabLinkLocator(String tabId)
    {
        return "id=" + tabId + "_a";
    }

    private String loadedTabContainerLocator(String tabId)
    {
        return "css=#" + tabId + "_li.active.loaded";
    }

    public void testEditDetails()
    {
        shouldNotChangeUserDetailsGivenDialogClosedUsingCancel();
        shouldNotAllowChangeUserDetailsGivenEmptyFullName();
        shouldNotAllowChangeUserDetailsGivenEmptyEmail();
        shouldChangeUserDetailsGivenCorrectData();
        shouldRemoveNotificationWhenUserDetailsDialogIsOpened();
    }

    private void shouldNotChangeUserDetailsGivenDialogClosedUsingCancel()
    {
        goToEditProfileDialog();
        assertIsEditProfileDialog();
        insertUserDetails("Nick", "nick@atlassian.com", "admin");
        closeDialogByClickingCancel();
        assertNoUserDetailsChange();
        assertDefaulUserDetails();
    }

    private void shouldNotAllowChangeUserDetailsGivenEmptyFullName()
    {
        goToEditProfileDialog();
        assertIsEditProfileDialog();
        insertUserDetails("", "nick@atlassian.com", "admin");
        submitEditProfileDialog();
        assertIsEditProfileDialog();
        assertFieldHasInlineError("fullName", "Invalid full name specified");
    }

    private void shouldNotAllowChangeUserDetailsGivenEmptyEmail()
    {
        goToEditProfileDialog();
        assertIsEditProfileDialog();
        insertUserDetails("Nick", "", "admin");
        submitEditProfileDialog();
        assertIsEditProfileDialog();
        assertFieldHasInlineError("email", "Invalid email address format");
    }

    private void shouldChangeUserDetailsGivenCorrectData()
    {
        goToEditProfileDialog();
        assertIsEditProfileDialog();
        insertUserDetails("Nick", "nick@atlassian.com", "admin");
        submitEditProfileDialog();
        assertUserDetailsChange();
        assertUserDetails("Nick", "nick@atlassian.com");
    }

    private void shouldRemoveNotificationWhenUserDetailsDialogIsOpened()
    {
        goToEditProfileDialog();
        assertIsEditProfileDialog();
        insertUserDetails("Nick", "nick@atlassian.com", "admin");
        submitEditProfileDialog();
        assertUserDetailsChange();
        openEditProfileDialog();
        assertNoUserDetailsChange();
    }

    private void assertDefaulUserDetails()
    {
        assertUserDetails("Administrator", "admin@example.com");
    }


    public void testChangePassword()
    {
        shouldNotChangePasswordGivenCancelClicked();
        shouldNotAllowChangePasswordGivenEmptyCurrent();
        shouldNotAllowChangePasswordGivenEmptyNew();
        shouldNotAllowChangePasswordGivenInvalidConfirmation();
        shouldChangePasswordGivenCorrectData();
        shouldRemoveNotificationWhenChangePasswordDialogIsOpened();
        shouldNotAllowChangeOtherUserPassword();
    }

    private void shouldNotChangePasswordGivenCancelClicked()
    {
        goToChangePasswordDialog();
        assertIsChangePasswordDialog();
        insertPasswordChange("admin", "admin2");
        closeDialogByClickingCancel();
        assertDialogNotOpen();
        assertNoUserDetailsChange();
    }

    private void shouldNotAllowChangePasswordGivenEmptyCurrent()
    {
        goToChangePasswordDialog();
        assertIsChangePasswordDialog();
        doNotInsertAnything();
        submitChangePasswordDialog();
        assertFieldHasInlineError("current", "The current password specified is incorrect.");
    }

    private void shouldNotAllowChangePasswordGivenEmptyNew()
    {
        goToChangePasswordDialog();
        assertIsChangePasswordDialog();
        insertPasswordChange("admin", "");
        submitChangePasswordDialog();
        assertFieldHasInlineError("password", "The new password cannot be blank.");
    }

    private void shouldNotAllowChangePasswordGivenInvalidConfirmation()
    {
        goToChangePasswordDialog();
        assertIsChangePasswordDialog();
        insertPasswordChange("admin", "something", "somethingelse");
        submitChangePasswordDialog();
        assertFieldHasInlineError("confirm", "The password and confirmation do not match.");
    }

    private void shouldChangePasswordGivenCorrectData()
    {
        goToChangePasswordDialog();
        assertIsChangePasswordDialog();
        // as for now JIRA does not validate if new password is different than the current one
        insertPasswordChange("admin", "admin", "admin");
        submitChangePasswordDialog();
        assertDialogNotOpen();
        assertUserDetailsChange();
    }

    private void shouldRemoveNotificationWhenChangePasswordDialogIsOpened()
    {
        goToChangePasswordDialog();
        assertIsChangePasswordDialog();
        insertPasswordChange("admin", "admin", "admin");
        submitChangePasswordDialog();
        assertDialogNotOpen();
        assertUserDetailsChange();
        openChangePasswordDialog();
        assertIsChangePasswordDialog();
        assertNoUserDetailsChange();
    }

    private void shouldNotAllowChangeOtherUserPassword()
    {
        gotoProfilePage(false, "fred");
        assertThat.elementNotPresent("id=up-d-change-password");
    }


    public void testEditPreferences()
    {
        shouldNotChangePreferencesGivenDialogClosedUsingCancel();
        shouldChangePageSizePreferenceGivenCorrectValue();
        shouldChangeEmailTypePreferenceGivenCorrectValue();
        shouldChangeLocalePreferenceGivenCorrectValue();
        shouldChangeNotificationPreferenceGivenCorrectValue();
        shouldChangeSharingPreferenceGivenCorrectValue();
        shouldChangeKeyboardShortcutPreferenceGivenCorrectValue();
        shouldRemoveNotificationWhenEditPreferencesDialogIsOpened();
        shouldNotAllowOtherUserEditPreferences();
    }

    private void shouldNotChangePreferencesGivenDialogClosedUsingCancel()
    {
        gotoProfilePage(true, "admin");
        assertDefaultPreferencesValues();
        goToEditPreferencesDialog();
        assertIsEditPreferencesDialog();
        setNewPreferencesValues();
        closeDialogByClickingCancel();
        assertNoUserPreferencesChange();
    }
    
    private void assertDefaultPreferencesValues()
    {
        assertPageSizePreference(50);
        assertEmailTypePreference("Text");
        assertLocalePreference(LOCALE_DEFAULT_TEXT);
        assertChangeNotificationPreference(true);
        assertSharingPreference(true);
    }

    private void setNewPreferencesValues()
    {
        setPageSizePreference(20);
        setEmailTypePreference("HTML");
        setLocalePreference(LOCALE_GERMANY_CODE);
        setChangeNotificationPreference(false);
        setSharingPreference(false);
    }

    private void shouldChangePageSizePreferenceGivenCorrectValue()
    {
        gotoProfilePage(true, "admin");
        assertPageSizePreference(50);
        assertNoUserPreferencesChange();
        goToEditPreferencesDialog();
        assertIsEditPreferencesDialog();
        setPageSizePreference(40);
        submitEditPreferencesDialog();
        assertUserPreferencesChange();
        assertPageSizePreference(40);
    }

    private void shouldChangeEmailTypePreferenceGivenCorrectValue()
    {
        gotoProfilePage(true, "admin");
        assertEmailTypePreference("Text");
        assertNoUserPreferencesChange();
        goToEditPreferencesDialog();
        assertIsEditPreferencesDialog();
        setEmailTypePreference("HTML");
        submitEditPreferencesDialog();
        assertUserPreferencesChange();
        assertEmailTypePreference("HTML");
    }

    private void shouldChangeLocalePreferenceGivenCorrectValue()
    {
        gotoProfilePage(true, "admin");
        assertLocalePreference(LOCALE_DEFAULT_TEXT);
        assertNoUserPreferencesChange();
        goToEditPreferencesDialog();
        assertIsEditPreferencesDialog();
        setLocalePreference(LOCALE_GERMANY_CODE);
        submitEditPreferencesDialogWithReload();
        // TODO We do this because we don't show the notification when the setting change requires the page to be reloaded
        // This is a bug (JRADEV-3326). The assertion will have to be changed to assertUserPreferencesChange(); when we fix it.
        assertNoUserPreferencesChange();
        assertLocalePreference(LOCALE_GERMANY_TEXT);
        resetToDefaultLocale();
    }

    private void resetToDefaultLocale()
    {
        goToEditPreferencesDialog();
        assertIsEditPreferencesDialog();
        setLocalePreference(LOCALE_DEFAULT_CODE);
        submitEditPreferencesDialogWithReload();
        // TODO We do this because we don't show the notification when the setting change requires the page to be reloaded
        // This is a bug (JRADEV-3326). The assertion will have to be changed to assertUserPreferencesChange(); when we fix it.
        assertNoUserPreferencesChange();
        assertLocalePreference(LOCALE_DEFAULT_TEXT);
    }

    private void shouldChangeNotificationPreferenceGivenCorrectValue()
    {
        gotoProfilePage(true, "admin");
        assertChangeNotificationPreference(true);
        assertNoUserPreferencesChange();
        goToEditPreferencesDialog();
        assertIsEditPreferencesDialog();
        setChangeNotificationPreference(false);
        submitEditPreferencesDialog();
        assertUserPreferencesChange();
        assertChangeNotificationPreference(false);
    }

    private void shouldChangeSharingPreferenceGivenCorrectValue()
    {
        gotoProfilePage(true, "admin");
        assertSharingPreference(true);
        assertNoUserPreferencesChange();
        goToEditPreferencesDialog();
        assertIsEditPreferencesDialog();
        setSharingPreference(false);
        submitEditPreferencesDialog();
        assertUserPreferencesChange();
        assertSharingPreference(false);
    }

    private void shouldChangeKeyboardShortcutPreferenceGivenCorrectValue()
    {
        gotoProfilePage(true, "admin");
        assertKeyboardShortcutPreference(true);
        assertNoUserPreferencesChange();
        goToEditPreferencesDialog();
        assertIsEditPreferencesDialog();
        setKeyboardShortcutPreference(false);
        submitEditPreferencesDialogWithReload();
        // TODO We do this because we don't show the notification when the setting change requires the page to be reloaded
        // This is a bug (JRADEV-3326). The assertion will have to be changed to assertUserPreferencesChange(); when we fix it.
        assertNoUserPreferencesChange();
        assertKeyboardShortcutPreference(false);
    }

    private void setKeyboardShortcutPreference(final boolean doEnabled)
    {
        if (doEnabled)
        {
            client.selectOption("keyboardShortcutsEnabled", "Enabled");
        }
        else
        {
            client.selectOption("keyboardShortcutsEnabled", "Disabled");
        }

    }

    private void assertKeyboardShortcutPreference(final boolean enabledExpected)
    {
        if (enabledExpected)
        {
            assertThat.elementVisible("Id=up-p-keyboard-shortcuts-enabled");
            assertThat.elementNotVisible("Id=up-p-keyboard-shortcuts-disabled");
        }
        else
        {
            assertThat.elementNotVisible("Id=up-p-keyboard-shortcuts-enabled");
            assertThat.elementVisible("Id=up-p-keyboard-shortcuts-disabled");
        }
    }

    private void shouldRemoveNotificationWhenEditPreferencesDialogIsOpened()
    {
        gotoProfilePage(true, "admin");
        assertNoUserPreferencesChange();
        goToEditPreferencesDialog();
        assertIsEditPreferencesDialog();
        submitEditPreferencesDialog();
        assertUserPreferencesChange();
        openEditPreferencesDialog();
        assertNoUserPreferencesChange();
    }

    private void shouldNotAllowOtherUserEditPreferences()
    {
        gotoProfilePage(false, "fred");
        assertThat.elementNotPresent("Id=preferences-profile-fragment");
    }

    public void testEditPreferencesNotLoggedIn()
    {
        getNavigator().gotoPage("/secure/UpdateUserPreferences!default.jspa?username=fred", true);
        assertThat.elementContainsText("Id=update-user-preferences", "You can only edit your own preferences");
        assertThat.elementContainsText("Id=update-user-preferences", "Administrator");

        getNavigator().logout(getXsrfToken());
        getNavigator().gotoPage("/secure/UpdateUserPreferences!default.jspa?username=admin", true);
        assertThat.elementContainsText("Id=update-user-preferences", "Your session has timed out");
        
    }

    public void testExpandedEmailAddresses()
    {
        getNavigator().gotoAdmin();
        client.click("Id=general_configuration", true);
        client.clickLinkWithText("Edit Configuration", true);
        client.click("Id=email_mask");
        client.click("Id=edit_property", true);

        goToEditProfileDialog();
        assertIsEditProfileDialog();
        insertUserDetails("Nick", "nick.menere@atlassian.com", "admin");
        submitEditProfileDialog();
        assertUserDetailsChange();
        assertEmailInExpandedMode("nick dot menere at atlassian dot com");
    }

    private void gotoProfilePage(boolean currentUser, String username)
    {
        if (currentUser)
        {
            getNavigator().gotoUserProfile();
        }
        else
        {
            getNavigator().gotoUserProfile(username);
        }
    }

    private void gotoProfileTab(boolean currentUser, String username, String tab)
    {
        if (currentUser)
        {
            getNavigator().gotoUserProfileTab(tab);
        }
        else
        {
            getNavigator().gotoUserProfileTab(tab, username);
        }
    }

    private void goToEditProfileDialog()
    {
        gotoProfilePage(true, "admin");
        assertThat.elementPresent("id=edit_profile_lnk");
        openEditProfileDialog();
    }

    private void openEditProfileDialog()
    {
        client.click("id=edit_profile_lnk");
    }


    private void goToChangePasswordDialog()
    {
        gotoProfilePage(true, "admin");
        assertThat.elementPresent("id=up-d-change-password");
        assertThat.elementPresent("id=view_change_password");
        openChangePasswordDialog();
    }

    private void openChangePasswordDialog()
    {
        client.click("id=view_change_password");
    }

    private void goToEditPreferencesDialog()
    {
        gotoProfilePage(true, "admin");
        assertThat.elementPresent("jquery=#preferences-profile-fragment");
        assertThat.elementPresent("jquery=#edit_prefs_lnk");
        openEditPreferencesDialog();
    }

    private void openEditPreferencesDialog()
    {
        client.click("jquery=#edit_prefs_lnk");
    }

    private void assertIsEditProfileDialog()
    {
        assertDialogIsOpenAndReady();
        assertDialogFormIsUndecorated();
        assertDialogContainsAuiForm(EDIT_PROFILE_FORM_ID);
    }

    private void assertIsChangePasswordDialog()
    {
        assertDialogIsOpenAndReady();
        assertDialogFormIsUndecorated();
        assertDialogContainsAuiForm(CHANGE_PASSWORD_FORM_ID);
    }

    private void assertIsEditPreferencesDialog()
    {
        assertDialogIsOpenAndReady();
        assertDialogFormIsUndecorated();
        assertDialogContainsAuiForm(EDIT_PREFERENCES_FORM_ID);
    }


    private void submitEditProfileDialog() {
        submitDialog("edit-profile-submit");
    }

    private void submitChangePasswordDialog() {
        submitDialog("change-password-submit");
    }

    private void submitEditPreferencesDialog() {
        submitDialog("update-user-preferences-submit");
    }

    private void submitEditPreferencesDialogWithReload() {
        submitDialogAndWaitForReload("update-user-preferences-submit");
    }

    private void insertPasswordChange(final String oldPassword, final String newPassword)
    {
        insertPasswordChange(oldPassword, newPassword, newPassword);
    }

    private void insertUserDetails(String fullName, String email, String password)
    {
        client.type("jquery=input[name='fullName']", fullName);
        client.type("jquery=input[name='email']", email);
        client.type("jquery=input[name='password']", password);
    }

    private void assertUserDetails(String fullName, String email)
    {
        assertThat.elementContainsText("id=up-user-title-name", fullName);
        assertThat.elementContainsText("jquery=#header-details-user .lnk", fullName);
        assertThat.elementContainsText("id=up-d-fullname", fullName);
        assertThat.elementContainsText("jquery=#up-d-email a", email);
    }

    private void assertEmailInExpandedMode(String email)
    {
        assertThat.elementContainsText("jquery=#up-d-email", email);
    }

    private void insertPasswordChange(String oldPassword, String newPassword, String confirmation)
    {
        client.typeWithFullKeyEvents("jquery=input[name='current']", oldPassword);
        client.typeWithFullKeyEvents("jquery=input[name='password']", newPassword);
        client.typeWithFullKeyEvents("jquery=input[name='confirm']", confirmation);
    }

    private void assertPageSizePreference(int expectedPageSize)
    {
        assertThat.elementContainsText("id=up-p-pagesize", Integer.toString(expectedPageSize));
    }
    private void setPageSizePreference(int pageSize)
    {
        client.type("id=update-user-preferences-pagesize", Integer.toString(pageSize));
    }

    private void assertEmailTypePreference(String expectedType)
    {
        assertThat.elementContainsText("id=up-p-mimetype", expectedType);
    }

    private void assertLocalePreference(String expectedLocaleText)
    {
        assertThat.elementContainsText("id=up-p-locale", expectedLocaleText);
    }

    private void setLocalePreference(String localeCode)
    {
        client.select("userLocale", "value=" + localeCode);
    }
    private void setEmailTypePreference(String emailType)
    {
        client.selectOption("userNotificationsMimeType", emailType);
    }

    private void assertChangeNotificationPreference(boolean expectedValue)
    {
        if (expectedValue)
        {
            assertThat.elementVisible("Id=up-p-notifications_on");
            assertThat.elementNotVisible("Id=up-p-notifications_off");
        }
        else
        {
            assertThat.elementNotVisible("Id=up-p-notifications_on");
            assertThat.elementVisible("Id=up-p-notifications_off");
        }
    }
    private void setChangeNotificationPreference(boolean doNotify)
    {
        if (doNotify)
        {
            client.selectOption("notifyOwnChanges", "Notify me");
        }
        else
        {
            client.selectOption("notifyOwnChanges", "Do not notify me");
        }

    }

    private void assertSharingPreference(boolean privateExpected)
    {
        if (privateExpected)
        {
            assertThat.elementVisible("Id=up-p-share-private");
            assertThat.elementNotVisible("Id=up-p-share-public");
        }
        else
        {
            assertThat.elementNotVisible("Id=up-p-share-private");
            assertThat.elementVisible("Id=up-p-share-public");
        }
    }
    private void setSharingPreference(boolean doPrivate)
    {
        if (doPrivate)
        {
            client.selectOption("shareDefault", "Unshared");
        }
        else
        {
            client.selectOption("shareDefault", "Shared");
        }
    }

    private void doNotInsertAnything()
    {
        //as the name says;)
    }

    private void assertUserDetailsChange()
    {
        assertThat.visibleByTimeout("jquery=#userdetails-notify", DEFAULT_TIMEOUT);
    }

    private void assertNoUserDetailsChange()
    {
        assertThat.notVisibleByTimeout("jquery=#userdetails-notify", DEFAULT_TIMEOUT);
    }

    private void assertUserPreferencesChange()
    {
        assertThat.visibleByTimeout("jquery=#userprofile-notify", DEFAULT_TIMEOUT);
    }

    private void assertNoUserPreferencesChange()
    {
        assertThat.notVisibleByTimeout("jquery=#userprofile-notify", DEFAULT_TIMEOUT);
    }

    private void assertUserProfilePage()
    {
        assertThat.elementPresentByTimeout("jquery=span.up-user-title-name", DEFAULT_TIMEOUT);
    }

    private void assertDialogFormIsUndecorated()
    {
        // TODO ask Scott
    }
}
