/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.event.listeners.mail;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.user.UserUtils;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
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
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.preferences.JiraUserPreferences;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.mail.MailFactory;
import com.atlassian.mail.server.MailServerManager;
import com.atlassian.mail.server.SMTPMailServer;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import com.opensymphony.user.User;
import mock.MockComment;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import org.easymock.MockControl;
import org.ofbiz.core.entity.GenericValue;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class TestMailListener extends AbstractUsersTestCase
{
    private MockMailListener ml;
    private NotificationSchemeManager nsm;
    private GenericValue scheme;
    private GenericValue project;
    private IssueEvent ie;
    //    private GenericValue comment;
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

        ml = new MockMailListener(null, null, userManager);
        ml.setSendMessage(false);

        mockListenerManager.setupResult("getListeners", EasyList.build(ml));

        testUser = UserUtils.createUser("test user", "password", "test@atlassian.com", "test user fullname");
        new JiraUserPreferences(testUser).setBoolean(PreferenceKeys.USER_NOTIFY_OWN_CHANGES, true);

        //        ManagerFactory.addService(NotificationTypeManager.class, new NotificationTypeManager("test-notification-event-types.xml"));

        nsm = ManagerFactory.getNotificationSchemeManager();
        scheme = nsm.createScheme("Test Scheme", null);
        project = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(10)));
        ManagerFactory.getNotificationSchemeManager().addSchemeToProject(project, scheme);
        nsm.createSchemeEntity(scheme, new SchemeEntity("Current_Assignee", "dave@atlassian.com", EventType.ISSUE_DELETED_ID));

        //nsm.createNotification(scheme, "TEST_EVENT_1", "TEST_TYPE_1", null);
        final Map issueProperties = EasyMap.build("id", new Long(20), "project", new Long(10), "key", "issue key", "summary", "issue summary",
            "created", new Timestamp(new Date().getTime()), "updated", new Timestamp(new Date().getTime()), "type", new Long(1));
        issueProperties.put("assignee", "dave");
        issueProperties.put("status", "assigned");

        issue = UtilsForTests.getTestEntity("Issue", issueProperties);
        //        comment = UtilsForTests.getTestEntity("Action", EasyMap.build("created", new Timestamp(new Date().getTime()), "type", ActionConstants.TYPE_COMMENT));

        // Create the issue object
        final IssueFactory issueFactory = ComponentManager.getComponentInstanceOfType(IssueFactory.class);
        issueObject = issueFactory.getIssue(issue);

        ie = new IssueEvent(issueObject, testUser, createMockComment(), null, null, EasyMap.build("", ""), EventType.ISSUE_CREATED_ID);

        final Mock mailServerManager = new Mock(MailServerManager.class);
        MailFactory.setServerManager((MailServerManager) mailServerManager.proxy());
        mailServer = new Mock(SMTPMailServer.class);

        mailServerManager.setupResult("getDefaultSMTPMailServer", mailServer.proxy());

        // Add permissions manager
        ctrlPermissionManager = MockControl.createControl(PermissionManager.class);
        mockPermissionManager = (PermissionManager) ctrlPermissionManager.getMock();

        ManagerFactory.addService(PermissionManager.class, mockPermissionManager);

        ManagerFactory.getApplicationProperties().setString(APKeys.JIRA_WEBWORK_ENCODING, "UTF-8");

    }

    @Override
    protected void tearDown() throws Exception
    {
        ManagerFactory.removeService(NotificationTypeManager.class);

        MailFactory.refresh();

        ml = null;
        nsm = null;
        scheme = null;
        project = null;
        ie = null;
        //        comment = null;
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
        assertTrue(ml.isInternal());
    }

    public void testIssueSendNotifications_IssueCreated() throws Exception
    {
        ie = new IssueEvent(issueObject, testUser, createMockComment(), null, null, EasyMap.build("", ""), EventType.ISSUE_CREATED_ID);
        ml.issueCreated(ie);
        assertEquals(EventType.ISSUE_CREATED_ID, ml.getEventTypeIDCalled());
    }

    public void testIssueSendNotifications_IssueAssigned() throws Exception
    {
        ie = new IssueEvent(issueObject, testUser, createMockComment(), null, null, EasyMap.build("", ""), EventType.ISSUE_ASSIGNED_ID);
        ml.issueAssigned(ie);
        assertEquals(EventType.ISSUE_ASSIGNED_ID, ml.getEventTypeIDCalled());
    }

    public void testIssueSendNotifications_IssueClosed() throws Exception
    {
        ie = new IssueEvent(issueObject, testUser, createMockComment(), null, null, EasyMap.build("", ""), EventType.ISSUE_CLOSED_ID);
        ml.issueClosed(ie);
        assertEquals(EventType.ISSUE_CLOSED_ID, ml.getEventTypeIDCalled());
    }

    public void testIssueSendNotifications_IssueResolved() throws Exception
    {
        ie = new IssueEvent(issueObject, testUser, createMockComment(), null, null, EasyMap.build("", ""), EventType.ISSUE_RESOLVED_ID);
        ml.issueResolved(ie);
        assertEquals(EventType.ISSUE_RESOLVED_ID, ml.getEventTypeIDCalled());
    }

    public void testIssueSendNotifications_IssueReopened() throws Exception
    {
        ie = new IssueEvent(issueObject, testUser, createMockComment(), null, null, EasyMap.build("", ""), EventType.ISSUE_REOPENED_ID);
        ml.issueReopened(ie);
        assertEquals(EventType.ISSUE_REOPENED_ID, ml.getEventTypeIDCalled());
    }

    public void testIssueSendNotifications_IssueUpdated() throws Exception
    {
        ie = new IssueEvent(issueObject, testUser, createMockComment(), null, null, EasyMap.build("", ""), EventType.ISSUE_UPDATED_ID);
        ml.issueUpdated(ie);
        assertEquals(EventType.ISSUE_UPDATED_ID, ml.getEventTypeIDCalled());
    }

    public void testIssueSendNotifications_IssueCommented() throws Exception
    {
        ie = new IssueEvent(issueObject, testUser, createMockComment(), null, null, EasyMap.build("", ""), EventType.ISSUE_COMMENTED_ID);
        ml.issueCommented(ie);
        assertEquals(EventType.ISSUE_COMMENTED_ID, ml.getEventTypeIDCalled());
    }

    public void testIssueSendNotifications_IssueWorkLogged() throws Exception
    {
        ie = new IssueEvent(issueObject, testUser, createMockComment(), null, null, EasyMap.build("", ""), EventType.ISSUE_WORKLOGGED_ID);
        ml.issueWorkLogged(ie);
        assertEquals(EventType.ISSUE_WORKLOGGED_ID, ml.getEventTypeIDCalled());
    }

    public void testIssueSendNotifications_IssueDeleted() throws Exception
    {
        ie = new IssueEvent(issueObject, testUser, createMockComment(), null, null, EasyMap.build("", ""), EventType.ISSUE_DELETED_ID);
        ml.issueDeleted(ie);
        assertEquals(EventType.ISSUE_DELETED_ID, ml.getEventTypeIDCalled());
    }

    public void testIssueSendNotifications_IssueWorkStarted() throws Exception
    {
        ie = new IssueEvent(issueObject, testUser, createMockComment(), null, null, EasyMap.build("", ""), EventType.ISSUE_WORKSTARTED_ID);
        ml.issueStarted(ie);
        assertEquals(EventType.ISSUE_WORKSTARTED_ID, ml.getEventTypeIDCalled());
    }

    public void testIssueSendNotifications_IssueWorkStopped() throws Exception
    {
        ie = new IssueEvent(issueObject, testUser, createMockComment(), null, null, EasyMap.build("", ""), EventType.ISSUE_WORKSTOPPED_ID);
        ml.issueStopped(ie);
        assertEquals(EventType.ISSUE_WORKSTOPPED_ID, ml.getEventTypeIDCalled());
    }

    public void testIssueNotificationSending() throws Exception
    {
        ManagerFactory.addService(NotificationTypeManager.class, new NotificationTypeManager("notification-event-types.xml"));
        final User dave = UserUtils.createUser("dave", "dave@atlassian.com");

        final Mock projectManager = new Mock(ProjectManager.class);
        projectManager.expectAndReturn("getProject", P.args(new IsEqual(issue)), project);
        ml = new MockMailListener(nsm, issueMailQueueItemFactory, null);

        // Add an event type to the system
        final EventTypeManager eventTypeManager = ComponentAccessor.getEventTypeManager();
        final Map eventTypeParamasMap = EasyMap.build("id", new Long(8), "name", "Issue Deleted", "description",
            "This is the issue deleted event type descrition", "type", EventType.JIRA_SYSTEM_EVENT_TYPE);
        final GenericValue issueEventTypeGV = UtilsForTests.getTestEntity(EventType.EVENT_TYPE, eventTypeParamasMap);
        final EventType eventType = new EventType(issueEventTypeGV);
        eventTypeManager.addEventType(eventType);

        ie = new IssueEvent(issueObject, testUser, createMockComment(), null, null, EasyMap.build("", ""), eventType.getId());

        ctrlPermissionManager.expectAndReturn(mockPermissionManager.hasPermission(Permissions.BROWSE, issueObject, dave), true);
        ctrlPermissionManager.replay();

        final Email email = (Email) new Email("dave@atlassian.com").setSubject("(issue key) issue summary").setFromName(
            "test user fullname (JIRA)");
        mailServer.expectVoid("send", P.args(new IsEqual(email)));
        ml.setSendMessage(true);
        ml.issueDeleted(ie);
        assertEquals(eventType.getId(), ml.getEventTypeIDCalled());
        ManagerFactory.getMailQueue().sendBuffer();
        mailServer.verify();
    }

    public void testUserSendNotification1() throws Exception
    {
        final Email email = (Email) new Email("test@atlassian.com").setSubject("Account signup");
        final UserEvent ue = new UserEvent(testUser, UserEventType.USER_SIGNUP);
        mailServer.expectVoid("send", P.args(new IsEqual(email)));

        ml.userSignup(ue);
        assertEquals("Account signup", ml.getUserMailCalled());
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

        ml.userCreated(ue);

        final Email email = (Email) new Email("test@atlassian.com").setSubject("Account created");
        mailServer.expectVoid("send", P.args(new IsEqual(email)));
        assertEquals("Account created", ml.getUserMailCalled());

        ManagerFactory.getMailQueue().sendBuffer();
        mailServer.verify();
        verify(userManager);
    }

    public void testUserSendNotification3() throws Exception
    {
        final Email email = (Email) new Email("test@atlassian.com").setSubject("Account password");
        final UserEvent ue = new UserEvent(testUser, UserEventType.USER_SIGNUP);
        mailServer.expectVoid("send", P.args(new IsEqual(email)));

        ml.userForgotPassword(ue);
        assertEquals("Account password", ml.getUserMailCalled());
        ManagerFactory.getMailQueue().sendBuffer();
        mailServer.verify();
    }

    // Test that mail is sent to correct list of recipients - i.e. a user will only recieve the first email notification
    // if there are multiple intended notification types for this user
    // For example, in this test, the user is both the assignee and the project lead - but the user will only receive one
    // email for the event.
    public void testCreateMailItems() throws Exception
    {
        ml = new MockMailListener(ComponentManager.getComponentInstanceOfType(NotificationSchemeManager.class), issueMailQueueItemFactory, null);
        ml.setSendMessage(true);

        ManagerFactory.addService(NotificationTypeManager.class, new NotificationTypeManager("notification-event-types.xml"));
        final User dave = UserUtils.createUser("dave", "dave@atlassian.com");

        // Add an event type to the system
        final EventTypeManager eventTypeManager = ComponentAccessor.getEventTypeManager();
        final Map eventTypeParamasMap = EasyMap.build("id", new Long(1), "name", "Issue Created", "description",
            "This is the issue created event type descrition", "type", EventType.JIRA_SYSTEM_EVENT_TYPE);
        final GenericValue issueEventTypeGV = UtilsForTests.getTestEntity(EventType.EVENT_TYPE, eventTypeParamasMap);
        final EventType eventType = new EventType(issueEventTypeGV);
        eventTypeManager.addEventType(eventType);

        final GenericValue project = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(11), "lead", "dave"));
        nsm.addSchemeToProject(project, scheme);
        nsm.createSchemeEntity(scheme, new SchemeEntity("Current_Assignee", eventType.getId()));
        nsm.createSchemeEntity(scheme, new SchemeEntity("Project_Lead", eventType.getId()));

        ie = new IssueEvent(issueObject, testUser, createMockComment(), null, null, EasyMap.build("", ""), eventType.getId());
        ml.issueCreated(ie);

        final Email email = (Email) new Email("test@atlassian.com").setSubject("Account password");
        final UserEvent ue = new UserEvent(testUser, UserEventType.USER_SIGNUP);
        mailServer.expectVoid("send", P.args(new IsEqual(email)));

        final Collection mailItems = ManagerFactory.getMailQueue().getQueue();
        assertTrue(mailItems.size() == 1);

        for (final Iterator iterator = mailItems.iterator(); iterator.hasNext();)
        {
            final IssueMailQueueItem mailQueueItem = (IssueMailQueueItem) iterator.next();
            final Set recipientList = mailQueueItem.getRecipientList();
            assertTrue(mailQueueItem.getRecipientList().size() == 1);

            for (final Iterator iterator1 = recipientList.iterator(); iterator1.hasNext();)
            {
                final NotificationRecipient recipient = (NotificationRecipient) iterator1.next();
                assertEquals(recipient.getEmail(), "dave@atlassian.com");
            }
        }
    }

    private Comment createMockComment()
    {
        return new MockComment("John Citizen", "Some comment text");
    }

}
