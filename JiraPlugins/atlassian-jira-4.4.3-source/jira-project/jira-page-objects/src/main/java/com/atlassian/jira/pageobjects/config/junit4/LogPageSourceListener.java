package com.atlassian.jira.pageobjects.config.junit4;

import com.atlassian.jira.functest.framework.AbstractWebTestListener;
import com.atlassian.jira.functest.framework.WebTestDescription;
import com.atlassian.jira.functest.framework.WebTestListener;
import com.atlassian.jira.functest.framework.log.FuncTestOut;
import com.atlassian.jira.functest.framework.suite.JUnit4WebTestListener;
import com.atlassian.webdriver.AtlassianWebDriver;
import org.junit.runner.notification.RunListener;

import javax.inject.Inject;

/**
 * {@link com.atlassian.jira.functest.framework.WebTestListener} that logs source of the current page into
 * func test log in case of error or failure
 *
 * @since v4.4
 */
public class LogPageSourceListener extends AbstractWebTestListener implements WebTestListener
{
    public static RunListener asRunListener()
    {
        return new JUnit4WebTestListener(new LogPageSourceListener());
    }

    @Inject
    private AtlassianWebDriver webDriver;

    @Override
    public void testError(WebTestDescription description, Throwable error)
    {
        logPageSource(description);
    }

    @Override
    public void testFailure(WebTestDescription description, Throwable failure)
    {
        logPageSource(description);
    }

    private void logPageSource(WebTestDescription description)
    {
        if (!description.isTest())
        {
            return;
        }
        FuncTestOut.log("\n---------------------------------- HTML DUMP ------------------------------------------\n");
        FuncTestOut.log(webDriver.getPageSource());
        FuncTestOut.log("\n-------------------------------- END HTML DUMP ----------------------------------------\n");
    }

}
