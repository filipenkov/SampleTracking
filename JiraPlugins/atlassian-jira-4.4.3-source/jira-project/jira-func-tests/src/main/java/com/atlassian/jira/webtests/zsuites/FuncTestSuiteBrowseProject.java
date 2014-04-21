package com.atlassian.jira.webtests.zsuites;

import com.atlassian.jira.functest.framework.FuncTestSuite;
import com.atlassian.jira.webtests.ztests.filter.TestPresetFiltersWebFragment;
import com.atlassian.jira.webtests.ztests.misc.TestReplacedLocalVelocityMacros;
import com.atlassian.jira.webtests.ztests.project.TestBrowseProjectCreateIssue;
import com.atlassian.jira.webtests.ztests.project.TestBrowseProjectPopularTab;
import com.atlassian.jira.webtests.ztests.project.TestBrowseProjectRoadMapAndChangeLogTab;
import com.atlassian.jira.webtests.ztests.project.TestBrowseProjectSummaryScreen;
import com.atlassian.jira.webtests.ztests.project.TestBrowseProjectVersionAndComponentTab;
import com.atlassian.jira.webtests.ztests.project.TestBrowseVersionsAndComponents;
import com.atlassian.jira.webtests.ztests.project.TestIssuesProjectTabPanel;
import com.atlassian.jira.webtests.ztests.project.TestIssuesTabPanelsIrrelevantIssues;
import com.atlassian.jira.webtests.ztests.project.TestProjectTabPanels;
import com.atlassian.jira.webtests.ztests.project.TestSummaryProjectTabPanel;
import com.atlassian.jira.webtests.ztests.project.component.TestIssuesComponentTabPanel;
import com.atlassian.jira.webtests.ztests.project.component.TestSummaryComponentTabPanel;
import com.atlassian.jira.webtests.ztests.project.version.TestIssuesVersionTabPanel;
import com.atlassian.jira.webtests.ztests.project.version.TestSummaryVersionTabPanel;
import junit.framework.Test;

/**
 * @since v4.0
 */
public class FuncTestSuiteBrowseProject extends FuncTestSuite
{
    /**
     * A static declaration of this particular FuncTestSuite
     */
    public static final FuncTestSuite SUITE = new FuncTestSuiteBrowseProject();

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

    public FuncTestSuiteBrowseProject()
    {
        // fast tests
        addTest(TestBrowseProjectCreateIssue.class);
        addTest(TestBrowseProjectPopularTab.class);
        addTest(TestBrowseProjectVersionAndComponentTab.class);
        addTest(TestBrowseProjectRoadMapAndChangeLogTab.class);
        addTest(TestBrowseVersionsAndComponents.class);
        addTest(TestBrowseProjectSummaryScreen.class);

        // slow test
        addTest(TestIssuesProjectTabPanel.class);
        addTest(TestIssuesTabPanelsIrrelevantIssues.class);
        addTest(TestIssuesComponentTabPanel.class);
        addTest(TestIssuesVersionTabPanel.class);

        addTest(TestSummaryProjectTabPanel.class);
        addTest(TestSummaryComponentTabPanel.class);
        addTest(TestSummaryVersionTabPanel.class);

        addTest(TestPresetFiltersWebFragment.class);
        addTest(TestProjectTabPanels.class);

        //Test for release notes.
        addTest(TestReplacedLocalVelocityMacros.class);

    }

}
