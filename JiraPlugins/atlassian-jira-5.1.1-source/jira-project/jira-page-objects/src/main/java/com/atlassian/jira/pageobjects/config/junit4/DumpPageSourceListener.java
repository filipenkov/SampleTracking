package com.atlassian.jira.pageobjects.config.junit4;

import com.atlassian.jira.functest.framework.AbstractWebTestListener;
import com.atlassian.jira.functest.framework.WebTestDescription;
import com.atlassian.jira.functest.framework.WebTestListener;
import com.atlassian.jira.functest.framework.suite.JUnit4WebTestListener;
import com.atlassian.jira.pageobjects.config.TestEnvironment;
import com.atlassian.webdriver.AtlassianWebDriver;
import org.junit.runner.notification.RunListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;

/**
 * {@link com.atlassian.jira.functest.framework.WebTestListener} that dumps source of the current page into a file in
 * case of error or failure
 *
 * @since v4.4
 */
public class DumpPageSourceListener extends AbstractWebTestListener implements WebTestListener
{
    public static RunListener asRunListener()
    {
        return new JUnit4WebTestListener(new DumpPageSourceListener());
    }

    private static final Logger logger = LoggerFactory.getLogger(DumpPageSourceListener.class);

    @Inject
    private AtlassianWebDriver webDriver;

    @Inject
    private TestEnvironment testEnvironment;

    @Override
    public void testError(WebTestDescription description, Throwable error)
    {
        dumpPageSource(description);
    }

    @Override
    public void testFailure(WebTestDescription description, Throwable failure)
    {
        dumpPageSource(description);
    }

    private void dumpPageSource(WebTestDescription description)
    {
        if (!description.isTest())
        {
            return;
        }
        File target = targetFile(description);
        webDriver.dumpSourceTo(target);
        logger.info("An HTML dump of the page has been stored under " + target.getAbsolutePath());
    }

    private File targetFile(WebTestDescription description)
    {
        return new File(testEnvironment.artifactDirectory(), fileName(description));
    }

    private String fileName(WebTestDescription description)
    {
        return description.testClass().getSimpleName() + "." + description.methodName() + ".html";
    }
}
