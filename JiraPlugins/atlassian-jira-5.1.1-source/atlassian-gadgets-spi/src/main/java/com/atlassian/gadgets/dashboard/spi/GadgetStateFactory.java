package com.atlassian.gadgets.dashboard.spi;

import java.net.URI;

import com.atlassian.gadgets.GadgetState;

/**
 * Constructs {@link GadgetState} instances for gadget spec URIs.  Implementations <strong>must</strong> be safe to use
 * concurrently from multiple threads.
 * 
 * @since 2.0
 */
public interface GadgetStateFactory
{
    /**
     * Constructs a new {@code GadgetState} instance for the specified gadget spec URI.  The returned object is expected
     * to always be a new instance, even if it has been called previously with the same gadget spec URI.  The {@link
     * GadgetState#id} field should have a value that is unique to that instance within the application.  The
     * application is free to customize the color of the gadget however it likes, or to use the default value provided
     * by {@link GadgetState.Builder}.
     *
     * @param gadgetSpecUri the URI that the gadget spec file for the new gadget can be retrieved from
     * @return a new {@code GadgetState} instance constructed from the specified spec URI
     */
    GadgetState createGadgetState(URI gadgetSpecUri);
}
