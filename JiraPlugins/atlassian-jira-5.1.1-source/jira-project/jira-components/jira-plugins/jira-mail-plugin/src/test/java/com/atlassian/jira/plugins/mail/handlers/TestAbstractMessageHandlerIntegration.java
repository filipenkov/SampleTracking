package com.atlassian.jira.plugins.mail.handlers;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.DirectoryType;
import com.atlassian.crowd.embedded.api.OperationType;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.model.directory.DirectoryImpl;
import com.atlassian.jira.JiraApplicationContext;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.mail.MailLoggingManager;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.atlassian.jira.service.util.handler.MessageHandlerContext;
import com.atlassian.jira.user.util.MockUserManager;
import com.mockobjects.dynamic.Mock;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Integration Test for the functionality of the AbstractMessageHandler.
 * Prefer {@link TestAbstractMessageHandler} where possible.
 */
public class TestAbstractMessageHandlerIntegration extends AbstractTestMessageHandler
{
    public static final String MESSAGE_STRING = "This is a test message body";
    public static final String MESSAGE_SUBJECT = "This is a test message subject";

    protected AbstractMessageHandler handler;
    private Mock mockApplicationProperties;
    private Mock mockJiraApplicationContext;
    private MailLoggingManager mockMailLoggingManager;
    private MockUserManager mockUserManager;


    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        mockApplicationProperties = new Mock(ApplicationProperties.class);
        mockUserManager = new MockUserManager();
        mockJiraApplicationContext = new Mock(JiraApplicationContext.class);
        mockMailLoggingManager = Mockito.mock(MailLoggingManager.class);
        Mockito.when(mockMailLoggingManager.getIncomingMailChildLogger(Mockito.<String>any())).thenReturn(Logger.getLogger(getClass()));


        handler = new MockAbstractMessageHandler(mockUserManager, (ApplicationProperties) mockApplicationProperties.proxy(),
                (JiraApplicationContext) mockJiraApplicationContext.proxy(), mockMailLoggingManager, messageUserProcessor);
    }

    /**
     * Simply test the successful case.  Valid e-mail with a user already created
     *
     * @throws javax.mail.MessagingException
     * @throws java.io.UnsupportedEncodingException
     *
     */
    @Test
    public void testGetReporterCreateUser() throws MessagingException, UnsupportedEncodingException
    {
        setExternalUserManagement(false);

        // Create a Directory for the reporter user to be created in
        DirectoryImpl directory = new DirectoryImpl("Internal", DirectoryType.INTERNAL, "xxx");
        directory.addAllowedOperation(OperationType.CREATE_USER);
        directory.addAllowedOperation(OperationType.UPDATE_USER);
        directory.addAllowedOperation(OperationType.DELETE_USER);

        Message message = createMessage("admin@atlassian.com", "First Last");

        Map params = EasyMap.build(AbstractMessageHandler.KEY_CREATEUSERS, "true", AbstractMessageHandler.KEY_NOTIFYUSERS, "true");

        handler.init(params, monitor);
        User reporter = handler.getReporter(message, context);
        assertNotNull("Valid reporter should have been returned", reporter);
        assertEquals("Reporter Username", "admin@atlassian.com", reporter.getName());
        assertEquals("Reporter Email", "admin@atlassian.com", reporter.getEmailAddress());
        assertEquals("Reporter Name", "First Last", reporter.getDisplayName());
    }

    /**
     * Test with an email coming from an existing user and no create users and
     * no overriding reporter set.
     *
     * @throws javax.mail.MessagingException
     * @throws java.io.UnsupportedEncodingException
     *
     */
    @Test
    public void testGetReporterExistingUser() throws MessagingException, UnsupportedEncodingException
    {
        setExternalUserManagement(false);

        Message message = createMessage(u1.getEmailAddress(), u1.getDisplayName());

        @SuppressWarnings("unchecked")
        Map<String, String> params = EasyMap.build(AbstractMessageHandler.KEY_CREATEUSERS, "false");

        handler.init(params, monitor);
        User reporter = handler.getReporter(message, context);
        assertNotNull("Valid reporter should have been returned", reporter);
        assertEquals("Reporter Username", u1.getName(), reporter.getName());
        assertEquals("Reporter Email", u1.getEmailAddress(), reporter.getEmailAddress());
        assertEquals("Reporter Name", u1.getDisplayName(), reporter.getDisplayName());
    }

    /**
     * Test that no reporter is created if createUsers is set to false.
     *
     * @throws javax.mail.MessagingException
     * @throws java.io.UnsupportedEncodingException
     *
     */
    @Test
    public void testGetReporterCreateUserOff() throws MessagingException, UnsupportedEncodingException
    {
        setExternalUserManagement(false);

        Message message = createMessage("admin@atlassina.com", "First Last");
        Map params = EasyMap.build(AbstractMessageHandler.KEY_CREATEUSERS, "false");

        handler.init(params, monitor);
        User reporter = handler.getReporter(message, context);
        assertNull("Reporter should be null", reporter);
    }

    /**
     * Test that no reporter is created with invalid e-mail (and create users set to true).
     *
     * @throws javax.mail.MessagingException
     * @throws java.io.UnsupportedEncodingException
     *
     */
    @Test
    public void testGetReporterCreateUserWithInvalidEmail() throws MessagingException, UnsupportedEncodingException
    {
        setExternalUserManagement(false);

        Message message = createMessage("admin", "First Last");
        Map params = EasyMap.build(AbstractMessageHandler.KEY_CREATEUSERS, "true");

        handler.init(params, monitor);
        User reporter = handler.getReporter(message, context);
        assertNull("Reporter should be null", reporter);
    }

    /**
     * Test that creating a user with External user mgmt fails.
     *
     * @throws MessagingException
     * @throws UnsupportedEncodingException
     */
    @Test
    public void testGetReporterCreateUserWithExternalUserMgmt() throws MessagingException, UnsupportedEncodingException
    {
        setExternalUserManagement(true);

        Message message = createMessage("admin@atlassian.com", "First Last");
        Map params = EasyMap.build(AbstractMessageHandler.KEY_CREATEUSERS, "true");

        handler.init(params, monitor);
        User reporter = handler.getReporter(message, context);
        assertNull("Reporter should be null", reporter);
    }

    /**
     * Test that getreporter returns a valid user if default reporter is specified.
     *
     * @throws javax.mail.MessagingException
     * @throws java.io.UnsupportedEncodingException
     *
     */
    @Test
    public void testGetReporterWithDefaultReporter() throws MessagingException, UnsupportedEncodingException
    {
        setExternalUserManagement(false);

        Message message = createMessage("admin@atlassian.com", "First Last");

        @SuppressWarnings("unchecked")
        Map<String, String> params = EasyMap.build(AbstractMessageHandler.KEY_CREATEUSERS, "false", AbstractMessageHandler.KEY_REPORTER, u1.getName());

        handler.init(params, monitor);
        User reporter = handler.getReporter(message, context);
        assertNotNull("Valid reporter should have been returned", reporter);
        assertEquals("Reporter Username", u1.getName(), reporter.getName());
        assertEquals("Reporter Email", u1.getEmailAddress(), reporter.getEmailAddress());
        assertEquals("Reporter Name", u1.getDisplayName(), reporter.getDisplayName());
    }

    /**
     * Test that getreporter returns a valid user if createuser is true, a default reporter is specified and the
     * e-mail address is invalid.
     *
     * @throws javax.mail.MessagingException
     * @throws java.io.UnsupportedEncodingException
     *
     */
    @Test
    public void testGetReporterCreateUserWithDefaultReporterAndInvalidEmail() throws MessagingException, UnsupportedEncodingException
    {
        setExternalUserManagement(false);

        Message message = createMessage("admin", "First Last");

        @SuppressWarnings("unchecked")
        Map<String, String> params = EasyMap.build(AbstractMessageHandler.KEY_CREATEUSERS, "true", AbstractMessageHandler.KEY_REPORTER, u1.getName());

        handler.init(params, monitor);
        User reporter = handler.getReporter(message, context);
        assertNotNull("Valid reporter should have been returned", reporter);
        assertEquals("Reporter Username", u1.getName(), reporter.getName());
        assertEquals("Reporter Email", u1.getEmailAddress(), reporter.getEmailAddress());
        assertEquals("Reporter Name", u1.getDisplayName(), reporter.getDisplayName());
    }

    private void setExternalUserManagement(boolean isExternalUserManagementOn)
    {
        mockUserManager.setWritableDirectory(!isExternalUserManagementOn);
    }

    private void setAttachmentsAllowed(boolean isAttachmentsAllowed)
    {
        mockApplicationProperties.expectAndReturn("getOption", APKeys.JIRA_OPTION_ALLOWATTACHMENTS, isAttachmentsAllowed);
    }

    /**
     * Test that getreporter returns a valid user if createuser is true, a default reporter is specified and
     * external user managment is on.
     *
     * @throws javax.mail.MessagingException
     * @throws java.io.UnsupportedEncodingException
     *
     */
    @Test
    public void testGetReporterCreateUserWithDefaultReporterAndExternalUserManagment() throws MessagingException, UnsupportedEncodingException
    {
        setExternalUserManagement(true);

        Message message = createMessage("admin@atlassian.com", "First Last");

        Map params = EasyMap.build(AbstractMessageHandler.KEY_CREATEUSERS, "true", AbstractMessageHandler.KEY_REPORTER, u1.getName());

        handler.init(params, monitor);
        User reporter = handler.getReporter(message, context);
        assertNotNull("Valid reporter should have been returned", reporter);
        assertEquals("Reporter Username", u1.getName(), reporter.getName());
        assertEquals("Reporter Email", u1.getEmailAddress(), reporter.getEmailAddress());
        assertEquals("Reporter Name", u1.getDisplayName(), reporter.getDisplayName());
    }



    private Message createMessage(String address, String name)
            throws MessagingException, UnsupportedEncodingException
    {
        Message message = new MimeMessage(Session.getDefaultInstance(new Properties()));
        message.setText(MESSAGE_STRING);
        message.setSubject(MESSAGE_SUBJECT);
        message.setFrom(new InternetAddress(address, name));
        return message;
    }

    @Test
    public void testRenameFileIfInvalid()
    {
        String filename;
        String newFileName;

        //null filename should return null
        filename = null;
        newFileName = handler.renameFileIfInvalid(filename, issue, u1, context);
        assertEquals(filename, newFileName);

        //valid filename without extension should be returned without a change
        filename = "validFileWith_NoExtension";
        newFileName = handler.renameFileIfInvalid(filename, issue, u1, context);
        assertEquals(filename, newFileName);

        //valid filename with extension should be returned without a change
        filename = "validFileWith.Extension";
        newFileName = handler.renameFileIfInvalid(filename, issue, u1, context);
        assertEquals(filename, newFileName);

        //valid filename starting with dot '.' (linux hidden files)
        filename = ".bashrc";
        newFileName = handler.renameFileIfInvalid(filename, issue, u1, context);
        assertEquals(filename, newFileName);

        //invalid filenames should be renamed
        assertRenameFileIfInvalid("invalid\\name", "invalid_name");
        assertRenameFileIfInvalid("invalid\"name", "invalid_name");
        assertRenameFileIfInvalid("invalid/name.txt", "invalid_name.txt");
        assertRenameFileIfInvalid("invalid:name.PNG", "invalid_name.PNG");
        assertRenameFileIfInvalid("invalid?name.random", "invalid_name.random");
        assertRenameFileIfInvalid("invalid*name.extra.ext", "invalid_name.extra.ext");
        assertRenameFileIfInvalid("invalid<name.", "invalid_name.");
        assertRenameFileIfInvalid("invalid_name.inva|id", "invalid_name.inva_id");
        assertRenameFileIfInvalid("invalid>name", "invalid_name");
    }

    @Test
    public void testSignatureMessageNotAttached() throws Exception
    {
        AbstractMessageHandler handler = new MockAbstractMessageHandler(mockUserManager,
                (ApplicationProperties) mockApplicationProperties.proxy(),
                (JiraApplicationContext) mockJiraApplicationContext.proxy(),
                mockMailLoggingManager, messageUserProcessor)
        {
            @Override
            protected ChangeItemBean createAttachmentWithPart(final Part part, final User reporter, final Issue issue,
                    MessageHandlerContext context) throws IOException
            {
                return new ChangeItemBean();
            }
        };
//        setupIssueAndMessage();
        setAttachmentsAllowed(true);

        Message xPkcsSig = createMessageWithAttachment("application/x-pkcs7-signature");
        Collection attachments = handler.createAttachmentsForMessage(xPkcsSig, issue, context);
        assertNotNull(attachments);
        assertTrue(attachments.isEmpty());

        Message pkcsSig = createMessageWithAttachment("application/pkcs7-signature");
        attachments = handler.createAttachmentsForMessage(pkcsSig, issue, context);
        assertNotNull(attachments);
        assertTrue(attachments.isEmpty());

        Message attach = createMessageWithAttachment("text/html");
        attachments = handler.createAttachmentsForMessage(attach, issue, context);
        assertNotNull(attachments);
        assertFalse(attachments.isEmpty());
    }

    @Test
    public void testAttachedEmailMessages() throws Exception
    {
        ApplicationProperties mockApplicationProperties = new MockApplicationProperties();
        mockApplicationProperties.setOption(APKeys.JIRA_OPTION_ALLOWATTACHMENTS, true);
        AbstractMessageHandler handler = new MockAbstractMessageHandler(
                                                 mockUserManager,
                                                 mockApplicationProperties,
                                                 (JiraApplicationContext) mockJiraApplicationContext.proxy(),
                                                 mockMailLoggingManager, messageUserProcessor)
        {
            @Override
            protected ChangeItemBean createAttachmentWithPart(final Part part, final User reporter, final Issue issue
                    , MessageHandlerContext context) throws IOException
            {
                return new ChangeItemBean();
            }
        };

        Message message = HandlerTestUtil.createMessageFromFile("message_in_reply_to.msg");
        // if not ignoring attachments, there should not be an attachment, since the message is in reply to the attached
        // message inside it
        mockApplicationProperties.setOption(APKeys.JIRA_OPTION_IGNORE_EMAIL_MESSAGE_ATTACHMENTS, false);
        Collection attachments = handler.createAttachmentsForMessage(message, issue, context);
        assertNotNull(attachments);
        assertTrue(attachments.isEmpty());

        // if ignoring attachments, there should still be no attachment
        mockApplicationProperties.setOption(APKeys.JIRA_OPTION_IGNORE_EMAIL_MESSAGE_ATTACHMENTS, true);
        attachments = handler.createAttachmentsForMessage(message, issue, context);
        assertNotNull(attachments);
        assertTrue(attachments.isEmpty());

        message = HandlerTestUtil.createMessageFromFile("message_is_forwarding.msg");
        // if not ignoring attachments, there should be an attachment
        mockApplicationProperties.setOption(APKeys.JIRA_OPTION_IGNORE_EMAIL_MESSAGE_ATTACHMENTS, false);
        attachments = handler.createAttachmentsForMessage(message, issue, context);
        assertNotNull(attachments);
        assertFalse(attachments.isEmpty());

        // if ignoring attachments, there should be no attachment
        mockApplicationProperties.setOption(APKeys.JIRA_OPTION_IGNORE_EMAIL_MESSAGE_ATTACHMENTS, true);
        attachments = handler.createAttachmentsForMessage(message, issue, context);
        assertNotNull(attachments);
        assertTrue(attachments.isEmpty());
    }

    @Test
    public void testGetFilenameForAttachment() throws Exception
    {
        ApplicationProperties mockApplicationProperties = new MockApplicationProperties();
        AbstractMessageHandler handler = new MockAbstractMessageHandler(
                                                 mockUserManager,
                                                 mockApplicationProperties,
                                                 (JiraApplicationContext) mockJiraApplicationContext.proxy(),
                                                 mockMailLoggingManager, messageUserProcessor);

        // "filename" header should be chosen first
        Part part = HandlerTestUtil.createPartFromFile("attachment_with_long_filename.part");
        String filename = handler.getFilenameForAttachment(part);
        assertTrue(filename.startsWith("SUPPORT Updated (XXXXXXX) get warning message when issue is"));

        // subject should be next if no filename
        part = HandlerTestUtil.createPartFromFile("attachment_with_no_filename.part");
        filename = handler.getFilenameForAttachment(part);
        assertEquals("This is the subject line", filename);

        // message id should be next if no subject
        part = HandlerTestUtil.createPartFromFile("attachment_with_no_subject.part");
        filename = handler.getFilenameForAttachment(part);
        assertEquals("<48604ED9.2030604@atlassian.com>", filename);

        // constant should be last resort
        part = HandlerTestUtil.createPartFromFile("attachment_with_no_message_id.part");
        filename = handler.getFilenameForAttachment(part);
        assertEquals("attachedmessage", filename);
    }

    @Test
    public void testGetFileFromPart() throws Exception
    {
        ApplicationProperties mockApplicationProperties = new MockApplicationProperties();
        AbstractMessageHandler handler = new MockAbstractMessageHandler(
                mockUserManager,
                mockApplicationProperties,
                (JiraApplicationContext) mockJiraApplicationContext.proxy(),
                mockMailLoggingManager, messageUserProcessor);

        Part part = HandlerTestUtil.createPartFromFile("attachment_with_long_filename.part");
        File result = handler.getFileFromPart(part, "blah");
        assertNotNull(result);
    }

    @Test
    public void testGetMessageId() throws Exception
    {
        ApplicationProperties mockApplicationProperties = new MockApplicationProperties();
        AbstractMessageHandler handler = new MockAbstractMessageHandler(
                mockUserManager,
                mockApplicationProperties,
                (JiraApplicationContext) mockJiraApplicationContext.proxy(),
                mockMailLoggingManager, messageUserProcessor);

        Message message = HandlerTestUtil.createMessageFromFile("message_with_no_message_id.msg");
        try
        {
            handler.getMessageId(message);
            fail("Expected ParseException to be thrown when no Message-ID is present");
        }
        catch (ParseException e)
        {
            // expected
        }
    }

    /**
     * This helper method creates an attachment with nonsense content (a string of exs)
     * which may or may not be compatible with the attachment or mime type.
     * @param attachmentType The mime type.
     * @return A new Message with an attachment.
     * @throws MessagingException If javamail complains
     */
    private Message createMessageWithAttachment(String attachmentType) throws MessagingException
    {
        Message message = new MimeMessage(Session.getDefaultInstance(new Properties()));
        message.setContent("xxxxxx", attachmentType);
        message.setFileName("file1");
        message.setHeader("Content-Type", attachmentType);
        message.setDisposition("attachment");
        return message;
    }

    private void assertRenameFileIfInvalid(String filename, String expectedFileName)
    {
        String newFileName = handler.renameFileIfInvalid(filename, issue, u1, context);
        assertEquals(expectedFileName, newFileName);
    }
}
