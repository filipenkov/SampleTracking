package com.atlassian.jira.plugins.mail.handlers;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.plugins.mail.MockAttachmentManager;
import com.atlassian.jira.service.util.handler.DefaultMessageHandlerContext;
import junit.framework.Assert;
import org.junit.Test;
import org.ofbiz.core.entity.GenericEntityException;

import javax.mail.MessagingException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

public class TestFullCommentHandler extends AbstractTestCommentHandler
{

    @Override
    protected AbstractMessageHandler createHandler()
    {
        return new FullCommentHandler();
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

    @Test
    public void testMailWithCatchEmailMiss() throws Exception
    {
        _testCatchEmailSettings();
    }

    @Test
    public void testAddCommentOnly() throws MessagingException, GenericEntityException
    {
        _testAddCommentOnly();
    }

    @Test
    public void testAddCommentOnlyToMovedIssue() throws MessagingException, GenericEntityException
    {
        setupMovedIssue();
        _testAddCommentOnlyToMovedIssue();
    }

    @Test
    public void testAddCommentAndAttachment() throws MessagingException, GenericEntityException
    {
        _testAddCommentAndAttachment();
    }

    @Test
    public void testAddAttachmentWithInvalidFilename() throws MessagingException, GenericEntityException
    {
        _testAddMultipleAttachmentsWithInvalidAndValidFilenames();
    }

    @Test
    public void testAddCommentWithEmptyBodyAndAttachment() throws MessagingException, GenericEntityException
    {
        _testAddCommentWithNonMultipartAttachment();
    }

    @Test
    public void testAddCommentWithInlineAttachment() throws GenericEntityException, MessagingException
    {
        _testAddCommentWithNonMultipartInline();
    }

    @Test
    public void testCreateCommentFromFileThunderbirdHtmlImageAttachment() throws Exception
    {
        final String filename = "ThunderbirdHtmlImageAttachment.msg";
        final String expectedContent = "articipants:[1]Anton Mazkovoi [Atlassian], [2]Chris Mountford [Atlassian], [3]Eddie Kua [Atlassian], [4]John Tang, [5]Maxim Dyuzhinov, [6]Michael Tokar [Atlassian], [7]Neal Applebaum, [8]Nick Menere [Atlassian] and [9]Terry [Atlassian]\n"
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
        this.addCommentWithImageAttachment(filename, expectedContent);
    }

    @Test
    public void testCreateCommentFromFileThunderbirdHtmlAndPlainTextBinaryAttachment() throws Exception
    {
        final String filename = "ThunderbirdHtmlOnlyBinaryAttachment.msg";
        final String expectedContent = "com.atlassian.jira.service.services.mail.MailQueueService\n"
                + "\t1\t[1]Edit\n"
                + "\n"
                + "CreateIssuesFromDir\n"
                + "com.atlassian.jira.service.services.file.FileService";
        this.addCommentWithBinaryAttachment(filename, expectedContent);
    }

    @Test
    public void testCreateCommentFromFileThunderbirdHtmlAndPlainTextImageAttachment() throws Exception
    {
        final String filename = "ThunderbirdHtmlAndPlainTextImageAttachment.msg";
        final String expectedContent = "    *Votes:*  \t 1\n"
                + "*Watchers:* \t5\n"
                + "\n"
                + "*Operations*";
        this.addCommentWithImageAttachment(filename, expectedContent);
    }

    @Test
    public void testCreateCommentFromFileThunderbirdHtmlAndPlainTextInlineImage() throws Exception
    {
        final String filename = "ThunderbirdHtmlAndPlainTextInlineImage.msg";
        final String expectedContent = "BEFORE IMAGE\n"
                + "\n"
                + "AFTER IMAGE";
        this.addCommentWithImageAttachment(filename, expectedContent);
    }

    @Test
    public void testCreateCommentFromFileThunderbirdHtmlBinaryAttachment() throws Exception
    {
        final String filename = "ThunderbirdHtmlOnlyBinaryAttachment.msg";
        final String expectedContent = "com.atlassian.jira.service.services.mail.MailQueueService\n"
                + "\t1\t[1]Edit\n"
                + "\n"
                + "CreateIssuesFromDir\n"
                + "com.atlassian.jira.service.services.file.FileService\n"
                + "----------------------------------------------------------------------------------------";
        this.addCommentWithBinaryAttachment(filename, expectedContent);
    }

    @Test
    public void testCreateCommentFromFileThunderbirdHtmlInlineImage() throws Exception
    {
        final String filename = "ThunderbirdPlainTextImageAttachment.msg";
        final String expectedContent = "   \n"
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
        this.addCommentWithNoAttachment(filename, expectedContent);
    }

    @Test
    public void testCreateCommentFromFileThunderbirdPlainTextImageAttachment() throws Exception
    {
        final String filename = "ThunderbirdPlainTextImageAttachment.msg";
        final String expectedContent = "   \n"
                + "*orter:* \tMaxim Dyuzhinov \n"
                + "<http://jira.atlassian.com/secure/ViewProfile.jspa?name=maximd>\n"
                + "*Votes:* \t1\n"
                + "*Watchers:* \t5\n"
                + "\n"
                + "*Operations*";
        this.addCommentWithNoAttachment(filename, expectedContent);
    }

    @Test
    public void testCreateCommentFromFileThunderbirdPlainTextBinaryAttachment() throws Exception
    {
        final String filename = "ThunderbirdPlainTextOnlyBinaryAttachment.msg";
        final String expectedContent = "PLAIN TEXT";
        this.addCommentWithBinaryAttachment(filename, expectedContent);
    }

    @Test
    public void testCreateCommentFromFileThunderbirdHtmlWithPlainTextAttachment() throws Exception
    {
        final String filename = "ThunderbirdHtmlWithPlainTextAttachment.msg";
        final String expectedContent = "Html\n"
                + "\n"
                + "*Bold*\n"
                + "\n"
                + "/Italics/";
        this.addCommentWithPlainTextAttachment(filename, expectedContent);
    }

    @Test
    public void testCreateCommentFromFileThunderbirdHtmlWithHtmlAttachment() throws Exception
    {
        final String filename = "ThunderbirdHtmlWithHtmlAttachment.msg";
        final String expectedContent = "*BOLD*\n"
                + "/italics/\n"
                + "\n"
                + "A very simple html file...";
        this.addCommentWithHtmlAttachment(filename, expectedContent);
    }

    @Test
    public void testCreateCommentFromFileLotusNotesHtmlAttachedImage() throws Exception
    {
        final String filename = "LotusNotesHtmlImageAttached.msg";
        final String expectedContent = "_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _\n"
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
        this.addCommentWithImageAttachment(filename, expectedContent);
    }

    @Test
    public void testCreateCommentFromFileLotusNotesHtmlInlineImage() throws Exception
    {
        final String filename = "LotusNotesHtmlInlineImage.msg";
        final String expectedContent = "Html Email with an inline image\n"
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
        this.addCommentWithImageAttachment(filename, expectedContent);
    }

    @Test
    public void testCreateCommentFromFileLotusNotesHtmlBinaryAttachment() throws Exception
    {
        final String filename = "LotusNotesHtmlBinaryAttachment.msg";
        final String expectedContent = "here u are: \n"
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
        this.addCommentWithBinaryAttachment(filename, expectedContent);
    }

    @Test
    public void testCreateCommentFromFileOutlookPlainText() throws Exception
    {
        final String filename = "OutlookPlainText.msg";
        final String expectedContent = "Plain Text - no html markup.";
        this.addCommentWithNoAttachment(filename, expectedContent);
    }

    @Test
    public void testCreateCommentFromFileOutlookHtml() throws Exception
    {
        final String filename = "OutlookHtml.msg";
        final String expectedContent = "BOLD\n"
                + "\n"
                + "Italics\n"
                + "\n"
                + "Underlined";
        this.addCommentWithNoAttachment(filename, expectedContent);
    }

    @Test
    public void testCreateCommentFromFileOutlookHtmlImageAttached() throws Exception
    {
        final String filename = "OutlookHtmlImageAttached.msg";
        final String expectedContent = "BOLD\n"
                + "\n"
                + "Image attached\n"
                + "\n"
                + "Underlined";
        this.addCommentWithImageAttachment(filename, expectedContent);
    }

    @Test
    public void testCreateCommentFromFileOutlookHtmlInlineImage() throws Exception
    {
        final String filename = "OutlookHtmlInlineImage.msg";
        final String expectedContent = "Before Inline Image (Bold)";
        this.addCommentWithImageAttachment(filename, expectedContent);
    }

    @Test
    public void testCreateCommentFromFileOutlookHtmlBinaryAttachment() throws Exception
    {
        final String filename = "OutlookHtmlBinaryAttachment.msg";
        final String expectedContent = "BOLD\n"
                + "\n"
                + "Underlined";
        this.addCommentWithBinaryAttachment(filename, expectedContent);
    }

    @Test
    public void testCreateCommentFromFileOutlookHtmlWithPlainTextAttachment() throws Exception
    {
        final String filename = "OutlookHtmlWithPlainTextAttachment.msg";
        final String expectedContent = "Bold\n"
                + "\n"
                + "italics\n"
                + "\n"
                + "plain.txt file is attached.";
        this.addCommentWithPlainTextAttachment(filename, expectedContent);
    }

    @Test
    public void testCreateCommentFromFileOutlookHtmlWithHtmlAttachment() throws Exception
    {
        final String filename = "OutlookHtmlWithHtmlAttachment.msg";
        final String expectedContent = "Bold\n"
                + "\n"
                + "italics\n"
                + "\n"
                + "Html file called (page.html) is attached.";
        this.addCommentWithHtmlAttachment(filename, expectedContent);
    }

    @Test
    public void testCreateCommentFromFileGmailPlainText() throws Exception
    {
        final String filename = "GmailPlainText.msg";
        final String expectedContent = "Plain Text from Gmail.";
        this.addCommentWithNoAttachment(filename, expectedContent);
    }

    @Test
    public void testCreateCommentFromFileGmailHtml() throws Exception
    {
        final String filename = "GmailHtml.msg";
        final String expectedContent = "*BOLD\n"
                + "\n"
                + "**Italics**\n"
                + "\n"
                + "**Underlined**\n"
                + "*";
        this.addCommentWithNoAttachment(filename, expectedContent);
    }

    @Test
    public void testCreateCommentFromFileGmailHtmlImageAttached() throws Exception
    {
        final String filename = "GmailHtmlImageAttached.msg";
        final String expectedContent = "*BOLD\n"
                + "\n"
                + "**Italics**\n"
                + "\n"
                + "**Underlined**\n"
                + "*";
        this.addCommentWithImageAttachment(filename, expectedContent);
    }

    @Test
    public void testCreateCommentFromFileGmailHtmlInlineImage() throws Exception
    {
        // cant create gmail mail with inline image.
    }

    @Test
    public void testCreateCommentFromFileGmailHtmlBinaryAttached() throws Exception
    {
        final String filename = "GmailHtmlBinaryattachment.msg";
        final String expectedContent = "*BOLD\n"
                + "\n"
                + "**Italics**\n"
                + "\n"
                + "**Underlined**\n"
                + "*";
        this.addCommentWithBinaryAttachment(filename, expectedContent);
    }

    @Test
    public void testCreateCommentFromFileGMailHtmlWithPlainTextAttachment() throws Exception
    {
        final String filename = "GMailHtmlWithPlainTextAttachment.msg";
        final String expectedContent = "*BOLD*\n"
                + "\n"
                + "*Italics*\n"
                + "\n"
                + "-- \n"
                + "mP";
        this.addCommentWithPlainTextAttachment(filename, expectedContent);
    }

    @Test
    public void testCreateCommentFromFileGMailHtmlWithHtmlAttachment() throws Exception
    {
        final String filename = "GMailHtmlWithHtmlAttachment.msg";
        final String expectedContent = "BOLD\n"
                + "\n"
                + "The html page attachment should be kept...\n"
                + "\n"
                + "-- \n"
                + "mP";
        this.addCommentWithHtmlAttachment(filename, expectedContent);
    }

    @Test
    public void testCreateCommentFromFileEvolutionPlainText() throws Exception
    {
        final String filename = "EvolutionPlainText.msg";
        final String expectedContent = "This is a plain text email";
        this.addCommentWithNoAttachment(filename, expectedContent);
    }

    @Test
    public void testCreateCommentFromFileEvolutionHtml() throws Exception
    {
        final String filename = "EvolutionHtml.msg";
        final String expectedContent = "BOLD underlineditalics\n"
                + "";
        this.addCommentWithNoAttachment(filename, expectedContent);
    }

    @Test
    public void testCreateCommentFromFileEvolutionHtmlInlineImage() throws Exception
    {
        final String filename = "EvolutionHtmlInlineImage.msg";
        final String expectedContent = "Before Image (bold)  After image(italics)";
        this.addCommentWithImageAttachment(filename, expectedContent);
    }

    @Test
    public void testCreateCommentFromFileEvolutionImageAttached() throws Exception
    {
        final String filename = "EvolutionHtmlImageAttached.msg";
        final String expectedContent = "\n"
                + "Before Image (bold) $$$ After image(italics)";
        this.addCommentWithImageAttachment(filename, expectedContent);
    }

    @Test
    public void testCreateCommentFromFileEvolutionHtmlBinaryAttached() throws Exception
    {
        final String filename = "EvolutionHtmlBinaryAttachment.msg";
        final String expectedContent = "\n"
                + "Bindary with attachment and body";
        this.addCommentWithBinaryAttachment(filename, expectedContent);
    }

    @Test
    public void testCreateCommentFromFileEvolutionHtmlWithPlainTextAttachment() throws Exception
    {
        final String filename = "EvolutionHtmlWithPlainTextAttachment.msg";
        final String expectedContent = "This is an HTML email:\n"
                + "\n"
                + "BOLD\n"
                + "italics!\n"
                + "plain text\n"
                + "\n"
                + "Plain text attachment to follow.";
        this.addCommentWithPlainTextAttachment(filename, expectedContent);
    }

    @Test
    public void testCreateCommentFromFileEvolutionHtmlWithHtmlAttachment() throws Exception
    {
        final String filename = "EvolutionHtmlWithHtmlAttachment.msg";
        final String expectedContent = "Also has a html attachment";
        this.addCommentWithHtmlAttachment(filename, expectedContent);
    }

    @Test
    public void testCreateCommentFromFileEvolutionPlainTextWithPlainTextAttachment() throws Exception
    {
        final String filename = "EvolutionPlainTextWithPlainTextAttachment.msg";
        final String expectedContent = "Plain text in message\n"
                + "\n"
                + "Plain text attachment should follow";
        this.addCommentWithPlainTextAttachment(filename, expectedContent);
    }

    @Test
    public void testCreateCommentFromFileEvolutionPlainTextWithHtmlAttachment() throws Exception
    {
        final String filename = "EvolutionPlainTextWithHtmlAttachment.msg";
        final String expectedContent = "Plain text.\n"
                + "There should be an HTML attachment here.";
        this.addCommentWithHtmlAttachment(filename, expectedContent);
    }

    @Test
    public void testCreateCommentFromFileEvolutionPlainTextWithImageAttachment() throws Exception
    {
        final String filename = "EvolutionPlainTextWithImageAttachment.msg";
        final String expectedContent = "Plain text.\n"
                + "Image attachment should follow.";
        this.addCommentWithImageAttachment(filename, expectedContent);
    }

    @Test
    public void testCreateCommentFromFileEvolutionPlainTextWithBinaryAttachment() throws Exception
    {
        final String filename = "EvolutionPlainTextWithBinaryAttachment.msg";
        final String expectedContent = "Plain text here.\n"
                + "Binary attachment to follow.";
        this.addCommentWithBinaryAttachment(filename, expectedContent);
    }

    @Test
    public void testCreateCommentFromFileAppleMailPlainText() throws Exception
    {
        final String filename = "AppleMailText.msg";
        final String expectedContent = "Plain\n"
                + "\n"
                + "Was Bold\n"
                + "\n"
                + "Was Underlined";
        this.addCommentWithNoAttachment(filename, expectedContent);
    }

    @Test
    public void testCreateCommentFromFileAppleMailHtml() throws Exception
    {
        final String filename = "AppleMailHtml.msg";
        final String expectedContent = "Html email\n"
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
        this.addCommentWithNoAttachment(filename, expectedContent);
    }

    @Test
    public void testCreateCommentFromFileAppleMailHtmlInlineImage() throws Exception
    {
        final String filename = "AppleMailHtmlInlineImage.msg";
        final String expectedContent = "Before Image(bold)\n"
                + "\n"
                + "\n"
                + "Italics(after)\n"
                + "\n"
                + "NB that the content type & filename within the inline image have been changed to image.jpg...";
        this.addCommentWithImageAttachment(filename, expectedContent);
    }

    @Test
    public void testCreateCommentFromFileAppleMailHtmlBinaryAttached() throws Exception
    {
        final String filename = "AppleMailHtmlBinaryAttachment.msg";
        final String expectedContent = "Bold\n"
                + "\n"
                + "Italics\n"
                + "\n"
                + "Underlined\n"
                + "\n"
                + "Binary attachment\n"
                + "\n"
                + "nb: The content-type from application/macbinary to application/octet-stream for testing purposes...";
        this.addCommentWithBinaryAttachment(filename, expectedContent);
    }

    @Test
    public void testCreateCommentFromFileEvolutionAppleMailWithPlainTextAttachment() throws Exception
    {
        final String filename = "AppleMailWithPlainTextAttachment.msg";
        final String expectedContent = "also has Plain Text attachment";
        this.addCommentWithPlainTextAttachment(filename, expectedContent);
    }

    @Test
    public void testCreateCommentFromFileAppleMailHtmlWithHtmlFileAttachment() throws Exception
    {
        final String filename = "AppleMailHtmlWithHtmlFileAttachment.msg";
        final String expectedContent = "Also has html attachment";
        this.addCommentWithHtmlAttachment(filename, expectedContent);
    }

    /**
     * This test helper creates a new comment and proceeds to add the attachment. After that it verifies the comment and
     * attachment by fetching the new comment back and asserting stuff.
     */
    private final static String UPDATED = "updated";
    // all prepared test image and binary files have one of the two mime types...
    private final static String IMAGE_MIME_TYPE = "image/jpeg";
    private final static String IMAGE_FILENAME = "image.jpeg";
    private final static String BINARY_MIME_TYPE = "application/octet-stream";
    private final static String BINARY_FILENAME = "binary.bin";
    private final static String PLAIN_TEXT_MIME_TYPE = "text/plain";
    private final static String PLAIN_TEXT_FILENAME = "plain.txt";
    private final static String HTML_MIME_TYPE = "text/html";
    private final static String HTML_FILENAME = "page.html";

    public void addCommentWithNoAttachment(final String messageSourceFilename, final String expectedContent)
            throws Exception
    {
        this.addCommentWithAttachment(messageSourceFilename, null, expectedContent, null);
    }

    public void addCommentWithPlainTextAttachment(final String messageSourceFilename, final String expectedContent)
            throws Exception
    {
        this.addCommentWithAttachment(messageSourceFilename, PLAIN_TEXT_MIME_TYPE, expectedContent, PLAIN_TEXT_FILENAME);
    }

    public void addCommentWithHtmlAttachment(final String messageSourceFilename, final String expectedContent)
            throws Exception
    {
        this.addCommentWithAttachment(messageSourceFilename, HTML_MIME_TYPE, expectedContent, HTML_FILENAME);
    }

    public void addCommentWithImageAttachment(final String messageSourceFilename, final String expectedContent)
            throws Exception
    {
        this.addCommentWithAttachment(messageSourceFilename, IMAGE_MIME_TYPE, expectedContent, IMAGE_FILENAME);
    }

    public void addCommentWithBinaryAttachment(final String attachmentFilename, final String expectedContent)
            throws Exception
    {
        this.addCommentWithAttachment(attachmentFilename, BINARY_MIME_TYPE, expectedContent, BINARY_FILENAME);
    }

    public void addCommentWithAttachment(final String messageSourceFilename, final String mimeType, final String expectedContent, final String attachmentFilename)
            throws Exception
    {
        applicationProperties.setString(APKeys.JIRA_PATH_ATTACHMENTS, AbstractTestCommentHandler.ATTACHMENT_DIRECTORY);
        applicationProperties.setOption(APKeys.JIRA_OPTION_ALLOWATTACHMENTS, true);

        //This is wrong should be create message from file
        this.message = HandlerTestUtil.createMessageFromFile(messageSourceFilename, AbstractTestMessageHandler.ISSUE_KEY);

        //reset the error handler
        this.monitor = new SimpleTestMessageHandlerExecutionMonitor();

        //grant comment permission and check its ok
        this.setupCommentPermission();

        // tell the handler to process the message...
        Assert.assertEquals(true, this.handler.handleMessage(this.message, new DefaultMessageHandlerContext(commentManager, monitor, issueManager, attachmentManager)));
        Assert.assertNull(this.monitor.getError());

        //check that the issue's updated date is changed
        final MutableIssue latestIssue = ComponentAccessor.getIssueManager().getIssueObject(AbstractTestMessageHandler.ISSUE_KEY);
        assertIssueHasBeenUpdated(latestIssue, issue);

        //Check to see if comment was created.
        final List actions = commentManager.getCommentsForUser(issueObject, u1);
        Assert.assertFalse(actions.isEmpty());
        Assert.assertEquals(1, actions.size());

        //examine the comments body and level
        final Comment comment = (Comment) actions.get(0);
        assertComment(comment.getBody(), expectedContent);

        //check that no change history was created
        final List<MockAttachmentManager.MockAttachmentInfo> changeHistoryActions = attachmentManager.attachmentLog;
        if (null != attachmentFilename)
        {
            Assert.assertFalse(changeHistoryActions.isEmpty());
            Assert.assertEquals(1, changeHistoryActions.size());

            //examine the change history, and confirm its correct
            final MockAttachmentManager.MockAttachmentInfo changeHistory = changeHistoryActions.iterator().next();
            Assert.assertEquals(AbstractTestMessageHandler.TESTUSER_USERNAME, changeHistory.remoteUser.getName());
            Assert.assertEquals(AbstractTestMessageHandler.ISSUE_KEY, changeHistory.issue.getString("key"));
        }
        //check that the attachments actually added
        final List<Attachment> attachments = attachmentManager.getAttachments(issueObject);

        for (Attachment attachment : attachments)
        {
            final String attachmentMimeType = attachment.getMimetype();
            if ("text/plain".equals(attachmentMimeType))
            {
                continue;
            }

            Assert.assertEquals(mimeType, attachmentMimeType);
            Assert.assertEquals(attachmentFilename, attachment.getFilename());
        }
    }

    /**
     * To avoid nasties when comparing strings which may contain eol each string is normalized like xml does (crnl are
     * converted into nl) before comparison
     *
     * @param expected The expected comment string.
     * @param actual The actual comment string about to be tested.
     */
    void assertComment(final String expected, final String actual) throws IOException
    {
        Assert.assertFalse("Expected description cannot be null or empty", null == expected || expected.length() == 0);

        final String expected0 = normalizeLineTerminators(expected).trim();
        final String actual0 = normalizeLineTerminators(actual).trim();

        if (!expected0.startsWith(actual0))
        {
            Assert.assertEquals(expected0, actual0);
        }
    }

    private String normalizeLineTerminators(String inputStr) throws IOException
    {
        BufferedReader br = new BufferedReader(new StringReader(inputStr));
        String line;
        StringBuilder output = new StringBuilder();
        try
        {
            while ((line = br.readLine()) != null)
            {
                output.append(line.trim()).append("\n");
            }
        }
        finally
        {
            br.close();
        }
        return output.toString();
    }
}
