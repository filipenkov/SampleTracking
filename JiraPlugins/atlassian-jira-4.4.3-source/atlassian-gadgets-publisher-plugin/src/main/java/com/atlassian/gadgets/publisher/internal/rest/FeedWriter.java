package com.atlassian.gadgets.publisher.internal.rest;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.WireFeedOutput;

@Provider
public class FeedWriter implements MessageBodyWriter<Feed>
{
    private static final String ATOM = "atom_1.0";
    private static final String DEFAULT_ENCODING = "UTF-8";

    public long getSize(Feed t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
    {
        return -1;
    }

    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
    {
        return Feed.class.equals(type);
    }

    public void writeTo(
            Feed feed, 
            Class<?> type, 
            Type genericType, 
            Annotation annotations[], 
            MediaType mediaType, 
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException
    {
        Writer writer = new OutputStreamWriter(entityStream, feed.getEncoding() != null ? feed.getEncoding() : DEFAULT_ENCODING);
        if (feed.getFeedType() == null)
        {
            feed.setFeedType(ATOM);
        }
        // we need to change out the thread context class loader because Rome does some ClassLoader based searching for
        // a properties file that would fail to work and cause a NPE - AG-1073
        ClassLoader origLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(Feed.class.getClassLoader());
        try
        {
            WireFeedOutput wireFeedOutput = new WireFeedOutput();
            wireFeedOutput.output(feed, writer);          
            writer.flush();
        }
        catch (FeedException cause)
        {
            IOException effect = new IOException("Error marshalling atom feed");
            effect.initCause(cause);
            throw effect;
        }
        finally
        {
            Thread.currentThread().setContextClassLoader(origLoader);
        }
    }    
}
