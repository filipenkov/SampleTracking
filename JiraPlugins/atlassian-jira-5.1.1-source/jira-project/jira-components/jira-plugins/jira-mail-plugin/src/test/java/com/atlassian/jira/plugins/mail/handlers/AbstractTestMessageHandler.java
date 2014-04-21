package com.atlassian.jira.plugins.mail.handlers;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.JiraApplicationContext;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.ApplicationPropertiesImpl;
import com.atlassian.jira.config.properties.ApplicationPropertiesStore;
import com.atlassian.jira.config.properties.MemorySwitchToDatabaseBackedPropertiesManager;
import com.atlassian.jira.config.properties.PropertiesManager;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.util.IssueUpdater;
import com.atlassian.jira.mail.MailLoggingManager;
import com.atlassian.jira.mail.MailThreadManager;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.mock.security.MockAuthenticationContext;
import com.atlassian.jira.plugins.mail.MockAttachmentManager;
import com.atlassian.jira.plugins.mail.MockIssueManager;
import com.atlassian.jira.plugins.mail.UTUtils;
import com.atlassian.jira.project.ProjectImpl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.service.util.handler.MessageHandlerExecutionMonitor;
import com.atlassian.jira.service.util.handler.MessageHandlerContext;
import com.atlassian.jira.service.util.handler.MessageUserProcessor;
import com.atlassian.jira.service.util.handler.MessageUserProcessorImpl;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.util.MockUserManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.util.AttachmentException;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.joda.time.DateTime;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import javax.annotation.Nullable;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public abstract class AbstractTestMessageHandler
{
    protected static final String ISSUE_KEY = "PRJ-1";
    protected static final String ISSUE_KEY_MOVED = "MOV-1";
    protected final static String MESSAGE_STRING = "something is wrong with this drink coaster\n";
    protected final static String MESSAGE_SUBJECT = "yo, this is bad\n";
    protected final static String TESTUSER_USERNAME = "testuser";
    protected final static String TESTUSER_FULLNAME = "testuser fullname";
    protected final static String TESTUSER_EMAIL = "test@from.com";
    protected final static String REPORTER_USERNAME = "reporteruser";
    protected final static String REPORTER_FULLNAME = "reporter fullname";
    protected final static String REPORTER_EMAIL = "reporter@from.com";
    protected final MessageFormat HAS_NO_PERMISSION_TO_COMMENT_ON = new MessageFormat("User ''{0}'' does not have permission to comment on an issue in project ''{1}''.");

    protected User u1;
    protected Long pid = 100L;
    protected Long pid2 = 101L;
    protected GenericValue projectA;
    protected GenericValue projectB;
    protected Issue issue;
    protected Issue movedIssue;
    protected Issue issueObject;
    protected Issue movedIssueObject;
    protected Message message;
    protected AbstractMessageHandler handler;
    protected SimpleTestMessageHandlerExecutionMonitor monitor;
    protected MessageHandlerContext context;
    protected MockComponentWorker worker;
    protected MockUserManager userManager = new MockUserManager();

    @Mock (answer = Answers.RETURNS_MOCKS)
    protected MailLoggingManager mailLoggingManager;
    @Mock
    protected PermissionManager permissionManager;
    protected MockIssueManager issueManager;
    @Mock
    protected IssueFactory issueFactory;
    @Mock
    protected MailThreadManager mailThreadManager;
    @Mock
    protected ChangeHistoryManager changeHistoryManager;
    @Mock
    protected JiraApplicationContext jiraApplicationContext;
    @Mock
    protected IssueUpdater issueUpdater;

    protected MessageUserProcessor messageUserProcessor = new MessageUserProcessorImpl(userManager);



    protected ApplicationProperties applicationProperties;
    protected MockAttachmentManager attachmentManager;
    protected I18nHelper i18nHelper;

    protected final List<String> commentsAdded = Lists.newArrayList();

    protected AttachmentManager getAttachmentManager() {
        return attachmentManager;
    }

    protected void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
//        MultiTenantContextTestUtils.setupMultiTenantSystem();
        u1 = createMockUser(TESTUSER_USERNAME, TESTUSER_FULLNAME, TESTUSER_EMAIL);
//
//        reporter = createMockUser(REPORTER_USERNAME, REPORTER_FULLNAME, REPORTER_EMAIL);
//
        projectA = UTUtils.getTestEntity("Project", EasyMap.build("key", "PRJ", "name", "Project 1", "counter", 100L, "id", pid, "lead", u1.getName()));
        projectB = UTUtils.getTestEntity("Project", EasyMap.build("key", "MOV", "name", "Project 2", "counter", 100L, "id", pid2, "lead", u1.getName()));
//        ComponentManager.getComponentInstanceOfType(ProjectManager.class).refresh();
        worker = new MockComponentWorker();
        ComponentAccessor.initialiseWorker(worker);
        worker.addMock(UserManager.class, userManager);

        MockAuthenticationContext authenticationContext = new MockAuthenticationContext(null);
        final ResourceBundle pluginBundle = new PropertyResourceBundle(getClass().getResourceAsStream("/com/atlassian/jira/plugins/mail/messages.properties"));
        i18nHelper = authenticationContext.getI18nHelper();
        final ResourceBundle defaultResourceBundle = i18nHelper.getDefaultResourceBundle();
        final Method setParent = ResourceBundle.class.getDeclaredMethod("setParent", ResourceBundle.class);
        setParent.setAccessible(true);
        setParent.invoke(defaultResourceBundle, pluginBundle);
        worker.addMock(JiraAuthenticationContext.class, authenticationContext);

        worker.addMock(MailThreadManager.class, mailThreadManager);
        worker.addMock(MailLoggingManager.class, Mockito.mock(MailLoggingManager.class, Mockito.RETURNS_MOCKS));
        worker.addMock(MessageUserProcessor.class, new MessageUserProcessorImpl(userManager));
//        worker.addMock(MessageUserProcessor.class, Mockito.mock(MessageUserProcessor.class, Mockito.RETURNS_MOCKS));
        attachmentManager = new MockAttachmentManager();
        worker.addMock(AttachmentManager.class, attachmentManager);

        worker.addMock(ChangeHistoryManager.class, changeHistoryManager);
        worker.addMock(PermissionManager.class, permissionManager);
        Mockito.when(permissionManager.hasPermission(Mockito.eq(Permissions.CREATE_ATTACHMENT), Mockito.<Issue>any(), Mockito.<User>any())).thenReturn(true);
        worker.addMock(IssueUpdater.class, issueUpdater);

        Mockito.when(jiraApplicationContext.getFingerPrint()).thenReturn("mock_fingerprint");
        worker.addMock(JiraApplicationContext.class, jiraApplicationContext);


        applicationProperties = new ApplicationPropertiesImpl(new ApplicationPropertiesStore(new PropertiesManager(new MemorySwitchToDatabaseBackedPropertiesManager()), Mockito.mock(JiraHome.class, Mockito.RETURNS_DEFAULTS)));
        worker.addMock(ApplicationProperties.class, applicationProperties);

        issueManager = new MockIssueManager();
        worker.addMock(IssueManager.class, issueManager);

        Mockito.when(issueFactory.getIssue(Mockito.<GenericValue>any())).thenAnswer(new Answer<Object>()
        {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable
            {
                final MockIssue mockIssue = new MockIssue();
                mockIssue.setGenericValue((GenericValue) invocation.getArguments()[0]);
                return mockIssue;
            }
        });
        worker.addMock(IssueFactory.class, issueFactory);

        monitor = new SimpleTestMessageHandlerExecutionMonitor();
        context = new MessageHandlerContext()
        {
            @Override
            public User createUser(String username, String password, String email, String fullname, Integer userEventType)
                    throws PermissionException, CreateException
            {
                return createMockUser(username, fullname, email);
            }

            @Override
            public Comment createComment(Issue issue, User author, String body, boolean dispatchEvent)
            {
                commentsAdded.add(body);
                return null;
            }

            @Override
            public boolean isRealRun()
            {
                return true;
            }

            @Override
            public MessageHandlerExecutionMonitor getMonitor()
            {
                return monitor;
            }

            @Override
            public Issue createIssue(@Nullable User reporter, Issue issue) throws CreateException
            {
                throw new UnsupportedOperationException("unexpected call");
            }

            @Override
            public ChangeItemBean createAttachment(File file, String filename, String contentType, User author, Issue issue)
                    throws AttachmentException
            {
                return getAttachmentManager().createAttachment(file, filename, contentType, author, issue);
            }
        };

    }

    protected void tearDown() throws Exception
    {
        u1 = null;
        projectA = null;
        projectB = null;
    }

    protected void setupIssueAndMessage() throws MessagingException
    {
        setupIssue();

        if (message == null)
        {
            message = createMessage(issue.getKey(), TESTUSER_EMAIL);
        }

        monitor = new SimpleTestMessageHandlerExecutionMonitor();
    }

    private void setupIssue()
    {
        if (issue == null) {
            DateTime dt = new DateTime(2000,8,8,12,12,12,0);
            Timestamp timestamp = new Timestamp(dt.toInstant().getMillis());
            MockIssue issue = new MockIssue();
            issue.setGenericValue(UTUtils.getTestEntity("Issue", EasyMap.build("key", ISSUE_KEY, "reporter", TESTUSER_USERNAME, "assignee", TESTUSER_USERNAME, "project", projectA.getLong("id"), "updated", timestamp)));
            issue.setUpdated(timestamp);
            issue.setCreated(timestamp);
            issue.setProjectObject(new ProjectImpl(projectA));
            issueManager.addIssue(issue);
            this.issue = issue;
            this.issueObject = issue;
        }
    }

    void setupMovedIssue() throws GenericEntityException
    {
        // The move tests fail on LabManager sometimes. the clock on the VM seems to behave erratically resulting in the
        // created time and updated time staying the same even though we've added a comment, so we start off a little in the past
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND, -5);
        Timestamp aLittleWhileAgo = new Timestamp(cal.getTime().getTime());

        // Delete the issue
        // Create a new issue which will be the moved issue
        MockIssue issue = new MockIssue();
        issue.setGenericValue(UTUtils.getTestEntity("Issue", EasyMap.build("key", ISSUE_KEY_MOVED, "reporter", TESTUSER_USERNAME, "assignee", TESTUSER_USERNAME, "project", projectB.getLong("id"), "updated", aLittleWhileAgo)));
        issue.setUpdated(aLittleWhileAgo);
        issue.setCreated(aLittleWhileAgo);
        issueManager.addIssue(issue);
        this.movedIssueObject = issue;
        this.movedIssue = issue;

        Mockito.when(changeHistoryManager.findMovedIssue(this.issueObject.getKey())).thenReturn(movedIssueObject);
        // Create a change history that will identify the issue as moved
//        GenericValue changeGroup = UtilsForTests.getTestEntity("ChangeGroup", EasyMap.build("issue", movedIssue.getLong("id")));
//        UtilsForTests.getTestEntity("ChangeItem", EasyMap.build("oldstring", ISSUE_KEY, "field", "Key", "group", changeGroup.getLong("id")));
    }

    private MimeMessage createMessage(final String subject, final String fromAddress) throws MessagingException
    {
        MimeMessage msg = new MimeMessage(Session.getDefaultInstance(new Properties()));
        msg.setText(MESSAGE_STRING);
        msg.setSubject(subject); //let the handlers know which issue to comment on
        msg.setFrom(new InternetAddress(fromAddress));
        return msg;
    }

    protected User createMockUser(String userName, String name, String email)
    {
        User user = new MockUser(userName, name, email);
        userManager.addUser(user);
        return user;
    }

    protected User createMockUser(String userName)
    {
        return createMockUser(userName, userName, "");
    }

    protected void setupCommentPermission() throws GenericEntityException
    {
        Mockito.when(permissionManager.hasPermission(Mockito.eq(Permissions.COMMENT_ISSUE), Mockito.<Issue>any(), Mockito.<User>any())).thenReturn(true);
        Mockito.when(permissionManager.hasPermission(Mockito.eq(Permissions.COMMENT_ISSUE), Mockito.<GenericValue>any(), Mockito.<User>any())).thenReturn(true);
    }

    /**
     * Tests that the handler correctly handles email with the header "Precedence: bulk" according to the value of the
     * bulk param of the service.
     */
    public void _testMailWithPrecedenceBulkHeader() throws MessagingException, GenericEntityException
    {
        setupIssueAndMessage();
        setupCommentPermission();
        message.setHeader("Precedence", "bulk");

        //setup the various possible values of the bulk option
        Map paramsNull = EasyMap.build("project", "PRJ", "issuetype", "1");
        Map paramsInvalid = EasyMap.build("project", "PRJ", "issuetype", "1", "bulk", "invalid");
        Map paramsIgnore = EasyMap.build("project", "PRJ", "issuetype", "1", "bulk", "ignore");
        Map paramsForward = EasyMap.build("project", "PRJ", "issuetype", "1", "bulk", "forward");
        Map paramsDelete = EasyMap.build("project", "PRJ", "issuetype", "1", "bulk", "delete");

        //Execute the tests
        //does default action (ie. processes normally)
        assertMessageHandling(paramsNull, message, true);
        assertMessageHandling(paramsInvalid, message, true);
        //the following cases ignore, forward, or delete the message respectively - but all fail canHandelMessage()
        //delete option is true in order to signal deletion of the mail.
        assertMessageHandling(paramsIgnore, message, false);
        assertMessageHandling(paramsForward, message, false);
        assertMessageHandling(paramsDelete, message, true);
    }

    //Make sure a messed up bulk header does not stop processing.
    public void _testMailWithIllegalPrecedenceBulkHeader() throws Exception
    {
        setupIssue();
        setupCommentPermission();
        message = new MimeMessage((Session) null)
        {
            public String[] getHeader(final String s) throws MessagingException
            {
                if (s != null && s.equals("Precidence"))
                {
                    throw new MessagingException("Header 'Precidence' will always return error for this message.");
                }
                else
                {
                    return super.getHeader(s);
                }
            }
        };

        message.setSubject(issue.getKey());
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(TESTUSER_EMAIL));
        message.setFrom(new InternetAddress(TESTUSER_EMAIL));
        message.setText(MESSAGE_STRING);

        monitor = new SimpleTestMessageHandlerExecutionMonitor();

        handler.reporteruserName = TESTUSER_USERNAME;
        handler.init(MapBuilder.build("project", "PRJ", "issuetype", "1", "bulk", "forward"), context.getMonitor());

        Assert.assertEquals(true, handler.handleMessage(message, context));
        Assert.assertFalse(monitor.hasErrors());
    }

    public void _testMailWithAutoSubmittedHeader() throws MessagingException, GenericEntityException
    {
        setupIssueAndMessage();
        setupCommentPermission();
        message.setHeader("Auto-Submitted", "auto-generated");

        //setup the various possible values of the bulk option
        Map paramsNull = EasyMap.build("project", "PRJ", "issuetype", "1");
        Map paramsInvalid = EasyMap.build("project", "PRJ", "issuetype", "1", "bulk", "invalid");
        Map paramsIgnore = EasyMap.build("project", "PRJ", "issuetype", "1", "bulk", "ignore");
        Map paramsForward = EasyMap.build("project", "PRJ", "issuetype", "1", "bulk", "forward");
        Map paramsDelete = EasyMap.build("project", "PRJ", "issuetype", "1", "bulk", "delete");

        //Execute the tests
        //does default action (ie. processes normally)
        assertMessageHandling(paramsNull, message, true);
        assertMessageHandling(paramsInvalid, message, true);
        //the following cases ignore, forward, or delete the message respectively - but all fail canHandelMessage()
        //delete option is true in order to signal deletion of the mail.
        assertMessageHandling(paramsIgnore, message, false);
        assertMessageHandling(paramsForward, message, false);
        assertMessageHandling(paramsDelete, message, true);
    }

    /**
     * Tests that the handler correctly handles email with the header "Content-Type: multipart/report; reporty-type=delivery-status" according to the value of the
     * bulk param of the service.
     */
    protected void _testMailWithDeliveryStatusHeader() throws MessagingException, GenericEntityException
    {
        setupIssue();
        setupCommentPermission();

        MimeMessage msg = new MimeMessage(Session.getDefaultInstance(new Properties()));
        msg.setSubject(issue.getKey()); //let the handlers know which issue to comment on
        msg.setFrom(new InternetAddress(TESTUSER_EMAIL));

        MimeMultipart multi = new MimeMultipart("report; report-type=delivery-status");
        BodyPart plainText = new MimeBodyPart();
        BodyPart deliveryStatus = new MimeBodyPart();

        plainText.setText(MESSAGE_STRING);
        deliveryStatus.setText(MESSAGE_STRING);
        deliveryStatus.setHeader("Content-Type","message/delivery-status" );

        multi.addBodyPart(plainText);
        multi.addBodyPart(deliveryStatus);

        msg.setContent(multi);
        msg.saveChanges();

        //setup the various possible values of the bulk option
        Map paramsNull = EasyMap.build("project", "PRJ", "issuetype", "1");
        Map paramsInvalid = EasyMap.build("project", "PRJ", "issuetype", "1", "bulk", "invalid");
        Map paramsIgnore = EasyMap.build("project", "PRJ", "issuetype", "1", "bulk", "ignore");
        Map paramsForward = EasyMap.build("project", "PRJ", "issuetype", "1", "bulk", "forward");
        Map paramsDelete = EasyMap.build("project", "PRJ", "issuetype", "1", "bulk", "delete");

        //Execute the tests
        //does default action (ie. processes normally)
        assertMessageHandling(paramsNull, msg, true);
        assertMessageHandling(paramsInvalid, msg, true);
        //the following cases ignore, forward, or delete the message respectively - but all fail canHandelMessage()
        //delete option is true in order to signal deletion of the mail.
        assertMessageHandling(paramsIgnore, msg, false);
        assertMessageHandling(paramsForward, msg, false);
        assertMessageHandling(paramsDelete, msg, true);
    }

    /**
     * This will test that if the catchemail parameter is in effect, it ensure that the mail is
     * correctly handled
     *
     * @throws Exception if stuff goes wrong
     */
    protected void _testCatchEmailSettings() throws Exception
    {
        setupIssue();
        setupCommentPermission();

        Map<String, String> paramsCatchEmail = new HashMap<String, String>();
        paramsCatchEmail.put("project", "PRJ");
        paramsCatchEmail.put("issuetype", "1");

        Message msg = createMessage(ISSUE_KEY, TESTUSER_EMAIL);

        //-------------------
        // a miss because there is NO TO address
        //-------------------
        paramsCatchEmail.put("catchemail", "catchmiss_no_to_address@there.com");
        handler.init(paramsCatchEmail, context.getMonitor());
        boolean deleteMsg = handler.handleMessage(msg, context);
        Assert.assertFalse(deleteMsg);
//        Assert.assertFalse(handler.deleteEmail);

        //-------------------
        // a miss because the TO field does not equal the catch email value
        //-------------------
        msg = createMessage(ISSUE_KEY, TESTUSER_EMAIL);
        msg.addRecipient(Message.RecipientType.TO, new InternetAddress("foobar@success.com"));

        paramsCatchEmail.put("catchemail", "catchmiss_no_to_address@there.com");
        handler.init(paramsCatchEmail, context.getMonitor());
        deleteMsg = handler.handleMessage(msg, context);
        Assert.assertFalse(deleteMsg);
//        Assert.assertFalse(handler.deleteEmail);

        //-------------------
        // a miss because the TO field is illegal.
        //-------------------
        msg = HandlerTestUtil.createMessageFromFile("BadAddress.msg");

        handler.init(MapBuilder.build(AbstractMessageHandler.KEY_CATCHEMAIL, "some_random_email@nowhere.ak"), context.getMonitor());
        Assert.assertFalse(handler.handleMessage(msg, context));
        assertTrue(monitor.hasErrors());

        //-------------------
        // a hit because the TO field DOES equal the catch email value
        //-------------------
        msg = createMessage(ISSUE_KEY, TESTUSER_EMAIL);
        msg.addRecipient(Message.RecipientType.TO, new InternetAddress("foobar@success.com"));
        //msg  = HandlerTestUtil.createMessageFromFile("catchemail_success.msg");

        paramsCatchEmail.put("catchemail", "foobar@success.com");
        handler.init(paramsCatchEmail, context.getMonitor());
        deleteMsg = handler.handleMessage(msg, context);
        assertTrue(deleteMsg);
//        Assert.assertTrue(handler.deleteEmail);
    }


    protected void assertMessageHandling(Map params, Message message, boolean expectedOutput)
            throws MessagingException
    {
        handler.init(params, context.getMonitor());
        handler.reporteruserName = TESTUSER_USERNAME;
        assertEquals(expectedOutput, handler.handleMessage(message, context));
        if ("forward".equalsIgnoreCase(getBulkParam(params)))
        {
            Assert.assertEquals("Forwarding email with bulk delivery type.", monitor.getError());
            monitor.reset(); //reset the error handler otherwise the error is carried through other tests
        }
        else
        {
            Assert.assertNull(monitor.getError());
        }
    }

    protected String getBulkParam(Map params)
    {
        if (params.get("bulk") != null)
        {
            return (String) params.get("bulk");
        }
        return null;
    }
}
