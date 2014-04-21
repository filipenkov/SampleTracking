package com.atlassian.trackback;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A class to send Trackback pings.
 */
public class DefaultTrackbackSender implements TrackbackSender
{
    private static Log log = LogFactory.getLog(DefaultTrackbackSender.class);
    private static final String TRACKBACK_EXCERPT_PARAM = "excerpt";
    private static final String TRACKBACK_BLOG_NAME_PARAM = "blog_name";
    private static final String TRACKBACK_URL_PARAM = "url";
    private static final String TRACKBACK_TITLE_PARAM = "title";

    private static final Map requestHeaderMap;
    static
    {
        final Map tempMap = new HashMap();
        tempMap.put("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
        requestHeaderMap = Collections.unmodifiableMap(tempMap);
    }

    public void sendPing(String pingUrl, Trackback tb) throws IOException
    {
        // Open a connection to the trackback URL and read its input
        HttpClient client = new HttpClient();
        // JRA-7589 - set timeout to abort connections that will not return
        client.setTimeout(TrackbackUtils.HTTPCLIENT_SOCKET_TIMEOUT);
        HttpMethod method = buildPostMethod(pingUrl, tb);
        int statusCode = client.executeMethod(method);

        if (statusCode != 200)
        {
            log.debug("Error writing trackback to " + pingUrl + ": " + statusCode + " response code.");
        }

    }

    /**
     * <p>This is called to build that HTTP request headers for the the track back ping request. For example
     * you could add the "ContentType" header to the HTTP request.</p>
     * <p>It must be a Map of String keys and values and MUST not be null.</p>
     * <p>By default we return a Map of:</p>
     * <ul><li>"Content-Type", "application/x-www-form-urlencoded; charset=utf-8"</li></ul>
     *
     * <p>This will possible help fix some bugs out there such as CONF-703 and JRA-12388</p>
     *
     * @param pingUrl the trackback URL in play
     * @param tb the {@link Trackback} in play
     * @return a Map of HTTP request header values
     */
    public Map buildHttpHeaders(final String pingUrl, final Trackback tb)
    {
        return requestHeaderMap;
    }

    private HttpMethod buildPostMethod(String trackbackPingURL, Trackback tb)
    {
        PostMethod method = new PostMethod(trackbackPingURL);

        // Build the URL parameters for the trackback ping URL
        method.addParameter(TRACKBACK_URL_PARAM, tb.getUrl());

        if (tb.getTitle() != null)
        {
            method.addParameter(TRACKBACK_TITLE_PARAM, tb.getTitle());
        }

        if (tb.getBlogName() != null)
        {
            method.addParameter(TRACKBACK_BLOG_NAME_PARAM, tb.getBlogName());
        }

        if (tb.getExcerpt() != null)
        {
            method.addParameter(TRACKBACK_EXCERPT_PARAM, tb.getExcerpt());
        }

        Map headers = buildHttpHeaders(trackbackPingURL, tb);
        for (Iterator iterator = headers.entrySet().iterator(); iterator.hasNext();)
        {
            Map.Entry entry = (Map.Entry) iterator.next();
            method.addRequestHeader((String) entry.getKey(), (String) entry.getValue());
        }
        return method;
    }

}
