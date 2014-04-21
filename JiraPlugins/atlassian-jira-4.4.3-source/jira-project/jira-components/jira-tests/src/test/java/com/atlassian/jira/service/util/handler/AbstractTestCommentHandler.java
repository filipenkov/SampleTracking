package com.atlassian.jira.service.util.handler;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.changehistory.ChangeHistory;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.local.testutils.UtilsForTestSetup;
import com.atlassian.jira.util.collect.MapBuilder;
import org.apache.commons.collections.map.ListOrderedMap;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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
    protected ChangeHistoryManager changeHistoryManager;
    protected static final String ATTACHMENT_DIRECTORY = System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + "jira-testrun-attachments" + System.currentTimeMillis();

    public AbstractTestCommentHandler(final String s)
    {
        super(s);
    }

    @Override
    protected void setUp() throws Exception
    {
        UtilsForTestSetup.setupJiraHome();
        super.setUp();
        setupHandlerState();
    }

    private void setupHandlerState() throws MessagingException {
        setupIssueAndMessage();
        handler = createHandler();
        // we now use the handler instance in the rest of setup
        handler.setErrorHandler(errorHandler);
        handler.init(EasyMap.build("project", "PRJ", "issuetype", "1", "reporterusername", TESTUSER_USERNAME));
        commentManager = ComponentManager.getComponentInstanceOfType(CommentManager.class);
        changeHistoryManager = ComponentManager.getComponentInstanceOfType(ChangeHistoryManager.class);
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
        assertEquals(false, handler.handleMessage(message));
        assertEquals(HAS_NO_PERMISSION_TO_COMMENT_ON.format(new Object[] { u1.getDisplayName(), pid }), errorHandler.getError());
        //check that the issue's updated date has not changed
        GenericValue latestIssue = ManagerFactory.getIssueManager().getIssue(ISSUE_KEY);
        assertEquals("The issue update date should be the same", latestIssue.getString("updated"), issue.getString("updated"));

        //check that the comment was not created
        List<Comment> comments = commentManager.getCommentsForUser(issueObject, u1);
        assertTrue(comments.isEmpty());
        //check that no change history was created
        List<ChangeHistory> changeHistoryActions = changeHistoryManager.getChangeHistoriesForUser(issueObject, u1);
        assertTrue(changeHistoryActions.isEmpty());

        //reset the error handler
        errorHandler = new MessageErrorHandler();

        //grant comment permission and check its ok
        setupCommentPermission();
        assertEquals(true, handler.handleMessage(message));
        assertNull(errorHandler.getError());
        //check that the issue's updated date is changed
        latestIssue = ManagerFactory.getIssueManager().getIssue(ISSUE_KEY);
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
        changeHistoryActions = changeHistoryManager.getChangeHistoriesForUser(issueObject, u1);
        assertTrue(changeHistoryActions.isEmpty());

        //check that no attachments were added
        final Collection attachments = ManagerFactory.getAttachmentManager().getAttachments(issueObject);
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
        assertEquals(false, handler.handleMessage(message));
        assertEquals(HAS_NO_PERMISSION_TO_COMMENT_ON.format(new Object[] { u1.getDisplayName(), pid2 }), errorHandler.getError());
        //check that the issue's updated date has not changed
        GenericValue latestIssue = ManagerFactory.getIssueManager().getIssue(ISSUE_KEY_MOVED);
        assertEquals(latestIssue.getString("updated"), movedIssue.getString("updated"));

        //check that the comment was not created
        List<Comment> comments = commentManager.getCommentsForUser(movedIssueObject, u1);
        assertTrue(comments.isEmpty());
        //check that only the change history for the move exists
        List<ChangeHistory> changeHistoryActions = changeHistoryManager.getChangeHistoriesForUser(movedIssueObject, u1);
        assertEquals(1, changeHistoryActions.size());

        //reset the error handler
        errorHandler = new MessageErrorHandler();

        //grant comment permission and check its ok
        setupCommentPermission(projectB);
        assertEquals(true, handler.handleMessage(message));
        assertNull(errorHandler.getError());

        //Check to see if comment was created.
        comments = commentManager.getCommentsForUser(movedIssueObject, u1);
        assertFalse(comments.isEmpty());
        assertEquals(1, comments.size());
        //examine the comments body and level
        final Comment comment = comments.iterator().next();
        assertEquals(MESSAGE_STRING, comment.getBody());
        assertEquals(null, comment.getGroupLevel());

        //check that no change history was created
        changeHistoryActions = changeHistoryManager.getChangeHistoriesForUser(movedIssueObject, u1);
        assertEquals(1, changeHistoryActions.size());

        //check that no attachments were added
        final Collection attachments = ManagerFactory.getAttachmentManager().getAttachments(movedIssueObject);
        assertTrue(attachments.isEmpty());

        //check that the issue's updated date is changed
        latestIssue = ManagerFactory.getIssueManager().getIssue(ISSUE_KEY_MOVED);
        assertNotNull(latestIssue.getString("updated"));
        assertFalse(latestIssue.getString("updated").equals(movedIssue.getString("updated")));        
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
        ManagerFactory.getApplicationProperties().setString(APKeys.JIRA_PATH_ATTACHMENTS, ATTACHMENT_DIRECTORY);
        ManagerFactory.getApplicationProperties().setOption(APKeys.JIRA_OPTION_ALLOWATTACHMENTS, true);

        createMessageWithAttachment(MESSAGE_STRING, MapBuilder.<String, String> newBuilder().add(FILENAME_VALID, TEXT_MIME_TYPE).toMap());

        //test that user with no permission cannot add comment
        assertEquals(false, handler.handleMessage(message));
        assertEquals(HAS_NO_PERMISSION_TO_COMMENT_ON.format(new Object[] { u1.getDisplayName(), pid }), errorHandler.getError());

        //check that the issue's updated date has not changed
        GenericValue latestIssue = ManagerFactory.getIssueManager().getIssue(ISSUE_KEY);
        assertEquals(latestIssue.getString("updated"), issue.getString("updated"));

        //check that the comment was not created
        List<Comment> comments = commentManager.getCommentsForUser(issueObject, u1);
        assertTrue(comments.isEmpty());
        //check that no change history was created
        List<ChangeHistory> changeHistoryActions = changeHistoryManager.getChangeHistoriesForUser(issueObject, u1);
        assertTrue(changeHistoryActions.isEmpty());
        //check that no Attachments was created
        List<Attachment> attachments = ManagerFactory.getAttachmentManager().getAttachments(issueObject);
        assertTrue(attachments.isEmpty());

        //reset the error handler
        errorHandler = new MessageErrorHandler();

        //grant comment permission and check its ok
        setupCommentPermission();
        assertEquals(true, handler.handleMessage(message));
        assertNull(errorHandler.getError());

        //check that the issue's updated date is changed
        latestIssue = ManagerFactory.getIssueManager().getIssue(ISSUE_KEY);
        assertNotNull(latestIssue.getString("updated"));
        assertFalse(latestIssue.getString("updated").equals(issue.getString("updated")));

        //Check to see if comment was created.
        comments = commentManager.getCommentsForUser(issueObject, u1);
        assertFalse(comments.isEmpty());
        assertEquals(1, comments.size());

        //examine the comments body and level
        assertEquals(MESSAGE_STRING, comments.get(0).getBody());

        //check that no change history was created
        changeHistoryActions = changeHistoryManager.getChangeHistoriesForUser(issueObject, u1);
        assertFalse(changeHistoryActions.isEmpty());
        assertEquals(1, changeHistoryActions.size());

        //examine the change history, and confirm its correct
        final ChangeHistory changeHistory = changeHistoryActions.iterator().next();
        final List changeItems = changeHistory.getChangeItems();
        assertEquals(1, changeItems.size());
        assertEquals(TESTUSER_USERNAME, changeHistory.getUsername());
        assertEquals(ISSUE_KEY, changeHistory.getIssue().getKey());
        assertChangeItemGV((GenericValue) changeItems.get(0), "Attachment", null, null, "1", FILENAME_VALID);

        //check that the attachments actually added
        attachments = ManagerFactory.getAttachmentManager().getAttachments(issueObject);
        assertEquals(1, attachments.size());
        assertAttachment(attachments.get(0), FILENAME_VALID, TEXT_MIME_TYPE, TESTUSER_USERNAME);
    }

    public void _testAddCommentWithNonMultipartInline() throws MessagingException, GenericEntityException
    {
        ManagerFactory.getApplicationProperties().setString(APKeys.JIRA_PATH_ATTACHMENTS, ATTACHMENT_DIRECTORY);
        ManagerFactory.getApplicationProperties().setOption(APKeys.JIRA_OPTION_ALLOWATTACHMENTS, true);

        createNonMultipartMessageWithAttachment(TEXT_MIME_TYPE, FILENAME_VALID);
        message.setDisposition(Part.INLINE);

        //grant comment permission and check its ok
        setupCommentPermission();
        assertEquals(true, handler.handleMessage(message));

        //check that the attachments actually added
        final Collection attachments = ManagerFactory.getAttachmentManager().getAttachments(issueObject);
        assertTrue(attachments.toString(), attachments.isEmpty());
    }

    public void _testAddCommentWithNonMultipartAttachment() throws MessagingException, GenericEntityException
    {
        ManagerFactory.getApplicationProperties().setString(APKeys.JIRA_PATH_ATTACHMENTS, ATTACHMENT_DIRECTORY);
        ManagerFactory.getApplicationProperties().setOption(APKeys.JIRA_OPTION_ALLOWATTACHMENTS, true);

        createNonMultipartMessageWithAttachment(TEXT_MIME_TYPE, FILENAME_VALID);

        //test that user with no permission cannot add comment
        assertEquals(false, handler.handleMessage(message));
        assertEquals(HAS_NO_PERMISSION_TO_COMMENT_ON.format(new Object[] { u1.getDisplayName(), pid }), errorHandler.getError());

        //check that the issue's updated date has not changed
        GenericValue latestIssue = ManagerFactory.getIssueManager().getIssue(ISSUE_KEY);
        assertEquals(latestIssue.getString("updated"), issue.getString("updated"));

        //check that the comment was not created
        List<Comment> comments = commentManager.getCommentsForUser(issueObject, u1);
        assertTrue(comments.isEmpty());
        //check that no change history was created
        List<ChangeHistory> changeHistoryActions = changeHistoryManager.getChangeHistoriesForUser(issueObject, u1);
        assertTrue(changeHistoryActions.isEmpty());
        //check that no Attachments was created
        List<Attachment> attachments = ManagerFactory.getAttachmentManager().getAttachments(issueObject);
        assertTrue(attachments.isEmpty());

        //reset the error handler
        errorHandler = new MessageErrorHandler();

        //grant comment permission and check its ok
        setupCommentPermission();
        assertEquals(true, handler.handleMessage(message));
        assertNull(errorHandler.getError());

        //check that the issue's updated date is changed
        latestIssue = ManagerFactory.getIssueManager().getIssue(ISSUE_KEY);
        assertIssueHasBeenUpdated(latestIssue, issue);

        //Check to see if comment was created.
        comments = commentManager.getCommentsForUser(issueObject, u1);
        assertFalse(comments.isEmpty());
        assertEquals(1, comments.size());

        //examine the comments body and level
        assertEquals(MESSAGE_STRING, comments.get(0).getBody());

        //check that no change history was created
        changeHistoryActions = changeHistoryManager.getChangeHistoriesForUser(issueObject, u1);
        assertFalse(changeHistoryActions.isEmpty());
        assertEquals(1, changeHistoryActions.size());

        //examine the change history, and confirm its correct
        final ChangeHistory changeHistory = changeHistoryActions.iterator().next();
        final List changeItems = changeHistory.getChangeItems();
        assertEquals(1, changeItems.size());
        assertEquals(TESTUSER_USERNAME, changeHistory.getUsername());
        assertEquals(ISSUE_KEY, changeHistory.getIssue().getKey());
        assertChangeItemGV((GenericValue) changeItems.get(0), "Attachment", null, null, "1", FILENAME_VALID);

        //check that the attachments actually added
        attachments = ManagerFactory.getAttachmentManager().getAttachments(issueObject);
        assertEquals(1, attachments.size());
        assertAttachment(attachments.get(0), FILENAME_VALID, TEXT_MIME_TYPE, TESTUSER_USERNAME);
    }

    protected void assertIssueHasBeenUpdated(GenericValue latestIssue, GenericValue previousIssue) {
        String latestUpdateDate = latestIssue.getString("updated");
        String previousUpdatedDate = previousIssue.getString("updated");
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
        ManagerFactory.getApplicationProperties().setString(APKeys.JIRA_PATH_ATTACHMENTS, ATTACHMENT_DIRECTORY);
        ManagerFactory.getApplicationProperties().setOption(APKeys.JIRA_OPTION_ALLOWATTACHMENTS, true);

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
        assertEquals(true, handler.handleMessage(message));
        assertNull(errorHandler.getError());

        //check that the issue's updated date is changed
        final GenericValue latestIssue = ManagerFactory.getIssueManager().getIssue(ISSUE_KEY);
        assertNotNull(latestIssue.getString("updated"));
        assertFalse(latestIssue.getString("updated").equals(issue.getString("updated")));

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
        final List<ChangeHistory> changeHistoryActions = changeHistoryManager.getChangeHistoriesForUser(issueObject, u1);
        assertFalse(changeHistoryActions.isEmpty());
        assertEquals(1, changeHistoryActions.size());

        //examine the change history, and confirm its correct
        final ChangeHistory changeHistory = changeHistoryActions.get(0);
        final List changeItems = changeHistory.getChangeItems();
        assertEquals(4, changeItems.size());
        assertEquals(TESTUSER_USERNAME, changeHistory.getUsername());
        assertEquals(ISSUE_KEY, changeHistory.getIssue().getKey());
        assertChangeItemGV((GenericValue) changeItems.get(0), "Attachment", null, null, "1", RENAMED_FILENAME_1);
        assertChangeItemGV((GenericValue) changeItems.get(1), "Attachment", null, null, "2", RENAMED_FILENAME_2);
        assertChangeItemGV((GenericValue) changeItems.get(2), "Attachment", null, null, "3", FILENAME_CALLED_RENAMED_10);
        assertChangeItemGV((GenericValue) changeItems.get(3), "Attachment", null, null, "4", RENAMED_FILENAME_11);

        //check that the attachments actually added
        final List<Attachment> attachments = ManagerFactory.getAttachmentManager().getAttachments(issueObject);
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

    protected void assertChangeItemGV(final GenericValue changeGV, final String expectedField, final String oldValue, final String oldString, final String newValue, final String newString)
    {
        assertEquals(expectedField, changeGV.get("field"));
        assertEquals(oldValue, changeGV.get("oldvalue"));
        assertEquals(oldString, changeGV.get("oldstring"));
        assertEquals(newValue, changeGV.get("newvalue"));
        assertEquals(newString, changeGV.get("newstring"));
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
