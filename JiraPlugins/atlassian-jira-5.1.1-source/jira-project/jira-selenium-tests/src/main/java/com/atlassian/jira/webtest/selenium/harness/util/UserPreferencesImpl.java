package com.atlassian.jira.webtest.selenium.harness.util;

import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.atlassian.selenium.SeleniumAssertions;
import com.atlassian.selenium.SeleniumClient;

/**
 * TODO: Document this class / interface here
 *
 * @since v4.2
 */
public class UserPreferencesImpl extends AbstractSeleniumUtil implements UserPreferences
{
    final Navigator navigator;
    final SeleniumAssertions assertThat;

    public UserPreferencesImpl(SeleniumClient selenium, JIRAEnvironmentData environmentData, final Navigator navigator, final SeleniumAssertions assertThat)
    {
        super(selenium, environmentData);
        this.navigator = navigator;
        this.assertThat = assertThat;
    }

    public void setKeyboardShortcutsEnabled(final boolean enabled)
    {
        gotoProfilePage();
        goToEditPreferencesDialog();
        if (enabled)
        {
            selenium.selectOption("keyboardShortcutsEnabled", "Enabled");
        }
        else
        {
            selenium.selectOption("keyboardShortcutsEnabled", "Disabled");
        }
        submitEditPreferencesDialog();

    }

    public void setLanguage(final String locale)
    {
        gotoProfilePage();
        goToEditPreferencesDialog();
        selenium.select("userLocale", "value=" + locale);
        submitEditPreferencesDialog();
    }

    private void gotoProfilePage()
    {
        navigator.gotoUserProfile();
    }

    private void goToEditPreferencesDialog()
    {
        gotoProfilePage();
        assertThat.elementPresent("jquery=#preferences-profile-fragment");
        assertThat.elementPresent("jquery=#edit_prefs_lnk");
        openEditPreferencesDialog();
    }

    private void openEditPreferencesDialog()
    {
        selenium.click("jquery=#edit_prefs_lnk");
        selenium.waitForAjaxWithJquery(10000);
    }

    private void submitEditPreferencesDialog() {
        selenium.click("jquery=#update-user-preferences-submit");
        selenium.waitForPageToLoad();
    }

}
