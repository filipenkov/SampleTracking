/*
 * Copyright (c) 2002-2006
 * All rights reserved.
 */

package com.atlassian.jira.mail;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.config.properties.APKeys;
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
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.atlassian.mail.MailException;
import com.atlassian.mail.MailFactory;
import com.atlassian.mail.server.MailServerManager;
import com.atlassian.velocity.VelocityManager;
import com.mockobjects.constraint.Constraint;
import com.mockobjects.constraint.IsAnything;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import com.opensymphony.module.propertyset.map.MapPropertySet;
import com.opensymphony.user.Group;
import com.opensymphony.user.User;
import mock.MockComment;
import org.apache.commons.collections.set.ListOrderedSet;
import org.apache.velocity.exception.VelocityException;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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
    private Mock mockVelocityManager;
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

        mockVelocityManager = new Mock(VelocityManager.class);

        u1 = UtilsForTests.getTestUser("text1");
        u1.setEmail("text1@atlassian.com");
        u1Properties.setMap(new HashMap());
        u1Properties.setString(PreferenceKeys.USER_NOTIFICATIONS_MIMETYPE, "text");

        u2 = UtilsForTests.getTestUser("html1");
        u2.setEmail("html1@atlassian.com");
        u2Properties.setMap(new HashMap());
        u2Properties.setString(PreferenceKeys.USER_NOTIFICATIONS_MIMETYPE, "html");

        g1 = UtilsForTests.getTestGroup("group1");

        u3 = UtilsForTests.getTestUser("text2");
        u3.setEmail("text2@atlassian.com");
        u3Properties.setMap(new HashMap());
        u3Properties.setString(PreferenceKeys.USER_NOTIFICATIONS_MIMETYPE, "text");
        u3.addToGroup(g1);

        u4 = UtilsForTests.getTestUser("html2");
        u4.setEmail("html2@atlassian.com");
        u4Properties.setMap(new HashMap());
        u4Properties.setString(PreferenceKeys.USER_NOTIFICATIONS_MIMETYPE, "html");
        u4.addToGroup(g1);

        ManagerFactory.getApplicationProperties().setString(APKeys.JIRA_WEBWORK_ENCODING, "UTF-8");

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
        mockVelocityManager = null;
        projectRoleMangerMock = null;
        templateIssue = null;
        u1Properties = null;
        u2Properties = null;
        u3Properties = null;
        u4Properties = null;
        templateIssue = null;
        userPropertyManager = null;
        MailFactory.refresh();
        super.tearDown(); //To change body of overriden methods use Options | File Templates.
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
        final Set users = new HashSet();
        users.add(new NotificationRecipient(u1));
        sendList(users, new HashMap(), 1);
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
        final Set addresses = new ListOrderedSet();
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
        final Set users = new HashSet();
        users.add(new NotificationRecipient(user));
        users.add(new NotificationRecipient(userWithGroup));

        final Map eventSource = EasyMap.build("eventsource", IssueEventSource.ACTION);
        final Map commentMap = new HashMap(EasyMap.build("params", eventSource, "comment", comment));

        ManagerFactory.addService(ProjectRoleManager.class, (ProjectRoleManager) projectRoleMangerMock.proxy());

        commentMap.put("issue", templateIssue);
        sendList(users, commentMap, numberOfRecipients);
    }

    private void sendWorklog(final User user, final User userWithGroup, final Worklog worklog, final Worklog originalWorklog, final int numberOfRecipients)
            throws VelocityException
    {
        final Set users = new HashSet();
        users.add(new NotificationRecipient(user));
        users.add(new NotificationRecipient(userWithGroup));

        final Map eventSource = EasyMap.build("eventsource", IssueEventSource.ACTION);
        if (originalWorklog != null)
        {
            eventSource.put(TimeTrackingIssueUpdater.EVENT_ORIGINAL_WORKLOG_PARAMETER, originalWorklog);
        }
        final Map worklogMap = new HashMap(EasyMap.build("params", eventSource, "worklog", worklog));

        ManagerFactory.addService(ProjectRoleManager.class, (ProjectRoleManager) projectRoleMangerMock.proxy());

        worklogMap.put("issue", templateIssue);
        sendList(users, worklogMap, numberOfRecipients);
    }

    /* Send a mail with a user and an is html or not */
    private void sendWorkflow(final User user, final Comment comment, final boolean seeComment) throws VelocityException
    {
        final Set users = new HashSet();
        users.add(new NotificationRecipient(user));

        final Map eventSource = EasyMap.build("eventsource", IssueEventSource.WORKFLOW);
        final Map commentMap = new HashMap(EasyMap.build("params", eventSource, "comment", comment));
        final Map commentExcepectedMap = new HashMap(EasyMap.build("params", eventSource));
        if (seeComment)
        {
            commentExcepectedMap.put("comment", comment);
        }
        sendList(users, commentMap, 1);
    }

    private void sendWorkflow(final String emailAddress, final Comment comment, final boolean seeComment)
            throws VelocityException
    {
        final Set users = new HashSet();
        users.add(new NotificationRecipient(emailAddress));

        final Map eventSource = EasyMap.build("eventsource", IssueEventSource.WORKFLOW);
        final Map commentMap = new HashMap(EasyMap.build("params", eventSource, "comment", comment));
        final Map commentExcepectedMap = new HashMap(EasyMap.build("params", eventSource));
        if (seeComment)
        {
            commentExcepectedMap.put("comment", comment);
        }
        sendList(users, commentMap, ((emailAddress != null) && (emailAddress.length() > 0)) ? 1 : 0);
    }

    private void sendList(final Set users, final Map contextParamsIn, final int numberOfRecipients)
            throws VelocityException
    {
        final Constraint[] velocitycons = new Constraint[3];

        velocitycons[0] = new IsAnything();
        velocitycons[1] = new IsEqual("base");
        velocitycons[2] = new IsAnything();

        mockVelocityManager.expectAndReturn("getEncodedBodyForContent", velocitycons, "bodytext");

        ManagerFactory.addService(VelocityManager.class, (VelocityManager) mockVelocityManager.proxy());

        final TemplateManager templateManager = (TemplateManager) ComponentManager.getComponentInstanceOfType(TemplateManager.class);
        final MailingListCompiler mailingListCompiler = new MailingListCompiler(templateManager, (ProjectRoleManager) projectRoleMangerMock.proxy());
        mailingListCompiler.sendLists(users, u1.getEmail(), null, new Long("1"), "base", contextParamsIn, null);

        if (hasAnyValidEmailAddresses(users))
        {
            mockVelocityManager.verify();
        }

        assertEquals(numberOfRecipients, ManagerFactory.getMailQueue().size());

        ManagerFactory.addService(VelocityManager.class, null); //reset the mailing list compiler
    }

    private boolean hasAnyValidEmailAddresses(final Collection users)
    {
        for (final Iterator i = users.iterator(); i.hasNext();)
        {
            final NotificationRecipient recipient = (NotificationRecipient) i.next();
            if ((recipient.getEmail() != null) && (recipient.getEmail().length() > 0))
            {
                return true;
            }
        }
        return false;
    }

}
