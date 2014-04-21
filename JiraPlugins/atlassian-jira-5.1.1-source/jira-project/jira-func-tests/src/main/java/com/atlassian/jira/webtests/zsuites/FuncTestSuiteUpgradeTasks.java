package com.atlassian.jira.webtests.zsuites;

import com.atlassian.jira.functest.framework.FuncTestSuite;
import com.atlassian.jira.webtests.ztests.misc.TestUpgradeTask552;
import com.atlassian.jira.webtests.ztests.misc.TestUpgradeTask606;
import com.atlassian.jira.webtests.ztests.misc.TestUpgradeTask641;
import com.atlassian.jira.webtests.ztests.misc.TestUpgradeTask701;
import com.atlassian.jira.webtests.ztests.misc.TestUpgradeTask707;
import com.atlassian.jira.webtests.ztests.misc.TestUpgradeTasks752To754;
import com.atlassian.jira.webtests.ztests.upgrade.tasks.TestUpgradeTask761;
import com.atlassian.jira.webtests.ztests.user.TestUpgradeTask602;
import junit.framework.Test;

/**
 * A suite of test related to Upgrade Tasks
 *
 * @since v4.0
 */
public class FuncTestSuiteUpgradeTasks extends FuncTestSuite
{
    /**
     * A static declaration of this particular FuncTestSuite
     */
    public static final FuncTestSuite SUITE = new FuncTestSuiteUpgradeTasks();

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

    public FuncTestSuiteUpgradeTasks()
    {
        addTest(TestUpgradeTask602.class);
        addTest(TestUpgradeTask606.class);
        addTest(TestUpgradeTask552.class);
        addTest(TestUpgradeTask641.class);
        addTest(TestUpgradeTask701.class);
        addTest(TestUpgradeTask707.class);
        addTest(TestUpgradeTasks752To754.class);
        addTest(TestUpgradeTask761.class);
    }
}