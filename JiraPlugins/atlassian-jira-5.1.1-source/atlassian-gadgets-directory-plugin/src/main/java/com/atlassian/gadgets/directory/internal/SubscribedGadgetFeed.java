package com.atlassian.gadgets.directory.internal;

import java.net.URI;

import static com.google.common.base.Preconditions.checkNotNull;

public class SubscribedGadgetFeed
{
    private final String id;
    private final URI feedUri;

    public SubscribedGadgetFeed(String id, URI feedUri)
    {
        this.id = checkNotNull(id, "id");
        this.feedUri = checkNotNull(feedUri, "feedUri");
    }
    
    public String getId()
    {
        return id;
    }
    
    public URI getUri()
    {
        return feedUri;
    }
    
    @Override
    public String toString()
    {
        return feedUri.toASCIIString();
    }
}
