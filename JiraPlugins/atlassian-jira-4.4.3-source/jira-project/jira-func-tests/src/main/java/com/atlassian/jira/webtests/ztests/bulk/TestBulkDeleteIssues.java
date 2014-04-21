package com.atlassian.jira.webtests.ztests.bulk;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.Groups;
import com.meterware.httpunit.WebTable;
import org.xml.sax.SAXException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@WebTest ({ Category.FUNC_TEST, Category.BULK_OPERATIONS, Category.ISSUES })
public class TestBulkDeleteIssues extends BulkChangeIssues
{
    private boolean mailServerExists = false;
    private static final String SESSION_TIMEOUT_MESSAGE = "Your session timed out while you were in the process bulk deleting issues.";

    public TestBulkDeleteIssues(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
    }

    public void testBulkDeleteIssues()
    {
        restoreData("TestBulkChangeInitialised.xml");
        mailServerExists = isMailServerExists();
        removeGlobalPermission(BULK_CHANGE, Groups.USERS);
        _testBulkDeleteIssueWithoutBulkChangePermission();
        grantGlobalPermission(BULK_CHANGE, Groups.USERS);
        _testBulkDeleteOneIssueInCurrentPage();
        _testBulkDeleteOneIssueInAllPages();
        _testBulkDeleteAllIssuesInCurrentPage();
        _testBulkDeleteAllIssuesInAllPages();
        removeGlobalPermission(BULK_CHANGE, Groups.USERS);
    }

    /**
     * Tests that the bulk operation limits work on issue navigator and through the bulk delete wizard.
     * NOTE!! If this test runs out of memory, increase the amount of heap for the web client process. 256m is good.
     */
    public void testBulkDeleteIssuesLimited() throws Exception
    {
        // test for JRA-9828 OOME on bulk delete
        restoreData("TestBulkDeleteIssuesLimited.xml");
        mailServerExists = isMailServerExists();
        displayAllIssues();
        // check bulk delete max message
        assertTextPresent("Bulk changes are currently limited to 1,000 issues."); // tooltip
        clickLinkWithText("maximum 1,000 issues");
        assertTextPresent("Bulk changes are currently limited to 1,000 issues."); // top of page
        bulkChangeChooseIssuesAll();
        final com.meterware.httpunit.WebLink webLink = getDialog().getResponse().getLinkWithID("bulkedit_chooseissues");
        final String[] tempMax = webLink.getParameterValues("tempMax");
        assertEquals(1, tempMax.length);
        assertEquals("1000", tempMax[0]); // as configured by default in jira-application.properties
        bulkChangeChooseOperationDelete(mailServerExists);

        bulkChangeConfirm();

        displayAllIssues();
        // assert there are only 1031 issues left after deleting 1000
        assertIssueNavigatorDisplaying("1", "50", "1031");
    }

    /** tests to ensure BULK CHANGE global permission is */
    public void _testBulkDeleteIssueWithoutBulkChangePermission()
    {
        log("Bulk Change - Delete Operation: No global Bulk Change permission");
        String summary = "BulkDeleteWithoutBulkChangePermission";
        addCurrentPageLink();
        addIssue(summary);
        displayAllIssues();
        assertTextNotPresent("Bulk Change");
    }

    /** tests to see if deleting one issue in the current page works. */
    public void _testBulkDeleteOneIssueInCurrentPage()
    {
        log("Bulk Change - Delete Operation: ONE issue from CURRENT page");
        String summary = "DeleteOneIssueInCurrentPages";
        addCurrentPageLink();
        String key = addIssue(summary);
        assertIndexedFieldCorrect("//item", EasyMap.build("key", key, "summary", summary), null, key);

        displayAllIssues();
        bulkChangeIncludeCurrentPage();

        bulkChangeSelectIssue(key);

        bulkChangeChooseOperationDelete(mailServerExists);
        assertLinkPresentWithText(summary);

        bulkChangeConfirm();
        checkIssueIsDeleted(summary);
        assertIssueNotIndexed(key);
    }

    private void assertIssueNotIndexed(String key)
    {
        log("Checking that item " + key + " was deleted in the index.");
        assertPageDoesNotExist("Index was not removed", "/si/jira.issueviews:issue-xml/" + key + "/" + key + ".xml?jira.issue.searchlocation=index");
    }


    /** tests to see if deleting one issue in all the pages works. */
    public void _testBulkDeleteOneIssueInAllPages()
    {
        log("Bulk Change - Delete Operation: ONE issue from ALL pages");
        String summary = "DeleteOneIssueInAllPages";
        String key = addIssue(summary);

        displayAllIssues();
        bulkChangeIncludeAllPages();

        bulkChangeSelectIssue(key);

        bulkChangeChooseOperationDelete(mailServerExists);
        assertLinkPresentWithText(summary);

        bulkChangeConfirm();
        checkIssueIsDeleted(summary);
        assertIssueNotIndexed(key);
    }

    /** tests to see if deleting all issues in the current page works. */
    public void _testBulkDeleteAllIssuesInCurrentPage()
    {
        log("Bulk Change - Delete Operation: All issue from CURRENT page");
        String prefix = "pre_";
        addIssues(prefix, NUM_RESULTS_PER_PG); //Add one page full of issues we track
        addCurrentPageLink();                  //add more issues to make multiple pages
        List issueIds = getAllIssueIdsFromBulkEditTable();
        displayAllIssues();
        bulkChangeIncludeCurrentPage();

        bulkChangeChooseIssuesAll();
        bulkChangeChooseOperationDelete(mailServerExists);
        checkIssuesAreListed(prefix, NUM_RESULTS_PER_PG);

        bulkChangeConfirm();
        checkIssuesAreNotListed(prefix, NUM_RESULTS_PER_PG);

        //let's see if all the issues were deleted in the index
        for (Iterator iterator = issueIds.iterator(); iterator.hasNext();)
        {
            String key = (String) iterator.next();
            assertIssueNotIndexed(key);
        }

        //let's see if an issue that shouldn't be deleted is still there
        assertIndexedFieldCorrect("//item", EasyMap.build("key", "HSP-40"), null, "HSP-40");
    }

    private List getAllIssueIdsFromBulkEditTable()
    {
        displayAllIssues();
        List issueIds = new ArrayList();
        try
        {
            WebTable issueTable = getDialog().getResponse().getTableWithID("issuetable");
            for (int i = 1; i < issueTable.getRowCount(); i++)
            {
                String key = issueTable.getTableCell(i, 1).asText().trim();
                issueIds.add(key);
            }
        }
        catch (SAXException e)
        {
            fail("table not found ... issuetable");
        }
        return issueIds;
    }

    /**
     * tests to see if deleting all issues in all the pages works.<br>
     * ie. deletes all issues
     */
    public void _testBulkDeleteAllIssuesInAllPages()
    {
        log("Bulk Change - Delete Operation: All issue from ALL pages");
        addCurrentPageLink();

        displayAllIssues();
        bulkChangeIncludeAllPages();

        bulkChangeChooseIssuesAll();
        bulkChangeChooseOperationDelete(mailServerExists);

        bulkChangeConfirm();

        displayAllIssues();
        assertTextPresent(LABEL_NO_ISSUE_FOUND);
    }

    public void testBulkDeleteSessionTimeouts()
    {
        log("Bulk Delete - Test that you get redirected to the session timeout page when jumping into the wizard");

        restoreBlankInstance();
        beginAt("secure/views/bulkedit/BulkDeleteDetails.jspa");
        assertTextPresent(SESSION_TIMEOUT_MESSAGE);
        beginAt("secure/BulkDeleteDetailsValidation.jspa");
        assertTextPresent(SESSION_TIMEOUT_MESSAGE);
    }
}
