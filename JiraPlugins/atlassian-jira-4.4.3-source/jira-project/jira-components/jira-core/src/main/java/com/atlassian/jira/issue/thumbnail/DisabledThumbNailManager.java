/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.thumbnail;

import com.atlassian.core.util.thumbnail.Thumbnail;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.attachment.Attachment;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.Collections;

public class DisabledThumbNailManager implements ThumbnailManager
{
    @Override
    public Collection<Thumbnail> getThumbnails(Collection<Attachment> attachments, com.opensymphony.user.User user)
            throws Exception
    {
        return Collections.emptyList();
    }

    @Override
    public Collection<Thumbnail> getThumbnails(Collection<Attachment> attachments, User user) throws Exception
    {
        return Collections.emptyList();
    }

    @Override
    public Collection<Thumbnail> getThumbnails(GenericValue issue, com.opensymphony.user.User user) throws Exception
    {
        return Collections.emptyList();
    }

    @Override
    public Collection<Thumbnail> getThumbnails(Issue issue, com.opensymphony.user.User user) throws Exception
    {
        return Collections.emptyList();
    }

    @Override
    public Collection<Thumbnail> getThumbnails(final Issue issue, final User user) throws Exception
    {
        return Collections.emptyList();
    }

    @Override
    public boolean isThumbnailable(Attachment attachment) throws DataAccessException
    {
        return false;
    }

    @Override
    public boolean checkToolkit(com.opensymphony.user.User user)
    {
        return false;
    }

    @Override
    public boolean checkToolkit()
    {
        return false;
    }

    @Override
    public Thumbnail getThumbnail(Attachment attachment)
    {
        return null;
    }
}

