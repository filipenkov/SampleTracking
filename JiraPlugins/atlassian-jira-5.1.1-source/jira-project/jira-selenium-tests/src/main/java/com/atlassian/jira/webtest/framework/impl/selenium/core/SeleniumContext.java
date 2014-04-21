package com.atlassian.jira.webtest.framework.impl.selenium.core;

import com.atlassian.jira.webtest.framework.core.PageObject;
import com.atlassian.jira.webtest.framework.core.PageObjectFactory;
import com.atlassian.jira.webtest.framework.core.Timeouts;
import com.atlassian.jira.webtest.framework.core.context.Browser;
import com.atlassian.jira.webtest.framework.core.context.BrowserBean;
import com.atlassian.jira.webtest.framework.core.context.WebTestContext;
import com.atlassian.jira.webtest.framework.impl.selenium.core.ui.SeleniumUi;
import com.atlassian.jira.webtest.framework.page.GlobalPages;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.atlassian.selenium.SeleniumAssertions;
import com.atlassian.selenium.SeleniumClient;
import com.atlassian.selenium.SeleniumConfiguration;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Encapsulates all key control objects involved in the Selenium test, for easy passing around.
 *
 * @since v4.2
 * @see com.atlassian.jira.webtest.framework.core.context.WebTestContext
 */
public final class SeleniumContext implements WebTestContext
{

    private final SeleniumClient client;
    private final SeleniumAssertions assertions;
    private final JIRAEnvironmentData environmentData;

    private final SeleniumConfiguration configuration;
    private final DefaultTimeouts timeouts;

    private final GlobalPages globalPages;
    private final PageObjectFactory objectFactory;
    private final Browser browser;
    private final SeleniumUi ui;

    public SeleniumContext(SeleniumClient client, SeleniumConfiguration config)
    {
        this(client, null, config, null);
    }

    // hide this monster from public
    public SeleniumContext(SeleniumClient client, SeleniumAssertions assertions, SeleniumConfiguration configuration,
            JIRAEnvironmentData envData)
    {
        this.client = notNull("client", client);
        this.assertions = assertions;
        this.configuration = notNull("configuration", configuration);
        this.environmentData = envData;
        this.timeouts = new DefaultTimeouts(configuration);
        this.globalPages = new SeleniumGlobalPages(this);
        this.objectFactory = new SeleniumPageObjectFactory(this);
        this.browser = initBrowser();
        this.ui = new SeleniumUi(this);
    }

    private Browser initBrowser()
    {
        // TODO HTFU & retrieve version (if necessary)
        com.atlassian.selenium.Browser seleniumBrowser = client.getBrowser();
        SeleniumBrowserType mapping = seleniumBrowser != null ? SeleniumBrowserType.forSeleniumType(seleniumBrowser) : SeleniumBrowserType.UNKNOWN;
        return new BrowserBean(mapping.apiType(), "Unknown");
    }

    public SeleniumClient client()
    {
        return client;
    }

    public SeleniumAssertions assertions()
    {
        return assertions;
    }

    public SeleniumConfiguration configuration()
    {
        return configuration;
    }

    public DefaultTimeouts timeouts()
    {
        return timeouts;
    }

    public long timeoutFor(Timeouts timeoutType)
    {
        return timeouts.timeoutFor(timeoutType);
    }

    public JIRAEnvironmentData environmentData()
    {
        return environmentData;
    }

    /* ----------------------------------------------- WebTestContext ----------------------------------------------- */

    @Override
    public PageObjectFactory pageObjectFactory()
    {
        return objectFactory;
    }

    @Override
    public <P extends PageObject> P getPageObject(Class<P> componentType)
    {
        // TODO more sohisticated! E.g. cache first-level pages, they don't need to be created each time anew
        return objectFactory.createPageObject(componentType);
    }

    @Override
    public GlobalPages globalPages()
    {
        return globalPages;
    }

    @Override
    public Browser browser()
    {
        return browser;
    }

    @Override
    public SeleniumUi ui()
    {
        return ui;
    }
}
