package com.atlassian.jira.webtests.ztests.project;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

@WebTest ({ Category.FUNC_TEST, Category.BROWSE_PROJECT, Category.PROJECTS })
public class TestProjectTabPanels extends FuncTestCase
{
    public void tearDownTest()
    {
        administration.restoreBlankInstance();
        super.tearDownTest();
    }

    public void testTabPanelLinks()
    {
        administration.restoreBlankInstance();

        tester.gotoPage("browse/" + PROJECT_HOMOSAP_KEY);
        tester.assertLinkPresentWithText("Summary");
        tester.assertLinkPresentWithText("Issues");
        tester.assertLinkPresentWithText("Road Map");
        tester.assertLinkPresentWithText("Change Log");
        tester.assertLinkPresentWithText("Popular Issues");
        tester.assertLinkPresentWithText("Versions");
        tester.assertLinkPresentWithText("Components");

        tester.gotoPage("browse/" + PROJECT_MONKEY_KEY);
        tester.assertLinkPresentWithText("Summary");
        tester.assertLinkPresentWithText("Issues");
        tester.assertLinkNotPresentWithText("Road Map");
        tester.assertLinkNotPresentWithText("Change Log");
        tester.assertLinkPresentWithText("Popular Issues");
        // not present - there are no versions for this project
        tester.assertLinkNotPresentWithText("Versions");
        // not present - there are no components for this project
        tester.assertLinkNotPresentWithText("Components");

        // hide fixfor versions and components fields
        administration.fieldConfigurations().defaultFieldConfiguration().hideField(8);
        administration.fieldConfigurations().defaultFieldConfiguration().hideField(4);

        tester.gotoPage("browse/" + PROJECT_HOMOSAP_KEY);

        tester.assertLinkPresentWithText("Summary");
        tester.assertLinkPresentWithText("Issues");
        tester.assertLinkNotPresentWithText("Road Map");
        tester.assertLinkNotPresentWithText("Change Log");
        tester.assertLinkPresentWithText("Popular Issues");
        tester.assertLinkNotPresentWithText("Versions");
        tester.assertLinkNotPresentWithText("Components");
    }
}