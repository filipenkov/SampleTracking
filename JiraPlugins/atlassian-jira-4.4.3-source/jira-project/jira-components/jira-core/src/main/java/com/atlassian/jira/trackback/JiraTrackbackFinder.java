/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.trackback;

import com.atlassian.trackback.DefaultTrackbackFinder;

import java.util.List;

public class JiraTrackbackFinder extends DefaultTrackbackFinder
{
    private final PingUrlFilterer pingUrlFilterer;

    public JiraTrackbackFinder(PingUrlFilterer pingUrlFilterer)
    {
        this.pingUrlFilterer = pingUrlFilterer;
    }

    protected List filterPingUrls(List urls)
    {
        return pingUrlFilterer.filterPingUrls(urls);
    }
}
