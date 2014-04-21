package com.atlassian.jira.webtest.framework.core.context;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Base class for all implementations of {@link com.atlassian.jira.webtest.framework.core.context.WebTestContextAware}.
 *
 * @since v4.3
 */
public abstract class AbstractWebTestContextAware implements WebTestContextAware
{
    protected final WebTestContext context;

    public AbstractWebTestContextAware(WebTestContext context)
    {
        this.context = notNull("context", context);
    }

    @Override
    public WebTestContext context()
    {
        return context;
    }
}
