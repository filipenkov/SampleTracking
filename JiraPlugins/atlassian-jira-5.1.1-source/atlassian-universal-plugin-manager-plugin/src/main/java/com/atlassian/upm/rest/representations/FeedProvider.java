package com.atlassian.upm.rest.representations;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import com.sun.syndication.feed.WireFeed;
import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.WireFeedInput;
import com.sun.syndication.io.WireFeedOutput;

/**
 * A Jersey {@code MessageBodyWriter} for writing atom feeds to an {@code OutputStream}. This code is
 * copied from the Atlassian Gadgets project (AG)
 */
@Provider
@Consumes({MediaType.APPLICATION_ATOM_XML, MediaType.TEXT_XML})
@Produces({MediaType.APPLICATION_ATOM_XML, MediaType.TEXT_XML})
public class FeedProvider implements MessageBodyWriter<Feed>, MessageBodyReader<Feed>
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
        // a properties file that would fail to work and cause a NPE
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

    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
    {
        return Feed.class.equals(type);
    }

    public Feed readFrom(Class<Feed> type, Type genericType, Annotation[] annotations, MediaType mediaType,
        MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException,
        WebApplicationException
    {
        try
        {
            WireFeedInput input = new WireFeedInput();
            WireFeed wireFeed = input.build(new InputStreamReader(entityStream));
            if (!(wireFeed instanceof Feed))
            {
                throw new IOException("Not an ATOM feed");
            }
            return (Feed) wireFeed;
        }
        catch (FeedException cause)
        {
            IOException effect = new IOException("Error reading ATOM feed");
            effect.initCause(cause);
            throw effect;
        }
    }
}
