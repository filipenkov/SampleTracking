package com.atlassian.jira.pageobjects.config.junit4;

import com.atlassian.jira.functest.framework.AbstractWebTestListener;
import com.atlassian.jira.functest.framework.WebTestDescription;
import com.atlassian.jira.functest.framework.WebTestListener;
import com.atlassian.jira.functest.framework.suite.JUnit4WebTestListener;
import com.atlassian.jira.pageobjects.config.TestEnvironment;
import com.atlassian.webdriver.AtlassianWebDriver;
import org.apache.commons.io.FileUtils;
import org.hamcrest.StringDescription;
import org.junit.runner.notification.RunListener;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;

/**
 * {@link com.atlassian.jira.functest.framework.WebTestListener} that performs a screenshot of the current page in
 * case of error or failure
 *
 * @since v4.4
 */
public class ScreenshotListener extends AbstractWebTestListener implements WebTestListener
{
    public static RunListener asRunListener()
    {
        return new JUnit4WebTestListener(new ScreenshotListener());
    }

    private static final Logger logger = LoggerFactory.getLogger(ScreenshotListener.class);

    @Inject
    private AtlassianWebDriver webDriver;

    @Inject
    private TestEnvironment testEnvironment;

    @Override
    public void testError(WebTestDescription description, Throwable error)
    {
        attemptScreenshot(description);
    }

    @Override
    public void testFailure(WebTestDescription description, Throwable failure)
    {
        attemptScreenshot(description);
    }

    private void attemptScreenshot(WebTestDescription description)
    {
        if (!description.isTest())
        {
            return;
        }
        if (!isScreenshotCapable())
        {
            logger.warn(new StringDescription().appendText("Unable to take screenshot: WebDriver ")
                    .appendValue(webDriver.getDriver()).appendText(" is not instance of TakesScreenshot").toString());
            return;
        }
        takeScreenshot(description);
    }

    private void takeScreenshot(WebTestDescription description)
    {
        try
        {
            TakesScreenshot takingScreenshot = (TakesScreenshot) webDriver.getDriver();
            File screenshot = takingScreenshot.getScreenshotAs(OutputType.FILE);
            File target = new File(testEnvironment.artifactDirectory(), fileName(description));
            FileUtils.copyFile(screenshot, target);
            logger.info("A screenshot of the page has been stored under " + target.getAbsolutePath());
        }
        catch(Exception e)
        {
            logger.error(new StringDescription().appendText("Unable to take screenshot for failed test ")
                    .appendValue(description).toString(), e);
        }
    }

    private String fileName(WebTestDescription description)
    {
        return description.testClass().getSimpleName() + "." + description.methodName() + ".png";
    }

    private boolean isScreenshotCapable()
    {
        return webDriver.getDriver() instanceof TakesScreenshot;
    }
}
