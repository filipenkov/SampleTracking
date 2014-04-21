package com.atlassian.jira.webtest.framework.core.locator;

/**
 * An exception thrown by locators provided with other locators that are incompatible with them.
 *
 * @see com.atlassian.jira.webtest.framework.core.locator.Locator
 * @see com.atlassian.jira.webtest.framework.core.locator.Locator#supports(Locator)
 * @see com.atlassian.jira.webtest.framework.core.locator.Locator#combine(Locator)
 *  
 */
public class LocatorIncompatibleException extends IllegalArgumentException
{

    public LocatorIncompatibleException()
    {
    }

    public LocatorIncompatibleException(final String msg)
    {
        super(msg);
    }

    public LocatorIncompatibleException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    public LocatorIncompatibleException(final Throwable cause)
    {
        super(cause);
    }
}
