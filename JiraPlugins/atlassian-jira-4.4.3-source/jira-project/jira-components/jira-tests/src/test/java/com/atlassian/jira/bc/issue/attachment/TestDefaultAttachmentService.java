package com.atlassian.jira.bc.issue.attachment;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.JiraException;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.util.IssueUpdateBean;
import com.atlassian.jira.issue.util.IssueUpdater;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.util.OSUserConverter;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.constraint.IsNull;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import com.opensymphony.user.User;
import org.easymock.MockControl;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

/**
 *
 */
public class TestDefaultAttachmentService extends LegacyJiraMockTestCase
{

    private static final String TEST_ERROR_MESSAGE = "Test Error Message";
    private JiraServiceContext jiraServiceContext;
    private DefaultAttachmentService defaultAttachmentService;
    private JiraAuthenticationContext jiraAuthenticationContext;
    private Issue mockIssue;
    private Project mockProject;
    private ErrorCollection errorCollection;
    private User testUser;
    private static final Long ATTACHMENT_ID = new Long(1);
    private CrowdService crowdService;

    protected void setUp() throws OperationNotPermittedException, InvalidUserException, InvalidCredentialException
    {
        try
        {
            super.setUp();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }

        crowdService = ComponentManager.getComponentInstanceOfType(CrowdService.class);
        testUser = OSUserConverter.convertToOSUser(new MockUser("testuser"));
        crowdService.addUser(testUser, "");


        jiraAuthenticationContext = ComponentAccessor.getJiraAuthenticationContext();
        mockIssue = new MockIssue(ATTACHMENT_ID);
        mockProject = new MockProject(10040);
        errorCollection = new SimpleErrorCollection();
        jiraServiceContext = new JiraServiceContextImpl(testUser, errorCollection);
    }

    public void testCanDeleteAnyAttachmentValidation()
    {
        defaultAttachmentService = new DefaultAttachmentService(null, null, jiraAuthenticationContext, null, null);
        defaultAttachmentService.canDeleteAnyAttachment(null, null, errorCollection);
        assertTrue(errorCollection.hasAnyErrors());
        assertEquals(1, errorCollection.getErrorMessages().size());
        String errorMessage = (String) errorCollection.getErrorMessages().iterator().next();
        assertEquals("Can not check attachment permissions without a specified issue.", errorMessage);
    }

    public void testCanDeleteAnyAttachmentWithDeleteAllPermission()
    {
        Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.expectAndReturn("hasPermission", P.args(new IsEqual(new Integer(Permissions.ATTACHMENT_DELETE_ALL)), new IsEqual(mockIssue), new IsNull()), Boolean.TRUE);

        DefaultAttachmentService defaultAttachmentService = new DefaultAttachmentService(null, (PermissionManager) mockPermissionManager.proxy(), jiraAuthenticationContext, null, null);

        assertTrue(defaultAttachmentService.canDeleteAnyAttachment(null, mockIssue, errorCollection));
        mockPermissionManager.verify();
    }

    public void testCanDeleteAnyAttachmentWithDeleteOwnPermissionAndUserHasAuthoredAttachment()
    {
        DefaultAttachmentService defaultAttachmentService = new DefaultAttachmentService(null, null, jiraAuthenticationContext, null, null)
        {
            boolean isAuthorOfAtLeastOneAttachment(Issue issue, User user)
            {
                return true;
            }

            boolean userHasAttachmentDeleteAllPermission(Issue issue, User user)
            {
                return false;
            }

            boolean userHasAttachmentDeleteOwnPermission(Issue issue, User user)
            {
                return true;
            }
        };

        assertTrue(defaultAttachmentService.canDeleteAnyAttachment(testUser, mockIssue, errorCollection));
    }

    public void testCanDeleteAnyAttachmentWithDeleteOwnPermissionAndUserHasNotAuthoredAttachment()
    {
        DefaultAttachmentService defaultAttachmentService = new DefaultAttachmentService(null, null, jiraAuthenticationContext, null, null)
        {
            boolean isAuthorOfAtLeastOneAttachment(Issue issue, User user)
            {
                return false;
            }

            boolean userHasAttachmentDeleteAllPermission(Issue issue, User user)
            {
                return false;
            }

            boolean userHasAttachmentDeleteOwnPermission(Issue issue, User user)
            {
                return true;
            }
        };

        assertFalse(defaultAttachmentService.canDeleteAnyAttachment(testUser, mockIssue, errorCollection));
    }

    public void testCanDeleteAnyAttachmentWithNoPermissionsAndUserHasAuthoredAttachment()
    {
        DefaultAttachmentService defaultAttachmentService = new DefaultAttachmentService(null, null, jiraAuthenticationContext, null, null)
        {
            boolean isAuthorOfAtLeastOneAttachment(Issue issue, User user)
            {
                return true;
            }

            boolean userHasAttachmentDeleteAllPermission(Issue issue, User user)
            {
                return false;
            }

            boolean userHasAttachmentDeleteOwnPermission(Issue issue, User user)
            {
                return false;
            }
        };

        assertFalse(defaultAttachmentService.canDeleteAnyAttachment(testUser, mockIssue, errorCollection));
    }

    public void testCanDeleteAnyAttachmentWithNoPermissionsAndUserHasNotAuthoredAttachment()
    {
        DefaultAttachmentService defaultAttachmentService = new DefaultAttachmentService(null, null, jiraAuthenticationContext, null, null)
        {
            boolean isAuthorOfAtLeastOneAttachment(Issue issue, User user)
            {
                return false;
            }

            boolean userHasAttachmentDeleteAllPermission(Issue issue, User user)
            {
                return false;
            }

            boolean userHasAttachmentDeleteOwnPermission(Issue issue, User user)
            {
                return false;
            }
        };

        assertFalse(defaultAttachmentService.canDeleteAnyAttachment(testUser, mockIssue, errorCollection));
    }

    public void testIsAuthorOfAtLeastOneAttachmentTrue()
    {
        Attachment mockAttachment = new MockAttachment()
        {
            public String getAuthor()
            {
                return testUser.getName();
            }
        };

        Mock mockAttachmentManager = new Mock(AttachmentManager.class);
        mockAttachmentManager.expectAndReturn("getAttachments", P.args(new IsEqual(mockIssue)), EasyList.build(mockAttachment));

        DefaultAttachmentService defaultAttachmentService = new DefaultAttachmentService((AttachmentManager) mockAttachmentManager.proxy(), null, jiraAuthenticationContext, null, null);
        assertTrue(defaultAttachmentService.isAuthorOfAtLeastOneAttachment(mockIssue, testUser));
    }

    public void testIsAuthorOfAtLeastOneAttachmentFalse()
    {
        Attachment mockAttachment = new MockAttachment()
        {
            public String getAuthor()
            {
                return "joe";
            }
        };

        Mock mockAttachmentManager = new Mock(AttachmentManager.class);
        mockAttachmentManager.expectAndReturn("getAttachments", P.args(new IsEqual(mockIssue)), EasyList.build(mockAttachment));

        DefaultAttachmentService defaultAttachmentService = new DefaultAttachmentService((AttachmentManager) mockAttachmentManager.proxy(), null, jiraAuthenticationContext, null, null);
        assertFalse(defaultAttachmentService.isAuthorOfAtLeastOneAttachment(mockIssue, testUser));
    }

    public void testIsUserAttachmentAuthorWithMatchingNames()
    {
        Attachment mockAttachment = new MockAttachment()
        {
            public String getAuthor()
            {
                return testUser.getName();
            }
        };

        DefaultAttachmentService defaultAttachmentService = new DefaultAttachmentService(null, null, null, null, null);
        assertTrue(defaultAttachmentService.isUserAttachmentAuthor(mockAttachment, testUser));
    }

    public void testIsUserAttachmentAuthorWithMismatchedNames()
    {
        Attachment mockAttachment = new MockAttachment()
        {
            public String getAuthor()
            {
                return "joe";
            }
        };

        DefaultAttachmentService defaultAttachmentService = new DefaultAttachmentService(null, null, null, null, null);
        assertFalse(defaultAttachmentService.isUserAttachmentAuthor(mockAttachment, testUser));
    }

    public void testIsUserAttachmentAuthorWithAuthorNull()
    {
        Attachment mockAttachment = new MockAttachment()
        {
            public String getAuthor()
            {
                return null;
            }
        };

        DefaultAttachmentService defaultAttachmentService = new DefaultAttachmentService(null, null, null, null, null);
        assertFalse(defaultAttachmentService.isUserAttachmentAuthor(mockAttachment, testUser));
    }

    public void testIsUserAttachmentAuthorWithUserNull()
    {
        Attachment mockAttachment = new MockAttachment()
        {
            public String getAuthor()
            {
                return testUser.getName();
            }
        };

        DefaultAttachmentService defaultAttachmentService = new DefaultAttachmentService(null, null, null, null, null);
        assertFalse(defaultAttachmentService.isUserAttachmentAuthor(mockAttachment, null));
    }

    public void testIsUserAttachmentAuthorWithUserAndAuthorNull()
    {
        Attachment mockAttachment = new MockAttachment()
        {
            public String getAuthor()
            {
                return null;
            }
        };

        DefaultAttachmentService defaultAttachmentService = new DefaultAttachmentService(null, null, null, null, null);
        assertTrue(defaultAttachmentService.isUserAttachmentAuthor(mockAttachment, null));
    }

    public void testUserHasAttachmentDeleteAllPermission()
    {
        MockControl controlPermissionManager = MockControl.createControl(PermissionManager.class);
        PermissionManager mockPermissionManager = (PermissionManager) controlPermissionManager.getMock();
        mockPermissionManager.hasPermission(Permissions.ATTACHMENT_DELETE_ALL, mockIssue, testUser);
        controlPermissionManager.setReturnValue(true);
        controlPermissionManager.replay();

        DefaultAttachmentService defaultAttachmentService = new DefaultAttachmentService(null, mockPermissionManager, null, null, null);
        assertTrue(defaultAttachmentService.userHasAttachmentDeleteAllPermission(mockIssue, testUser));
        controlPermissionManager.verify();
    }

    public void testUserDoesNotHaveAttachmentDeleteAllPermission()
    {
        MockControl controlPermissionManager = MockControl.createControl(PermissionManager.class);
        PermissionManager mockPermissionManager = (PermissionManager) controlPermissionManager.getMock();
        mockPermissionManager.hasPermission(Permissions.ATTACHMENT_DELETE_ALL, mockIssue, testUser);
        controlPermissionManager.setReturnValue(false);
        controlPermissionManager.replay();

        DefaultAttachmentService defaultAttachmentService = new DefaultAttachmentService(null, mockPermissionManager, null, null, null);
        assertFalse(defaultAttachmentService.userHasAttachmentDeleteAllPermission(mockIssue, testUser));
        controlPermissionManager.verify();
    }

    public void testUserHasAttachmentDeleteOwnPermission()
    {
        MockControl controlPermissionManager = MockControl.createControl(PermissionManager.class);
        PermissionManager mockPermissionManager = (PermissionManager) controlPermissionManager.getMock();
        mockPermissionManager.hasPermission(Permissions.ATTACHMENT_DELETE_OWN, mockIssue, testUser);
        controlPermissionManager.setReturnValue(true);
        controlPermissionManager.replay();

        DefaultAttachmentService defaultAttachmentService = new DefaultAttachmentService(null, mockPermissionManager, null, null, null);
        assertTrue(defaultAttachmentService.userHasAttachmentDeleteOwnPermission(mockIssue, testUser));
        controlPermissionManager.verify();
    }

    public void testUserDoesNotHaveAttachmentDeleteOwnPermission()
    {
        MockControl controlPermissionManager = MockControl.createControl(PermissionManager.class);
        PermissionManager mockPermissionManager = (PermissionManager) controlPermissionManager.getMock();
        mockPermissionManager.hasPermission(Permissions.ATTACHMENT_DELETE_OWN, mockIssue, testUser);
        controlPermissionManager.setReturnValue(false);
        controlPermissionManager.replay();

        DefaultAttachmentService defaultAttachmentService = new DefaultAttachmentService(null, mockPermissionManager, null, null, null);
        assertFalse(defaultAttachmentService.userHasAttachmentDeleteOwnPermission(mockIssue, testUser));
        controlPermissionManager.verify();
    }

    public void testUserHasCreateAttachmentPermission()
    {
        MockControl controlPermissionManager = MockControl.createControl(PermissionManager.class);
        PermissionManager mockPermissionManager = (PermissionManager) controlPermissionManager.getMock();
        mockPermissionManager.hasPermission(Permissions.CREATE_ATTACHMENT, mockIssue, testUser);
        controlPermissionManager.setReturnValue(true);
        controlPermissionManager.replay();

        DefaultAttachmentService defaultAttachmentService = new DefaultAttachmentService(null, mockPermissionManager, null, null, null);
        assertTrue(defaultAttachmentService.userHasCreateAttachmentPermission(mockIssue, testUser));
        controlPermissionManager.verify();
    }

    public void testUserDoesNotHaveCreateAttachmentPermission()
    {
        MockControl controlPermissionManager = MockControl.createControl(PermissionManager.class);
        PermissionManager mockPermissionManager = (PermissionManager) controlPermissionManager.getMock();
        mockPermissionManager.hasPermission(Permissions.CREATE_ATTACHMENT, mockIssue, testUser);
        controlPermissionManager.setReturnValue(false);
        controlPermissionManager.replay();

        DefaultAttachmentService defaultAttachmentService = new DefaultAttachmentService(null, mockPermissionManager, null, null, null);
        assertFalse(defaultAttachmentService.userHasCreateAttachmentPermission(mockIssue, testUser));
        controlPermissionManager.verify();
    }

    public void testCanDeleteAttachmentHappyPathWithPopulatedErrorCollectionPassedIn()
    {
        final Attachment mockAttachment = new MockAttachment()
        {
            public Issue getIssueObject()
            {
                return mockIssue;
            }
        };

        DefaultAttachmentService defaultAttachmentService = new DefaultAttachmentService(null, null, jiraAuthenticationContext, null, null)
        {
            boolean userHasAttachmentDeleteAllPermission(Issue issue, User user)
            {
                return true;
            }

            Attachment getAndVerifyAttachment(Long attachmentId, ErrorCollection errorCollection)
            {
                return mockAttachment;
            }

            Issue getAndVerifyIssue(Attachment attachment, ErrorCollection errorCollection)
            {
                return mockIssue;
            }

            boolean isIssueInEditableWorkflowState(Issue issue)
            {
                return true;
            }
        };

        //error messages passed in shall not stray us from the happy path!
        jiraServiceContext.getErrorCollection().addErrorMessage(TEST_ERROR_MESSAGE);

        assertTrue(defaultAttachmentService.canDeleteAttachment(jiraServiceContext, ATTACHMENT_ID));
        assertEquals(1, jiraServiceContext.getErrorCollection().getErrorMessages().size());
        assertEquals(TEST_ERROR_MESSAGE, jiraServiceContext.getErrorCollection().getErrorMessages().iterator().next());
    }

    public void testCanDeleteAttachmentWithDeleteAllPermission()
    {
        final Attachment mockAttachment = new MockAttachment()
        {
            public Issue getIssueObject()
            {
                return mockIssue;
            }
        };

        DefaultAttachmentService defaultAttachmentService = new DefaultAttachmentService(null, null, jiraAuthenticationContext, null, null)
        {
            boolean userHasAttachmentDeleteAllPermission(Issue issue, User user)
            {
                return true;
            }

            Attachment getAndVerifyAttachment(Long attachmentId, ErrorCollection errorCollection)
            {
                return mockAttachment;
            }

            Issue getAndVerifyIssue(Attachment attachment, ErrorCollection errorCollection)
            {
                return mockIssue;
            }

            boolean isIssueInEditableWorkflowState(Issue issue)
            {
                return true;
            }
        };

        assertTrue(defaultAttachmentService.canDeleteAttachment(jiraServiceContext, ATTACHMENT_ID));
        assertFalse(errorCollection.hasAnyErrors());
    }

    public void testCanDeleteAttachmentWithDeleteAllPermissionIssueNotInEditableWorkflowState()
    {
        final Attachment mockAttachment = new MockAttachment()
        {
            public Issue getIssueObject()
            {
                return mockIssue;
            }
        };

        DefaultAttachmentService defaultAttachmentService = new DefaultAttachmentService(null, null, jiraAuthenticationContext, null, null)
        {
            boolean isIssueInEditableWorkflowState(Issue issue)
            {
                return false;
            }

            boolean userHasAttachmentDeleteAllPermission(Issue issue, User user)
            {
                return true;
            }

            boolean userHasAttachmentDeleteOwnPermission(Issue issue, User user)
            {
                return false;
            }

            boolean isUserAttachmentAuthor(Attachment attachment, User user)
            {
                return false;
            }

            Attachment getAndVerifyAttachment(Long attachmentId, ErrorCollection errorCollection)
            {
                return mockAttachment;
            }

            Issue getAndVerifyIssue(Attachment attachment, ErrorCollection errorCollection)
            {
                return mockIssue;
            }
        };

        assertFalse(defaultAttachmentService.canDeleteAttachment(jiraServiceContext, ATTACHMENT_ID));
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("You can not delete the attachment as the issue is in a non-editable workflow state.", errorCollection.getErrorMessages().iterator().next());
    }

    public void testCanDeleteAttachmentWithDeleteOwnPermissionAndDoesOwnAttachment()
    {
        final Attachment mockAttachment = new MockAttachment()
        {
            public Issue getIssueObject()
            {
                return mockIssue;
            }
        };

        DefaultAttachmentService defaultAttachmentService = new DefaultAttachmentService(null, null, jiraAuthenticationContext, null, null)
        {
            boolean userHasAttachmentDeleteAllPermission(Issue issue, User user)
            {
                return false;
            }

            boolean userHasAttachmentDeleteOwnPermission(Issue issue, User user)
            {
                return true;
            }

            boolean isUserAttachmentAuthor(Attachment attachment, User user)
            {
                return true;
            }

            Attachment getAndVerifyAttachment(Long attachmentId, ErrorCollection errorCollection)
            {
                return mockAttachment;
            }

            Issue getAndVerifyIssue(Attachment attachment, ErrorCollection errorCollection)
            {
                return mockIssue;
            }

            boolean isIssueInEditableWorkflowState(Issue issue)
            {
                return true;
            }
        };

        assertTrue(defaultAttachmentService.canDeleteAttachment(jiraServiceContext, ATTACHMENT_ID));
        assertFalse(errorCollection.hasAnyErrors());
    }

    public void testCanDeleteAttachmentWithDeleteOwnPermissionAndDoesntOwnAttachment()
    {
        final Attachment mockAttachment = new MockAttachment()
        {
            public Issue getIssueObject()
            {
                return mockIssue;
            }
        };

        DefaultAttachmentService defaultAttachmentService = new DefaultAttachmentService(null, null, jiraAuthenticationContext, null, null)
        {
            boolean userHasAttachmentDeleteAllPermission(Issue issue, User user)
            {
                return false;
            }

            boolean userHasAttachmentDeleteOwnPermission(Issue issue, User user)
            {
                return true;
            }

            boolean isUserAttachmentAuthor(Attachment attachment, User user)
            {
                return false;
            }

            Attachment getAndVerifyAttachment(Long attachmentId, ErrorCollection errorCollection)
            {
                return mockAttachment;
            }

            Issue getAndVerifyIssue(Attachment attachment, ErrorCollection errorCollection)
            {
                return mockIssue;
            }

            boolean isIssueInEditableWorkflowState(Issue issue)
            {
                return true;
            }
        };

        assertFalse(defaultAttachmentService.canDeleteAttachment(jiraServiceContext, ATTACHMENT_ID));
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("You do not have permission to delete attachment with id: 1", errorCollection.getErrorMessages().iterator().next());
    }

    public void testGetAndVerifyAttachmentWithNullId()
    {
        DefaultAttachmentService defaultAttachmentService = new DefaultAttachmentService(null, null, jiraAuthenticationContext, null, null);
        assertNull(defaultAttachmentService.getAndVerifyAttachment(null, errorCollection));
        assertTrue(errorCollection.hasAnyErrors());
        assertEquals(1, errorCollection.getErrorMessages().size());
        String errorMessage = (String) errorCollection.getErrorMessages().iterator().next();
        assertEquals("Can not resolve attachment for null id.", errorMessage);
    }

    public void testGetAndVerifyAttachmentWithNullAttachment()
    {
        Mock mockAttachmentManager = new Mock(AttachmentManager.class);
        mockAttachmentManager.expectAndReturn("getAttachment", P.args(new IsEqual(ATTACHMENT_ID)), null);

        DefaultAttachmentService defaultAttachmentService = new DefaultAttachmentService((AttachmentManager) mockAttachmentManager.proxy(), null, jiraAuthenticationContext, null, null);
        assertNull(defaultAttachmentService.getAndVerifyAttachment(ATTACHMENT_ID, errorCollection));
        assertTrue(errorCollection.hasAnyErrors());
        assertEquals(1, errorCollection.getErrorMessages().size());
        String errorMessage = (String) errorCollection.getErrorMessages().iterator().next();
        assertEquals("Can not resolve attachment for id 1.", errorMessage);
    }

    public void testGetAndVerifyIssueWithNullIssue()
    {
        final Attachment mockAttachment = new MockAttachment()
        {
            public Issue getIssueObject()
            {
                return null;
            }

            public Long getId()
            {
                return ATTACHMENT_ID;
            }
        };

        DefaultAttachmentService defaultAttachmentService = new DefaultAttachmentService(null, null, jiraAuthenticationContext, null, null);
        assertNull(defaultAttachmentService.getAndVerifyIssue(mockAttachment, errorCollection));
        assertTrue(errorCollection.hasAnyErrors());
        assertEquals(1, errorCollection.getErrorMessages().size());
        String errorMessage = (String) errorCollection.getErrorMessages().iterator().next();
        assertEquals("There is not an issue associated with attachment with id 1.", errorMessage);
    }

    public void testDeleteHappyPath()
    {
        final Attachment mockAttachment = new MockAttachment();

        Mock mockAttachmentManager = new Mock(AttachmentManager.class);
        mockAttachmentManager.expectVoid("deleteAttachment", P.ANY_ARGS);

        Mock mockIssueUpdater = new Mock(IssueUpdater.class);
        mockIssueUpdater.expectVoid("doUpdate", P.ANY_ARGS);

        DefaultAttachmentService defaultAttachmentService = new DefaultAttachmentService((AttachmentManager) mockAttachmentManager.proxy(), null, jiraAuthenticationContext, (IssueUpdater) mockIssueUpdater.proxy(), null)
        {
            IssueUpdateBean constructIssueUpdateBeanForAttachmentDelete(Attachment attachment, Issue issue, User user)
            {
                return new IssueUpdateBean(null, null, null, null);
            }

            Attachment getAndVerifyAttachment(Long attachmentId, ErrorCollection errorCollection)
            {
                return mockAttachment;
            }

            Issue getAndVerifyIssue(Attachment attachment, ErrorCollection errorCollection)
            {
                return mockIssue;
            }
        };

        defaultAttachmentService.delete(jiraServiceContext, ATTACHMENT_ID);
        assertFalse(errorCollection.hasAnyErrors());
        mockAttachmentManager.verify();
        mockIssueUpdater.verify();
    }

    public void testDeleteAttachmentManagerThrowsException()
    {
        final Attachment mockAttachment = new MockAttachment();

        Mock mockAttachmentManager = new Mock(AttachmentManager.class);
        mockAttachmentManager.expectAndThrow("deleteAttachment", P.ANY_ARGS, new RemoveException());

        DefaultAttachmentService defaultAttachmentService = new DefaultAttachmentService((AttachmentManager) mockAttachmentManager.proxy(), null, jiraAuthenticationContext, null, null)
        {
            Attachment getAndVerifyAttachment(Long attachmentId, ErrorCollection errorCollection)
            {
                return mockAttachment;
            }

            Issue getAndVerifyIssue(Attachment attachment, ErrorCollection errorCollection)
            {
                return mockIssue;
            }
        };

        defaultAttachmentService.delete(jiraServiceContext, ATTACHMENT_ID);
        assertTrue(errorCollection.hasAnyErrors());
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("Failed to delete attachment with id 1.", errorCollection.getErrorMessages().iterator().next());
        mockAttachmentManager.verify();
    }

    public void testDeleteIssueUpdaterThrowsException()
    {
        final Attachment mockAttachment = new MockAttachment();

        Mock mockAttachmentManager = new Mock(AttachmentManager.class);
        mockAttachmentManager.expectVoid("deleteAttachment", P.ANY_ARGS);

        Mock mockIssueUpdater = new Mock(IssueUpdater.class);
        mockIssueUpdater.expectAndThrow("doUpdate", P.ANY_ARGS, new JiraException());

        DefaultAttachmentService defaultAttachmentService = new DefaultAttachmentService((AttachmentManager) mockAttachmentManager.proxy(), null, jiraAuthenticationContext, (IssueUpdater) mockIssueUpdater.proxy(), null)
        {
            IssueUpdateBean constructIssueUpdateBeanForAttachmentDelete(Attachment attachment, Issue issue, User user)
            {
                return new IssueUpdateBean(null, null, null, null);
            }

            Attachment getAndVerifyAttachment(Long attachmentId, ErrorCollection errorCollection)
            {
                return mockAttachment;
            }

            Issue getAndVerifyIssue(Attachment attachment, ErrorCollection errorCollection)
            {
                return mockIssue;
            }
        };

        //set key for error message
        ((MockIssue) mockIssue).setKey("TST-1");

        defaultAttachmentService.delete(jiraServiceContext, ATTACHMENT_ID);
        assertTrue(errorCollection.hasAnyErrors());
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("Failed to update change history for issue with key TST-1 for the delete attachment operation.", errorCollection.getErrorMessages().iterator().next());
        mockAttachmentManager.verify();
        mockIssueUpdater.verify();
    }

    public void testCanCreateAttachmentsNullIssue()
    {
        DefaultAttachmentService defaultAttachmentService = new DefaultAttachmentService(null, null, jiraAuthenticationContext, null, null);
        assertFalse(defaultAttachmentService.canCreateAttachments(jiraServiceContext, (Issue) null));
        assertTrue(errorCollection.hasAnyErrors());
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("Can not check attachment permissions without a specified issue.", errorCollection.getErrorMessages().iterator().next());
    }

    public void testCanCreateAttachmentsNullProject()
    {
        DefaultAttachmentService defaultAttachmentService = new DefaultAttachmentService(null, null, jiraAuthenticationContext, null, null);
        assertFalse(defaultAttachmentService.canCreateAttachments(jiraServiceContext, (Project) null));
        assertTrue(errorCollection.hasAnyErrors());
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("Can not check attachment permissions without a specified project.", errorCollection.getErrorMessages().iterator().next());
    }

    public void testCanCreateAttachmentsHappyPath()
    {
        DefaultAttachmentService defaultAttachmentService = new DefaultAttachmentService(null, null, null, null, null)
        {

            boolean isAttachmentsEnabledAndPathSet()
            {
                return true;
            }

            boolean userHasCreateAttachmentPermission(Issue issue, User user)
            {
                return true;
            }

            boolean isIssueInEditableWorkflowState(Issue issue)
            {
                return true;
            }
        };
        assertTrue(defaultAttachmentService.canCreateAttachments(jiraServiceContext, mockIssue));
    }

    public void testCanCreateAttachmentsForProjectHappyPath()
    {
        final PermissionManager mockPermissionManager = createMock(PermissionManager.class);
        expect(mockPermissionManager.hasPermission(Permissions.CREATE_ATTACHMENT, mockProject, jiraServiceContext.getLoggedInUser())).andReturn(true);
        replay(mockPermissionManager);
        DefaultAttachmentService defaultAttachmentService = new DefaultAttachmentService(null, mockPermissionManager, null, null, null)
        {

            boolean isAttachmentsEnabledAndPathSet()
            {
                return true;
            }

        };
        assertTrue(defaultAttachmentService.canCreateAttachments(jiraServiceContext, mockProject));
        verify(mockPermissionManager);
    }

    public void testCanCreateAttachmentsAttachmentsDisabled()
    {
        DefaultAttachmentService defaultAttachmentService = new DefaultAttachmentService(null, null, jiraAuthenticationContext, null, null)
        {

            boolean isAttachmentsEnabledAndPathSet()
            {
                return false;
            }

            boolean userHasCreateAttachmentPermission(Issue issue, User user)
            {
                return true;
            }

            boolean isIssueInEditableWorkflowState(Issue issue)
            {
                return true;
            }
        };

        assertFalse(defaultAttachmentService.canCreateAttachments(jiraServiceContext, mockIssue));
        assertEquals(1, jiraServiceContext.getErrorCollection().getErrorMessages().size());
        assertEquals("Attachments have been disabled for this instance of JIRA.", jiraServiceContext.getErrorCollection().getErrorMessages().iterator().next());
    }

    public void testCanCreateAttachmentsAttachmentsForProjectDisabled()
    {
        DefaultAttachmentService defaultAttachmentService = new DefaultAttachmentService(null, null, jiraAuthenticationContext, null, null)
        {

            boolean isAttachmentsEnabledAndPathSet()
            {
                return false;
            }

            boolean userHasCreateAttachmentPermission(Issue issue, User user)
            {
                return true;
            }

            boolean isIssueInEditableWorkflowState(Issue issue)
            {
                return true;
            }
        };

        assertFalse(defaultAttachmentService.canCreateAttachments(jiraServiceContext, mockProject));
        assertEquals(1, jiraServiceContext.getErrorCollection().getErrorMessages().size());
        assertEquals("Attachments have been disabled for this instance of JIRA.", jiraServiceContext.getErrorCollection().getErrorMessages().iterator().next());
    }

    public void testCanCreateAttachmentsNoCreateAttachmentPermission()
    {
        DefaultAttachmentService defaultAttachmentService = new DefaultAttachmentService(null, null, jiraAuthenticationContext, null, null)
        {

            boolean isAttachmentsEnabledAndPathSet()
            {
                return true;
            }

            boolean userHasCreateAttachmentPermission(Issue issue, User user)
            {
                return false;
            }

            boolean isIssueInEditableWorkflowState(Issue issue)
            {
                return true;
            }
        };
        assertFalse(defaultAttachmentService.canCreateAttachments(jiraServiceContext, mockIssue));
        assertEquals(1, jiraServiceContext.getErrorCollection().getErrorMessages().size());
        assertEquals("You do not have permission to create attachments for this issue.", jiraServiceContext.getErrorCollection().getErrorMessages().iterator().next());
    }

    public void testCanCreateAttachmentsNoCreateAttachmentPermissionForProject()
    {
        final PermissionManager mockPermissionManager = createMock(PermissionManager.class);
        expect(mockPermissionManager.hasPermission(Permissions.CREATE_ATTACHMENT, mockProject, jiraServiceContext.getLoggedInUser())).andReturn(false);
        replay(mockPermissionManager);
        DefaultAttachmentService defaultAttachmentService = new DefaultAttachmentService(null, mockPermissionManager, jiraAuthenticationContext, null, null)
        {
            boolean isAttachmentsEnabledAndPathSet()
            {
                return true;
            }
        };
        assertFalse(defaultAttachmentService.canCreateAttachments(jiraServiceContext, mockProject));
        assertEquals(1, jiraServiceContext.getErrorCollection().getErrorMessages().size());
        assertEquals("You do not have permission to create attachments for this project.", jiraServiceContext.getErrorCollection().getErrorMessages().iterator().next());
        verify(mockPermissionManager);
    }

    public void testCanCreateAttachmentsNotInEditableWorkflowState()
    {
        DefaultAttachmentService defaultAttachmentService = new DefaultAttachmentService(null, null, jiraAuthenticationContext, null, null)
        {

            boolean isAttachmentsEnabledAndPathSet()
            {
                return true;
            }

            boolean userHasCreateAttachmentPermission(Issue issue, User user)
            {
                return true;
            }

            boolean isIssueInEditableWorkflowState(Issue issue)
            {
                return false;
            }
        };
        assertFalse(defaultAttachmentService.canCreateAttachments(jiraServiceContext, mockIssue));
        assertEquals(1, jiraServiceContext.getErrorCollection().getErrorMessages().size());
        assertEquals("You can not create attachments as the issue is in a non-editable workflow state.", jiraServiceContext.getErrorCollection().getErrorMessages().iterator().next());
    }

    public void testCanAttachScreenshotsHappyPath()
    {
        DefaultAttachmentService defaultAttachmentService = new DefaultAttachmentService(null, null, jiraAuthenticationContext, null, null)
        {
            public boolean canCreateAttachments(JiraServiceContext jiraServiceContext, Issue issue)
            {
                return true;
            }

            boolean isScreenshotAppletEnabledAndSupportedByOS(JiraServiceContext jiraServiceContext)
            {
                return true;
            }
        };
        assertTrue(defaultAttachmentService.canAttachScreenshots(jiraServiceContext, mockIssue));
    }

    public void testCanAttachScreenshotsCantCreateAttachments()
    {
        DefaultAttachmentService defaultAttachmentService = new DefaultAttachmentService(null, null, null, null, null)
        {
            public boolean canCreateAttachments(JiraServiceContext jiraServiceContext, Issue issue)
            {
                return false;
            }

            boolean isScreenshotAppletEnabledAndSupportedByOS(JiraServiceContext jiraServiceContext)
            {
                return true;
            }
        };
        assertFalse(defaultAttachmentService.canAttachScreenshots(jiraServiceContext, mockIssue));
    }

    public void testCanAttachScreenshotsScreenshotAppletDisabled()
    {
        DefaultAttachmentService defaultAttachmentService = new DefaultAttachmentService(null, null, null, null, null)
        {
            public boolean canCreateAttachments(JiraServiceContext jiraServiceContext, Issue issue)
            {
                return true;
            }

            boolean isScreenshotAppletEnabledAndSupportedByOS(JiraServiceContext jiraServiceContext)
            {
                return false;
            }
        };
        assertFalse(defaultAttachmentService.canAttachScreenshots(jiraServiceContext, mockIssue));
    }

    public void testCanManageAttachmentsHappyPathWithPopulatedErrorCollectionPassedIn()
    {
        DefaultAttachmentService defaultAttachmentService = new DefaultAttachmentService(null, null, null, null, null)
        {

            boolean canDeleteAnyAttachment(User user, Issue issue, ErrorCollection errorCollection)
            {
                return true;
            }

            boolean userHasCreateAttachmentPermission(Issue issue, User user)
            {
                return true;
            }

            boolean isAttachmentsEnabledAndPathSet()
            {
                return true;
            }
        };

        //error messages passed in shall not stray us from the happy path!
        jiraServiceContext.getErrorCollection().addErrorMessage(TEST_ERROR_MESSAGE);

        JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(testUser, errorCollection);
        assertTrue(defaultAttachmentService.canManageAttachments(jiraServiceContext, mockIssue));
        assertEquals(1, jiraServiceContext.getErrorCollection().getErrorMessages().size());
        assertEquals(TEST_ERROR_MESSAGE, jiraServiceContext.getErrorCollection().getErrorMessages().iterator().next());
    }

    public void testCanManageAttachmentsFullPermissionsHappyPath()
    {
        DefaultAttachmentService defaultAttachmentService = new DefaultAttachmentService(null, null, null, null, null)
        {

            boolean canDeleteAnyAttachment(User user, Issue issue, ErrorCollection errorCollection)
            {
                return true;
            }

            boolean userHasCreateAttachmentPermission(Issue issue, User user)
            {
                return true;
            }

            boolean isAttachmentsEnabledAndPathSet()
            {
                return true;
            }
        };
        JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(testUser, errorCollection);
        assertTrue(defaultAttachmentService.canManageAttachments(jiraServiceContext, mockIssue));
    }

    public void testCanManageAttachmentsCreatePermissionHappyPath()
    {
        DefaultAttachmentService defaultAttachmentService = new DefaultAttachmentService(null, null, null, null, null)
        {

            boolean canDeleteAnyAttachment(User user, Issue issue, ErrorCollection errorCollection)
            {
                return false;
            }

            boolean userHasCreateAttachmentPermission(Issue issue, User user)
            {
                return true;
            }

            boolean isAttachmentsEnabledAndPathSet()
            {
                return true;
            }
        };
        JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(testUser, errorCollection);
        assertTrue(defaultAttachmentService.canManageAttachments(jiraServiceContext, mockIssue));
    }

    public void testCanManageAttachmentsDeletePermissionHappyPath()
    {
        DefaultAttachmentService defaultAttachmentService = new DefaultAttachmentService(null, null, null, null, null)
        {
            boolean canDeleteAnyAttachment(User user, Issue issue, ErrorCollection errorCollection)
            {
                return true;
            }

            boolean userHasCreateAttachmentPermission(Issue issue, User user)
            {
                return false;
            }

            boolean isAttachmentsEnabledAndPathSet()
            {
                return true;
            }
        };
        JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(testUser, errorCollection);
        assertTrue(defaultAttachmentService.canManageAttachments(jiraServiceContext, mockIssue));
    }

    public void testCanManageAttachmentsNoAttachmentPermissions()
    {
        DefaultAttachmentService defaultAttachmentService = new DefaultAttachmentService(null, null, jiraAuthenticationContext, null, null)
        {
            boolean canDeleteAnyAttachment(User user, Issue issue, ErrorCollection errorCollection)
            {
                return false;
            }

            boolean userHasCreateAttachmentPermission(Issue issue, User user)
            {
                return false;
            }

            boolean isAttachmentsEnabledAndPathSet()
            {
                return true;
            }
        };
        JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(testUser, errorCollection);
        assertFalse(defaultAttachmentService.canManageAttachments(jiraServiceContext, mockIssue));
        assertEquals(1, jiraServiceContext.getErrorCollection().getErrorMessages().size());
        assertEquals("You do not have permission to manage the attachments for this issue.", jiraServiceContext.getErrorCollection().getErrorMessages().iterator().next());
    }

    public void testCanManageAttachmentsAttachmentsDisabled()
    {
        DefaultAttachmentService defaultAttachmentService = new DefaultAttachmentService(null, null, jiraAuthenticationContext, null, null)
        {
            boolean canDeleteAnyAttachment(User user, Issue issue, ErrorCollection errorCollection)
            {
                return true;
            }

            boolean userHasCreateAttachmentPermission(Issue issue, User user)
            {
                return true;
            }

            boolean isAttachmentsEnabledAndPathSet()
            {
                return false;
            }
        };
        JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(testUser, errorCollection);
        assertFalse(defaultAttachmentService.canManageAttachments(jiraServiceContext, mockIssue));
        assertEquals(1, jiraServiceContext.getErrorCollection().getErrorMessages().size());
        assertEquals("Attachments have been disabled for this instance of JIRA.", jiraServiceContext.getErrorCollection().getErrorMessages().iterator().next());
    }

    public void testIsScreenshotAppletEnabledAndSupportedByOSHappyPath()
    {
        Mock mockAttachmentManager = new Mock(AttachmentManager.class);
        mockAttachmentManager.expectAndReturn("isScreenshotAppletEnabled", P.ANY_ARGS, Boolean.TRUE);
        mockAttachmentManager.expectAndReturn("isScreenshotAppletSupportedByOS", P.ANY_ARGS, Boolean.TRUE);

        DefaultAttachmentService defaultAttachmentService = new DefaultAttachmentService((AttachmentManager) mockAttachmentManager.proxy(), null, jiraAuthenticationContext, null, null);
        JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(testUser, errorCollection);
        assertTrue(defaultAttachmentService.isScreenshotAppletEnabledAndSupportedByOS(jiraServiceContext));
        assertFalse(jiraServiceContext.getErrorCollection().hasAnyErrors());
    }

    public void testIsScreenshotAppletEnabledAndSupportedByOSAppletDisabled()
    {
        Mock mockAttachmentManager = new Mock(AttachmentManager.class);
        mockAttachmentManager.expectAndReturn("isScreenshotAppletEnabled", P.ANY_ARGS, Boolean.FALSE);
        mockAttachmentManager.expectAndReturn("isScreenshotAppletSupportedByOS", P.ANY_ARGS, Boolean.TRUE);

        DefaultAttachmentService defaultAttachmentService = new DefaultAttachmentService((AttachmentManager) mockAttachmentManager.proxy(), null, jiraAuthenticationContext, null, null);
        JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(testUser, errorCollection);
        assertFalse(defaultAttachmentService.isScreenshotAppletEnabledAndSupportedByOS(jiraServiceContext));
        assertEquals(1, jiraServiceContext.getErrorCollection().getErrorMessages().size());
        assertEquals("The screenshot applet has been disabled for this instance of JIRA.", jiraServiceContext.getErrorCollection().getErrorMessages().iterator().next());
    }

    public void testIsScreenshotAppletEnabledAndSupportedByOSUnsupportedOS()
    {
        Mock mockAttachmentManager = new Mock(AttachmentManager.class);
        mockAttachmentManager.expectAndReturn("isScreenshotAppletEnabled", P.ANY_ARGS, Boolean.TRUE);
        mockAttachmentManager.expectAndReturn("isScreenshotAppletSupportedByOS", P.ANY_ARGS, Boolean.FALSE);

        DefaultAttachmentService defaultAttachmentService = new DefaultAttachmentService((AttachmentManager) mockAttachmentManager.proxy(), null, jiraAuthenticationContext, null, null);
        JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(testUser, errorCollection);
        assertFalse(defaultAttachmentService.isScreenshotAppletEnabledAndSupportedByOS(jiraServiceContext));
        assertEquals(1, jiraServiceContext.getErrorCollection().getErrorMessages().size());
        assertEquals("The JIRA screenshot applet does not support your client operating system.", jiraServiceContext.getErrorCollection().getErrorMessages().iterator().next());
    }

    // This is stupid, but needed. For some reason, seemingly related to
    // the Attachment class taking a GenericValue in its constructor, a mock
    // of the Attachment class seems to fail, only under windows. This class
    // allows the mock to use a no-arg constructor and to bs the genericValue.
    private class MockAttachment extends Attachment
    {
        public MockAttachment()
        {
            super(null, new MockGenericValue("AttachmentMock"));
        }
    }
}
