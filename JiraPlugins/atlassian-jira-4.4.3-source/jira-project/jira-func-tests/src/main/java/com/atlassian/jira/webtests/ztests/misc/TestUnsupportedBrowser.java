package com.atlassian.jira.webtests.ztests.misc;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import net.sourceforge.jwebunit.TestContext;

/**
 * Tests the Unsupported Browser functionality
 *
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.BROWSING })
public class TestUnsupportedBrowser  extends FuncTestCase
{
    public void testIE6()
    {
        TestContext ctx = tester.getTestContext();
        ctx.setUserAgent("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1");
        navigation.loginUsingForm(ADMIN_USERNAME, ADMIN_PASSWORD);
        tester.assertElementPresent("browser-warning");
        ctx.addCookie("UNSUPPORTED_BROWSER_WARNING", "handled");
        navigation.logout();
        tester.assertElementNotPresent("browser-warning");
    }
}
