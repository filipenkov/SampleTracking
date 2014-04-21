package com.atlassian.jira.webtest.framework.core.context;

/**
 * A component aware of the web test context.
 *
 * @since v4.3
 */
public interface WebTestContextAware
{
    /**
     * Web test context instance of this component.
     *
     * @return web test context
     */
    WebTestContext context();
}
