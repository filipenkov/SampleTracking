package com.atlassian.jira.test.qunit;

import com.atlassian.jira.web.ExecutingHttpRequest;

import javax.servlet.http.HttpServletRequest;
import java.text.MessageFormat;

/**
 * For use in velocity - creates a script src attribute pointing to the requested QUnit test JavaScript.
 */
public class QUnitTestResourceProvider
{
    private static final String INTEGRATION_TEST_PATH = "/plugins/servlet/qunit-test-loader/integration/";
    private static final String SRC_FORMAT = "{0}{1}{2}";
    private static final String TEST_PARAM_NAME = "qunit-test";

    public String getScriptSrcIntegrationTest()
    {
        HttpServletRequest request = ExecutingHttpRequest.get();
        String contextPath = request.getContextPath();
        String testName = request.getParameter(TEST_PARAM_NAME);

        return MessageFormat.format(SRC_FORMAT, contextPath, INTEGRATION_TEST_PATH, testName);
    }
}
