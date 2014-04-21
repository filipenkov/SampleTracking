package com.atlassian.upm.rest.representations;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import com.atlassian.upm.rest.async.AsynchronousTask;

import static com.google.common.base.Preconditions.checkNotNull;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.MediaType.TEXT_HTML;

/**
 * Serializes entities using the {@link JsonProvider}, if possible, and then wraps the serialized string in
 * a {@code <textarea>} so that it can be properly parsed by clients.  This is useful for browsers when trying to
 * submit multipart form data where the browser MUST submit a form and load a response.  In situations like this,
 * JSON cannot be returned.
 */
@Provider
@Produces(TEXT_HTML)
public class HtmlWrappedJsonProvider implements MessageBodyWriter<Object>
{
    private final JsonProvider jsonProvider;

    public HtmlWrappedJsonProvider(RepresentationFactory representationFactory)
    {
        this.jsonProvider = new JsonProvider(checkNotNull(representationFactory, "representationFactory"));
    }

    public long getSize(Object t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
    {
        return -1;
    }

    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
    {
        return AsynchronousTask.Representation.class.isAssignableFrom(type) || ErrorRepresentation.class.isAssignableFrom(type);
    }

    public void writeTo(Object value, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
        MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
        throws IOException, WebApplicationException
    {
        Writer writer = new OutputStreamWriter(entityStream);
        writer.write("<textarea>");
        writer.flush();
        jsonProvider.writeTo(value, type, genericType, annotations, APPLICATION_JSON_TYPE, httpHeaders, entityStream);
        writer.write("</textarea>");
        writer.flush();
    }
}
