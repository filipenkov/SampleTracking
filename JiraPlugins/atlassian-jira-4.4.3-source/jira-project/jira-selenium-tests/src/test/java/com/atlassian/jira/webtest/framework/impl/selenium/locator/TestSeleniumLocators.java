package com.atlassian.jira.webtest.framework.impl.selenium.locator;

import com.atlassian.jira.webtest.framework.core.locator.Locators;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.selenium.mock.MockSeleniumClient;
import com.atlassian.selenium.mock.MockSeleniumConfiguration;
import junit.framework.TestCase;

/**
 * Tests for basic functionality of the Selenium locators, including full locators, support and nesting.
 *
 * @since v4.3
 */
public class TestSeleniumLocators extends TestCase
{
    
    private SeleniumContext mockContext = new SeleniumContext(new MockSeleniumClient(), new MockSeleniumConfiguration());


    public void testIdFullLocator()
    {
        SeleniumLocator id = SeleniumLocators.id("someid", mockContext);
        assertEquals(Locators.ID, id.type());
        assertEquals("someid", id.value());
        assertEquals("id=someid", id.fullLocator());
    }

    public void testClassLocator()
    {
        SeleniumLocator classLoc = SeleniumLocators.forClass("someclass", mockContext);
        assertEquals(Locators.CLASS, classLoc.type());
        assertEquals("someclass", classLoc.value());
        assertEquals("css=.someclass", classLoc.fullLocator());
    }

    public void testCssWithinId()
    {
        SeleniumLocator idParent = SeleniumLocators.id("someid", mockContext);
        SeleniumLocator cssClass = SeleniumLocators.css("#otherid.andclass", mockContext);
        assertTrue(idParent.supports(cssClass));
        SeleniumLocator result = idParent.combine(cssClass);
        assertEquals(Locators.CSS, result.type());
        assertEquals("#someid #otherid.andclass", result.value());
        assertEquals("css=#someid #otherid.andclass", result.fullLocator());
    }

    // TODO more
}
