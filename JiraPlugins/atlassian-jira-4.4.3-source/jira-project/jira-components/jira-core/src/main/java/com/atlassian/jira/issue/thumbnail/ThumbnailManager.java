/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.thumbnail;

import com.atlassian.core.util.thumbnail.Thumbnail;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.attachment.Attachment;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * Provides key services for small-sized images representing image attachments on issues.
 */
public interface ThumbnailManager
{
    /**
     * The JIRA global thumbnail MIME type.
     */
    public static Thumbnail.MimeType MIME_TYPE = Thumbnail.MimeType.PNG;

    Collection<Thumbnail> getThumbnails(Collection<Attachment> attachments, com.opensymphony.user.User user) throws Exception;

    Collection<Thumbnail> getThumbnails(Collection<Attachment> attachments, User user) throws Exception;

    /**
     * Retrieves {@link com.atlassian.core.util.thumbnail.Thumbnail Thumbnails} for an issue.
     * @deprecated since 4.0 use {@link com.atlassian.jira.issue.thumbnail.ThumbnailManager#getThumbnails(com.atlassian.jira.issue.Issue, User)}
     * @param issue the issue to get the thumnails for.
     * @param user the user on whose behalf the request is made.
     * @return the thumbnails.
     * @throws Exception
     */
    @Deprecated
    Collection<Thumbnail> getThumbnails(GenericValue issue, com.opensymphony.user.User user) throws Exception;

    /**
     * Retrieves {@link com.atlassian.core.util.thumbnail.Thumbnail Thumbnails} for an {@link com.atlassian.jira.issue.Issue}
     * @param issue the issue to get the thumnails for.
     * @param user the user on whose behalf the request is made.
     * @return the thumbnails.
     * @throws Exception
     *
     * @deprecated Call {@link #getThumbnails(com.atlassian.jira.issue.Issue, com.atlassian.crowd.embedded.api.User)} instead. Since v4.3
     */
    Collection<Thumbnail> getThumbnails(Issue issue, com.opensymphony.user.User user) throws Exception;

    /**
     * Retrieves {@link com.atlassian.core.util.thumbnail.Thumbnail Thumbnails} for an {@link com.atlassian.jira.issue.Issue}
     * @param issue the issue to get the thumnails for.
     * @param user the user on whose behalf the request is made.
     * @return the thumbnails.
     * @throws Exception
     */
    Collection<Thumbnail> getThumbnails(Issue issue, User user) throws Exception;

    boolean isThumbnailable(Attachment attachmentGV) throws GenericEntityException;

    /**
     * Checks if there is an "Abstract Window Toolkit" (AWT Toolkit) available.
     * @return true if there is an "Abstract Window Toolkit" (AWT Toolkit) available.
     *
     * @deprecated Call {@link #checkToolkit()} instead. Since v4.3
     */
    boolean checkToolkit(com.opensymphony.user.User user);

    /**
     * Checks if there is an "Abstract Window Toolkit" (AWT Toolkit) available.
     * @return true if there is an "Abstract Window Toolkit" (AWT Toolkit) available.
     */
    boolean checkToolkit();

    /**
     * Returns the Thumbnail that corresponds to an Attachment, or null if the given attachment is not an image.
     *
     * @param attachment an Attachment
     * @return returns a Thumbnail, or null
     */
    @Nullable
    Thumbnail getThumbnail(Attachment attachment);
}
