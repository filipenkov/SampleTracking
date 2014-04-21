package com.atlassian.plugins.rest.common.multipart.jersey;

import com.atlassian.plugins.rest.common.multipart.MultipartConfig;
import com.atlassian.plugins.rest.common.multipart.MultipartConfigClass;
import com.atlassian.plugins.rest.common.multipart.MultipartForm;
import com.atlassian.plugins.rest.common.multipart.fileupload.CommonsFileUploadMultipartHandler;
import com.sun.jersey.core.provider.AbstractMessageReaderWriterProvider;
import org.apache.commons.fileupload.RequestContext;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

/**
 * Reads a multipart form data object
 *
 * @since 2.4
 */
@Provider
public class MultipartFormMessageBodyReader implements MessageBodyReader<MultipartForm>
{
    public boolean isReadable(final Class<?> type, final Type genericType, final Annotation[] annotations, final MediaType mediaType)
    {
        return type.equals(MultipartForm.class);
    }

    public MultipartForm readFrom(final Class<MultipartForm> type, final Type genericType,
            final Annotation[] annotations, final MediaType mediaType, final MultivaluedMap<String, String> httpHeaders,
            final InputStream entityStream)
            throws IOException, WebApplicationException
    {
        CommonsFileUploadMultipartHandler handler = getMultipartHandler(annotations);
        return handler.getForm(new RequestContext()
        {
            public String getCharacterEncoding()
            {
                return AbstractMessageReaderWriterProvider.getCharset(mediaType).name();
            }

            public String getContentType()
            {
                return mediaType.toString();
            }

            public int getContentLength()
            {
                return -1;
            }

            public InputStream getInputStream() throws IOException
            {
                return entityStream;
            }
        });
    }

    private CommonsFileUploadMultipartHandler getMultipartHandler(Annotation[] annotations)
    {
        for (Annotation annotation : annotations)
        {
            if (annotation instanceof MultipartConfigClass)
            {
                Class<? extends MultipartConfig> configClass = ((MultipartConfigClass) annotation).value();
                try
                {
                    MultipartConfig multipartConfig = configClass.newInstance();
                    return new CommonsFileUploadMultipartHandler(multipartConfig.getMaxFileSize(), multipartConfig.getMaxSize());
                }
                catch (InstantiationException e)
                {
                    throw new RuntimeException(e);
                }
                catch (IllegalAccessException e)
                {
                    throw new RuntimeException(e);
                }
            }
        }
        return new CommonsFileUploadMultipartHandler(-1, -1);
    }
}
