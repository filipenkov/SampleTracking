package com.atlassian.jira.issue.fields.rest.json.beans;

import com.atlassian.core.util.FileSize;
import com.atlassian.core.util.thumbnail.Thumbnail;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.thumbnail.ThumbnailManager;
import com.atlassian.jira.user.UserUtils;
import com.atlassian.jira.util.JiraUrlCodec;
import com.google.common.base.Function;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;

import static com.google.common.collect.Collections2.transform;

/**
 * Same as {@link AttachmentJsonBean} but contains rendered data
 *
 * @since v5.0
 */
@JsonIgnoreProperties (ignoreUnknown = true)
public class AttachmentRenderedJsonBean
{
    @JsonProperty
    private String self;

    @JsonProperty
    private String id;

    @JsonProperty
    private String filename;

    @JsonProperty
    private UserJsonBean author;

    @JsonProperty
    private String created;

    @JsonProperty
    private String size;

    @JsonProperty
    private String mimeType;

    @JsonProperty
    private String content;

    @JsonProperty
    private String thumbnail;

    public String getSelf()
    {
        return self;
    }

    public String getId()
    {
        return id;
    }

    public String getFilename()
    {
        return filename;
    }

    public UserJsonBean getAuthor()
    {
        return author;
    }

    public String getCreated()
    {
        return created;
    }

    public String getSize()
    {
        return size;
    }

    public String getMimeType()
    {
        return mimeType;
    }

    public String getContent()
    {
        return content;
    }

    public String getThumbnail()
    {
        return thumbnail;
    }

    public static Collection<AttachmentRenderedJsonBean> shortBeans(final Collection<Attachment> attachments, final JiraBaseUrls urls, final ThumbnailManager thumbnailManager, final DateTimeFormatterFactory dateTimeFormatterFactory)
    {
        return transform(attachments, new Function<Attachment, AttachmentRenderedJsonBean>()
        {
            @Override
            public AttachmentRenderedJsonBean apply(Attachment from)
            {
                return shortBean(from, urls, thumbnailManager, dateTimeFormatterFactory);
            }
        });
    }

    /**
     * @return null if the input is null
     */
    public static AttachmentRenderedJsonBean shortBean(final Attachment attachment, final JiraBaseUrls urls, ThumbnailManager thumbnailManager, DateTimeFormatterFactory dateTimeFormatterFactory)
    {
        if (attachment == null)
        {
            return null;
        }
        final AttachmentRenderedJsonBean bean;
        try
        {
            bean = new AttachmentRenderedJsonBean();
            bean.self = urls.restApi2BaseUrl() + "attachment/" + JiraUrlCodec.encode(attachment.getId().toString());
            bean.id = attachment.getId().toString();
            bean.filename = attachment.getFilename();
            bean.size = FileSize.format(attachment.getFilesize());
            bean.mimeType = attachment.getMimetype();
            User author = UserUtils.getUserEvenWhenUnknown(attachment.getAuthor());
            bean.author = UserJsonBean.shortBean(author, urls);
            bean.content = attachment.getFilename();
            bean.created = attachment.getCreated() == null ? "" : dateTimeFormatterFactory.formatter().forLoggedInUser().format(attachment.getCreated());

            String encodedFilename = URLEncoder.encode(attachment.getFilename(), "UTF-8");
            bean.content = String.format("%s/secure/attachment/%s/%s", urls.baseUrl(), attachment.getId(), encodedFilename);

            Thumbnail thumbnail = thumbnailManager.getThumbnail(attachment.getIssueObject(), attachment);
            if (thumbnail != null)
            {
                bean.thumbnail = getThumbnailURL(urls, thumbnail, attachment.getId());
            }
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException("Error encoding file name", e);
        }

        return bean;
    }

    private static String getThumbnailURL(final JiraBaseUrls urls, final Thumbnail thumbnail, final Long id)
            throws UnsupportedEncodingException
    {
        final String thumbnailURL;
        if (thumbnail != null)
        {
            String encodedThumbnailFilename = URLEncoder.encode(thumbnail.getFilename(), "UTF-8");
            thumbnailURL = String.format("%s/secure/thumbnail/%s/%s", urls.baseUrl(), id, encodedThumbnailFilename);
        }
        else
        {
            thumbnailURL = null;
        }
        return thumbnailURL;
    }
}
