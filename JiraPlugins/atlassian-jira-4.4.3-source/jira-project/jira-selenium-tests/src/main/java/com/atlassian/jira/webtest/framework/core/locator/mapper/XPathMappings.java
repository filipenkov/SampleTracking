package com.atlassian.jira.webtest.framework.core.locator.mapper;

import com.atlassian.jira.webtest.framework.core.locator.LocatorData;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.atlassian.jira.webtest.framework.core.locator.Locators.*;

/**
 * Mappings with XPath locator type as a parent.
 *
 * @since v4.2
 */
final class XPathMappings
{
    static final List<LocatorMapping> ALL = createAll();

    private static List<LocatorMapping> createAll()
    {
        return Collections.unmodifiableList(Arrays.asList(
                xpath(), xpathToId(), xpathToClass()
        ));
    }

    private XPathMappings()
    {
        throw new AssertionError("Don't instantiate me");
    }

    public static LocatorMapping xpath()
    {
        return Xpath.INSTANCE;
    }

    public static LocatorMapping xpathToId()
    {
        return XPathToId.INSTANCE;
    }

    public static LocatorMapping xpathToClass()
    {
        return XPathToClass.INSTANCE;
    }

    private static class Xpath extends AbstractMapping
    {
        static LocatorMapping INSTANCE = new Xpath();

        private Xpath()
        {
            super(XPATH, XPATH);
        }

        @Override
        protected LocatorData doCombine(final String parentValue, final String childValue)
        {
            // TODO this is probably too simplistic
            return new LocatorDataBean(XPATH, parentValue + "//" + removeLeadingPathChars(childValue));
        }

    }

    private static class XPathToId extends AbstractMapping
    {
        static LocatorMapping INSTANCE = new XPathToId();

        private XPathToId()
        {
            super(XPATH, ID);
        }

        @Override
        protected LocatorData doCombine(String parentValue, String childValue)
        {
            return new LocatorDataBean(XPATH, parentValue + "//*[@id='" + childValue + "']");
        }
    }

    private static class XPathToClass extends AbstractMapping
    {
        static LocatorMapping INSTANCE = new XPathToClass();

        private XPathToClass()
        {
            super(XPATH, CLASS);
        }

        @Override
        protected LocatorData doCombine(String parentValue, String childValue)
        {
            return new LocatorDataBean(XPATH, parentValue + "//*[contains(@class,'" + childValue + "')]");
        }
    }

    static String removeLeadingPathChars(String xpath)
    {
        String result = xpath;
        while (result.startsWith("/"))
        {
            result = result.substring(1);
        }
        return result;
    }

    static String combine(String parent, String child)
    {
        return parent + "//" + removeLeadingPathChars(child);
    }
}
