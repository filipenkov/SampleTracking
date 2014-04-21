package com.atlassian.jira.collector.plugin.components;

/**
 * A helper to render the Javascript code necessary to include a collector on another HTML page
 *
 * @since v4.4
 */
public interface ScriptletRenderer
{
    /**
     * Returns the javascript code necessary to include the collector in another HTML page.
     *
     * @param collector The collector to include in another page
     * @return the javascript code necessary to include the collector in another HTML page
     */
    String render(final Collector collector);

    /**
     * Returns the javascript code necessary to include the collector in another Javascript resource.
     *
     * @param collector The collector to include in another page
     * @return the javascript code necessary to include the collector in another Javascript resource.
     */
    String renderJavascript(final Collector collector);
}
