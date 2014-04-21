package com.atlassian.jira.webtests.ztests.security;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * @since v4.1.1
 */
@WebTest ({ Category.FUNC_TEST, Category.SECURITY })
public class TestLogout extends FuncTestCase
{
    @Override
    protected void setUpTest()
    {
        administration.restoreBlankInstance();
    }

    public void testInvokingLogOutJspDirectlyResultsInAnError()
    {
        allowUnhandledExceptionsToBeShownInA500Page();
        tester.gotoPage("logout.jsp");
        tester.assertTextPresent("InvalidDirectJspCallException");
        tester.assertTextPresent("Calling logout.jsp directly. This is no longer supported.");
    }

    /**
     * Allows unhandled exceptions to be thrown.
     * Note: There is no need to reset this flag as it is reset in {@link com.atlassian.jira.functest.framework.FuncTestCase#setUp()}
     */
    private void allowUnhandledExceptionsToBeShownInA500Page()
    {
        getTester().getTestContext().getWebClient().setExceptionsThrownOnErrorStatus(false);
    }
}