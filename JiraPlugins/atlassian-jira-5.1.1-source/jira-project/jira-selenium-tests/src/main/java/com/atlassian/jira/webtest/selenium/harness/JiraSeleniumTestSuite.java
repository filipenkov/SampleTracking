package com.atlassian.jira.webtest.selenium.harness;

import com.atlassian.jira.webtest.selenium.JiraSeleniumConfiguration;
import com.atlassian.jira.webtests.util.LocalTestEnvironmentData;
import com.atlassian.selenium.SeleniumConfiguration;
import com.atlassian.selenium.SeleniumTestSuite;

/**
 *
 * @since v3.12
 */
public class JiraSeleniumTestSuite extends SeleniumTestSuite {

    private final LocalTestEnvironmentData environmentData;

    public JiraSeleniumTestSuite(LocalTestEnvironmentData environmentData){
        this.environmentData = environmentData;
    }

    protected SeleniumConfiguration getSeleniumConfiguration() {
        return new JiraSeleniumConfiguration(environmentData);
    }
}
