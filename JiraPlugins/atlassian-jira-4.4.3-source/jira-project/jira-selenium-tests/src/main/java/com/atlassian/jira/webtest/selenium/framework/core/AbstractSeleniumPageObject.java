package com.atlassian.jira.webtest.selenium.framework.core;

import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.selenium.SeleniumAssertions;
import com.atlassian.selenium.SeleniumClient;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * <p>
 * Abstract Selenium utility class, exposes protected variables that every decent page object
 * needs to survive in the Selenium World&trade;, encapsulated in an instance of
 * {@link com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext}.
 *
 * <p>
 * Your page objects will be so much <i>cooler</i> now!
 *
 * @since v4.2
 * @deprecated use {@link com.atlassian.jira.webtest.framework.impl.selenium.core.AbstractSeleniumPageObject}
 */
@Deprecated
public abstract class AbstractSeleniumPageObject
{
    protected final SeleniumClient client;
    protected final SeleniumAssertions assertThat;

    protected final SeleniumContext context;

    protected AbstractSeleniumPageObject(SeleniumContext context)
    {
        this.context = notNull("context", context);
        this.client = context.client();
        this.assertThat = context.assertions();
    }
    
}
