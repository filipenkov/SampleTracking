package com.atlassian.jira.webtest.selenium.jquery;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import com.atlassian.jira.webtest.selenium.harness.util.Navigator;

import java.util.List;

import static java.util.Arrays.asList;
import static org.apache.commons.lang.StringUtils.split;

/**
 * Test cases around the jQuery locator strategy in Atlassian Selenium.
 *
 * @since v4.3
 */
@WebTest({Category.SELENIUM_TEST })
public class TestJQueryLocatorStrategyInSelenium extends JiraSeleniumTest
{
    private Navigator navigation;

    @Override
    public void onSetUp()
    {
        super.onSetUp();
        restoreBlankInstance();
        navigation = getNavigator();
    }

    public void testJQueryLocatorShouldReturnElementWhenASingleElementMatches() throws Exception
    {
        navigation.gotoUserProfile();

        client.click("jquery=#admin_user"); // should click the "Administer User" link
    }

    public void testJQueryLocatorShouldReturnFirstElementWhenMultipleElementsMatch() throws Exception
    {
        navigation.gotoUserProfile();

        // sanity check
        assertTrue(Integer.valueOf(client.getEval("this.browserbot.getCurrentWindow().jQuery('a').length")) > 1);

        // should click the first link on the page
        client.click("jquery=a");
    }

    public void testJQueryLocatorShouldReturnAttributeValueWhenASingleElementMatches() throws Exception
    {
        navigation.gotoUserProfile();

        // sanity check
        assertEquals(1, (int) Integer.valueOf(client.getEval("this.browserbot.getCurrentWindow().jQuery('#up-user-title').length")));

        // returns the class attribute of the item
        List<String> classes = asList(split(client.getAttribute("jquery=#up-user-title@class")));
        assertTrue(classes.contains("item-summary"));
    }

    public void testJQueryLocatorShouldReturnAttributeValueOfFirstElementWhenMultipleElementsMatch() throws Exception
    {
        navigation.gotoUserProfile();

        // sanity check
        assertTrue(Integer.valueOf(client.getEval("this.browserbot.getCurrentWindow().jQuery('ul.ops').length")) > 1);

        // returns the class attribute of the first selected item
        List<String> classes = asList(split(client.getAttribute("jquery=ul.ops@class")));
        assertTrue(classes.contains("ops"));
    }
}
