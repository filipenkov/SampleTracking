package com.atlassian.jira.webtest.selenium.visualregression;

import com.atlassian.jira.functest.framework.util.env.EnvironmentUtils;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import com.atlassian.selenium.visualcomparison.SeleniumVisualComparer;
import com.atlassian.selenium.visualcomparison.utils.ScreenResolution;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract base class for UI Regression tests. These tests take screenshots of JIRA and compare them with previous
 * baseline images.
 *
 * @since v4.3
 */
public abstract class JiraVisualRegressionTest extends JiraSeleniumTest
{
    protected abstract String getXmlDataName();
    protected SeleniumVisualComparer visualComparer;

    // Don't restore the data for every single test; there's no need
    // and it takes much longer than necessary.
    // We can't use the @BeforeClass annotation because we are indirectly inheriting from TestClass.
    private static boolean hasBeenSetUp = false;

    public void onSetUp()
    {
        super.onSetUp();
        if (!hasBeenSetUp)
        {
            restoreData(getXmlDataName());
            hasBeenSetUp = true;
        }
        else
        {
            getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
        }
        // Initialise this every time, so individual tests can change settings if needed.
        visualComparer = new SeleniumVisualComparer(client);
        visualComparer.setRefreshAfterResize(true);
        visualComparer.setScreenResolutions(new ScreenResolution[]
            {
                    new ScreenResolution(1024, 768),
                    new ScreenResolution(1280, 1024)
            });
        Map<String, String> uiStringReplacements = new HashMap<String, String>();
        uiStringReplacements.put("footer-build-information","(Build information removed)");
        visualComparer.setUIStringReplacements(uiStringReplacements);
        visualComparer.enableReportGeneration(EnvironmentUtils.getMavenAwareOutputDir());
    }

    protected void assertUIMatches(String id)
    {
        visualComparer.assertUIMatches(id, environmentData.getXMLDataLocation() + "/baseline-screenshots");
    }

    // Override runBareTestCase() from JiraSeleniumTest because there's no value
    // in taking another screenshot if it fails, it just clutters up the output directory.
    protected void runBareTestCase() throws Throwable
    {
        setUp();
        try
        {
            runTest();
        }
        catch (Throwable throwable)
        {
            throw throwable;
        }
        finally
        {
            tearDown();
        }
    }
}
