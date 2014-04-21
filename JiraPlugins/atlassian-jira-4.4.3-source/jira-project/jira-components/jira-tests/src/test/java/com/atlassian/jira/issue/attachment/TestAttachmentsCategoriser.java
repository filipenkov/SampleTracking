package com.atlassian.jira.issue.attachment;

import com.atlassian.core.util.thumbnail.Thumbnail;
import com.atlassian.jira.issue.thumbnail.ThumbnailManager;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public class TestAttachmentsCategoriser
{
    @Mock
    Attachment txtAttachment;

    @Mock
    Attachment jpgAttachment;

    @Mock
    Attachment jpgAttachmentNoThumbnail;

    @Mock
    ThumbnailManager thumbnailManager;

    @Mock
    Thumbnail jpgThumbnail;

    @Test
    public void categoriserReturnsAllAttachmentsIncludingThoseWithoutThumbnails() throws Exception
    {
        AttachmentsCategoriser categoriser = new AttachmentsCategoriser(thumbnailManager, new AttachmentsSource());

        List<Attachment> attachmentsWithThumbnails = ImmutableList.of(jpgAttachment);
        List<Attachment> attachmentsWithoutThumbnails = ImmutableList.of(txtAttachment, jpgAttachmentNoThumbnail);
        List<Thumbnail> thumbnails = ImmutableList.of(jpgThumbnail);

        assertThat(categoriser.thumbnailAttachments(), equalTo(attachmentsWithThumbnails));
        assertThat(categoriser.noThumbnailAttachments(), equalTo(attachmentsWithoutThumbnails));
        assertThat(categoriser.thumbnails(), equalTo(thumbnails));
    }

    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);

        when(thumbnailManager.isThumbnailable(txtAttachment)).thenReturn(false);
        when(thumbnailManager.getThumbnail(txtAttachment)).thenReturn(null);

        when(thumbnailManager.isThumbnailable(jpgAttachment)).thenReturn(true);
        when(thumbnailManager.getThumbnail(jpgAttachment)).thenReturn(jpgThumbnail);

        when(thumbnailManager.isThumbnailable(jpgAttachmentNoThumbnail)).thenReturn(true);
        when(thumbnailManager.getThumbnail(jpgAttachmentNoThumbnail)).thenReturn(null);
    }

    private class AttachmentsSource implements AttachmentsCategoriser.Source
    {
        @Override
        public List<Attachment> getAttachments()
        {
            return ImmutableList.of(txtAttachment, jpgAttachment, jpgAttachmentNoThumbnail);
        }
    }
}
