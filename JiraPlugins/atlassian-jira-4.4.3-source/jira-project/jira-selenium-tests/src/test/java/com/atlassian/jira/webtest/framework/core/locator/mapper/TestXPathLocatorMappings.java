package com.atlassian.jira.webtest.framework.core.locator.mapper;

import com.atlassian.jira.webtest.framework.core.locator.LocatorData;
import junit.framework.TestCase;

import static com.atlassian.jira.webtest.framework.core.locator.Locators.*;

/**
 * Test for XPath locator mappings.
 *
 * @since v4.3
 */
public class TestXPathLocatorMappings extends TestCase
{
    private DefaultLocatorMapper tested = new DefaultLocatorMapper();


    public void testXPathToXPath()
    {
        LocatorData result = tested.combine(XPATH.create("//div/input[@id='foo']"), XPATH.create("/span[@class='bar']"));
        assertEquals(XPATH, result.type());
        assertEquals("//div/input[@id='foo']//span[@class='bar']", result.value());
    }

    public void testXPathToId()
    {
        LocatorData result = tested.combine(XPATH.create("//div/input[@id='foo']"), ID.create("bar"));
        assertEquals(XPATH, result.type());
        assertEquals("//div/input[@id='foo']//*[@id='bar']", result.value());
    }

    public void testXPathToClass()
    {
        LocatorData result = tested.combine(XPATH.create("//div/input[@id='foo']"), CLASS.create("bar"));
        assertEquals(XPATH, result.type());
        assertEquals("//div/input[@id='foo']//*[contains(@class,'bar')]", result.value());
    }

    public void testXPathToCss()
    {
        assertFalse(tested.supports(XPATH.create("//two//three[@id='four']"), CSS.create("#one .classtwo")));
    }

    public void testXPathToJQuery()
    {
        assertFalse(tested.supports(XPATH.create("//two//three[@id='four']"), JQUERY.create("#one .classtwo")));
    }
}
