package com.atlassian.jira.webtests.zsuites;

import com.atlassian.jira.functest.framework.FuncTestSuite;
import com.atlassian.jira.webtests.ztests.admin.TestUpgradeTask1_2;
import com.atlassian.jira.webtests.ztests.admin.TestUpgradeTask296;
import com.atlassian.jira.webtests.ztests.filter.TestFilterSharesUpgrade;
import com.atlassian.jira.webtests.ztests.issue.TestUpgradeTask663;
import com.atlassian.jira.webtests.ztests.misc.*;
import com.atlassian.jira.webtests.ztests.navigator.jql.TestUpgradeTask604;
import com.atlassian.jira.webtests.ztests.timetracking.legacy.TestWorklogUpgradeTasks;
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
        addTest(TestUpgradeTask1_2.class);
        addTest(TestUpgradeTask296.class);
        addTest(TestUpgradeTask401.class);
        addTest(TestUpgradeTask602.class);
        addTest(TestUpgradeTask604.class);
        addTest(TestUpgradeTask606.class);
        addTest(TestCronEditorUpgradeTask.class);
        addTest(TestFilterSharesUpgrade.class);
        addTest(TestWorklogUpgradeTasks.class);
        addTest(TestUpgradeTask552.class);
        addTest(TestUpgradeTask641.class);
        addTest(TestUpgradeTask663.class);
    }
}