package com.atlassian.jira.plugins.mail;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.AttachmentNotFoundException;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.util.mime.MimeManager;
import com.atlassian.jira.web.action.issue.TemporaryAttachmentsMonitor;
import com.atlassian.jira.web.util.AttachmentException;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Longs;
import net.jcip.annotations.Immutable;
import org.mockito.Mockito;
import org.ofbiz.core.entity.GenericValue;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * TODO: Document this class / interface here
 *
 * @since v5.0
 */
public class MockAttachmentManager implements AttachmentManager
{
    public final List<MockAttachmentInfo> attachmentLog = Lists.newArrayList();
    private final MimeManager mimeManager = new MimeManager(getClass().getResourceAsStream("/mime.types"));

    @Override
    public ChangeItemBean createAttachment(File file, String filename, String contentType, User remoteUser, GenericValue issue)
            throws AttachmentException
    {
        if (filename == null) {
            return null; // that is what com.atlassian.jira.issue.managers.DefaultAttachmentManager would do
        }
        contentType = mimeManager.getSanitisedMimeType(contentType, filename);
        attachmentLog.add(new MockAttachmentInfo(file, filename, contentType, remoteUser, issue));
        return new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "Attachment", null, null, null, filename);
    }

    @Override
    public List<Attachment> getAttachments(final Issue issue) throws DataAccessException
    {
        final Iterable<Attachment> unsorted = Iterables.transform(Iterables.filter(attachmentLog, new Predicate<MockAttachmentInfo>()
        {
            @Override
            public boolean apply(MockAttachmentInfo input)
            {
                return Objects.equal(issue.getId(), input.issue.getLong("id"));

            }
        }), new Function<MockAttachmentInfo, Attachment>()
        {
            @Override
            public Attachment apply(MockAttachmentInfo from)
            {
                MockGenericValue gv = new MockGenericValue("Attachment");
                gv.set("filename", from.filename);
                gv.set("author", from.remoteUser.getName());
                gv.set("mimetype", from.contentType);
                return new Attachment(ComponentAccessor.getIssueManager(), gv);
            }
        });

        return Ordering.natural().onResultOf(new Function<Attachment, Comparable>()
        {
            @Override
            public Comparable apply(Attachment from)
            {
                return from.getFilename();
            }
        }).sortedCopy(unsorted);
    }

    @Override
    public Attachment getAttachment(Long id) throws DataAccessException, AttachmentNotFoundException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Attachment> getAttachments(Issue issue, Comparator<? super Attachment> comparator)
            throws DataAccessException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Attachment createAttachmentCopySourceFile(File file, String filename, String contentType, String attachmentAuthor, Issue issue, Map<String, Object> attachmentProperties, Date createdTime)
            throws AttachmentException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChangeItemBean createAttachment(File file, String filename, String contentType, User author, Issue issue, Map<String, Object> attachmentProperties, Date createdTime)
            throws AttachmentException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChangeItemBean createAttachment(File file, String filename, String contentType, User author, GenericValue issue, Map<String, Object> attachmentProperties, Date createdTime)
            throws AttachmentException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChangeItemBean createAttachment(File file, String filename, String contentType, User author, Issue issue)
            throws AttachmentException
    {
        return createAttachment(file, filename, contentType, author, issue.getGenericValue());
    }


    @Override
    public Attachment createAttachment(GenericValue issue, User author, String mimetype, String filename, Long filesize, Map<String, Object> attachmentProperties, Date createdTime)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteAttachment(Attachment attachment) throws RemoveException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteAttachmentDirectory(Issue issue) throws RemoveException
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
    public List<ChangeItemBean> convertTemporaryAttachments(User user, Issue issue, List<Long> selectedAttachments, TemporaryAttachmentsMonitor temporaryAttachmentsMonitor)
            throws AttachmentException
    {
        throw new UnsupportedOperationException();
    }

    public static class MockAttachmentInfo {
        public final File file;
        public final String filename;
        public final String contentType;
        public final User remoteUser;
        public final GenericValue issue;

        public MockAttachmentInfo(File file, String filename, String contentType, User remoteUser, GenericValue issue)
        {
            this.file = file;
            this.filename = filename;
            this.contentType = contentType;
            this.remoteUser = remoteUser;
            this.issue = issue;
        }
    }
}
