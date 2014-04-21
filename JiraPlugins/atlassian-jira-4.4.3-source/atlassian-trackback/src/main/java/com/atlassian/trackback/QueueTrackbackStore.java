package com.atlassian.trackback;

import sun.misc.Queue;

import javax.servlet.http.HttpServletRequest;
import java.util.Stack;

/** 'Stores' a trackback ping by putting it on a queue for later processing. */
public class QueueTrackbackStore implements TrackbackStore
{
    TrackbackStore backingStore;

    public QueueTrackbackStore(TrackbackStore backingStore)
    {
        this.backingStore = backingStore;
    }

    public void storeTrackback(Trackback tb, HttpServletRequest request) throws TrackbackException
    {
        backingStore.storeTrackback(tb, request);
    }
}
