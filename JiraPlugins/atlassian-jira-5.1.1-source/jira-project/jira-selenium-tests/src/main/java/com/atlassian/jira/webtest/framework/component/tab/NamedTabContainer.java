package com.atlassian.jira.webtest.framework.component.tab;

/**
 * Container of named tabs.
 *
 * @since v4.3
 */
public interface NamedTabContainer<T extends NamedTab<T>>
{

    /**
     * Open tab with given <tt>tabName</tt>
     *
     * @param tabName name of the tab to open
     * @return tab instance
     */
    T openTab(String tabName);

    /**
     * Retrun tab with given name.
     *
     * @param name name of the tab
     * @return tab instance
     */
    T tab(String name);
}
