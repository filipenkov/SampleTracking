package com.atlassian.jira.issue.fields.rest.json.beans;

import com.atlassian.core.util.thumbnail.Thumbnail;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.thumbnail.ThumbnailManager;
import com.atlassian.jira.rest.Dates;
import com.atlassian.jira.user.UserUtils;
import com.atlassian.jira.util.JiraUrlCodec;
import com.google.common.base.Function;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Date;

import static com.google.common.collect.Collections2.transform;

/**
* @since v5.0
*/
@JsonIgnoreProperties (ignoreUnknown = true)
public class AttachmentJsonBean
{
    @JsonProperty
    private String self;

    @JsonProperty
    private String id;

    @JsonProperty
    private String filename;

    @JsonProperty
    private UserJsonBean author;

    @XmlJavaTypeAdapter (Dates.DateTimeAdapter.class)
    private Date created;

    @JsonProperty
    private long size;

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

    public void setSelf(String self)
    {
        this.self = self;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getFilename()
    {
        return filename;
    }

    public void setFilename(String filename)
    {
        this.filename = filename;
    }

    public UserJsonBean getAuthor()
    {
        return author;
    }

    public void setAuthor(UserJsonBean author)
    {
        this.author = author;
    }

    public Date getCreated()
    {
        return created;
    }

    public void setCreated(Date created)
    {
        this.created = created;
    }

    public long getSize()
    {
        return size;
    }

    public void setSize(long size)
    {
        this.size = size;
    }

    public String getMimeType()
    {
        return mimeType;
    }

    public void setMimeType(String mimeType)
    {
        this.mimeType = mimeType;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public String getThumbnail()
    {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail)
    {
        this.thumbnail = thumbnail;
    }

    public static Collection<AttachmentJsonBean> shortBeans(final Collection<Attachment> attachments, final JiraBaseUrls urls, final ThumbnailManager thumbnailManager)
    {
        return transform(attachments, new Function<Attachment, AttachmentJsonBean>()
        {
            @Override
            public AttachmentJsonBean apply(Attachment from)
            {
                return shortBean(from, urls, thumbnailManager);
            }
        });
    }

    /**
     *
     * @return null if the input is null
     */
    public static AttachmentJsonBean shortBean(final Attachment attachment, final JiraBaseUrls urls, ThumbnailManager thumbnailManager)
    {
        if (attachment == null)
        {
            return null;
        }
        final AttachmentJsonBean bean;
        try
        {
            bean = new AttachmentJsonBean();
            bean.self = urls.restApi2BaseUrl() + "attachment/" + JiraUrlCodec.encode(attachment.getId().toString());
            bean.id = attachment.getId().toString();
            bean.filename = attachment.getFilename();
            bean.size = attachment.getFilesize();
            bean.mimeType = attachment.getMimetype();
            User author = UserUtils.getUserEvenWhenUnknown(attachment.getAuthor());
            bean.author = UserJsonBean.shortBean(author, urls);
            bean.content = attachment.getFilename();
            bean.created = attachment.getCreated();

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

    private static String getThumbnailURL(final JiraBaseUrls urls, final Thumbnail thumbnail, final Long id) throws UnsupportedEncodingException
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
