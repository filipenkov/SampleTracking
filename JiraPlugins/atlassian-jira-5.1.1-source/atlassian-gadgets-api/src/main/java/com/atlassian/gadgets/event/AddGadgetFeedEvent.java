package com.atlassian.gadgets.event;

import java.net.URI;

/**
 * This event is fired when a gadget subscription is added by an administrator.  Host applications can listen for
 * this event and add a whitelist entry based on the feed entry to ensure all gadget specs from this location will be
 * allowed to be retrieved.
 *
 * @since v3.0
 */
public class AddGadgetFeedEvent
{
    private final URI feedUri;

    public AddGadgetFeedEvent(final URI feedUri)
    {
        this.feedUri = feedUri;
    }

    public URI getFeedUri()
    {
        return feedUri;
    }
}
