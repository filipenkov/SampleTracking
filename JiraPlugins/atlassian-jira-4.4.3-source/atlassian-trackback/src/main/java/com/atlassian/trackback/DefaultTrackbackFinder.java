package com.atlassian.trackback;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class DefaultTrackbackFinder implements TrackbackFinder
{
    private Log log = LogFactory.getLog(DefaultTrackbackFinder.class);

    /**
     * Find trackback ping URLs by scanning links in HTML content.
     *
     * @param content The HTML content to find links in
     * @return list of trackback:ping urls
     */
    public List findPingUrls(String content)
    {
        List urls = findPingUrls(TrackbackUtils.getHttpLinks(content));
        return filterPingUrls(urls);
    }

    /**
     * By subclassing, you can filter which pings to send.
     */
    protected List filterPingUrls(List urls)
    {
        return urls;
    }

    /**
     * feed in a list of urls to autodiscover
     *
     * @param urlLinks
     * @return list of trackback:ping urls
     */
    public List findPingUrls(Collection urlLinks)
    {
        List result = new ArrayList();

        for (Iterator iterator = urlLinks.iterator(); iterator.hasNext();)
        {
            String hyperlink = (String) iterator.next();
            String remoteContent = null;
            try
            {
                remoteContent = TrackbackUtils.getUrlContent(hyperlink);
            }
            catch (ConnectException ce)
            {
                // When the page is inaccessible and 'hangs' or the server is offline/behind a firewall
                log.info("Unable to connect to '" + hyperlink + "': " + ce, ce);
            }
            catch (UnknownHostException uhe)
            {
                // Host unreachable
                log.debug("Cannot reach host on URL '" + hyperlink + "': " + uhe, uhe);
            }
            catch (IOException e)
            {
                // Regular socket errors
                log.info("Error getting content of URL '" + hyperlink + "': " + e, e);
            }
            catch (RuntimeException re)
            {
                // Odd HTTPClient errors
                log.error("Error getting content of " + hyperlink + ": " + re, re);
            }

            if (remoteContent == null)
            {
                log.debug("No remote content found for url: " + hyperlink);
                continue;
            }

            try
            {
                String pingUrl = TrackbackUtils.getTrackbackUrl(remoteContent, hyperlink);

                if (pingUrl == null)
                {
                    log.debug("No trackback URL found in content.");
                    continue;
                }
                else
                {
                    result.add(pingUrl);
                }
            }
            catch (RuntimeException re)
            {
                // Commons HTTPClient throws RuntimeExceptions for things like invalid URLs
                log.info("Error parsing text for trackback pings", re);
            }
        }

        return result;
    }
}
