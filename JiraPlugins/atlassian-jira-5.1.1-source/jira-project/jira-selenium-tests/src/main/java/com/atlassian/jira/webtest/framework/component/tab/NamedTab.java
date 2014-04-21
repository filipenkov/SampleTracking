package com.atlassian.jira.webtest.framework.component.tab;

/**
 * A tab with a name.
 *
 * @since v4.3
 */
public interface NamedTab<T extends NamedTab<T>> extends Tab<T>
{

    /**
     * Name of the tab.
     *
     * @return name of the tab
     */
    String name();
}
