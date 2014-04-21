package com.atlassian.jira.webtest.selenium.admin;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import junit.framework.Test;

@WebTest({Category.SELENIUM_TEST })
public class TestGadgetWhitelistUpgrade extends JiraSeleniumTest
{
    private static final String WARNING_TEXT = "Your JIRA instance has external gadgets configured. Due to a security related";

    public static Test suite()
    {
        return suiteFor(TestGadgetWhitelistUpgrade.class);
    }

    public void testOnlyAdminsGetWarning()
    {
        restoreData("TestGadgetApplinksUpgrade.xml");
        getNavigator().gotoHome();
        assertThat.textPresentByTimeout(WARNING_TEXT);

        getNavigator().logout(getXsrfToken());
        getNavigator().login("fred");
        assertThat.textNotPresentByTimeout(WARNING_TEXT);
    }

    public void testWarningDismissal()
    {
        restoreData("TestGadgetApplinksUpgrade.xml");
        getNavigator().gotoHome();
        assertThat.textPresentByTimeout(WARNING_TEXT);

        client.clickLinkWithText("whitelisted", true);
        assertThat.textPresent("Thank you for upgrading to JIRA 4.3.");


        //now if we go back to the dashboard the notice should be gone!
        getNavigator().gotoHome();
        assertThat.textPresent("his dashboard does not contain any gadgets or you do not have permission to view them");
        assertThat.textNotPresentByTimeout(WARNING_TEXT);
    }

    public void testDisableWhitelist()
    {
        restoreData("TestGadgetApplinksUpgrade.xml");
        getNavigator().gotoHome();
        client.clickLinkWithText("whitelisted", true);

        assertThat.elementContainsText("configure-whitelist-rules", "http://www.atlassian.com/*");
        assertThat.elementContainsText("configure-whitelist-rules", "http://extranet.atlassian.com/*");
        assertThat.elementContainsText("configure-whitelist-rules", "http://www.rememberthemilk.com/*");
        assertThat.elementContainsText("configure-whitelist-rules", "http://confluence.atlassian.com/*");
        client.click("configure-whitelist-allow");
        assertThat.notVisibleByTimeout("configure-whitelist-rules", DROP_DOWN_WAIT);
        client.click("configure-whitelist-submit", true);
        assertThat.notVisibleByTimeout("configure-whitelist-rules", DROP_DOWN_WAIT);
        client.click("configure-whitelist-restrict");
        assertThat.visibleByTimeout("configure-whitelist-rules", DROP_DOWN_WAIT);
        client.click("configure-whitelist-submit", true);
        assertThat.visibleByTimeout("configure-whitelist-rules", DROP_DOWN_WAIT);
        assertThat.elementDoesNotContainText("configure-whitelist-rules", "http://www.atlassian.com/*");
        assertThat.elementDoesNotContainText("configure-whitelist-rules", "http://extranet.atlassian.com/*");
        assertThat.elementDoesNotContainText("configure-whitelist-rules", "http://www.rememberthemilk.com/*");
        assertThat.elementDoesNotContainText("configure-whitelist-rules", "http://confluence.atlassian.com/*");
        assertThat.textPresent("Whitelist saved successfully!");
    }

    public void testNoExternalGadgetsNoWarning()
    {
        restoreData("blankprojects.xml");
        getNavigator().gotoHome();
        assertThat.textNotPresentByTimeout(WARNING_TEXT);
    }

}
