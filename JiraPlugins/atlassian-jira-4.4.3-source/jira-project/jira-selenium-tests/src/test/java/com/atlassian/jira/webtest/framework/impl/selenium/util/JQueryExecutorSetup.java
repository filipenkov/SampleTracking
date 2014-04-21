package com.atlassian.jira.webtest.framework.impl.selenium.util;

import com.atlassian.selenium.mock.MockSeleniumClient;


/**
 * Sets up a mock Selenium context to answer 'true' to the
 * {@link com.atlassian.jira.webtest.framework.impl.selenium.util.JqueryExecutor} 'canExecute' query
 *
 * @since v4.3
 */
public class JQueryExecutorSetup
{
    public static void setUpSelenium(MockSeleniumClient client)
    {
        client.addScriptResult(JqueryExecutor.JQUERY_CHECK_IS_DEFINED, "true")
                .addScriptResult(JqueryExecutor.JQUERY_CHECK_IS_FUNCTION, "true");
    }
}
