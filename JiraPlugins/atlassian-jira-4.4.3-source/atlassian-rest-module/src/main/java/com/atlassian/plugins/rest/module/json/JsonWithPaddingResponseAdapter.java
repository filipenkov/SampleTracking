package com.atlassian.plugins.rest.module.json;

import static com.atlassian.plugins.rest.common.MediaTypes.*;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseWriter;
import org.apache.commons.lang.Validate;

import static javax.ws.rs.core.HttpHeaders.*;
import java.io.IOException;
import java.io.OutputStream;

class JsonWithPaddingResponseAdapter implements ContainerResponseWriter
{
    private final String callbackFunction;
    private final ContainerResponseWriter containerResponseWriter;

    private OutputStream out;

    public JsonWithPaddingResponseAdapter(String callbackFunction, ContainerResponseWriter containerResponseWriter)
    {
        Validate.notNull(callbackFunction);
        Validate.notNull(containerResponseWriter);
        this.callbackFunction = callbackFunction;
        this.containerResponseWriter = containerResponseWriter;
    }

    public OutputStream writeStatusAndHeaders(long contentLength, ContainerResponse response) throws IOException
    {
        // set the header to application/javascript
        response.getHttpHeaders().putSingle(CONTENT_TYPE, APPLICATION_JAVASCRIPT_TYPE);

        out = containerResponseWriter.writeStatusAndHeaders(-1, response);
        out.write((callbackFunction + "(").getBytes());
        return out;
    }

    public void finish() throws IOException
    {
        out.write(");".getBytes());
    }
}
