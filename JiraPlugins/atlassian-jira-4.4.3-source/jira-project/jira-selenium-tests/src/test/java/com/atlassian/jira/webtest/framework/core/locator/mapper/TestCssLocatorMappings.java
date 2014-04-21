package com.atlassian.jira.webtest.framework.core.locator.mapper;

import com.atlassian.jira.webtest.framework.core.locator.LocatorData;
import junit.framework.TestCase;

import static com.atlassian.jira.webtest.framework.core.locator.Locators.*;

/**
 * Test for CSS locator mappings.
 *
 * @since v4.3
 */
public class TestCssLocatorMappings extends TestCase
{
    private DefaultLocatorMapper tested = new DefaultLocatorMapper();


    public void testCssToCss()
    {
        LocatorData result = tested.combine(CSS.create(".someclass #andsomeid"), CSS.create("#anotherid.withclass"));
        assertEquals(CSS, result.type());
        assertEquals(".someclass #andsomeid #anotherid.withclass", result.value());
    }

    public void testCssToId()
    {
        LocatorData result = tested.combine(CSS.create("#one.withclass"), ID.create("two"));
        assertEquals(CSS, result.type());
        assertEquals("#one.withclass #two", result.value());
    }

    public void testCssToClass()
    {
        LocatorData result = tested.combine(CSS.create("#one.withclass"), CLASS.create("two"));
        assertEquals(CSS, result.type());
        assertEquals("#one.withclass .two", result.value());
    }

    public void testCssToJQuery()
    {
        LocatorData result = tested.combine(CSS.create("#one.withclass #andtwo"), JQUERY.create("#two:hidden"));
        assertEquals(JQUERY, result.type());
        assertEquals("#one.withclass #andtwo #two:hidden", result.value());
    }

    public void testCssToXPath()
    {
        assertFalse(tested.supports(CSS.create("#one .classtwo"), XPATH.create("//two//three[@id='four']")));
    }
}
