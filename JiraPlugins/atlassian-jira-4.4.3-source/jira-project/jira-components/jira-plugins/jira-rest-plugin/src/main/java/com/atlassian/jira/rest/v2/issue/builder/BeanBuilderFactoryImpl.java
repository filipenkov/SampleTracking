package com.atlassian.jira.rest.v2.issue.builder;

import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.thumbnail.ThumbnailManager;
import com.atlassian.jira.rest.v2.issue.AttachmentBeanBuilder;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;

import java.net.URI;

import static com.atlassian.jira.rest.v2.issue.VelocityRequestContextFactories.getBaseURI;

/**
 * Default implementation for BeanBuilderFactory.
 *
 * @since v4.2
 */
public class BeanBuilderFactoryImpl implements BeanBuilderFactory
{
    /**
     * A VelocityRequestContextFactory.
     */
    private final VelocityRequestContextFactory velocityRequestContextFactory;

    /**
     * A UserManager.
     */
    private final UserManager userManager;

    /**
     * A ThumbnailManager.
     */
    private final ThumbnailManager thumbnailManager;

    /**
     * Creates a new BeanBuilderFactoryImpl.
     *
     * @param velocityRequestContextFactory a VelocityRequestContextFactory
     * @param userManager a UserManager
     * @param thumbnailManager a ThumbnailManager
     */
    public BeanBuilderFactoryImpl(VelocityRequestContextFactory velocityRequestContextFactory, UserManager userManager, ThumbnailManager thumbnailManager)
    {
        this.velocityRequestContextFactory = velocityRequestContextFactory;
        this.userManager = userManager;
        this.thumbnailManager = thumbnailManager;
    }

    /**
     * Returns a new AttachmentBeanBuilder.
     *
     * @param attachment an Attachment
     * @return an AttachmentBeanBuilder
     */
    public AttachmentBeanBuilder attachmentBean(Attachment attachment)
    {
        return new AttachmentBeanBuilder(baseURI(), userManager, thumbnailManager, attachment); 
    }

    private URI baseURI()
    {
        return getBaseURI(velocityRequestContextFactory);
    }
}
