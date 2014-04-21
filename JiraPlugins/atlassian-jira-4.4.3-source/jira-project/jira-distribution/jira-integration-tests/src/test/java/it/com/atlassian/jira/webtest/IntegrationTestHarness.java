package it.com.atlassian.jira.webtest;

import com.atlassian.jira.webtest.selenium.harness.SeleniumTestHarness;
import com.atlassian.jira.webtests.util.LocalTestEnvironmentData;
import junit.framework.Test;

import java.io.IOException;

public class IntegrationTestHarness
{
    public static Test suite() throws IOException
    {
        return SeleniumTestHarness.makeSuiteFrom(new LocalTestEnvironmentData(), IntegrationAcceptanceTestHarness.SUITE);
    }
}
