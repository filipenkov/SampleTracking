package com.atlassian.jira.webtest.framework.impl.selenium.core;

import com.atlassian.selenium.SeleniumAssertions;
import com.atlassian.selenium.SeleniumClient;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Base class for all objects in the Selenium framework aware of and using
 * {@link SeleniumContext}.
 *
 * @since v4.3
 */
public abstract class SeleniumContextAware
{
    protected final SeleniumContext context;

    protected final SeleniumClient client;
    protected final SeleniumAssertions assertThat;
    protected final DefaultTimeouts timeouts;

    protected SeleniumContextAware(SeleniumContext context)
    {
        this.context = notNull("context", context);
        this.client = context.client();
        this.assertThat = context.assertions();
        this.timeouts = context.timeouts();
    }

    public final SeleniumContext context()
    {
        return context;
    }
}
