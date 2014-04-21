package com.atlassian.jira.webtest.selenium.gadgets;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * Selenium Test for the Introduction Gadget.
 *
 * @since v4.0
 */
@WebTest({Category.SELENIUM_TEST })
public class TestIntroGadget extends GadgetTest
{
    public void onSetUp()
    {
        super.onSetUp();
        addGadget("Introduction");
    }

    public void testView()
    {
        assertGadgetTitle("Introduction");
        client.selectWindow(null);
        waitFor(3000);
        assertThat.elementContainsText("//div[@class='gadget']/div", "Welcome to JIRA");
    }
}
