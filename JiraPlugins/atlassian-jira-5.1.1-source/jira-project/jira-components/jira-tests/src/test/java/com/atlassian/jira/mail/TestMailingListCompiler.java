/*
 * Copyright (c) 2002-2006
 * All rights reserved.
 */

package com.atlassian.jira.mail;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.issue.IssueEventSource;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.worklog.TimeTrackingIssueUpdater;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.local.AbstractUsersTestCase;
import com.atlassian.jira.mock.MockProjectRoleManager;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.template.TemplateManager;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.template.mocks.VelocityTemplatingEngineMocks;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.mail.MailException;
import com.atlassian.mail.MailFactory;
import com.atlassian.mail.server.MailServerManager;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import com.opensymphony.module.propertyset.map.MapPropertySet;
import mock.MockComment;
import org.apache.velocity.exception.VelocityException;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class TestMailingListCompiler extends AbstractUsersTestCase
{
    private User u1;
    private User u2;
    private User u3;
    private User u4;
    private MapPropertySet u1Properties = new MapPropertySet();
    private MapPropertySet u2Properties = new MapPropertySet();
    private MapPropertySet u3Properties = new MapPropertySet();
    private MapPropertySet u4Properties = new MapPropertySet();
    private Group g1;
    private Mock projectRoleMangerMock;
    public Issue templateIssue;
    private Mock userPropertyManager;

    public TestMailingListCompiler(final String s)
    {
        super(s);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        // Setup a mockmail mail factory to use in the tests.
        final Mock mockmail = new Mock(MailServerManager.class);
        MailFactory.setServerManager((MailServerManager) mockmail.proxy());

        u1 = createMockUser("text1", "text1", "text1@atlassian.com");
        u1Properties.setMap(new HashMap());
        u1Properties.setString(PreferenceKeys.USER_NOTIFICATIONS_MIMETYPE, "text");

        u2 = createMockUser("html1", "html1", "html1@atlassian.com");
        u2Properties.setMap(new HashMap());
        u2Properties.setString(PreferenceKeys.USER_NOTIFICATIONS_MIMETYPE, "html");

        g1 = createMockGroup("group1");

        u3 = createMockUser("text2", "text2", "text2@atlassian.com");
        u3Properties.setMap(new HashMap());
        u3Properties.setString(PreferenceKeys.USER_NOTIFICATIONS_MIMETYPE, "text");
        addUserToGroup(u3, g1);

        u4 = createMockUser("html2", "html2", "html2@atlassian.com");
        u4Properties.setMap(new HashMap());
        u4Properties.setString(PreferenceKeys.USER_NOTIFICATIONS_MIMETYPE, "html");
        addUserToGroup(u4, g1);

        // Setting up the issue to be used by the tests
        final GenericValue project = UtilsForTests.getTestEntity("Project", EasyMap.build("name", "test project"));
        final GenericValue issueGV = UtilsForTests.getTestEntity("Issue", EasyMap.build("id", new Long(1), "project", project.getLong("id"), "key",
                "TST-1"));

        final IssueFactory issueFactory = ComponentManager.getComponentInstanceOfType(IssueFactory.class);
        final Issue issue = issueFactory.getIssue(issueGV);
        templateIssue = new TemplateIssue(issue, null, null, null, null, null);

        // Setting up the ProjectRoleManager
        projectRoleMangerMock = new Mock(ProjectRoleManager.class);
        projectRoleMangerMock.expectAndReturn("getProjectRole", MockComment.COMMENT_ROLE_NAME, MockProjectRoleManager.PROJECT_ROLE_TYPE_1);
        projectRoleMangerMock.expectAndReturn("isUserInProjectRole", P.ANY_ARGS, Boolean.TRUE);

        userPropertyManager = new Mock(UserPropertyManager.class);
        userPropertyManager.expectAndReturn("getPropertySet", u1, u1Properties);
        userPropertyManager.expectAndReturn("getPropertySet", u2, u2Properties);
        userPropertyManager.expectAndReturn("getPropertySet", u3, u3Properties);
        userPropertyManager.expectAndReturn("getPropertySet", u4, u4Properties);
    }

    @Override
    protected void tearDown() throws Exception
    {
        u1 = null;
        u2 = null;
        u3 = null;
        u4 = null;
        g1 = null;
        projectRoleMangerMock = null;
        templateIssue = null;
        u1Properties = null;
        u2Properties = null;
        u3Properties = null;
        u4Properties = null;
        templateIssue = null;
        userPropertyManager = null;
        MailFactory.refresh();
        super.tearDown();
    }

    public void testSendWithNoUserEmailAddressOnly() throws Exception
    {
        final Comment comment = new MockComment("John Citizen", "bodytext");
        sendWorkflow("test@example.com", comment, true);
    }

    public void testNoUserNoEmailAddressDoesNotThrowException() throws Exception
    {
        final Comment comment = new MockComment("John Citizen", "bodytext");
        sendWorkflow((String) null, comment, true);
        sendWorkflow("", comment, true);
    }

    /* Test by sending a subscription email to the mailing list */
    public void testSendSubscription() throws MailException, VelocityException
    {
        final Set<NotificationRecipient> users = new HashSet<NotificationRecipient>();
        users.add(new NotificationRecipient(u1));
        sendList(users, new HashMap<String, Object>(), 1);
    }

    /* Test by sending a non restricted worklog to two html users */
    public void testSendHtmlWorklog() throws MailException, GenericEntityException, VelocityException
    {
        final Mock mockWorklog = new Mock(Worklog.class);
        mockWorklog.expectAndReturn("getGroupLevel", null);
        mockWorklog.expectAndReturn("getRoleLevel", null);
        sendWorklog(u2, u4, (Worklog) mockWorklog.proxy(), null, 2);
        mockWorklog.proxy();
    }

    public void testSendHtmlWorklogWithRoleRestriction() throws MailException, GenericEntityException, VelocityException
    {
        final Mock mockWorklog = new Mock(Worklog.class);
        mockWorklog.expectAndReturn("getGroupLevel", null);
        mockWorklog.expectAndReturn("getRoleLevel", new MockProjectRoleManager.MockProjectRole(1, "My Role", "Test Role Desc"));
        sendWorklog(u2, u4, (Worklog) mockWorklog.proxy(), null, 2);
        mockWorklog.proxy();
    }

    /* Test by sending a non restricted worklog with an updated worklog to two html users */
    public void testSendHtmlWorklogUpdated() throws MailException, GenericEntityException, VelocityException
    {
        final Mock mockWorklog = new Mock(Worklog.class);
        mockWorklog.expectAndReturn("getGroupLevel", null);
        mockWorklog.expectAndReturn("getRoleLevel", null);
        final Mock originalWorklog = new Mock(Worklog.class);
        originalWorklog.expectAndReturn("getGroupLevel", null);
        originalWorklog.expectAndReturn("getRoleLevel", null);
        sendWorklog(u2, u4, (Worklog) mockWorklog.proxy(), (Worklog) originalWorklog.proxy(), 2);
        originalWorklog.proxy();
    }

    public void testSendHtmlWorklogUpdatedWithRoleRestriction() throws Exception
    {
        final Mock mockWorklog = new Mock(Worklog.class);
        mockWorklog.expectAndReturn("getGroupLevel", null);
        mockWorklog.expectAndReturn("getRoleLevel", new MockProjectRoleManager.MockProjectRole(1, "My Role", "Test Role Desc"));
        final Mock originalWorklog = new Mock(Worklog.class);
        originalWorklog.expectAndReturn("getGroupLevel", null);
        originalWorklog.expectAndReturn("getRoleLevel", new MockProjectRoleManager.MockProjectRole(1, "My Role", "Test Role Desc"));
        sendWorklog(u2, u4, (Worklog) mockWorklog.proxy(), (Worklog) originalWorklog.proxy(), 2);
        originalWorklog.proxy();
    }

    /* Test by sending a non restricted worklog to two html users */
    public void testSendTextWorklog() throws MailException, GenericEntityException, VelocityException
    {
        final Mock mockWorklog = new Mock(Worklog.class);
        mockWorklog.expectAndReturn("getGroupLevel", null);
        mockWorklog.expectAndReturn("getRoleLevel", null);
        sendWorklog(u2, u4, (Worklog) mockWorklog.proxy(), null, 2);
        mockWorklog.proxy();
    }

    /* Test by sending a non restricted worklog with an updated worklog to two html users */
    public void testSendTextWorklogUpdated() throws MailException, GenericEntityException, VelocityException
    {
        final Mock mockWorklog = new Mock(Worklog.class);
        mockWorklog.expectAndReturn("getGroupLevel", null);
        mockWorklog.expectAndReturn("getRoleLevel", null);
        final Mock originalWorklog = new Mock(Worklog.class);
        originalWorklog.expectAndReturn("getGroupLevel", null);
        originalWorklog.expectAndReturn("getRoleLevel", null);
        sendWorklog(u2, u4, (Worklog) mockWorklog.proxy(), (Worklog) originalWorklog.proxy(), 2);
        originalWorklog.proxy();
    }

    public void testGetEmailAddresses()
    {
        final Set<String> addresses = new TreeSet<String>();
        addresses.add("address1");
        addresses.add("address2");
        addresses.add("address3");
        assertEquals("address1,address2,address3", MailingListCompiler.getEmailAddresses(addresses));
    }

    /* Test by sending a non restricted comment to two html users */
    public void testSendHtmlComment1() throws MailException, GenericEntityException, VelocityException
    {
        final Comment comment = new MockComment("John Citizen", "bodytext");
        sendComment(u2, u4, comment, 2);
    }

    /* Test by sending a non restricted comment to two text users */
    public void testSendTextComment1() throws MailException, GenericEntityException, VelocityException
    {
        final Comment comment = new MockComment("John Citizen", "bodytext");
        sendComment(u1, u3, comment, 2);
    }

    /* Test by sending a restricted comment to two html users */
    public void testSendHtmlComment2() throws MailException, GenericEntityException, VelocityException
    {
        final Comment comment = new MockComment("John Citizen", "some test comment", g1.getName(), null);
        sendComment(u2, u4, comment, 1);
    }

    /* Test by sending a restricted comment to two text users */
    public void testSendTextComment2() throws MailException, GenericEntityException, VelocityException
    {
        final Comment comment = new MockComment("John Citizen", "some test comment", g1.getName(), null);
        sendComment(u1, u3, comment, 1);
    }

    /* Test by sending a non restricted workflow */
    public void testSendTextWorkflow1() throws MailException, GenericEntityException, VelocityException
    {
        final Comment comment = new MockComment("John Citizen", "some test comment");
        sendWorkflow(u1, comment, true);
    }

    /* Test by sending a non restricted workflow */
    public void testSendHtmlWorkflow1() throws MailException, GenericEntityException, VelocityException
    {
        final Comment comment = new MockComment("John Citizen", "some test comment");
        sendWorkflow(u2, comment, true);
    }

    /* Test by sending a non restricted workflow */
    public void testSendTextWorkflow2() throws MailException, GenericEntityException, VelocityException
    {
        final Comment comment = new MockComment("John Citizen", "some test comment");
        sendWorkflow(u3, comment, true);
    }

    /* Test by sending a non restricted workflow */
    public void testSendHtmlWorkflow2() throws MailException, GenericEntityException, VelocityException
    {
        final Comment comment = new MockComment("John Citizen", "some test comment");
        sendWorkflow(u4, comment, true);
    }

    /* Test by sending a non restricted workflow */
    public void testSendRestrictedTextWorkflow1() throws MailException, GenericEntityException, VelocityException
    {
        final Comment comment = new MockComment("John Citizen", "some test comment", g1.getName(), null);
        sendWorkflow(u1, comment, false);
    }

    /* Test by sending a non restricted workflow */
    public void testSendRestrictedHtmlWorkflow1() throws MailException, GenericEntityException, VelocityException
    {
        final Comment comment = new MockComment("John Citizen", "some test comment", g1.getName(), null);
        sendWorkflow(u2, comment, false);
    }

    /* Test by sending a non restricted workflow */
    public void testSendRestrictedTextWorkflow2() throws MailException, GenericEntityException, VelocityException
    {
        final Comment comment = new MockComment("John Citizen", "some test comment", g1.getName(), null);
        sendWorkflow(u3, comment, true);
    }

    /* Test by sending a non restricted workflow */
    public void testSendRestrictedHtmlWorkflow2() throws MailException, GenericEntityException, VelocityException
    {
        final Comment comment = new MockComment("John Citizen", "some test comment", g1.getName(), null);
        sendWorkflow(u4, comment, true);
    }

    /* Send a mail with two users and an is html or not */
    private void sendComment(final User user, final User userWithGroup, final Comment comment, final int numberOfRecipients)
            throws VelocityException
    {
        final Set<NotificationRecipient> users =
                ImmutableSet.of(new NotificationRecipient(user), new NotificationRecipient(userWithGroup));

        final Map<String, Object> eventSource = ImmutableMap.<String, Object>of("eventsource", IssueEventSource.ACTION);

        final Map<String, Object> commentMap =
                MapBuilder.<String, Object>newBuilder().
                        add("params", eventSource).add("comment", comment).add("issue", templateIssue).toMutableMap();

        ManagerFactory.addService(ProjectRoleManager.class, (ProjectRoleManager) projectRoleMangerMock.proxy());

        sendList(users, commentMap, numberOfRecipients);
    }

    private void sendWorklog(final User user, final User userWithGroup, final Worklog worklog, final Worklog originalWorklog, final int numberOfRecipients)
            throws VelocityException
    {
        final Set<NotificationRecipient> users =
                ImmutableSet.of(new NotificationRecipient(user), new NotificationRecipient(userWithGroup));

        final Map<String, Object> eventSource = MapBuilder.<String, Object>newBuilder().
                add("eventsource", IssueEventSource.ACTION).toMutableMap();

        if (originalWorklog != null)
        {
            eventSource.put(TimeTrackingIssueUpdater.EVENT_ORIGINAL_WORKLOG_PARAMETER, originalWorklog);
        }
        final Map<String, Object> worklogMap = MapBuilder.<String, Object>newBuilder().
                add("params", eventSource).add("worklog", worklog).add("issue", templateIssue).toMutableMap();

        ManagerFactory.addService(ProjectRoleManager.class, (ProjectRoleManager) projectRoleMangerMock.proxy());

        sendList(users, worklogMap, numberOfRecipients);
    }

    /* Send a mail with a user and an is html or not */
    private void sendWorkflow(final User user, final Comment comment, final boolean seeComment) throws VelocityException
    {
        final Set<NotificationRecipient> users = ImmutableSet.of(new NotificationRecipient(user));

        final Map<String, Object> eventSource = ImmutableMap.<String, Object>of("eventsource", IssueEventSource.WORKFLOW);

        final Map<String, Object> commentMap = MapBuilder.<String, Object>newBuilder().
                add("params", eventSource).add("comment", comment).toMutableMap();

        final Map<String, Object> commentExpectedMap = MapBuilder.<String, Object>newBuilder().add("params", eventSource).toMutableMap();
        if (seeComment)
        {
            commentExpectedMap.put("comment", comment);
        }
        sendList(users, commentMap, 1);
    }

    private void sendWorkflow(final String emailAddress, final Comment comment, final boolean seeComment)
            throws VelocityException
    {
        final Set<NotificationRecipient> users = new HashSet<NotificationRecipient>();
        users.add(new NotificationRecipient(emailAddress));

        final Map<String, Object> eventSource = ImmutableMap.<String, Object>of("eventsource", IssueEventSource.WORKFLOW);
        final Map<String, Object> commentMap = MapBuilder.<String, Object>newBuilder().
                add("params", eventSource).add("comment", comment).toMutableMap();

        final Map<String, Object> commentExpectedMap = MapBuilder.<String, Object>newBuilder().add("params", eventSource).toMutableMap();
        if (seeComment)
        {
            commentExpectedMap.put("comment", comment);
        }
        sendList(users, commentMap, ((emailAddress != null) && (emailAddress.length() > 0)) ? 1 : 0);
    }

    private void sendList(final Set<NotificationRecipient> users, final Map<String, Object> contextParamsIn,
            final int numberOfRecipients) throws VelocityException
    {
        final VelocityTemplatingEngine mockVelocityTemplatingEngine = VelocityTemplatingEngineMocks.alwaysOutput("bodytext").get();

        ManagerFactory.addService(VelocityTemplatingEngine.class, mockVelocityTemplatingEngine);

        final TemplateManager templateManager = ComponentAccessor.getComponent(TemplateManager.class);
        final MailingListCompiler mailingListCompiler = new MailingListCompiler(templateManager, (ProjectRoleManager) projectRoleMangerMock.proxy());
        mailingListCompiler.sendLists(users, u1.getEmailAddress(), null, new Long("1"), "base", contextParamsIn, null);

        assertEquals(numberOfRecipients, ComponentAccessor.getMailQueue().size());

        ManagerFactory.addService(VelocityTemplatingEngine.class, null); //reset the mailing list compiler
    }
}
