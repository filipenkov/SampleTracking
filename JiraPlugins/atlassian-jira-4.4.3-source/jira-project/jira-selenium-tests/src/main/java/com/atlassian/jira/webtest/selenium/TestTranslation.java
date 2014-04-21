package com.atlassian.jira.webtest.selenium;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * Test AJS.I18n.getText() transform works.
 * @since v4.4
 */
@WebTest({Category.SELENIUM_TEST })
public class TestTranslation extends JiraSeleniumTest
{

    public void onSetUp()
    {
        super.onSetUp();

        restoreData("keyboardcommands.xml");
    }
    public void testKeyboardTitleShortcuts()
    {
        _testEnglish();
        getUserPreferences().setLanguage("de_DE");
        _testGerman();
        getUserPreferences().setLanguage("-1");
        _testEnglish();
    }

    private void _testEnglish()
    {
        getNavigator().gotoIssue("HSP-4");
        assertThat.attributeContainsValue("home_link", "title", "( Type 'g' then 'd' )");
    }
    private void _testGerman()
    {
        getNavigator().gotoIssue("HSP-4");
        assertThat.attributeContainsValue("home_link", "title", "( Typ 'g' dann 'd' )");
    }

}
