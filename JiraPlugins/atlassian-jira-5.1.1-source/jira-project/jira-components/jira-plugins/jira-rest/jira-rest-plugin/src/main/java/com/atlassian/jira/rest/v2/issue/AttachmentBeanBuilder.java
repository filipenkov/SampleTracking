package com.atlassian.jira.rest.v2.issue;

import com.atlassian.core.util.thumbnail.Thumbnail;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.thumbnail.ThumbnailManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.dbc.Assertions;

import javax.ws.rs.core.UriInfo;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.util.HashMap;

/**
 * Builder for AttachmentBean instances.
 *
 * @since v4.2
 */
public class AttachmentBeanBuilder
{
    /**
     * The attachment for which we want to create an AttachmentBean.
     */
    private final Attachment attachment;

    /**
     * The base URI for JIRA.
     */
    private final URI baseUri;

    /**
     * The context.
     */
    private UriInfo context;

    /**
     * The UserManager.
     */
    private final UserManager userManager;

    /**
     * The ThumbnailManager.
     */
    private final ThumbnailManager thumbnailManager;

    /**
     * Creates a new AttachmentBeanBuilder.
     *
     * @param baseUri a URI containing the JIRA base URI
     * @param userManager a UserManager
     * @param thumbnailManager a ThumbnailManager
     * @param attachment an Attachment
     */
    public AttachmentBeanBuilder(URI baseUri, UserManager userManager, ThumbnailManager thumbnailManager, Attachment attachment)
    {
        this.baseUri = Assertions.notNull("baseURI", baseUri);
        this.userManager = Assertions.notNull("userManager", userManager);
        this.thumbnailManager = Assertions.notNull("thumbnailManager", thumbnailManager);
        this.attachment = Assertions.notNull("attachment", attachment);
    }

    /**
     * Sets the context.
     *
     * @param context a UriInfo
     * @return this
     */
    public AttachmentBeanBuilder context(UriInfo context)
    {
        this.context = context;
        return this;
    }

    /**
     * Builds a new AttachmentBean.
     *
     * @return a new AttachmentBean instance
     * @throws IllegalStateException if any of this builder's properties is not set
     */
    public AttachmentBean build() throws IllegalStateException
    {
        if (attachment == null) { throw new IllegalStateException("attachment not set"); }
        if (context == null) { throw new IllegalStateException("context not set"); }

        try
        {
            URI self = new ResourceUriBuilder().build(context, AttachmentResource.class, attachment.getId().toString());
            String filename = attachment.getFilename();
            UserBean author = new UserBeanBuilder().user(userManager.getUser(attachment.getAuthor())).context(context).buildShort();
            Timestamp created = attachment.getCreated();
            Long size = attachment.getFilesize();
            String mimeType = attachment.getMimetype();
            HashMap<String, Object> properties = new PropertySetAdapter().marshal(attachment.getProperties());

            String encodedFilename = URLEncoder.encode(attachment.getFilename(), "UTF-8");
            String content = String.format("%s/secure/attachment/%s/%s", baseUri, attachment.getId(), encodedFilename);

            Thumbnail thumbnail = thumbnailManager.getThumbnail(attachment);
            String thumbnailURL = getThumbnailURL(thumbnail);
            return new AttachmentBean(self, filename, author, created, size, mimeType, properties, content, thumbnailURL);
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException("Error encoding file name", e);
        }
    }

    private String getThumbnailURL(final Thumbnail thumbnail) throws UnsupportedEncodingException
    {
        final String thumbnailURL;
        if (thumbnail != null)
        {
            String encodedThumbnailFilename = URLEncoder.encode(thumbnail.getFilename(), "UTF-8");
            thumbnailURL = String.format("%s/secure/thumbnail/%s/%s", baseUri, attachment.getId(), encodedThumbnailFilename);
        }
        else
        {
            thumbnailURL = null;
        }
        return thumbnailURL;
    }
}
