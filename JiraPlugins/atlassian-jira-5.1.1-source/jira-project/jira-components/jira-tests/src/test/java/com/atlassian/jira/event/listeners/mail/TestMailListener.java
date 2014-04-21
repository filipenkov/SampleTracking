package com.atlassian.jira.event.listeners.mail;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.event.ListenerManager;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.event.type.EventTypeManager;
import com.atlassian.jira.event.user.UserEvent;
import com.atlassian.jira.event.user.UserEventType;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.local.AbstractUsersTestCase;
import com.atlassian.jira.mail.Email;
import com.atlassian.jira.mail.IssueMailQueueItem;
import com.atlassian.jira.mail.IssueMailQueueItemFactory;
import com.atlassian.jira.mock.event.MockMailListener;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.notification.NotificationTypeManager;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.preferences.JiraUserPreferences;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.mail.MailFactory;
import com.atlassian.mail.queue.MailQueueItem;
import com.atlassian.mail.server.MailServerManager;
import com.atlassian.mail.server.SMTPMailServer;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import mock.MockComment;

import org.easymock.MockControl;
import org.ofbiz.core.entity.GenericValue;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;

public class TestMailListener extends AbstractUsersTestCase
{
    private MockMailListener mailListener;
    private NotificationSchemeManager notificationSchemeManager;
    private GenericValue scheme;
    private GenericValue project;
    private IssueEvent issueEvent;
    private Mock mailServer;
    private User testUser;

    private PermissionManager mockPermissionManager;
    private MockControl ctrlPermissionManager;
    private GenericValue issue;
    private Issue issueObject;
    private IssueMailQueueItemFactory issueMailQueueItemFactory;
    private UserManager userManager;

    public TestMailListener(final String s)
    {
        super(s);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        final Mock mockListenerManager = new Mock(ListenerManager.class);
        ManagerFactory.addService(ListenerManager.class, (ListenerManager) mockListenerManager.proxy());

        issueMailQueueItemFactory = ComponentManager.getComponentInstanceOfType(IssueMailQueueItemFactory.class);

        userManager = createMock(UserManager.class);

        mailListener = new MockMailListener(null, null, userManager);
        mailListener.setSendMessage(false);

        mockListenerManager.setupResult("getListeners", EasyList.build(mailListener));

        testUser = createMockUser("test user", "test user fullname", "test@atlassian.com");
        new JiraUserPreferences(testUser).setBoolean(PreferenceKeys.USER_NOTIFY_OWN_CHANGES, true);

        notificationSchemeManager = ManagerFactory.getNotificationSchemeManager();
        scheme = notificationSchemeManager.createScheme("Test Scheme", null);
        project = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(10)));
        ManagerFactory.getNotificationSchemeManager().addSchemeToProject(project, scheme);
        notificationSchemeManager.createSchemeEntity(scheme, new SchemeEntity("Current_Assignee", "dave@atlassian.com", EventType.ISSUE_DELETED_ID));

        final Map issueProperties = EasyMap.build("id", new Long(20), "project", new Long(10), "key", "issue key", "summary", "issue summary",
            "created", new Timestamp(new Date().getTime()), "updated", new Timestamp(new Date().getTime()), "type", new Long(1));
        issueProperties.put("assignee", "dave");
        issueProperties.put("status", "assigned");

        issue = UtilsForTests.getTestEntity("Issue", issueProperties);

        // Create the issue object
        final IssueFactory issueFactory = ComponentManager.getComponentInstanceOfType(IssueFactory.class);
        issueObject = issueFactory.getIssue(issue);

        issueEvent = new IssueEvent(issueObject, testUser, createMockComment(), null, null, EasyMap.build("", ""), EventType.ISSUE_CREATED_ID);

        final Mock mailServerManager = new Mock(MailServerManager.class);
        MailFactory.setServerManager((MailServerManager) mailServerManager.proxy());
        mailServer = new Mock(SMTPMailServer.class);

        mailServerManager.setupResult("getDefaultSMTPMailServer", mailServer.proxy());

        // Add permissions manager
        ctrlPermissionManager = MockControl.createControl(PermissionManager.class);
        mockPermissionManager = (PermissionManager) ctrlPermissionManager.getMock();

        ManagerFactory.addService(PermissionManager.class, mockPermissionManager);

        ComponentAccessor.getApplicationProperties().setString(APKeys.JIRA_WEBWORK_ENCODING, "UTF-8");

    }

    @Override
    protected void tearDown() throws Exception
    {
        ManagerFactory.removeService(NotificationTypeManager.class);

        MailFactory.refresh();

        mailListener = null;
        notificationSchemeManager = null;
        scheme = null;
        project = null;
        issueEvent = null;
        mailServer = null;
        testUser = null;
        mockPermissionManager = null;
        ctrlPermissionManager = null;
        issue = null;
        issueObject = null;
        issueMailQueueItemFactory = null;

        super.tearDown();
    }

    public void testIsInternal()
    {
        assertTrue(mailListener.isInternal());
    }

    public void testIssueSendNotifications_IssueCreated() throws Exception
    {
        issueEvent = new IssueEvent(issueObject, testUser, createMockComment(), null, null, EasyMap.build("", ""), EventType.ISSUE_CREATED_ID);
        mailListener.issueCreated(issueEvent);
        assertEquals(EventType.ISSUE_CREATED_ID, mailListener.getEventTypeIDCalled());
    }

    public void testIssueSendNotifications_IssueAssigned() throws Exception
    {
        issueEvent = new IssueEvent(issueObject, testUser, createMockComment(), null, null, EasyMap.build("", ""), EventType.ISSUE_ASSIGNED_ID);
        mailListener.issueAssigned(issueEvent);
        assertEquals(EventType.ISSUE_ASSIGNED_ID, mailListener.getEventTypeIDCalled());
    }

    public void testIssueSendNotifications_IssueClosed() throws Exception
    {
        issueEvent = new IssueEvent(issueObject, testUser, createMockComment(), null, null, EasyMap.build("", ""), EventType.ISSUE_CLOSED_ID);
        mailListener.issueClosed(issueEvent);
        assertEquals(EventType.ISSUE_CLOSED_ID, mailListener.getEventTypeIDCalled());
    }

    public void testIssueSendNotifications_IssueResolved() throws Exception
    {
        issueEvent = new IssueEvent(issueObject, testUser, createMockComment(), null, null, EasyMap.build("", ""), EventType.ISSUE_RESOLVED_ID);
        mailListener.issueResolved(issueEvent);
        assertEquals(EventType.ISSUE_RESOLVED_ID, mailListener.getEventTypeIDCalled());
    }

    public void testIssueSendNotifications_IssueReopened() throws Exception
    {
        issueEvent = new IssueEvent(issueObject, testUser, createMockComment(), null, null, EasyMap.build("", ""), EventType.ISSUE_REOPENED_ID);
        mailListener.issueReopened(issueEvent);
        assertEquals(EventType.ISSUE_REOPENED_ID, mailListener.getEventTypeIDCalled());
    }

    public void testIssueSendNotifications_IssueUpdated() throws Exception
    {
        issueEvent = new IssueEvent(issueObject, testUser, createMockComment(), null, null, EasyMap.build("", ""), EventType.ISSUE_UPDATED_ID);
        mailListener.issueUpdated(issueEvent);
        assertEquals(EventType.ISSUE_UPDATED_ID, mailListener.getEventTypeIDCalled());
    }

    public void testIssueSendNotifications_IssueCommented() throws Exception
    {
        issueEvent = new IssueEvent(issueObject, testUser, createMockComment(), null, null, EasyMap.build("", ""), EventType.ISSUE_COMMENTED_ID);
        mailListener.issueCommented(issueEvent);
        assertEquals(EventType.ISSUE_COMMENTED_ID, mailListener.getEventTypeIDCalled());
    }

    public void testIssueSendNotifications_IssueWorkLogged() throws Exception
    {
        issueEvent = new IssueEvent(issueObject, testUser, createMockComment(), null, null, EasyMap.build("", ""), EventType.ISSUE_WORKLOGGED_ID);
        mailListener.issueWorkLogged(issueEvent);
        assertEquals(EventType.ISSUE_WORKLOGGED_ID, mailListener.getEventTypeIDCalled());
    }

    public void testIssueSendNotifications_IssueDeleted() throws Exception
    {
        issueEvent = new IssueEvent(issueObject, testUser, createMockComment(), null, null, EasyMap.build("", ""), EventType.ISSUE_DELETED_ID);
        mailListener.issueDeleted(issueEvent);
        assertEquals(EventType.ISSUE_DELETED_ID, mailListener.getEventTypeIDCalled());
    }

    public void testIssueSendNotifications_IssueWorkStarted() throws Exception
    {
        issueEvent = new IssueEvent(issueObject, testUser, createMockComment(), null, null, EasyMap.build("", ""), EventType.ISSUE_WORKSTARTED_ID);
        mailListener.issueStarted(issueEvent);
        assertEquals(EventType.ISSUE_WORKSTARTED_ID, mailListener.getEventTypeIDCalled());
    }

    public void testIssueSendNotifications_IssueWorkStopped() throws Exception
    {
        issueEvent = new IssueEvent(issueObject, testUser, createMockComment(), null, null, EasyMap.build("", ""), EventType.ISSUE_WORKSTOPPED_ID);
        mailListener.issueStopped(issueEvent);
        assertEquals(EventType.ISSUE_WORKSTOPPED_ID, mailListener.getEventTypeIDCalled());
    }

    public void testUserSendNotification1() throws Exception
    {
        final Email email = (Email) new Email("test@atlassian.com").setSubject("Account signup");
        final UserEvent ue = new UserEvent(testUser, UserEventType.USER_SIGNUP);
        mailServer.expectVoid("send", P.args(new IsEqual(email)));

        mailListener.userSignup(ue);
        assertEquals("Account signup", mailListener.getUserMailCalled());
        ManagerFactory.getMailQueue().sendBuffer();
        mailServer.verify();
    }

    public void testUserSendNotificationForUserCreatedEvent() throws Exception
    {
        assertUserCreatedEvent(false);
        assertUserCreatedEvent(true);
    }

    private void assertUserCreatedEvent(boolean canUpdateUserPassword)
    {
        final UserEvent ue = new UserEvent(testUser, UserEventType.USER_SIGNUP);

        reset(userManager);
        expect(userManager.canUpdateUserPassword(testUser)).andReturn(canUpdateUserPassword);
        replay(userManager);

        mailListener.userCreated(ue);

        final Email email = (Email) new Email("test@atlassian.com").setSubject("Account created").setMimeType("text/html");
        mailServer.expectVoid("send", P.args(new IsEqual(email)));
        assertEquals("Account created", mailListener.getUserMailCalled());

        ManagerFactory.getMailQueue().sendBuffer();
        mailServer.verify();
        verify(userManager);
    }

    public void testUserSendNotification3() throws Exception
    {
        final Email email = (Email) new Email("test@atlassian.com").setSubject("Account password");
        final UserEvent ue = new UserEvent(testUser, UserEventType.USER_SIGNUP);
        mailServer.expectVoid("send", P.args(new IsEqual(email)));

        mailListener.userForgotPassword(ue);
        assertEquals("Account password", mailListener.getUserMailCalled());
        ManagerFactory.getMailQueue().sendBuffer();
        mailServer.verify();
    }

    // Test that mail is sent to correct list of recipients - i.e. a user will only recieve the first email notification
    // if there are multiple intended notification types for this user
    // For example, in this test, the user is both the assignee and the project lead - but the user will only receive one
    // email for the event.
    public void testCreateMailItems() throws Exception
    {
        mailListener = new MockMailListener(ComponentManager.getComponentInstanceOfType(NotificationSchemeManager.class), issueMailQueueItemFactory, null);
        mailListener.setSendMessage(true);

        ManagerFactory.addService(NotificationTypeManager.class, new NotificationTypeManager("notification-event-types.xml"));
        createMockUser("dave", "dave", "dave@atlassian.com");

        // Add an event type to the system
        final EventTypeManager eventTypeManager = ComponentAccessor.getEventTypeManager();
        final Map eventTypeParamasMap = EasyMap.build("id", new Long(1), "name", "Issue Created", "description",
            "This is the issue created event type descrition", "type", EventType.JIRA_SYSTEM_EVENT_TYPE);
        final GenericValue issueEventTypeGV = UtilsForTests.getTestEntity(EventType.EVENT_TYPE, eventTypeParamasMap);
        final EventType eventType = new EventType(issueEventTypeGV);
        eventTypeManager.addEventType(eventType);

        final GenericValue project = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(11), "lead", "dave"));
        notificationSchemeManager.addSchemeToProject(project, scheme);
        notificationSchemeManager.createSchemeEntity(scheme, new SchemeEntity("Current_Assignee", eventType.getId()));
        notificationSchemeManager.createSchemeEntity(scheme, new SchemeEntity("Project_Lead", eventType.getId()));

        issueEvent = new IssueEvent(issueObject, testUser, createMockComment(), null, null, EasyMap.build("", ""), eventType.getId());
        mailListener.issueCreated(issueEvent);

        final Email email = (Email) new Email("test@atlassian.com").setSubject("Account password");
        final UserEvent ue = new UserEvent(testUser, UserEventType.USER_SIGNUP);
        mailServer.expectVoid("send", P.args(new IsEqual(email)));

        final Collection<MailQueueItem> mailItems = ManagerFactory.getMailQueue().getQueue();
        assertTrue(mailItems.size() == 1);

        for (MailQueueItem mailItem : mailItems)
        {
            final IssueMailQueueItem mailQueueItem = (IssueMailQueueItem) mailItem;
            final Set<NotificationRecipient> recipientList = mailQueueItem.getRecipientList();
            assertTrue(mailQueueItem.getRecipientList().size() == 1);

            for (final NotificationRecipient recipient : recipientList)
            {
                assertEquals(recipient.getEmail(), "dave@atlassian.com");
            }
        }
    }

    private Comment createMockComment()
    {
        return new MockComment("John Citizen", "Some comment text");
    }

}
