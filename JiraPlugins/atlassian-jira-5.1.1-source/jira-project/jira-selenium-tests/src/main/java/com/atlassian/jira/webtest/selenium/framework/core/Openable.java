package com.atlassian.jira.webtest.selenium.framework.core;

/**
 * Page object that is capable of being opened and closed.
 *
 * @since v4.2
 */
public interface Openable
{
    public static interface OpenMode
    {
        Openable byClick();

        Openable byShortcut();
    }

    // TODO come up with better abstractions, return Query object that will enable querying by chosen timeout

    boolean isOpenBy(long timeout);

    boolean canOpenBy(long timeout);

    Openable assertIsOpen(long timout);

    OpenMode open();

    // TODO other: close, isClosed, toggle, canClose
}
