package com.atlassian.jira.webtest.selenium.issue;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;

/**
 * @since v4.2
 */
@WebTest({Category.SELENIUM_TEST })
public class TestKeyboardShortcutHintsInIssueNavigator extends JiraSeleniumTest
{

    public void onSetUp()
    {
        super.onSetUp();
        restoreData("TestActionsAndOperations.xml");
    }

    public void testTooltipPopup() throws Exception
    {
        getNavigator().findAllIssues();
        assertThat.elementPresentByTimeout("jquery=.action-dropdown:first a[title='Actions (Type . to access issue actions)']",800);
        disableKeyboardShortcuts();
        getNavigator().findAllIssues();
        assertThat.elementPresentByTimeout("jquery=.action-dropdown:first a[title='Actions']",800);
    }

    private void disableKeyboardShortcuts()
    {
        getNavigator().gotoPage("secure/admin/UpdateUserPreferences!default.jspa?username=admin",true);
        client.selectOption("jquery=#update-user-preferences-keyboard-shortcuts","Disabled");
        client.click("jquery=#update-user-preferences-submit");
        getNavigator().gotoHome();
    }

}