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

    @Override
    public Attachment getAttachment(Long id)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List getAttachments(Issue issue)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Attachment> getAttachments(final Issue issue, final Comparator<? super Attachment> comparator)
            throws DataAccessException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChangeItemBean createAttachment(File file, String filename, String contentType, User author, Issue issue, Map<String, Object> attachmentProperties, Date createdTime)
            throws AttachmentException
    {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Attachment createAttachmentCopySourceFile(final File file, final String filename, final String contentType, final String attachmentAuthor, final Issue issue, final Map attachmentProperties, final Date createdTime)
            throws AttachmentException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Attachment createAttachment(GenericValue issue, User author, String mimetype, String filename, Long filesize, Map attachmentProperties, Date createdTime)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChangeItemBean createAttachment(File file, String filename, String contentType, User remoteUser, GenericValue issue, Map attachmentProperties, Date createdTime) throws AttachmentException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChangeItemBean createAttachment(File file, String filename, String contentType, User remoteUser, GenericValue issue) throws AttachmentException
    {
        throw new UnsupportedOperationException();

    }

    @Override
    public ChangeItemBean createAttachment(File file, String filename, String contentType, User remoteUser, Issue issue) throws AttachmentException
    {
        throw new UnsupportedOperationException();

    }

    @Override
    public void deleteAttachment(Attachment attachment) throws RemoveException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteAttachmentDirectory(Issue issue)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean attachmentsEnabled()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isScreenshotAppletEnabled()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isScreenshotAppletSupportedByOS()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ChangeItemBean> convertTemporaryAttachments(final User user, final Issue issue, final List<Long> selectedAttachments, final TemporaryAttachmentsMonitor temporaryAttachmentsMonitor)
            throws AttachmentException
    {
        return null;
    }
}
