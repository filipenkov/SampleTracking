package com.atlassian.streams.spi;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This builds type 3 UUID URNs, according to RFC4122, using the URL namespace.  It uses Java's UUID class to do most
 * of the dirty work.  See section 4.3 of RFC4122 for details on the exact format of these URNs.
 * <p/>
 * The URL used to build this URN is the one supplied to the setUrl() method, plus, if a timestamp is specified, the
 * timestamp appended as a parameter called "activityTimestamp".  For example, if the URL supplied is
 * http://example.com/browse/TST-1, the URL in the UUID will be http://example.com/browse/TST-1?activityTimestamp=1293098289323.
 * <p/>
 * The general contract for whether a timestamp is required is if the activity item represents a creation activity, eg a
 * page or issue created, or if only one action can ever happen with that URL, eg a changeset gets committed, then no
 * timestamp is required.  For all other activity, a timestamp is required.
 */
public class StreamsUriBuilder
{
    // See RFC4122 Appendix C.  This is the namespace ID for URL named UUID URNs.
    private static final UUID URL_UUID_NAMESPACE_ID = UUID.fromString("6ba7b811-9dad-11d1-80b4-00c04fd430c8");
    private static final String UUID_URN_NAMESPACE = "urn:uuid:";
    private static final Logger log = LoggerFactory.getLogger(StreamsUriBuilder.class);
    private String url;
    private Date timestamp;

    public StreamsUriBuilder()
    {
        // Do nothing
    }

    public URI getUri()
    {
        checkNotNull(url, "url");
        try
        {
            final ByteArrayOutputStream os = new ByteArrayOutputStream();
            final DataOutputStream dos = new DataOutputStream(os);
            // Write the URL namespace ID
            dos.writeLong(URL_UUID_NAMESPACE_ID.getMostSignificantBits());
            dos.writeLong(URL_UUID_NAMESPACE_ID.getLeastSignificantBits());

            // Construct the URL
            String tmpUrl = url;
            // If there's a timestamp, convert it to a parameter
            if (timestamp != null)
            {
                if (tmpUrl.contains("?"))
                {
                    tmpUrl = tmpUrl + "&activityTimestamp=" + timestamp.getTime();
                }
                else
                {
                    tmpUrl = tmpUrl + "?activityTimestamp=" + timestamp.getTime();

                }
            }
            // Write the URL
            dos.writeUTF(tmpUrl);
            dos.flush();

            // Convert to UUID
            final UUID uuid = UUID.nameUUIDFromBytes(os.toByteArray());
            // Convert to URN
            return URI.create(UUID_URN_NAMESPACE + uuid.toString());
        }
        catch (final IOException ioe)
        {
            // This shouldn't happen, there's no IO
            log.error("Error writing to byte array output stream", ioe);
            return null;
        }
    }

    public StreamsUriBuilder setUrl(final String url)
    {
        this.url = checkNotNull(url, "url");
        return this;
    }

    public StreamsUriBuilder setTimestamp(final Date timestamp)
    {
        this.timestamp = timestamp;
        return this;
    }

}
