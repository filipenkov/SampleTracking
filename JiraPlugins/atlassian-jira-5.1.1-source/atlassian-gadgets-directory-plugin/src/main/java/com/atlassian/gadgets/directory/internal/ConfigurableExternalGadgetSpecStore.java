package com.atlassian.gadgets.directory.internal;

import java.net.URI;

import com.atlassian.gadgets.directory.spi.ExternalGadgetSpecId;

/**
 * Store for gadget specs that are added by users after application startup.
 */
public interface ConfigurableExternalGadgetSpecStore
{
    /**
     * Adds the specified gadget spec URI to the store. If the URI is not
     * contained in the store, this method is a noop.
     * @param gadgetSpecUri the URI to add to the store
     */
    void add(URI gadgetSpecUri);

    /**
     * Removes the gadget spec with the specified ID from the store
     *
     * @param gadgetSpecId the id of the gadget to remove
     */
    void remove(ExternalGadgetSpecId gadgetSpecId);
}
