package com.atlassian.jira.webtest.selenium.issue;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import com.atlassian.selenium.Browser;
import com.atlassian.selenium.SkipInBrowser;

@SkipInBrowser(browsers={Browser.IE}) //Selenium Command Exception - Responsibility: Hamish
@WebTest({ Category.SELENIUM_TEST, Category.ISSUES })
public class TestViewIssue extends JiraSeleniumTest
{
    public void testCommentBoxExpand()
    {
        restoreData("TestOpsBar.xml");
        getNavigator().gotoIssue("HSP-1");
        client.runScript("window.resize(800,800)");
        client.click("Id=comment-issue");
        assertThat.elementPresentByTimeout("jquery=#comment", DROP_DOWN_WAIT);
        int commentHeight = client.getElementHeight("jquery=#comment").intValue();
        client.typeWithFullKeyEvents("jquery=#comment", "This is some test, just enough to make sure that the the height changes when I reach 4 rows. This is some test, just enough to make sure that the the height changes when I reach 4 rows. This is some test, just enough to make sure that the the height changes when I reach 4 rows. This is some test, just enough to make sure that the the height changes when I reach 4 rows.This is some test, just enough to make sure that the the height changes when I reach 4 rows.");
        assertTrue("Expected comment box height to expand on input", client.getElementHeight("jquery=#comment").intValue() != commentHeight);
        client.click("issue-comment-add-submit", true);
    }

    /**
     * Test that a number of fields are shortened properly and that the preference whether a particular field should
     * show up shortened or expanded is saved in a cookie!
     */
    public void testFieldsShortened()
    {
        restoreData("TestFieldsShortened.xml");
        getNavigator().gotoIssue("HSP-1");

        //first check all the right elements are visible!
        assertFieldShortened("versions");
        assertFieldShortened("fixVersions");
        assertFieldShortened("components");
        assertFieldShortened("customfield_10001");
        assertFieldShortened("customfield_10002");

        //then expand them all!
        client.click("jquery=#versions-field .ellipsis");
        assertFieldExpanded("versions");
        client.click("jquery=#fixVersions-field .ellipsis");
        assertFieldExpanded("fixVersions");
        client.click("jquery=#components-field  .ellipsis");
        assertFieldExpanded("components");
        client.click("jquery=#customfield_10001-field .ellipsis");
        assertFieldExpanded("customfield_10001");
        client.click("jquery=#customfield_10002-field .ellipsis");
        assertFieldExpanded("customfield_10002");

        //reload the Issue. Everything should open up expanded now!
        getNavigator().gotoIssue("HSP-1");
        assertFieldExpanded("versions");
        assertFieldExpanded("fixVersions");
        assertFieldExpanded("components");
        assertFieldExpanded("customfield_10001");
        assertFieldExpanded("customfield_10002");

        //try to collapse a couple
        client.click("jquery=#fixVersions-field .icon-hide");
        assertFieldShortened("fixVersions");
        client.click("jquery=#components-field .icon-hide");
        assertFieldShortened("components");

        getNavigator().gotoIssue("HSP-1");
        assertFieldExpanded("versions");    
        assertFieldShortened("fixVersions");
        assertFieldShortened("components");
        assertFieldExpanded("customfield_10001");
        assertFieldExpanded("customfield_10002");
    }

    private void assertFieldExpanded(final String fieldId)
    {
        assertThat.elementPresentByTimeout("jquery=#" + fieldId + "-field .icon-hide", 5000);
        assertThat.elementNotPresentByTimeout("jquery=#" + fieldId + "-field .ellipsis", 5000);
    }

    private void assertFieldShortened(final String fieldId)
    {
        assertThat.elementPresentByTimeout("jquery=#" + fieldId + "-field .ellipsis", 5000);
        assertThat.elementNotPresentByTimeout("jquery=#" + fieldId + "-field .icon-hide", 5000);
    }

    public void testCollapsingSections()
    {
        restoreData("TestViewIssueCollapsing.xml");

        getNavigator().gotoIssue("HSP-1");

        assertSectionNotCollapsed("details-module");
        assertSectionNotCollapsed("attachmentmodule");
        assertSectionNotCollapsed("linkingmodule");
        assertSectionNotCollapsed("view-subtasks");
        assertSectionNotCollapsed("activitymodule");
        assertSectionNotCollapsed("peoplemodule");
        assertSectionNotCollapsed("datesmodule");
        assertSectionNotCollapsed("timetrackingmodule");

        collapseSection("attachmentmodule");

        getNavigator().gotoIssue("HSP-1");

        assertSectionNotCollapsed("details-module");
        assertSectionCollapsed("attachmentmodule");
        assertSectionNotCollapsed("linkingmodule");
        assertSectionNotCollapsed("view-subtasks");
        assertSectionNotCollapsed("activitymodule");
        assertSectionNotCollapsed("peoplemodule");
        assertSectionNotCollapsed("datesmodule");
        assertSectionNotCollapsed("timetrackingmodule");

        expandSection("attachmentmodule");

        getNavigator().gotoIssue("HSP-1");

        assertSectionNotCollapsed("details-module");
        assertSectionNotCollapsed("attachmentmodule");
        assertSectionNotCollapsed("linkingmodule");
        assertSectionNotCollapsed("view-subtasks");
        assertSectionNotCollapsed("activitymodule");
        assertSectionNotCollapsed("peoplemodule");
        assertSectionNotCollapsed("datesmodule");
        assertSectionNotCollapsed("timetrackingmodule");

        getNavigator().gotoIssue("HSP-1");

        collapseSection("details-module");
        collapseSection("attachmentmodule");
        collapseSection("linkingmodule");
        collapseSection("view-subtasks");
        collapseSection("activitymodule");
        collapseSection("peoplemodule");
        collapseSection("datesmodule");
        collapseSection("timetrackingmodule");

        getNavigator().gotoIssue("HSP-1");

        assertSectionCollapsed("details-module");
        assertSectionCollapsed("attachmentmodule");
        assertSectionCollapsed("linkingmodule");
        assertSectionCollapsed("view-subtasks");
        assertSectionCollapsed("activitymodule");
        assertSectionCollapsed("peoplemodule");
        assertSectionCollapsed("datesmodule");
        assertSectionCollapsed("timetrackingmodule");

        expandSection("details-module");
        expandSection("attachmentmodule");
        expandSection("linkingmodule");
        expandSection("view-subtasks");
        expandSection("activitymodule");
        expandSection("peoplemodule");
        expandSection("datesmodule");
        expandSection("timetrackingmodule");

        getNavigator().gotoIssue("HSP-1");

        assertSectionNotCollapsed("details-module");
        assertSectionNotCollapsed("attachmentmodule");
        assertSectionNotCollapsed("linkingmodule");
        assertSectionNotCollapsed("view-subtasks");
        assertSectionNotCollapsed("activitymodule");
        assertSectionNotCollapsed("peoplemodule");
        assertSectionNotCollapsed("datesmodule");
        assertSectionNotCollapsed("timetrackingmodule");

        collapseSection("view-subtasks");       
        client.click("id=stqc_show");
        assertThat.visibleByTimeout("id=stqcform", 2000);
        assertSectionNotCollapsed("view-subtasks");
                
        client.click("id=manage-attachment-link", true);

        getNavigator().gotoIssue("HSP-1");
        assertSectionNotCollapsed("attachmentmodule");        
        
    }

    public void testFieldTabVisibility()
    {
        restoreData("TestViewIssueTabs.xml");
        getNavigator().gotoIssue("MKY-3");
        assertThat.elementVisible("id=tabCellPane1");
        assertThat.elementNotVisible("id=tabCellPane2");

        assertThat.elementPresent("jquery=#tabCell1.active");
        assertThat.elementNotPresent("jquery=#tabCell2.active");

        // test second tab
        client.click("jquery=#tabCell2 a");

        assertThat.elementNotVisible("id=tabCellPane1");
        assertThat.elementVisible("id=tabCellPane2");

        assertThat.elementNotPresent("jquery=#tabCell1.active");
        assertThat.elementPresent("jquery=#tabCell2.active");

        // test first tab
        client.click("jquery=#tabCell1 a");

        assertThat.elementVisible("id=tabCellPane1");
        assertThat.elementNotVisible("id=tabCellPane2");

        assertThat.elementPresent("jquery=#tabCell1.active");
        assertThat.elementNotPresent("jquery=#tabCell2.active");

        // test second tab again
        client.click("jquery=#tabCell2 a");

        assertThat.elementNotVisible("id=tabCellPane1");
        assertThat.elementVisible("id=tabCellPane2");

        assertThat.elementNotPresent("jquery=#tabCell1.active");
        assertThat.elementPresent("jquery=#tabCell2.active");

        // this should not be sticky
        getNavigator().gotoIssue("MKY-3");
        assertThat.elementVisible("id=tabCellPane1");
        assertThat.elementNotVisible("id=tabCellPane2");
        
    }

    private void collapseSection(String section)
    {
        client.click("jquery=#" + section + " .mod-header h3");
        assertThat.elementPresentByTimeout("jquery=#" + section + ".collapsed");
        assertThat.notVisibleByTimeout("jquery=#" + section + " .mod-content");
    }

    private void expandSection(String section)
    {
        client.click("jquery=#" + section + " .mod-header h3");
        assertThat.elementNotPresentByTimeout("jquery=#" + section + ".collapsed");
        assertThat.elementPresentByTimeout("jquery=#" + section + " .mod-content:visible");
    }

    private void assertSectionNotCollapsed(final String sectionId)
    {
        assertThat.elementNotPresent("jquery=#" + sectionId + ".collapsed");
        assertThat.elementPresent("jquery=#" + sectionId + " .mod-content:visible");

    }
    private void assertSectionCollapsed(final String sectionId)
    {
        assertThat.elementPresent("jquery=#" + sectionId + ".collapsed");
        assertThat.elementNotVisible("jquery=#" + sectionId + " .mod-content");
    }
}