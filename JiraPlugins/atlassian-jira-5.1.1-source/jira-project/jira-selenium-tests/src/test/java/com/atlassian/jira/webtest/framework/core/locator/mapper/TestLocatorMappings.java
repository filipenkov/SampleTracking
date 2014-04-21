package com.atlassian.jira.webtest.framework.core.locator.mapper;

import com.atlassian.jira.webtest.framework.core.locator.Locator;
import junit.framework.TestCase;

import static com.atlassian.jira.webtest.framework.core.locator.Locators.JQUERY;
import static com.atlassian.jira.webtest.framework.core.locator.Locators.XPATH;

/**
 * Tests basic functionality of {@link DefaultLocatorMapper}.
 *
 * @since v4.3
 */
public class TestLocatorMappings extends TestCase
{
    private DefaultLocatorMapper tested = new DefaultLocatorMapper();

    public void testExceptionOnNotExistingMappings()
    {
        Locator xpath = XPATH.create("//test");
        Locator jQuery = JQUERY.create("#test");
        assertFalse(tested.supports(xpath, jQuery));
        try
        {
            tested.combine(xpath, jQuery);
            fail("Expected IllegalArgumentException for incompatible locators");
        }
        catch (IllegalArgumentException success)
        {
        }
    }

    public void testExceptionOnIncompatibleMappingArguments()
    {
        Locator xpath = XPATH.create("//test");
        Locator jQuery = JQUERY.create("#test");
        LocatorMapping mapping = XPathMappings.xpathToId();
        assertTrue(mapping.supportsParent(xpath));
        assertFalse(mapping.supportsChild(jQuery));
        try
        {
            mapping.combine(xpath, jQuery);
            fail("Expected IllegalArgumentException for incompatible locators");
        }
        catch (IllegalArgumentException success)
        {
        }
    }

    // TODO test adding/replacing mappings

}


