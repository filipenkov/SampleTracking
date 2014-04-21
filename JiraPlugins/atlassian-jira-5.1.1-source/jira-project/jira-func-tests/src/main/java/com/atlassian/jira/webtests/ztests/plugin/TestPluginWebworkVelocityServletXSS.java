package com.atlassian.jira.webtests.ztests.plugin;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * Test for XSS in JiraPluginWebworkVelocityServlet.
 *
 * @since v4.4
 */
@WebTest ({ Category.FUNC_TEST, Category.PLUGINS, Category.SECURITY })
public class TestPluginWebworkVelocityServletXSS extends FuncTestCase
{
    public void testRenderingError()
    {
        tester.gotoPage("/secure/<script>alert('XSS!')</script>.vm");
        assertions.getTextAssertions().assertTextNotPresent("<script>alert('XSS!')</script>");
    }

    public void testXssInPathNameParam()
    {
        tester.gotoPage("/secure/\"><script>alert</script>.vm");
        assertions.getTextAssertions().assertTextPresent("Could not find template");
        assertions.getTextAssertions().assertTextNotPresent("secure/\"><script>alert</script>.vm");
    }
}
