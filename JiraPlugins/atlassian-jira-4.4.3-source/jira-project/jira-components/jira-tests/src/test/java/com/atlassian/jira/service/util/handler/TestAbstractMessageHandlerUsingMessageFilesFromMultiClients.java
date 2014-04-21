package com.atlassian.jira.service.util.handler;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.JiraApplicationContext;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.action.issue.TemporaryAttachmentsMonitor;
import com.atlassian.jira.web.util.AttachmentException;
import com.atlassian.mail.MailUtils;
import com.mockobjects.dynamic.Mock;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.util.UtilDateTime;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * Integration Test for the functionality of the AbstractMessageHandler.
 * Prefer {@link TestAbstractMessageHandler} where possible.
 * <p/>
 * Note that for all test<InsertMailClientNameHere>XXX the image and binary attachment is the same.
 * To make testing simpler in some cases images within a msg file have had their mimetype changed to match #IMAGE_MIME_TYPE
 * <p/>
 * <h3>Testing approach</h3>
 * A wide variety of email clients have been used to produce a combination of different styles of emails with different
 * attachments. Most messages where created with the same client and should be one of the following.
 * <ul>
 * <li>Thunderbird 2.0.0.14 (Windows/20080421)</li>
 * <li>Gmail 25 July 2008 - Ajax version</li>
 * <li>Evolution 2.22.3.1 </li>
 * <li>Apple Mail (2.926)</li>
 * <li>Lotus Notes</li>
 * <li>Microsoft Outlook Express 6.00.2900.3138</li>
 * </ul>
 * <p/>
 * <h3>Attachment naming & content type</h3>
 * All attachments use a common naming mechanism provided the client includes (aka keeps) the original filename as part
 * of the part included in the email.
 * <ul>
 * <li>Plain text files = plain.txt</li>
 * <li>Html files = page.html</li>
 * <li>Image files = image.jpeg</li>
 * <li>Binary files = binary.bin</li>
 * </ul>
 * <p/>
 * Even if the image file included in the email is not a true jpeg but a gif, the msg file has been edited
 * with the original content type changed to image/jpeg to simplify testing. The base64 encoded form within the part
 * remains untouched.
 * <p/>
 * <h3>Example test</h3>
 * All the tests that include a client within the name such as {@link #testAppleHtmlBinaryAttached()} have simple emails which
 * have a minimum number of parts. The prepared attachment for {@link #testAppleHtmlBinaryAttached()} uses a similarly
 * named message file which has 3 parts,
 * <ul>
 * <li>A html part containing the html form of the email content</li>
 * <li>A plain text part containing the plain text form of the email.</li>
 * <li>A binary attachment with a specific filename.</li>
 * </ul>
 * <p/>
 * To verify that the right attachments have made it through, the tests perform 4 checks.
 * For instance in the above html with a binary attachment sent from applemail, the 4 tests would check that no
 * attachment with a content type of plain, html and image attachment is present in the attachment manager.
 * However the attachment manager must have a binary attachment.
 */
public class TestAbstractMessageHandlerUsingMessageFilesFromMultiClients extends AbstractTestMessageHandler
{
    SimpleMockAbstractMessageHandler handler = null;

    public TestAbstractMessageHandlerUsingMessageFilesFromMultiClients(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        if (issue == null)
        {
            issue = UtilsForTests.getTestEntity
                    (
                            "Issue",
                            MapBuilder.newBuilder().
                                    add("key", ISSUE_KEY).
                                    add("reporter", TESTUSER_USERNAME).
                                    add("assignee", TESTUSER_USERNAME).
                                    add("project", projectA.getLong("id")).
                                    add("updated", UtilDateTime.nowTimestamp()).
                                    toHashMap()
                    );


            issueObject = getIssueFactory().getIssue(issue);
        }

        // we need to create a new one because it holds some state - ie the attachments
        this.handler = createAbstractMessageHandler();
    }

    private IssueFactory getIssueFactory()
    {
        return ComponentManager.getComponentInstanceOfType(IssueFactory.class);
    }

    /**
     * Start of tests for messages composed using thunderbird.
     * @throws Exception ignored
     */
    public void testThunderbirdHtmlImageAttachment() throws Exception
    {
        final Message message = HandlerTestUtil.createMessageFromFile("ThunderbirdHtmlImageAttachment.msg");

        assertMessageIdPresent(message, handler);
        HandlerTestUtil.assertSubjectNotEmpty(message);

        final Collection<SimpleAttachment> attachments = createThenReturnAttachments(message, handler);
        assertPlainTextAttachmentNotPresent(attachments);
        assertHtmlAttachmentNotPresent(attachments);
        assertImageAttachmentPresent(attachments);
        assertBinaryAttachmentNotPresent(attachments);
    }

    public void testThunderbirdHtmlAndPlainTextBinaryAttachment() throws Exception
    {
        final Message message = HandlerTestUtil.createMessageFromFile("ThunderbirdHtmlAndPlainTextBinaryAttachment.msg");

        assertMessageIdPresent(message, handler);
        HandlerTestUtil.assertSubjectNotEmpty(message);

        final Collection<SimpleAttachment> attachments = createThenReturnAttachments(message, handler);
        assertPlainTextAttachmentNotPresent(attachments);
        assertHtmlAttachmentNotPresent(attachments);
        assertImageAttachmentNotPresent(attachments);
        assertBinaryAttachmentPresent(attachments);
    }

    public void testThunderbirdHtmlAndPlainTextImageAttachment() throws Exception
    {
        final Message message = HandlerTestUtil.createMessageFromFile("ThunderbirdHtmlAndPlainTextImageAttachment.msg");

        assertMessageIdPresent(message, handler);
        HandlerTestUtil.assertSubjectNotEmpty(message);

        final Collection<SimpleAttachment> attachments = createThenReturnAttachments(message, handler);
        assertPlainTextAttachmentNotPresent(attachments);
        assertHtmlAttachmentNotPresent(attachments);
        assertImageAttachmentPresent(attachments);
        assertBinaryAttachmentNotPresent(attachments);
    }

    public void testThunderbirdHtmlAndPlainTextInlineImage() throws Exception
    {
        final Message message = HandlerTestUtil.createMessageFromFile("ThunderbirdHtmlAndPlainTextInlineImage.msg");

        assertMessageIdPresent(message, handler);
        HandlerTestUtil.assertSubjectNotEmpty(message);

        final Collection<SimpleAttachment> attachments = createThenReturnAttachments(message, handler);
        assertPlainTextAttachmentNotPresent(attachments);
        assertHtmlAttachmentNotPresent(attachments);
        assertImageAttachmentPresent(attachments);
        assertBinaryAttachmentNotPresent(attachments);
    }

    public void testThunderbirdHtmlBinaryAttachment() throws Exception
    {
        final Message message = HandlerTestUtil.createMessageFromFile("ThunderbirdHtmlOnlyBinaryAttachment.msg");

        assertMessageIdPresent(message, handler);
        HandlerTestUtil.assertSubjectNotEmpty(message);

        final Collection<SimpleAttachment> attachments = createThenReturnAttachments(message, handler);
        assertPlainTextAttachmentNotPresent(attachments);
        assertHtmlAttachmentNotPresent(attachments);
        assertImageAttachmentNotPresent(attachments);
        assertBinaryAttachmentPresent(attachments);
    }

    public void testThunderbirdHtmlInlineImage() throws Exception
    {
        final Message message = HandlerTestUtil.createMessageFromFile("ThunderbirdHtmlOnlyInlineImage.msg");

        assertMessageIdPresent(message, handler);
        HandlerTestUtil.assertSubjectNotEmpty(message);

        final Collection<SimpleAttachment> attachments = createThenReturnAttachments(message, handler);
        assertPlainTextAttachmentNotPresent(attachments);
        assertHtmlAttachmentNotPresent(attachments);
        assertImageAttachmentPresent(attachments);
        assertBinaryAttachmentNotPresent(attachments);
    }

    public void testThunderbirdPlainTextImageAttachment() throws Exception
    {
        final Message message = HandlerTestUtil.createMessageFromFile("ThunderbirdPlainTextImageAttachment.msg");

        assertMessageIdPresent(message, handler);
        HandlerTestUtil.assertSubjectNotEmpty(message);

        final Collection<SimpleAttachment> attachments = createThenReturnAttachments(message, handler);
        assertPlainTextAttachmentNotPresent(attachments);
        assertHtmlAttachmentNotPresent(attachments);
        assertImageAttachmentNotPresent(attachments);
        assertBinaryAttachmentNotPresent(attachments);
    }

    public void testThunderbirdPlainTextBinaryAttachment() throws Exception
    {
        final Message message = HandlerTestUtil.createMessageFromFile("ThunderbirdPlainTextOnlyBinaryAttachment.msg");

        assertMessageIdPresent(message, handler);
        HandlerTestUtil.assertSubjectNotEmpty(message);

        final Collection<SimpleAttachment> attachments = createThenReturnAttachments(message, handler);
        assertPlainTextAttachmentNotPresent(attachments);
        assertHtmlAttachmentNotPresent(attachments);
        assertImageAttachmentNotPresent(attachments);
        assertBinaryAttachmentPresent(attachments);
    }

    public void testThunderbirdHtmlWithPlainTextAttachment() throws Exception
    {
        final Message message = HandlerTestUtil.createMessageFromFile("ThunderbirdHtmlWithPlainTextAttachment.msg");

        assertMessageIdPresent(message, handler);
        HandlerTestUtil.assertSubjectNotEmpty(message);

        final Collection<SimpleAttachment> attachments = createThenReturnAttachments(message, handler);
        assertPlainTextAttachmentPresent(attachments);
        assertHtmlAttachmentNotPresent(attachments);
        assertImageAttachmentNotPresent(attachments);
        assertBinaryAttachmentNotPresent(attachments);
    }

    public void testThunderbirdHtmlWithHtmlAttachment() throws Exception
    {
        final Message message = HandlerTestUtil.createMessageFromFile("ThunderbirdHtmlWithHtmlAttachment.msg");

        assertMessageIdPresent(message, handler);
        HandlerTestUtil.assertSubjectNotEmpty(message);

        final Collection<SimpleAttachment> attachments = createThenReturnAttachments(message, handler);
        assertPlainTextAttachmentNotPresent(attachments);
        assertHtmlAttachmentPresent(attachments);
        assertImageAttachmentNotPresent(attachments);
        assertBinaryAttachmentNotPresent(attachments);
    }

    public void testLotusNotesHtmlAttachedImage() throws Exception
    {
        final Message message = HandlerTestUtil.createMessageFromFile("LotusNotesHtmlImageAttached.msg");
        assertMessageIdPresent(message, handler);
        HandlerTestUtil.assertSubjectNotEmpty(message);

        final Collection<SimpleAttachment> attachments = createThenReturnAttachments(message, handler);
        assertPlainTextAttachmentNotPresent(attachments);
        assertHtmlAttachmentNotPresent(attachments);

        // unfortunately notes sets the content type of the image attached to application/octet-stream so the
        // assertImageAttachmentXXX and assertBinaryXXX will fail because whilst they will find the content type
        // they also attempt to verify the filename which is hardcoded for each of the content types.
        assertEquals(attachments.toString(), 1, attachments.size());
        boolean imageFound = false;
        for (SimpleAttachment attachment : attachments)
        {
            assertEquals(attachments.toString(), "application/octet-stream", attachment.getContentType());
            assertEquals(attachments.toString(), "image.jpeg", attachment.getFilename());
            imageFound = true;
        }
        assertTrue("Image attachment not found amongst attachments: " + attachments.toString(), imageFound);
    }

    public void testLotusNotesHtmlInlineImage() throws Exception
    {
        final Message message = HandlerTestUtil.createMessageFromFile("LotusNotesHtmlInlineImage.msg");
        assertMessageIdPresent(message, handler);
        HandlerTestUtil.assertSubjectNotEmpty(message);

        final Collection<SimpleAttachment> attachments = createThenReturnAttachments(message, handler);
        assertPlainTextAttachmentNotPresent(attachments);
        assertHtmlAttachmentNotPresent(attachments);
        assertImageAttachmentPresent(attachments);
        assertBinaryAttachmentNotPresent(attachments);
    }

    public void testLotusNotesHtmlBinaryAttachment() throws Exception
    {
        final Message message = HandlerTestUtil.createMessageFromFile("LotusNotesHtmlBinaryAttachment.msg");
        assertMessageIdPresent(message, handler);
        HandlerTestUtil.assertSubjectNotEmpty(message);

        final Collection<SimpleAttachment> attachments = createThenReturnAttachments(message, handler);
        assertPlainTextAttachmentNotPresent(attachments);
        assertHtmlAttachmentNotPresent(attachments);
        assertImageAttachmentNotPresent(attachments);
        assertBinaryAttachmentPresent(attachments);
    }

    public void testOutlookPlainText() throws Exception
    {
        final Message message = HandlerTestUtil.createMessageFromFile("OutlookPlainText.msg");
        assertMessageIdPresent(message, handler);
        HandlerTestUtil.assertSubjectNotEmpty(message);

        final Collection<SimpleAttachment> attachments = createThenReturnAttachments(message, handler);
        assertPlainTextAttachmentNotPresent(attachments);
        assertHtmlAttachmentNotPresent(attachments);
        assertImageAttachmentNotPresent(attachments);
        assertBinaryAttachmentNotPresent(attachments);
    }

    public void testOutlookHtml() throws Exception
    {
        final Message message = HandlerTestUtil.createMessageFromFile("OutlookHtml.msg");
        assertMessageIdPresent(message, handler);
        HandlerTestUtil.assertSubjectNotEmpty(message);

        final Collection<SimpleAttachment> attachments = createThenReturnAttachments(message, handler);
        assertPlainTextAttachmentNotPresent(attachments);
        assertHtmlAttachmentNotPresent(attachments);
        assertImageAttachmentNotPresent(attachments);
        assertBinaryAttachmentNotPresent(attachments);
    }

    public void testOutlookHtmlImageAttached() throws Exception
    {
        final Message message = HandlerTestUtil.createMessageFromFile("OutlookHtmlImageAttached.msg");
        assertMessageIdPresent(message, handler);
        HandlerTestUtil.assertSubjectNotEmpty(message);

        final Collection<SimpleAttachment> attachments = createThenReturnAttachments(message, handler);
        assertPlainTextAttachmentNotPresent(attachments);
        assertHtmlAttachmentNotPresent(attachments);
        assertImageAttachmentPresent(attachments);
        assertBinaryAttachmentNotPresent(attachments);
    }

    public void testOutlookHtmlInlineImage() throws Exception
    {
        final Message message = HandlerTestUtil.createMessageFromFile("OutlookHtmlInlineImage.msg");
        assertMessageIdPresent(message, handler);
        HandlerTestUtil.assertSubjectNotEmpty(message);

        final Collection<SimpleAttachment> attachments = createThenReturnAttachments(message, handler);
        assertPlainTextAttachmentNotPresent(attachments);
        assertHtmlAttachmentNotPresent(attachments);
        assertImageAttachmentPresent(attachments);
        assertBinaryAttachmentNotPresent(attachments);
    }

    public void testOutlookHtmlBinaryAttachment() throws Exception
    {
        final Message message = HandlerTestUtil.createMessageFromFile("OutlookHtmlBinaryAttachment.msg");
        assertMessageIdPresent(message, handler);
        HandlerTestUtil.assertSubjectNotEmpty(message);

        final Collection<SimpleAttachment> attachments = createThenReturnAttachments(message, handler);
        assertPlainTextAttachmentNotPresent(attachments);
        assertHtmlAttachmentNotPresent(attachments);
        assertImageAttachmentNotPresent(attachments);
        assertBinaryAttachmentPresent(attachments);
    }

    public void testOutlookHtmlWithPlainTextAttachment() throws Exception
    {
        final Message message = HandlerTestUtil.createMessageFromFile("OutlookHtmlWithPlainTextAttachment.msg");
        assertMessageIdPresent(message, handler);
        HandlerTestUtil.assertSubjectNotEmpty(message);

        final Collection<SimpleAttachment> attachments = createThenReturnAttachments(message, handler);
        assertPlainTextAttachmentPresent(attachments);
        assertHtmlAttachmentNotPresent(attachments);
        assertImageAttachmentNotPresent(attachments);
        assertBinaryAttachmentNotPresent(attachments);
    }

    public void testOutlookHtmlWithHtmlAttachment() throws Exception
    {
        final Message message = HandlerTestUtil.createMessageFromFile("OutlookHtmlWithHtmlAttachment.msg");
        assertMessageIdPresent(message, handler);
        HandlerTestUtil.assertSubjectNotEmpty(message);

        // unfortunately outlook sets the attachment type of the html attachment to text/plain when it should be
        // text/html which screws up our assert methods which use a naming convention where the attachment filename
        // is taken from the content type..
        final Collection<SimpleAttachment> attachments = createThenReturnAttachments(message, handler);

        assertEquals(attachments.toString(), 1, attachments.size());
        boolean htmlPageFound = false;
        for (Object attachmentObject : attachments)
        {
            final SimpleAttachment attachment = (SimpleAttachment) attachmentObject;
            assertEquals(attachments.toString(), "text/plain", attachment.getContentType());
            assertEquals(attachments.toString(), "page.html", attachment.getFilename());
            htmlPageFound = true;
        }
        assertTrue("HtmlPage attachment not found amongst attachments: " + attachments.toString(), htmlPageFound);

        assertImageAttachmentNotPresent(attachments);
        assertBinaryAttachmentNotPresent(attachments);
    }

    public void testOutlookRichTextInlineImage() throws Exception
    {
        final Message message = HandlerTestUtil.createMessageFromFile("OutlookRichTextInlineImage.msg");
        assertMessageIdPresent(message, handler);
        HandlerTestUtil.assertSubjectNotEmpty(message);

        final Collection<SimpleAttachment> attachments = createThenReturnAttachments(message, handler);
        assertPlainTextAttachmentNotPresent(attachments);
        assertHtmlAttachmentNotPresent(attachments);
        assertImageAttachmentPresent(attachments);
        assertBinaryAttachmentNotPresent(attachments);
    }

    public void testGmailPlainText() throws Exception
    {
        final Message message = HandlerTestUtil.createMessageFromFile("GmailPlainText.msg");
        assertMessageIdPresent(message, handler);
        HandlerTestUtil.assertSubjectNotEmpty(message);

        final Collection<SimpleAttachment> attachments = createThenReturnAttachments(message, handler);
        assertPlainTextAttachmentNotPresent(attachments);
        assertHtmlAttachmentNotPresent(attachments);
        assertImageAttachmentNotPresent(attachments);
        assertBinaryAttachmentNotPresent(attachments);
    }

    public void testGmailHtml() throws Exception
    {
        final Message message = HandlerTestUtil.createMessageFromFile("GmailHtml.msg");
        assertMessageIdPresent(message, handler);
        HandlerTestUtil.assertSubjectNotEmpty(message);

        final Collection<SimpleAttachment> attachments = createThenReturnAttachments(message, handler);
        assertPlainTextAttachmentNotPresent(attachments);
        assertHtmlAttachmentNotPresent(attachments);
        assertImageAttachmentNotPresent(attachments);
        assertBinaryAttachmentNotPresent(attachments);
    }

    public void testGmailHtmlImageAttached() throws Exception
    {
        final Message message = HandlerTestUtil.createMessageFromFile("GmailHtmlImageAttached.msg");
        assertMessageIdPresent(message, handler);
        HandlerTestUtil.assertSubjectNotEmpty(message);

        final Collection<SimpleAttachment> attachments = createThenReturnAttachments(message, handler);
        assertPlainTextAttachmentNotPresent(attachments);
        assertHtmlAttachmentNotPresent(attachments);
        assertImageAttachmentPresent(attachments);
        assertBinaryAttachmentNotPresent(attachments);
    }

    public void testGmailHtmlInlineImage()
    {
        // GMAIL DOESNT support inline images properly
    }

    public void testGmailHtmlBinaryAttached() throws Exception
    {
        final Message message = HandlerTestUtil.createMessageFromFile("GmailHtmlBinaryattachment.msg");
        assertMessageIdPresent(message, handler);
        HandlerTestUtil.assertSubjectNotEmpty(message);

        final Collection<SimpleAttachment> attachments = createThenReturnAttachments(message, handler);
        assertPlainTextAttachmentNotPresent(attachments);
        assertHtmlAttachmentNotPresent(attachments);
        assertImageAttachmentNotPresent(attachments);
        assertBinaryAttachmentPresent(attachments);
    }

    public void testGMailHtmlWithPlainTextAttachment() throws Exception
    {
        final Message message = HandlerTestUtil.createMessageFromFile("GMailHtmlWithPlainTextAttachment.msg");
        assertMessageIdPresent(message, handler);
        HandlerTestUtil.assertSubjectNotEmpty(message);

        final Collection<SimpleAttachment> attachments = createThenReturnAttachments(message, handler);
        assertPlainTextAttachmentPresent(attachments);
        assertHtmlAttachmentNotPresent(attachments);
        assertImageAttachmentNotPresent(attachments);
        assertBinaryAttachmentNotPresent(attachments);
    }

    public void testGMailHtmlWithHtmlAttachment() throws Exception
    {
        final Message message = HandlerTestUtil.createMessageFromFile("GMailHtmlWithHtmlAttachment.msg");
        assertMessageIdPresent(message, handler);
        HandlerTestUtil.assertSubjectNotEmpty(message);

        final Collection<SimpleAttachment> attachments = createThenReturnAttachments(message, handler);
        assertPlainTextAttachmentNotPresent(attachments);
        assertHtmlAttachmentPresent(attachments);
        assertImageAttachmentNotPresent(attachments);
        assertBinaryAttachmentNotPresent(attachments);
    }

    public void testEvolutionPlainText() throws Exception
    {
        final Message message = HandlerTestUtil.createMessageFromFile("EvolutionPlainText.msg");
        assertMessageIdPresent(message, handler);
        HandlerTestUtil.assertSubjectNotEmpty(message);

        final Collection<SimpleAttachment> attachments = createThenReturnAttachments(message, handler);
        assertPlainTextAttachmentNotPresent(attachments);
        assertHtmlAttachmentNotPresent(attachments);
        assertImageAttachmentNotPresent(attachments);
        assertBinaryAttachmentNotPresent(attachments);
    }

    public void testEvolutionHtml() throws Exception
    {
        final Message message = HandlerTestUtil.createMessageFromFile("EvolutionHtml.msg");
        assertMessageIdPresent(message, handler);
        HandlerTestUtil.assertSubjectNotEmpty(message);

        final Collection<SimpleAttachment> attachments = createThenReturnAttachments(message, handler);
        assertPlainTextAttachmentNotPresent(attachments);
        assertHtmlAttachmentNotPresent(attachments);
        assertImageAttachmentNotPresent(attachments);
        assertBinaryAttachmentNotPresent(attachments);
    }

    public void testEvolutionHtmlInlineImage() throws Exception
    {
        final Message message = HandlerTestUtil.createMessageFromFile("EvolutionHtmlInlineImage.msg");
        assertMessageIdPresent(message, handler);
        HandlerTestUtil.assertSubjectNotEmpty(message);

        final Collection<SimpleAttachment> attachments = createThenReturnAttachments(message, handler);
        assertPlainTextAttachmentNotPresent(attachments);
        assertHtmlAttachmentNotPresent(attachments);
        assertImageAttachmentPresent(attachments);
        assertBinaryAttachmentNotPresent(attachments);
    }

    public void testEvolutionImageAttached() throws Exception
    {
        final Message message = HandlerTestUtil.createMessageFromFile("EvolutionHtmlImageAttached.msg");
        assertMessageIdPresent(message, handler);
        HandlerTestUtil.assertSubjectNotEmpty(message);

        final Collection<SimpleAttachment> attachments = createThenReturnAttachments(message, handler);
        assertPlainTextAttachmentNotPresent(attachments);
        assertHtmlAttachmentNotPresent(attachments);
        assertImageAttachmentPresent(attachments);
        assertBinaryAttachmentNotPresent(attachments);
    }

    public void testEvolutionHtmlBinaryAttached() throws Exception
    {
        final Message message = HandlerTestUtil.createMessageFromFile("EvolutionHtmlBinaryAttachment.msg");
        assertMessageIdPresent(message, handler);
        HandlerTestUtil.assertSubjectNotEmpty(message);

        final Collection<SimpleAttachment> attachments = createThenReturnAttachments(message, handler);
        assertPlainTextAttachmentNotPresent(attachments);
        assertHtmlAttachmentNotPresent(attachments);
        assertImageAttachmentNotPresent(attachments);
        assertBinaryAttachmentPresent(attachments);
    }

    public void testEvolutionHtmlWithPlainTextAttachment() throws Exception
    {
        final Message message = HandlerTestUtil.createMessageFromFile("EvolutionHtmlWithPlainTextAttachment.msg");
        assertMessageIdPresent(message, handler);
        HandlerTestUtil.assertSubjectNotEmpty(message);

        final Collection<SimpleAttachment> attachments = createThenReturnAttachments(message, handler);
        assertPlainTextAttachmentPresent(attachments);
        assertHtmlAttachmentNotPresent(attachments);
        assertImageAttachmentNotPresent(attachments);
        assertBinaryAttachmentNotPresent(attachments);
    }

    public void testEvolutionHtmlWithHtmlAttachment() throws Exception
    {
        final Message message = HandlerTestUtil.createMessageFromFile("EvolutionHtmlWithHtmlAttachment.msg");
        assertMessageIdPresent(message, handler);
        HandlerTestUtil.assertSubjectNotEmpty(message);

        final Collection<SimpleAttachment> attachments = createThenReturnAttachments(message, handler);
        assertPlainTextAttachmentNotPresent(attachments);
        assertHtmlAttachmentPresent(attachments);
        assertImageAttachmentNotPresent(attachments);
        assertBinaryAttachmentNotPresent(attachments);
    }

    public void testEvolutionPlainTextWithPlainTextAttachment() throws Exception
    {
        final Message message = HandlerTestUtil.createMessageFromFile("EvolutionPlainTextWithPlainTextAttachment.msg");
        assertMessageIdPresent(message, handler);
        HandlerTestUtil.assertSubjectNotEmpty(message);

        final Collection<SimpleAttachment> attachments = createThenReturnAttachments(message, handler);
        assertPlainTextAttachmentPresent(attachments);
        assertHtmlAttachmentNotPresent(attachments);
        assertImageAttachmentNotPresent(attachments);
        assertBinaryAttachmentNotPresent(attachments);
    }

    public void testEvolutionPlainTextWithHtmlAttachment() throws Exception
    {
        final Message message = HandlerTestUtil.createMessageFromFile("EvolutionPlainTextWithHtmlAttachment.msg");
        assertMessageIdPresent(message, handler);
        HandlerTestUtil.assertSubjectNotEmpty(message);

        final Collection<SimpleAttachment> attachments = createThenReturnAttachments(message, handler);
        assertPlainTextAttachmentNotPresent(attachments);
        assertHtmlAttachmentPresent(attachments);
        assertImageAttachmentNotPresent(attachments);
        assertBinaryAttachmentNotPresent(attachments);
    }

    public void testEvolutionPlainTextWithImageAttachment() throws Exception
    {
        final Message message = HandlerTestUtil.createMessageFromFile("EvolutionPlainTextWithImageAttachment.msg");
        assertMessageIdPresent(message, handler);
        HandlerTestUtil.assertSubjectNotEmpty(message);

        final Collection<SimpleAttachment> attachments = createThenReturnAttachments(message, handler);
        assertPlainTextAttachmentNotPresent(attachments);
        assertHtmlAttachmentNotPresent(attachments);
        assertImageAttachmentPresent(attachments);
        assertBinaryAttachmentNotPresent(attachments);
    }

    public void testEvolutionPlainTextWithBinaryAttachment() throws Exception
    {
        final Message message = HandlerTestUtil.createMessageFromFile("EvolutionPlainTextWithBinaryAttachment.msg");
        assertMessageIdPresent(message, handler);
        HandlerTestUtil.assertSubjectNotEmpty(message);

        final Collection<SimpleAttachment> attachments = createThenReturnAttachments(message, handler);
        assertPlainTextAttachmentNotPresent(attachments);
        assertHtmlAttachmentNotPresent(attachments);
        assertImageAttachmentNotPresent(attachments);
        assertBinaryAttachmentPresent(attachments);
    }

    public void testAppleText() throws Exception
    {
        final Message message = HandlerTestUtil.createMessageFromFile("AppleMailText.msg");
        assertMessageIdPresent(message, handler);
        HandlerTestUtil.assertSubjectNotEmpty(message);

        final Collection<SimpleAttachment> attachments = createThenReturnAttachments(message, handler);
        assertPlainTextAttachmentNotPresent(attachments);
        assertHtmlAttachmentNotPresent(attachments);
        assertImageAttachmentNotPresent(attachments);
        assertBinaryAttachmentNotPresent(attachments);
    }

    public void testAppleHtml() throws Exception
    {
        final Message message = HandlerTestUtil.createMessageFromFile("AppleMailHtml.msg");
        assertMessageIdPresent(message, handler);
        HandlerTestUtil.assertSubjectNotEmpty(message);

        final Collection<SimpleAttachment> attachments = createThenReturnAttachments(message, handler);
        assertPlainTextAttachmentNotPresent(attachments);
        assertHtmlAttachmentNotPresent(attachments);
        assertImageAttachmentNotPresent(attachments);
        assertBinaryAttachmentNotPresent(attachments);
    }

    public void testAppleHtmlInlineImage() throws Exception
    {
        final Message message = HandlerTestUtil.createMessageFromFile("AppleMailHtmlInlineImage.msg");
        assertMessageIdPresent(message, handler);
        HandlerTestUtil.assertSubjectNotEmpty(message);

        final Collection<SimpleAttachment> attachments = createThenReturnAttachments(message, handler);
        assertPlainTextAttachmentNotPresent(attachments);
        assertHtmlAttachmentNotPresent(attachments);
        assertImageAttachmentPresent(attachments);
        assertBinaryAttachmentNotPresent(attachments);
    }

    public void testAppleHtmlBinaryAttached() throws Exception
    {
        final Message message = HandlerTestUtil.createMessageFromFile("AppleMailHtmlBinaryAttachment.msg");
        assertMessageIdPresent(message, handler);
        HandlerTestUtil.assertSubjectNotEmpty(message);

        final Collection<SimpleAttachment> attachments = createThenReturnAttachments(message, handler);
        assertPlainTextAttachmentNotPresent(attachments);
        assertHtmlAttachmentNotPresent(attachments);
        assertImageAttachmentNotPresent(attachments);
        assertBinaryAttachmentPresent(attachments);
    }

    public void testAppleHtmlPlainTextAttached() throws Exception
    {
        final Message message = HandlerTestUtil.createMessageFromFile("AppleMailWithPlainTextAttachment.msg");
        assertMessageIdPresent(message, handler);
        HandlerTestUtil.assertSubjectNotEmpty(message);

        final Collection<SimpleAttachment> attachments = createThenReturnAttachments(message, handler);
        assertPlainTextAttachmentPresent(attachments);
        assertHtmlAttachmentNotPresent(attachments);
        assertImageAttachmentNotPresent(attachments);
        assertBinaryAttachmentNotPresent(attachments);
    }

    public void testAppleHtmlWithHtmlFileAttached() throws Exception
    {
        final Message message = HandlerTestUtil.createMessageFromFile("AppleMailHtmlWithHtmlFileAttachment.msg");
        assertMessageIdPresent(message, handler);
        HandlerTestUtil.assertSubjectNotEmpty(message);

        final Collection<SimpleAttachment> attachments = createThenReturnAttachments(message, handler);
        assertPlainTextAttachmentNotPresent(attachments);
        assertHtmlAttachmentPresent(attachments);
        assertImageAttachmentNotPresent(attachments);
        assertBinaryAttachmentNotPresent(attachments);
    }

    /**
     * A simple factory that returns a AbstractMessageHandler that allows attachments.
     * All the other boring stuff (dependendencies) are taken from a variety of managers setup by setup();
     *
     * @return a new instance of the SimpleMockAbstractMessageHandler with components injected
     */
    SimpleMockAbstractMessageHandler createAbstractMessageHandler()
    {
        final CommentManager commentManager = (CommentManager) new Mock(CommentManager.class).proxy();
        final IssueFactory issueFactory = (IssueFactory) new Mock(IssueFactory.class).proxy();
        final JiraApplicationContext jiraApplicationContext = (JiraApplicationContext) new Mock(JiraApplicationContext.class).proxy();

        final MockApplicationProperties applicationProperties = new MockApplicationProperties();
        applicationProperties.setOption(APKeys.JIRA_OPTION_ALLOWATTACHMENTS, true);
        applicationProperties.setOption(APKeys.JIRA_OPTION_IGNORE_EMAIL_MESSAGE_ATTACHMENTS, false);
        return new SimpleMockAbstractMessageHandler(commentManager, issueFactory, applicationProperties, jiraApplicationContext);
    }

    class SimpleMockAbstractMessageHandler extends MockAbstractMessageHandler
    {
        SimpleMockAbstractMessageHandler(final CommentManager commentManager, final IssueFactory issueFactory,
                final ApplicationProperties applicationProperties, final JiraApplicationContext jiraApplicationContext)
        {
            super(commentManager, issueFactory, applicationProperties, jiraApplicationContext);

            assertTrue
                    (
                            "JIRA_OPTION_ALLOWATTACHMENTS must be true",
                            applicationProperties.getOption(APKeys.JIRA_OPTION_ALLOWATTACHMENTS)
                    );

            assertFalse
                    (
                            "JIRA_OPTION_IGNORE_EMAIL_MESSAGE_ATTACHMENTS must be false",
                            applicationProperties.getOption(APKeys.JIRA_OPTION_IGNORE_EMAIL_MESSAGE_ATTACHMENTS)
                    );
        }

        AttachmentRecorderAttachmentManager attachmentManager = new AttachmentRecorderAttachmentManager();

        Collection<SimpleAttachment> getAttachments()
        {
            return this.attachmentManager.attachments;
        }

        AttachmentManager getAttachmentManager()
        {
            return this.attachmentManager;
        }

        /**
         * This method and {@link #attachHtmlParts(javax.mail.Part)}  is abstract in AbstractMessageHandler. Therefore
         * to check that it correctly keeps plain text and html file attachments the method has been implemented
         * to keep non empty attachments.
         *
         * @param part The plain text part.
         * @return true if not empty; otherwise, false.
         * @throws javax.mail.MessagingException
         * @throws java.io.IOException
         */
        protected boolean attachPlainTextParts(final Part part) throws MessagingException, IOException
        {
            return !MailUtils.isContentEmpty(part) && MailUtils.isPartAttachment(part);
        }

        protected boolean attachHtmlParts(final Part part) throws MessagingException, IOException
        {
            return !MailUtils.isContentEmpty(part) && MailUtils.isPartAttachment(part);
        }
    }

    void assertMessageIdPresent(final Message message, final AbstractMessageHandler handler)
    {
        try
        {
            handler.getMessageId(message);
        }
        catch (final Exception cause)
        {
            fail(
                    String.format
                            (
                                    "The message which had a subject of '%s' did not have a message id, cause: %s",
                                    getSubjectSafely(message), cause
                            )
            );
        }
    }

    Collection<SimpleAttachment> createThenReturnAttachments(final Message message, final SimpleMockAbstractMessageHandler handler)
    {
        try
        {
            final Collection<ChangeItemBean> parts = handler.createAttachmentsForMessage(message, issue);
            assertNotNull("The message has no parts", parts);

            return handler.getAttachments();

        }
        catch (final Exception e)
        {
            fail("Unable to get the attachments for the message, message: \"" + e.getMessage() + "\"...");
            return null;// never happens
        }
    }

    void assertAttachmentByMimeTypePresent(final Collection<SimpleAttachment> attachments, final String mimeType)
    {
        final Collection filtered = this.findAttachmentByMimeType(attachments, mimeType);

        if (filtered.size() == 0)
        {
            fail(
                    String.format(
                            "Unable to find a single '%s' part within the provided message attachments, "
                                    + "all attachments regardless of mimetype: %s",
                            mimeType, attachments
                    )
            );
        }

        // hack verify the filename matches the mimetype...
        final SimpleAttachment simpleAttachment = (SimpleAttachment) filtered.iterator().next();
        final String filename = simpleAttachment.getFilename();
        if (mimeType.equals(PLAINTEXT_MIME_TYPE))
        {
            // if this assert fails it could be the plain text non attachment was found rather than a genuine plain text attachment.
            assertEquals("" + attachments, PLAINTEXT_ATTACHMENT, filename);
        }
        if (mimeType.equals(HTML_MIME_TYPE))
        {
            // if this assert fails it could be the html text non attachment was found rather than a genuine html attachment.
            assertEquals("" + attachments, HTML_ATTACHMENT, filename);
        }
    }

    void assertAttachmentByMimeTypeNotPresent(final Collection<SimpleAttachment> attachments, final String mimeType)
    {
        final Collection filtered = this.findAttachmentByMimeType(attachments, mimeType);

        if (filtered.size() != 0)
        {
            fail(
                    String.format
                            (
                                    "Found %d attachments with a mimetype of '%s' within the provided message "
                                            + "attachments when 0 should have been found, all attachments: %s",
                                    filtered.size(), mimeType, attachments
                            )
            );
        }
    }

    Collection findAttachmentByMimeType(final Collection<SimpleAttachment> attachments, final Comparable mimeType)
    {
        List<SimpleAttachment> filtered = new ArrayList<SimpleAttachment>();

        try
        {
            for (SimpleAttachment attachmentObject : attachments)
            {
                final String contentType = MailUtils.getContentType(attachmentObject.getContentType());
                if (mimeType.equals(contentType))
                {
                    filtered.add(attachmentObject);
                }
            }
        }
        catch (final Exception e)
        {
            e.printStackTrace();
            fail(String.format("Unable to check if any part is a '%s' , message: %s", mimeType, e.getMessage()));
        }

        return filtered;
    }

    static final String IMAGE_MIME_TYPE = "image/jpeg";

    void assertImageAttachmentPresent(final Collection<SimpleAttachment> attachments)
    {
        this.assertAttachmentByMimeTypePresent(attachments, IMAGE_MIME_TYPE);
    }

    void assertImageAttachmentNotPresent(final Collection<SimpleAttachment> attachments) throws Exception
    {
        this.assertAttachmentByMimeTypeNotPresent(attachments, IMAGE_MIME_TYPE);
    }

    static final String BINARY_MIME_TYPE = "application/octet-stream";

    void assertBinaryAttachmentPresent(final Collection<SimpleAttachment> attachments) throws Exception
    {
        this.assertAttachmentByMimeTypePresent(attachments, BINARY_MIME_TYPE);
    }

    void assertBinaryAttachmentNotPresent(final Collection<SimpleAttachment> attachments)
    {
        this.assertAttachmentByMimeTypeNotPresent(attachments, BINARY_MIME_TYPE);
    }

    static final String PLAINTEXT_ATTACHMENT = "plain.txt";
    static final String PLAINTEXT_MIME_TYPE = "text/plain";

    void assertPlainTextAttachmentPresent(final Collection<SimpleAttachment> attachments)
    {
        this.assertAttachmentByMimeTypePresent(attachments, PLAINTEXT_MIME_TYPE);
    }

    void assertPlainTextAttachmentNotPresent(final Collection<SimpleAttachment> attachments)
    {
        this.assertAttachmentByMimeTypeNotPresent(attachments, PLAINTEXT_MIME_TYPE);
    }

    static final String HTML_ATTACHMENT = "page.html";
    static final String HTML_MIME_TYPE = "text/html";

    void assertHtmlAttachmentPresent(final Collection<SimpleAttachment> attachments)
    {
        this.assertAttachmentByMimeTypePresent(attachments, HTML_MIME_TYPE);
    }

    void assertHtmlAttachmentNotPresent(final Collection<SimpleAttachment> attachments)
    {
        this.assertAttachmentByMimeTypeNotPresent(attachments, HTML_MIME_TYPE);
    }

    String getSubjectSafely(final Message message)
    {
        try
        {
            return message.getSubject();

        }
        catch (final Exception swallow)
        {
            return "<UNABLE TO GET SUBJECT FROM MESSAGE>";
        }
    }


    /**
     * A simple attachment manager which will add to a list any parts being created.
     */
    static class AttachmentRecorderAttachmentManager implements AttachmentManager
    {
        List<SimpleAttachment> attachments = new ArrayList<SimpleAttachment>();

        public ChangeItemBean createAttachment(final File file, final String filename, final String contentType,
                final User remoteUser, final GenericValue issue) throws AttachmentException, GenericEntityException
        {
            final String cleanContentType = MailUtils.getContentType(contentType);
            this.attachments.add(new SimpleAttachment(filename, cleanContentType));
            // don't care about the ChangeItemBean in our tests
            return new ChangeItemBean();
        }

        @Override
        public Attachment createAttachment(GenericValue issue, com.opensymphony.user.User author, String mimetype,
                String filename, Long filesize, Map attachmentProperties, Date createdTime)
                throws GenericEntityException
        {
            // Old OSUser Object
            throw new UnsupportedOperationException("Not implemented yet.");
        }

        // return sensible values.
        public boolean attachmentsEnabled()
        {
            return true;
        }

        public boolean isScreenshotAppletEnabled()
        {
            return false;
        }

        public boolean isScreenshotAppletSupportedByOS()
        {
            return false;
        }

        @Override
        public List<ChangeItemBean> convertTemporaryAttachments(com.opensymphony.user.User user, Issue issue,
                List<Long> selectedAttachments, TemporaryAttachmentsMonitor temporaryAttachmentsMonitor)
                throws AttachmentException, GenericEntityException
        {
            // Old OSUser Object
            throw new UnsupportedOperationException("Not implemented yet.");
        }

        public List<ChangeItemBean> convertTemporaryAttachments(final User user, final Issue issue,
                final List<Long> selectedAttachments, final TemporaryAttachmentsMonitor temporaryAttachmentsMonitor)
                throws AttachmentException, GenericEntityException
        {
            return null;
        }

        // The remaining methods below throw UnsupportedOperationException as they should never be invoked.

        public Attachment getAttachment(final Long id) throws DataAccessException
        {
            throw new UnsupportedOperationException();
        }

        public List<Attachment> getAttachments(final GenericValue issue) throws DataAccessException
        {
            throw new UnsupportedOperationException();
        }

        public List<Attachment> getAttachments(final Issue issue) throws DataAccessException
        {
            throw new UnsupportedOperationException();
        }

        public List<Attachment> getAttachments(final Issue issue, final Comparator<? super Attachment> comparator)
                throws DataAccessException
        {
            throw new UnsupportedOperationException();
        }

        public Attachment createAttachmentCopySourceFile(final File file, final String filename, final String contentType,
                final String attachmentAuthor, final Issue issue, final Map attachmentProperties, final Date createdTime)
                throws AttachmentException
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public ChangeItemBean createAttachment(File file, String filename, String contentType,
                com.opensymphony.user.User remoteUser, GenericValue issue, Map attachmentProperties, Date createdTime)
                throws AttachmentException, GenericEntityException
        {
            // Old OSUser Object
            throw new UnsupportedOperationException("Not implemented yet.");
        }

        public ChangeItemBean createAttachment(final File file, final String filename, final String contentType,
                final User remoteUser, final GenericValue issue, final Map attachmentProperties, final Date createdTime)
                throws AttachmentException, GenericEntityException
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public ChangeItemBean createAttachment(File file, String filename, String contentType,
                com.opensymphony.user.User remoteUser, GenericValue issue)
                throws AttachmentException, GenericEntityException
        {
            return createAttachment(file, filename, contentType, (User) remoteUser, issue);
        }

        public Attachment createAttachment(final GenericValue issue, final User author, final String mimetype,
                final String filename, final Long filesize, final Map attachmentProperties, final Date createdTime)
                throws GenericEntityException
        {
            throw new UnsupportedOperationException();
        }

        public void deleteAttachment(final Attachment attachment) throws RemoveException
        {
            throw new UnsupportedOperationException();
        }

        public void deleteAttachmentDirectory(final GenericValue issue)
        {
            throw new UnsupportedOperationException();
        }

        public void deleteAttachmentDirectory(final Issue issue)
        {
            throw new UnsupportedOperationException();
        }
    }

    static class SimpleAttachment
    {
        SimpleAttachment(final String filename, final String contentType)
        {
            this.filename = filename;
            this.contentType = contentType;
        }

        String filename;

        String getFilename()
        {
            return this.filename;
        }

        String contentType;

        String getContentType()
        {
            return this.contentType;
        }

        public String toString()
        {
            return contentType + "(filename=" + filename + ")";
        }
    }
}
