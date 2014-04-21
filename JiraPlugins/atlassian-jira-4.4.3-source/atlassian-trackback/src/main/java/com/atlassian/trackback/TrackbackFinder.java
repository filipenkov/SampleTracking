package com.atlassian.trackback;

import java.util.List;
import java.util.Collection;

public interface TrackbackFinder
{
    /**
     * Find trackback ping URLs by scanning links in HTML content.
     *
     * @param content The HTML content to find links in
     */
    public List findPingUrls(String content);

    /**
     * Searches for trackback ping urls among the HTML content for the links passed in
     *
     * @param urlLinks list of URL strings
     * @return
     */
    public List findPingUrls(Collection urlLinks);

}
