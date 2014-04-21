package com.atlassian.jira.webtests.zsuites;

import com.atlassian.jira.functest.framework.FuncTestSuite;
import com.atlassian.jira.webtests.ztests.dashboard.portlet.TestLazyLoadingPortletServlet;
import com.atlassian.jira.webtests.ztests.filter.TestFavouritesPortletAndPopup;
import com.atlassian.jira.webtests.ztests.misc.TestReplacedLocalVelocityMacros;
import junit.framework.Test;

/**
 * A suite of tests related to Portlets
 *
 * @since v4.0
 */
public class FuncTestSuitePortlets extends FuncTestSuite
{
    /**
     * A static declaration of this particular FuncTestSuite
     */
    public static final FuncTestSuite SUITE = new FuncTestSuitePortlets();

    /**
     * The pattern in JUnit/IDEA JUnit runner is that if a class has a static suite() method that returns a Test, then
     * this is the entry point for running your tests.  So make sure you declare one of these in the FuncTestSuite
     * implementation.
     *
     * @return a Test that can be run by as JUnit TestRunner
     */
    public static Test suite()
    {
        return SUITE.createTest();
    }

    public FuncTestSuitePortlets()
    {
        addTest(TestFavouritesPortletAndPopup.class);
        addTest(TestLazyLoadingPortletServlet.class);

        //Contains tests for portlets.
        addTest(TestReplacedLocalVelocityMacros.class);
    }
}