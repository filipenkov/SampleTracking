package com.atlassian.jira.webtest.framework.impl.selenium.locator;

import junit.framework.TestCase;

/**
 * Test case for {@link com.atlassian.jira.webtest.selenium.framework.model.Locators} enumeration.
 *
 * @since v4.2
 */
public class TestStringLocators extends TestCase
{
    public void testSimpleRemovePrefix()
    {
        final String locatorWithPrefix = "css=div#nice-id a.nice-class";
        assertEquals("div#nice-id a.nice-class", StringLocators.removeLocatorPrefix(locatorWithPrefix));
        final String locatorWithoutPrefix = "great_id";
        assertEquals("great_id", StringLocators.removeLocatorPrefix(locatorWithoutPrefix));
        final String locatorWithInvalidPrefix = "invalid=some_other_id";
        assertEquals("invalid=some_other_id", StringLocators.removeLocatorPrefix(locatorWithInvalidPrefix));
    }

    public void testRemovePrefixWithMultipleEqualsSigns()
    {
        final String locatorWithPrefix = "css=div#nice-id a.nice-class[rel=14]";
        assertEquals("div#nice-id a.nice-class[rel=14]", StringLocators.removeLocatorPrefix(locatorWithPrefix));
        final String locatorWithInvalidPrefix = "invalid=a.nice-class[rel=14]";
        assertEquals("invalid=a.nice-class[rel=14]", StringLocators.removeLocatorPrefix(locatorWithInvalidPrefix));
    }

}
