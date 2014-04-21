package com.atlassian.trackback;

import java.io.IOException;
import java.util.List;

public interface TrackbackHelper {

    /** For each URL in some text content, check if there is a trackback URL and if so, ping it. */
    void pingTrackbacksInContent(String content, Trackback ping) throws IOException;    

    /**
     * Use this if you already know the list of urls
     * @param urlLinks list of URL strings
     * @param ping
     * @throws IOException
     */
    void pingTrackbacksInContent(List urlLinks, Trackback ping) throws IOException;
}
