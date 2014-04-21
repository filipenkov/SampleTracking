package com.atlassian.jira.webtest.framework.core.locator.mapper;

import com.atlassian.jira.webtest.framework.core.locator.LocatorData;
import junit.framework.TestCase;

import static com.atlassian.jira.webtest.framework.core.locator.Locators.*;

/**
 * Test for ID locator mappings.
 *
 * @since v4.3
 */
public class TestIdLocatorMappings extends TestCase
{
    private DefaultLocatorMapper tested = new DefaultLocatorMapper();


    public void testIdToId()
    {
        LocatorData result = tested.combine(ID.create("one"), ID.create("two"));
        assertEquals(CSS, result.type());
        assertEquals("#one #two", result.value());
    }

    public void testIdToClass()
    {
        LocatorData result = tested.combine(ID.create("one"), CLASS.create("two"));
        assertEquals(CSS, result.type());
        assertEquals("#one .two", result.value());
    }

    public void testIdToJQuery()
    {
        LocatorData result = tested.combine(ID.create("one"), JQUERY.create("#two:hidden"));
        assertEquals(JQUERY, result.type());
        assertEquals("#one #two:hidden", result.value());
    }

    public void testIdToCss()
    {
        LocatorData result = tested.combine(ID.create("one"), CSS.create("#two.three"));
        assertEquals(CSS, result.type());
        assertEquals("#one #two.three", result.value());
    }

    public void testIdToXPath()
    {
        LocatorData result = tested.combine(ID.create("one"), XPATH.create("//two//three[@id='four']"));
        assertEquals(XPATH, result.type());
        assertEquals("//*[@id='one']//two//three[@id='four']", result.value());
    }
}
