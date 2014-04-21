package com.atlassian.jira.issue.managers;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueImpl;
import com.atlassian.jira.issue.changehistory.ChangeHistory;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.local.AbstractUsersTestCase;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.vcs.RepositoryException;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.sql.Timestamp;
import java.util.List;

public class TestChangeHistoryManager extends AbstractUsersTestCase
{
    private static final String ISSUE_OLD_SUMMARY = "this will be changed";
    private static final String ISSUE_NEW_SUMMARY = "this has been changed";

    private ChangeHistoryManager changeHistoryManager;
    private Issue issueObject;
    private GenericValue issue;

    public TestChangeHistoryManager(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        changeHistoryManager = (ChangeHistoryManager) ComponentManager.getComponentInstanceOfType(ChangeHistoryManager.class);

        GenericValue project = UtilsForTests.getTestEntity("Project", EasyMap.build("name", "test project"));

        issue = UtilsForTests.getTestEntity("Issue", EasyMap.build("id", new Long(1), "project", project.getLong("id"), "key", "TST-1", "summary", ISSUE_OLD_SUMMARY));
        issueObject = IssueImpl.getIssueObject(issue);
    }


    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testChangeHistory() throws GenericEntityException
    {
        //add a change history
        GenericValue cg = UtilsForTests.getTestEntity("ChangeGroup", EasyMap.build("issue", issue.getLong("id")));
        UtilsForTests.getTestEntity("ChangeItem", EasyMap.build("group", cg.getLong("id"), "field", "Summary", "oldstring", ISSUE_OLD_SUMMARY, "newstring", ISSUE_NEW_SUMMARY));

        List changeHistories = changeHistoryManager.getChangeHistoriesForUser(issueObject, null);
        assertNotNull(changeHistories);
        assertEquals(1, changeHistories.size());
        assertEquals(new Long(1), ((ChangeHistory) changeHistories.get(0)).getId());
    }

    public void testChangeHistoryNone() throws GenericEntityException
    {
        List changeHistories = changeHistoryManager.getChangeHistoriesForUser(issueObject, null);
        assertNotNull(changeHistories);
        assertTrue(changeHistories.isEmpty());
    }

    /**
     * Test that multiple change histories are returned in order
     */
    public void testMultipleChangeHistories() throws GenericEntityException, RepositoryException
    {
        List changeHistories = changeHistoryManager.getChangeHistoriesForUser(issueObject, null);
        assertNotNull(changeHistories);
        assertTrue(changeHistories.isEmpty());

        Timestamp timestamp1 = new Timestamp(System.currentTimeMillis());
        Timestamp timestamp2 = new Timestamp(timestamp1.getTime() + 1);

        //add two change histories
        GenericValue cg = UtilsForTests.getTestEntity("ChangeGroup", EasyMap.build("created", timestamp1, "issue", issue.getLong("id")));
        UtilsForTests.getTestEntity("ChangeItem", EasyMap.build("group", cg.getLong("id"), "field", "Summary", "oldstring", ISSUE_OLD_SUMMARY, "newstring", ISSUE_NEW_SUMMARY));
        cg = UtilsForTests.getTestEntity("ChangeGroup", EasyMap.build("created", timestamp2, "issue", issue.getLong("id")));
        UtilsForTests.getTestEntity("ChangeItem", EasyMap.build("group", cg.getLong("id"), "field", "Description", "oldstring", "", "newstring", "New Description"));

        changeHistories = changeHistoryManager.getChangeHistoriesForUser(issueObject, null);
        assertNotNull(changeHistories);
        assertEquals(2, changeHistories.size());
        assertEquals(new Long(1), ((ChangeHistory) changeHistories.get(0)).getId());
        assertEquals(new Long(2), ((ChangeHistory) changeHistories.get(1)).getId());
    }

    public void testGetChangeItemsForFieldNullIssue()
    {
        try
        {
            changeHistoryManager.getChangeItemsForField(null, "Link");
            fail("Should throw IAE");
        }
        catch (IllegalArgumentException e)
        {
            //expected
        }
    }

    public void testGetChangeItemsForFieldNullFieldName()
    {
        try
        {
            changeHistoryManager.getChangeItemsForField(new MockIssue(), null);
            fail("Should throw IAE");
        }
        catch (IllegalArgumentException e)
        {
            //expected
        }
    }

    public void testGetChangeItemsForFieldEmptyFieldName()
    {
        try
        {
            changeHistoryManager.getChangeItemsForField(new MockIssue(), "");
            fail("Should throw IAE");
        }
        catch (IllegalArgumentException e)
        {
            //expected
        }
    }

    public void testGetChangeItemsForField()
    {
        List changeHistories = changeHistoryManager.getChangeItemsForField(issueObject, "Link");
        assertNotNull(changeHistories);
        assertTrue(changeHistories.isEmpty());

        Timestamp timestamp1 = new Timestamp(System.currentTimeMillis());
        Timestamp timestamp2 = new Timestamp(timestamp1.getTime() + 1);

        //add two change histories
        GenericValue cg = UtilsForTests.getTestEntity("ChangeGroup", EasyMap.build("created", timestamp1, "issue", issue.getLong("id")));
        UtilsForTests.getTestEntity("ChangeItem", EasyMap.build("group", cg.getLong("id"), "field", "Link", "oldstring", ISSUE_OLD_SUMMARY, "newstring", ISSUE_NEW_SUMMARY));
        cg = UtilsForTests.getTestEntity("ChangeGroup", EasyMap.build("created", timestamp2, "issue", issue.getLong("id")));
        UtilsForTests.getTestEntity("ChangeItem", EasyMap.build("group", cg.getLong("id"), "field", "Link", "oldstring", "", "newstring", "New Description"));

        changeHistories = changeHistoryManager.getChangeItemsForField(issueObject, "Link");
        assertNotNull(changeHistories);
        assertEquals(2, changeHistories.size());
        assertEquals(ISSUE_NEW_SUMMARY, ((ChangeItemBean) changeHistories.get(0)).getToString());
        assertEquals("New Description", ((ChangeItemBean) changeHistories.get(1)).getToString());
    }

    public void testFindMovedIssue() throws GenericEntityException
    {
        moveIssue("OLD-1", "NEW-1", 11);
        Issue movedIssue = changeHistoryManager.findMovedIssue("OLD-1");
        assertEquals("NEW-1", movedIssue.getKey());
    }

    public void testFindMovedIssueMovedTwice() throws GenericEntityException
    {
        moveIssue("OLD-1", "NEW-1", "NEWEST-1");
        Issue movedIssue = changeHistoryManager.findMovedIssue("OLD-1");
        assertEquals("NEWEST-1", movedIssue.getKey());
    }

    public void testFindMovedIssueOverOneKey() throws GenericEntityException
    {
        moveIssue("OLD-1", "NEW-1", 12);
        // Later, another issue was created in the vacated JRA-1 spot, and was also moved.
        moveIssue("OLD-1", "NEWER-1", 13);
        // Now what do we do? Two issues used to be OLD-1. Answer: return the latest to occupy OLD-1
        Issue movedIssue = changeHistoryManager.findMovedIssue("OLD-1");
        assertEquals("NEWER-1", movedIssue.getKey());
    }

    private void moveIssue(String oldKey, String newKey, long id)
    {
        GenericValue issue = UtilsForTests.getTestEntity("Issue", EasyMap.build("id", new Long(id), "key", newKey));
        GenericValue cg = UtilsForTests.getTestEntity("ChangeGroup", EasyMap.build("issue", issue.getLong("id")));
        UtilsForTests.getTestEntity("ChangeItem", EasyMap.build("group", cg.getLong("id"), "field", "Key", "oldstring", oldKey, "newstring", newKey));
    }

    private void moveIssue(String oldKey, String intKey, String newKey)
    {
        GenericValue issue = UtilsForTests.getTestEntity("Issue", EasyMap.build("key", newKey, "id", new Long(14)));
        GenericValue cg = UtilsForTests.getTestEntity("ChangeGroup", EasyMap.build("id", new Long(15), "issue", issue.getLong("id")));
        UtilsForTests.getTestEntity("ChangeItem", EasyMap.build("group", cg.getLong("id"), "field", "Key", "oldstring", oldKey, "newstring", intKey));
        UtilsForTests.getTestEntity("ChangeGroup", EasyMap.build("id", new Long(16), "issue", issue.getLong("id")));
        UtilsForTests.getTestEntity("ChangeItem", EasyMap.build("group", cg.getLong("id"), "field", "Key", "oldstring", intKey, "newstring", newKey));
    }
}

