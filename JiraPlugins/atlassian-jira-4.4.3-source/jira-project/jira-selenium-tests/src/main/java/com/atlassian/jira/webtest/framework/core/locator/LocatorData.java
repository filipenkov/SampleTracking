package com.atlassian.jira.webtest.framework.core.locator;

/**
 * Holds core data describing a single locator instance.
 *
 * @since v4.3
 *
 * @see com.atlassian.jira.webtest.framework.core.locator.Locator
 */
public interface LocatorData
{
    /**
     * Type of the locator.
     *
     * @return type of the locator 
     * @see com.atlassian.jira.webtest.framework.core.locator.LocatorType
     */
    LocatorType type();

    /**
     * Value of the locator, used to query and manipulate it in the current test context.
     *
     * @return value of the locator instance represented by this data
     */
    String value();
}
