package com.atlassian.plugins.rest.module.util;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.net.URI;

/**
 * Dummy response solely for URI generation
 */
class GeneratedURIResponse extends Response
{
    private URI uri;

    GeneratedURIResponse(URI uri)
    {
        this.uri = uri;
    }

    public URI getURI()
    {
        return uri;
    }

    public String toString()
    {
        return uri.toString();
    }

    @Override
    public Object getEntity()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getStatus()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public MultivaluedMap<String, Object> getMetadata()
    {
        throw new UnsupportedOperationException();
    }

}
