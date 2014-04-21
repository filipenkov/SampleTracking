package com.atlassian.jira.webtests.ztests.filter;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.meterware.httpunit.HttpUnitOptions;

/**
 * A func test for testing basic page navigation between Filter related pages.
 * <p/>
 * Does stuff go back to where it should
 *
 * @since v3.13
 */
@WebTest ({ Category.FUNC_TEST, Category.BROWSING })
public class TestFilterPageNavigation extends FuncTestCase
{
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("sharedfilters/SharedFiltersBase.xml");
        HttpUnitOptions.setScriptingEnabled(true);
    }

    protected void tearDownTest()
    {
        super.tearDownTest();
        HttpUnitOptions.setScriptingEnabled(false);
    }

    /**
     * As we move around the page, do things return back to the right place?
     *
     * @throws Exception
     */
    public void testBasicNavigationPage() throws Exception
    {
        tester.gotoPage("secure/ManageFilters.jspa");

        // follow the individual filter edit paths
        tester.clickLink("edit_filter_10000");

        pressCancel();
        assertOnManageFiltersPage();

        tester.clickLink("edit_filter_10000");
        tester.setWorkingForm("filter-edit");
        tester.setFormElement("filterName", "Nothing in The Session Filter");
        tester.submit("Save");
        assertOnManageFiltersPage();

        // now we have something in the session we have more edit links
        // try the edit current operation link
        tester.clickLink("edit_filter_10000");
        pressCancel();
        assertOnManageFiltersPage();

        tester.clickLink("edit_filter_10000");
        tester.setWorkingForm("filter-edit");
        tester.setFormElement("filterName", "Now Its In The Session Filter");
        tester.submit("Save");
        assertOnManageFiltersPage();

        // ok move to another tab view and try the left hand side edit again
        tester.clickLink("filterlink_10000");
        tester.clickLink("filtereditshares");
        pressCancel();
        assertOnFilterPage("Now Its In The Session Filter");

        tester.clickLink("filtereditshares");
        tester.setWorkingForm("filter-edit");
        tester.setFormElement("filterName", "Now has New Name");
        tester.submit("Save");
        assertOnFilterPage("Now has New Name");

        //______________________________________________
        // make it saved in the session
        
        // Removed as there is neither a link nor button named save on the manage filters page.
        //tester.clickLinkWithText("Save");
        //tester.submit("Save");

        tester.gotoPage("secure/ManageFilters.jspa");
        tester.clickLinkWithText("Delete");
        tester.clickLinkWithText("Cancel");
        assertOnManageFiltersPage();
    }

    public void testEditValidationFailures() throws Exception
    {
        tester.gotoPage("secure/ManageFilters.jspa");

        // follow the individual filter edit paths
        tester.clickLink("edit_filter_10000");
        pressCancel();
        assertOnManageFiltersPage();

        tester.clickLink("edit_filter_10000");
        tester.setWorkingForm("filter-edit");
        tester.setFormElement("filterName", "");
        tester.submit("Save");
        pressCancel();
        assertOnManageFiltersPage();

        // now we have something in the session we have more edit links
        // try the edit current operation link
        tester.clickLink("edit_filter_10000");
        pressCancel();
        assertOnManageFiltersPage();

        tester.clickLink("edit_filter_10000");
        tester.setWorkingForm("filter-edit");
        tester.setFormElement("filterName", "");
        tester.submit("Save");
        pressCancel();
        assertOnManageFiltersPage();

        tester.clickLink("edit_filter_10000");
        tester.setWorkingForm("filter-edit");
        tester.setFormElement("filterName", "Now Its In The Session Filter");
        tester.submit("Save");
        assertOnManageFiltersPage();

        // ok move to another tab view and try the left hand side edit again
        tester.clickLink("filterlink_10000");
        tester.clickLink("filtereditshares");
        pressCancel();
        assertOnFilterPage("Now Its In The Session Filter");

        tester.clickLink("filtereditshares");
        tester.setWorkingForm("filter-edit");
        tester.setFormElement("filterName", "");
        tester.submit("Save");
        tester.setWorkingForm("filter-edit");
        tester.setFormElement("filterName", "Now has New Name");
        tester.submit("Save");
        assertOnFilterPage("Now has New Name");
    }

    private void assertOnFilterPage(final String pageName)
    {
        text.assertTextPresent(new IdLocator(tester, "filter-name"), pageName);
    }

    private void assertOnManageFiltersPage()
    {
        Locator mfLocator = new XPathLocator(tester, "//div[@id='content-top']//h1");
        assertEquals("Manage Filters", mfLocator.getText());
    }

    private void pressCancel()
    {
        assertTrue("Scripting must be enabled in the HttpUnit for cancel to work correctly", HttpUnitOptions.isScriptingEnabled());
        tester.setWorkingForm("filter-edit");
        tester.clickLink("filter-edit-cancel");
    }
}