package com.atlassian.gadgets.directory.internal;

import java.net.URI;

/**
 * Exception thrown if the gadget spec feed cannot be parsed
 */
public class GadgetFeedParsingException extends RuntimeException
{
    private final URI feedUri;
    
    public GadgetFeedParsingException(String message, URI feedUri, Throwable cause)
    {
        super(message, cause);
        this.feedUri = feedUri;
    }
    
    public URI getFeedUri()
    {
        return feedUri;
    }
}
