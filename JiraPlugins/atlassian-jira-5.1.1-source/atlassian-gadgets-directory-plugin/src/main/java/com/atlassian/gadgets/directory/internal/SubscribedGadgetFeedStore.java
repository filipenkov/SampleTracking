package com.atlassian.gadgets.directory.internal;

/**
 * Provides persistent storage of the base URIs of external applications whose gadgets appear in the directory.
 */
public interface SubscribedGadgetFeedStore
{
    /**
     * Add the gadget feed to those that are subscribed to.
     * 
     * @param feed feed information to store
     */
    void add(SubscribedGadgetFeed feed);

    /**
     * Returns {@code true} if a feed with the given ID has been subscribed to, {@code false} otherwise.
     * 
     * @param feedId ID of the feed to check for existence
     */
    boolean contains(String feedId);
    
    /**
     * Returns the feed with the given ID, if it exists.  {@code null} otherwise.
     * 
     * @param feedId ID of the feed to retrieve
     * @return feed with the given ID, if it exists, {@code null} otherwise
     */
    SubscribedGadgetFeed get(String feedId);
    
    /**
     * Returns the feeds that have been subscribed to
     *  
     * @return feeds that have been subscribed to
     */
    Iterable<SubscribedGadgetFeed> getAll();

    /**
     * Remove the subscribed feed
     *  
     * @param feedId ID of the feed to remove
     */
    void remove(String feedId);
}
