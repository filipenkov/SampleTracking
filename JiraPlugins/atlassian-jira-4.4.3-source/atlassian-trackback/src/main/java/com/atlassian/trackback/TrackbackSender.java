package com.atlassian.trackback;

import java.io.IOException;

public interface TrackbackSender
{
    void sendPing(String pingUrl, Trackback tb) throws IOException;
}
