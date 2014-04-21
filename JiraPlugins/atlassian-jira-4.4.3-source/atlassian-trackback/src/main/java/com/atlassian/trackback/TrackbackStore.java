package com.atlassian.trackback;

import javax.servlet.http.HttpServletRequest;

/**
 * An interface to be implemented by different TrackbackStore (ie different storage mechanisms).
 */
public interface TrackbackStore
{
    /**
     * Store a trackback ping.
     *
     * @param tb The trackback ping being received
     * @param request The request being 'pinged' (useful to get out the path info or some way to determine the content being pinged within your system)
     */
    void storeTrackback(final Trackback tb, final HttpServletRequest request) throws TrackbackException;
}
