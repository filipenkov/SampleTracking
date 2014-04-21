package com.atlassian.jira.webtests.ztests.issue;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;

/**
 * Tests for issue constants (Issue type, priority, status, resolution).
 */
@WebTest ({ Category.FUNC_TEST, Category.ISSUES })
public class TestIssueConstants extends JIRAWebTest
{
    private static final String INVALID_ELEMENT_NAME = "wrong";
    private static final String SUFFIX_HTML = " </td><td><input name=&quot;" + INVALID_ELEMENT_NAME + "&quot;>";
    private static final String SUFFIX_TEXT = " </td><td><input name=\"" + INVALID_ELEMENT_NAME + "\">";
    private static final String SUFFIX_ESC = " &lt;/td&gt;&lt;td&gt;&lt;input name=&quot;" + INVALID_ELEMENT_NAME + "&quot;&gt;";
    private static final String NAME_PREFIX = "name ";
    private static final String DESC_PREFIX = "desc ";

    private static final String ISSUE_TYPE_HTML_OPTION = NAME_PREFIX + "type" + SUFFIX_TEXT;
    private static final String ISSUE_TYPE_NAME_HTML = NAME_PREFIX + "type" + SUFFIX_HTML;
    private static final String ISSUE_TYPE_DESC_HTML = DESC_PREFIX + "type" + SUFFIX_HTML;
    private static final String ISSUE_TYPE_NAME_HTML_ESC = NAME_PREFIX + "type" + SUFFIX_ESC;
    private static final String ISSUE_TYPE_DESC_HTML_ESC = DESC_PREFIX + "type" + SUFFIX_ESC;
    private static final String ISSUE_TYPE_NAME_TEXT = "New Feature";
    private static final String ISSUE_TYPE_DESC_TEXT = "A new feature of the product, which has yet to be developed.";

    private static final String SUBTASK_TYPE_NAME_HTML = NAME_PREFIX + "subtype" + SUFFIX_HTML;
    private static final String SUBTASK_TYPE_DESC_HTML = DESC_PREFIX + "subtype" + SUFFIX_HTML;
    private static final String SUBTASK_TYPE_NAME_HTML_ESC = NAME_PREFIX + "subtype" + SUFFIX_ESC;
    private static final String SUBTASK_TYPE_DESC_HTML_ESC = DESC_PREFIX + "subtype" + SUFFIX_ESC;

    private static final String PRIORITY_HTML_OPTION = NAME_PREFIX + "priority" + SUFFIX_TEXT;
    private static final String PRIORITY_NAME_HTML = NAME_PREFIX + "priority" + SUFFIX_HTML;
    private static final String PRIORITY_DESC_HTML = DESC_PREFIX + "priority" + SUFFIX_HTML;
    private static final String PRIORITY_NAME_HTML_ESC = NAME_PREFIX + "priority" + SUFFIX_ESC;
    private static final String PRIORITY_DESC_HTML_ESC = DESC_PREFIX + "priority" + SUFFIX_ESC;
    private static final String PRIORITY_NAME_TEXT = "Major";
    private static final String PRIORITY_DESC_TEXT = "Major loss of function.";

    private static final String RESOLUTION_HTML_OPTION = NAME_PREFIX + "resolution" + SUFFIX_TEXT;
    private static final String RESOLUTION_NAME_HTML = NAME_PREFIX + "resolution" + SUFFIX_HTML;
    private static final String RESOLUTION_DESC_HTML = DESC_PREFIX + "resolution" + SUFFIX_HTML;
    private static final String RESOLUTION_NAME_HTML_ESC = NAME_PREFIX + "resolution" + SUFFIX_ESC;
    private static final String RESOLUTION_DESC_HTML_ESC = DESC_PREFIX + "resolution" + SUFFIX_ESC;
    private static final String RESOLUTION_NAME_TEXT = "Duplicate";
    private static final String RESOLUTION_DESC_TEXT = "The problem is a duplicate of an existing issue.";

    private static final String STATUS_HTML_OPTION = NAME_PREFIX + "status" + SUFFIX_TEXT;
    private static final String STATUS_NAME_HTML = NAME_PREFIX + "status" + SUFFIX_HTML;
    private static final String STATUS_DESC_HTML = DESC_PREFIX + "status" + SUFFIX_HTML;
    private static final String STATUS_NAME_HTML_ESC = NAME_PREFIX + "status" + SUFFIX_ESC;
    private static final String STATUS_DESC_HTML_ESC = DESC_PREFIX + "status" + SUFFIX_ESC;
    private static final String STATUS_NAME_TEXT = "In Progress";
    private static final String STATUS_DESC_TEXT = "This issue is being actively worked on at the moment by the assignee.";

    private static final String RESOLVED_STATUS_HTML_OPTION = NAME_PREFIX + "resolved" + SUFFIX_TEXT;
    private static final String RESOLVED_STATUS_NAME_HTML = NAME_PREFIX + "resolved" + SUFFIX_HTML;
    private static final String RESOLVED_STATUS_DESC_HTML = DESC_PREFIX + "resolved" + SUFFIX_HTML;
    private static final String RESOLVED_STATUS_NAME_HTML_ESC = NAME_PREFIX + "resolved" + SUFFIX_ESC;
    private static final String RESOLVED_STATUS_DESC_HTML_ESC = DESC_PREFIX + "resolved" + SUFFIX_ESC;

    public TestIssueConstants(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreData("TestIssueConstants.xml");
    }

    public void testIssueConstantsAreEncodedOnBrowseProjectPage()
    {
        //check the right hand side of the browse project page (Note: not description in tooltip)
        gotoProjectBrowse(PROJECT_HOMOSAP_KEY);
        clickLink("issues-panel-panel");
        assertFormElementNotPresent(INVALID_ELEMENT_NAME);
        assertTextPresent("title=\"" + PRIORITY_NAME_HTML_ESC + "");
        assertTextPresent("title=\"" + PRIORITY_NAME_TEXT + "");
        assertTextPresent("title=\"" + STATUS_NAME_HTML_ESC + "");
        assertTextPresent("title=\"" + STATUS_NAME_TEXT + "");

        //road map project tab
        //gotoProjectTabPanel(PROJECT_HOMOSAP_KEY, PROJECT_TAB_ROAD_MAP);
        gotoPage("/browse/HSP?selectedTab=com.atlassian.jira.plugin.system.project:roadmap-panel&expandVersion=10002");
        assertFormElementNotPresent(INVALID_ELEMENT_NAME);
        assertIssueConstantHTMLTitlesPresent();

        //change log project tab
        gotoPage("/browse/HSP?selectedTab=com.atlassian.jira.plugin.system.project:changelog-panel&expandVersion=10001");
        assertFormElementNotPresent(INVALID_ELEMENT_NAME);
        assertIssueConstantHTMLTitlesPresent();

        //popular issues project tab
        gotoPage("/browse/HSP?selectedTab=com.atlassian.jira.plugin.system.project:popularissues-panel");
        assertFormElementNotPresent(INVALID_ELEMENT_NAME);
        assertIssueConstantHTMLTitlesPresent();
    }

    public void testIssueConstantsAreEncodedOnReports()
    {
        //version workload report
        gotoPage("/secure/ConfigureReport!default.jspa?selectedProjectId=" + 10000 + "&reportKey=com.atlassian.jira.plugin.system.reports:version-workload");
        selectOption("versionId", "- version 3");
        submit("Next");
        assertFormElementNotPresent(INVALID_ELEMENT_NAME);
        assertTextPresent(ISSUE_TYPE_NAME_HTML_ESC);
        assertTextPresent(ISSUE_TYPE_NAME_TEXT);
        assertTextPresent(PRIORITY_NAME_HTML_ESC);
        assertTextPresent(PRIORITY_NAME_TEXT);

        //single level group by report
        gotoPage("/secure/ConfigureReport!default.jspa?selectedProjectId=" + 10000 + "&reportKey=com.atlassian.jira.plugin.system.reports:singlelevelgroupby");
        selectOption("mapper", "Issue Type");
        setFormElement("filterid", "10002");
        submit("Next");
        assertFormElementNotPresent(INVALID_ELEMENT_NAME);
        assertIssueConstantHTMLTitlesPresent();
    }

    public void testIssueConstantsAreEncodedOnIssueNavigator()
    {
        //check the "View and Hide" summary of navigator with text constants
        clickLink("find_link");
        assertFormElementNotPresent(INVALID_ELEMENT_NAME);
        selectOption("type", ISSUE_TYPE_NAME_TEXT);
        selectOption("priority", PRIORITY_NAME_TEXT);
        selectOption("status", STATUS_NAME_TEXT);
        selectOption("resolution", RESOLUTION_NAME_TEXT);
        tester.submit("show");
        tester.clickLink("viewfilter");

        assertFormElementNotPresent(INVALID_ELEMENT_NAME);
        assertTextPresent(ISSUE_TYPE_NAME_TEXT);
        assertTextPresent(PRIORITY_NAME_TEXT);
        assertTextPresent(STATUS_NAME_TEXT);
        assertTextPresent(RESOLUTION_NAME_TEXT);

        //check the results on the "View" of navigator
        displayAllIssues();
        assertIssueConstantHTMLTitlesPresent();

        //check the "View and Hide" summary of navigator with html constants
        //NOTE: the above "View" should reset the navigator
        clickLink("find_link");
        assertFormElementNotPresent(INVALID_ELEMENT_NAME);
        selectOption("type", ISSUE_TYPE_HTML_OPTION);
        selectOption("priority", PRIORITY_HTML_OPTION);
        selectOption("status", STATUS_HTML_OPTION);
        selectOption("resolution", RESOLUTION_HTML_OPTION);
        tester.submit("show");
        tester.clickLink("viewfilter");

        assertFormElementNotPresent(INVALID_ELEMENT_NAME);
        assertTextPresent(ISSUE_TYPE_NAME_HTML_ESC);
        assertTextPresent(PRIORITY_NAME_HTML_ESC);
        assertTextPresent(STATUS_NAME_HTML_ESC);
        assertTextPresent(RESOLUTION_NAME_HTML_ESC);

        //check the navigator's 'printable' view
        displayAllIssues();
        clickLink("printable");
        assertIssueConstantHTMLTitlesPresent();
        gotoPage("/secure/Dashboard.jspa");

        //check the navigator's 'full content' view
        displayAllIssues();
        clickLink("fullContent");
        assertIssueConstantHTMLContentViewPresent();
        assertIssueConstantTextContentViewPresent();
        gotoPage("/secure/Dashboard.jspa");

        //check the navigator's 'XML' view
        displayAllIssues();
        clickLink("xml");
        assertIssueConstantHTMLPresentInXML();
        assertTextSequence(new String[]{"<status id=\"1\"", ">", STATUS_NAME_HTML_ESC, "</status>"});
        assertTextSequence(new String[]{"<type id=\"2\"", ">", ISSUE_TYPE_NAME_TEXT, "</type>"});
        assertTextSequence(new String[]{"<priority id=\"3\"", ">", PRIORITY_NAME_TEXT, "</priority>"});
        assertTextSequence(new String[]{"<status id=\"3\"", ">", STATUS_NAME_TEXT, "</status>"});
        gotoPage("/secure/Dashboard.jspa");

        //check the navigator's 'Word' view
        displayAllIssues();
        clickLink("word");
        assertIssueConstantHTMLContentViewPresent();
        assertIssueConstantTextContentViewPresent();
        gotoPage("/secure/Dashboard.jspa");

        //check the navigator's 'Current Fields' view
        displayAllIssues();
        clickLink("currentExcelFields");
        assertTextPresent(ISSUE_TYPE_NAME_TEXT);
        assertTextPresent(PRIORITY_NAME_TEXT);
        assertTextPresent(STATUS_NAME_TEXT);
        assertTextPresent(ISSUE_TYPE_NAME_HTML_ESC);
        assertTextPresent(PRIORITY_NAME_HTML_ESC);
        assertTextPresent(STATUS_NAME_HTML_ESC);
        gotoPage("/secure/Dashboard.jspa");
    }

    public void testIssueConstantsAreEncodedOnBulkOperation()
    {
        //assert on bulk edit all issues confirmation page
        displayAllIssues();
        clickLink("bulkedit_all");
        selectIssuesForBulkOperation();
        checkCheckbox("operation", "bulk.edit.operation.name");
        submit("Next");
        assertFormElementNotPresent(INVALID_ELEMENT_NAME);
        checkCheckbox("actions", "issuetype");
        selectOption("issuetype", ISSUE_TYPE_HTML_OPTION);
        checkCheckbox("actions", "priority");
        selectOption("priority", PRIORITY_HTML_OPTION);
        submit("Next");
        assertFormElementNotPresent(INVALID_ELEMENT_NAME);
        assertIssueConstantHTMLTitlesPresent();

        //assert on bulk move issue
        displayAllIssues();
        clickLink("bulkedit_all");
        selectIssuesForBulkOperation();
        checkCheckbox("operation", "bulk.move.operation.name");
        submit("Next");
        assertFormElementNotPresent(INVALID_ELEMENT_NAME);
        assertIssueConstantStraightHTMLNotPresent();

        //assert on bulk transition issue
        displayAllIssues();
        clickLink("bulkedit_all");
        selectIssuesForBulkOperation();
        checkCheckbox("operation", "bulk.workflowtransition.operation.name");
        submit("Next");
        checkCheckbox("wftransition", "jira_4_3");
        submit("Next");
        assertFormElementNotPresent(INVALID_ELEMENT_NAME);
        assertTextNotPresent(STATUS_NAME_HTML);
        assertTextPresent(STATUS_NAME_HTML_ESC);

        //assert on bulk delete issue
        displayAllIssues();
        clickLink("bulkedit_all");
        selectIssuesForBulkOperation();
        checkCheckbox("operation", "bulk.delete.operation.name");
        submit("Next");
        assertFormElementNotPresent(INVALID_ELEMENT_NAME);
        assertIssueConstantStraightHTMLNotPresent();
    }

    public void testIssueConstantsAreEncodedOnViewIssue()
    {
        //check the standard view issue page
        gotoIssue("HSP-3");
        assertFormElementNotPresent(INVALID_ELEMENT_NAME);
        assertIssueConstantStraightHTMLNotPresent();
        assertTextPresent("title=\"" + ISSUE_TYPE_NAME_HTML_ESC + " - " + ISSUE_TYPE_DESC_HTML_ESC + "\"");
        assertTextPresent("title=\"" + PRIORITY_NAME_HTML_ESC + " - " + PRIORITY_DESC_HTML_ESC + "\"");
        assertTextPresent("title=\"" + RESOLVED_STATUS_NAME_HTML_ESC + " - " + RESOLVED_STATUS_DESC_HTML_ESC + "\"");
        assertTextPresent(RESOLUTION_NAME_HTML_ESC);
        //check the quick subtask create form for professional/enterprise
        assertTextPresent(SUBTASK_TYPE_NAME_HTML_ESC);

        //check the subtask's view issue page (for standard, this is just a plain issue still with the subtask type)
        gotoIssue("HSP-4");
        assertFormElementNotPresent(INVALID_ELEMENT_NAME);
        assertTextNotPresent(SUBTASK_TYPE_NAME_HTML);
        assertTextNotPresent(PRIORITY_NAME_HTML);
        assertTextNotPresent(RESOLVED_STATUS_NAME_HTML);
        assertTextNotPresent(SUBTASK_TYPE_DESC_HTML);
        assertTextNotPresent(PRIORITY_DESC_HTML);
        assertTextNotPresent(RESOLVED_STATUS_DESC_HTML);
        assertTextPresent("title=\"" + SUBTASK_TYPE_NAME_HTML_ESC + " - " + SUBTASK_TYPE_DESC_HTML_ESC + "\"");
        assertTextPresent("title=\"" + PRIORITY_NAME_HTML_ESC + " - " + PRIORITY_DESC_HTML_ESC + "\"");
        assertTextPresent("title=\"" + STATUS_NAME_HTML_ESC + " - " + STATUS_DESC_HTML_ESC + "\"");

        //check view issue's printable view
        gotoIssue("HSP-3");
        clickLinkWithText("Printable");
        assertIssueConstantHTMLContentViewPresent();
        assertTextPresentBeforeText("Resolution:", RESOLUTION_NAME_HTML_ESC);
        assertFormNotPresent();
        gotoPage("/secure/Dashboard.jspa");

        //check view issues's XML view
        gotoIssue("HSP-3");
        clickLinkWithText("XML");
        assertIssueConstantHTMLPresentInXML();
        assertTextSequence(new String[]{"<status id=\"5\"", ">", RESOLVED_STATUS_NAME_HTML_ESC, "</status>"});
        gotoPage("/secure/Dashboard.jspa");

        //check view issues's Word view (just like looking at printable view)
        gotoIssue("HSP-3");
        clickLinkWithText("Word");
        assertIssueConstantHTMLContentViewPresent();
        assertTextPresentBeforeText("Resolution:", RESOLUTION_NAME_HTML_ESC);
        gotoPage("/secure/Dashboard.jspa");
    }

    public void testIssueConstantsAreEncodedOnAdminPage()
    {
        //manage issue types page
        gotoAdmin();
        clickLink("issue_types");
        assertFormElementNotPresent(INVALID_ELEMENT_NAME);
        assertTextPresentBeforeText(ISSUE_TYPE_NAME_HTML_ESC, ISSUE_TYPE_DESC_HTML_ESC);
        assertTextPresentBeforeText(SUBTASK_TYPE_NAME_HTML_ESC, SUBTASK_TYPE_DESC_HTML_ESC);
        //check the translation page for the issue types
        clickLink("translate_link");
        assertFormElementNotPresent(INVALID_ELEMENT_NAME);
        assertTextPresentBeforeText(ISSUE_TYPE_NAME_HTML_ESC, ISSUE_TYPE_DESC_HTML_ESC);

        //view priorities
        gotoAdmin();
        clickLink("priorities");
        assertFormElementNotPresent(INVALID_ELEMENT_NAME);
        assertTextPresentBeforeText(PRIORITY_NAME_HTML_ESC, PRIORITY_DESC_HTML_ESC);
        gotoPage("/secure/admin/ViewTranslations!default.jspa?issueConstantType=priority");
        assertFormElementNotPresent(INVALID_ELEMENT_NAME);
        assertTextPresentBeforeText(PRIORITY_NAME_HTML_ESC, PRIORITY_DESC_HTML_ESC);

        //view resolutions
        gotoAdmin();
        clickLink("resolutions");
        assertFormElementNotPresent(INVALID_ELEMENT_NAME);
        assertTextPresentBeforeText(RESOLUTION_NAME_HTML_ESC, RESOLUTION_DESC_HTML_ESC);
        gotoPage("/secure/admin/ViewTranslations!default.jspa?issueConstantType=resolution");
        assertFormElementNotPresent(INVALID_ELEMENT_NAME);
        assertTextPresentBeforeText(RESOLUTION_NAME_HTML_ESC, RESOLUTION_DESC_HTML_ESC);

        //view statuses
        gotoAdmin();
        clickLink("statuses");
        assertFormElementNotPresent(INVALID_ELEMENT_NAME);
        assertTextPresentBeforeText(STATUS_NAME_HTML_ESC, STATUS_DESC_HTML_ESC);
        assertTextPresentBeforeText(RESOLVED_STATUS_NAME_HTML_ESC, RESOLVED_STATUS_DESC_HTML_ESC);
        gotoPage("/secure/admin/ViewTranslations!default.jspa?issueConstantType=status");
        assertFormElementNotPresent(INVALID_ELEMENT_NAME);
        assertTextPresentBeforeText(STATUS_NAME_HTML_ESC, STATUS_DESC_HTML_ESC);
        assertTextPresentBeforeText(RESOLVED_STATUS_NAME_HTML_ESC, RESOLVED_STATUS_DESC_HTML_ESC);
    }

    public void testIssueConstantsAreEncodedOnEditIssue()
    {
        gotoIssue("HSP-3");
        clickLink("edit-issue");
        assertFormElementNotPresent(INVALID_ELEMENT_NAME);
        //for now just assert more than one issue type is available
        assertTextPresent(ISSUE_TYPE_NAME_HTML_ESC);
        assertTextPresent(ISSUE_TYPE_NAME_TEXT);

        //delete all but 1 issue type and check the edit page displays only one issue type (issuetype-edit-not-allowed.vm)
        gotoAdmin();
        clickLink("issue_types");
        gotoPage("/secure/admin/DeleteIssueType!default.jspa?id=4"); // Improvements
        submit("Delete");
        gotoPage("/secure/admin/DeleteIssueType!default.jspa?id=3"); // Task
        submit("Delete");
        gotoPage("/secure/admin/DeleteIssueType!default.jspa?id=2"); // New Feature
        submit("Delete");
        assertFormElementNotPresent(INVALID_ELEMENT_NAME);
        assertTextPresentBeforeText(ISSUE_TYPE_NAME_HTML_ESC, ISSUE_TYPE_DESC_HTML_ESC);
        //now go back to the edit issue page, and check that issuetype cannot be changed (issuetype-edit-not-allowed.vm)
        gotoIssue("HSP-3");
        clickLink("edit-issue");
        assertFormElementNotPresent(INVALID_ELEMENT_NAME);
        assertTextPresent(ISSUE_TYPE_NAME_HTML_ESC);
        assertTextNotPresent(ISSUE_TYPE_NAME_TEXT);
        assertTextPresent("There are no issue types with compatible field configuration and/or workflow associations.");
    }

    //--------------------------------------------------------------------------------------------------- helper methods
    private void assertIssueConstantHTMLTitlesPresent()
    {
        assertIssueConstantStraightHTMLNotPresent();
        assertTextPresent("title=\"" + ISSUE_TYPE_NAME_HTML_ESC + " - " + ISSUE_TYPE_DESC_HTML_ESC );
//        assertTextPresent("title=\"" + PRIORITY_NAME_HTML_ESC + " - " + PRIORITY_DESC_HTML_ESC);
        assertTextPresent("title=\"" + STATUS_NAME_HTML_ESC + " - " + STATUS_DESC_HTML_ESC);

        assertTextPresent("title=\"" + ISSUE_TYPE_NAME_TEXT + " - " + ISSUE_TYPE_DESC_TEXT);
//        assertTextPresent("title=\"" + PRIORITY_NAME_TEXT + " - " + PRIORITY_DESC_TEXT);
        assertTextPresent("title=\"" + STATUS_NAME_TEXT + " - " + STATUS_DESC_TEXT);
    }

    private void assertIssueConstantHTMLPresentInXML()
    {
        assertIssueConstantStraightHTMLNotPresent();
        assertTextSequence(new String[] {"<type id=\"1\"", ">", ISSUE_TYPE_NAME_HTML_ESC, "</type>"});
        assertTextSequence(new String[] {"<priority id=\"1\"", ">", PRIORITY_NAME_HTML_ESC, "</priority>"});
        assertTextSequence(new String[] {"<resolution id=\"1\"", ">", RESOLUTION_NAME_HTML_ESC, "</resolution>"});
    }

    private void assertIssueConstantHTMLContentViewPresent()
    {
        assertIssueConstantStraightHTMLNotPresent();
        assertTextPresentBeforeText("Type:", ISSUE_TYPE_NAME_HTML_ESC);
        assertTextPresentBeforeText("Priority:", PRIORITY_NAME_HTML_ESC);
        assertTextPresentBeforeText("Status:", STATUS_NAME_HTML_ESC);
    }

    private void assertIssueConstantTextContentViewPresent()
    {
        assertIssueConstantStraightHTMLNotPresent();
        assertTextPresentBeforeText("Type:", ISSUE_TYPE_NAME_TEXT);
        assertTextPresentBeforeText("Priority:", PRIORITY_NAME_TEXT);
        assertTextPresentBeforeText("Status:", STATUS_NAME_TEXT);
    }

    private void assertIssueConstantStraightHTMLNotPresent()
    {
        assertTextNotPresent(ISSUE_TYPE_NAME_HTML);
        assertTextNotPresent(PRIORITY_NAME_HTML);
        assertTextNotPresent(STATUS_NAME_HTML);

        assertTextNotPresent(ISSUE_TYPE_DESC_HTML);
        assertTextNotPresent(PRIORITY_DESC_HTML);
        assertTextNotPresent(STATUS_DESC_HTML);
    }

    private void selectIssuesForBulkOperation()
    {
        checkCheckbox("bulkedit_10010", "on"); //HSP-3
        checkCheckbox("bulkedit_10001", "on"); //HSP-2
        checkCheckbox("bulkedit_10000", "on"); //HSP-1
        clickOnNext();
    }
}
