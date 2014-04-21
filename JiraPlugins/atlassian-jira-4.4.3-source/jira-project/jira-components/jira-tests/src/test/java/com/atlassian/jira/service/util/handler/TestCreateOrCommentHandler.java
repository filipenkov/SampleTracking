package com.atlassian.jira.service.util.handler;

import org.ofbiz.core.entity.GenericEntityException;

import javax.mail.MessagingException;

public class TestCreateOrCommentHandler extends AbstractTestCommentHandler
{
    public TestCreateOrCommentHandler(String s)
    {
        super(s);
    }

    @Override
    protected AbstractMessageHandler createHandler()
    {
        return new CreateOrCommentHandler();
    }

    public void testMailWithPrecedenceBulkHeader()
            throws MessagingException, GenericEntityException
    {
        _testMailWithPrecedenceBulkHeader();
    }

    public void testMailWithIllegalPrecedenceBulkHeader() throws Exception
    {
        _testMailWithIllegalPrecedenceBulkHeader();
    }

    public void testMailWithDeliveryStatusHeader() throws MessagingException, GenericEntityException
    {
        _testMailWithDeliveryStatusHeader();
    }

    public void testMailWithAutoSubmittedHeader() throws MessagingException, GenericEntityException
    {
        _testMailWithAutoSubmittedHeader();
    }

    public void testMailWithCatchEmailMiss() throws Exception
    {
        _testCatchEmailSettings();
    }

    public void testAddCommentOnly() throws MessagingException, GenericEntityException
    {
        _testAddCommentOnly();
    }

    public void testAddCommentOnlyToMovedIssue() throws MessagingException, GenericEntityException
    {
        setupMovedIssue();
        _testAddCommentOnlyToMovedIssue();
    }

    public void testAddCommentAndAttachment() throws MessagingException, GenericEntityException
    {
        _testAddCommentAndAttachment();
    }

    public void testAddAttachmentWithInvalidFilename() throws MessagingException, GenericEntityException
    {
        _testAddMultipleAttachmentsWithInvalidAndValidFilenames();
    }

    public void testAddCommentWithEmptyBodyAndAttachment() throws MessagingException, GenericEntityException
    {
        _testAddCommentWithNonMultipartAttachment();
    }

    public void testAddCommentWithInlineAttachment() throws GenericEntityException, MessagingException
    {
        _testAddCommentWithNonMultipartInline();
    }
}
