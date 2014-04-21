package com.atlassian.jira.webtest.webdriver.tests.visualregression;

import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import com.atlassian.selenium.visualcomparison.VisualComparableClient;
import com.atlassian.selenium.visualcomparison.VisualComparer;
import com.atlassian.selenium.visualcomparison.utils.ScreenResolution;
import com.google.inject.Inject;
import org.junit.Before;

/**
 * Abstract base class for UI Regression tests. These tests take screenshots of JIRA and compare them with previous
 * baseline images.
 *
 * @since v4.3
 */
public abstract class JiraVisualRegressionTest extends BaseJiraWebTest
{
    @Inject
    protected VisualComparableClient client;

    protected VisualComparer visualComparer;


    @Before
    public void resetVisualComparer ()
    {
        visualComparer = new VisualComparer(client);
        visualComparer.setRefreshAfterResize(true);
        visualComparer.setScreenResolutions(new ScreenResolution[]
                {
                        new ScreenResolution(1024, 768),
                        new ScreenResolution(1280, 1024)

                });

        //Map<String, String> uiStringReplacements = new HashMap<String, String>();
        //uiStringReplacements.put("studioVersion","(Build information removed)");
        //visualComparer.setUIStringReplacements(uiStringReplacements);

        visualComparer.setWaitforJQueryTimeout(1000);
        visualComparer.enableReportGeneration("target/surefire-reports/visual-regression-reports");
    }



    protected void assertUIMatches(String id)

    {

        visualComparer.assertUIMatches(id, "src/test/resources/data/baseline-screenshots");

    }



}

