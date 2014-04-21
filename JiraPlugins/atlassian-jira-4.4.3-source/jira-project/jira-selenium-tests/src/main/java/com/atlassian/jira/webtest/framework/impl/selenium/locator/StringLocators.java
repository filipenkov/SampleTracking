package com.atlassian.jira.webtest.framework.impl.selenium.locator;

import com.atlassian.jira.webtest.framework.core.locator.LocatorData;
import com.atlassian.jira.webtest.framework.core.locator.LocatorType;
import com.atlassian.jira.webtest.framework.core.locator.Locators;

import static com.atlassian.jira.util.dbc.Assertions.notBlank;
import static com.atlassian.jira.webtest.framework.core.locator.Locators.values;
import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * <p>
 * String locator support for Selenium.
 *
 * <p>
 * Locators in the Selenium World&#8482; come with a simple syntax: type=value.
 * This class is a suite of static utilities to help with various conversions and manipulations of such locators.
 *
 * @since v4.3
 */
public final class StringLocators
{
    private static final String LOCATOR_SYNTAX = "%s=%s";

    private StringLocators(final String prefix)
    {
        throw new AssertionError("No way");
    }

    /**
     * Create Selenium string locator from given locator <tt>type</tt> and <tt>value</tt>.
     *
     * @param type type of locator
     * @param value value of locator
     * @return Selenium-compatible locator string
     */
    public static String create(LocatorType type, String value)
    {
        return String.format(LOCATOR_SYNTAX, type.id(), value);
    }

    /**
     * Create Selenium string locator from an arbitrary <tt>locatorData</tt>.
     *
     * @param locatorData locator data representing a locator to create from
     * @return Selenium-compatible locator string
     *
     * @see #create(com.atlassian.jira.webtest.framework.core.locator.LocatorType, String)
     */
    public static String create(LocatorData locatorData)
    {
        return create(locatorData.type(), locatorData.value());
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
            return matchesAnyType(retrievePrefix(locator));
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
     * @return corresponding {@link com.atlassian.jira.webtest.framework.core.locator.Locators} instance
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
            if (matches(sl, locator))
            {
                return sl;
            }
        }
        throw new IllegalArgumentException("No matching default locator for <" + locator + "> found");
    }


    /**
     * Check if <tt>locator</tt> matches given <tt>locatorType</tt>.
     *
     * @param locatorType locator type
     * @param locator locator to check
     * @return <code>true</code>, if <tt>locator</tt> matches <tt>locatorType</tt>
     */
    public static boolean matches(Locators locatorType, String locator)
    {
        return hasLocatorPrefix(locator) && retrievePrefix(locator).equalsIgnoreCase(locatorType.id());
    }


    public static boolean matchesAnyType(String locatorPrefix)
    {
        for (Locators sl : values())
        {
            if (sl.id().equalsIgnoreCase(locatorPrefix))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Add this locator's prefix to locator string if necessary (i.e. the <tt>locator</tt> is not prefixed yet).
     *
     * @param type locator type
     * @param locator locator to transform
     * @return prefixed locator (if not yet prefixed)
     */
    public final String addPrefixIfNecessary(LocatorType type, String locator)
    {
        if (!hasLocatorPrefix(locator))
        {
            return create(type, locator);
        }
        else
        {
            return locator;
        }
    }
    
}
