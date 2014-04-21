package com.atlassian.gadgets.publisher.internal;

import com.sun.syndication.feed.atom.Feed;

/**
 * Handles maintaining an ATOM feed of published gadgets for syndication to other containers.
 */
public interface GadgetSpecSyndication
{
    /**
     * Returns a feed containing all published gadget specs as entries.
     * 
     * @return feed containing all published gadget specs as entries
     */
    Feed getFeed();
}
