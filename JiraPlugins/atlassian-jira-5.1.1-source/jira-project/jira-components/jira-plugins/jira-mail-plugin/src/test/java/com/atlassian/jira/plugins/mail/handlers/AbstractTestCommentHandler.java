package com.atlassian.jira.plugins.mail.handlers;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.plugins.mail.MockAttachmentManager;
import com.atlassian.jira.plugins.mail.MockCommentManager;
import com.atlassian.jira.service.util.handler.DefaultMessageHandlerContext;
import com.atlassian.jira.util.collect.MapBuilder;
import com.google.common.collect.ImmutableList;
import org.apache.commons.collections.map.ListOrderedMap;
import org.junit.Before;
import org.junit.Ignore;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@Ignore("Not done yet")
public abstract class AbstractTestCommentHandler extends AbstractTestMessageHandler
{
    private static final String FILENAME_WITH_INVALID_CHARS_1 = "has<invalid?chars";
    private static final String FILENAME_WITH_INVALID_CHARS_2 = "hasi?nvali:dchars";
    private static final String FILENAME_WITH_INVALID_CHARS_11 = "hasin?val*idchars";
    private static final String FILENAME_VALID = "testFile.txt";
    private static final String FILENAME_CALLED_RENAMED_10 = "renamedFile-10";
    protected static final String TEXT_MIME_TYPE = "text/plain";
    private static final String RENAMED_FILENAME_1 = "has_invalid_chars";
    private static final String RENAMED_FILENAME_2 = "hasi_nvali_dchars";
    private static final String RENAMED_FILENAME_11 = "hasin_val_idchars";

    protected CommentManager commentManager;


    protected static final String ATTACHMENT_DIRECTORY = System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + "jira-testrun-attachments" + System.currentTimeMillis();

    @Override
    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        commentManager = new MockCommentManager(issueManager);
        worker.addMock(CommentManager.class, commentManager);

        setupHandlerState();
    }

    private void setupHandlerState() throws MessagingException {
        setupIssueAndMessage();
        handler = createHandler();
        // we now use the handler instance in the rest of setup
        handler.init(MapBuilder.build("project", "PRJ", "issuetype", "1", "reporterusername", TESTUSER_USERNAME), monitor);
    }

    /**
     * Implementations of this abstract class are expected to provide an instance of the MessageHandler under test.
     *
     * This is called during the setup method.
     * 
     * @return an instance of the MessageHandler under test.
     */
    protected abstract AbstractMessageHandler createHandler();

    /**
     * Test that correct permission is needed to comment on the issue for the handler.<br>
     * Also check that comments are created upon success.<br>
     * Also check that no change history is made (since its only comments - no attachments)
     * @throws GenericEntityException
     * @throws MessagingException
     */
    public void _testAddCommentOnly() throws GenericEntityException, MessagingException
    {
        //test that user with no permission cannot add comment
        assertEquals(false, handler.handleMessage(message, new DefaultMessageHandlerContext(commentManager, monitor, issueManager, attachmentManager)));
        assertEquals(HAS_NO_PERMISSION_TO_COMMENT_ON.format(new Object[] { u1.getDisplayName(), pid }), monitor.getError());
        //check that the issue's updated date has not changed
        MutableIssue latestIssue = ComponentAccessor.getIssueManager().getIssueObject(ISSUE_KEY);
        assertEquals("The issue update date should be the same", latestIssue.getUpdated(), issue.getUpdated());

        //check that the comment was not created
        List<Comment> comments = commentManager.getCommentsForUser(issueObject, u1);
        assertTrue(comments.isEmpty());
        //check that no change history was created
        assertTrue(attachmentManager.attachmentLog.isEmpty());

        //reset the error handler
        monitor = new SimpleTestMessageHandlerExecutionMonitor();

        //grant comment permission and check its ok
        setupCommentPermission();
        assertEquals(true, handler.handleMessage(message, new DefaultMessageHandlerContext(commentManager, monitor, issueManager, attachmentManager)));
        assertNull(monitor.getError());
        //check that the issue's updated date is changed
        latestIssue = ComponentAccessor.getIssueManager().getIssueObject(ISSUE_KEY);
        assertIssueHasBeenUpdated(latestIssue, issue);

        //Check to see if comment was created.
        comments = commentManager.getCommentsForUser(issueObject, u1);
        assertFalse(comments.isEmpty());
        assertEquals(1, comments.size());
        //examine the comments body and level
        final Comment comment = comments.iterator().next();
        assertEquals(MESSAGE_STRING, comment.getBody());
        assertEquals(null, comment.getGroupLevel());

        //check that no change history was created
        assertTrue(attachmentManager.attachmentLog.isEmpty());

        //check that no attachments were added
        final Collection attachments = ComponentAccessor.getAttachmentManager().getAttachments(issueObject);
        assertTrue(attachments.isEmpty());
    }

    /**
     * Test that correct permission is needed to comment on the issue for the handler.<br>
     * Also check that comments are created upon success.<br>
     * Also check that no change history is made (since its only comments - no attachments)
     * @throws GenericEntityException
     * @throws MessagingException
     */
    public void _testAddCommentOnlyToMovedIssue() throws GenericEntityException, MessagingException
    {
        //test that user with no permission cannot add comment
        assertEquals(false, handler.handleMessage(message, new DefaultMessageHandlerContext(commentManager, monitor, issueManager, attachmentManager)));
        assertEquals(HAS_NO_PERMISSION_TO_COMMENT_ON.format(new Object[] { u1.getDisplayName(), pid2 }), monitor.getError());
        //check that the issue's updated date has not changed
        Issue latestIssue = ComponentAccessor.getIssueManager().getIssueObject(ISSUE_KEY_MOVED);
        assertEquals(latestIssue.getUpdated(), movedIssue.getUpdated());

        //check that the comment was not created
        List<Comment> comments = commentManager.getCommentsForUser(movedIssueObject, u1);
        assertTrue(comments.isEmpty());
        //check that no change history exists - move is mocked
        List<MockAttachmentManager.MockAttachmentInfo> changeHistoryActions = attachmentManager.attachmentLog;
        assertTrue(changeHistoryActions.isEmpty());

        //reset the error handler
        monitor = new SimpleTestMessageHandlerExecutionMonitor();

        //grant comment permission and check its ok
        setupCommentPermission();
        assertEquals(true, handler.handleMessage(message, new DefaultMessageHandlerContext(commentManager, monitor, issueManager, attachmentManager)));
        assertNull(monitor.getError());

        //Check to see if comment was created.
        comments = commentManager.getCommentsForUser(movedIssueObject, u1);
        assertFalse(comments.isEmpty());
        assertEquals(1, comments.size());
        //examine the comments body and level
        final Comment comment = comments.iterator().next();
        assertEquals(MESSAGE_STRING, comment.getBody());
        assertEquals(null, comment.getGroupLevel());

        //check that no change history was created
        changeHistoryActions = attachmentManager.attachmentLog;
        assertTrue(changeHistoryActions.isEmpty());

        //check that no attachments were added
        final Collection attachments = attachmentManager.getAttachments(movedIssueObject);
        assertTrue(attachments.isEmpty());

        //check that the issue's updated date is changed
        latestIssue = ComponentAccessor.getIssueManager().getIssueObject(ISSUE_KEY_MOVED);
        assertNotNull(latestIssue.getUpdated());
        assertFalse(latestIssue.getUpdated().equals(movedIssue.getUpdated()));
    }

    /**
     * Test that correct permission is needed to comment on the issue for the handler.(with attachments)<br>
     * Also check that comments are created upon success.<br>
     * Also check that change history is made (for successful comment and attaching)<br>
     * Also check that the Attachment is created<br>
     * Also check that the updated date of the issue is updated on success only
     * @throws GenericEntityException
     * @throws MessagingException
     */
    public void _testAddCommentAndAttachment() throws GenericEntityException, MessagingException
    {
        applicationProperties.setString(APKeys.JIRA_PATH_ATTACHMENTS, ATTACHMENT_DIRECTORY);
        applicationProperties.setOption(APKeys.JIRA_OPTION_ALLOWATTACHMENTS, true);

        createMessageWithAttachment(MESSAGE_STRING, MapBuilder.<String, String> newBuilder().add(FILENAME_VALID, TEXT_MIME_TYPE).toMap());

        //test that user with no permission cannot add comment
        assertEquals(false, handler.handleMessage(message, new DefaultMessageHandlerContext(commentManager, monitor, issueManager, attachmentManager)));
        assertEquals(HAS_NO_PERMISSION_TO_COMMENT_ON.format(new Object[] { u1.getDisplayName(), pid }), monitor.getError());

        //check that the issue's updated date has not changed
        Issue latestIssue = ComponentAccessor.getIssueManager().getIssueObject(ISSUE_KEY);
        assertEquals(latestIssue.getUpdated(), issue.getUpdated());

        //check that the comment was not created
        List<Comment> comments = commentManager.getCommentsForUser(issueObject, u1);
        assertTrue(comments.isEmpty());
        //check that no change history was created
        assertTrue(attachmentManager.attachmentLog.isEmpty());
        //check that no Attachments was created
        List<Attachment> attachments = ComponentAccessor.getAttachmentManager().getAttachments(issueObject);
        assertTrue(attachments.isEmpty());

        //reset the error handler
        monitor = new SimpleTestMessageHandlerExecutionMonitor();

        //grant comment permission and check its ok
        setupCommentPermission();
        assertEquals(true, handler.handleMessage(message, new DefaultMessageHandlerContext(commentManager, monitor, issueManager, attachmentManager)));
        assertNull(monitor.getError());

        //check that the issue's updated date is changed
        latestIssue = ComponentAccessor.getIssueManager().getIssueObject(ISSUE_KEY);
        assertNotNull(latestIssue.getUpdated());
        assertFalse(latestIssue.getUpdated().equals(issue.getUpdated()));

        //Check to see if comment was created.
        comments = commentManager.getCommentsForUser(issueObject, u1);
        assertFalse(comments.isEmpty());
        assertEquals(1, comments.size());

        //examine the comments body and level
        assertEquals(MESSAGE_STRING, comments.get(0).getBody());

        //check that no change history was created
        List<MockAttachmentManager.MockAttachmentInfo> changeHistoryActions = attachmentManager.attachmentLog;
        assertFalse(changeHistoryActions.isEmpty());
        assertEquals(1, changeHistoryActions.size());

        //examine the change history, and confirm its correct
        MockAttachmentManager.MockAttachmentInfo expectedAttachment = changeHistoryActions.get(0);
        assertEquals(TESTUSER_USERNAME, expectedAttachment.remoteUser.getName());
        assertEquals(ISSUE_KEY, expectedAttachment.issue.getString("key"));
        assertEquals(FILENAME_VALID, expectedAttachment.filename);

        //check that the attachments actually added
        attachments = attachmentManager.getAttachments(issueObject);
        assertEquals(1, attachments.size());
        assertAttachment(attachments.get(0), FILENAME_VALID, TEXT_MIME_TYPE, TESTUSER_USERNAME);
    }

    public void _testAddCommentWithNonMultipartInline() throws MessagingException, GenericEntityException
    {
        applicationProperties.setString(APKeys.JIRA_PATH_ATTACHMENTS, ATTACHMENT_DIRECTORY);
        applicationProperties.setOption(APKeys.JIRA_OPTION_ALLOWATTACHMENTS, true);

        createNonMultipartMessageWithAttachment(TEXT_MIME_TYPE, FILENAME_VALID);
        message.setDisposition(Part.INLINE);

        //grant comment permission and check its ok
        setupCommentPermission();
        assertEquals(true, handler.handleMessage(message, new DefaultMessageHandlerContext(commentManager, monitor, issueManager, attachmentManager)));

        //check that the attachments actually added
        final Collection attachments = attachmentManager.getAttachments(issueObject);
        assertTrue(attachments.toString(), attachments.isEmpty());
    }

    public void _testAddCommentWithNonMultipartAttachment() throws MessagingException, GenericEntityException
    {
        applicationProperties.setString(APKeys.JIRA_PATH_ATTACHMENTS, ATTACHMENT_DIRECTORY);
        applicationProperties.setOption(APKeys.JIRA_OPTION_ALLOWATTACHMENTS, true);

        createNonMultipartMessageWithAttachment(TEXT_MIME_TYPE, FILENAME_VALID);

        //test that user with no permission cannot add comment
        assertEquals(false, handler.handleMessage(message, new DefaultMessageHandlerContext(commentManager, monitor, issueManager, attachmentManager)));
        assertEquals(HAS_NO_PERMISSION_TO_COMMENT_ON.format(new Object[] { u1.getDisplayName(), pid }), monitor.getError());

        //check that the issue's updated date has not changed
        Issue latestIssue = ComponentAccessor.getIssueManager().getIssueObject(ISSUE_KEY);
        assertEquals(latestIssue.getUpdated(), issue.getUpdated());

        //check that the comment was not created
        List<Comment> comments = commentManager.getCommentsForUser(issueObject, u1);
        assertTrue(comments.isEmpty());
        //check that no change history was created
        assertTrue(attachmentManager.attachmentLog.isEmpty());
        //check that no Attachments was created
        List<Attachment> attachments = attachmentManager.getAttachments(issueObject);
        assertTrue(attachments.isEmpty());

        //reset the error handler
        monitor = new SimpleTestMessageHandlerExecutionMonitor();

        //grant comment permission and check its ok
        setupCommentPermission();
        assertEquals(true, handler.handleMessage(message, new DefaultMessageHandlerContext(commentManager, monitor, issueManager, attachmentManager)));
        assertNull(monitor.getError());

        //check that the issue's updated date is changed
        latestIssue = ComponentAccessor.getIssueManager().getIssueObject(ISSUE_KEY);
        assertIssueHasBeenUpdated(latestIssue, issue);

        //Check to see if comment was created.
        comments = commentManager.getCommentsForUser(issueObject, u1);
        assertFalse(comments.isEmpty());
        assertEquals(1, comments.size());

        //examine the comments body and level
        assertEquals(MESSAGE_STRING, comments.get(0).getBody());

        //check that no change history was created
        List<MockAttachmentManager.MockAttachmentInfo> changeHistoryActions = attachmentManager.attachmentLog;
        assertFalse(changeHistoryActions.isEmpty());
        assertEquals(1, changeHistoryActions.size());

        //examine the change history, and confirm its correct
        final MockAttachmentManager.MockAttachmentInfo changeHistory = changeHistoryActions.iterator().next();
        assertEquals(TESTUSER_USERNAME, changeHistory.remoteUser.getName());
        assertEquals(ISSUE_KEY, changeHistory.issue.getString("key"));
        assertEquals(FILENAME_VALID, changeHistory.filename);

        //check that the attachments actually added
        attachments = attachmentManager.getAttachments(issueObject);
        assertEquals(1, attachments.size());
        assertAttachment(attachments.get(0), FILENAME_VALID, TEXT_MIME_TYPE, TESTUSER_USERNAME);
    }

    protected void assertIssueHasBeenUpdated(Issue latestIssue, Issue previousIssue) {
        Timestamp latestUpdateDate = latestIssue.getUpdated();
        Timestamp previousUpdatedDate = previousIssue.getUpdated();
        assertNotNull(latestUpdateDate);
        assertFalse("The issue updated date should be different - prev : " + previousUpdatedDate + " new : " + latestUpdateDate, latestUpdateDate.equals(previousUpdatedDate));
    }


    /**
     * Similar test as {@link #_testAddCommentAndAttachment()} but with <strong>multiple</strong> attachments that has
     * both invalid and valid filenames, which causes the handlers to rename the files with invalid ones and to leave
     * the valid ones as it is.
     *
     * @throws GenericEntityException
     * @throws MessagingException
     */
    public void _testAddMultipleAttachmentsWithInvalidAndValidFilenames() throws GenericEntityException, MessagingException
    {
        applicationProperties.setString(APKeys.JIRA_PATH_ATTACHMENTS, ATTACHMENT_DIRECTORY);
        applicationProperties.setOption(APKeys.JIRA_OPTION_ALLOWATTACHMENTS, true);

        //Add four attachments in order
        // FILENAME_WITH_INVALID_CHARS_1  - invalid - causes rename to be RENAMED_FILENAME_1
        // FILENAME_WITH_INVALID_CHARS_2  - invalid - causes rename to be RENAMED_FILENAME_2
        // FILENAME_CALLED_RENAMED_10     - valid   - does not get renamed
        // FILENAME_WITH_INVALID_CHARS_11 - invalid - causes rename to be RENAMED_FILENAME_11
        final Map<String, String> attachedFiles = new ListOrderedMap();
        attachedFiles.put(FILENAME_WITH_INVALID_CHARS_1, TEXT_MIME_TYPE);
        attachedFiles.put(FILENAME_WITH_INVALID_CHARS_2, TEXT_MIME_TYPE);
        attachedFiles.put(FILENAME_CALLED_RENAMED_10, TEXT_MIME_TYPE);
        attachedFiles.put(FILENAME_WITH_INVALID_CHARS_11, TEXT_MIME_TYPE);

        createMessageWithAttachment(MESSAGE_STRING, attachedFiles);

        //grant comment permission and check its ok
        setupCommentPermission();
        assertEquals(true, handler.handleMessage(message, new DefaultMessageHandlerContext(commentManager, monitor, issueManager, attachmentManager)));
        assertNull(monitor.getError());

        //check that the issue's updated date is changed
        final MutableIssue latestIssue = ComponentAccessor.getIssueManager().getIssueObject(ISSUE_KEY);
        assertNotNull(latestIssue.getUpdated());
        assertFalse(latestIssue.getUpdated().equals(issue.getUpdated()));

        //Check to see if comment was created and another for the renamed file
        final List<Comment> comments = commentManager.getCommentsForUser(issueObject, u1);
        assertFalse(comments.isEmpty());
        assertEquals(4, comments.size()); //1 email body and 3 renamed files

        //examine the comments body and level
        assertEquals(MESSAGE_STRING, comments.get(0).getBody());
        assertEquals(getRenamedCommentBody(FILENAME_WITH_INVALID_CHARS_1, RENAMED_FILENAME_1), comments.get(1).getBody());
        assertEquals(getRenamedCommentBody(FILENAME_WITH_INVALID_CHARS_2, RENAMED_FILENAME_2), comments.get(2).getBody());
        assertEquals(getRenamedCommentBody(FILENAME_WITH_INVALID_CHARS_11, RENAMED_FILENAME_11), comments.get(3).getBody());

        //check that change history was created
        final List<MockAttachmentManager.MockAttachmentInfo> changeItems = attachmentManager.attachmentLog;
        assertFalse(changeItems.isEmpty());
        assertEquals(4, changeItems.size());

        //examine the change history, and confirm its correct
        assertEquals(4, changeItems.size());
        Iterator<String> expectedFileNames = ImmutableList.of(RENAMED_FILENAME_1, RENAMED_FILENAME_2, FILENAME_CALLED_RENAMED_10, RENAMED_FILENAME_11).iterator();
        for (MockAttachmentManager.MockAttachmentInfo changeItem : changeItems)
        {
            assertEquals(TESTUSER_USERNAME, changeItem.remoteUser.getName());
            assertEquals(ISSUE_KEY, changeItem.issue.getString("key"));
            assertEquals(expectedFileNames.next(), changeItem.filename);
        }

        //check that the attachments actually added
        // this would go to integration test
        final List<Attachment> attachments = attachmentManager.getAttachments(issueObject);
        assertEquals(4, attachments.size());
        //sorted alphabetically
        assertAttachment(attachments.get(0), RENAMED_FILENAME_1, TEXT_MIME_TYPE, TESTUSER_USERNAME);
        assertAttachment(attachments.get(1), RENAMED_FILENAME_2, TEXT_MIME_TYPE, TESTUSER_USERNAME);
        assertAttachment(attachments.get(2), RENAMED_FILENAME_11, TEXT_MIME_TYPE, TESTUSER_USERNAME);
        assertAttachment(attachments.get(3), FILENAME_CALLED_RENAMED_10, TEXT_MIME_TYPE, TESTUSER_USERNAME);
    }

    private String getRenamedCommentBody(final String originalFileName, final String renamedFileName)
    {
        return "Renamed attached file: '" + originalFileName + "' to '" + renamedFileName + "' because it contained invalid character(s).";
    }

    protected void assertAttachment(final Attachment attachment, final String expectedFilename, final String expectedMimeType, final String expectedAuthor)
    {
        assertEquals(expectedFilename, attachment.getFilename());
        assertEquals(expectedMimeType, attachment.getMimetype());
        assertEquals(expectedAuthor, attachment.getAuthor());
    }

    /**
     * @param comment
     * @param fileNameToType
     * @throws MessagingException
     */
    protected void createMessageWithAttachment(final String comment, final Map<String, String> fileNameToType) throws MessagingException
    {
        //create an multipart message (the body and the attachment)
        final Multipart mp = new MimeMultipart();

        final MimeBodyPart messageBody = new MimeBodyPart();
        messageBody.setContent(comment, TEXT_MIME_TYPE);
        mp.addBodyPart(messageBody);

        for (final String filename : fileNameToType.keySet())
        {
            final MimeBodyPart messageAttachment = new MimeBodyPart();
            // "content" was empty string - needs to be non-empty to pass assertions in sub class tests
            messageAttachment.setContent("content", fileNameToType.get(filename));
            messageAttachment.setFileName(filename);
            mp.addBodyPart(messageAttachment);
        }

        message.setContent(mp);
        message.setHeader("Content-Type", "multipart/mixed");
    }

    private void createNonMultipartMessageWithAttachment(final String type, final String filename) throws MessagingException
    {
        message.setContent(MESSAGE_STRING, type);
        message.setFileName(filename);
        message.setDisposition(Part.ATTACHMENT);
    }
}
