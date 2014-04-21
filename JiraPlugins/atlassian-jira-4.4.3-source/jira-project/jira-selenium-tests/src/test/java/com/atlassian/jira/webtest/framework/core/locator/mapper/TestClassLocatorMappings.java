package com.atlassian.jira.webtest.framework.core.locator.mapper;

import com.atlassian.jira.webtest.framework.core.locator.LocatorData;
import junit.framework.TestCase;

import static com.atlassian.jira.webtest.framework.core.locator.Locators.*;

/**
 * Test for class locator mappings.
 *
 * @since v4.3
 */
public class TestClassLocatorMappings extends TestCase
{
    private DefaultLocatorMapper tested = new DefaultLocatorMapper();


    public void testClassToClass()
    {
        LocatorData result = tested.combine(CLASS.create("one"), CLASS.create("two"));
        assertEquals(CSS, result.type());
        assertEquals(".one .two", result.value());
    }

    public void testClassToId()
    {
        LocatorData result = tested.combine(CLASS.create("one"), ID.create("two"));
        assertEquals(CSS, result.type());
        assertEquals(".one #two", result.value());
    }

    public void testClassToJQuery()
    {
        LocatorData result = tested.combine(CLASS.create("one"), JQUERY.create("#two:hidden"));
        assertEquals(JQUERY, result.type());
        assertEquals(".one #two:hidden", result.value());
    }

    public void testClassToCss()
    {
        LocatorData result = tested.combine(CLASS.create("one"), CSS.create("#two.three"));
        assertEquals(CSS, result.type());
        assertEquals(".one #two.three", result.value());
    }

    public void testClassToXPath()
    {
        LocatorData result = tested.combine(CLASS.create("one"), XPATH.create("//two//three[@id='four']"));
        assertEquals(XPATH, result.type());
        assertEquals("//*[@class='one']//two//three[@id='four']", result.value());
    }
}
