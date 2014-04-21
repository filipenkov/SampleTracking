package com.atlassian.jira.service.util.handler;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueImpl;
import com.atlassian.jira.local.AbstractUsersTestCase;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.type.GroupDropdown;
import com.atlassian.jira.user.MockUser;
import org.joda.time.DateTime;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public abstract class AbstractTestMessageHandler extends AbstractUsersTestCase
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
    protected User reporter;
    protected Long pid = 100L;
    protected Long pid2 = 101L;
    protected GenericValue projectA;
    protected GenericValue projectB;
    protected GenericValue issue;
    protected GenericValue movedIssue;
    protected Issue issueObject;
    protected Issue movedIssueObject;
    protected Message message;
    protected AbstractMessageHandler handler;
    protected MessageErrorHandler errorHandler;
    protected CrowdService crowdService;

    public AbstractTestMessageHandler(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        crowdService = ComponentManager.getComponentInstanceOfType(CrowdService.class);

        u1 = new MockUser(TESTUSER_USERNAME, TESTUSER_FULLNAME, TESTUSER_EMAIL);
        crowdService.addUser(u1, "");

        reporter = new MockUser(REPORTER_USERNAME, REPORTER_FULLNAME, REPORTER_EMAIL);
        crowdService.addUser(reporter, "");

        projectA = UtilsForTests.getTestEntity("Project", EasyMap.build("key", "PRJ", "name", "Project 1", "counter", 100L, "id", pid, "lead", u1.getName()));
        projectB = UtilsForTests.getTestEntity("Project", EasyMap.build("key", "MOV", "name", "Project 2", "counter", 100L, "id", pid2, "lead", u1.getName()));
    }

    protected void tearDown() throws Exception
    {
        u1 = null;
        reporter = null;
        projectA = null;
        projectB = null;
        super.tearDown();
    }

    protected void setupIssueAndMessage() throws MessagingException
    {
        setupIssue();

        if (message == null)
        {
            message = createMessage((String) issue.get("key"), TESTUSER_EMAIL);
        }

        errorHandler = new MessageErrorHandler();
    }

    private void setupIssue()
    {
        if (issue == null) {
            DateTime dt = new DateTime(2000,8,8,12,12,12,0);
            Timestamp timestamp = new Timestamp(dt.toInstant().getMillis());
            issue = UtilsForTests.getTestEntity("Issue", EasyMap.build("key", ISSUE_KEY, "reporter", TESTUSER_USERNAME, "assignee", TESTUSER_USERNAME, "project", projectA.getLong("id"), "updated", timestamp));
            issueObject = IssueImpl.getIssueObject(issue);
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
        CoreFactory.getGenericDelegator().removeByAnd("Issue", EasyMap.build("key", ISSUE_KEY, "reporter", TESTUSER_USERNAME, "assignee", TESTUSER_USERNAME, "project", projectA.getLong("id")));
        // Create a new issue which will be the moved issue
        this.movedIssue = UtilsForTests.getTestEntity("Issue", EasyMap.build("key", ISSUE_KEY_MOVED, "reporter", TESTUSER_USERNAME, "assignee", TESTUSER_USERNAME, "project", projectB.getLong("id"), "updated", aLittleWhileAgo));
        this.movedIssueObject = IssueImpl.getIssueObject(movedIssue);
        // Create a change history that will identify the issue as moved
        GenericValue changeGroup = UtilsForTests.getTestEntity("ChangeGroup", EasyMap.build("issue", movedIssue.getLong("id")));
        UtilsForTests.getTestEntity("ChangeItem", EasyMap.build("oldstring", ISSUE_KEY, "field", "Key", "group", changeGroup.getLong("id")));
    }

    private MimeMessage createMessage(final String subject, final String fromAddress) throws MessagingException
    {
        MimeMessage msg = new MimeMessage(Session.getDefaultInstance(new Properties()));
        msg.setText(MESSAGE_STRING);
        msg.setSubject(subject); //let the handlers know which issue to comment on
        msg.setFrom(new InternetAddress(fromAddress));
        return msg;
    }

    protected void setupCommentPermission() throws GenericEntityException
    {
        setupCommentPermission(projectA);
    }

    protected void setupCommentPermission(GenericValue project) throws GenericEntityException
    {
        //Create a new permission scheme for this project
        GenericValue permissionScheme = UtilsForTests.getTestEntity("PermissionScheme", EasyMap.build("name", "Permission scheme", "description", "Permission scheme"));

        //Associate the project with permission scheme
        PermissionSchemeManager psm = ManagerFactory.getPermissionSchemeManager();
        psm.addSchemeToProject(project, permissionScheme);

        //Create a Scheme Permissions to browse for all users
        UtilsForTests.getTestEntity("SchemePermissions", EasyMap.build("scheme", permissionScheme.getLong("id"), "permission", Permissions.COMMENT_ISSUE, "type", GroupDropdown.DESC));
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
        handler.setErrorHandler(errorHandler);

        //setup the various possible values of the bulk option
        Map paramsNull = EasyMap.build("project", "PRJ", "issuetype", "1");
        Map paramsInvalid = EasyMap.build("project", "PRJ", "issuetype", "1", "bulk", "invalid");
        Map paramsIgnore = EasyMap.build("project", "PRJ", "issuetype", "1", "bulk", "ignore");
        Map paramsForward = EasyMap.build("project", "PRJ", "issuetype", "1", "bulk", "forward");
        Map paramsDelete = EasyMap.build("project", "PRJ", "issuetype", "1", "bulk", "delete");

        //Execute the tests
        //does default action (ie. processes normally)
        assertMessageHandling(paramsNull, message, errorHandler, true);
        assertMessageHandling(paramsInvalid, message, errorHandler, true);
        //the following cases ignore, forward, or delete the message respectively - but all fail canHandelMessage()
        //delete option is true in order to signal deletion of the mail.
        assertMessageHandling(paramsIgnore, message, errorHandler, false);
        assertMessageHandling(paramsForward, message, errorHandler, false);
        assertMessageHandling(paramsDelete, message, errorHandler, true);
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

        message.setSubject((String) issue.get("key"));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(TESTUSER_EMAIL));
        message.setFrom(new InternetAddress(TESTUSER_EMAIL));
        message.setText(MESSAGE_STRING);

        errorHandler = new MessageErrorHandler();

        handler.reporteruserName = TESTUSER_USERNAME;
        handler.setErrorHandler(errorHandler);
        handler.init(EasyMap.build("project", "PRJ", "issuetype", "1", "bulk", "forward"));
        handler.setErrorHandler(errorHandler);

        assertEquals(true, handler.handleMessage(message));
        assertFalse(errorHandler.hasErrors());
    }

    public void _testMailWithAutoSubmittedHeader() throws MessagingException, GenericEntityException
    {
        setupIssueAndMessage();
        setupCommentPermission();
        message.setHeader("Auto-Submitted", "auto-generated");
        handler.setErrorHandler(errorHandler);

        //setup the various possible values of the bulk option
        Map paramsNull = EasyMap.build("project", "PRJ", "issuetype", "1");
        Map paramsInvalid = EasyMap.build("project", "PRJ", "issuetype", "1", "bulk", "invalid");
        Map paramsIgnore = EasyMap.build("project", "PRJ", "issuetype", "1", "bulk", "ignore");
        Map paramsForward = EasyMap.build("project", "PRJ", "issuetype", "1", "bulk", "forward");
        Map paramsDelete = EasyMap.build("project", "PRJ", "issuetype", "1", "bulk", "delete");

        //Execute the tests
        //does default action (ie. processes normally)
        assertMessageHandling(paramsNull, message, errorHandler, true);
        assertMessageHandling(paramsInvalid, message, errorHandler, true);
        //the following cases ignore, forward, or delete the message respectively - but all fail canHandelMessage()
        //delete option is true in order to signal deletion of the mail.
        assertMessageHandling(paramsIgnore, message, errorHandler, false);
        assertMessageHandling(paramsForward, message, errorHandler, false);
        assertMessageHandling(paramsDelete, message, errorHandler, true);
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
        msg.setSubject((String)issue.get("key")); //let the handlers know which issue to comment on
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

        MessageErrorHandler error = new MessageErrorHandler();

        handler.setErrorHandler(error);

        //setup the various possible values of the bulk option
        Map paramsNull = EasyMap.build("project", "PRJ", "issuetype", "1");
        Map paramsInvalid = EasyMap.build("project", "PRJ", "issuetype", "1", "bulk", "invalid");
        Map paramsIgnore = EasyMap.build("project", "PRJ", "issuetype", "1", "bulk", "ignore");
        Map paramsForward = EasyMap.build("project", "PRJ", "issuetype", "1", "bulk", "forward");
        Map paramsDelete = EasyMap.build("project", "PRJ", "issuetype", "1", "bulk", "delete");

        //Execute the tests
        //does default action (ie. processes normally)
        assertMessageHandling(paramsNull, msg, error, true);
        assertMessageHandling(paramsInvalid, msg, error, true);
        //the following cases ignore, forward, or delete the message respectively - but all fail canHandelMessage()
        //delete option is true in order to signal deletion of the mail.
        assertMessageHandling(paramsIgnore, msg, error, false);
        assertMessageHandling(paramsForward, msg, error, false);
        assertMessageHandling(paramsDelete, msg, error, true);
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
        handler.init(paramsCatchEmail);
        boolean deleteMsg = handler.handleMessage(msg);
        assertFalse(deleteMsg);
        assertFalse(handler.deleteEmail);

        //-------------------
        // a miss because the TO field does not equal the catch email value
        //-------------------
        msg = createMessage(ISSUE_KEY, TESTUSER_EMAIL);
        msg.addRecipient(Message.RecipientType.TO, new InternetAddress("foobar@success.com"));

        paramsCatchEmail.put("catchemail", "catchmiss_no_to_address@there.com");
        handler.init(paramsCatchEmail);
        deleteMsg = handler.handleMessage(msg);
        assertFalse(deleteMsg);
        assertFalse(handler.deleteEmail);

        //-------------------
        // a miss because the TO field is illegal.
        //-------------------
        final MessageErrorHandler messageErrorHandler = new MessageErrorHandler();
        msg = HandlerTestUtil.createMessageFromFile("BadAddress.msg");

        handler.setErrorHandler(messageErrorHandler);
        handler.init(EasyMap.build(AbstractMessageHandler.KEY_CATCHEMAIL, "some_random_email@nowhere.ak"));
        assertFalse(handler.handleMessage(msg));
        assertTrue(messageErrorHandler.hasErrors());

        //-------------------
        // a hit because the TO field DOES equal the catch email value
        //-------------------
        msg = createMessage(ISSUE_KEY, TESTUSER_EMAIL);
        msg.addRecipient(Message.RecipientType.TO, new InternetAddress("foobar@success.com"));
        //msg  = HandlerTestUtil.createMessageFromFile("catchemail_success.msg");

        paramsCatchEmail.put("catchemail", "foobar@success.com");
        handler.init(paramsCatchEmail);
        deleteMsg = handler.handleMessage(msg);
        assertTrue(deleteMsg);
        assertTrue(handler.deleteEmail);
    }


    protected void assertMessageHandling(Map params, Message message, MessageErrorHandler errorHandler, boolean expectedOutput)
            throws MessagingException
    {
        handler.init(params);
        handler.reporteruserName = TESTUSER_USERNAME;
        assertEquals(expectedOutput, handler.handleMessage(message));
        if ("forward".equalsIgnoreCase(getBulkParam(params)))
        {
            assertEquals("Forwarding email with bulk delivery type.", errorHandler.getError());
            errorHandler.setError(null); //reset the error handler otherwise the error is carried through other tests
        }
        else
        {
            assertNull(errorHandler.getError());
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