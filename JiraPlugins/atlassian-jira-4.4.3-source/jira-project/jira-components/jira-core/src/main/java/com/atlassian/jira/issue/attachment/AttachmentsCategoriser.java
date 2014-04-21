package com.atlassian.jira.issue.attachment;

import com.atlassian.core.util.thumbnail.Thumbnail;
import com.atlassian.jira.issue.thumbnail.ThumbnailManager;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import org.ofbiz.core.entity.GenericEntityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;

/**
 * Helper class for handling attachments on the view issue page. This class sorts attachments into two categories: <ul>
 * <li>attachments that have thumbnails</li> <li>attachments that do not have thumbnails</li> </ul>
 * <p/>
 * This is useful because of the different way in which these two types of attachments are displayed on the view issue
 * page. Note that an attachment may be "thumbnailable" but not have a thumbnail in practice, i.e. because there was an
 * error creating the thumbnail. This class cares not about theoretical thumbnailability, but only about actual
 * presence/absence of a thumbnail.
 *
 * @since v5.0
 */
public class AttachmentsCategoriser
{
    public interface Source
    {
        /**
         * @return a list of Attachment in the desired order.
         */
        List<Attachment> getAttachments();
    }

    /**
     * Logger for this AttachmentsCategoriser instance.
     */
    private static final Logger log = LoggerFactory.getLogger(AttachmentsCategoriser.class);

    /**
     * A Source used to get attachments from.
     */
    @Nonnull
    private final Source attachmentsSource;

    /**
     * A ThumbnailManager.
     */
    @Nonnull
    private final ThumbnailManager thumbnailManager;

    /**
     * Lazily loaded attachment list.
     */
    private List<AttachmentItem> attachmentItems = null;

    /**
     * Creates a new AttachmentsCategoriser
     *
     * @param thumbnailManager a ThumbnailManager
     * @param attachmentsSource the Source used to get Attachments from
     */
    public AttachmentsCategoriser(ThumbnailManager thumbnailManager, Source attachmentsSource)
    {
        this.thumbnailManager = checkNotNull(thumbnailManager);
        this.attachmentsSource = checkNotNull(attachmentsSource);
    }

    public List<Attachment> attachments()
    {
        return transform(attachmentItems(), new AttachmentGetter());
    }

    public List<Attachment> thumbnailAttachments()
    {
        return attachmentsThatHaveThumbnail(true);
    }

    public List<Attachment> noThumbnailAttachments()
    {
        return attachmentsThatHaveThumbnail(false);
    }

    public List<Thumbnail> thumbnails()
    {
        return newArrayList(transform(itemsThatHaveThumbnail(true), new ThumbnailGetter()));
    }

    protected List<Attachment> attachmentsThatHaveThumbnail(boolean hasThumbnail)
    {
        return newArrayList(transform(itemsThatHaveThumbnail(hasThumbnail), new AttachmentGetter()));
    }

    /**
     * Returns the AttachmentItem instances that either have or don't have thumbnails, depending on the hasThumbnail
     * parameter.
     *
     * @param hasThumbnail a boolean indicating whether to return attachments that have thumbnails or attachments that
     * don't have thumbnails
     * @return a Collection of AttachmentItem
     */
    protected Collection<AttachmentItem> itemsThatHaveThumbnail(boolean hasThumbnail)
    {
        return filter(attachmentItems(), new IfHasThumbnail(hasThumbnail));
    }

    /**
     * Returns a list of AttachmentItem containing all the attachments for current issue. This method reads the
     * attachment list from the database the first time it is called.
     *
     * @return a List of AttachmentItem
     */
    protected List<AttachmentItem> attachmentItems()
    {
        if (attachmentItems == null)
        {
            attachmentItems = newArrayList(transform(attachmentsSource.getAttachments(), new AttachmentItemCreator()));
        }

        return attachmentItems;
    }

    static class AttachmentItem
    {
        final boolean hasThumbnail;
        final Attachment attachment;
        final Thumbnail thumbnail;

        AttachmentItem(Attachment attachment, Thumbnail thumbnail)
        {
            this.attachment = attachment;
            this.thumbnail = thumbnail;
            this.hasThumbnail = thumbnail != null;
        }
    }

    static class AttachmentGetter implements Function<AttachmentItem, Attachment>
    {
        @Override
        public Attachment apply(AttachmentItem item)
        {
            return item.attachment;
        }
    }

    static class IfHasThumbnail implements Predicate<AttachmentItem>
    {
        private final boolean hasThumbnail;

        public IfHasThumbnail(boolean hasThumbnail) {this.hasThumbnail = hasThumbnail;}

        @Override
        public boolean apply(AttachmentItem item)
        {
            return item.hasThumbnail == hasThumbnail;
        }
    }

    private static class ThumbnailGetter implements Function<AttachmentItem, Thumbnail>
    {
        @Override
        public Thumbnail apply(AttachmentItem item)
        {
            return item.thumbnail;
        }
    }

    private class AttachmentItemCreator implements Function<Attachment, AttachmentItem>
    {
        @Override
        public AttachmentItem apply(Attachment attachment)
        {
            Thumbnail thumbnail = null;
            try
            {
                if (thumbnailManager.isThumbnailable(attachment))
                {
                    thumbnail = thumbnailManager.getThumbnail(attachment);
                }
            }
            catch (GenericEntityException e)
            {
                log.warn("Failed to get thumbnail for {}", attachment);
            }

            return new AttachmentItem(attachment, thumbnail);
        }
    }
}
