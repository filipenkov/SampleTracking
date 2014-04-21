package com.atlassian.jira.webtest.selenium.unittestrunner;

import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;

/**
 * TODO: Document this class / interface here
 *
 * @since v4.0
 */
public class UnitTestRunner extends JiraSeleniumTest
{
    String URL_PREFIX = "includes/js/test";

    public void onSetUp()
    {
        super.onSetUp();
    }

//    public void testIssuePicker () {
//        runTests("/ajs/multiselect/issuepicker/issuepicker.jsp");
//    }

    public void testKeyboardShortcuts () {
        runTests("/keyboardshortcuts/keyboardshortcuts.jsp");
    }

    private void runTests (String testLocation) {
        getNavigator().gotoPage(URL_PREFIX + testLocation, true);
        assertThat.elementPresentByTimeout("jquery=.failed", 50000);
        String failed = client.getText("jquery=.failed");
        assertTrue(failed.equals("0"));
    }
}
