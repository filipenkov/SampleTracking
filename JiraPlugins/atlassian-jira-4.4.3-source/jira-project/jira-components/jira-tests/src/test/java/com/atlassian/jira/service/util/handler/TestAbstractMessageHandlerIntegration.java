package com.atlassian.jira.service.util.handler;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.user.UserUtils;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.DirectoryType;
import com.atlassian.crowd.embedded.api.OperationType;
import com.atlassian.crowd.embedded.spi.DirectoryDao;
import com.atlassian.crowd.model.directory.DirectoryImpl;
import com.atlassian.jira.JiraApplicationContext;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.atlassian.jira.user.util.OSUserConverter;
import com.mockobjects.dynamic.Mock;
import com.opensymphony.user.User;
import org.ofbiz.core.entity.GenericValue;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

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
    private Mock mockCommentManager;
    private Mock mockIssueFactory;
    private Mock mockJiraApplicationContext;

    public TestAbstractMessageHandlerIntegration(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        mockApplicationProperties = new Mock(ApplicationProperties.class);
        mockCommentManager = new Mock(CommentManager.class);
        mockIssueFactory = new Mock(IssueFactory.class);
        mockJiraApplicationContext = new Mock(JiraApplicationContext.class);

        handler = new MockAbstractMessageHandler((CommentManager) mockCommentManager.proxy(),
                                                 (IssueFactory) mockIssueFactory.proxy(),
                                                 (ApplicationProperties) mockApplicationProperties.proxy(),
                                                 (JiraApplicationContext) mockJiraApplicationContext.proxy());
    }

    /**
     * Simply test the successful case.  Valid e-mail with a user already created
     *
     * @throws javax.mail.MessagingException
     * @throws java.io.UnsupportedEncodingException
     *
     */
    public void testGetReporterCreateUser() throws MessagingException, UnsupportedEncodingException
    {
        setExternalUserManagement(false);

        // Create a Directory for the reporter user to be created in
        DirectoryImpl directory = new DirectoryImpl("Internal", DirectoryType.INTERNAL, "xxx");
        directory.addAllowedOperation(OperationType.CREATE_USER);
        directory.addAllowedOperation(OperationType.UPDATE_USER);
        directory.addAllowedOperation(OperationType.DELETE_USER);
        ComponentAccessor.getComponentOfType(DirectoryDao.class).add(directory);
        
        Message message = createMessage("admin@atlassian.com", "First Last");

        Map params = EasyMap.build(AbstractMessageHandler.KEY_CREATEUSERS, "true", AbstractMessageHandler.KEY_NOTIFYUSERS, "true");

        handler.init(params);
        User reporter = handler.getReporter(message);
        assertNotNull("Valid reporter should have been returned", reporter);
        assertEquals("Reporter Username", "admin@atlassian.com", reporter.getName());
        assertEquals("Reporter Email", "admin@atlassian.com", reporter.getEmail());
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
    public void testGetReporterExistingUser() throws MessagingException, UnsupportedEncodingException
    {
        setExternalUserManagement(false);

        Message message = createMessage(u1.getEmailAddress(), u1.getDisplayName());

        Map params = EasyMap.build(AbstractMessageHandler.KEY_CREATEUSERS, "false");

        handler.init(params);
        User reporter = handler.getReporter(message);
        assertNotNull("Valid reporter should have been returned", reporter);
        assertEquals("Reporter Username", u1.getName(), reporter.getName());
        assertEquals("Reporter Email", u1.getEmailAddress(), reporter.getEmail());
        assertEquals("Reporter Name", u1.getDisplayName(), reporter.getDisplayName());
    }

    /**
     * Test that no reporter is created if createUsers is set to false.
     *
     * @throws javax.mail.MessagingException
     * @throws java.io.UnsupportedEncodingException
     *
     */
    public void testGetReporterCreateUserOff() throws MessagingException, UnsupportedEncodingException
    {
        setExternalUserManagement(false);

        Message message = createMessage("admin@atlassina.com", "First Last");
        Map params = EasyMap.build(AbstractMessageHandler.KEY_CREATEUSERS, "false");

        handler.init(params);
        User reporter = handler.getReporter(message);
        assertNull("Reporter should be null", reporter);
    }

    /**
     * Test that no reporter is created with invalid e-mail (and create users set to true).
     *
     * @throws javax.mail.MessagingException
     * @throws java.io.UnsupportedEncodingException
     *
     */
    public void testGetReporterCreateUserWithInvalidEmail() throws MessagingException, UnsupportedEncodingException
    {
        setExternalUserManagement(false);

        Message message = createMessage("admin", "First Last");
        Map params = EasyMap.build(AbstractMessageHandler.KEY_CREATEUSERS, "true");

        handler.init(params);
        User reporter = handler.getReporter(message);
        assertNull("Reporter should be null", reporter);
    }

    /**
     * Test that creating a user with External user mgmt fails.
     *
     * @throws MessagingException
     * @throws UnsupportedEncodingException
     */
    public void testGetReporterCreateUserWithExternalUserMgmt() throws MessagingException, UnsupportedEncodingException
    {
        setExternalUserManagement(true);

        Message message = createMessage("admin@atlassian.com", "First Last");
        Map params = EasyMap.build(AbstractMessageHandler.KEY_CREATEUSERS, "true");

        handler.init(params);
        User reporter = handler.getReporter(message);
        assertNull("Reporter should be null", reporter);
    }

    /**
     * Test that getreporter returns a valid user if default reporter is specified.
     *
     * @throws javax.mail.MessagingException
     * @throws java.io.UnsupportedEncodingException
     *
     */
    public void testGetReporterWithDefaultReporter() throws MessagingException, UnsupportedEncodingException
    {
        setExternalUserManagement(false);

        Message message = createMessage("admin@atlassian.com", "First Last");

        Map params = EasyMap.build(AbstractMessageHandler.KEY_CREATEUSERS, "false", AbstractMessageHandler.KEY_REPORTER, u1.getName());

        handler.init(params);
        User reporter = handler.getReporter(message);
        assertNotNull("Valid reporter should have been returned", reporter);
        assertEquals("Reporter Username", u1.getName(), reporter.getName());
        assertEquals("Reporter Email", u1.getEmailAddress(), reporter.getEmail());
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
    public void testGetReporterCreateUserWithDefaultReporterAndInvalidEmail() throws MessagingException, UnsupportedEncodingException
    {
        setExternalUserManagement(false);

        Message message = createMessage("admin", "First Last");

        Map params = EasyMap.build(AbstractMessageHandler.KEY_CREATEUSERS, "true", AbstractMessageHandler.KEY_REPORTER, u1.getName());

        handler.init(params);
        User reporter = handler.getReporter(message);
        assertNotNull("Valid reporter should have been returned", reporter);
        assertEquals("Reporter Username", u1.getName(), reporter.getName());
        assertEquals("Reporter Email", u1.getEmailAddress(), reporter.getEmail());
        assertEquals("Reporter Name", u1.getDisplayName(), reporter.getDisplayName());
    }

    private void setExternalUserManagement(boolean isExternalUserManagementOn)
    {
        mockApplicationProperties.expectAndReturn("getOption", APKeys.JIRA_OPTION_USER_EXTERNALMGT, isExternalUserManagementOn);
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
    public void testGetReporterCreateUserWithDefaultReporterAndExternalUserManagment() throws MessagingException, UnsupportedEncodingException
    {
        setExternalUserManagement(true);

        Message message = createMessage("admin@atlassian.com", "First Last");

        Map params = EasyMap.build(AbstractMessageHandler.KEY_CREATEUSERS, "true", AbstractMessageHandler.KEY_REPORTER, u1.getName());

        handler.init(params);
        User reporter = handler.getReporter(message);
        assertNotNull("Valid reporter should have been returned", reporter);
        assertEquals("Reporter Username", u1.getName(), reporter.getName());
        assertEquals("Reporter Email", u1.getEmailAddress(), reporter.getEmailAddress());
        assertEquals("Reporter Name", u1.getDisplayName(), reporter.getDisplayName());
    }

    public void testGetAuthorFromSenderNoUser() throws Exception
    {
        setExternalUserManagement(true);
        handler.init(Collections.EMPTY_MAP);

        // test with no matching user to get
        Message pokemonMessage = createMessage("pokemon@atlassian.com", "Pocket Monster");
        assertNull("unexpected pokemon", handler.getAuthorFromSender(pokemonMessage));
    }

    public void testGetAuthorFromSenderUserMatchByEmail() throws Exception
    {
        setExternalUserManagement(true);
        handler.init(Collections.EMPTY_MAP);
        Message pokemonMessage = createMessage("pokemon@atlassian.com", "Pocket Monster");
        String pokemonEmail = "pokemon@atlassian.com";
        UserUtils.createUser("pokemon", pokemonEmail);
        User pokemonAuthor = handler.getAuthorFromSender(pokemonMessage);
        assertNotNull("should have found pokemon this time", pokemonAuthor);
        assertEquals(pokemonEmail, pokemonAuthor.getEmailAddress());
    }


    public void testGetAuthorFromSenderMultipleUserMatchByEmail() throws Exception
    {
        setExternalUserManagement(true);
        handler.init(Collections.EMPTY_MAP);
        String pokemonEmail = "pokemon@atlassian.com";
        Message pokemonMessage = createMessage(pokemonEmail, "Pocket Monster");
        UserUtils.createUser("digimon", pokemonEmail);
        UserUtils.createUser("pokemon", pokemonEmail);
        UserUtils.createUser("yugiyo", pokemonEmail);
        User pokemonAuthor = handler.getAuthorFromSender(pokemonMessage);
        assertNotNull("should have found one of the pokemons", pokemonAuthor);
        assertEquals(pokemonEmail, pokemonAuthor.getEmailAddress());
        String authorName = pokemonAuthor.getName();
        boolean foundOne = authorName.equals("digimon")
                || authorName.equals("pokemon")
                || authorName.equals("yugiyo");
        assertTrue("should have found either digimon or pokemon", foundOne);
    }

    /**
     * Tests getAuthorFromSender with no user with right email, but a user with the email as its username.
     *
     * @throws Exception whenever.
     */
    public void testGetAuthorFromSenderUserMatchByUsername() throws Exception
    {
        setExternalUserManagement(true);
        handler.init(Collections.EMPTY_MAP);
        String messageEmail = "bob@wailers.org";
        Message reggaeMsg = createMessage(messageEmail, "bobMarley");
        User useWithEmailAsUsername= UserUtils.createUser(messageEmail, "bob@marley.com");
        User author = handler.getAuthorFromSender(reggaeMsg);
        assertEquals(useWithEmailAsUsername, author);
    }

    /**
     * Test with user with email username (and wrong email) but checks that
     * the user with right email is returned instead.
     *
     * @throws Exception on error.
     */
    public void testGetAuthorFromSenderEmailAndUsernameMatch() throws Exception
    {
        setExternalUserManagement(true);
        handler.init(Collections.EMPTY_MAP);
        String messageEmail = "chris@atlassian.com";
        Message chrisEmail = createMessage(messageEmail, "chris");
        User userWithEmailAsUsername = UserUtils.createUser(messageEmail, "cmountford@atlassian.com");
        User userWithEmailAsEmail = UserUtils.createUser("christo", messageEmail);
        User authorFromSender = handler.getAuthorFromSender(chrisEmail);
        assertEquals(userWithEmailAsEmail, authorFromSender);
        assertFalse("returned the wrong user, should prefer email to email match", userWithEmailAsUsername.equals(authorFromSender));
    }

    public void testFindUserByUsername()
    {
        setExternalUserManagement(true);
        handler.init(Collections.EMPTY_MAP);

        String god = "jimiHendrix";
        assertNull("god should not exist", handler.findUserByUsername(god));

        UtilsForTests.getTestUser(god);
        User foundUser = handler.findUserByUsername(god);
        assertNotNull("I should have found god", foundUser);
        assertEquals(god, foundUser.getName());
    }

    public void testFindUserByEmail() throws Exception
    {
        setExternalUserManagement(true);
        handler.init(Collections.EMPTY_MAP);

        User foundUser = handler.findUserByEmail(u1.getEmailAddress());
        assertNotNull("couldn't find user", foundUser);
        assertEquals("found the wrong user", foundUser, u1);

        assertNull("shouldn't have found a user!", handler.findUserByEmail("email@does.not.exist.com"));

        String email = "chris@atlassian.com";
        // create a user with email as username
        User chris = UtilsForTests.getTestUser(email);

        assertNull("shouldn't have found this user because their email is not set", handler.findUserByEmail(email));

        chris.setEmail(email);
        chris.store();

        User userByEmail = handler.findUserByEmail(email);
        assertNotNull("should have found a user that time", userByEmail);
        assertEquals(email, userByEmail.getEmailAddress());

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

    public void testRenameFileIfInvalid()
    {
        String filename;
        String newFileName;

        //null filename should return null
        filename = null;
        newFileName = handler.renameFileIfInvalid(filename, issue, OSUserConverter.convertToOSUser(u1));
        assertEquals(filename, newFileName);

        //valid filename without extension should be returned without a change
        filename = "validFileWith_NoExtension";
        newFileName = handler.renameFileIfInvalid(filename, issue, OSUserConverter.convertToOSUser(u1));
        assertEquals(filename, newFileName);

        //valid filename with extension should be returned without a change
        filename = "validFileWith.Extension";
        newFileName = handler.renameFileIfInvalid(filename, issue, OSUserConverter.convertToOSUser(u1));
        assertEquals(filename, newFileName);

        //valid filename starting with dot '.' (linux hidden files)
        filename = ".bashrc";
        newFileName = handler.renameFileIfInvalid(filename, issue, OSUserConverter.convertToOSUser(u1));
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

    public void testSignatureMessageNotAttached() throws Exception
    {
        AbstractMessageHandler handler = new MockAbstractMessageHandler((CommentManager) mockCommentManager.proxy(),
                                                 (IssueFactory) mockIssueFactory.proxy(),
                                                 (ApplicationProperties) mockApplicationProperties.proxy(),
                                                 (JiraApplicationContext) mockJiraApplicationContext.proxy())
        {
            protected ChangeItemBean createAttachmentWithPart(final Part part, final User reporter, final GenericValue issue) throws IOException
            {
                return new ChangeItemBean();
            }
        };
        setupIssueAndMessage();
        setAttachmentsAllowed(true);

        Message xPkcsSig = createMessageWithAttachment("application/x-pkcs7-signature");
        Collection attachments = handler.createAttachmentsForMessage(xPkcsSig, issue);
        assertNotNull(attachments);
        assertTrue(attachments.isEmpty());

        Message pkcsSig = createMessageWithAttachment("application/pkcs7-signature");
        attachments = handler.createAttachmentsForMessage(pkcsSig, issue);
        assertNotNull(attachments);
        assertTrue(attachments.isEmpty());

        Message attach = createMessageWithAttachment("text/html");
        attachments = handler.createAttachmentsForMessage(attach, issue);
        assertNotNull(attachments);
        assertFalse(attachments.isEmpty());
    }

    public void testAttachedEmailMessages() throws Exception
    {
        ApplicationProperties mockApplicationProperties = new MockApplicationProperties();
        mockApplicationProperties.setOption(APKeys.JIRA_OPTION_ALLOWATTACHMENTS, true);
        AbstractMessageHandler handler = new MockAbstractMessageHandler((CommentManager) mockCommentManager.proxy(),
                                                 (IssueFactory) mockIssueFactory.proxy(),
                                                 mockApplicationProperties,
                                                 (JiraApplicationContext) mockJiraApplicationContext.proxy())
        {
            protected ChangeItemBean createAttachmentWithPart(final Part part, final User reporter, final GenericValue issue) throws IOException
            {
                return new ChangeItemBean();
            }
        };

        setupIssueAndMessage();

        Message message = createMessageFromFile("message_in_reply_to.msg");
        // if not ignoring attachments, there should not be an attachment, since the message is in reply to the attached
        // message inside it
        mockApplicationProperties.setOption(APKeys.JIRA_OPTION_IGNORE_EMAIL_MESSAGE_ATTACHMENTS, false);
        Collection attachments = handler.createAttachmentsForMessage(message, issue);
        assertNotNull(attachments);
        assertTrue(attachments.isEmpty());

        // if ignoring attachments, there should still be no attachment
        mockApplicationProperties.setOption(APKeys.JIRA_OPTION_IGNORE_EMAIL_MESSAGE_ATTACHMENTS, true);
        attachments = handler.createAttachmentsForMessage(message, issue);
        assertNotNull(attachments);
        assertTrue(attachments.isEmpty());

        message = createMessageFromFile("message_is_forwarding.msg");
        // if not ignoring attachments, there should be an attachment
        mockApplicationProperties.setOption(APKeys.JIRA_OPTION_IGNORE_EMAIL_MESSAGE_ATTACHMENTS, false);
        attachments = handler.createAttachmentsForMessage(message, issue);
        assertNotNull(attachments);
        assertFalse(attachments.isEmpty());

        // if ignoring attachments, there should be no attachment
        mockApplicationProperties.setOption(APKeys.JIRA_OPTION_IGNORE_EMAIL_MESSAGE_ATTACHMENTS, true);
        attachments = handler.createAttachmentsForMessage(message, issue);
        assertNotNull(attachments);
        assertTrue(attachments.isEmpty());
    }

    public void testGetFilenameForAttachment() throws Exception
    {
        ApplicationProperties mockApplicationProperties = new MockApplicationProperties();
        AbstractMessageHandler handler = new MockAbstractMessageHandler((CommentManager) mockCommentManager.proxy(),
                                                 (IssueFactory) mockIssueFactory.proxy(),
                                                 mockApplicationProperties,
                                                 (JiraApplicationContext) mockJiraApplicationContext.proxy());

        // "filename" header should be chosen first
        Part part = createPartFromFile("attachment_with_long_filename.part");
        String filename = handler.getFilenameForAttachment(part);
        assertTrue(filename.startsWith("SUPPORT Updated (XXXXXXX) get warning message when issue is"));

        // subject should be next if no filename
        part = createPartFromFile("attachment_with_no_filename.part");
        filename = handler.getFilenameForAttachment(part);
        assertEquals("This is the subject line", filename);

        // message id should be next if no subject
        part = createPartFromFile("attachment_with_no_subject.part");
        filename = handler.getFilenameForAttachment(part);
        assertEquals("<48604ED9.2030604@atlassian.com>", filename);

        // constant should be last resort
        part = createPartFromFile("attachment_with_no_message_id.part");
        filename = handler.getFilenameForAttachment(part);
        assertEquals("attachedmessage", filename);
    }

    public void testGetFileFromPart() throws Exception
    {
        ApplicationProperties mockApplicationProperties = new MockApplicationProperties();
        AbstractMessageHandler handler = new MockAbstractMessageHandler((CommentManager) mockCommentManager.proxy(),
                                                 (IssueFactory) mockIssueFactory.proxy(),
                                                 mockApplicationProperties,
                                                 (JiraApplicationContext) mockJiraApplicationContext.proxy());

        Part part = createPartFromFile("attachment_with_long_filename.part");
        File result = handler.getFileFromPart(part, "blah");
        assertNotNull(result);
    }

    public void testGetMessageId() throws Exception
    {
        ApplicationProperties mockApplicationProperties = new MockApplicationProperties();
        AbstractMessageHandler handler = new MockAbstractMessageHandler((CommentManager) mockCommentManager.proxy(),
                                                 (IssueFactory) mockIssueFactory.proxy(),
                                                 mockApplicationProperties,
                                                 (JiraApplicationContext) mockJiraApplicationContext.proxy());

        Message message = createMessageFromFile("message_with_no_message_id.msg");
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

    private Message createMessageFromFile(String filename) throws IOException, MessagingException
    {
        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream(new File(getDirectory(), filename));
            return new MimeMessage(Session.getDefaultInstance(new Properties()), fis);
        }
        finally
        {
            if (fis != null)
            {
                fis.close();
            }
        }
    }

    private MimeBodyPart createPartFromFile(String filename) throws IOException, MessagingException
    {
        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream(new File(getDirectory(), filename));
            return new MimeBodyPart(fis);
        }
        finally
        {
            if (fis != null)
            {
                fis.close();
            }
        }
    }

    private String getDirectory()
    {
        return new File(this.getClass().getResource("/" + this.getClass().getName().replace('.', '/') + ".class").getFile()).getParent();
    }

    private void assertRenameFileIfInvalid(String filename, String expectedFileName)
    {
        String newFileName = handler.renameFileIfInvalid(filename, issue, OSUserConverter.convertToOSUser(u1));
        assertEquals(expectedFileName, newFileName);
    }
}
