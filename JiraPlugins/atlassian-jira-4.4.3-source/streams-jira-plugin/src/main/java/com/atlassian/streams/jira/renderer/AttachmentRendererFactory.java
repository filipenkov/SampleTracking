package com.atlassian.streams.jira.renderer;

import java.awt.Dimension;
import java.net.URI;

import com.atlassian.core.util.thumbnail.Thumbnail;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.thumbnail.ThumbnailManager;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.streams.api.StreamsEntry;
import com.atlassian.streams.api.StreamsEntry.ActivityObject;
import com.atlassian.streams.api.StreamsEntry.Html;
import com.atlassian.streams.api.StreamsEntry.Renderer;
import com.atlassian.streams.api.UserProfile;
import com.atlassian.streams.api.common.Fold;
import com.atlassian.streams.api.common.Function2;
import com.atlassian.streams.api.common.Option;
import com.atlassian.streams.api.common.Pair;
import com.atlassian.streams.api.renderer.StreamsEntryRendererFactory;
import com.atlassian.streams.jira.JiraActivityItem;
import com.atlassian.streams.jira.JiraHelper;
import com.atlassian.streams.jira.UriProvider;
import com.atlassian.templaterenderer.TemplateRenderer;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.atlassian.streams.api.StreamsEntry.Html.trimHtmlToNone;
import static com.atlassian.streams.api.common.Option.none;
import static com.atlassian.streams.api.common.Option.option;
import static com.atlassian.streams.api.common.Option.some;
import static com.atlassian.streams.api.common.Pair.pair;
import static com.atlassian.streams.jira.util.RenderingUtilities.scaleToThumbnailSize;
import static com.atlassian.streams.spi.renderer.Renderers.render;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.size;
import static com.google.common.collect.Iterables.transform;

public class AttachmentRendererFactory
{
    private static final Logger log = LoggerFactory.getLogger(AttachmentRendererFactory.class);
    private static final Dimension BROKEN_THUMBNAIL_DIMENSION = new Dimension(48, 48);

    private final I18nResolver i18nResolver;
    private final ThumbnailManager thumbnailManager;
    private final StreamsEntryRendererFactory rendererFactory;
    private final IssueActivityObjectRendererFactory issueActivityObjectRendererFactory;
    private final TemplateRenderer templateRenderer;
    private final UriProvider uriProvider;

    private final JiraHelper helper;

    AttachmentRendererFactory(I18nResolver i18nResolver,
              ThumbnailManager thumbnailManager,
              StreamsEntryRendererFactory rendererFactory,
              IssueActivityObjectRendererFactory issueActivityObjectRendererFactory,
              TemplateRenderer templateRenderer,
              UriProvider uriProvider,
              JiraHelper helper)
    {
        this.helper = checkNotNull(helper, "helper");
        this.i18nResolver = checkNotNull(i18nResolver, "i18nResolver");
        this.thumbnailManager = checkNotNull(thumbnailManager, "thumbnailManager");
        this.rendererFactory = checkNotNull(rendererFactory, "rendererFactory");
        this.issueActivityObjectRendererFactory = checkNotNull(issueActivityObjectRendererFactory, "issueActivityObjectRendererFactory");
        this.templateRenderer = checkNotNull(templateRenderer, "templateRenderer");
        this.uriProvider = checkNotNull(uriProvider, "uriProvider");
    }

    public Renderer newAttachmentsEntryRenderer(JiraActivityItem item, Iterable<Attachment> attachments)
    {
        return new AttachmentsEntryRenderer(item, attachments);
    }

    public Function<Iterable<Attachment>, Html> newAttachmentsRenderer(JiraActivityItem item)
    {
        return new AttachmentListRenderer(item, true);
    }

    public Function<Iterable<Attachment>, Html> newAttachmentsRendererWithoutComment(JiraActivityItem item)
    {
        return new AttachmentListRenderer(item, false);
    }

    private final class AttachmentListRenderer implements Function<Iterable<Attachment>, Html>
    {
        private final JiraActivityItem item;
        private final boolean withComment;

        public AttachmentListRenderer(JiraActivityItem item, boolean withComment)
        {
            this.item = checkNotNull(item, "item");
            this.withComment = withComment;
        }

        public Html apply(Iterable<Attachment> attachments)
        {
            Pair<? extends Iterable<Attachment>, ? extends Iterable<Attachment>> init = pair(ImmutableList.<Attachment>of(), ImmutableList.<Attachment>of());
            Pair<? extends Iterable<Attachment>, ? extends Iterable<Attachment>> items = Fold.foldl(attachments, init, splitAttachments());

            return new Html(render(templateRenderer, "attachment-list.vm", ImmutableMap.of(
                    "comment", withComment ? item.getComment().map(helper.renderComment()).flatMap(trimHtmlToNone()) : none(),
                    "attachments", ImmutableList.copyOf(transform(items.first(), asAttachmentItem())),
                    "thumbnails", ImmutableList.copyOf(transform(items.second(), asThumbnailItem())))));
        }

        private Function2<Attachment, Pair<? extends Iterable<Attachment>, ? extends Iterable<Attachment>>, Pair<? extends Iterable<Attachment>, ? extends Iterable<Attachment>>> splitAttachments()
        {
            return splitAttachments;
        }

        private final Function2<Attachment, Pair<? extends Iterable<Attachment>, ? extends Iterable<Attachment>>, Pair<? extends Iterable<Attachment>, ? extends Iterable<Attachment>>> splitAttachments = new Function2<Attachment, Pair<? extends Iterable<Attachment>, ? extends Iterable<Attachment>>, Pair<? extends Iterable<Attachment>, ? extends Iterable<Attachment>>>()
        {
            public Pair<? extends Iterable<Attachment>, ? extends Iterable<Attachment>> apply(Attachment attachment,
                    Pair<? extends Iterable<Attachment>, ? extends Iterable<Attachment>> acc)
            {
                try
                {
                    if (thumbnailManager.isThumbnailable(attachment))
                    {
                        return pair(acc.first(), concat(acc.second(), ImmutableList.of(attachment)));
                    }
                }
                catch (Exception e)
                {
                    // Not much we can do about this
                    log.error("Error getting thumbnail for attachment", e);
                }
                return pair(concat(acc.first(), ImmutableList.of(attachment)), acc.second());
            }
        };
    }

    private final class AttachmentsEntryRenderer implements Renderer
    {
        private final Iterable<Attachment> attachments;
        private final Function<Iterable<UserProfile>, Html> authorsRenderer;
        private final Function<ActivityObject, Option<Html>> targetRenderer;
        private final Function<Iterable<Attachment>, Html> attachmentsRenderer;

        public AttachmentsEntryRenderer(JiraActivityItem item, Iterable<Attachment> attachments)
        {
            this.attachments = attachments;

            attachmentsRenderer = newAttachmentsRenderer(item);
            authorsRenderer = rendererFactory.newAuthorsRenderer();
            targetRenderer = issueActivityObjectRendererFactory.newIssueActivityObjectRendererWithSummary(item.getIssue());
        }

        public Option<Html> renderContentAsHtml(StreamsEntry entry)
        {
            return some(attachments).map(attachmentsRenderer);
        }

        public Option<Html> renderSummaryAsHtml(StreamsEntry entry)
        {
            return none();
        }

        public Html renderTitleAsHtml(StreamsEntry entry)
        {
            return new Html(i18nResolver.getText("streams.item.jira.title.attached",
                    authorsRenderer.apply(entry.getAuthors()),
                    size(attachments),
                    targetRenderer.apply(entry.getTarget().get()).get()));
        }
    }

    public static class AttachmentItem
    {
        private final URI uri;
        private final String filename;

        AttachmentItem(URI uri, String filename)
        {
            this.uri = uri;
            this.filename = filename;
        }

        public URI getUri()
        {
            return uri;
        }

        public String getFilename()
        {
            return filename;
        }
    }

    private Function<Attachment, AttachmentItem> asAttachmentItem()
    {
        return asAttachmentItem;
    }

    private final Function<Attachment, AttachmentItem> asAttachmentItem = new Function<Attachment, AttachmentItem>()
    {
        public AttachmentItem apply(Attachment attachment)
        {
            return new AttachmentItem(uriProvider.getAttachmentUri(attachment), attachment.getFilename());
        }
    };

    public static class ThumbnailItem
    {
        private final URI uri;
        private final URI attachmentUri;
        private final Dimension dim;

        ThumbnailItem(URI uri, URI attachmentUri, Dimension dim)
        {
            this.uri = uri;
            this.attachmentUri = attachmentUri;
            this.dim = dim;
        }

        public URI getUri()
        {
            return uri;
        }

        public URI getAttachmentUri()
        {
            return attachmentUri;
        }

        public int getWidth()
        {
            return (int) dim.getWidth();
        }

        public int getHeight()
        {
            return (int) dim.getHeight();
        }
    }

    private Function<Attachment, ThumbnailItem> asThumbnailItem()
    {
        return asThumbnailItem;
    }

    private final Function<Attachment, ThumbnailItem> asThumbnailItem = new Function<Attachment, ThumbnailItem>()
    {
        public ThumbnailItem apply(Attachment attachment)
        {
            return option(thumbnailManager.getThumbnail(attachment)).
                map(mkThumbnailItem(attachment)).
                getOrElse(brokenThumbnailItem(attachment));
        }
    };

    private Function<Thumbnail, ThumbnailItem> mkThumbnailItem(final Attachment attachment)
    {
        return new Function<Thumbnail, AttachmentRendererFactory.ThumbnailItem>()
        {
            public ThumbnailItem apply(Thumbnail thumbnail)
            {
                if (thumbnail.getFilename() == null)
                {
                    return brokenThumbnailItem(attachment);
                }
                return new ThumbnailItem(
                        uriProvider.getThumbnailUri(thumbnail),
                        uriProvider.getAttachmentUri(attachment),
                        scaleToThumbnailSize(thumbnail.getWidth(), thumbnail.getHeight()));
            }
        };
    }

    private ThumbnailItem brokenThumbnailItem(Attachment attachment)
    {
        return new ThumbnailItem(
                uriProvider.getBrokenThumbnailUri(),
                uriProvider.getAttachmentUri(attachment),
                BROKEN_THUMBNAIL_DIMENSION);
    }
}
