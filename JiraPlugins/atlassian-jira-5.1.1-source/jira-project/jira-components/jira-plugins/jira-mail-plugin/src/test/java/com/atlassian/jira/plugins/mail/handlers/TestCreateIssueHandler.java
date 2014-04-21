package com.atlassian.jira.plugins.mail.handlers;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.fields.SummarySystemField;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.action.issue.IssueCreationHelperBean;
import com.atlassian.mail.MailUtils;
import com.google.common.collect.ImmutableList;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.stubbing.answers.Returns;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import static org.junit.Assert.assertEquals;

@Ignore ("This test is much different from the rest of the family, will fix it later")
public class TestCreateIssueHandler extends AbstractTestMessageHandler
{
    private final static String TESTGROUP = "Test Group";
    private static final String X_PRIORITY_HEADER = "X-Priority";
    private static final String PRIORITY_ONE = "1";
    private static final String PRIORITY_TWO = "2";
    private static final String PRIORITY_THREE = "3";
    private static final String PRIORITY_FOUR = "4";
    private static final String PRIORITY_FIVE = "5";

    @Mock
    private ProjectManager projectManager;
    @Mock
    private IssueCreationHelperBean issueCreationHelperBean;
    @Mock
    protected User reporter;


    /**
     * Create a project, user, group, components, permission scheme, priorities
     */
    @Before
    public void setUp() throws Exception
    {
        super.setUp();

        MockGenericValue projectGV = new MockGenericValue("Project");

        worker.addMock(ProjectManager.class, projectManager);
        Mockito.when(projectManager.getProjectByKey("PRJ")).thenReturn(projectGV);

        worker.addMock(IssueCreationHelperBean.class, issueCreationHelperBean);
        worker.addMock(I18nHelper.BeanFactory.class, Mockito.mock(I18nHelper.BeanFactory.class, new Returns(i18nHelper)));
        handler =  new CreateIssueHandler();
        
//        Group g1 = createMockGroup("Test Group");
//        addUserToGroup(u1, g1);
//        addUserToGroup(reporter, g1);
//
//        UtilsForTests.getTestEntity("IssueType", MapBuilder.build("id", "1", "name", "Bug"));
//        UtilsForTests.getTestEntity("FieldScreen", MapBuilder.build("id", WorkflowActionsBean.VIEW_COMMENTASSIGN_ID, "name", "Test Screen 1"));
//        UtilsForTests.getTestEntity("FieldScreen", MapBuilder.build("id", WorkflowActionsBean.VIEW_RESOLVE_ID, "name", "Test Screen 2"));
//
//        ComponentManager.getComponentInstanceOfType(FieldScreenManager.class).refresh();
//
//        GenericValue scheme = JiraTestUtil.setupAndAssociateDefaultPermissionScheme(projectA);
//        ManagerFactory.getPermissionManager().addPermission(Permissions.ASSIGNABLE_USER, scheme, TESTGROUP, GroupDropdown.DESC);
//        ManagerFactory.getPermissionManager().addPermission(Permissions.CREATE_ISSUE, scheme, TESTGROUP, GroupDropdown.DESC);
//
//        //these aren't used by the CreateIssueHandler, but maybe later
//        GenericValue component1 = UtilsForTests.getTestEntity("Component", MapBuilder.build("id", new Long(2000), "name", "comp 1", "project", projectA.getLong("id")));
//        GenericValue component2 = UtilsForTests.getTestEntity("Component", MapBuilder.build("id", new Long(2001), "name", "comp 2", "project", projectA.getLong("id")));
//        GenericValue component3 = UtilsForTests.getTestEntity("Component", MapBuilder.build("id", new Long(2002), "name", "comp 3", "project", projectA.getLong("id")));
//
//        CoreFactory.getAssociationManager().createAssociation(projectA, component1, IssueRelationConstants.COMPONENT);
//        CoreFactory.getAssociationManager().createAssociation(projectA, component2, IssueRelationConstants.COMPONENT);
//        CoreFactory.getAssociationManager().createAssociation(projectA, component3, IssueRelationConstants.COMPONENT);
//
//        UtilsForTests.getTestEntity("Priority", MapBuilder.build("id", PRIORITY_ONE, "name", "Blocker", "sequence", new Long(1)));
//        UtilsForTests.getTestEntity("Priority", MapBuilder.build("id", PRIORITY_TWO, "name", "Critical", "sequence", new Long(2)));
//        UtilsForTests.getTestEntity("Priority", MapBuilder.build("id", PRIORITY_THREE, "name", "Major", "sequence", new Long(3)));
//        UtilsForTests.getTestEntity("Priority", MapBuilder.build("id", PRIORITY_FOUR, "name", "Minor", "sequence", new Long(4)));
//        UtilsForTests.getTestEntity("Priority", MapBuilder.build("id", PRIORITY_FIVE, "name", "Trivial", "sequence", new Long(5)));
//        setDefaultPriority("");
    }

    /*
     * Simple test that creates a message and tests the handler using the the default project owner as the assignee
     */
    @Test
    public void testHandleMessage() throws IOException, MessagingException, GenericEntityException
    {
        Message message = new MimeMessage(Session.getDefaultInstance(new Properties()));
        message.setText(AbstractTestMessageHandler.MESSAGE_STRING);
        message.setSubject(AbstractTestMessageHandler.MESSAGE_SUBJECT);

        handleMessage(message);
    }

    /*
     * Tests that summary of issues created are truncated if too long
     */
    @Test
    public void testSummaryTooLong() throws IOException, MessagingException, GenericEntityException
    {
        Message message = new MimeMessage(Session.getDefaultInstance(new Properties()));
        message.setText(AbstractTestMessageHandler.MESSAGE_STRING);
        //String with 64 characters
        String STRING_64 = "This string is 64 characters long. Testing create issue handler!";
        String STRING_256 = STRING_64 + STRING_64 + STRING_64 + STRING_64; //add summary of length 256 (64 * 4)
        message.setSubject(STRING_256);

        CreateIssueHandler handler = new CreateIssueHandler();
        handler.init(MapBuilder.build("project", "PRJ", "issuetype", "1"), context.getMonitor());
        handler.reporteruserName = AbstractTestMessageHandler.TESTUSER_USERNAME;
        assertEquals(true, handler.handleMessage(message, context));
        Project project = ManagerFactory.getProjectManager().getProjectObjByKey("PRJ");
        for (GenericValue genericValue : ComponentAccessor.getIssueManager().getProjectIssues(project.getGenericValue()))
        {
            Assert.assertEquals(genericValue.get("summary"), STRING_256.substring(0, SummarySystemField.MAX_LEN.intValue() - 3) + "...");
        }
    }

    /*
     * Tests that emails with no subjects are rejected
     */
    @Test
    public void testNoSummary() throws IOException, MessagingException, GenericEntityException
    {
        final Message message = new MimeMessage(Session.getDefaultInstance(new Properties()));
        message.setText(AbstractTestMessageHandler.MESSAGE_STRING);
        message.setSubject("");

        final SimpleTestMessageHandlerExecutionMonitor errorHandler = new SimpleTestMessageHandlerExecutionMonitor();

        final CreateIssueHandler handler = new CreateIssueHandler();
        handler.init(MapBuilder.build("project", "PRJ", "issuetype", "1"), context.getMonitor());
        handler.reporteruserName = AbstractTestMessageHandler.TESTUSER_USERNAME;
        assertEquals(false, handler.handleMessage(message, context));
        Assert.assertEquals("Issue must have a summary. The mail message has an empty or no subject.", errorHandler.getError());
    }

    @Test
    public void testCcWatcherWithSingleUserWhoIsReporter() throws GenericEntityException, MessagingException
    {
        //the reporter should be stripped from the list of e-mails
        _testCreateIssueHandlerWithCcWatcher(true, ImmutableList.of(reporter.getEmailAddress()), null, Collections.<User>emptyList());
    }

    @Test
    public void testCcWatcherWithCCUserWhoIsReporter() throws GenericEntityException, MessagingException
    {
        //the reporter should be stripped from the list of e-mails
        _testCreateIssueHandlerWithCcWatcher(true, ImmutableList.of(u1.getEmailAddress()), ImmutableList.of(reporter.getEmailAddress()), ImmutableList.of(u1));
    }

    @Test
    public void testCcWatcherWithSingleToUser() throws Exception
    {
        _testCreateIssueHandlerWithCcWatcher(true, ImmutableList.of(u1.getEmailAddress()), null, ImmutableList.of(u1));
    }

    @Test
    public void testCcWatcherWithSingleToUserAndSingleCcUser() throws Exception
    {
        User user = createUser("user", "User", "user@localhost");
        _testCreateIssueHandlerWithCcWatcher(true, ImmutableList.of(u1.getEmailAddress()), ImmutableList.of(user.getEmailAddress()), ImmutableList.of(u1, user));
    }

    @Test
    public void testCcWatcherWithSingleToUserAndSingleCcEmail() throws Exception
    {
        _testCreateIssueHandlerWithCcWatcher(true, ImmutableList.of(u1.getEmailAddress()), ImmutableList.of("not@jira.user"), ImmutableList.of(u1));
    }

    @Test
    public void testCcWatcherWithSingleToEmailAndSingleCcUser() throws Exception
    {
        _testCreateIssueHandlerWithCcWatcher(true, ImmutableList.of("not@jira.user"), ImmutableList.of(u1.getEmailAddress()), ImmutableList.of(u1));
    }

    @Test
    public void testCcWatcherWithSingleToEmailAndSingleCcEmail() throws Exception
    {
        _testCreateIssueHandlerWithCcWatcher(true, ImmutableList.of("not1@jira.user"), ImmutableList.of("not2@jira.user"), Collections.<User>emptyList());
    }

    @Test
    public void testCcWatcherWithSingleToEmailAndMultipleCcEmail() throws Exception
    {
        _testCreateIssueHandlerWithCcWatcher(true, ImmutableList.of("not1@jira.user"), ImmutableList.of("not2@jira.user", "not3@jira.user"), Collections.<User>emptyList());
    }

    @Test
    public void testCcWatcherWithSingleToEmailAndMultipleCcUser() throws Exception
    {
        User user1 = createUser("uone", "User 1", "userOne@localhost");
        User user2 = createUser("utwo", "User 2", "userTwo@localhost");
        _testCreateIssueHandlerWithCcWatcher(true, ImmutableList.of("not@jira.user"), ImmutableList.of(user1.getEmailAddress(), user2.getEmailAddress()), ImmutableList.of(user1, user2));
    }

    @Test
    public void testCcWatcherWithSingleToEmailAndMixCcEmail() throws Exception
    {
        User user = createUser("mixed", "Mixed User", "mixed@localhost");
        _testCreateIssueHandlerWithCcWatcher(true, ImmutableList.of("not1@jira.user"), ImmutableList.of("not2@jira.user", user.getEmailAddress()), ImmutableList.of(user));
    }

    @Test
    public void testCcWatcherWithMixToEmailAndMixCcEmail() throws Exception
    {
        User toUser = createUser("mixed1", "Mixed User One", "mixed_one@localhost");
        User ccUser = createUser("mixed2", "Mixed User Two", "mixed_two@localhost");
        _testCreateIssueHandlerWithCcWatcher(true, ImmutableList.of("not1@jira.user", toUser.getEmailAddress()), ImmutableList.of(ccUser.getEmailAddress(), "not2@jira.user"), ImmutableList.of(ccUser, toUser));
    }

    @Test
    public void testNoCcWatcherWithSingleToUser() throws Exception
    {
        _testCreateIssueHandlerWithCcWatcher(false, ImmutableList.of(u1.getEmailAddress()), null, Collections.<User>emptyList());
    }

    @Test
    public void testNoCcWatcherWithSingleToUserAndSingleCcUser() throws Exception
    {
        User user = createUser("user", "User", "user@localhost");
        _testCreateIssueHandlerWithCcWatcher(false, ImmutableList.of(u1.getEmailAddress()), ImmutableList.of(user.getEmailAddress()), Collections.<User>emptyList());
    }

    @Test
    public void testNoCcWatcherWithSingleToUserAndSingleCcEmail() throws Exception
    {
        _testCreateIssueHandlerWithCcWatcher(false, ImmutableList.of(u1.getEmailAddress()), ImmutableList.of("not@jira.user"), Collections.<User>emptyList());
    }

    @Test
    public void testNoCcWatcherWithSingleToEmailAndSingleCcUser() throws Exception
    {
        _testCreateIssueHandlerWithCcWatcher(false, ImmutableList.of("not@jira.user"), ImmutableList.of(u1.getEmailAddress()), Collections.<User>emptyList());
    }

    @Test
    public void testNoCcWatcherWithSingleToEmailAndSingleCcEmail() throws Exception
    {
        _testCreateIssueHandlerWithCcWatcher(false, ImmutableList.of("not1@jira.user"), ImmutableList.of("not2@jira.user"), Collections.<User>emptyList());
    }

    @Test
    public void testNoCcWatcherWithSingleToEmailAndMultipleCcEmail() throws Exception
    {
        _testCreateIssueHandlerWithCcWatcher(false, ImmutableList.of("not1@jira.user"), ImmutableList.of("not2@jira.user", "not3@jira.user"), Collections.<User>emptyList());
    }

    @Test
    public void testNoCcWatcherWithSingleToEmailAndMultipleCcUser() throws Exception
    {
        User user1 = createUser("uone", "User 1", "userOne@localhost");
        User user2 = createUser("utwo", "User 2", "userTwo@localhost");
        _testCreateIssueHandlerWithCcWatcher(false, ImmutableList.of("not@jira.user"), ImmutableList.of(user1.getEmailAddress(), user2.getEmailAddress()), Collections.<User>emptyList());
    }

    @Test
    public void testNoCcWatcherWithSingleToEmailAndMixCcEmail() throws Exception
    {
        User user = createUser("mixed", "Mixed User", "mixed@localhost");
        _testCreateIssueHandlerWithCcWatcher(false, ImmutableList.of("not1@jira.user"), ImmutableList.of("not2@jira.user", user.getEmailAddress()), Collections.<User>emptyList());
    }

    @Test
    public void testNoCcWatcherWithMixToEmailAndMixCcEmail() throws Exception
    {
        User toUser = createUser("mixed1", "Mixed User One", "mixed_one@localhost");
        User ccUser = createUser("mixed2", "Mixed User Two", "mixed_two@localhost");
        _testCreateIssueHandlerWithCcWatcher(false, ImmutableList.of("not1@jira.user", toUser.getEmailAddress()), ImmutableList.of(ccUser.getEmailAddress(), "not2@jira.user"), Collections.<User>emptyList());
    }

    private Message createSimpleMessage(List<String> toAddresses, List<String> ccAddresses) throws MessagingException
    {
        Message message = new MimeMessage(Session.getDefaultInstance(new Properties()));
        message.setText(AbstractTestMessageHandler.MESSAGE_STRING);
        message.setSubject(AbstractTestMessageHandler.MESSAGE_SUBJECT);
        message.setRecipients(Message.RecipientType.TO, parseAddresses(toAddresses));
        if (ccAddresses != null)
        {
            message.setRecipients(Message.RecipientType.CC, parseAddresses(ccAddresses));
        }
        return message;
    }

    private Address[] parseAddresses(List<String> toAddresses) throws AddressException
    {
        Address[] addresses = new Address[toAddresses.size()];
        for (int i = 0; i < toAddresses.size(); i++)
        {
            final String address = toAddresses.get(i);
            addresses[i] = new InternetAddress(address);
        }
        return addresses;
    }

    private User createUser(String username, String fullname, String email) throws OperationNotPermittedException,
            InvalidUserException, InvalidCredentialException
    {
//        return  createMockUser(username, fullname, email);
        return null;
    }

    private void _testCreateIssueHandlerWithCcWatcher(boolean isCcWatcher, List<String> toAddresses, List<String> ccAddresses, Collection<User> expectedWatchersList)
            throws GenericEntityException, MessagingException
    {
        Set<User> expectedWatchers = expectedWatchersList != null ? new HashSet<User>(expectedWatchersList) : Collections.<User>emptySet();
        Message message = createSimpleMessage(toAddresses, ccAddresses);

        IssueManager issueManager = ComponentAccessor.getIssueManager();

        //assert that initially no issues exist
        Collection issueIdsBefore = issueManager.getIssueIdsForProject(projectA.getLong("id"));
        Assert.assertTrue(issueIdsBefore.isEmpty());

        //setup the message handler
        SimpleTestMessageHandlerExecutionMonitor errorHandler = new SimpleTestMessageHandlerExecutionMonitor();
        CreateIssueHandler handler = new CreateIssueHandler();
        handler.init(MapBuilder.build("project", "PRJ", "issuetype", "1", "reporterusername",
                AbstractTestMessageHandler.REPORTER_USERNAME, "ccwatcher", String.valueOf(isCcWatcher)), context.getMonitor());

        //handle the message and assert no errors
        assertEquals(true, handler.handleMessage(message, context));
        Assert.assertFalse(errorHandler.hasErrors());

        //assert that a new issue was created
        Collection issueIdsAfter = issueManager.getIssueIdsForProject(projectA.getLong("id"));
        Assert.assertEquals(1, issueIdsAfter.size());

        Long issueId = (Long) issueIdsAfter.iterator().next();
        MutableIssue issueObj = issueManager.getIssueObject(issueId);
        List<User> actualWatchers = issueManager.getWatchers(issueObj);
        //assert the created issues watchers match the expected users.
        Assert.assertEquals(expectedWatchers.size(), actualWatchers.size());
        Assert.assertEquals(expectedWatchers, new HashSet<User>(actualWatchers));
    }

    @Test
    public void testMailWithPrecedenceBulkHeader() throws MessagingException, GenericEntityException
    {
        _testMailWithPrecedenceBulkHeader();
    }

    @Test
    public void testMailWithIllegalPrecedenceBulkHeader() throws Exception
    {
        _testMailWithIllegalPrecedenceBulkHeader();
    }

    @Test
    public void testMailWithDeliveryStatusHeader() throws MessagingException, GenericEntityException
    {
        _testMailWithDeliveryStatusHeader();
    }

    @Test
    public void testMailWithAutoSubmittedHeader() throws MessagingException, GenericEntityException
    {
        _testMailWithAutoSubmittedHeader();
    }

    /*
     * No Default Priority, No X-priority Expected priority: 3 (middle priority) - NOTE: logs error
     */
    @Test
    public void testGetPriorityNoDefaultNoXPriority() throws MessagingException, GenericEntityException
    {
        _testGetPriority("", null, PRIORITY_THREE);
    }

    /*
     * No Default Priority, X-priority of 1 Expected priority: 1
     */
    @Test
    public void testGetPriorityNoDefaultYesXPriority() throws MessagingException, GenericEntityException
    {
        _testGetPriority("", PRIORITY_ONE, PRIORITY_ONE);
    }

    /*
     * No Default Priority, X-priority NULL Expected priority: 3 (middle priority) - NOTE: logs error
     */
    @Test
    public void testGetPriorityNoDefaultNullXPriority() throws MessagingException, GenericEntityException
    {
        _testGetPriority("", "", PRIORITY_THREE);
    }

    /*
     * Default Priority 5, No X-priority Expected priority: 5
     */
    @Test
    public void testGetPriorityYesDefaultNoXPriority() throws MessagingException, GenericEntityException
    {
        _testGetPriority(PRIORITY_FIVE, null, PRIORITY_FIVE);
    }

    /*
     * Default Priority 5, X-priority 1 Expected priority: 5
     */
    @Test
    public void testGetPriorityYesDefaultYesXPriority() throws MessagingException, GenericEntityException
    {
        _testGetPriority(PRIORITY_FIVE, PRIORITY_ONE, PRIORITY_ONE);
    }

    /*
     * Default Priority 5, X-priority NULL Expected priority: 5
     */
    @Test
    public void testGetPriorityYesDefaultNullXPriority() throws MessagingException, GenericEntityException
    {
        _testGetPriority(PRIORITY_FIVE, "", PRIORITY_FIVE);
    }

    //--------------------------------------------------------------------------------------------------- Helper Methods

    public void _testGetPriority(String defaultPriority, String xPriority, String expectedPriority)
            throws MessagingException, GenericEntityException
    {
        Message message = new MimeMessage(Session.getDefaultInstance(new Properties()));
        message.setText(AbstractTestMessageHandler.MESSAGE_STRING);
        message.setSubject(AbstractTestMessageHandler.MESSAGE_SUBJECT);
        if (xPriority != null)
        {
            message.setHeader(X_PRIORITY_HEADER, xPriority);
        }
        setDefaultPriority(defaultPriority);

        handleMessage(message);

        List<GenericValue> projectIssues = ComponentAccessor.getIssueManager().getProjectIssues(projectA);
        Assert.assertEquals(1, projectIssues.size());
        for (GenericValue projectIssue : projectIssues)
        {
            Assert.assertEquals(expectedPriority, projectIssue.get("priority"));
        }
    }

    private void handleMessage(Message message) throws MessagingException
    {
        CreateIssueHandler handler = new CreateIssueHandler();
        handler.init(MapBuilder.build("project", "PRJ", "issuetype", "1"), context.getMonitor());

        handler.reporteruserName = AbstractTestMessageHandler.TESTUSER_USERNAME;
        assertEquals(true, handler.handleMessage(message, context));
    }

    public void setDefaultPriority(String priority)
    {
        ComponentAccessor.getApplicationProperties().setString(APKeys.JIRA_CONSTANT_DEFAULT_PRIORITY, priority);
    }

    // JRA13720 a number of tests that create issues from a number of emails created by various email clients...
    // note that the code that tests content for equality normalizes all strings by replacing crnl combos with nl.
    @Test
    public void testCreateIssueFromFileThunderbirdHtmlImageAttachment() throws Exception
    {
        final String filename = "ThunderbirdHtmlImageAttachment.msg";
        final String summary = "Thunderbird Html ImageAttachment";
        final String description = "articipants:[1]Anton Mazkovoi [Atlassian], [2]Chris Mountford [Atlassian], [3]Eddie Kua [Atlassian], [4]John Tang, [5]Maxim Dyuzhinov, [6]Michael Tokar [Atlassian], [7]Neal Applebaum, [8]Nick Menere [Atlassian] and [9]Terry [Atlassian]\n"
                + "Since last comment:\t1 week, 6 days ago\n"
                + "Labels:\t\n"
                + "----------------------------------------------------------------------------------------\n"
                + "[1] http://jira.atlassian.com/secure/IssueNavigator.jspa?reset=true&customfield_10150=anton@atlassian.com\n"
                + "[2] http://jira.atlassian.com/secure/IssueNavigator.jspa?reset=true&customfield_10150=chris@atlassian.com\n"
                + "[3] http://jira.atlassian.com/secure/IssueNavigator.jspa?reset=true&customfield_10150=ekua\n"
                + "[4] http://jira.atlassian.com/secure/IssueNavigator.jspa?reset=true&customfield_10150=johntang\n"
                + "[5] http://jira.atlassian.com/secure/IssueNavigator.jspa?reset=true&customfield_10150=maximd\n"
                + "[6] http://jira.atlassian.com/secure/IssueNavigator.jspa?reset=true&customfield_10150=mtokar\n"
                + "[7] http://jira.atlassian.com/secure/IssueNavigator.jspa?reset=true&customfield_10150=napplebaum\n"
                + "[8] http://jira.atlassian.com/secure/IssueNavigator.jspa?reset=true&customfield_10150=nick.menere\n"
                + "[9] http://jira.atlassian.com/secure/IssueNavigator.jspa?reset=true&customfield_10150=terry.ooi";
        this.createIssueFromFileWithImageAttachment(filename, summary, description);
    }

    @Test
    public void testCreateIssueFromFileThunderbirdHtmlAndPlainTextBinaryAttachment() throws Exception
    {
        final String filename = "ThunderbirdHtmlOnlyBinaryAttachment.msg";
        final String summary = "Thunderbird HtmlOnly BinaryAttachment";
        final String description = "com.atlassian.jira.service.services.mail.MailQueueService\n"
                + "\t1\t[1]Edit\n"
                + "\n"
                + "CreateIssuesFromDir\n"
                + "com.atlassian.jira.service.services.file.FileService";
        this.createIssueFromFileWithBinaryAttachment(filename, summary, description);
    }

    @Test
    public void testCreateIssueFromFileThunderbirdHtmlAndPlainTextImageAttachment() throws Exception
    {
        final String filename = "ThunderbirdHtmlAndPlainTextImageAttachment.msg";
        final String summary = "Thunderbird HtmlAndPlain ImageAttachment";
        final String description = "    *Votes:*  \t 1\n"
                + "*Watchers:* \t5\n"
                + "\n"
                + "*Operations*";
        this.createIssueFromFileWithImageAttachment(filename, summary, description);
    }

    @Test
    public void testCreateIssueFromFileThunderbirdHtmlAndPlainTextInlineImage() throws Exception
    {
        final String filename = "ThunderbirdHtmlAndPlainTextInlineImage.msg";
        final String summary = "Thunderbird HtmlAndPlain InlineImage";
        final String description = "BEFORE IMAGE\n"
                + "\n"
                + "AFTER IMAGE";
        this.createIssueFromFileWithImageAttachment(filename, summary, description);
    }

    @Test
    public void testCreateIssueFromFileThunderbirdHtmlBinaryAttachment() throws Exception
    {
        final String filename = "ThunderbirdHtmlOnlyBinaryAttachment.msg";
        final String summary = "Thunderbird HtmlOnly BinaryAttachment";
        final String description = "com.atlassian.jira.service.services.mail.MailQueueService\n"
                + "\t1\t[1]Edit\n"
                + "\n"
                + "CreateIssuesFromDir\n"
                + "com.atlassian.jira.service.services.file.FileService\n"
                + "----------------------------------------------------------------------------------------";
        this.createIssueFromFileWithBinaryAttachment(filename, summary, description);
    }

    @Test
    public void testCreateIssueFromFileThunderbirdHtmlInlineImage() throws Exception
    {
        final String filename = "ThunderbirdPlainTextImageAttachment.msg";
        final String summary = "Thunderbird PlainText ImageAttachment";
        final String description = "   \n"
                + "*orter:* \tMaxim Dyuzhinov \n"
                + "<http://jira.atlassian.com/secure/ViewProfile.jspa?name=maximd>\n"
                + "*Votes:* \t1\n"
                + "*Watchers:* \t5\n"
                + "\n"
                + "*Operations*\n"
                + "\n"
                + "\n"
                + "If you were logged in \n"
                + "<http://jira.atlassian.com/secure/Dashboard.jspa?os_destination=%2Fbrowse%2FJRA-13720> \n"
                + "you would be able to see more operations.\n"
                + "The attached image should be lost.";
        this.createIssueFromFileWithNoAttachments(filename, summary, description);
    }

    @Test
    public void testCreateIssueFromFileThunderbirdPlainTextImageAttachment() throws Exception
    {
        final String filename = "ThunderbirdPlainTextImageAttachment.msg";
        final String summary = "Thunderbird PlainText ImageAttachment";
        final String description = "   \n"
                + "*orter:* \tMaxim Dyuzhinov \n"
                + "<http://jira.atlassian.com/secure/ViewProfile.jspa?name=maximd>\n"
                + "*Votes:* \t1\n"
                + "*Watchers:* \t5\n"
                + "\n"
                + "*Operations*";
        this.createIssueFromFileWithNoAttachments(filename, summary, description);
    }

    @Test
    public void testCreateIssueFromFileThunderbirdPlainTextBinaryAttachment() throws Exception
    {
        final String filename = "ThunderbirdPlainTextOnlyBinaryAttachment.msg";
        final String summary = "Thunderbird PlainTextOnly BinaryAttachment";
        final String description = "PLAIN TEXT";
        this.createIssueFromFileWithBinaryAttachment(filename, summary, description);
    }

    @Test
    public void testCreateIssueFromFileThunderbirdHtmlWithPlainTextAttachment() throws Exception
    {
        final String filename = "ThunderbirdHtmlWithPlainTextAttachment.msg";
        final String summary = "ThunderbirdHtmlWithPlainTextAttachment";
        final String description = "Html\n"
                + "\n"
                + "*Bold*\n"
                + "\n"
                + "/Italics/\n"
                + "\n"
                + "This plain text file should be kept...";
        this.createIssueFromFileWithPlainTextAttachment(filename, summary, description);
    }

    @Test
    public void testCreateIssueFromFileThunderbirdHtmlWithHtmlAttachment() throws Exception
    {
        final String filename = "ThunderbirdHtmlWithHtmlAttachment.msg";
        final String summary = "ThunderbirdHtmlWithHtmlAttachment";
        final String description = "*BOLD*\n"
                + "/italics/\n"
                + "\n"
                + "A very simple html file...";
        this.createIssueFromFileWithHtmlAttachment(filename, summary, description);
    }

    @Test
    public void testCreateIssueFromFileLotusNotesHtmlAttachedImage() throws Exception
    {
        final String filename = "LotusNotesHtmlImageAttached.msg";
        final String summary = "LotusNotesHtmlImageAttached";
        final String description = "_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _\n"
                + "\n"
                + "The information contained in this email is intended for the named recipient(s) \n"
                + "only. It may contain private, confidential, copyright or legally privileged \n"
                + "information.  If you are not the intended recipient or you have received this \n"
                + "email by mistake, please reply to the author and delete this email immediately. \n"
                + "You must not copy, print, forward or distribute this email, nor place reliance \n"
                + "on its contents. This email and any attachment have been virus scanned. However, \n"
                + "you are requested to conduct a virus scan as well.  No liability is accepted \n"
                + "for any loss or damage resulting from a computer virus, or resulting from a delay\n"
                + "or defect in transmission of this email or any attached file. This email does not \n"
                + "constitute a representation by the Atlassian Test unless the author is legally \n"
                + "entitled to do so.\n"
                + "\n"
                + "\n"
                + "\n";
        this.createIssueFromFileWithImageAttachment(filename, summary, description);
    }

    @Test
    public void testCreateIssueFromFileLotusNotesHtmlInlineImage() throws Exception
    {
        final String filename = "LotusNotesHtmlInlineImage.msg";
        final String summary = "LotusNotes-html-iinlineImage";
        final String description = "Html Email with an inline image\n"
                + "\n"
                + "BEFORE IMAGE\n"
                + "\n"
                + "\n"
                + "\n"
                + "\n"
                + "AFTER IMAGE\n"
                + "_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _\n"
                + "\n"
                + "The information contained in this email is intended for the named recipient(s) \n"
                + "only. It may contain private, confidential, copyright or legally privileged \n"
                + "information.  If you are not the intended recipient or you have received this \n"
                + "email by mistake, please reply to the author and delete this email immediately. \n"
                + "You must not copy, print, forward or distribute this email, nor place reliance \n"
                + "on its contents. This email and any attachment have been virus scanned. However, \n"
                + "you are requested to conduct a virus scan as well.  No liability is accepted \n"
                + "for any loss or damage resulting from a computer virus, or resulting from a delay\n"
                + "or defect in transmission of this email or any attached file. This email does not \n"
                + "constitute a representation by the Atlassian Test unless the author is legally \n"
                + "entitled to do so.\n"
                + "\n"
                + "\n"
                + "\n";
        this.createIssueFromFileWithImageAttachment(filename, summary, description);
    }

    @Test
    public void testCreateIssueFromFileLotusNotesHtmlBinaryAttachment() throws Exception
    {
        final String filename = "LotusNotesHtmlBinaryAttachment.msg";
        final String summary = "LotusNotesHtmlBinaryAttachment";
        final String description = "here u are: \n"
                + "\n"
                + "_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _\n"
                + "\n"
                + "The information contained in this email is intended for the named recipient(s) \n"
                + "only. It may contain private, confidential, copyright or legally privileged \n"
                + "information.  If you are not the intended recipient or you have received this \n"
                + "email by mistake, please reply to the author and delete this email immediately. \n"
                + "You must not copy, print, forward or distribute this email, nor place reliance \n"
                + "on its contents. This email and any attachment have been virus scanned. However, \n"
                + "you are requested to conduct a virus scan as well.  No liability is accepted \n"
                + "for any loss or damage resulting from a computer virus, or resulting from a delay\n"
                + "or defect in transmission of this email or any attached file. This email does not \n"
                + "constitute a representation by the Atlassian Test unless the author is legally \n"
                + "entitled to do so.\n"
                + "\n"
                + "\n"
                + "\n";
        this.createIssueFromFileWithBinaryAttachment(filename, summary, description);
    }

    @Test
    public void testCreateIssueFromFileOutlookPlainText() throws Exception
    {
        final String filename = "OutlookPlainText.msg";
        final String summary = "Outlook PlainText";
        final String description = "Plain Text - no html markup.";
        this.createIssueFromFileWithNoAttachments(filename, summary, description);
    }

    @Test
    public void testCreateIssueFromFileOutlookHtml() throws Exception
    {
        final String filename = "OutlookHtml.msg";
        final String summary = "Outlook Html";
        final String description = "BOLD\n"
                + "\n"
                + "Italics\n"
                + "\n"
                + "Underlined";
        this.createIssueFromFileWithNoAttachments(filename, summary, description);
    }

    @Test
    public void testCreateIssueFromFileOutlookHtmlImageAttached() throws Exception
    {
        final String filename = "OutlookHtmlImageAttached.msg";
        final String summary = "Outlook Html ImageAttached";
        final String description = "BOLD\n"
                + "\n"
                + "Image attached\n"
                + "\n"
                + "Underlined";
        this.createIssueFromFileWithImageAttachment(filename, summary, description);
    }

    @Test
    public void testCreateIssueFromFileOutlookHtmlInlineImage() throws Exception
    {
        final String filename = "OutlookHtmlInlineImage.msg";
        final String summary = "Outlook Html InlineImage";
        final String description = "Before Inline Image (Bold)";
        this.createIssueFromFileWithImageAttachment(filename, summary, description);
    }

    @Test
    public void testCreateIssueFromFileOutlookHtmlBinaryAttachment() throws Exception
    {
        final String filename = "OutlookHtmlBinaryAttachment.msg";
        final String summary = "Outlook Html BinaryAttachment";
        final String description = "BOLD\n"
                + "\n"
                + "Underlined";
        this.createIssueFromFileWithBinaryAttachment(filename, summary, description);
    }

    @Test
    public void testCreateIssueFromFileOutlookHtmlWithPlainTextAttachment() throws Exception
    {
        final String filename = "OutlookHtmlWithPlainTextAttachment.msg";
        final String summary = "OutlookHtmlWithPlainTextAttachment";
        final String description = "Bold\n"
                + "\n"
                + "italics\n"
                + "\n"
                + "plain.txt file is attached.";
        this.createIssueFromFileWithPlainTextAttachment(filename, summary, description);
    }

    @Test
    public void testCreateIssueFromFileOutlookHtmlWithHtmlAttachment() throws Exception
    {
        final String filename = "OutlookHtmlWithHtmlAttachment.msg";
        final String summary = "OutlookHtmlWithHtmlAttachment";
        final String description = "Bold\n"
                + "\n"
                + "italics\n"
                + "\n"
                + "Html file called (page.html) is attached.";
        // unfortunately outlook reports html attachments as plain text...hence we test for plain and not html
        this.createIssueFromFileWithPlainTextAttachment(filename, summary, description);
    }

    @Test
    public void testCreateIssueFromFileGmailPlainText() throws Exception
    {
        final String filename = "GmailPlainText.msg";
        final String summary = "Gmail PlainText";
        final String description = "Plain Text from Gmail.";
        this.createIssueFromFileWithNoAttachments(filename, summary, description);
    }

    @Test
    public void testCreateIssueFromFileGmailHtml() throws Exception
    {
        final String filename = "GmailHtml.msg";
        final String summary = "Gmail Html";
        final String description = "*BOLD\n"
                + "\n"
                + "**Italics**\n"
                + "\n"
                + "**Underlined**\n"
                + "*";
        this.createIssueFromFileWithNoAttachments(filename, summary, description);
    }

    @Test
    public void testCreateIssueFromFileGmailHtmlImageAttached() throws Exception
    {
        final String filename = "GmailHtmlImageAttached.msg";
        final String summary = "Gmail Html ImageAttached";
        final String description = "*BOLD\n"
                + "\n"
                + "**Italics**\n"
                + "\n"
                + "**Underlined**\n"
                + "*";
        this.createIssueFromFileWithImageAttachment(filename, summary, description);
    }

    @Test
    public void testCreateIssueFromFileGmailHtmlInlineImage() throws Exception
    {
        // cant create gmail mail with inline image.
    }

    @Test
    public void testCreateIssueFromFileGmailHtmlBinaryAttached() throws Exception
    {
        final String filename = "GmailHtmlBinaryattachment.msg";
        final String summary = "Gmail Html Binary attachment";
        final String description = "*BOLD\n"
                + "\n"
                + "**Italics**\n"
                + "\n"
                + "**Underlined**\n"
                + "*";
        this.createIssueFromFileWithBinaryAttachment(filename, summary, description);
    }

    @Test
    public void testCreateIssueFromFileGMailHtmlWithPlainTextAttachment() throws Exception
    {
        final String filename = "GMailHtmlWithPlainTextAttachment.msg";
        final String summary = "GMailHtmlWithPlainTextAttachment";
        final String description = "*BOLD*\n"
                + "\n"
                + "*Italics*\n"
                + "\n"
                + "-- \n"
                + "mP";
        this.createIssueFromFileWithPlainTextAttachment(filename, summary, description);
    }

    @Test
    public void testCreateIssueFromFileGMailHtmlWithHtmlAttachment() throws Exception
    {
        final String filename = "GMailHtmlWithHtmlAttachment.msg";
        final String summary = "GMailHtmlWithHtmlAttachment";
        final String description = "BOLD\n"
                + "\n"
                + "The html page attachment should be kept...\n"
                + "\n"
                + "-- \n"
                + "mP";
        this.createIssueFromFileWithHtmlAttachment(filename, summary, description);
    }

    @Test
    public void testCreateIssueFromFileEvolutionPlainText() throws Exception
    {
        final String filename = "EvolutionPlainText.msg";
        final String summary = "EvolutionPlainText";
        final String description = "This is a plain text email";
        this.createIssueFromFileWithNoAttachments(filename, summary, description);
    }

    @Test
    public void testCreateIssueFromFileEvolutionHtml() throws Exception
    {
        final String filename = "EvolutionHtml.msg";
        final String summary = "Evolution Html";
        final String description = "BOLD underlineditalics\n"
                + "";
        this.createIssueFromFileWithNoAttachments(filename, summary, description);
    }

    @Test
    public void testCreateIssueFromFileEvolutionHtmlInlineImage() throws Exception
    {
        final String filename = "EvolutionHtmlInlineImage.msg";
        final String summary = "Evolution Html InlineImage";
        final String description = "Before Image (bold)  After image(italics)";
        this.createIssueFromFileWithImageAttachment(filename, summary, description);
    }

    @Test
    public void testCreateIssueFromFileEvolutionImageAttached() throws Exception
    {
        final String filename = "EvolutionHtmlImageAttached.msg";
        final String summary = "Evolution Html ImageAttached";
        final String description = "\n"
                + "Before Image (bold) $$$ After image(italics)";
        this.createIssueFromFileWithImageAttachment(filename, summary, description);
    }

    @Test
    public void testCreateIssueFromFileEvolutionHtmlBinaryAttached() throws Exception
    {
        final String filename = "EvolutionHtmlBinaryAttachment.msg";
        final String summary = "Evolution Html Binary Attachment";
        final String description = "\n"
                + "Bindary with attachment and body";
        this.createIssueFromFileWithBinaryAttachment(filename, summary, description);
    }

    @Test
    public void testCreateIssueFromFileEvolutionHtmlWithPlainTextAttachment() throws Exception
    {
        final String filename = "EvolutionHtmlWithPlainTextAttachment.msg";
        final String summary = "EvolutionHtmlWithPlainTextAttachment";
        final String description = "This is an HTML email:\n"
                + "\n"
                + "BOLD\n"
                + "italics!\n"
                + "plain text\n"
                + "\n"
                + "Plain text attachment to follow.";
        this.createIssueFromFileWithPlainTextAttachment(filename, summary, description);
    }

    @Test
    public void testCreateIssueFromFileEvolutionHtmlWithHtmlAttachment() throws Exception
    {
        final String filename = "EvolutionHtmlWithHtmlAttachment.msg";
        final String summary = "EvolutionHtmlWithHtmlAttachment";
        final String description = "\n"
                + "Also has a html attachment";
        this.createIssueFromFileWithHtmlAttachment(filename, summary, description);
    }

    @Test
    public void testCreateIssueFromFileEvolutionPlainTextWithPlainTextAttachment() throws Exception
    {
        final String filename = "EvolutionPlainTextWithPlainTextAttachment.msg";
        final String summary = "EvolutionPlainTextWithPlainTextAttachment";
        final String description = "Plain text in message\n"
                + "\n"
                + "Plain text attachment should follow";
        this.createIssueFromFileWithPlainTextAttachment(filename, summary, description);
    }

    @Test
    public void testCreateIssueFromFileEvolutionPlainTextWithHtmlAttachment() throws Exception
    {
        final String filename = "EvolutionPlainTextWithHtmlAttachment.msg";
        final String summary = "EvolutionPlainTextWithHtmlAttachment";
        final String description = "Plain text.\n"
                + "There should be an HTML attachment here.";
        this.createIssueFromFileWithHtmlAttachment(filename, summary, description);
    }

    @Test
    public void testCreateIssueFromFileEvolutionPlainTextWithImageAttachment() throws Exception
    {
        final String filename = "EvolutionPlainTextWithImageAttachment.msg";
        final String summary = "EvolutionPlainTextWithImageAttachment";
        final String description = "Plain text.\n"
                + "Image attachment should follow.";
        this.createIssueFromFileWithImageAttachment(filename, summary, description);
    }

    @Test
    public void testCreateIssueFromFileEvolutionPlainTextWithBinaryAttachment() throws Exception
    {
        final String filename = "EvolutionPlainTextWithBinaryAttachment.msg";
        final String summary = "EvolutionPlainTextWithBinaryAttachment";
        final String description = "Plain text here.\n"
                + "Binary attachment to follow.";
        this.createIssueFromFileWithBinaryAttachment(filename, summary, description);
    }

    @Test
    public void testCreateIssueFromFileAppleMailPlainText() throws Exception
    {
        final String filename = "AppleMailText.msg";
        final String summary = "Apple mail Plain Text";
        final String description = "Plain\n"
                + "\n"
                + "Was Bold\n"
                + "\n"
                + "Was Underlined";
        this.createIssueFromFileWithNoAttachments(filename, summary, description);
    }

    @Test
    public void testCreateIssueFromFileAppleMailHtml() throws Exception
    {
        final String filename = "AppleMailHtml.msg";
        final String summary = "AppleMailHtml";
        final String description = "Html email\n"
                + "\n"
                + "BOLD\n"
                + "\n"
                + "Plain\n"
                + "\n"
                + "Italics\n"
                + "\n"
                + "Underline\n"
                + "\n"
                + "Big Font";
        this.createIssueFromFileWithNoAttachments(filename, summary, description);
    }

    @Test
    public void testCreateIssueFromFileAppleMailHtmlInlineImage() throws Exception
    {
        final String filename = "AppleMailHtmlInlineImage.msg";
        final String summary = "AppleMailHtmlInlineImage";
        final String description = "Before Image(bold)\n"
                + "\n"
                + "\n"
                + "Italics(after)\n"
                + "\n"
                + "NB that the content type & filename within the inline image have been changed to image.jpg...";
        this.createIssueFromFileWithImageAttachment(filename, summary, description);
    }

    @Test
    public void testCreateIssueFromFileAppleMailHtmlBinaryAttached() throws Exception
    {
        final String filename = "AppleMailHtmlBinaryAttachment.msg";
        final String summary = "AppleMail Html BinaryAttachment";
        final String description = "Bold\n"
                + "\n"
                + "Italics\n"
                + "\n"
                + "Underlined\n"
                + "\n"
                + "Binary attachment\n"
                + "\n"
                + "nb: The content-type from application/macbinary to application/octet-stream for testing purposes...";
        this.createIssueFromFileWithBinaryAttachment(filename, summary, description);
    }

    @Test
    public void testCreateIssueFromFileAppleMailWithPlainTextAttachment() throws Exception
    {
        final String filename = "AppleMailWithPlainTextAttachment.msg";
        final String summary = "AppleMailWithPlainTextAttachment";
        final String description = "also has Plain Text attachment";
        this.createIssueFromFileWithPlainTextAttachment(filename, summary, description);
    }

    @Test
    public void testCreateIssueFromFileAppleMailHtmlWithHtmlFileAttachment() throws Exception
    {
        final String filename = "AppleMailHtmlWithHtmlFileAttachment.msg";
        final String summary = "AppleMailHtmlWithHtmlFileAttachment";
        final String description = "Also has html attachment";
        this.createIssueFromFileWithHtmlAttachment(filename, summary, description);
    }

    /**
     * The test methods named testCreateIssueFromFileXXX (where XXX is the name of a mail client aka Gmail/Evolution
     * etc) simply pass in the filename that is a file that contains the email message content after which this method
     * does the following.
     * <p/>
     * <ul> <li>creates an issue from a file read from disk</li> <li>validates no errors occured along the way</li>
     * <li>validates that the issue now exists</li> </ul>
     *
     * @throws Exception any exception that can occur along the way which should record a failure for the current test.
     */
    void createIssueFromFileWithImageAttachment(final String filename, final String summary, final String description)
            throws Exception
    {
        this.createIssueFromFile(filename, summary, description, true, false, false, false);
    }

    void createIssueFromFileWithBinaryAttachment(final String filename, final String summary, final String description)
            throws Exception
    {
        this.createIssueFromFile(filename, summary, description, false, true, false, false);
    }

    void createIssueFromFileWithPlainTextAttachment(final String filename, final String summary, final String description)
            throws Exception
    {
        this.createIssueFromFile(filename, summary, description, false, false, true, false);
    }

    void createIssueFromFileWithHtmlAttachment(final String filename, final String summary, final String description)
            throws Exception
    {
        this.createIssueFromFile(filename, summary, description, false, false, false, true);
    }

    void createIssueFromFileWithNoAttachments(final String filename, final String summary, final String description)
            throws Exception
    {
        this.createIssueFromFile(filename, summary, description, false, false, false, false);
    }

    final static String ATTACHMENT_DIRECTORY =
            System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + "jira-testrun-attachments" +
                    System.currentTimeMillis();

    void createIssueFromFile(final String filename, final String expectedSummary, final String expectedDescription,
            final boolean hasImageAttachment, final boolean hasBinaryAttachment, final boolean hasPlainTextAttachment,
            final boolean hasHtmlAttachment)
            throws Exception
    {
        Assert.assertNotNull("createIssueFromFile filename", filename);
        Assert.assertFalse("createIssueFromFile filename is empty", filename.length() == 0);

        // make sure attachments are allowed...
        applicationProperties.setOption(APKeys.JIRA_OPTION_ALLOWATTACHMENTS, true);
        applicationProperties.setString(APKeys.JIRA_PATH_ATTACHMENTS, ATTACHMENT_DIRECTORY);

        // first create the message...
        final Message message = HandlerTestUtil.createMessageFromFile(filename);
        Assert.assertNotNull(message);
        HandlerTestUtil.assertSubjectNotEmpty(message);
        HandlerTestUtil.assertSubjectNotEmpty(message);

        // make sure no issues initially exist.
        final Collection existingIssueIds = this.getAllIssuesForProject();
        Assert.assertEquals("no issues should exist prior to inserting this new one..., existing issue ids: " + existingIssueIds, 0, existingIssueIds.size());

        //setup the message handler
        final SimpleTestMessageHandlerExecutionMonitor errorHandler = new SimpleTestMessageHandlerExecutionMonitor();
        final CreateIssueHandler handler = new CreateIssueHandler();

        handler.init(MapBuilder.build("project", "PRJ", "issuetype", "1", "reporterusername",
                AbstractTestMessageHandler.REPORTER_USERNAME, "ccwatcher", Boolean.FALSE.toString()), context.getMonitor());

        // handle the message...
        final boolean passed = handler.handleMessage(message, context);
        Assert.assertEquals("handler.handleMessage handled the message successfully.", true, passed);

        // did the handler complain ?
        Assert.assertFalse("Error handler complained with >>> " + errorHandler.getError(), errorHandler.hasErrors());

        // was an issue created succesfully ?
        final Collection currentIssueIds = this.getAllIssuesForProject();
        Assert.assertEquals("The new issue should exist, issues ids: " + currentIssueIds, 1, currentIssueIds.size());

        // verify
        final Issue readIssue = getFirstIssue();

        // verify expectedSummary!
        final String actualSummary = readIssue.getSummary();
        Assert.assertEquals(expectedSummary, actualSummary);

        // verify expectedDescription
        final String actualDescription = readIssue.getDescription();
        assertDescription(expectedDescription, actualDescription);

        // check the attachments exist...
        int imageAttachmentCount = 0;
        int binaryAttachmentCount = 0;
        int plainTextAttachmentCount = 0;
        int htmlAttachmentCount = 0;

        final Collection<Attachment> attachments = readIssue.getAttachments();
        for (Attachment attachment : attachments)
        {
            String attachmentMimeType = MailUtils.getContentType(attachment.getMimetype());

            if ("text/plain".equals(attachmentMimeType))
            {
                if (!hasPlainTextAttachment)
                {
                    Assert.fail("A plain text attachment was encountered when none was expected...");
                }
                plainTextAttachmentCount++;
                continue;
            }

            if ("text/html".equals(attachmentMimeType))
            {
                if (!hasHtmlAttachment)
                {
                    Assert.fail("A html attachment was encountered when none was expected...");
                }
                htmlAttachmentCount++;
                continue;
            }

            if ("image/jpeg".equals(attachmentMimeType))
            {
                if (!hasImageAttachment)
                {
                    Assert.fail("An image attachment was encountered when none was expected...");
                }
                imageAttachmentCount++;
                continue;
            }
            if ("application/octet-stream".equals(attachmentMimeType))
            {
                if (!hasBinaryAttachment)
                {
                    Assert.fail("A binary attachment was encountered when none was expected...");
                }

                binaryAttachmentCount++;
            }
        }

        Assert.assertEquals("Image attachments", hasImageAttachment ? 1 : 0, imageAttachmentCount);
        Assert.assertEquals("Binary attachments", hasBinaryAttachment ? 1 : 0, binaryAttachmentCount);
        Assert.assertEquals("plain attachments", hasPlainTextAttachment ? 1 : 0, plainTextAttachmentCount);
        Assert.assertEquals("html attachments", hasHtmlAttachment ? 1 : 0, htmlAttachmentCount);
    }

    void assertDescription(final String expected, final String actual)
    {
        Assert.assertFalse("Expected description cannot be null or empty", null == expected || expected.length() == 0);

        // normalize cr/nl into just nl for comparison purposes...
        final String expected0 = expected.replaceAll("\r\n", "\n");
        final String actual0 = actual.replaceAll("\r\n", "\n");

        if (!actual0.startsWith(expected0))
        {
            Assert.assertEquals(expected0, actual0);
        }

        final String shouldBeCreatedViaEmailMesssage = actual0.substring(expected0.length());
        if (shouldBeCreatedViaEmailMesssage.indexOf("[Created via e-mail received from:") == -1)
        {
            Assert.fail("Unable to find created via email message at the end of \"" + actual0 + "\"");
        }
        if (shouldBeCreatedViaEmailMesssage.indexOf(']') == -1)
        {
            Assert.fail("Unable to find created via email message at the end of \"" + actual0 + "\"");
        }
    }

    Collection getAllIssuesForProject() throws Exception
    {
        final IssueManager issueManager = ComponentAccessor.getIssueManager();
        return issueManager.getIssueIdsForProject(projectA.getLong("id"));
    }

    Issue getFirstIssue() throws Exception
    {
        final Collection allIssues = this.getAllIssuesForProject();
        final Long id = (Long) allIssues.iterator().next();

        final IssueManager issueManager = ComponentAccessor.getIssueManager();
        return issueManager.getIssueObject(id);
    }
}
