package com.atlassian.jira.webtest.selenium.gadgets;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

@WebTest({Category.SELENIUM_TEST })
public class TestProjectGadget extends StatsTestBase
{
    @Override
    public void onSetUp()
    {
        super.onSetUp();
        addGadget("Projects");
    }

    protected void restoreGadgetData()
    {
        restoreData("projectgadget.xml");
    }

    public void testGadgetConfigurationShouldShowAvailableProjectsAndRefreshInterval()
    {
        waitForGadgetConfiguration();
        assertThat.textPresent("Projects");
        assertThat.textPresent(HOMOSAPIEN);
        assertThat.textPresent(MONKEY);

        assertRefreshIntervalFieldPresent();
    }

    public void testGadgetShouldShowLinksToProjectsAndProjectLeads()
    {
        getWebUnitTest().login(ADMIN_USERNAME, ADMIN_PASSWORD);
        getWebUnitTest().addIssueOnly(HOMOSAPIEN, null, null, BLARGH + 1, null, null, null, null, ADMIN_NAME, null, null, null, null, null);
        getWebUnitTest().addIssueOnly(HOMOSAPIEN, null, null, BLARGH + 2, null, null, null, null, ADMIN_NAME, null, null, null, null, null);
        assertThat.elementPresentByTimeout("jquery=#proj_cat_picker_projectsOrCategories .multi-select", 10000);
        client.addSelection("jquery=#proj_cat_picker_projectsOrCategories .multi-select", "All Projects");
        client.click("jquery=input.button.save");

        assertThat.elementPresentByTimeout("jquery=ul.project-list", 10000);
        assertThat.linkPresentWithText(HOMOSAPIEN);
        assertThat.linkPresentWithText(MONKEY);
        assertThat.linkPresentWithText(ADMIN_NAME);
    }

    public void testProjectCategories()
    {
        waitForGadgetConfiguration();
        client.click("jquery=.proj-cat-option:eq(1)");
        client.addSelection("jquery=#proj_cat_picker_projectsOrCategories .multi-select", "All Categories");
        client.click("jquery=input.button.save");
        assertThat.elementPresentByTimeout("jquery=ul.project-list", 10000);
        assertThat.elementContainsText("jquery=.category-item:eq(0) h4", "Blue");
        assertThat.elementContainsText("jquery=.category-item:eq(1) h4", "Green");
        assertThat.elementContainsText("jquery=.category-item:eq(2) h4", "No Categories");

    }

       // Commented due to a legitimate bug. See JRADEV-3662
//    public void testGadgetShouldShowLinkToCreateFirstProject() throws Exception
//    {
//        getWebUnitTest().login(ADMIN_USERNAME, ADMIN_PASSWORD);
//        getWebUnitTest().deleteProject("monkey");
//        getWebUnitTest().deleteProject("homosapien");
//
//        assertThat.elementPresentByTimeout("id=projectsOrCategories", 10000);
//        client.addSelection("id=projectsOrCategories", "All Projects");
//        client.click("jquery=input.button.save");
//
//        assertThat.elementPresentByTimeout("jquery=div.project-message", 10000);
//        assertThat.textPresent("You have no projects yet.");
//        assertThat.linkPresentWithText("Create your first project");
//        assertThat.linkPresentWithText("online documentation");
//    }


}
