package com.atlassian.jira.webtest.selenium.framework.model;

import static com.atlassian.jira.util.dbc.Assertions.notBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * Enumeration of Selenium locator strategies.
 *
 * @since v4.2
 */
public enum Locators
{
    ID("id"),
    CSS("css"),
    XPATH("xpath"),
    JQUERY("jquery");

    private static final String LOCATOR_SYNTAX = "%s=%s"; 

    public static final class Common
    {
        private Common()
        {
            throw new AssertionError("Don't hack with me");
        }

        public static final String BODY = CSS.addPrefix("body");

    }

    private final String prefix;

    private Locators(final String prefix)
    {
        this.prefix = prefix;
    }

    public static String removeLocatorPrefix(String locator)
    {
        if (hasLocatorPrefix(notBlank("locator", locator)))
        {
            return locator.substring(locator.indexOf("=") + 1);
        }
        else
        {
            return locator;
        }
    }


    private static String retrievePrefix(String locator)
    {
        if (hasAnyPrefix(notBlank("locator", locator)))
        {
            return locator.substring(0, locator.indexOf("="));
        }
        else
        {
            throw new IllegalArgumentException("<" + locator + "> has no prefix");
        }
    }

    public static boolean hasLocatorPrefix(String locator)
    {
        if (hasAnyPrefix(locator))
        {
            return matchesAnyPrefix(retrievePrefix(locator));
        }
        else
        {
            return false;
        }
    }

    private static boolean hasAnyPrefix(final String locator)
    {
        return isNotBlank(locator) && locator.contains("=");
    }

    /**
     * Resolve locator instance from locator string.
     *
     * @param locator string containing any locator
     * @return corresponding SeleniumLocator instance
     * @throws IllegalArgumentException if <tt>locator</tt> has no prefix, or could no corresponding SeleniumLocator
     * instance exists.
     */
    public static Locators fromLocator(String locator)
    {
        if (!hasAnyPrefix(locator))
        {
            throw new IllegalArgumentException("<" + locator + "> has no prefix");
        }
        for (Locators sl : values())
        {
            if (sl.matches(locator))
            {
                return sl;
            }
        }
        throw new IllegalArgumentException("No matching SeleniumLocator for <" + locator + "> found");
    }

    public static boolean matchesAnyPrefix(String locatorPrefix)
    {
        for (Locators sl : values())
        {
            if (sl.prefix.equalsIgnoreCase(locatorPrefix))
            {
                return true;
            }
        }
        return false;
    }

    public final String prefix()
    {
        return prefix;
    }

    public final String prefixWithEquals()
    {
        return prefix + "=";
    }

    public final String create(String locator)
    {
        return String.format(LOCATOR_SYNTAX, prefix, removeLocatorPrefix(locator));
    }


    public final boolean matches(String locator)
    {
        return hasLocatorPrefix(locator) && retrievePrefix(locator).equalsIgnoreCase(prefix);
    }

    public final String addPrefix(String locator)
    {
        return prefixWithEquals() + locator;
    }

    /**
     * Add this locator's prefix to locator string if necessary (i.e. the <tt>locator</tt> is not prefixed yet).
     *
     * @param locator locator to transform
     * @return prefixed locator (if not yet prefixed)
     */
    public final String addPrefixIfNecessary(String locator)
    {
        if (!hasLocatorPrefix(locator))
        {
            return addPrefix(locator);
        }
        else
        {
            return locator;
        }
    }
    
}
