package com.atlassian.jira.issue.worklog;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.ofbiz.util.EntityUtils;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.core.util.map.EasyMap;
import com.mockobjects.dynamic.Mock;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestOfBizWorklogStore extends LegacyJiraMockTestCase
{
    private OfBizDelegator ofBizDelegator;
    private static final Long ID = new Long(1000);
    private static final Long TIME_SPENT = new Long(60000);
    private static final long CREATED_MS = 1000;
    private static final long STARTDATE_MS = 5000;
    private static final long UPDATED_MS = 10000;
    private static final String AUTHOR = "testauthor";
    private static final String COMMENT = "testbody";
    private static final String GROUP_LEVEL = "testgrouplevel";
    private static final String UPDATE_AUTHOR = "testupdateauthor";

    protected void setUp() throws Exception
    {
        super.setUp();
        ofBizDelegator = (OfBizDelegator) ComponentManager.getComponentInstanceOfType(OfBizDelegator.class);
    }

    public void testGetById()
    {
        final Long expectedIssueId = new Long(10001);
        final Long expectedWorklogId = new Long(1000);
        final Long expectedTimeSpent = new Long(100);
        UtilsForTests.getTestEntity("Worklog", EasyMap.build("id", expectedWorklogId, "issue", expectedIssueId, "timeworked", expectedTimeSpent));

        OfBizWorklogStore ofBizWorklogStore = new OfBizWorklogStore(ofBizDelegator, null)
        {
            Issue getIssueForId(Long issueId)
            {
                assertEquals(expectedIssueId, issueId);
                return null;
            }
        };

        final Worklog worklog = ofBizWorklogStore.getById(expectedWorklogId);
        assertEquals(expectedWorklogId, worklog.getId());
        assertEquals(expectedTimeSpent, worklog.getTimeSpent());
    }

    public void testGetByIdNotFound()
    {
        OfBizWorklogStore ofBizWorklogStore = new OfBizWorklogStore(ofBizDelegator, null);

        final Worklog worklog = ofBizWorklogStore.getById(null);
        assertNull(worklog);

        final Worklog worklog2 = ofBizWorklogStore.getById(new Long(10000));
        assertNull(worklog2);
    }

    public void testGetByIssue()
    {
        long now = System.currentTimeMillis();
        final Long id1 = new Long(100);
        final Long id2 = new Long(200);
        final Long id3 = new Long(300);

        UtilsForTests.getTestEntity("Worklog", EasyMap.build("id", id1, "issue", new Long(150), "timeworked", new Long(1000), "created", new Timestamp(now)));
        UtilsForTests.getTestEntity("Worklog", EasyMap.build("id", id2, "issue", new Long(150), "timeworked", new Long(2000), "created", new Timestamp(now + 500)));
        UtilsForTests.getTestEntity("Worklog", EasyMap.build("id", id3, "issue", new Long(150), "timeworked", new Long(0), "created", new Timestamp(now + 550)));
        UtilsForTests.getTestEntity("Worklog", EasyMap.build("id", new Long(400), "issue", new Long(250), "timeworked", new Long(3000), "created", new Timestamp(now)));

        OfBizWorklogStore ofBizWorklogstore = new OfBizWorklogStore(ofBizDelegator, null);

        Mock mockIssue = new Mock(Issue.class);
        mockIssue.expectAndReturn("getId", new Long(150));

        List worklogs = ofBizWorklogstore.getByIssue((Issue) mockIssue.proxy());

        assertEquals(3, worklogs.size());
        //test that worklogs are ordered by creation date
        assertEquals(id1, ((Worklog) worklogs.get(0)).getId());
        assertEquals(id2, ((Worklog) worklogs.get(1)).getId());
        assertEquals(id3, ((Worklog) worklogs.get(2)).getId());
    }

    public void testGetByIssueNullIssue()
    {
        OfBizWorklogStore ofBizWorklogstore = new OfBizWorklogStore(ofBizDelegator, null);
        try
        {
            ofBizWorklogstore.getByIssue(null);
            fail("supplying null issue should throw IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {
            //expected
        }
    }

    public void testConvertToWorklog()
    {
        Map attributes = getTestParamMap();

        GenericValue worklogGV = UtilsForTests.getTestEntity("Worklog", attributes);

        OfBizWorklogStore ofBizWorklogStore = new OfBizWorklogStore(null, null);

        Mock issueMock = new Mock(Issue.class);
        Issue issue = (Issue) issueMock.proxy();

        Worklog worklog = ofBizWorklogStore.convertToWorklog(issue, worklogGV);

        assertEquals(issue, worklog.getIssue());
        assertEquals(attributes.get("id"), worklog.getId());
        assertEquals(attributes.get("timeworked"), worklog.getTimeSpent());
        assertEquals(((Timestamp) attributes.get("created")).getTime(), worklog.getCreated().getTime());
        assertEquals(((Timestamp) attributes.get("startdate")).getTime(), worklog.getStartDate().getTime());
        assertEquals(((Timestamp) attributes.get("updated")).getTime(), worklog.getUpdated().getTime());
        assertEquals(attributes.get("author"), worklog.getAuthor());
        assertEquals(attributes.get("updateauthor"), worklog.getUpdateAuthor());
        assertEquals(attributes.get("body"), worklog.getComment());
        assertEquals(attributes.get("grouplevel"), worklog.getGroupLevel());
        assertNull(worklog.getRoleLevelId());
    }

    public void testCreateParamMap()
    {
        OfBizWorklogStore ofBizWorklogStore = new OfBizWorklogStore(ofBizDelegator, null);

        Mock mockIssue = new Mock(Issue.class);
        final Long issueId = new Long(1);
        mockIssue.expectAndReturn("getId", issueId);
        Issue issue = (Issue) mockIssue.proxy();

        long now = System.currentTimeMillis();
        Date created = new Date(now - 1000);
        Date performed = new Date(now);
        Date updated = new Date(now + 1000);

        Worklog worklog = new WorklogImpl(null, issue, null, "tim", "a comment", performed,
                "group level", new Long(12783), new Long(1000), "dylan", created, updated);

        final Map params = ofBizWorklogStore.createParamMap(worklog);

        assertEquals(issueId, params.get("issue"));
        assertEquals("tim", params.get("author"));
        assertEquals("a comment", params.get("body"));
        assertEquals("dylan", params.get("updateauthor"));
        assertEquals("group level", params.get("grouplevel"));
        assertEquals(new Long(12783), params.get("rolelevel"));
        assertEquals(new Long(1000), params.get("timeworked"));
        assertEquals(created, params.get("created"));
        assertEquals(performed, params.get("startdate"));
        assertEquals(updated, params.get("updated"));
    }

    public void testCreateAndRetrieve() throws GenericEntityException
    {
        final IssueManager issueManager = ComponentAccessor.getIssueManager();
        OfBizWorklogStore ofBizWorklogStore = new OfBizWorklogStore(ofBizDelegator, issueManager);

        final Long issueId = new Long(101);
        EntityUtils.createValue("Issue", EasyMap.build("id", issueId, "key", "TST-20"));
        Issue issue = issueManager.getIssueObject(issueId);

        long now = System.currentTimeMillis();
        Date created = new Date(now - 1000);
        Date performed = new Date(now);
        Date updated = new Date(now + 1000);

        Worklog worklog = new WorklogImpl(null, issue, null, "tim", "a comment", performed,
                "group level", new Long(12783), new Long(1000), "dylan", created, updated);

        Worklog createdWorklog = ofBizWorklogStore.create(worklog);
        //check attributes returned by the create method
        assertWorklogValues(issue, createdWorklog, created, performed, updated);

        Worklog retrievedWorklog = ofBizWorklogStore.getById(createdWorklog.getId());
        //retrieve worklog from data store and assert attributes again
        assertWorklogValues(issue, retrievedWorklog, created, performed, updated);
    }

    public void testUpdateAndRetrieve() throws GenericEntityException
    {
        final IssueManager issueManager = ComponentAccessor.getIssueManager();
        OfBizWorklogStore ofBizWorklogStore = new OfBizWorklogStore(ofBizDelegator, issueManager);

        final Long issueId = new Long(101);
        EntityUtils.createValue("Issue", EasyMap.build("id", issueId, "key", "TST-20"));
        Issue issue = issueManager.getIssueObject(issueId);

        Map params = getTestParamMap();
        params.put("issue", issue.getId());

        EntityUtils.createValue("Worklog", params);

        final Long updatedTimeSpent = new Long(100000);
        Worklog worklog = new WorklogImpl(null, issue, ID, AUTHOR, COMMENT, new Date(STARTDATE_MS), GROUP_LEVEL, null, updatedTimeSpent, UPDATE_AUTHOR, new Date(CREATED_MS), new Date(UPDATED_MS));

        Worklog editedWorklog = ofBizWorklogStore.update(worklog);
        //assert returned worklog has updated value
        assertEquals(updatedTimeSpent, editedWorklog.getTimeSpent());

        Worklog retrievedWorklog = ofBizWorklogStore.getById(editedWorklog.getId());
        //assert worklog retrieved from data store has updated value
        assertEquals(updatedTimeSpent, retrievedWorklog.getTimeSpent());
    }

    public void testRemove() throws GenericEntityException
    {
        final IssueManager issueManager = ComponentAccessor.getIssueManager();
        Map params = getTestParamMap();

        GenericValue value = EntityUtils.createValue("Worklog", params);
        OfBizWorklogStore ofBizWorklogStore = new OfBizWorklogStore(ofBizDelegator, issueManager);
        Long worklogId = value.getLong("id");

        // Confirm the worklog exists
        assertNotNull(ofBizWorklogStore.getById(worklogId));

        assertTrue(ofBizWorklogStore.delete(worklogId));

        // Confirm that the worklog no longer exists
        assertNull(ofBizWorklogStore.getById(worklogId));
    }

    public void testRemoveWithNullId() throws GenericEntityException
    {
        OfBizWorklogStore ofBizWorklogStore = new OfBizWorklogStore(ofBizDelegator, null);

        //test exception is thrown
        try
        {
            ofBizWorklogStore.delete(null);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            // we want this
        }
    }

    public void testRemoveWithNonExistantId() throws GenericEntityException
    {
        OfBizWorklogStore ofBizWorklogStore = new OfBizWorklogStore(ofBizDelegator, null);

        //should return false when delete fails 
        assertFalse(ofBizWorklogStore.delete(new Long(123)));
    }

    public void testGetCountForWorklogsRestrictedByGroupNullGroup()
    {
        OfBizWorklogStore ofBizWorklogstore = new OfBizWorklogStore(ofBizDelegator, null);
        try
        {
            ofBizWorklogstore.getCountForWorklogsRestrictedByGroup(null);
            fail();
        }
        catch(Exception e)
        {
            assertEquals("You must provide a non null group name.", e.getMessage());
        }
    }

    public void testGetCountForWorklogsRestrictedByGroup()
    {
        long now = System.currentTimeMillis();
        final Long id1 = new Long(100);
        final Long id2 = new Long(200);
        final Long id3 = new Long(300);

        final Long expectedIssueId = new Long(150);
        UtilsForTests.getTestEntity("Worklog", EasyMap.build("id", id1, "issue", expectedIssueId, "timeworked", new Long(1000), "created", new Timestamp(now), "grouplevel", "Test Group"));
        UtilsForTests.getTestEntity("Worklog", EasyMap.build("id", id2, "issue", expectedIssueId, "timeworked", new Long(2000), "created", new Timestamp(now + 500), "grouplevel", "Test Group"));
        UtilsForTests.getTestEntity("Worklog", EasyMap.build("id", id3, "issue", expectedIssueId, "timeworked", new Long(0), "created", new Timestamp(now + 550), "grouplevel", "Test Group"));
        UtilsForTests.getTestEntity("Worklog", EasyMap.build("id", new Long(400), "issue", new Long(250), "timeworked", new Long(3000), "created", new Timestamp(now)));

        Mock mockIssue = new Mock(Issue.class);
        mockIssue.expectAndReturn("getId", expectedIssueId);

        OfBizWorklogStore ofBizWorklogstore = new OfBizWorklogStore(ofBizDelegator, null);

        assertEquals(3, ofBizWorklogstore.getCountForWorklogsRestrictedByGroup("Test Group"));

    }

    public void testSwapWorklogGroupRestrictionNullGroups()
    {
        OfBizWorklogStore ofBizWorklogstore = new OfBizWorklogStore(ofBizDelegator, null);
        try
        {
            ofBizWorklogstore.swapWorklogGroupRestriction(null, "SwapGroup");
            fail();
        }
        catch(Exception e)
        {
            assertEquals("You must provide a non null group name.", e.getMessage());
        }
        try
        {
            ofBizWorklogstore.swapWorklogGroupRestriction("GroupName", null);
            fail();
        }
        catch(Exception e)
        {
            assertEquals("You must provide a non null swap group name.", e.getMessage());
        }
    }

    public void testSwapWorklogGroupRestriction()
    {
        long now = System.currentTimeMillis();
        final Long id1 = new Long(100);
        final Long id2 = new Long(200);
        final Long id3 = new Long(300);

        final Long expectedIssueId = new Long(150);
        UtilsForTests.getTestEntity("Worklog", EasyMap.build("id", id1, "issue", expectedIssueId, "timeworked", new Long(1000), "created", new Timestamp(now), "grouplevel", "Test Group"));
        UtilsForTests.getTestEntity("Worklog", EasyMap.build("id", id2, "issue", expectedIssueId, "timeworked", new Long(2000), "created", new Timestamp(now + 500), "grouplevel", "Test Group"));
        UtilsForTests.getTestEntity("Worklog", EasyMap.build("id", id3, "issue", expectedIssueId, "timeworked", new Long(0), "created", new Timestamp(now + 550), "grouplevel", "Test Group"));
        UtilsForTests.getTestEntity("Worklog", EasyMap.build("id", new Long(400), "issue", new Long(250), "timeworked", new Long(3000), "created", new Timestamp(now)));

        Mock mockIssue = new Mock(Issue.class);
        mockIssue.expectAndReturn("getId", expectedIssueId);

        OfBizWorklogStore ofBizWorklogstore = new OfBizWorklogStore(ofBizDelegator, null);

        assertEquals(3, ofBizWorklogstore.swapWorklogGroupRestriction("Test Group", "SwapGroup"));

        List worklogs = ofBizWorklogstore.getByIssue((Issue) mockIssue.proxy());
        //test that worklogs are ordered by creation date
        assertEquals(id1, ((Worklog) worklogs.get(0)).getId());
        assertEquals("SwapGroup", ((Worklog) worklogs.get(0)).getGroupLevel());
        assertEquals(id2, ((Worklog) worklogs.get(1)).getId());
        assertEquals("SwapGroup", ((Worklog) worklogs.get(1)).getGroupLevel());
        assertEquals(id3, ((Worklog) worklogs.get(2)).getId());
        assertEquals("SwapGroup", ((Worklog) worklogs.get(2)).getGroupLevel());
    }

    private void assertWorklogValues(Issue issue, Worklog worklog, Date created, Date performed, Date updated)
    {
        assertEquals(issue, worklog.getIssue());
        assertEquals("tim", worklog.getAuthor());
        assertEquals("a comment", worklog.getComment());
        assertEquals("dylan", worklog.getUpdateAuthor());
        assertEquals("group level", worklog.getGroupLevel());
        assertEquals(new Long(12783), worklog.getRoleLevelId());
        assertEquals(new Long(1000), worklog.getTimeSpent());
        assertEquals(created, worklog.getCreated());
        assertEquals(performed, worklog.getStartDate());
        assertEquals(updated, worklog.getUpdated());
    }

    private Map getTestParamMap()
    {
        long now = System.currentTimeMillis();

        Map attributes = new HashMap();
        attributes.put("id", ID);
        attributes.put("timeworked", TIME_SPENT);
        attributes.put("created", new Timestamp(CREATED_MS));
        attributes.put("startdate", new Timestamp(STARTDATE_MS));
        attributes.put("updated", new Timestamp(now));
        attributes.put("author", AUTHOR);
        attributes.put("body", COMMENT);
        attributes.put("grouplevel", GROUP_LEVEL);
        attributes.put("updateauthor", UPDATE_AUTHOR);
        return attributes;
    }
}
