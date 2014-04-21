package com.atlassian.jira.bc.issue.comment;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.user.UserUtils;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.JiraException;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.issue.util.VisibilityValidator;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.issue.comments.CommentPermissionManager;
import com.atlassian.jira.issue.comments.MutableComment;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import mock.MockComment;
import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import com.atlassian.jira.local.AbstractUsersTestCase;
import com.atlassian.jira.mock.issue.MockIssue;

import java.util.Date;
import java.util.List;

/**
 * 
 */
public class TestDefaultCommentService extends AbstractUsersTestCase
{
    private User user;
    private User testUser;
    private Issue mockIssue;
    private JiraAuthenticationContext jiraAuthenticationContext;
    JiraServiceContext jiraServiceContext;
    private static final Long COMMENT_ID = new Long(1);

    public TestDefaultCommentService(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        testUser = UtilsForTests.getTestUser("testuser");
        if (testUser == null)
        {
            testUser = UserUtils.getUser("testuser");
        }
        jiraAuthenticationContext = ComponentAccessor.getJiraAuthenticationContext();
        jiraServiceContext = new JiraServiceContextImpl(testUser, new SimpleErrorCollection());
        mockIssue = new MockIssue(COMMENT_ID);
        com.opensymphony.user.User owen = UtilsForTests.getTestUser("Owen");
        owen.setFullName("Owen");
        user = owen;
    }

    protected void tearDown() throws Exception
    {
        user = null;
        super.tearDown();
    }

    // NOTE: this also tests the getMutableComment method as they run through the same code
    public void testGetCommentByIdHappyPath()
    {
        Comment mockComment = new MockComment("dude", "comm-ent");

        Mock mockCommentManager = new Mock(CommentManager.class);
        mockCommentManager.expectAndReturn("getMutableComment", P.ANY_ARGS, mockComment);

        Mock mockCommentPermissionManager = new Mock(CommentPermissionManager.class);
        mockCommentPermissionManager.expectAndReturn("hasBrowsePermission", P.ANY_ARGS, Boolean.TRUE);

        CommentService commentService = new DefaultCommentService(
                (CommentManager) mockCommentManager.proxy(), null, null, null,
                (CommentPermissionManager) mockCommentPermissionManager.proxy(), null, null, null);

        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        Comment comment = commentService.getCommentById(user, new Long(1000), errorCollection);
        assertFalse(errorCollection.hasAnyErrors());
        assertNotNull(comment);
        assertEquals(mockComment, comment);
        mockCommentManager.verify();
        mockCommentPermissionManager.verify();
    }

    // NOTE: this also tests the getMutableComment method as they run through the same code
    public void testGetCommentByIdNoPermission()
    {
        Comment mockComment = new MockComment("dude", "comm-ent");

        Mock mockCommentManager = new Mock(CommentManager.class);
        mockCommentManager.expectAndReturn("getMutableComment", P.ANY_ARGS, mockComment);

        Mock mockCommentPermissionManager = new Mock(CommentPermissionManager.class);
        mockCommentPermissionManager.expectAndReturn("hasBrowsePermission", P.ANY_ARGS, Boolean.FALSE);

        CommentService commentService = new DefaultCommentService(
                (CommentManager) mockCommentManager.proxy(), null,
                ComponentAccessor.getJiraAuthenticationContext(), null,
                (CommentPermissionManager) mockCommentPermissionManager.proxy(), null, null, null);

        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        Comment comment = commentService.getCommentById(user, new Long(1000), errorCollection);
        assertTrue(errorCollection.hasAnyErrors());
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("Owen, you do not have the permission to comment on this issue.",
                errorCollection.getErrorMessages().iterator().next());

        assertNull(comment);
        mockCommentManager.verify();
        mockCommentPermissionManager.verify();
    }

    // NOTE: this also tests the getMutableComment method as they run through the same code
    public void testGetCommentByIdNoCommentFound()
    {
        Mock mockCommentManager = new Mock(CommentManager.class);
        mockCommentManager.expectAndReturn("getMutableComment", P.ANY_ARGS, null);

        CommentService commentService = new DefaultCommentService(
                (CommentManager) mockCommentManager.proxy(), null,
                ComponentAccessor.getJiraAuthenticationContext(), null, null, null, null, null);

        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        Comment comment = commentService.getCommentById(user, new Long(1000), errorCollection);
        assertTrue(errorCollection.hasAnyErrors());
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("Can not find a comment for the id: 1000.", errorCollection.getErrorMessages().iterator().next());
        assertNull(comment);
        mockCommentManager.verify();
    }

    // NOTE: this also tests the getMutableComment method as they run through the same code
    public void testGetCommentByIdNullId()
    {
        CommentService commentService = new DefaultCommentService(null, null,
                ComponentAccessor.getJiraAuthenticationContext(), null, null, null, null, null);

        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        Comment comment = commentService.getCommentById(user, null, errorCollection);
        assertTrue(errorCollection.hasAnyErrors());
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("You must specify an id to locate a comment.", errorCollection.getErrorMessages().iterator().next());
        assertNull(comment);
    }

    public void testValidateCommentUpdateHappyPath()
    {
        Comment mockComment = new MockComment("dude", "comm-ent");

        Mock mockCommentManager = new Mock(CommentManager.class);
        mockCommentManager.expectAndReturn("getMutableComment", P.ANY_ARGS, mockComment);

        Mock mockCommentPermissionManager = new Mock(CommentPermissionManager.class);
        mockCommentPermissionManager.expectAndReturn("hasBrowsePermission", P.ANY_ARGS, Boolean.TRUE);

        CommentService commentService = new DefaultCommentService(
                (CommentManager) mockCommentManager.proxy(), null,
                ComponentAccessor.getJiraAuthenticationContext(), null,
                (CommentPermissionManager) mockCommentPermissionManager.proxy(), null, null, null)
        {
            public boolean isValidCommentData(User currentUser, Issue issue, String groupLevel, String roleLevelId, ErrorCollection errorCollection)
            {
                return true;
            }

            public boolean hasPermissionToEdit(User user, Comment comment, ErrorCollection errorCollection)
            {
                return true;
            }
        };

        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        commentService.validateCommentUpdate(user, new Long(1000), "test body", null, null, errorCollection);
        assertFalse(errorCollection.hasAnyErrors());
        mockCommentManager.verify();
        mockCommentPermissionManager.verify();
    }

    public void testValidateCommentUpdateNoId()
    {
        CommentService commentService = new DefaultCommentService(null, null,
                ComponentAccessor.getJiraAuthenticationContext(), null, null, null, null, null)
        {
            public boolean hasPermissionToEdit(User user, Comment comment, ErrorCollection errorCollection)
            {
                return true;
            }
        };

        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        commentService.validateCommentUpdate(user, null, "test body", null, null, errorCollection);
        assertTrue(errorCollection.hasAnyErrors());
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("You can not update a comment with a null id.", errorCollection.getErrorMessages().iterator().next());
    }

    public void testValidateCommentUpdateNoSuchComment()
    {
        Mock mockCommentManager = new Mock(CommentManager.class);
        mockCommentManager.expectAndReturn("getMutableComment", P.ANY_ARGS, null);

        CommentService commentService = new DefaultCommentService(
                (CommentManager) mockCommentManager.proxy(), null,
                ComponentAccessor.getJiraAuthenticationContext(), null, null, null, null, null)
        {
            public boolean hasPermissionToEdit(User user, Comment comment, ErrorCollection errorCollection)
            {
                return true;
            }
        };

        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        commentService.validateCommentUpdate(user, null, "test body", null, null, errorCollection);
        assertTrue(errorCollection.hasAnyErrors());
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("You can not update a comment with a null id.", errorCollection.getErrorMessages().iterator().next());
    }

    public void testUpdateCommentHappyPath()
    {
        Date date = new Date(System.currentTimeMillis() - 100);
        MutableComment mockComment = new MockComment(new Long(1000), "dude", "udpateAuthor", "comm-ent", "group-level", new Long(1), date, date, null);

        Mock mockCommentManager = new Mock(CommentManager.class);
        mockCommentManager.expectVoid("update", P.ANY_ARGS);

        CommentService commentService = new DefaultCommentService(
                (CommentManager) mockCommentManager.proxy(), null,
                ComponentAccessor.getJiraAuthenticationContext(), null, null,
                null, null, null)
        {
            public boolean isValidCommentData(User currentUser, Issue issue, String groupLevel, String roleLevelId, ErrorCollection errorCollection)
            {
                return true;
            }

            public boolean hasPermissionToEdit(User user, Comment comment, ErrorCollection errorCollection)
            {
                return true;
            }
        };

        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        commentService.update(user, mockComment, true, errorCollection);
        assertFalse(errorCollection.hasAnyErrors());
        assertTrue(date.getTime() < mockComment.getUpdated().getTime());
        assertEquals("Owen", mockComment.getUpdateAuthor());
        mockCommentManager.verify();
    }

    public void testUpdateCommentNullComment()
    {
        CommentService commentService = new DefaultCommentService(
                null, null,
                ComponentAccessor.getJiraAuthenticationContext(), null, null,
                null, null, null)
        {
            public boolean hasPermissionToEdit(User user, Comment comment, ErrorCollection errorCollection)
            {
                return true;
            }
        };

        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        commentService.update(user, null, true, errorCollection);
        assertTrue(errorCollection.hasAnyErrors());
        assertEquals("You can not update a null comment.", errorCollection.getErrorMessages().iterator().next());
    }

    public void testUpdateCommentNullCommentId()
    {
        Date date = new Date();
        MutableComment mockComment = new MockComment(null, "dude", "udpateAuthor", "comm-ent", "group-level", new Long(1), date, date, null);

        CommentService commentService = new DefaultCommentService(
                null, null,
                ComponentAccessor.getJiraAuthenticationContext(), null, null,
                null, null, null)
        {
            public boolean hasPermissionToEdit(User user, Comment comment, ErrorCollection errorCollection)
            {
                return true;
            }
        };

        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        commentService.update(user, mockComment, true, errorCollection);
        assertTrue(errorCollection.hasAnyErrors());
        assertEquals("You can not update a comment with a null id.", errorCollection.getErrorMessages().iterator().next());
    }

    public void testHasPermissionToEditNullComment()
    {
        CommentService commentService = new DefaultCommentService(
                null, null,
                ComponentAccessor.getJiraAuthenticationContext(), null, null,
                null, null, null);
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        assertFalse(commentService.hasPermissionToEdit(null, null, errorCollection));
        assertTrue(errorCollection.hasAnyErrors());
        assertEquals("You can not update a null comment.", errorCollection.getErrorMessages().iterator().next());
    }

    public void testHasPermissionToEditCommentNullId()
    {
        CommentService commentService = new DefaultCommentService(
                null, null,
                ComponentAccessor.getJiraAuthenticationContext(), null, null,
                null, null, null);
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        MutableComment mockComment = new MockComment(null, "dude", "udpateAuthor", "comm-ent", "group-level", new Long(1), null, null, null);
        assertFalse(commentService.hasPermissionToEdit(null, mockComment, errorCollection));
        assertTrue(errorCollection.hasAnyErrors());
        assertEquals("You can not update a comment with a null id.", errorCollection.getErrorMessages().iterator().next());
    }

    public void testHasPermissionToEditNoPermission()
    {
        Mock mockCommentPermissionManager = new Mock(CommentPermissionManager.class);
        mockCommentPermissionManager.expectAndReturn("hasEditPermission", P.ANY_ARGS, Boolean.FALSE);

        final VisibilityValidator visibilityValidator = new VisibilityValidator()
        {
            public boolean isGroupVisiblityEnabled()
            {
                return false;
            }

            public boolean isProjectRoleVisiblityEnabled()
            {
                return false;
            }

            public boolean isValidVisibilityData(JiraServiceContext jiraServiceContext, String i18nPrefix, Issue issue, String groupLevel, String roleLevelId)
            {
                return false;
            }
        };
        CommentService commentService = new DefaultCommentService(
                null, null,
                ComponentAccessor.getJiraAuthenticationContext(), null,
                (CommentPermissionManager) mockCommentPermissionManager.proxy(), null, null, visibilityValidator)
        {
            boolean isIssueInEditableWorkflowState(Issue issue)
            {
                return true;
            }
        };

        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        MutableComment mockComment = new MockComment(new Long(9), "dude", "udpateAuthor", "comm-ent", "group-level", new Long(1), null, null, null);
        assertFalse(commentService.hasPermissionToEdit(null, mockComment, errorCollection));
        assertTrue(errorCollection.hasAnyErrors());
        assertEquals("You do not have the permission for this comment.", errorCollection.getErrorMessages().iterator().next());

        errorCollection = new SimpleErrorCollection();
        assertFalse(commentService.hasPermissionToEdit(user, mockComment, errorCollection));
        assertTrue(errorCollection.hasAnyErrors());
        assertEquals("Owen, you do not have the permission for this comment.", errorCollection.getErrorMessages().iterator().next());

    }

    public void testHasPermissionToEditHappyPath()
    {
        Mock mockCommentPermissionManager = new Mock(CommentPermissionManager.class);
        mockCommentPermissionManager.expectAndReturn("hasEditPermission", P.ANY_ARGS, Boolean.TRUE);

        final VisibilityValidator visibilityValidator = new VisibilityValidator()
        {
            public boolean isGroupVisiblityEnabled()
            {
                return false;
            }

            public boolean isProjectRoleVisiblityEnabled()
            {
                return false;
            }

            public boolean isValidVisibilityData(JiraServiceContext jiraServiceContext, String i18nPrefix, Issue issue, String groupLevel, String roleLevelId)
            {
                return true;
            }
        };
        CommentService commentService = new DefaultCommentService(
                null, null,
                ComponentAccessor.getJiraAuthenticationContext(), null,
                (CommentPermissionManager) mockCommentPermissionManager.proxy(), null, null, visibilityValidator)
        {
            boolean isIssueInEditableWorkflowState(Issue issue)
            {
                return true;
            }
        };

        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        MutableComment mockComment = new MockComment(new Long(9), "dude", "udpateAuthor", "comm-ent", "group-level", new Long(1), null, null, null);
        assertTrue(commentService.hasPermissionToEdit(null, mockComment, errorCollection));
        assertFalse(errorCollection.hasAnyErrors());
    }

    public void testHasPermissionToEditNonEditableWorkflowState()
    {
        Mock mockCommentPermissionManager = new Mock(CommentPermissionManager.class);
        mockCommentPermissionManager.expectAndReturn("hasEditPermission", P.ANY_ARGS, Boolean.TRUE);

        CommentService commentService = new DefaultCommentService(
                null, null,
                ComponentAccessor.getJiraAuthenticationContext(), null,
                (CommentPermissionManager) mockCommentPermissionManager.proxy(), null, null, null)
        {
            boolean isIssueInEditableWorkflowState(Issue issue)
            {
                return false;
            }
        };

        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        MutableComment mockComment = new MockComment(new Long(9), "dude", "udpateAuthor", "comm-ent", "group-level", new Long(1), null, null, null);
        assertFalse(commentService.hasPermissionToEdit(null, mockComment, errorCollection));
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("You can not edit the comment as the issue is in a non-editable workflow state.", errorCollection.getErrorMessages().iterator().next());
    }

    public void testUserHasCommentDeleteAllPermission()
    {
        MockControl controlPermissionManager = MockControl.createControl(PermissionManager.class);
        PermissionManager mockPermissionManager = (PermissionManager) controlPermissionManager.getMock();
        mockPermissionManager.hasPermission(Permissions.COMMENT_DELETE_ALL, mockIssue, testUser);
        controlPermissionManager.setReturnValue(true);
        controlPermissionManager.replay();

        DefaultCommentService defaultCommentService = new DefaultCommentService(null, mockPermissionManager, null, null, null, null, null, null);
        assertTrue(defaultCommentService.userHasCommentDeleteAllPermission(mockIssue, testUser));
        controlPermissionManager.verify();
    }

    public void testUserDoesNotHaveCommentDeleteAllPermission()
    {
        MockControl controlPermissionManager = MockControl.createControl(PermissionManager.class);
        PermissionManager mockPermissionManager = (PermissionManager) controlPermissionManager.getMock();
        mockPermissionManager.hasPermission(Permissions.COMMENT_DELETE_ALL, mockIssue, testUser);
        controlPermissionManager.setReturnValue(false);
        controlPermissionManager.replay();

        DefaultCommentService defaultCommentService = new DefaultCommentService(null, mockPermissionManager, null, null, null, null, null, null);
        assertFalse(defaultCommentService.userHasCommentDeleteAllPermission(mockIssue, testUser));
        controlPermissionManager.verify();
    }

    public void testUserHasCommentDeleteOwnPermission()
    {
        MockControl controlPermissionManager = MockControl.createControl(PermissionManager.class);
        PermissionManager mockPermissionManager = (PermissionManager) controlPermissionManager.getMock();
        mockPermissionManager.hasPermission(Permissions.COMMENT_DELETE_OWN, mockIssue, testUser);
        controlPermissionManager.setReturnValue(true);
        controlPermissionManager.replay();

        DefaultCommentService defaultCommentService = new DefaultCommentService(null, mockPermissionManager, null, null, null, null, null, null);
        assertTrue(defaultCommentService.userHasCommentDeleteOwnPermission(mockIssue, testUser));
        controlPermissionManager.verify();
    }

    public void testUserDoesNotHaveCommentDeleteOwnPermission()
    {
        MockControl controlPermissionManager = MockControl.createControl(PermissionManager.class);
        PermissionManager mockPermissionManager = (PermissionManager) controlPermissionManager.getMock();
        mockPermissionManager.hasPermission(Permissions.COMMENT_DELETE_OWN, mockIssue, testUser);
        controlPermissionManager.setReturnValue(false);
        controlPermissionManager.replay();

        DefaultCommentService defaultCommentService = new DefaultCommentService(null, mockPermissionManager, null, null, null, null, null, null);
        assertFalse(defaultCommentService.userHasCommentDeleteOwnPermission(mockIssue, testUser));
        controlPermissionManager.verify();
    }

    public void testDeleteWithNullComment()
    {
        DefaultCommentService defaultCommentService = new DefaultCommentService(null, null, jiraAuthenticationContext, null, null, null, null, null);
        JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(testUser, new SimpleErrorCollection());
        defaultCommentService.delete(jiraServiceContext, null, true);

        assertEquals(1, jiraServiceContext.getErrorCollection().getErrorMessages().size());
        assertEquals("You can not delete a null comment.", jiraServiceContext.getErrorCollection().getErrorMessages().iterator().next());
    }

    public void testDeleteWithNullCommentId()
    {
        DefaultCommentService defaultCommentService = new DefaultCommentService(null, null, jiraAuthenticationContext, null, null, null, null, null);

        MockControl controlComment = MockClassControl.createControl(Comment.class);
        Comment mockComment = (Comment) controlComment.getMock();
        mockComment.getId();
        controlComment.setReturnValue(null);
        controlComment.replay();

        defaultCommentService.delete(jiraServiceContext, mockComment, true);

        assertEquals(1, jiraServiceContext.getErrorCollection().getErrorMessages().size());
        assertEquals("You can not delete a comment with a null id.", jiraServiceContext.getErrorCollection().getErrorMessages().iterator().next());
        controlComment.verify();
    }

    public void testDeleteWithExceptionInUpdateIssue()
    {
        Mock mockCommentManager = new Mock(CommentManager.class);
        mockCommentManager.expectAndReturn("delete", P.ANY_ARGS, new ChangeItemBean(null, null, null, null));

        DefaultCommentService defaultCommentService = new DefaultCommentService((CommentManager) mockCommentManager.proxy(), null, jiraAuthenticationContext, null, null, null, null, null)
        {
            protected void doUpdateWithChangelog(Long eventTypeId, List changeItems, Issue issue, User user, boolean dispatchEvent) throws JiraException
            {
                throw new JiraException();
            }

            public boolean hasPermissionToDelete(JiraServiceContext jiraServiceContext, Long commentId)
            {
                return true;
            }
        };

        JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(testUser, new SimpleErrorCollection());

        MockControl controlComment = MockClassControl.createControl(Comment.class);
        Comment mockComment = (Comment) controlComment.getMock();
        mockComment.getId();
        controlComment.setReturnValue(COMMENT_ID);
        mockComment.getId();
        controlComment.setReturnValue(COMMENT_ID);
        mockComment.getIssue();
        controlComment.setReturnValue(mockIssue);
        controlComment.replay();

        defaultCommentService.delete(jiraServiceContext, mockComment, true);

        assertEquals(1, jiraServiceContext.getErrorCollection().getErrorMessages().size());
        assertEquals("There was an error generating the change item and updating the issues updated date, the comment was deleted.", jiraServiceContext.getErrorCollection().getErrorMessages().iterator().next());
        controlComment.verify();
        mockCommentManager.verify();
    }

    public void testHasPermissionToDeleteIssueNotEditable()
    {
        MockControl controlComment = MockClassControl.createControl(Comment.class);
        final Comment mockComment = (Comment) controlComment.getMock();
        mockComment.getIssue();
        controlComment.setReturnValue(mockIssue);
        controlComment.replay();

        DefaultCommentService defaultCommentService = new DefaultCommentService(null, null, jiraAuthenticationContext, null, null, null, null, null)
        {

            protected boolean hasVisibility(JiraServiceContext jiraServiceContext, Comment comment)
            {
                return true;
            }

            boolean isIssueInEditableWorkflowState(Issue issue)
            {
                return false;
            }

            public Comment getCommentById(User user, Long commentId, ErrorCollection errorCollection)
            {
                return mockComment;
            }
        };

        assertFalse(defaultCommentService.hasPermissionToDelete(jiraServiceContext, COMMENT_ID));
        assertEquals(1, jiraServiceContext.getErrorCollection().getErrorMessages().size());
        assertEquals("You can not delete the comment as the issue is in a non-editable workflow state.", jiraServiceContext.getErrorCollection().getErrorMessages().iterator().next());
    }

    public void testHasPermissionToDeleteHasDeleteAllPermission()
    {
        MockControl controlComment = MockClassControl.createControl(Comment.class);
        final Comment mockComment = (Comment) controlComment.getMock();
        mockComment.getIssue();
        controlComment.setReturnValue(mockIssue);
        controlComment.replay();

        DefaultCommentService defaultCommentService = new DefaultCommentService(null, null, jiraAuthenticationContext, null, null, null, null, null)
        {
            protected boolean hasVisibility(JiraServiceContext jiraServiceContext, Comment comment)
            {
                return true;
            }

            boolean isIssueInEditableWorkflowState(Issue issue)
            {
                return true;
            }

            public Comment getCommentById(User user, Long commentId, ErrorCollection errorCollection)
            {
                return mockComment;
            }


            boolean userHasCommentDeleteAllPermission(Issue issue, User user)
            {
                return true;
            }
        };

        assertTrue(defaultCommentService.hasPermissionToDelete(jiraServiceContext, COMMENT_ID));
    }

    public void testHasPermissionToDeleteHasDeleteOwnPermissionUserIsAuthor()
    {
        MockControl controlComment = MockClassControl.createControl(Comment.class);
        final Comment mockComment = (Comment) controlComment.getMock();
        mockComment.getIssue();
        controlComment.setReturnValue(mockIssue);
        controlComment.replay();

        MockControl commentManagerControl = MockClassControl.createControl(CommentManager.class);
        final CommentManager commentManager = (CommentManager) commentManagerControl.getMock();
        commentManager.isUserCommentAuthor((User) jiraServiceContext.getUser(), mockComment);
        commentManagerControl.setReturnValue(true);
        commentManagerControl.replay();

        DefaultCommentService defaultCommentService = new DefaultCommentService(commentManager, null, jiraAuthenticationContext, null, null, null, null, null)
        {
            protected boolean hasVisibility(JiraServiceContext jiraServiceContext, Comment comment)
            {
                return true;
            }

            boolean isIssueInEditableWorkflowState(Issue issue)
            {
                return true;
            }

            public Comment getCommentById(User user, Long commentId, ErrorCollection errorCollection)
            {
                return mockComment;
            }

            boolean userHasCommentDeleteAllPermission(Issue issue, User user)
            {
                return false;
            }

            boolean userHasCommentDeleteOwnPermission(Issue issue, User user)
            {
                return true;
            }
        };

        assertTrue(defaultCommentService.hasPermissionToDelete(jiraServiceContext, COMMENT_ID));

        controlComment.verify();
        commentManagerControl.verify();
    }

    public void testHasPermissionToDeleteHasDeleteOwnUserPermissionIsNotAuthor()
    {
        MockControl controlComment = MockClassControl.createControl(Comment.class);
        final Comment mockComment = (Comment) controlComment.getMock();
        mockComment.getIssue();
        controlComment.setReturnValue(mockIssue);
        mockComment.getId();
        controlComment.setReturnValue(new Long(1));
        controlComment.replay();

        MockControl commentManagerControl = MockClassControl.createControl(CommentManager.class);
        final CommentManager commentManager = (CommentManager) commentManagerControl.getMock();
        commentManager.isUserCommentAuthor((User) jiraServiceContext.getUser(), mockComment);
        commentManagerControl.setReturnValue(false);
        commentManagerControl.replay();
        
        DefaultCommentService defaultCommentService = new DefaultCommentService(commentManager, null, jiraAuthenticationContext, null, null, null, null, null)
        {
            protected boolean hasVisibility(JiraServiceContext jiraServiceContext, Comment comment)
            {
                return true;
            }

            boolean isIssueInEditableWorkflowState(Issue issue)
            {
                return true;
            }

            public Comment getCommentById(User user, Long commentId, ErrorCollection errorCollection)
            {
                return mockComment;
            }

            boolean userHasCommentDeleteAllPermission(Issue issue, User user)
            {
                return false;
            }

            boolean userHasCommentDeleteOwnPermission(Issue issue, User user)
            {
                return true;
            }
        };

        assertFalse(defaultCommentService.hasPermissionToDelete(jiraServiceContext, COMMENT_ID));
        assertEquals(1, jiraServiceContext.getErrorCollection().getErrorMessages().size());
        assertEquals("You do not have permission to delete comment with id: 1", jiraServiceContext.getErrorCollection().getErrorMessages().iterator().next());

        controlComment.verify();
        commentManagerControl.verify();        
    }
}
