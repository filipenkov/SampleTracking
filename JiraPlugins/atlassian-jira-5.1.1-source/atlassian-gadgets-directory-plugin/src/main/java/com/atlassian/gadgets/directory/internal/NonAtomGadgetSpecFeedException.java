package com.atlassian.gadgets.directory.internal;

import java.net.URI;

/**
 * Thrown if the feed that we are trying to use as a gadget spec feed is not an Atom feed.
 */
public class NonAtomGadgetSpecFeedException extends RuntimeException
{
    private final URI feedUri;

    public NonAtomGadgetSpecFeedException(URI feedUri)
    {
        this.feedUri = feedUri;
    }
    
    public URI getFeedUri()
    {
        return feedUri;
    }
}
