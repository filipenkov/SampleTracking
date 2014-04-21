package com.atlassian.plugins.rest.module.jersey;

import com.atlassian.sal.api.net.Response;
import com.atlassian.sal.api.net.ResponseException;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JerseyResponse implements Response
{
    private final Response delegateResponse;
    private final JerseyEntityHandler jerseyEntityHandler;

    public JerseyResponse(Response delegateResponse, JerseyEntityHandler jerseyEntityHandler)
    {
        this.delegateResponse = delegateResponse;
        this.jerseyEntityHandler = jerseyEntityHandler;
    }

    public <T> T getEntity(final Class<T> entityClass) throws ResponseException
    {
        Map<String, String> headers = getHeaders();
        Map<String, List<String>> headerListMap = new HashMap<String, List<String>>();
        for (final Map.Entry<String, String> entry : headers.entrySet())
        {
            headerListMap.put(entry.getKey(), Collections.singletonList(entry.getValue()));
        }
        String contentType = HeaderHelper.getSingleHeaderValue(headerListMap, HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML);
        InputStream entityStream = getResponseBodyAsStream();
        try
        {
            return jerseyEntityHandler.unmarshall(entityClass, MediaType.valueOf(contentType), entityStream, headerListMap);
        }
        catch (IOException e)
        {
            throw new EntityConversionException(e);
        }
    }

    public int getStatusCode()
    {
        return delegateResponse.getStatusCode();
    }

    public String getResponseBodyAsString()
            throws ResponseException
    {
        return delegateResponse.getResponseBodyAsString();
    }

    public InputStream getResponseBodyAsStream()
            throws ResponseException
    {
        return delegateResponse.getResponseBodyAsStream();
    }

    public String getStatusText()
    {
        return delegateResponse.getStatusText();
    }

    public boolean isSuccessful()
    {
        return delegateResponse.isSuccessful();
    }

    public String getHeader(final String s)
    {
        return delegateResponse.getHeader(s);
    }

    public Map<String, String> getHeaders()
    {
        return delegateResponse.getHeaders();
    }
}
