package com.atlassian.gadgets.directory.spi;

import java.net.URI;

/**
 * <p>Provides a means for storing, retrieving, and modifying a collection of external gadget spec URIs.
 * Host applications that wish to allow administrators to add or remove gadgets should implement
 * this interface.</p>
 *
 * <p>Collection members are stored internally as {@link ExternalGadgetSpec}s. The implementation must create
 * these members on demand when the {@link #add} method is called.</p>
 *
 * <p>Implementations should consider gadget spec URIs to be equal if they are semantically equivalent;
 * formally, two gadget spec URIs are equal if the following is true:</p>
 *
 * {@code
 *     firstURI.normalize().equals(secondURI.normalize());
 * }
 *
 * <p>Host applications that will not use the directory plugin do not need to implement this interface.</p>
 *
 * @see ExternalGadgetSpec
 * @since 2.0
 */
public interface ExternalGadgetSpecStore
{
    /**
     * Retrieves all {@code ExternalGadgetSpec}s from the data store.  There is no guarantee that these will be
     * sequenced in any particular order.  This method must not return {@code null}; if the store is empty, it
     * must return an {@code Iterable} with no contents.
     *
     * @return all of the {@code ExternalGadgetSpec}s in the store, returned in an unspecified order
     * @throws ExternalGadgetSpecStoreException if there is a problem when retrieving the {@code ExternalGadgetSpec}s
     *                                      from the persistent store
     */
    Iterable<ExternalGadgetSpec> entries();

    /**
     * <p>Adds a gadget spec URI to this store and returns an {@code ExternalGadgetSpec} wrapper for it.  The store must
     * not allow duplicate entries to be stored; if the gadget spec URI is already contained in this store, this
     * method should return the existing {@code ExternalGadgetSpec} for it.</p>
     *
     * <p>The implementation must consider syntactically different but semantically equivalent URIs to be equal for the
     * purposes of duplicate elimination; specifically, the implementation is responsible for calling
     * {@link java.net.URI#normalize()} on {@code gadgetSpecUri} before storing it.</p>
     *
     * @param gadgetSpecUri the gadget spec URI to store.  Must not be {@code null}.
     * @return an {@code ExternalGadgetSpec} object wrapping the specified gadget spec URI
     * @throws ExternalGadgetSpecStoreException if there is a problem when adding the gadget spec URI to the
     *                                      persistent data store
     * @throws NullPointerException         if {@code gadgetSpecUri} is {@code null}
     */
    ExternalGadgetSpec add(URI gadgetSpecUri);

    /**
     * Removes the spec URI corresponding to the specified {@code externalGadgetSpecId} from the persistent data store.
     * If {@code externalGadgetSpecId} does not correspond to any stored gadget spec URI, this method should do nothing
     * but return.
     *
     * @param externalGadgetSpecId the gadget spec ID to be removed from the persistent data store.  Must not be
     *                             {@code null}.
     * @throws ExternalGadgetSpecStoreException thrown if there is a problem when removing the gadget from
     *                                      the persistent data store
     * @throws NullPointerException         if {@code externalGadgetSpecId} is {@code null}
     */
    void remove(ExternalGadgetSpecId externalGadgetSpecId);

    /**
     * Returns {@code true} if the URI is in the store, {@code false} otherwise.
     *
     * @param gadgetSpecUri URI to check is in the store
     * @return {@code true} if the URI is in the store, {@code false} otherwise
     */
    boolean contains(URI gadgetSpecUri);
}
