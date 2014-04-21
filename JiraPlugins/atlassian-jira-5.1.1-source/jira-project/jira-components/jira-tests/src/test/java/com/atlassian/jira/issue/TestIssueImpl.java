package com.atlassian.jira.issue;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.impl.ImmutableUser;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.jira.local.testutils.MultiTenantContextTestUtils;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.user.MockCrowdService;
import com.atlassian.jira.user.util.DefaultUserManager;
import com.atlassian.jira.user.util.UserManager;
import org.junit.Test;
import static org.junit.Assert.*;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.config.MockSubTaskManager;
import com.atlassian.jira.mock.MockIssueManager;
import com.atlassian.jira.mock.MockProjectManager;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.local.ListeningTestCase;
import org.ofbiz.core.entity.GenericValue;

import java.sql.Timestamp;
import java.util.GregorianCalendar;
import java.util.Map;

/**
 * Test Case for IssueImpl.
 *
 * @since v3.13
 */
public class TestIssueImpl extends ListeningTestCase
{
    @Test
    public void testSetProjectID()
    {
        MockProjectManager mockProjectManager = new MockProjectManager();
        MockProject mockProject = new MockProject(10, "TST", "Test");
        mockProjectManager.addProject(mockProject);

        // Set up an issue
        MockGenericValue gvIssue_20 = new MockGenericValue("Issue", EasyMap.build("id", new Long(20)));
        IssueImpl issue = new IssueImpl(gvIssue_20, null, mockProjectManager, null, null, null, null, null, null, null, null);

        // test setProjectID with an issue that exists in the ProjectManager
        issue.setProjectId(new Long(10));
        assertEquals(new Long(10), issue.getProjectObject().getId());
        assertEquals("TST", issue.getProjectObject().getKey());
        assertEquals(new Long(10), issue.getProject().getLong("id"));
        assertEquals("TST", issue.getProject().getString("key"));
        //check the underlying GV has been updated.
        assertEquals(new Long(10), issue.getGenericValue().getLong("project"));

        //check the appropriate project entry has been made in the modified fields map.
        Map modifiedFields = issue.getModifiedFields();
        ModifiedValue modifiedValue = (ModifiedValue) modifiedFields.get("project");
        assertEquals(mockProject.getGenericValue(), modifiedValue.getNewValue());

        // test setProjectID with an issue that does not exist in the ProjectManager
        try
        {
            issue.setProjectId(new Long(12));
            fail("Should throw IllegalArgumentException.");
        }
        catch (IllegalArgumentException ex)
        {
            // Expected
            assertEquals("Invalid Project ID '12'.", ex.getMessage());
        }

        // test setProjectID with null ID
        issue.setProjectId(null);
        assertNull(issue.getProject());
        assertNull(issue.getProjectObject());
        assertNull(issue.getGenericValue().getLong("project"));
        //check the appropriate project entry has been made in the modified fields map.
        modifiedFields = issue.getModifiedFields();
        modifiedValue = (ModifiedValue) modifiedFields.get("project");
        assertNull(modifiedValue.getNewValue());
    }

    /**
     * Tests the setParentObject() method.
     *
     * @noinspection deprecation We are calling known deprecated methods as they are being tested.
     * Note that IDEA's statement-level suppression wasn't working for me in IDEA v6.04
     */
    @Test
    public void testSetParentObject()
    {
        // Create a Mock IssueManager and add a parent issue with ID 1
        MockIssueManager mockIssueManager = new MockIssueManager();
        MutableIssue oldParent = MockIssueFactory.createIssue(1);
        oldParent.setSecurityLevel(new MockGenericValue("SecurityLevel", EasyMap.build("id", new Long(10001))));
        mockIssueManager.addIssue(oldParent);

        // Create our subtask - issue 20
        MockGenericValue gvIssue_20 = new MockGenericValue("Issue", EasyMap.build("id", new Long(20)));
        IssueImpl issue = new IssueImpl(gvIssue_20, mockIssueManager, null, null, null, null, new MockSubTaskManager(), null, null, null, null);

        // assert parent ID is null by default.
        assertNull(issue.getParentId());
        // assert setParentId() works
        issue.setParentId(new Long(1));
        assertEquals(new Long(1), issue.getParentId());
        assertEquals(new Long(10001), issue.getParentObject().getSecurityLevelId());

        // Now modify a parent Issue and set this explicitly with setParentObject()
        MutableIssue newParent = MockIssueFactory.createIssue(12);
        newParent.setSecurityLevel(new MockGenericValue("SecurityLevel", EasyMap.build("id", new Long(10666))));
        issue.setParentObject(newParent);
        assertEquals(new Long(12), issue.getParentId());
        // The subtask should now be using the in-memory version of the parent.
        assertEquals(new Long(10666), issue.getParentObject().getSecurityLevelId());
        // Also we should get an in-memory Generic Value.
        assertEquals(new Long(10666), issue.getParent().getLong("security"));

        // resetting the subtask to use parentId, we should start looking up the IssueManager again.
        issue.setParentId(new Long(1));
        // first chekc the ParentID
        assertEquals(new Long(1), issue.getParentId());
        // The subtask should now be using the IssueManager version of the parent.
        assertEquals(new Long(10001), issue.getParentObject().getSecurityLevelId());
        // And the IssueManager version of the parent GenericValue
        assertEquals(new Long(10001), issue.getParent().getLong("security"));

        // Setting parentObject to null, should set ParentId to null as well
        issue.setParentObject(null);
        assertNull(issue.getParentId());
        assertNull(issue.getParentObject());
        assertNull(issue.getParent());
    }

    @Test
    public void testSetParentObjectIllegalArgument()
    {
        // Create our subtask - issue 20
        MockGenericValue gvIssue_20 = new MockGenericValue("Issue", EasyMap.build("id", new Long(20)));
        IssueImpl subTask = new IssueImpl(gvIssue_20, null, null, null, null, null, null, null, null, null, null);

        // Try to add an invalid Parent object
        // Now modify a parent Issue and set this explicitly with setParentObject()
        MockIssue newParent = new MockIssue(null);
        try
        {
            subTask.setParentObject(newParent);
            fail("Should have thrown IllegalArgumentException.");
        }
        catch (IllegalArgumentException ex)
        {
            // Expected
        }

    }

    @Test
    public void testResolutionDate()
    {
        final MutableIssue issue = MockIssueFactory.createIssue(1);
        final GenericValue issueGV = issue.getGenericValue();

        assertNull(issue.getResolutionDate());

        final Timestamp sometime = new Timestamp(new GregorianCalendar(2001, 2, 2).getTimeInMillis());
        final Timestamp now = new Timestamp(System.currentTimeMillis());

        //first try setting the resolution date directly
        issue.setResolutionDate(now);
        assertEquals(now, issue.getResolutionDate());
        assertEquals(now, issueGV.getTimestamp("resolutiondate"));

        issue.setResolutionDate(null);
        assertNull(issue.getResolutionDate());
        assertNull(issueGV.getTimestamp("resolutiondate"));

        //now try setting it via the resolution field.
        GenericValue mockResolutionGV = new MockGenericValue("resolution", EasyMap.build("id", new Long(10000)));
        issue.setResolution(mockResolutionGV);
        assertNotNull(issue.getResolutionDate());
        assertNotNull(issueGV.getTimestamp("resolutiondate"));
        //now check the date is 'roughly now' i.e. within the last 100 millis of now
        assertTimeEqualsNow(issue.getResolutionDate().getTime());
        assertTimeEqualsNow(issueGV.getTimestamp("resolutiondate").getTime());

        //now try setting the resolution field to the same value.  The date should not change.
        mockResolutionGV = new MockGenericValue("resolution", EasyMap.build("id", new Long(10000)));
        issue.setResolutionDate(sometime);
        issue.setResolution(mockResolutionGV);
        assertNotNull(issue.getResolutionDate());
        assertNotNull(issueGV.getTimestamp("resolutiondate"));
        assertEquals(sometime.getTime(), issue.getResolutionDate().getTime());
        assertEquals(sometime.getTime(), issueGV.getTimestamp("resolutiondate").getTime());

        //now try setting the resolution field to a different value.  The date should change.
        mockResolutionGV = new MockGenericValue("resolution", EasyMap.build("id", new Long(10010)));
        issue.setResolutionDate(sometime);
        issue.setResolution(mockResolutionGV);
        assertNotNull(issue.getResolutionDate());
        assertNotNull(issueGV.getTimestamp("resolutiondate"));
        //now check the date is 'roughly now' i.e. within the last 100 millis of now
        assertTimeEqualsNow(issue.getResolutionDate().getTime());
        assertTimeEqualsNow(issueGV.getTimestamp("resolutiondate").getTime());

        //now try setting the resolution field to the same value, when date is not set.  The date should change.
        mockResolutionGV = new MockGenericValue("resolution", EasyMap.build("id", new Long(10010)));
        issue.setResolutionDate(null);
        issue.setResolution(mockResolutionGV);
        assertNotNull(issue.getResolutionDate());
        assertNotNull(issueGV.getTimestamp("resolutiondate"));
        //now check the date is 'roughly now' i.e. within the last 100 millis of now
        assertTimeEqualsNow(issue.getResolutionDate().getTime());
        assertTimeEqualsNow(issueGV.getTimestamp("resolutiondate").getTime());

        issue.setResolution(null);
        assertNull(issue.getResolutionDate());
        assertNull(issueGV.getTimestamp("resolutiondate"));
    }

    @Test
    public void testGetAssigneeUserNull()
    {
        MultiTenantContextTestUtils.setupMultiTenantSystem();

        // Create our GV - issue 20
        MockGenericValue gvIssue_20 = new MockGenericValue("Issue", EasyMap.build("id", new Long(20), "assignee", null));
        CrowdService crowdService = new MockCrowdService();
        UserManager userManager = new DefaultUserManager(crowdService, null, null);
        IssueImpl issue = new IssueImpl(gvIssue_20, null, null, null, null, null, null, null, null, null, userManager);
        assertEquals(null, issue.getAssigneeId());
        assertEquals(null, issue.getAssigneeUser());
    }

    @Test
    public void testGetAssigneeUserExists() throws InvalidUserException, InvalidCredentialException
    {
        MultiTenantContextTestUtils.setupMultiTenantSystem();

        // Create our GV - issue 20
        MockGenericValue gvIssue_20 = new MockGenericValue("Issue", EasyMap.build("id", new Long(20), "assignee", "dude"));
        MockCrowdService crowdService = new MockCrowdService();
        crowdService.addUser(ImmutableUser.newUser().name("dude").displayName("Freaky Dude").toUser(), null);
        UserManager userManager = new DefaultUserManager(crowdService, null, null);
        IssueImpl issue = new IssueImpl(gvIssue_20, null, null, null, null, null, null, null, null, null, userManager);
        assertEquals("dude", issue.getAssigneeId());
        assertEquals("Freaky Dude", issue.getAssigneeUser().getDisplayName());
    }

    @Test
    public void testGetAssigneeUserAfterUserDeleted()
    {
        MultiTenantContextTestUtils.setupMultiTenantSystem();

        // Create our GV - issue 20
        MockGenericValue gvIssue_20 = new MockGenericValue("Issue", EasyMap.build("id", new Long(20), "assignee", "dude"));
        CrowdService crowdService = new MockCrowdService();
        UserManager userManager = new DefaultUserManager(crowdService, null, null);
        IssueImpl issue = new IssueImpl(gvIssue_20, null, null, null, null, null, null, null, null, null, userManager);
        assertEquals("dude", issue.getAssigneeId());
        // no user in system, but we should get a fake one with username as displayname
        assertEquals("dude", issue.getAssigneeUser().getDisplayName());
    }

    private void assertTimeEqualsNow(final long timeInMillis)
    {
        final long currentTimeInMillis = System.currentTimeMillis();
        assertTrue("date is not now (or 100ms before now)",
                timeInMillis > (currentTimeInMillis - 100) && timeInMillis <= currentTimeInMillis);
    }
}
