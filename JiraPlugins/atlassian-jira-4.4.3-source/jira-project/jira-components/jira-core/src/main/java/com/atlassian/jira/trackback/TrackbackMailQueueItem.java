/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.trackback;

import com.atlassian.jira.config.properties.SystemPropertyKeys;
import com.atlassian.mail.MailException;
import com.atlassian.mail.queue.AbstractMailQueueItem;
import com.atlassian.trackback.Trackback;
import com.atlassian.trackback.TrackbackHelper;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.List;

/**
 * Trackback ping queue item.
 * A quick hack until we get a queue that isn't mail-specific.
 */
public class TrackbackMailQueueItem extends AbstractMailQueueItem
{
    private static final Logger log = Logger.getLogger(TrackbackMailQueueItem.class);
    private final TrackbackHelper helper;
    private final Trackback trackback;
    private final String content;
    private final List urllinks;

    public TrackbackMailQueueItem(TrackbackHelper helper, Trackback trackback, List urlLinks)
    {
        this.helper = helper;
        this.trackback = trackback;
        this.urllinks = urlLinks;
        this.content = null;
    }

    public TrackbackMailQueueItem(TrackbackHelper helper, Trackback trackback, String content)
    {
        super("Trackback ping analysis for " + trackback.getTitle());
        this.helper = helper;
        this.trackback = trackback;
        this.urllinks = null;
        this.content = content;
    }

    public void send() throws MailException
    {
        incrementSendCount();
        if (!Boolean.getBoolean(SystemPropertyKeys.TRACKBACK_SYSTEM_PROPERTY))
        {
            try
            {
                if (this.urllinks != null)
                {
                    helper.pingTrackbacksInContent(this.urllinks, this.trackback);
                }
                else if (this.content != null)
                {
                    helper.pingTrackbacksInContent(this.content, this.trackback);
                }
            }
            catch (IOException e)
            {
                log.error("Could not process trackbacks", e);
            }
        }
    }

    public String toString()
    {
        return "Trackback ping " + trackback;
    }
}
