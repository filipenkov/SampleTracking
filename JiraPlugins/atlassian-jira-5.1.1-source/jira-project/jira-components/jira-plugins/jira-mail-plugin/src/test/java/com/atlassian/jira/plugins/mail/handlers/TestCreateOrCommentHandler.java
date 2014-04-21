package com.atlassian.jira.plugins.mail.handlers;

import org.junit.Test;
import org.ofbiz.core.entity.GenericEntityException;

import javax.mail.MessagingException;

public class TestCreateOrCommentHandler extends AbstractTestCommentHandler
{

    @Override
    protected AbstractMessageHandler createHandler()
    {
        return new CreateOrCommentHandler();
    }

    @Test
    public void testMailWithPrecedenceBulkHeader()
            throws MessagingException, GenericEntityException
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
}
