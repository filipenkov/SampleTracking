/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.trackback;

import com.atlassian.trackback.Trackback;
import com.atlassian.trackback.TrackbackException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;

public interface TrackbackManager
{
    public void storeTrackback(Trackback trackback, GenericValue issue) throws TrackbackException;

    public Collection<Trackback> getTrackbacksForIssue(GenericValue issue);

    public Trackback getTrackback(Long trackbackId);

    public void deleteTrackback(Long id);
}
