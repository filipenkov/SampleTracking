package com.atlassian.jira.webtest.framework.core.locator.mapper;

import com.atlassian.jira.webtest.framework.core.locator.LocatorData;
import junit.framework.TestCase;

import static com.atlassian.jira.webtest.framework.core.locator.Locators.*;

/**
 * Test for jQuery locator mappings.
 *
 * @since v4.3
 */
public class TestJQueryLocatorMappings extends TestCase
{
    private DefaultLocatorMapper tested = new DefaultLocatorMapper();


    public void testJQueryToJQuery()
    {
        LocatorData result = tested.combine(JQUERY.create("div.someclass"), JQUERY.create(".anotherclass:text"));
        assertEquals(JQUERY, result.type());
        assertEquals("div.someclass .anotherclass:text", result.value());
    }

    public void testJQueryToId()
    {
        LocatorData result = tested.combine(JQUERY.create("#one.withclass"), ID.create("two"));
        assertEquals(JQUERY, result.type());
        assertEquals("#one.withclass #two", result.value());
    }

    public void testJQueryToClass()
    {
        LocatorData result = tested.combine(JQUERY.create("#one.withclass"), CLASS.create("two"));
        assertEquals(JQUERY, result.type());
        assertEquals("#one.withclass .two", result.value());
    }

    public void testJQueryToCss()
    {
        LocatorData result = tested.combine(JQUERY.create("#one.withclass #andtwo"), CSS.create("#two.hidden"));
        assertEquals(JQUERY, result.type());
        assertEquals("#one.withclass #andtwo #two.hidden", result.value());
    }

    public void testJQueryToXPath()
    {
        assertFalse(tested.supports(JQUERY.create("#one .classtwo"), XPATH.create("//two//three[@id='four']")));
    }
}
