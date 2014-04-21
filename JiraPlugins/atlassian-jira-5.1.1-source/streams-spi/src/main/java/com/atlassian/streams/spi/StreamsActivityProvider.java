package com.atlassian.streams.spi;

import com.atlassian.streams.api.ActivityRequest;
import com.atlassian.streams.api.StreamsException;
import com.atlassian.streams.api.StreamsFeed;

/**
 * A provider of streams activity
 */
public interface StreamsActivityProvider
{
    /**
     * The rel value for reply-to links
     */
    static final String REPLY_TO_LINK_REL = "http://streams.atlassian.com/syndication/reply-to";

    /**
     * The rel value for icon links
     */
    static final String ICON_LINK_REL = "http://streams.atlassian.com/syndication/icon";

    /**
     * The rel value for cascading style sheets
     */
    static final String CSS_LINK_REL = "http://streams.atlassian.com/syndication/css";

    /**
     * The rel value for repeating a request with an increased timeout
     */
    static final String TIMEDOUT_INCREASE_LINK_REL = "http://streams.atlassian.com/syndication/timedout-increase";

    /**
     * The rel value for repeating a request with the same timeout
     */
    static final String TIMEDOUT_RETRY_LINK_REL = "http://streams.atlassian.com/syndication/timedout-retry";

    /**
     * The rel value for "watch" links
     */
    static final String WATCH_LINK_REL = "http://streams.atlassian.com/syndication/watch";

    /**
     * Get a callable that can create the activity feed for the given request.
     * <p>
     * The task will be cancelled if we no longer care about the result of the query, so it
     * would also be okay to return an empty or incomplete StreamsFeed in this case; the unchecked
     * exception mechanism is provided for convenience.  The important thing is that the task
     * must not continue executing indefinitely after cancellation.
     *
     * @param activityRequest The request
     * @return a callable that creates the activity feed
     */
    CancellableTask<StreamsFeed> getActivityFeed(ActivityRequest activityRequest) throws StreamsException;
}
