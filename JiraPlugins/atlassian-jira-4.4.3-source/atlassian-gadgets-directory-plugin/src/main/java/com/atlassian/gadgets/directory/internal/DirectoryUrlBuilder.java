package com.atlassian.gadgets.directory.internal;

import com.atlassian.gadgets.directory.spi.ExternalGadgetSpecId;

public interface DirectoryUrlBuilder
{
    /**
     * Returns a URL that can be used to retrieve the list of available gadgets in the directory or to post a new gadget
     * spec URL to the directory.
     *
     * @return URL that can be used to retrieve the list of available gadgets in the directory or to post a new gadget
     *         spec URL to the directory.
     */
    String buildDirectoryResourceUrl();

    /**
     * Returns a URL that can be used to delete a gadget spec file from the Directory
     *
     * @param id the id of the Gadget whose resource you want to build.
     *
     * @return a URL that can be used to delete a gadget spec file from the Directory
     */
    String buildDirectoryGadgetResourceUrl(ExternalGadgetSpecId id);

    /**
     * Returns the URL of the collection of subscribed gadget feeds.
     * 
     * @return URL of the collection of subscribed gadget feeds
     */
    String buildSubscribedGadgetFeedsUrl();
    
    /**
     * Returns a URL that can be used to view or remove a subscribed gadget feed.
     * 
     * @param feedId ID of a subscribed gadget feed
     * @return URL that can be used to view or remove an subscribed gadget feed.
     */
    String buildSubscribedGadgetFeedUrl(String feedId);
}
