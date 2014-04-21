package com.atlassian.jira.webtest.framework.core.locator.mapper;

import com.atlassian.jira.webtest.framework.core.locator.LocatorData;
import com.atlassian.jira.webtest.framework.core.locator.LocatorType;

/**
 * Represents single type-based mapping between two locators. It defines parent and child
 * locator types, and a method to combine them into a new locator.
 *
 * @since v4.3
 */
public interface LocatorMapping
{
    /**
     * Parent locator type of this mapping
     *
     * @return parent locator type
     */
    LocatorType parentType();

    /**
     * Child locator type of this mapping.
     *
     * @return child locator type
     */
    LocatorType childType();

    /**
     * Checks if this mapping supports locator represented <tt>parent</tt>.
     *
     * @param parent parent data to check
     * @return <code>true</code>, if parent is supported, <code>false<code> otherwise
     */
    boolean supportsParent(LocatorData parent);

    /**
     * Checks if this mapping supports locator represented <tt>child</tt>.
     *
     * @param child child data to check
     * @return <code>true</code>, if <tt>child</tt> is supported, <code>false<code> otherwise
     */
    boolean supportsChild(LocatorData child);

    /**
     * Apply this mapping to a given locator data pair.
     *
     * @param parent parent locator data
     * @param child child locator data
     * @return resulting locator data
     * @throws IllegalArgumentException if parent, or child is not supported by this mapping
     *
     * @see #supportsParent(com.atlassian.jira.webtest.framework.core.locator.LocatorData)
     * @see #supportsChild(com.atlassian.jira.webtest.framework.core.locator.LocatorData)
     */
    LocatorData combine(LocatorData parent, LocatorData child);
}
