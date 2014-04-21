/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.web.action.admin.mail;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.crowd.exception.embedded.InvalidGroupException;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.PropertiesManager;
import com.atlassian.jira.jelly.tag.projectroles.MockProjectRoleService;
import com.atlassian.jira.local.AbstractUsersIndexingTestCase;
import com.atlassian.jira.mail.Email;
import com.atlassian.jira.mock.MockPermissionManager;
import com.atlassian.jira.mock.MockProjectManager;
import com.atlassian.jira.mock.MockProjectRoleManager;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleActors;
import com.atlassian.jira.security.roles.ProjectRoleActorsImpl;
import com.atlassian.jira.security.roles.ProjectRoleImpl;
import com.atlassian.jira.security.roles.RoleActorDoesNotExistException;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.mail.MailException;
import com.atlassian.mail.server.MailServerManager;
import com.atlassian.mail.server.SMTPMailServer;
import com.mockobjects.constraint.IsAnything;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.constraint.Or;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import webwork.action.Action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TestSendBulkMail extends AbstractUsersIndexingTestCase
{
    SendBulkMail sbm;
    private Group testGroup1;
    private Group testGroup2;
    private Group testGroup3;
    private String testEmail1;
    private String testEmail2;
    private String testEmail3;
    private String testEmail4;
    private String expectedSubject;
    private String expectedBody;
    private String expectedMimeType;
    private UserUtil userUtil;
    private GroupManager groupManager;

    public TestSendBulkMail(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        userUtil = ComponentAccessor.getUserUtil();
        groupManager = ComponentAccessor.getGroupManager();
    }

    public void testIsHasMailServerNoMailServer() throws Exception
    {
        Mock mockMailServerManager = new Mock(MailServerManager.class);
        mockMailServerManager.setStrict(true);
        mockMailServerManager.expectAndReturn("getDefaultSMTPMailServer", null);

        sbm = new SendBulkMail((MailServerManager) mockMailServerManager.proxy(), null, null, null, userUtil, groupManager);
        assertFalse(sbm.isHasMailServer());
        mockMailServerManager.verify();
    }

    public void testIsHasMailServer() throws Exception
    {
        Mock mockMailServer = new Mock(SMTPMailServer.class);
        mockMailServer.setStrict(true);

        Mock mockMailServerManager = new Mock(MailServerManager.class);
        mockMailServerManager.setStrict(true);
        mockMailServerManager.expectAndReturn("getDefaultSMTPMailServer", mockMailServer.proxy());

        sbm = new SendBulkMail((MailServerManager) mockMailServerManager.proxy(), null, null, null, userUtil, groupManager);
        assertTrue(sbm.isHasMailServer());

        mockMailServerManager.verify();
    }

    public void testGetMimeTypes()
    {
        Mock mockMailServerManager = new Mock(MailServerManager.class);
        mockMailServerManager.setStrict(true);

        sbm = new SendBulkMail((MailServerManager) mockMailServerManager.proxy(), null, null, null, userUtil, groupManager);
        assertEquals(EasyMap.build(NotificationRecipient.MIMETYPE_HTML, NotificationRecipient.MIMETYPE_HTML_DISPLAY, NotificationRecipient.MIMETYPE_TEXT, NotificationRecipient.MIMETYPE_TEXT_DISPLAY), sbm.getMimeTypes());

        mockMailServerManager.verify();
    }

    public void testGettersSetters()
    {
        Mock mockMailServerManager = new Mock(MailServerManager.class);
        mockMailServerManager.setStrict(true);

        sbm = new SendBulkMail((MailServerManager) mockMailServerManager.proxy(), null, null, null, userUtil, groupManager);

        String expectedMessage = "Test message.";
        sbm.setMessage(expectedMessage);
        assertEquals(expectedMessage, sbm.getMessage());

        String expectedMessageType = "Test Message Type";
        sbm.setMessageType(expectedMessageType);
        assertEquals(expectedMessageType, sbm.getMessageType());

        String expectedSubject = "Test Subject";
        sbm.setSubject(expectedSubject);
        assertEquals(expectedSubject, sbm.getSubject());

        String[] expectedGroups = new String[]{"test group 1", "test group 2", "test group 3"};
        sbm.setGroups(expectedGroups);
        assertEquals(expectedGroups, sbm.getGroups());

        String[] expectedProjects = new String[]{"test project 1", "test project 2", "test project 3"};
        sbm.setProjects(expectedProjects);
        assertEquals(expectedProjects, sbm.getProjects());

        String[] expectedRoles = new String[]{"test role 1", "test role 2", "test role 3"};
        sbm.setRoles(expectedRoles);
        assertEquals(expectedRoles, sbm.getRoles());

        mockMailServerManager.verify();
    }

    public void testGetGroupsFieldSize() throws OperationNotPermittedException, InvalidGroupException
    {
        // No need to pass a MailServerManager
        sbm = new SendBulkMail(null, null, null, null, userUtil, groupManager);

        for (int i = 1; i <= SendBulkMail.MAX_MULTISELECT_SIZE - 1; i++)
        {
            createMockGroup("Test Group " + i);
            //due to the optgroup title in the select element, size = groupcount + 1
            assertEquals(i, sbm.getGroupsFieldSize() - 1);
        }

        createMockGroup("Test Group " + SendBulkMail.MAX_MULTISELECT_SIZE);

        // When there are more than the Max_MultiSelect_Size groups, the size should be Max_MultiSelect_Size
        assertEquals(SendBulkMail.MAX_MULTISELECT_SIZE, sbm.getGroupsFieldSize());
    }

    public void testGetProjectsRolesFieldSize()
    {
        //Prepare mock manager for returning a mock project collection to SendBulkMail
        final List mockProjectList = new ArrayList();
        PermissionManager mockPermissionManager = new MockPermissionManager()
        {

            public Collection getProjects(int permissionId, User user)
            {
                return mockProjectList;
            }
        };
        //Prepare mock service for returning a mock project role collection to SendBulkMail
        final List mockProjectRoleList = new ArrayList();
        ProjectRoleService mockProjectRoleService = new MockProjectRoleService()
        {
            @Override
            public Collection getProjectRoles(User currentUser, ErrorCollection errorCollection)
            {
                return mockProjectRoleList;
            }
        };

        sbm = new SendBulkMail(null, mockPermissionManager, mockProjectRoleService, null, userUtil, groupManager);

        ////
        //Test by incrementing number of projects
        ////
        for (int i = 1; i <= SendBulkMail.MAX_MULTISELECT_SIZE - 1; i++)
        {
            mockProjectList.add("Project " + i);
            sbm.setProjects(toStringArray(mockProjectList));
            assertEquals(i, sbm.getProjectsRolesFieldSize() - 1);
        }

        mockProjectList.clear();
        assertEquals(1, sbm.getProjectsRolesFieldSize());

        ////
        //Test by incrementing number of roles
        ////
        for (int i = 1; i <= SendBulkMail.MAX_MULTISELECT_SIZE - 1; i++)
        {
            mockProjectRoleList.add("Role " + i);
            sbm.setProjects(toStringArray(mockProjectRoleList));
            assertEquals(i, sbm.getProjectsRolesFieldSize() - 1);
        }

        mockProjectRoleList.clear();

        assertEquals(1, sbm.getProjectsRolesFieldSize());
    }

    private String[] toStringArray(List strings)
    {
        String[] strArray = new String[strings.size()];
        for (int i = 0; i < strArray.length; i++)
        {
            strArray[i] = (String) strings.get(i);
        }
        return strArray;
    }

    public void testDoValidationNoMailServer() throws Exception
    {
        Mock mockMailServerManager = new Mock(MailServerManager.class);
        mockMailServerManager.setStrict(true);
        mockMailServerManager.expectAndReturn("getDefaultSMTPMailServer", null);

        sbm = new SendBulkMail((MailServerManager) mockMailServerManager.proxy(), null, null, null, userUtil, groupManager);

        final String result = sbm.execute();
        assertEquals(Action.INPUT, result);
        checkSingleElementCollection(sbm.getErrorMessages(), "No mail server configured.");
        mockMailServerManager.verify();
    }

    public void testDoValidationNoDataProvidedAndGroupsSelected() throws Exception
    {
        Mock mockMailServer = new Mock(SMTPMailServer.class);
        mockMailServer.setStrict(true);

        Mock mockMailServerManager = new Mock(MailServerManager.class);
        mockMailServerManager.setStrict(true);
        mockMailServerManager.expectAndReturn("getDefaultSMTPMailServer", mockMailServer.proxy());

        sbm = new SendBulkMail((MailServerManager) mockMailServerManager.proxy(), null, null, null, userUtil, groupManager);

        sbm.setSendToRoles(false);

        final String result = sbm.execute();
        assertEquals(Action.INPUT, result);

        final Map errors = sbm.getErrors();
        assertEquals(4, errors.size());
        assertEquals("Please select at least one group.", errors.get("sendToRoles"));
        assertEquals("Please specify a subject.", errors.get("subject"));
        assertEquals("Please specify message type.", errors.get("messageType"));
        assertEquals("Please provide a message body.", errors.get("message"));

        mockMailServerManager.verify();
    }

    public void testDoValidationSomeMissingDataAndRolesSelected() throws Exception
    {
        Mock mockMailServer = new Mock(SMTPMailServer.class);
        mockMailServer.setStrict(true);

        Mock mockMailServerManager = new Mock(MailServerManager.class);
        mockMailServerManager.setStrict(true);
        mockMailServerManager.expectAndReturn("getDefaultSMTPMailServer", mockMailServer.proxy());

        sbm = new SendBulkMail((MailServerManager) mockMailServerManager.proxy(), null, null, null, userUtil, groupManager);

        sbm.setSendToRoles(true);

        String result = sbm.execute();
        assertEquals(Action.INPUT, result);

        Map errors = sbm.getErrors();
        assertEquals(4, errors.size());
        assertEquals("Please select at least one project and one role.", errors.get("sendToRoles"));
        assertEquals("Please specify a subject.", errors.get("subject"));
        assertEquals("Please specify message type.", errors.get("messageType"));
        assertEquals("Please provide a message body.", errors.get("message"));

        sbm.setRoles(new String[]{"something"});

        result = sbm.execute();
        assertEquals(Action.INPUT, result);

        mockMailServerManager.verify();

        errors = sbm.getErrors();
        assertEquals(4, errors.size());
        assertEquals("Please select at least one project.", errors.get("sendToRoles"));
        assertEquals("Please specify a subject.", errors.get("subject"));
        assertEquals("Please specify message type.", errors.get("messageType"));
        assertEquals("Please provide a message body.", errors.get("message"));

        sbm.setRoles(null);
        sbm.setProjects(new String[]{"something"});

        mockMailServerManager.verify();

        result = sbm.execute();
        assertEquals(Action.INPUT, result);

        errors = sbm.getErrors();
        assertEquals(4, errors.size());
        assertEquals("Please select at least one role.", errors.get("sendToRoles"));
        assertEquals("Please specify a subject.", errors.get("subject"));
        assertEquals("Please specify message type.", errors.get("messageType"));
        assertEquals("Please provide a message body.", errors.get("message"));

        mockMailServerManager.verify();
    }

    public void testDoValidationChosenGroupHasNoMembers() throws Exception
    {
        // Setup a group
        final Group testGroup = createMockGroup("test group");

        Mock mockMailServer = new Mock(SMTPMailServer.class);
        mockMailServer.setStrict(true);

        Mock mockMailServerManager = new Mock(MailServerManager.class);
        mockMailServerManager.setStrict(true);
        mockMailServerManager.expectAndReturn("getDefaultSMTPMailServer", mockMailServer.proxy());

        sbm = new SendBulkMail((MailServerManager) mockMailServerManager.proxy(), null, null, null, userUtil, groupManager);

        sbm.setSendToRoles(false);

        sbm.setGroups(new String[]{testGroup.getName()});
        sbm.setSubject("Test Subject");
        sbm.setMessageType("Message type");
        sbm.setMessage("Test Message Body.");

        final String result = sbm.execute();
        assertEquals(Action.INPUT, result);

        final Map errors = sbm.getErrors();
        assertEquals(1, errors.size());
        assertEquals("The chosen group(s) have no members.", errors.get("sendToRoles"));

        mockMailServerManager.verify();
    }

    public void testDoValidationChosenProjectRoleHasNoMembers() throws Exception
    {
        final ProjectRole pr = new ProjectRoleImpl(1L, "Test Project Role", "a test project role");
        final ProjectRoleActors actors = new ProjectRoleActorsImpl(null, pr.getId(), Collections.EMPTY_SET);
        ProjectRoleService mockProjectRoleService = new MockProjectRoleService()
        {
            @Override
            public ProjectRole getProjectRole(User currentUser, Long id, ErrorCollection errorCollection)
            {
                return pr;
            }

            @Override
            public ProjectRoleActors getProjectRoleActors(User currentUser, ProjectRole projectRole, Project project, ErrorCollection errorCollection)
            {
                return actors;
            }
        };

        MockProjectManager mockProjectManager = new MockProjectManager();

        Mock mockMailServer = new Mock(SMTPMailServer.class);
        mockMailServer.setStrict(true);

        Mock mockMailServerManager = new Mock(MailServerManager.class);
        mockMailServerManager.setStrict(true);
        mockMailServerManager.expectAndReturn("getDefaultSMTPMailServer", mockMailServer.proxy());

        sbm = new SendBulkMail((MailServerManager) mockMailServerManager.proxy(), null, mockProjectRoleService, mockProjectManager, userUtil, groupManager);

        sbm.setSendToRoles(true);

        sbm.setRoles(new String[]{"1"});
        sbm.setProjects(new String[]{"2"});
        sbm.setSubject("Test Subject");
        sbm.setMessageType("Message type");
        sbm.setMessage("Test Message Body.");

        final String result = sbm.execute();
        assertEquals(Action.INPUT, result);

        final Map errors = sbm.getErrors();
        assertEquals(1, errors.size());
        assertEquals("The chosen project/role combination(s) have no members.", errors.get("sendToRoles"));

        mockMailServerManager.verify();
    }

    public void testDoValidationInvalidReplyToEmailFormat() throws Exception
    {
        // Setup a group
        final Group testGroup = createMockGroup("test group");
        final User testUser = createMockUser("Test User 1");
        addUserToGroup(testUser, testGroup);

        Mock mockMailServer = new Mock(SMTPMailServer.class);
        mockMailServer.setStrict(true);

        Mock mockMailServerManager = new Mock(MailServerManager.class);
        mockMailServerManager.setStrict(true);
        mockMailServerManager.expectAndReturn("getDefaultSMTPMailServer", mockMailServer.proxy());

        sbm = new SendBulkMail((MailServerManager) mockMailServerManager.proxy(), null, null, null, userUtil, groupManager);

        sbm.setSendToRoles(false);

        sbm.setGroups(new String[]{testGroup.getName()});
        sbm.setSubject("Test Subject");
        sbm.setMessageType("Message type");
        sbm.setMessage("Test Message Body.");
        sbm.setReplyTo("bademail");

        final String result = sbm.execute();
        assertEquals(Action.INPUT, result);

        final Map errors = sbm.getErrors();
        assertEquals(1, errors.size());
        assertEquals("Invalid email address format.", errors.get("replyTo"));

        mockMailServerManager.verify();
    }

    private ProjectRoleActors makeMockProjectRoleActors(Collection users) throws RoleActorDoesNotExistException
    {
        Set userSet = new HashSet(users);
        Set actors = new HashSet();
        actors.add(new MockProjectRoleManager.MockRoleActor(null, null, null, userSet, "testType", "testParameter"));
        return new ProjectRoleActorsImpl(null, null, actors);
    }

    public void testGetUsersUsingProjectRoles() throws Exception
    {
        // Setup logged in user
        final User testAdminUser = createMockUser("Test Admin User", "", "admin@email.com");
        JiraTestUtil.loginUser(testAdminUser);

        final ProjectRole pr1 = new ProjectRoleImpl(1L, "Test Project Role 1", "a test project role1");
        final ProjectRole pr2 = new ProjectRoleImpl(2L, "Test Project Role 2", "a test project role2");
        final ProjectRole pr3 = new ProjectRoleImpl(3L, "Test Project Role 3", "a test project role3");
        final Map projectRoleMap = new HashMap();
        projectRoleMap.put(pr1.getId(), pr1);
        projectRoleMap.put(pr2.getId(), pr2);
        projectRoleMap.put(pr3.getId(), pr3);

        User user1 = createMockUser("Test User 1");
        User user2 = createMockUser("Test User 2");
        User user3 = createMockUser("Test User 3");

        final Map projectRoleActorsMap = new HashMap();
        projectRoleActorsMap.put(pr1, makeMockProjectRoleActors(EasyList.build(user1, user2)));
        projectRoleActorsMap.put(pr2, makeMockProjectRoleActors(EasyList.build(user2, user3)));
        projectRoleActorsMap.put(pr3, makeMockProjectRoleActors(Collections.EMPTY_SET));

        ProjectRoleService mockProjectRoleService = new MockProjectRoleService()
        {
            @Override
            public ProjectRole getProjectRole(User currentUser, Long id, ErrorCollection errorCollection)
            {
                return (ProjectRole) projectRoleMap.get(id);
            }

            @Override
            public ProjectRoleActors getProjectRoleActors(User currentUser, ProjectRole projectRole, Project project, ErrorCollection errorCollection)
            {
                return (ProjectRoleActors) projectRoleActorsMap.get(projectRole);
            }
        };

        String expectedSubject = "Test Subject";
        String expectedBody = "Test Message Body";

        Mock mockMailServer = new Mock(SMTPMailServer.class);
        mockMailServer.setStrict(true);
        mockMailServer.expectVoid("send", P.args(new IsAnything()));

        Mock mockMailServerManager = new Mock(MailServerManager.class);
        mockMailServerManager.setStrict(true);
        mockMailServerManager.expectAndReturn("getDefaultSMTPMailServer", mockMailServer.proxy());

        sbm = new SendBulkMail((MailServerManager) mockMailServerManager.proxy(), null, mockProjectRoleService, new MockProjectManager(), userUtil, groupManager);

        sbm.setSendToRoles(true);

        sbm.setProjects(new String[]{"1"});
        sbm.setRoles(new String[]{"1", "2", "3"});
        sbm.setSubject(expectedSubject);
        sbm.setMessageType(NotificationRecipient.MIMETYPE_HTML);
        sbm.setMessage(expectedBody);

        // Let SendBulkMail initialize the users collection
        sbm.execute();

        // Test that the correct users are returned
        final List expectedUsers = EasyList.build(user1, user2, user3);
        assertEquals(expectedUsers, sbm.getUsers());

        mockMailServer.verify();
        mockMailServerManager.verify();
    }

    public void testGetUsersUsingGroups() throws Exception
    {
        // Setup logged in user
        final User testAdminUser = createMockUser("Test Admin User", "", "admin@email.com");
        JiraTestUtil.loginUser(testAdminUser);

        // Setup a group
        final Group testGroup1 = createMockGroup("test group 1");
        final Group testGroup2 = createMockGroup("test group 2");
        final Group testGroup3 = createMockGroup("test group 3");

        // Setup users
        final String testEmail1 = "email1@email.com";
        final User testUser1 = createMockUser("Test User 1", "", testEmail1);

        final String testEmail2 = "email2@email.com";
        final User testUser2 = createMockUser("Test User 2", "", testEmail2);

        final String testEmail3 = "email3@email.com";
        final User testUser3 = createMockUser("Test User 3", "", testEmail3);

        final String testEmail4 = "email4@email.com";
        final User testUser4 = createMockUser("Test User 4", "", testEmail4);

        // Add users to groups
        addUserToGroup(testUser1, testGroup1);
        addUserToGroup(testUser2, testGroup1);
        addUserToGroup(testUser2, testGroup2);
        addUserToGroup(testUser3, testGroup2);
        addUserToGroup(testUser4, testGroup3);

        String expectedSubject = "Test Subject";
        String expectedBody = "Test Message Body";

        Mock mockMailServer = new Mock(SMTPMailServer.class);
        mockMailServer.setStrict(true);
        mockMailServer.expectVoid("send", P.args(new IsAnything()));

        Mock mockMailServerManager = new Mock(MailServerManager.class);
        mockMailServerManager.setStrict(true);
        mockMailServerManager.expectAndReturn("getDefaultSMTPMailServer", mockMailServer.proxy());

        sbm = new SendBulkMail((MailServerManager) mockMailServerManager.proxy(), null, null, null, userUtil, groupManager);

        sbm.setSendToRoles(false);

        sbm.setGroups(new String[]{testGroup1.getName(), testGroup2.getName()});
        sbm.setSubject(expectedSubject);
        sbm.setMessageType(NotificationRecipient.MIMETYPE_HTML);
        sbm.setMessage(expectedBody);

        // Let SendBulkMail initialize the users collection
        sbm.execute();

        // Test that the correct users are returned
        final List expectedUsers = EasyList.build(testUser1, testUser2, testUser3);
        assertEquals(expectedUsers, sbm.getUsers());

        mockMailServer.verify();
        mockMailServerManager.verify();
    }

    private void setupUsersForGroups()
            throws OperationNotPermittedException, InvalidGroupException, InvalidUserException, InvalidCredentialException
    {
        // Setup a group
        testGroup1 = createMockGroup("test group 1");
        testGroup2 = createMockGroup("test group 2");
        testGroup3 = createMockGroup("test group 3");

        // Setup users
        testEmail1 = "email1@email.com";
        final User testUser1 = createMockUser("Test User 1", "", testEmail1);

        testEmail2 = "email2@email.com";
        final User testUser2 = createMockUser("Test User 2", "", testEmail2);

        testEmail3 = "email3@email.com";
        final User testUser3 = createMockUser("Test User 3", "", testEmail3);

        testEmail4 = "email4@email.com";
        final User testUser4 = createMockUser("Test User 4", "", testEmail4);

        // Add users to groups
        addUserToGroup(testUser1, testGroup1);
        addUserToGroup(testUser2, testGroup1);
        addUserToGroup(testUser2, testGroup2);
        addUserToGroup(testUser3, testGroup2);
        addUserToGroup(testUser4, testGroup3);

     }

    private Email setupEmail(String expectedToList, User testAdminUser)
    {
        Email email = new Email(expectedToList);
        email.setFromName(testAdminUser.getName());
        email.setReplyTo(testAdminUser.getEmailAddress());
        expectedMimeType = "text/html";
        email.setMimeType(expectedMimeType);
        expectedSubject = "Test Subject";
        email.setSubject(expectedSubject);
        expectedBody = "Test Message Body";
        email.setBody(expectedBody);
        return email;
    }

    public void testDoExecuteErrorOccurred() throws Exception
    {
        // Setup logged in user
        final User testAdminUser = createMockUser("Test Admin User", "", "admin@email.com");
        JiraTestUtil.loginUser(testAdminUser);

        setupUsersForGroups();

        String expectedToList = testEmail1 + "," + testEmail2 + "," + testEmail3;
        final Email email = setupEmail(expectedToList, testAdminUser);

        Mock mockMailServer = new Mock(SMTPMailServer.class);
        mockMailServer.setStrict(true);

        final String expectedExceptionMessage = "Test Mail Exception.";
        mockMailServer.expectAndThrow("send", P.args(new IsEqual(email)), new MailException(expectedExceptionMessage));

        Mock mockMailServerManager = new Mock(MailServerManager.class);
        mockMailServerManager.setStrict(true);
        mockMailServerManager.expectAndReturn("getDefaultSMTPMailServer", mockMailServer.proxy());

        sbm = new SendBulkMail((MailServerManager) mockMailServerManager.proxy(), null, null, null, userUtil, groupManager);

        sbm.setSendToRoles(false);

        sbm.setGroups(new String[]{testGroup1.getName(), testGroup2.getName()});

        sbm.setSubject(expectedSubject);
        sbm.setMessageType(NotificationRecipient.MIMETYPE_HTML);
        sbm.setMessage(expectedBody);

        final String result = sbm.execute();
        assertEquals(Action.ERROR, result);
        checkSingleElementCollection(sbm.getErrorMessages(), "The error was: " + expectedExceptionMessage);
        assertEquals("<font color=\"bb0000\">FAILED</font> to send email to some of the recipients, please check the log for details.", sbm.getStatus());

        mockMailServer.verify();
        mockMailServerManager.verify();
    }

    public void testDoExecuteNoReplyTo() throws Exception
    {
        // Setup logged in user
        final User testAdminUser = createMockUser("Test Admin User", "", "admin@email.com");
        JiraTestUtil.loginUser(testAdminUser);

        setupUsersForGroups();

        String expectedToList = testEmail1 + "," + testEmail2 + "," + testEmail3;
        Email email = setupEmail(expectedToList, testAdminUser);

        Mock mockMailServer = new Mock(SMTPMailServer.class);
        mockMailServer.setStrict(true);

        final String expectedExceptionMessage = "Test Mail Exception.";
        mockMailServer.expectVoid("send", P.args(new IsEqual(email)));

        Mock mockMailServerManager = new Mock(MailServerManager.class);
        mockMailServerManager.setStrict(true);
        mockMailServerManager.expectAndReturn("getDefaultSMTPMailServer", mockMailServer.proxy());

        sbm = new SendBulkMail((MailServerManager) mockMailServerManager.proxy(), null, null, null, userUtil, groupManager);

        sbm.setSendToRoles(false);

        sbm.setGroups(new String[]{testGroup1.getName(), testGroup2.getName()});
        sbm.setSubject(expectedSubject);
        sbm.setMessageType(NotificationRecipient.MIMETYPE_HTML);
        sbm.setMessage(expectedBody);

        final String result = sbm.execute();
        assertEquals(Action.SUCCESS, result);
        assertEquals("Your message has been sent successfully to the following users:", sbm.getStatus());

        mockMailServer.verify();
        mockMailServerManager.verify();
    }

    public void testDoExecuteWithReplyTo() throws Exception
    {
        // Setup logged in user
        final User testAdminUser = createMockUser("Test Admin User", "", "admin@email.com");
        JiraTestUtil.loginUser(testAdminUser);

        setupUsersForGroups();

        String expectedToList = testEmail1 + "," + testEmail2 + "," + testEmail3;
        String expectedReplyTo = "replyto@email.com";
        Email email = setupEmail(expectedToList, testAdminUser);
        email.setReplyTo(expectedReplyTo);

        Mock mockMailServer = new Mock(SMTPMailServer.class);
        mockMailServer.setStrict(true);

        final String expectedExceptionMessage = "Test Mail Exception.";
        mockMailServer.expectVoid("send", P.args(new IsEqual(email)));

        Mock mockMailServerManager = new Mock(MailServerManager.class);
        mockMailServerManager.setStrict(true);
        mockMailServerManager.expectAndReturn("getDefaultSMTPMailServer", mockMailServer.proxy());

        sbm = new SendBulkMail((MailServerManager) mockMailServerManager.proxy(), null, null, null, userUtil, groupManager);

        sbm.setSendToRoles(false);

        sbm.setGroups(new String[]{testGroup1.getName(), testGroup2.getName()});
        sbm.setSubject(expectedSubject);
        sbm.setMessageType(NotificationRecipient.MIMETYPE_HTML);
        sbm.setMessage(expectedBody);
        sbm.setReplyTo(expectedReplyTo);

        final String result = sbm.execute();
        assertEquals(Action.SUCCESS, result);
        assertEquals("Your message has been sent successfully to the following users:", sbm.getStatus());

        mockMailServer.verify();
        mockMailServerManager.verify();
    }

    public void testDoExecuteByBatch() throws Exception
    {
        // Setup logged in user
        final User testAdminUser = createMockUser("Test Admin User", "", "admin@email.com");
        JiraTestUtil.loginUser(testAdminUser);
        ComponentAccessor.getComponent(PropertiesManager.class).getPropertySet()
                        .setString(APKeys.JIRA_SENDMAIL_RECIPENT_BATCH_SIZE, "3");

        setupUsersForGroups();

        String expectedToListBatch1 = testEmail1 + "," + testEmail2 + "," + testEmail3;
        Email email1 = setupEmail(expectedToListBatch1, testAdminUser);

        String expectedToListBatch2 = testEmail4;
        Email email2 = setupEmail(expectedToListBatch2, testAdminUser);

        Mock mockMailServer = new Mock(SMTPMailServer.class);
        mockMailServer.setStrict(true);

        //the mail server will send two emails because the batch size is 3 and there are 4 recipients
        mockMailServer.expectVoid("send", P.args(new Or(new IsEqual(email1), new IsEqual(email2))));

        Mock mockMailServerManager = new Mock(MailServerManager.class);
        mockMailServerManager.setStrict(true);
        mockMailServerManager.expectAndReturn("getDefaultSMTPMailServer", mockMailServer.proxy());

        sbm = new SendBulkMail((MailServerManager) mockMailServerManager.proxy(), null, null, null, userUtil, groupManager);

        sbm.setSendToRoles(false);

        sbm.setGroups(new String[]{testGroup1.getName(), testGroup2.getName(), testGroup3.getName()});
        sbm.setSubject(expectedSubject);
        sbm.setMessageType(NotificationRecipient.MIMETYPE_HTML);
        sbm.setMessage(expectedBody);

        final String result = sbm.execute();
        assertEquals(Action.SUCCESS, result);
        assertEquals("Your message has been sent successfully to the following users:", sbm.getStatus());

        mockMailServer.verify();
        mockMailServerManager.verify();
    }
}
