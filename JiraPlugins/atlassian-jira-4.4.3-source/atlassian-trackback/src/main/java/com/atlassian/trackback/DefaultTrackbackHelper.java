package com.atlassian.trackback;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.Iterator;
import java.io.IOException;

public class DefaultTrackbackHelper implements TrackbackHelper {
    private final TrackbackFinder finder;
    private final TrackbackSender sender;
    private static Log log = LogFactory.getLog(DefaultTrackbackHelper.class);

    public DefaultTrackbackHelper(TrackbackFinder finder, TrackbackSender sender)
    {
        this.finder = finder;
        this.sender = sender;
    }

    /**
     * @param content html/xhtml content that will be parsed for url links to autodiscover
     * @param ping
     */
    public void pingTrackbacksInContent(String content, Trackback ping)
    {
        List pingUrls = finder.findPingUrls(content);
        sendPings(pingUrls, ping);
    }

    /**
     * An alternate helper method if you already know the list of URLs
     * @param urlLinks links to autodiscover for trackback:ping urls
     * @param ping
     */
    public void pingTrackbacksInContent(List urlLinks, Trackback ping)
    {
        List pingUrls = finder.findPingUrls(urlLinks);
        sendPings(pingUrls, ping);
    }

    private void sendPings(List pingUrls, Trackback ping)
    {
        for (Iterator iterator = pingUrls.iterator(); iterator.hasNext();)
        {
            String pingUrl = (String) iterator.next();
            try
            {
                sender.sendPing(pingUrl, ping);
            }
            catch (IOException e)
            {
                log.debug("IOException trying to send trackback to " + pingUrl);
                e.printStackTrace();
            }
        }
    }
}
