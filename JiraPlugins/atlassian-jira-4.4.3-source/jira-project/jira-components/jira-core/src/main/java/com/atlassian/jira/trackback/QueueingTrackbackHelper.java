/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.trackback;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.trackback.Trackback;
import com.atlassian.trackback.TrackbackHelper;

import java.io.IOException;
import java.util.List;

public class QueueingTrackbackHelper implements TrackbackHelper
{
    private final TrackbackHelper backingHelper;

    public QueueingTrackbackHelper(TrackbackHelper backingHelper)
    {
        this.backingHelper = backingHelper;
    }

    public void pingTrackbacksInContent(String content, Trackback ping) throws IOException
    {
        ManagerFactory.getMailQueue().addItem(new TrackbackMailQueueItem(backingHelper, ping, content));
    }

    public void pingTrackbacksInContent(List urlLinks, Trackback ping) throws IOException
    {
        ManagerFactory.getMailQueue().addItem(new TrackbackMailQueueItem(backingHelper, ping, urlLinks));
    }
}
