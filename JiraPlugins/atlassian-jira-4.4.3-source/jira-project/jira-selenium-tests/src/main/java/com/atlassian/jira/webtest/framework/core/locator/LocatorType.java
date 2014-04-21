package com.atlassian.jira.webtest.framework.core.locator;

/**
 * Represents a particular type of locator. Locators of different types may, or may not be compatible among each other,
 * depending on their general compatibility and the current test context. Use
 * {@link com.atlassian.jira.webtest.framework.core.locator.Locator#supports(com.atlassian.jira.webtest.framework.core.locator.Locator)}
 * to check for the compatibility of two locators (not that compatibility is not symmetric, see documentation of
 * {@link Locator} for more details).
 *  
 *
 * @see com.atlassian.jira.webtest.framework.core.locator.Locator
 */
public interface LocatorType
{
    /**
     * Unique id of this locator type.
     *
     * @return id of this locator type
     */
    String id();
    
}
