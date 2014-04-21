/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.mock;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.web.action.issue.TemporaryAttachmentsMonitor;
import com.atlassian.jira.web.util.AttachmentException;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.io.File;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class MockAttachmentManager implements AttachmentManager
{
    public MockAttachmentManager()
    {
    }

    public Attachment getAttachment(Long id)
    {
        throw new UnsupportedOperationException();
    }

    public List getAttachments(GenericValue issue)
    {
        throw new UnsupportedOperationException();
    }

    public List getAttachments(Issue issue)
    {
        throw new UnsupportedOperationException();
    }

    public List<Attachment> getAttachments(final Issue issue, final Comparator<? super Attachment> comparator)
            throws DataAccessException
    {
        throw new UnsupportedOperationException();
    }

    public Attachment createAttachmentCopySourceFile(final File file, final String filename, final String contentType, final String attachmentAuthor, final Issue issue, final Map attachmentProperties, final Date createdTime)
            throws AttachmentException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChangeItemBean createAttachment(File file, String filename, String contentType, com.opensymphony.user.User remoteUser, GenericValue issue, Map attachmentProperties, Date createdTime)
            throws AttachmentException, GenericEntityException
    {
        // Old OSUser Object
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public Attachment createAttachment(GenericValue issue, User author, String mimetype, String filename, Long filesize, Map attachmentProperties, Date createdTime) throws GenericEntityException
    {
        throw new UnsupportedOperationException();
    }

    public ChangeItemBean createAttachment(File file, String filename, String contentType, User remoteUser, GenericValue issue, Map attachmentProperties, Date createdTime) throws AttachmentException, GenericEntityException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChangeItemBean createAttachment(File file, String filename, String contentType, com.opensymphony.user.User remoteUser, GenericValue issue)
            throws AttachmentException, GenericEntityException
    {
        // Old OSUser Object
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public ChangeItemBean createAttachment(File file, String filename, String contentType, User remoteUser, GenericValue issue) throws AttachmentException, GenericEntityException
    {
        throw new UnsupportedOperationException();

    }

    @Override
    public Attachment createAttachment(GenericValue issue, com.opensymphony.user.User author, String mimetype, String filename, Long filesize, Map attachmentProperties, Date createdTime)
            throws GenericEntityException
    {
        // Old OSUser Object
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public void deleteAttachment(Attachment attachment) throws RemoveException
    {
        throw new UnsupportedOperationException();
    }

    public void deleteAttachmentDirectory(GenericValue issue)
    {
        throw new UnsupportedOperationException();
    }

    public void deleteAttachmentDirectory(Issue issue)
    {
        throw new UnsupportedOperationException();
    }

    public boolean attachmentsEnabled()
    {
        throw new UnsupportedOperationException();
    }

    public boolean isScreenshotAppletEnabled()
    {
        throw new UnsupportedOperationException();
    }

    public boolean isScreenshotAppletSupportedByOS()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ChangeItemBean> convertTemporaryAttachments(com.opensymphony.user.User user, Issue issue, List<Long> selectedAttachments, TemporaryAttachmentsMonitor temporaryAttachmentsMonitor)
            throws AttachmentException, GenericEntityException
    {
        // Old OSUser Object
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public List<ChangeItemBean> convertTemporaryAttachments(final User user, final Issue issue, final List<Long> selectedAttachments, final TemporaryAttachmentsMonitor temporaryAttachmentsMonitor)
            throws AttachmentException, GenericEntityException
    {
        return null;
    }
}
