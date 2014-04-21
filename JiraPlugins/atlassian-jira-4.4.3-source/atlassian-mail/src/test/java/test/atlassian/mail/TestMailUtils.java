package test.atlassian.mail;

import com.atlassian.core.user.UserUtils;
import com.atlassian.mail.MailUtils;
import com.opensymphony.user.DuplicateEntityException;
import com.opensymphony.user.ImmutableException;
import com.opensymphony.user.User;
import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;
import test.mock.mail.MockMessage;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.NewsAddress;

public class TestMailUtils extends TestCase
{
    private User testuser;

    public TestMailUtils(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        testuser = UserUtils.createUser("test user", "password", "test@atlassian.com", "test user fullname");
    }

    protected void tearDown() throws Exception
    {
        testuser.remove();
    }

	//MAIL-49
	public void testGetAttachmentWithMissingDisposition() throws MessagingException, IOException
	{
		MimeMessage msg = new MimeMessage(Session.getDefaultInstance(new Properties()));
		MimeMultipart content = new MimeMultipart();

		BodyPart text = new MimeBodyPart();
		text.setText("this is a text");
		content.addBodyPart(text);

		MimeBodyPart attachmentPart = new MimeBodyPart();
        attachmentPart.setDataHandler(new DataHandler("testing", "text/plain"));
		attachmentPart.addHeader("Content-Type", "text/plain;\n" +
				"\tname=\"text.txt\"");
		content.addBodyPart(attachmentPart);
		msg.setContent(content);

		MailUtils.Attachment[] attachments = MailUtils.getAttachments(msg);
		assertEquals(1, attachments.length);
		assertEquals("text.txt", attachments[0].getFilename());		
	}

    public void testEmptyBodyPart() throws MessagingException
    {
        MimeMessage msg = new MimeMessage(Session.getDefaultInstance(new Properties()));

        MimeMultipart multi = new MimeMultipart();
        BodyPart plainText = new MimeBodyPart();
        BodyPart deliveryStatus = new MimeBodyPart();

        plainText.setText("test");
        deliveryStatus.setHeader("Content-Type","text/plain" );

        multi.addBodyPart(plainText);
        multi.addBodyPart(deliveryStatus);

        msg.setContent(multi);
        msg.saveChanges();

        String body = MailUtils.getBody(msg);
        assertEquals("test", body);
    }

    public void testParseAddresses() throws AddressException
    {
        InternetAddress[] addresses = MailUtils.parseAddresses("edwin@atlassian.com, mike@atlassian.com, owen@atlassian.com");

        assertEquals(new InternetAddress("edwin@atlassian.com"), addresses[0]);
        assertEquals(new InternetAddress("mike@atlassian.com"), addresses[1]);
        assertEquals(new InternetAddress("owen@atlassian.com"), addresses[2]);
    }

    public void testGetBody() throws MessagingException, IOException
    {
        testGetBodyWithContentType("text/plain");
    }
    
    public void testGetBodyWithDifferentContentTypes() throws MessagingException, IOException
    {
        testGetBodyWithContentType("TEXT/PLAIN");
        testGetBodyWithContentType("tExt/plAIN");

        testGetBodyWithContentType("text/html");
        testGetBodyWithContentType("text/HTML");
        testGetBodyWithContentType("TEXT/HTML");
    }

    private void testGetBodyWithContentType(final String contentType) throws MessagingException
    {
        Message msg = new MimeMessage(null, new ByteArrayInputStream("test".getBytes()));
        msg.setContent("test message", "");
        msg.setHeader("Content-Type", contentType);
        assertEquals("test message", MailUtils.getBody(msg));

        msg.setContent(new Integer(1), contentType);
        assertNull(MailUtils.getBody(msg));
    }

    public void testGetAuthorFromSender() throws MessagingException, ImmutableException, DuplicateEntityException
    {
        Message msg = new MimeMessage(null, new ByteArrayInputStream("test".getBytes()));
        assertNull(MailUtils.getAuthorFromSender(msg));

        msg.setFrom(new InternetAddress("edwin@atlassian.com"));
        assertNull(MailUtils.getAuthorFromSender(msg));

        msg.setFrom(new InternetAddress("test@atlassian.com"));
        assertNotNull(MailUtils.getAuthorFromSender(msg));
        assertEquals(testuser, MailUtils.getAuthorFromSender(msg));
    }

    public void testGetFirstValidUser() throws ImmutableException, DuplicateEntityException, AddressException
    {
        Address[] blankaddresslist = { };
        assertNull(MailUtils.getFirstValidUser(blankaddresslist));

        Address[] nouseraddresslist = { new InternetAddress("edwin@atlassian.com"), new InternetAddress("owen@atlassian.coM") };
        assertNull(MailUtils.getFirstValidUser(nouseraddresslist));

        Address[] addresslist = { new InternetAddress("mike@atlassian.com"), new InternetAddress("test@atlassian.com") };
        assertNotNull(MailUtils.getFirstValidUser(addresslist));
        assertEquals(testuser, MailUtils.getFirstValidUser(addresslist));
    }

    /**
     * Creating edge cases for this test is difficult because the mail api
     * tries to stop you doing funky things with nulls, blanks and padded
     * values for addresses. This looks like the 80% coverage with 20% effort.
     *
     * @throws Exception
     */
    public void testGetSenders() throws Exception
    {
        Address[] blankaddresslist = { };

        Message msg = createMockMessageWithSenders(blankaddresslist);
        assertEquals(0, MailUtils.getSenders(msg).size());

        // make a mock message that will return a null in the from address list
        Address[] allNulls = { null, null };
        msg = createMockMessageWithSenders(allNulls);
        assertEquals(0, MailUtils.getSenders(msg).size());

        // check mixed bad with one good address list
        String goodAddress = "good@address.com";
        Address[] oneGood = { null, new InternetAddress(goodAddress) };
        msg = createMockMessageWithSenders(oneGood);
        List senders = MailUtils.getSenders(msg);
        assertEquals(1, senders.size());
        assertEquals(goodAddress, senders.get(0));

        // check mixed bag of types, nulls and a padded duplicate
        Address[] mixedBag = {
                new InternetAddress(goodAddress),
                new NewsAddress(),
                null,
                new InternetAddress("  " + goodAddress) };
        msg = createMockMessageWithSenders(mixedBag);
        senders = MailUtils.getSenders(msg);
        assertEquals(2, senders.size());
        // should have two the same (trimmed)
        assertEquals(goodAddress, senders.get(0));
        assertEquals(goodAddress, senders.get(1));
    }

    private Message createMockMessageWithSenders(final Address[] addresses) throws MessagingException
    {
        Message msg = new MockMessage()
        {

            public Address[] getFrom() throws MessagingException
            {
                return addresses;
            }
        };
        return msg;
    }


    public void testHasRecipient() throws MessagingException
    {
        Message msg = new MimeMessage(null, new ByteArrayInputStream("test".getBytes()));
        assertTrue(!MailUtils.hasRecipient("edwin@atlassian.com", msg));
        msg.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress("edwin@atlassian.com"));
        assertTrue(MailUtils.hasRecipient("edwin@atlassian.com", msg));
    }

    public void testCreateAttachmentMimeBodyPartFileNames() throws MessagingException
    {
        MimeBodyPart mbp = MailUtils.createAttachmentMimeBodyPart("C:/test/foo/bar/export.zip");
        assertEquals("export.zip", mbp.getFileName());
    }

    public void testCreateAttachmentMimeBodyPartFileNames2() throws MessagingException
    {
        MimeBodyPart mbp = MailUtils.createAttachmentMimeBodyPart("C:\\test\\foo\\bar\\export.zip");
        assertEquals("export.zip", mbp.getFileName());
    }

    public void testGetBodyText() throws MessagingException
    {
        assertEquals("This is a simple test mail, which isn't in HTML and has no attachments.",
                MailUtils.getBody(makeMessageFromResourceFile("/testmails/simplebody.txt")));
    }

    public void testGetBodyTextIfContentEncodingUnsupported() throws MessagingException
    {
        assertEquals("This is a simple test mail, which isn't in HTML and has no attachments.",
                     MailUtils.getBody(makeMessageFromResourceFile("/testmails/simplebodyunsupportedencoding.txt")));
    }

    public void testGetBodyHtml() throws MessagingException
    {
        assertEquals("This is an HTML test mail\nwith a linebreak.",
                MailUtils.getBody(makeMessageFromResourceFile("/testmails/simplehtml.txt")));
    }

    public void testGetBodyMultipartAlternativeGetsTextMatch() throws MessagingException
    {
        assertEquals("This is the text part of the mail",
                MailUtils.getBody(makeMessageFromResourceFile("/testmails/multipart.txt")));
    }

    public void testGetBodyMultipartGetsFirstTextMatch() throws MessagingException
    {
        assertEquals("This is the first part",
                MailUtils.getBody(makeMessageFromResourceFile("/testmails/multipartbothtext.txt")));
    }

    public void testGetBodyMultipartGetsHtmlIfNoText() throws MessagingException
    {
        assertEquals("This is\nthe HTML part of the mail",
                MailUtils.getBody(makeMessageFromResourceFile("/testmails/multipartnotext.txt")));
    }

    public void testGetBodyMultipartNoMatch() throws MessagingException
    {
        assertEquals("", MailUtils.getBody(makeMessageFromResourceFile("/testmails/multipartnobody.txt")));
    }

    public void testGetBodyMultipartMixed() throws MessagingException
    {
        assertEquals("Text alternative", MailUtils.getBody(makeMessageFromResourceFile("/testmails/multipartmixedotherorder.txt")));
    }

    public void testGetBodyMultipartMixedWithLotsOfTextAndHtml() throws MessagingException
    {
        assertEquals("Text alternative\nThis is the first part after the alternative\nThis is the second part after the alternative",
                MailUtils.getBody(makeMessageFromResourceFile("/testmails/multipartmixedtextandhtml.txt")));
    }

    public void testGetAttachmentsNoAttachments() throws MessagingException, IOException
    {
        assertEquals("no attachments", 0,
                MailUtils.getAttachments(makeMessageFromResourceFile("/testmails/multipart.txt")).length);
    }

    public void testGetAttachmentsIfContentEncodingIsNotSupported() throws MessagingException, IOException
    {
        Message message = makeMessageFromResourceFile("/testmails/multipartunsupportedencoding.txt");
        assertEquals("This is the text part of the mail", MailUtils.getBody(message));
        assertEquals("no attachments", 0,
                     MailUtils.getAttachments(message).length);
    }

    public void testGetAttachmentsHasAttachments() throws MessagingException, IOException
    {
        MailUtils.Attachment[] attachments = MailUtils.getAttachments(makeMessageFromResourceFile("/testmails/multipartmixed.txt"));
        assertEquals("has attachments", 2, attachments.length);
        assertEquals("Attachment 1", new String(attachments[0].getContents()));
        assertEquals("image/jpeg", attachments[0].getContentType().substring(0, 10));
        assertEquals("bugger.jpg", attachments[0].getFilename());
        assertEquals("html<br>attachment", new String(attachments[1].getContents()));
        assertEquals("text/html", attachments[1].getContentType().substring(0, 9));
        assertEquals("foo.html", attachments[1].getFilename());
    }

    private Message makeMessageFromResourceFile(String resourceName) throws MessagingException
    {
        return new MimeMessage(null, this.getClass().getResourceAsStream(resourceName));
    }


    public void testGetContentTypeFromHeaderValue()
    {
        final String input = "text/plain";
        final String actual = MailUtils.getContentType(input);
        final String expected = input;

        assertEquals(expected, actual);
    }

    public void testGetContentTypeFromHeaderValueWithParameter()
    {
        final String input = "text/plain; something=somevalue";
        final String actual = MailUtils.getContentType(input);
        final String expected = "text/plain";

        assertEquals(expected, actual);
    }

    /**
     * A simplistic attempt at a enum safe pattern. Use these constnats to make the checkParts invocations
     * more englishlike and readable.
     */
    final static boolean PLAIN_TEXT_YES = true;
    final static boolean PLAIN_TEXT_NO = false;

    final static boolean HTML_YES = true;
    final static boolean HTML_NO = false;

    final static boolean INLINE_YES = true;
    final static boolean INLINE_NO = false;

    final static boolean ATTACHMENT_YES = true;
    final static boolean ATTACHMENT_NO = false;

    public void testIsPlainTextPart() throws Exception
    {
        this.checkParts("OutlookPlainText.msg", PLAIN_TEXT_YES, HTML_YES, INLINE_NO, ATTACHMENT_NO);
    }

    public void testIsHtmlPart() throws Exception
    {
        this.checkParts("OutlookHtml.msg", PLAIN_TEXT_YES, HTML_YES, INLINE_NO, ATTACHMENT_NO);
    }

    public void testIsImageAttachedPart() throws Exception
    {
        this.checkParts("OutlookHtmlImageAttached.msg", PLAIN_TEXT_YES, HTML_YES, INLINE_NO, ATTACHMENT_YES);
    }

    public void testIsInlineImagePart() throws Exception
    {
        this.checkParts("OutlookHtmlInlineImage.msg", PLAIN_TEXT_YES, HTML_YES, INLINE_YES, ATTACHMENT_NO);
    }

    public void testIsInlineImagePartWherePartHasDisposition() throws Exception
    {
        this.checkParts("ThunderbirdHtmlAndPlainTextInlineImage.msg", PLAIN_TEXT_YES, HTML_YES, INLINE_YES, ATTACHMENT_NO);
    }

    public void testIsAttachmentPart() throws Exception
    {
        this.checkParts("OutlookHtmlBinaryAttachment.msg", PLAIN_TEXT_YES, HTML_YES, INLINE_NO, ATTACHMENT_YES);
    }

    public void testIsRelatedPart() throws Exception
    {
        assertFalse(MailUtils.isPartRelated(makeMessageFromResourceFile("/testmails/multipartmixedtextandhtml.txt")));
        assertTrue(MailUtils.isPartRelated(makeMessageFromResourceFile("/testmails/multipartrelated.txt")));
    }

    /**
     * The mail method which firstly creates a message from a message file.
     * After that all the parts present are tested against the predicates passed in as the boolean parameters. Eg if plainTextPresent is set to false
     * and a plain text part is found by MailUtils.isPartPlainText() then it will complain. Its got other smarts to make sure the XXXPresent parameters
     * match what was found.
     *
     * @param filename
     * @param plainTextExpected
     * @param htmlExpected
     * @param inlineExpected
     * @param attachmentExpected
     * @throws Exception
     */
    void checkParts(final String filename, final boolean plainTextExpected, final boolean htmlExpected, final boolean inlineExpected, final boolean attachmentExpected)
            throws Exception
    {
        assertNotNull(filename);

        final Part[] parts = this.createPartsFromMessage(filename);
        assertNotNull("parts", parts);
        assertTrue("Expected atleast 1 part but got " + parts.length + " part(s)", parts.length > 0);

        // testing time...
        boolean plainTextFound = false;
        boolean htmlFound = false;
        boolean inlineFound = false;
        boolean attachmentFound = false;

        for (int i = 0; i < parts.length; i++)
        {
            final Part part = parts[i];

            if (MailUtils.isPartPlainText(part))
            {
                assertTrue("PlainText part found when none was expected", plainTextExpected);
                plainTextFound = true;
                continue;
            }

            if (MailUtils.isPartHtml(part))
            {
                assertTrue("Html part found when none was expected", htmlExpected);
                htmlFound = true;
            }

            if (MailUtils.isPartInline(part))
            {
                assertTrue("Inline part found when none was expected", inlineExpected);
                inlineFound = true;

                final boolean reportedEmpty = MailUtils.isContentEmpty(part);
                assertFalse("All inline parts in the prepared msg files are never empty...", reportedEmpty);
            }

            if (MailUtils.isPartAttachment(part))
            {
                assertTrue("Attachment part found when none was expected", attachmentExpected);
                attachmentFound = true;

                final boolean reportedEmpty = MailUtils.isContentEmpty(part);
                assertFalse("All attachments parts in the prepared msg files are never empty...", reportedEmpty);
            }
        }

        assertEquals("Expected to find a plain text part but one was not found", plainTextExpected, plainTextFound);
        assertEquals("Expected to find a html part but one was not found", htmlExpected, htmlFound);
        assertEquals("Expected to find a inline part but one was not found", inlineExpected, inlineFound);
        assertEquals("Expected to find a attachment part but one was not found", attachmentExpected, attachmentFound);
    }

    /**
     * Nice little method that creates an array of parts after creating a new message from a *.msg file.
     * Any multiparts are traversed until to grab their parts.
     *
     * @param filename The test msg to load.
     * @return An array of parts.
     * @throws Exception
     */
    Part[] createPartsFromMessage(final String filename) throws Exception
    {
        assertNotNull(filename);

        InputStream fis = null;

        try
        {
            fis = getTestEmailFile(filename);

            // build a message...
            final Message message = new MimeMessage(Session.getDefaultInstance(new Properties()), fis);
            final Part[] parts = this.getAllParts(message);
            assertNotNull("parts", parts);
            assertTrue("Expected atleast 1 part but got " + parts.length + " part(s)", parts.length > 0);

            return parts;
        }
        finally
        {
            IOUtils.closeQuietly(fis);
        }
    }

    private InputStream getTestEmailFile(String filename) throws FileNotFoundException
    {
        // try it as a class resource
        return getClass().getResourceAsStream("/testmails/" + filename);
    }

    /**
     * This method along with its helpers returns all parts that contain content other than Multiparts( they are continuously
     * expanded until the raw parts are located). All these parts are placed into an array and returned.
     *
     * @param message The message
     * @return An array of parts nb, no MultiParts will be in here...
     * @throws Exception only because javamail does...
     */
    Part[] getAllParts(final Message message) throws Exception
    {
        final List partsList = new ArrayList();
        this.processMessageParts(message, partsList);
        final Part[] parts = (Part[]) partsList.toArray(new Part[0]);
        return parts;
    }

    void processMessageParts(final Message message, final Collection parts) throws Exception
    {
        final Object content = message.getContent();
        this.handlePartOrContent(content, parts);
    }

    void handlePartOrContent(final Object partOrContent, final Collection parts) throws Exception
    {
        if (partOrContent instanceof Multipart)
        {
            this.processMultipartParts((Multipart) partOrContent, parts);
            return;
        }

        if (partOrContent instanceof BodyPart)
        {
            final BodyPart bodyPart = (BodyPart) partOrContent;
            final Object bodyPartContent = bodyPart.getContent();

            if (bodyPartContent instanceof Multipart)
            {
                this.handlePartOrContent(bodyPartContent, parts);
            }
            else
            {
                parts.add(bodyPart);
            }
            return;
        }
        parts.add((Part) partOrContent);
    }

    void processMultipartParts(final Multipart multipart, final Collection parts) throws Exception
    {
        final int partsCount = multipart.getCount();
        for (int i = 0; i < partsCount; i++)
        {
            final BodyPart bodyPart = multipart.getBodyPart(i);
            this.handlePartOrContent(bodyPart, parts);
        }
    }


    static final String X_PKCS7_SIGNATURE = "application/x-pkcs7-signature";

    public void testPositivePartIsSignature() throws Exception
    {
        final Message message = createMessageWithAttachment(X_PKCS7_SIGNATURE, "attachment", "filename", "xxxx");
        final boolean actual = MailUtils.isPartSignaturePKCS7(message);
        assertTrue("The message being tested is of \"" + X_PKCS7_SIGNATURE + "\" but MailUtils returned false instead of true", actual);
    }

    public void testNegativePartIsSignature() throws Exception
    {
        final Message message = createMessageWithAttachment("text/plain", "", null, "xxxx");
        final boolean actual = MailUtils.isPartSignaturePKCS7(message);
        assertFalse("The message being tested is of \"" + X_PKCS7_SIGNATURE + "\" but MailUtils returned true for a text/plain when it should have returned false.", actual);
    }

    public void testTextPartContentIsEmpty() throws Exception
    {
        final Message message = createMessageWithAttachment("text/plain", "", null, "");
        final boolean actual = MailUtils.isContentEmpty(message);
        assertTrue("The plaintext message contains a part that should be empty.", actual);
    }


    public void testTextPartContentIsEmptyOnNullContent() throws Exception
    {
        final Message message = createMessageWithAttachment("text/plain", "", null, (String) null);
        final boolean actual = MailUtils.isContentEmpty(message);
        assertTrue("The plaintext message contains a part that should be empty.", actual);
    }

    public void testTextPartContentIsEmptyBecauseContainsOnlyWhitespace() throws Exception
    {
        final Message message = createMessageWithAttachment("text/plain", "", null, "   ");
        final boolean actual = MailUtils.isContentEmpty(message);
        assertTrue("The plaintext message contains a part that should be empty.", actual);
    }

    public void testBinaryAttachmentPartContentIsEmpty() throws Exception
    {
        final Message message = createMessageWithAttachment("binary/octet-stream", "file.bin", Part.ATTACHMENT, new byte[0]);
        final boolean actual = MailUtils.isContentEmpty(message);
        assertTrue("The attachment part should be empty.", actual);
    }

    public void testBinaryAttachmentPartContentIsNotEmpty() throws Exception
    {
        final Message message = createMessageWithAttachment("binary/octet-stream", "file.bin", Part.ATTACHMENT, " NOT EMPTY!!!  ".getBytes());
        final boolean actual = MailUtils.isContentEmpty(message);
        assertFalse("The attachment part should be empty.", actual);
    }

    public void testInlineAttachment() throws Exception
    {
        final Message message = createMessageWithAttachment("image/jpeg", "image.jpeg", Part.INLINE, " NOT EMPTY!!!  ".getBytes());
        final boolean actual = MailUtils.isPartInline(message);
        assertFalse("The attachment part is an inline part.", actual);
    }

    /**
     * This test tests the oddf case where a part has a content-id but is not base64 encoded(aka binary) and therefore
     * fails to be recognised as a probable inline part.
     *
     * @throws Exception
     */
    public void testInlineAttachmentThatIsntBase64Encoded() throws Exception
    {
        final Message message = createMessageWithAttachment("image/jpeg", "image.jpeg", null, " NOT EMPTY!!!  ".getBytes());
        message.setHeader("Content-ID", "1234567890");
        final boolean actual = MailUtils.isPartInline(message);
        assertFalse("The attachment part isnt not base64 encoded.", actual);
    }

    /**
     * Helper which creates a message with a part with the given disposition, filename and content etc.
     *
     * @param mimeType
     * @param disposition
     * @param filename
     * @param content
     * @return
     * @throws Exception
     */
    Message createMessageWithAttachment(final String mimeType, final String disposition, final String filename, final String content)
            throws Exception
    {
        assertNotNull("mimeType", mimeType);
        assertTrue("mimeType must not be empty", mimeType.length() > 0);

        final Message message = new MimeMessage(Session.getDefaultInstance(new Properties()));
        message.setContent(content, mimeType);
        if (null != filename && filename.length() > 0)
        {
            message.setFileName(filename);
        }
        message.setHeader("Content-Type", mimeType);
        message.setDisposition(disposition);
        return message;
    }

    Message createMessageWithAttachment(final String mimeType, final String disposition, final String filename, final byte[] content)
            throws Exception
    {
        assertNotNull("mimeType", mimeType);
        assertTrue("mimeType must not be empty", mimeType.length() > 0);

        final Message message = new MimeMessage(Session.getDefaultInstance(new Properties()));
        message.setContent(new ByteArrayInputStream(content), mimeType);
        if (null != filename && filename.length() > 0)
        {
            message.setFileName(filename);
        }
        message.setHeader("Content-Type", mimeType);
        message.setDisposition(disposition);
        return message;
    }
}
