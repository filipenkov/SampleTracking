package com.atlassian.jira.webtests.ztests.project;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * Test how the statistics break down when you have fields hidden
 *
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.BROWSE_PROJECT })
public class TestIssuesTabPanelsIrrelevantIssues extends FuncTestCase
{
    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestIssuesTabPanelsIrrelevantIssues.xml");
    }

    public void testProjectIssues() throws Exception
    {
        navigation.browseProjectTabPanel("ANA", "issues");

        assertPriorityBreakdown("Major", "4", "100%");

        assertAssigneeBreakdown("Admin", "4", "100%");

        assertFixVersionBreakdown(
                "1", "Aversion 1",
                "1", "Aversion 2",
                "1", "Aversion 3",
                "1", "Unscheduled");

        assertComponentBreakdown(
                "1", "Acomponent 1",
                "3", "No Component"
        );

        // hide Fix Version in default config and reload tab panel
        administration.fieldConfigurations().defaultFieldConfiguration().hideFields("Assignee");
        administration.fieldConfigurations().defaultFieldConfiguration().hideFields("Component/s");
        administration.fieldConfigurations().defaultFieldConfiguration().hideFields("Fix Version/s");
        administration.fieldConfigurations().defaultFieldConfiguration().hideFields("Priority");
        administration.reIndex();

        navigation.browseProjectTabPanel("ANA", "issues");

        assertPriorityBreakdown(
                "Major", "2", "50%",
                "Irrelevant", "2", "50%"
        );

        assertAssigneeBreakdown(
                "Admin", "2", "50%",
                "Irrelevant", "2", "50%"
        );

        assertFixVersionBreakdown(
                "1", "Aversion 2",
                "1", "Aversion 3",
                "2", "Irrelevant"
        );

        assertComponentBreakdown(
                "1", "Acomponent 1",
                "1", "No Component",
                "2", "Irrelevant"
        );

        assertions.getLinkAssertions().assertLinkNotPresentWithExactText("//div[@id='project-tab']", "Irrelevant");
    }

    public void testVersionIssues() throws Exception
    {
        navigation.browseVersionTabPanel("ANA", "Aversion 1", "issues");

        assertPriorityBreakdown("Major", "1", "100%");

        assertAssigneeBreakdown("Admin", "1", "100%");

        assertComponentBreakdown("1", "No Component");

        // hide fields in default config and reload tab panel
        administration.fieldConfigurations().defaultFieldConfiguration().hideFields("Assignee");
        administration.fieldConfigurations().defaultFieldConfiguration().hideFields("Component/s");
        administration.fieldConfigurations().defaultFieldConfiguration().hideFields("Priority");
        administration.reIndex();

        navigation.browseVersionTabPanel("ANA", "Aversion 1", "issues");

        assertPriorityBreakdown("Irrelevant", "1", "100%");

        assertAssigneeBreakdown("Irrelevant", "1", "100%");

        assertComponentBreakdown("1", "Irrelevant");

        assertions.getLinkAssertions().assertLinkNotPresentWithExactText("//div[@id='project-tab']", "Irrelevant");
    }

    public void testComponentIssues() throws Exception
    {
        navigation.browseComponentTabPanel("ANA", "Acomponent 1", "issues");

        assertPriorityBreakdown("Major", "1", "100%");

        assertAssigneeBreakdown("Admin", "1", "100%");

        assertFixVersionBreakdown("1", "Aversion 3");

        // hide fields in copy of default config (because issue with component is a Task not Bug) and reload tab panel
        administration.fieldConfigurations().fieldConfiguration("Copy of Default Field Configuration").hideFields("Assignee");
        administration.fieldConfigurations().fieldConfiguration("Copy of Default Field Configuration").hideFields("Fix Version/s");
        administration.fieldConfigurations().fieldConfiguration("Copy of Default Field Configuration").hideFields("Priority");
        administration.reIndex();

        navigation.browseComponentTabPanel("ANA", "Acomponent 1", "issues");

        assertPriorityBreakdown("Irrelevant", "1", "100%");

        assertAssigneeBreakdown("Irrelevant", "1", "100%");

        assertFixVersionBreakdown("1", "Irrelevant");

        assertions.getLinkAssertions().assertLinkNotPresentWithExactText("//div[@id='project-tab']", "Irrelevant");
    }

    private void assertAssigneeBreakdown(String... breakdown)
    {
        text.assertTextSequence(new IdLocator(tester, "fragunresolvedissuesbyassignee"), breakdown);
    }

    private void assertFixVersionBreakdown(String... breakdown)
    {
        text.assertTextSequence(new IdLocator(tester, "fragunresolvedissuesbyfixversion"), breakdown);
    }

    private void assertPriorityBreakdown(String... breakdown)
    {
        text.assertTextSequence(new IdLocator(tester, "fragunresolvedissuesbypriority"), breakdown);
    }

    private void assertComponentBreakdown(String... breakdown)
    {
        text.assertTextSequence(new IdLocator(tester, "fragunresolvedissuesbycomponent"), breakdown);
    }
}
