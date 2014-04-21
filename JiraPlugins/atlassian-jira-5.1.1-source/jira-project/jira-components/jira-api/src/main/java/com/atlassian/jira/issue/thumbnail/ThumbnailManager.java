/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.thumbnail;

import com.atlassian.annotations.PublicApi;
import com.atlassian.core.util.thumbnail.Thumbnail;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.attachment.Attachment;
import org.ofbiz.core.entity.GenericEntityException;

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * Provides key services for small-sized images representing image attachments on issues.
 */
@PublicApi
public interface ThumbnailManager
{
    /**
     * The JIRA global thumbnail MIME type.
     */
    public static Thumbnail.MimeType MIME_TYPE = Thumbnail.MimeType.PNG;

    /**
     * Retrieves {@link com.atlassian.core.util.thumbnail.Thumbnail Thumbnails} for an {@link com.atlassian.jira.issue.Issue}
     * @param issue the issue to get the thumnails for.
     * @param user the user on whose behalf the request is made.
     * @return the thumbnails.
     * @throws Exception
     */
    Collection<Thumbnail> getThumbnails(Issue issue, User user) throws Exception;

    boolean isThumbnailable(Attachment attachmentGV) throws GenericEntityException;
    boolean isThumbnailable(Issue issue, Attachment attachmentGV) throws GenericEntityException;

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

    /**
     * Returns the Thumbnail that corresponds to an Attachment, or null if the given attachment is not an image.
     *
     * @param issue the issue for the attachment (passed in for performance reasons)
     * @param attachment an Attachment
     * @return returns a Thumbnail, or null
     */
    Thumbnail getThumbnail(Issue issue, Attachment attachment);
}
