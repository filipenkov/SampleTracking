package com.atlassian.jira.webtest.selenium;

import com.atlassian.selenium.AbstractSeleniumConfiguration;
import com.atlassian.jira.webtests.util.LocalTestEnvironmentData;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;

import java.util.Properties;

/**
 * @since v3.12
 */
public class JiraSeleniumConfiguration extends AbstractSeleniumConfiguration {

    public static final String SELENIUM_PROPERTY_LOCATION = "jira.functest.seleniumproperties";
    private static final String DEFAULT_SELENIUMTEST_PROPERTIES = "seleniumtest.properties";
    private final Properties seleniumProperties = LocalTestEnvironmentData.loadProperties(SELENIUM_PROPERTY_LOCATION, DEFAULT_SELENIUMTEST_PROPERTIES);
    private final JIRAEnvironmentData environmentData;

    public JiraSeleniumConfiguration(JIRAEnvironmentData environmentData)
    {
        this.environmentData = environmentData;
    }

    public String getServerLocation() {
         return seleniumProperties.getProperty("selenium.server.location");
    }

    public String getFirefoxProfileTemplate() {
        return seleniumProperties.getProperty("selenium.browser.profile");
    }

    public int getServerPort() {
        return Integer.parseInt(seleniumProperties.getProperty("selenium.server.port"));
    }

    public String getBrowserStartString() {
        String startString = seleniumProperties.getProperty("selenium.browser.startstring." + System.getProperty("os.name"));
        if (startString == null) {
            return seleniumProperties.getProperty("selenium.browser.startstring");
        } else {
            return startString;
        }
    }

    public String getBaseUrl() {
        return environmentData.getBaseUrl().toString();
    }

    public boolean getStartSeleniumServer()
    {
        boolean startSeleniumServer = true;
        String seleniumServerStartStr = seleniumProperties.getProperty("selenium.server.start");
        if(seleniumServerStartStr != null)
        {
            startSeleniumServer = Boolean.parseBoolean(seleniumServerStartStr);
        }

        return startSeleniumServer;
    }
}
