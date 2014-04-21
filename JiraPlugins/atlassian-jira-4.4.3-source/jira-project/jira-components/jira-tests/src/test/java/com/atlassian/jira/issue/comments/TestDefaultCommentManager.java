/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.comments;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.ActionConstants;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueImpl;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.util.TextAnalyzer;
import com.atlassian.jira.local.AbstractUsersTestCase;
import com.atlassian.jira.mock.MockProjectRoleManager;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.SubvertedPermissionManager;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.vcs.RepositoryException;
import com.mockobjects.constraint.Constraint;
import com.mockobjects.constraint.IsAnything;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import com.opensymphony.user.DuplicateEntityException;
import com.opensymphony.user.EntityNotFoundException;
import com.opensymphony.user.ImmutableException;
import com.opensymphony.user.User;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class TestDefaultCommentManager extends AbstractUsersTestCase
{
    private Issue issueObject;
    private User user;
    private User updater;
    private Timestamp timestamp;
    private static final String AN_UPDATED_COMMENT_BODY = "an updated comment body";
    private static final String UPDATED_AUTHOR = "updated author";
    private static final String A_TEST_COMMENT = "a test comment";

    public TestDefaultCommentManager(final String s)
    {
        super(s);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        user = UtilsForTests.getTestUser("Owen");
        updater = UtilsForTests.getTestUser("MrUpdater");

        final GenericValue project = UtilsForTests.getTestEntity("Project", EasyMap.build("name", "test project"));

        timestamp = new Timestamp(System.currentTimeMillis());
        final String key = "TST-1";
        final GenericValue issue = UtilsForTests.getTestEntity("Issue", EasyMap.build("id", new Long(1), "project", project.getLong("id"), "key",
            key, "updated", timestamp));
        issueObject = IssueImpl.getIssueObject(issue);

        UtilsForTests.getTestEntity("Action", EasyMap.build("id", new Long(1000), "issue", new Long(1), "body", "a comment", "type",
            ActionConstants.TYPE_COMMENT, "level", "Group A", "created", timestamp));
    }

    @Override
    protected void tearDown() throws Exception
    {
        issueObject = null;
        user = null;
        timestamp = null;
        super.tearDown();
    }

    public void testComments() throws EntityNotFoundException, GenericEntityException, DuplicateEntityException, ImmutableException
    {
        final CommentManager commentManager = ComponentManager.getComponentInstanceOfType(CommentManager.class);
        user.addToGroup(UtilsForTests.getTestGroup("Group A"));

        final List comments = commentManager.getCommentsForUser(issueObject, user);
        assertNotNull(comments);
        assertEquals(1, comments.size());
        assertEquals(new Long(1000), ((Comment) comments.get(0)).getId());
    }

    public void testCommentsNotInGroup() throws EntityNotFoundException, GenericEntityException, DuplicateEntityException, ImmutableException
    {
        final CommentManager commentManager = ComponentManager.getComponentInstanceOfType(CommentManager.class);
        final List comments = commentManager.getCommentsForUser(issueObject, user);
        assertNotNull(comments);
        assertTrue(comments.isEmpty());
    }

    public void testCommentsGroup() throws EntityNotFoundException, GenericEntityException, DuplicateEntityException, ImmutableException
    {
        final CommentManager commentManager = ComponentManager.getComponentInstanceOfType(CommentManager.class);
        user.addToGroup(UtilsForTests.getTestGroup("Group A"));

        final List comments = commentManager.getCommentsForUser(issueObject, user);
        assertNotNull(comments);
        assertEquals(1, comments.size());
        assertEquals(new Long(1000), ((Comment) comments.get(0)).getId());
    }

    public void testGetCommentById() throws EntityNotFoundException, GenericEntityException, DuplicateEntityException, ImmutableException
    {
        final CommentManager commentManager = ComponentManager.getComponentInstanceOfType(CommentManager.class);
        user.addToGroup(UtilsForTests.getTestGroup("Group A"));

        final Comment comment = commentManager.getCommentById(new Long(1000));
        assertNotNull(comment);
        assertEquals(new Long(1000), comment.getId());

        try
        {
            commentManager.getCommentById(null);
            fail("Null comment id should throw IllegalArgumentException");
        }
        catch (final IllegalArgumentException e)
        {
            // expected
        }

    }

    public void testGetMutableComment() throws EntityNotFoundException, GenericEntityException, DuplicateEntityException, ImmutableException
    {
        final CommentManager commentManager = ComponentManager.getComponentInstanceOfType(CommentManager.class);
        user.addToGroup(UtilsForTests.getTestGroup("Group A"));

        final Comment comment = commentManager.getMutableComment(new Long(1000));
        assertNotNull(comment);
        assertEquals(new Long(1000), comment.getId());

        try
        {
            commentManager.getMutableComment(null);
            fail("Null comment id should throw IllegalArgumentException");
        }
        catch (final IllegalArgumentException e)
        {
            // expected
        }

    }

    public void testActionsOneComment() throws ImmutableException, DuplicateEntityException, GenericEntityException, EntityNotFoundException, RepositoryException
    {
        final CommentManager commentManager = ComponentManager.getComponentInstanceOfType(CommentManager.class);
        user.addToGroup(UtilsForTests.getTestGroup("Group A"));

        List comments = commentManager.getCommentsForUser(issueObject, null);
        assertNotNull(comments);
        assertTrue(comments.isEmpty());
        comments = commentManager.getCommentsForUser(issueObject, user);
        assertNotNull(comments);
        assertEquals(1, comments.size());
        assertEquals(new Long(1000), ((Comment) comments.get(0)).getId());
    }

    public void testActionsMultipleComments() throws ImmutableException, DuplicateEntityException, GenericEntityException, EntityNotFoundException, RepositoryException
    {
        final CommentManager commentManager = ComponentManager.getComponentInstanceOfType(CommentManager.class);

        // Setup another comment
        final Timestamp anotherTimestamp = new Timestamp(timestamp.getTime() + 1);
        UtilsForTests.getTestEntity("Action", EasyMap.build("id", new Long(1001), "issue", new Long(1), "body", "the body of the comment", "type",
            ActionConstants.TYPE_COMMENT, "level", "Group A", "created", anotherTimestamp));

        user.addToGroup(UtilsForTests.getTestGroup("Group A"));

        List comments = commentManager.getCommentsForUser(issueObject, null);
        assertNotNull(comments);
        assertTrue(comments.isEmpty());
        comments = commentManager.getCommentsForUser(issueObject, user);
        assertNotNull(comments);
        assertEquals(2, comments.size());
        assertEquals(new Long(1000), ((Comment) comments.get(0)).getId());
        assertEquals(new Long(1001), ((Comment) comments.get(1)).getId());
    }

    public void testGetCommentsForUserWithRoleLevels() throws Exception
    {
        ManagerFactory.addService(PermissionManager.class, new SubvertedPermissionManager());
        final Mock mockProjectRoleManager = new Mock(ProjectRoleManager.class);
        ManagerFactory.addService(ProjectRoleManager.class, (ProjectRoleManager) mockProjectRoleManager.proxy());

        final ProjectRole projectRole = MockProjectRoleManager.PROJECT_ROLE_TYPE_2;

        mockProjectRoleManager.expectAndReturn("getProjectRole", projectRole.getId(), projectRole);
        mockProjectRoleManager.expectAndReturn("isUserInProjectRole", new Constraint[] { new IsEqual(user), new IsEqual(projectRole), new IsEqual(
            issueObject.getProjectObject()) }, Boolean.TRUE);

        final IssueManager issueManager = ComponentManager.getComponentInstanceOfType(IssueManager.class);
        final TextAnalyzer textAnalyzer = ComponentManager.getComponentInstanceOfType(TextAnalyzer.class);
        final GroupManager groupManager = ComponentManager.getComponentInstanceOfType(GroupManager.class);
        final ProjectRoleManager projectRoleManager = (ProjectRoleManager) mockProjectRoleManager.proxy();
        final CommentManager commentManager = new DefaultCommentManager(issueManager, textAnalyzer, projectRoleManager,
            new DefaultCommentPermissionManager(projectRoleManager, null, groupManager), null, null); // TODO add impl of CommentPermissionManager

        // check that user in this role can see the comment
        commentManager.create(issueObject, user.getName(), A_TEST_COMMENT, null, projectRole.getId(), false);
        final List comments = commentManager.getCommentsForUser(issueObject, user);
        assertEquals("User was in the role so he should see the comment", 1, comments.size());
        comments.get(0);
        mockProjectRoleManager.verify();
    }

    public void testCommentsForUserWithRoleLevelsNotPermitted() throws Exception
    {
        ManagerFactory.addService(PermissionManager.class, new SubvertedPermissionManager());
        final Mock mockProjectRoleManager = new Mock(ProjectRoleManager.class);
        ManagerFactory.addService(ProjectRoleManager.class, (ProjectRoleManager) mockProjectRoleManager.proxy());

        //CommentManager commentManager = (CommentManager) ComponentManager.getComponentInstanceOfType(CommentManager.class);

        final ProjectRole adminProjectRole = MockProjectRoleManager.PROJECT_ROLE_TYPE_1;
        mockProjectRoleManager.expectAndReturn("getProjectRole", adminProjectRole.getId(), adminProjectRole);
        mockProjectRoleManager.expectAndReturn("isUserInProjectRole",
            new Constraint[] { new IsEqual(user), new IsEqual(adminProjectRole), new IsEqual(issueObject.getProjectObject()) }, Boolean.FALSE);

        final IssueManager issueManager = ComponentManager.getComponentInstanceOfType(IssueManager.class);
        final TextAnalyzer textAnalyzer = ComponentManager.getComponentInstanceOfType(TextAnalyzer.class);
        final GroupManager groupManager = ComponentManager.getComponentInstanceOfType(GroupManager.class);
        final ProjectRoleManager projectRoleManager = (ProjectRoleManager) mockProjectRoleManager.proxy();
        final CommentManager commentManager = new DefaultCommentManager(issueManager, textAnalyzer, projectRoleManager,
            new DefaultCommentPermissionManager(projectRoleManager, null, groupManager), null, null); // TODO add impl of CommentPermissionManager

        // check that user cannot see admin's comment
        final User admin = UtilsForTests.getTestUser("Admin");
        commentManager.create(issueObject, admin.getName(), "comment for admins", null, adminProjectRole.getId(), false);
        final List comments = commentManager.getCommentsForUser(issueObject, user);
        assertEquals("User was NOT in the role so he should not see the comment", 0, comments.size());
        mockProjectRoleManager.verify();
    }

    public void testCreateComment() throws Exception
    {
        final CommentManager commentManager = ComponentManager.getComponentInstanceOfType(CommentManager.class);

        final Mock permissionsManagerMock = new Mock(PermissionManager.class);
        permissionsManagerMock.expectAndReturn("hasPermission", P.ANY_ARGS, Boolean.TRUE);

        final Comment comment = commentManager.create(issueObject, user.getName(), A_TEST_COMMENT, false);

        assertNotNull(comment.getId());
        assertNotNull(comment.getCreated());
        assertEquals(comment.getCreated(), comment.getUpdated());
        assertNull(comment.getGroupLevel());
        assertEquals(user.getName(), comment.getAuthor());
        assertEquals(user.getName(), comment.getUpdateAuthor());
        assertEquals(A_TEST_COMMENT, comment.getBody());

        final List comments = commentManager.getCommentsForUser(issueObject, user);
        assertNotNull(comments);
        assertEquals(1, comments.size());

        final Comment retrieved = (Comment) comments.get(0);
        assertEquals(comment, retrieved);
    }

    public void testCreateCommentIncUpdate() throws Exception
    {
        final CommentManager commentManager = ComponentManager.getComponentInstanceOfType(CommentManager.class);

        final Mock permissionsManagerMock = new Mock(PermissionManager.class);
        permissionsManagerMock.expectAndReturn("hasPermission", P.ANY_ARGS, Boolean.TRUE);

        final Date updated = new Date();
        final Comment comment = commentManager.create(issueObject, user.getName(), updater.getName(), A_TEST_COMMENT, null, null, null, updated,
            false);

        assertNotNull(comment.getId());
        assertNotNull(comment.getCreated());
        assertEquals(updated, comment.getUpdated());
        assertNull(comment.getGroupLevel());
        assertEquals(user.getName(), comment.getAuthor());
        assertEquals(updater.getName(), comment.getUpdateAuthor());
        assertEquals(A_TEST_COMMENT, comment.getBody());

        final List comments = commentManager.getCommentsForUser(issueObject, user);
        assertNotNull(comments);
        assertEquals(1, comments.size());

        final Comment retrieved = (Comment) comments.get(0);
        assertEquals(comment, retrieved);
    }

    public void testCreateActionAddComment() throws Exception
    {
        final CommentManager commentManager = ComponentManager.getComponentInstanceOfType(CommentManager.class);

        final Mock eventPublisher = new Mock(EventPublisher.class);
        eventPublisher.expectVoid("publish", P.args(new IsAnything()));

        ManagerFactory.addService(EventPublisher.class, (EventPublisher) eventPublisher.proxy());
        final Mock permissionsManagerMock = new Mock(PermissionManager.class);
        permissionsManagerMock.expectAndReturn("hasPermission", P.ANY_ARGS, Boolean.TRUE);

        commentManager.create(issueObject, user.getName(), "somebody", true);

        eventPublisher.verify();
    }

    public void testUpdateCommentWithDefaultUpdateDate() throws InterruptedException
    {
        final CommentManager commentManager = ComponentManager.getComponentInstanceOfType(CommentManager.class);

        final Comment comment = commentManager.create(issueObject, user.getName(), A_TEST_COMMENT, false);

        final Date originalIssueUpdateDate = comment.getIssue().getUpdated();

        final Date originalUpdateDate = comment.getUpdated();

        final MutableComment mutableComment = commentManager.getMutableComment(comment.getId());
        // Update the comment body, updateAuthor
        mutableComment.setBody(AN_UPDATED_COMMENT_BODY);
        mutableComment.setUpdateAuthor(UPDATED_AUTHOR);
        // A null date will set the updated date to now
        mutableComment.setUpdated(null);

        // This is to make sure that the updated time will be different than the original time
        Thread.sleep(100);

        // Store the comment this calls through to the manager
        commentManager.update(mutableComment, false);

        // re-Get the comment from the database
        final Comment updatedComment = commentManager.getCommentById(mutableComment.getId());

        assertEquals(AN_UPDATED_COMMENT_BODY, updatedComment.getBody());
        assertEquals(UPDATED_AUTHOR, updatedComment.getUpdateAuthor());
        assertEquals(comment.getAuthor(), updatedComment.getAuthor());
        assertEquals(comment.getRoleLevelId(), updatedComment.getRoleLevelId());
        assertEquals(comment.getGroupLevel(), updatedComment.getGroupLevel());
        assertEquals(comment.getCreated(), updatedComment.getCreated());
        assertTrue(originalUpdateDate.getTime() < updatedComment.getUpdated().getTime());
        assertTrue(originalIssueUpdateDate.getTime() < updatedComment.getIssue().getUpdated().getTime());
    }

    public void testUpdateCommentWithSetUpdateDate()
    {
        final CommentManager commentManager = ComponentManager.getComponentInstanceOfType(CommentManager.class);

        final Comment comment = commentManager.create(issueObject, user.getName(), A_TEST_COMMENT, false);

        final MutableComment mutableComment = commentManager.getMutableComment(comment.getId());
        // Update the comment body, updateAuthor
        mutableComment.setBody(AN_UPDATED_COMMENT_BODY);
        // A null date will set the updated date to now
        final Date MY_UPDATED_DATE = new Date();
        mutableComment.setUpdated(MY_UPDATED_DATE);

        // Store the comment this calls through to the manager
        commentManager.update(mutableComment, false);

        // re-Get the comment from the database
        final Comment updatedComment = commentManager.getCommentById(mutableComment.getId());

        assertEquals(MY_UPDATED_DATE.getTime(), updatedComment.getUpdated().getTime());
        assertEquals(AN_UPDATED_COMMENT_BODY, updatedComment.getBody());
    }

    public void testUpdateCommentDispatchEvent()
    {
        final Date MY_UPDATED_DATE = new Date();

        final AtomicBoolean dispatchCalled = new AtomicBoolean(false);

        final CommentManager commentManager = new DefaultCommentManager(ComponentAccessor.getIssueManager(),
            ComponentManager.getInstance().getTextAnalyzer(),
            ComponentManager.getComponentInstanceOfType(ProjectRoleManager.class),
            ComponentManager.getComponentInstanceOfType(CommentPermissionManager.class),
            ComponentManager.getComponentInstanceOfType(OfBizDelegator.class), null)
        {
            // This is mostly here for testing purposes so we do not really need to dispatch the event to know it was called correctly
            @Override
            void dispatchEvent(final Long eventTypeId, final Comment comment, final Map parameters)
            {
                dispatchCalled.set(true);
                // Verify we get what we care about.
                assertEquals(EventType.ISSUE_COMMENT_EDITED_ID, eventTypeId);
                assertEquals(AN_UPDATED_COMMENT_BODY, comment.getBody());
                assertEquals(MY_UPDATED_DATE, comment.getUpdated());
                final Comment originalComment = (Comment) parameters.get(CommentManager.EVENT_ORIGINAL_COMMENT_PARAMETER);
                assertNotNull(originalComment);
                assertEquals(A_TEST_COMMENT, originalComment.getBody());
                assertEquals(user.getName(), originalComment.getAuthor());
            }
        };

        final Comment comment = commentManager.create(issueObject, user.getName(), A_TEST_COMMENT, false);

        final MutableComment mutableComment = commentManager.getMutableComment(comment.getId());
        // Update the comment body, updateAuthor
        mutableComment.setBody(AN_UPDATED_COMMENT_BODY);
        // A null date will set the updated date to now
        mutableComment.setUpdated(MY_UPDATED_DATE);

        // Store the comment this calls through to the manager
        commentManager.update(mutableComment, true);

        // re-Get the comment from the database
        final Comment updatedComment = commentManager.getCommentById(mutableComment.getId());

        assertTrue(dispatchCalled.get());
        assertEquals(MY_UPDATED_DATE.getTime(), updatedComment.getUpdated().getTime());
        assertEquals(AN_UPDATED_COMMENT_BODY, updatedComment.getBody());

    }

    public void testUpdateCommentNoChangesCommentNotUpdated() throws Exception
    {
        final CommentManager commentManager = ComponentManager.getComponentInstanceOfType(CommentManager.class);

        final Comment comment = commentManager.create(issueObject, user.getName(), A_TEST_COMMENT, false);

        final MutableComment mutableComment = commentManager.getMutableComment(comment.getId());

        // Lets nuke the updated date so that it will become now
        mutableComment.setUpdated(null);

        // Lets sleep for a few millis to make sure that the update date will not be the same if an update occurs
        Thread.sleep(20);

        // Do not update the comment so that the update date will not be set and nothing will be persisted.
        // Store the comment this calls through to the manager, this should do nothing
        commentManager.update(mutableComment, false);

        // re-Get the comment from the database
        final Comment updatedComment = commentManager.getCommentById(mutableComment.getId());

        assertEquals(comment.getUpdated(), updatedComment.getUpdated());
    }

    public void testConstructChangeItemBeanForCommentDeleteWithGroupLevel()
    {
        final DefaultCommentManager commentManager = new DefaultCommentManager(null, null, null, null, null,
            ComponentAccessor.getJiraAuthenticationContext());

        final Comment commentWithGroupLevel = new CommentImpl(null, null, null, null, "testgroup", null, null, null, null);
        final ChangeItemBean changeItemBean = commentManager.constructChangeItemBeanForCommentDelete(commentWithGroupLevel);
        assertNotNull(changeItemBean);
        assertEquals("A comment with security level 'testgroup' was removed.", changeItemBean.getFrom());
    }

    public void testConstructChangeItemBeanForCommentDeleteWithRoleLevel()
    {
        final DefaultCommentManager commentManager = new DefaultCommentManager(null, null, null, null, null,
            ComponentAccessor.getJiraAuthenticationContext())
        {
            @Override
            public ProjectRole getProjectRole(final Long projectRoleId)
            {
                final Mock mockProjectRole = new Mock(ProjectRole.class);
                mockProjectRole.expectAndReturn("getName", "testrole");
                return (ProjectRole) mockProjectRole.proxy();
            }
        };

        final Comment commentWithGroupLevel = new CommentImpl(commentManager, null, null, null, null, new Long(1), null, null, null);
        final ChangeItemBean changeItemBean = commentManager.constructChangeItemBeanForCommentDelete(commentWithGroupLevel);
        assertNotNull(changeItemBean);
        assertEquals("A comment with security level 'testrole' was removed.", changeItemBean.getFrom());
    }

    public void testConstructChangeItemBeanForCommentDeleteWithNoLevel()
    {
        final DefaultCommentManager commentManager = new DefaultCommentManager(null, null, null, null, null,
            ComponentAccessor.getJiraAuthenticationContext());

        final Comment commentWithGroupLevel = new CommentImpl(commentManager, null, null, "testbody", null, null, null, null, null);
        final ChangeItemBean changeItemBean = commentManager.constructChangeItemBeanForCommentDelete(commentWithGroupLevel);
        assertNotNull(changeItemBean);
        assertEquals("testbody", changeItemBean.getFrom());
    }

    public void testGetCountForCommentsRestrictedByGroupNullGroup()
    {
        final CommentManager commentManager = ComponentManager.getComponentInstanceOfType(CommentManager.class);

        try
        {
            commentManager.getCountForCommentsRestrictedByGroup(null);
            fail();
        }
        catch (final Exception e)
        {
            // this should happen
            assertEquals("You must provide a non null group name.", e.getMessage());
        }
    }

    public void testGetCountForCommentsRestrictedByGroup()
    {
        final CommentManager commentManager = ComponentManager.getComponentInstanceOfType(CommentManager.class);

        assertEquals(1, commentManager.getCountForCommentsRestrictedByGroup("Group A"));
    }

    public void testSwapCommentGroupRestrictionNullGroups()
    {
        final CommentManager commentManager = ComponentManager.getComponentInstanceOfType(CommentManager.class);

        try
        {
            commentManager.swapCommentGroupRestriction(null, "AnotherGroup");
        }
        catch (final Exception e)
        {
            assertEquals("You must provide a non null group name.", e.getMessage());
        }
        try
        {
            commentManager.swapCommentGroupRestriction("Group", null);
        }
        catch (final Exception e)
        {
            assertEquals("You must provide a non null swap group name.", e.getMessage());
        }
    }

    public void testSwapCommentGroupRestriction()
    {
        final CommentManager commentManager = ComponentManager.getComponentInstanceOfType(CommentManager.class);

        assertEquals(1, commentManager.swapCommentGroupRestriction("Group A", "AnotherGroup"));
        final Comment comment = commentManager.getCommentById(new Long(1000));
        assertEquals("AnotherGroup", comment.getGroupLevel());
    }

}
